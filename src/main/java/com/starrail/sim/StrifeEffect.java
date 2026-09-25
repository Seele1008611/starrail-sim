package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Temporary damage bonus from Flame of Blood, Burning the Path Ahead. */
public final class StrifeEffect extends MobEffect {
    public StrifeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xB52A2A);
    }
}
