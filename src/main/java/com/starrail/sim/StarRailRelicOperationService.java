package com.starrail.sim;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Server-authoritative relic operations and their inventory/material transactions. */
public final class StarRailRelicOperationService {
    public enum Action {
        EQUIP, LOCK, ENHANCE, SALVAGE_CRYSTAL, SALVAGE_REMAINS,
        FORGE, CRAFT_RESIN, CRAFT_DICE, CRAFT_KEY, REROLL, KEEP_OLD, TAKE_NEW
    }

    public static final int INTERFERENCE_KEY_REMAINS_COST = 500;

    public record Request(Action action, UUID relicId, long expectedVersion, int value,
                          RelicStackData.SetId set, RelicStackData.Part part,
                          RelicStackData.Stat mainStat, boolean targetMain, int targetSubstatMask) {}

    public record Result(boolean accepted, String translationKey) {
        private static Result ok() { return new Result(true, "message.starrail_sim.relic.operation_success"); }
        private static Result fail(String reason) {
            return new Result(false, "message.starrail_sim.relic." + reason);
        }
    }

    private StarRailRelicOperationService() {}

    public static Result perform(ServerPlayer player, Request request) {
        if (player == null || request == null || request.action() == null
                || player.isRemoved() || player.level().isClientSide()) {
            return Result.fail("operation_invalid");
        }
        return switch (request.action()) {
            case EQUIP -> equip(player, request);
            case LOCK -> mutateRelic(player, request, Operation.LOCK);
            case ENHANCE -> mutateRelic(player, request, Operation.ENHANCE);
            case SALVAGE_CRYSTAL -> salvage(player, request, false);
            case SALVAGE_REMAINS -> salvage(player, request, true);
            case FORGE -> forge(player, request);
            case CRAFT_RESIN -> craft(player, StarRailRelicItems.SELF_MODELING_RESIN.get(), 200);
            case CRAFT_DICE -> craft(player, StarRailRelicItems.VARIABLE_DICE.get(), 300);
            case CRAFT_KEY -> craft(player, StarRailRelicItems.INTERFERENCE_KEY.get(), INTERFERENCE_KEY_REMAINS_COST);
            case REROLL -> mutateRelic(player, request, Operation.REROLL);
            case KEEP_OLD -> mutateRelic(player, request, Operation.KEEP_OLD);
            case TAKE_NEW -> mutateRelic(player, request, Operation.TAKE_NEW);
        };
    }

    private static Result equip(ServerPlayer player, Request request) {
        LocatedRelic target = locate(player, request.relicId());
        if (target == null) return Result.fail("relic_missing");
        RelicStackData.Data data = RelicStackData.read(target.stack()).orElse(null);
        if (data == null || data.operationVersion() != request.expectedVersion()) {
            return Result.fail("operation_stale");
        }
        if (data.hasPendingReroll()) return Result.fail("reroll_pending");

        var curiosOptional = CuriosApi.getCuriosInventory(player);
        if (!curiosOptional.isPresent()) return Result.fail("operation_invalid");
        var curios = curiosOptional.orElse(null);
        if (curios == null) return Result.fail("operation_invalid");
        var handlerOptional = curios.getStacksHandler(data.part().curiosSlot());
        if (handlerOptional.isEmpty()) return Result.fail("operation_invalid");
        IItemHandlerModifiable stacks = handlerOptional.orElseThrow().getStacks();
        Inventory inventory = player.getInventory();

        if (target.equipped()) {
            int equippedSlot = findCurioSlot(stacks, data.instanceId());
            if (equippedSlot < 0) return Result.fail("operation_stale");
            ItemStack equippedStack = stacks.getStackInSlot(equippedSlot);
            ItemStack toInventory = equippedStack.copy();
            RelicStackData.write(toInventory, RelicStackData.touch(data));
            if (!canReceive(inventory, toInventory)) return Result.fail("inventory_full");
            stacks.setStackInSlot(equippedSlot, ItemStack.EMPTY);
            if (!inventory.add(toInventory)) {
                stacks.setStackInSlot(equippedSlot, equippedStack);
                return Result.fail("inventory_full");
            }
            StarRailRelicService.refresh(player);
            return Result.ok();
        }

        if (target.stack().getItem() instanceof RelicItem item && !item.matches(data.set(), data.part())) {
            return Result.fail("operation_invalid");
        }
        int targetSlot = -1;
        int replaceSlot = -1;
        for (int slot = 0; slot < stacks.getSlots(); slot++) {
            ItemStack worn = stacks.getStackInSlot(slot);
            if (worn.isEmpty()) {
                if (targetSlot < 0) targetSlot = slot;
                continue;
            }
            RelicStackData.Data wornData = RelicStackData.read(worn).orElse(null);
            if (wornData != null && wornData.part() == data.part()) {
                if (replaceSlot >= 0) return Result.fail("operation_invalid");
                replaceSlot = slot;
            }
        }
        if (replaceSlot < 0) replaceSlot = targetSlot;
        if (replaceSlot < 0) return Result.fail("inventory_full");
        ItemStack oldStack = stacks.getStackInSlot(replaceSlot);
        ItemStack oldToInventory = oldStack.copy();
        if (!oldToInventory.isEmpty()) {
            RelicStackData.read(oldToInventory).ifPresent(oldData ->
                    RelicStackData.write(oldToInventory, RelicStackData.touch(oldData)));
            if (!canReceive(inventory, oldToInventory, target.inventorySlot())) {
                return Result.fail("inventory_full");
            }
        }

        ItemStack incoming = target.stack().copy();
        RelicStackData.write(incoming, RelicStackData.touch(data));
        target.remove();
        if (!oldToInventory.isEmpty() && !inventory.add(oldToInventory)) {
            target.save();
            return Result.fail("inventory_full");
        }
        stacks.setStackInSlot(replaceSlot, incoming);
        StarRailRelicService.refresh(player);
        return Result.ok();
    }

    private static int findCurioSlot(IItemHandlerModifiable stacks, UUID instanceId) {
        for (int slot = 0; slot < stacks.getSlots(); slot++) {
            if (hasId(stacks.getStackInSlot(slot), instanceId)) return slot;
        }
        return -1;
    }

    private static Result mutateRelic(ServerPlayer player, Request request, Operation operation) {
        LocatedRelic located = locate(player, request.relicId());
        if (located == null) return Result.fail("relic_missing");
        RelicStackData.Data data = RelicStackData.read(located.stack()).orElse(null);
        if (data == null || data.operationVersion() != request.expectedVersion()) {
            return Result.fail("operation_stale");
        }

        try {
            RelicStackData.Data updated;
            switch (operation) {
                case LOCK -> {
                    if (data.hasPendingReroll()) return Result.fail("reroll_pending");
                    updated = RelicStackData.withLocked(data, !data.locked());
                }
                case ENHANCE -> {
                    if (data.hasPendingReroll()) return Result.fail("reroll_pending");
                    int targetLevel = request.value();
                    if (targetLevel <= data.level() || targetLevel > data.maxLevel()) {
                        return Result.fail("operation_invalid");
                    }
                    int xpCost = RelicStackData.xpAtLevel(data.stars(), targetLevel) - data.experience();
                    if (xpCost <= 0 || xpCost % 100 != 0) return Result.fail("operation_invalid");
                    int crystalCost = xpCost / 100;
                    if (!hasItems(player.getInventory(), StarRailRelicItems.LOST_CRYSTAL.get(), crystalCost)) {
                        return Result.fail("materials_insufficient");
                    }
                    updated = RelicStackData.addExperience(data, xpCost, player.getRandom());
                    removeItems(player.getInventory(), StarRailRelicItems.LOST_CRYSTAL.get(), crystalCost);
                }
                case REROLL -> {
                    if (located.equipped() || data.locked() || data.stars() != 5
                            || data.level() != data.maxLevel() || data.hasPendingReroll()) {
                        return Result.fail("relic_protected");
                    }
                    if (!hasItems(player.getInventory(), StarRailRelicItems.VARIABLE_DICE.get(), 1)) {
                        return Result.fail("materials_insufficient");
                    }
                    updated = RelicStackData.prepareReroll(data, player.getRandom());
                    removeItems(player.getInventory(), StarRailRelicItems.VARIABLE_DICE.get(), 1);
                }
                case KEEP_OLD, TAKE_NEW -> {
                    if (!data.hasPendingReroll()) return Result.fail("reroll_missing");
                    updated = RelicStackData.resolveReroll(data, operation == Operation.TAKE_NEW);
                }
                default -> throw new IllegalStateException("Unexpected relic mutation");
            }
            RelicStackData.write(located.stack(), updated);
            located.save();
            if (located.equipped()) StarRailRelicService.refresh(player);
            return Result.ok();
        } catch (IllegalArgumentException | IllegalStateException invalidOperation) {
            return Result.fail("operation_invalid");
        }
    }

    private static Result salvage(ServerPlayer player, Request request, boolean remains) {
        LocatedRelic located = locate(player, request.relicId());
        if (located == null) return Result.fail("relic_missing");
        RelicStackData.Data data = RelicStackData.read(located.stack()).orElse(null);
        if (data == null || data.operationVersion() != request.expectedVersion()) {
            return Result.fail("operation_stale");
        }
        if (located.equipped() || data.locked() || data.hasPendingReroll()) {
            return Result.fail("relic_protected");
        }
        if (remains && data.stars() != 5) return Result.fail("operation_invalid");

        Item rewardItem = remains ? StarRailRelicItems.RELIC_REMAINS.get()
                : StarRailRelicItems.LOST_CRYSTAL.get();
        int rewardCount = remains ? 10
                : (data.stars() <= 3 ? 1 : data.stars() == 4 ? 3 : 5)
                + data.experience() * 8 / 1000;
        ItemStack reward = new ItemStack(rewardItem, rewardCount);
        Inventory inventory = player.getInventory();
        if (!canReceive(inventory, reward)) return Result.fail("inventory_full");

        located.remove();
        inventory.add(reward);
        StarRailRelicService.refresh(player);
        return Result.ok();
    }

    private static Result forge(ServerPlayer player, Request request) {
        List<RelicStackData.Stat> targetSubstats;
        try {
            targetSubstats = decodeTargetSubstats(request.targetSubstatMask());
        } catch (IllegalArgumentException invalidMask) {
            return Result.fail("operation_invalid");
        }
        if (request.set() == null || request.part() == null
                || request.set().inner() != request.part().inner()
                || (request.targetMain() && (request.mainStat() == null
                || !request.part().acceptsMainStat(request.mainStat())
                || request.part() == RelicStackData.Part.HEAD
                || request.part() == RelicStackData.Part.HANDS))
                || (request.targetMain() && targetSubstats.contains(request.mainStat()))
                || (!request.targetMain()
                && (request.part() == RelicStackData.Part.HEAD || request.part() == RelicStackData.Part.HANDS)
                && targetSubstats.contains(request.part().defaultMainStat()))) {
            return Result.fail("operation_invalid");
        }
        Inventory inventory = player.getInventory();
        int resinCost = request.targetMain() ? 1 : 0;
        int keyCost = targetSubstats.isEmpty() ? 0 : 1;
        if (!hasItems(inventory, StarRailRelicItems.RELIC_REMAINS.get(), 100)
                || resinCost > 0 && !hasItems(inventory, StarRailRelicItems.SELF_MODELING_RESIN.get(), resinCost)
                || keyCost > 0 && !hasItems(inventory, StarRailRelicItems.INTERFERENCE_KEY.get(), keyCost)) {
            return Result.fail("materials_insufficient");
        }
        ItemStack output;
        try {
            output = StarRailRelicItems.createGeneratedStack(request.set(), request.part(), 5,
                    request.targetMain() ? request.mainStat() : null, targetSubstats, player.getRandom());
        } catch (RuntimeException invalidRequest) {
            return Result.fail("operation_invalid");
        }
        if (!canReceive(inventory, output)) return Result.fail("inventory_full");

        removeItems(inventory, StarRailRelicItems.RELIC_REMAINS.get(), 100);
        if (resinCost > 0) removeItems(inventory, StarRailRelicItems.SELF_MODELING_RESIN.get(), resinCost);
        if (keyCost > 0) removeItems(inventory, StarRailRelicItems.INTERFERENCE_KEY.get(), keyCost);
        inventory.add(output);
        return Result.ok();
    }

    private static List<RelicStackData.Stat> decodeTargetSubstats(int mask) {
        if (mask < 0 || (mask >>> RelicStackData.Stat.values().length) != 0) {
            throw new IllegalArgumentException("Invalid targeted substat mask");
        }
        int count = Integer.bitCount(mask);
        if (count != 0 && count != 4) throw new IllegalArgumentException("Expected four targeted substats");
        List<RelicStackData.Stat> selected = new ArrayList<>(count);
        for (RelicStackData.Stat stat : RelicStackData.Stat.values()) {
            if ((mask & (1 << stat.ordinal())) != 0) selected.add(stat);
        }
        return selected;
    }

    private static Result craft(ServerPlayer player, Item outputItem, int remainsCost) {
        Inventory inventory = player.getInventory();
        if (!hasItems(inventory, StarRailRelicItems.RELIC_REMAINS.get(), remainsCost)) {
            return Result.fail("materials_insufficient");
        }
        ItemStack output = new ItemStack(outputItem);
        if (!canReceive(inventory, output)) return Result.fail("inventory_full");
        removeItems(inventory, StarRailRelicItems.RELIC_REMAINS.get(), remainsCost);
        inventory.add(output);
        return Result.ok();
    }

    static LocatedRelic locateForPreset(ServerPlayer player, UUID instanceId) { return locate(player,instanceId); }

    private static LocatedRelic locate(ServerPlayer player, UUID instanceId) {
        if (instanceId == null) return null;
        List<LocatedRelic> found = new ArrayList<>();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            final int index = slot;
            ItemStack stack = inventory.getItem(index);
            if (hasId(stack, instanceId)) {
                found.add(new LocatedRelic(stack, false, index,
                        () -> inventory.setItem(index, stack),
                        () -> inventory.setItem(index, ItemStack.EMPTY)));
            }
        }
        CuriosApi.getCuriosInventory(player).ifPresent(curios -> {
            for (RelicStackData.Part part : RelicStackData.Part.values()) {
                curios.getStacksHandler(part.curiosSlot()).ifPresent(handler -> {
                    for (int slot = 0; slot < handler.getSlots(); slot++) {
                        final int index = slot;
                        IItemHandlerModifiable stacks = handler.getStacks();
                        ItemStack stack = stacks.getStackInSlot(index);
                        if (hasId(stack, instanceId)) {
                            found.add(new LocatedRelic(stack, true, -1,
                                    () -> stacks.setStackInSlot(index, stack),
                                    () -> stacks.setStackInSlot(index, ItemStack.EMPTY)));
                        }
                    }
                });
            }
        });
        return found.size() == 1 ? found.get(0) : null;
    }

    private static boolean hasId(ItemStack stack, UUID instanceId) {
        return RelicStackData.read(stack).map(data -> data.instanceId().equals(instanceId)).orElse(false);
    }

    private static boolean hasItems(Inventory inventory, Item item, int count) {
        if (count <= 0) return true;
        int found = 0;
        for (ItemStack stack : inventory.items) if (stack.is(item)) found += stack.getCount();
        return found >= count;
    }

    private static void removeItems(Inventory inventory, Item item, int count) {
        int remaining = count;
        for (ItemStack stack : inventory.items) {
            if (remaining <= 0) break;
            if (!stack.is(item)) continue;
            int used = Math.min(remaining, stack.getCount());
            stack.shrink(used);
            remaining -= used;
        }
        if (remaining != 0) throw new IllegalStateException("Material inventory changed during transaction");
        inventory.setChanged();
    }

    private static boolean canReceive(Inventory inventory, ItemStack output) {
        return canReceive(inventory, output, -1);
    }

    private static boolean canReceive(Inventory inventory, ItemStack output, int freedSlot) {
        int remaining = output.getCount();
        for (int slot = 0; slot < inventory.items.size(); slot++) {
            ItemStack existing = inventory.items.get(slot);
            if (slot == freedSlot || existing.isEmpty()) {
                remaining -= output.getMaxStackSize();
            } else if (ItemStack.isSameItemSameTags(existing, output)) {
                remaining -= Math.max(0, Math.min(existing.getMaxStackSize(), output.getMaxStackSize())
                        - existing.getCount());
            }
            if (remaining <= 0) return true;
        }
        return false;
    }

    private enum Operation { LOCK, ENHANCE, REROLL, KEEP_OLD, TAKE_NEW }

    record LocatedRelic(ItemStack stack, boolean equipped, int inventorySlot,
                                Runnable saveAction, Runnable removeAction) {
        private void save() { saveAction.run(); }
        private void remove() { removeAction.run(); }
    }
}
