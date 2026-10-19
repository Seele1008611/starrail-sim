package com.starrail.sim;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;

/** Sapling grower backed by a configured tree feature in the data pack. */
public final class PathTreeGrower extends AbstractTreeGrower {
    private final ResourceKey<ConfiguredFeature<?, ?>> feature;

    public PathTreeGrower(String featureId) {
        this.feature = ResourceKey.create(Registries.CONFIGURED_FEATURE,
                new ResourceLocation(StarRailSimMod.MOD_ID, featureId));
    }

    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(
            RandomSource random, boolean hasFlowers) {
        return feature;
    }
}
