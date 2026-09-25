package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Temporary damage-and-attack buff from Pure Thought Baptism. */
public final class DebateEffect extends MobEffect {
    public DebateEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8D5A8B);
    }
}
