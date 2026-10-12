package com.starrail.sim;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Applies the shared path-rank regeneration without firing healing-based path procs. */
public final class StarRailPathRegenService {
    private static final String NATURAL_RECOVERY_TAG = "starrail_sim_path_natural_recovery";

    private StarRailPathRegenService() {
    }

    public static void tick(ServerPlayer player, IStarRailPathData data) {
        if (player.tickCount % 20 != 0 || !player.isAlive()) {
            return;
        }

        StarRailPath path = data.getCurrentPath();
        if (path == null || !path.isRealPath()) {
            return;
        }
        int rank = StarRailPathGrowth.clampLevel(data.getPathRank(path).getLevel());
        if (rank <= 0 || player.getHealth() >= player.getMaxHealth()) {
            return;
        }

        float amount = (float) (player.getMaxHealth()
                * StarRailPathGrowth.regenerationFraction(rank));
        if (amount <= 0.0F) {
            return;
        }

        boolean previouslyMarked = player.getPersistentData().getBoolean(NATURAL_RECOVERY_TAG);
        player.getPersistentData().putBoolean(NATURAL_RECOVERY_TAG, true);
        try {
            player.heal(amount);
        } finally {
            if (previouslyMarked) {
                player.getPersistentData().putBoolean(NATURAL_RECOVERY_TAG, true);
            } else {
                player.getPersistentData().remove(NATURAL_RECOVERY_TAG);
            }
        }
    }

    public static boolean isNaturalRecovery(Player player) {
        return player.getPersistentData().getBoolean(NATURAL_RECOVERY_TAG);
    }
}
