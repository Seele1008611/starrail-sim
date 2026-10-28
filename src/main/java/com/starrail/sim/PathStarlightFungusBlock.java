package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A faintly glowing cave fungus that slowly spreads on damp Path ground in low light. */
public final class PathStarlightFungusBlock extends BushBlock {
    private static final int MAX_NEARBY_FUNGUS = 7;
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(7, 0, 7, 9, 6, 9),
            Block.box(4, 5, 4, 12, 9, 12));

    public PathStarlightFungusBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!level.getFluidState(pos).isEmpty()) return false;
        BlockState soil = level.getBlockState(pos.below());
        return soil.is(PathTerrainBlocks.PATH_FOREST_HUMUS.get())
                || soil.is(PathTerrainBlocks.PATH_MARSH_HUMUS.get())
                || soil.is(PathTerrainBlocks.PATH_DIRT.get())
                || soil.is(PathTerrainBlocks.PATH_STONE.get())
                || soil.is(PathTerrainBlocks.PATH_DEEP_STONE.get())
                || soil.is(Blocks.DIRT)
                || soil.is(Blocks.COARSE_DIRT)
                || soil.is(Blocks.ROOTED_DIRT)
                || soil.is(Blocks.PODZOL)
                || soil.is(Blocks.MYCELIUM)
                || soil.is(Blocks.GRASS_BLOCK)
                || soil.is(Blocks.STONE)
                || soil.is(Blocks.DEEPSLATE);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos) > 9 || random.nextInt(12) != 0
                || countNearby(level, pos) >= MAX_NEARBY_FUNGUS) return;

        for (int attempt = 0; attempt < 8; attempt++) {
            BlockPos target = pos.offset(random.nextInt(7) - 3,
                    random.nextInt(3) - 1, random.nextInt(7) - 3);
            if (target.equals(pos) || !level.isEmptyBlock(target)
                    || level.getMaxLocalRawBrightness(target) > 9) continue;
            BlockState candidate = defaultBlockState();
            if (canSurvive(candidate, level, target)) {
                level.setBlock(target, candidate, 3);
                return;
            }
        }
    }

    private int countNearby(LevelReader level, BlockPos center) {
        int count = 0;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    if (level.getBlockState(center.offset(dx, dy, dz)).is(this)) count++;
                    if (count >= MAX_NEARBY_FUNGUS) return count;
                }
            }
        }
        return count;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        return canSurvive(state, context.getLevel(), context.getClickedPos()) ? state : null;
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
