package com.starrail.sim;

/**
 * 模组代码说明：注册模组网络通道，并封装客户端与服务端的数据包发送。
 */

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** Network channel for server-authoritative combat feedback. */
public final class StarRailNetwork {
    // 战斗特效新增服务端消息；客户端和服务端需要使用同一协议版本。
    private static final String PROTOCOL_VERSION = "7";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(StarRailSimMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int messageId;

    private StarRailNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(messageId++, CombatVfxPacket.class,
                CombatVfxPacket::encode, CombatVfxPacket::decode, CombatVfxPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(messageId++, TraceActionPacket.class, TraceActionPacket::encode, TraceActionPacket::decode, TraceActionPacket::handle, java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(messageId++, TraceStatePacket.class, TraceStatePacket::encode, TraceStatePacket::decode, TraceStatePacket::handle, java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(
                messageId++,
                DamageNumberPacket.class,
                DamageNumberPacket::encode,
                DamageNumberPacket::decode,
                DamageNumberPacket::handle);
        CHANNEL.registerMessage(
                messageId++,
                PathActionPacket.class,
                PathActionPacket::encode,
                PathActionPacket::decode,
                PathActionPacket::handle);
        CHANNEL.registerMessage(
                messageId++,
                PathStatePacket.class,
                PathStatePacket::encode,
                PathStatePacket::decode,
                PathStatePacket::handle);
        CHANNEL.registerMessage(
                messageId++,
                NotificationPacket.class,
                NotificationPacket::encode,
                NotificationPacket::decode,
                NotificationPacket::handle);
    }

    public static void sendDamageNumber(ServerPlayer player, LivingEntity target,
                                        float amount, boolean critical) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new DamageNumberPacket(target.getId(), amount, critical));
    }

    public static void sendToughnessNumber(ServerPlayer player, LivingEntity target,
                                           float amount, boolean finalHit) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new DamageNumberPacket(target.getId(), amount, finalHit, true));
    }

    public static void sendPathState(ServerPlayer player, PathStatePacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendNotification(ServerPlayer player, Component message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                NotificationPacket.queued(message));
    }

    public static void sendCombatNotification(ServerPlayer player, Component message) {
        sendCombatNotification(player, message, false);
    }

    public static void sendCombatNotification(ServerPlayer player, Component message,
                                              boolean priority) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                NotificationPacket.combat(message, priority));
    }
}
