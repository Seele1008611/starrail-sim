package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

/** Server-side entry/return flow and per-player origin persistence for The Path. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarRailDimensionService {
    private static final String RETURN_DATA = StarRailSimMod.MOD_ID + "_dimension_return";
    private static final int HUB_X = 0;
    private static final int HUB_Y = 128;
    private static final int HUB_Z = 0;
    private static final int HUB_PLATFORM_RADIUS = 3;
    public static final BlockPos HUB_RETURN_BEACON = new BlockPos(HUB_X + 2, HUB_Y + 1, HUB_Z);

    private StarRailDimensionService() {
    }

    static boolean hasRuinHubClearance(ServerLevel level, int x, int z) {
        if (!level.dimension().equals(StarRailDimensionIds.THE_PATH)) return true;
        long dx = (long) x - HUB_X;
        long dz = (long) z - HUB_Z;
        return dx * dx + dz * dz >= 400L * 400L;
    }

    public static boolean isHubReturnBeacon(BlockPos pos) {
        return HUB_RETURN_BEACON.equals(pos);
    }

    public static void activateTrain(ServerPlayer player) {
        StarRailExplorationService.markTrainUsed(player);
        if (player.serverLevel().dimension().equals(StarRailDimensionIds.THE_PATH)) returnToOrigin(player);
        else enter(player);
    }

    public static boolean enter(ServerPlayer player) {
        ServerLevel current = player.serverLevel();
        if (current.dimension().equals(StarRailDimensionIds.THE_PATH)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "你已身处命途遗境；回到中继平台右键星轨信标，或执行 /starrail dimension return。"));
            return false;
        }

        ServerLevel destination = current.getServer().getLevel(StarRailDimensionIds.THE_PATH);
        if (destination == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "命途遗境当前未加载，请确认世界包含本模组的维度数据。"));
            return false;
        }

        ensureArrivalPlatform(destination);
        Landing landing = findHubLanding(player, destination);
        if (landing == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "中继平台暂不可安全抵达；没有修改你的当前位置。"));
            return false;
        }

        CompoundTag previous = player.getPersistentData().contains(RETURN_DATA)
                ? player.getPersistentData().getCompound(RETURN_DATA).copy() : null;
        boolean keepExistingReturn = previous != null
                && previous.contains("x") && previous.contains("y") && previous.contains("z")
                && resolveDimension(player, previous) != null;
        if (!keepExistingReturn) {
            CompoundTag origin = new CompoundTag();
            origin.putString("dimension", current.dimension().location().toString());
            origin.putDouble("x", player.getX());
            origin.putDouble("y", player.getY());
            origin.putDouble("z", player.getZ());
            origin.putFloat("yaw", player.getYRot());
            origin.putFloat("pitch", player.getXRot());
            player.getPersistentData().put(RETURN_DATA, origin);
        }

        if (!teleport(player, destination, landing.position(), player.getYRot(), player.getXRot())) {
            restorePreviousReturn(player, previous);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "进入命途遗境失败，已保留原位置和返回记录。"));
            return false;
        }

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                keepExistingReturn
                        ? "已进入命途遗境；保留原有返回位置。中继平台信标可返程，也可执行 /starrail dimension return。"
                        : "已进入命途遗境。回到中继平台右键星轨信标，或执行 /starrail dimension return 返回原位置。"));
        return true;
    }

    public static boolean returnToOrigin(ServerPlayer player) {
        if (!player.serverLevel().dimension().equals(StarRailDimensionIds.THE_PATH)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "只有身处命途遗境时才能使用此返回命令。"));
            return false;
        }

        CompoundTag saved = player.getPersistentData().contains(RETURN_DATA)
                ? player.getPersistentData().getCompound(RETURN_DATA).copy() : null;
        ServerLevel destination = saved == null ? null : resolveDimension(player, saved);
        boolean savedDimensionResolved = destination != null;
        if (destination == null) {
            destination = player.serverLevel().getServer().getLevel(Level.OVERWORLD);
        }
        if (destination == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "找不到可返回的维度，返回记录仍已保留。"));
            return false;
        }

        Landing landing = saved == null || !savedDimensionResolved
                ? null : findSavedLanding(player, destination, saved);
        if (landing == null) {
            landing = findWorldSpawn(player, destination);
        }
        if (landing == null && destination.dimension() != Level.OVERWORLD) {
            destination = player.serverLevel().getServer().getLevel(Level.OVERWORLD);
            if (destination != null) {
                landing = findWorldSpawn(player, destination);
            }
        }
        if (destination == null || landing == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "没有找到安全的返回位置，仍留在命途遗境；返回记录未清除。"));
            return false;
        }

        float yaw = saved != null && destination.dimension().location().toString()
                .equals(saved.getString("dimension")) ? saved.getFloat("yaw") : player.getYRot();
        float pitch = saved != null && destination.dimension().location().toString()
                .equals(saved.getString("dimension")) ? saved.getFloat("pitch") : player.getXRot();
        if (!teleport(player, destination, landing.position(), yaw, pitch)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "返回传送失败，返回记录仍已保留。"));
            return false;
        }

        player.getPersistentData().remove(RETURN_DATA);
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "已返回来源维度的安全位置。"));
        return true;
    }

    private static void ensureArrivalPlatform(ServerLevel level) {
        BlockPos center = new BlockPos(HUB_X, HUB_Y, HUB_Z);
        level.getChunkAt(center);
        for (int x = HUB_X - HUB_PLATFORM_RADIUS; x <= HUB_X + HUB_PLATFORM_RADIUS; x++) {
            for (int z = HUB_Z - HUB_PLATFORM_RADIUS; z <= HUB_Z + HUB_PLATFORM_RADIUS; z++) {
                BlockPos floor = new BlockPos(x, HUB_Y, z);
                if (level.getBlockState(floor).isAir()) {
                    level.setBlockAndUpdate(floor,
                            PathTerrainBlocks.PATH_STONE_BRICKS.get().defaultBlockState());
                }
            }
        }
        if (level.getBlockState(HUB_RETURN_BEACON).isAir()) {
            level.setBlockAndUpdate(HUB_RETURN_BEACON,
                    StarRailWorldBlocks.STAR_RAIL_BEACON.get().defaultBlockState());
        }
        StarRailRuinSpacingData.get(level).recordRuin(center);
    }

    private static Landing findHubLanding(ServerPlayer player, ServerLevel level) {
        for (int radius = 0; radius <= HUB_PLATFORM_RADIUS; radius++) {
            for (int x = HUB_X - radius; x <= HUB_X + radius; x++) {
                for (int z = HUB_Z - radius; z <= HUB_Z + radius; z++) {
                    if (Math.max(Math.abs(x - HUB_X), Math.abs(z - HUB_Z)) != radius) continue;
                    BlockPos floor = new BlockPos(x, HUB_Y, z);
                    if (!level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) continue;
                    Vec3 position = new Vec3(x + 0.5D, HUB_Y + 1.0D, z + 0.5D);
                    if (isSafeLanding(player, level, position)) return new Landing(position);
                }
            }
        }
        return null;
    }

    private static Landing findSavedLanding(ServerPlayer player, ServerLevel level, CompoundTag saved) {
        if (!saved.contains("x") || !saved.contains("y") || !saved.contains("z")) return null;
        Vec3 exact = new Vec3(saved.getDouble("x"), saved.getDouble("y"), saved.getDouble("z"));
        if (isSafeLanding(player, level, exact)) return new Landing(exact);

        BlockPos origin = BlockPos.containing(exact);
        for (int radius = 1; radius <= 8; radius++) {
            for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
                for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {
                    if (Math.max(Math.abs(x - origin.getX()), Math.abs(z - origin.getZ())) != radius) continue;
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                    Vec3 surface = new Vec3(x + 0.5D, y, z + 0.5D);
                    if (isSafeLanding(player, level, surface)) return new Landing(surface);
                }
            }
        }
        return null;
    }

    private static Landing findWorldSpawn(ServerPlayer player, ServerLevel level) {
        BlockPos spawn = level.getSharedSpawnPos();
        for (int radius = 0; radius <= 16; radius++) {
            for (int x = spawn.getX() - radius; x <= spawn.getX() + radius; x++) {
                for (int z = spawn.getZ() - radius; z <= spawn.getZ() + radius; z++) {
                    if (Math.max(Math.abs(x - spawn.getX()), Math.abs(z - spawn.getZ())) != radius) continue;
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                    Vec3 surface = new Vec3(x + 0.5D, y, z + 0.5D);
                    if (isSafeLanding(player, level, surface)) return new Landing(surface);
                }
            }
        }
        return null;
    }

    private static boolean isSafeLanding(ServerPlayer player, ServerLevel level, Vec3 position) {
        if (position.y < level.getMinBuildHeight() + 1 || position.y >= level.getMaxBuildHeight() - 2) {
            return false;
        }
        BlockPos floor = BlockPos.containing(position.x, position.y - 1.0E-4D, position.z);
        if (!level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) return false;
        AABB targetBox = player.getBoundingBox().move(
                position.x - player.getX(), position.y - player.getY(), position.z - player.getZ());
        return level.noCollision(player, targetBox) && !level.containsAnyLiquid(targetBox);
    }

    private static boolean teleport(ServerPlayer player, ServerLevel destination,
                                     Vec3 position, float yaw, float pitch) {
        return player.teleportTo(destination, position.x, position.y, position.z,
                Set.<RelativeMovement>of(), yaw, pitch);
    }

    private static ServerLevel resolveDimension(ServerPlayer player, CompoundTag saved) {
        ResourceLocation location = ResourceLocation.tryParse(saved.getString("dimension"));
        if (location == null) return null;
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, location);
        return player.serverLevel().getServer().getLevel(key);
    }

    private static void restorePreviousReturn(ServerPlayer player, CompoundTag previous) {
        if (previous == null) player.getPersistentData().remove(RETURN_DATA);
        else player.getPersistentData().put(RETURN_DATA, previous);
    }

    @SubscribeEvent
    public static void copyReturnPointOnDeath(PlayerEvent.Clone event) {
        CompoundTag original = event.getOriginal().getPersistentData();
        if (original.contains(RETURN_DATA)) {
            event.getEntity().getPersistentData().put(RETURN_DATA,
                    original.getCompound(RETURN_DATA).copy());
        }
    }

    private record Landing(Vec3 position) {
    }
}
