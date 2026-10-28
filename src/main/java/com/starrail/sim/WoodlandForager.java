package com.starrail.sim;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

/** Forest and wetland forager; only Forestbean Pods and Star Tubers can start breeding. */
public class WoodlandForager extends Pig {
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> FORAGING =
            net.minecraft.network.syncher.SynchedEntityData.defineId(
                    WoodlandForager.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);

    public WoodlandForager(EntityType<? extends Pig> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(FORAGING, false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(2, new TemptGoal(this, 1.25D,
                Ingredient.of(PathEcologyContent.FORESTBEAN_POD.get(), PathEcologyContent.STAR_TUBER.get()), false));
        goalSelector.addGoal(7, new RootingGoal());
    }

    public boolean isForaging() {
        return entityData.get(FORAGING);
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        return otherAnimal instanceof WoodlandForager && super.canMate(otherAnimal);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return PathAnimalSounds.FORAGER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return PathAnimalSounds.FORAGER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return PathAnimalSounds.FORAGER_DEATH.get();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Pig.createAttributes();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(PathEcologyContent.FORESTBEAN_POD.get())
                || stack.is(PathEcologyContent.STAR_TUBER.get());
    }

    @Override
    public WoodlandForager getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new WoodlandForager(PathAnimalEntities.WOODLAND_FORAGER.get(), level);
    }

    private final class RootingGoal extends Goal {
        private int remainingTicks;

        private RootingGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return onGround() && !isBaby() && !isVehicle() && !isPassenger()
                    && !isInWaterOrBubble() && getRandom().nextInt(200) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return remainingTicks > 0 && onGround();
        }

        @Override
        public void start() {
            remainingTicks = 32;
            entityData.set(FORAGING, true);
            getNavigation().stop();
        }

        @Override
        public void tick() {
            remainingTicks--;
        }

        @Override
        public void stop() {
            entityData.set(FORAGING, false);
        }
    }

}
