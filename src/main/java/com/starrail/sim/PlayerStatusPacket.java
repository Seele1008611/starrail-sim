package com.starrail.sim;

import com.starrail.sim.client.StarRailPlayerStatusHud;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.UUID;
import java.util.function.Supplier;

/** Saturation is not reliably supplied to the owner by vanilla health packets. */
public record PlayerStatusPacket(UUID player, ResourceLocation dimension, float saturation) {
    public static void encode(PlayerStatusPacket p, FriendlyByteBuf b) {
        b.writeUUID(p.player); b.writeResourceLocation(p.dimension); b.writeFloat(p.saturation);
    }
    public static PlayerStatusPacket decode(FriendlyByteBuf b) {
        return new PlayerStatusPacket(b.readUUID(), b.readResourceLocation(), b.readFloat());
    }
    public static void handle(PlayerStatusPacket p, Supplier<NetworkEvent.Context> supplier) {
        var c = supplier.get();
        c.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> StarRailPlayerStatusHud.accept(p)));
        c.setPacketHandled(true);
    }
}
