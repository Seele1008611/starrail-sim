package com.starrail.sim;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Server-authoritative operations shared by commands and the path screen. */
public final class StarRailPathService {
    private StarRailPathService() {
    }

    public static void startTrial(ServerPlayer player, StarRailPath path) {
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            if (!data.isPathUnlocked()) {
                return;
            }
            if (data.getCurrentPath().isRealPath()) {
                StarRailPathMessages.sendQueued(player, data.getCurrentPath(), Component.translatable(
                        "message.starrail_sim.path_already_chosen"));
                return;
            }
            if (data.getTrialPath().isRealPath()) {
                StarRailPathMessages.sendQueued(player, data.getTrialPath(), Component.translatable(
                        "message.starrail_sim.trial_already_active"));
                return;
            }
            if (!StarRailPathRules.isImplemented(path)) {
                StarRailPathMessages.sendQueued(player, path, Component.translatable(
                        "message.starrail_sim.path_unavailable",
                        path.getDisplayName()));
                return;
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
            StarRailPathMessages.sendQueued(player, path,
                    Component.translatable(startMessageKey(path)));
            sync(player);
        });
    }

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
            StarRailPathEffects.refresh(player, StarRailPath.NONE);
            StarRailPathMessages.sendQueued(player, previousPath, Component.translatable(
                    "message.starrail_sim.path_reset"));
            sync(player);
        });
    }

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
                    remaining));
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
