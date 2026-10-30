package com.starrail.sim;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;

/** Ranged voidranger that fires a straight, non-homing bolt at a fixed marked block. */
public final class EliminatorVoidranger extends VoidrangerMob {
    private static final int ATTACK_COOLDOWN_TICKS = 80;
    private static final double PROJECTILE_SPEED = 0.6D;

    public EliminatorVoidranger(EntityType<? extends EliminatorVoidranger> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.ATTACK_DAMAGE, 4.5D);
    }

    @Override
    protected int windupTicks() {
        return 24;
    }

    @Override
    protected int executionTicks() {
        return 4;
    }

    @Override
    protected int recoveryTicks() {
        return 12;
    }

    @Override
    protected int cooldownTicks() {
        return ATTACK_COOLDOWN_TICKS;
    }

    @Override
    protected double specialAttackRange() {
        return 24.0D;
    }

    @Override
    protected SoundEvent ambientSound() {
        return VoidrangerSounds.ELIMINATOR_AMBIENT.get();
    }

    @Override
    protected SoundEvent hurtSound() {
        return VoidrangerSounds.ELIMINATOR_HURT.get();
    }

    @Override
    protected SoundEvent deathSound() {
        return VoidrangerSounds.ELIMINATOR_DEATH.get();
    }

    @Override
    protected SoundEvent meleeAttackSound() {
        return VoidrangerSounds.ELIMINATOR_MELEE.get();
    }

    @Override
    protected boolean hasBaseMeleeAttack() {
        return true;
    }

    @Override
    protected void onSpecialAttackStart(LivingEntity target) {
        setAttackMark(target.position());
        playCombatSound(VoidrangerSounds.ELIMINATOR_SPECIAL_TELEGRAPH.get(), 0.72F, 0.9F);
        Vec3 mark = getAttackMark();
        sendCombatParticles(ParticleTypes.END_ROD, mark.add(0.0D, 0.15D, 0.0D),
                2, 0.06D, 0.04D, 0.06D, 0.008D);
    }

    @Override
    protected void onSpecialAttackTelegraphTick(int phaseTick, LivingEntity target) {
        if (phaseTick != 20 || !hasAttackMark()) return;
        Vec3 mark = getAttackMark();
        sendCombatParticles(ParticleTypes.END_ROD, mark.add(0.0D, 0.15D, 0.0D),
                2, 0.06D, 0.04D, 0.06D, 0.008D);
    }

    @Override
    protected void onSpecialAttackExecutionTick(int phaseTick, LivingEntity target) {
        if (phaseTick != 0 || !(this.level() instanceof ServerLevel serverLevel)) return;
        Vec3 mark = getAttackMark();
        Vec3 start = position().add(0.0D, getBbHeight() * 0.68D, 0.0D);
        Vec3 destination = new Vec3(mark.x, mark.y + 0.1D, mark.z);
        Vec3 direction = destination.subtract(start).normalize();
        if (direction.lengthSqr() < 1.0E-8D) direction = lockedHorizontalDirection();

        playCombatSound(VoidrangerSounds.ELIMINATOR_SPECIAL_HIT.get(), 0.78F, 0.96F);
        sendCombatParticles(ParticleTypes.CRIT, start, 3, 0.1D, 0.1D, 0.1D, 0.04D);

        VoidrangerBoltEntity bolt = new VoidrangerBoltEntity(
                VoidrangerEntities.VOIDRANGER_BOLT.get(), this.level());
        bolt.setOwner(this);
        bolt.setPos(start.x, start.y, start.z);
        bolt.setNoGravity(true);
        bolt.setDeltaMovement(direction.scale(PROJECTILE_SPEED));
        serverLevel.addFreshEntity(bolt);
        setAttackMark(null);
    }
}
