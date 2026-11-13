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
    public static final RegistryObject<SoundEvent> GEM_LIZARD = sound("gem_lizard_call");
    public static final RegistryObject<SoundEvent> PACKBEAST_AMBIENT = sound("star_scree_packbeast_ambient");
    public static final RegistryObject<SoundEvent> PACKBEAST_HURT = sound("star_scree_packbeast_hurt");
    public static final RegistryObject<SoundEvent> PACKBEAST_DEATH = sound("star_scree_packbeast_death");

    public static final RegistryObject<SoundEvent> DAILY_DEER_STEP = sound("animal_daily_deer_step");
    public static final RegistryObject<SoundEvent> DAILY_RAM_STEP = sound("animal_daily_ram_step");
    public static final RegistryObject<SoundEvent> DAILY_FORAGER_STEP = sound("animal_daily_forager_step");
    public static final RegistryObject<SoundEvent> DAILY_FOWL_STEP = sound("animal_daily_fowl_step");
    public static final RegistryObject<SoundEvent> DAILY_PACK_STEP = sound("animal_daily_pack_step");
    public static final RegistryObject<SoundEvent> DAILY_PACK_GALLOP = sound("animal_daily_pack_gallop");
    public static final RegistryObject<SoundEvent> DAILY_PACK_EAT = sound("animal_daily_pack_eat");
    public static final RegistryObject<SoundEvent> DAILY_PACK_ANGRY = sound("animal_daily_pack_angry");
    public static final RegistryObject<SoundEvent> DAILY_PACK_BREATHE = sound("animal_daily_pack_breathe");
    public static final RegistryObject<SoundEvent> DAILY_PACK_JUMP = sound("animal_daily_pack_jump");
    public static final RegistryObject<SoundEvent> DAILY_PACK_LAND = sound("animal_daily_pack_land");
    public static final RegistryObject<SoundEvent> DAILY_FOWL_EGG = sound("animal_daily_fowl_egg");

    private PathAnimalSounds() {
    }
    public static final RegistryObject<SoundEvent> LOTUS_OTTER=sound("lotus_otter_voice");
    public static final RegistryObject<SoundEvent> ROCK_CRAB=sound("rockdwelling_crab_voice");
    public static final RegistryObject<SoundEvent> ORANGE_FINCH=sound("duskorange_finch_voice");
    public static final RegistryObject<SoundEvent> DUSK_FISH=sound("duskred_fish_flop");

    private static RegistryObject<SoundEvent> sound(String id) {
        ResourceLocation location = new ResourceLocation(StarRailSimMod.MOD_ID, id);
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(location));
    }
}
