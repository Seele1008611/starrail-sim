package com.starrail.sim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** A miniature of the full train; no duplicate item-only geometry or truncation. */
public final class StarRailTrainItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static StarRailTrainItemRenderer instance;

    private StarRailTrainItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static StarRailTrainItemRenderer instance() {
        if (instance == null) instance = new StarRailTrainItemRenderer();
        return instance;
    }

    @Override public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose,
                                       MultiBufferSource buffers, int light, int overlay) {
        boolean gui = context == ItemDisplayContext.GUI;
        float scale = gui ? 0.075F : context == ItemDisplayContext.GROUND ? 0.046F : 0.057F;
        pose.pushPose();
        pose.translate(0.5, gui ? 0.25 : 0.35, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(gui ? -55 : 35));
        if (gui) pose.mulPose(Axis.XP.rotationDegrees(15));
        pose.scale(scale, scale, scale);
        pose.translate(-2.3, -0.5, -6.5);
        StarRailTrainMesh.INSTANCE.render(pose, buffers, light, overlay);
        pose.popPose();
    }
}
