package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Unlimited attack-stack effect accumulated by attacking with Night of Flowing Colors. */
public final class ChantEffect extends MobEffect {
    public ChantEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xA66CE0);
    }
}
