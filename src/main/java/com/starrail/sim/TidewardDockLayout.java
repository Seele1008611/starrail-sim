package com.starrail.sim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

final class TidewardDockLayout {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final Map<Integer, Map<Long, List<StarRailRuinCommands.Placement>>> CHUNKS =
            new ConcurrentHashMap<>();

    private TidewardDockLayout() {}

    private static final class Data {
        static final StarRailRuinCommands.Blueprint BLUEPRINT = load("tideward_dock");
        static final Map<String, List<DaylessArchiveLayout.Site>> SITES =
                DaylessArchiveLayout.readSites("tideward_dock_sites");

        private static StarRailRuinCommands.Blueprint load(String id) {
            try {
                return StarRailRuinCommands.readBlueprint(id, false);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot load tideward dock", e);
            }
        }
    }

    private static final class RevisedMaterials {
        static final StarRailRuinCommands.Blueprint BLUEPRINT = Data.load("tideward_dock_v2");
    }

    static StarRailRuinCommands.Bounds bounds() {
        return Data.BLUEPRINT.bounds();
    }

    static void place(WorldGenLevel level, BlockPos origin, BoundingBox chunkBox, ChunkPos chunk,
                      RandomSource random, int version) {
        var blueprint = version >= 2 ? RevisedMaterials.BLUEPRINT : Data.BLUEPRINT;
        var footing = (version >= 2 ? Blocks.STONE_BRICKS : Blocks.DEEPSLATE_BRICKS).defaultBlockState();
        int phaseX = Math.floorMod(origin.getX(), 16);
        int phaseZ = Math.floorMod(origin.getZ(), 16);
        var buckets = CHUNKS.computeIfAbsent(version * 256 + phaseX * 16 + phaseZ, ignored -> {
            Map<Long, List<StarRailRuinCommands.Placement>> result = new HashMap<>();
            for (var p : blueprint.placements()) {
                long key = ChunkPos.asLong(Math.floorDiv(p.x() + phaseX, 16), Math.floorDiv(p.z() + phaseZ, 16));
                result.computeIfAbsent(key, unused -> new ArrayList<>()).add(p);
            }
            return result;
        });
        long key = ChunkPos.asLong(chunk.x - Math.floorDiv(origin.getX(), 16),
                chunk.z - Math.floorDiv(origin.getZ(), 16));
        var positions = buckets.getOrDefault(key, List.of());
        for (var p : positions) {
            BlockPos pos = origin.offset(p.x(), p.y(), p.z());
            if (!chunkBox.isInside(pos)) continue;
            level.setBlock(pos, blueprint.palette().get(p.paletteIndex()), FLAGS);
        }
        // Extend the fixed footings to the seabed within the current chunk.
        for (var p : positions) {
            if (p.y() != bounds().minY()) continue;
            var base = blueprint.palette().get(p.paletteIndex());
            if (base.isAir() || base.is(Blocks.WATER)) continue;
            BlockPos pos = origin.offset(p.x(), p.y() - 1, p.z());
            while (pos.getY() >= level.getMinBuildHeight() && chunkBox.isInside(pos)) {
                if (level.getBlockState(pos).isSolidRender(level, pos)) break;
                level.setBlock(pos, footing, FLAGS);
                pos = pos.below();
            }
        }
        for (var site : Data.SITES.get("chests")) {
            var pos = site.at(origin);
            if (chunkBox.isInside(pos) && level.getBlockEntity(pos) instanceof BarrelBlockEntity barrel) {
                barrel.getPersistentData().putBoolean(CurioEffects.LOOT, true);
                barrel.setLootTable(new ResourceLocation(site.loot()), random.nextLong());
                barrel.setChanged();
            }
        }
        for (var site : Data.SITES.get("spawners")) {
            var pos = site.at(origin);
            if (chunkBox.isInside(pos) && level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
                DaylessArchiveLayout.configureSpawner(spawner, site.entity());
            }
        }
    }

    static int guards(WorldGenLevel level, BlockPos origin, BoundingBox chunkBox, int mask) {
        return DaylessArchiveLayout.spawnGuards(level, origin, chunkBox, mask, Data.SITES.get("guards"));
    }
}
