package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Short-lived damage buff from Love Is Eternal. */
public final class BlankEffect extends MobEffect {
    public BlankEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xB7EFFF);
    }
}
