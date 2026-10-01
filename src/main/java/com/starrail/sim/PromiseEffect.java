package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Attack bonus stacks granted by A Price Paid in Hopes after attacking. */
public final class PromiseEffect extends MobEffect {
    public PromiseEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC69AF2);
    }
}
