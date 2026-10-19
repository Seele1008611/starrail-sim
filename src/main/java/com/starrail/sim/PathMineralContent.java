package com.starrail.sim;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

/** Mineral blocks and processed materials for the redesigned Path dimension. */
public final class PathMineralContent {
    private static final List<RegistryObject<Item>> BLOCK_ITEMS = new ArrayList<>();
    private static final List<RegistryObject<Item>> MATERIAL_ITEMS = new ArrayList<>();

    public static final RegistryObject<Block> STARCOAL_ORE = ore("path_starcoal_ore", 3.0F);
    public static final RegistryObject<Block> DAWNCOPPER_ORE = ore("path_dawncopper_ore", 3.0F);
    public static final RegistryObject<Block> SUNIRON_ORE = ore("path_suniron_ore", 3.0F);
    public static final RegistryObject<Block> DAWN_GOLD_ORE = ore("path_dawn_gold_ore", 3.5F);
    public static final RegistryObject<Block> RESONANCE_ORE = ore("path_resonance_ore", 3.5F);
    public static final RegistryObject<Block> MEMORY_ORE = ore("path_memory_ore", 3.0F);
    public static final RegistryObject<Block> STARMARROW_ORE = ore("path_starmarrow_ore", 4.0F);
    public static final RegistryObject<Block> STELLARJADE_ORE = ore("path_stellarjade_ore", 4.0F);
    public static final RegistryObject<Block> ANCIENTDREAM_ORE = ore("path_ancientdream_ore", 4.5F);

    public static final RegistryObject<Block> STARCOAL_BLOCK = storage("path_starcoal_block");
    public static final RegistryObject<Block> DAWNCOPPER_BLOCK = storage("path_dawncopper_block");
    public static final RegistryObject<Block> SUNIRON_BLOCK = storage("path_suniron_block");
    public static final RegistryObject<Block> DAWN_GOLD_BLOCK = storage("path_dawn_gold_block");
    public static final RegistryObject<Block> RESONANCE_BLOCK = storage("path_resonance_block");
    public static final RegistryObject<Block> MEMORY_BLOCK = storage("path_memory_block");
    public static final RegistryObject<Block> STARMARROW_BLOCK = storage("path_starmarrow_block");
    public static final RegistryObject<Block> STELLARJADE_BLOCK = storage("path_stellarjade_block");
    public static final RegistryObject<Block> ANCIENTDREAM_BLOCK = storage("path_ancientdream_block");

    public static final RegistryObject<Item> STARCOAL = item("path_starcoal");
    public static final RegistryObject<Item> RAW_DAWNCOPPER = item("path_raw_dawncopper");
    public static final RegistryObject<Item> DAWNCOPPER_INGOT = item("path_dawncopper_ingot");
    public static final RegistryObject<Item> RAW_SUNIRON = item("path_raw_suniron");
    public static final RegistryObject<Item> SUNIRON_INGOT = item("path_suniron_ingot");
    public static final RegistryObject<Item> SUNIRON_NUGGET = item("path_suniron_nugget");
    public static final RegistryObject<Item> RAW_DAWN_GOLD = item("path_raw_dawn_gold");
    public static final RegistryObject<Item> DAWN_GOLD_INGOT = item("path_dawn_gold_ingot");
    public static final RegistryObject<Item> DAWN_GOLD_NUGGET = item("path_dawn_gold_nugget");
    public static final RegistryObject<Item> RESONANCE_CRYSTAL = item("path_resonance_crystal");
    public static final RegistryObject<Item> MEMORY_CRYSTAL = item("path_memory_crystal");
    public static final RegistryObject<Item> STARMARROW = item("path_starmarrow");
    public static final RegistryObject<Item> RAW_STELLARJADE = item("path_raw_stellarjade");
    public static final RegistryObject<Item> STELLARJADE_INGOT = item("path_stellarjade_ingot");
    public static final RegistryObject<Item> RAW_ANCIENTDREAM = item("path_raw_ancientdream");
    public static final RegistryObject<Item> ANCIENTDREAM_INGOT = item("path_ancientdream_ingot");

    private PathMineralContent() {
    }

    /** Forces registration fields to initialize while the mod's DeferredRegisters are assembled. */
    static void register() {
    }

    public static void addCreativeItems(CreativeModeTab.Output output) {
        for (RegistryObject<Item> item : BLOCK_ITEMS) output.accept(item.get());
        for (RegistryObject<Item> item : MATERIAL_ITEMS) output.accept(item.get());
    }

    private static RegistryObject<Block> ore(String id, float hardness) {
        return block(id, BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                .strength(hardness, 6.0F).requiresCorrectToolForDrops().sound(SoundType.STONE));
    }

    private static RegistryObject<Block> storage(String id) {
        return block(id, BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                .strength(4.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    }

    private static RegistryObject<Block> block(String id, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = StarRailSimMod.BLOCKS.register(id, () -> new Block(properties));
        RegistryObject<Item> item = StarRailSimMod.ITEMS.register(id,
                () -> new BlockItem(block.get(), new Item.Properties()));
        BLOCK_ITEMS.add(item);
        return block;
    }

    private static RegistryObject<Item> item(String id) {
        RegistryObject<Item> item = StarRailSimMod.ITEMS.register(id, () -> new Item(new Item.Properties()));
        MATERIAL_ITEMS.add(item);
        return item;
    }
}
