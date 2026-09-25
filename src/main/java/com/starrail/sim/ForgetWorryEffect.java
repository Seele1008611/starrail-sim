package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Damage-taken debuff applied by The Scent Remains True. */
public final class ForgetWorryEffect extends MobEffect {
    public ForgetWorryEffect() {
        super(MobEffectCategory.HARMFUL, 0xD6A548);
    }
}
