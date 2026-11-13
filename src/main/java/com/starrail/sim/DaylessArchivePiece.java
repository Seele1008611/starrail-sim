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

final class DaylessArchivePiece extends StructurePiece {
    private final BlockPos origin;
    private final int layoutVersion;
    private final Set<Long> generatedChunks = new HashSet<>();
    private int guardMask;

    DaylessArchivePiece(BlockPos origin) {
        super(StarRailRuinWorldgen.DAYLESS_ARCHIVE_PIECE.get(), 0,
                new BoundingBox(origin.getX() - 66, origin.getY() - 14, origin.getZ() - 54,
                        origin.getX() + 73, origin.getY() + 55, origin.getZ() + 61));
        this.origin = origin.immutable();
        layoutVersion = 5;
    }

    DaylessArchivePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(StarRailRuinWorldgen.DAYLESS_ARCHIVE_PIECE.get(), tag);
        origin = BlockPos.of(tag.getLong("Origin"));
        layoutVersion = tag.contains("LayoutVersion") ? tag.getInt("LayoutVersion") : 1;
        guardMask = tag.getInt("GuardMask");
        for (long key : tag.getLongArray("GeneratedChunks")) generatedChunks.add(key);
    }

    @Override
    protected synchronized void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putLong("Origin", origin.asLong());
        tag.putInt("LayoutVersion", layoutVersion);
        tag.putInt("GuardMask", guardMask);
        tag.putLongArray("GeneratedChunks", generatedChunks.stream().mapToLong(Long::longValue).sorted().toArray());
    }

    @Override
    public synchronized void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator,
            RandomSource random, BoundingBox chunkBox, ChunkPos chunk, BlockPos reference) {
        if (generatedChunks.contains(chunk.toLong())) return;
        DaylessArchiveLayout.place(level, origin, chunkBox, chunk, random, layoutVersion);
        guardMask = DaylessArchiveLayout.guards(level, origin, chunkBox, guardMask, layoutVersion);
        generatedChunks.add(chunk.toLong());
    }
}
