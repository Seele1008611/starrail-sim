package com.starrail.sim;

import com.mojang.datafixers.util.Either;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/** 定位自然生成的遗迹与决斗场，并按区块状态逐步核验候选起点。 */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID)
public final class StarRailRuinLocateService {
    // 没有已知结构时的初始搜索半径；找到候选后按其距离扩展覆盖范围。
    private static final int REGION_RADIUS = 8;
    private static final int MAX_REGION_RADIUS = 32;
    // 先检查玩家当前视距附近已经完整加载的区块，兼容旧结构间距与已生成结构。
    private static final int LOADED_CHUNK_RADIUS = 16;
    private static final int MAX_WORLD_BLOCK = 29_999_984;
    private static final ResourceKey<Structure> RUIN_KEY = ResourceKey.create(
            Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(StarRailSimMod.MOD_ID,
                    "ordinary_floating_ruin"));
    private static final ResourceKey<Structure> ARENA_KEY = ResourceKey.create(
            Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(StarRailSimMod.MOD_ID,
                    "relic_trial_arena"));
    private static final Map<MinecraftServer, LocateJob> ACTIVE = new ConcurrentHashMap<>();

    private StarRailRuinLocateService() { }

    /** 开始由近到远定位普通浮空遗迹。 */
    public static int start(CommandSourceStack source) {
        return start(source, null, Target.ORDINARY);
    }

    /** 在命令来源维度运行，或显式指定另一个已加载维度。 */
    public static int start(CommandSourceStack source, ResourceLocation requestedDimension) {
        return start(source, requestedDimension, Target.ORDINARY);
    }

    public static int startArena(CommandSourceStack source, ResourceLocation requestedDimension) {
        return start(source, requestedDimension, Target.ARENA);
    }

    private static int start(CommandSourceStack source, ResourceLocation requestedDimension,
                             Target target) {
        ServerLevel level = requestedDimension == null ? source.getLevel()
                : source.getServer().getLevel(ResourceKey.create(Registries.DIMENSION,
                        requestedDimension));
        if (level == null) {
            source.sendFailure(Component.literal("目标维度不存在或尚未加载："
                    + requestedDimension));
            return 0;
        }
        MinecraftServer server = source.getServer();
        LocateJob existing = ACTIVE.get(server);
        if (existing != null) {
            source.sendFailure(Component.literal("已有结构定位任务；可执行 /starrail dimension locate cancel 取消。"));
            return 0;
        }

        Holder<Structure> holder = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .getHolder(target.structureKey).orElse(null);
        if (holder == null) {
            source.sendFailure(Component.literal("当前世界未注册" + target.displayName + "结构。"));
            return 0;
        }
        List<RandomSpreadStructurePlacement> placements = level.getChunkSource()
                .getGeneratorState().getPlacementsForStructure(holder).stream()
                .filter(RandomSpreadStructurePlacement.class::isInstance)
                .map(RandomSpreadStructurePlacement.class::cast).toList();
        if (placements.isEmpty()) {
            source.sendFailure(Component.literal("当前维度没有" + target.displayName + "自然生成配置。"));
            return 0;
        }

        StarRailRuinSpacingData spacingData = StarRailRuinSpacingData.get(level);
        BlockPos loaded = findNearestLoaded(level, source.getPosition(), holder.value(), target);
        if (loaded != null) target.record(spacingData, loaded);
        BlockPos recorded = target.findNearestRecorded(spacingData,
                net.minecraft.util.Mth.floor(source.getPosition().x),
                net.minecraft.util.Mth.floor(source.getPosition().z));
        BlockPos bestKnown = nearer(source.getPosition(), loaded, recorded);

        LocateJob job = new LocateJob(source, level, holder.value(), placements, target, bestKnown);
        ACTIVE.put(server, job);
        source.sendSuccess(() -> Component.literal(
                "正在维度 " + level.dimension().location()
                        + " 按距离核验" + target.displayName
                        + "：优先检查可能更近的生成位置，再与已记录结构比较。"), false);
        return 1;
    }

    /** 从当前视距内已加载的区块结构起点中找目标结构。 */
    private static BlockPos findNearestLoaded(ServerLevel level,
                                              net.minecraft.world.phys.Vec3 position,
                                              Structure structure, Target target) {
        int centerChunkX = net.minecraft.util.Mth.floor(position.x) >> 4;
        int centerChunkZ = net.minecraft.util.Mth.floor(position.z) >> 4;
        BlockPos nearest = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        for (int dx = -LOADED_CHUNK_RADIUS; dx <= LOADED_CHUNK_RADIUS; dx++) {
            for (int dz = -LOADED_CHUNK_RADIUS; dz <= LOADED_CHUNK_RADIUS; dz++) {
                var chunk = level.getChunkSource().getChunkNow(centerChunkX + dx, centerChunkZ + dz);
                if (chunk == null) continue;
                for (StructureStart start : chunk.getAllStarts().values()) {
                    if (start.getStructure().type() != structure.type() || !start.isValid()) continue;
                    for (var piece : start.getPieces()) {
                        BlockPos origin = target.pieceOrigin(piece);
                        if (origin == null) continue;
                        double distanceX = origin.getX() - position.x;
                        double distanceZ = origin.getZ() - position.z;
                        double distanceSquared = distanceX * distanceX + distanceZ * distanceZ;
                        if (distanceSquared < nearestDistanceSquared) {
                            nearest = origin;
                            nearestDistanceSquared = distanceSquared;
                        }
                    }
                }
            }
        }
        return nearest;
    }

    private static BlockPos nearer(net.minecraft.world.phys.Vec3 position,
                                   BlockPos first, BlockPos second) {
        if (first == null) return second;
        if (second == null) return first;
        return horizontalDistanceSquared(position, first) <= horizontalDistanceSquared(position, second)
                ? first : second;
    }

    private static long horizontalDistanceSquared(net.minecraft.world.phys.Vec3 position,
                                                  BlockPos target) {
        double dx = target.getX() - position.x;
        double dz = target.getZ() - position.z;
        return (long) (dx * dx + dz * dz);
    }

    private static void reportFound(CommandSourceStack source, ServerLevel level,
                                    Target target, BlockPos origin, String detail) {
        double dx = origin.getX() - source.getPosition().x;
        double dz = origin.getZ() - source.getPosition().z;
        int distance = (int) Math.sqrt(dx * dx + dz * dz);
        source.sendSuccess(() -> Component.literal("定位到最近的" + target.displayName
                + detail + "：锚点 X=" + origin.getX() + " Y=" + origin.getY() + " Z="
                + origin.getZ() + "，维度 " + level.dimension().location() + "，水平距离约 "
                + distance + " 格。请先进入目标维度，再传送到该坐标。"), false);
    }

    /** 停止当前搜索；已提交的区块仍按原世界生成规则处理。 */
    public static int cancel(CommandSourceStack source) {
        LocateJob job = ACTIVE.remove(source.getServer());
        if (job == null) {
            source.sendFailure(Component.literal("当前没有结构定位任务。"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(
                "已取消后续结构扫描；已开始的候选区块生成仍可能完成并保存。"), false);
        return 1;
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        LocateJob job = ACTIVE.get(level.getServer());
        if (job != null && job.level == level && job.tick()) ACTIVE.remove(level.getServer(), job);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ACTIVE.remove(event.getServer());
        // 服务端关闭时停止后续扫描；已提交的区块请求由原版区块系统自行收尾。
    }

    private record Candidate(ChunkPos chunk, long distanceSquared) { }

    private enum Target {
        ORDINARY(RUIN_KEY, "普通浮空遗迹"),
        ARENA(ARENA_KEY, "遗器决斗场");

        private final ResourceKey<Structure> structureKey;
        private final String displayName;

        Target(ResourceKey<Structure> structureKey, String displayName) {
            this.structureKey = structureKey;
            this.displayName = displayName;
        }

        private BlockPos pieceOrigin(StructurePiece piece) {
            if (this == ORDINARY && piece instanceof FloatingRuinPiece ruin
                    && "ordinary".equals(ruin.getVariantId())) {
                return ruin.getOrigin();
            }
            if (this == ARENA && piece instanceof RelicTrialArenaPiece arena) {
                return arena.getOrigin().offset(RelicTrialArenaCommands.CENTER, 0,
                        RelicTrialArenaCommands.CENTER);
            }
            return null;
        }

        private BlockPos findNearestRecorded(StarRailRuinSpacingData data, int x, int z) {
            return this == ORDINARY ? data.findNearestOrdinaryRuin(x, z)
                    : data.findNearestArena(x, z);
        }

        private void record(StarRailRuinSpacingData data, BlockPos origin) {
            if (this == ORDINARY) data.recordOrdinaryRuin(origin);
            else data.recordArena(origin);
        }

        private BlockPos predictedOrigin(ChunkPos chunk) {
            return new BlockPos(chunk.getMiddleBlockX(), 0, chunk.getMiddleBlockZ());
        }
    }

    private static final class LocateJob {
        private final CommandSourceStack source;
        private final ServerLevel level;
        private final Structure structure;
        private final List<RandomSpreadStructurePlacement> placements;
        private final Target target;
        private final Set<Long> checkedChunks = new HashSet<>();
        private List<Candidate> candidates;
        private BlockPos bestOrigin;
        private long bestDistanceSquared;
        private long closestFailedDistanceSquared = Long.MAX_VALUE;
        private Candidate pendingCandidate;
        private int searchRegionRadius;
        private int nextCandidate;
        private int checkedCandidates;
        private int failedChunkRequests;
        private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> pending;

        private LocateJob(CommandSourceStack source, ServerLevel level, Structure structure,
                          List<RandomSpreadStructurePlacement> placements, Target target,
                          BlockPos bestKnown) {
            this.source = source;
            this.level = level;
            this.structure = structure;
            this.placements = placements;
            this.target = target;
            this.bestOrigin = bestKnown;
            this.bestDistanceSquared = bestKnown == null ? Long.MAX_VALUE
                    : horizontalDistanceSquared(source.getPosition(), bestKnown);
            this.searchRegionRadius = bestKnown == null ? REGION_RADIUS
                    : Math.min(MAX_REGION_RADIUS,
                    requiredRegionRadius(bestDistanceSquared, placements));
            this.candidates = buildCandidates(source, level, placements, target,
                    searchRegionRadius);
        }

        /** 枚举覆盖已知最近锚点距离的候选区段，并按实际结构锚点距离排序。 */
        private static List<Candidate> buildCandidates(CommandSourceStack source, ServerLevel level,
                                                        List<RandomSpreadStructurePlacement> placements,
                                                        Target target, int regionRadius) {
            Map<Long, Candidate> unique = new HashMap<>();
            int centerChunkX = net.minecraft.util.Mth.floor(source.getPosition().x) >> 4;
            int centerChunkZ = net.minecraft.util.Mth.floor(source.getPosition().z) >> 4;
            for (RandomSpreadStructurePlacement placement : placements) {
                int centerRegionX = Math.floorDiv(centerChunkX, placement.spacing());
                int centerRegionZ = Math.floorDiv(centerChunkZ, placement.spacing());
                for (int rx = -regionRadius; rx <= regionRadius; rx++) {
                    for (int rz = -regionRadius; rz <= regionRadius; rz++) {
                        ChunkPos chunk = placement.getPotentialStructureChunk(level.getSeed(),
                                centerRegionX + rx, centerRegionZ + rz);
                        BlockPos origin = target.predictedOrigin(chunk);
                        if (Math.abs(origin.getX()) > MAX_WORLD_BLOCK
                                || Math.abs(origin.getZ()) > MAX_WORLD_BLOCK) continue;
                        Candidate candidate = new Candidate(chunk,
                                horizontalDistanceSquared(source.getPosition(), origin));
                        unique.putIfAbsent(ChunkPos.asLong(chunk.x, chunk.z), candidate);
                    }
                }
            }
            List<Candidate> result = new ArrayList<>(unique.values());
            result.sort(Comparator.comparingLong(Candidate::distanceSquared));
            return result;
        }

        private static int requiredRegionRadius(long distanceSquared,
                                                List<RandomSpreadStructurePlacement> placements) {
            double distance = Math.sqrt(distanceSquared);
            int radius = 1;
            for (RandomSpreadStructurePlacement placement : placements) {
                radius = Math.max(radius,
                        (int) Math.ceil(distance / (placement.spacing() * 16.0D)) + 2);
            }
            return radius;
        }

        private void expandCoverageIfNeeded() {
            if (bestOrigin == null) return;
            int required = requiredRegionRadius(bestDistanceSquared, placements);
            if (required <= searchRegionRadius || searchRegionRadius >= MAX_REGION_RADIUS) return;
            searchRegionRadius = Math.min(MAX_REGION_RADIUS, required);
            candidates = buildCandidates(source, level, placements, target, searchRegionRadius);
            nextCandidate = 0;
        }

        /** 每 tick 最多提交一个区块结构起始状态请求，避免一次性同步跑图卡顿。 */
        private boolean tick() {
            if (pending != null) {
                if (!pending.isDone()) return false;
                Candidate completed = pendingCandidate;
                try {
                    Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> result = pending.join();
                    ChunkAccess chunk = result.left().orElse(null);
                    if (chunk == null) {
                        failedChunkRequests++;
                        closestFailedDistanceSquared = Math.min(closestFailedDistanceSquared,
                                completed.distanceSquared());
                        checkedChunks.add(ChunkPos.asLong(completed.chunk().x,
                                completed.chunk().z));
                    } else {
                        checkedCandidates++;
                        for (StructureStart start : chunk.getAllStarts().values()) {
                            if (start.getStructure().type() != structure.type() || !start.isValid()) continue;
                            for (var piece : start.getPieces()) {
                                BlockPos origin = target.pieceOrigin(piece);
                                if (origin == null) continue;
                                target.record(StarRailRuinSpacingData.get(level), origin);
                                long distanceSquared = horizontalDistanceSquared(source.getPosition(), origin);
                                if (distanceSquared < bestDistanceSquared) {
                                    bestOrigin = origin;
                                    bestDistanceSquared = distanceSquared;
                                }
                            }
                        }
                        checkedChunks.add(ChunkPos.asLong(completed.chunk().x, completed.chunk().z));
                    }
                } catch (RuntimeException exception) {
                    failedChunkRequests++;
                    closestFailedDistanceSquared = Math.min(closestFailedDistanceSquared,
                            completed.distanceSquared());
                    checkedChunks.add(ChunkPos.asLong(completed.chunk().x, completed.chunk().z));
                }
                pending = null;
                pendingCandidate = null;
            }

            expandCoverageIfNeeded();
            while (nextCandidate < candidates.size()) {
                Candidate candidate = candidates.get(nextCandidate++);
                long key = ChunkPos.asLong(candidate.chunk().x, candidate.chunk().z);
                if (checkedChunks.contains(key)) continue;

                if (bestOrigin != null && candidate.distanceSquared() >= bestDistanceSquared) {
                    reportBest();
                    return true;
                }

                pendingCandidate = candidate;
                pending = level.getChunkSource().getChunkFuture(candidate.chunk().x,
                        candidate.chunk().z, ChunkStatus.STRUCTURE_STARTS, true);
                if (checkedCandidates > 0 && checkedCandidates % 64 == 0) {
                    int checked = checkedCandidates;
                    source.sendSuccess(() -> Component.literal(target.displayName
                            + "定位中，已检查 " + checked + " 个候选区块……"), false);
                }
                return false;
            }

            if (bestOrigin != null) {
                reportBest();
                return true;
            }

            source.sendFailure(Component.literal("扫描结束：已检查 " + checkedCandidates
                    + "/" + candidates.size() + " 个候选区块，区段半径 " + searchRegionRadius
                    + "，读取失败 " + failedChunkRequests + "，维度 "
                    + level.dimension().location() + "。搜索范围内没有找到" + target.displayName + "。"));
            return true;
        }

        private void reportBest() {
            if (bestOrigin == null) return;
            boolean searchCapped = requiredRegionRadius(bestDistanceSquared, placements)
                    > MAX_REGION_RADIUS;
            String detail = "（已按距离检查更近的候选点";
            if (searchCapped) {
                detail += "；达到搜索半径上限，坐标为最近已确认结构";
            }
            if (failedChunkRequests > 0
                    && closestFailedDistanceSquared < bestDistanceSquared) {
                detail += "；有 " + failedChunkRequests + " 个更近候选区块读取失败，无法完全确认最近性";
            }
            detail += "）";
            reportFound(source, level, target, bestOrigin, detail);
        }
    }
}
