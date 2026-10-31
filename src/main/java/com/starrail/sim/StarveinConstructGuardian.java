package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Original ruin guardian prototype with synchronized, telegraphed attacks. */
public final class StarveinConstructGuardian extends Monster {
    public static final int ATTACK_NONE = 0;
    public static final int ATTACK_STAR_TRAIL_SWEEP = 1;
    public static final int ATTACK_CORE_WAVE = 2;

    private static final EntityDataAccessor<Integer> DATA_ATTACK_STATE =
            SynchedEntityData.defineId(StarveinConstructGuardian.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ATTACK_TICKS =
            SynchedEntityData.defineId(StarveinConstructGuardian.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STAGGER_TICKS =
            SynchedEntityData.defineId(StarveinConstructGuardian.class, EntityDataSerializers.INT);

    private static final int SWEEP_WINDUP_TICKS = 14;
    private static final int SWEEP_HIT_TICK = 16;
    private static final int SWEEP_END_TICK = 38;
    private static final int WAVE_HIT_TICK = 30;
    private static final int WAVE_END_TICK = 52;
    private static final double SWEEP_RANGE = 4.6D;
    private static final String PATH_TAG = "StarRailRuinPath";

    private record CombatProfile(double maxHealth, double movementSpeed, double attackDamage,
                                 double armor, double cooldownScale, int waveEvery,
                                 double waveRadius) {}

    private static final CombatProfile ORDINARY_PROFILE =
            new CombatProfile(400.0D, 0.23D, 13.0D, 16.0D, 1.0D, 3, 4.4D);
    private static final CombatProfile ASSAULT_PROFILE =
            new CombatProfile(370.0D, 0.26D, 14.0D, 14.0D, 0.84D, 3, 4.4D);
    private static final CombatProfile DEFENSE_PROFILE =
            new CombatProfile(450.0D, 0.21D, 12.0D, 20.0D, 1.12D, 3, 4.4D);
    private static final CombatProfile DOMAIN_PROFILE =
            new CombatProfile(400.0D, 0.22D, 11.5D, 16.0D, 0.96D, 2, 4.2D);

    private int attackCooldown;
    private int attackCycle;
    private boolean attackHit;
    private Vec3 waveCenter = Vec3.ZERO;
    private String pathId = "ordinary";

    public StarveinConstructGuardian(EntityType<? extends StarveinConstructGuardian> type,
                                    Level level) {
        super(type, level);
        this.xpReward = 35;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 13.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 16.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D);
    }

    /** Applies the shared light-weight combat profile for this ruin's path. */
    public void configureForPath(StarRailPath path) {
        this.pathId = path != null && path.isRealPath() ? path.getId() : "ordinary";
        CombatProfile profile = combatProfile();
        setBaseAttribute(Attributes.MAX_HEALTH, profile.maxHealth());
        setBaseAttribute(Attributes.MOVEMENT_SPEED, profile.movementSpeed());
        setBaseAttribute(Attributes.ATTACK_DAMAGE, profile.attackDamage());
        setBaseAttribute(Attributes.ARMOR, profile.armor());
        // Reconfiguration of a loaded or damaged guardian must never heal it.
        if (this.getHealth() > this.getMaxHealth()) this.setHealth(this.getMaxHealth());
    }

    private void setBaseAttribute(net.minecraft.world.entity.ai.attributes.Attribute attribute,
                                   double value) {
        var instance = this.getAttribute(attribute);
        if (instance != null) instance.setBaseValue(value);
    }

    private CombatProfile combatProfile() {
        return switch (StarRailPath.byId(this.pathId)) {
            case HUNT, DESTRUCTION, ELATION -> ASSAULT_PROFILE;
            case PRESERVATION, ABUNDANCE -> DEFENSE_PROFILE;
            case ERUDITION, HARMONY, NIHILITY, REMEMBRANCE -> DOMAIN_PROFILE;
            default -> ORDINARY_PROFILE;
        };
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 14.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_STATE, ATTACK_NONE);
        this.entityData.define(DATA_ATTACK_TICKS, 0);
        this.entityData.define(DATA_STAGGER_TICKS, 0);
    }

    public int getAttackState() {
        return this.entityData.get(DATA_ATTACK_STATE);
    }

    public int getAttackTicks() {
        return this.entityData.get(DATA_ATTACK_TICKS);
    }

    public int getStaggerTicks() {
        return this.entityData.get(DATA_STAGGER_TICKS);
    }

    /** Called by the toughness system when the guardian is broken. */
    public void staggerFor(int ticks) {
        if (this.level().isClientSide) return;
        this.entityData.set(DATA_ATTACK_STATE, ATTACK_NONE);
        this.entityData.set(DATA_ATTACK_TICKS, 0);
        this.entityData.set(DATA_STAGGER_TICKS, Math.max(this.getStaggerTicks(), ticks));
        this.attackCooldown = Math.max(this.attackCooldown, 24);
        this.getNavigation().stop();
        this.playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 1.2F, 0.65F);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || !this.isAlive()) return;

        int staggerTicks = this.getStaggerTicks();
        if (staggerTicks > 0) {
            this.entityData.set(DATA_STAGGER_TICKS, staggerTicks - 1);
            this.entityData.set(DATA_ATTACK_STATE, ATTACK_NONE);
            this.entityData.set(DATA_ATTACK_TICKS, 0);
            this.getNavigation().stop();
            return;
        }

        if (this.getAttackState() != ATTACK_NONE) {
            tickAttack();
            return;
        }

        if (this.attackCooldown > 0) this.attackCooldown--;
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()
                || (target instanceof Player player && player.isSpectator())) {
            this.getNavigation().stop();
            return;
        }

        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double distance = this.distanceToSqr(target);
        if (distance > 11.0D) {
            if (this.tickCount % 10 == 0) this.getNavigation().moveTo(target, 1.0D);
        } else {
            this.getNavigation().stop();
        }

        if (this.attackCooldown <= 0 && distance <= 64.0D) {
            faceTarget(target);
            CombatProfile profile = combatProfile();
            boolean coreWave = distance > SWEEP_RANGE * SWEEP_RANGE
                    || (this.attackCycle + 1) % profile.waveEvery() == 0;
            beginAttack(coreWave ? ATTACK_CORE_WAVE : ATTACK_STAR_TRAIL_SWEEP, target);
        }
    }

    private void faceTarget(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    private void beginAttack(int state, LivingEntity target) {
        this.entityData.set(DATA_ATTACK_STATE, state);
        this.entityData.set(DATA_ATTACK_TICKS, 0);
        this.attackHit = false;
        this.attackCycle++;
        if (state == ATTACK_CORE_WAVE) {
            this.waveCenter = new Vec3(target.getX(), target.getY(), target.getZ());
        }
        this.getNavigation().stop();
    }

    private void tickAttack() {
        int state = this.getAttackState();
        int ticks = this.getAttackTicks() + 1;
        this.entityData.set(DATA_ATTACK_TICKS, ticks);
        this.getNavigation().stop();

        if (state == ATTACK_STAR_TRAIL_SWEEP) {
            if (ticks == 5 || ticks == 9 || ticks == 13) sendSweepTelegraph();
            if (ticks == SWEEP_HIT_TICK) {
                this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.25F, 0.82F);
                hitSweepTargets();
            }
            if (ticks >= SWEEP_END_TICK) finishAttack(28);
        } else if (state == ATTACK_CORE_WAVE) {
            if (ticks < WAVE_HIT_TICK && ticks % 6 == 0) sendWaveTelegraph();
            if (ticks == WAVE_HIT_TICK) {
                this.playSound(SoundEvents.ANVIL_LAND, 1.4F, 0.8F);
                hitWaveTargets();
                if (this.level() instanceof ServerLevel level) {
                    level.sendParticles(ParticleTypes.POOF, waveCenter.x, waveCenter.y + 0.15D,
                            waveCenter.z, 18, 1.2D, 0.08D, 1.2D, 0.01D);
                }
            }
            if (ticks >= WAVE_END_TICK) finishAttack(34);
        }
    }

    private void finishAttack(int cooldown) {
        this.entityData.set(DATA_ATTACK_STATE, ATTACK_NONE);
        this.entityData.set(DATA_ATTACK_TICKS, 0);
        this.attackCooldown = Math.max(1,
                (int) Math.round(cooldown * combatProfile().cooldownScale()));
        this.attackHit = false;
    }

    private void sendSweepTelegraph() {
        if (!(this.level() instanceof ServerLevel level)) return;
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot()).normalize();
        Vec3 right = new Vec3(forward.z, 0.0D, -forward.x);
        for (int i = -4; i <= 4; i++) {
            Vec3 point = this.position().add(forward.scale(3.35D)).add(right.scale(i * 0.8D));
            level.sendParticles(ParticleTypes.END_ROD, point.x, this.getY() + 0.12D, point.z,
                    1, 0.0D, 0.02D, 0.0D, 0.0D);
        }
    }

    private void sendWaveTelegraph() {
        if (!(this.level() instanceof ServerLevel level)) return;
        double radius = combatProfile().waveRadius();
        double y = waveCenter.y + 0.12D;
        for (int i = 0; i < 24; i++) {
            double angle = (Math.PI * 2.0D * i) / 24.0D;
            double x = waveCenter.x + Math.cos(angle) * radius;
            double z = waveCenter.z + Math.sin(angle) * radius;
            level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1,
                    0.0D, 0.015D, 0.0D, 0.0D);
        }
    }

    private void hitSweepTargets() {
        if (this.attackHit) return;
        this.attackHit = true;
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot()).normalize();
        AABB area = this.getBoundingBox().inflate(SWEEP_RANGE, 1.4D, SWEEP_RANGE);
        for (Player player : this.level().getEntitiesOfClass(Player.class, area,
                candidate -> candidate.isAlive() && !candidate.isSpectator())) {
            Vec3 toTarget = player.position().subtract(this.position());
            Vec3 horizontal = new Vec3(toTarget.x, 0.0D, toTarget.z);
            if (horizontal.lengthSqr() > SWEEP_RANGE * SWEEP_RANGE
                    || horizontal.lengthSqr() < 1.0E-4D
                    || forward.dot(horizontal.normalize()) < 0.20D
                    || Math.abs(player.getY() - this.getY()) > 2.4D) continue;
            hurtAndPush(player, 0.8D);
        }
    }

    private void hitWaveTargets() {
        if (this.attackHit) return;
        this.attackHit = true;
        double radius = combatProfile().waveRadius();
        AABB area = new AABB(waveCenter.x - radius, waveCenter.y - 1.0D,
                waveCenter.z - radius, waveCenter.x + radius,
                waveCenter.y + 2.5D, waveCenter.z + radius);
        for (Player player : this.level().getEntitiesOfClass(Player.class, area,
                candidate -> candidate.isAlive() && !candidate.isSpectator())) {
            double dx = player.getX() - waveCenter.x;
            double dz = player.getZ() - waveCenter.z;
            if (dx * dx + dz * dz > radius * radius
                    || player.getY() > waveCenter.y + 1.0D) continue;
            hurtAndPush(player, 1.0D);
        }
    }

    private void hurtAndPush(Player player, double knockback) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (!player.hurt(this.damageSources().mobAttack(this), damage)) return;
        Vec3 away = player.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (away.lengthSqr() > 1.0E-4D) {
            away = away.normalize().scale(knockback);
            player.push(away.x, 0.32D, away.z);
            player.hurtMarked = true;
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 0.85F, 0.75F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString(PATH_TAG, this.pathId);
        tag.putInt("AttackCooldown", this.attackCooldown);
        tag.putInt("AttackCycle", this.attackCycle);
        tag.putInt("StaggerTicks", this.getStaggerTicks());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        // LivingEntity reads the saved Health before Mob restores its saved attributes.
        // Apply the saved path's max health first so a healthy 320-HP profile is not
        // clamped to the default 280 while loading; reapply it after Mob restores NBT.
        configureForPath(StarRailPath.byId(tag.getString(PATH_TAG)));
        super.readAdditionalSaveData(tag);
        configureForPath(StarRailPath.byId(tag.getString(PATH_TAG)));
        this.attackCooldown = Math.max(20, tag.getInt("AttackCooldown"));
        this.attackCycle = tag.getInt("AttackCycle");
        this.entityData.set(DATA_ATTACK_STATE, ATTACK_NONE);
        this.entityData.set(DATA_ATTACK_TICKS, 0);
        this.entityData.set(DATA_STAGGER_TICKS, Math.max(0, tag.getInt("StaggerTicks")));
    }
}
