package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** An immovable, path-specific interaction point inside its matching ruin. */
public final class PathRuinAnchorBlock extends Block {
    private final StarRailPath path;

    public PathRuinAnchorBlock(StarRailPath path, Properties properties) {
        super(properties);
        this.path = path;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(StarRailRuinContent.key(path).get())) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable(
                    "message.starrail_sim.ruin_anchor.need_key",
                    StarRailRuinContent.pathName(path)), true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.FAIL;
        return StarRailRuinGuardService.trySummonGuardian(serverLevel, pos, player, path)
                ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
}
