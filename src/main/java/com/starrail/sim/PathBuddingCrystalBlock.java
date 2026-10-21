package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/** Budding base that grows only the mod's own starvein crystal stages. */
public final class PathBuddingCrystalBlock extends BuddingAmethystBlock {
    public PathBuddingCrystalBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 0) {
            return;
        }

        Direction direction = Direction.getRandom(random);
        BlockPos targetPos = pos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);
        FluidState fluidState = targetState.getFluidState();

        if (targetState.isAir() || fluidState.is(FluidTags.WATER)) {
            BlockState bud = PathCaveContent.SMALL_STARVEIN_BUD.get().defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, direction)
                    .setValue(AmethystClusterBlock.WATERLOGGED, fluidState.is(FluidTags.WATER));
            level.setBlock(targetPos, bud, Block.UPDATE_ALL);
            return;
        }

        Block nextStage = nextStage(targetState.getBlock());
        if (nextStage == null || !targetState.hasProperty(AmethystClusterBlock.FACING)
                || targetState.getValue(AmethystClusterBlock.FACING) != direction) {
            return;
        }

        BlockState grown = nextStage.defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, direction)
                .setValue(AmethystClusterBlock.WATERLOGGED,
                        targetState.getValue(AmethystClusterBlock.WATERLOGGED));
        level.setBlock(targetPos, grown, Block.UPDATE_ALL);
    }

    private static Block nextStage(Block current) {
        if (current == PathCaveContent.SMALL_STARVEIN_BUD.get()) {
            return PathCaveContent.MEDIUM_STARVEIN_BUD.get();
        }
        if (current == PathCaveContent.MEDIUM_STARVEIN_BUD.get()) {
            return PathCaveContent.LARGE_STARVEIN_BUD.get();
        }
        if (current == PathCaveContent.LARGE_STARVEIN_BUD.get()) {
            return PathCaveContent.STARVEIN_CRYSTAL_CLUSTER.get();
        }
        return null;
    }
}
