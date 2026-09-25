package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Vulnerability mark applied by Rain Never Stops. */
public final class AetherCodeEffect extends MobEffect {
    public AetherCodeEffect() {
        super(MobEffectCategory.HARMFUL, 0x5577FF);
    }
}
