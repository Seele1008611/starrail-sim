package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Timed self/nearby-player buff marker for The Game Is Afoot. */
public final class MaskEffect extends MobEffect {
    public MaskEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE9A83E);
    }
}
