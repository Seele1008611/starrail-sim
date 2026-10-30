package com.starrail.sim;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

/** Marks a fixed ground location and releases one server-side slowing pulse. */
public final class DistorterVoidranger extends VoidrangerMob {
    private static final double PULSE_RADIUS = 2.5D;

    public DistorterVoidranger(EntityType<? extends DistorterVoidranger> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    protected int windupTicks() {
        return 30;
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
        return 60;
    }

    @Override
    protected double specialAttackRange() {
        return 12.0D;
    }

    @Override
    protected SoundEvent ambientSound() {
        return VoidrangerSounds.DISTORTER_AMBIENT.get();
    }

    @Override
    protected SoundEvent hurtSound() {
        return VoidrangerSounds.DISTORTER_HURT.get();
    }

    @Override
    protected SoundEvent deathSound() {
        return VoidrangerSounds.DISTORTER_DEATH.get();
    }

    @Override
    protected SoundEvent meleeAttackSound() {
        return VoidrangerSounds.DISTORTER_MELEE.get();
    }

    @Override
    protected boolean hasBaseMeleeAttack() {
        return true;
    }

    @Override
    protected void onSpecialAttackStart(LivingEntity target) {
        setAttackMark(target.position());
        playCombatSound(VoidrangerSounds.DISTORTER_SPECIAL_TELEGRAPH.get(), 0.72F, 0.96F);
        Vec3 mark = getAttackMark();
        sendCombatParticles(ParticleTypes.WITCH, mark.add(0.0D, 0.18D, 0.0D),
                2, 0.08D, 0.05D, 0.08D, 0.01D);
    }

    @Override
    protected void onSpecialAttackTelegraphTick(int phaseTick, LivingEntity target) {
        if (phaseTick != 26 || !hasAttackMark()) return;
        Vec3 mark = getAttackMark();
        sendCombatParticles(ParticleTypes.WITCH, mark.add(0.0D, 0.2D, 0.0D),
                2, 0.08D, 0.05D, 0.08D, 0.01D);
    }

    @Override
    protected void onSpecialAttackExecutionTick(int phaseTick, LivingEntity target) {
        if (phaseTick != 0) return;
        Vec3 mark = getAttackMark();
        playCombatSound(VoidrangerSounds.DISTORTER_SPECIAL_HIT.get(), 0.72F, 1.02F);
        sendCombatParticles(ParticleTypes.POOF, mark.add(0.0D, 0.18D, 0.0D),
                4, 0.22D, 0.06D, 0.22D, 0.016D);
        for (Player player : playersInsideGroundRadius(mark, PULSE_RADIUS, 2.0D)) {
            if (hitPlayer(player, 3.0F)) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0), this);
            }
        }
        setAttackMark(null);
    }
}
