package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Increases critical damage taken from attacks. */
public final class InfernoEffect extends MobEffect {
    public InfernoEffect() {
        super(MobEffectCategory.HARMFUL, 0xD94A20);
    }
}
