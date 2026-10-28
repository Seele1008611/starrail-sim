package com.starrail.sim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Mob;

/** Shared renderer for the five accepted Path wildlife cuboid models. */
public final class PathAnimalRenderer<T extends Mob> extends MobRenderer<T, PathAnimalModel<T>> {
    private final float modelScale;

    public PathAnimalRenderer(EntityRendererProvider.Context context,
                              PathAnimalModel.Species species,
                              float modelScale,
                              float shadowRadius) {
        super(context, new PathAnimalModel<>(species), shadowRadius);
        this.modelScale = modelScale;
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTick) {
        float ageScale = entity instanceof AgeableMob ageable && ageable.isBaby() ? 0.5F : 1.0F;
        poseStack.scale(modelScale * ageScale, modelScale * ageScale, modelScale * ageScale);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return model.texture();
    }
}
