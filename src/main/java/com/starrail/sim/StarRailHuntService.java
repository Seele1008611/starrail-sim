package com.starrail.sim;

/**
 * 模组代码说明：服务层类，封装对应系统的状态操作与规则，供事件、指令或界面调用。
 */

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Server-side state and combat rules for the Hunt path's rank mechanics. */
public final class StarRailHuntService {
    public static final int MAX_INTENT_STACKS = 3;
    public static final long INTENT_DURATION = 8L * 20L;
    public static final long REVERSE_PURSUIT_COOLDOWN = 15L * 20L;

    private static final float PURSUIT_STRIKE_MULTIPLIER = 1.25F;
    private static final float REVERSE_PURSUIT_MULTIPLIER = 1.50F;

    private StarRailHuntService() {
    }

    /** Adds one Hunt intent stack for each defeated hostile entity. */
    public static void onMonsterKilled(ServerPlayer player, IStarRailPathData data) {
        if (data.getCurrentPath() != StarRailPath.HUNT
                || data.getCurrentPathRank().getLevel() < StarRailPathRank.PRACTICE.getLevel()) {
            return;
        }

        long currentTick = player.level().getGameTime();
        int stacks = data.getHuntIntentExpireTick() >= currentTick
                ? data.getHuntIntentStacks() : 0;
        stacks = Math.min(MAX_INTENT_STACKS, stacks + 1);
        data.setHuntIntentStacks(stacks);
        data.setHuntIntentExpireTick(currentTick + INTENT_DURATION);
        sendIntentMessage(player, data, currentTick);
    }

    /** Returns the damage multiplier for the next direct hit, or 1 when inactive. */
    public static float consumePursuitStrike(ServerPlayer player, IStarRailPathData data) {
        int rank = data.getCurrentPathRank().getLevel();
        if (data.getCurrentPath() != StarRailPath.HUNT
                || rank < StarRailPathRank.DEEP_PRACTICE.getLevel()
                || data.getHuntIntentStacks() < MAX_INTENT_STACKS) {
            return 1.0F;
        }

        long currentTick = player.level().getGameTime();
        if (data.getHuntIntentExpireTick() < currentTick) {
            clearIntent(data);
            return 1.0F;
        }
        if (rank >= StarRailPathRank.PATH_PINNACLE.getLevel()
                && data.getHuntFinisherCooldownTick() > currentTick) {
            return 1.0F;
        }

        data.setHuntIntentStacks(0);
        data.setHuntIntentExpireTick(-1L);
        boolean reversePursuit = rank >= StarRailPathRank.PATH_PINNACLE.getLevel();
        if (reversePursuit) {
            data.setHuntFinisherCooldownTick(currentTick + REVERSE_PURSUIT_COOLDOWN);
            StarRailPathMessages.send(player, StarRailPath.HUNT, Component.translatable(
                    "message.starrail_sim.hunt_reverse_pursuit"));
            return REVERSE_PURSUIT_MULTIPLIER;
        }

        StarRailPathMessages.send(player, StarRailPath.HUNT, Component.translatable(
                "message.starrail_sim.hunt_pursuit_strike"));
        return PURSUIT_STRIKE_MULTIPLIER;
    }

    /** Rank six lets a critical hit keep an active hunting chain alive. */
    public static void onCriticalHit(ServerPlayer player, IStarRailPathData data) {
        if (data.getCurrentPath() != StarRailPath.HUNT
                || data.getCurrentPathRank().getLevel()
                < StarRailPathRank.HIGH_PATHSTRIDER.getLevel()
                || data.getHuntIntentStacks() <= 0) {
            return;
        }
        data.setHuntIntentExpireTick(player.level().getGameTime() + INTENT_DURATION);
        StarRailPathMessages.send(player, StarRailPath.HUNT, Component.translatable(
                "message.starrail_sim.hunt_intent_extended"));
    }

    public static void tick(ServerPlayer player, IStarRailPathData data) {
        if (data.getHuntIntentStacks() <= 0) {
            return;
        }
        if (data.getCurrentPath() != StarRailPath.HUNT
                || data.getHuntIntentExpireTick() < player.level().getGameTime()) {
            clearIntent(data);
            if (data.getCurrentPath() == StarRailPath.HUNT) {
                StarRailPathMessages.send(player, StarRailPath.HUNT, Component.translatable(
                        "message.starrail_sim.hunt_intent_expired"));
            }
        }
    }

    public static void clear(IStarRailPathData data) {
        clearIntent(data);
        data.setHuntFinisherCooldownTick(-1L);
    }

    private static void clearIntent(IStarRailPathData data) {
        data.setHuntIntentStacks(0);
        data.setHuntIntentExpireTick(-1L);
    }

    private static void sendIntentMessage(ServerPlayer player, IStarRailPathData data,
                                          long currentTick) {
        int seconds = (int) Math.max(0L,
                (data.getHuntIntentExpireTick() - currentTick + 19L) / 20L);
        StarRailPathMessages.send(player, StarRailPath.HUNT, Component.translatable(
                "message.starrail_sim.hunt_intent",
                data.getHuntIntentStacks(), MAX_INTENT_STACKS, seconds));
    }
}
