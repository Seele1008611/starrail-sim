package com.starrail.sim;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/** Terrain selects the anchor only; obstructions never remove rooms or reject a candidate. */
public final class DaylessArchiveStructure extends Structure {
    public static final Codec<DaylessArchiveStructure> CODEC = simpleCodec(DaylessArchiveStructure::new);

    public DaylessArchiveStructure(StructureSettings settings) { super(settings); }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int min = context.heightAccessor().getMinBuildHeight() + 14;
        int max = context.heightAccessor().getMaxBuildHeight() - 56;
        if (min > max) return Optional.empty();
        int x = context.chunkPos().getMiddleBlockX(), z = context.chunkPos().getMiddleBlockZ();
        int surface = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        BlockPos origin = new BlockPos(x, Math.max(min, Math.min(max, surface - 1)), z);
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new DaylessArchivePiece(origin))));
    }

    @Override
    public StructureType<?> type() { return StarRailRuinWorldgen.DAYLESS_ARCHIVE.get(); }
}
