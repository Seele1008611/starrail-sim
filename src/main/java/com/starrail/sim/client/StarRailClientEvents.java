package com.starrail.sim.client;

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
        while (StarRailClient.OPEN_CHARACTER.consumeClick()
                && minecraft.player != null && minecraft.screen == null) {
            minecraft.setScreen(new StarRailCharacterScreen());
        }
    }
}
