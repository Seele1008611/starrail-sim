package com.starrail.sim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.VoidrangerBoltEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Small 3D visual for the eliminator's server-spawned straight bolt. */
public final class VoidrangerBoltModel extends EntityModel<VoidrangerBoltEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(StarRailSimMod.MOD_ID, "voidranger_bolt"), "main");

    private final ModelPart root;

    public VoidrangerBoltModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bolt", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 6.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
    public void setupAnim(VoidrangerBoltEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        Vec3 motion = entity.getDeltaMovement();
        this.root.yRot = (float) Math.atan2(-motion.x, motion.z);
        this.root.xRot = (float) Math.atan2(motion.y, Math.max(1.0E-5D, motion.horizontalDistance()));
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public static ResourceLocation texture() {
        return new ResourceLocation(StarRailSimMod.MOD_ID, "textures/entity/voidranger_bolt.png");
    }
}
