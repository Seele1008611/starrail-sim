package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Temporary party buff granted by If Time Were a Flower. */
public final class EdictEffect extends MobEffect {
    public EdictEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE9B94F);
    }
}
