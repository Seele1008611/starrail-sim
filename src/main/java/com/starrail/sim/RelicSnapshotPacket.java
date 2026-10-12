package com.starrail.sim;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server-to-client relic inventory and material snapshot. */
public record RelicSnapshotPacket(CompoundTag snapshot) {
    public static void encode(RelicSnapshotPacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.snapshot());
    }

    public static RelicSnapshotPacket decode(FriendlyByteBuf buffer) {
        CompoundTag snapshot = buffer.readNbt();
        return new RelicSnapshotPacket(snapshot == null ? new CompoundTag() : snapshot);
    }

    public static void handle(RelicSnapshotPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.starrail.sim.client.StarRailRelicClientData.update(packet.snapshot())));
        context.setPacketHandled(true);
    }
}
