package com.starrail.sim.client;

import com.starrail.sim.StarRailSimMod;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, value = Dist.CLIENT)
public final class DoomsdayBossOverlay {
    private DoomsdayBossOverlay() {}

    @SubscribeEvent
    public static void draw(CustomizeGuiOverlayEvent.BossEventProgress event) {
        var boss = event.getBossEvent();
        if (!(boss.getName().getContents() instanceof TranslatableContents title)
                || !title.getKey().equals("entity.starrail_sim.doomsday_beast")) return;
        event.setCanceled(true);
        event.setIncrement(28);
        var gui = event.getGuiGraphics();
        int center = event.getX() + 91;
        int width = Math.min(230, gui.guiWidth() - 24);
        int x = center - width / 2;
        int y = event.getY() + 1;
        float health = Mth.clamp(boss.getProgress(), 0, 1);
        int color = health <= .5F ? 0xFFC15472 : 0xFF7252A8;
        int trim = 0xFFDDAC65;
        gui.drawCenteredString(Minecraft.getInstance().font, boss.getName(), center, y - 10, 0xFFF9E4C4);
        gui.fill(x - 2, y, x + width + 2, y + 10, trim);
        gui.fill(x, y + 1, x + width, y + 9, 0xFF1B142C);
        int filled = Math.round(health * width);
        if (filled > 0) {
            gui.fill(x, y + 2, x + filled, y + 8, color);
            gui.fill(x, y + 2, x + filled, y + 3, 0xFFE3A4DD);
        }
        gui.fill(center, y + 1, center + 1, y + 9, trim);
        for (int side : new int[] {-1, 1}) {
            int tip = side < 0 ? x - 6 : x + width + 6;
            gui.fill(tip - 1, y + 2, tip + 2, y + 8, trim);
            gui.fill(tip - 2, y + 4, tip + 3, y + 6, trim);
        }
    }
}
