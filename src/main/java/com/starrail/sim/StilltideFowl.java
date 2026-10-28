package com.starrail.sim;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/** Small flock animal based on vanilla chicken, retaining egg laying and seed-based breeding. */
public class StilltideFowl extends Chicken {
    private static final TagKey<Item> BREEDING_FOOD_TAG = TagKey.create(
            Registries.ITEM,
            new ResourceLocation(StarRailSimMod.MOD_ID, "stilltide_fowl_breeding_food"));

    public StilltideFowl(EntityType<? extends Chicken> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Chicken.createAttributes();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Chicken's BreedGoal already runs at priority 2. Keep seed temptation below it
        // so a player holding food cannot prevent two love-state adults from mating.
        goalSelector.addGoal(4, new TemptGoal(this, 1.25D,
                Ingredient.of(BREEDING_FOOD_TAG), false));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(BREEDING_FOOD_TAG);
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        return otherAnimal instanceof StilltideFowl && super.canMate(otherAnimal);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return PathAnimalSounds.FOWL_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return PathAnimalSounds.FOWL_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return PathAnimalSounds.FOWL_DEATH.get();
    }

    @Override
    public StilltideFowl getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new StilltideFowl(PathAnimalEntities.STILLTIDE_FOWL.get(), level);
    }

}
