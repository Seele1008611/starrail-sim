package com.starrail.sim;

/**
 * 模组代码说明：遗迹奖励道具。命途标记保存在物品 NBT 中；只有服务端确认能启动对应试炼后才消耗道具。
 */

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** A ruin reward that starts the trial for its encoded Path. */
public final class PathTrialTokenItem extends Item {
    public static final String PATH_TAG = "Path";

    public PathTrialTokenItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    // 使用凭证时检查 NBT 命途和玩家解锁状态；服务端成功启动试炼后才消耗凭证。
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        StarRailPath path = getPath(stack);
        if (!path.isRealPath()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable(
                        "message.starrail_sim.invalid_path_trial_token"), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }

        boolean unlocked = player.getCapability(StarRailPathCapability.PATH_DATA)
                .map(IStarRailPathData::isPathUnlocked).orElse(false);
        if (!unlocked) {
            player.displayClientMessage(Component.translatable("message.starrail_sim.path_locked"), true);
            return InteractionResultHolder.fail(stack);
        }

        if (StarRailPathService.startTrialFromToken(serverPlayer, stack)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.sidedSuccess(stack, false);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    // 把凭证对应的命途与使用提示显示在物品悬浮说明中。
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip,
                                TooltipFlag flag) {
        StarRailPath path = getPath(stack);
        if (path.isRealPath()) {
            tooltip.add(Component.translatable("tooltip.starrail_sim.path_trial_token.path",
                    path.getDisplayName()).withStyle(ChatFormatting.AQUA));
        }
        tooltip.add(Component.translatable("tooltip.starrail_sim.path_trial_token.use")
                .withStyle(ChatFormatting.GRAY));
    }

    // 从物品 NBT 的 Path 字段解析命途；缺失或无效时返回 NONE。
    static StarRailPath getPath(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? StarRailPath.NONE : StarRailPath.byId(tag.getString(PATH_TAG));
    }
}
