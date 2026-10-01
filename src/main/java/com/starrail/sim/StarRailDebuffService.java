package com.starrail.sim;

/**
 * 模组代码说明：集中应用、叠加和清除模组中的负面战斗效果。
 */

import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Shared server-side support for temporary negative effects and damage over
 * time. Effects are visible through vanilla MobEffect instances while the
 * damage schedule is stored on the affected entity for later reuse.
 */
public final class StarRailDebuffService {
    public static final String NIHILITY_MARK_KEY = "nihility_erosion";
    private static final String DOTS_TAG = "starrail_sim_dots";
    private static final String REMAINING_TICKS = "remaining_ticks";
    private static final String NEXT_DAMAGE_TICKS = "next_damage_ticks";
    private static final String DAMAGE = "damage";
    private static final String INTERVAL = "interval";

    private static final int NIHILITY_MARK_DURATION = 100;
    private static final int NIHILITY_DAMAGE_INTERVAL = 20;
    private static final float NIHILITY_DAMAGE = 1.0F;
    private static final int NIHILITY_DEEP_DURATION = 140;
    private static final float NIHILITY_PAIN_DAMAGE = 1.5F;
    private static final float NIHILITY_FINAL_DAMAGE = 2.0F;
    private static final double NIHILITY_DIFFUSION_RADIUS = 2.5D;
    private static final double NIHILITY_FINAL_RADIUS = 3.0D;
    private static final String FINAL_BURST_GUARD_KEY =
            "starrail_sim_nihility_final_burst_guard";

    private StarRailDebuffService() {
    }

    /**
     * Applies the first reusable debuff: Weakness plus one magic damage every
     * second for five seconds. The return value is true only for a new mark,
     * which prevents repeated hits from inflating a trial objective.
     */
    public static boolean applyNihilityMark(LivingEntity target, ServerPlayer owner) {
        int rank = owner == null ? 0 : owner.getCapability(StarRailPathCapability.PATH_DATA)
                .map(data -> data.getPathRank(StarRailPath.NIHILITY).getLevel())
                .orElse(0);
        return applyNihilityMark(target, owner, rank, true);
    }

    private static boolean applyNihilityMark(LivingEntity target, ServerPlayer owner,
                                             int rank, boolean allowDiffusion) {
        if (target.level().isClientSide()) {
            return false;
        }
        if (owner != null && !StarRailEffectService.rollEffectHit(owner, target)) {
            return false;
        }

        boolean wasMarked = hasNihilityMark(target);
        int duration = rank >= StarRailPathRank.PRACTICE.getLevel()
                ? NIHILITY_DEEP_DURATION : NIHILITY_MARK_DURATION;
        float damage = rank >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel()
                ? NIHILITY_PAIN_DAMAGE : NIHILITY_DAMAGE;
        target.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS,
                duration,
                0,
                false,
                true,
                true));
        applyDamageOverTime(target, NIHILITY_MARK_KEY, owner,
                damage, duration, NIHILITY_DAMAGE_INTERVAL);

        if (!wasMarked && owner != null) {
            if (rank >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel()) {
                StarRailPathMessages.send(owner, StarRailPath.NIHILITY,
                        Component.translatable("message.starrail_sim.nihility_pain_echo"));
            } else if (rank >= StarRailPathRank.PRACTICE.getLevel()) {
                StarRailPathMessages.send(owner, StarRailPath.NIHILITY,
                        Component.translatable("message.starrail_sim.nihility_deep_erosion"));
            }
        }

        if (allowDiffusion && !wasMarked
                && rank >= StarRailPathRank.DEEP_PRACTICE.getLevel()) {
            diffuseNihilityMark(target, owner, rank);
        }
        return !wasMarked;
    }

    private static void diffuseNihilityMark(LivingEntity source, ServerPlayer owner, int rank) {
        if (!(source instanceof Monster)) {
            return;
        }
        List<Monster> nearby = source.level().getEntitiesOfClass(
                Monster.class,
                source.getBoundingBox().inflate(NIHILITY_DIFFUSION_RADIUS),
                monster -> monster.isAlive()
                        && monster != source
                        && !hasNihilityMark(monster));
        if (nearby.isEmpty()) {
            return;
        }
        if (applyNihilityMark(nearby.get(0), owner, rank, false)) {
            StarRailPathMessages.send(owner, StarRailPath.NIHILITY,
                    Component.translatable("message.starrail_sim.nihility_diffusion"));
        }
    }

    /** Triggers the rank-seven burst when an owned eroded monster dies. */
    public static void onNihilityMarkedDeath(ServerPlayer owner, LivingEntity deceased) {
        if (deceased.getPersistentData().getBoolean(FINAL_BURST_GUARD_KEY)) {
            return;
        }
        int rank = owner.getCapability(StarRailPathCapability.PATH_DATA)
                .map(data -> data.getCurrentPath() == StarRailPath.NIHILITY
                        ? data.getPathRank(StarRailPath.NIHILITY).getLevel() : 0)
                .orElse(0);
        if (rank < StarRailPathRank.PATH_PINNACLE.getLevel()
                || !isNihilityMarkOwnedBy(deceased, owner.getUUID())) {
            return;
        }

        List<Monster> nearby = deceased.level().getEntitiesOfClass(
                Monster.class,
                deceased.getBoundingBox().inflate(NIHILITY_FINAL_RADIUS),
                monster -> monster.isAlive() && monster != deceased);
        if (nearby.isEmpty()) {
            return;
        }

        StarRailPathMessages.send(owner, StarRailPath.NIHILITY, Component.translatable(
                "message.starrail_sim.nihility_final"));
        for (Monster target : nearby) {
            applyNihilityMark(target, owner, rank, false);
            target.getPersistentData().putBoolean(FINAL_BURST_GUARD_KEY, true);
            boolean hurt = target.hurt(owner.damageSources().magic(), NIHILITY_FINAL_DAMAGE);
            target.getPersistentData().remove(FINAL_BURST_GUARD_KEY);
            if (hurt) {
                StarRailNetwork.sendDamageNumber(owner, target,
                        NIHILITY_FINAL_DAMAGE, false);
            }
        }
    }

    public static boolean hasNihilityMark(LivingEntity target) {
        return hasDamageOverTime(target, NIHILITY_MARK_KEY);
    }

    public static boolean hasSilkThread(LivingEntity target) {
        return target != null && hasDamageOverTime(target, "only_wait_silk_thread");
    }

    public static boolean isNihilityMarkOwnedBy(LivingEntity target, UUID owner) {
        CompoundTag dot = getDamageOverTime(target, NIHILITY_MARK_KEY);
        return dot != null && dot.hasUUID("owner")
                && dot.getUUID("owner").equals(owner);
    }

    public static ServerPlayer getNihilityMarkOwner(LivingEntity target) {
        CompoundTag dot = getDamageOverTime(target, NIHILITY_MARK_KEY);
        if (dot == null || !dot.hasUUID("owner")
                || !(target.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getServer().getPlayerList().getPlayer(dot.getUUID("owner"));
    }

    /** Adds a named DoT schedule so later paths and light cones can reuse it. */
    public static void applyDamageOverTime(LivingEntity target, String key,
                                           ServerPlayer owner, float damage,
                                           int durationTicks, int intervalTicks) {
        CompoundTag root = target.getPersistentData();
        CompoundTag dots = root.getCompound(DOTS_TAG);
        CompoundTag dot = new CompoundTag();
        dot.putInt(REMAINING_TICKS, Math.max(1, durationTicks));
        dot.putInt(NEXT_DAMAGE_TICKS, Math.max(1, intervalTicks));
        dot.putFloat(DAMAGE, Math.max(0.0F, damage));
        dot.putInt(INTERVAL, Math.max(1, intervalTicks));
        if (owner != null) {
            dot.putUUID("owner", owner.getUUID());
        }
        dots.put(key, dot);
        root.put(DOTS_TAG, dots);
    }

    private static boolean hasDamageOverTime(LivingEntity target, String key) {
        CompoundTag dot = getDamageOverTime(target, key);
        return dot != null && dot.getInt(REMAINING_TICKS) > 0;
    }

    private static CompoundTag getDamageOverTime(LivingEntity target, String key) {
        CompoundTag dots = target.getPersistentData().getCompound(DOTS_TAG);
        if (!dots.contains(key)) {
            return null;
        }
        return dots.getCompound(key);
    }

    private static ServerPlayer getDamageOverTimeOwner(LivingEntity target, CompoundTag dot) {
        if (!dot.hasUUID("owner") || !(target.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getServer().getPlayerList().getPlayer(dot.getUUID("owner"));
    }

    /** Runs all active DoT schedules once per server-side living tick. */
    public static void tick(LivingEntity target) {
        if (target.level().isClientSide()) {
            return;
        }

        CompoundTag root = target.getPersistentData();
        if (!root.contains(DOTS_TAG)) {
            return;
        }

        CompoundTag dots = root.getCompound(DOTS_TAG);
        List<String> keys = new ArrayList<>(dots.getAllKeys());
        for (String key : keys) {
            CompoundTag dot = dots.getCompound(key);
            int remaining = dot.getInt(REMAINING_TICKS) - 1;
            if (remaining <= 0) {
                dots.remove(key);
                continue;
            }

            int nextDamage = dot.getInt(NEXT_DAMAGE_TICKS) - 1;
            if (nextDamage <= 0 && dot.getFloat(DAMAGE) > 0.0F && target.isAlive()) {
                float damage = dot.getFloat(DAMAGE);
                ServerPlayer owner = getDamageOverTimeOwner(target, dot);
                if (target.hurt(target.damageSources().magic(), damage)
                        && owner != null) {
                    StarRailNetwork.sendDamageNumber(owner, target, damage, false);
                }
                nextDamage = Math.max(1, dot.getInt(INTERVAL));
            }
            dot.putInt(REMAINING_TICKS, remaining);
            dot.putInt(NEXT_DAMAGE_TICKS, nextDamage);
            dots.put(key, dot);
        }

        if (dots.isEmpty()) {
            root.remove(DOTS_TAG);
        } else {
            root.put(DOTS_TAG, dots);
        }
    }
}
