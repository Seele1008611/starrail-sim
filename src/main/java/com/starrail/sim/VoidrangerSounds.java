package com.starrail.sim;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registers the custom short cues used by the four Path voidrangers. */
public final class VoidrangerSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, StarRailSimMod.MOD_ID);

    public static final RegistryObject<SoundEvent> REAVER_AMBIENT = sound("voidranger_reaver_ambient");
    public static final RegistryObject<SoundEvent> REAVER_HURT = sound("voidranger_reaver_hurt");
    public static final RegistryObject<SoundEvent> REAVER_DEATH = sound("voidranger_reaver_death");
    public static final RegistryObject<SoundEvent> REAVER_MELEE = sound("voidranger_reaver_melee");
    public static final RegistryObject<SoundEvent> REAVER_SPECIAL_TELEGRAPH =
            sound("voidranger_reaver_special_telegraph");
    public static final RegistryObject<SoundEvent> REAVER_SPECIAL_HIT =
            sound("voidranger_reaver_special_hit");

    public static final RegistryObject<SoundEvent> DISTORTER_AMBIENT = sound("voidranger_distorter_ambient");
    public static final RegistryObject<SoundEvent> DISTORTER_HURT = sound("voidranger_distorter_hurt");
    public static final RegistryObject<SoundEvent> DISTORTER_DEATH = sound("voidranger_distorter_death");
    public static final RegistryObject<SoundEvent> DISTORTER_MELEE = sound("voidranger_distorter_melee");
    public static final RegistryObject<SoundEvent> DISTORTER_SPECIAL_TELEGRAPH =
            sound("voidranger_distorter_special_telegraph");
    public static final RegistryObject<SoundEvent> DISTORTER_SPECIAL_HIT =
            sound("voidranger_distorter_special_hit");

    public static final RegistryObject<SoundEvent> TRAMPLER_AMBIENT = sound("voidranger_trampler_ambient");
    public static final RegistryObject<SoundEvent> TRAMPLER_HURT = sound("voidranger_trampler_hurt");
    public static final RegistryObject<SoundEvent> TRAMPLER_DEATH = sound("voidranger_trampler_death");
    public static final RegistryObject<SoundEvent> TRAMPLER_MELEE = sound("voidranger_trampler_melee");
    public static final RegistryObject<SoundEvent> TRAMPLER_SPECIAL_TELEGRAPH =
            sound("voidranger_trampler_special_telegraph");
    public static final RegistryObject<SoundEvent> TRAMPLER_SPECIAL_HIT =
            sound("voidranger_trampler_special_hit");

    public static final RegistryObject<SoundEvent> ELIMINATOR_AMBIENT = sound("voidranger_eliminator_ambient");
    public static final RegistryObject<SoundEvent> ELIMINATOR_HURT = sound("voidranger_eliminator_hurt");
    public static final RegistryObject<SoundEvent> ELIMINATOR_DEATH = sound("voidranger_eliminator_death");
    public static final RegistryObject<SoundEvent> ELIMINATOR_MELEE = sound("voidranger_eliminator_melee");
    public static final RegistryObject<SoundEvent> ELIMINATOR_SPECIAL_TELEGRAPH =
            sound("voidranger_eliminator_special_telegraph");
    public static final RegistryObject<SoundEvent> ELIMINATOR_SPECIAL_HIT =
            sound("voidranger_eliminator_special_hit");

    private VoidrangerSounds() {
    }

    /** Attaches all voidranger sound events to the mod event bus. */
    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }

    private static RegistryObject<SoundEvent> sound(String name) {
        ResourceLocation id = new ResourceLocation(StarRailSimMod.MOD_ID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
