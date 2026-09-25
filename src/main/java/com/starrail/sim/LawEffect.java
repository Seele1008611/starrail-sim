package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Temporary party buff granted by An Age Etched in Golden Blood. */
public final class LawEffect extends MobEffect {
    public LawEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xD9A63E);
    }
}
