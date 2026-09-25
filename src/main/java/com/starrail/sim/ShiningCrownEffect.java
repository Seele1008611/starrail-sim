package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Attack and damage bonus granted by The Sparkle of Stars after a kill. */
public final class ShiningCrownEffect extends MobEffect {
    public ShiningCrownEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF4C430);
    }
}
