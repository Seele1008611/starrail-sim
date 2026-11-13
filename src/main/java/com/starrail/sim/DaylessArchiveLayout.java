package com.starrail.sim;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.registries.ForgeRegistries;

/** Complete reviewed layout, clipped to the active generation chunk, with persistent structure encounters. */
final class DaylessArchiveLayout {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final Map<Integer, Map<Long, List<StarRailRuinCommands.Placement>>> BUCKETS = new ConcurrentHashMap<>();
    record Site(int x, int y, int z, String entity, String loot) {
        BlockPos at(BlockPos origin) { return origin.offset(x, y, z); }
    }
    private DaylessArchiveLayout() {}

    private static final class Data {
        static final StarRailRuinCommands.Blueprint BLUEPRINT = blueprint("dayless_archive");
        static final Map<String, List<Site>> SITES = readSites("dayless_archive_sites");
        private static StarRailRuinCommands.Blueprint blueprint(String id) {
            try { return StarRailRuinCommands.readBlueprint(id, false); }
            catch (IOException e) { throw new IllegalStateException("Cannot load dayless archive", e); }
        }

    }

    static Map<String, List<Site>> readSites(String id) {
        try (var raw = DaylessArchiveLayout.class.getResourceAsStream("/data/starrail_sim/ruins/" + id + ".json")) {
            if (raw == null) throw new IOException("Missing encounter manifest: " + id);
            var root = JsonParser.parseReader(new InputStreamReader(raw, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, List<Site>> result = new HashMap<>();
            for (String kind : List.of("spawners", "guards", "chests")) {
                JsonArray entries = root.getAsJsonArray(kind);
                List<Site> sites = new ArrayList<>();
                for (var entry : entries) {
                    var v = entry.getAsJsonObject();
                    sites.add(new Site(v.get("x").getAsInt(), v.get("y").getAsInt(), v.get("z").getAsInt(),
                            v.has("entity") ? v.get("entity").getAsString() : null,
                            v.has("loot") ? v.get("loot").getAsString() : null));
                }
                result.put(kind, List.copyOf(sites));
            }
            return Map.copyOf(result);
        } catch (IOException e) { throw new IllegalStateException("Cannot load encounters: " + id, e); }
    }

    private static final class Legacy {
        static final StarRailRuinCommands.Blueprint BLUEPRINT = Data.blueprint("dayless_archive_v1");
        static final Map<String, List<Site>> SITES = readSites("dayless_archive_v1_sites");
    }

    private static final class Previous {
        static final StarRailRuinCommands.Blueprint BLUEPRINT = Data.blueprint("dayless_archive_v2");
        static final Map<String, List<Site>> SITES = readSites("dayless_archive_v2_sites");
    }

    private static final class RevisedWings {
        static final StarRailRuinCommands.Blueprint BLUEPRINT = Data.blueprint("dayless_archive_v4");
    }

    private static final class RevisedMaterials {
        static final StarRailRuinCommands.Blueprint BLUEPRINT = Data.blueprint("dayless_archive_v5");
    }

    private static StarRailRuinCommands.Blueprint blueprintFor(int version) {
        if (version >= 5) return RevisedMaterials.BLUEPRINT;
        if (version >= 4) return RevisedWings.BLUEPRINT;
        if (version == 3) return Data.BLUEPRINT;
        return version == 2 ? Previous.BLUEPRINT : Legacy.BLUEPRINT;
    }

    static void place(WorldGenLevel level, BlockPos origin, BoundingBox chunkBox, ChunkPos chunk, RandomSource random, int version) {
        var blueprint = blueprintFor(version);
        var sites = version >= 3 ? Data.SITES : version == 2 ? Previous.SITES : Legacy.SITES;
        clear(level, origin, chunkBox);
        int phaseX = Math.floorMod(origin.getX(), 16), phaseZ = Math.floorMod(origin.getZ(), 16);
        var buckets = BUCKETS.computeIfAbsent(version * 256 + phaseX * 16 + phaseZ, unused -> {
            Map<Long, List<StarRailRuinCommands.Placement>> map = new HashMap<>();
            for (var p : blueprint.placements()) {
                long key = ChunkPos.asLong(Math.floorDiv(p.x() + phaseX, 16), Math.floorDiv(p.z() + phaseZ, 16));
                map.computeIfAbsent(key, ignored -> new ArrayList<>()).add(p);
            }
            return map;
        });
        long key = ChunkPos.asLong(chunk.x - Math.floorDiv(origin.getX(), 16), chunk.z - Math.floorDiv(origin.getZ(), 16));
        for (var p : buckets.getOrDefault(key, List.of())) {
            BlockPos pos = origin.offset(p.x(), p.y(), p.z());
            if (chunkBox.isInside(pos)) level.setBlock(pos, blueprint.palette().get(p.paletteIndex()), FLAGS);
        }
        for (Site site : sites.get("chests")) {
            BlockPos pos = site.at(origin);
            if (chunkBox.isInside(pos) && level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
                chest.getPersistentData().putBoolean(CurioEffects.LOOT, true);
                chest.setLootTable(new ResourceLocation(site.loot()), random.nextLong());
                chest.setChanged();
            }
        }
        for (Site site : sites.get("spawners")) {
            BlockPos pos = site.at(origin);
            if (chunkBox.isInside(pos) && level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
                configureSpawner(spawner, site.entity());
            }
        }
    }

    private static void clear(WorldGenLevel level, BlockPos origin, BoundingBox chunkBox) {
        int minX = Math.max(-66, chunkBox.minX() - origin.getX()), maxX = Math.min(73, chunkBox.maxX() - origin.getX());
        int minZ = Math.max(-54, chunkBox.minZ() - origin.getZ()), maxZ = Math.min(61, chunkBox.maxZ() - origin.getZ());
        var pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) for (int y = -11; y <= 55; y++) {
            pos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
            if (chunkBox.isInside(pos)) level.setBlock(pos, Blocks.AIR.defaultBlockState(), FLAGS);
        }
    }

    static void configureSpawner(SpawnerBlockEntity spawner, String id) {
        CompoundTag tag = spawner.saveWithoutMetadata(), data = new CompoundTag(), entity = new CompoundTag();
        entity.putString("id", id); data.put("entity", entity);
        CompoundTag rules = new CompoundTag(), light = new CompoundTag();
        light.putInt("min_inclusive", 0); light.putInt("max_inclusive", 15);
        rules.put("block_light_limit", light.copy()); rules.put("sky_light_limit", light.copy());
        data.put("custom_spawn_rules", rules);
        tag.put("SpawnData", data); tag.remove("SpawnPotentials");
        tag.putShort("Delay", (short) 100);
        tag.putShort("MinSpawnDelay", (short) 280); tag.putShort("MaxSpawnDelay", (short) 520);
        tag.putShort("SpawnCount", (short) 2); tag.putShort("MaxNearbyEntities", (short) 4);
        tag.putShort("RequiredPlayerRange", (short) 12); tag.putShort("SpawnRange", (short) 3);
        spawner.load(tag);
        spawner.getPersistentData().putBoolean(CurioEffects.LOOT, true);
        spawner.setChanged();
    }

    static int guards(WorldGenLevel level, BlockPos origin, BoundingBox chunkBox, int mask, int version) {
        var sites = (version >= 3 ? Data.SITES : version == 2 ? Previous.SITES : Legacy.SITES).get("guards");
        return spawnGuards(level, origin, chunkBox, mask, sites);
    }

    static int spawnGuards(WorldGenLevel level, BlockPos origin, BoundingBox chunkBox, int mask, List<Site> sites) {
        for (int i = 0; i < sites.size(); i++) {
            if ((mask & (1 << i)) != 0) continue;
            Site site = sites.get(i); BlockPos pos = site.at(origin);
            if (!chunkBox.isInside(pos)) continue;
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(site.entity()));
            if (type == null) throw new IllegalStateException("Unknown structure guard: " + site.entity());
            if (!(type.create(level.getLevel()) instanceof Mob mob)) continue;
            mob.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 180, 0);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
            mob.setPersistenceRequired();
            if (level.addFreshEntity(mob)) mask |= 1 << i;
        }
        return mask;
    }
}
