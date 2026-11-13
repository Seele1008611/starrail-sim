package com.starrail.sim.client;

import com.starrail.sim.PrototypeCurios;
import com.starrail.sim.StarRailSimMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, value = Dist.CLIENT)
public final class CurioBonusTooltips {
    private static LocalPlayer owner;
    private static CompoundTag bonuses;

    private CurioBonusTooltips() {}

    public static void update(CompoundTag snapshot) {
        owner = Minecraft.getInstance().player;
        bonuses = snapshot;
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        String key = PrototypeCurios.bonusKey(event.getItemStack());
        if (key.isEmpty() || !(event.getEntity() instanceof LocalPlayer player)) return;
        Component current = null;
        if (owner == player && bonuses != null && bonuses.contains(key)) {
            boolean applies = switch (key) {
                case "WHEEL", "PLAGUE", "DEBT", "BOX" -> true;
                default -> matchesFirstEquipped(player, key, event.getItemStack());
            };
            if (applies) current = Component.Serializer.fromJson(bonuses.getString(key));
        }
        if (current == null) current = Component.translatable("tooltip.starrail_sim.curio.inactive");
        event.getToolTip().add(Component.translatable("tooltip.starrail_sim.curio.current", current)
                .withStyle(ChatFormatting.AQUA));
    }

    private static boolean matchesFirstEquipped(LocalPlayer player, String key, ItemStack stack) {
        for (int slot = 0; slot < PrototypeCurios.SLOT_COUNT; slot++) {
            ItemStack equipped = PrototypeCurios.equipped(player, slot);
            if (PrototypeCurios.bonusKey(equipped).equals(key)) return ItemStack.matches(equipped, stack);
        }
        return false;
    }
}
