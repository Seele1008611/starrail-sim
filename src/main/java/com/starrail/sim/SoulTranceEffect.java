package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Damage-over-time debuff inflicted by Why Does the Ocean Sing. */
public final class SoulTranceEffect extends MobEffect {
    public SoulTranceEffect() {
        super(MobEffectCategory.HARMFUL, 0x168FA8);
    }
}
