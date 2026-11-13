package com.starrail.sim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Mob;

/** Shared renderer for the five accepted Path wildlife cuboid models. */
public final class PathAnimalRenderer<T extends Mob> extends MobRenderer<T, PathAnimalModel<T>> {

    public PathAnimalRenderer(EntityRendererProvider.Context context,
                              PathAnimalModel.Species species,
                              float shadowRadius) {
        super(context, new PathAnimalModel<>(species), shadowRadius);
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTick) {
        float ageScale = entity instanceof AgeableMob ageable && ageable.isBaby() ? 0.5F : 1.0F;
        poseStack.scale(ageScale, ageScale, ageScale);
    }

    @Override
    protected float getFlipDegrees(T entity) { return 0; }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return model.texture();
    }
}
