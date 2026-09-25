package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Temporary nearby-player damage buff from She Has Already Closed Her Eyes. */
public final class VisionEffect extends MobEffect {
    public VisionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x7E83D6);
    }
}
