package com.starrail.sim;

/**
 * 模组代码说明：服务层类，封装对应系统的状态操作与规则，供事件、指令或界面调用。
 */

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

/** Single-target memory marks, echoes, and higher-rank remembrance mechanics. */
public final class StarRailRemembranceService {
    private static final String DATA_TAG = "starrail_sim_remembrance";
    private static final String AFTERGLOW_TARGET_TAG = "afterglow_target";
    private static final String AFTERGLOW_EXPIRE_TAG = "afterglow_expire";
    private static final String ECHO_COUNT_TAG = "echo_count";
    private static final String ETERNAL_COOLDOWN_TAG = "eternal_cooldown";
    private static final String ECHO_LOCK_TAG = "echo_lock";

    private static final int BASE_WINDOW = 8 * 20;
    private static final int DEEP_WINDOW = 12 * 20;
    private static final int AFTERGLOW_DURATION = 5 * 20;
    private static final int ECHO_LOCK_DURATION = 3 * 20;
    private static final int ETERNAL_COOLDOWN = 10 * 20;

    private StarRailRemembranceService() {
    }

    public static int memoryWindow(int rank) {
        return rank >= StarRailPathRank.PRACTICE.getLevel()
                ? DEEP_WINDOW : BASE_WINDOW;
    }

    public static int memoryWindow(ServerPlayer player, int rank) {
        return memoryWindow(rank) + (StarRailTraceService.hasPassive(
                player, StarRailPath.REMEMBRANCE, 6) ? 3 * 20 : 0);
    }

    public static float echoBonus(int rank) {
        return rank >= StarRailPathRank.PRACTICE.getLevel()
                ? 2.0F : 1.0F;
    }

    /** Consumes rank-five afterglow on the next direct hit to the remembered target. */
    public static float consumeAfterglow(ServerPlayer player, LivingEntity target,
                                         IStarRailPathData data) {
        if (data.getCurrentPath() != StarRailPath.REMEMBRANCE
                || data.getPathRank(StarRailPath.REMEMBRANCE).getLevel()
                < StarRailPathRank.DEEP_PRACTICE.getLevel()) {
            return 1.0F;
        }

        CompoundTag state = getState(player);
        long now = player.level().getGameTime();
        if (!state.hasUUID(AFTERGLOW_TARGET_TAG)
                || !state.getUUID(AFTERGLOW_TARGET_TAG).equals(target.getUUID())
                || now > state.getLong(AFTERGLOW_EXPIRE_TAG)) {
            return 1.0F;
        }
        state.remove(AFTERGLOW_TARGET_TAG);
        state.remove(AFTERGLOW_EXPIRE_TAG);
        saveState(player, state);
        return 1.10F + (StarRailTraceService.hasPassive(
                player, StarRailPath.REMEMBRANCE, 7) ? 0.10F : 0.0F);
    }

    /** Records the target to receive the rank-five follow-up attack bonus. */
    public static void startAfterglow(ServerPlayer player, LivingEntity target,
                                      int rank) {
        if (rank < StarRailPathRank.DEEP_PRACTICE.getLevel()) {
            return;
        }
        CompoundTag state = getState(player);
        state.putUUID(AFTERGLOW_TARGET_TAG, target.getUUID());
        state.putLong(AFTERGLOW_EXPIRE_TAG,
                player.level().getGameTime() + AFTERGLOW_DURATION);
        saveState(player, state);
        StarRailPathMessages.send(player, StarRailPath.REMEMBRANCE,
                Component.translatable("message.starrail_sim.remembrance_afterglow"));
    }

    /** Records an echo and reports whether rank seven's eternal echo is ready. */
    public static boolean recordEcho(ServerPlayer player, int rank) {
        if (rank < StarRailPathRank.PATH_PINNACLE.getLevel()) {
            return false;
        }
        CompoundTag state = getState(player);
        long now = player.level().getGameTime();
        int count = state.getInt(ECHO_COUNT_TAG) + 1;
        int requiredEchoes = StarRailTraceService.hasPassive(
                player, StarRailPath.REMEMBRANCE, 8) ? 2 : 3;
        boolean eternal = count >= requiredEchoes && now >= state.getLong(ETERNAL_COOLDOWN_TAG);
        if (eternal) {
            state.putInt(ECHO_COUNT_TAG, 0);
            state.putLong(ETERNAL_COOLDOWN_TAG, now + ETERNAL_COOLDOWN);
        } else {
            state.putInt(ECHO_COUNT_TAG, count);
        }
        saveState(player, state);
        return eternal;
    }

    public static void lockEcho(ServerPlayer player, int rank) {
        if (rank < StarRailPathRank.HIGH_PATHSTRIDER.getLevel()) {
            return;
        }
        CompoundTag state = getState(player);
        state.putLong(ECHO_LOCK_TAG,
                player.level().getGameTime() + ECHO_LOCK_DURATION);
        saveState(player, state);
    }

    public static boolean canEcho(ServerPlayer player, int rank) {
        if (rank < StarRailPathRank.HIGH_PATHSTRIDER.getLevel()) {
            return true;
        }
        return player.level().getGameTime() >= getState(player).getLong(ECHO_LOCK_TAG);
    }

    public static void sendEternalEcho(ServerPlayer player) {
        StarRailPathMessages.send(player, StarRailPath.REMEMBRANCE,
                Component.translatable("message.starrail_sim.remembrance_eternal_echo"));
    }

    private static CompoundTag getState(ServerPlayer player) {
        return player.getPersistentData().getCompound(DATA_TAG);
    }

    private static void saveState(ServerPlayer player, CompoundTag state) {
        player.getPersistentData().put(DATA_TAG, state);
    }
}
