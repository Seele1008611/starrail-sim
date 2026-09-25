package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Nearby-player damage buff from Though Rivers and Mountains May Divide Us. */
public final class GarrisonEffect extends MobEffect {
    public GarrisonEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC9A95D);
    }
}
