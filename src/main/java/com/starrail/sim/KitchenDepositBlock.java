package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

final class KitchenDepositBlock extends Block {
    private final Supplier<Item> harvest;

    KitchenDepositBlock(Properties properties, Supplier<Item> harvest) {
        super(properties);
        this.harvest = harvest;
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.AGE_1, 1));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.AGE_1);
    }

    @Override public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(BlockStateProperties.AGE_1) == 0;
    }

    @Override public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(6) == 0) level.setBlock(pos, state.setValue(BlockStateProperties.AGE_1, 1), UPDATE_CLIENTS);
    }

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                           InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        boolean collectBrine = held.is(PathKitchen.FLASK.get()) && harvest.get() == PathKitchen.FROST_SALT.get();
        if ((!held.isEmpty() && !collectBrine) || state.getValue(BlockStateProperties.AGE_1) == 0) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            if (collectBrine) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, new ItemStack(PathKitchen.BRINE.get())));
            } else {
                popResource(level, pos, new ItemStack(harvest.get()));
            }
            level.setBlock(pos, state.setValue(BlockStateProperties.AGE_1, 0), UPDATE_CLIENTS);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
