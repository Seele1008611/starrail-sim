package com.starrail.sim.client.jei;

import com.starrail.sim.KitchenRecipe;
import com.starrail.sim.PathKitchen;
import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.client.KitchenScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

/** Discovered by JEI only; common mod initialization never references this class. */
@JeiPlugin
public final class StarRailJeiPlugin implements IModPlugin {
    public static final RecipeType<KitchenRecipe> KITCHEN = RecipeType.create(
            StarRailSimMod.MOD_ID, "kitchen", KitchenRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(StarRailSimMod.MOD_ID, "kitchen_jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new KitchenRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        // Use the server-synchronized recipe manager, including datapack changes.
        var recipes = level.getRecipeManager().getAllRecipesFor(PathKitchen.TYPE.get()).stream()
                .sorted(Comparator.comparing(KitchenRecipe::getId)).toList();
        registration.addRecipes(KITCHEN, recipes);
        registration.addItemStackInfo(new ItemStack(PathKitchen.STATION.get()),
                Component.translatable("jei.starrail_sim.kitchen.intro"),
                Component.translatable("jei.starrail_sim.kitchen.operation"),
                Component.translatable("jei.starrail_sim.kitchen.batch"));
        registration.addItemStackInfo(new ItemStack(PathKitchen.POT.get()),
                Component.translatable("jei.starrail_sim.kitchen.pot_info"));
        registration.addItemStackInfo(new ItemStack(PathKitchen.PAN.get()),
                Component.translatable("jei.starrail_sim.kitchen.pan_info"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(PathKitchen.STATION.get()), KITCHEN);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(KitchenScreen.class, 108, 34, 28, 16, KITCHEN);
    }
}
