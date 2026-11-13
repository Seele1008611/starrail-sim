package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public final class GemLizard extends Animal {
    private static final ResourceLocation TERRACES = new ResourceLocation(StarRailSimMod.MOD_ID, "ember_spring_terraces");

    public GemLizard(EntityType<? extends GemLizard> type, Level level) { super(type, level); }

    static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes().add(Attributes.MAX_HEALTH, 10).add(Attributes.MOVEMENT_SPEED, .24);
    }

    static boolean canSpawn(EntityType<GemLizard> type, ServerLevelAccessor level, MobSpawnType reason,
                            BlockPos pos, RandomSource random) {
        var below = level.getBlockState(pos.below());
        return level.getLevel().dimension().equals(StarRailDimensionIds.THE_PATH) && level.getBiome(pos).is(TERRACES)
                && level.getRawBrightness(pos, 0) > 8 && level.getFluidState(pos).isEmpty()
                && (below.is(PathKitchen.EMBER_ROCK.get()) || below.is(PathKitchen.TRAVERTINE.get())
                    || below.is(PathKitchen.SPRING_CRUST.get()));
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.55));
        goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        goalSelector.addGoal(3, new TemptGoal(this, 1.15, Ingredient.of(PathKitchen.FLAME_PEPPER.get()), false));
        goalSelector.addGoal(4, new FollowParentGoal(this, 1.05));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, .8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 5));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override public boolean isFood(ItemStack stack) { return stack.is(PathKitchen.FLAME_PEPPER.get()); }

    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return PathAnimalSounds.GEM_LIZARD.get(); }

    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return PathAnimalSounds.GEM_LIZARD.get();
    }

    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return PathAnimalSounds.GEM_LIZARD.get(); }

    @Override public float getWalkTargetValue(BlockPos pos, net.minecraft.world.level.LevelReader level) {
        var ground = level.getBlockState(pos.below());
        return ground.is(PathKitchen.EMBER_ROCK.get()) || ground.is(PathKitchen.TRAVERTINE.get()) ? 10 : 0;
    }

    @Override public GemLizard getBreedOffspring(ServerLevel level, AgeableMob parent) {
        return PathAnimalEntities.GEM_LIZARD.get().create(level);
    }
}
