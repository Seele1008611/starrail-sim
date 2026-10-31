package com.starrail.sim;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;

/** Close-range voidranger that commits to a locked four-tick dash and one slash. */
public final class ReaverVoidranger extends VoidrangerMob {
    private static final double DASH_STEP = 0.75D;

    public ReaverVoidranger(EntityType<? extends ReaverVoidranger> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ARMOR, 5.0D);
    }

    @Override
    protected int windupTicks() {
        return 16;
    }

    @Override
    protected int executionTicks() {
        return 4;
    }

    @Override
    protected int recoveryTicks() {
        return 8;
    }

    @Override
    protected int cooldownTicks() {
        return 0;
    }

    @Override
    protected double specialAttackRange() {
        return 7.0D;
    }

    @Override
    protected SoundEvent ambientSound() {
        return VoidrangerSounds.REAVER_AMBIENT.get();
    }

    @Override
    protected SoundEvent hurtSound() {
        return VoidrangerSounds.REAVER_HURT.get();
    }

    @Override
    protected SoundEvent deathSound() {
        return VoidrangerSounds.REAVER_DEATH.get();
    }

    @Override
    protected SoundEvent meleeAttackSound() {
        return VoidrangerSounds.REAVER_MELEE.get();
    }

    @Override
    protected boolean hasBaseMeleeAttack() {
        return true;
    }

    @Override
    protected boolean staggerOnMiss() {
        return true;
    }

    @Override
    protected int staggerTicks() {
        return 18;
    }

    @Override
    protected void onSpecialAttackStart(LivingEntity target) {
        // Facing is locked by the base state before the windup begins.
        playCombatSound(VoidrangerSounds.REAVER_SPECIAL_TELEGRAPH.get(), 0.46F, 0.9F);
        sendCombatParticles(ParticleTypes.CRIT, position().add(0.0D, 0.9D, 0.0D),
                2, 0.2D, 0.12D, 0.2D, 0.04D);
    }

    @Override
    protected void onSpecialAttackTelegraphTick(int phaseTick, LivingEntity target) {
        if (phaseTick == 5) {
            sendCombatParticles(ParticleTypes.CLOUD, position().add(0.0D, 0.12D, 0.0D),
                    2, 0.12D, 0.025D, 0.12D, 0.008D);
        }
    }

    @Override
    protected void onSpecialAttackExecutionTick(int phaseTick, LivingEntity target) {
        Vec3 step = lockedHorizontalDirection().scale(DASH_STEP);
        AABB before = getBoundingBox();
        move(MoverType.SELF, step);
        AABB swept = before.minmax(getBoundingBox()).inflate(0.35D, 0.25D, 0.35D);

        if (phaseTick == 0) {
            playCombatSound(VoidrangerSounds.REAVER_SPECIAL_HIT.get(), 0.64F, 0.94F);
            sendCombatParticles(ParticleTypes.CLOUD, position().add(0.0D, 0.12D, 0.0D),
                    3, 0.14D, 0.025D, 0.14D, 0.02D);
        }

        double actualDistanceSqr = position().subtract(before.getCenter()).horizontalDistanceSqr();
        if (this.horizontalCollision || actualDistanceSqr + 1.0E-5D < step.horizontalDistanceSqr() * 0.25D) {
            enterAttackState(ATTACK_STAGGER);
            return;
        }

        // Sequence tick 18 is phase tick 2: one server-side slash window along the swept dash path.
        if (phaseTick == 2 && target.getBoundingBox().intersects(swept)) {
            if (damageLockedTarget(target, (float) getAttributeValue(Attributes.ATTACK_DAMAGE))) {
                sendCombatParticles(ParticleTypes.CRIT, target.position().add(0.0D, 0.9D, 0.0D),
                        4, 0.16D, 0.18D, 0.16D, 0.07D);
            }
        }
    }
}
