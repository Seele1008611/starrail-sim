package com.starrail.sim;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/** Incrementally searches saved Path chunks for registered mineral blocks. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID)
public final class StarRailPathOreLocateService {
    private static final int CHUNK_RADIUS = 12;
    private static final int MAX_WORLD_BLOCK = 29_999_984;
    private static final Map<MinecraftServer, LocateJob> ACTIVE = new ConcurrentHashMap<>();

    private StarRailPathOreLocateService() {
    }

    public static int start(CommandSourceStack source, String oreId) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("矿物定位需要由玩家执行。"));
            return 0;
        }
        if (!player.level().dimension().location().equals(StarRailDimensionIds.THE_PATH.location())) {
            source.sendFailure(Component.literal("请先进入命途遗境，再使用矿物定位命令。"));
            return 0;
        }

        OreTarget target = target(oreId);
        if (target == null) {
            source.sendFailure(Component.literal("未知矿物：" + oreId));
            return 0;
        }
        MinecraftServer server = source.getServer();
        if (ACTIVE.containsKey(server)) {
            source.sendFailure(Component.literal("已有矿物定位任务；可执行 /starrail dimension locate ore cancel 取消。"));
            return 0;
        }

        ServerLevel level = (ServerLevel) player.level();
        LocateJob job = new LocateJob(source, level, player.getX(), player.getZ(), target);
        ACTIVE.put(server, job);
        source.sendSuccess(() -> Component.literal("正在附近已生成区域中搜索" + target.displayName
                + "，按距离读取区块存档；本次不会加载或生成新区块。"), false);
        return 1;
    }

    public static int cancel(CommandSourceStack source) {
        LocateJob job = ACTIVE.remove(source.getServer());
        if (job == null) {
            source.sendFailure(Component.literal("当前没有矿物定位任务。"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("已取消矿物定位任务。"), false);
        return 1;
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        LocateJob job = ACTIVE.get(level.getServer());
        if (job != null && job.level == level && job.tick()) {
            ACTIVE.remove(level.getServer(), job);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ACTIVE.remove(event.getServer());
    }

    private static OreTarget target(String id) {
        return switch (id) {
            case "starcoal" -> new OreTarget("星炭矿", PathMineralContent.STARCOAL_ORE.get(), 0, 96);
            case "dawncopper" -> new OreTarget("霞铜矿", PathMineralContent.DAWNCOPPER_ORE.get(), -16, 112);
            case "suniron" -> new OreTarget("曦铁矿", PathMineralContent.SUNIRON_ORE.get(), -48, 96);
            case "dawn_gold" -> new OreTarget("曜金矿", PathMineralContent.DAWN_GOLD_ORE.get(), -48, 24);
            case "resonance" -> new OreTarget("谐振晶矿", PathMineralContent.RESONANCE_ORE.get(), -56, 16);
            case "memory" -> new OreTarget("记忆晶矿", PathMineralContent.MEMORY_ORE.get(), -32, 48);
            case "starmarrow" -> new OreTarget("星髓矿", PathMineralContent.STARMARROW_ORE.get(), -48, 16);
            case "stellarjade" -> new OreTarget("星琼矿", PathMineralContent.STELLARJADE_ORE.get(), -56, 8);
            case "ancientdream" -> new OreTarget("古老梦华矿", PathMineralContent.ANCIENTDREAM_ORE.get(), -60, -32);
            default -> null;
        };
    }

    private record OreTarget(String displayName, Block block, int minY, int maxY) {
    }

    private record Candidate(ChunkPos chunk, double lowerBoundDistanceSquared) {
    }

    private record OreHit(BlockPos pos, double distanceSquared) {
    }

    private static final class LocateJob {
        private final CommandSourceStack source;
        private final ServerLevel level;
        private final OreTarget target;
        private final List<Candidate> candidates;
        private final double centerX;
        private final double centerZ;
        private int nextCandidate;
        private int checkedChunks;
        private int savedChunks;
        private int absentChunks;
        private int unavailableChunks;
        private double closestUnavailableDistanceSquared = Double.MAX_VALUE;
        private BlockPos nearestOre;
        private double nearestOreDistanceSquared = Double.MAX_VALUE;
        private Candidate pendingCandidate;
        private CompletableFuture<Optional<CompoundTag>> pending;

        private LocateJob(CommandSourceStack source, ServerLevel level, double centerX, double centerZ,
                          OreTarget target) {
            this.source = source;
            this.level = level;
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.target = target;
            this.candidates = buildCandidates(centerX, centerZ);
        }

        private static List<Candidate> buildCandidates(double centerX, double centerZ) {
            int centerChunkX = net.minecraft.util.Mth.floor(centerX) >> 4;
            int centerChunkZ = net.minecraft.util.Mth.floor(centerZ) >> 4;
            List<Candidate> result = new ArrayList<>((CHUNK_RADIUS * 2 + 1) * (CHUNK_RADIUS * 2 + 1));
            for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
                for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
                    ChunkPos chunk = new ChunkPos(centerChunkX + dx, centerChunkZ + dz);
                    if (Math.abs(chunk.getMinBlockX()) > MAX_WORLD_BLOCK
                            || Math.abs(chunk.getMinBlockZ()) > MAX_WORLD_BLOCK) continue;
                    double nearestX = Math.max(chunk.getMinBlockX() + 0.5D,
                            Math.min(centerX, chunk.getMinBlockX() + 15.5D));
                    double nearestZ = Math.max(chunk.getMinBlockZ() + 0.5D,
                            Math.min(centerZ, chunk.getMinBlockZ() + 15.5D));
                    double offsetX = nearestX - centerX;
                    double offsetZ = nearestZ - centerZ;
                    result.add(new Candidate(chunk, offsetX * offsetX + offsetZ * offsetZ));
                }
            }
            result.sort(Comparator.comparingDouble(Candidate::lowerBoundDistanceSquared));
            return result;
        }

        /** Reads at most one saved chunk per tick without loading or generating it in the world. */
        private boolean tick() {
            if (pending != null) {
                if (!pending.isDone()) return false;
                checkedChunks++;
                try {
                    Optional<CompoundTag> savedTag = pending.join();
                    if (savedTag.isPresent()) {
                        ProtoChunk chunk = ChunkSerializer.read(level,
                                level.getChunkSource().getPoiManager(), pendingCandidate.chunk(),
                                savedTag.get());
                        inspectChunk(chunk);
                    } else {
                        absentChunks++;
                    }
                } catch (RuntimeException exception) {
                    unavailableChunks++;
                    closestUnavailableDistanceSquared = Math.min(closestUnavailableDistanceSquared,
                            pendingCandidate.lowerBoundDistanceSquared());
                }
                pending = null;
                pendingCandidate = null;
            }

            while (nextCandidate < candidates.size()) {
                Candidate candidate = candidates.get(nextCandidate++);
                if (nearestOre != null
                        && candidate.lowerBoundDistanceSquared() > nearestOreDistanceSquared) {
                    reportNearestOre();
                    return true;
                }

                ChunkAccess loadedChunk = level.getChunkSource().getChunkNow(
                        candidate.chunk().x, candidate.chunk().z);
                if (loadedChunk != null) {
                    checkedChunks++;
                    inspectChunk(loadedChunk);
                    if (checkedChunks > 0 && checkedChunks % 128 == 0) {
                        int checked = checkedChunks;
                        source.sendSuccess(() -> Component.literal("矿物定位中，已检查 " + checked
                                + " 个区块存档……"), false);
                    }
                    return false;
                }

                pendingCandidate = candidate;
                // Read the region-file record directly so unloaded saved chunks are searchable
                // without issuing a chunk ticket or starting terrain generation.
                pending = level.getChunkSource().chunkMap.read(candidate.chunk())
                        .thenApplyAsync(tag -> tag.map(raw -> level.getChunkSource().chunkMap
                                .upgradeChunkTag(level.dimension(),
                                        () -> level.getServer().overworld().getDataStorage(), raw,
                                        level.getChunkSource().getGenerator()
                                                .getTypeNameForDataFixer())), Util.backgroundExecutor());
                if (checkedChunks > 0 && checkedChunks % 128 == 0) {
                    int checked = checkedChunks;
                    source.sendSuccess(() -> Component.literal("矿物定位中，已检查 " + checked
                            + " 个区块存档……"), false);
                }
                return false;
            }

            if (nearestOre != null) {
                reportNearestOre();
                return true;
            }
            String readWarning = unavailableChunks > 0
                    ? " 部分区块读取失败，结果可能不完整。" : "";
            source.sendFailure(Component.literal("搜索结束：已检查 " + checkedChunks
                    + " 个区块位置，读取到 " + savedChunks + " 个存档区块，未生成或无存档 "
                    + absentChunks + " 个，读取失败 " + unavailableChunks + " 个。半径为 "
                    + CHUNK_RADIUS + " 区块，未发现" + target.displayName
                    + "；可扩大探索范围或调整高度后再试。" + readWarning));
            return true;
        }

        private void inspectChunk(ChunkAccess chunk) {
            savedChunks++;
            OreHit found = findNearestOre(chunk);
            if (found != null && found.distanceSquared() < nearestOreDistanceSquared) {
                nearestOre = found.pos();
                nearestOreDistanceSquared = found.distanceSquared();
            }
        }

        private OreHit findNearestOre(ChunkAccess chunk) {
            LevelChunkSection[] sections = chunk.getSections();
            BlockPos nearest = null;
            double nearestDistance = Double.MAX_VALUE;
            int minSection = level.getMinSection();
            for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
                int sectionY = minSection + sectionIndex;
                int sectionMinY = sectionY << 4;
                int minY = Math.max(target.minY, sectionMinY);
                int maxY = Math.min(target.maxY, sectionMinY + 15);
                if (minY > maxY) continue;
                LevelChunkSection section = sections[sectionIndex];
                if (!section.maybeHas(state -> state.is(target.block))) continue;
                for (int y = minY; y <= maxY; y++) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            if (!section.getBlockState(x, y & 15, z).is(target.block)) continue;
                            int worldX = chunk.getPos().getMinBlockX() + x;
                            int worldZ = chunk.getPos().getMinBlockZ() + z;
                            double dx = worldX + 0.5 - centerX;
                            double dz = worldZ + 0.5 - centerZ;
                            double distance = dx * dx + dz * dz;
                            if (distance < nearestDistance) {
                                nearestDistance = distance;
                                nearest = new BlockPos(worldX, y, worldZ);
                            }
                        }
                    }
                }
            }
            return nearest == null ? null : new OreHit(nearest, nearestDistance);
        }

        private void reportNearestOre() {
            BlockPos pos = nearestOre;
            String command = "/execute in " + StarRailDimensionIds.THE_PATH.location()
                    + " run tp @s " + (pos.getX() + 0.5) + " " + (pos.getY() + 1)
                    + " " + (pos.getZ() + 0.5);
            String caveat = closestUnavailableDistanceSquared < nearestOreDistanceSquared
                    ? "；有更近的区块未能读取，无法完全确认最近结果" : "";
            source.sendSuccess(() -> Component.literal("找到附近最近的" + target.displayName + "："
                            + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()
                            + "（点击传送到矿石上方" + caveat + "）")
                    .withStyle(style -> style.withClickEvent(
                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))), false);
        }
    }
}
