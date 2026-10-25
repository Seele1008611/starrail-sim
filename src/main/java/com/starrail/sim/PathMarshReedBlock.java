package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.material.Fluids;

/** A narrow, non-blocking two-block marsh plant that needs moist soil and nearby water. */
public final class PathMarshReedBlock extends DoublePlantBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(6, 0, 6, 10, 15, 10),
            Block.box(3, 5, 6, 8, 9, 10),
            Block.box(8, 8, 6, 13, 12, 10));

    public PathMarshReedBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return super.canSurvive(state, level, pos);
        }
        if (!level.getFluidState(pos).isEmpty() || !hasSuitableSoil(level.getBlockState(pos.below()))) {
            return false;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(direction);
            if (level.getFluidState(neighbor).is(Fluids.WATER)
                    || level.getFluidState(neighbor.below()).is(Fluids.WATER)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasSuitableSoil(BlockState ground) {
        return ground.is(Blocks.DIRT) || ground.is(Blocks.GRASS_BLOCK)
                || ground.is(Blocks.MUD) || ground.is(Blocks.CLAY)
                || ground.is(PathTerrainBlocks.PATH_DIRT.get())
                || ground.is(PathTerrainBlocks.PATH_GRASS_BLOCK.get())
                || ground.is(PathTerrainBlocks.PATH_COARSE_SOIL.get())
                || ground.is(PathTerrainBlocks.PATH_FOREST_HUMUS.get())
                || ground.is(PathTerrainBlocks.PATH_MARSH_HUMUS.get())
                || ground.is(PathTerrainBlocks.PATH_RIVER_SAND.get())
                || ground.is(PathTerrainBlocks.PATH_RIVER_GRAVEL.get());
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
