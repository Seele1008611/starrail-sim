package com.starrail.sim;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import java.io.IOException;
import java.util.Optional;

/** 普通浮空遗迹：只检查空间与地形高度，不筛选主世界群系。 */
public final class FloatingRuinStructure extends Structure {
    public static final Codec<FloatingRuinStructure> CODEC = simpleCodec(FloatingRuinStructure::new);

    public FloatingRuinStructure(StructureSettings settings) { super(settings); }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        final StarRailRuinCommands.Blueprint blueprint;
        try {
            // 自然生成只使用普通母体；九种命途版本由命途寻迹系统单独放置。
            blueprint = StarRailRuinCommands.getBlueprint("ordinary");
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load floating ruin blueprint", exception);
        }
        var bounds = blueprint.bounds();
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        int highest = context.heightAccessor().getMinBuildHeight();
        // 检查整片占地的每个地形柱，避免山峰穿进底座；不依赖已加载区块。
        for (int dx = bounds.minX(); dx <= bounds.maxX(); dx++) {
            for (int dz = bounds.minZ(); dz <= bounds.maxZ(); dz++) {
                highest = Math.max(highest, context.chunkGenerator().getBaseHeight(x + dx, z + dz,
                        Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState()));
            }
        }
        int maxOrigin = context.heightAccessor().getMaxBuildHeight() - 1 - bounds.maxY();
        int minOrigin = Math.max(151, highest + 24) - bounds.minY();
        if (minOrigin > maxOrigin) return Optional.empty();
        // 随机选择合法高度。最高方块不超过319，最低方块始终高于150。
        int y = minOrigin + context.random().nextInt(maxOrigin - minOrigin + 1);
        BlockPos origin = new BlockPos(x, y, z);
        return Optional.of(new GenerationStub(origin,
                builder -> builder.addPiece(new FloatingRuinPiece(origin, blueprint))));
    }

    @Override
    public StructureType<?> type() { return StarRailRuinWorldgen.ORDINARY.get(); }
}
