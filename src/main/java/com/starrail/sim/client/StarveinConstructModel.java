package com.starrail.sim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.StarveinConstructGuardian;
import net.minecraft.client.model.EntityModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Approved 1.27 guardian rig shared by ordinary and all path combat profiles. */
public final class StarveinConstructModel extends EntityModel<StarveinConstructGuardian> {
    private final BlockbenchRig rig;
    private Matrix4f[] pose;
    private float unitsToBlocks;
    private final Vector3f transformedNormal = new Vector3f();

    public StarveinConstructModel() {
        String resource = "/assets/starrail_sim/entity_models/starvein_construct_guard.json";
        try (InputStream in = StarveinConstructModel.class.getResourceAsStream(resource)) {
            if (in == null) throw new IllegalStateException("Missing guardian rig");
            rig = BlockbenchRig.read(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load approved guardian rig",e);
        }
        pose = rig.matrices(rig.sample("idle",0,1));
        unitsToBlocks = 1/16f;
    }

    public static ResourceLocation texture() {
        return new ResourceLocation(StarRailSimMod.MOD_ID,"textures/entity/starvein_construct_guard.png");
    }

    @Override
    public void setupAnim(StarveinConstructGuardian guardian, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float partial = Mth.clamp(ageInTicks-guardian.tickCount,0,1);
        unitsToBlocks = guardian.getBbHeight()/rig.height;
        boolean action = true;
        String clip;
        float time, weight = 1;
        int state = guardian.getAttackState();
        if (guardian.deathTime > 0) {
            clip = "death";
            time = (guardian.deathTime+partial)/20f*rig.length(clip);
        } else if (guardian.getStaggerTicks() > 0) {
            clip = "stagger";
            time = guardian.getStaggerAnimationTicks(partial)/20f;
        } else if (state != StarveinConstructGuardian.ATTACK_NONE) {
            clip = state == StarveinConstructGuardian.ATTACK_STAR_TRAIL_SWEEP ? "sweep" : "wave";
            time = guardian.getAttackAnimationTicks(partial)/20f;
        } else if (this.attackTime > 0) {
            // Reserved for an actual ordinary swing event; no new melee AI or damage is added here.
            clip = "melee";
            time = this.attackTime*rig.length(clip);
        } else if (guardian.hurtTime > 0) {
            clip = "hurt";
            time = (10-guardian.hurtTime+partial)/10f*rig.length(clip);
        } else {
            action = false;
            float walk = Mth.clamp(limbSwingAmount*3,0,1);
            if (walk > .03f) {
                clip = "walk";
                time = limbSwing/14.959965f*rig.length(clip);
                weight = walk;
            } else {
                clip = "idle";
                time = ageInTicks/20f;
            }
        }
        float[][] locals = rig.sample(clip,time,weight);
        if (!action) rig.aim(locals,"头盔",-Mth.clamp(headPitch,-30,30)*Mth.DEG_TO_RAD,
                -Mth.clamp(netHeadYaw,-60,60)*Mth.DEG_TO_RAD);
        pose = rig.matrices(locals);
        ExtraMonsterCombatEffects.capture(guardian,rig,pose,unitsToBlocks,partial,this.attackTime);
    }

    @Override
    public void renderToBuffer(PoseStack stack, VertexConsumer out, int light, int overlay,
                               float red, float green, float blue, float alpha) {
        stack.pushPose();
        stack.translate(0,1.501,0);
        stack.scale(-unitsToBlocks,-unitsToBlocks,unitsToBlocks);
        for (BlockbenchRig.Geometry mesh : rig.geometry) {
            Matrix4f matrix = new Matrix4f(stack.last().pose()).mul(pose[mesh.node()]);
            Matrix3f normal = matrix.normal(new Matrix3f());
            float[] vertices = mesh.vertices();
            int[] indices = mesh.indices();
            for (int i=0; i<indices.length; i+=3) {
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
