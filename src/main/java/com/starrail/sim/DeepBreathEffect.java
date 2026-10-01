package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Stacking attack bonus granted by Night of Fright. */
public final class DeepBreathEffect extends MobEffect {
    public DeepBreathEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x58C8B3);
    }
}
