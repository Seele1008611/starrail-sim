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
        if (StarRailTraceService.hasPassive(player, StarRailPath.HARMONY, 6)) {
            duration += 4 * 20;
        }
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, duration, 0, false, true, true));
        StarRailCombatVfx.self(player, rank >= StarRailPathRank.PATH_PINNACLE.getLevel() ? CombatVfxPacket.Kind.HARMONY_UNISON : CombatVfxPacket.Kind.HARMONY_RESONANCE);
        if (rank >= StarRailPathRank.PRACTICE.getLevel()) {
            StarRailPathMessages.send(player, StarRailPath.HARMONY, Component.translatable(
                    "message.starrail_sim.harmony_resonance_started"));
        }

        CompoundTag state = getState(player);
        state.putLong("resonance_until", StarRailRankRuntime.now(player) + duration);
        if (rank >= StarRailPathRank.DEEP_PRACTICE.getLevel()) {
            int hits = 3 + (StarRailTraceService.hasPassive(
                    player, StarRailPath.HARMONY, 7) ? 1 : 0);
            state.putInt(AFTERGLOW_HITS_TAG, hits);
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
                || !isResonating(player)) {
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
        if (!isResonating(player)) {
            return;
        }

        int rank = data.getPathRank(StarRailPath.HARMONY).getLevel();
        CompoundTag state = getState(player);
        long now = player.level().getGameTime();
        if (rank >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel()
                && StarRailRankRuntime.ready(player, "skill_harmony_heal")) {
            float heal = 0.025F + (StarRailTraceService.hasPassive(
                    player, StarRailPath.HARMONY, 8) ? 0.05F : 0.0F);
            player.heal(player.getMaxHealth() * heal);
            state.putLong(LAST_RESTORE_TICK_TAG, now + KILL_HEAL_COOLDOWN);
            StarRailRankRuntime.cooldown(player, "skill_harmony_heal", KILL_HEAL_COOLDOWN);
            StarRailCombatVfx.self(player, CombatVfxPacket.Kind.HARMONY_CONCERT);
            StarRailPathMessages.send(player, StarRailPath.HARMONY, Component.translatable(
                    "message.starrail_sim.harmony_concert_echo"));
        }

        if (rank >= StarRailPathRank.PATH_PINNACLE.getLevel()) {
            int remaining = (int) Math.max(0, state.getLong("resonance_until") - StarRailRankRuntime.now(player));
            int extended = Math.min(MAX_RESONANCE_DURATION, remaining + RESONANCE_EXTENSION);
            state.putLong("resonance_until", StarRailRankRuntime.now(player) + extended);
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, extended, 0, false, true, true));
            StarRailPathMessages.send(player, StarRailPath.HARMONY, Component.translatable("message.starrail_sim.harmony_resonance_extended"));
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
        observeShield(player, state);
        saveState(player, state);
        long expireTick = state.getLong(ABSORPTION_EXPIRE_TICK_TAG);
        if (expireTick <= 0L || StarRailRankRuntime.now(player) < expireTick) {
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
        observeShield(player, state);
        float amount = player.getMaxHealth() * 0.025F;
        float added = Math.max(0, amount - player.getAbsorptionAmount());
        player.setAbsorptionAmount(Math.max(player.getAbsorptionAmount(), amount));
        StarRailPathMessages.send(player, StarRailPath.HARMONY, Component.translatable(
                "message.starrail_sim.harmony_absorption"));
        state.putFloat(ABSORPTION_AMOUNT_TAG, state.getFloat(ABSORPTION_AMOUNT_TAG) + added);
        state.putFloat("observed_absorption", player.getAbsorptionAmount());
        state.putLong(ABSORPTION_EXPIRE_TICK_TAG,
                StarRailRankRuntime.now(player) + ABSORPTION_DURATION);
    }

    private static void observeShield(ServerPlayer player, CompoundTag state) {
        float lost = Math.max(0, state.getFloat("observed_absorption") - player.getAbsorptionAmount());
        state.putFloat(ABSORPTION_AMOUNT_TAG, Math.max(0, state.getFloat(ABSORPTION_AMOUNT_TAG) - lost));
        state.putFloat("observed_absorption", player.getAbsorptionAmount());
    }

    public static boolean isResonating(ServerPlayer player) {
        return getState(player).getLong("resonance_until") > StarRailRankRuntime.now(player);
    }

    public static void clear(ServerPlayer player) {
        CompoundTag state = getState(player);
        observeShield(player, state);
        player.setAbsorptionAmount(Math.max(0, player.getAbsorptionAmount() - state.getFloat(ABSORPTION_AMOUNT_TAG)));
        // Only remove our resistance when its amplifier/duration still match our grant.
        MobEffectInstance resistance = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
        long left = state.getLong("resonance_until") - StarRailRankRuntime.now(player);
        if (resistance != null && resistance.getAmplifier() == 0 && Math.abs(resistance.getDuration() - left) <= 2)
            player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        player.getPersistentData().remove(DATA_TAG);
    }

    private static CompoundTag getState(ServerPlayer player) {
        return player.getPersistentData().getCompound(DATA_TAG);
    }

    private static void saveState(ServerPlayer player, CompoundTag state) {
        player.getPersistentData().put(DATA_TAG, state);
    }
}
