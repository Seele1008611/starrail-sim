package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** One renderer per locomotive; hull cells do not allocate block entities or tick. */
public final class StarRailTrainBlockEntity extends BlockEntity {
    public StarRailTrainBlockEntity(BlockPos pos, BlockState state) {
        super(StarRailWorldBlocks.STAR_RAIL_TRAIN_ENTITY.get(), pos, state);
    }

    @Override public AABB getRenderBoundingBox() {
        Direction facing = getBlockState().getValue(StarRailTrainBlock.FACING);
        BlockPos far = worldPosition.relative(facing.getClockWise(), StarRailTrainLayout.WIDTH - 1)
                .relative(facing.getOpposite(), StarRailTrainLayout.LENGTH - 1)
                .above(StarRailTrainLayout.HEIGHT - 1);
        return new AABB(Math.min(worldPosition.getX(), far.getX()), Math.min(worldPosition.getY(), far.getY()),
                Math.min(worldPosition.getZ(), far.getZ()), Math.max(worldPosition.getX(), far.getX()) + 1,
                Math.max(worldPosition.getY(), far.getY()) + 1, Math.max(worldPosition.getZ(), far.getZ()) + 1)
                .inflate(0.15);
    }
}
