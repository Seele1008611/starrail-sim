package com.starrail.sim;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/** Server-authoritative effect-hit checks shared by paths and future light cones. */
public final class StarRailEffectService {
    private static final double BASE_EFFECT_CHANCE = 0.90D;
    private static final double MIN_EFFECT_CHANCE = 0.05D;
    private static final double MAX_EFFECT_CHANCE = 1.0D;

    private StarRailEffectService() {
    }

    /**
     * Rolls whether an effect can be applied. Effect Hit Rate and Effect
     * Resistance are stored as fractions, so +10% is represented by 0.10.
     */
    public static boolean rollEffectHit(ServerPlayer owner, LivingEntity target) {
        return rollEffectHit(owner, target, BASE_EFFECT_CHANCE);
    }

    /** Rolls a custom base chance with the same hit-rate/resistance calculation. */
    public static boolean rollEffectHit(ServerPlayer owner, LivingEntity target,
                                        double baseChance) {
        if (owner == null || target == null) {
            return true;
        }

        double effectHit = StarRailAttributes.getValue(
                owner, StarRailAttributes.EFFECT_HIT_RATE, 0.0D);
        double resistance = getEffectResistance(target);
        double chance = Mth.clamp(
                baseChance + effectHit - resistance,
                MIN_EFFECT_CHANCE, MAX_EFFECT_CHANCE);
        return owner.getRandom().nextDouble() < chance;
    }

    /**
     * Rolls the defender side of the system for harmful MobEffects applied to
     * a player. Effect Resistance is a direct resistance chance here: +10%
     * means a 10% chance to reject the incoming negative effect.
     */
    public static boolean rollPlayerEffectResistance(ServerPlayer target) {
        if (target == null) {
            return false;
        }
        double resistance = Mth.clamp(
                StarRailAttributes.getValue(
                        target, StarRailAttributes.EFFECT_RESISTANCE, 0.0D),
                0.0D, 1.0D);
        if (resistance <= 0.0D || target.getRandom().nextDouble() >= resistance) {
            return false;
        }

        return true;
    }

    /** Base resistance supplied by enemy category until custom enemy stats exist. */
    public static double getEffectResistance(LivingEntity target) {
        var customResistance = target.getAttribute(
                StarRailAttributes.EFFECT_RESISTANCE.get());
        if (customResistance != null) {
            return Mth.clamp(customResistance.getValue(), 0.0D, 1.0D);
        }
        if (target.getType() == EntityType.WARDEN
                || target.getType() == EntityType.WITHER
                || target.getType() == EntityType.ENDER_DRAGON) {
            return 0.25D;
        }
        if (target.getType() == EntityType.ELDER_GUARDIAN) {
            return 0.15D;
        }
        return target.getMaxHealth() >= 40.0F ? 0.10D : 0.0D;
    }
}
