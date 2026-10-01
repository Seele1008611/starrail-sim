package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Attack stacks granted by Ninjutsu Inscription: Dazzling Evilbreaker. */
public final class ThunderEscapeEffect extends MobEffect {
    public ThunderEscapeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x54D8F2);
    }
}
