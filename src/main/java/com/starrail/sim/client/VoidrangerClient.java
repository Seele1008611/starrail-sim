package com.starrail.sim.client;

import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.VoidrangerEntities;
import com.starrail.sim.VoidrangerKind;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client-only renderer registration for the stage-2 voidrangers. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class VoidrangerClient {
    private VoidrangerClient() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(VoidrangerEntities.VOIDRANGER_REAVER.get(),
                context -> new VoidrangerRenderer<>(context, VoidrangerKind.REAVER, 0.62F));
        event.registerEntityRenderer(VoidrangerEntities.VOIDRANGER_DISTORTER.get(),
                context -> new VoidrangerRenderer<>(context, VoidrangerKind.DISTORTER, 0.62F));
        event.registerEntityRenderer(VoidrangerEntities.VOIDRANGER_TRAMPLER.get(),
                context -> new VoidrangerRenderer<>(context, VoidrangerKind.TRAMPLER, 0.95F));
        event.registerEntityRenderer(VoidrangerEntities.VOIDRANGER_ELIMINATOR.get(),
                context -> new VoidrangerRenderer<>(context, VoidrangerKind.ELIMINATOR, 0.90F));
        event.registerEntityRenderer(VoidrangerEntities.VOIDRANGER_BOLT.get(), VoidrangerBoltRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(VoidrangerBoltModel.LAYER_LOCATION,
                VoidrangerBoltModel::createBodyLayer);
    }
}
