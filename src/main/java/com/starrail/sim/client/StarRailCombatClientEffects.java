package com.starrail.sim.client;

import com.starrail.sim.CombatVfxPacket;
import com.starrail.sim.StarRailCombatSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;

/** Dispatches local presentation for a server-confirmed combat event. */
public final class StarRailCombatClientEffects {
    private StarRailCombatClientEffects() {}

    public static void accept(CombatVfxPacket packet) {
        StarRailCombatMesh.accept(packet);
        playSound(packet);
    }

    private static void playSound(CombatVfxPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null
                || minecraft.player.distanceToSqr(packet.impact()) > 48 * 48) return;

        SoundCue cue = cue(packet.kind());
        minecraft.level.playLocalSound(packet.impact().x, packet.impact().y, packet.impact().z,
                StarRailCombatSounds.get(packet.kind()), SoundSource.PLAYERS,
                cue.volume(), cue.pitch(), false);
    }

    /** Per-action mix; the source clips are original procedural sounds bundled with the mod. */
    private static SoundCue cue(CombatVfxPacket.Kind kind) {
        return switch (kind) {
            case HUNT_STRIKE -> new SoundCue(.62F, 1.08F);
            case HUNT_REVERSE -> new SoundCue(.72F, .94F);
            case TOUGHNESS_HIT -> new SoundCue(.34F, 1.12F);
            case TOUGHNESS_BREAK -> new SoundCue(.68F, .86F);
            case PRESERVATION_WALL -> new SoundCue(.48F, .92F);
            case PRESERVATION_COUNTER -> new SoundCue(.72F, .98F);
            case DESTRUCTION_RAGE -> new SoundCue(.48F, .88F);
            case DESTRUCTION_BURN -> new SoundCue(.62F, .96F);
            case DESTRUCTION_DESPERATE -> new SoundCue(.78F, .82F);
            case ERUDITION_ECHO -> new SoundCue(.54F, 1.02F);
            case ERUDITION_FINAL -> new SoundCue(.68F, .92F);
            case NIHILITY_MARK -> new SoundCue(.42F, .92F);
            case NIHILITY_SPREAD -> new SoundCue(.50F, 1.04F);
            case NIHILITY_END -> new SoundCue(.64F, .82F);
            case HARMONY_RESONANCE -> new SoundCue(.62F, 1.00F);
            case HARMONY_AFTERGLOW -> new SoundCue(.48F, 1.08F);
            case HARMONY_CONCERT -> new SoundCue(.58F, 1.02F);
            case HARMONY_UNISON -> new SoundCue(.66F, .96F);
            case ABUNDANCE_HEAL -> new SoundCue(.55F, .92F);
            case ABUNDANCE_MANNA -> new SoundCue(.44F, 1.08F);
            case ABUNDANCE_MERCY -> new SoundCue(.72F, .88F);
            case REMEMBRANCE_RECORD -> new SoundCue(.48F, 1.08F);
            case REMEMBRANCE_ECHO -> new SoundCue(.52F, 1.12F);
            case REMEMBRANCE_ETERNAL -> new SoundCue(.68F, .94F);
            case ELATION_BURST -> new SoundCue(.56F, 1.06F);
            case ELATION_GRAND -> new SoundCue(.72F, 1.02F);
        };
    }

    private record SoundCue(float volume, float pitch) {}
}
