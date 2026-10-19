package com.starrail.sim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client choice sent back after using a Star Rail Special Pass. */
public record StarRailPassSelectionPacket(StarRailPath path, InteractionHand hand,
                                          ResourceLocation itemId) {
    public static void encode(StarRailPassSelectionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.path());
        buffer.writeEnum(packet.hand());
        buffer.writeResourceLocation(packet.itemId());
    }

    public static StarRailPassSelectionPacket decode(FriendlyByteBuf buffer) {
        return new StarRailPassSelectionPacket(buffer.readEnum(StarRailPath.class),
                buffer.readEnum(InteractionHand.class), buffer.readResourceLocation());
    }

    public static void handle(StarRailPassSelectionPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                StarRailLightConePassService.select(player, packet.path(), packet.hand(),
                        packet.itemId());
            }
        });
        context.setPacketHandled(true);
    }
}
