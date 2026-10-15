package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.Map;

/** 分区块生成决斗场：无视原地形起伏，切除场地内地形并为低处补足地基。 */
public final class RelicTrialArenaPiece extends StructurePiece {
    private final BlockPos origin;

    public RelicTrialArenaPiece(BlockPos origin) {
        super(StarRailRuinWorldgen.ARENA_PIECE.get(), 0, box(origin));
        this.origin = origin.immutable();
    }

    public RelicTrialArenaPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(StarRailRuinWorldgen.ARENA_PIECE.get(), tag);
        this.origin = new BlockPos(tag.getInt("OriginX"), tag.getInt("OriginY"),
                tag.getInt("OriginZ"));
    }

    public BlockPos getOrigin() {
        return origin;
    }

    private static BoundingBox box(BlockPos origin) {
        return new BoundingBox(origin.getX(), origin.getY(), origin.getZ(),
                origin.getX() + RelicTrialArenaCommands.SIZE - 1,
                origin.getY() + RelicTrialArenaCommands.HEIGHT - 1,
                origin.getZ() + RelicTrialArenaCommands.SIZE - 1);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("OriginX", origin.getX());
        tag.putInt("OriginY", origin.getY());
        tag.putInt("OriginZ", origin.getZ());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator,
                            RandomSource random, BoundingBox chunkBox, net.minecraft.world.level.ChunkPos chunk,
                            BlockPos reference) {
        int minX = Math.max(origin.getX(), chunkBox.minX());
        int maxX = Math.min(origin.getX() + RelicTrialArenaCommands.SIZE - 1, chunkBox.maxX());
        int minZ = Math.max(origin.getZ(), chunkBox.minZ());
        int maxZ = Math.min(origin.getZ() + RelicTrialArenaCommands.SIZE - 1, chunkBox.maxZ());
        if (minX > maxX || minZ > maxZ) return;

        // Capture each column's original surface before modifying it. Terrain above the
        // floor is cut away; lower columns get a stone-brick footing up to the arena floor.
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                int clearTop = Math.min(Math.max(origin.getY() + RelicTrialArenaCommands.HEIGHT - 1,
                        surfaceY - 1), chunkBox.maxY());
                int clearBottom = Math.max(origin.getY(), chunkBox.minY());
                for (int y = clearBottom; y <= clearTop; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, air, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                    }
                }

                int footingBottom = Math.max(surfaceY, chunkBox.minY());
                int footingTop = Math.min(origin.getY() - 1, chunkBox.maxY());
                for (int y = footingBottom; y <= footingTop; y++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.STONE_BRICKS.defaultBlockState(),
                            Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
            }
        }

        for (Map.Entry<BlockPos, BlockState> placement :
                RelicTrialArenaCommands.createPlan(origin).entrySet()) {
            BlockPos pos = placement.getKey();
            if (chunkBox.isInside(pos)) {
                level.setBlock(pos, placement.getValue(),
                        Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }
}
