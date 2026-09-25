package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Temporary party buff granted by If Time Were a Flower. */
public final class EdictEffect extends MobEffect {
    public EdictEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE9B94F);
    }
}
