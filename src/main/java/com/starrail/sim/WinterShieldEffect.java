package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Temporary defensive buff from Moment of Victory. */
public final class WinterShieldEffect extends MobEffect {
    public WinterShieldEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x5B9BD8);
    }
}
