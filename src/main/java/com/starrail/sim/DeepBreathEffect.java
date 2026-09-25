package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Stacking attack bonus granted by Night of Fright. */
public final class DeepBreathEffect extends MobEffect {
    public DeepBreathEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x58C8B3);
    }
}
