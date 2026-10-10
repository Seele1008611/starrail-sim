package com.starrail.sim;

/**
 * 模组代码说明：服务层类，封装对应系统的状态操作与规则，供事件、指令或界面调用。
 */

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Combo bursts and temporary random bonuses for the Elation path. */
public final class StarRailElationService {
    private static final String DATA_TAG = "starrail_sim_elation";
    private static final String AFTERGLOW_HITS_TAG = "afterglow_hits";
    private static final String GRAND_COOLDOWN_TAG = "grand_cooldown";

    private static final int AFTERGLOW_HITS = 2;
    private static final int GRAND_COOLDOWN = 10 * 20;
    private static final float AFTERGLOW_MULTIPLIER = 1.10F;

    private StarRailElationService() {
    }

    public static long comboWindow(int rank) {
        return rank >= StarRailPathRank.DEEP_PRACTICE.getLevel()
                ? 7L * 20L : StarRailPathRules.ELATION_COMBO_WINDOW;
    }

    public static long comboWindow(ServerPlayer player, int rank) {
        return comboWindow(rank) + (StarRailTraceService.hasPassive(
                player, StarRailPath.ELATION, 6) ? 2L * 20L : 0L);
    }

    public static float burstBonus(int rank) {
        return rank >= StarRailPathRank.DEEP_PRACTICE.getLevel()
                ? 4.0F : 2.0F;
    }

    public static float burstBonus(ServerPlayer player, int rank) {
        float bonus = burstBonus(rank);
        return StarRailTraceService.hasPassive(player, StarRailPath.ELATION, 7)
                ? bonus * 1.15F : bonus;
    }

    /** Consumes one rank-six follow-up attack bonus. */
    public static float consumeAfterglow(ServerPlayer player,
                                         IStarRailPathData data) {
        if (data.getCurrentPath() != StarRailPath.ELATION
                || data.getPathRank(StarRailPath.ELATION).getLevel()
                < StarRailPathRank.HIGH_PATHSTRIDER.getLevel()) {
            return 1.0F;
        }

        CompoundTag state = getState(player);
        int hits = state.getInt(AFTERGLOW_HITS_TAG);
        if (hits <= 0) {
            return 1.0F;
        }
        state.putInt(AFTERGLOW_HITS_TAG, hits - 1);
        saveState(player, state);
        return AFTERGLOW_MULTIPLIER;
    }

    /** Gives the next two direct attacks a small burst follow-up bonus. */
    public static void startAfterglow(ServerPlayer player, int rank) {
        if (rank < StarRailPathRank.HIGH_PATHSTRIDER.getLevel()) {
            return;
        }
        CompoundTag state = getState(player);
        int hits = AFTERGLOW_HITS + (StarRailTraceService.hasPassive(
                player, StarRailPath.ELATION, 8) ? 2 : 0);
        state.putInt(AFTERGLOW_HITS_TAG, hits);
        saveState(player, state);
        StarRailPathMessages.send(player, StarRailPath.ELATION, Component.translatable(
                "message.starrail_sim.elation_afterglow"));
    }

    /** Rank-four dice: random support effects, deliberately excluding speed. */
    public static void rollJoyDice(ServerPlayer player, int rank) {
        if (rank < StarRailPathRank.PRACTICE.getLevel()) {
            return;
        }
        grantDice(player, player.getRandom().nextInt(4));
    }

    public static void rollDistinctJoyDice(ServerPlayer player, int rank) {
        if (rank < 7) return;
        int first = player.getRandom().nextInt(4);
        int second = (first + 1 + player.getRandom().nextInt(3)) % 4;
        grantDice(player, first); grantDice(player, second);
    }

    private static void grantDice(ServerPlayer player, int choice) {
        switch (choice) {
            case 0 -> StarRailRankRuntime.grantEffect(player, new MobEffectInstance(
                    MobEffects.DAMAGE_BOOST, 5 * 20, 0, false, true, true));
            case 1 -> StarRailRankRuntime.grantEffect(player, new MobEffectInstance(
                    MobEffects.REGENERATION, 3 * 20, 0, false, true, true));
            case 2 -> StarRailRankRuntime.grantEffect(player, new MobEffectInstance(
                    MobEffects.LUCK, 10 * 20, 0, false, true, true));
            default -> StarRailRankRuntime.grantEffect(player, new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, 2 * 20, 0, false, true, true));
        }
        StarRailPathMessages.send(player, StarRailPath.ELATION,
                Component.translatable("message.starrail_sim.elation_dice"));
    }

    /** Returns true when rank seven's grand burst is ready, then starts cooldown. */
    public static boolean tryGrandBurst(ServerPlayer player, int rank) {
        if (rank < StarRailPathRank.PATH_PINNACLE.getLevel()) {
            return false;
        }
        CompoundTag state = getState(player);
        long now = player.level().getGameTime();
        if (!StarRailRankRuntime.ready(player, "skill_elation_grand")) {
            return false;
        }
        state.putLong(GRAND_COOLDOWN_TAG, now + GRAND_COOLDOWN);
        StarRailRankRuntime.cooldown(player, "skill_elation_grand", GRAND_COOLDOWN);
        saveState(player, state);
        return true;
    }

    public static void sendGrandBurst(ServerPlayer player) {
        StarRailPathMessages.send(player, StarRailPath.ELATION,
                Component.translatable("message.starrail_sim.elation_grand_burst"));
    }

    private static CompoundTag getState(ServerPlayer player) {
        return player.getPersistentData().getCompound(DATA_TAG);
    }

    private static void saveState(ServerPlayer player, CompoundTag state) {
        player.getPersistentData().put(DATA_TAG, state);
    }
}
