package com.starrail.sim;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Reusable seal for summoning the matching path ruin guardian. */
public final class PathRuinSummonKeyItem extends Item {
    private final StarRailPath path;

    public PathRuinSummonKeyItem(StarRailPath path, Properties properties) {
        super(properties);
        this.path = path;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip,
            TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.starrail_sim.ruin_summon_key",
                StarRailRuinContent.pathName(path)));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
