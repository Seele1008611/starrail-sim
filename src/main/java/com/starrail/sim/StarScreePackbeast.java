package com.starrail.sim;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;

/** Highland mount based on vanilla horse, retaining taming, riding, armor, and horse breeding behavior. */
public class StarScreePackbeast extends Horse {
    public StarScreePackbeast(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.225D)
                .add(Attributes.JUMP_STRENGTH, 0.7D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(2, new TemptGoal(this, 1.2D,
                Ingredient.of(Items.GOLDEN_CARROT, Items.GOLDEN_APPLE), false));
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        return otherAnimal instanceof StarScreePackbeast && super.canMate(otherAnimal);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return PathAnimalSounds.PACKBEAST_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return PathAnimalSounds.PACKBEAST_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return PathAnimalSounds.PACKBEAST_DEATH.get();
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new StarScreePackbeast(PathAnimalEntities.STAR_SCREE_PACKBEAST.get(), level);
    }

}
