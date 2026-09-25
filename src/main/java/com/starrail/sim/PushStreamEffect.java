package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Beneficial stacking status granted by A Dazzling World. */
public final class PushStreamEffect extends MobEffect {
    public PushStreamEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF05BCB);
    }
}
