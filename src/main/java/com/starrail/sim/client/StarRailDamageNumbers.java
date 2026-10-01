package com.starrail.sim.client;

/**
 * 模组代码说明：在客户端绘制战斗伤害数字及其显示动画。
 */

import com.mojang.blaze3d.vertex.PoseStack;
import com.starrail.sim.StarRailSimMod;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Client-side floating combat number renderer. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class StarRailDamageNumbers {
    private static final int MAX_AGE_TICKS = 30;
    private static final double DAMAGE_LANE_OFFSET = 0.34D;
    private static final List<DamageNumber> NUMBERS = new ArrayList<>();
    private static final Random RANDOM = new Random();

    private StarRailDamageNumbers() {
    }

    public static void add(int targetId, float amount, boolean critical) {
        add(targetId, amount, critical, false);
    }

    public static void add(int targetId, float amount, boolean critical,
                           boolean toughness) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        NUMBERS.add(new DamageNumber(
                targetId,
                amount,
                critical,
                toughness,
                minecraft.level.getGameTime(),
                (RANDOM.nextDouble() - 0.5D) * 0.12D));
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES
                || NUMBERS.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null) {
            NUMBERS.clear();
            return;
        }

        long gameTime = level.getGameTime();
        Camera camera = event.getCamera();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource =
                minecraft.renderBuffers().bufferSource();
        Font font = minecraft.font;

        Iterator<DamageNumber> iterator = NUMBERS.iterator();
        while (iterator.hasNext()) {
            DamageNumber number = iterator.next();
            float age = (gameTime - number.spawnTick()) + event.getPartialTick();
            if (age >= MAX_AGE_TICKS) {
                iterator.remove();
                continue;
            }

            Entity target = level.getEntity(number.targetId());
            if (target == null) {
                iterator.remove();
                continue;
            }

            double rise = age * 0.025D;
            // Keep HP damage and toughness damage in separate visual lanes.
            // Both packets can be emitted by one attack, so a shared origin
            // makes the two numbers overlap even when their colors differ.
            double laneOffset = number.toughness()
                    ? DAMAGE_LANE_OFFSET : -DAMAGE_LANE_OFFSET;
            double x = target.getX() + laneOffset + number.xOffset()
                    - camera.getPosition().x;
            double y = target.getY() + target.getBbHeight() + 0.45D + rise
                    + (number.toughness() ? 0.08D : 0.0D)
                    - camera.getPosition().y;
            double z = target.getZ() - camera.getPosition().z;

            Component text = Component.literal(formatAmount(number.amount(), number.critical()));
            int color = number.toughness() ? 0xFFFF5555 : 0xFFFFFFFF;

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(camera.rotation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);

            String renderedText = text.getString();
            float textX = -font.width(renderedText) / 2.0F;
            font.drawInBatch(
                    renderedText,
                    textX,
                    0.0F,
                    color,
                    false,
                    poseStack.last().pose(),
                    bufferSource,
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    LightTexture.FULL_BRIGHT);
            poseStack.popPose();
        }
    }

    private static String formatAmount(float amount, boolean critical) {
        String value = String.format(Locale.ROOT, "-%.1f", Math.abs(amount));
        return critical ? value + "!" : value;
    }

    private record DamageNumber(int targetId, float amount, boolean critical,
                                boolean toughness,
                                long spawnTick, double xOffset) {
    }
}
