package com.starrail.sim;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Original vocalizations for the five species native to the Path dimension. */
public final class PathAnimalSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, StarRailSimMod.MOD_ID);

    public static final RegistryObject<SoundEvent> DEER_AMBIENT = sound("sunnyharvest_deer_ambient");
    public static final RegistryObject<SoundEvent> DEER_HURT = sound("sunnyharvest_deer_hurt");
    public static final RegistryObject<SoundEvent> DEER_DEATH = sound("sunnyharvest_deer_death");
    public static final RegistryObject<SoundEvent> RAM_AMBIENT = sound("starvelvet_ram_ambient");
    public static final RegistryObject<SoundEvent> RAM_HURT = sound("starvelvet_ram_hurt");
    public static final RegistryObject<SoundEvent> RAM_DEATH = sound("starvelvet_ram_death");
    public static final RegistryObject<SoundEvent> FORAGER_AMBIENT = sound("woodland_forager_ambient");
    public static final RegistryObject<SoundEvent> FORAGER_HURT = sound("woodland_forager_hurt");
    public static final RegistryObject<SoundEvent> FORAGER_DEATH = sound("woodland_forager_death");
    public static final RegistryObject<SoundEvent> FOWL_AMBIENT = sound("stilltide_fowl_ambient");
    public static final RegistryObject<SoundEvent> FOWL_HURT = sound("stilltide_fowl_hurt");
    public static final RegistryObject<SoundEvent> FOWL_DEATH = sound("stilltide_fowl_death");
    public static final RegistryObject<SoundEvent> PACKBEAST_AMBIENT = sound("star_scree_packbeast_ambient");
    public static final RegistryObject<SoundEvent> PACKBEAST_HURT = sound("star_scree_packbeast_hurt");
    public static final RegistryObject<SoundEvent> PACKBEAST_DEATH = sound("star_scree_packbeast_death");

    private PathAnimalSounds() {
    }

    private static RegistryObject<SoundEvent> sound(String id) {
        ResourceLocation location = new ResourceLocation(StarRailSimMod.MOD_ID, id);
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(location));
    }
}
