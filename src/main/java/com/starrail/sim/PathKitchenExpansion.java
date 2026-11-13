package com.starrail.sim;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;
import java.util.*;
import java.util.function.Supplier;

/** The second kitchen region batch. Suppliers defer all registry lookups until registration. */
public final class PathKitchenExpansion {
    private static final Map<String,RegistryObject<Item>> FOODS=new LinkedHashMap<>();
    private static final Map<String,RegistryObject<Block>> BLOCKS=new LinkedHashMap<>();
    private static final List<RegistryObject<Item>> MATERIALS=new ArrayList<>();
    static {
        terrain("jadebranch_mud",Blocks.MUD);terrain("lotus_gravel",Blocks.GRAVEL);
        stone("tidal_stripe_rock",Blocks.STONE);terrain("shell_sand",Blocks.SAND);
        block("duskorange_soil",()->new PathTillableSoilBlock(BlockBehaviour.Properties.copy(Blocks.DIRT),
                ()->Blocks.FARMLAND.defaultBlockState(),()->Blocks.DIRT_PATH.defaultBlockState()));
        stone("autumn_streak_rock",Blocks.STONE);
        for(String id:List.of("alfalfa_leaf","jadebranch_radish","orange","lemon","white_grape","sourberry","brownnut",
                "roasted_brownnut_powder","milkcream_base","kelp_agar","rock_crab_meat",
                "lemon_tart_dough","stargazing_cake_dough","oak_roll_dough")) ingredient(id);
        food("duskred_fish",3,.3F,null,false);
        food("cooked_duskred_fish",6,.8F,null,false);
        forage("alfalfa_crop","alfalfa_seeds","alfalfa_leaf",false);
        forage("jadebranch_radish_crop","jadebranch_radish_seeds","jadebranch_radish",false);
        forage("white_grape_vine","white_grape_cutting","white_grape",false);
        forage("sourberry_bush","sourberry_cutting","sourberry",false);
        forage("brownnut_bush","brownnut_cutting","brownnut",false);
        aquatic("singing_lotus","singing_lotus_root");aquatic("edible_anemone_patch","edible_anemone");
        block("lotus_lily_pad",()->new WaterlilyBlock(BlockBehaviour.Properties.copy(Blocks.LILY_PAD)),true);
        block("saltgrass",()->new BushBlock(BlockBehaviour.Properties.copy(Blocks.GRASS)){
            @Override protected boolean mayPlaceOn(net.minecraft.world.level.block.state.BlockState s,
                    net.minecraft.world.level.BlockGetter l,net.minecraft.core.BlockPos p){
                return s.is(block("shell_sand").get())||s.is(block("tidal_stripe_rock").get())||super.mayPlaceOn(s,l,p);
            }
        },true);
        block("renewable_tidepool",()->new KitchenDepositBlock(BlockBehaviour.Properties.copy(Blocks.STONE).randomTicks(),food("rock_crab_meat")));
        wood();
        food("alfalfa_salad",8,.8F,()->Items.BOWL,false);
        food("mixed_vegetable_soup",10,.8F,()->Items.BOWL,false,() -> new MobEffectInstance(MobEffects.REGENERATION, 160));
        food("singing_lotus_cake",6,.65F,null,false);
        food("happy_immortal_tea",4,.5F,PathKitchen.CUP,true,()->new MobEffectInstance(MobEffects.JUMP,600));
        food("cold_duskred_fish",9,.8F,null,false,() -> new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 600));
        food("fried_rock_crab",10,.85F,null,false,() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400));
        food("flash_fried_anemone",10,.85F,null,false,() -> new MobEffectInstance(MobEffects.WATER_BREATHING, 600));
        food("mr_herring_lemon_tart",6,.65F,null,false,() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300));
        food("seabreeze_tricolor_icecream",6,.7F,PathKitchen.CUP,false,()->new MobEffectInstance(MobEffects.WATER_BREATHING,900));
        food("stargazing_cake_slice",7,.75F,null,false,()->new MobEffectInstance(MobEffects.NIGHT_VISION,1200));
        food("oak_cake_roll",7,.8F,null,false,
                () -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 3),
                () -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 3));
        food("pika_whitegrape_soda",3,.4F,PathKitchen.FLASK,true);
        food("cheerful_orange_heart",4,.5F,PathKitchen.FLASK,true);
        food("sour_dream_gummies",3,.4F,null,false);
        FOODS.put("duskred_fish_bucket",StarRailSimMod.ITEMS.register("duskred_fish_bucket",()->
            new MobBucketItem(PathAnimalEntities.DUSKRED_FISH,()->net.minecraft.world.level.material.Fluids.WATER,
                    ()->net.minecraft.sounds.SoundEvents.BUCKET_EMPTY_FISH,new Item.Properties().stacksTo(1))));
    }
    private PathKitchenExpansion(){}
    static void init(){}
    public static RegistryObject<Item> food(String id){return Objects.requireNonNull(FOODS.get(id),id);}
    public static RegistryObject<Block> block(String id){return Objects.requireNonNull(BLOCKS.get(id),id);}
    static void addFoods(CreativeModeTab.Output out){FOODS.values().forEach(i->out.accept(i.get()));}
    static void addMaterials(CreativeModeTab.Output out){MATERIALS.forEach(i->out.accept(i.get()));}
    private static void ingredient(String id){FOODS.put(id,StarRailSimMod.ITEMS.register(id,()->{
        var p=new Item.Properties();if(Set.of("jadebranch_radish","orange","lemon","white_grape","sourberry","brownnut").contains(id))
            p.food(new FoodProperties.Builder().nutrition(2).saturationMod(.3F).build());
        return new Item(p);
    }));}
    @SafeVarargs
    private static void food(String id, int nutrition, float saturation, Supplier<Item> container, boolean drink,
                             Supplier<MobEffectInstance>... effects) {
        FOODS.put(id, StarRailSimMod.ITEMS.register(id, () -> new Meal(
                new Item.Properties().food(PathKitchen.mealFood(nutrition, saturation, effects)), container, drink)));
    }
    private static void terrain(String id,Block template){block(id,()->new Block(BlockBehaviour.Properties.copy(template)));}
    private static void block(String id,Supplier<? extends Block> factory){block(id,factory,false);}
    private static void block(String id,Supplier<? extends Block> factory,boolean plant){
        RegistryObject<Block> b=StarRailSimMod.BLOCKS.register(id,factory::get);BLOCKS.put(id,b);
        RegistryObject<Item> item=StarRailSimMod.ITEMS.register(id,()->id.equals("lotus_lily_pad")
                ?new PlaceOnWaterBlockItem(b.get(),new Item.Properties()):new BlockItem(b.get(),new Item.Properties()));
        if(plant)FOODS.put(id,item);else MATERIALS.add(item);
    }
    private static void stone(String id,Block base){
        terrain(id,base);block(id+"_stairs",()->new StairBlock(()->block(id).get().defaultBlockState(),BlockBehaviour.Properties.copy(base)));
        block(id+"_slab",()->new SlabBlock(BlockBehaviour.Properties.copy(base)));
        block(id+"_wall",()->new WallBlock(BlockBehaviour.Properties.copy(base)));
    }
    private static void forage(String id,String seed,String result,boolean unused){
        RegistryObject<Block> b=StarRailSimMod.BLOCKS.register(id,()->new KitchenForageBlock(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH),
                food(result),List.of(block("jadebranch_mud").get(),block("duskorange_soil").get())));
        BLOCKS.put(id,b);FOODS.put(seed,StarRailSimMod.ITEMS.register(seed,()->new ItemNameBlockItem(b.get(),new Item.Properties())));
    }
    private static void aquatic(String id,String result){
        RegistryObject<Block> b=StarRailSimMod.BLOCKS.register(id,()->new KitchenAquaticPlant(BlockBehaviour.Properties.copy(Blocks.SEAGRASS).randomTicks(),()->food(result).get()));
        BLOCKS.put(id,b);FOODS.put(result,StarRailSimMod.ITEMS.register(result,()->new ItemNameBlockItem(b.get(),new Item.Properties())));
    }
    private static BlockBehaviour.Properties woodProps(){return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2).sound(SoundType.WOOD);}
    private static void wood(){
        block("stripped_fruitwood_log",()->new RotatedPillarBlock(woodProps()));
        block("fruitwood_log",()->new PathStrippablePillarBlock(woodProps(),()->block("stripped_fruitwood_log").get()));
        block("fruitwood_planks",()->new Block(woodProps()));
        block("fruitwood_stairs",()->new StairBlock(()->block("fruitwood_planks").get().defaultBlockState(),woodProps()));
        block("fruitwood_slab",()->new SlabBlock(woodProps()));block("fruitwood_fence",()->new FenceBlock(woodProps()));
        block("fruitwood_fence_gate",()->new FenceGateBlock(woodProps(),WoodType.OAK));
        block("fruitwood_door",()->new DoorBlock(woodProps().noOcclusion(),BlockSetType.OAK));
        block("fruitwood_trapdoor",()->new TrapDoorBlock(woodProps().noOcclusion(),BlockSetType.OAK));
        for(String fruit:List.of("orange","lemon")){
            block(fruit+"_leaves",()->new OrchardLeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES),food(fruit)));
            block(fruit+"_sapling",()->new SaplingBlock(new PathTreeGrower(fruit+"_tree"),BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)),true);
        }
    }
    private static final class Meal extends Item {
        private final Supplier<Item> container;private final boolean drink;
        Meal(Properties p,Supplier<Item> c,boolean d){super(p);container=c;drink=d;}
        @Override public UseAnim getUseAnimation(ItemStack s){return drink?UseAnim.DRINK:UseAnim.EAT;}
        @Override public ItemStack finishUsingItem(ItemStack s,Level level,LivingEntity eater){
            ItemStack result=super.finishUsingItem(s,level,eater);
            if(container==null || (eater instanceof net.minecraft.world.entity.player.Player p && p.getAbilities().instabuild))return result;
            var empty=new ItemStack(container.get());if(result.isEmpty())return empty;
            if(!level.isClientSide && eater instanceof net.minecraft.world.entity.player.Player p && !p.getInventory().add(empty))p.drop(empty,false);
            return result;
        }
    }
}
