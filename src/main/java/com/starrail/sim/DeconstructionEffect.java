package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Damage and attack stacks granted by Towards the Unanswerable after attacking. */
public final class DeconstructionEffect extends MobEffect {
    public DeconstructionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x54D8F2);
    }
}
