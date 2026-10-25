package com.starrail.sim;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.MossBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Renewable wood, crop, and food content for the redesigned Path dimension. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PathEcologyContent {
    private static final List<RegistryObject<Item>> MATERIAL_BLOCK_ITEMS = new ArrayList<>();
    private static final List<RegistryObject<Item>> FOOD_BLOCK_ITEMS = new ArrayList<>();
    private static final List<RegistryObject<Item>> FOOD_ITEMS = new ArrayList<>();

    public static final WoodSet SUNMEADOW = woodSet("sunmeadow", MapColor.WOOD, "sunmeadow_tree");
    public static final WoodSet GLIMMERWOOD = woodSet("glimmerwood", MapColor.COLOR_GREEN, "glimmerwood_tree");

    public static final RegistryObject<Block> MORNING_BERRY_BUSH = foodBlock("morning_berry_bush",
            () -> new PathBerryBushBlock(plantProperties(MapColor.PLANT).randomTicks()));
    public static final RegistryObject<Item> MORNING_BERRY = item("morning_berry",
            () -> new PathBerryItem(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(2).saturationMod(0.2F).build())));

    public static final RegistryObject<Block> FORESTBEAN_WILD = foodBlock("forestbean_wild",
            () -> new net.minecraft.world.level.block.BushBlock(plantProperties(MapColor.PLANT)));
    public static final RegistryObject<Block> FORESTBEAN_CROP = StarRailSimMod.BLOCKS.register(
            "forestbean_crop", () -> new PathForestBeanCropBlock(plantProperties(MapColor.PLANT).randomTicks()));
    public static final RegistryObject<Item> FORESTBEAN_SEEDS = item("forestbean_seeds",
            () -> new ItemNameBlockItem(FORESTBEAN_CROP.get(), new Item.Properties()));
    public static final RegistryObject<Item> FORESTBEAN_POD = item("forestbean_pod",
            () -> new Item(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(3).saturationMod(0.3F).build())));
    public static final RegistryObject<Item> ROASTED_FORESTBEAN = item("roasted_forestbean",
            () -> new Item(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(5).saturationMod(0.6F).build())));

    public static final RegistryObject<Block> SUNMEADOW_BLOOM = foodBlock("sunmeadow_bloom",
            () -> new net.minecraft.world.level.block.BushBlock(plantProperties(MapColor.PLANT)));
    public static final RegistryObject<Block> GLIMMER_MOSS = foodBlock("glimmer_moss",
            () -> new MossBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
                    .strength(0.1F).sound(SoundType.GRASS)));
    public static final RegistryObject<Block> TIDE_GRASS = foodBlock("tide_grass",
            () -> new PathShorePlantBlock(plantProperties(MapColor.PLANT)));
    public static final RegistryObject<Block> CLEAR_SAND_BELLFLOWER = foodBlock("clear_sand_bellflower",
            () -> new PathClearSandBellflowerBlock(plantProperties(MapColor.PLANT)));
    public static final RegistryObject<Block> MARSH_REED = block("path_marsh_reed",
            () -> new PathMarshReedBlock(plantProperties(MapColor.PLANT)));
    public static final RegistryObject<Block> RIDGE_CUSHION = block("path_ridge_cushion",
            () -> new PathRidgeCushionBlock(plantProperties(MapColor.PLANT)));
    public static final RegistryObject<Block> STAR_TIDE_ALGAE = StarRailSimMod.BLOCKS.register(
            "star_tide_algae", () -> new PathTideAlgaeBlock(plantProperties(MapColor.COLOR_CYAN)));
    public static final RegistryObject<Item> STAR_TIDE_ALGAE_ITEM = foodBlockItem("star_tide_algae",
            () -> new BlockItem(STAR_TIDE_ALGAE.get(), new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(1).saturationMod(0.1F).build())));
    public static final RegistryObject<Item> ROASTED_STAR_TIDE_ALGAE = item("roasted_star_tide_algae",
            () -> new Item(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(4).saturationMod(0.5F).build())));

    private PathEcologyContent() {
    }

    /** Forces all DeferredRegister entries in this class to be initialized. */
    static void register() {
    }

    @SubscribeEvent
    public static void registerCompostables(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ComposterBlock.COMPOSTABLES.put(MORNING_BERRY.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(FORESTBEAN_SEEDS.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(FORESTBEAN_POD.get(), 0.5F);
            ComposterBlock.COMPOSTABLES.put(ROASTED_FORESTBEAN.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(SUNMEADOW_BLOOM.get(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(GLIMMER_MOSS.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(TIDE_GRASS.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(CLEAR_SAND_BELLFLOWER.get(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(MARSH_REED.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(RIDGE_CUSHION.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(STAR_TIDE_ALGAE_ITEM.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(ROASTED_STAR_TIDE_ALGAE.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(FORESTBEAN_WILD.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(SUNMEADOW.leaves().get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(SUNMEADOW.sapling().get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(GLIMMERWOOD.leaves().get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(GLIMMERWOOD.sapling().get(), 0.3F);
        });
    }

    public static void addMaterialCreativeItems(CreativeModeTab.Output output) {
        for (RegistryObject<Item> item : MATERIAL_BLOCK_ITEMS) output.accept(item.get());
    }

    public static void addFoodCreativeItems(CreativeModeTab.Output output) {
        for (RegistryObject<Item> item : FOOD_BLOCK_ITEMS) output.accept(item.get());
        for (RegistryObject<Item> item : FOOD_ITEMS) output.accept(item.get());
    }

    private static WoodSet woodSet(String id, MapColor color, String feature) {
        RegistryObject<Block> strippedLog = pillar("stripped_" + id + "_log", color);
        RegistryObject<Block> strippedWood = pillar("stripped_" + id + "_wood", color);
        RegistryObject<Block> log = strippablePillar(id + "_log", color, strippedLog);
        RegistryObject<Block> wood = strippablePillar(id + "_wood", color, strippedWood);
        RegistryObject<Block> leaves = block(id + "_leaves", () -> new LeavesBlock(
                BlockBehaviour.Properties.of().mapColor(color).strength(0.2F).randomTicks()
                        .noOcclusion().sound(SoundType.GRASS).isSuffocating((s, l, p) -> false)
                        .isViewBlocking((s, l, p) -> false)));
        RegistryObject<Block> sapling = foodBlock(id + "_sapling", () -> new SaplingBlock(
                new PathTreeGrower(feature), plantProperties(color)));
        RegistryObject<Block> planks = block(id + "_planks", woodBlockProperties(color));
        RegistryObject<Block> stairs = block(id + "_stairs", () -> new StairBlock(
                planks.get().defaultBlockState(), woodBlockProperties(color)));
        RegistryObject<Block> slab = block(id + "_slab", () -> new SlabBlock(woodBlockProperties(color)));
        RegistryObject<Block> fence = block(id + "_fence", () -> new FenceBlock(woodBlockProperties(color)));
        RegistryObject<Block> fenceGate = block(id + "_fence_gate", () -> new FenceGateBlock(
                woodBlockProperties(color), WoodType.OAK));
        RegistryObject<Block> door = block(id + "_door", () -> new DoorBlock(
                woodBlockProperties(color).noOcclusion(), BlockSetType.OAK));
        RegistryObject<Block> trapdoor = block(id + "_trapdoor", () -> new TrapDoorBlock(
                woodBlockProperties(color).noOcclusion(), BlockSetType.OAK));
        RegistryObject<Block> pressurePlate = block(id + "_pressure_plate", () -> new PressurePlateBlock(
                PressurePlateBlock.Sensitivity.EVERYTHING, woodBlockProperties(color), BlockSetType.OAK));
        RegistryObject<Block> button = block(id + "_button", () -> new ButtonBlock(
                woodBlockProperties(color).noCollission(), BlockSetType.OAK, 30, true));
        return new WoodSet(log, wood, strippedLog, strippedWood, leaves, sapling, planks, stairs,
                slab, fence, fenceGate, door, trapdoor, pressurePlate, button);
    }

    private static RegistryObject<Block> pillar(String id, MapColor color) {
        return block(id, () -> new RotatedPillarBlock(woodBlockProperties(color)));
    }

    private static RegistryObject<Block> strippablePillar(String id, MapColor color,
                                                           RegistryObject<Block> strippedVariant) {
        return block(id, () -> new PathStrippablePillarBlock(woodBlockProperties(color), strippedVariant::get));
    }

    private static RegistryObject<Block> block(String id, BlockBehaviour.Properties properties) {
        return block(id, () -> new Block(properties));
    }

    private static RegistryObject<Block> block(String id, Supplier<? extends Block> factory) {
        RegistryObject<Block> block = StarRailSimMod.BLOCKS.register(id, factory::get);
        RegistryObject<Item> item = StarRailSimMod.ITEMS.register(id,
                () -> new BlockItem(block.get(), new Item.Properties()));
        MATERIAL_BLOCK_ITEMS.add(item);
        return block;
    }

    private static RegistryObject<Block> foodBlock(String id, Supplier<? extends Block> factory) {
        RegistryObject<Block> block = StarRailSimMod.BLOCKS.register(id, factory::get);
        RegistryObject<Item> item = StarRailSimMod.ITEMS.register(id,
                () -> new BlockItem(block.get(), new Item.Properties()));
        FOOD_BLOCK_ITEMS.add(item);
        return block;
    }

    private static RegistryObject<Item> item(String id, Supplier<? extends Item> factory) {
        RegistryObject<Item> item = StarRailSimMod.ITEMS.register(id, factory::get);
        FOOD_ITEMS.add(item);
        return item;
    }

    private static RegistryObject<Item> foodBlockItem(String id, Supplier<? extends Item> factory) {
        RegistryObject<Item> item = StarRailSimMod.ITEMS.register(id, factory::get);
        FOOD_BLOCK_ITEMS.add(item);
        return item;
    }

    private static BlockBehaviour.Properties woodBlockProperties(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).strength(2.0F)
                .sound(SoundType.WOOD);
    }

    private static BlockBehaviour.Properties plantProperties(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).noCollission().instabreak()
                .noOcclusion().sound(SoundType.GRASS);
    }

    public record WoodSet(RegistryObject<Block> log, RegistryObject<Block> wood,
                          RegistryObject<Block> strippedLog, RegistryObject<Block> strippedWood,
                          RegistryObject<Block> leaves, RegistryObject<Block> sapling,
                          RegistryObject<Block> planks, RegistryObject<Block> stairs,
                          RegistryObject<Block> slab, RegistryObject<Block> fence,
                          RegistryObject<Block> fenceGate, RegistryObject<Block> door,
                          RegistryObject<Block> trapdoor, RegistryObject<Block> pressurePlate,
                          RegistryObject<Block> button) {
    }
}
