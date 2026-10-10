package com.starrail.sim;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Owned secondary damage credits kills without re-entering the direct-attack pipeline. */
public final class StarRailOwnedDamage {
    private static final String SECONDARY = "starrail_secondary_damage";
    private StarRailOwnedDamage() {}
    public static boolean isSecondary(ServerPlayer player) { return player.getPersistentData().getBoolean(SECONDARY); }

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
