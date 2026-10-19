package com.starrail.sim.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Shared raised armor geometry for the new Path material tiers. */
@OnlyIn(Dist.CLIENT)
public final class PathArmorModels {
    private static final HumanoidModel<LivingEntity> OUTER = create(false);
    private static final HumanoidModel<LivingEntity> INNER = create(true);

    private PathArmorModels() {
    }

    public static HumanoidModel<?> forSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.LEGS ? INNER : OUTER;
    }

    private static HumanoidModel<LivingEntity> create(boolean leggingsLayer) {
        MeshDefinition mesh = HumanoidModel.createMesh(
                new CubeDeformation(leggingsLayer ? 0.5F : 1.0F), 0.0F);
        PartDefinition root = mesh.getRoot();

        if (leggingsLayer) {
            addKneeGuard(root.getChild("right_leg"), false);
            addKneeGuard(root.getChild("left_leg"), true);
        } else {
            addHelmetRidge(root.getChild("head"));
            addChestPlate(root.getChild("body"));
            addShoulderPlate(root.getChild("right_arm"), false);
            addShoulderPlate(root.getChild("left_arm"), true);
            addBootToe(root.getChild("right_leg"), false);
            addBootToe(root.getChild("left_leg"), true);
        }

        return new HumanoidModel<>(LayerDefinition.create(mesh, 64, 32).bakeRoot());
    }

    private static void addHelmetRidge(PartDefinition head) {
        head.addOrReplaceChild("path_helmet_ridge",
                CubeListBuilder.create().texOffs(8, 8)
                        .addBox(-3.0F, -6.5F, -5.0F, 6.0F, 2.0F, 0.8F,
                                new CubeDeformation(0.15F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
    }

    private static void addChestPlate(PartDefinition body) {
        body.addOrReplaceChild("path_chest_plate",
                CubeListBuilder.create().texOffs(20, 20)
                        .addBox(-3.0F, 3.0F, -3.05F, 6.0F, 5.0F, 1.0F,
                                new CubeDeformation(0.15F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
    }

    private static void addShoulderPlate(PartDefinition arm, boolean left) {
        CubeListBuilder plate = CubeListBuilder.create().texOffs(40, 16);
        if (left) plate.mirror();
        arm.addOrReplaceChild(left ? "path_left_shoulder_plate" : "path_right_shoulder_plate",
                plate.addBox(left ? -0.5F : -3.5F, -2.5F, -3.0F, 4.0F, 3.0F, 4.0F,
                        new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
    }

    private static void addKneeGuard(PartDefinition leg, boolean left) {
        CubeListBuilder guard = CubeListBuilder.create().texOffs(0, 16);
        if (left) guard.mirror();
        leg.addOrReplaceChild(left ? "path_left_knee_guard" : "path_right_knee_guard",
                guard.addBox(-2.0F, 5.0F, -2.55F, 4.0F, 3.0F, 0.9F,
                        new CubeDeformation(0.12F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
    }

    private static void addBootToe(PartDefinition leg, boolean left) {
        CubeListBuilder toe = CubeListBuilder.create().texOffs(0, 16);
        if (left) toe.mirror();
        leg.addOrReplaceChild(left ? "path_left_boot_toe" : "path_right_boot_toe",
                toe.addBox(-2.0F, 8.5F, -3.0F, 4.0F, 3.5F, 1.5F,
                        new CubeDeformation(0.12F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
    }
}
