package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Edible berry that can be planted on valid soil to start another bush. */
public final class PathBerryItem extends Item {
    public PathBerryItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos target = context.getClickedPos().relative(context.getClickedFace());
        BlockState bush = PathEcologyContent.MORNING_BERRY_BUSH.get().defaultBlockState();
        if (!level.getBlockState(target).canBeReplaced()
                || !bush.canSurvive(level, target)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            level.setBlock(target, bush, 3);
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
