package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Temporary critical-damage amplification from What You See Is Me. */
public final class KinglyEntertainmentEffect extends MobEffect {
    public KinglyEntertainmentEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xD83A68);
    }
}
