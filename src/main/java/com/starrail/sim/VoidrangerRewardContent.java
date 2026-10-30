package com.starrail.sim;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

/** Materials dropped by voidrangers and used to craft or repair the void rift blade. */
public final class VoidrangerRewardContent {
    private static final List<RegistryObject<Item>> ITEMS = new ArrayList<>();

    public static final RegistryObject<Item> VOID_ESSENCE_SHARD = material("void_essence_shard");
    public static final RegistryObject<Item> VOID_HEAVY_ARMOR_PLATE = material("void_heavy_armor_plate");

    private VoidrangerRewardContent() {
    }

    /** Forces item registrations to initialize before the mod registry event is fired. */
    public static void register() {
    }

    public static void addCreativeItems(CreativeModeTab.Output output) {
        for (RegistryObject<Item> item : ITEMS) output.accept(item.get());
    }

    private static RegistryObject<Item> material(String id) {
        RegistryObject<Item> item = StarRailSimMod.ITEMS.register(id,
                () -> new Item(new Item.Properties()));
        ITEMS.add(item);
        return item;
    }
}
