package com.starrail.sim;

import net.minecraft.server.level.ServerPlayer;

/** Clears temporary state on path transitions without erasing persistent skill/reward cooldowns. */
public final class StarRailPathTemporaryState {
    private StarRailPathTemporaryState() {}
    public static void tick(ServerPlayer player, IStarRailPathData data) {
        String previous = player.getPersistentData().getString("starrail_runtime_path");
        if (!previous.equals(data.getCurrentPath().getId())) {
            clear(player);
            player.getPersistentData().putString("starrail_runtime_path", data.getCurrentPath().getId());
        }
    }
    public static void clear(ServerPlayer player) {
        clear(player, true);
    }
    public static void clear(ServerPlayer player, boolean clearTrial) {
        StarRailRankRuntime.clearEffects(player);
        StarRailHarmonyService.clear(player);
        StarRailAbundanceService.clearTemporary(player);
        player.getPersistentData().remove("starrail_sim_remembrance");
        player.getPersistentData().remove("starrail_sim_elation");
        player.getPersistentData().remove("starrail_destruction_sustained");
        player.getPersistentData().remove("trace_destruction_wrath_crit");
        player.getPersistentData().remove("trace_destruction_desperation_blast");
        if (clearTrial) player.getPersistentData().remove("starrail_trial_runtime");
        StarRailPracticeService.clearAccumulation(player);
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            StarRailHuntService.clear(data); StarRailPreservationService.clear(data);
            StarRailDestructionService.clear(data); StarRailEruditionService.clear(data);
            data.setRemembranceTargetId(null); data.setRemembranceTargetTick(-1);
            data.setElationCombo(0); data.setElationLastHitTick(-1);
        });
    }
}
