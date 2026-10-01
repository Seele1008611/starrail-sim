package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Timed wearer and party damage buff granted after a kill. */
public final class SplendorEffect extends MobEffect {
    public SplendorEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF4CF4E);
    }
}
