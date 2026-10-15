package com.starrail.sim;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import java.util.function.Consumer;

/** Uses the same reference mesh for inventory and hand previews as the placed locomotive. */
public final class StarRailTrainItem extends BlockItem {
    public StarRailTrainItem(Block block, Properties properties) { super(block, properties); }

    @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack,
            @org.jetbrains.annotations.Nullable net.minecraft.world.level.Level level,
            java.util.List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.starrail_sim.train_space")
                .withStyle(net.minecraft.ChatFormatting.WHITE));
    }

    @Override public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return com.starrail.sim.client.StarRailTrainItemRenderer.instance();
            }
        });
    }
}
