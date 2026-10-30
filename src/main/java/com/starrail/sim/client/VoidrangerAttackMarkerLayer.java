package com.starrail.sim.client;

import com.starrail.sim.VoidrangerKind;
import com.starrail.sim.VoidrangerMob;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;

/** Disabled visual marker layer; gameplay targeting remains server-authoritative. */
public final class VoidrangerAttackMarkerLayer<T extends VoidrangerMob>
        extends RenderLayer<T, VoidrangerModel<T>> {
    public VoidrangerAttackMarkerLayer(RenderLayerParent<T, VoidrangerModel<T>> parent,
                                       VoidrangerKind kind) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        // Attack cues now use a small number of particles; do not render ground meshes or marks.
    }
}
