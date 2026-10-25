package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A low, non-blocking cushion plant that can be replanted on suitable Path ground. */
public final class PathRidgeCushionBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(3, 0, 3, 13, 4, 13),
            Block.box(1, 0, 5, 7, 3, 12),
            Block.box(8, 0, 4, 15, 3, 11),
            Block.box(5, 0, 1, 11, 5, 8));

    public PathRidgeCushionBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!level.getFluidState(pos).isEmpty()) return false;
        BlockState ground = level.getBlockState(pos.below());
        return ground.is(PathTerrainBlocks.PATH_GRASS_BLOCK.get())
                || ground.is(PathTerrainBlocks.PATH_COARSE_SOIL.get())
                || ground.is(PathTerrainBlocks.PATH_RIDGE_SCREE.get());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        return canSurvive(state, context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                               CollisionContext context) {
        return SHAPE;
    }
}
