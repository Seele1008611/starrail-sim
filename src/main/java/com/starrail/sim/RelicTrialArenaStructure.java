package com.starrail.sim;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Arrays;
import java.util.Optional;

/** 自然生成的遗器试炼场：不要求平坦地形，按中位高度锚定并切平整片场地。 */
public final class RelicTrialArenaStructure extends Structure {
    public static final Codec<RelicTrialArenaStructure> CODEC =
            simpleCodec(RelicTrialArenaStructure::new);

    public RelicTrialArenaStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int centerX = context.chunkPos().getMiddleBlockX();
        int centerZ = context.chunkPos().getMiddleBlockZ();
        int[] surfaceHeights = new int[RelicTrialArenaCommands.SIZE * RelicTrialArenaCommands.SIZE];
        int sample = 0;
        for (int dx = -RelicTrialArenaCommands.CENTER; dx <= RelicTrialArenaCommands.CENTER; dx++) {
            for (int dz = -RelicTrialArenaCommands.CENTER; dz <= RelicTrialArenaCommands.CENTER; dz++) {
                surfaceHeights[sample++] = context.chunkGenerator().getBaseHeight(centerX + dx,
                        centerZ + dz, Heightmap.Types.WORLD_SURFACE_WG,
                        context.heightAccessor(), context.randomState());
            }
        }
        Arrays.sort(surfaceHeights);
        // Terrain is not a spawn condition: use the median only as a stable vertical
        // anchor. The piece clears the full footprint and builds foundations beneath low
        // columns, so hills, slopes, and ravines do not prevent natural generation.
        int y = surfaceHeights[surfaceHeights.length / 2];
        if (y < context.heightAccessor().getMinBuildHeight()
                || y + RelicTrialArenaCommands.HEIGHT > context.heightAccessor().getMaxBuildHeight()) {
            return Optional.empty();
        }

        BlockPos origin = new BlockPos(centerX - RelicTrialArenaCommands.CENTER, y,
                centerZ - RelicTrialArenaCommands.CENTER);
        return Optional.of(new GenerationStub(origin,
                builder -> builder.addPiece(new RelicTrialArenaPiece(origin))));
    }

    @Override
    public StructureType<?> type() {
        return StarRailRuinWorldgen.RELIC_TRIAL_ARENA.get();
    }
}
