package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Four-stage star tuber crop; mature wild plants may grow on native highland soil. */
public final class PathStarTuberCropBlock extends CropBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    public PathStarTuberCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public int getMaxAge() {
        return 3;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return PathEcologyContent.STAR_TUBER_SEEDS.get();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState soil = level.getBlockState(pos.below());
        if (soil.is(Blocks.FARMLAND)) return true;
        if (state.getValue(AGE) < getMaxAge()) return false;
        return soil.is(PathTerrainBlocks.PATH_GRASS_BLOCK.get())
                || soil.is(PathTerrainBlocks.PATH_COARSE_SOIL.get())
                || soil.is(PathTerrainBlocks.PATH_RIDGE_SCREE.get());
    }
}
