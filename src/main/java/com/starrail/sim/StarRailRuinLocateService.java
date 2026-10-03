package com.starrail.sim;

import com.mojang.datafixers.util.Either;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/** 只定位自然生成的普通母体；按区块状态逐步检查，避免同步生成大量区块卡住服务器。 */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID)
public final class StarRailRuinLocateService {
    // 搜索配置间距周围八个区段，限制候选区块数量，避免无边界地加载新区块。
    private static final int REGION_RADIUS = 8;
    private static final int MAX_WORLD_BLOCK = 29_999_984;
    private static final ResourceKey<Structure> RUIN_KEY = ResourceKey.create(
            Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(StarRailSimMod.MOD_ID,
                    "ordinary_floating_ruin"));
    private static final Map<MinecraftServer, LocateJob> ACTIVE = new ConcurrentHashMap<>();

    private StarRailRuinLocateService() { }

    /** 开始由近到远检查自然生成候选区块，只在找到普通母体时返回坐标。 */
    public static int start(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        if (level.dimension() != Level.OVERWORLD) {
            source.sendFailure(Component.literal("普通浮空遺跡只會在主世界生成。"));
            return 0;
        }
        MinecraftServer server = source.getServer();
        LocateJob existing = ACTIVE.get(server);
        if (existing != null) {
            source.sendFailure(Component.literal("已有普通母體定位任務；可执行 /starrail ruin locate ordinary cancel 取消。"));
            return 0;
        }

        Holder<Structure> holder = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .getHolder(RUIN_KEY).orElse(null);
        if (holder == null) {
            source.sendFailure(Component.literal("当前世界未注册浮空遗迹结构。"));
            return 0;
        }
        List<RandomSpreadStructurePlacement> placements = level.getChunkSource()
                .getGeneratorState().getPlacementsForStructure(holder).stream()
                .filter(RandomSpreadStructurePlacement.class::isInstance)
                .map(RandomSpreadStructurePlacement.class::cast).toList();
        if (placements.isEmpty()) {
            source.sendFailure(Component.literal("当前世界没有浮空遗迹自然生成配置。"));
            return 0;
        }

        LocateJob job = new LocateJob(source, level, holder.value(), placements);
        ACTIVE.put(server, job);
        source.sendSuccess(() -> Component.literal(
                "正在由近到远查找自然生成的普通母体；其他命途变体会自动跳过。"), false);
        return 1;
    }

    /** 停止当前搜索；已提交的区块仍按原世界生成规则处理。 */
    public static int cancel(CommandSourceStack source) {
        LocateJob job = ACTIVE.remove(source.getServer());
        if (job == null) {
            source.sendFailure(Component.literal("当前没有普通母体定位任务。"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(
                "已取消后续扫描；当前候选区块若已开始生成，仍可能完成并保存。"), false);
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

    private static final class LocateJob {
        private final CommandSourceStack source;
        private final ServerLevel level;
        private final Structure structure;
        private final List<Candidate> candidates;
        private int nextCandidate;
        private int checkedCandidates;
        private int validRuinStarts;
        private int floatingRuinPieces;
        private int skippedVariants;
        private int failedChunkRequests;
        private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> pending;

        private LocateJob(CommandSourceStack source, ServerLevel level, Structure structure,
                          List<RandomSpreadStructurePlacement> placements) {
            this.source = source;
            this.level = level;
            this.structure = structure;
            this.candidates = buildCandidates(source, level, placements);
        }

        /** 按候选区块与玩家的水平距离排序，优先检查近处的自然生成位置。 */
        private static List<Candidate> buildCandidates(CommandSourceStack source, ServerLevel level,
                                                        List<RandomSpreadStructurePlacement> placements) {
            List<Candidate> result = new ArrayList<>();
            int centerChunkX = net.minecraft.util.Mth.floor(source.getPosition().x) >> 4;
            int centerChunkZ = net.minecraft.util.Mth.floor(source.getPosition().z) >> 4;
            long centerX = net.minecraft.util.Mth.floor(source.getPosition().x);
            long centerZ = net.minecraft.util.Mth.floor(source.getPosition().z);
            for (RandomSpreadStructurePlacement placement : placements) {
                int centerRegionX = Math.floorDiv(centerChunkX, placement.spacing());
                int centerRegionZ = Math.floorDiv(centerChunkZ, placement.spacing());
                for (int rx = -REGION_RADIUS; rx <= REGION_RADIUS; rx++) {
                    for (int rz = -REGION_RADIUS; rz <= REGION_RADIUS; rz++) {
                        ChunkPos chunk = placement.getPotentialStructureChunk(level.getSeed(),
                                centerRegionX + rx, centerRegionZ + rz);
                        long x = chunk.getMiddleBlockX();
                        long z = chunk.getMiddleBlockZ();
                        if (Math.abs(x) > MAX_WORLD_BLOCK || Math.abs(z) > MAX_WORLD_BLOCK) continue;
                        long dx = x - centerX;
                        long dz = z - centerZ;
                        result.add(new Candidate(chunk, dx * dx + dz * dz));
                    }
                }
            }
            result.sort(Comparator.comparingLong(Candidate::distanceSquared));
            return result;
        }

        /** 每 tick 最多提交一个区块结构起始状态请求，避免一次性同步跑图卡顿。 */
        private boolean tick() {
            if (pending != null) {
                if (!pending.isDone()) return false;
                try {
                    Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> result = pending.join();
                    ChunkAccess chunk = result.left().orElse(null);
                    if (chunk == null) {
                        failedChunkRequests++;
                    } else {
                        checkedCandidates++;
                        // 遍历区块保存的全部起点，并按结构类型筛选，避免单键查询漏掉已记录的起点。
                        for (StructureStart start : chunk.getAllStarts().values()) {
                            if (start.getStructure().type() != structure.type() || !start.isValid()) continue;
                            validRuinStarts++;
                            for (var piece : start.getPieces()) {
                                if (!(piece instanceof FloatingRuinPiece ruin)) continue;
                                floatingRuinPieces++;
                                if ("ordinary".equals(ruin.getVariantId())) {
                                    reportFound(ruin.getOrigin());
                                    return true;
                                }
                                skippedVariants++;
                            }
                        }
                    }
                } catch (RuntimeException exception) {
                    // 单个区块请求失败时计数并继续，最终把失败数显示给测试者。
                    failedChunkRequests++;
                }
                pending = null;
            }

            if (nextCandidate >= candidates.size()) {
                source.sendFailure(Component.literal("扫描结束：已检查区块 " + checkedCandidates
                        + "/" + candidates.size() + "，有效遗迹起点 " + validRuinStarts
                        + "，遗迹结构片 " + floatingRuinPieces + "，跳过的其他变体 "
                        + skippedVariants + "，区块请求失败 " + failedChunkRequests
                        + "。本次范围内没有找到普通母体；请依据这些计数反馈结果。"));
                return true;
            }

            Candidate candidate = candidates.get(nextCandidate++);
            ChunkPos chunk = candidate.chunk();
            pending = level.getChunkSource().getChunkFuture(chunk.x, chunk.z,
                    ChunkStatus.STRUCTURE_STARTS, true);
            if (checkedCandidates > 0 && checkedCandidates % 64 == 0) {
                int checked = checkedCandidates;
                source.sendSuccess(() -> Component.literal(
                        "普通母体定位中，已完成 " + checked + " 个候选区块检查……"), false);
            }
            return false;
        }

        private void reportFound(net.minecraft.core.BlockPos origin) {
            double dx = origin.getX() - source.getPosition().x;
            double dz = origin.getZ() - source.getPosition().z;
            int distance = (int) Math.sqrt(dx * dx + dz * dz);
            source.sendSuccess(() -> Component.literal("找到自然生成的普通母体：锚点 X="
                    + origin.getX() + " Y=" + origin.getY() + " Z=" + origin.getZ()
                    + "，水平距离约 " + distance + " 格。可用 /tp " + origin.getX() + " "
                    + (origin.getY() + 12) + " " + origin.getZ() + " 前往遗迹上方观察。"), false);
        }
    }
}
