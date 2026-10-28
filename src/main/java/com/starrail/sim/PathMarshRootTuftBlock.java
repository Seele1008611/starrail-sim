package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A low, non-blocking root tuft that grows on marsh banks beside shallow water. */
public final class PathMarshRootTuftBlock extends BushBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(3, 0, 6, 13, 5, 10),
            Block.box(5, 3, 3, 8, 10, 6),
            Block.box(8, 2, 10, 11, 9, 13),
            Block.box(6, 4, 5, 10, 12, 11));

    public PathMarshRootTuftBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!level.getFluidState(pos).isEmpty()) return false;
        BlockState soil = level.getBlockState(pos.below());
        if (!(soil.is(PathTerrainBlocks.PATH_MARSH_HUMUS.get())
                || soil.is(PathTerrainBlocks.PATH_RIVER_SAND.get())
                || soil.is(PathTerrainBlocks.PATH_GRASS_BLOCK.get()))) return false;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(direction);
            if (level.getFluidState(neighbor).is(Fluids.WATER)
                    || level.getFluidState(neighbor.below()).is(Fluids.WATER)) return true;
        }
        return false;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!canSurvive(state, level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                               CollisionContext context) {
        return SHAPE;
    }
}
