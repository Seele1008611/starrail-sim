package com.starrail.sim;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public final class DoomsdayArenaStructure extends Structure {
    public static final Codec<DoomsdayArenaStructure> CODEC = simpleCodec(DoomsdayArenaStructure::new);
    public static final ResourceKey<Structure> KEY = ResourceKey.create(Registries.STRUCTURE,
            new ResourceLocation(StarRailSimMod.MOD_ID, "doomsday_arena"));

    public DoomsdayArenaStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        // Terrain only selects an anchor height. The entire building and its clearance replace obstructions.
        int minimum = context.heightAccessor().getMinBuildHeight() + 40;
        int maximum = context.heightAccessor().getMaxBuildHeight() - 26;
        if (minimum > maximum) return Optional.empty();
        int surface = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        int y = Math.max(minimum, Math.min(maximum, surface + 23));
        BlockPos origin = new BlockPos(x, y, z);
        int landingY = -24;
        return Optional.of(new GenerationStub(origin,
                pieces -> pieces.addPiece(new DoomsdayArenaPiece(origin, landingY))));
    }

    @Override
    public StructureType<?> type() {
        return StarRailRuinWorldgen.DOOMSDAY_ARENA.get();
    }
}
