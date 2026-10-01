package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Nihility damage-over-time debuff applied by Only Wait. */
public final class SilkThreadEffect extends MobEffect {
    public SilkThreadEffect() {
        super(MobEffectCategory.HARMFUL, 0xB84F9D);
    }
}
