package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Party buff granted when the wearer breaks an enemy's toughness. */
public final class PlumFragranceEffect extends MobEffect {
    public PlumFragranceEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xA92F54);
    }
}
