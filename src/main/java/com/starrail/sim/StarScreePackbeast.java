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

    /** Source saddle anchor (0,30,7); same scale and rear pose as the native client rig. */
    @Override
    public double getPassengersRidingOffset() {
        return 30.0 * getBbHeight() / 43.039771;
    }

    @Override
    protected void positionRider(net.minecraft.world.entity.Entity passenger,
                                 net.minecraft.world.entity.Entity.MoveFunction move) {
        if (!hasPassenger(passenger)) return;
        double scale=getBbHeight()/43.039771;
        double stand=getStandAnim(1.0F), angle=Math.toRadians(12.0)*stand;
        double rearDistance=18.5-8.5;
        // Interpolated native body translation plus the rotated local attachment.
        double y=20+rearDistance*Math.sin(Math.toRadians(12.0))*stand
                +10*Math.cos(angle)+1.5*Math.sin(angle);
        double z=8.5+rearDistance*(1-Math.cos(Math.toRadians(12.0)))*stand
                +10*Math.sin(angle)-1.5*Math.cos(angle);
        double yaw=Math.toRadians(yBodyRot);
        move.accept(passenger,getX()+z*scale*Math.sin(yaw),
                getY()+y*scale+passenger.getMyRidingOffset(),getZ()-z*scale*Math.cos(yaw));
        if(passenger instanceof net.minecraft.world.entity.LivingEntity living) living.yBodyRot=yBodyRot;
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        return otherAnimal instanceof StarScreePackbeast && super.canMate(otherAnimal);
    }

    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        if (sound == net.minecraft.sounds.SoundEvents.HORSE_STEP) sound = PathAnimalSounds.DAILY_PACK_STEP.get();
        if (sound == net.minecraft.sounds.SoundEvents.HORSE_STEP_WOOD) sound = PathAnimalSounds.DAILY_PACK_STEP.get();
        if (sound == net.minecraft.sounds.SoundEvents.HORSE_GALLOP) sound = PathAnimalSounds.DAILY_PACK_GALLOP.get();
        if (sound == net.minecraft.sounds.SoundEvents.HORSE_EAT) sound = PathAnimalSounds.DAILY_PACK_EAT.get();
        if (sound == net.minecraft.sounds.SoundEvents.HORSE_ANGRY) sound = PathAnimalSounds.DAILY_PACK_ANGRY.get();
        if (sound == net.minecraft.sounds.SoundEvents.HORSE_BREATHE) sound = PathAnimalSounds.DAILY_PACK_BREATHE.get();
        if (sound == net.minecraft.sounds.SoundEvents.HORSE_JUMP) sound = PathAnimalSounds.DAILY_PACK_JUMP.get();
        if (sound == net.minecraft.sounds.SoundEvents.HORSE_LAND) sound = PathAnimalSounds.DAILY_PACK_LAND.get();
        super.playSound(sound, volume, pitch);
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
