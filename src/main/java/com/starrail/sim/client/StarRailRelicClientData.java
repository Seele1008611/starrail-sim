package com.starrail.sim.client;

import com.starrail.sim.RelicActionPacket;
import com.starrail.sim.RelicStackData;
import com.starrail.sim.StarRailNetwork;
import com.starrail.sim.StarRailRelicOperationService;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.starrail.sim.client.StarRailRelicUiData.*;

/** Client view adapter for server snapshots; it never mutates local ItemStacks or player data. */
public final class StarRailRelicClientData implements StarRailRelicUiData {
    public static final StarRailRelicClientData INSTANCE = new StarRailRelicClientData();
    private static final Map<UUID, Long> IDS_BY_UUID = new HashMap<>();
    private static long nextId = 1L;
    private static Object snapshotConnection;
    private static Snapshot current = new Snapshot(List.of(), Map.of(), Map.of(), false);

    private StarRailRelicClientData() {}

    public static void requestSnapshot() {
        Minecraft minecraft = Minecraft.getInstance();
        Object connection = minecraft.getConnection();
        if (snapshotConnection != connection) {
            snapshotConnection = connection;
            IDS_BY_UUID.clear();
            nextId = 1L;
            current = new Snapshot(List.of(), Map.of(), Map.of(), false);
        }
        StarRailNetwork.CHANNEL.sendToServer(new com.starrail.sim.RelicStateRequestPacket());
    }

    public static void update(CompoundTag root) {
        List<Entry> entries = new ArrayList<>();
        EnumMap<Part, Long> equipped = new EnumMap<>(Part.class);
        EnumMap<Material, Integer> materials = new EnumMap<>(Material.class);
        ListTag relics = root.getList("Relics", Tag.TAG_COMPOUND);
        for (int index = 0; index < relics.size(); index++) {
            CompoundTag tag = relics.getCompound(index);
            RelicStackData.Data data = RelicStackData.decode(tag).orElse(null);
            if (data == null) continue;
            long id = IDS_BY_UUID.computeIfAbsent(data.instanceId(), ignored -> nextId++);
            Part part = Part.valueOf(data.part().name());
            List<SubStat> substats = toUiStats(data.substats());
            List<SubStat> candidate = toUiStats(data.pendingReroll());
            Entry entry = new Entry(id, data.set().ordinal(), part, data.stars(), data.level(),
                    Stat.valueOf(data.mainStat().name()), data.mainValue(), substats,
                    data.locked(), candidate, data.operationVersion());
            entries.add(entry);
            if (tag.getBoolean("Equipped")) equipped.put(part, id);
        }
        materials.put(Material.CRYSTAL, Math.max(0, root.getInt("Crystal")));
        materials.put(Material.REMAINS, Math.max(0, root.getInt("Remains")));
        materials.put(Material.RESIN, Math.max(0, root.getInt("Resin")));
        materials.put(Material.DICE, Math.max(0, root.getInt("Dice")));
        materials.put(Material.INTERFERENCE_KEY, Math.max(0, root.getInt("InterferenceKey")));
        current = new Snapshot(entries, equipped, materials, true);

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof StarRailCharacterScreen screen && screen.isRelicSection()) {
            screen.onRelicSnapshotUpdated(root.getString("ResultKey"), root.getBoolean("ResultAccepted"));
        }
    }

    private static List<SubStat> toUiStats(List<RelicStackData.SubStat> stats) {
        return stats.stream().map(substat -> new SubStat(Stat.valueOf(substat.stat().name()),
                substat.baseValue(), substat.upgradeRolls().stream().mapToDouble(Double::doubleValue).sum(),
                substat.upgradeRolls().size())).toList();
    }

    @Override public Snapshot snapshot() { return current; }
    @Override public boolean preview() { return false; }

    @Override
    public Feedback perform(Command command) {
        UUID relicId = null;
        long expectedVersion = 0L;
        if (command.action() != Action.FORGE && command.action() != Action.CRAFT_RESIN
                && command.action() != Action.CRAFT_DICE && command.action() != Action.CRAFT_KEY) {
            Entry selected = current.find(command.itemId());
            if (selected == null) return new Feedback(false, "select_item");
            relicId = currentUuid(selected.id());
            expectedVersion = selected.version();
            if (relicId == null) return new Feedback(false, "select_item");
        }

        StarRailRelicOperationService.Action action = switch (command.action()) {
            case EQUIP -> StarRailRelicOperationService.Action.EQUIP;
            case LOCK -> StarRailRelicOperationService.Action.LOCK;
            case ENHANCE -> StarRailRelicOperationService.Action.ENHANCE;
            case SALVAGE_CRYSTAL -> StarRailRelicOperationService.Action.SALVAGE_CRYSTAL;
            case SALVAGE_REMAINS -> StarRailRelicOperationService.Action.SALVAGE_REMAINS;
            case FORGE -> StarRailRelicOperationService.Action.FORGE;
            case CRAFT_RESIN -> StarRailRelicOperationService.Action.CRAFT_RESIN;
            case CRAFT_DICE -> StarRailRelicOperationService.Action.CRAFT_DICE;
            case CRAFT_KEY -> StarRailRelicOperationService.Action.CRAFT_KEY;
            case REROLL -> StarRailRelicOperationService.Action.REROLL;
            case KEEP_OLD -> StarRailRelicOperationService.Action.KEEP_OLD;
            case TAKE_NEW -> StarRailRelicOperationService.Action.TAKE_NEW;
        };
        RelicStackData.SetId set = command.setIndex() >= 0 && command.setIndex() < RelicStackData.SetId.values().length
                ? RelicStackData.SetId.values()[command.setIndex()] : RelicStackData.SetId.MUSKETEER;
        RelicStackData.Part part = command.part() == null ? RelicStackData.Part.HEAD
                : RelicStackData.Part.valueOf(command.part().name());
        RelicStackData.Stat main = command.main() == null ? RelicStackData.Stat.HP
                : RelicStackData.Stat.valueOf(command.main().name());
        StarRailNetwork.CHANNEL.sendToServer(new RelicActionPacket(action, relicId, expectedVersion,
                command.value(), set, part, main, command.action() == Action.FORGE && command.value() == 1,
                targetSubstatMask(command.targetSubstats())));
        return new Feedback(true, "request_sent");
    }

    private static int targetSubstatMask(List<Stat> stats) {
        int mask = 0;
        for (Stat stat : stats) mask |= 1 << RelicStackData.Stat.valueOf(stat.name()).ordinal();
        return mask;
    }

    private static UUID currentUuid(long id) {
        for (Map.Entry<UUID, Long> entry : IDS_BY_UUID.entrySet()) {
            if (entry.getValue() == id) return entry.getKey();
        }
        return null;
    }
}
