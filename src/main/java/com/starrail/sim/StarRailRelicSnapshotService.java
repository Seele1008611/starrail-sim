package com.starrail.sim;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Builds the bounded inventory/equipment/material snapshot consumed by the relic screen. */
public final class StarRailRelicSnapshotService {
    private StarRailRelicSnapshotService() {}

    public static CompoundTag create(ServerPlayer player) {
        return create(player, null, false);
    }

    public static CompoundTag create(ServerPlayer player, String resultKey, boolean resultAccepted) {
        CompoundTag snapshot = new CompoundTag();
        Map<UUID, CompoundTag> entriesById = new LinkedHashMap<>();
        Set<UUID> duplicateIds = new HashSet<>();

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            collect(entriesById, duplicateIds, player.getInventory().getItem(slot), false);
        }
        CuriosApi.getCuriosInventory(player).ifPresent(curios -> {
            for (RelicStackData.Part part : RelicStackData.Part.values()) {
                curios.getStacksHandler(part.curiosSlot()).ifPresent(handler -> {
                    for (int slot = 0; slot < handler.getSlots(); slot++) {
                        ItemStack stack = handler.getStacks().getStackInSlot(slot);
                        RelicStackData.read(stack).filter(data -> data.part() == part)
                                .ifPresent(data -> collect(entriesById, duplicateIds, stack, true));
                    }
                });
            }
        });
        ListTag entries = new ListTag();
        entriesById.values().forEach(entries::add);
        snapshot.put("Relics", entries);
        snapshot.putInt("Crystal", count(player, StarRailRelicItems.LOST_CRYSTAL.get()));
        snapshot.putInt("Remains", count(player, StarRailRelicItems.RELIC_REMAINS.get()));
        snapshot.putInt("Resin", count(player, StarRailRelicItems.SELF_MODELING_RESIN.get()));
        snapshot.putInt("Dice", count(player, StarRailRelicItems.VARIABLE_DICE.get()));
        snapshot.putInt("InterferenceKey", count(player, StarRailRelicItems.INTERFERENCE_KEY.get()));
        if (resultKey != null && !resultKey.isBlank()) {
            snapshot.putString("ResultKey", resultKey);
            snapshot.putBoolean("ResultAccepted", resultAccepted);
        }
        return snapshot;
    }

    private static void collect(Map<UUID, CompoundTag> entriesById, Set<UUID> duplicateIds,
                                ItemStack stack, boolean equipped) {
        RelicStackData.read(stack).ifPresent(data -> {
            UUID id = data.instanceId();
            if (duplicateIds.contains(id)) return;
            if (entriesById.containsKey(id)) {
                entriesById.remove(id);
                duplicateIds.add(id);
                return;
            }
            CompoundTag entry = RelicStackData.encode(data);
            entry.putBoolean("Equipped", equipped);
            entriesById.put(id, entry);
        });
    }

    private static int count(ServerPlayer player, net.minecraft.world.item.Item item) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) if (stack.is(item)) count += stack.getCount();
        return count;
    }
}
