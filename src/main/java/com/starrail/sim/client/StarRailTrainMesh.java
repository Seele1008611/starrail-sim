package com.starrail.sim.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.client.event.RegisterShadersEvent;
import org.joml.Matrix4f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;

/** Immutable textured mesh shared by the world renderer and miniature item renderer. */
public final class StarRailTrainMesh extends SimplePreparableReloadListener<List<StarRailTrainMesh.Quad>> {
    public static final StarRailTrainMesh INSTANCE = new StarRailTrainMesh();
    private static final ResourceLocation MODEL = new ResourceLocation("starrail_sim", "models/train/star_rail_train_reference.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation("starrail_sim", "textures/block/star_rail_train_reference.png");
    private volatile List<Quad> quads = List.of();
    private VertexBuffer gpuMesh;
    private static ShaderInstance cachedShader;

    record Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {}
    record Quad(Vertex[] vertices, boolean emissive) {}

    private StarRailTrainMesh() {}

    @Override protected List<Quad> prepare(ResourceManager manager, ProfilerFiller profiler) {
        try (var reader = manager.openAsReader(MODEL)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            List<Quad> result = new ArrayList<>();
            for (JsonElement e : root.getAsJsonArray("quads")) {
                JsonObject face = e.getAsJsonObject();
                JsonArray data = face.getAsJsonArray("vertices");
                if (data.size() != 4) throw new IllegalArgumentException("Train face must have four vertices");
                Vertex[] vertices = new Vertex[4];
                for (int i = 0; i < 4; i++) {
                    JsonArray v = data.get(i).getAsJsonArray();
                    vertices[i] = new Vertex(v.get(0).getAsFloat(), v.get(1).getAsFloat(), v.get(2).getAsFloat(),
                            v.get(3).getAsFloat(), v.get(4).getAsFloat(), v.get(5).getAsFloat(),
                            v.get(6).getAsFloat(), v.get(7).getAsFloat());
                }
                result.add(new Quad(vertices, face.get("emissive").getAsBoolean()));
            }
            return List.copyOf(result);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load the Astral Express reference mesh", e);
        }
    }

    public static void registerShader(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    new ResourceLocation("starrail_sim", "train_cached"), DefaultVertexFormat.NEW_ENTITY),
                    shader -> cachedShader = shader);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Unable to load the cached train shader", e);
        }
    }

    @Override protected void apply(List<Quad> data, ResourceManager manager, ProfilerFiller profiler) {
        if (gpuMesh != null) { gpuMesh.close(); gpuMesh = null; }
        quads = data;
    }

    /** Upload once per resource reload; light and normal transforms are draw uniforms. */
    private void ensureGpuMesh() {
        if (gpuMesh != null) return;
        BufferBuilder builder = new BufferBuilder(Math.max(256, quads.size() * 4 * 36));
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
        for (Quad q : quads) for (Vertex v : q.vertices) {
            builder.vertex(v.x, v.y, v.z).color(255, 255, 255, 255).uv(v.u, v.v)
                    .overlayCoords(0, 10).uv2(q.emissive ? LightTexture.FULL_BRIGHT : 0)
                    .normal(v.nx, v.ny, v.nz).endVertex();
        }
        gpuMesh = new VertexBuffer(VertexBuffer.Usage.STATIC);
        gpuMesh.bind();
        try { gpuMesh.upload(builder.end()); } finally { VertexBuffer.unbind(); }
    }

    public void render(PoseStack stack, MultiBufferSource buffers, int light, int overlay) {
        if (cachedShader != null && !quads.isEmpty()) {
            // Immediate GPU drawing must follow previously queued opaque/UI geometry.
            if (buffers instanceof MultiBufferSource.BufferSource source) source.endBatch();
            ensureGpuMesh();
            RenderType type = RenderType.entityCutoutNoCull(TEXTURE);
            type.setupRenderState();
            try {
                cachedShader.safeGetUniform("TrainNormalMat").set(stack.last().normal());
                cachedShader.safeGetUniform("TrainLight").set((float)(light & 65535), (float)(light >>> 16));
                cachedShader.safeGetUniform("TrainOverlay").set((float)(overlay & 65535), (float)(overlay >>> 16));
                gpuMesh.bind();
                gpuMesh.drawWithShader(new Matrix4f(RenderSystem.getModelViewMatrix()).mul(stack.last().pose()),
                        RenderSystem.getProjectionMatrix(), cachedShader);
            } finally {
                VertexBuffer.unbind();
                type.clearRenderState();
            }
            return;
        }
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        PoseStack.Pose pose = stack.last();
        for (Quad quad : quads) {
            int packedLight = quad.emissive ? LightTexture.FULL_BRIGHT : light;
            for (Vertex v : quad.vertices) {
                consumer.vertex(pose.pose(), v.x, v.y, v.z).color(255, 255, 255, 255)
                        .uv(v.u, v.v).overlayCoords(overlay).uv2(packedLight)
                        .normal(pose.normal(), v.nx, v.ny, v.nz).endVertex();
            }
        }
    }
}
