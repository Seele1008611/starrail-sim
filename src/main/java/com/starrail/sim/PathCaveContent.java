package com.starrail.sim;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

/** Custom crystal geode blocks for the Path dimension. */
public final class PathCaveContent {
    private static final List<RegistryObject<Item>> CREATIVE_ITEMS = new ArrayList<>();

    public static final RegistryObject<Block> STARVEIN_CRYSTAL = block("starvein_crystal", () ->
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN)
                    .strength(2.0F, 3.0F).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)));
    public static final RegistryObject<Block> STARVEIN_GEODE_CRUST = block("starvein_geode_crust", () ->
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN)
                    .strength(2.0F, 3.0F).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> STARVEIN_GEODE_SHELL = block("starvein_geode_shell", () ->
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE)
                    .strength(2.5F, 4.0F).requiresCorrectToolForDrops().sound(SoundType.STONE)));

    public static final RegistryObject<Block> BUDDING_STARVEIN_CRYSTAL = block(
            "budding_starvein_crystal", () -> new PathBuddingCrystalBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(1.5F)
                            .randomTicks().requiresCorrectToolForDrops().sound(SoundType.AMETHYST)));

    public static final RegistryObject<Block> SMALL_STARVEIN_BUD = crystalGrowthBlock(
            "small_starvein_bud", 7, 3, 1, SoundType.SMALL_AMETHYST_BUD);
    public static final RegistryObject<Block> MEDIUM_STARVEIN_BUD = crystalGrowthBlock(
            "medium_starvein_bud", 10, 2, 2, SoundType.MEDIUM_AMETHYST_BUD);
    public static final RegistryObject<Block> LARGE_STARVEIN_BUD = crystalGrowthBlock(
            "large_starvein_bud", 13, 2, 4, SoundType.LARGE_AMETHYST_BUD);
    public static final RegistryObject<Block> STARVEIN_CRYSTAL_CLUSTER = crystalGrowthBlock(
            "starvein_crystal_cluster", 13, 1, 5, SoundType.AMETHYST_CLUSTER);

    private PathCaveContent() {
    }

    /** Forces this class's deferred registry entries to initialize. */
    static void register() {
    }

    public static void addCreativeItems(CreativeModeTab.Output output) {
        for (RegistryObject<Item> item : CREATIVE_ITEMS) {
            output.accept(item.get());
        }
    }

    private static RegistryObject<Block> crystalGrowthBlock(String id, int outlineHeight, int outlineInset,
                                                              int light, SoundType sound) {
        return block(id, () -> new AmethystClusterBlock(outlineHeight, outlineInset,
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(1.0F)
                        .noOcclusion().lightLevel(state -> light).requiresCorrectToolForDrops()
                        .sound(sound)));
    }

    private static RegistryObject<Block> block(String id, java.util.function.Supplier<? extends Block> factory) {
        RegistryObject<Block> block = StarRailSimMod.BLOCKS.register(id, factory::get);
        RegistryObject<Item> item = StarRailSimMod.ITEMS.register(id,
                () -> new BlockItem(block.get(), new Item.Properties()));
        CREATIVE_ITEMS.add(item);
        return block;
    }
}
