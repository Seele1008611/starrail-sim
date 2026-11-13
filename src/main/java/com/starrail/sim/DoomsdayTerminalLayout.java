package com.starrail.sim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Reviewed terminal blueprint, applied only to the current generation chunk. */
final class DoomsdayTerminalLayout {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final Map<Integer, Map<Long, List<StarRailRuinCommands.Placement>>> CHUNKS =
            new ConcurrentHashMap<>();
    private static final int[][] GUARDS = {{-34, 1, -17}, {13, -9, -33}, {42, 1, -31}};

    private DoomsdayTerminalLayout() {}

    private static final class BlueprintHolder {
        private static final StarRailRuinCommands.Blueprint VALUE = load("doomsday_terminal");
        private static final StarRailRuinCommands.Blueprint REVISED = load("doomsday_terminal_v3");
        private static final StarRailRuinCommands.Blueprint LEGACY = load("doomsday_terminal_v1");
        private static StarRailRuinCommands.Blueprint load(String id) {
            try { return StarRailRuinCommands.readBlueprint(id, false); }
            catch (IOException e) { throw new IllegalStateException("Cannot read doomsday terminal blueprint", e); }
        }
    }

    static void place(WorldGenLevel level, BlockPos origin, BoundingBox chunkBox, ChunkPos chunk,
                      RandomSource random, int landingY, int version) {
        var blueprint = version >= 3 ? BlueprintHolder.REVISED : version == 2 ? BlueprintHolder.VALUE : BlueprintHolder.LEGACY;
        if (version >= 2) clearEnvelope(level, origin, chunkBox);
        int phaseX = Math.floorMod(origin.getX(), 16), phaseZ = Math.floorMod(origin.getZ(), 16);
        var buckets = CHUNKS.computeIfAbsent(version * 256 + phaseX * 16 + phaseZ, phase -> {
            Map<Long, List<StarRailRuinCommands.Placement>> result = new HashMap<>();
            for (var p : blueprint.placements()) {
                long key = ChunkPos.asLong(Math.floorDiv(p.x() + phaseX, 16), Math.floorDiv(p.z() + phaseZ, 16));
                result.computeIfAbsent(key, unused -> new ArrayList<>()).add(p);
            }
            return result;
        });
        long key = ChunkPos.asLong(chunk.x - Math.floorDiv(origin.getX(), 16),
                chunk.z - Math.floorDiv(origin.getZ(), 16));
        for (var p : buckets.getOrDefault(key, List.of())) {
            BlockPos pos = origin.offset(p.x(), p.y(), p.z());
            if (chunkBox.isInside(pos)) level.setBlock(pos, blueprint.palette().get(p.paletteIndex()), FLAGS);
        }
        approach(level, origin, chunkBox, landingY, version);
        if (version >= 2) foundations(level, origin, chunkBox);
        else support(level, origin, chunkBox);
        chest(level, origin.offset(-44, 1, -11), chunkBox, "terminal_supplies", random);
        chest(level, origin.offset(46, 8, -17), chunkBox, "terminal_salvage", random);
        chest(level, origin.offset(9, -9, -48), chunkBox, "terminal_salvage", random);
        spawner(level, origin.offset(-46, 1, -31), chunkBox, VoidrangerEntities.VOIDRANGER_REAVER.get());
        spawner(level, origin.offset(-10, -9, -46), chunkBox, VoidrangerEntities.VOIDRANGER_DISTORTER.get());
    }

    private static void clearEnvelope(WorldGenLevel level, BlockPos origin, BoundingBox chunkBox) {
        int minX = Math.max(-52, chunkBox.minX() - origin.getX());
        int maxX = Math.min(52, chunkBox.maxX() - origin.getX());
        int minZ = Math.max(-89, chunkBox.minZ() - origin.getZ());
        int maxZ = Math.min(25, chunkBox.maxZ() - origin.getZ());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            for (int y = -23; y <= 25; y++) {
                pos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                if (chunkBox.isInside(pos)) level.setBlock(pos, Blocks.AIR.defaultBlockState(), FLAGS);
            }
        }
    }

    private static void foundations(WorldGenLevel level, BlockPos origin, BoundingBox chunkBox) {
        // Extend each designed footing continuously through obstructing terrain; never stop at a solid block.
        for (var p : BlueprintHolder.VALUE.placements()) {
            if (p.y() != -23 || BlueprintHolder.VALUE.palette().get(p.paletteIndex()).isAir()) continue;
            BlockPos column = origin.offset(p.x(), 0, p.z());
            if (column.getX() < chunkBox.minX() || column.getX() > chunkBox.maxX()
                    || column.getZ() < chunkBox.minZ() || column.getZ() > chunkBox.maxZ()) continue;
            for (int y = -24; y >= -40; y--) {
                set(level, chunkBox, origin.offset(p.x(), y, p.z()), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
            }
        }
    }

    private static void chest(WorldGenLevel level, BlockPos pos, BoundingBox chunkBox, String table,
                              RandomSource random) {
        if (chunkBox.isInside(pos) && level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            chest.getPersistentData().putBoolean(CurioEffects.LOOT, true);
            chest.setLootTable(new ResourceLocation(StarRailSimMod.MOD_ID, "chests/" + table), random.nextLong());
            chest.setChanged();
        }
    }

    private static void spawner(WorldGenLevel level, BlockPos pos, BoundingBox chunkBox, EntityType<?> type) {
        if (!chunkBox.isInside(pos) || !(level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner)) return;
        CompoundTag tag = spawner.saveWithoutMetadata();
        CompoundTag entity = new CompoundTag();
        entity.putString("id", EntityType.getKey(type).toString());
        CompoundTag data = new CompoundTag();
        data.put("entity", entity);
        // These lit industrial rooms are intentional encounters, independent of natural darkness checks.
        CompoundTag rules = new CompoundTag(), light = new CompoundTag();
        light.putInt("min_inclusive", 0); light.putInt("max_inclusive", 15);
        rules.put("block_light_limit", light.copy()); rules.put("sky_light_limit", light.copy());
        data.put("custom_spawn_rules", rules);
        tag.put("SpawnData", data); tag.remove("SpawnPotentials");
        tag.putShort("Delay", (short) 60);
        tag.putShort("MinSpawnDelay", (short) 220); tag.putShort("MaxSpawnDelay", (short) 420);
        tag.putShort("SpawnCount", (short) 2); tag.putShort("MaxNearbyEntities", (short) 5);
        tag.putShort("RequiredPlayerRange", (short) 16); tag.putShort("SpawnRange", (short) 4);
        spawner.load(tag);
        spawner.getPersistentData().putBoolean(CurioEffects.LOOT, true);
        spawner.setChanged();
    }

    static int guards(WorldGenLevel level, BlockPos origin, BoundingBox chunkBox, int mask) {
        for (int i = 0; i < GUARDS.length; i++) {
            if ((mask & (1 << i)) != 0) continue;
            int[] p = GUARDS[i]; BlockPos pos = origin.offset(p[0], p[1], p[2]);
            if (!chunkBox.isInside(pos)) continue;
            Mob mob = (i == 0 ? VoidrangerEntities.VOIDRANGER_REAVER.get()
                    : VoidrangerEntities.VOIDRANGER_DISTORTER.get()).create(level.getLevel());
            if (mob == null) continue;
            mob.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 180, 0);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
            mob.setPersistenceRequired();
            if (level.addFreshEntity(mob)) mask |= 1 << i;
        }
        return mask;
    }

    private static void approach(WorldGenLevel level, BlockPos origin, BoundingBox chunkBox, int landingY,
                                 int version) {
        boolean enclosedLayout = version >= 2;
        // Serialized base-height landing keeps cross-chunk stair rows consistent after save/reload.
        for (int z = -90; z >= -124; z--) for (int x = -4; x <= 4; x++) {
            BlockPos column = origin.offset(x, 0, z);
            if (column.getX() < chunkBox.minX() || column.getX() > chunkBox.maxX()
                    || column.getZ() < chunkBox.minZ() || column.getZ() > chunkBox.maxZ()) continue;
            int y = (int) Math.floor((-z - 89) * landingY / 35.0);
            int next = (int) Math.floor((-z - 90) * landingY / 35.0);
            for (int yy = y + 1; yy <= (enclosedLayout ? 25 : y + 5); yy++) {
                set(level, chunkBox, origin.offset(x, yy, z), Blocks.AIR.defaultBlockState());
            }
            BlockState state = next > y ? Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH) : approachDeck(version, y);
            set(level, chunkBox, origin.offset(x, y, z), state);
            set(level, chunkBox, origin.offset(x, y - 1, z), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
            if (enclosedLayout) {
                for (int yy = -24; yy < y - 1; yy++) {
                    set(level, chunkBox, origin.offset(x, yy, z), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
                }
                if (Math.abs(x) == 4) {
                    set(level, chunkBox, origin.offset(x, y + 1, z),
                            z % 7 == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : approachDeck(version, y + 1));
                }
            }
        }
    }

    private static BlockState approachDeck(int version, int y) {
        return (version >= 3 && y >= 0 ? Blocks.GRAY_CONCRETE : Blocks.POLISHED_DEEPSLATE).defaultBlockState();
    }

    private static void support(WorldGenLevel level, BlockPos origin, BoundingBox chunkBox) {
        for (int x : new int[] {-39, 0, 39}) for (int z : new int[] {-49, -25, 10}) {
            if (x != 0 && z != -25) continue;
            BlockPos column = origin.offset(x, 0, z);
            if (column.getX() < chunkBox.minX() || column.getX() > chunkBox.maxX()
                    || column.getZ() < chunkBox.minZ() || column.getZ() > chunkBox.maxZ()) continue;
            int bottom = 0;
            for (var p : BlueprintHolder.LEGACY.placements()) {
                if (p.x() == x && p.z() == z && !BlueprintHolder.LEGACY.palette().get(p.paletteIndex()).isAir()) {
                    bottom = Math.min(bottom, p.y());
                }
            }
            for (int y = bottom - 1; y >= -40; y--) {
                BlockPos pos = origin.offset(x, y, z);
                if (!chunkBox.isInside(pos)) continue;
                if (level.getBlockState(pos).isSolidRender(level, pos)) break;
                set(level, chunkBox, pos, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
            }
        }
    }

    private static void set(WorldGenLevel level, BoundingBox chunkBox, BlockPos pos, BlockState state) {
        if (chunkBox.isInside(pos)) level.setBlock(pos, state, FLAGS);
    }
}
