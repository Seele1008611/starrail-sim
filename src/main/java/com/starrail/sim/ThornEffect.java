package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Short-lived attack bonus from Echoes of the Coffin. */
public final class ThornEffect extends MobEffect {
    public ThornEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xA52F48);
    }
}
