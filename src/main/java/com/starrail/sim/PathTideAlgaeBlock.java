package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Waterlogged, harvestable seaweed rooted in the seabed. */
public final class PathTideAlgaeBlock extends Block implements SimpleWaterloggedBlock {
    private static final VoxelShape SHAPE = Block.box(5, 0, 5, 11, 14, 11);

    public PathTideAlgaeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED)
                ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState ground = level.getBlockState(pos.below());
        return state.getValue(BlockStateProperties.WATERLOGGED)
                && (ground.is(Blocks.SAND) || ground.is(Blocks.GRAVEL) || ground.is(Blocks.CLAY)
                || ground.is(Blocks.DIRT) || ground.is(Blocks.STONE)
                || ground.is(PathTerrainBlocks.PATH_RIVER_SAND.get())
                || ground.is(PathTerrainBlocks.PATH_RIVER_GRAVEL.get())
                || ground.is(PathTerrainBlocks.PATH_RIVER_SANDSTONE.get())
                || ground.is(PathTerrainBlocks.PATH_STONE.get())
                || ground.is(PathTerrainBlocks.PATH_COBBLESTONE.get())
                || ground.is(PathTerrainBlocks.PATH_SMOOTH_STONE.get())
                || ground.is(PathTerrainBlocks.PATH_DEEP_STONE.get())
                || ground.is(PathTerrainBlocks.PATH_DEEP_COBBLESTONE.get())
                || ground.is(PathTerrainBlocks.PATH_SMOOTH_DEEP_STONE.get())
                || ground.is(PathTerrainBlocks.PATH_STONE_BRICKS.get())
                || ground.is(PathTerrainBlocks.PATH_DEEP_STONE_BRICKS.get()));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        if (!context.getLevel().getFluidState(pos).is(Fluids.WATER)
                || !context.getLevel().getFluidState(pos).isSource()) {
            return null;
        }
        BlockState state = defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
        return canSurvive(state, context.getLevel(), pos) ? state : null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
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
