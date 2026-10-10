package com.starrail.sim;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Monster;

/** Objective accounting after a real hit, with separate initial and breakthrough rules. */
public final class StarRailRankTrialService {
    private StarRailRankTrialService() {}
    private static CompoundTag state(ServerPlayer player, IStarRailPathData data) {
        CompoundTag root = player.getPersistentData();
        String key = "starrail_trial_runtime";
        if (!root.contains(key)) root.put(key, new CompoundTag());
        CompoundTag s = root.getCompound(key);
        String id = data.getTrialPath().getId() + ":" + data.getTrialRank().getLevel() + ":" + data.getTrialStartTick();
        if (!s.getString("id").equals(id)) {
            s = new CompoundTag(); s.putString("id", id); root.put(key, s);
        }
        return s;
    }

    public static void objective(ServerPlayer player, IStarRailPathData data, int index, int amount) {
        if (!data.getTrialPath().isRealPath() || amount <= 0) return;
        if (index == 1) data.setObjective1Progress(data.getObjective1Progress() + amount);
        else data.setObjective2Progress(data.getObjective2Progress() + amount);
    }

    public static void finish(ServerPlayer player, IStarRailPathData data) {
        if (!data.getTrialPath().isRealPath()) return;
        if (data.isTrialComplete()) {
            StarRailPracticeService.clearAccumulation(player);
            if (data.isRankTrial()) StarRailPathService.confirmRankTrial(player);
            else StarRailPathService.confirmTrial(player);
        } else StarRailPathService.sync(player);
    }

    public static void queue(ServerPlayer player, LivingEntity target, String flag, int value) {
        CompoundTag root = target.getPersistentData();
        String key = "starrail_pending_hit";
        CompoundTag s = root.getCompound(key);
        long now = StarRailRankRuntime.now(player);
        if (s.getLong("tick") != now || !s.getString("owner").equals(player.getUUID().toString())) {
            s = new CompoundTag(); s.putLong("tick", now); s.putString("owner", player.getUUID().toString());
        }
        s.putInt(flag, value); root.put(key, s);
    }

    public static void hit(ServerPlayer player, IStarRailPathData data, Monster target,
                           DamageSource source, float damage) {
        StarRailPracticeService.combat(player);
        CompoundTag flags = target.getPersistentData().getCompound("starrail_pending_hit");
        boolean pending = flags.getLong("tick") == StarRailRankRuntime.now(player)
                && flags.getString("owner").equals(player.getUUID().toString());
        target.getPersistentData().remove("starrail_pending_hit");
        StarRailPath trial = data.getTrialPath();
        StarRailPath path = trial.isRealPath() ? trial : data.getCurrentPath();
        if (pending && path == StarRailPath.ELATION) {
            int combo = flags.getInt("combo");
            if (trial.isRealPath()) {
                // Keep the best chain in this trial, rather than counting disconnected hits.
                data.setObjective1Progress(Math.max(data.getObjective1Progress(), combo));
                if (flags.getInt("burst") != 0) objective(player, data, 2, 1);
            } else if (flags.getInt("burst") != 0) StarRailPracticeService.signal(player, data, path);
        }
        if (pending && path == StarRailPath.REMEMBRANCE) {
            if (flags.getInt("echo") != 0) {
                if (trial.isRealPath()) objective(player, data, 2, 1);
                else StarRailPracticeService.signal(player, data, path);
            } else if (flags.getInt("record") != 0 && trial.isRealPath()) objective(player, data, 1, 1);
        }
        if (path == StarRailPath.NIHILITY && StarRailDebuffService.applyNihilityMark(target, player, source)) {
            if (trial.isRealPath()) objective(player, data, 1, 1);
            else StarRailPracticeService.mark(player, data, target);
        }
        if (path == StarRailPath.ERUDITION) {
            CompoundTag s = state(player, data);
            String attack = source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile projectile
                    ? "projectile_" + projectile.getUUID() : "melee_" + StarRailRankRuntime.now(player);
            if (!s.getString("attack").equals(attack)) {
                s.putString("attack", attack); s.put("hit_targets", new CompoundTag()); s.putBoolean("multi", false);
            }
            CompoundTag seen = s.getCompound("hit_targets");
            seen.putBoolean(target.getUUID().toString(), true); s.put("hit_targets", seen);
            if (trial.isRealPath() && data.isRankTrial()) {
                CompoundTag all = s.getCompound("unique");
                if (!all.contains(target.getUUID().toString())) {
                    all.putBoolean(target.getUUID().toString(), true); objective(player, data, 2, 1);
                }
                s.put("unique", all);
            }
            if (seen.size() >= 2 && !s.getBoolean("multi")) {
                s.putBoolean("multi", true);
                if (trial.isRealPath()) {
                    objective(player, data, 1, 1);
                    if (!data.isRankTrial()) objective(player, data, 2, 1);
                } else StarRailPracticeService.signal(player, data, path);
            }
            if (data.getCurrentPath() == StarRailPath.ERUDITION)
                StarRailEruditionService.onPlayerAttack(player, target, damage, data, source);
        }
        finish(player, data);
    }

    public static void blocked(ServerPlayer player, IStarRailPathData data) {
        if (data.getTrialPath() == StarRailPath.PRESERVATION) {
            objective(player, data, 1, 1); finish(player, data);
        } else StarRailPracticeService.blocked(player, data);
    }

    public static void hurt(ServerPlayer player, IStarRailPathData data, float amount, boolean low) {
        StarRailPath trial = data.getTrialPath();
        if (trial != StarRailPath.PRESERVATION && !(trial == StarRailPath.DESTRUCTION && low)) return;
        CompoundTag s = state(player, data);
        s.putDouble("damage", s.getDouble("damage") + amount);
        data.setObjective2Progress((int) Math.floor(s.getDouble("damage")));
        finish(player, data);
    }

    public static void healing(ServerPlayer player, IStarRailPathData data, float actual) {
        if (data.getTrialPath() != StarRailPath.ABUNDANCE) {
            StarRailPracticeService.healing(player, data, actual); return;
        }
        CompoundTag s = state(player, data);
        s.putDouble("healing", s.getDouble("healing") + actual);
        data.setObjective1Progress((int) Math.floor(s.getDouble("healing")));
        long tick = StarRailRankRuntime.now(player);
        if (!s.contains("heal_tick") || s.getLong("heal_tick") != tick) {
            objective(player, data, 2, 1); s.putLong("heal_tick", tick);
        }
        finish(player, data);
    }

    public static void food(ServerPlayer player, IStarRailPathData data) {
        if (data.getTrialPath() != StarRailPath.HARMONY) {
            StarRailPracticeService.food(player, data); return;
        }
        CompoundTag s = state(player, data);
        long now = StarRailRankRuntime.now(player);
        if (!data.isRankTrial() || !s.contains("food_tick") || now - s.getLong("food_tick") >= 200) {
            objective(player, data, 1, 1); s.putLong("food_tick", now);
        }
        finish(player, data);
    }

    public static void killed(ServerPlayer player, IStarRailPathData data, LivingEntity target) {
        StarRailPath trial = data.getTrialPath();
        if (!trial.isRealPath()) { StarRailPracticeService.killed(player, data, target); return; }
        if (trial == StarRailPath.HUNT) {
            objective(player, data, 1, 1);
            long now = player.level().getGameTime();
            if (data.isRankTrial()) {
                CompoundTag s = state(player, data);
                int chain = data.getLastHuntKillTick() >= data.getTrialStartTick()
                        && now - data.getLastHuntKillTick() <= StarRailPathRules.HUNT_STREAK_WINDOW
                        ? s.getInt("chain") + 1 : 1;
                s.putInt("chain", chain);
                data.setObjective2Progress(Math.max(data.getObjective2Progress(), chain));
            } else if (data.getLastHuntKillTick() >= data.getTrialStartTick()
                    && now - data.getLastHuntKillTick() <= StarRailPathRules.HUNT_STREAK_WINDOW) objective(player, data, 2, 1);
            data.setLastHuntKillTick(now);
        } else if (trial == StarRailPath.DESTRUCTION && player.getHealth() <= player.getMaxHealth() * .5F) {
            objective(player, data, 1, 1);
        } else if (trial == StarRailPath.NIHILITY && StarRailDebuffService.isNihilityMarkOwnedBy(target, player.getUUID())) {
            objective(player, data, 2, 1);
        } else if (trial == StarRailPath.HARMONY && StarRailHarmonyService.isResonating(player)) objective(player, data, 2, 1);
        finish(player, data);
    }
}
