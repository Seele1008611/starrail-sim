package com.starrail.sim;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Balanced one-point practice rewards. Skill triggers and breakthrough objectives are independent. */
public final class StarRailPracticeService {
    private static final String KEY = "starrail_practice_runtime";
    private static final ThreadLocal<net.minecraft.world.entity.Entity> ACTION = new ThreadLocal<>();
    private StarRailPracticeService() {}

    public static void duringAttack(net.minecraft.world.damagesource.DamageSource source, Runnable action) {
        net.minecraft.world.entity.Entity previous = ACTION.get();
        if (source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile)
            ACTION.set(source.getDirectEntity());
        try { action.run(); }
        finally { if (previous == null) ACTION.remove(); else ACTION.set(previous); }
    }

    private static CompoundTag state(ServerPlayer player, StarRailPath path) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(KEY)) root.put(KEY, new CompoundTag());
        CompoundTag all = root.getCompound(KEY);
        if (!all.contains(path.getId())) all.put(path.getId(), new CompoundTag());
        CompoundTag state = all.getCompound(path.getId());
        long now = StarRailRankRuntime.now(player);
        if (now - state.getLong("activity") > 600) {
            state.remove("amount"); state.remove("blocks"); state.remove("food");
        }
        if (now - state.getLong("injured") > 600) state.remove("debt");
        return state;
    }

    public static void clearAccumulation(ServerPlayer player) {
        player.getPersistentData().remove(KEY);
    }

    private static boolean active(IStarRailPathData data, StarRailPath path) {
        return data.getCurrentPath() == path && !data.getTrialPath().isRealPath();
    }

    private static boolean award(ServerPlayer player, IStarRailPathData data, StarRailPath path,
                                 int intervalSeconds, String branch) {
        if (!active(data, path)) return false;
        net.minecraft.world.entity.Entity projectile = ACTION.get();
        String projectileReward = "starrail_practice_paid_" + player.getUUID();
        if (projectile != null && projectile.getPersistentData().getBoolean(projectileReward)) return false;
        long now = StarRailRankRuntime.now(player);
        CompoundTag saved = StarRailRankRuntime.persistent(player);
        // One physical attack (including sweep kills and derived effects) can never pay twice.
        if (saved.contains("reward_tick") && saved.getLong("reward_tick") == now) return false;
        String gate = "practice_" + path.getId() + branch;
        if (!StarRailRankRuntime.ready(player, gate)) return false;
        saved.putLong("reward_tick", now);
        if (projectile != null) projectile.getPersistentData().putBoolean(projectileReward, true);
        StarRailRankRuntime.cooldown(player, gate, intervalSeconds * 20L);
        StarRailPathProgress.record(player, data, path, 1);
        return true;
    }

    public static void signal(ServerPlayer player, IStarRailPathData data, StarRailPath path) {
        if (path == StarRailPath.ELATION || path == StarRailPath.REMEMBRANCE
                || path == StarRailPath.ERUDITION) award(player, data, path, 8, "");
    }

    public static void mark(ServerPlayer player, IStarRailPathData data, LivingEntity target) {
        String key = "starrail_practice_nihility_" + player.getUUID();
        if (!target.getPersistentData().getBoolean(key)
                && award(player, data, StarRailPath.NIHILITY, 10, "_mark"))
            target.getPersistentData().putBoolean(key, true);
    }

    public static void blocked(ServerPlayer player, IStarRailPathData data) {
        StarRailPath path = StarRailPath.PRESERVATION;
        if (!active(data, path)) return;
        CompoundTag s = state(player, path);
        s.putLong("activity", StarRailRankRuntime.now(player));
        s.putInt("blocks", Math.min(3, s.getInt("blocks") + 1));
        if (s.getInt("blocks") >= 3 && award(player, data, path, 12, "")) reset(s);
    }

    public static void combat(ServerPlayer player) {
        StarRailRankRuntime.persistent(player).putLong("combat", StarRailRankRuntime.now(player));
    }

    public static void hurt(ServerPlayer player, IStarRailPathData data, float healthDamage,
                            float absorbedDamage, boolean lowHealth) {
        combat(player);
        StarRailPath path = data.getCurrentPath();
        if (path == StarRailPath.ABUNDANCE && !data.getTrialPath().isRealPath()) {
            CompoundTag s = state(player, path);
            s.putDouble("debt", Math.min(player.getMaxHealth(), s.getDouble("debt") + healthDamage));
            s.putLong("injured", StarRailRankRuntime.now(player));
        }
        if (!active(data, path)) return;
        double amount = healthDamage + absorbedDamage;
        if (path != StarRailPath.PRESERVATION && !(path == StarRailPath.DESTRUCTION && lowHealth)) return;
        CompoundTag s = state(player, path);
        int threshold = path == StarRailPath.PRESERVATION ? 6 : 8;
        s.putDouble("amount", Math.min(threshold, s.getDouble("amount") + amount));
        s.putLong("activity", StarRailRankRuntime.now(player));
        if (s.getDouble("amount") >= threshold && award(player, data, path,
                path == StarRailPath.PRESERVATION ? 12 : 15,
                path == StarRailPath.PRESERVATION ? "" : "_aux")) reset(s);
    }

    public static void healing(ServerPlayer player, IStarRailPathData data, float actualHealing) {
        if (data.getCurrentPath() != StarRailPath.ABUNDANCE) return;
        CompoundTag s = state(player, StarRailPath.ABUNDANCE);
        double credited = Math.min(actualHealing, s.getDouble("debt"));
        s.putDouble("debt", Math.max(0, s.getDouble("debt") - actualHealing));
        if (!active(data, StarRailPath.ABUNDANCE) || credited <= 0) return;
        s.putDouble("amount", Math.min(6, s.getDouble("amount") + credited));
        s.putLong("activity", StarRailRankRuntime.now(player));
        if (s.getDouble("amount") >= 6 && award(player, data, StarRailPath.ABUNDANCE, 12, "")) {
            s.remove("amount");
        }
    }

    public static void food(ServerPlayer player, IStarRailPathData data) {
        if (!active(data, StarRailPath.HARMONY)) return;
        long now = StarRailRankRuntime.now(player);
        CompoundTag saved = StarRailRankRuntime.persistent(player);
        if (!saved.contains("combat") || now - saved.getLong("combat") > 200) return;
        if (!StarRailRankRuntime.ready(player, "practice_harmony_food_event")) return;
        StarRailRankRuntime.cooldown(player, "practice_harmony_food_event", 200);
        CompoundTag s = state(player, StarRailPath.HARMONY);
        s.putLong("activity", now); s.putInt("food", Math.min(3, s.getInt("food") + 1));
        if (s.getInt("food") >= 3 && award(player, data, StarRailPath.HARMONY, 30, "_aux")) reset(s);
    }

    public static void killed(ServerPlayer player, IStarRailPathData data, LivingEntity enemy) {
        StarRailPath path = data.getCurrentPath();
        if (path == StarRailPath.NIHILITY) {
            String key = "starrail_practice_nihility_" + player.getUUID();
            if (StarRailDebuffService.isNihilityMarkOwnedBy(enemy, player.getUUID())
                    && !enemy.getPersistentData().getBoolean(key)
                    && award(player, data, path, 0, "_kill")) enemy.getPersistentData().putBoolean(key, true);
        } else if (path == StarRailPath.HUNT) {
            award(player, data, path, 0, "");
        } else if ((path == StarRailPath.DESTRUCTION && player.getHealth() <= player.getMaxHealth() * .5F)
                || (path == StarRailPath.HARMONY && StarRailHarmonyService.isResonating(player))) {
            if (award(player, data, path, 0, "_kill")) {
                reset(state(player, path));
                StarRailRankRuntime.cooldown(player, "practice_" + path.getId() + "_aux",
                        path == StarRailPath.DESTRUCTION ? 300 : 600);
            }
        }
    }

    private static void reset(CompoundTag s) {
        s.remove("amount"); s.remove("blocks"); s.remove("food");
    }
}
