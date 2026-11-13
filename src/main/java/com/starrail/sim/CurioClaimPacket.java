package com.starrail.sim;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record CurioClaimPacket(UUID id, int index) {
    public static void encode(CurioClaimPacket p, FriendlyByteBuf b) {
        b.writeUUID(p.id);
        b.writeByte(p.index);
    }

    public static CurioClaimPacket decode(FriendlyByteBuf b) {
        return new CurioClaimPacket(b.readUUID(), b.readUnsignedByte());
    }

    public static void handle(CurioClaimPacket p, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player != null) CurioEffects.claim(player,p.id,p.index);
        });
        context.setPacketHandled(true);
    }
}
