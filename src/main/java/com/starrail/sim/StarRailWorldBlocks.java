package com.starrail.sim;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Train and beacon blocks used to enter, navigate, and return from The Path. */
public final class StarRailWorldBlocks {
    private static final List<RegistryObject<Item>> BLOCK_ITEMS = new ArrayList<>();

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

    private StarRailWorldBlocks() {
    }

    /** Forces class initialization while the mod's DeferredRegisters are being assembled. */
    static void register() {
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
    }
}
