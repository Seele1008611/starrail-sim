package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Timed self-buff from Do Not Forget Her Flame. */
public final class BurningSelfEffect extends MobEffect {
    public BurningSelfEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x7466D9);
    }
}
