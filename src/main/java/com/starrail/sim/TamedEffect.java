package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** The stack-based debuff applied to targets by Worrisome, Blissful. */
public final class TamedEffect extends MobEffect {
    public TamedEffect() {
        super(MobEffectCategory.HARMFUL, 0xD88A72);
    }
}
