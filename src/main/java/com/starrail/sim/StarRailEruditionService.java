package com.starrail.sim;

/**
 * 模组代码说明：服务层类，封装对应系统的状态操作与规则，供事件、指令或界面调用。
 */

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

import java.util.List;

/** Server-side multi-target combat mechanics for the Erudition path. */
public final class StarRailEruditionService {
    private static final long ECHO_COOLDOWN = 24L;
    private static final long ANALYSIS_WINDOW = 5L * 20L;
    private static final long ANALYSIS_DURATION = 6L * 20L;
    private static final long FINAL_COOLDOWN = 12L * 20L;

    private StarRailEruditionService() {
    }

    /** Consumes the one-hit bonus created by Deconstruct Weakness. */
    public static float consumeAttackMultiplier(ServerPlayer player,
                                                IStarRailPathData data) {
        if (data.getCurrentPath() != StarRailPath.ERUDITION
                || data.getPathRank(StarRailPath.ERUDITION).getLevel()
                < StarRailPathRank.HIGH_PATHSTRIDER.getLevel()) {
            return 1.0F;
        }

        long currentTick = player.level().getGameTime();
        if (data.getEruditionAnalysisExpireTick() <= currentTick) {
            if (data.getEruditionAnalysisExpireTick() >= 0L) {
                data.setEruditionAnalysisExpireTick(-1L);
            }
            return 1.0F;
        }

        data.setEruditionAnalysisExpireTick(-1L);
        data.clearEruditionAnalysisTargets();
        StarRailPathMessages.send(player, StarRailPath.ERUDITION, Component.translatable(
                "message.starrail_sim.erudition_analysis_consumed"));
        return 1.20F;
    }

    /** Processes one player attack after the normal damage multipliers. */
    public static void onPlayerAttack(ServerPlayer player, Monster target,
                                      float finalDamage, IStarRailPathData data) {
        if (data.getCurrentPath() != StarRailPath.ERUDITION
                || !target.isAlive()
                || finalDamage <= 0.0F) {
            return;
        }

        int level = data.getPathRank(StarRailPath.ERUDITION).getLevel();
        long currentTick = player.level().getGameTime();

        if (level >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel()) {
            updateAnalysis(player, target, data, currentTick);
        }

        if (level >= StarRailPathRank.PRACTICE.getLevel()
                && currentTick >= data.getEruditionEchoCooldownTick()) {
            double echoRadius = level >= StarRailPathRank.DEEP_PRACTICE.getLevel()
                    ? 3.0D : 2.5D;
            List<Monster> nearby = nearbyMonsters(target, echoRadius);
            int maximumTargets = level >= StarRailPathRank.DEEP_PRACTICE.getLevel() ? 2 : 1;
            if (!nearby.isEmpty()) {
                double multiplier = level >= StarRailPathRank.DEEP_PRACTICE.getLevel()
                        ? 0.20D : 0.15D;
                int affected = 0;
                for (Monster secondary : nearby) {
                    dealSecondaryDamage(player, secondary,
                            Math.max(0.1F, (float) (finalDamage * multiplier)));
                    if (++affected >= maximumTargets) {
                        break;
                    }
                }
                data.setEruditionEchoCooldownTick(currentTick + ECHO_COOLDOWN);
                StarRailPathMessages.send(player, StarRailPath.ERUDITION, Component.translatable(
                        "message.starrail_sim.erudition_echo"));
            }
        }

        if (level >= StarRailPathRank.PATH_PINNACLE.getLevel()
                && currentTick >= data.getEruditionFinalCooldownTick()) {
            List<Monster> crowd = nearbyMonsters(target, 3.0D);
            if (crowd.size() >= 2) {
                for (Monster secondary : nearbyMonsters(target, 4.0D)) {
                    dealSecondaryDamage(player, secondary,
                            Math.max(0.1F, finalDamage * 0.45F));
                }
                data.setEruditionFinalCooldownTick(currentTick + FINAL_COOLDOWN);
                StarRailPathMessages.send(player, StarRailPath.ERUDITION, Component.translatable(
                        "message.starrail_sim.erudition_final"));
            }
        }
    }

    /** Expires temporary analysis tracking and one-hit windows. */
    public static void tick(ServerPlayer player, IStarRailPathData data) {
        long currentTick = player.level().getGameTime();
        if (data.getEruditionAnalysisWindowExpireTick() > 0L
                && currentTick >= data.getEruditionAnalysisWindowExpireTick()) {
            data.setEruditionAnalysisWindowExpireTick(-1L);
            data.clearEruditionAnalysisTargets();
        }
        if (data.getEruditionAnalysisExpireTick() > 0L
                && currentTick >= data.getEruditionAnalysisExpireTick()) {
            data.setEruditionAnalysisExpireTick(-1L);
        }
    }

    public static void clear(IStarRailPathData data) {
        data.setEruditionEchoCooldownTick(-1L);
        data.setEruditionAnalysisExpireTick(-1L);
        data.setEruditionAnalysisWindowExpireTick(-1L);
        data.clearEruditionAnalysisTargets();
        data.setEruditionFinalCooldownTick(-1L);
    }

    private static void updateAnalysis(ServerPlayer player, Monster target,
                                       IStarRailPathData data, long currentTick) {
        if (data.getEruditionAnalysisExpireTick() > currentTick) {
            return;
        }
        if (data.getEruditionAnalysisWindowExpireTick() <= currentTick) {
            data.clearEruditionAnalysisTargets();
            data.setEruditionAnalysisWindowExpireTick(currentTick + ANALYSIS_WINDOW);
        }
        if (!data.addEruditionAnalysisTarget(target.getUUID())) {
            return;
        }
        if (data.getEruditionAnalysisTarget(2) != null) {
            data.clearEruditionAnalysisTargets();
            data.setEruditionAnalysisWindowExpireTick(-1L);
            data.setEruditionAnalysisExpireTick(currentTick + ANALYSIS_DURATION);
            StarRailPathMessages.send(player, StarRailPath.ERUDITION, Component.translatable(
                    "message.starrail_sim.erudition_analysis"));
        }
    }

    private static List<Monster> nearbyMonsters(Monster target, double radius) {
        AABB area = target.getBoundingBox().inflate(radius);
        return target.level().getEntitiesOfClass(Monster.class, area,
                monster -> monster.isAlive() && monster != target);
    }

    private static void dealSecondaryDamage(ServerPlayer player, Monster target,
                                            float amount) {
        if (target.isAlive()) {
            target.hurt(player.damageSources().generic(), amount);
        }
    }
}
