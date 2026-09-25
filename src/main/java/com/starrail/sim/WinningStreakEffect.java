package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Stack display for the winning streak granted by Welcome to Galactic City. */
public final class WinningStreakEffect extends MobEffect {
    public WinningStreakEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x7667F5);
    }
}
