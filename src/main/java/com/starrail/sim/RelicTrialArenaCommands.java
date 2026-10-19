package com.starrail.sim;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Permission-gated, safe in-game builder matching the 25x25 duel-arena prototype. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RelicTrialArenaCommands {
    static final int SIZE = 25;
    static final int HEIGHT = 10;
    static final int CENTER = 12;
    private static final int BLOCKS_PER_TICK = 384;
    private static final int[] PROGRESS_MARKS = {25, 50, 75};
    private static final Map<MinecraftServer, BuildJob> ACTIVE_JOBS = new LinkedHashMap<>();

    private RelicTrialArenaCommands() {}

    static boolean isBuildActive(MinecraftServer server) {
        return ACTIVE_JOBS.containsKey(server);
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("starrail")
                .then(Commands.literal("arena")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("build")
                                .executes(context -> build(context, null))
                                .then(Commands.argument("dimension", ResourceLocationArgument.id())
                                        .executes(context -> build(context,
                                                ResourceLocationArgument.getId(context, "dimension")))))
                        .then(Commands.literal("locate")
                                .executes(context -> locate(context, null))
                                .then(Commands.argument("dimension", ResourceLocationArgument.id())
                                        .executes(context -> locate(context,
                                                ResourceLocationArgument.getId(context, "dimension")))))
                        .then(Commands.literal("cancel").executes(RelicTrialArenaCommands::cancel))));
    }

    private static int build(CommandContext<CommandSourceStack> context,
                             ResourceLocation requestedDimension) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("message.starrail_sim.arena.build_player_only"));
            return 0;
        }
        MinecraftServer server = source.getServer();
        ServerLevel level = requestedDimension == null ? player.serverLevel()
                : server.getLevel(ResourceKey.create(Registries.DIMENSION, requestedDimension));
        if (level == null) {
            source.sendFailure(Component.literal("目标维度不存在或尚未加载："
                    + requestedDimension));
            return 0;
        }
        if (ACTIVE_JOBS.containsKey(server) || StarRailRuinCommands.isPlacementActive(server)) {
            source.sendFailure(Component.translatable("message.starrail_sim.arena.build_busy"));
            return 0;
        }

        // The player stands outside the north gate. The floor starts at their feet level,
        // centered on X and two blocks south of them, so a flat supported site is required.
        BlockPos playerPos = player.blockPosition();
        BlockPos origin = new BlockPos(playerPos.getX() - CENTER, playerPos.getY(),
                playerPos.getZ() + 2);
        int arenaCenterX = origin.getX() + CENTER;
        int arenaCenterZ = origin.getZ() + CENTER;
        if (!StarRailRuinSpacingData.get(level).hasClearance(level, arenaCenterX, arenaCenterZ)) {
            source.sendFailure(Component.translatable(
                    "message.starrail_sim.path_seek_no_spaced_site"));
            return 0;
        }
        LinkedHashMap<BlockPos, BlockState> plan = createPlan(origin);
        Component issue = validateSite(level, origin, plan, player);
        if (issue != null) {
            source.sendFailure(issue);
            return 0;
        }

        BuildJob job = new BuildJob(source, level, origin, plan);
        ACTIVE_JOBS.put(server, job);
        source.sendSuccess(() -> Component.translatable(
                "message.starrail_sim.arena.build_started", plan.size(),
                level.dimension().location()), false);
        return 1;
    }

    private static int locate(CommandContext<CommandSourceStack> context,
                              ResourceLocation requestedDimension) {
        CommandSourceStack source = context.getSource();
        ServerLevel target = requestedDimension == null ? source.getLevel()
                : source.getServer().getLevel(ResourceKey.create(Registries.DIMENSION,
                        requestedDimension));
        if (target == null) {
            source.sendFailure(Component.literal("目标维度不存在或尚未加载："
                    + requestedDimension));
            return 0;
        }
        return StarRailRuinLocateService.startArena(source, target.dimension().location());
    }

    private static int cancel(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();
        BuildJob job = ACTIVE_JOBS.remove(server);
        if (job == null) {
            context.getSource().sendFailure(Component.translatable(
                    "message.starrail_sim.arena.build_not_running"));
            return 0;
        }
        job.rollback();
        context.getSource().sendSuccess(() -> Component.translatable(
                "message.starrail_sim.arena.build_cancelled"), false);
        return 1;
    }

    private static Component validateSite(ServerLevel level, BlockPos origin,
                                         Map<BlockPos, BlockState> plan, ServerPlayer player) {
        if (origin.getY() < level.getMinBuildHeight()
                || origin.getY() + HEIGHT > level.getMaxBuildHeight()) {
            return Component.translatable("message.starrail_sim.arena.build_height");
        }
        for (int y = 0; y < HEIGHT; y++) {
            for (int z = 0; z < SIZE; z++) {
                for (int x = 0; x < SIZE; x++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (!level.getWorldBorder().isWithinBounds(pos)) {
                        return Component.translatable("message.starrail_sim.arena.build_border");
                    }
                    if (!level.hasChunkAt(pos)) {
                        return Component.translatable("message.starrail_sim.arena.build_chunks");
                    }
                    BlockState existing = level.getBlockState(pos);
                    if (!isReplaceableSite(existing)) {
                        return Component.translatable("message.starrail_sim.arena.build_obstructed",
                                pos.getX(), pos.getY(), pos.getZ(), existing.getBlock().getName());
                    }
                }
            }
        }
        for (int z = 0; z < SIZE; z++) {
            for (int x = 0; x < SIZE; x++) {
                BlockPos supportPos = origin.offset(x, -1, z);
                if (!level.hasChunkAt(supportPos)
                        || !level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.UP)) {
                    return Component.translatable("message.starrail_sim.arena.build_support");
                }
            }
        }
        AABB area = new AABB(origin.getX(), origin.getY(), origin.getZ(),
                origin.getX() + SIZE, origin.getY() + HEIGHT, origin.getZ() + SIZE);
        if (!level.getEntities((Entity) null, area,
                entity -> entity.isAlive() && entity != player).isEmpty()) {
            return Component.translatable("message.starrail_sim.arena.build_entities");
        }
        return null;
    }

    private static boolean isReplaceableSite(BlockState state) {
        return state.isAir() || (state.canBeReplaced() && state.getFluidState().isEmpty()
                && !state.hasBlockEntity());
    }

    /** Creates the same local block map as the HTML planner, with the interactive device last. */
    static LinkedHashMap<BlockPos, BlockState> createPlan(BlockPos origin) {
        LinkedHashMap<BlockPos, BlockState> plan = new LinkedHashMap<>();
        for (int z = 2; z <= 22; z++) {
            for (int x = 2; x <= 22; x++) {
                double radius = Math.hypot(x - CENTER, z - CENTER);
                boolean ring = (radius >= 7.8D && radius <= 9.15D)
                        || (radius >= 4.05D && radius <= 5.15D);
                put(plan, origin, x, 0, z, ring ? Blocks.DARK_PRISMARINE : Blocks.POLISHED_ANDESITE);
            }
        }
        for (int z = 0; z < SIZE; z++) {
            for (int x = 0; x < SIZE; x++) {
                boolean perimeter = x < 2 || x > 22 || z < 2 || z > 22;
                if (!perimeter) continue;
                boolean gateApproach = z < 2 && x >= 10 && x <= 14;
                boolean corner = (x <= 2 || x >= 22) && (z <= 2 || z >= 22);
                put(plan, origin, x, 0, z, gateApproach ? Blocks.POLISHED_ANDESITE
                        : corner ? Blocks.DEEPSLATE_BRICKS : Blocks.STONE_BRICKS);
            }
        }
        for (int[] pad : new int[][]{{12, 4}, {20, 12}, {12, 20}, {4, 12}}) {
            put(plan, origin, pad[0], 0, pad[1], Blocks.SEA_LANTERN);
        }
        for (int z = 11; z <= 13; z++) {
            for (int x = 11; x <= 13; x++) put(plan, origin, x, 1, z, Blocks.POLISHED_ANDESITE);
        }
        for (int[] lamp : new int[][]{{12, 11}, {13, 12}, {12, 13}, {11, 12}}) {
            put(plan, origin, lamp[0], 1, lamp[1], Blocks.SEA_LANTERN);
        }

        for (int y = 1; y <= 7; y++) {
            for (int z = 0; z < SIZE; z++) {
                for (int x = 0; x < SIZE; x++) {
                    if (!(x < 2 || x > 22 || z < 2 || z > 22)) continue;
                    if (z < 2 && x >= 10 && x <= 14 && y <= 4) continue;
                    boolean tower = (x <= 2 || x >= 22) && (z <= 2 || z >= 22);
                    put(plan, origin, x, y, z, tower ? Blocks.DEEPSLATE_BRICKS : Blocks.STONE_BRICKS);
                }
            }
        }
        for (int y = 8; y <= 9; y++) {
            for (int z = 0; z < SIZE; z++) {
                for (int x = 0; x < SIZE; x++) {
                    if ((x <= 2 || x >= 22) && (z <= 2 || z >= 22)) {
                        put(plan, origin, x, y, z, Blocks.DEEPSLATE_BRICKS);
                    }
                }
            }
        }
        BlockState gate = Blocks.DARK_OAK_FENCE_GATE.defaultBlockState()
                .setValue(FenceGateBlock.FACING, Direction.SOUTH);
        for (int x = 10; x <= 14; x++) put(plan, origin, x, 1, 0, gate);
        BlockState endRod = Blocks.END_ROD.defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.DOWN);
        for (int[] sconce : new int[][]{{7, 1}, {17, 1}, {1, 12}, {23, 12}}) {
            put(plan, origin, sconce[0], 3, sconce[1], endRod);
            put(plan, origin, sconce[0], 4, sconce[1], Blocks.SEA_LANTERN);
        }
        put(plan, origin, CENTER, 2, CENTER, StarRailSimMod.RELIC_TRIAL_DEVICE.get());
        return plan;
    }

    private static void put(Map<BlockPos, BlockState> plan, BlockPos origin,
                            int x, int y, int z, Block block) {
        put(plan, origin, x, y, z, block.defaultBlockState());
    }

    private static void put(Map<BlockPos, BlockState> plan, BlockPos origin,
                            int x, int y, int z, BlockState state) {
        plan.put(origin.offset(x, y, z), state);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;
        MinecraftServer server = level.getServer();
        BuildJob job = ACTIVE_JOBS.get(server);
        if (job != null && job.level == level && job.tick()) ACTIVE_JOBS.remove(server, job);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        BuildJob job = ACTIVE_JOBS.remove(event.getServer());
        if (job != null) job.rollback();
    }

    private static final class BuildJob {
        private final CommandSourceStack source;
        private final ServerLevel level;
        private final BlockPos origin;
        private final List<Map.Entry<BlockPos, BlockState>> placements;
        private final Map<BlockPos, BlockState> originals = new LinkedHashMap<>();
        private final List<ChunkPos> temporaryForcedChunks = new ArrayList<>();
        private final java.util.UUID builderId;
        private int placed;
        private int nextProgress;
        private boolean forcedChunksReleased;

        private BuildJob(CommandSourceStack source, ServerLevel level, BlockPos origin,
                         LinkedHashMap<BlockPos, BlockState> plan) {
            this.source = source;
            this.level = level;
            this.origin = origin;
            this.placements = new ArrayList<>(plan.entrySet());
            this.builderId = source.getPlayer().getUUID();
            for (Map.Entry<BlockPos, BlockState> placement : placements) {
                originals.put(placement.getKey(), level.getBlockState(placement.getKey()));
            }
            forceRequiredChunks();
        }

        /** Returns true after completion or a safely rolled-back failure. */
        private boolean tick() {
            AABB area = new AABB(origin.getX(), origin.getY(), origin.getZ(),
                    origin.getX() + SIZE, origin.getY() + HEIGHT, origin.getZ() + SIZE);
            if (!level.getEntities((Entity) null, area,
                    entity -> entity.isAlive() && !entity.getUUID().equals(builderId)).isEmpty()) {
                notifyFailure(Component.translatable("message.starrail_sim.arena.build_entities"));
                rollback();
                return true;
            }
            int end = Math.min(placed + BLOCKS_PER_TICK, placements.size());
            for (int index = placed; index < end; index++) {
                Map.Entry<BlockPos, BlockState> placement = placements.get(index);
                BlockPos pos = placement.getKey();
                if (!level.hasChunkAt(pos)
                        || !level.getBlockState(pos).equals(originals.get(pos))) {
                    notifyFailure(Component.translatable("message.starrail_sim.arena.build_interrupted"));
                    rollback();
                    return true;
                }
                if (!level.setBlock(pos, placement.getValue(), Block.UPDATE_ALL)) {
                    notifyFailure(Component.translatable("message.starrail_sim.arena.build_interrupted"));
                    rollback();
                    return true;
                }
                placed = index + 1;
            }
            int percent = placed * 100 / placements.size();
            if (nextProgress < PROGRESS_MARKS.length && percent >= PROGRESS_MARKS[nextProgress]) {
                source.sendSuccess(() -> Component.translatable(
                        "message.starrail_sim.arena.build_progress", percent), false);
                nextProgress++;
            }
            if (placed < placements.size()) return false;
            releaseTemporaryChunks();
            StarRailRuinSpacingData.get(level).recordArena(
                    new BlockPos(origin.getX() + CENTER, origin.getY(), origin.getZ() + CENTER));
            source.sendSuccess(() -> Component.translatable(
                    "message.starrail_sim.arena.build_complete", placements.size(),
                    origin.getX(), origin.getY(), origin.getZ(),
                    level.dimension().location()), false);
            source.sendSuccess(this::materialSummary, false);
            return true;
        }

        private Component materialSummary() {
            LinkedHashMap<Block, Integer> counts = new LinkedHashMap<>();
            for (Map.Entry<BlockPos, BlockState> placement : placements) {
                counts.merge(placement.getValue().getBlock(), 1, Integer::sum);
            }
            net.minecraft.network.chat.MutableComponent result = Component.translatable(
                    "message.starrail_sim.arena.build_materials");
            boolean first = true;
            for (Map.Entry<Block, Integer> count : counts.entrySet()) {
                if (!first) result.append(Component.literal(" · "));
                result.append(count.getKey().getName())
                        .append(Component.literal(" ×" + count.getValue()));
                first = false;
            }
            return result;
        }

        private void notifyFailure(Component message) {
            source.sendFailure(message);
        }

        private void rollback() {
            for (int index = placed - 1; index >= 0; index--) {
                Map.Entry<BlockPos, BlockState> placement = placements.get(index);
                BlockPos pos = placement.getKey();
                if (level.hasChunkAt(pos)
                        && level.getBlockState(pos).getBlock() == placement.getValue().getBlock()) {
                    level.setBlock(pos, originals.get(pos), Block.UPDATE_ALL);
                }
            }
            placed = 0;
            releaseTemporaryChunks();
        }

        private void forceRequiredChunks() {
            int minChunkX = origin.getX() >> 4;
            int maxChunkX = (origin.getX() + SIZE - 1) >> 4;
            int minChunkZ = origin.getZ() >> 4;
            int maxChunkZ = (origin.getZ() + SIZE - 1) >> 4;
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    long key = ChunkPos.asLong(chunkX, chunkZ);
                    if (!level.getForcedChunks().contains(key)) {
                        level.setChunkForced(chunkX, chunkZ, true);
                        temporaryForcedChunks.add(new ChunkPos(chunkX, chunkZ));
                    }
                }
            }
        }

        private void releaseTemporaryChunks() {
            if (forcedChunksReleased) return;
            forcedChunksReleased = true;
            for (ChunkPos chunk : temporaryForcedChunks) {
                level.setChunkForced(chunk.x, chunk.z, false);
            }
            temporaryForcedChunks.clear();
        }
    }
}
