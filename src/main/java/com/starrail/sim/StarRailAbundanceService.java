package com.starrail.sim;

/**
 * 模组代码说明：服务层类，封装对应系统的状态操作与规则，供事件、指令或界面调用。
 */

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Healing, overflow, and emergency recovery mechanics for Abundance. */
public final class StarRailAbundanceService {
    private static final String DATA_TAG = "starrail_sim_abundance";
    private static final String REGEN_COOLDOWN_TAG = "regen_cooldown";
    private static final String RESISTANCE_COOLDOWN_TAG = "resistance_cooldown";
    private static final String SAVE_COOLDOWN_TAG = "save_cooldown";

    private static final int REGEN_COOLDOWN = 5 * 20;
    private static final int RESISTANCE_COOLDOWN = 8 * 20;
    private static final int SAVE_COOLDOWN = 60 * 20;
    private static final int REGEN_DURATION = 3 * 20;
    private static final int RESISTANCE_DURATION = 2 * 20;
    private static final float OVERFLOW_CONVERSION = 0.50F;
    private static final float MAX_OVERFLOW_ABSORPTION = 4.0F;

    private StarRailAbundanceService() {
    }

    /** Applies healing-triggered rank mechanics after the event amount is scaled. */
    public static void onHealing(ServerPlayer player, IStarRailPathData data,
                                 float healingAmount) {
        if (data.getCurrentPath() != StarRailPath.ABUNDANCE || healingAmount <= 0.0F) {
            return;
        }

        int rank = data.getPathRank(StarRailPath.ABUNDANCE).getLevel();
        CompoundTag state = getState(player);
        long now = player.level().getGameTime();
        float maximum = player.getMaxHealth();
        float healthBefore = player.getHealth();

        if (rank >= StarRailPathRank.PRACTICE.getLevel()
                && healthBefore < maximum * 0.80F
                && now >= state.getLong(REGEN_COOLDOWN_TAG)) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.REGENERATION, REGEN_DURATION, 0, false, true, true));
            state.putLong(REGEN_COOLDOWN_TAG, now + REGEN_COOLDOWN);
            StarRailPathMessages.send(player, StarRailPath.ABUNDANCE, Component.translatable(
                    "message.starrail_sim.abundance_lifeblood"));
        }

        if (rank >= StarRailPathRank.DEEP_PRACTICE.getLevel()) {
            float missing = Math.max(0.0F, maximum - healthBefore);
            float overflow = Math.max(0.0F, healingAmount - missing);
            float absorption = Math.min(MAX_OVERFLOW_ABSORPTION,
                    overflow * OVERFLOW_CONVERSION);
            if (absorption > 0.0F) {
                player.setAbsorptionAmount(player.getAbsorptionAmount() + absorption);
                StarRailPathMessages.send(player, StarRailPath.ABUNDANCE, Component.translatable(
                        "message.starrail_sim.abundance_manna"));
            }
        }

        if (rank >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel()
                && healthBefore < maximum * 0.50F
                && now >= state.getLong(RESISTANCE_COOLDOWN_TAG)) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, RESISTANCE_DURATION, 0,
                    false, true, true));
            state.putLong(RESISTANCE_COOLDOWN_TAG, now + RESISTANCE_COOLDOWN);
            StarRailPathMessages.send(player, StarRailPath.ABUNDANCE, Component.translatable(
                    "message.starrail_sim.abundance_rejuvenation"));
        }
        saveState(player, state);
    }

    /** Prevents one lethal hit at rank seven and starts the long cooldown. */
    public static boolean tryEmergencyRecovery(ServerPlayer player,
                                               IStarRailPathData data,
                                               float damage) {
        if (data.getCurrentPath() != StarRailPath.ABUNDANCE
                || data.getPathRank(StarRailPath.ABUNDANCE).getLevel()
                < StarRailPathRank.PATH_PINNACLE.getLevel()
                || damage < player.getHealth()) {
            return false;
        }

        CompoundTag state = getState(player);
        long now = player.level().getGameTime();
        if (now < state.getLong(SAVE_COOLDOWN_TAG)) {
            return false;
        }

        player.setHealth(1.0F);
        player.heal(player.getMaxHealth() * 0.20F);
        player.setAbsorptionAmount(Math.max(
                player.getAbsorptionAmount(), player.getMaxHealth() * 0.05F));
        state.putLong(SAVE_COOLDOWN_TAG, now + SAVE_COOLDOWN);
        saveState(player, state);
        StarRailPathMessages.send(player, StarRailPath.ABUNDANCE, Component.translatable(
                "message.starrail_sim.abundance_emergency"));
        return true;
    }

    private static CompoundTag getState(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        return root.getCompound(DATA_TAG);
    }

    private static void saveState(ServerPlayer player, CompoundTag state) {
        player.getPersistentData().put(DATA_TAG, state);
    }
}
