package com.starrail.sim;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

final class DoomsdayBeastEffects {

    private DoomsdayBeastEffects() {}

    static void impact(ServerLevel level, Vec3 target, double radius, int action) {
        Vec3 center = target.add(0, .22, 0);
        if (action == 1) {
            cloud(level, ParticleTypes.ELECTRIC_SPARK, center, 24, radius * .36);
            sound(level, center, VoidrangerSounds.DOOMSDAY_SWEEP.get(), 1.5F, .86F);
        } else if (action == 2) {
            cloud(level, ParticleTypes.REVERSE_PORTAL, center, 24, .75);
            sound(level, center, VoidrangerSounds.DOOMSDAY_STRIKE.get(), 1.35F, 1.04F);
        } else if (action == DoomsdayBeast.DOUBLE_SLAM) {
            cloud(level, ParticleTypes.CRIT, center, 34, radius * .35);
            cloud(level, ParticleTypes.CAMPFIRE_COSY_SMOKE, center, 12, radius * .28);
            sound(level, center, VoidrangerSounds.DOOMSDAY_SLAM.get(), 2.1F, .88F);
        } else if (action == DoomsdayBeast.WING_GALE) {
            cloud(level, ParticleTypes.CLOUD, center, 24, radius * .42);
            sound(level, center, VoidrangerSounds.DOOMSDAY_GALE.get(), 1.65F, .92F);
        } else {
            cloud(level, ParticleTypes.END_ROD, center, 35, radius * .32);
            sound(level, center, VoidrangerSounds.DOOMSDAY_BEAM.get(), 1.75F, .92F);
        }
    }

    static void transfer(ServerLevel level, Vec3 claw) {
        cloud(level, ParticleTypes.ELECTRIC_SPARK, claw, 10, .35);
    }

    static void breakPart(ServerLevel level, Vec3 position) {
        cloud(level, ParticleTypes.ELECTRIC_SPARK, position, 32, .55);
        cloud(level, ParticleTypes.SMOKE, position, 12, .35);
        sound(level, position, VoidrangerSounds.DOOMSDAY_BREAK.get(), 1.35F, .85F);
    }

    static void sound(ServerLevel level, Vec3 position, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, position.x, position.y, position.z, sound, SoundSource.HOSTILE, volume, pitch);
    }

    private static void cloud(ServerLevel level, ParticleOptions type, Vec3 position, int count, double spread) {
        level.sendParticles(type, position.x, position.y, position.z, count, spread, spread * .4, spread, .12);
    }
}
