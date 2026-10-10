package com.starrail.sim;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID)
public final class StarRailPlayerStatusSync {
    private static final Map<ServerPlayer, PlayerStatusPacket> SENT = new WeakHashMap<>();
    private record Request(net.minecraft.resources.ResourceLocation dimension, long tick) {}
    private static final Map<ServerPlayer, Request> REQUESTS = new WeakHashMap<>();
    static void request(ServerPlayer player) {
        long now = player.level().getGameTime();
        var dimension = player.level().dimension().location();
        Request last = REQUESTS.get(player);
        if (last != null && last.dimension().equals(dimension) && now - last.tick() < 20) return;
        REQUESTS.put(player, new Request(dimension, now)); send(player, true);
    }
    private static void send(ServerPlayer player, boolean force) {
        var packet = new PlayerStatusPacket(player.getUUID(), player.level().dimension().location(),
                player.getFoodData().getSaturationLevel());
        if (force || !packet.equals(SENT.get(player))) {
            StarRailNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
            SENT.put(player, packet);
        }
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e) {
        if (e.phase == TickEvent.Phase.END && e.player instanceof ServerPlayer p && p.tickCount % 5 == 0)
            send(p, false);
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e) { refresh(e); }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e) { refresh(e); }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e) { refresh(e); }
    private static void refresh(PlayerEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) send(p, true);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e) {
        SENT.remove(e.getEntity()); REQUESTS.remove(e.getEntity());
    }
    @SubscribeEvent public static void stop(ServerStoppedEvent e) { SENT.clear(); REQUESTS.clear(); }
    private StarRailPlayerStatusSync() {}
}
