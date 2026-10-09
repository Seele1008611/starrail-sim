package com.starrail.sim;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

/** Shared registry lookups for the nine path-bound ruin anchors and reusable seals. */
public final class StarRailRuinContent {
    private static final Map<StarRailPath, RegistryObject<Block>> ANCHORS =
            new EnumMap<>(StarRailPath.class);
    private static final Map<StarRailPath, RegistryObject<Item>> KEYS =
            new EnumMap<>(StarRailPath.class);
    private static final Map<StarRailPath, RegistryObject<Item>> ANCHOR_ITEMS =
            new EnumMap<>(StarRailPath.class);

    private StarRailRuinContent() {
    }

    /** Called after the legacy Hunt registry objects exist, preserving their public IDs. */
    static void registerOtherPaths() {
        for (StarRailPath path : StarRailPath.values()) {
            if (!path.isRealPath()) continue;
            if (path == StarRailPath.HUNT) {
                ANCHORS.put(path, StarRailSimMod.HUNT_RUIN_ANCHOR);
                KEYS.put(path, StarRailSimMod.HUNT_RUIN_SUMMON_KEY);
                ANCHOR_ITEMS.put(path, StarRailSimMod.HUNT_RUIN_ANCHOR_ITEM);
                continue;
            }

            String id = path.getId() + "_ruin";
            RegistryObject<Block> anchor = StarRailSimMod.BLOCKS.register(id + "_anchor",
                    () -> new PathRuinAnchorBlock(path, BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLUE).strength(3.5F, 6.0F)
                            .requiresCorrectToolForDrops().pushReaction(PushReaction.BLOCK)));
            RegistryObject<Item> key = StarRailSimMod.ITEMS.register(id + "_summon_key",
                    () -> new PathRuinSummonKeyItem(path, new Item.Properties().stacksTo(1)));
            RegistryObject<Item> anchorItem = StarRailSimMod.ITEMS.register(id + "_anchor",
                    () -> new BlockItem(anchor.get(), new Item.Properties()));
            ANCHORS.put(path, anchor);
            KEYS.put(path, key);
            ANCHOR_ITEMS.put(path, anchorItem);
        }
    }

    public static RegistryObject<Block> anchor(StarRailPath path) {
        return ANCHORS.get(path);
    }

    public static RegistryObject<Item> key(StarRailPath path) {
        return KEYS.get(path);
    }

    public static RegistryObject<Item> anchorItem(StarRailPath path) {
        return ANCHOR_ITEMS.get(path);
    }

    public static Component pathName(StarRailPath path) {
        return Component.translatable("path.starrail_sim." + path.getId());
    }

    public static StarRailPath pathOf(Block block) {
        for (var entry : ANCHORS.entrySet()) {
            if (entry.getValue().get() == block) return entry.getKey();
        }
        return StarRailPath.NONE;
    }

    public static boolean isAnchor(Block block) {
        return pathOf(block).isRealPath();
    }
}
