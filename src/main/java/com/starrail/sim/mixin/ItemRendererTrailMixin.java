package com.starrail.sim.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starrail.sim.client.StarRailHandTrails;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Client-only hooks: final baked-model coordinates, shared by first/third-person hand rendering. */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererTrailMixin {
    @Inject(method="renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",at=@At("HEAD"))
    private void starrail$begin(LivingEntity entity,ItemStack stack,ItemDisplayContext display,boolean left,PoseStack poses,MultiBufferSource buffers,Level level,int light,int overlay,int seed,CallbackInfo ci) {
        StarRailHandTrails.begin(entity,stack,display,left);
    }
    @Inject(method="renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",at=@At("RETURN"))
    private void starrail$end(LivingEntity entity,ItemStack stack,ItemDisplayContext display,boolean left,PoseStack poses,MultiBufferSource buffers,Level level,int light,int overlay,int seed,CallbackInfo ci) {
        StarRailHandTrails.end();
    }
    @Inject(method="renderModelLists",at=@At("HEAD"))
    private void starrail$sample(BakedModel model,ItemStack stack,int light,int overlay,PoseStack poses,VertexConsumer vertices,CallbackInfo ci) {
        StarRailHandTrails.capture(poses);
    }
}
