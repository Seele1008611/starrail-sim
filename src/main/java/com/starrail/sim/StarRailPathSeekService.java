package com.starrail.sim;

/** 命途寻迹流程：验证玩家状态、分配目标点，并在遗迹建成后保存线索坐标。 */

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/** 服务端权威的命途遗迹寻迹入口。 */
public final class StarRailPathSeekService {
    private static final int MIN_SEEK_DISTANCE = 384;
    private static final int MAX_SEEK_DISTANCE = 768;

    private StarRailPathSeekService() {
    }

    /** UI 点击命途只会开始寻迹与生成对应遗迹，不会开启试炼。 */
    public static void seek(ServerPlayer player, StarRailPath path) {
        IStarRailPathData data = player.getCapability(StarRailPathCapability.PATH_DATA)
                .resolve().orElse(null);
        if (data == null) {
            return;
        }
        if (!data.isPathUnlocked()) {
            StarRailPathMessages.sendQueued(player, path,
                    Component.translatable("message.starrail_sim.path_locked"));
            return;
        }
        if (data.getCurrentPath().isRealPath()) {
            StarRailPathMessages.sendQueued(player, data.getCurrentPath(),
                    Component.translatable("message.starrail_sim.path_already_chosen"));
            return;
        }
        if (data.getTrialPath().isRealPath()) {
            StarRailPathMessages.sendQueued(player, data.getTrialPath(),
                    Component.translatable("message.starrail_sim.trial_already_active"));
            return;
        }
        if (!StarRailPathRules.isImplemented(path)) {
            StarRailPathMessages.sendQueued(player, path, Component.translatable(
                    "message.starrail_sim.path_unavailable", path.getDisplayName()));
            return;
        }
        if (player.level().dimension() != Level.OVERWORLD) {
            StarRailPathMessages.sendQueued(player, path, Component.translatable(
                    "message.starrail_sim.path_seek_overworld_only"));
            return;
        }

        // 同一命途已有未使用线索时，重新打开 UI 会再次显示原坐标，不会复制遗迹。
        if (data.getSoughtPath() == path) {
            sendTarget(player, path, data.getSoughtX(), data.getSoughtY(), data.getSoughtZ());
            return;
        }

        // 在玩家附近选择一个新目标；按区块对齐，便于稳定加载整座大型遗迹。
        if (data.getSoughtPath().isRealPath()) {
            StarRailPathMessages.sendQueued(player, path, Component.translatable(
                    "message.starrail_sim.path_seek_pending", data.getSoughtPath().getDisplayName()));
            return;
        }

        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        StarRailRuinSpacingData spacing = StarRailRuinSpacingData.get(level);
        int targetX = 0;
        int targetZ = 0;
        boolean foundSpacedSite = false;
        for (int attempt = 0; attempt < 64; attempt++) {
            double angle = player.getRandom().nextDouble() * Math.PI * 2.0;
            int distance = MIN_SEEK_DISTANCE + player.getRandom().nextInt(
                    MAX_SEEK_DISTANCE - MIN_SEEK_DISTANCE + 1);
            int candidateX = alignToChunk((int) Math.floor(
                    player.getX() + Math.cos(angle) * distance));
            int candidateZ = alignToChunk((int) Math.floor(
                    player.getZ() + Math.sin(angle) * distance));
            if (spacing.hasClearance(level, candidateX, candidateZ)) {
                targetX = candidateX;
                targetZ = candidateZ;
                foundSpacedSite = true;
                break;
            }
        }
        if (!foundSpacedSite) {
            StarRailPathMessages.sendQueued(player, path, Component.translatable(
                    "message.starrail_sim.path_seek_no_spaced_site"));
            return;
        }

        if (!StarRailRuinCommands.placeSoughtRuin(player, path, targetX, targetZ,
                origin -> onRuinPlaced(player, path, origin),
                () -> onRuinPlacementFailed(player, path))) {
            return;
        }

        // 新寻迹一旦排入队列，旧命途线索立即失效，避免拿旧凭证绕过当前选择。
        data.clearSoughtRuin();
        StarRailPathService.sync(player);
    }

    private static int alignToChunk(int coordinate) {
        return Math.floorDiv(coordinate, 16) * 16 + 8;
    }

    private static void onRuinPlaced(ServerPlayer originalPlayer, StarRailPath path,
                                     net.minecraft.core.BlockPos origin) {
        ServerPlayer player = originalPlayer.getServer().getPlayerList()
                .getPlayer(originalPlayer.getUUID());
        if (player == null) {
            return;
        }
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            if (!data.getCurrentPath().isRealPath() && !data.getTrialPath().isRealPath()) {
                data.setSoughtRuin(path, origin.getX(), origin.getY(), origin.getZ());
                sendTarget(player, path, origin.getX(), origin.getY(), origin.getZ());
                StarRailPathService.sync(player);
            }
        });
    }

    private static void onRuinPlacementFailed(ServerPlayer originalPlayer, StarRailPath path) {
        if (originalPlayer.getServer() == null) {
            return;
        }
        ServerPlayer player = originalPlayer.getServer().getPlayerList()
                .getPlayer(originalPlayer.getUUID());
        if (player != null) {
            player.sendSystemMessage(Component.translatable(
                    "message.starrail_sim.path_seek_retry", path.getDisplayName()));
        }
    }

    private static void sendTarget(ServerPlayer player, StarRailPath path,
                                   int x, int y, int z) {
        player.sendSystemMessage(Component.translatable(
                "message.starrail_sim.path_seek_target", path.getDisplayName(), x, y, z));
    }
}
