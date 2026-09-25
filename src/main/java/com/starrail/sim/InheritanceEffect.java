package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Timed party-wide buffs granted by But the Battle Isn't Over. */
public final class InheritanceEffect extends MobEffect {
    public InheritanceEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x83C8D8);
    }
}
