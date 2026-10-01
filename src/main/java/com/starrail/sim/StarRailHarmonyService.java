package com.starrail.sim;

/**
 * 模组代码说明：服务层类，封装对应系统的状态操作与规则，供事件、指令或界面调用。
 */

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Server-side resonance and support mechanics for the Harmony path. */
public final class StarRailHarmonyService {
    private static final String DATA_TAG = "starrail_sim_harmony";
    private static final String AFTERGLOW_HITS_TAG = "afterglow_hits";
    private static final String LAST_RESTORE_TICK_TAG = "last_restore_tick";
    private static final String ABSORPTION_AMOUNT_TAG = "absorption_amount";
    private static final String ABSORPTION_EXPIRE_TICK_TAG = "absorption_expire_tick";

    private static final int BASE_RESONANCE_DURATION = 15 * 20;
    private static final int HARMONY_RESONANCE_DURATION = 20 * 20;
    private static final int MAX_RESONANCE_DURATION = 30 * 20;
    private static final int ABSORPTION_DURATION = 5 * 20;
    private static final int KILL_HEAL_COOLDOWN = 5 * 20;
    private static final int RESONANCE_EXTENSION = 3 * 20;
    private static final float AFTERGLOW_ATTACK_MULTIPLIER = 1.05F;

    private StarRailHarmonyService() {
    }

    /** Starts or refreshes the food-triggered resonance state. */
    public static void startResonance(ServerPlayer player, int rank) {
        int duration = rank >= StarRailPathRank.PRACTICE.getLevel()
                ? HARMONY_RESONANCE_DURATION : BASE_RESONANCE_DURATION;
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, duration, 0, false, true, true));
        if (rank >= StarRailPathRank.PRACTICE.getLevel()) {
            StarRailPathMessages.send(player, StarRailPath.HARMONY, Component.translatable(
                    "message.starrail_sim.harmony_resonance_started"));
        }

        CompoundTag state = getState(player);
        if (rank >= StarRailPathRank.DEEP_PRACTICE.getLevel()) {
            state.putInt(AFTERGLOW_HITS_TAG, 3);
        }
        if (rank >= StarRailPathRank.PATH_PINNACLE.getLevel()) {
            grantAbsorption(player, state);
        }
        saveState(player, state);
    }

    /** Consumes one rank-five afterglow attack and returns its damage multiplier. */
    public static float consumeAttackMultiplier(ServerPlayer player,
                                                IStarRailPathData data) {
        if (data.getCurrentPath() != StarRailPath.HARMONY
                || data.getPathRank(StarRailPath.HARMONY).getLevel()
                < StarRailPathRank.DEEP_PRACTICE.getLevel()
                || !player.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
            return 1.0F;
        }

        CompoundTag state = getState(player);
        int hits = state.getInt(AFTERGLOW_HITS_TAG);
        if (hits <= 0) {
            return 1.0F;
        }
        state.putInt(AFTERGLOW_HITS_TAG, hits - 1);
        saveState(player, state);
        StarRailPathMessages.send(player, StarRailPath.HARMONY, Component.translatable(
                "message.starrail_sim.harmony_afterglow"));
        return AFTERGLOW_ATTACK_MULTIPLIER;
    }

    /** Applies rank-six recovery and rank-seven resonance extension on a kill. */
    public static void onResonanceKill(ServerPlayer player, IStarRailPathData data) {
        if (!player.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
            return;
        }

        int rank = data.getPathRank(StarRailPath.HARMONY).getLevel();
        CompoundTag state = getState(player);
        long now = player.level().getGameTime();
        if (rank >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel()
                && now >= state.getLong(LAST_RESTORE_TICK_TAG)) {
            player.heal(player.getMaxHealth() * 0.025F);
            state.putLong(LAST_RESTORE_TICK_TAG, now + KILL_HEAL_COOLDOWN);
            StarRailPathMessages.send(player, StarRailPath.HARMONY, Component.translatable(
                    "message.starrail_sim.harmony_concert_echo"));
        }

        if (rank >= StarRailPathRank.PATH_PINNACLE.getLevel()) {
            MobEffectInstance resonance = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
            if (resonance != null) {
                int extendedDuration = Math.min(
                        MAX_RESONANCE_DURATION,
                        resonance.getDuration() + RESONANCE_EXTENSION);
                if (extendedDuration > resonance.getDuration()) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.DAMAGE_RESISTANCE,
                            extendedDuration,
                            resonance.getAmplifier(), false, true, true));
                    StarRailPathMessages.send(player, StarRailPath.HARMONY,
                            Component.translatable(
                                    "message.starrail_sim.harmony_resonance_extended"));
                }
            }
        }
        saveState(player, state);
    }

    /** Removes expired rank-seven absorption without affecting normal effects. */
    public static void tick(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(DATA_TAG)) {
            return;
        }
        CompoundTag state = root.getCompound(DATA_TAG);
        long expireTick = state.getLong(ABSORPTION_EXPIRE_TICK_TAG);
        if (expireTick <= 0L || player.level().getGameTime() < expireTick) {
            return;
        }

        float granted = state.getFloat(ABSORPTION_AMOUNT_TAG);
        if (granted > 0.0F) {
            player.setAbsorptionAmount(Math.max(
                    0.0F, player.getAbsorptionAmount() - granted));
        }
        state.remove(ABSORPTION_AMOUNT_TAG);
        state.remove(ABSORPTION_EXPIRE_TICK_TAG);
        saveState(player, state);
    }

    private static void grantAbsorption(ServerPlayer player, CompoundTag state) {
        float amount = player.getMaxHealth() * 0.025F;
        player.setAbsorptionAmount(Math.max(player.getAbsorptionAmount(), amount));
        StarRailPathMessages.send(player, StarRailPath.HARMONY, Component.translatable(
                "message.starrail_sim.harmony_absorption"));
        state.putFloat(ABSORPTION_AMOUNT_TAG, amount);
        state.putLong(ABSORPTION_EXPIRE_TICK_TAG,
                player.level().getGameTime() + ABSORPTION_DURATION);
    }

    private static CompoundTag getState(ServerPlayer player) {
        return player.getPersistentData().getCompound(DATA_TAG);
    }

    private static void saveState(ServerPlayer player, CompoundTag state) {
        player.getPersistentData().put(DATA_TAG, state);
    }
}
