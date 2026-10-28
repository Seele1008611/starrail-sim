package com.starrail.sim.client;

import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.StarveinConstructEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Registers the original guardian renderer on the physical client only. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class StarveinConstructClient {
    private StarveinConstructClient() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(StarveinConstructEntities.STARVEIN_CONSTRUCT_GUARDIAN.get(),
                StarveinConstructRenderer::new);
    }
}
