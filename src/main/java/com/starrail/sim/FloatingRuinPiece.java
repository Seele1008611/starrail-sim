package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 按当前生成区块写入蓝图，并保存锚点；不强制加载周边区块。 */
public final class FloatingRuinPiece extends StructurePiece {
    private final BlockPos origin;
    private final String variantId;
    private final StarRailRuinCommands.Blueprint blueprint;
    // 每个蓝图分开缓存，避免不同命途复用到错误的方块清单。
    private static final Map<String, Map<Long, List<StarRailRuinCommands.Placement>>> CHUNK_BLOCKS =
            new ConcurrentHashMap<>();

    public FloatingRuinPiece(BlockPos origin, StarRailRuinCommands.Blueprint blueprint) {
        super(StarRailRuinWorldgen.PIECE.get(), 0, box(origin, blueprint.bounds()));
        this.origin = origin.immutable();
        this.variantId = blueprint.variantId();
        this.blueprint = blueprint;
    }

    /** 返回结构锚点，供世界数据登记间距使用。 */
    public BlockPos getOrigin() {
        return origin;
    }

    /** 返回存档中记录的命途版本，供普通母体专用定位筛选自然生成结构。 */
    public String getVariantId() {
        return variantId;
    }

    public FloatingRuinPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(StarRailRuinWorldgen.PIECE.get(), tag);
        origin = new BlockPos(tag.getInt("OriginX"), tag.getInt("OriginY"), tag.getInt("OriginZ"));
        // 旧存档中的母体结构没有 VariantId，读取时继续按 ordinary 兼容。
        variantId = tag.contains("VariantId") ? tag.getString("VariantId") : "ordinary";
        try {
            blueprint = StarRailRuinCommands.readBlueprint(variantId, tag.getBoolean("Guarded"));
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot restore floating ruin variant: " + variantId,
                    exception);
        }
    }

    private static BoundingBox box(BlockPos p, StarRailRuinCommands.Bounds b) {
        return new BoundingBox(p.getX()+b.minX(), p.getY()+b.minY(), p.getZ()+b.minZ(),
                p.getX()+b.maxX(), p.getY()+b.maxY(), p.getZ()+b.maxZ());
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("OriginX", origin.getX());
        tag.putInt("OriginY", origin.getY());
        tag.putInt("OriginZ", origin.getZ());
        tag.putString("VariantId", variantId);
        tag.putBoolean("Guarded", blueprint.guarded());
    }

    // 锚点在区块中心，因此局部坐标加8再分桶；每个区块只处理自己的方块。
    private static Map<Long, List<StarRailRuinCommands.Placement>> blocksByChunk(
            StarRailRuinCommands.Blueprint blueprint) {
        return CHUNK_BLOCKS.computeIfAbsent(blueprint.variantId() + (blueprint.guarded() ? "_guard" : ""), variantKey -> {
            Map<Long, List<StarRailRuinCommands.Placement>> result = new HashMap<>();
            for (var p : blueprint.placements()) {
                long key = ChunkPos.asLong(Math.floorDiv(p.x()+8,16), Math.floorDiv(p.z()+8,16));
                result.computeIfAbsent(key, chunkKey -> new ArrayList<>()).add(p);
            }
            result.replaceAll((key, value) -> List.copyOf(value));
            return Map.copyOf(result);
        });
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator,
            RandomSource random, BoundingBox chunkBox, ChunkPos chunk, BlockPos reference) {
        long key = ChunkPos.asLong(chunk.x - (origin.getX() >> 4), chunk.z - (origin.getZ() >> 4));
        for (var p : blocksByChunk(blueprint).getOrDefault(key, List.of())) {
            BlockPos target = origin.offset(p.x(), p.y(), p.z());
            if (!chunkBox.isInside(target)) continue;
            var state = blueprint.palette().get(p.paletteIndex());
            level.setBlock(target, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            // 原版结构生成阶段绑定战利品表，首次开箱生成奖励。
            if (state.is(Blocks.CHEST) && level.getBlockEntity(target) instanceof ChestBlockEntity chest) {
                if (blueprint.guarded()) {
                    StarRailRuinGuardService.markChest(chest, origin, blueprint.variantId());
                    StarRailRuinGuardService.queue(level.getLevel(), origin);
                } else chest.setLootTable(new ResourceLocation(StarRailSimMod.MOD_ID,
                        "chests/ruin_" + variantId), random.nextLong());
                chest.setChanged();
            }
        }
    }
}
