package com.starrail.sim.client;

import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** A centered card plane; inventory renderer's GUI depth translation must not be rotated. */
final class StarRailLightConeDisplay {
    private StarRailLightConeDisplay() { }

    static void banner(GuiGraphics graphics, ItemStack stack, int x, int y, int width, int height) {
        if (stack.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        ResourceLocation sprite = minecraft.getItemRenderer().getModel(stack, minecraft.level,
                minecraft.player, 0).getParticleIcon().contents().name();
        ResourceLocation texture = new ResourceLocation(sprite.getNamespace(), "textures/" + sprite.getPath() + ".png");
        StarRailLightConePortraits.Focus focus = StarRailLightConePortraits.forSprite(sprite);
        int cropWidth = focus.sourceWidth();
        int cropHeight = Math.max(1, Math.round(cropWidth * height / (float) width));
        int sourceX = Math.max(160, Math.min(924 - cropWidth, focus.centerX() - cropWidth / 2));
        int sourceY = Math.max(0, Math.min(1080 - cropHeight, focus.centerY() - cropHeight / 2));
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        float scale = width / (float) cropWidth;
        graphics.pose().scale(scale, scale, 1);
        graphics.blit(texture, 0, 0, (float) sourceX, (float) sourceY,
                cropWidth, cropHeight, 1080, 1080);
        graphics.flush();
        graphics.pose().popPose();
    }

    static void draw(GuiGraphics graphics, ItemStack stack, float centerX, float centerY,
            float cardHeight, float angle, boolean upright) {
        if (stack.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        ResourceLocation sprite = minecraft.getItemRenderer().getModel(stack, minecraft.level,
                minecraft.player, 0).getParticleIcon().contents().name();
        ResourceLocation texture = new ResourceLocation(sprite.getNamespace(), "textures/" + sprite.getPath() + ".png");
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 40);
        if (!upright) {
            graphics.pose().mulPose(Axis.ZP.rotationDegrees(-8));
            graphics.pose().mulPose(Axis.YP.rotationDegrees(angle));
        }
        float scale = cardHeight / 1080F;
        graphics.pose().scale(scale, scale, scale);
        // Existing cone textures share a 1080-square canvas, card bounds x=160..924.
        if (!upright) {
            for (int layer = 3; layer > 0; layer--) {
                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, -layer * 9);
                graphics.setColor(.4F, .39F, .47F, 1F);
                graphics.blit(texture, -382, -540, 160F, 0F, 764, 1080, 1080, 1080);
                graphics.pose().popPose();
            }
        }
        graphics.setColor(1, 1, 1, 1);
        graphics.blit(texture, -382, -540, 160F, 0F, 764, 1080, 1080, 1080);
        if (!upright) {
            graphics.pose().translate(0, 0, 1);
            double shimmer = .5 + .5 * Math.sin(StarRailCosmicUi.seconds() * Math.PI * 2 / 8);
            int center = (int) (-angle * 5 + (shimmer - .5) * 42);
            for (int y = -460; y < 460; y += 10) {
                int x = center + y / 5;
                for (int band = -4; band <= 4; band++) {
                    int alpha = (int) ((3 + (4 - Math.abs(band)) * 3) * (.65 + .35 * shimmer));
                    int left = Math.max(-320, x + band * 14);
                    int right = Math.min(320, x + band * 14 + 14);
                    if (right > left) graphics.fill(left, y, right, y + 10, alpha << 24 | 0xF2F1FF);
                }
            }
        }
        graphics.flush();
        graphics.pose().popPose();
        graphics.setColor(1, 1, 1, 1);
    }
}
