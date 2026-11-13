package com.starrail.sim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

final class BrokenRingObservatoryLayout {
    static final int HEADROOM = 3;
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private BrokenRingObservatoryLayout() {}

    private static final class Column {
        final List<StarRailRuinCommands.Placement> blocks = new ArrayList<>();
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        void include(int first, int last) {
            min = Math.min(min, first);
            max = Math.max(max, last);
        }
    }

    private static final class Data {
        static final StarRailRuinCommands.Blueprint BLUEPRINT = load();
        static final Map<String, List<DaylessArchiveLayout.Site>> SITES =
                DaylessArchiveLayout.readSites("broken_ring_observatory_sites");
        static final Map<Long, Column> COLUMNS = columns();

        private static StarRailRuinCommands.Blueprint load() {
            try {
                return StarRailRuinCommands.readBlueprint("broken_ring_observatory", false);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot load Broken Ring Observatory", e);
            }
        }

        private static Map<Long, Column> columns() {
            var columns = new HashMap<Long, Column>();
            for (var placement : BLUEPRINT.placements()) {
                var column = columns.computeIfAbsent(ChunkPos.asLong(placement.x(), placement.z()), ignored -> new Column());
                column.blocks.add(placement);
                column.include(placement.y(), placement.y() + HEADROOM);
            }
            // Keep the enclosed stair shaft and the open approach clear even in columns without a wall.
            for (int x = -14; x <= 14; x++) for (int z = -11; z <= 11; z++) {
                columns.computeIfAbsent(ChunkPos.asLong(x, z), ignored -> new Column()).include(0, 103);
            }
            for (int x = -3; x <= 3; x++) for (int z = 12; z <= 20; z++) {
                columns.computeIfAbsent(ChunkPos.asLong(x, z), ignored -> new Column()).include(0, 5);
            }
            return Map.copyOf(columns);
        }
    }

    static StarRailRuinCommands.Bounds bounds() {
        return Data.BLUEPRINT.bounds();
    }

    static void place(WorldGenLevel level, BlockPos origin, BoundingBox chunkBox, RandomSource random) {
        var bounds = bounds();
        int minX = Math.max(bounds.minX(), chunkBox.minX() - origin.getX());
        int maxX = Math.min(bounds.maxX(), chunkBox.maxX() - origin.getX());
        int minZ = Math.max(bounds.minZ(), chunkBox.minZ() - origin.getZ());
        int maxZ = Math.min(bounds.maxZ(), chunkBox.maxZ() - origin.getZ());
        var pos = new BlockPos.MutableBlockPos();
        var air = Blocks.AIR.defaultBlockState();
        var foundation = Blocks.SMOOTH_BASALT.defaultBlockState();
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            Column column = Data.COLUMNS.get(ChunkPos.asLong(x, z));
            if (column == null) continue;
            int first = Math.max(chunkBox.minY(), Math.max(level.getMinBuildHeight(), origin.getY() + column.min));
            int last = Math.min(chunkBox.maxY(), Math.min(level.getMaxBuildHeight() - 1, origin.getY() + column.max));
            for (int y = first; y <= last; y++) {
                pos.set(origin.getX() + x, y, origin.getZ() + z);
                if (!level.getBlockState(pos).isAir()) level.setBlock(pos, air, FLAGS);
            }
            for (var placement : column.blocks) {
                pos.setWithOffset(origin, x, placement.y(), z);
                if (pos.getY() >= first && pos.getY() <= last) {
                    level.setBlock(pos, Data.BLUEPRINT.palette().get(placement.paletteIndex()), FLAGS);
                }
            }
            if (column.min != bounds.minY()) continue;
            int bottom = Math.max(chunkBox.minY(), level.getMinBuildHeight());
            for (int y = origin.getY() + bounds.minY() - 1; y >= bottom; y--) {
                pos.set(origin.getX() + x, y, origin.getZ() + z);
                if (!chunkBox.isInside(pos) || level.getBlockState(pos).isSolidRender(level, pos)) break;
                level.setBlock(pos, foundation, FLAGS);
            }
        }
        for (var site : Data.SITES.get("spawners")) {
            var target = site.at(origin);
            if (chunkBox.isInside(target) && level.getBlockEntity(target) instanceof SpawnerBlockEntity spawner) {
                DaylessArchiveLayout.configureSpawner(spawner, site.entity());
            }
        }
        for (var site : Data.SITES.get("chests")) {
            var target = site.at(origin);
            if (chunkBox.isInside(target) && level.getBlockEntity(target) instanceof BarrelBlockEntity barrel) {
                barrel.getPersistentData().putBoolean(CurioEffects.LOOT, true);
                barrel.setLootTable(new ResourceLocation(site.loot()), random.nextLong());
                barrel.setChanged();
            }
        }
    }

    static int guards(WorldGenLevel level, BlockPos origin, BoundingBox chunkBox, int mask) {
        return DaylessArchiveLayout.spawnGuards(level, origin, chunkBox, mask, Data.SITES.get("guards"));
    }
}
