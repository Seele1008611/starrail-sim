package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Visible status marker for the Psalm stacks. */
public final class PsalmEffect extends MobEffect {
    public PsalmEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC5DFFF);
    }
}
