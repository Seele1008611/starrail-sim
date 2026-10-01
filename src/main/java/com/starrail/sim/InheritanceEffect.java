package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Timed party-wide buffs granted by But the Battle Isn't Over. */
public final class InheritanceEffect extends MobEffect {
    public InheritanceEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x83C8D8);
    }
}
