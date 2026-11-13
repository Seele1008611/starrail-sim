package com.starrail.sim.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starrail.sim.client.StarRailHandTrails;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hand bloom runs after all first-person hand geometry and before the HUD. */
@Mixin(GameRenderer.class)
public abstract class GameRendererTrailMixin {
    @Inject(method="renderItemInHand",at=@At("RETURN"))
    private void starrail$handBloom(PoseStack poses,Camera camera,float partial,CallbackInfo ci) {
        StarRailHandTrails.flushBloom(true);
    }
}
