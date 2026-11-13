package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

final class DoomsdayArenaPiece extends StructurePiece {
    private final BlockPos origin;
    private final int layoutVersion;
    private final int landingY;
    private int guardMask;

    DoomsdayArenaPiece(BlockPos origin, int landingY) {
        super(StarRailRuinWorldgen.DOOMSDAY_ARENA_PIECE.get(), 0,
                new BoundingBox(origin.getX() - 52, origin.getY() - 40, origin.getZ() - 124,
                        origin.getX() + 52, origin.getY() + 25, origin.getZ() + 25));
        this.origin = origin;
        layoutVersion = 3;
        this.landingY = landingY;
    }

    DoomsdayArenaPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(StarRailRuinWorldgen.DOOMSDAY_ARENA_PIECE.get(), tag);
        origin = BlockPos.of(tag.getLong("Origin"));
        layoutVersion = tag.getInt("LayoutVersion");
        landingY = tag.contains("LandingY") ? tag.getInt("LandingY") : -14;
        guardMask = tag.getInt("GuardMask");
    }

    BlockPos beacon() { return origin.offset(0, 1, -23); }

    BlockPos spawn() { return origin.offset(0, 1, 0); }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putLong("Origin", origin.asLong());
        tag.putInt("LayoutVersion", layoutVersion);
        tag.putInt("LandingY", landingY);
        tag.putInt("GuardMask", guardMask);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunk, BlockPos reference) {
        if (layoutVersion >= 1) {
            DoomsdayTerminalLayout.place(level, origin, chunkBox, chunk, random, landingY, layoutVersion);
            guardMask = DoomsdayTerminalLayout.guards(level, origin, chunkBox, guardMask);
            return;
        }
        int minX = Math.max(-20, chunkBox.minX() - origin.getX());
        int maxX = Math.min(20, chunkBox.maxX() - origin.getX());
        int minZ = Math.max(-33, chunkBox.minZ() - origin.getZ());
        int maxZ = Math.min(14, chunkBox.maxZ() - origin.getZ());
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int ax = Math.abs(x);
                boolean front = z >= -25 && z <= -2 && ax <= 17;
                boolean rear = z >= -1 && ax <= 17 && (z <= 11 || ax <= 14 && z <= 14);
                boolean access = z < -25 && ax <= 4;
                if (!front && !rear && !access) continue;
                int floor = access ? (z + 25) / 2 : 0;
                for (int y = floor + 1; y <= 10; y++) put(level, chunkBox, x, y, z, Blocks.AIR);
                Block deck = front ? Blocks.SMOOTH_QUARTZ : Blocks.POLISHED_DEEPSLATE;
                if (ax == 17 || z == -2 || z == 14) deck = Blocks.POLISHED_BLACKSTONE;
                if (front && (ax == 5 && z % 4 == 0 || z == -3 && ax % 4 == 0)) deck = Blocks.SEA_LANTERN;
                if (front && (ax == 6 || z == -20 && ax <= 6)) deck = Blocks.CUT_COPPER;
                if (rear && (ax == 10 || z == 9) && (x + z) % 3 == 0) deck = Blocks.OCHRE_FROGLIGHT;
                put(level, chunkBox, x, floor, z, deck);
                put(level, chunkBox, x, floor - 1, z, Blocks.POLISHED_DEEPSLATE);
                if (ax == 13 && (z == -22 || z == -10 || z == 6) || access && ax <= 2) {
                    for (int y = floor - 2; y >= Math.max(-24, level.getMinBuildHeight() - origin.getY()); y--) {
                        BlockPos pos = origin.offset(x, y, z);
                        if (!chunkBox.isInside(pos) || level.getBlockState(pos).isSolidRender(level, pos)) break;
                        put(level, chunkBox, x, y, z, Blocks.DEEPSLATE_BRICKS);
                    }
                }
            }
        }
        for (int side : new int[] {-1, 1}) {
            for (int z : new int[] {-24, -12, 7}) {
                for (int y = 0; y <= 6; y++) {
                    for (int dx = 0; dx <= 2; dx++) {
                        put(level, chunkBox, side * (18 + dx), y, z,
                                y == 5 ? Blocks.SEA_LANTERN : Blocks.POLISHED_BLACKSTONE_BRICKS);
                    }
                }
            }
        }
        put(level, chunkBox, 0, 1, -23, StarRailSimMod.DOOMSDAY_SUMMONING_CONSOLE.get());
    }

    private void put(WorldGenLevel level, BoundingBox chunkBox, int x, int y, int z, Block block) {
        BlockPos pos = origin.offset(x, y, z);
        if (chunkBox.isInside(pos)) {
            level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
    }
}
