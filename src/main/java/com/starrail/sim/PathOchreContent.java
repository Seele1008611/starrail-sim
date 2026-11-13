package com.starrail.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

/** Ochrecrystal Hills geology, decorations and renewable sunpod food loop. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PathOchreContent {
    private static final List<RegistryObject<Item>> MATERIALS = new ArrayList<>();
    private static final List<RegistryObject<Item>> FOODS = new ArrayList<>();

    public static final RegistryObject<Block> OCHRE_SAND = block("ochre_sand", () -> new FallingBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.5F).sound(SoundType.SAND)));
    public static final StoneSet OCHRE_STONE = stoneSet("ochre_stone");
    public static final StoneSet SMOOTH_OCHRE_STONE = stoneSet("smooth_ochre_stone");
    public static final StoneSet OCHRE_STONE_BRICKS = stoneSet("ochre_stone_bricks");
    public static final RegistryObject<Block> CRYSTAL_SAND_LAMP = block("crystal_sand_lamp", () ->
            new Block(stoneProperties().strength(1.5F).lightLevel(s -> 15).sound(SoundType.GLASS)));
    public static final RegistryObject<Block> OCHRE_LEAF_BUSH = block("ochre_leaf_bush", () ->
            new OchreGroundPlantBlock(plantProperties().sound(SoundType.GRASS), 10));
    public static final RegistryObject<Block> CRYSTAL_SAND_CLUSTER = block("crystal_sand_cluster", () ->
            new OchreGroundPlantBlock(plantProperties().strength(0.6F).sound(SoundType.AMETHYST_CLUSTER)
                    .lightLevel(s -> 2), 9));
    public static final RegistryObject<Item> CRYSTAL_SAND_GRAIN = material("crystal_sand_grain");

    public static final RegistryObject<Block> SUNPOD_WILD = foodBlock("sunpod_wild", () ->
            new OchreGroundPlantBlock(plantProperties().sound(SoundType.GRASS), 13));
    public static final RegistryObject<Block> SUNPOD_CROP = StarRailSimMod.BLOCKS.register("sunpod_crop", () ->
            new SunpodCropBlock(plantProperties().sound(SoundType.CROP).randomTicks()));
    public static final RegistryObject<Item> SUNPOD_SEEDS = foodItem("sunpod_seeds", () ->
            new ItemNameBlockItem(SUNPOD_CROP.get(), new Item.Properties()));
    public static final RegistryObject<Item> SUNSEED = food("sunseed", 1, 0.2F);
    public static final RegistryObject<Item> ROASTED_SUNSEED = food("roasted_sunseed", 4, 0.5F);
    public static final RegistryObject<Item> SUNSEED_CAKE = food("sunseed_cake", 6, 0.6F);

    private PathOchreContent() { }
    static void register() { }
    public static void addMaterials(CreativeModeTab.Output output) {
        for (var item : MATERIALS) output.accept(item.get());
    }
    public static void addFoods(CreativeModeTab.Output output) {
        for (var item : FOODS) output.accept(item.get());
    }

    @SubscribeEvent
    public static void compostables(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ComposterBlock.COMPOSTABLES.put(SUNPOD_SEEDS.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(SUNSEED.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(SUNSEED_CAKE.get(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(SUNPOD_WILD.get().asItem(), 0.5F);
            ComposterBlock.COMPOSTABLES.put(OCHRE_LEAF_BUSH.get().asItem(), 0.3F);
        });
    }

    private static StoneSet stoneSet(String id) {
        RegistryObject<Block> base = block(id, () -> new Block(stoneProperties()));
        RegistryObject<Block> stairs = block(id + "_stairs", () -> new StairBlock(base.get().defaultBlockState(), stoneProperties()));
        RegistryObject<Block> slab = block(id + "_slab", () -> new SlabBlock(stoneProperties()));
        RegistryObject<Block> wall = block(id + "_wall", () -> new WallBlock(stoneProperties()));
        return new StoneSet(base, stairs, slab, wall);
    }
    private static BlockBehaviour.Properties stoneProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE)
                .strength(1.8F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.STONE);
    }
    private static BlockBehaviour.Properties plantProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).noCollission().noOcclusion().instabreak();
    }
    private static RegistryObject<Block> block(String id, Supplier<Block> factory) {
        var b = StarRailSimMod.BLOCKS.register(id, factory);
        MATERIALS.add(StarRailSimMod.ITEMS.register(id, () -> new BlockItem(b.get(), new Item.Properties())));
        return b;
    }
    private static RegistryObject<Block> foodBlock(String id, Supplier<Block> factory) {
        var b = StarRailSimMod.BLOCKS.register(id, factory);
        FOODS.add(StarRailSimMod.ITEMS.register(id, () -> new BlockItem(b.get(), new Item.Properties())));
        return b;
    }
    private static RegistryObject<Item> material(String id) {
        var item = StarRailSimMod.ITEMS.register(id, () -> new Item(new Item.Properties()));
        MATERIALS.add(item);
        return item;
    }
    private static RegistryObject<Item> food(String id, int nutrition, float saturation) {
        return foodItem(id, () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                .nutrition(nutrition).saturationMod(saturation).build())));
    }
    private static RegistryObject<Item> foodItem(String id, Supplier<Item> factory) {
        var item = StarRailSimMod.ITEMS.register(id, factory);
        FOODS.add(item);
        return item;
    }
    public record StoneSet(RegistryObject<Block> base, RegistryObject<Block> stairs,
                           RegistryObject<Block> slab, RegistryObject<Block> wall) { }
}
