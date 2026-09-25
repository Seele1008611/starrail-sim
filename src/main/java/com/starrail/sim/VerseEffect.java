package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Team critical-damage buff from Love Is Eternal. */
public final class VerseEffect extends MobEffect {
    public VerseEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x7B9EFF);
    }
}
