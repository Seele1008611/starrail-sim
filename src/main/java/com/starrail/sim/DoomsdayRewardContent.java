package com.starrail.sim;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.RegistryObject;

/** Repeatable boss material and a rare commemorative collectible. */
public final class DoomsdayRewardContent {
    public static final RegistryObject<Item> ANTIMATTER_CORE_FRAGMENT = StarRailSimMod.ITEMS.register(
            "antimatter_core_fragment", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> DOOMSDAY_MEDAL = StarRailSimMod.ITEMS.register(
            "doomsday_medal", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    private DoomsdayRewardContent() {}

    public static void register() {}

    public static void addCreativeItems(CreativeModeTab.Output output) {
        output.accept(ANTIMATTER_CORE_FRAGMENT.get());
        output.accept(DOOMSDAY_MEDAL.get());
    }
}
