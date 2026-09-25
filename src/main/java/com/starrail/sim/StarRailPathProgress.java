package com.starrail.sim;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Tracks per-rank practice and starts automatic breakthrough trials. */
public final class StarRailPathProgress {
    public static final int PINNACLE_PRACTICE_TARGET = 120;
    public static final int MAX_PINNACLE_PRACTICE_COUNT = 10;
    public static final double PINNACLE_ATTACK_BONUS_PER_COUNT = 0.05D;

    private StarRailPathProgress() {
    }

    public static int targetFor(StarRailPathRank rank) {
        return switch (rank) {
            case PATHFARING -> 5;
            case GLIMPSE -> 8;
            case RESONANCE -> 12;
            case PRACTICE -> 30;
            case DEEP_PRACTICE -> 60;
            case HIGH_PATHSTRIDER -> 120;
            case PATH_PINNACLE -> PINNACLE_PRACTICE_TARGET;
            default -> 0;
        };
    }

    public static double pinnacleAttackBonus(int count) {
        return Math.max(0, Math.min(MAX_PINNACLE_PRACTICE_COUNT, count))
                * PINNACLE_ATTACK_BONUS_PER_COUNT;
    }

    public static StarRailPathRank nextRank(StarRailPathRank rank) {
        return switch (rank) {
            case PATHFARING -> StarRailPathRank.GLIMPSE;
            case GLIMPSE -> StarRailPathRank.RESONANCE;
            case RESONANCE -> StarRailPathRank.PRACTICE;
            case PRACTICE -> StarRailPathRank.DEEP_PRACTICE;
            case DEEP_PRACTICE -> StarRailPathRank.HIGH_PATHSTRIDER;
            case HIGH_PATHSTRIDER -> StarRailPathRank.PATH_PINNACLE;
            default -> StarRailPathRank.UNALIGNED;
        };
    }

    public static void record(ServerPlayer player, IStarRailPathData data,
                              StarRailPath path, int amount) {
        if (data.getCurrentPath() != path || !path.isRealPath()
                || data.getTrialPath().isRealPath()) {
            return;
        }

        StarRailPathRank rank = data.getPathRank(path);
        int target = targetFor(rank);
        if (target <= 0 || amount <= 0) {
            return;
        }

        if (rank == StarRailPathRank.PATH_PINNACLE) {
            int completed = data.getPinnaclePracticeCount(path);
            if (completed >= MAX_PINNACLE_PRACTICE_COUNT) {
                data.setPracticeProgress(path, 0);
                return;
            }

            int before = data.getPracticeProgress(path);
            long accumulated = (long) before + amount;
            int newMilestones = (int) Math.min(
                    MAX_PINNACLE_PRACTICE_COUNT - completed,
                    accumulated / PINNACLE_PRACTICE_TARGET);
            int newCount = completed + newMilestones;
            int remainder = newCount >= MAX_PINNACLE_PRACTICE_COUNT
                    ? 0 : (int) (accumulated % PINNACLE_PRACTICE_TARGET);
            data.setPinnaclePracticeCount(path, newCount);
            data.setPracticeProgress(path, remainder);
            if (newMilestones > 0) {
                StarRailPathEffects.refresh(player, path);
            }
            StarRailPathService.sync(player);
            return;
        }

        int before = data.getPracticeProgress(path);
        int after = Math.min(target, before + amount);
        if (after <= before) {
            if (rank != StarRailPathRank.PATH_PINNACLE
                    && !data.getTrialPath().isRealPath()
                    && before >= target) {
                beginRankTrial(player, data, path, rank);
            }
            return;
        }
        data.setPracticeProgress(path, after);

        if (rank == StarRailPathRank.PATHFARING && after >= target) {
            data.setPathRank(path, StarRailPathRank.GLIMPSE);
            data.setPracticeProgress(path, 0);
        } else if (after >= target) {
            beginRankTrial(player, data, path, rank);
        }

        StarRailPathService.sync(player);
    }

    public static void beginRankTrial(ServerPlayer player, IStarRailPathData data,
                                      StarRailPath path, StarRailPathRank currentRank) {
        if (data.getTrialPath().isRealPath()) {
            return;
        }
        StarRailPathRank nextRank = nextRank(currentRank);
        if (!path.isRealPath() || nextRank == StarRailPathRank.UNALIGNED) {
            return;
        }

        data.resetTrial();
        data.setTrialPath(path);
        data.setTrialRank(nextRank);
        data.setTrialStartTick(player.level().getGameTime());
        data.setObjective1Progress(0);
        data.setObjective2Progress(0);
        StarRailPathMessages.sendQueued(player, path, Component.translatable(
                "message.starrail_sim.rank_trial_started",
                path.getDisplayName(),
                Component.translatable(currentRank.getTranslationKey()),
                Component.translatable(nextRank.getTranslationKey()),
                StarRailPathRules.trialTimeLimit(path, nextRank) / 20L));
        StarRailPathService.sync(player);
    }
}
