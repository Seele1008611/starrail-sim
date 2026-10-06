package com.starrail.sim;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Registers client-only model predicates for the NBT-backed trial-token variants. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class PathTrialTokenModelProperties {
    private PathTrialTokenModelProperties() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                StarRailSimMod.PATH_TRIAL_TOKEN.get(),
                new ResourceLocation(StarRailSimMod.MOD_ID, "path"),
                (stack, level, entity, seed) -> switch (PathTrialTokenItem.getPath(stack)) {
                    case PRESERVATION -> 0.1F;
                    case DESTRUCTION -> 0.2F;
                    case HUNT -> 0.3F;
                    case ERUDITION -> 0.4F;
                    case HARMONY -> 0.5F;
                    case NIHILITY -> 0.6F;
                    case ABUNDANCE -> 0.7F;
                    case REMEMBRANCE -> 0.8F;
                    case ELATION -> 0.9F;
                    case NONE -> 0.0F;
                }));
    }
}
