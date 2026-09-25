package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Attack-stack counter for The Game Is Afoot. */
public final class ColorfulFlameEffect extends MobEffect {
    public ColorfulFlameEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE64A55);
    }
}
