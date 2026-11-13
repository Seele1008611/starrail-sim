package com.starrail.sim;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public final class BrokenRingObservatoryStructure extends Structure {
    public static final Codec<BrokenRingObservatoryStructure> CODEC =
            simpleCodec(BrokenRingObservatoryStructure::new);

    public BrokenRingObservatoryStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        var bounds = BrokenRingObservatoryLayout.bounds();
        int bottom = context.heightAccessor().getMinBuildHeight();
        int min = bottom - bounds.minY();
        int max = context.heightAccessor().getMaxBuildHeight() - 1 - bounds.maxY()
                - BrokenRingObservatoryLayout.HEADROOM;
        if (min > max) return Optional.empty();
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        int surface = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        var origin = new BlockPos(x, Math.max(min, Math.min(max, surface)), z);
        return Optional.of(new GenerationStub(origin,
                builder -> builder.addPiece(new BrokenRingObservatoryPiece(origin, bottom))));
    }

    @Override
    public StructureType<?> type() {
        return StarRailRuinWorldgen.BROKEN_RING_OBSERVATORY.get();
    }
}
