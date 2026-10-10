package com.starrail.sim;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class StarRailPlayerDefense {
    public static float damage(Player player, float damage, float vanillaArmor, float vanillaToughness) {
        double armor = Math.max(0, player.getAttributeValue(Attributes.ARMOR));
        double toughness = Math.max(0, player.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        if (!StarRailStatusConfig.EXTENDED_DEFENSE.get() || armor <= 30 && toughness <= 20)
            return CombatRules.getDamageAfterAbsorb(damage, vanillaArmor, vanillaToughness);
        double a = Math.min(armor, 30), t = Math.min(toughness, 20);
        double base = Mth.clamp(a - damage / (2 + t / 4), a * .2, 20) / 25;
        double extra = Math.max(armor - 30, 0)
                + Math.max(toughness - 20, 0) * StarRailStatusConfig.TOUGHNESS_WEIGHT.get();
        double cap = StarRailStatusConfig.DEFENSE_CAP.get(), curve = StarRailStatusConfig.DEFENSE_CURVE.get();
        double reduction = cap - (cap - base) * curve / (curve + extra);
        return (float) (damage * (1 - reduction));
    }
    private StarRailPlayerDefense() {}
}
