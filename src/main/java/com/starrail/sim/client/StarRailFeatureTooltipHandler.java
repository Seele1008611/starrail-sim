package com.starrail.sim.client;

import com.starrail.sim.StarRailRelicItems;
import com.starrail.sim.StarRailRuinContent;
import com.starrail.sim.StarRailSimMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Adds concise usage descriptions to the relic trial device, ruin anchors and relic materials. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarRailFeatureTooltipHandler {
    private StarRailFeatureTooltipHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        String description = descriptionKey(item);
        if (description != null) {
            event.getToolTip().add(Component.translatable(description));
        }
    }

    private static String descriptionKey(Item item) {
        if (item == StarRailRelicItems.LOST_CRYSTAL.get()) {
            return "tooltip.starrail_sim.lost_crystal";
        }
        if (item == StarRailRelicItems.RELIC_REMAINS.get()) {
            return "tooltip.starrail_sim.relic_remains";
        }
        if (item == StarRailRelicItems.SELF_MODELING_RESIN.get()) {
            return "tooltip.starrail_sim.self_modeling_resin";
        }
        if (item == StarRailRelicItems.VARIABLE_DICE.get()) {
            return "tooltip.starrail_sim.variable_dice";
        }
        if (item instanceof BlockItem blockItem) {
            if (blockItem.getBlock() == StarRailSimMod.RELIC_TRIAL_DEVICE.get()) {
                return "tooltip.starrail_sim.relic_trial_device";
            }
            if (StarRailRuinContent.isAnchor(blockItem.getBlock())) {
                return "tooltip.starrail_sim.ruin_anchor";
            }
        }
        return null;
    }
}
