package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.tags.FluidTags;

/** Schooling fish with vanilla bucket data preservation and local cooking/fishing routes. */
public final class DuskredFish extends AbstractSchoolingFish {
    public DuskredFish(EntityType<? extends DuskredFish> type,Level l){super(type,l);}
    static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,4);}
    @Override public ItemStack getBucketItemStack(){return new ItemStack(PathKitchenExpansion.food("duskred_fish_bucket").get());}
    @Override protected SoundEvent getFlopSound(){return PathAnimalSounds.DUSK_FISH.get();}
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource s){return PathAnimalSounds.DUSK_FISH.get();}
    @Override protected SoundEvent getDeathSound(){return PathAnimalSounds.DUSK_FISH.get();}
    public static boolean spawn(EntityType<DuskredFish> type,ServerLevelAccessor l,MobSpawnType reason,BlockPos p,RandomSource r){
        if(!l.getLevel().dimension().equals(StarRailDimensionIds.THE_PATH)||!l.getFluidState(p).isSource()||!l.getFluidState(p).is(FluidTags.WATER))return false;
        int connected=0;for(var d:net.minecraft.core.Direction.values())if(SpawnHabitatChecks.waterAt(l,p.relative(d)))connected++;
        return connected>=4&&SpawnHabitatChecks.waterAt(l,p.below());
    }
}
