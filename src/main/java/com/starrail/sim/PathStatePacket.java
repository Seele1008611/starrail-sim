package com.starrail.sim;

import com.starrail.sim.client.StarRailPathClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server-to-client snapshot used by the path selection screen. */
public final class PathStatePacket {
    private final boolean unlocked;
    private final StarRailPath currentPath;
    private final StarRailPathRank currentPathRank;
    private final int practiceProgress;
    private final int practiceTarget;
    private final int pinnaclePracticeCount;
    private final StarRailPath trialPath;
    private final StarRailPathRank trialRank;
    private final int objective1;
    private final int objective2;
    private final int secondsRemaining;

    public PathStatePacket(boolean unlocked, StarRailPath currentPath,
                           StarRailPathRank currentPathRank,
                           int practiceProgress, int practiceTarget,
                           int pinnaclePracticeCount,
                           StarRailPath trialPath, StarRailPathRank trialRank,
                           int objective1,
                           int objective2, int secondsRemaining) {
        this.unlocked = unlocked;
        this.currentPath = currentPath;
        this.currentPathRank = currentPathRank;
        this.practiceProgress = practiceProgress;
        this.practiceTarget = practiceTarget;
        this.pinnaclePracticeCount = pinnaclePracticeCount;
        this.trialPath = trialPath;
        this.trialRank = trialRank;
        this.objective1 = objective1;
        this.objective2 = objective2;
        this.secondsRemaining = secondsRemaining;
    }

    public static void encode(PathStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.unlocked);
        buffer.writeEnum(packet.currentPath);
        buffer.writeEnum(packet.currentPathRank);
        buffer.writeInt(packet.practiceProgress);
        buffer.writeInt(packet.practiceTarget);
        buffer.writeInt(packet.pinnaclePracticeCount);
        buffer.writeEnum(packet.trialPath);
        buffer.writeEnum(packet.trialRank);
        buffer.writeInt(packet.objective1);
        buffer.writeInt(packet.objective2);
        buffer.writeInt(packet.secondsRemaining);
    }

    public static PathStatePacket decode(FriendlyByteBuf buffer) {
        return new PathStatePacket(
                buffer.readBoolean(),
                buffer.readEnum(StarRailPath.class),
                buffer.readEnum(StarRailPathRank.class),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readEnum(StarRailPath.class),
                buffer.readEnum(StarRailPathRank.class),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt());
    }

    public static void handle(PathStatePacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> StarRailPathClientState.update(
                packet.unlocked,
                packet.currentPath,
                packet.currentPathRank,
                packet.practiceProgress,
                packet.practiceTarget,
                packet.pinnaclePracticeCount,
                packet.trialPath,
                packet.trialRank,
                packet.objective1,
                packet.objective2,
                packet.secondsRemaining));
        context.setPacketHandled(true);
    }
}
