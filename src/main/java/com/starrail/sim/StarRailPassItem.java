package com.starrail.sim;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/** Random or selectable Path-specific Light Cone ticket. */
public final class StarRailPassItem extends Item {
    private final boolean selectable;

    public StarRailPassItem(boolean selectable) {
        super(new Item.Properties());
        this.selectable = selectable;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (selectable) {
                StarRailLightConePassService.openPicker(serverPlayer, hand);
            } else {
                StarRailLightConePassService.drawRandom(serverPlayer, hand);
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip,
                                TooltipFlag flags) {
        super.appendHoverText(stack, level, tooltip, flags);
        tooltip.add(Component.translatable(selectable
                ? "tooltip.starrail_sim.star_rail_special_pass"
                : "tooltip.starrail_sim.star_rail_pass"));
    }
}
