package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Attack bonus stacks granted by A Price Paid in Hopes after attacking. */
public final class PromiseEffect extends MobEffect {
    public PromiseEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC69AF2);
    }
}
