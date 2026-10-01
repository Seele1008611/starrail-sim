package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Vulnerability mark applied by Rain Never Stops. */
public final class AetherCodeEffect extends MobEffect {
    public AetherCodeEffect() {
        super(MobEffectCategory.HARMFUL, 0x5577FF);
    }
}
