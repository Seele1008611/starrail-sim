package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Temporary damage bonus from Something Irreplaceable. */
public final class FamilyEffect extends MobEffect {
    public FamilyEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xB84A4A);
    }
}
