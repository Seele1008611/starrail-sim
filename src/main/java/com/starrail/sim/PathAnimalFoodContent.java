package com.starrail.sim;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

/** Meat drops and cooked food for the Path dimension's wildlife. */
public final class PathAnimalFoodContent {
    public static final RegistryObject<Item> SUNHARVEST_MEAT = meat("sunharvest_meat", 3, 0.3F);
    public static final RegistryObject<Item> COOKED_SUNHARVEST_MEAT = meat("cooked_sunharvest_meat", 7, 0.8F);
    public static final RegistryObject<Item> STARFLEECE_MUTTON = meat("starfleece_mutton", 2, 0.3F);
    public static final RegistryObject<Item> COOKED_STARFLEECE_MUTTON = meat("cooked_starfleece_mutton", 6, 0.8F);
    public static final RegistryObject<Item> WOODLAND_MEAT = meat("woodland_meat", 3, 0.3F);
    public static final RegistryObject<Item> COOKED_WOODLAND_MEAT = meat("cooked_woodland_meat", 7, 0.8F);
    public static final RegistryObject<Item> STILLTIDE_FOWL_MEAT = meat("stilltide_fowl_meat", 2, 0.3F);
    public static final RegistryObject<Item> COOKED_STILLTIDE_FOWL_MEAT = meat("cooked_stilltide_fowl_meat", 6, 0.6F);

    private PathAnimalFoodContent() {
    }

    /** Forces all DeferredRegister entries in this class to be initialized. */
    static void register() {
    }

    public static void addCreativeItems(CreativeModeTab.Output output) {
        output.accept(SUNHARVEST_MEAT.get());
        output.accept(COOKED_SUNHARVEST_MEAT.get());
        output.accept(STARFLEECE_MUTTON.get());
        output.accept(COOKED_STARFLEECE_MUTTON.get());
        output.accept(WOODLAND_MEAT.get());
        output.accept(COOKED_WOODLAND_MEAT.get());
        output.accept(STILLTIDE_FOWL_MEAT.get());
        output.accept(COOKED_STILLTIDE_FOWL_MEAT.get());
    }

    private static RegistryObject<Item> meat(String id, int nutrition, float saturation) {
        return StarRailSimMod.ITEMS.register(id, () -> new Item(new Item.Properties().food(
                new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturation)
                        .meat().build())));
    }
}
