package com.starrail.sim;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record TraceActionPacket(Action action, StarRailPath path, int node) {
    public enum Action { REQUEST, UNLOCK, ROLLBACK, RESET }
    public static void encode(TraceActionPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.action); buf.writeEnum(packet.path); buf.writeInt(packet.node);
    }
    public static TraceActionPacket decode(FriendlyByteBuf buf) {
        return new TraceActionPacket(buf.readEnum(Action.class), buf.readEnum(StarRailPath.class), buf.readInt());
    }
    public static void handle(TraceActionPacket packet, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player != null) StarRailTraceService.act(player, packet.action, packet.path, packet.node);
        });
        context.setPacketHandled(true);
    }
}
