package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;

import java.util.List;

final class KitchenBlockEntity extends BlockEntity {
    static final int INPUTS = 6, FUEL = 6, TOOL = 7, OUTPUT = 8, RETURN = 9, SIZE = 11;
    private ResourceLocation activeRecipe;
    private ResourceLocation lastRecipe;
    private int progress;
    private int duration;
    private int fuel;
    private NonNullList<ItemStack> snapshot = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    final SimpleContainer inventory = new SimpleContainer(SIZE) {
        @Override public void setChanged() {
            super.setChanged();
            KitchenBlockEntity.this.setChanged();
        }
    };
    final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> duration;
                case 2 -> Math.min(fuel, 32000);
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 3; }
    };

    KitchenBlockEntity(BlockPos pos, BlockState state) { super(PathKitchen.ENTITY.get(), pos, state); }

    boolean start(KitchenRecipe recipe) {
        if (level == null || activeRecipe != null || !recipe.matches(inventory, level)) return false;
        activeRecipe = recipe.getId();
        duration = recipe.ticks();
        progress = 0;
        snapshotInputs();
        setChanged();
        return true;
    }

    private void snapshotInputs() {
        for (int i = 0; i < INPUTS; i++) snapshot.set(i, inventory.getItem(i).copy());
        snapshot.set(TOOL, inventory.getItem(TOOL).copy());
    }

    ResourceLocation rememberedRecipe() {
        return activeRecipe != null ? activeRecipe : lastRecipe;
    }

    private boolean inputsUnchanged() {
        for (int i = 0; i < INPUTS; i++) {
            if (!ItemStack.matches(snapshot.get(i), inventory.getItem(i))) return false;
        }
        return ItemStack.matches(snapshot.get(TOOL), inventory.getItem(TOOL));
    }

    void tick() {
        if (activeRecipe == null || level == null) return;
        var recipe = level.getRecipeManager().byKey(activeRecipe).orElse(null);
        if (!(recipe instanceof KitchenRecipe meal) || duration != meal.ticks()
                || !inputsUnchanged() || !meal.matches(inventory, level)) {
            reset();
            return;
        }
        ItemStack merged = merge(inventory.getItem(OUTPUT), meal.output());
        if (merged.isEmpty()) return;
        ItemStack[] returns = {inventory.getItem(RETURN).copy(), inventory.getItem(RETURN + 1).copy()};
        if (fuel <= 0) {
            ItemStack stack = inventory.getItem(FUEL);
            int burn = burnTime(stack);
            if (burn <= 0) return;
            ItemStack remainder = stack.getCraftingRemainingItem();
            ItemStack[] fuelReturns = returns.clone();
            if (!remainder.isEmpty() && !fitReturns(fuelReturns, List.of(remainder))) return;
            returns = fuelReturns.clone();
            if (!fitReturns(returns, meal.returns())) return;
            if (!remainder.isEmpty()) applyReturns(fuelReturns);
            inventory.removeItem(FUEL, 1);
            fuel = burn;
        } else if (!fitReturns(returns, meal.returns())) return;
        fuel--;
        progress++;
        if (progress >= duration) {
            for (ItemStack input : meal.inputs()) {
                int remaining = input.getCount();
                for (int i = 0; i < INPUTS && remaining > 0; i++) {
                    ItemStack inSlot = inventory.getItem(i);
                    if (!ItemStack.isSameItemSameTags(input, inSlot)) continue;
                    remaining -= inventory.removeItem(i, Math.min(remaining, inSlot.getCount())).getCount();
                }
            }
            inventory.setItem(OUTPUT, merged);
            applyReturns(returns);
            lastRecipe = meal.getId();
            if (meal.matches(inventory, level)) {
                progress = 0;
                snapshotInputs();
            } else {
                reset();
            }
        }
        setChanged();
    }

    static int burnTime(ItemStack stack) { return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING); }

    private static ItemStack merge(ItemStack existing, ItemStack added) {
        if (existing.isEmpty()) return added.copy();
        if (!ItemStack.isSameItemSameTags(existing, added)
                || existing.getCount() + added.getCount() > existing.getMaxStackSize()) return ItemStack.EMPTY;
        ItemStack result = existing.copy();
        result.grow(added.getCount());
        return result;
    }

    private static boolean fitReturns(ItemStack[] slots, List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            int remaining = stack.getCount();
            for (int pass = 0; pass < 2 && remaining > 0; pass++) {
                for (int i = 0; i < slots.length && remaining > 0; i++) {
                    ItemStack existing = slots[i];
                    if (pass == 0 ? !ItemStack.isSameItemSameTags(existing, stack) : !existing.isEmpty()) continue;
                    int count = Math.min(remaining, stack.getMaxStackSize() - existing.getCount());
                    if (count <= 0) continue;
                    ItemStack combined = stack.copy();
                    combined.setCount(existing.getCount() + count);
                    slots[i] = combined;
                    remaining -= count;
                }
            }
            if (remaining > 0) return false;
        }
        return true;
    }

    private void applyReturns(ItemStack[] returns) {
        for (int i = 0; i < returns.length; i++) inventory.setItem(RETURN + i, returns[i]);
    }

    private void reset() {
        activeRecipe = null;
        progress = duration = 0;
        snapshot.clear();
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        var contents = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        for (int i = 0; i < SIZE; i++) contents.set(i, inventory.getItem(i));
        ContainerHelper.saveAllItems(tag, contents);
        tag.putInt("Fuel", fuel);
        if (lastRecipe != null) tag.putString("LastRecipe", lastRecipe.toString());
        if (activeRecipe != null) {
            tag.putString("Recipe", activeRecipe.toString());
            tag.putInt("Progress", progress);
            tag.putInt("Duration", duration);
            tag.put("Snapshot", ContainerHelper.saveAllItems(new CompoundTag(), snapshot));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        var contents = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, contents);
        for (int i = 0; i < SIZE; i++) inventory.setItem(i, contents.get(i));
        fuel = Math.max(0, tag.getInt("Fuel"));
        activeRecipe = tag.contains("Recipe", Tag.TAG_STRING)
                ? ResourceLocation.tryParse(tag.getString("Recipe")) : null;
        lastRecipe = tag.contains("LastRecipe", Tag.TAG_STRING)
                ? ResourceLocation.tryParse(tag.getString("LastRecipe")) : activeRecipe;
        duration = tag.getInt("Duration");
        progress = tag.getInt("Progress");
        snapshot.clear();
        ContainerHelper.loadAllItems(tag.getCompound("Snapshot"), snapshot);
        if (duration < 1 || progress < 0 || progress >= duration) activeRecipe = null;
        if (activeRecipe == null) progress = duration = 0;
    }
}
