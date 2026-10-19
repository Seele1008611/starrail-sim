package com.starrail.sim;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.List;

/** Data-driven light-cone pools shared by the server draw logic and client picker. */
public final class StarRailLightConePools {
    private StarRailLightConePools() {
    }

    public static TagKey<Item> tagFor(StarRailPath path) {
        return ItemTags.create(new ResourceLocation(StarRailSimMod.MOD_ID,
                "light_cone/" + path.getId()));
    }

    public static List<Item> forPath(StarRailPath path) {
        if (path == null || !path.isRealPath()) {
            return List.of();
        }
        TagKey<Item> tag = tagFor(path);
        return ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> new ItemStack(item).is(tag))
                .sorted(Comparator.comparing(item -> ForgeRegistries.ITEMS.getKey(item).toString()))
                .toList();
    }
}
