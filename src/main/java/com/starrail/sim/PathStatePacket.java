package com.starrail.sim;

/**
 * 模组代码说明：网络数据包类，定义需要在客户端与服务端之间传递的数据及其处理入口。
 */

import com.starrail.sim.client.StarRailPathClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;

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
    private final StarRailPath soughtPath;
    private final int soughtX;
    private final int soughtY;
    private final int soughtZ;
    private final ResourceLocation soughtDimension;
    private final int pathRegionMask;
    private final int pathRuinMask;
    private final int pathRuinPositionMask;
    private final long[] pathRuinPositions;
    private final int pathBeaconCount;
    private final long[] pathBeaconPositions;
    private final long[] pathArenaPositions;
    private final boolean pathArenaDiscovered;
    private final int pathArenaVictories;
    private final Map<String, String> pathArchiveCustomNames;

    public PathStatePacket(boolean unlocked, StarRailPath currentPath,
                           StarRailPathRank currentPathRank,
                           int practiceProgress, int practiceTarget,
                           int pinnaclePracticeCount,
                           StarRailPath trialPath, StarRailPathRank trialRank,
                           int objective1,
                           int objective2, int secondsRemaining,
                           StarRailPath soughtPath, ResourceLocation soughtDimension,
                           int soughtX, int soughtY, int soughtZ,
                           int pathRegionMask, int pathRuinMask,
                           int pathRuinPositionMask, long[] pathRuinPositions,
                           int pathBeaconCount,
                           long[] pathBeaconPositions, long[] pathArenaPositions,
                           boolean pathArenaDiscovered, int pathArenaVictories,
                           Map<String, String> pathArchiveCustomNames) {
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
        this.soughtPath = soughtPath;
        this.soughtX = soughtX;
        this.soughtY = soughtY;
        this.soughtZ = soughtZ;
        this.soughtDimension = soughtDimension;
        this.pathRegionMask = pathRegionMask;
        this.pathRuinMask = pathRuinMask;
        this.pathRuinPositionMask = pathRuinPositionMask;
        this.pathRuinPositions = pathRuinPositions.clone();
        this.pathBeaconCount = pathBeaconCount;
        this.pathBeaconPositions = pathBeaconPositions.clone();
        this.pathArenaPositions = pathArenaPositions.clone();
        this.pathArenaDiscovered = pathArenaDiscovered;
        this.pathArenaVictories = pathArenaVictories;
        this.pathArchiveCustomNames = Map.copyOf(pathArchiveCustomNames);
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
        buffer.writeEnum(packet.soughtPath);
        buffer.writeResourceLocation(packet.soughtDimension);
        buffer.writeInt(packet.soughtX);
        buffer.writeInt(packet.soughtY);
        buffer.writeInt(packet.soughtZ);
        buffer.writeInt(packet.pathRegionMask);
        buffer.writeInt(packet.pathRuinMask);
        buffer.writeInt(packet.pathRuinPositionMask);
        writePositions(buffer, packet.pathRuinPositions);
        buffer.writeInt(packet.pathBeaconCount);
        writePositions(buffer, packet.pathBeaconPositions);
        writePositions(buffer, packet.pathArenaPositions);
        buffer.writeBoolean(packet.pathArenaDiscovered);
        buffer.writeInt(packet.pathArenaVictories);
        buffer.writeVarInt(packet.pathArchiveCustomNames.size());
        packet.pathArchiveCustomNames.forEach((key, name) -> {
            buffer.writeUtf(key, 128);
            buffer.writeUtf(name, 64);
        });
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
                buffer.readInt(),
                buffer.readEnum(StarRailPath.class),
                buffer.readResourceLocation(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                readPositions(buffer),
                buffer.readInt(),
                readPositions(buffer),
                readPositions(buffer),
                buffer.readBoolean(),
                buffer.readInt(),
                readArchiveNames(buffer));
    }

    private static Map<String, String> readArchiveNames(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 0 || count > 65_536) {
            throw new io.netty.handler.codec.DecoderException("Invalid archive custom-name count: " + count);
        }
        Map<String, String> names = new HashMap<>();
        for (int index = 0; index < count; index++) {
            names.put(buffer.readUtf(128), buffer.readUtf(64));
        }
        return names;
    }

    private static void writePositions(FriendlyByteBuf buffer, long[] positions) {
        buffer.writeVarInt(positions.length);
        for (long position : positions) buffer.writeLong(position);
    }

    private static long[] readPositions(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 0 || count > 65_536) {
            throw new io.netty.handler.codec.DecoderException("Invalid travel archive position count: " + count);
        }
        long[] positions = new long[count];
        for (int index = 0; index < count; index++) positions[index] = buffer.readLong();
        return positions;
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
                packet.secondsRemaining,
                packet.soughtPath,
                packet.soughtDimension,
                packet.soughtX,
                packet.soughtY,
                packet.soughtZ,
                packet.pathRegionMask,
                packet.pathRuinMask,
                packet.pathRuinPositionMask,
                packet.pathRuinPositions,
                packet.pathBeaconCount,
                packet.pathBeaconPositions,
                packet.pathArenaPositions,
                packet.pathArenaDiscovered,
                packet.pathArenaVictories,
                packet.pathArchiveCustomNames));
        context.setPacketHandled(true);
    }
}
