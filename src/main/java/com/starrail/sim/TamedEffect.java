package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** The stack-based debuff applied to targets by Worrisome, Blissful. */
public final class TamedEffect extends MobEffect {
    public TamedEffect() {
        super(MobEffectCategory.HARMFUL, 0xD88A72);
    }
}
