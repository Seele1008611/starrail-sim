package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Damage amplification granted after breaking an enemy with Galaxy Railway's Night. */
public final class MeteorEffect extends MobEffect {
    public MeteorEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x4CD8FF);
    }
}
