package com.starrail.sim;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public final class PathKitchen {
    private static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, StarRailSimMod.MOD_ID);
    private static final List<RegistryObject<Item>> MATERIALS = new ArrayList<>();
    private static final List<RegistryObject<Item>> FOODS = new ArrayList<>();

    public static final RegistryObject<RecipeType<KitchenRecipe>> TYPE = TYPES.register("kitchen", () -> new RecipeType<>() {});
    static final RegistryObject<RecipeSerializer<KitchenRecipe>> SERIALIZER =
            StarRailSimMod.RECIPE_SERIALIZERS.register("kitchen", KitchenRecipe.Serializer::new);
    public static final RegistryObject<Block> STATION = StarRailSimMod.BLOCKS.register("kitchen_station",
            () -> new KitchenBlock(BlockBehaviour.Properties.of().strength(2.5F).pushReaction(PushReaction.BLOCK)));
    static final RegistryObject<Item> STATION_ITEM = StarRailSimMod.ITEMS.register("kitchen_station",
            () -> new BlockItem(STATION.get(), new Item.Properties()));
    static final RegistryObject<BlockEntityType<KitchenBlockEntity>> ENTITY =
            StarRailSimMod.BLOCK_ENTITY_TYPES.register("kitchen_station",
                    () -> BlockEntityType.Builder.of(KitchenBlockEntity::new, STATION.get()).build(null));
    public static final RegistryObject<MenuType<KitchenMenu>> MENU =
            StarRailSimMod.MENU_TYPES.register("kitchen_station", () -> IForgeMenuType.create(KitchenMenu::new));
    public static final RegistryObject<Item> POT = material("cooking_pot");
    public static final RegistryObject<Item> PAN = material("cooking_pan");
    static final RegistryObject<Item> FLASK = StarRailSimMod.ITEMS.register("path_water_flask", SpringFlaskItem::new);
    static final RegistryObject<Item> WATER = ingredient("path_spring_water");
    static final RegistryObject<Item> CUP = material("kitchen_cup");
    static final RegistryObject<Item> FOWL_EGG = ingredient("stilltide_fowl_egg");
    static final RegistryObject<Item> SHEEP_MILK = ingredient("starfleece_milk");
    static final RegistryObject<Item> FROST_SALMON = ingredient("froststripe_salmon");
    static final RegistryObject<Item> GRAIN_HONEY = foodItem("grainflower_honey",
            () -> new Item(new Item.Properties().craftRemainder(FLASK.get())));
    static final RegistryObject<Item> FROST_SALT = ingredient("frost_salt");
    static final RegistryObject<Item> BRINE = ingredient("mirrorfrost_brine");
    static final RegistryObject<Item> SPRING_MINERAL = material("spring_mineral");
    static final RegistryObject<Item> FLAME_PEPPER = ingredient("flame_pepper");
    static final RegistryObject<Block> EMBER_ROCK = buildingBlock("ember_spring_rock", Blocks.BASALT);
    static final RegistryObject<Block> TRAVERTINE = stoneSet("spring_travertine", Blocks.TUFF);
    static final RegistryObject<Block> SPRING_CRUST = deposit("warm_spring_crust", Blocks.TUFF, SPRING_MINERAL);
    static final RegistryObject<Block> HARD_SNOW = buildingBlock("mirrorfrost_hard_snow", Blocks.SNOW_BLOCK);
    static final RegistryObject<Block> MIRROR_ICE = buildingBlock("mirrorfrost_ice", Blocks.PACKED_ICE);
    static final RegistryObject<Block> FROZEN_ROCK = stoneSet("bluegrey_frozen_rock", Blocks.CALCITE);
    static final RegistryObject<Block> GRAIN_SOIL = buildingBlock("grainvale_soil", () -> new PathTillableSoilBlock(
            BlockBehaviour.Properties.copy(Blocks.DIRT), () -> Blocks.FARMLAND.defaultBlockState(),
            () -> Blocks.DIRT_PATH.defaultBlockState()));
    static final RegistryObject<MobEffect> FROST_GUARD = StarRailSimMod.EFFECTS.register("frost_guard", FrostGuard::new);

    static {
        for (String id : List.of("snowroot", "grainvale_corn", "grain_flour", "grain_sugar",
                "sunseed_oil", "sparkling_water",
                "grainvale_tomato", "sunmeadow_skewer", "fruitwood_sprig",
                "fruitwood_spice", "gem_lizard_meat", "path_tea_leaf", "raw_belobog_sausage",
                "marinated_shantak_drumstick", "bean_pie_dough", "honey_cake_dough")) ingredient(id);
        material("kelp_fiber");
        forage("snowroot_crop", "snowroot_seeds", "snowroot", Blocks.SNOW_BLOCK, Blocks.PACKED_ICE, Blocks.CALCITE);
        forage("flame_pepper_bush", "flame_pepper_seeds", "flame_pepper", Blocks.BASALT, Blocks.SMOOTH_BASALT, Blocks.TUFF);
        forage("path_tea_bush", "path_tea_seeds", "path_tea_leaf", Blocks.BASALT, Blocks.SMOOTH_BASALT, Blocks.TUFF);
        forage("grainvale_corn_crop", "grainvale_corn_seeds", "grainvale_corn");
        forage("grainvale_tomato_crop", "grainvale_tomato_seeds", "grainvale_tomato");
        forage("fruitwood_shrub", "fruitwood_cutting", "fruitwood_sprig");
        deposit("briny_ice", Blocks.PACKED_ICE, FROST_SALT);
        stoneSet("windgrain_sandstone", Blocks.SANDSTONE);
        decoration("mirrorfrost_lichen", HARD_SNOW, FROZEN_ROCK, MIRROR_ICE);
        decoration("ember_heatgrass", EMBER_ROCK, TRAVERTINE);
        decoration("grainvale_grass", GRAIN_SOIL);
        decoration("grainvale_honeyflower", GRAIN_SOIL);
        FOODS.add(StarRailSimMod.ITEMS.register("snow_plains_combo_stew",
                () -> new ServedFood(new Item.Properties().stacksTo(16).food(food(12, .8F)), Items.BOWL, false, true)));
        FOODS.add(StarRailSimMod.ITEMS.register("cosmic_fried_rice", () -> new Item(new Item.Properties().food(mealFood(10, .8F,
                        () -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400),
                        () -> new MobEffectInstance(MobEffects.DIG_SPEED, 400))))));
        served("mung_bean_soda", 4, .5F, FLASK, true,
                () -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 4),
                () -> new MobEffectInstance(MobEffects.CONFUSION, 200));
        dish("belobog_sausage", 8, .80F, () -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300));
        dish("cooked_froststripe_salmon", 6, .80F);
        dish("grain_bread", 5, .60F);
        dish("sun_pancake", 6, .65F);
        dish("froststripe_salmon_bread", 9, .80F, () -> new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 500));
        served("hot_floating_sheep_milk", 4, .50F, CUP, true, () -> new MobEffectInstance(MobEffects.REGENERATION, 120));
        served("flaming_pepper_sauce", 2, .25F, FLASK, false);
        dish("rock_roasted_mountain_skewer", 8, .80F);
        dish("fruitwood_roasted_shantak_drumstick", 11, .85F, () -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600));
        dish("gem_lizard_skewer", 8, .80F);
        served("blazing_tea", 3, .45F, FLASK, true, () -> new MobEffectInstance(MobEffects.DIG_SPEED, 400));
        dish("delicious_shumai", 8, .80F, () -> new MobEffectInstance(MobEffects.ABSORPTION, 500));
        dish("delicious_bean_pie", 6, .65F);
        dish("popcorn", 3, .40F);
        dish("golden_honey_cake", 8, .80F, () -> new MobEffectInstance(MobEffects.SATURATION, 60));
    }

    private PathKitchen() {}

    public static void register(IEventBus bus) { PathKitchenExpansion.init(); KitchenRegionFeature.register(bus); TYPES.register(bus); }

    static void addMaterials(CreativeModeTab.Output output) {
        output.accept(STATION_ITEM.get());
        output.accept(FLASK.get());
        for (var item : MATERIALS) output.accept(item.get());
        PathKitchenExpansion.addMaterials(output);
    }

    static void addFoods(CreativeModeTab.Output output) {
        for (var item : FOODS) output.accept(item.get());
        PathKitchenExpansion.addFoods(output);
    }

    private static RegistryObject<Item> ingredient(String id) {
        return foodItem(id, () -> new Item(new Item.Properties()));
    }

    private static RegistryObject<Item> material(String id) {
        var result = StarRailSimMod.ITEMS.register(id, () -> new Item(new Item.Properties()));
        MATERIALS.add(result);
        return result;
    }

    private static RegistryObject<Item> foodItem(String id, java.util.function.Supplier<Item> factory) {
        var result = StarRailSimMod.ITEMS.register(id, factory);
        FOODS.add(result);
        return result;
    }

    private static void forage(String blockId, String seedId, String produceId, Block... terrain) {
        var produce = FOODS.stream().filter(item -> item.getId().getPath().equals(produceId)).findFirst().orElseThrow();
        var plant = StarRailSimMod.BLOCKS.register(blockId, () -> new KitchenForageBlock(
                BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH), produce, List.of(terrain)));
        foodItem(seedId, () -> new ItemNameBlockItem(plant.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> deposit(String id, Block base, RegistryObject<Item> harvest) {
        return buildingBlock(id, () -> new KitchenDepositBlock(
                BlockBehaviour.Properties.copy(base).randomTicks(), harvest));
    }

    @SafeVarargs
    private static void decoration(String id, RegistryObject<Block>... terrain) {
        buildingBlock(id, () -> new BushBlock(BlockBehaviour.Properties.copy(Blocks.FERN)) {
            @Override protected boolean mayPlaceOn(net.minecraft.world.level.block.state.BlockState state,
                                                   net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos) {
                for (var block : terrain) if (state.is(block.get())) return true;
                return super.mayPlaceOn(state, level, pos);
            }
        });
    }

    private static RegistryObject<Block> stoneSet(String id, Block material) {
        var base = buildingBlock(id, material);
        buildingBlock(id + "_stairs", () -> new StairBlock(() -> base.get().defaultBlockState(), BlockBehaviour.Properties.copy(material)));
        buildingBlock(id + "_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(material)));
        buildingBlock(id + "_wall", () -> new WallBlock(BlockBehaviour.Properties.copy(material)));
        return base;
    }

    private static RegistryObject<Block> buildingBlock(String id, Block material) {
        return buildingBlock(id, () -> new Block(BlockBehaviour.Properties.copy(material)));
    }

    private static RegistryObject<Block> buildingBlock(String id, java.util.function.Supplier<Block> factory) {
        var block = StarRailSimMod.BLOCKS.register(id, factory);
        MATERIALS.add(StarRailSimMod.ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties())));
        return block;
    }

    private static FoodProperties food(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturation).build();
    }

    private static void dish(String id, int nutrition, float saturation) {
        FOODS.add(StarRailSimMod.ITEMS.register(id, () -> new Item(new Item.Properties().food(food(nutrition, saturation)))));
    }

    private static void dish(String id, int nutrition, float saturation, java.util.function.Supplier<MobEffectInstance> effect) {
        FOODS.add(StarRailSimMod.ITEMS.register(id, () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                .nutrition(nutrition).saturationMod(saturation).effect(effect, 1.0F).build()))));
    }

    @SafeVarargs
    private static void served(String id, int nutrition, float saturation, RegistryObject<Item> container, boolean drink,
                               java.util.function.Supplier<MobEffectInstance>... effects) {
        FOODS.add(StarRailSimMod.ITEMS.register(id, () -> new ServedFood(new Item.Properties().stacksTo(16)
                .food(mealFood(nutrition, saturation, effects)), container.get(), drink, false)));
    }

    @SafeVarargs
    static FoodProperties mealFood(int nutrition, float saturation, java.util.function.Supplier<MobEffectInstance>... effects) {
        var builder = new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturation);
        for (var effect : effects) builder.effect(effect, 1.0F);
        return builder.build();
    }

    private static final class FrostGuard extends MobEffect {
        FrostGuard() { super(MobEffectCategory.BENEFICIAL, 0x92D5E6); }
        @Override public boolean isDurationEffectTick(int duration, int amplifier) { return true; }
        @Override public void applyEffectTick(LivingEntity entity, int amplifier) { entity.setTicksFrozen(0); }
    }

    private static final class ServedFood extends Item {
        private final Item container;
        private final boolean drink;
        private final boolean frostGuard;

        ServedFood(Properties properties, Item container, boolean drink, boolean frostGuard) {
            super(properties);
            this.container = container;
            this.drink = drink;
            this.frostGuard = frostGuard;
        }

        @Override public UseAnim getUseAnimation(ItemStack stack) { return drink ? UseAnim.DRINK : UseAnim.EAT; }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
            ItemStack remainder = super.finishUsingItem(stack, level, consumer);
            if (!level.isClientSide && frostGuard) {
                consumer.setTicksFrozen(0);
                consumer.addEffect(new MobEffectInstance(FROST_GUARD.get(), 1200));
            }
            if (consumer instanceof net.minecraft.world.entity.player.Player player && player.getAbilities().instabuild) return remainder;
            ItemStack empty = new ItemStack(container);
            if (remainder.isEmpty()) return empty;
            if (!level.isClientSide && consumer instanceof net.minecraft.world.entity.player.Player player) {
                if (!player.getInventory().add(empty)) player.drop(empty, false);
            }
            return remainder;
        }
    }
}
