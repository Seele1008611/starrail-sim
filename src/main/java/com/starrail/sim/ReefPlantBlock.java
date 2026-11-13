package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/** Source-water plant with a bounded kelp column and a distinct growing tip. */
public final class ReefPlantBlock extends Block implements LiquidBlockContainer {
    public static final BooleanProperty TIP = BooleanProperty.create("tip");
    private static final TagKey<Block> SUBSTRATE = TagKey.create(Registries.BLOCK,
            new ResourceLocation(StarRailSimMod.MOD_ID, "reef_plant_substrate"));
    private final int maxHeight;

    public ReefPlantBlock(Properties properties, int maxHeight) {
        super(properties);
        this.maxHeight = maxHeight;
        registerDefaultState(stateDefinition.any().setValue(TIP, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIP);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return Fluids.WATER.getSource(false);
    }

    @Override
    public boolean canPlaceLiquid(net.minecraft.world.level.BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState floor = level.getBlockState(pos.below());
        return floor.is(SUBSTRATE) || (maxHeight > 1 && floor.is(this));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        var level = context.getLevel();
        if (!isSourceWater(level.getBlockState(pos)) || !canSurvive(defaultBlockState(), level, pos)
                || heightBelow(level, pos) >= maxHeight) return null;
        return defaultBlockState().setValue(TIP, !level.getBlockState(pos.above()).is(this));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        if (direction == Direction.DOWN && !canSurvive(state, level, pos)) level.scheduleTick(pos, this, 1);
        if (direction == Direction.UP && maxHeight > 1) return state.setValue(TIP, !neighbor.is(this));
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) level.destroyBlock(pos, true);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (maxHeight == 1 || !state.getValue(TIP) || random.nextInt(6) != 0) return;
        BlockPos above = pos.above();
        if (above.getY() < level.getMaxBuildHeight() && isSourceWater(level.getBlockState(above))
                && heightBelow(level, pos) + 1 < maxHeight) {
            level.setBlock(above, defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private int heightBelow(LevelReader level, BlockPos pos) {
        int height = 0;
        while (height < maxHeight && level.getBlockState(pos.below(height + 1)).is(this)) height++;
        return height;
    }

    private static boolean isSourceWater(BlockState state) {
        return state.is(Blocks.WATER) && state.getFluidState().isSource();
    }
}
