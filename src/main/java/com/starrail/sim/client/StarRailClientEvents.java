package com.starrail.sim.client;

/**
 * 模组代码说明：监听客户端玩家和界面事件，刷新模组客户端状态。
 */

import com.starrail.sim.StarRailSimMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client tick hooks used to open read-only screens. */
@Mod.EventBusSubscriber(
        modid = StarRailSimMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class StarRailClientEvents {
    private StarRailClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase != ClientTickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        while (StarRailClient.TOGGLE_STATUS.consumeClick()) {
            if (minecraft.player == null || minecraft.screen != null) continue;
            boolean enabled = !com.starrail.sim.StarRailStatusConfig.HUD.get();
            com.starrail.sim.StarRailStatusConfig.HUD.set(enabled);
            com.starrail.sim.StarRailStatusConfig.CLIENT.save();
            minecraft.player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    enabled ? "ui.starrail_sim.status.enabled" : "ui.starrail_sim.status.disabled"), true);
        }
        while (StarRailClient.OPEN_CHARACTER.consumeClick()
                && minecraft.player != null && minecraft.screen == null) {
            minecraft.setScreen(new StarRailCharacterScreen());
        }
    }
}
