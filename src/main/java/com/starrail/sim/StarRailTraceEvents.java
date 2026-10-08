package com.starrail.sim;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** The four-second combat window must not survive reconnects. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID)
public final class StarRailTraceEvents {
    private StarRailTraceEvents() { }
    @SubscribeEvent public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        event.getEntity().getPersistentData().remove("trace_hunt_echo");
    }
    @SubscribeEvent public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        event.getEntity().getPersistentData().remove("trace_hunt_echo");
    }
}
