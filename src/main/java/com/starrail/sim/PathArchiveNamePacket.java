package com.starrail.sim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Player-authored label for one discovered archive coordinate. */
public final class PathArchiveNamePacket {
    private final String recordKey;
    private final String name;

    public PathArchiveNamePacket(String recordKey, String name) {
        this.recordKey = recordKey == null ? "" : recordKey;
        this.name = name == null ? "" : name;
    }

    public static void encode(PathArchiveNamePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.recordKey, 128);
        buffer.writeUtf(packet.name, 64);
    }

    public static PathArchiveNamePacket decode(FriendlyByteBuf buffer) {
        return new PathArchiveNamePacket(buffer.readUtf(128), buffer.readUtf(64));
    }

    public static void handle(PathArchiveNamePacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                StarRailExplorationService.renameArchiveRecord(
                        player, packet.recordKey, packet.name);
            }
        });
        context.setPacketHandled(true);
    }
}
