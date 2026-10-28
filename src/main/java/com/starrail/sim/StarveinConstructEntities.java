package com.starrail.sim;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registry for the original ruin guardian prototype. */
public final class StarveinConstructEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, StarRailSimMod.MOD_ID);

    public static final RegistryObject<EntityType<StarveinConstructGuardian>> STARVEIN_CONSTRUCT_GUARDIAN =
            ENTITY_TYPES.register("starvein_construct_guardian",
                    () -> EntityType.Builder.of(StarveinConstructGuardian::new, MobCategory.MONSTER)
                            .sized(1.65F, 3.0F)
                            .clientTrackingRange(12)
                            .updateInterval(2)
                            .build("starvein_construct_guardian"));

    private StarveinConstructEntities() {
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(StarveinConstructEntities::registerAttributes);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(STARVEIN_CONSTRUCT_GUARDIAN.get(),
                StarveinConstructGuardian.createAttributes().build());
    }
}
