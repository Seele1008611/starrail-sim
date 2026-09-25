package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Damage-taken vulnerability from Fate Never Fair. */
public final class ChipsEffect extends MobEffect {
    public ChipsEffect() {
        super(MobEffectCategory.HARMFUL, 0xC14B43);
    }
}
