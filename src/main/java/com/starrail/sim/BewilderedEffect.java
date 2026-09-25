package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Damage-vulnerability status applied by Lies in the Wind. */
public final class BewilderedEffect extends MobEffect {
    public BewilderedEffect() {
        super(MobEffectCategory.HARMFUL, 0x5269D5);
    }
}
