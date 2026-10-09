package com.starrail.sim;

/**
 * 模组代码说明：服务层类，封装对应系统的状态操作与规则，供事件、指令或界面调用。
 */

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;

/** Server-side state and combat rules for the Preservation path. */
public final class StarRailPreservationService {
    private static final String PASSIVE_GUARD_TAG = "trace_preservation_countershock";
    private static final String PASSIVE_SHIELD_BARRIER_TAG = "trace_preservation_shield_barrier";
    private static final int MAX_GUARD_STACKS = 3;
    private static final long GUARD_DURATION = 8L * 20L;
    private static final long BARRIER_COOLDOWN = 6L * 20L;
    private static final long BARRIER_DURATION = 10L * 20L;
    private static final long FORTRESS_DURATION = 15L * 20L;
    private static final long SHIELD_INTERVAL = 10L * 20L;

    private StarRailPreservationService() {
    }

    /**
     * Records a successful block and returns the countershock damage to apply
     * after the LivingAttackEvent has finished.
     */
    public static float onBlockedAttack(ServerPlayer player, IStarRailPathData data,
                                        Monster attacker) {
        if (data.getCurrentPath() != StarRailPath.PRESERVATION) {
            return 0.0F;
        }

        int level = data.getCurrentPathRank().getLevel();
        if (level < StarRailPathRank.PRACTICE.getLevel()) {
            return 0.0F;
        }

        long currentTick = player.level().getGameTime();
        boolean barrierActive = data.getPreservationBarrierExpireTick() > currentTick;
        if (!barrierActive) {
            data.setPreservationBarrierExpireTick(-1L);
        }

        int stacks = data.getPreservationGuardExpireTick() > currentTick
                ? data.getPreservationGuardStacks() : 0;
        stacks = Math.min(MAX_GUARD_STACKS, stacks + 1);
        data.setPreservationGuardStacks(stacks);
        data.setPreservationGuardExpireTick(currentTick + GUARD_DURATION);
        sendGuardProgress(player, stacks);

        boolean activatedBarrier = false;
        boolean reducedThreshold = player.getPersistentData().getBoolean(PASSIVE_GUARD_TAG);
        int requiredStacks = reducedThreshold ? MAX_GUARD_STACKS - 1 : MAX_GUARD_STACKS;
        if (stacks >= requiredStacks
                && level >= StarRailPathRank.DEEP_PRACTICE.getLevel()
                && data.getPreservationBarrierCooldownTick() <= currentTick
                && !barrierActive) {
            activateBarrier(player, data, currentTick, level);
            player.getPersistentData().remove(PASSIVE_GUARD_TAG);
            activatedBarrier = true;
        }

        if (!activatedBarrier
                && barrierActive
                && level >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel()) {
            double armor = player.getAttributeValue(Attributes.ARMOR);
            float multiplier = level >= StarRailPathRank.PATH_PINNACLE.getLevel()
                    ? 0.50F : 0.20F;
            if (StarRailTraceService.hasPassive(player, StarRailPath.PRESERVATION, 8)) {
                multiplier *= 1.20F;
            }
            float damage = (float) (armor * multiplier);
            if (StarRailTraceService.hasPassive(player, StarRailPath.PRESERVATION, 7)) {
                player.getPersistentData().putBoolean(PASSIVE_GUARD_TAG, true);
            }
            if (StarRailTraceService.hasPassive(player, StarRailPath.PRESERVATION, 6)
                    && player.getPersistentData().getLong(PASSIVE_SHIELD_BARRIER_TAG)
                    != data.getPreservationBarrierExpireTick()) {
                player.setAbsorptionAmount(player.getAbsorptionAmount() + 1.0F);
                player.getPersistentData().putLong(PASSIVE_SHIELD_BARRIER_TAG,
                        data.getPreservationBarrierExpireTick());
            }
            StarRailPathMessages.send(player, StarRailPath.PRESERVATION,
                    Component.translatable("message.starrail_sim.preservation_countershock",
                            String.format("%.2f", damage)));
            return damage;
        }
        return 0.0F;
    }

    /** Records hostile damage for Preservation practice, excluding blocks. */
    public static void onUnblockedDamage(ServerPlayer player, IStarRailPathData data,
                                         float amount) {
        if (data.getCurrentPath() != StarRailPath.PRESERVATION
                || data.getTrialPath().isRealPath()
                || amount < 2.0F) {
            return;
        }
        int practice = Math.min(3, (int) Math.floor(amount / 2.0F));
        StarRailPathProgress.record(player, data, StarRailPath.PRESERVATION, practice);
    }

    /** Expires temporary Preservation state on the server tick. */
    public static void tick(ServerPlayer player, IStarRailPathData data) {
        if (data.getCurrentPath() != StarRailPath.PRESERVATION) {
            clear(data);
            return;
        }

        long currentTick = player.level().getGameTime();
        int level = data.getPathRank(StarRailPath.PRESERVATION).getLevel();
        if (level >= StarRailPathRank.GLIMPSE.getLevel()) {
            tickShield(player, data, currentTick);
        } else {
            data.setPreservationNextShieldTick(-1L);
        }
        if (data.getPreservationGuardStacks() > 0
                && data.getPreservationGuardExpireTick() <= currentTick) {
            data.setPreservationGuardStacks(0);
            data.setPreservationGuardExpireTick(-1L);
            StarRailPathMessages.send(player, StarRailPath.PRESERVATION,
                    Component.translatable(
                            "message.starrail_sim.preservation_guard_expired"));
        }
        if (data.getPreservationBarrierExpireTick() >= 0L
                && data.getPreservationBarrierExpireTick() <= currentTick) {
            data.setPreservationBarrierExpireTick(-1L);
            StarRailPathMessages.send(player, StarRailPath.PRESERVATION,
                    Component.translatable(
                            "message.starrail_sim.preservation_barrier_expired"));
        }
    }

    /** Clears temporary state when the player changes or rolls back a path. */
    public static void clear(IStarRailPathData data) {
        data.setPreservationGuardStacks(0);
        data.setPreservationGuardExpireTick(-1L);
        data.setPreservationBarrierCooldownTick(-1L);
        data.setPreservationBarrierExpireTick(-1L);
        data.setPreservationNextShieldTick(-1L);
    }

    private static void tickShield(ServerPlayer player, IStarRailPathData data,
                                   long currentTick) {
        long nextShieldTick = data.getPreservationNextShieldTick();
        if (nextShieldTick < 0L) {
            data.setPreservationNextShieldTick(currentTick + SHIELD_INTERVAL);
            return;
        }
        if (currentTick < nextShieldTick) {
            return;
        }

        double armor = Math.max(0.0D, player.getAttributeValue(Attributes.ARMOR));
        if (armor > 0.0D) {
            float currentAbsorption = Math.max(0.0F, player.getAbsorptionAmount());
            float maximumAbsorption = (float) (armor * 2.5D);
            if (currentAbsorption < maximumAbsorption) {
                player.setAbsorptionAmount(Math.min(maximumAbsorption,
                        currentAbsorption + (float) armor));
            }
        }
        data.setPreservationNextShieldTick(currentTick + SHIELD_INTERVAL);
    }

    private static void activateBarrier(ServerPlayer player, IStarRailPathData data,
                                        long currentTick, int level) {
        boolean fortress = level >= StarRailPathRank.PATH_PINNACLE.getLevel();
        int duration = fortress ? (int) FORTRESS_DURATION : (int) BARRIER_DURATION;
        data.setPreservationGuardStacks(0);
        data.setPreservationGuardExpireTick(-1L);
        data.setPreservationBarrierCooldownTick(currentTick + BARRIER_COOLDOWN);
        data.setPreservationBarrierExpireTick(currentTick + duration);
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, duration, fortress ? 2 : 1,
                false, true, true));
        StarRailCombatVfx.self(player, CombatVfxPacket.Kind.PRESERVATION_WALL);
        if (fortress) {
            StarRailPathMessages.send(player, StarRailPath.PRESERVATION,
                    Component.translatable(
                            "message.starrail_sim.preservation_fortress",
                            duration / 20));
        } else {
            StarRailPathMessages.send(player, StarRailPath.PRESERVATION,
                    Component.translatable(
                            "message.starrail_sim.preservation_barrier",
                            duration / 20));
        }
    }

    private static void sendGuardProgress(ServerPlayer player, int stacks) {
        long remaining = (GUARD_DURATION + 19L) / 20L;
        StarRailPathMessages.send(player, StarRailPath.PRESERVATION,
                Component.translatable("message.starrail_sim.preservation_guard",
                        stacks, MAX_GUARD_STACKS, remaining));
    }
}
