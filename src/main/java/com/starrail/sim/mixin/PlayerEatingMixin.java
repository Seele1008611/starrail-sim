package com.starrail.sim.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Treat food as always edible on both logical sides while using the normal consumption pipeline. */
@Mixin(Player.class)
public abstract class PlayerEatingMixin {
    @ModifyVariable(method = "canEat(Z)Z", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private boolean starrail$allowEatingWhenFull(boolean ignoreHunger) {
        return true;
    }
}
