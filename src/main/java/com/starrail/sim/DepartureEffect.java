package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Damage and attack stacks granted by A Star Illuminates the Night Sky. */
public final class DepartureEffect extends MobEffect {
    public DepartureEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x55BFF2);
    }
}
