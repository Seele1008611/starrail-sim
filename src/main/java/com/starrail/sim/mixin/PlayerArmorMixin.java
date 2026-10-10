package com.starrail.sim.mixin;

import com.starrail.sim.StarRailPlayerDefense;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Replace only the armor formula call, retaining bypass tags, equipment wear and later stages. */
@Mixin(LivingEntity.class)
public abstract class PlayerArmorMixin {
    @Redirect(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(FFF)F"), require = 1)
    private float starrail$armor(float damage, float armor, float toughness) {
        return (Object) this instanceof Player player
                ? StarRailPlayerDefense.damage(player, damage, armor, toughness)
                : CombatRules.getDamageAfterAbsorb(damage, armor, toughness);
    }
}
