package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** The visible, stack-based status granted by the In the Night light cone. */
public final class NightButterflyEffect extends MobEffect {
    public NightButterflyEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x2F6FFF);
    }
}
