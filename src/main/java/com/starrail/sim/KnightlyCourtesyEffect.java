package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Attack damage stacks granted by A Moment of Beauty when an attack is cast. */
public final class KnightlyCourtesyEffect extends MobEffect {
    public KnightlyCourtesyEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE8B84A);
    }
}
