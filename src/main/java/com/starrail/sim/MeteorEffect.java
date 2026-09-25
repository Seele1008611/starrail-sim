package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Damage amplification granted after breaking an enemy with Galaxy Railway's Night. */
public final class MeteorEffect extends MobEffect {
    public MeteorEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x4CD8FF);
    }
}
