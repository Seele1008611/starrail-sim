package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Temporary damage bonus from The Unreachable Side. */
public final class NoRetreatEffect extends MobEffect {
    public NoRetreatEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x3E5AA8);
    }
}
