package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Damage amplification stacks granted by Before Dawn after attacking. */
public final class DreamBodyEffect extends MobEffect {
    public DreamBodyEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8D63E8);
    }
}
