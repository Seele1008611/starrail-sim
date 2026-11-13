package com.starrail.sim.client.jei;

import com.starrail.sim.KitchenRecipe;
import com.starrail.sim.PathKitchen;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Locale;

/** Six unordered inputs, reusable cookware, fuel example, product and returned containers. */
public final class KitchenRecipeCategory implements IRecipeCategory<KitchenRecipe> {
    private final IGuiHelper gui;
    private final IDrawable background;
    private final IDrawable icon;

    KitchenRecipeCategory(IGuiHelper gui) {
        this.gui = gui;
        background = gui.createBlankDrawable(176, 112);
        icon = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(PathKitchen.STATION.get()));
    }

    @Override public RecipeType<KitchenRecipe> getRecipeType() { return StarRailJeiPlugin.KITCHEN; }
    @Override public Component getTitle() { return Component.translatable("block.starrail_sim.kitchen_station"); }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, KitchenRecipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.inputs().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 2 + i % 3 * 18, 16 + i / 3 * 18)
                    .setBackground(gui.getSlotDrawable(), -1, -1).addItemStack(recipe.inputs().get(i).copy());
        }
        boolean pot = recipe.process() == KitchenRecipe.Process.STEW || recipe.process() == KitchenRecipe.Process.STEAM;
        builder.addSlot(RecipeIngredientRole.CATALYST, 78, 16)
                .setBackground(gui.getSlotDrawable(), -1, -1)
                .addItemStack(new ItemStack(pot ? PathKitchen.POT.get() : PathKitchen.PAN.get()))
                .addTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("jei.starrail_sim.kitchen.reusable")));
        // Coal is an example, not a fixed ingredient or a one-coal-per-dish requirement.
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 78, 44)
                .setBackground(gui.getSlotDrawable(), -1, -1).addItemStack(new ItemStack(Items.COAL))
                .addTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("jei.starrail_sim.kitchen.fuel_info")));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 134, 16)
                .setBackground(gui.getSlotDrawable(), -1, -1).addItemStack(recipe.output().copy());
        for (int i = 0; i < recipe.returns().size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 134 + i * 18, 44)
                    .setBackground(gui.getSlotDrawable(), -1, -1).addItemStack(recipe.returns().get(i).copy())
                    .addTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("jei.starrail_sim.kitchen.return_info")));
        }
    }

    @Override
    public void draw(KitchenRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, Component.translatable("gui.starrail_sim.kitchen.ingredients"), 1, 2, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.starrail_sim.kitchen.tool"), 72, 2, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.starrail_sim.kitchen.output"), 131, 2, 0x404040, false);
        graphics.drawString(font, Component.literal("→"), 108, 21, 0x805C30, false);
        graphics.drawString(font, Component.translatable("jei.starrail_sim.kitchen.fuel"), 72, 64, 0x606060, false);
        if (!recipe.returns().isEmpty()) {
            graphics.drawString(font, Component.translatable("jei.starrail_sim.kitchen.returns"), 131, 64, 0x606060, false);
        }
        graphics.drawString(font, Component.translatable("gui.starrail_sim.kitchen."
                + recipe.process().name().toLowerCase(Locale.ROOT)), 1, 80, 0x404040, false);
        graphics.drawString(font, Component.translatable("jei.starrail_sim.kitchen.time",
                String.format(Locale.ROOT, "%.1f", recipe.ticks() / 20.0)), 91, 80, 0x404040, false);
        graphics.drawString(font, Component.translatable("jei.starrail_sim.kitchen.manual"), 1, 98, 0x606060, false);
    }
}
