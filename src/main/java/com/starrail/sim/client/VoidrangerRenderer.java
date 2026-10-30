package com.starrail.sim.client;

import com.starrail.sim.VoidrangerKind;
import com.starrail.sim.VoidrangerMob;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Renderer that uses the converted Blockbench model and synced attack marker layer. */
public final class VoidrangerRenderer<T extends VoidrangerMob>
        extends MobRenderer<T, VoidrangerModel<T>> {

    public VoidrangerRenderer(EntityRendererProvider.Context context,
                              VoidrangerKind kind, float shadowRadius) {
        super(context, new VoidrangerModel<>(kind), shadowRadius);
        addLayer(new VoidrangerAttackMarkerLayer<>(this, kind));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return this.model.texture();
    }
}
