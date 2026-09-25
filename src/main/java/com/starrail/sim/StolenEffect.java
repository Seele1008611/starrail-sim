package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Damage-vulnerability status applied after breaking enemy toughness. */
public final class StolenEffect extends MobEffect {
    public StolenEffect() {
        super(MobEffectCategory.HARMFUL, 0xEABF46);
    }
}
