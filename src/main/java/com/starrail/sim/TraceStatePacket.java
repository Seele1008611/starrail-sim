package com.starrail.sim;

import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public record TraceStatePacket(StarRailPath current, CompoundTag state) {
    public TraceStatePacket(IStarRailPathData data) { this(data.getCurrentPath(), snapshot(data)); }
    private static CompoundTag snapshot(IStarRailPathData data) {
        var tag = new CompoundTag();
        for (var path : StarRailPath.values()) if (path.isRealPath()) {
            tag.putInt(path.getId(), data.getTraceMask(path));
            tag.putInt(path.getId() + "_rank", data.getPathRank(path).getLevel());
        }
        return tag;
    }
    public static void encode(TraceStatePacket packet, FriendlyByteBuf buf) { buf.writeEnum(packet.current); buf.writeNbt(packet.state); }
    public static TraceStatePacket decode(FriendlyByteBuf buf) {
        var path = buf.readEnum(StarRailPath.class); var tag = buf.readNbt();
        return new TraceStatePacket(path, tag == null ? new CompoundTag() : tag);
    }
    public static void handle(TraceStatePacket packet, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
            () -> () -> com.starrail.sim.client.StarRailTraceClientState.update(packet)));
        context.setPacketHandled(true);
    }
}
