package com.starrail.sim;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumSet;

/** Gentle herd animal based on the vanilla cow, retaining milk-bucket and breeding interactions. */
public class SunnyHarvestDeer extends Cow {
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> GRAZING =
            net.minecraft.network.syncher.SynchedEntityData.defineId(
                    SunnyHarvestDeer.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> ALERTED =
            net.minecraft.network.syncher.SynchedEntityData.defineId(
                    SunnyHarvestDeer.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private int alertTicks;

    public SunnyHarvestDeer(EntityType<? extends Cow> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(GRAZING, false);
        entityData.define(ALERTED, false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(2, new TemptGoal(this, 1.25D, Ingredient.of(Items.WHEAT), false));
        goalSelector.addGoal(7, new GrazingGoal());
    }

    public boolean isGrazing() {
        return entityData.get(GRAZING);
    }

    public boolean isAlerted() {
        return entityData.get(ALERTED);
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        return otherAnimal instanceof SunnyHarvestDeer && super.canMate(otherAnimal);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return PathAnimalSounds.DEER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return PathAnimalSounds.DEER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return PathAnimalSounds.DEER_DEATH.get();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide) {
            alertTicks = 60;
            entityData.set(GRAZING, false);
            entityData.set(ALERTED, true);
        }
        return hurt;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && alertTicks > 0 && --alertTicks == 0) {
            entityData.set(ALERTED, false);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Cow.createAttributes();
    }

    @Override
    public SunnyHarvestDeer getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new SunnyHarvestDeer(PathAnimalEntities.SUNNYHARVEST_DEER.get(), level);
    }

    private final class GrazingGoal extends Goal {
        private int remainingTicks;

        private GrazingGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return onGround() && !isBaby() && !isVehicle() && !isPassenger()
                    && !isInWaterOrBubble() && alertTicks == 0 && getRandom().nextInt(220) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return remainingTicks > 0 && onGround();
        }

        @Override
        public void start() {
            remainingTicks = 36;
            entityData.set(GRAZING, true);
            getNavigation().stop();
        }

        @Override
        public void tick() {
            remainingTicks--;
        }

        @Override
        public void stop() {
            entityData.set(GRAZING, false);
        }
    }

}
