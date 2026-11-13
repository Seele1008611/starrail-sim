package com.starrail.sim;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Owned secondary damage credits kills without re-entering the direct-attack pipeline. */
public final class StarRailOwnedDamage {
    private static final String ACTIVE="starrail_active_damage";
    public static boolean isActive(ServerPlayer player) {return player.getPersistentData().getBoolean(ACTIVE);}
    private static final String SECONDARY = "starrail_secondary_damage";
    private StarRailOwnedDamage() {}
    public static boolean isSecondary(ServerPlayer player) { return player.getPersistentData().getBoolean(SECONDARY); }

    /** Active skill snapshot: ordinary armour mitigation, without recursive direct-hit procs. */
    public static boolean activeHurt(ServerPlayer owner,LivingEntity target,float amount) {
        boolean previous=isSecondary(owner),active=isActive(owner);owner.getPersistentData().putBoolean(SECONDARY,true);owner.getPersistentData().putBoolean(ACTIVE,true);
        try {return target.hurt(owner.damageSources().playerAttack(owner),amount);}
        finally {owner.getPersistentData().putBoolean(SECONDARY,previous);owner.getPersistentData().putBoolean(ACTIVE,active);}
    }

    /** Distinct supplementary hits must not be swallowed by the triggering hit's invulnerability window. */
    public static boolean activeExtraHurt(ServerPlayer owner,LivingEntity target,float amount) {
        boolean previous=isSecondary(owner),active=isActive(owner);owner.getPersistentData().putBoolean(SECONDARY,true);owner.getPersistentData().putBoolean(ACTIVE,true);
        try {
            var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                    new net.minecraft.resources.ResourceLocation("starrail_sim","active_skill"));
            var type=owner.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(key);
            return target.hurt(new net.minecraft.world.damagesource.DamageSource(type,owner,owner),amount);
        } finally {owner.getPersistentData().putBoolean(SECONDARY,previous);owner.getPersistentData().putBoolean(ACTIVE,active);}
    }

    public static boolean hurt(ServerPlayer owner, LivingEntity target, float amount, boolean inheritsDirectMultiplier) {
        if (owner == null) return target.hurt(target.damageSources().magic(), amount);
        boolean previous = isSecondary(owner);
        owner.getPersistentData().putBoolean(SECONDARY, true);
        try {
            float scaled = inheritsDirectMultiplier ? amount : amount * StarRailDestructionService.sustainedMultiplier(owner);
            return target.hurt(owner.damageSources().indirectMagic(owner, owner), scaled);
        } finally { owner.getPersistentData().putBoolean(SECONDARY, previous); }
    }
}
