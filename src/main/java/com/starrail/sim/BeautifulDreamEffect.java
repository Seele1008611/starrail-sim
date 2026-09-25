package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** The temporary self-buff granted by Sleep Like the Dead. */
public final class BeautifulDreamEffect extends MobEffect {
    public BeautifulDreamEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8C78B8);
    }
}
