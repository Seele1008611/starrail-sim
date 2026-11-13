package com.starrail.sim;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record CurioUsePacket(int slot, int item) {
    public static void encode(CurioUsePacket packet, FriendlyByteBuf buffer) {
        buffer.writeByte(packet.slot());
        buffer.writeVarInt(packet.item());
    }

    public static CurioUsePacket decode(FriendlyByteBuf buffer) {
        return new CurioUsePacket(buffer.readUnsignedByte(), buffer.readVarInt());
    }

    public static void handle(CurioUsePacket packet, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player == null) return;
            if (packet.slot() == 255) CurioEffects.offer(player);
            else PrototypeCurios.use(player, packet.slot(), packet.item());
        });
        context.setPacketHandled(true);
    }
}
