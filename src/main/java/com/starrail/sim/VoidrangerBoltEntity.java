package com.starrail.sim;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;

/** Server-collided, non-homing bolt fired by the eliminator. */
public final class VoidrangerBoltEntity extends ThrowableProjectile {
    public VoidrangerBoltEntity(EntityType<? extends VoidrangerBoltEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        // The projectile adds no synchronized fields; ThrowableProjectile leaves this Entity hook abstract.
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity instanceof Player player && player.isAlive()
                && !player.isCreative() && !player.isSpectator()
                && super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide && result.getEntity() instanceof Player player && player.isAlive()
                && !player.isCreative() && !player.isSpectator()) {
            Entity owner = getOwner();
            LivingEntity attacker = owner instanceof LivingEntity living ? living : null;
            player.hurt(this.level().damageSources().mobProjectile(this, attacker), 4.5F);
            discard();
        }
        super.onHitEntity(result);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide) discard();
        super.onHitBlock(result);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount > 100) {
            discard();
        }
    }
}
