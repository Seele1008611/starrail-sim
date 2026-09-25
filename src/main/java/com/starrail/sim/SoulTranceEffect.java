package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Damage-over-time debuff inflicted by Why Does the Ocean Sing. */
public final class SoulTranceEffect extends MobEffect {
    public SoulTranceEffect() {
        super(MobEffectCategory.HARMFUL, 0x168FA8);
    }
}
