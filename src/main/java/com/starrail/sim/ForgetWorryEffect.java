package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Damage-taken debuff applied by The Scent Remains True. */
public final class ForgetWorryEffect extends MobEffect {
    public ForgetWorryEffect() {
        super(MobEffectCategory.HARMFUL, 0xD6A548);
    }
}
