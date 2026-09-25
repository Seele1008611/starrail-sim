package com.starrail.sim;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Enhanced vulnerability status applied by Those Many Springs. */
public final class CorneredPreyEffect extends MobEffect {
    public CorneredPreyEffect() {
        super(MobEffectCategory.HARMFUL, 0x426A52);
    }
}
