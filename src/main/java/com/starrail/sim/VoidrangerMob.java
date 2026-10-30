package com.starrail.sim;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/** Shared server-authoritative attack state for the four Path voidrangers. */
public abstract class VoidrangerMob extends Monster {
    public static final int ATTACK_IDLE = 0;
    public static final int ATTACK_TELEGRAPH = 1;
    public static final int ATTACK_EXECUTION = 2;
    public static final int ATTACK_RECOVERY = 3;
    public static final int ATTACK_STAGGER = 4;

    private static final EntityDataAccessor<Integer> ATTACK_STATE =
            SynchedEntityData.defineId(VoidrangerMob.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_PHASE_TICKS =
            SynchedEntityData.defineId(VoidrangerMob.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> ATTACK_MARK_X =
            SynchedEntityData.defineId(VoidrangerMob.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ATTACK_MARK_Y =
            SynchedEntityData.defineId(VoidrangerMob.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ATTACK_MARK_Z =
            SynchedEntityData.defineId(VoidrangerMob.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> HAS_ATTACK_MARK =
            SynchedEntityData.defineId(VoidrangerMob.class, EntityDataSerializers.BOOLEAN);

    private int cooldownTicks;
    private UUID lockedTargetId;
    private float lockedYaw;
    private boolean attackStartedThisTick;
    private boolean attackHit;

    protected VoidrangerMob(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACK_STATE, ATTACK_IDLE);
        this.entityData.define(ATTACK_PHASE_TICKS, 0);
        this.entityData.define(ATTACK_MARK_X, 0.0F);
        this.entityData.define(ATTACK_MARK_Y, 0.0F);
        this.entityData.define(ATTACK_MARK_Z, 0.0F);
        this.entityData.define(HAS_ATTACK_MARK, false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SpecialAttackGoal(this));
        if (hasBaseMeleeAttack()) {
            this.goalSelector.addGoal(3, new VoidrangerMeleeAttackGoal(this));
        } else {
            this.goalSelector.addGoal(3, new PursueTargetGoal(this));
        }
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public final void tick() {
        if (!this.level().isClientSide && this.cooldownTicks > 0) {
            --this.cooldownTicks;
        }
        super.tick();
        if (!this.level().isClientSide) {
            tickAttackState();
        }
    }

    private void tickAttackState() {
        if (this.attackStartedThisTick) {
            this.attackStartedThisTick = false;
            return;
        }
        int state = getAttackState();
        if (state == ATTACK_IDLE) return;

        LivingEntity target = getLockedTarget();
        if (!isAlive() || isDeadOrDying() || this.hurtTime > 0 || target == null) {
            cancelSpecialAttack();
            return;
        }

        int nextTick = getAttackPhaseTicks() + 1;
        if (state == ATTACK_TELEGRAPH) {
            if (nextTick >= windupTicks()) {
                enterAttackState(ATTACK_EXECUTION);
                onSpecialAttackExecutionTick(0, target);
            } else {
                this.entityData.set(ATTACK_PHASE_TICKS, nextTick);
                onSpecialAttackTelegraphTick(nextTick, target);
            }
            return;
        }
        if (state == ATTACK_EXECUTION) {
            if (nextTick >= executionTicks()) {
                onSpecialAttackExecutionComplete();
            } else {
                this.entityData.set(ATTACK_PHASE_TICKS, nextTick);
                onSpecialAttackExecutionTick(nextTick, target);
            }
            return;
        }

        int duration = state == ATTACK_STAGGER ? staggerTicks() : recoveryTicks();
        if (nextTick >= duration) {
            finishSpecialAttack();
        } else {
            this.entityData.set(ATTACK_PHASE_TICKS, nextTick);
        }
    }

    protected abstract int windupTicks();

    protected abstract int executionTicks();

    protected abstract int recoveryTicks();

    protected abstract int cooldownTicks();

    protected abstract double specialAttackRange();

    /** Keeps close targets available for the dedicated ordinary melee attack. */
    protected double minimumSpecialAttackRange() {
        return 2.25D;
    }

    protected abstract SoundEvent ambientSound();

    protected abstract SoundEvent hurtSound();

    protected abstract SoundEvent deathSound();

    protected abstract SoundEvent meleeAttackSound();

    protected abstract void onSpecialAttackStart(LivingEntity target);

    protected void onSpecialAttackTelegraphTick(int phaseTick, LivingEntity target) {
    }

    protected abstract void onSpecialAttackExecutionTick(int phaseTick, LivingEntity target);

    protected boolean hasBaseMeleeAttack() {
        return true;
    }

    protected boolean staggerOnMiss() {
        return false;
    }

    protected int staggerTicks() {
        return 0;
    }

    protected void onSpecialAttackExecutionComplete() {
        if (staggerOnMiss() && !this.attackHit) {
            enterAttackState(ATTACK_STAGGER);
        } else {
            enterAttackState(ATTACK_RECOVERY);
        }
    }

    protected final void beginSpecialAttack(LivingEntity target) {
        if (this.level().isClientSide || !isAlive() || target == null || !target.isAlive()
                || getAttackState() != ATTACK_IDLE || this.cooldownTicks > 0) return;
        this.lockedTargetId = target.getUUID();
        this.attackHit = false;
        this.attackStartedThisTick = true;
        this.lockedYaw = yawToward(target.position());
        setYRot(this.lockedYaw);
        setYBodyRot(this.lockedYaw);
        setYHeadRot(this.lockedYaw);
        this.navigation.stop();
        this.cooldownTicks = Math.max(0, cooldownTicks());
        setAttackMark(null);
        enterAttackState(ATTACK_TELEGRAPH);
        onSpecialAttackStart(target);
    }

    protected final void enterAttackState(int state) {
        this.entityData.set(ATTACK_STATE, state);
        this.entityData.set(ATTACK_PHASE_TICKS, 0);
    }

    protected final boolean damageLockedTarget(LivingEntity target, float damage) {
        if (this.attackHit || this.level().isClientSide || target == null || !target.isAlive()) return false;
        boolean hit = target.hurt(this.level().damageSources().mobAttack(this), damage);
        this.attackHit |= hit;
        return hit;
    }

    protected final void setAttackMark(Vec3 position) {
        this.entityData.set(ATTACK_MARK_X, position == null ? 0.0F : (float) position.x);
        this.entityData.set(ATTACK_MARK_Y, position == null ? 0.0F : (float) position.y);
        this.entityData.set(ATTACK_MARK_Z, position == null ? 0.0F : (float) position.z);
        this.entityData.set(HAS_ATTACK_MARK, position != null);
    }

    protected final List<Player> playersInsideGroundRadius(Vec3 center, double radius,
                                                            double verticalRange) {
        double x = center.x;
        double y = center.y;
        double z = center.z;
        AABB bounds = new AABB(x - radius, y - verticalRange, z - radius,
                x + radius, y + verticalRange, z + radius);
        double radiusSqr = radius * radius;
        return this.level().getEntitiesOfClass(Player.class, bounds, player -> player.isAlive()
                && !player.isCreative() && !player.isSpectator()
                && Math.abs(player.getY() - y) <= verticalRange
                && player.distanceToSqr(x, player.getY(), z) <= radiusSqr);
    }

    protected final boolean hitPlayer(Player player, float damage) {
        if (this.level().isClientSide || player == null || !player.isAlive()
                || player.isCreative() || player.isSpectator()) return false;
        return player.hurt(this.level().damageSources().mobAttack(this), damage);
    }

    protected final void cancelSpecialAttack() {
        if (getAttackState() == ATTACK_IDLE) return;
        this.lockedTargetId = null;
        this.attackHit = false;
        this.attackStartedThisTick = false;
        setAttackMark(null);
        enterAttackState(ATTACK_IDLE);
    }

    private void finishSpecialAttack() {
        this.lockedTargetId = null;
        this.attackStartedThisTick = false;
        setAttackMark(null);
        enterAttackState(ATTACK_IDLE);
    }

    private LivingEntity getLockedTarget() {
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || target.isRemoved() || this.lockedTargetId == null
                || !this.lockedTargetId.equals(target.getUUID())) return null;
        return target;
    }

    private boolean validPlayerTarget(LivingEntity target) {
        return target instanceof Player && target.isAlive() && !target.isRemoved();
    }

    private float yawToward(Vec3 position) {
        double dx = position.x - getX();
        double dz = position.z - getZ();
        return (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);
    }

    protected final Vec3 lockedHorizontalDirection() {
        double radians = Math.toRadians(this.lockedYaw);
        return new Vec3(-Math.sin(radians), 0.0D, Math.cos(radians));
    }

    protected final boolean isAttackHit() {
        return this.attackHit;
    }

    protected final void playCombatSound(SoundEvent sound, float volume, float pitch) {
        if (!this.level().isClientSide && sound != null) {
            this.playSound(sound, volume, pitch);
        }
    }

    protected final void sendCombatParticles(ParticleOptions particle, Vec3 position, int count,
                                              double spreadX, double spreadY, double spreadZ,
                                              double speed) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(particle, position.x, position.y, position.z, count,
                    spreadX, spreadY, spreadZ, speed);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ambientSound();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return hurtSound();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return deathSound();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && !this.level().isClientSide) {
            playCombatSound(meleeAttackSound(), 0.68F,
                    0.94F + this.random.nextFloat() * 0.12F);
        }
        return hit;
    }

    public final int getAttackState() {
        return this.entityData.get(ATTACK_STATE);
    }

    public final int getAttackPhaseTicks() {
        return this.entityData.get(ATTACK_PHASE_TICKS);
    }

    public final Vec3 getAttackMark() {
        return new Vec3(this.entityData.get(ATTACK_MARK_X),
                this.entityData.get(ATTACK_MARK_Y), this.entityData.get(ATTACK_MARK_Z));
    }

    public final boolean hasAttackMark() {
        return this.entityData.get(HAS_ATTACK_MARK);
    }

    public final boolean isAttackActive() {
        return getAttackState() != ATTACK_IDLE;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && isAttackActive()) {
            cancelSpecialAttack();
        }
        return hurt;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!this.level().isClientSide && isAttackActive()) {
            cancelSpecialAttack();
        }
        super.remove(reason);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("VoidrangerCooldown", this.cooldownTicks);
        // In-flight attack phases and marks deliberately do not persist. Reloading cannot replay an event.
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.cooldownTicks = Math.max(0, tag.getInt("VoidrangerCooldown"));
        this.lockedTargetId = null;
        this.attackHit = false;
        this.attackStartedThisTick = false;
        setAttackMark(null);
        enterAttackState(ATTACK_IDLE);
    }

    private static final class SpecialAttackGoal extends Goal {
        private final VoidrangerMob mob;

        private SpecialAttackGoal(VoidrangerMob mob) {
            this.mob = mob;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.mob.isAlive()) return false;
            if (this.mob.isAttackActive()) return true;
            LivingEntity target = this.mob.getTarget();
            if (!this.mob.validPlayerTarget(target) || this.mob.cooldownTicks > 0) return false;
            double distanceSqr = this.mob.distanceToSqr(target);
            double minimumRange = this.mob.minimumSpecialAttackRange();
            double maximumRange = this.mob.specialAttackRange();
            return distanceSqr > minimumRange * minimumRange
                    && distanceSqr <= maximumRange * maximumRange;
        }

        @Override
        public boolean canContinueToUse() {
            return this.mob.isAttackActive() && this.mob.getLockedTarget() != null
                    && this.mob.hurtTime <= 0 && this.mob.isAlive();
        }

        @Override
        public void start() {
            if (!this.mob.isAttackActive()) {
                beginForCurrentTarget();
            }
        }

        private void beginForCurrentTarget() {
            LivingEntity target = this.mob.getTarget();
            if (this.mob.validPlayerTarget(target)) {
                this.mob.beginSpecialAttack(target);
            }
        }

        @Override
        public void tick() {
            if (this.mob.isAttackActive()) {
                this.mob.getNavigation().stop();
                this.mob.setYRot(this.mob.lockedYaw);
                this.mob.setYBodyRot(this.mob.lockedYaw);
                this.mob.setYHeadRot(this.mob.lockedYaw);
            }
        }

        @Override
        public void stop() {
            if (this.mob.isAttackActive() && this.mob.getLockedTarget() == null) {
                this.mob.cancelSpecialAttack();
            }
        }
    }

    private static final class VoidrangerMeleeAttackGoal extends MeleeAttackGoal {
        private VoidrangerMeleeAttackGoal(VoidrangerMob mob) {
            super(mob, 1.0D, true);
        }

        @Override
        protected double getAttackReachSqr(LivingEntity target) {
            return Math.max(super.getAttackReachSqr(target), 4.0D);
        }
    }

    private static final class PursueTargetGoal extends Goal {
        private final VoidrangerMob mob;

        private PursueTargetGoal(VoidrangerMob mob) {
            this.mob = mob;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            return !this.mob.isAttackActive() && this.mob.validPlayerTarget(target)
                    && this.mob.distanceToSqr(target) > this.mob.specialAttackRange()
                    * this.mob.specialAttackRange();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (this.mob.validPlayerTarget(target)) {
                this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
                this.mob.getNavigation().moveTo(target, 1.0D);
            }
        }

        @Override
        public void stop() {
            this.mob.getNavigation().stop();
        }
    }
}
