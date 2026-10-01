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
import java.util.Set;

/** 保存已生成遗迹的锚点，并为寻迹遗迹执行统一的水平间距检查。 */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarRailRuinSpacingData extends SavedData {
    private static final String DATA_NAME = "starrail_sim_ruin_spacing";
    private static final String ANCHORS_KEY = "anchors";
    private static final long MIN_DISTANCE_SQUARED = 400L * 400L;

    private final Set<Long> anchors = new LinkedHashSet<>();

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
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag savedAnchors = new ListTag();
        for (long anchor : anchors) {
            savedAnchors.add(net.minecraft.nbt.LongTag.valueOf(anchor));
        }
        tag.put(ANCHORS_KEY, savedAnchors);
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
        if (!hasClearance(x, z, allowSameSite)) {
            return false;
        }

        ResourceKey<Structure> ruinKey = ResourceKey.create(Registries.STRUCTURE,
                new ResourceLocation(StarRailSimMod.MOD_ID, "ordinary_floating_ruin"));
        Holder<Structure> ruinHolder = level.registryAccess()
                .registryOrThrow(Registries.STRUCTURE).getHolderOrThrow(ruinKey);
        for (StructurePlacement placement : level.getChunkSource().getGeneratorState()
                .getPlacementsForStructure(ruinHolder)) {
            if (!(placement instanceof RandomSpreadStructurePlacement spread)) {
                continue;
            }

            int chunkX = Math.floorDiv(x, 16);
            int chunkZ = Math.floorDiv(z, 16);
            int regionX = Math.floorDiv(chunkX, spread.spacing());
            int regionZ = Math.floorDiv(chunkZ, spread.spacing());
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
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

    /** 区块载入时登记其中已保存的遗迹结构，兼容更新前生成的遗迹。 */
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        ChunkAccess chunk = event.getChunk();
        for (StructureStart start : chunk.getAllStarts().values()) {
            if (!start.isValid()
                    || start.getStructure().type() != StarRailRuinWorldgen.ORDINARY.get()) {
                continue;
            }
            for (StructurePiece piece : start.getPieces()) {
                if (piece instanceof FloatingRuinPiece ruinPiece) {
                    get(level).recordRuin(ruinPiece.getOrigin());
                    break;
                }
            }
        }
    }
}
