package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Damage-up status granted by Starlight for Long Nights. */
public final class StarlightNightEffect extends MobEffect {
    public StarlightNightEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x9D79E8);
    }
}
