package com.starrail.sim;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/** Places the two fixed Astral Express hubs once per world. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarRailTrainHubService {
    public static final int OVERWORLD_CENTER_Y = 280;
    public static final int THE_PATH_CENTER_Y = 280;
    private static final int LOCAL_FLOOR_Y = 5;
    private static final int LOCAL_BEACON_X = 297;
    private static final int LOCAL_BEACON_Y = 6;
    private static final int LOCAL_BEACON_Z = 11;
    private static final int LOCAL_CENTER_X = 294;
    private static final int LOCAL_CENTER_Z = 11;
    private static final Vec3i EXPECTED_SIZE = new Vec3i(424, 23, 23);
    private static final ResourceLocation TEMPLATE_ID = ResourceLocation.fromNamespaceAndPath(
            StarRailSimMod.MOD_ID, "astral_express");
    private static final String DATA_NAME = "starrail_sim_train_hubs";
    private static final Logger LOGGER = LogUtils.getLogger();

    private StarRailTrainHubService() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.overworld();
        HubPlacementData data = HubPlacementData.get(overworld);
        StructureTemplate template = server.getStructureManager().get(TEMPLATE_ID).orElse(null);
        if (template == null) {
            LOGGER.error("Could not find Astral Express structure template {}", TEMPLATE_ID);
            return;
        }
        if (!template.getSize().equals(EXPECTED_SIZE)) {
            LOGGER.error("Astral Express template has unexpected size {}; expected {}. " +
                    "Train hubs were not placed.", template.getSize(), EXPECTED_SIZE);
            return;
        }

        placeIfNeeded(overworld, template, data, false);
        ServerLevel path = server.getLevel(StarRailDimensionIds.THE_PATH);
        if (path == null) {
            LOGGER.error("Could not find dimension {} while placing the Astral Express hub",
                    StarRailDimensionIds.THE_PATH.location());
            return;
        }
        placeIfNeeded(path, template, data, true);
    }

    private static void placeIfNeeded(ServerLevel level, StructureTemplate template,
                                      HubPlacementData data, boolean pathHub) {
        if (pathHub ? data.isPathPlaced() : data.isOverworldPlaced()) return;

        int centerY = pathHub ? THE_PATH_CENTER_Y : OVERWORLD_CENTER_Y;
        BlockPos origin = structureOrigin(centerY);
        int topY = origin.getY() + EXPECTED_SIZE.getY();
        if (origin.getY() < level.getMinBuildHeight() || topY > level.getMaxBuildHeight()) {
            LOGGER.error("Astral Express hub at {} does not fit build height {}..{} in {}",
                    origin, level.getMinBuildHeight(), level.getMaxBuildHeight() - 1,
                    level.dimension().location());
            return;
        }

        boolean placed;
        try {
            placed = template.placeInWorld(level, origin, origin,
                    new StructurePlaceSettings().setIgnoreEntities(false), level.getRandom(),
                    Block.UPDATE_CLIENTS);
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to place Astral Express hub in {} at {}",
                    level.dimension().location(), origin, exception);
            return;
        }
        if (!placed) {
            LOGGER.error("Astral Express template placement returned false in {} at {}",
                    level.dimension().location(), origin);
            return;
        }

        // The reserved cell is clear in the source template so this mod can insert its route beacon.
        level.setBlockAndUpdate(beaconBlock(level.dimension()),
                StarRailWorldBlocks.STAR_RAIL_BEACON.get().defaultBlockState());

        if (pathHub) {
            data.setPathPlaced(true);
            StarRailRuinSpacingData.get(level).recordRuin(landingFeetBlock(StarRailDimensionIds.THE_PATH));
        } else {
            data.setOverworldPlaced(true);
        }
        LOGGER.info("Placed Astral Express hub in {} around (0, {}, 0)",
                level.dimension().location(), centerY);
    }

    private static BlockPos structureOrigin(int centerY) {
        // Local (294, 5, 11) is the floor at world (0, centerY - 1, 0).
        return new BlockPos(-LOCAL_CENTER_X, centerY - LOCAL_FLOOR_Y - 1, -LOCAL_CENTER_Z);
    }

    public static BlockPos landingFeetBlock(ResourceKey<Level> dimension) {
        int y = dimension.equals(StarRailDimensionIds.THE_PATH)
                ? THE_PATH_CENTER_Y : OVERWORLD_CENTER_Y;
        return new BlockPos(0, y, 0);
    }

    public static BlockPos beaconBlock(ResourceKey<Level> dimension) {
        int centerY = dimension.equals(StarRailDimensionIds.THE_PATH)
                ? THE_PATH_CENTER_Y : OVERWORLD_CENTER_Y;
        BlockPos origin = structureOrigin(centerY);
        return origin.offset(LOCAL_BEACON_X, LOCAL_BEACON_Y, LOCAL_BEACON_Z);
    }

    public static boolean isHubPlaced(ServerLevel level) {
        HubPlacementData data = HubPlacementData.get(level.getServer().overworld());
        if (level.dimension().equals(StarRailDimensionIds.THE_PATH)) return data.isPathPlaced();
        if (level.dimension().equals(Level.OVERWORLD)) return data.isOverworldPlaced();
        return false;
    }

    public static boolean hasIntactHubBeacon(ServerLevel level) {
        return isHubPlaced(level)
                && level.getBlockState(beaconBlock(level.dimension()))
                .is(StarRailWorldBlocks.STAR_RAIL_BEACON.get());
    }

    private static final class HubPlacementData extends SavedData {
        private static final String OVERWORLD_KEY = "overworld_placed";
        private static final String PATH_KEY = "path_placed";

        private boolean overworldPlaced;
        private boolean pathPlaced;

        private static HubPlacementData get(ServerLevel overworld) {
            return overworld.getDataStorage().computeIfAbsent(
                    HubPlacementData::load, HubPlacementData::new, DATA_NAME);
        }

        private static HubPlacementData load(CompoundTag tag) {
            HubPlacementData data = new HubPlacementData();
            data.overworldPlaced = tag.getBoolean(OVERWORLD_KEY);
            data.pathPlaced = tag.getBoolean(PATH_KEY);
            return data;
        }

        private boolean isOverworldPlaced() {
            return overworldPlaced;
        }

        private boolean isPathPlaced() {
            return pathPlaced;
        }

        private void setOverworldPlaced(boolean placed) {
            overworldPlaced = placed;
            setDirty();
        }

        private void setPathPlaced(boolean placed) {
            pathPlaced = placed;
            setDirty();
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putBoolean(OVERWORLD_KEY, overworldPlaced);
            tag.putBoolean(PATH_KEY, pathPlaced);
            return tag;
        }
    }
}
