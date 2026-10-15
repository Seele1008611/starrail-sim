package com.starrail.sim;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** Stable registry keys for The Path dimension. */
public final class StarRailDimensionIds {
    public static final ResourceLocation THE_PATH_LOCATION =
            ResourceLocation.fromNamespaceAndPath(StarRailSimMod.MOD_ID, "the_path");
    public static final ResourceKey<Level> THE_PATH =
            ResourceKey.create(Registries.DIMENSION, THE_PATH_LOCATION);

    private StarRailDimensionIds() {
    }
}
