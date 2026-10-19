package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/** Matches vanilla hoe and shovel behavior for custom Path soils. */
final class PathSoilToolInteraction {
    private PathSoilToolInteraction() {
    }

    static InteractionResult interact(Level level, BlockPos pos, Player player, InteractionHand hand,
                                      Supplier<BlockState> hoeResult,
                                      Supplier<BlockState> shovelResult) {
        ItemStack tool = player.getItemInHand(hand);
        if (!level.getBlockState(pos.above()).isAir()) {
            return InteractionResult.PASS;
        }

        if (tool.getItem() instanceof HoeItem || tool.is(ItemTags.HOES)) {
            return apply(level, pos, player, hand, tool, hoeResult,
                    SoundEvents.HOE_TILL);
        }
        if (tool.getItem() instanceof ShovelItem || tool.is(ItemTags.SHOVELS)) {
            return apply(level, pos, player, hand, tool, shovelResult,
                    SoundEvents.SHOVEL_FLATTEN);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult apply(Level level, BlockPos pos, Player player,
                                           InteractionHand hand, ItemStack tool,
                                           Supplier<BlockState> resultState,
                                           net.minecraft.sounds.SoundEvent sound) {
        if (!level.isClientSide) {
            level.setBlock(pos, resultState.get(), 11);
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            tool.hurtAndBreak(1, player, entity -> entity.broadcastBreakEvent(hand));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
