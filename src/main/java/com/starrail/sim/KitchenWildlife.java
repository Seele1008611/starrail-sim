package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.Vec3;

/** Three regional breeding species; all share food-following and vanilla age/cooldown handling. */
public class KitchenWildlife extends Animal {
    // Separate runtime classes keep vanilla FollowParentGoal's class-based search species-specific.
    public static final class Otter extends KitchenWildlife {public Otter(EntityType<? extends KitchenWildlife> t,Level l){super(t,l);}}
    public static final class Crab extends KitchenWildlife {
        public Crab(EntityType<? extends KitchenWildlife> type, Level level) { super(type, level); }

        @Override
        public boolean checkSpawnObstruction(LevelReader level) {
            return level.isUnobstructed(this);
        }
    }
    public static final class Finch extends KitchenWildlife {public Finch(EntityType<? extends KitchenWildlife> t,Level l){super(t,l);}}
    public KitchenWildlife(EntityType<? extends KitchenWildlife> type,Level level){super(type,level);
        if(isFinch())moveControl=new FlyingMoveControl(this,20,false);
        if(isOtter()||isCrab())setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER,0);
    }
    private String species(){return getType().builtInRegistryHolder().key().location().getPath();}
    public boolean isOtter(){return species().equals("lotus_otter");}
    public boolean isCrab(){return species().equals("rockdwelling_crab");}
    public boolean isFinch(){return species().equals("duskorange_finch");}
    static AttributeSupplier.Builder attributes(double health){return Animal.createMobAttributes().add(Attributes.MAX_HEALTH,health)
            .add(Attributes.MOVEMENT_SPEED,.23).add(Attributes.FLYING_SPEED,.4);}
    private net.minecraft.world.item.Item breedingFood(){return isOtter()?PathKitchen.FROST_SALMON.get():
            isFinch()?PathKitchenExpansion.food("white_grape").get():PathReefContent.REEF_ALGAE.get().asItem();}
    @Override public boolean isFood(ItemStack stack){return stack.is(breedingFood());}
    @Override public boolean canMate(Animal other){return other.getType()==getType()&&super.canMate(other);}
    @Override protected void registerGoals(){
        if(!isCrab())goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(1,new PanicGoal(this,1.5));
        if(isOtter())goalSelector.addGoal(0,new BreathAirGoal(this));
        goalSelector.addGoal(2,new BreedGoal(this,1));goalSelector.addGoal(3,new TemptGoal(this,1.1,Ingredient.of(breedingFood()),false));
        goalSelector.addGoal(4,new FollowParentGoal(this,1.05));
        if(isFinch())goalSelector.addGoal(5,new WaterAvoidingRandomFlyingGoal(this,1));
        else if(isOtter()){goalSelector.addGoal(5,new RandomSwimmingGoal(this,.9,40));goalSelector.addGoal(6,new RandomStrollGoal(this,.8,80));}
        else goalSelector.addGoal(5,new RandomStrollGoal(this,.7,80));
        goalSelector.addGoal(7,new LookAtPlayerGoal(this,Player.class,5));goalSelector.addGoal(8,new RandomLookAroundGoal(this));
    }
    @Override protected PathNavigation createNavigation(Level level){
        if(isFinch()){var nav=new FlyingPathNavigation(this,level);nav.setCanOpenDoors(false);nav.setCanFloat(true);return nav;}
        return new AmphibiousPathNavigation(this,level);
    }
    @Override public boolean canBreatheUnderwater(){return isCrab();}
    @Override public void travel(Vec3 input){
        if(isEffectiveAi()&&isInWater()&&!isFinch()){
            moveRelative(.04F,input);move(MoverType.SELF,getDeltaMovement());setDeltaMovement(getDeltaMovement().scale(.85));
            if(getTarget()==null)setDeltaMovement(getDeltaMovement().add(0,-.002,0));
        }else super.travel(input);
    }
    @Override public void aiStep(){super.aiStep();
        if(isFinch()){
            if(!level().isClientSide)setNoGravity(!getNavigation().isDone());
            if(!onGround()&&getDeltaMovement().y<0)setDeltaMovement(getDeltaMovement().multiply(1,.65,1));
        }
    }
    @Override public boolean causeFallDamage(float distance,float multiplier,DamageSource source){return isFinch()?false:super.causeFallDamage(distance,multiplier,source);}
    @Override public KitchenWildlife getBreedOffspring(ServerLevel l,AgeableMob p){return (KitchenWildlife)getType().create(l);}
    private SoundEvent voice(){return (isOtter()?PathAnimalSounds.LOTUS_OTTER:isCrab()?PathAnimalSounds.ROCK_CRAB:PathAnimalSounds.ORANGE_FINCH).get();}
    @Override protected SoundEvent getAmbientSound(){return voice();}
    @Override protected SoundEvent getHurtSound(DamageSource source){return voice();}
    @Override protected SoundEvent getDeathSound(){return voice();}
    @Override protected float getSoundVolume(){return .7F;}
    public static boolean spawn(EntityType<KitchenWildlife> type,ServerLevelAccessor l,MobSpawnType reason,BlockPos p,RandomSource r){
        if(!l.getLevel().dimension().equals(StarRailDimensionIds.THE_PATH))return false;
        String id=type.builtInRegistryHolder().key().location().getPath();var below=l.getBlockState(p.below());
        int light = l.getRawBrightness(p, 0);
        if(id.equals("lotus_otter"))return light>8&&l.getFluidState(p).isEmpty()&&below.is(PathKitchenExpansion.block("jadebranch_mud").get())
                && BlockPos.betweenClosedStream(p.offset(-5,-1,-5),p.offset(5,0,5)).anyMatch(q->SpawnHabitatChecks.waterAt(l,q));
        if(id.equals("rockdwelling_crab")) {
            var fluid = l.getFluidState(p);
            return light > 5 && (below.is(PathKitchenExpansion.block("tidal_stripe_rock").get())
                    || below.is(PathKitchenExpansion.block("shell_sand").get())
                    || below.is(PathKitchenExpansion.block("renewable_tidepool").get()))
                    && (fluid.isEmpty() || fluid.is(FluidTags.WATER))
                    && l.getFluidState(p.above()).isEmpty();
        }
        return light > 7 && l.getFluidState(p).isEmpty() && below.isFaceSturdy(l,p.below(),Direction.UP);
    }
}
