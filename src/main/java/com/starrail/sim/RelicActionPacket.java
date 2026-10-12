package com.starrail.sim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Client requests a relic operation; all inventory changes are validated on the server. */
public record RelicActionPacket(StarRailRelicOperationService.Action action, UUID relicId,
                               long expectedVersion, int value, RelicStackData.SetId set,
                               RelicStackData.Part part, RelicStackData.Stat mainStat,
                               boolean targetMain, int targetSubstatMask) {
    public static void encode(RelicActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.action());
        buffer.writeBoolean(packet.relicId() != null);
        if (packet.relicId() != null) buffer.writeUUID(packet.relicId());
        buffer.writeLong(packet.expectedVersion());
        buffer.writeInt(packet.value());
        buffer.writeEnum(packet.set() == null ? RelicStackData.SetId.MUSKETEER : packet.set());
        buffer.writeEnum(packet.part() == null ? RelicStackData.Part.HEAD : packet.part());
        buffer.writeEnum(packet.mainStat() == null ? RelicStackData.Stat.HP : packet.mainStat());
        buffer.writeBoolean(packet.targetMain());
        buffer.writeInt(packet.targetSubstatMask());
    }

    public static RelicActionPacket decode(FriendlyByteBuf buffer) {
        StarRailRelicOperationService.Action action = buffer.readEnum(StarRailRelicOperationService.Action.class);
        UUID relicId = buffer.readBoolean() ? buffer.readUUID() : null;
        return new RelicActionPacket(action, relicId, buffer.readLong(), buffer.readInt(),
                buffer.readEnum(RelicStackData.SetId.class), buffer.readEnum(RelicStackData.Part.class),
                buffer.readEnum(RelicStackData.Stat.class), buffer.readBoolean(), buffer.readInt());
    }

    public static void handle(RelicActionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            StarRailRelicOperationService.Request request = new StarRailRelicOperationService.Request(
                    packet.action(), packet.relicId(), packet.expectedVersion(), packet.value(),
                    packet.set(), packet.part(), packet.mainStat(), packet.targetMain(), packet.targetSubstatMask());
            StarRailRelicOperationService.Result result = StarRailRelicOperationService.perform(player, request);
            player.displayClientMessage(Component.translatable(result.translationKey()), false);
            StarRailNetwork.sendRelicSnapshot(player, result.translationKey(), result.accepted());
        });
        context.setPacketHandled(true);
    }
}
