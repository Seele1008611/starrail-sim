package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Timed self-buff from Do Not Forget Her Flame. */
public final class BurningSelfEffect extends MobEffect {
    public BurningSelfEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x7466D9);
    }
}
