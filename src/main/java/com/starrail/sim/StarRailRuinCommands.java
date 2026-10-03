package com.starrail.sim;

/**
 * 模组代码说明：遗迹调试命令：读取压缩蓝图、检查世界边界与区块加载状态，再分 tick 安全放置结构和宝箱战利品表。
 */

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

/** Permission-gated, incremental commands for inspecting ruin blueprints in a test world. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarRailRuinCommands {
    private static final int BLOCKS_PER_TICK = 384;
    private static final int[] PROGRESS_MARKS = {25, 50, 75, 100};
    private static final Map<MinecraftServer, PlacementJob> ACTIVE_JOBS = new HashMap<>();
    private static final Map<String, Blueprint> CACHED_BLUEPRINTS = new ConcurrentHashMap<>();

    private StarRailRuinCommands() {
    }

    @SubscribeEvent
    // 注册普通遗迹与九种命途变体的放置指令，以及取消进行中任务的指令。
    public static void register(RegisterCommandsEvent event) {
        var locate = Commands.literal("locate")
                .then(Commands.literal("ordinary")
                        .executes(context -> StarRailRuinLocateService.start(context.getSource()))
                        .then(Commands.literal("cancel")
                                .executes(context -> StarRailRuinLocateService.cancel(
                                        context.getSource()))));
        var ruin = Commands.literal("ruin")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("place")
                        .then(placeCommand("ordinary"))
                        .then(placeCommand("destruction"))
                        .then(placeCommand("hunt"))
                        .then(placeCommand("erudition"))
                        .then(placeCommand("harmony"))
                        .then(placeCommand("nihility"))
                        .then(placeCommand("preservation"))
                        .then(placeCommand("abundance"))
                        .then(placeCommand("remembrance"))
                        .then(placeCommand("elation")))
                .then(Commands.literal("cancel").executes(StarRailRuinCommands::cancel))
                .then(Commands.literal("guard_status")
                        .executes(context -> StarRailRuinGuardService.status(context.getSource())))
                .then(locate);
        event.getDispatcher().register(Commands.literal("starrail").then(ruin));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> placeCommand(String variantId) {
        return Commands.literal(variantId)
                .then(Commands.argument("origin", BlockPosArgument.blockPos())
                        .executes(context -> start(context, variantId, false))
                        .then(Commands.literal("overwrite")
                                .executes(context -> start(context, variantId, true))));
    }

    // 验证放置任务、蓝图、世界高度和区块状态，通过后创建分帧放置任务。
    private static int start(CommandContext<CommandSourceStack> context, String variantId,
                             boolean overwrite) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        MinecraftServer server = source.getServer();
        if (ACTIVE_JOBS.containsKey(server)) {
            source.sendFailure(Component.literal("已有遗迹放置任务正在进行；完成后再试，或执行 /starrail ruin cancel。"));
            return 0;
        }

        final Blueprint blueprint;
        try {
            blueprint = getBlueprint(variantId);
        } catch (IOException | RuntimeException exception) {
            source.sendFailure(Component.literal("读取遗迹蓝图失败，请确认模组资源文件完整。"));
            return 0;
        }

        BlockPos origin = BlockPosArgument.getBlockPos(context, "origin");
        String boundsError = checkBounds(level, origin, blueprint.bounds());
        if (boundsError != null) {
            source.sendFailure(Component.literal(boundsError));
            return 0;
        }
        if (!StarRailRuinSpacingData.get(level).hasClearance(
                level, origin.getX(), origin.getZ(), overwrite)) {
            source.sendFailure(Component.translatable(
                    "message.starrail_sim.path_seek_no_spaced_site"));
            return 0;
        }
        String chunksError = checkChunksLoaded(level, origin, blueprint.bounds());
        if (chunksError != null) {
            source.sendFailure(Component.literal(chunksError));
            return 0;
        }

        PlacementJob job = new PlacementJob(source, level, origin, blueprint, overwrite,
                false, false, null, null);
        ACTIVE_JOBS.put(server, job);
        source.sendSuccess(() -> Component.literal("已开始检查" + displayName(blueprint) + "放置区域："
                + blueprint.placements().size() + " 个方块；锚点 " + origin.getX() + " "
                + origin.getY() + " " + origin.getZ() + "。每 tick 分批处理。"
                + (overwrite ? " 将覆盖模型占用位置的现有方块。" : " 若占位会覆盖实体方块则停止，不会留下半座遗迹。")), false);
        return 1;
    }

    /**
     * 为命途寻迹队列创建一座主题遗迹；目标区块会临时加载，完成后释放加载票据。
     */
    public static boolean placeSoughtRuin(ServerPlayer player, StarRailPath path, int x, int z,
                                          Consumer<BlockPos> onComplete, Runnable onFailure) {
        MinecraftServer server = player.getServer();
        if (server == null || ACTIVE_JOBS.containsKey(server)) {
            player.sendSystemMessage(Component.translatable(
                    "message.starrail_sim.path_seek_busy"));
            return false;
        }
        if (!(player.level() instanceof ServerLevel level)
                || level.dimension() != net.minecraft.world.level.Level.OVERWORLD) {
            player.sendSystemMessage(Component.translatable(
                    "message.starrail_sim.path_seek_overworld_only"));
            return false;
        }

        final Blueprint blueprint;
        try {
            blueprint = getBlueprint(path.getId());
        } catch (IOException | RuntimeException exception) {
            player.sendSystemMessage(Component.translatable(
                    "message.starrail_sim.path_seek_failed"));
            return false;
        }

        // 先用安全的临时高度校验水平世界边界；正式浮空高度等目标区块载入后计算。
        BlockPos initialOrigin = new BlockPos(x, 200, z);
        String boundsError = checkBounds(level, initialOrigin, blueprint.bounds());
        if (boundsError != null) {
            player.sendSystemMessage(Component.translatable(
                    "message.starrail_sim.path_seek_failed"));
            return false;
        }
        if (!StarRailRuinSpacingData.get(level).hasClearance(level, x, z)) {
            player.sendSystemMessage(Component.translatable(
                    "message.starrail_sim.path_seek_no_spaced_site"));
            return false;
        }

        PlacementJob job = new PlacementJob(player.createCommandSourceStack(), level,
                initialOrigin, blueprint, false, true, true, origin -> {
                    StarRailRuinSpacingData.get(level).recordRuin(origin);
                    onComplete.accept(origin);
                }, onFailure);
        job.forceRequiredChunks();
        ACTIVE_JOBS.put(server, job);
        player.sendSystemMessage(Component.translatable(
                "message.starrail_sim.path_seek_started", path.getDisplayName(), x, z));
        return true;
    }

    private static String displayName(Blueprint blueprint) {
        return "普通遗迹".equals(blueprint.name())
                ? blueprint.name() : blueprint.name() + "变体";
    }

    private static int cancel(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();
        PlacementJob job = ACTIVE_JOBS.remove(server);
        if (job == null) {
            context.getSource().sendFailure(Component.literal("当前没有遗迹放置任务。"));
            return 0;
        }
        job.cancel();
        job.releaseTemporaryChunks();
        context.getSource().sendSuccess(() -> Component.literal(job.placedCount == 0
                ? "已取消遗迹放置任务。"
                : "已停止遗迹放置；已放置的 " + job.placedCount
                        + " 个方块保留在世界中，需要手动清理或重新放置。"), false);
        return 1;
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        MinecraftServer server = level.getServer();
        PlacementJob job = ACTIVE_JOBS.get(server);
        if (job == null || job.level != level) {
            return;
        }
        if (job.tick()) {
            ACTIVE_JOBS.remove(server, job);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PlacementJob job = ACTIVE_JOBS.remove(event.getServer());
        if (job != null) {
            job.releaseTemporaryChunks();
        }
    }

    // 按变体读取并缓存压缩蓝图，避免重复解压和解析大型数据。
    static Blueprint getBlueprint(String variantId) throws IOException {
        // 新放置的母体和命途变体统一使用守卫室蓝图；旧结构片仍按 NBT 标记读取。
        return readBlueprint(variantId, true);
    }

    /** 旧结构片显式读取旧蓝图，避免更新后把半生成的旧遗迹改成守卫室。 */
    static Blueprint readBlueprint(String variantId, boolean guarded) throws IOException {
        String resourceId = variantId + (guarded ? "_guard" : "");
        Blueprint result = CACHED_BLUEPRINTS.get(resourceId);
        if (result != null) {
            return result;
        }
        synchronized (CACHED_BLUEPRINTS) {
            result = CACHED_BLUEPRINTS.get(resourceId);
            if (result != null) {
                return result;
            }
            try (InputStream raw = StarRailRuinCommands.class.getResourceAsStream(
                    "/data/starrail_sim/ruins/" + resourceId + ".json.gz")) {
                if (raw == null) {
                    throw new IOException("Missing data/starrail_sim/ruins/" + resourceId + ".json.gz");
                }
                try (InputStreamReader reader = new InputStreamReader(
                        new GZIPInputStream(raw), StandardCharsets.UTF_8)) {
                    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                    if (root.get("version").getAsInt() != 1) {
                        throw new IOException("Unsupported ruin blueprint version");
                    }
                    JsonObject rawBounds = root.getAsJsonObject("bounds");
                    Bounds bounds = new Bounds(rawBounds.get("minX").getAsInt(),
                            rawBounds.get("maxX").getAsInt(), rawBounds.get("minY").getAsInt(),
                            rawBounds.get("maxY").getAsInt(), rawBounds.get("minZ").getAsInt(),
                            rawBounds.get("maxZ").getAsInt());
                    List<BlockState> states = readPalette(root.getAsJsonArray("palette"));
                    List<Placement> placements = readPlacements(root.getAsJsonArray("blocks"),
                            states.size());
                    result = new Blueprint(variantId, root.get("name").getAsString(), bounds, states,
                            placements, guarded);
                    CACHED_BLUEPRINTS.put(resourceId, result);
                    return result;
                }
            }
        }
    }

    // 解析蓝图方块调色板，并恢复朝向、半砖和叶子等方块状态。
    private static List<BlockState> readPalette(JsonArray entries) throws IOException {
        List<BlockState> states = new ArrayList<>(entries.size());
        for (JsonElement element : entries) {
            JsonObject entry = element.getAsJsonObject();
            ResourceLocation id = ResourceLocation.tryParse(entry.get("block").getAsString());
            if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
                throw new IOException("Unknown block in ruin palette: " + entry.get("block"));
            }
            Block block = BuiltInRegistries.BLOCK.get(id);
            BlockState state = block.defaultBlockState();
            if (block == Blocks.LADDER) {
                state = state.setValue(LadderBlock.FACING, Direction.WEST);
            } else if (block instanceof SlabBlock) {
                state = state.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
            } else if (block == Blocks.CHAIN) {
                state = state.setValue(BlockStateProperties.AXIS, Direction.Axis.Y);
            } else if (block == Blocks.END_ROD) {
                state = state.setValue(BlockStateProperties.FACING, Direction.UP);
            } else if (block == Blocks.CHEST) {
                state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
            } else if (block == Blocks.IRON_BARS) {
                // 门洞沿 X 轴排列，显式连接横向栏杆，避免已知形状放置留下独立细杆。
                state = state.setValue(BlockStateProperties.EAST, true)
                        .setValue(BlockStateProperties.WEST, true);
            } else if (block instanceof LeavesBlock) {
                state = state.setValue(LeavesBlock.PERSISTENT, true);
            }
            states.add(state);
        }
        return states;
    }

    // 读取每个方块的局部坐标与调色板索引，并拒绝越界索引。
    private static List<Placement> readPlacements(JsonArray entries, int paletteSize)
            throws IOException {
        List<Placement> placements = new ArrayList<>(entries.size());
        for (JsonElement element : entries) {
            JsonArray values = element.getAsJsonArray();
            if (values.size() != 4) {
                throw new IOException("Malformed ordinary ruin block entry");
            }
            int state = values.get(3).getAsInt();
            if (state < 0 || state >= paletteSize) {
                throw new IOException("Ruin block entry references a missing palette state");
            }
            placements.add(new Placement(values.get(0).getAsInt(), values.get(1).getAsInt(),
                    values.get(2).getAsInt(), state));
        }
        return placements;
    }

    // 检查结构是否超出维度建造高度或世界边界。
    private static String checkBounds(ServerLevel level, BlockPos origin, Bounds bounds) {
        int minY = origin.getY() + bounds.minY();
        int maxY = origin.getY() + bounds.maxY();
        if (minY < level.getMinBuildHeight() || maxY >= level.getMaxBuildHeight()) {
            return "遗迹高度超出当前维度可建造范围：Y " + minY + "…" + maxY + "。请调整锚点 Y。";
        }
        int[][] corners = {{bounds.minX(), bounds.minZ()}, {bounds.minX(), bounds.maxZ()},
                {bounds.maxX(), bounds.minZ()}, {bounds.maxX(), bounds.maxZ()}};
        for (int[] corner : corners) {
            if (!level.getWorldBorder().isWithinBounds(origin.offset(corner[0], 0, corner[1]))) {
                return "遗迹范围超出世界边界，请换一个锚点。";
            }
        }
        return null;
    }

    // 检查结构覆盖的每个区块是否已加载，避免放置到未加载区域。
    private static String checkChunksLoaded(ServerLevel level, BlockPos origin, Bounds bounds) {
        int minChunkX = (origin.getX() + bounds.minX()) >> 4;
        int maxChunkX = (origin.getX() + bounds.maxX()) >> 4;
        int minChunkZ = (origin.getZ() + bounds.minZ()) >> 4;
        int maxChunkZ = (origin.getZ() + bounds.maxZ()) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                BlockPos sample = new BlockPos((chunkX << 4) + 8, origin.getY(),
                        (chunkZ << 4) + 8);
                if (!level.hasChunkAt(sample)) {
                    return "遗迹覆盖的区块尚未全部加载。请站在锚点附近等待区块加载，再运行命令。";
                }
            }
        }
        return null;
    }

    record Bounds(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
    }

    record Placement(int x, int y, int z, int paletteIndex) {
    }

    record Blueprint(String variantId, String name, Bounds bounds, List<BlockState> palette,
                              List<Placement> placements, boolean guarded) {
    }

    private static final class PlacementJob {
        private final CommandSourceStack source;
        private final ServerLevel level;
        private BlockPos origin;
        private final Blueprint blueprint;
        private final boolean overwrite;
        private final boolean forceChunks;
        private final boolean adaptFloatingHeight;
        private final Consumer<BlockPos> onComplete;
        private final Runnable onFailure;
        private final List<ForcedChunk> temporaryChunks = new ArrayList<>();
        private int validateIndex;
        private int placedCount;
        private boolean placing;
        private int nextProgressMark;
        private boolean waitingForChunks;
        private boolean floatingHeightAdjusted;
        private boolean temporaryChunksReleased;

        private PlacementJob(CommandSourceStack source, ServerLevel level, BlockPos origin,
                             Blueprint blueprint, boolean overwrite, boolean forceChunks,
                             boolean adaptFloatingHeight, Consumer<BlockPos> onComplete,
                             Runnable onFailure) {
            this.source = source;
            this.level = level;
            this.origin = origin.immutable();
            this.blueprint = blueprint;
            this.overwrite = overwrite;
            this.forceChunks = forceChunks;
            this.adaptFloatingHeight = adaptFloatingHeight;
            this.onComplete = onComplete;
            this.onFailure = onFailure;
            this.nextProgressMark = 0;
        }

        private void forceRequiredChunks() {
            if (!forceChunks) {
                return;
            }
            Bounds bounds = blueprint.bounds();
            int minChunkX = (origin.getX() + bounds.minX()) >> 4;
            int maxChunkX = (origin.getX() + bounds.maxX()) >> 4;
            int minChunkZ = (origin.getZ() + bounds.minZ()) >> 4;
            int maxChunkZ = (origin.getZ() + bounds.maxZ()) >> 4;
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    long key = ChunkPos.asLong(chunkX, chunkZ);
                    if (!level.getForcedChunks().contains(key)) {
                        level.setChunkForced(chunkX, chunkZ, true);
                        temporaryChunks.add(new ForcedChunk(chunkX, chunkZ));
                    }
                }
            }
        }

        private void releaseTemporaryChunks() {
            if (temporaryChunksReleased) {
                return;
            }
            temporaryChunksReleased = true;
            for (ForcedChunk chunk : temporaryChunks) {
                level.setChunkForced(chunk.x(), chunk.z(), false);
            }
            temporaryChunks.clear();
        }

        /** Returns true when this job has finished or was stopped. */
        // 每个服务器 tick 处理一小批方块；先完整检查覆盖位置，再开始实际放置。
        private boolean tick() {
            if (!allChunksLoaded()) {
                if (!waitingForChunks) {
                    notify("遗迹放置已暂停：有区块卸载了。回到锚点附近加载完整范围后会继续。", true);
                    waitingForChunks = true;
                }
                return false;
            }
            waitingForChunks = false;

            if (adaptFloatingHeight && !floatingHeightAdjusted
                    && !adjustFloatingHeight()) {
                return finish(false);
            }

            List<Placement> placements = blueprint.placements();
            int cursor = placing ? placedCount : validateIndex;
            // 限制单个 tick 的工作量，防止大型遗迹一次性放置卡住服务器。
            int end = Math.min(cursor + BLOCKS_PER_TICK, placements.size());
            for (int i = cursor; i < end; i++) {
                Placement placement = placements.get(i);
                BlockPos worldPos = origin.offset(placement.x(), placement.y(), placement.z());
                if (!placing) {
                    BlockState existing = level.getBlockState(worldPos);
                    if (!overwrite && !existing.isAir()) {
                        notify("放置检查失败：" + worldPos.getX() + " " + worldPos.getY()
                                + " " + worldPos.getZ() + " 已有方块（"
                                + existing.getBlock().getName().getString()
                                + "）。没有放置任何遗迹方块；换空地或在命令末尾加 overwrite。", true);
                        return finish(false);
                    }
                } else {
                    BlockState state = blueprint.palette().get(placement.paletteIndex());
                    level.setBlock(worldPos, state,
                            Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                    // 宝箱方块放置后绑定对应变体的战利品表，首次打开时再随机生成内容。
                    if (state.is(Blocks.CHEST)
                            && level.getBlockEntity(worldPos) instanceof ChestBlockEntity chest) {
                        if (blueprint.guarded()) {
                            StarRailRuinGuardService.markChest(chest, origin, blueprint.variantId());
                        } else chest.setLootTable(new ResourceLocation(StarRailSimMod.MOD_ID,
                                "chests/ruin_" + blueprint.variantId()),
                                level.getRandom().nextLong());
                        chest.setChanged();
                    }
                }
            }

            if (!placing) {
                validateIndex = end;
                if (validateIndex >= placements.size()) {
                    placing = true;
                    notify("放置区域检查通过，开始分批搭建。", false);
                }
                return false;
            }

            placedCount = end;
            int percent = placedCount * 100 / placements.size();
            if (nextProgressMark < PROGRESS_MARKS.length
                    && percent >= PROGRESS_MARKS[nextProgressMark]) {
                notify(displayName(blueprint) + "放置进度 " + percent + "%（" + placedCount + "/"
                        + placements.size() + "）", false);
                nextProgressMark++;
            }
            if (placedCount >= placements.size()) {
                if (blueprint.guarded()) StarRailRuinGuardService.register(
                        level, origin, blueprint.variantId());
                notify(displayName(blueprint) + "浮空遗迹已放置完成。锚点 " + origin.getX() + " "
                        + origin.getY() + " " + origin.getZ()
                        + (blueprint.guarded() ? "；击败主殿监守者后开放宝箱。"
                        : "；对应战利品已装入宝箱，首次打开时生成。"), false);
                return finish(true);
            }
            return false;
        }

        /** 等区块完整生成后采样地表，让整座遗迹悬在地形上方且不穿山。 */
        private boolean adjustFloatingHeight() {
            // 区块强制加载期间可能刚生成了附近遗迹，因此开工前再校验一次。
            if (!StarRailRuinSpacingData.get(level).hasClearance(
                    level, origin.getX(), origin.getZ())) {
                notify("附近出现了间距不足的遗迹；本次寻迹取消，请移动到更远区域后重试。", true);
                return false;
            }
            Bounds bounds = blueprint.bounds();
            int highestSurface = level.getMinBuildHeight();
            for (int x = bounds.minX(); x <= bounds.maxX(); x += 4) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z += 4) {
                    highestSurface = Math.max(highestSurface, level.getHeight(
                            Heightmap.Types.WORLD_SURFACE,
                            origin.getX() + x, origin.getZ() + z));
                }
            }
            int platformY = Math.max(151, highestSurface + 24);
            origin = new BlockPos(origin.getX(), platformY - bounds.minY(), origin.getZ());
            String boundsError = checkBounds(level, origin, bounds);
            if (boundsError != null) {
                notify("寻迹点上方没有足够的浮空空间，请重新选择命途寻迹。", true);
                return false;
            }
            floatingHeightAdjusted = true;
            return true;
        }

        private boolean finish(boolean success) {
            if (success) {
                StarRailRuinSpacingData.get(level).recordRuin(origin);
            }
            if (success && onComplete != null) {
                onComplete.accept(origin.immutable());
            } else if (!success && onFailure != null) {
                onFailure.run();
            }
            releaseTemporaryChunks();
            return true;
        }

        private void cancel() {
            if (onFailure != null) {
                onFailure.run();
            }
        }

        private boolean allChunksLoaded() {
            return checkChunksLoaded(level, origin, blueprint.bounds()) == null;
        }

        private void notify(String message, boolean failure) {
            Component component = Component.literal(message);
            if (failure) {
                source.sendFailure(component);
            } else {
                source.sendSuccess(() -> component, false);
            }
        }
    }

    private record ForcedChunk(int x, int z) {
    }
}
