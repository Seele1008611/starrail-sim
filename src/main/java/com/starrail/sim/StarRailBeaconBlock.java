package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

/** A reusable route marker that records its position in the explorer's Path archive. */
public final class StarRailBeaconBlock extends Block {
    public StarRailBeaconBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState,
                        boolean moving) {
        if (state.getBlock() != oldState.getBlock() && level instanceof ServerLevel serverLevel
                && serverLevel.dimension().equals(StarRailDimensionIds.THE_PATH)) {
            StarRailBeaconArchiveData.get(serverLevel).markPresent(pos.asLong());
        }
        super.onPlace(state, level, pos, oldState, moving);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
                         boolean moving) {
        if (state.getBlock() != newState.getBlock() && level instanceof ServerLevel serverLevel) {
            StarRailExplorationService.removeBeacon(serverLevel, pos);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            StarRailExplorationService.discoverBeacon(serverPlayer, pos);
            if (StarRailDimensionService.isHubReturnBeacon(pos)
                    && level.dimension().equals(StarRailDimensionIds.THE_PATH)) {
                StarRailDimensionService.returnToOrigin(serverPlayer);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
