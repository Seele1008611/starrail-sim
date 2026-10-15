package com.starrail.sim;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** First-pass terrain and construction palette for The Path. */
public final class StarRailWorldBlocks {
    private static final List<RegistryObject<Item>> BLOCK_ITEMS = new ArrayList<>();

    public static final RegistryObject<Block> STAR_DUST_STONE = block("star_dust_stone",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(2.0F, 6.0F)
                    .requiresCorrectToolForDrops());
    public static final RegistryObject<Block> STARFALL_ROCK = block("starfall_rock",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(2.5F, 7.0F)
                    .requiresCorrectToolForDrops());
    public static final RegistryObject<Block> STAR_DUST_SOIL = block("star_dust_soil",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(0.6F));
    public static final RegistryObject<Block> STAR_DUST_GRAVEL = block("star_dust_gravel",
            () -> new FallingBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
                    .strength(0.6F).sound(SoundType.GRAVEL)));
    public static final RegistryObject<Block> STAR_RAIL_SAND = block("star_rail_sand",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.5F));
    public static final RegistryObject<Block> STAR_RAIL_SANDSTONE = block("star_rail_sandstone",
            BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(1.8F, 5.0F)
                    .requiresCorrectToolForDrops());
    public static final RegistryObject<Block> STAR_VEIN_ROCK = block("star_vein_rock",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(2.4F, 6.0F)
                    .requiresCorrectToolForDrops());
    public static final RegistryObject<Block> STAR_VEIN_CRYSTAL_BRICKS = block("star_vein_crystal_bricks",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(2.4F, 6.0F)
                    .requiresCorrectToolForDrops().lightLevel(state -> 2));
    public static final RegistryObject<Block> STAR_VEIN_CRYSTAL = block("star_vein_crystal",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(3.0F, 6.0F)
                    .requiresCorrectToolForDrops().lightLevel(state -> 4));
    public static final RegistryObject<Block> STAR_RAIL_CUT_STONE = block("star_rail_cut_stone",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(2.0F, 6.0F)
                    .requiresCorrectToolForDrops());
    public static final RegistryObject<Block> STAR_DUST_BRICKS = block("star_dust_bricks",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(2.0F, 6.0F)
                    .requiresCorrectToolForDrops());
    public static final RegistryObject<Block> STAR_DUST_BRICKS_STAIRS = stairs(
            "star_dust_bricks_stairs", STAR_DUST_BRICKS, MapColor.COLOR_BLUE, 2.0F, 6.0F);
    public static final RegistryObject<Block> STAR_DUST_BRICKS_SLAB = slab(
            "star_dust_bricks_slab", MapColor.COLOR_BLUE, 2.0F, 6.0F);
    public static final RegistryObject<Block> STAR_DUST_BRICKS_WALL = wall(
            "star_dust_bricks_wall", MapColor.COLOR_BLUE, 2.0F, 6.0F);
    public static final RegistryObject<Block> STAR_RAIL_CUT_STONE_STAIRS = stairs(
            "star_rail_cut_stone_stairs", STAR_RAIL_CUT_STONE, MapColor.COLOR_BLUE, 2.0F, 6.0F);
    public static final RegistryObject<Block> STAR_RAIL_CUT_STONE_SLAB = slab(
            "star_rail_cut_stone_slab", MapColor.COLOR_BLUE, 2.0F, 6.0F);
    public static final RegistryObject<Block> STAR_RAIL_CUT_STONE_WALL = wall(
            "star_rail_cut_stone_wall", MapColor.COLOR_BLUE, 2.0F, 6.0F);

    public static final RegistryObject<Block> TERMINAL_ALLOY = block("terminal_alloy",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(4.0F, 8.0F)
                    .requiresCorrectToolForDrops());
    public static final RegistryObject<Block> TERMINAL_FLOOR_PANEL = block("terminal_floor_panel",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(3.0F, 7.0F)
                    .requiresCorrectToolForDrops());
    public static final RegistryObject<Block> TERMINAL_WARNING_TILE = block("terminal_warning_tile",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(3.0F, 7.0F)
                    .requiresCorrectToolForDrops());
    public static final RegistryObject<Block> TERMINAL_GRATE = block("terminal_grate",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(3.0F, 7.0F)
                    .noOcclusion().requiresCorrectToolForDrops());
    public static final RegistryObject<Block> TERMINAL_CABLE_TROUGH = block("terminal_cable_trough",
            () -> new TerminalCableTroughBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
                    .strength(3.0F, 7.0F).noOcclusion().requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> WEATHERED_TERMINAL_ALLOY_ORE = block("weathered_terminal_alloy_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(3.0F, 6.0F)
                    .requiresCorrectToolForDrops());
    public static final RegistryObject<Block> WEATHERED_TERMINAL_ALLOY = block("weathered_terminal_alloy",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(3.0F, 6.0F)
                    .requiresCorrectToolForDrops());
    public static final RegistryObject<Block> TERMINAL_LIGHT_STRIP = block("terminal_light_strip",
            () -> new TerminalLightStripBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN)
                    .strength(0.4F).noCollission().noOcclusion().lightLevel(state -> 7)));
    public static final RegistryObject<Block> TERMINAL_ALLOY_BRICKS = block("terminal_alloy_bricks",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(4.0F, 8.0F)
                    .requiresCorrectToolForDrops());
    public static final RegistryObject<Block> TERMINAL_ALLOY_BRICKS_STAIRS = stairs(
            "terminal_alloy_bricks_stairs", TERMINAL_ALLOY_BRICKS, MapColor.COLOR_GRAY, 4.0F, 8.0F);
    public static final RegistryObject<Block> TERMINAL_ALLOY_BRICKS_SLAB = slab(
            "terminal_alloy_bricks_slab", MapColor.COLOR_GRAY, 4.0F, 8.0F);
    public static final RegistryObject<Block> TERMINAL_ALLOY_BRICKS_WALL = wall(
            "terminal_alloy_bricks_wall", MapColor.COLOR_GRAY, 4.0F, 8.0F);
    public static final RegistryObject<Block> TERMINAL_ALLOY_PILLAR = block("terminal_alloy_pillar",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(4.0F, 8.0F)
                    .requiresCorrectToolForDrops());
    public static final RegistryObject<Block> OLD_RAIL_PLATFORM = block("old_rail_platform",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(3.0F, 6.0F)
                    .requiresCorrectToolForDrops());
    public static final RegistryObject<Block> BROKEN_RAIL = block("broken_rail",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(2.0F, 5.0F)
                    .requiresCorrectToolForDrops());
    public static final RegistryObject<Block> LUMINOUS_GLASS = block("luminous_glass",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.4F)
                    .noOcclusion().lightLevel(state -> 5));
    public static final RegistryObject<Block> TERMINAL_HULL_GLASS = block("terminal_hull_glass",
            () -> new GlassBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(0.3F).noOcclusion()));
    public static final RegistryObject<Block> TERMINAL_SIGNAL_LAMP = block("terminal_signal_lamp",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(1.5F, 3.0F)
                    .lightLevel(state -> 15));
    public static final RegistryObject<Block> STAR_RAIL_BEACON = block("star_rail_beacon",
            () -> new StarRailBeaconBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN)
                    .strength(2.5F, 5.0F).requiresCorrectToolForDrops().lightLevel(state -> 9)));
    public static final RegistryObject<Block> STAR_RAIL_TRAIN = block("star_rail_train",
            () -> new StarRailTrainBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE)
                    .strength(3.0F, 6.0F).noOcclusion().requiresCorrectToolForDrops()
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> STAR_RAIL_TRAIN_PART = StarRailSimMod.BLOCKS.register(
            "star_rail_train_part", () -> new StarRailTrainPartBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE).strength(3.0F, 6.0F).noOcclusion()
                    .requiresCorrectToolForDrops().pushReaction(PushReaction.BLOCK)));
    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<StarRailTrainBlockEntity>>
            STAR_RAIL_TRAIN_ENTITY = StarRailSimMod.BLOCK_ENTITY_TYPES.register("star_rail_train",
                    () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(
                            StarRailTrainBlockEntity::new, STAR_RAIL_TRAIN.get()).build(null));

    public static final RegistryObject<Item> STAR_RAIL_FRAGMENT = StarRailSimMod.ITEMS.register(
            "star_rail_fragment", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TERMINAL_ALLOY_INGOT = StarRailSimMod.ITEMS.register(
            "terminal_alloy_ingot", () -> new Item(new Item.Properties()));

    private StarRailWorldBlocks() {
    }

    /** Forces class initialization while the mod's DeferredRegisters are being assembled. */
    static void register() {
    }

    private static RegistryObject<Block> block(String id, BlockBehaviour.Properties properties) {
        return block(id, () -> new Block(properties));
    }

    private static RegistryObject<Block> stairs(String id, RegistryObject<Block> base,
                                                 MapColor color, float hardness, float resistance) {
        return block(id, () -> new StairBlock(base.get().defaultBlockState(), toolStone(color, hardness, resistance)));
    }

    private static RegistryObject<Block> slab(String id, MapColor color, float hardness, float resistance) {
        return block(id, () -> new SlabBlock(toolStone(color, hardness, resistance)));
    }

    private static RegistryObject<Block> wall(String id, MapColor color, float hardness, float resistance) {
        return block(id, () -> new WallBlock(toolStone(color, hardness, resistance)));
    }

    private static BlockBehaviour.Properties toolStone(MapColor color, float hardness, float resistance) {
        return BlockBehaviour.Properties.of().mapColor(color).strength(hardness, resistance)
                .requiresCorrectToolForDrops();
    }

    private static RegistryObject<Block> block(String id, Supplier<? extends Block> factory) {
        RegistryObject<Block> block = StarRailSimMod.BLOCKS.register(id, factory::get);
        RegistryObject<Item> item = StarRailSimMod.ITEMS.register(id,
                () -> id.equals("star_rail_train") ? new StarRailTrainItem(block.get(), new Item.Properties())
                        : new BlockItem(block.get(), new Item.Properties()));
        BLOCK_ITEMS.add(item);
        return block;
    }

    public static void addCreativeItems(CreativeModeTab.Output output) {
        for (RegistryObject<Item> item : BLOCK_ITEMS) output.accept(item.get());
        output.accept(STAR_RAIL_FRAGMENT.get());
        output.accept(TERMINAL_ALLOY_INGOT.get());
    }
}
