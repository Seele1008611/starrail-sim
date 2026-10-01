package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Stack display for the golden brocade woven by the light cone. */
public final class BrocadeEffect extends MobEffect {
    public BrocadeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE6B83F);
    }
}
