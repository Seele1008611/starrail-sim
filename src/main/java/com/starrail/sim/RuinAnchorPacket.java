package com.starrail.sim;

import com.starrail.sim.client.StarRailRuinAnchorHud;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** Look-at state supplied by the server, never a client request to load an anchor chunk. */
public record RuinAnchorPacket(ResourceLocation dimension, boolean visible, BlockPos pos,
                               StarRailPath path, StarRailRuinGuardService.AnchorPhase phase,
                               int remainingTicks) {
    public static RuinAnchorPacket hidden(ResourceLocation dimension) {
        return new RuinAnchorPacket(dimension, false, BlockPos.ZERO, StarRailPath.NONE,
                StarRailRuinGuardService.AnchorPhase.UNBOUND, 0);
    }
    public static void encode(RuinAnchorPacket p, FriendlyByteBuf b) {
        b.writeResourceLocation(p.dimension); b.writeBoolean(p.visible);
        if (!p.visible) return;
        b.writeBlockPos(p.pos); b.writeEnum(p.path); b.writeEnum(p.phase); b.writeVarInt(p.remainingTicks);
    }
    public static RuinAnchorPacket decode(FriendlyByteBuf b) {
        ResourceLocation dimension = b.readResourceLocation();
        if (!b.readBoolean()) return hidden(dimension);
        return new RuinAnchorPacket(dimension, true, b.readBlockPos(), b.readEnum(StarRailPath.class),
                b.readEnum(StarRailRuinGuardService.AnchorPhase.class), b.readVarInt());
    }
    public static void handle(RuinAnchorPacket packet, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> StarRailRuinAnchorHud.accept(packet)));
        context.setPacketHandled(true);
    }
}
