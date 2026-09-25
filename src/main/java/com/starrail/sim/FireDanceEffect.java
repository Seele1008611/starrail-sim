package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Temporary damage bonus from Dance at Sunset. */
public final class FireDanceEffect extends MobEffect {
    public FireDanceEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE47738);
    }
}
