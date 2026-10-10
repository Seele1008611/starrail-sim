package com.starrail.sim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** One owner snapshot request after the local player/world is actually ready. */
public record PlayerStatusRequestPacket(ResourceLocation dimension) {
    public static void encode(PlayerStatusRequestPacket p, FriendlyByteBuf b) { b.writeResourceLocation(p.dimension); }
    public static PlayerStatusRequestPacket decode(FriendlyByteBuf b) { return new PlayerStatusRequestPacket(b.readResourceLocation()); }
    public static void handle(PlayerStatusRequestPacket p, Supplier<NetworkEvent.Context> supplier) {
        var c = supplier.get();
        c.enqueueWork(() -> {
            var player = c.getSender();
            if (player != null && player.level().dimension().location().equals(p.dimension))
                StarRailPlayerStatusSync.request(player);
        });
        c.setPacketHandled(true);
    }
}
