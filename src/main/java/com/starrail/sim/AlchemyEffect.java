package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Damage and attack bonus granted by Life Should Be Cast to Flames after a break. */
public final class AlchemyEffect extends MobEffect {
    public AlchemyEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF08A24);
    }
}
