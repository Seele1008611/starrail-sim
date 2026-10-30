package com.starrail.sim.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.VoidrangerKind;
import com.starrail.sim.VoidrangerMob;
import net.minecraft.client.model.EntityModel;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Runtime cuboid renderer for the approved stage-1 Blockbench models. */
public final class VoidrangerModel<T extends VoidrangerMob> extends EntityModel<T> {
    private final VoidrangerKind kind;
    private final ModelData modelData;
    private final Map<String, Rotation> groupRotations = new HashMap<>();
    private final float[] reaverUpperBodyPivot;
    private final Set<String> reaverUpperBodyGroups;
    private boolean reaverUpperBodyRigid;
    private Rotation reaverUpperBodyRotation = Rotation.ZERO;

    public VoidrangerModel(VoidrangerKind kind) {
        this.kind = kind;
        this.modelData = ModelData.load(kind);
        this.reaverUpperBodyPivot = this.modelData.parts.stream()
                .filter(part -> "躯干".equals(part.name))
                .map(part -> part.pivot)
                .findFirst()
                .orElse(new float[]{0.0F, 18.0F, 0.0F});
        this.reaverUpperBodyGroups = this.modelData.parts.stream()
                .filter(part -> part.pivot[1] >= this.reaverUpperBodyPivot[1])
                .map(part -> part.name)
                .collect(Collectors.toUnmodifiableSet());
    }

    public ResourceLocation texture() {
        return new ResourceLocation(StarRailSimMod.MOD_ID,
                "textures/entity/" + this.kind.id() + ".png");
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.groupRotations.clear();
        int attackState = entity.getAttackState();
        boolean actionPose = entity.deathTime > 0 || entity.hurtTime > 0
                || attackState != VoidrangerMob.ATTACK_IDLE;
        float walk = actionPose ? 0.0F : Math.min(1.0F, Math.max(0.0F, limbSwingAmount));
        float stride = (float) Math.sin(limbSwing * 0.67F) * 0.48F * walk;
        float idle = actionPose ? 0.0F : (float) Math.sin(ageInTicks * 0.045F);
        this.reaverUpperBodyRigid = false;
        this.reaverUpperBodyRotation = Rotation.ZERO;

        switch (this.kind) {
            case REAVER -> animateReaverLocomotion(stride, idle, netHeadYaw, headPitch, actionPose);
            case DISTORTER -> animateDistorterLocomotion(stride, idle, netHeadYaw, headPitch);
            case TRAMPLER -> animateTramplerLocomotion(stride, idle, netHeadYaw, headPitch);
            case ELIMINATOR -> animateEliminatorLocomotion(stride, idle, netHeadYaw, headPitch);
        }

        if (entity.deathTime > 0) {
            animateDeath(entity.deathTime / 20.0F);
        } else if (entity.hurtTime > 0) {
            animateHurt(entity.hurtTime / 10.0F);
        } else {
            animateAttack(attackState, entity.getAttackPhaseTicks());
            if (attackState == VoidrangerMob.ATTACK_IDLE && this.attackTime > 0.0F) {
                animateMeleeSwing(this.attackTime);
            }
        }
    }

    private void animateMeleeSwing(float attackTime) {
        float swing = (float) Math.sin(Math.sqrt(Math.max(0.0F, attackTime)) * Math.PI);
        switch (this.kind) {
            case REAVER -> {
                set("躯干", 0.08F * swing, 0.0F, -0.06F * swing);
                set("右臂", -1.05F * swing, 0.0F, -0.22F * swing);
                set("左臂", 0.16F * swing, 0.0F, 0.12F * swing);
                set("左腿", -0.14F * swing, 0.0F, 0.0F);
                set("右腿", 0.14F * swing, 0.0F, 0.0F);
            }
            case DISTORTER -> {
                set("躯干", 0.04F * swing, 0.0F, 0.0F);
                set("右臂", -0.82F * swing, 0.0F, -0.18F * swing);
                set("左臂", 0.12F * swing, 0.0F, 0.12F * swing);
            }
            case TRAMPLER -> {
                set("下身躯干", 0.10F * swing, 0.0F, 0.0F);
                set("前胸与颈部", 0.16F * swing, 0.0F, 0.0F);
                set("左臂", -0.62F * swing, 0.0F, -0.12F * swing);
                set("右臂", -0.62F * swing, 0.0F, 0.12F * swing);
                set("左前腿", 0.12F * swing, 0.0F, 0.0F);
                set("右前腿", 0.12F * swing, 0.0F, 0.0F);
            }
            case ELIMINATOR -> {
                set("躯干", 0.06F * swing, 0.0F, -0.04F * swing);
                set("左臂", -0.58F * swing, 0.0F, -0.16F * swing);
                set("右臂", -0.94F * swing, 0.0F, 0.16F * swing);
                set("重型弩架", -0.08F * swing, 0.0F, 0.0F);
            }
        }
    }

    private void animateReaverLocomotion(float stride, float idle, float yaw, float pitch,
                                         boolean actionPose) {
        set("左腿", stride, 0.0F, 0.0F);
        set("右腿", -stride, 0.0F, 0.0F);
        set("左臂", -stride * 0.6F, 0.0F, 0.0F);
        set("右臂", stride * 0.6F, 0.0F, 0.0F);
        set("躯干", 0.0F, 0.0F, idle * 0.018F);
        if (!actionPose) {
            set("头部与头盔", (float) Math.toRadians(pitch), (float) Math.toRadians(yaw), 0.0F);
        }
    }

    private void animateDistorterLocomotion(float stride, float idle, float yaw, float pitch) {
        set("左腿", stride, 0.0F, 0.0F);
        set("右腿", -stride, 0.0F, 0.0F);
        set("左臂", -stride * 0.35F, 0.0F, 0.0F);
        set("右臂", stride * 0.35F, 0.0F, 0.0F);
        set("躯干", 0.0F, 0.0F, idle * 0.012F);
        set("背部短披片", 0.0F, idle * 0.025F, 0.0F);
        set("头部与冠盔", (float) Math.toRadians(pitch), (float) Math.toRadians(yaw), 0.0F);
    }

    private void animateTramplerLocomotion(float stride, float idle, float yaw, float pitch) {
        set("左前腿", stride, 0.0F, 0.0F);
        set("右前腿", -stride, 0.0F, 0.0F);
        set("左后腿", -stride, 0.0F, 0.0F);
        set("右后腿", stride, 0.0F, 0.0F);
        set("左臂", -stride * 0.28F, 0.0F, 0.0F);
        set("右臂", stride * 0.28F, 0.0F, 0.0F);
        set("下身躯干", 0.0F, 0.0F, idle * 0.012F);
        set("尾部装甲", 0.0F, idle * 0.035F, 0.0F);
        set("头盔与双角", (float) Math.toRadians(pitch) * 0.35F,
                (float) Math.toRadians(yaw), 0.0F);
    }

    private void animateEliminatorLocomotion(float stride, float idle, float yaw, float pitch) {
        set("左腿", stride * 0.75F, 0.0F, 0.0F);
        set("右腿", -stride * 0.75F, 0.0F, 0.0F);
        set("左臂", -stride * 0.22F, 0.0F, 0.0F);
        set("右臂", stride * 0.22F, 0.0F, 0.0F);
        set("躯干", 0.0F, 0.0F, idle * 0.01F);
        set("肩部装甲", 0.0F, 0.0F, idle * 0.008F);
        set("重型弩架", 0.0F, 0.0F, idle * 0.01F);
        set("头部与头盔", (float) Math.toRadians(pitch), (float) Math.toRadians(yaw), 0.0F);
    }

    private void animateAttack(int state, int tick) {
        switch (this.kind) {
            case REAVER -> animateReaverAttack(state, tick);
            case DISTORTER -> animateDistorterAttack(state, tick);
            case TRAMPLER -> animateTramplerAttack(state, tick);
            case ELIMINATOR -> animateEliminatorAttack(state, tick);
        }
    }

    private void animateReaverAttack(int state, int tick) {
        if (state == VoidrangerMob.ATTACK_TELEGRAPH) {
            // One five-tick set, then a held, motionless crouch for the rest of the windup.
            float brace = smooth(tick / 5.0F);
            this.reaverUpperBodyRigid = true;
            this.reaverUpperBodyRotation = new Rotation(-0.34F * brace, 0.0F, -0.06F * brace);
            set("左腿", 0.42F * brace, 0.0F, -0.08F * brace);
            set("右腿", 0.42F * brace, 0.0F, 0.08F * brace);
        } else if (state == VoidrangerMob.ATTACK_EXECUTION) {
            float dash = smooth(tick / 3.0F);
            // Every upper-body outliner root, including future armor groups above the torso pivot,
            // receives this one transform. Do not add local shoulder/head rotations during dash.
            this.reaverUpperBodyRigid = true;
            this.reaverUpperBodyRotation = new Rotation(-0.42F - 0.18F * dash,
                    0.0F, 0.12F - 0.2F * dash);
            set("左腿", -0.36F + 0.16F * dash, 0.0F, 0.0F);
            set("右腿", 0.3F - 0.18F * dash, 0.0F, 0.0F);
        } else if (state == VoidrangerMob.ATTACK_STAGGER) {
            float settle = 1.0F - smooth(tick / 18.0F);
            set("躯干", 0.16F * settle, 0.0F, -0.28F * settle);
            set("头部与头盔", -0.1F * settle, 0.0F, -0.14F * settle);
            set("左臂", 0.55F * settle, 0.0F, -0.42F * settle);
            set("右臂", 0.45F * settle, 0.0F, 0.48F * settle);
            set("左腿", 0.26F * settle, 0.0F, 0.0F);
            set("右腿", -0.16F * settle, 0.0F, 0.0F);
        } else if (state == VoidrangerMob.ATTACK_RECOVERY) {
            float settle = 1.0F - smooth(tick / 8.0F);
            set("躯干", 0.12F * settle, 0.0F, 0.0F);
            set("头部与头盔", -0.035F * settle, 0.0F, 0.0F);
            set("左臂", 0.08F * settle, 0.0F, 0.0F);
            set("右臂", -0.35F * settle, 0.0F, 0.0F);
            set("左腿", -0.08F * settle, 0.0F, 0.0F);
            set("右腿", 0.08F * settle, 0.0F, 0.0F);
        }
    }

    private void animateDistorterAttack(int state, int tick) {
        if (state == VoidrangerMob.ATTACK_TELEGRAPH) {
            float gather = smooth(tick / 22.0F);
            set("躯干", -0.3F * gather, 0.0F, 0.0F);
            set("左臂", -1.18F * gather, 0.0F, -0.32F * gather);
            set("右臂", -1.18F * gather, 0.0F, 0.32F * gather);
            set("头部与冠盔", 0.18F * gather, 0.0F, 0.0F);
            set("背部短披片", 0.12F * gather, 0.0F, 0.0F);
        } else if (state == VoidrangerMob.ATTACK_EXECUTION) {
            float release = tick == 0 ? 1.0F : tick == 1 ? 0.42F : 0.0F;
            set("躯干", 0.22F * release, 0.0F, 0.0F);
            set("左臂", 1.25F * release, 0.0F, -0.5F * release);
            set("右臂", 1.25F * release, 0.0F, 0.5F * release);
            set("头部与冠盔", -0.14F * release, 0.0F, 0.0F);
            set("背部短披片", -0.24F * release, 0.0F, 0.0F);
        } else if (state == VoidrangerMob.ATTACK_RECOVERY) {
            float settle = 1.0F - smooth(tick / 12.0F);
            set("躯干", -0.08F * settle, 0.0F, 0.0F);
            set("头部与冠盔", 0.06F * settle, 0.0F, 0.0F);
            set("左臂", 0.66F * settle, 0.0F, -0.14F * settle);
            set("右臂", 0.66F * settle, 0.0F, 0.14F * settle);
            set("背部短披片", -0.1F * settle, 0.0F, 0.0F);
        }
    }

    private void animateTramplerAttack(int state, int tick) {
        if (state == VoidrangerMob.ATTACK_TELEGRAPH) {
            float crouch = smooth(tick / 22.0F);
            set("下身躯干", -0.16F * crouch, 0.0F, 0.0F);
            set("前胸与颈部", -0.46F * crouch, 0.0F, 0.0F);
            set("头盔与双角", 0.34F * crouch, 0.0F, 0.0F);
            set("左臂", -0.45F * crouch, 0.0F, -0.1F * crouch);
            set("右臂", -0.45F * crouch, 0.0F, 0.1F * crouch);
            set("左前腿", 0.38F * crouch, 0.0F, 0.0F);
            set("右前腿", 0.38F * crouch, 0.0F, 0.0F);
            set("左后腿", -0.1F * crouch, 0.0F, 0.0F);
            set("右后腿", -0.1F * crouch, 0.0F, 0.0F);
            set("尾部装甲", -0.36F * crouch, 0.0F, 0.0F);
        } else if (state == VoidrangerMob.ATTACK_EXECUTION) {
            float slam = tick == 0 ? 1.0F : 0.28F;
            set("下身躯干", 0.34F * slam, 0.0F, 0.0F);
            set("前胸与颈部", 0.68F * slam, 0.0F, 0.0F);
            set("头盔与双角", 0.54F * slam, 0.0F, 0.0F);
            set("左臂", 0.52F * slam, 0.0F, -0.16F * slam);
            set("右臂", 0.52F * slam, 0.0F, 0.16F * slam);
            set("左前腿", -0.22F * slam, 0.0F, 0.0F);
            set("右前腿", -0.22F * slam, 0.0F, 0.0F);
            set("左后腿", 0.18F * slam, 0.0F, 0.0F);
            set("右后腿", 0.18F * slam, 0.0F, 0.0F);
            set("尾部装甲", -0.56F * slam, 0.0F, 0.0F);
        } else if (state == VoidrangerMob.ATTACK_RECOVERY) {
            float settle = 1.0F - smooth(tick / 18.0F);
            set("下身躯干", 0.16F * settle, 0.0F, 0.0F);
            set("前胸与颈部", 0.26F * settle, 0.0F, 0.0F);
            set("头盔与双角", -0.12F * settle, 0.0F, 0.0F);
            set("左前腿", -0.14F * settle, 0.0F, 0.0F);
            set("右前腿", -0.14F * settle, 0.0F, 0.0F);
            set("左后腿", 0.1F * settle, 0.0F, 0.0F);
            set("右后腿", 0.1F * settle, 0.0F, 0.0F);
            set("尾部装甲", -0.2F * settle, 0.0F, 0.0F);
        }
    }

    private void animateEliminatorAttack(int state, int tick) {
        if (state == VoidrangerMob.ATTACK_TELEGRAPH) {
            float aim = smooth(tick / 18.0F);
            set("躯干", -0.18F * aim, 0.0F, 0.05F * aim);
            set("左臂", -1.18F * aim, 0.0F, -0.12F * aim);
            set("右臂", -1.02F * aim, 0.0F, 0.16F * aim);
            set("重型弩架", -0.18F * aim, 0.0F, 0.0F);
            set("肩部装甲", -0.12F * aim, 0.0F, 0.0F);
            set("腰甲与长衣", -0.12F * aim, 0.0F, 0.0F);
            set("头部与头盔", -0.12F * aim, 0.0F, 0.0F);
            set("左腿", 0.22F * aim, 0.0F, -0.06F * aim);
            set("右腿", 0.22F * aim, 0.0F, 0.06F * aim);
        } else if (state == VoidrangerMob.ATTACK_EXECUTION) {
            float recoil = tick == 0 ? 1.0F : tick == 1 ? 0.42F : 0.12F;
            set("躯干", 0.32F * recoil, 0.0F, -0.12F * recoil);
            set("头部与头盔", -0.2F * recoil, 0.0F, 0.0F);
            set("左臂", 0.48F * recoil, 0.0F, -0.16F * recoil);
            set("右臂", 0.62F * recoil, 0.0F, 0.14F * recoil);
            set("重型弩架", 0.38F * recoil, 0.0F, 0.0F);
            set("肩部装甲", 0.18F * recoil, 0.0F, 0.0F);
            set("腰甲与长衣", 0.12F * recoil, 0.0F, 0.0F);
            set("左腿", -0.12F * recoil, 0.0F, 0.0F);
            set("右腿", -0.12F * recoil, 0.0F, 0.0F);
        } else if (state == VoidrangerMob.ATTACK_RECOVERY) {
            float settle = 1.0F - smooth(tick / 12.0F);
            set("躯干", -0.12F * settle, 0.0F, 0.0F);
            set("头部与头盔", -0.08F * settle, 0.0F, 0.0F);
            set("左臂", 0.52F * settle, 0.0F, 0.0F);
            set("右臂", 0.6F * settle, 0.0F, 0.0F);
            set("重型弩架", 0.18F * settle, 0.0F, 0.0F);
            set("肩部装甲", -0.06F * settle, 0.0F, 0.0F);
            set("腰甲与长衣", 0.08F * settle, 0.0F, 0.0F);
        }
    }

    private void animateHurt(float hurtAmount) {
        float recoil = Math.min(1.0F, hurtAmount) * 0.45F;
        switch (this.kind) {
            case REAVER -> {
                set("躯干", -recoil, 0.0F, 0.0F);
                set("左臂", 0.35F * recoil, 0.0F, -0.24F * recoil);
                set("右臂", 0.35F * recoil, 0.0F, 0.24F * recoil);
            }
            case DISTORTER -> {
                set("躯干", -recoil, 0.0F, 0.08F * recoil);
                set("左臂", 0.5F * recoil, 0.0F, -0.18F * recoil);
                set("右臂", 0.15F * recoil, 0.0F, 0.12F * recoil);
                set("背部短披片", 0.12F * recoil, 0.0F, 0.0F);
            }
            case TRAMPLER -> {
                set("前胸与颈部", -recoil, 0.0F, 0.0F);
                set("头盔与双角", -0.5F * recoil, 0.0F, 0.0F);
                set("尾部装甲", 0.0F, 0.2F * recoil, 0.0F);
            }
            case ELIMINATOR -> {
                set("躯干", -recoil, 0.0F, 0.05F * recoil);
                set("肩部装甲", 0.0F, 0.0F, 0.08F * recoil);
                set("左臂", 0.28F * recoil, 0.0F, -0.16F * recoil);
                set("右臂", 0.2F * recoil, 0.0F, 0.12F * recoil);
            }
        }
    }

    private void animateDeath(float progress) {
        float fall = smooth(progress);
        switch (this.kind) {
            case REAVER -> {
                set("躯干", 0.0F, 0.0F, -0.8F * fall);
                set("左臂", 0.5F * fall, 0.0F, -0.3F * fall);
                set("右臂", 0.45F * fall, 0.0F, 0.3F * fall);
                set("头部与头盔", -0.25F * fall, 0.0F, 0.0F);
            }
            case DISTORTER -> {
                set("躯干", 0.0F, 0.0F, 0.7F * fall);
                set("左臂", 0.5F * fall, 0.0F, -0.25F * fall);
                set("右臂", 0.45F * fall, 0.0F, 0.25F * fall);
                set("背部短披片", -0.15F * fall, 0.0F, 0.0F);
            }
            case TRAMPLER -> {
                set("下身躯干", 0.0F, 0.0F, 0.55F * fall);
                set("前胸与颈部", 0.18F * fall, 0.0F, 0.0F);
                set("头盔与双角", 0.32F * fall, 0.0F, 0.0F);
                set("左前腿", 0.45F * fall, 0.0F, 0.0F);
                set("右前腿", 0.45F * fall, 0.0F, 0.0F);
                set("尾部装甲", 0.2F * fall, 0.0F, 0.0F);
            }
            case ELIMINATOR -> {
                set("躯干", 0.0F, 0.0F, 0.72F * fall);
                set("左臂", 0.45F * fall, 0.0F, -0.18F * fall);
                set("右臂", 0.5F * fall, 0.0F, 0.2F * fall);
                set("重型弩架", 0.3F * fall, 0.0F, 0.0F);
                set("腰甲与长衣", 0.14F * fall, 0.0F, 0.0F);
            }
        }
    }

    private static float smooth(float value) {
        float t = Math.max(0.0F, Math.min(1.0F, value));
        return t * t * (3.0F - 2.0F * t);
    }

    private void set(String group, float x, float y, float z) {
        this.groupRotations.put(group, new Rotation(x, y, z));
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        // The approved models use Y-up Blockbench coordinates, while the living model space is inverted.
        poseStack.translate(0.0F, 1.501F, 0.0F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        for (Part part : this.modelData.parts) {
            poseStack.pushPose();
            if (this.kind == VoidrangerKind.REAVER && this.reaverUpperBodyRigid
                    && this.reaverUpperBodyGroups.contains(part.name)) {
                translatePivot(poseStack, this.reaverUpperBodyPivot, 1.0F);
                rotate(poseStack, this.reaverUpperBodyRotation.x,
                        this.reaverUpperBodyRotation.y, this.reaverUpperBodyRotation.z);
                translatePivot(poseStack, this.reaverUpperBodyPivot, -1.0F);
            }
            poseStack.pushPose();
            translatePivot(poseStack, part.pivot, 1.0F);
            Rotation dynamic = this.groupRotations.getOrDefault(part.name, Rotation.ZERO);
            rotate(poseStack, part.rotation[0] + dynamic.x,
                    part.rotation[1] + dynamic.y, part.rotation[2] + dynamic.z);
            translatePivot(poseStack, part.pivot, -1.0F);
            for (Cube cube : part.cubes) {
                renderCube(poseStack, consumer, cube, this.modelData.atlasWidth,
                        this.modelData.atlasHeight, packedLight, packedOverlay, red, green, blue, alpha);
            }
            poseStack.popPose();
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void renderCube(PoseStack stack, VertexConsumer out, Cube cube,
                                   float atlasWidth, float atlasHeight,
                                   int light, int overlay, float red, float green,
                                   float blue, float alpha) {
        stack.pushPose();
        translatePivot(stack, cube.pivot, 1.0F);
        rotateDegrees(stack, cube.rotation[0], cube.rotation[1], cube.rotation[2]);
        translatePivot(stack, cube.pivot, -1.0F);

        Matrix4f pose = stack.last().pose();
        Matrix3f normal = stack.last().normal();
        float x0 = cube.from[0] / 16.0F;
        float y0 = cube.from[1] / 16.0F;
        float z0 = cube.from[2] / 16.0F;
        float x1 = cube.to[0] / 16.0F;
        float y1 = cube.to[1] / 16.0F;
        float z1 = cube.to[2] / 16.0F;
        for (Face face : cube.faces) {
            if (face != null) {
                emitFace(out, pose, normal, face, atlasWidth, atlasHeight,
                        x0, y0, z0, x1, y1, z1, light, overlay, red, green, blue, alpha);
            }
        }
        stack.popPose();
    }

    private static void translatePivot(PoseStack stack, float[] pivot, float sign) {
        stack.translate(sign * pivot[0] / 16.0F,
                sign * pivot[1] / 16.0F, sign * pivot[2] / 16.0F);
    }

    private static void rotateDegrees(PoseStack stack, float x, float y, float z) {
        rotate(stack, (float) Math.toRadians(x), (float) Math.toRadians(y), (float) Math.toRadians(z));
    }

    private static void rotate(PoseStack stack, float x, float y, float z) {
        if (x != 0.0F || y != 0.0F || z != 0.0F) {
            stack.mulPose(new Quaternionf().rotationXYZ(x, y, z));
        }
    }

    private static void emitFace(VertexConsumer out, Matrix4f pose, Matrix3f normal, Face face,
                                 float atlasWidth, float atlasHeight,
                                 float x0, float y0, float z0, float x1, float y1, float z1,
                                 int light, int overlay, float red, float green, float blue,
                                 float alpha) {
        float[][] points;
        float nx;
        float ny;
        float nz;
        switch (face.direction) {
            case "north" -> { points = new float[][]{{x0,y0,z0},{x1,y0,z0},{x1,y1,z0},{x0,y1,z0}}; nx=0; ny=0; nz=-1; }
            case "south" -> { points = new float[][]{{x1,y0,z1},{x0,y0,z1},{x0,y1,z1},{x1,y1,z1}}; nx=0; ny=0; nz=1; }
            case "east" -> { points = new float[][]{{x1,y0,z0},{x1,y0,z1},{x1,y1,z1},{x1,y1,z0}}; nx=1; ny=0; nz=0; }
            case "west" -> { points = new float[][]{{x0,y0,z1},{x0,y0,z0},{x0,y1,z0},{x0,y1,z1}}; nx=-1; ny=0; nz=0; }
            case "up" -> { points = new float[][]{{x0,y1,z1},{x1,y1,z1},{x1,y1,z0},{x0,y1,z0}}; nx=0; ny=1; nz=0; }
            case "down" -> { points = new float[][]{{x0,y0,z0},{x1,y0,z0},{x1,y0,z1},{x0,y0,z1}}; nx=0; ny=-1; nz=0; }
            default -> throw new IllegalStateException("Unknown Blockbench cuboid face " + face.direction);
        }
        float u0 = (face.u0 + face.texture * 16.0F) / atlasWidth;
        float v0 = face.v0 / atlasHeight;
        float u1 = (face.u1 + face.texture * 16.0F) / atlasWidth;
        float v1 = face.v1 / atlasHeight;
        float[][] uv = {{u0,v0},{u1,v0},{u1,v1},{u0,v1}};
        int turns = Math.floorMod(face.rotation / 90, 4);
        for (int turn = 0; turn < turns; turn++) {
            float[] first = uv[0]; uv[0] = uv[1]; uv[1] = uv[2]; uv[2] = uv[3]; uv[3] = first;
        }
        for (int i = 0; i < 4; i++) {
            out.vertex(pose, points[i][0], points[i][1], points[i][2])
                    .color(red, green, blue, alpha)
                    .uv(uv[i][0], uv[i][1])
                    .overlayCoords(overlay)
                    .uv2(light)
                    .normal(normal, nx, ny, nz)
                    .endVertex();
        }
    }

    private record Rotation(float x, float y, float z) {
        private static final Rotation ZERO = new Rotation(0.0F, 0.0F, 0.0F);
    }

    private record Part(String name, float[] pivot, float[] rotation, List<Cube> cubes) {}

    private record Cube(float[] from, float[] to, float[] pivot, float[] rotation, Face[] faces) {}

    private record Face(String direction, float u0, float v0, float u1, float v1,
                        int texture, int rotation) {}

    private static final class ModelData {
        private final float atlasWidth;
        private final float atlasHeight;
        private final List<Part> parts;

        private ModelData(float atlasWidth, float atlasHeight, List<Part> parts) {
            this.atlasWidth = atlasWidth;
            this.atlasHeight = atlasHeight;
            this.parts = parts;
        }

        private static ModelData load(VoidrangerKind kind) {
            String resource = "/assets/" + StarRailSimMod.MOD_ID + "/entity_models/" + kind.id() + ".json";
            try (InputStream stream = VoidrangerModel.class.getResourceAsStream(resource)) {
                if (stream == null) throw new IllegalStateException("Missing runtime model " + resource);
                JsonObject root = JsonParser.parseReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                JsonObject resolution = root.getAsJsonObject("resolution");
                float width = resolution.get("width").getAsFloat();
                float height = resolution.get("height").getAsFloat();
                Map<String, JsonObject> groupById = new HashMap<>();
                for (JsonElement item : arrayOrEmpty(root, "groups")) {
                    if (!item.isJsonObject()) continue;
                    JsonObject group = item.getAsJsonObject();
                    String uuid = stringOrEmpty(group, "uuid");
                    if (!uuid.isEmpty()) groupById.put(uuid, group);
                }
                Map<String, JsonObject> elementById = new HashMap<>();
                for (JsonElement item : arrayOrEmpty(root, "elements")) {
                    if (!item.isJsonObject()) continue;
                    JsonObject element = item.getAsJsonObject();
                    String uuid = stringOrEmpty(element, "uuid");
                    if (!uuid.isEmpty()) elementById.put(uuid, element);
                }
                List<Part> parts = new ArrayList<>();
                for (JsonElement item : arrayOrEmpty(root, "outliner")) {
                    if (!item.isJsonObject()) continue;
                    JsonObject node = item.getAsJsonObject();
                    JsonObject group = groupById.get(stringOrEmpty(node, "uuid"));
                    if (group == null) continue;
                    List<Cube> cubes = new ArrayList<>();
                    collectCubes(arrayOrEmpty(node, "children"), groupById, elementById, cubes);
                    parts.add(new Part(stringOrEmpty(group, "name"),
                            vectorOrZero(group, "origin"), vectorOrZero(group, "rotation"),
                            cubes));
                }
                if (parts.isEmpty()) throw new IllegalStateException("Approved model has no top-level groups: " + kind);
                return new ModelData(width, height, parts);
            } catch (IOException | RuntimeException exception) {
                throw new IllegalStateException("Could not load the approved Blockbench model for " + kind, exception);
            }
        }

        private static void collectCubes(JsonArray children, Map<String, JsonObject> groups,
                                         Map<String, JsonObject> elements, List<Cube> output) {
            for (JsonElement child : children) {
                if (!child.isJsonPrimitive()) continue;
                String id = child.getAsString();
                JsonObject subgroup = groups.get(id);
                if (subgroup != null) {
                    collectCubes(arrayOrEmpty(subgroup, "children"), groups, elements, output);
                    continue;
                }
                JsonObject element = elements.get(id);
                if (element == null || !"cube".equals(stringOrEmpty(element, "type"))) continue;
                JsonArray from = arrayOrEmpty(element, "from");
                JsonArray to = arrayOrEmpty(element, "to");
                if (from.size() < 3 || to.size() < 3) continue;
                float[] pivot = vectorOrZero(element, "origin");
                float[] rotation = vectorOrZero(element, "rotation");
                JsonObject facesData = objectOrEmpty(element, "faces");
                Face[] faces = new Face[6];
                String[] directions = {"north", "east", "south", "west", "up", "down"};
                for (int i = 0; i < directions.length; i++) {
                    JsonElement faceElement = facesData.get(directions[i]);
                    if (faceElement == null || !faceElement.isJsonObject()) continue;
                    JsonObject face = faceElement.getAsJsonObject();
                    JsonArray uv = arrayOrEmpty(face, "uv");
                    int texture = integerOr(face, "texture", -1);
                    if (uv.size() < 4 || texture < 0) continue;
                    int faceRotation = integerOr(face, "rotation", 0);
                    faces[i] = new Face(directions[i], uv.get(0).getAsFloat(), uv.get(1).getAsFloat(),
                            uv.get(2).getAsFloat(), uv.get(3).getAsFloat(),
                            texture, faceRotation);
                }
                output.add(new Cube(vector(from), vector(to), pivot, rotation, faces));
            }
        }

        private static JsonArray arrayOrEmpty(JsonObject object, String key) {
            JsonElement value = object == null ? null : object.get(key);
            return value != null && value.isJsonArray() ? value.getAsJsonArray() : new JsonArray();
        }

        private static JsonObject objectOrEmpty(JsonObject object, String key) {
            JsonElement value = object == null ? null : object.get(key);
            return value != null && value.isJsonObject() ? value.getAsJsonObject() : new JsonObject();
        }

        private static String stringOrEmpty(JsonObject object, String key) {
            JsonElement value = object == null ? null : object.get(key);
            return value != null && value.isJsonPrimitive() ? value.getAsString() : "";
        }

        private static int integerOr(JsonObject object, String key, int fallback) {
            JsonElement value = object == null ? null : object.get(key);
            return value != null && value.isJsonPrimitive() ? value.getAsInt() : fallback;
        }

        private static float[] vectorOrZero(JsonObject object, String key) {
            JsonArray values = arrayOrEmpty(object, key);
            return values.size() >= 3 ? vector(values) : zeroVector();
        }

        private static float[] vector(JsonArray values) {
            return new float[]{values.get(0).getAsFloat(), values.get(1).getAsFloat(),
                    values.get(2).getAsFloat()};
        }

        private static float[] zeroVector() {
            return new float[]{0.0F, 0.0F, 0.0F};
        }
    }
}
