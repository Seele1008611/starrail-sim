package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Beneficial status granted by When She Decides to See. */
public final class BestFortuneEffect extends MobEffect {
    public BestFortuneEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x9ECFFF);
    }
}
