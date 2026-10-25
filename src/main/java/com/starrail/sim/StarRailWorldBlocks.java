package com.starrail.sim;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

/** The reusable Star Trail Beacon block used by the train hubs and Path archive. */
public final class StarRailWorldBlocks {
    private static final List<RegistryObject<Item>> BLOCK_ITEMS = new ArrayList<>();

    public static final RegistryObject<Block> STAR_RAIL_BEACON = block("star_rail_beacon",
            () -> new StarRailBeaconBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN)
                    .strength(2.5F, 5.0F).requiresCorrectToolForDrops().lightLevel(state -> 9)));

    private StarRailWorldBlocks() {
    }

    /** Forces class initialization while the mod's DeferredRegisters are being assembled. */
    static void register() {
    }

    private static RegistryObject<Block> block(String id,
                                                java.util.function.Supplier<? extends Block> factory) {
        RegistryObject<Block> block = StarRailSimMod.BLOCKS.register(id, factory::get);
        RegistryObject<Item> item = StarRailSimMod.ITEMS.register(id,
                () -> new BlockItem(block.get(), new Item.Properties()));
        BLOCK_ITEMS.add(item);
        return block;
    }

    public static void addCreativeItems(CreativeModeTab.Output output) {
        for (RegistryObject<Item> item : BLOCK_ITEMS) output.accept(item.get());
    }
}
