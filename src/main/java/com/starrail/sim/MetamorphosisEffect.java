package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Persistent damage and attack bonus from Where Dreams Belong. */
public final class MetamorphosisEffect extends MobEffect {
    public MetamorphosisEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x62D5D2);
    }
}
