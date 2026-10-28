package com.starrail.sim.client;

import com.starrail.sim.StarveinConstructGuardian;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Client renderer for the original ruin guardian. */
public final class StarveinConstructRenderer
        extends MobRenderer<StarveinConstructGuardian, StarveinConstructModel> {
    public StarveinConstructRenderer(EntityRendererProvider.Context context) {
        super(context, new StarveinConstructModel(), 0.9F);
    }

    @Override
    public ResourceLocation getTextureLocation(StarveinConstructGuardian entity) {
        return StarveinConstructModel.texture();
    }
}
