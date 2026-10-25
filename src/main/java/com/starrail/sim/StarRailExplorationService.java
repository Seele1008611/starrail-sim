package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.List;
import java.util.Set;

/** Tracks Path discoveries and exploration archives. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarRailExplorationService {
    private static final String PATH_REGION_SUNMEADOW = "sunmeadow";
    private static final String PATH_REGION_GLIMMERWOOD = "glimmerwood";
    private static final String PATH_REGION_STAR_TIDE_OCEAN = "star_tide_ocean";
    private static final String PATH_REGION_CLEAR_SAND_BEACH = "clear_sand_beach";
    private static final String PATH_REGION_STARVEIN_CAVERN = "starvein_cavern";
    private static final String PATH_REGION_STAR_SCREE_HIGHLANDS = "star_scree_highlands";
    private static final String PATH_REGION_STILLTIDE_MARSH = "stilltide_marsh";
    private static final List<String> PATH_RUIN_IDS = List.of(
            "ordinary", "preservation", "destruction", "hunt", "erudition", "harmony",
            "nihility", "abundance", "remembrance", "elation");
    private StarRailExplorationService() {}

    /** Run once on login. Existing path state stays intact; future exploration is dimension-checked. */
    public static void onLogin(ServerPlayer player, IStarRailPathData data) {
        if (data.getCurrentPath().isRealPath()) data.setPathUnlocked(true);
        StarRailAdvancementMigrationService.migrate(player, data);
        if (data.isPathUnlocked()) StarRailPathService.sync(player);
    }

    /** Records Path region exploration for the server-side player tick. */
    public static void tick(ServerPlayer player, IStarRailPathData data) {
        if (!player.level().dimension().equals(StarRailDimensionIds.THE_PATH)) return;
        player.level().getBiome(player.blockPosition()).unwrapKey().ifPresent(key -> {
            String id = key.location().getPath();
            String currentRegionId = switch (id) {
                case PATH_REGION_SUNMEADOW -> PATH_REGION_SUNMEADOW;
                case PATH_REGION_GLIMMERWOOD -> PATH_REGION_GLIMMERWOOD;
                case PATH_REGION_STAR_TIDE_OCEAN -> PATH_REGION_STAR_TIDE_OCEAN;
                case PATH_REGION_CLEAR_SAND_BEACH -> PATH_REGION_CLEAR_SAND_BEACH;
                case PATH_REGION_STARVEIN_CAVERN -> PATH_REGION_STARVEIN_CAVERN;
                case PATH_REGION_STAR_SCREE_HIGHLANDS -> PATH_REGION_STAR_SCREE_HIGHLANDS;
                case PATH_REGION_STILLTIDE_MARSH -> PATH_REGION_STILLTIDE_MARSH;
                default -> null;
            };
            if (currentRegionId != null && data.addDiscoveredPathRegion(currentRegionId)) {
                Component region = Component.translatable("biome.starrail_sim." + currentRegionId);
                player.sendSystemMessage(Component.translatable(
                        "message.starrail_sim.archive.region_found", region));
                StarRailPathService.sync(player);
            }
        });
    }

    /** Unlocks Path content after the player successfully arrives in The Path. */
    public static void markPathVisited(ServerPlayer player) {
        if (!player.serverLevel().dimension().equals(StarRailDimensionIds.THE_PATH)) return;
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            data.setPathUnlocked(true);
            StarRailPathService.sync(player);
        });
    }

    public static void discoverBeacon(ServerPlayer player, BlockPos pos) {
        if (!player.serverLevel().dimension().equals(StarRailDimensionIds.THE_PATH)) return;
        StarRailBeaconArchiveData.get(player.serverLevel()).markPresent(pos.asLong());
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            boolean discovered = data.addDiscoveredPathBeacon(pos.asLong());
            player.sendSystemMessage(Component.translatable(discovered
                            ? "message.starrail_sim.archive.beacon_found"
                            : "message.starrail_sim.archive.beacon_location",
                    pos.getX(), pos.getY(), pos.getZ(), data.getDiscoveredPathBeacons().size()));
            if (discovered) {
                StarRailPathService.sync(player);
            }
        });
    }

    public static void discoverArena(ServerPlayer player, BlockPos pos) {
        if (!player.serverLevel().dimension().equals(StarRailDimensionIds.THE_PATH)) return;
        StarRailAdvancementService.award(player, "exploration/arena_discovered");
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            if (data.addDiscoveredPathArena(pos.asLong())) {
                player.sendSystemMessage(Component.translatable(
                        "message.starrail_sim.archive.arena_found",
                        pos.getX(), pos.getY(), pos.getZ()));
                StarRailPathService.sync(player);
            }
        });
    }

    /** Remove a destroyed beacon from every online player's saved travel archive. */
    public static void removeBeacon(ServerLevel level, BlockPos pos) {
        if (!level.dimension().equals(StarRailDimensionIds.THE_PATH)) return;
        long packedPosition = pos.asLong();
        StarRailBeaconArchiveData.get(level).markRemoved(packedPosition);
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
                if (data.removeDiscoveredPathBeacon(packedPosition)) {
                    StarRailPathService.sync(player);
                }
            });
        }
    }

    public static void pruneRemovedBeacons(ServerPlayer player, IStarRailPathData data) {
        ServerLevel pathLevel = player.getServer().getLevel(StarRailDimensionIds.THE_PATH);
        if (pathLevel == null) return;
        for (long position : StarRailBeaconArchiveData.get(pathLevel).getRemovedPositions()) {
            data.removeDiscoveredPathBeacon(position);
        }
    }

    public static void recordArenaVictory(ServerPlayer player, BlockPos arenaPos) {
        if (!player.serverLevel().dimension().equals(StarRailDimensionIds.THE_PATH)) return;
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            boolean newlyDiscovered = data.addDiscoveredPathArena(arenaPos.asLong());
            data.recordPathArenaVictory();
            StarRailAdvancementService.award(player, "exploration/arena_victory");
            StarRailPathService.sync(player);
            if (newlyDiscovered) {
                player.sendSystemMessage(Component.translatable(
                        "message.starrail_sim.archive.arena_found",
                        arenaPos.getX(), arenaPos.getY(), arenaPos.getZ()));
            }
        });
    }

    public static long regionMask(IStarRailPathData data) {
        long mask = 0L;
        Set<String> regions = data.getDiscoveredPathRegions();
        if (regions.contains(PATH_REGION_SUNMEADOW)) mask |= 1L;
        if (regions.contains(PATH_REGION_GLIMMERWOOD)) mask |= 2L;
        if (regions.contains(PATH_REGION_STAR_TIDE_OCEAN)) mask |= 4L;
        if (regions.contains(PATH_REGION_CLEAR_SAND_BEACH)) mask |= 8L;
        if (regions.contains(PATH_REGION_STARVEIN_CAVERN)) mask |= 16L;
        if (regions.contains(PATH_REGION_STAR_SCREE_HIGHLANDS)) mask |= 32L;
        if (regions.contains(PATH_REGION_STILLTIDE_MARSH)) mask |= 64L;
        return mask;
    }

    public static int ruinMask(IStarRailPathData data) {
        int mask = 0;
        for (int index = 0; index < PATH_RUIN_IDS.size(); index++) {
            if (data.getDiscoveredPathRuins().contains(PATH_RUIN_IDS.get(index))) mask |= 1 << index;
        }
        return mask;
    }

    public static int ruinPositionMask(IStarRailPathData data) {
        int mask = 0;
        for (int index = 0; index < PATH_RUIN_IDS.size(); index++) {
            if (data.getDiscoveredPathRuinPositions().containsKey(PATH_RUIN_IDS.get(index))) {
                mask |= 1 << index;
            }
        }
        return mask;
    }

    public static long[] ruinPositions(IStarRailPathData data) {
        long[] positions = new long[PATH_RUIN_IDS.size()];
        for (int index = 0; index < PATH_RUIN_IDS.size(); index++) {
            Long position = data.getDiscoveredPathRuinPositions().get(PATH_RUIN_IDS.get(index));
            if (position != null) positions[index] = position;
        }
        return positions;
    }

    /** Persist a player's label only for a coordinate present in their own archive. */
    public static void renameArchiveRecord(ServerPlayer player, String recordKey, String rawName) {
        String name = sanitizeArchiveName(rawName);
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            if (!isArchiveRecordOwned(data, recordKey)) return;
            data.setArchiveCustomName(recordKey, name);
            player.sendSystemMessage(Component.translatable(name.isEmpty()
                    ? "message.starrail_sim.archive.name_cleared"
                    : "message.starrail_sim.archive.name_saved"));
            StarRailPathService.sync(player);
        });
    }

    private static boolean isArchiveRecordOwned(IStarRailPathData data, String key) {
        if (key == null) return false;
        if (key.startsWith("ruin:")) {
            String ruinId = key.substring("ruin:".length());
            return PATH_RUIN_IDS.contains(ruinId)
                    && data.getDiscoveredPathRuinPositions().containsKey(ruinId);
        }
        String[] parts = key.split(":", 2);
        if (parts.length != 2) return false;
        try {
            long position = Long.parseLong(parts[1]);
            return switch (parts[0]) {
                case "beacon" -> data.getDiscoveredPathBeacons().contains(position);
                case "arena" -> data.getDiscoveredPathArenas().contains(position);
                default -> false;
            };
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private static String sanitizeArchiveName(String rawName) {
        StringBuilder clean = new StringBuilder();
        rawName.codePoints().filter(codePoint -> !Character.isISOControl(codePoint))
                .limit(32).forEach(clean::appendCodePoint);
        return clean.toString().strip();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRuinChestInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled() || event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND
                || !(event.getEntity() instanceof ServerPlayer player)
                || !player.serverLevel().dimension().equals(StarRailDimensionIds.THE_PATH)
                || !(player.serverLevel().getBlockEntity(event.getPos())
                instanceof ChestBlockEntity chest)) return;

        String ruinId = StarRailRuinGuardService.getArchiveRuinId(chest);
        if (ruinId.isBlank()) {
            String lootTableId = chest.saveWithFullMetadata().getString("LootTable");
            ResourceLocation lootTable = ResourceLocation.tryParse(lootTableId);
            if (lootTable == null || !lootTable.getNamespace().equals(StarRailSimMod.MOD_ID)
                    || !lootTable.getPath().startsWith("chests/ruin_")) return;
            ruinId = lootTable.getPath().substring("chests/ruin_".length());
        }
        String resolvedRuinId = ruinId;
        if (!PATH_RUIN_IDS.contains(resolvedRuinId)) return;
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            boolean firstDiscovery = data.addDiscoveredPathRuin(resolvedRuinId);
            boolean firstLocation = data.addDiscoveredPathRuinPosition(
                    resolvedRuinId, event.getPos().asLong());
            if (firstDiscovery || firstLocation) {
                Component ruin = resolvedRuinId.equals("ordinary")
                        ? Component.translatable("guide.starrail_sim.archive.ordinary_ruin")
                        : Component.literal(StarRailPath.byId(resolvedRuinId).getDisplayName());
                player.sendSystemMessage(Component.translatable(
                        "message.starrail_sim.archive.ruin_found", ruin,
                        event.getPos().getX(), event.getPos().getY(), event.getPos().getZ()));
                StarRailPathService.sync(player);
            }
        });
    }

    @SubscribeEvent
    public static void onRuinChestOpened(PlayerContainerEvent.Open event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !player.serverLevel().dimension().equals(StarRailDimensionIds.THE_PATH)
                || !(event.getContainer() instanceof ChestMenu chestMenu)) return;

        Container openedContainer = chestMenu.getContainer();
        if (openedContainer instanceof ChestBlockEntity chest
                && isOrdinaryRuinChest(chest)) {
            StarRailAdvancementService.award(player, "exploration/ordinary_ruin");
            return;
        }
        if (!(openedContainer instanceof CompoundContainer doubleChest)) return;

        BlockPos origin = player.blockPosition();
        BlockPos.betweenClosedStream(origin.offset(-8, -4, -8), origin.offset(8, 4, 8))
                .map(player.serverLevel()::getBlockEntity)
                .filter(ChestBlockEntity.class::isInstance)
                .map(ChestBlockEntity.class::cast)
                .filter(doubleChest::contains)
                .filter(StarRailExplorationService::isOrdinaryRuinChest)
                .findFirst()
                .ifPresent(chest -> StarRailAdvancementService.award(
                        player, "exploration/ordinary_ruin"));
    }

    private static boolean isOrdinaryRuinChest(ChestBlockEntity chest) {
        return StarRailRuinGuardService.getArchiveRuinId(chest).equals("ordinary");
    }

}
