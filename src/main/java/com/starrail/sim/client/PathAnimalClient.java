package com.starrail.sim.client;

import com.starrail.sim.PathAnimalEntities;
import com.starrail.sim.StarRailSimMod;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client-only renderer registration for the original Path wildlife. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class PathAnimalClient {
    private PathAnimalClient() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PathAnimalEntities.SUNNYHARVEST_DEER.get(),
                context -> renderer(context, PathAnimalModel.Species.SUN_GRAIN_DEER, 0.35F, 0.60F));
        event.registerEntityRenderer(PathAnimalEntities.STARVELVET_RAM.get(),
                context -> renderer(context, PathAnimalModel.Species.STARFLEECE_RAM, 0.44F, 0.58F));
        event.registerEntityRenderer(PathAnimalEntities.WOODLAND_FORAGER.get(),
                context -> renderer(context, PathAnimalModel.Species.WOODLAND_FORAGER, 0.50F, 0.62F));
        event.registerEntityRenderer(PathAnimalEntities.STILLTIDE_FOWL.get(),
                context -> renderer(context, PathAnimalModel.Species.MARSH_FOWL, 0.20F, 0.32F));
        event.registerEntityRenderer(PathAnimalEntities.STAR_SCREE_PACKBEAST.get(),
                context -> renderer(context, PathAnimalModel.Species.STARGRAVEL_PACKBEAST, 0.75F, 0.86F));
    }

    private static <T extends Mob> PathAnimalRenderer<T> renderer(
            net.minecraft.client.renderer.entity.EntityRendererProvider.Context context,
            PathAnimalModel.Species species, float scale, float shadowRadius) {
        return new PathAnimalRenderer<>(context, species, scale, shadowRadius);
    }
}
