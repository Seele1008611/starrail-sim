package com.starrail.sim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starrail.sim.VoidrangerBoltEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Client renderer for the automatically tracked projectile entity. */
public final class VoidrangerBoltRenderer extends EntityRenderer<VoidrangerBoltEntity> {
    private final VoidrangerBoltModel model;

    public VoidrangerBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new VoidrangerBoltModel(context.bakeLayer(VoidrangerBoltModel.LAYER_LOCATION));
    }

    @Override
    public void render(VoidrangerBoltEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        this.model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTick, 0.0F, 0.0F);
        VertexConsumer out = buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, out, packedLight, OverlayTexture.NO_OVERLAY,
                0.30F, 0.95F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(VoidrangerBoltEntity entity) {
        return VoidrangerBoltModel.texture();
    }
}
