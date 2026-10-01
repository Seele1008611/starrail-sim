package com.starrail.sim;

/**
 * 模组代码说明：自定义状态效果类，定义对应效果的属性修正、持续表现或服务端更新行为。
 */

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Temporary damage bonus from This Body as Sword. */
public final class MoonEclipseEffect extends MobEffect {
    public MoonEclipseEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x6266D9);
    }
}
