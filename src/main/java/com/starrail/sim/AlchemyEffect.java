package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Damage and attack bonus granted by Life Should Be Cast to Flames after a break. */
public final class AlchemyEffect extends MobEffect {
    public AlchemyEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF08A24);
    }
}
