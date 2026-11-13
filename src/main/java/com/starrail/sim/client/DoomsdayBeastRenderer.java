package com.starrail.sim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starrail.sim.DoomsdayBeast;
import com.starrail.sim.StarRailSimMod;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class DoomsdayBeastRenderer extends EntityRenderer<DoomsdayBeast> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(StarRailSimMod.MOD_ID, "textures/entity/doomsday_beast.png");

    public DoomsdayBeastRenderer(EntityRendererProvider.Context context) { super(context); }

    @Override
    public void render(DoomsdayBeast boss, float yaw, float partial, PoseStack stack, MultiBufferSource buffers, int light) {
        stack.pushPose();
        stack.scale(1 / 16F, 1 / 16F, 1 / 16F);
        var out = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        var pose = boss.pose(partial);
        DoomsdayBeastVisuals.capture(boss, pose, partial);
        var normal = new Vector3f();
        boolean wingsPowered = boss.wingsPowered();
        for (var geometry : DoomsdayBeast.rig().geometry) {
            String part = DoomsdayBeast.rig().nodes.get(geometry.node()).name();
            boolean crystal = part.contains("紫") || part.contains("晶片");
            boolean charging = boss.visualAction() == 3 || boss.visualAction() == 4 || boss.visualAction() == 6;
            int partLight = wingsPowered && part.contains("蓝色") && part.contains("翼") || charging && crystal
                    ? net.minecraft.client.renderer.LightTexture.FULL_BRIGHT : light;
            Matrix4f matrix = new Matrix4f(stack.last().pose()).mul(pose[geometry.node()]);
            var normals = matrix.normal(new org.joml.Matrix3f());
            float[] vertices = geometry.vertices();
            int[] indices = geometry.indices();
            for (int i = 0; i < indices.length; i += 3) {
                for (int corner = 0; corner < 4; corner++) {
                    int v = indices[i + Math.min(corner, 2)] * 8;
                    normals.transform(vertices[v + 5], vertices[v + 6], vertices[v + 7], normal).normalize();
                    out.vertex(matrix, vertices[v], vertices[v + 1], vertices[v + 2]).color(255, 255, 255, 255)
                            .uv(vertices[v + 3], vertices[v + 4]).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(partLight)
                            .normal(normal.x, normal.y, normal.z).endVertex();
                }
            }
        }
        stack.popPose();
        super.render(boss, yaw, partial, stack, buffers, light);
    }

    @Override public ResourceLocation getTextureLocation(DoomsdayBeast boss) { return TEXTURE; }
}
