package com.starrail.sim;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;

/** Server-authoritative light cone swaps between the player's inventory and Curios slot. */
public final class StarRailLightConeEquipService {
    private static final String SLOT_ID = "light_cone";
    private static final TagKey<Item> LIGHT_CONES = ItemTags.create(
            new ResourceLocation("curios", "light_cone"));

    public record Result(boolean accepted, String translationKey) { }

    private StarRailLightConeEquipService() { }

    public static Result equip(ServerPlayer player, int inventorySlot,
                               ResourceLocation expectedItem, int expectedSuperimposition) {
        if (player == null || player.isRemoved() || player.level().isClientSide()
                || expectedItem == null) {
            return fail("invalid");
        }
        Inventory inventory = player.getInventory();
        if (inventorySlot < 0 || inventorySlot >= inventory.items.size()) return fail("stale");

        ItemStack source = inventory.getItem(inventorySlot);
        if (source.isEmpty() || source.getCount() != 1 || !source.is(LIGHT_CONES)
                || !expectedItem.equals(BuiltInRegistries.ITEM.getKey(source.getItem()))
                || superimposition(source) != expectedSuperimposition) {
            return fail("stale");
        }

        IItemHandlerModifiable slots = lightConeSlots(player);
        if (slots == null) return fail("slot_missing");
        int targetSlot = -1;
        for (int slot = 0; slot < slots.getSlots(); slot++) {
            if (slots.isItemValid(slot, source)) {
                targetSlot = slot;
                break;
            }
        }
        if (targetSlot < 0) return fail("not_equipable");

        ItemStack previous = slots.getStackInSlot(targetSlot).copy();
        ItemStack incoming = source.copy();
        slots.setStackInSlot(targetSlot, incoming);
        inventory.setItem(inventorySlot, previous);
        inventory.setChanged();
        syncInventory(player);
        return new Result(true, "message.starrail_sim.light_cone.equipped");
    }

    public static Result unequip(ServerPlayer player) {
        if (player == null || player.isRemoved() || player.level().isClientSide()) return fail("invalid");
        IItemHandlerModifiable slots = lightConeSlots(player);
        if (slots == null) return fail("slot_missing");

        int equippedSlot = -1;
        for (int slot = 0; slot < slots.getSlots(); slot++) {
            if (!slots.getStackInSlot(slot).isEmpty()) {
                equippedSlot = slot;
                break;
            }
        }
        if (equippedSlot < 0) return new Result(true, "message.starrail_sim.light_cone.none_equipped");

        Inventory inventory = player.getInventory();
        int emptySlot = -1;
        for (int slot = 0; slot < inventory.items.size(); slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                emptySlot = slot;
                break;
            }
        }
        if (emptySlot < 0) return fail("inventory_full");

        ItemStack removed = slots.getStackInSlot(equippedSlot).copy();
        slots.setStackInSlot(equippedSlot, ItemStack.EMPTY);
        inventory.setItem(emptySlot, removed);
        inventory.setChanged();
        syncInventory(player);
        return new Result(true, "message.starrail_sim.light_cone.unequipped");
    }

    private static IItemHandlerModifiable lightConeSlots(ServerPlayer player) {
        IItemHandlerModifiable[] result = new IItemHandlerModifiable[1];
        CuriosApi.getCuriosInventory(player).ifPresent(inventory ->
                inventory.getStacksHandler(SLOT_ID).ifPresent(handler -> result[0] = handler.getStacks()));
        return result[0];
    }

    private static int superimposition(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt("Superimposition") : 0;
    }

    private static void syncInventory(ServerPlayer player) {
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
    }

    private static Result fail(String reason) {
        return new Result(false, "message.starrail_sim.light_cone." + reason);
    }
}
