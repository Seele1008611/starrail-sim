package com.starrail.sim;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumSet;

/** Wool-bearing highland animal based on the vanilla sheep, retaining shearing and breeding. */
public class StarvelvetRam extends Sheep {
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> SHEAR_REACTION =
            net.minecraft.network.syncher.SynchedEntityData.defineId(
                    StarvelvetRam.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private int shearReactionTicks;

    public StarvelvetRam(EntityType<? extends Sheep> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SHEAR_REACTION, false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(2, new TemptGoal(this, 1.25D, Ingredient.of(Items.WHEAT), false));
        goalSelector.addGoal(7, new MountainHopGoal());
    }

    @Override
    public void shear(SoundSource source) {
        boolean wasSheared = isSheared();
        super.shear(source);
        if (!wasSheared && !level().isClientSide) {
            shearReactionTicks = 18;
            entityData.set(SHEAR_REACTION, true);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && shearReactionTicks > 0 && --shearReactionTicks == 0) {
            entityData.set(SHEAR_REACTION, false);
        }
    }

    public boolean isShearReacting() {
        return entityData.get(SHEAR_REACTION);
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        return otherAnimal instanceof StarvelvetRam && super.canMate(otherAnimal);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return PathAnimalSounds.RAM_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return PathAnimalSounds.RAM_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return PathAnimalSounds.RAM_DEATH.get();
    }

    @Override
    public ResourceLocation getDefaultLootTable() {
        // Sheep selects a vanilla color-specific loot table here, which bypasses this
        // species' data/starrail_sim loot table and drops vanilla mutton.
        return getType().getDefaultLootTable();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Sheep.createAttributes();
    }

    @Override
    public StarvelvetRam getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new StarvelvetRam(PathAnimalEntities.STARVELVET_RAM.get(), level);
    }

    private final class MountainHopGoal extends Goal {
        private MountainHopGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return onGround() && !isBaby() && !isVehicle() && !isPassenger()
                    && !isInWaterOrBubble() && getRandom().nextInt(320) == 0;
        }

        @Override
        public void start() {
            getJumpControl().jump();
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

}
