package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Self-buff granted by Reforged Remembrance. */
public final class ProphetEffect extends MobEffect {
    public ProphetEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x9A68E8);
    }
}
