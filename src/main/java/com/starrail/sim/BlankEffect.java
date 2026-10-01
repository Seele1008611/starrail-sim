package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Short-lived damage buff from Love Is Eternal. */
public final class BlankEffect extends MobEffect {
    public BlankEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xB7EFFF);
    }
}
