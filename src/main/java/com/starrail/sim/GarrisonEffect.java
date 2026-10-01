package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Nearby-player damage buff from Though Rivers and Mountains May Divide Us. */
public final class GarrisonEffect extends MobEffect {
    public GarrisonEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC9A95D);
    }
}
