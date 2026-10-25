package com.starrail.sim;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Fresh natural geology palette for the redesigned Path dimension. */
public final class PathTerrainBlocks {
    private static final List<RegistryObject<Item>> BLOCK_ITEMS = new ArrayList<>();

    public static final RegistryObject<Block> PATH_GRASS_BLOCK = block("path_grass_block",
            () -> new PathTillableGrassBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GRASS)
                    .randomTicks().strength(0.6F).sound(SoundType.GRASS),
                    () -> Blocks.FARMLAND.defaultBlockState(),
                    () -> Blocks.DIRT_PATH.defaultBlockState()));
    public static final RegistryObject<Block> PATH_DIRT = block("path_dirt",
            () -> new PathTillableSoilBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT)
                    .strength(0.5F).sound(SoundType.GRAVEL),
                    () -> Blocks.FARMLAND.defaultBlockState(),
                    () -> Blocks.DIRT_PATH.defaultBlockState()));
    public static final RegistryObject<Block> PATH_FOREST_HUMUS = block("path_forest_humus",
            () -> new PathTillableSoilBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT)
                    .strength(0.6F).sound(SoundType.GRAVEL),
                    () -> Blocks.FARMLAND.defaultBlockState(),
                    () -> Blocks.DIRT_PATH.defaultBlockState()));
    public static final RegistryObject<Block> PATH_MARSH_HUMUS = block("path_marsh_humus",
            () -> new PathTillableSoilBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT)
                    .strength(0.6F).sound(SoundType.GRAVEL),
                    () -> Blocks.FARMLAND.defaultBlockState(),
                    () -> Blocks.DIRT_PATH.defaultBlockState()));
    public static final RegistryObject<Block> PATH_COARSE_SOIL = block("path_coarse_soil",
            () -> new PathTillableSoilBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT)
                    .strength(0.6F).sound(SoundType.GRAVEL),
                    () -> PATH_DIRT.get().defaultBlockState(),
                    () -> Blocks.DIRT_PATH.defaultBlockState()));
    public static final RegistryObject<Block> PATH_RIVER_SAND = block("path_river_sand",
            () -> new FallingBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                    .strength(0.5F).sound(SoundType.SAND)));
    public static final RegistryObject<Block> PATH_RIVER_SANDSTONE = block("path_river_sandstone",
            stone(MapColor.SAND, 1.8F, 5.0F));
    public static final RegistryObject<Block> PATH_RIVER_GRAVEL = block("path_river_gravel",
            () -> new FallingBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                    .strength(0.6F).sound(SoundType.GRAVEL)));
    public static final RegistryObject<Block> PATH_RIDGE_SCREE = block("path_ridge_scree",
            () -> new FallingBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                    .strength(0.6F).sound(SoundType.GRAVEL)));

    public static final RegistryObject<Block> PATH_STONE = block("path_stone",
            stone(MapColor.STONE, 1.8F, 6.0F));
    public static final RegistryObject<Block> PATH_COBBLESTONE = block("path_cobblestone",
            stone(MapColor.STONE, 2.0F, 6.0F));
    public static final RegistryObject<Block> PATH_SMOOTH_STONE = block("path_smooth_stone",
            stone(MapColor.STONE, 2.0F, 6.0F));
    public static final RegistryObject<Block> PATH_DEEP_STONE = block("path_deep_stone",
            stone(MapColor.DEEPSLATE, 3.0F, 6.0F));
    public static final RegistryObject<Block> PATH_DEEP_COBBLESTONE = block("path_deep_cobblestone",
            stone(MapColor.DEEPSLATE, 3.2F, 6.0F));
    public static final RegistryObject<Block> PATH_SMOOTH_DEEP_STONE = block("path_smooth_deep_stone",
            stone(MapColor.DEEPSLATE, 3.2F, 6.0F));
    public static final RegistryObject<Block> PATH_STONE_BRICKS = block("path_stone_bricks",
            stone(MapColor.STONE, 2.0F, 6.0F));
    public static final RegistryObject<Block> PATH_DEEP_STONE_BRICKS = block("path_deep_stone_bricks",
            stone(MapColor.DEEPSLATE, 3.2F, 6.0F));

    private PathTerrainBlocks() {
    }

    /** Forces registration fields to initialize while mod registries are assembled. */
    static void register() {
    }

    public static void addCreativeItems(CreativeModeTab.Output output) {
        for (RegistryObject<Item> item : BLOCK_ITEMS) output.accept(item.get());
    }

    private static BlockBehaviour.Properties stone(MapColor color, float hardness, float resistance) {
        return BlockBehaviour.Properties.of().mapColor(color).strength(hardness, resistance)
                .requiresCorrectToolForDrops().sound(SoundType.STONE);
    }

    private static RegistryObject<Block> block(String id, BlockBehaviour.Properties properties) {
        return block(id, () -> new Block(properties));
    }

    private static RegistryObject<Block> block(String id, Supplier<? extends Block> factory) {
        RegistryObject<Block> block = StarRailSimMod.BLOCKS.register(id, factory::get);
        RegistryObject<Item> item = StarRailSimMod.ITEMS.register(id,
                () -> new BlockItem(block.get(), new Item.Properties()));
        BLOCK_ITEMS.add(item);
        return block;
    }
}
