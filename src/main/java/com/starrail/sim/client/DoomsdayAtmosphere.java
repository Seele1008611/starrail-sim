package com.starrail.sim.client;

import com.starrail.sim.DoomsdayBeast;
import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.StarRailStatusConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Presentation samples the synchronized action clock, so interrupts stop feedback immediately. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, value = Dist.CLIENT)
public final class DoomsdayAtmosphere {
    private DoomsdayAtmosphere() {}

    @SubscribeEvent
    public static void camera(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused() || mc.screen != null
                || StarRailStatusConfig.REDUCED_MOTION.get()
                || !StarRailStatusConfig.COMBAT_CAMERA_SHAKE.get()) return;
        double magnitude = 0;
        double phase = mc.level.getGameTime() + event.getPartialTick();
        int count = 0;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof DoomsdayBeast boss) || boss.distanceToSqr(mc.player) > 48 * 48) continue;
            if (++count > 6) break;
            int action = boss.visualAction();
            float time = boss.visualTicks((float) event.getPartialTick());
            double amplitude = switch (action) {
                case 8 -> impulse(time, 8, 22) * 1.1;
                case 6 -> impulse(time, 12, 20) * 1.4;
                case 7 -> impulse(time, 1, 18) * 1.6;
                case 11 -> impulse(time, 25, 15) * 1.8;
                case 12 -> impulse(time, 21, 8) * .6 + impulse(time, 31, 9) * .8;
                case 4 -> (time >= 18 && time < 46 ? (time - 18) / 28D * .28 : 0)
                        + impulse(time, 46, 12) * 1.6 + impulse(time, 60, 15) * 1.9;
                case 1 -> impulse(time, 16, 8) * .5;
                case 2 -> impulse(time, 14, 8) * .6;
                default -> 0;
            };
            double distance = boss.position().distanceTo(mc.player.position());
            magnitude += amplitude * Math.pow(Math.max(0, 1 - distance / 48), 2);
        }
        magnitude = Math.min(1.25, magnitude * StarRailStatusConfig.COMBAT_STRENGTH.get());
        event.setPitch(event.getPitch() + (float) (Math.sin(phase * 2.1) * magnitude));
        event.setRoll(event.getRoll() + (float) (Math.sin(phase * 1.6) * magnitude * .45));
    }

    private static double impulse(float time, int start, int duration) {
        double phase = (time - start) / duration;
        return phase >= 0 && phase < 1 ? Math.pow(1 - phase, 2) : 0;
    }

    @SubscribeEvent
    public static void atmosphere(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.screen != null || mc.isPaused()
                || mc.options.hideGui) return;
        DoomsdayBeast nearest = null;
        double distance = 32 * 32;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof DoomsdayBeast boss) || !boss.isAlive()) continue;
            double squared = boss.distanceToSqr(mc.player);
            if (squared < distance) { nearest = boss; distance = squared; }
        }
        if (nearest == null) return;
        int action = nearest.visualAction();
        float ticks = nearest.visualTicks(event.getPartialTick());
        boolean ultimate = action == 4 && ticks >= 18 && ticks < 60;
        boolean arrival = action == 8 && ticks < 35;
        boolean phase = action == 6;
        if (!ultimate && !arrival && !phase) return;
        var gui = event.getGuiGraphics();
        int width = gui.guiWidth();
        int height = gui.guiHeight();
        double strength = Math.min(1, StarRailStatusConfig.COMBAT_STRENGTH.get());
        int alpha = (int) (36 * strength * (1 - Math.sqrt(distance) / 40));
        int edge = Math.max(8, height / 12);
        for (int i = 0; i < edge; i++) {
            int color = Mth.clamp(alpha * (edge - i) / edge, 0, 40) << 24 | 0x170D2B;
            gui.fill(0, i, width, i + 1, color);
            gui.fill(0, height - 1 - i, width, height - i, color);
        }
        String key = ultimate ? "ultimate" : phase ? "rebuild" : "arrival";
        gui.drawCenteredString(mc.font, Component.translatable("screen.starrail_sim.doomsday." + key),
                width / 2, height / 5, ultimate ? 0xFFE6B4 : 0xC6A6EF);
    }
}
