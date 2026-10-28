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
import net.minecraft.world.level.material.Fluids;

/** Eight-stage rice crop that needs nearby water; mature wild rice can root in marsh humus. */
public final class PathStilltideRiceCropBlock extends CropBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;

    public PathStilltideRiceCropBlock(Properties properties) {
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
    protected ItemLike getBaseSeedId() {
        return PathEcologyContent.STILLTIDE_RICE_SEEDS.get();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!hasNearbyWater(level, pos)) return false;
        BlockState soil = level.getBlockState(pos.below());
        if (soil.is(Blocks.FARMLAND)) return true;
        return state.getValue(AGE) == getMaxAge()
                && soil.is(PathTerrainBlocks.PATH_MARSH_HUMUS.get());
    }

    private static boolean hasNearbyWater(LevelReader level, BlockPos cropPos) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (Math.abs(dx) + Math.abs(dz) > 4) continue;
                for (int dy = -1; dy <= 0; dy++) {
                    var fluid = level.getFluidState(cropPos.offset(dx, dy, dz));
                    if (fluid.is(Fluids.WATER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
