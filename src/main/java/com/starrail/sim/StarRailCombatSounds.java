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

    public static final RegistryObject<SoundEvent> GUARDIAN_CORE = guardian("core");
    public static final RegistryObject<SoundEvent> GUARDIAN_ATTACK = guardian("attack");
    public static final RegistryObject<SoundEvent> GUARDIAN_SLAM = guardian("slam");
    public static final RegistryObject<SoundEvent> GUARDIAN_HURT = guardian("hurt");
    public static final RegistryObject<SoundEvent> GUARDIAN_DEATH = guardian("death");
    public static final RegistryObject<SoundEvent> GUARDIAN_STEP = guardian("step");

    private static RegistryObject<SoundEvent> guardian(String name) {
        String id = "guardian_" + name;
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(StarRailSimMod.MOD_ID, id)));
    }

    private static final Map<StarRailPath, RegistryObject<SoundEvent>> ACTIVE = new EnumMap<>(StarRailPath.class);

    private static final Map<CombatVfxPacket.Kind, RegistryObject<SoundEvent>> BY_KIND =
            new EnumMap<>(CombatVfxPacket.Kind.class);

    static {
        for (StarRailPath path : StarRailPath.values()) {
            if (!path.isRealPath()) continue;
            String id = "active_" + path.getId();
            ACTIVE.put(path, SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(StarRailSimMod.MOD_ID, id))));
        }
        for (CombatVfxPacket.Kind kind : CombatVfxPacket.Kind.values()) {
            if(kind==CombatVfxPacket.Kind.PATH_BASIC)continue;
            String soundId = "combat_" + kind.name().toLowerCase(Locale.ROOT);
            ResourceLocation id = new ResourceLocation(StarRailSimMod.MOD_ID, soundId);
            BY_KIND.put(kind, SOUNDS.register(soundId,
                    () -> SoundEvent.createVariableRangeEvent(id)));
        }
    }

    private StarRailCombatSounds() {}

    public static SoundEvent active(StarRailPath path) { return ACTIVE.get(path).get(); }

    public static SoundEvent get(CombatVfxPacket.Kind kind) {
        RegistryObject<SoundEvent> sound = BY_KIND.get(kind);
        if (sound == null) {
            throw new IllegalArgumentException("No combat sound registered for " + kind);
        }
        return sound.get();
    }
}
