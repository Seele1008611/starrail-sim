package com.starrail.sim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Requests a server-validated light cone swap; slot -1 means unequip. */
public record LightConeEquipPacket(int inventorySlot, ResourceLocation expectedItem,
                                   int expectedSuperimposition) {
    public static void encode(LightConeEquipPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.inventorySlot());
        buffer.writeBoolean(packet.expectedItem() != null);
        if (packet.expectedItem() != null) buffer.writeResourceLocation(packet.expectedItem());
        buffer.writeVarInt(packet.expectedSuperimposition());
    }

    public static LightConeEquipPacket decode(FriendlyByteBuf buffer) {
        int slot = buffer.readVarInt();
        ResourceLocation item = buffer.readBoolean() ? buffer.readResourceLocation() : null;
        return new LightConeEquipPacket(slot, item, buffer.readVarInt());
    }

    public static void handle(LightConeEquipPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            StarRailLightConeEquipService.Result result = packet.inventorySlot() == -1
                    ? StarRailLightConeEquipService.unequip(player)
                    : StarRailLightConeEquipService.equip(player, packet.inventorySlot(),
                    packet.expectedItem(), packet.expectedSuperimposition());
            StarRailNetwork.sendNotification(player, Component.translatable(result.translationKey()));
        });
        context.setPacketHandled(true);
    }
}
