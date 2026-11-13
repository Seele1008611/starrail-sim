package com.starrail.sim;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

final class BrokenRingObservatoryPiece extends StructurePiece {
    private final BlockPos origin;
    private final Set<Long> generatedChunks = new HashSet<>();
    private int guardMask;

    BrokenRingObservatoryPiece(BlockPos origin, int bottom) {
        super(StarRailRuinWorldgen.BROKEN_RING_OBSERVATORY_PIECE.get(), 0, bounds(origin, bottom));
        this.origin = origin.immutable();
    }

    BrokenRingObservatoryPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(StarRailRuinWorldgen.BROKEN_RING_OBSERVATORY_PIECE.get(), tag);
        origin = BlockPos.of(tag.getLong("Origin"));
        guardMask = tag.getInt("GuardMask");
        for (long chunk : tag.getLongArray("GeneratedChunks")) generatedChunks.add(chunk);
    }

    private static BoundingBox bounds(BlockPos origin, int bottom) {
        var bounds = BrokenRingObservatoryLayout.bounds();
        return new BoundingBox(origin.getX() + bounds.minX(), bottom, origin.getZ() + bounds.minZ(),
                origin.getX() + bounds.maxX(),
                origin.getY() + bounds.maxY() + BrokenRingObservatoryLayout.HEADROOM,
                origin.getZ() + bounds.maxZ());
    }

    @Override
    protected synchronized void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putLong("Origin", origin.asLong());
        tag.putInt("GuardMask", guardMask);
        tag.putLongArray("GeneratedChunks", generatedChunks.stream().mapToLong(Long::longValue).sorted().toArray());
    }

    @Override
    public synchronized void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator,
            RandomSource random, BoundingBox chunkBox, ChunkPos chunk, BlockPos reference) {
        if (generatedChunks.contains(chunk.toLong())) return;
        BrokenRingObservatoryLayout.place(level, origin, chunkBox, random);
        guardMask = BrokenRingObservatoryLayout.guards(level, origin, chunkBox, guardMask);
        generatedChunks.add(chunk.toLong());
    }
}
