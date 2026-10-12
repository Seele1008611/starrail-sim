package com.starrail.sim;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/** Relic equipment and material item registry for the five currently planned sets. */
public final class StarRailRelicItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, StarRailSimMod.MOD_ID);

    public static final RegistryObject<Item> LOST_CRYSTAL = material("lost_crystal");
    public static final RegistryObject<Item> RELIC_REMAINS = material("relic_remains");
    public static final RegistryObject<Item> SELF_MODELING_RESIN = material("self_modeling_resin");
    public static final RegistryObject<Item> VARIABLE_DICE = material("variable_dice");
    public static final RegistryObject<Item> INTERFERENCE_KEY = ITEMS.register("interference_key",
            () -> new InterferenceKeyItem(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<RelicItem> MUSKETEER_HEAD = register("relic_musketeer_head", RelicStackData.SetId.MUSKETEER, RelicStackData.Part.HEAD);
    public static final RegistryObject<RelicItem> MUSKETEER_HANDS = register("relic_musketeer_hands", RelicStackData.SetId.MUSKETEER, RelicStackData.Part.HANDS);
    public static final RegistryObject<RelicItem> MUSKETEER_BODY = register("relic_musketeer_body", RelicStackData.SetId.MUSKETEER, RelicStackData.Part.BODY);
    public static final RegistryObject<RelicItem> MUSKETEER_FEET = register("relic_musketeer_feet", RelicStackData.SetId.MUSKETEER, RelicStackData.Part.FEET);
    public static final RegistryObject<RelicItem> PASSERBY_HEAD = register("relic_passerby_head", RelicStackData.SetId.PASSERBY, RelicStackData.Part.HEAD);
    public static final RegistryObject<RelicItem> PASSERBY_HANDS = register("relic_passerby_hands", RelicStackData.SetId.PASSERBY, RelicStackData.Part.HANDS);
    public static final RegistryObject<RelicItem> PASSERBY_BODY = register("relic_passerby_body", RelicStackData.SetId.PASSERBY, RelicStackData.Part.BODY);
    public static final RegistryObject<RelicItem> PASSERBY_FEET = register("relic_passerby_feet", RelicStackData.SetId.PASSERBY, RelicStackData.Part.FEET);
    public static final RegistryObject<RelicItem> KNIGHT_HEAD = register("relic_knight_head", RelicStackData.SetId.KNIGHT, RelicStackData.Part.HEAD);
    public static final RegistryObject<RelicItem> KNIGHT_HANDS = register("relic_knight_hands", RelicStackData.SetId.KNIGHT, RelicStackData.Part.HANDS);
    public static final RegistryObject<RelicItem> KNIGHT_BODY = register("relic_knight_body", RelicStackData.SetId.KNIGHT, RelicStackData.Part.BODY);
    public static final RegistryObject<RelicItem> KNIGHT_FEET = register("relic_knight_feet", RelicStackData.SetId.KNIGHT, RelicStackData.Part.FEET);
    public static final RegistryObject<RelicItem> SPACE_STATION_SPHERE = register("relic_space_station_sphere", RelicStackData.SetId.SPACE_STATION, RelicStackData.Part.SPHERE);
    public static final RegistryObject<RelicItem> SPACE_STATION_ROPE = register("relic_space_station_rope", RelicStackData.SetId.SPACE_STATION, RelicStackData.Part.ROPE);
    public static final RegistryObject<RelicItem> FLEET_SPHERE = register("relic_fleet_sphere", RelicStackData.SetId.FLEET, RelicStackData.Part.SPHERE);
    public static final RegistryObject<RelicItem> FLEET_ROPE = register("relic_fleet_rope", RelicStackData.SetId.FLEET, RelicStackData.Part.ROPE);

    public static final List<RegistryObject<RelicItem>> ALL = List.of(
            MUSKETEER_HEAD, MUSKETEER_HANDS, MUSKETEER_BODY, MUSKETEER_FEET,
            PASSERBY_HEAD, PASSERBY_HANDS, PASSERBY_BODY, PASSERBY_FEET,
            KNIGHT_HEAD, KNIGHT_HANDS, KNIGHT_BODY, KNIGHT_FEET,
            SPACE_STATION_SPHERE, SPACE_STATION_ROPE, FLEET_SPHERE, FLEET_ROPE);

    private StarRailRelicItems() {}

    private static RegistryObject<RelicItem> register(String id, RelicStackData.SetId set,
                                                       RelicStackData.Part part) {
        return ITEMS.register(id, () -> new RelicItem(set, part));
    }

    private static RegistryObject<Item> material(String id) {
        return ITEMS.register(id, () -> new Item(new Item.Properties().stacksTo(64)));
    }

    public static ItemStack createGeneratedStack(RelicStackData.SetId set, RelicStackData.Part part,
                                                 int stars, RandomSource random) {
        return createGeneratedStack(set, part, stars, null, random);
    }

    public static ItemStack createGeneratedStack(RelicStackData.SetId set, RelicStackData.Part part,
                                                 int stars, RelicStackData.Stat forcedMainStat,
                                                 RandomSource random) {
        return createGeneratedStack(set, part, stars, forcedMainStat, List.of(), random);
    }

    public static ItemStack createGeneratedStack(RelicStackData.SetId set, RelicStackData.Part part,
                                                 int stars, RelicStackData.Stat forcedMainStat,
                                                 List<RelicStackData.Stat> forcedSubstats,
                                                 RandomSource random) {
        RelicItem item = ALL.stream().map(RegistryObject::get)
                .filter(candidate -> candidate.matches(set, part))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("No relic item for set/part"));
        ItemStack stack = new ItemStack(item);
        RelicStackData.Data data = RelicStackData.generate(set, part, stars, forcedMainStat, forcedSubstats, random);
        RelicStackData.write(stack, data);
        return stack;
    }

    /** Adds valid two-star +0 examples for quick item/slot inspection in Creative. */
    public static void addCreativeItems(CreativeModeTab.Output output) {
        for (RegistryObject<RelicItem> entry : ALL) {
            RelicItem item = entry.get();
            ItemStack example = item.createStack(2, item.part().defaultMainStat());
            output.accept(example);
        }
        output.accept(LOST_CRYSTAL.get());
        output.accept(RELIC_REMAINS.get());
        output.accept(SELF_MODELING_RESIN.get());
        output.accept(VARIABLE_DICE.get());
        output.accept(INTERFERENCE_KEY.get());
    }
}
