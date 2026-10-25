package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/** Server-side travel between the fixed Astral Express hubs. */
public final class StarRailDimensionService {
    private StarRailDimensionService() {
    }

    static boolean hasRuinHubClearance(ServerLevel level, int x, int z) {
        if (!level.dimension().equals(StarRailDimensionIds.THE_PATH)) return true;
        long dx = x;
        long dz = z;
        return dx * dx + dz * dz >= 400L * 400L;
    }

    public static boolean isHubBeacon(ServerLevel level, BlockPos pos) {
        return (level.dimension().equals(Level.OVERWORLD)
                || level.dimension().equals(StarRailDimensionIds.THE_PATH))
                && StarRailTrainHubService.beaconBlock(level.dimension()).equals(pos);
    }

    /** Travel from the clicked hub beacon; other archive beacons remain discovery markers. */
    public static boolean travelFromBeacon(ServerPlayer player, BlockPos pos) {
        if (!isHubBeacon(player.serverLevel(), pos)) return false;
        return travelToCounterpart(player);
    }

    /** Operator command entry point. Normal players use the beacon inside the train. */
    public static boolean enter(ServerPlayer player) {
        if (player.serverLevel().dimension().equals(StarRailDimensionIds.THE_PATH)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "message.starrail_sim.dimension.already_in_path"));
            return false;
        }
        if (!player.serverLevel().dimension().equals(Level.OVERWORLD)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "message.starrail_sim.dimension.wrong_source"));
            return false;
        }
        return travelToCounterpart(player);
    }

    /** Operator command entry point; the return destination is always the Overworld train. */
    public static boolean returnToOrigin(ServerPlayer player) {
        if (!player.serverLevel().dimension().equals(StarRailDimensionIds.THE_PATH)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "message.starrail_sim.dimension.return_requires_path"));
            return false;
        }
        return travelToCounterpart(player);
    }

    private static boolean travelToCounterpart(ServerPlayer player) {
        ServerLevel source = player.serverLevel();
        ResourceKey<Level> sourceDimension = source.dimension();
        ResourceKey<Level> destinationDimension;
        if (sourceDimension.equals(Level.OVERWORLD)) {
            destinationDimension = StarRailDimensionIds.THE_PATH;
        } else if (sourceDimension.equals(StarRailDimensionIds.THE_PATH)) {
            destinationDimension = Level.OVERWORLD;
        } else {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "message.starrail_sim.dimension.wrong_source"));
            return false;
        }

        ServerLevel destination = player.getServer().getLevel(destinationDimension);
        if (destination == null || !StarRailTrainHubService.hasIntactHubBeacon(destination)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "message.starrail_sim.dimension.hub_not_ready"));
            return false;
        }

        BlockPos feetBlock = StarRailTrainHubService.landingFeetBlock(destinationDimension);
        Vec3 landing = new Vec3(feetBlock.getX() + 0.5D, feetBlock.getY(),
                feetBlock.getZ() + 0.5D);
        if (!isSafeLanding(player, destination, landing)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "message.starrail_sim.dimension.hub_landing_unsafe"));
            return false;
        }

        if (!player.teleportTo(destination, landing.x, landing.y, landing.z,
                Set.<RelativeMovement>of(), player.getYRot(), player.getXRot())) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "message.starrail_sim.dimension.travel_failed"));
            return false;
        }

        if (destinationDimension.equals(StarRailDimensionIds.THE_PATH)) {
            StarRailExplorationService.markPathVisited(player);
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "message.starrail_sim.dimension.arrived_path"));
        } else {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "message.starrail_sim.dimension.arrived_overworld"));
        }
        return true;
    }

    private static boolean isSafeLanding(ServerPlayer player, ServerLevel level, Vec3 position) {
        if (position.y < level.getMinBuildHeight() + 1
                || position.y >= level.getMaxBuildHeight() - 2) return false;

        BlockPos floor = BlockPos.containing(position.x, position.y - 1.0E-4D, position.z);
        if (!level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) return false;
        AABB targetBox = player.getBoundingBox().move(
                position.x - player.getX(), position.y - player.getY(), position.z - player.getZ());
        return level.noCollision(player, targetBox) && !level.containsAnyLiquid(targetBox);
    }
}
