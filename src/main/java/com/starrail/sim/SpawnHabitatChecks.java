package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;

/** Spawn predicates may run inside a one-chunk WorldGenRegion; never demand a missing neighbour. */
final class SpawnHabitatChecks {
    private SpawnHabitatChecks() {}

    static boolean waterAt(LevelReader level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos) || !level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) return false;
        return level.getFluidState(pos).is(FluidTags.WATER);
    }
}
