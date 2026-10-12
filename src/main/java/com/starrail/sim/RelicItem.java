package com.starrail.sim;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/** One set-and-slot relic type; individual rolls live on the ItemStack. */
public final class RelicItem extends Item implements ICurioItem {
    private final RelicStackData.SetId set;
    private final RelicStackData.Part part;

    public RelicItem(RelicStackData.SetId set, RelicStackData.Part part) {
        super(new Item.Properties().stacksTo(1));
        if (set.inner() != part.inner()) throw new IllegalArgumentException("Relic set and part ring do not match");
        this.set = set;
        this.part = part;
    }

    public RelicStackData.SetId set() { return set; }
    public RelicStackData.Part part() { return part; }
    public boolean matches(RelicStackData.SetId expectedSet, RelicStackData.Part expectedPart) {
        return set == expectedSet && part == expectedPart;
    }

    public ItemStack createStack(int stars, RelicStackData.Stat mainStat) {
        ItemStack stack = new ItemStack(this);
        RelicStackData.write(stack, RelicStackData.fresh(set, part, stars, mainStat));
        return stack;
    }

    public Optional<RelicStackData.Data> relicData(ItemStack stack) {
        return RelicStackData.read(stack);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return part.curiosSlot().equals(slotContext.identifier())
                && RelicStackData.read(stack).filter(data -> !data.hasPendingReroll()).isPresent();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        Optional<RelicStackData.Data> data = relicData(stack);
        if (data.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.starrail_sim.relic.invalid_data")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        RelicStackData.Data relic = data.get();
        tooltip.add(Component.translatable("tooltip.starrail_sim.relic.rarity_level",
                relic.stars(), relic.level()).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.starrail_sim.relic.set_header",
                Component.translatable("ui.starrail_sim.relic.set." + set.id()))
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("tooltip.starrail_sim.relic.two_piece",
                Component.translatable("ui.starrail_sim.relic.set_effect." + set.id() + ".two"))
                .withStyle(ChatFormatting.AQUA));
        if (!set.inner()) {
            tooltip.add(Component.translatable("tooltip.starrail_sim.relic.four_piece",
                    Component.translatable("ui.starrail_sim.relic.set_effect." + set.id() + ".four"))
                    .withStyle(ChatFormatting.AQUA));
        }
        tooltip.add(Component.translatable("tooltip.starrail_sim.relic.main_stat",
                Component.translatable("ui.starrail_sim.relic.stat." + relic.mainStat().id()),
                RelicStackData.format(relic.mainValue()) + (relic.mainStat().percent() ? "%" : ""))
                .withStyle(ChatFormatting.GRAY));
        for (RelicStackData.SubStat substat : relic.substats()) {
            tooltip.add(Component.translatable("tooltip.starrail_sim.relic.substat",
                    Component.translatable("ui.starrail_sim.relic.stat." + substat.stat().id()),
                    RelicStackData.format(substat.value()) + (substat.stat().percent() ? "%" : ""))
                    .withStyle(ChatFormatting.AQUA));
        }
        if (relic.locked()) tooltip.add(Component.translatable("tooltip.starrail_sim.relic.locked")
                .withStyle(ChatFormatting.YELLOW));
    }
}
