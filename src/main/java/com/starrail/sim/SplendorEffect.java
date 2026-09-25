package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Timed wearer and party damage buff granted after a kill. */
public final class SplendorEffect extends MobEffect {
    public SplendorEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF4CF4E);
    }
}
