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
    public static final long REVERSE_PURSUIT_COOLDOWN = 8L * 20L;

    private static final float PURSUIT_STRIKE_MULTIPLIER = 2.0F;
    private static final float REVERSE_PURSUIT_MULTIPLIER = 4.0F;

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
        StarRailPathEffects.refresh(player, StarRailPath.HUNT);
    }

    /** Returns the damage multiplier for the next landed player hit, or 1 when inactive. */
    public static float consumePursuitStrike(ServerPlayer player, IStarRailPathData data) {
        int rank = data.getCurrentPathRank().getLevel();
        int requiredStacks = requiredIntentStacks(player);
        if (data.getCurrentPath() != StarRailPath.HUNT
                || rank < StarRailPathRank.DEEP_PRACTICE.getLevel()
                || data.getHuntIntentStacks() < requiredStacks) {
            return 1.0F;
        }

        long currentTick = player.level().getGameTime();
        if (data.getHuntIntentExpireTick() < currentTick) {
            clearIntent(data);
            StarRailPathEffects.refresh(player, StarRailPath.HUNT);
            return 1.0F;
        }
        if (rank >= StarRailPathRank.PATH_PINNACLE.getLevel()
                && !StarRailRankRuntime.ready(player, "skill_hunt_reverse")) {
            return 1.0F;
        }

        data.setHuntIntentStacks(0);
        data.setHuntIntentExpireTick(-1L);
        boolean reversePursuit = rank >= StarRailPathRank.PATH_PINNACLE.getLevel();
        if (reversePursuit) {
            long cooldown = StarRailTraces.has(data.getTraceMask(StarRailPath.HUNT), 8)
                    ? 4L * 20L : REVERSE_PURSUIT_COOLDOWN;
            data.setHuntFinisherCooldownTick(currentTick + cooldown);
            StarRailRankRuntime.cooldown(player, "skill_hunt_reverse", cooldown);
            StarRailPathMessages.send(player, StarRailPath.HUNT, Component.translatable(
                    "message.starrail_sim.hunt_reverse_pursuit"));
            StarRailPathEffects.refresh(player, StarRailPath.HUNT);
            return REVERSE_PURSUIT_MULTIPLIER;
        }

        StarRailPathMessages.send(player, StarRailPath.HUNT, Component.translatable(
                "message.starrail_sim.hunt_pursuit_strike"));
        StarRailPathEffects.refresh(player, StarRailPath.HUNT);
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
        long currentTick = player.level().getGameTime();
        if (data.getHuntIntentExpireTick() < currentTick) {
            clearIntent(data);
            StarRailPathEffects.refresh(player, StarRailPath.HUNT);
            return;
        }
        data.setHuntIntentExpireTick(currentTick + INTENT_DURATION);
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

    /** Attack-damage attribute bonus contributed by currently active Hunt Intent. */
    public static double intentAttackDamageBonus(IStarRailPathData data, long currentTick) {
        int rank = data.getCurrentPathRank().getLevel();
        if (data.getCurrentPath() != StarRailPath.HUNT
                || rank < StarRailPathRank.PRACTICE.getLevel()
                || data.getHuntIntentStacks() <= 0
                || data.getHuntIntentExpireTick() < currentTick) {
            return 0.0D;
        }
        double perStack = rank >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel()
                ? 0.12D : 0.08D;
        return Math.min(MAX_INTENT_STACKS, data.getHuntIntentStacks()) * perStack;
    }

    private static void sendIntentMessage(ServerPlayer player, IStarRailPathData data,
                                          long currentTick) {
        int seconds = (int) Math.max(0L,
                (data.getHuntIntentExpireTick() - currentTick + 19L) / 20L);
        StarRailPathMessages.send(player, StarRailPath.HUNT, Component.translatable(
                "message.starrail_sim.hunt_intent",
                data.getHuntIntentStacks(), MAX_INTENT_STACKS, seconds));
    }

    private static int requiredIntentStacks(ServerPlayer player) {
        return StarRailTraceService.hasPassive(player, StarRailPath.HUNT, 6)
                ? 2 : MAX_INTENT_STACKS;
    }
}
