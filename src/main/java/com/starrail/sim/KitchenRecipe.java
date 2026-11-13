package com.starrail.sim;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public record KitchenRecipe(ResourceLocation id, Process process, int ticks, List<ItemStack> inputs,
                            ItemStack output, List<ItemStack> returns) implements Recipe<Container> {
    public enum Process {
        STEW(160), STEAM(120), FRY(100), DRY(160), BAKE(200);
        final int ticks;
        Process(int ticks) { this.ticks = ticks; }
    }

    public KitchenRecipe {
        inputs = List.copyOf(inputs);
        returns = List.copyOf(returns);
        if (inputs.isEmpty() || inputs.size() > KitchenBlockEntity.INPUTS || returns.size() > 2
                || ticks < 1 || ticks > 32000 || !valid(output)
                || inputs.stream().anyMatch(stack -> !valid(stack)) || returns.stream().anyMatch(stack -> !valid(stack))) {
            throw new JsonSyntaxException("Invalid kitchen recipe " + id);
        }
        var ingredients = new HashSet<>();
        for (ItemStack input : inputs) {
            if (!ingredients.add(input.getItem())) throw new JsonSyntaxException("Repeated ingredient in " + id);
        }
    }

    private static boolean valid(ItemStack stack) {
        return !stack.isEmpty() && stack.getCount() <= stack.getMaxStackSize();
    }

    static List<KitchenRecipe> sorted(Level level) {
        return level.getRecipeManager().getAllRecipesFor(PathKitchen.TYPE.get()).stream()
                .sorted(Comparator.comparing(KitchenRecipe::getId)).toList();
    }

    public boolean uses(ItemStack tool) {
        return tool.is(process == Process.STEW || process == Process.STEAM ? PathKitchen.POT.get() : PathKitchen.PAN.get());
    }

    @Override
    public boolean matches(Container inventory, Level level) {
        if (!uses(inventory.getItem(KitchenBlockEntity.TOOL))) return false;
        for (int i = 0; i < KitchenBlockEntity.INPUTS; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && inputs.stream().noneMatch(input -> ItemStack.isSameItemSameTags(input, stack))) return false;
        }
        for (ItemStack input : inputs) {
            int count = 0;
            for (int i = 0; i < KitchenBlockEntity.INPUTS; i++) {
                ItemStack stack = inventory.getItem(i);
                if (ItemStack.isSameItemSameTags(input, stack)) count += stack.getCount();
            }
            if (count < input.getCount()) return false;
        }
        return true;
    }

    @Override public ItemStack assemble(Container inventory, RegistryAccess registries) { return output.copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= inputs.size(); }
    @Override public ItemStack getResultItem(RegistryAccess registries) { return output.copy(); }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return PathKitchen.SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return PathKitchen.TYPE.get(); }
    @Override public boolean isSpecial() { return true; }

    static final class Serializer implements RecipeSerializer<KitchenRecipe> {
        @Override
        public KitchenRecipe fromJson(ResourceLocation id, JsonObject json) {
            Process process = Process.valueOf(GsonHelper.getAsString(json, "process").toUpperCase(java.util.Locale.ROOT));
            return new KitchenRecipe(id, process, GsonHelper.getAsInt(json, "ticks", process.ticks),
                    readStacks(json, "inputs"), ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result")),
                    json.has("returns") ? readStacks(json, "returns") : List.of());
        }

        private static List<ItemStack> readStacks(JsonObject json, String key) {
            var result = new ArrayList<ItemStack>();
            for (var element : GsonHelper.getAsJsonArray(json, key)) {
                result.add(ShapedRecipe.itemStackFromJson(element.getAsJsonObject()));
            }
            return result;
        }

        @Override
        public KitchenRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new KitchenRecipe(id, buffer.readEnum(Process.class), buffer.readVarInt(),
                    buffer.readList(FriendlyByteBuf::readItem), buffer.readItem(), buffer.readList(FriendlyByteBuf::readItem));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, KitchenRecipe recipe) {
            buffer.writeEnum(recipe.process);
            buffer.writeVarInt(recipe.ticks);
            buffer.writeCollection(recipe.inputs, FriendlyByteBuf::writeItem);
            buffer.writeItem(recipe.output);
            buffer.writeCollection(recipe.returns, FriendlyByteBuf::writeItem);
        }
    }
}
