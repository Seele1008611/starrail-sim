package com.starrail.sim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.starrail.sim.StarRailTrainBlock;
import com.starrail.sim.StarRailTrainBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** Full-scale reference model rendered once at its front-left anchor. */
public final class StarRailTrainRenderer implements BlockEntityRenderer<StarRailTrainBlockEntity> {
    public StarRailTrainRenderer(BlockEntityRendererProvider.Context context) {}

    @Override public void render(StarRailTrainBlockEntity be, float partialTick, PoseStack stack,
                                 MultiBufferSource buffers, int light, int overlay) {
        if (!be.getBlockState().getValue(StarRailTrainBlock.LARGE)) return;
        float angle = switch (be.getBlockState().getValue(StarRailTrainBlock.FACING)) {
            case EAST -> -90; case SOUTH -> 180; case WEST -> 90; default -> 0;
        };
        stack.pushPose();
        stack.translate(0.5, 0, 0.5);
        stack.mulPose(Axis.YP.rotationDegrees(angle));
        stack.translate(-0.5, 0, -0.5);
        StarRailTrainMesh.INSTANCE.render(stack, buffers, light, overlay);
        stack.popPose();
    }

    @Override public boolean shouldRenderOffScreen(StarRailTrainBlockEntity be) { return false; }
    @Override public int getViewDistance() { return 192; }
}
