package com.starrail.sim;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Monster;

/** Server-side toughness, break, and toughness-recovery rules. */
public final class StarRailToughnessService {
    private static final String TOUGHNESS_TAG = "starrail_sim_toughness";
    private static final String CURRENT_TAG = "current";
    private static final String MAX_TAG = "max";
    private static final String BROKEN_UNTIL_TAG = "broken_until";
    private static final String RECOVERY_START_TAG = "recovery_start";

    private static final float NORMAL_TOUGHNESS = 10.0F;
    private static final float ELITE_TOUGHNESS = 20.0F;
    private static final float BOSS_TOUGHNESS = 30.0F;
    private static final float TOUGHNESS_PER_HIT = 1.0F;
    private static final int BROKEN_DURATION = 10 * 20;
    private static final int RECOVERY_DURATION = 20 * 20;
    private static final float BASE_BREAK_DAMAGE = 2.0F;

    private StarRailToughnessService() {
    }

    /**
     * Reduces toughness alongside the normal HP damage from a player attack.
     * One landed attack removes one toughness point in this first pass.
     */
    public static boolean onPlayerAttack(ServerPlayer player, LivingEntity target) {
        if (player == null || !isToughnessTarget(target) || !target.isAlive()
                || target.level().isClientSide()) {
            return false;
        }

        CompoundTag state = getOrCreateState(target);
        long now = target.level().getGameTime();
        tickState(state, now);
        if (state.getLong(BROKEN_UNTIL_TAG) > now) {
            return false;
        }

        float current = state.getFloat(CURRENT_TAG);
        float next = Math.max(0.0F, current - TOUGHNESS_PER_HIT);
        state.putFloat(CURRENT_TAG, next);
        boolean finalHit = next <= 0.0F;
        StarRailNetwork.sendToughnessNumber(
                player, target, TOUGHNESS_PER_HIT, finalHit);
        if (finalHit) {
            breakTarget(player, target, state, now);
        }
        return finalHit;
    }

    /** Advances the post-break recovery without showing a separate toughness bar. */
    public static void tick(LivingEntity target) {
        if (target.level().isClientSide()) {
            return;
        }
        CompoundTag root = target.getPersistentData();
        if (!root.contains(TOUGHNESS_TAG)) {
            return;
        }
        tickState(root.getCompound(TOUGHNESS_TAG), target.level().getGameTime());
    }

    private static void breakTarget(ServerPlayer player, LivingEntity target,
                                    CompoundTag state, long now) {
        long brokenUntil = now + BROKEN_DURATION;
        state.putLong(BROKEN_UNTIL_TAG, brokenUntil);
        state.putLong(RECOVERY_START_TAG, brokenUntil);
        state.putFloat(CURRENT_TAG, 0.0F);

        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                BROKEN_DURATION, 1, false, true, true));
        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WEAKNESS,
                BROKEN_DURATION, 0, false, true, true));
        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.GLOWING,
                BROKEN_DURATION, 0, false, false, true));

        float breakEffect = (float) StarRailAttributes.getValue(
                player, StarRailAttributes.BREAK_EFFECT, 0.0D);
        float breakDamage = BASE_BREAK_DAMAGE * (1.0F + Math.max(0.0F, breakEffect))
                * (float) StarRailLightConeService.breakDamageMultiplier(player);
        if (target.isAlive() && target.hurt(target.damageSources().magic(), breakDamage)) {
            StarRailNetwork.sendDamageNumber(player, target, breakDamage, false);
        }
        StarRailNetwork.sendCombatNotification(player, Component.translatable(
                "message.starrail_sim.toughness_broken"));
    }

    private static CompoundTag getOrCreateState(LivingEntity target) {
        CompoundTag root = target.getPersistentData();
        CompoundTag state = root.getCompound(TOUGHNESS_TAG);
        float max = maxToughness(target);
        if (!state.contains(MAX_TAG) || state.getFloat(MAX_TAG) != max) {
            state.putFloat(MAX_TAG, max);
            state.putFloat(CURRENT_TAG, max);
            state.putLong(BROKEN_UNTIL_TAG, 0L);
            state.putLong(RECOVERY_START_TAG, 0L);
            root.put(TOUGHNESS_TAG, state);
        } else {
            root.put(TOUGHNESS_TAG, state);
        }
        return state;
    }

    private static void tickState(CompoundTag state, long now) {
        long brokenUntil = state.getLong(BROKEN_UNTIL_TAG);
        if (brokenUntil > now) {
            state.putFloat(CURRENT_TAG, 0.0F);
            return;
        }

        long recoveryStart = state.getLong(RECOVERY_START_TAG);
        if (recoveryStart <= 0L || now < recoveryStart) {
            return;
        }

        float max = state.getFloat(MAX_TAG);
        float progress = Math.min(1.0F,
                (now - recoveryStart) / (float) RECOVERY_DURATION);
        state.putFloat(CURRENT_TAG, max * progress);
        if (progress >= 1.0F) {
            state.putLong(RECOVERY_START_TAG, 0L);
        }
    }

    private static boolean isToughnessTarget(LivingEntity target) {
        return target instanceof Monster || target instanceof EnderDragon;
    }

    private static float maxToughness(LivingEntity target) {
        if (target.getType() == EntityType.WARDEN
                || target.getType() == EntityType.WITHER
                || target.getType() == EntityType.ENDER_DRAGON) {
            return BOSS_TOUGHNESS;
        }
        return target.getMaxHealth() >= 40.0F
                ? ELITE_TOUGHNESS : NORMAL_TOUGHNESS;
    }
}
