package com.starrail.sim;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public final class TidewardDockStructure extends Structure {
    public static final Codec<TidewardDockStructure> CODEC = simpleCodec(TidewardDockStructure::new);

    public TidewardDockStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int sea = context.chunkGenerator().getSeaLevel();
        int bottom = context.heightAccessor().getMinBuildHeight();
        var bounds = TidewardDockLayout.bounds();
        if (sea + bounds.minY() < bottom
                || sea + bounds.maxY() >= context.heightAccessor().getMaxBuildHeight()) return Optional.empty();
        var origin = new BlockPos(context.chunkPos().getMiddleBlockX(), sea, context.chunkPos().getMiddleBlockZ());
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new TidewardDockPiece(origin, bottom))));
    }

    @Override
    public StructureType<?> type() {
        return StarRailRuinWorldgen.TIDEWARD_DOCK.get();
    }
}
