package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Attack and damage bonus granted by The Sparkle of Stars after a kill. */
public final class ShiningCrownEffect extends MobEffect {
    public ShiningCrownEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF4C430);
    }
}
