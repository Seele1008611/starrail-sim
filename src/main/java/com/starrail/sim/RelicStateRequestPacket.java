package com.starrail.sim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Requests a fresh authoritative relic snapshot when the native relic page opens. */
public record RelicStateRequestPacket() {
    public static void encode(RelicStateRequestPacket packet, FriendlyByteBuf buffer) {}
    public static RelicStateRequestPacket decode(FriendlyByteBuf buffer) { return new RelicStateRequestPacket(); }

    public static void handle(RelicStateRequestPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) StarRailNetwork.sendRelicSnapshot(player);
        });
        context.setPacketHandled(true);
    }
}
