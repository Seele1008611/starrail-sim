package com.starrail.sim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.VoidrangerKind;
import com.starrail.sim.VoidrangerMob;
import net.minecraft.client.model.EntityModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.WeakHashMap;

/** The four approved 1.27 rigs, including meshes, parent bones and native animation timelines. */
public final class VoidrangerModel<T extends VoidrangerMob> extends EntityModel<T> {
    private final VoidrangerKind kind;
    private final BlockbenchRig rig;
    private Matrix4f[] pose;
    private float unitsToBlocks;
    private final Vector3f transformedNormal = new Vector3f();
    private final Map<T, Float> lockedAimPitch = new WeakHashMap<>();

    public VoidrangerModel(VoidrangerKind kind) {
        this.kind = kind;
        String resource = "/assets/"+StarRailSimMod.MOD_ID+"/entity_models/"+kind.id()+".json";
        try (InputStream in = VoidrangerModel.class.getResourceAsStream(resource)) {
            if (in == null) throw new IllegalStateException("Missing rig "+resource);
            this.rig = BlockbenchRig.read(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load approved rig "+kind, e);
        }
        pose = rig.matrices(rig.sample("idle",0,1));
        unitsToBlocks = 1/16f;
    }

    public ResourceLocation texture() {
        return new ResourceLocation(StarRailSimMod.MOD_ID,"textures/entity/"+kind.id()+".png");
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float partial = Mth.clamp(ageInTicks-entity.tickCount,0,1);
        unitsToBlocks = entity.getBbHeight()/rig.height;
        int state = entity.getAttackState();
        boolean action = true;
        String clip;
        float time, weight = 1;
        if (entity.deathTime > 0) {
            clip = "death";
            time = (entity.deathTime+partial)/20f*rig.length(clip);
        } else if (entity.hurtTime > 0) {
            clip = "hurt";
            time = (10-entity.hurtTime+partial)/10f*rig.length(clip);
        } else if (state == VoidrangerMob.ATTACK_STAGGER) {
            clip = "hurt";
            time = Math.min(rig.length(clip),entity.getAttackAnimationPhaseTicks(partial)/20f);
        } else if (state != VoidrangerMob.ATTACK_IDLE) {
            clip = switch (kind) {
                case REAVER -> "dash";
                case DISTORTER -> "mark";
                case TRAMPLER -> "stomp";
                case ELIMINATOR -> "bolt";
            };
            int windup = switch (kind) { case REAVER -> 16; case DISTORTER -> 30; case TRAMPLER -> 28; case ELIMINATOR -> 24; };
            int execution = kind == VoidrangerKind.TRAMPLER ? 2 : 4;
            float ticks = entity.getAttackAnimationPhaseTicks(partial);
            if (state == VoidrangerMob.ATTACK_EXECUTION) ticks += windup;
            else if (state == VoidrangerMob.ATTACK_RECOVERY) ticks += windup+execution;
            time = ticks/20f;
        } else if (this.attackTime > 0) {
            clip = "melee";
            time = this.attackTime*rig.length(clip);
        } else {
            action = false;
            float walk = Mth.clamp(limbSwingAmount*3,0,1);
            if (walk > .03f) {
                clip = "walk";
                time = limbSwing/9.424778f*rig.length(clip);
                weight = walk;
            } else {
                clip = "idle";
                time = ageInTicks/20f;
            }
        }
        float[][] locals = rig.sample(clip,time,weight);
        if (kind == VoidrangerKind.ELIMINATOR && entity.deathTime == 0 && entity.hurtTime == 0
                && (state == VoidrangerMob.ATTACK_TELEGRAPH || state == VoidrangerMob.ATTACK_EXECUTION
                || state == VoidrangerMob.ATTACK_RECOVERY)) {
            if (entity.hasAttackMark()) {
                var mark = entity.getAttackMark();
                double horizontal = Math.hypot(mark.x-entity.getX(),mark.z-entity.getZ());
                float pitch = (float)Math.atan2(mark.y+.1-(entity.getY()+entity.getBbHeight()*.68),horizontal);
                lockedAimPitch.put(entity,Mth.clamp(pitch,-.61f,.61f));
            }
            // Keep the fixed mark's elevation through release; arms and bow share this parent bone.
            rig.aim(locals,"胸腔",lockedAimPitch.getOrDefault(entity,0f),0);
        } else if (state == VoidrangerMob.ATTACK_IDLE) {
            lockedAimPitch.remove(entity);
        }
        if (!action) {
            String head = switch (kind) { case REAVER -> "头盔"; case DISTORTER, TRAMPLER -> "冠盔"; case ELIMINATOR -> "角盔"; };
            float yaw = Mth.clamp(netHeadYaw,-65,65)*(float)Math.PI/180;
            float pitch = Mth.clamp(headPitch,-35,35)*(float)Math.PI/180;
            // Native Y-up space is converted once below; its yaw/pitch signs oppose ModelPart's.
            if (kind == VoidrangerKind.ELIMINATOR) {
                rig.aim(locals,"胸腔",0,-yaw*.3f);
                rig.aim(locals,head,-pitch,-yaw*.7f);
            } else rig.aim(locals,head,-pitch,-yaw);
        }
        pose = rig.matrices(locals);
        MonsterCombatEffects.capture(entity,rig,pose,unitsToBlocks,partial,this.attackTime);
    }

    @Override
    public void renderToBuffer(PoseStack stack, VertexConsumer out, int light, int overlay,
                               float red, float green, float blue, float alpha) {
        stack.pushPose();
        // Cancel LivingEntityRenderer's Y-down model basis and put the native ground at entity feet.
        stack.translate(0,1.501,0);
        stack.scale(-unitsToBlocks,-unitsToBlocks,unitsToBlocks);
        // Center the quadruped's long footprint instead of centering on its humanoid chest.
        if (kind == VoidrangerKind.TRAMPLER) stack.translate(0,0,-10.5);
        for (BlockbenchRig.Geometry mesh : rig.geometry) {
            Matrix4f matrix = new Matrix4f(stack.last().pose()).mul(pose[mesh.node()]);
            Matrix3f normal = matrix.normal(new Matrix3f());
            float[] vertices = mesh.vertices();
            int[] indices = mesh.indices();
            for (int i=0; i<indices.length; i+=3) {
                // The entity RenderType consumes quads; repeat the last triangle vertex.
                for (int corner=0; corner<4; corner++) {
                    int v = indices[i+Math.min(corner,2)]*8;
                    normal.transform(vertices[v+5],vertices[v+6],vertices[v+7],transformedNormal).normalize();
                    out.vertex(matrix,vertices[v],vertices[v+1],vertices[v+2]).color(red,green,blue,alpha)
                            .uv(vertices[v+3],vertices[v+4]).overlayCoords(overlay).uv2(light)
                            .normal(transformedNormal.x,transformedNormal.y,transformedNormal.z).endVertex();
                }
            }
        }
        stack.popPose();
    }
}
