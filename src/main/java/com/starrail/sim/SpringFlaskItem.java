package com.starrail.sim;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

final class SpringFlaskItem extends Item {
    SpringFlaskItem() { super(new Properties()); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack empty = player.getItemInHand(hand);
        if (!level.dimension().equals(StarRailDimensionIds.THE_PATH)) return InteractionResultHolder.pass(empty);
        var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK || !level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)) {
            return InteractionResultHolder.pass(empty);
        }
        if (level.isClientSide) return InteractionResultHolder.success(empty);
        level.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1, 1);
        return InteractionResultHolder.consume(ItemUtils.createFilledResult(empty, player, new ItemStack(PathKitchen.WATER.get())));
    }
}
