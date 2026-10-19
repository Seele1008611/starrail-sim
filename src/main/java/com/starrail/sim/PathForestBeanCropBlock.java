package com.starrail.sim;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;

/** Farmable forest bean crop; its custom seed is also the planting item. */
public final class PathForestBeanCropBlock extends CropBlock {
    public PathForestBeanCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return PathEcologyContent.FORESTBEAN_SEEDS.get();
    }
}
