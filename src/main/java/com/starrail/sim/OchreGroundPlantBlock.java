package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Ground decorations tolerate dry sand and rock without requiring farmland or water. */
public final class OchreGroundPlantBlock extends BushBlock {
    private final VoxelShape shape;
    public OchreGroundPlantBlock(Properties properties, int height) {
        super(properties);
        shape = box(2, 0, 2, 14, height, 14);
    }
    @Override
    protected boolean mayPlaceOn(BlockState ground, BlockGetter level, BlockPos pos) {
        return ground.is(BlockTags.DIRT) || ground.is(BlockTags.SAND)
                || ground.is(PathOchreContent.OCHRE_SAND.get())
                || ground.is(PathOchreContent.OCHRE_STONE.base().get())
                || ground.is(PathTerrainBlocks.PATH_COARSE_SOIL.get())
                || ground.is(PathTerrainBlocks.PATH_STONE.get())
                || ground.is(PathTerrainBlocks.PATH_RIDGE_SCREE.get());
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }
}
