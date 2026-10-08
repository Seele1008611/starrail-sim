package com.starrail.sim.client;

import com.starrail.sim.*;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class StarRailTraceClientState {
    private static CompoundTag state = new CompoundTag();
    private StarRailTraceClientState() { }
    public static int mask() { return mask(StarRailPath.HUNT); }
    public static int mask(StarRailPath path) { return state.getInt(path.getId()); }
    public static int rank() { return rank(StarRailPath.HUNT); }
    public static int rank(StarRailPath path) { return state.getInt(path.getId() + "_rank"); }
    public static void update(TraceStatePacket packet) {
        state = packet.state().copy();
        var player = Minecraft.getInstance().player;
        if (player != null) StarRailTraceService.refresh(player, packet.current(), mask(packet.current()));
        if (Minecraft.getInstance().screen instanceof StarRailTraceScreen screen) screen.acknowledge();
    }
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void onLogout(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        state = new CompoundTag();
    }
}
