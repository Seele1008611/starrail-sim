package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** The stack-based status granted by the I Venture Forth to Hunt light cone. */
public final class FlowingLightEffect extends MobEffect {
    public FlowingLightEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xD6B36A);
    }
}
