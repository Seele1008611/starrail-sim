package com.starrail.sim.mixin;

import com.starrail.sim.StarRailPlayerDefense;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies the extended player armor curve after other mods finish armor calculation. */
@Mixin(LivingEntity.class)
public abstract class PlayerArmorMixin {
    @Inject(method = "getDamageAfterArmorAbsorb", at = @At("RETURN"), cancellable = true,
            require = 0)
    private void starrail$armor(DamageSource source, float damage,
                                CallbackInfoReturnable<Float> callback) {
        if (source.is(DamageTypeTags.BYPASSES_ARMOR)) return;
        if ((Object) this instanceof Player player
                && StarRailPlayerDefense.shouldUseExtendedFormula(player)) {
            float armor = (float) player.getAttributeValue(
                    net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);
            float toughness = (float) player.getAttributeValue(
                    net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
            callback.setReturnValue(StarRailPlayerDefense.damage(
                    player, damage, armor, toughness));
        }
    }
}
