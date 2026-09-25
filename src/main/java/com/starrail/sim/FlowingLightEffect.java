package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** The stack-based status granted by the I Venture Forth to Hunt light cone. */
public final class FlowingLightEffect extends MobEffect {
    public FlowingLightEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xD6B36A);
    }
}
