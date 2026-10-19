package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** 保存已生成遗迹的锚点，并为寻迹遗迹执行统一的水平间距检查。 */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarRailRuinSpacingData extends SavedData {
    private static final String DATA_NAME = "starrail_sim_ruin_spacing";
    private static final String ANCHORS_KEY = "anchors";
    private static final String ORDINARY_ANCHORS_KEY = "ordinary_anchors";
    private static final String ARENA_ANCHORS_KEY = "arena_anchors";
    private static final long MIN_DISTANCE_SQUARED = 400L * 400L;
    private static final List<String> SPACED_STRUCTURE_IDS = List.of(
            "ordinary_floating_ruin", "relic_trial_arena");

    private final Set<Long> anchors = new LinkedHashSet<>();
    private final Set<Long> ordinaryAnchors = new LinkedHashSet<>();
    private final Set<Long> arenaAnchors = new LinkedHashSet<>();

    private StarRailRuinSpacingData() {
    }

    /** 从当前维度的世界数据中读取遗迹锚点表。 */
    public static StarRailRuinSpacingData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                StarRailRuinSpacingData::load,
                StarRailRuinSpacingData::new,
                DATA_NAME);
    }

    private static StarRailRuinSpacingData load(CompoundTag tag) {
        StarRailRuinSpacingData data = new StarRailRuinSpacingData();
        ListTag savedAnchors = tag.getList(ANCHORS_KEY, Tag.TAG_LONG);
        for (int i = 0; i < savedAnchors.size(); i++) {
            data.anchors.add(((net.minecraft.nbt.LongTag) savedAnchors.get(i)).getAsLong());
        }
        ListTag savedOrdinaryAnchors = tag.getList(ORDINARY_ANCHORS_KEY, Tag.TAG_LONG);
        for (int i = 0; i < savedOrdinaryAnchors.size(); i++) {
            long anchor = ((net.minecraft.nbt.LongTag) savedOrdinaryAnchors.get(i)).getAsLong();
            data.ordinaryAnchors.add(anchor);
            data.anchors.add(anchor);
        }
        ListTag savedArenaAnchors = tag.getList(ARENA_ANCHORS_KEY, Tag.TAG_LONG);
        for (int i = 0; i < savedArenaAnchors.size(); i++) {
            long anchor = ((net.minecraft.nbt.LongTag) savedArenaAnchors.get(i)).getAsLong();
            data.arenaAnchors.add(anchor);
            data.anchors.add(anchor);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag savedAnchors = new ListTag();
        for (long anchor : anchors) {
            savedAnchors.add(net.minecraft.nbt.LongTag.valueOf(anchor));
        }
        tag.put(ANCHORS_KEY, savedAnchors);
        ListTag savedOrdinaryAnchors = new ListTag();
        for (long anchor : ordinaryAnchors) {
            savedOrdinaryAnchors.add(net.minecraft.nbt.LongTag.valueOf(anchor));
        }
        tag.put(ORDINARY_ANCHORS_KEY, savedOrdinaryAnchors);
        ListTag savedArenaAnchors = new ListTag();
        for (long anchor : arenaAnchors) {
            savedArenaAnchors.add(net.minecraft.nbt.LongTag.valueOf(anchor));
        }
        tag.put(ARENA_ANCHORS_KEY, savedArenaAnchors);
        return tag;
    }

    /** 检查给定 X/Z 是否与所有已记录遗迹至少相距 400 格。 */
    public boolean hasClearance(int x, int z) {
        return hasClearance(x, z, false);
    }

    /** 同时避开地图上已记录遗迹与本世界种子确定的自然生成候选点。 */
    public boolean hasClearance(ServerLevel level, int x, int z) {
        return hasClearance(level, x, z, false);
    }

    /** overwrite 命令可重建同一寻迹锚点，但仍须避开所有其他遗迹。 */
    public boolean hasClearance(ServerLevel level, int x, int z, boolean allowSameSite) {
        if (!hasClearance(x, z, allowSameSite)
                || !StarRailDimensionService.hasRuinHubClearance(level, x, z)) {
            return false;
        }

        var structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (String structureId : SPACED_STRUCTURE_IDS) {
            ResourceKey<Structure> structureKey = ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(StarRailSimMod.MOD_ID, structureId));
            Holder<Structure> structureHolder = structureRegistry.getHolderOrThrow(structureKey);
            for (StructurePlacement placement : level.getChunkSource().getGeneratorState()
                    .getPlacementsForStructure(structureHolder)) {
                if (!(placement instanceof RandomSpreadStructurePlacement spread)) {
                    continue;
                }

                int chunkX = Math.floorDiv(x, 16);
                int chunkZ = Math.floorDiv(z, 16);
                int regionX = Math.floorDiv(chunkX, spread.spacing());
                int regionZ = Math.floorDiv(chunkZ, spread.spacing());
                int regionRadius = (int) Math.ceil(400.0D / (spread.spacing() * 16.0D)) + 1;
                for (int offsetX = -regionRadius; offsetX <= regionRadius; offsetX++) {
                    for (int offsetZ = -regionRadius; offsetZ <= regionRadius; offsetZ++) {
                        ChunkPos candidateChunk = spread.getPotentialStructureChunk(
                                level.getSeed(), regionX + offsetX, regionZ + offsetZ);
                        long dx = (long) x - candidateChunk.getMiddleBlockX();
                        long dz = (long) z - candidateChunk.getMiddleBlockZ();
                        if (dx * dx + dz * dz < MIN_DISTANCE_SQUARED) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /** overwrite 命令可在原遗迹锚点重建自身，但仍须避开其他遗迹。 */
    public boolean hasClearance(int x, int z, boolean allowSameSite) {
        for (long packedAnchor : anchors) {
            BlockPos anchor = BlockPos.of(packedAnchor);
            long dx = (long) x - anchor.getX();
            long dz = (long) z - anchor.getZ();
            if (allowSameSite && dx == 0 && dz == 0) {
                continue;
            }
            if (dx * dx + dz * dz < MIN_DISTANCE_SQUARED) {
                return false;
            }
        }
        return true;
    }

    /** 记录一个遗迹锚点；同一遗迹跨多个区块加载时只保存一次。 */
    public void recordRuin(BlockPos origin) {
        if (anchors.add(origin.asLong())) {
            setDirty();
        }
    }

    /** 记录普通母体，供跨区块距离的定位命令直接查询。 */
    public void recordOrdinaryRuin(BlockPos origin) {
        boolean changed = anchors.add(origin.asLong());
        changed |= ordinaryAnchors.add(origin.asLong());
        if (changed) {
            setDirty();
        }
    }

    /** 返回已探索范围中距给定位置最近的普通母体锚点。 */
    public BlockPos findNearestOrdinaryRuin(int x, int z) {
        return findNearest(ordinaryAnchors, x, z);
    }

    /** 登记自然生成或手动建造的遗器决斗场，供定位命令查找。 */
    public void recordArena(BlockPos origin) {
        boolean changed = anchors.add(origin.asLong());
        changed |= arenaAnchors.add(origin.asLong());
        if (changed) {
            setDirty();
        }
    }

    /** 返回距给定位置最近的已登记决斗场锚点。 */
    public BlockPos findNearestArena(int x, int z) {
        return findNearest(arenaAnchors, x, z);
    }

    private static BlockPos findNearest(Set<Long> positions, int x, int z) {
        BlockPos nearest = null;
        long nearestDistanceSquared = Long.MAX_VALUE;
        for (long packedAnchor : positions) {
            BlockPos anchor = BlockPos.of(packedAnchor);
            long dx = (long) anchor.getX() - x;
            long dz = (long) anchor.getZ() - z;
            long distanceSquared = dx * dx + dz * dz;
            if (distanceSquared < nearestDistanceSquared) {
                nearest = anchor;
                nearestDistanceSquared = distanceSquared;
            }
        }
        return nearest;
    }

    /** 区块载入时登记其中已保存的遗迹结构，兼容更新前生成的遗迹。 */
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        ChunkAccess chunk = event.getChunk();
        for (StructureStart start : chunk.getAllStarts().values()) {
            if (!start.isValid()) continue;
            var type = start.getStructure().type();
            if (type != StarRailRuinWorldgen.ORDINARY.get()
                    && type != StarRailRuinWorldgen.RELIC_TRIAL_ARENA.get()) continue;
            for (StructurePiece piece : start.getPieces()) {
                if (piece instanceof FloatingRuinPiece ruinPiece) {
                    if ("ordinary".equals(ruinPiece.getVariantId())) {
                        get(level).recordOrdinaryRuin(ruinPiece.getOrigin());
                    } else {
                        get(level).recordRuin(ruinPiece.getOrigin());
                    }
                    break;
                }
                if (piece instanceof RelicTrialArenaPiece arenaPiece) {
                    get(level).recordArena(arenaPiece.getOrigin().offset(
                            RelicTrialArenaCommands.CENTER, 0, RelicTrialArenaCommands.CENTER));
                    break;
                }
            }
        }
    }
}
