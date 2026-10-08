package com.starrail.sim;

import java.util.EnumMap;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

/** Nine path-specific upgrade materials, using the approved cutout artwork. */
public final class StarRailTraceMaterials {
    private static final EnumMap<StarRailPath, RegistryObject<Item>> ITEMS = new EnumMap<>(StarRailPath.class);
    static {
        register(StarRailPath.HUNT, "star_chasing_arrow");
        register(StarRailPath.ERUDITION, "key_of_wisdom");
        register(StarRailPath.ABUNDANCE, "flower_of_eternity");
        register(StarRailPath.HARMONY, "stellar_symphony");
        register(StarRailPath.DESTRUCTION, "worldbreaker_blade");
        register(StarRailPath.PRESERVATION, "amber_safeguard");
        register(StarRailPath.NIHILITY, "obsidian_of_obsession");
        register(StarRailPath.REMEMBRANCE, "alaya_blossom");
        register(StarRailPath.ELATION, "fluffy_collection");
    }
    private StarRailTraceMaterials() { }
    private static void register(StarRailPath path, String id) {
        ITEMS.put(path, StarRailSimMod.ITEMS.register(id, () -> new Item(new Item.Properties())));
    }
    public static void register() { /* Force static registration before the event bus fires. */ }
    public static Item get(StarRailPath path) { return ITEMS.get(path).get(); }
    public static StarRailPath pathOf(Item item) {
        for (var entry : ITEMS.entrySet()) {
            if (entry.getValue().get() == item) {
                return entry.getKey();
            }
        }
        return StarRailPath.NONE;
    }

    public static String displayName(StarRailPath path) {
        return switch (path) {
            case ELATION -> "《绒绒号》典藏版合集";
            case REMEMBRANCE -> "阿赖耶华";
            case NIHILITY -> "沉沦黑曜";
            case PRESERVATION -> "琥珀的坚守";
            case DESTRUCTION -> "净世残刃";
            case HARMONY -> "群星乐章";
            case ABUNDANCE -> "永恒之花";
            case ERUDITION -> "智识之钥";
            case HUNT -> "逐星之矢";
            default -> "行迹材料";
        };
    }
}
