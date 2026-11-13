package com.starrail.sim;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Building materials and harvestable plants for the Starazure Reef. */
public final class PathReefContent {
    private static final List<RegistryObject<Item>> MATERIALS = new ArrayList<>();
    private static final List<RegistryObject<Item>> FOODS = new ArrayList<>();

    public static final RegistryObject<Block> REEF_STONE = stone("starazure_reef_stone");
    public static final RegistryObject<Block> POROUS_STONE = stone("porous_reef_stone");
    public static final RegistryObject<Block> REEF_BRICKS = stone("starazure_reef_bricks");
    public static final RegistryObject<Block> REEF_ALGAE_MAT = stone("reef_algae_mat");
    public static final RegistryObject<Block> REEF_ALGAE = plant("reef_algae", 1, null);
    public static final RegistryObject<Block> REEF_SEA_FAN = plant("reef_sea_fan", 1, null);
    public static final RegistryObject<Block> STARAZURE_KELP = plant("starazure_kelp", 7, food(1, 0.1F));

    static {
        variants("starazure_reef_stone", REEF_STONE);
        variants("porous_reef_stone", POROUS_STONE);
        variants("starazure_reef_bricks", REEF_BRICKS);
        edible("reef_algae_chips", 3, 0.4F);
        edible("reef_algae_rice_ball", 6, 0.6F);
        edible("dried_starazure_kelp", 3, 0.4F);
    }

    private PathReefContent() {}
    static void register() {}
    static void addMaterials(CreativeModeTab.Output output) {
        for (var item : MATERIALS) output.accept(item.get());
    }
    static void addFoods(CreativeModeTab.Output output) {
        for (var item : FOODS) output.accept(item.get());
    }

    private static void variants(String id, RegistryObject<Block> base) {
        block(id + "_stairs", () -> new StairBlock(base.get().defaultBlockState(), stoneProperties()), MATERIALS, new Item.Properties());
        block(id + "_slab", () -> new SlabBlock(stoneProperties()), MATERIALS, new Item.Properties());
        block(id + "_wall", () -> new WallBlock(stoneProperties()), MATERIALS, new Item.Properties());
    }
    private static RegistryObject<Block> stone(String id) {
        return block(id, () -> new Block(stoneProperties()), MATERIALS, new Item.Properties());
    }
    private static BlockBehaviour.Properties stoneProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN)
                .strength(1.5F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.STONE);
    }
    private static RegistryObject<Block> plant(String id, int height, FoodProperties nutrition) {
        Item.Properties item = new Item.Properties();
        if (nutrition != null) item.food(nutrition);
        return block(id, () -> new ReefPlantBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_CYAN).noCollission().noOcclusion().instabreak()
                .sound(SoundType.WET_GRASS).randomTicks(), height), FOODS, item);
    }
    private static RegistryObject<Block> block(String id, Supplier<Block> factory,
                                                List<RegistryObject<Item>> tab, Item.Properties item) {
        var block = StarRailSimMod.BLOCKS.register(id, factory);
        tab.add(StarRailSimMod.ITEMS.register(id, () -> new BlockItem(block.get(), item)));
        return block;
    }
    private static void edible(String id, int nutrition, float saturation) {
        FOODS.add(StarRailSimMod.ITEMS.register(id, () -> new Item(new Item.Properties().food(food(nutrition, saturation)))));
    }
    private static FoodProperties food(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturation).build();
    }
}
