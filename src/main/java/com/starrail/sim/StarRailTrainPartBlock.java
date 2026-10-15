package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.BlockHitResult;

/** Invisible linked hull cells provide hit detection and collision for the full train mesh. */
public final class StarRailTrainPartBlock extends HorizontalDirectionalBlock {
    public static final IntegerProperty CELL = IntegerProperty.create("cell", 0, StarRailTrainLayout.CELL_COUNT - 1);

    public StarRailTrainPartBlock(Properties properties) { super(properties); }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CELL);
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                           InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            StarRailDimensionService.activateTrain(serverPlayer);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return StarRailTrainLayout.shape(state.getValue(CELL), state.getValue(FACING));
    }

    @Override public net.minecraft.world.item.ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new net.minecraft.world.item.ItemStack(StarRailWorldBlocks.STAR_RAIL_TRAIN.get());
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && !level.isClientSide) {
            BlockPos anchor = StarRailTrainLayout.anchor(pos, state.getValue(FACING), state.getValue(CELL));
            BlockState main = level.getBlockState(anchor);
            if (main.is(StarRailWorldBlocks.STAR_RAIL_TRAIN.get())
                    && main.getValue(StarRailTrainBlock.LARGE)
                    && main.getValue(FACING) == state.getValue(FACING)) {
                StarRailTrainBlock.removeLargeStructure(level, anchor, state.getValue(FACING), pos);
            }
        }
        super.onRemove(state, level, pos, next, moving);
    }

    @Override public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
