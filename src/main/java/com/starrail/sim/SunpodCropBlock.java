package com.starrail.sim;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;

/** Uses vanilla farmland, bone-meal, lighting and age 0..7 growth rules. */
public final class SunpodCropBlock extends CropBlock {
    public SunpodCropBlock(Properties properties) { super(properties); }
    @Override
    protected ItemLike getBaseSeedId() { return PathOchreContent.SUNPOD_SEEDS.get(); }
}
