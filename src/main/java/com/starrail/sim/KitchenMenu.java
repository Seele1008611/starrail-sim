package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class KitchenMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final KitchenBlockEntity kitchen;
    private final SimpleContainer inventory;
    private final ContainerData data;
    private final Player player;
    private ResourceLocation initialRecipe;

    public KitchenMenu(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(id, playerInventory, buffer.readBlockPos(), null);
        if (buffer.isReadable()) initialRecipe = buffer.readNullable(FriendlyByteBuf::readResourceLocation);
    }

    KitchenMenu(int id, Inventory playerInventory, BlockPos pos, KitchenBlockEntity kitchen) {
        super(PathKitchen.MENU.get(), id);
        this.pos = pos;
        this.kitchen = kitchen;
        player = playerInventory.player;
        inventory = kitchen == null ? new SimpleContainer(KitchenBlockEntity.SIZE) : kitchen.inventory;
        data = kitchen == null ? new SimpleContainerData(3) : kitchen.data;
        addDataSlots(data);
        for (int i = 0; i < 6; i++) addSlot(new Slot(inventory, i, 16 + i % 3 * 18, 32 + i / 3 * 18));
        addSlot(new Slot(inventory, 6, 87, 50) {
            @Override public boolean mayPlace(ItemStack stack) { return KitchenBlockEntity.burnTime(stack) > 0; }
        });
        addSlot(new Slot(inventory, 7, 87, 32) {
            @Override public boolean mayPlace(ItemStack stack) { return isCookware(stack); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(resultSlot(8, 143, 32));
        addSlot(resultSlot(9, 143, 50));
        addSlot(resultSlot(10, 161, 50));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 16 + column * 18, 132 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(playerInventory, column, 16 + column * 18, 190));
    }

    private Slot resultSlot(int slot, int x, int y) {
        return new Slot(inventory, slot, x, y) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        };
    }

    private static boolean isCookware(ItemStack stack) { return stack.is(PathKitchen.POT.get()) || stack.is(PathKitchen.PAN.get()); }
    public List<KitchenRecipe> recipes() { return KitchenRecipe.sorted(player.level()); }
    public int initialSelection() {
        var recipes = recipes();
        for (int i = 0; i < recipes.size(); i++) {
            if (recipes.get(i).getId().equals(initialRecipe)) return i;
        }
        return 0;
    }
    public boolean matches(KitchenRecipe recipe) { return recipe.matches(inventory, player.level()); }
    public int progress() { return data.get(0); }
    public int duration() { return data.get(1); }
    public int fuel() { return data.get(2); }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer) || kitchen == null || !stillValid(player)) return false;
        var recipes = recipes();
        return id >= 0 && id < recipes.size() && kitchen.start(recipes.get(id));
    }

    @Override
    public boolean stillValid(Player player) {
        return kitchen == null || (player.level().getBlockEntity(pos) == kitchen
                && player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) <= 64);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack old = stack.copy();
        if (index < KitchenBlockEntity.SIZE) {
            if (!moveItemStackTo(stack, KitchenBlockEntity.SIZE, slots.size(), true)) return ItemStack.EMPTY;
        } else if (isCookware(stack)) {
            if (!moveItemStackTo(stack, 7, 8, false)) return ItemStack.EMPTY;
        } else if (KitchenBlockEntity.burnTime(stack) > 0) {
            if (!moveItemStackTo(stack, 6, 7, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, KitchenBlockEntity.INPUTS, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, stack);
        return old;
    }
}
