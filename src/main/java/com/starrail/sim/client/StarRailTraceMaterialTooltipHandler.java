package com.starrail.sim.client;

import com.starrail.sim.StarRailPath;
import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.StarRailTraceMaterials;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Adds the in-game lore for the nine Path Trace upgrade materials. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarRailTraceMaterialTooltipHandler {
    private StarRailTraceMaterialTooltipHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        StarRailPath path = StarRailTraceMaterials.pathOf(stack.getItem());
        if (!path.isRealPath()) {
            return;
        }

        String prefix = "tooltip.starrail_sim.trace_material." + path.getId();
        event.getToolTip().add(Component.translatable(prefix + ".summary"));

        for (int index = 0; index < loreLineCount(path); index++) {
            event.getToolTip().add(Component.translatable(prefix + ".lore_" + index)
                    .withStyle(ChatFormatting.GRAY));
        }
        for (int index = 0; index < quoteLineCount(path); index++) {
            event.getToolTip().add(Component.translatable(prefix + ".quote_" + index)
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static int loreLineCount(StarRailPath path) {
        return switch (path) {
            case ABUNDANCE -> 4;
            case PRESERVATION -> 5;
            case ERUDITION -> 3;
            case DESTRUCTION -> 4;
            case HUNT, REMEMBRANCE -> 4;
            case HARMONY, NIHILITY -> 1;
            case ELATION -> 4;
            default -> 0;
        };
    }

    private static int quoteLineCount(StarRailPath path) {
        return switch (path) {
            case NIHILITY -> 1;
            case HARMONY -> 2;
            case ELATION -> 2;
            default -> 1;
        };
    }
}
