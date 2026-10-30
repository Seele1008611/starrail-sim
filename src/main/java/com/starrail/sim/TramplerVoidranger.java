package com.starrail.sim;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;

/** Heavy elite whose only special attack is a stationary radial stomp. */
public final class TramplerVoidranger extends VoidrangerMob {
    private static final double STOMP_RADIUS = 3.5D;
    private static final double HORIZONTAL_KNOCKBACK = 0.6D;
    private static final double VERTICAL_LIFT = 0.12D;

    public TramplerVoidranger(EntityType<? extends TramplerVoidranger> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 72.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D);
    }

    @Override
    protected int windupTicks() {
        return 28;
    }

    @Override
    protected int executionTicks() {
        return 2;
    }

    @Override
    protected int recoveryTicks() {
        return 18;
    }

    @Override
    protected int cooldownTicks() {
        return 60;
    }

    @Override
    protected double specialAttackRange() {
        return 5.5D;
    }

    @Override
    protected SoundEvent ambientSound() {
        return VoidrangerSounds.TRAMPLER_AMBIENT.get();
    }

    @Override
    protected SoundEvent hurtSound() {
        return VoidrangerSounds.TRAMPLER_HURT.get();
    }

    @Override
    protected SoundEvent deathSound() {
        return VoidrangerSounds.TRAMPLER_DEATH.get();
    }

    @Override
    protected SoundEvent meleeAttackSound() {
        return VoidrangerSounds.TRAMPLER_MELEE.get();
    }

    @Override
    protected boolean hasBaseMeleeAttack() {
        return true;
    }

    @Override
    protected void onSpecialAttackStart(LivingEntity target) {
        // The stomp is centered under the trampler; the entity does not charge.
        playCombatSound(VoidrangerSounds.TRAMPLER_SPECIAL_TELEGRAPH.get(), 0.62F, 0.86F);
        sendCombatParticles(ParticleTypes.CLOUD, position().add(0.0D, 0.16D, 0.0D),
                4, 0.22D, 0.04D, 0.22D, 0.01D);
    }

    @Override
    protected void onSpecialAttackTelegraphTick(int phaseTick, LivingEntity target) {
        if (phaseTick == 21) {
            sendCombatParticles(ParticleTypes.CLOUD, position().add(0.0D, 0.2D, 0.0D),
                    3, 0.2D, 0.04D, 0.2D, 0.012D);
        }
    }

    @Override
    protected void onSpecialAttackExecutionTick(int phaseTick, LivingEntity target) {
        if (phaseTick == 1) {
            sendCombatParticles(ParticleTypes.CLOUD, position().add(0.0D, 0.12D, 0.0D),
                    2, 0.14D, 0.025D, 0.14D, 0.008D);
            return;
        }
        if (phaseTick != 0) return;
        playCombatSound(VoidrangerSounds.TRAMPLER_SPECIAL_HIT.get(), 0.76F, 0.9F);
        sendCombatParticles(ParticleTypes.POOF, position().add(0.0D, 0.2D, 0.0D),
                5, 0.28D, 0.08D, 0.28D, 0.018D);
        for (Player player : playersInsideGroundRadius(position(), STOMP_RADIUS, 2.0D)) {
            if (!hitPlayer(player, 5.0F)) continue;
            Vec3 outward = new Vec3(player.getX() - getX(), 0.0D, player.getZ() - getZ());
            double length = outward.horizontalDistance();
            if (length < 1.0E-4D) {
                outward = lockedHorizontalDirection();
                length = 1.0D;
            }
            player.push(outward.x / length * HORIZONTAL_KNOCKBACK, VERTICAL_LIFT,
                    outward.z / length * HORIZONTAL_KNOCKBACK);
        }
    }
}
