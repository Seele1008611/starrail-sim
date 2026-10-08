package com.starrail.sim;

/**
 * 模组代码说明：命途服务层，统一处理试炼启动、完成确认、命途回退以及向客户端同步玩家命途状态。
 */

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Server-authoritative operations shared by commands and the path screen. */
public final class StarRailPathService {
    private StarRailPathService() {
    }

    // 校验解锁、当前命途和试炼状态后，初始化新试炼；只有返回 true 才表示启动成功。
    public static boolean startTrialFromToken(ServerPlayer player, ItemStack token) {
        // 试炼只能由注册的命途凭证启动，界面按钮本身不授予试炼资格。
        if (token == null || !token.is(StarRailSimMod.PATH_TRIAL_TOKEN.get())) {
            return false;
        }
        StarRailPath path = PathTrialTokenItem.getPath(token);
        IStarRailPathData data = player.getCapability(StarRailPathCapability.PATH_DATA)
                .resolve().orElse(null);
        if (data == null || !data.isPathUnlocked()) {
            return false;
        }
        if (data.getCurrentPath().isRealPath()) {
            StarRailPathMessages.sendQueued(player, data.getCurrentPath(), Component.translatable(
                    "message.starrail_sim.path_already_chosen"));
            return false;
        }
        if (data.getTrialPath().isRealPath()) {
            StarRailPathMessages.sendQueued(player, data.getTrialPath(), Component.translatable(
                    "message.starrail_sim.trial_already_active"));
            return false;
        }
        if (!StarRailPathRules.isImplemented(path)) {
            StarRailPathMessages.sendQueued(player, path, Component.translatable(
                    "message.starrail_sim.path_unavailable",
                    path.getDisplayName()));
            return false;
        }
        if (data.getSoughtPath() != path) {
            StarRailPathMessages.sendQueued(player, path, Component.translatable(
                    "message.starrail_sim.path_seek_wrong_token",
                    path.getDisplayName()));
            return false;
        }

        data.setTrialPath(path);
        data.setTrialRank(StarRailPathRank.UNALIGNED);
        data.setTrialStartTick(player.level().getGameTime());
        data.setObjective1Progress(0);
        data.setObjective2Progress(0);
        data.setLastHuntKillTick(-1L);
        data.setEruditionHitTick(-1L);
        data.setEruditionHitCount(0);
        data.setLastEruditionMultiHitTick(-1L);
        data.clearSoughtRuin();
        StarRailPathMessages.sendQueued(player, path,
                Component.translatable(startMessageKey(path)));
        sync(player);
        return true;
    }

    // 首个试炼完成后，将试炼命途设为当前命途并清理临时试炼数据。
    public static void confirmTrial(ServerPlayer player) {
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            if (data.isRankTrial()) {
                StarRailPathMessages.sendQueued(player, data.getTrialPath(), Component.translatable(
                        "message.starrail_sim.rank_trial_auto_complete"));
                return;
            }
            if (!data.isTrialComplete()) {
                StarRailPathMessages.sendQueued(player, data.getTrialPath(), Component.translatable(
                        "message.starrail_sim.trial_not_complete"));
                return;
            }

            StarRailPath path = data.getTrialPath();
            if (data.getPathRank(path) == StarRailPathRank.UNALIGNED) {
                data.setPathRank(path, StarRailPathRank.PATHFARING);
            }
            data.setCurrentPath(path);
            data.resetTrial();
            StarRailPathEffects.refresh(player, path);
            StarRailPathMessages.sendQueued(player, path,
                    Component.translatable(confirmMessageKey(path)));
            sync(player);
        });
    }

    // 阶位试炼完成后保存新阶位、重置该命途修行进度并刷新效果。
    public static void confirmRankTrial(ServerPlayer player) {
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            if (!data.isRankTrial() || !data.isTrialComplete()) {
                return;
            }

            StarRailPath path = data.getTrialPath();
            StarRailPathRank previousRank = data.getPathRank(path);
            StarRailPathRank nextRank = data.getTrialRank();
            data.setPathRank(path, nextRank);
            data.setPracticeProgress(path, 0);
            data.resetTrial();
            StarRailPathEffects.refresh(player, path);
            StarRailPathMessages.sendQueued(player, path, Component.translatable(
                    "message.starrail_sim.path_rank_up",
                    path.getDisplayName(),
                    Component.translatable(previousRank.getTranslationKey()),
                    Component.translatable(nextRank.getTranslationKey())));
            sync(player);
        });
    }

    // 回退当前命途时清除对应服务状态，但保留已解锁记录和各命途历史阶位。
    public static void resetPath(ServerPlayer player) {
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            if (!data.getCurrentPath().isRealPath()) {
                sync(player);
                return;
            }

            StarRailPath previousPath = data.getCurrentPath();
            StarRailPreservationService.clear(data);
            StarRailDestructionService.clear(data);
            StarRailEruditionService.clear(data);
            data.setCurrentPath(StarRailPath.NONE);
            data.resetTrial();
            data.clearSoughtRuin();
            StarRailPathEffects.refresh(player, StarRailPath.NONE);
            StarRailPathMessages.sendQueued(player, previousPath, Component.translatable(
                    "message.starrail_sim.path_reset"));
            sync(player);
        });
    }

    // 把服务端的玩家命途和试炼状态打包发送给客户端界面。
    public static void sync(ServerPlayer player) {
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            int remaining = 0;
            if (data.getTrialPath().isRealPath()) {
                long elapsed = player.level().getGameTime() - data.getTrialStartTick();
                remaining = (int) Math.max(0L,
                        (StarRailPathRules.trialTimeLimit(data.getTrialPath(), data.getTrialRank()) - elapsed)
                                / 20L);
            }
            StarRailNetwork.sendPathState(player, new PathStatePacket(
                    data.isPathUnlocked(),
                    data.getCurrentPath(),
                    data.getCurrentPathRank(),
                    data.getPracticeProgress(data.getCurrentPath()),
                    StarRailPathProgress.targetFor(data.getCurrentPathRank()),
                    data.getPinnaclePracticeCount(data.getCurrentPath()),
                    data.getTrialPath(),
                    data.getTrialRank(),
                    data.getObjective1Progress(),
                    data.getObjective2Progress(),
                    remaining,
                    data.getSoughtPath(),
                    data.getSoughtX(),
                    data.getSoughtY(),
                    data.getSoughtZ()));
            StarRailTraceService.sync(player, data);
        });
    }

    private static String startMessageKey(StarRailPath path) {
        return switch (path) {
            case PRESERVATION -> "message.starrail_sim.preservation_started";
            case ABUNDANCE -> "message.starrail_sim.abundance_started";
            case DESTRUCTION -> "message.starrail_sim.destruction_started";
            case ERUDITION -> "message.starrail_sim.erudition_started";
            case NIHILITY -> "message.starrail_sim.nihility_started";
            case HARMONY -> "message.starrail_sim.harmony_started";
            case REMEMBRANCE -> "message.starrail_sim.remembrance_started";
            case ELATION -> "message.starrail_sim.elation_started";
            default -> "message.starrail_sim.hunt_started";
        };
    }

    private static String confirmMessageKey(StarRailPath path) {
        return switch (path) {
            case PRESERVATION -> "message.starrail_sim.preservation_confirmed";
            case ABUNDANCE -> "message.starrail_sim.abundance_confirmed";
            case DESTRUCTION -> "message.starrail_sim.destruction_confirmed";
            case ERUDITION -> "message.starrail_sim.erudition_confirmed";
            case NIHILITY -> "message.starrail_sim.nihility_confirmed";
            case HARMONY -> "message.starrail_sim.harmony_confirmed";
            case REMEMBRANCE -> "message.starrail_sim.remembrance_confirmed";
            case ELATION -> "message.starrail_sim.elation_confirmed";
            default -> "message.starrail_sim.hunt_confirmed";
        };
    }
}
