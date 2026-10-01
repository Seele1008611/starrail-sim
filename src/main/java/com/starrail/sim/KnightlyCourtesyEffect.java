package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Attack damage stacks granted by A Moment of Beauty when an attack is cast. */
public final class KnightlyCourtesyEffect extends MobEffect {
    public KnightlyCourtesyEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE8B84A);
    }
}
