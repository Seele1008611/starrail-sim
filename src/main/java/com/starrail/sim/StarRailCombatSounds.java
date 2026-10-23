package com.starrail.sim;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/** Registers the mod's original one-shot audio cues for combat feedback. */
public final class StarRailCombatSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, StarRailSimMod.MOD_ID);

    private static final Map<CombatVfxPacket.Kind, RegistryObject<SoundEvent>> BY_KIND =
            new EnumMap<>(CombatVfxPacket.Kind.class);

    static {
        for (CombatVfxPacket.Kind kind : CombatVfxPacket.Kind.values()) {
            String soundId = "combat_" + kind.name().toLowerCase(Locale.ROOT);
            ResourceLocation id = new ResourceLocation(StarRailSimMod.MOD_ID, soundId);
            BY_KIND.put(kind, SOUNDS.register(soundId,
                    () -> SoundEvent.createVariableRangeEvent(id)));
        }
    }

    private StarRailCombatSounds() {}

    public static SoundEvent get(CombatVfxPacket.Kind kind) {
        RegistryObject<SoundEvent> sound = BY_KIND.get(kind);
        if (sound == null) {
            throw new IllegalArgumentException("No combat sound registered for " + kind);
        }
        return sound.get();
    }
}
