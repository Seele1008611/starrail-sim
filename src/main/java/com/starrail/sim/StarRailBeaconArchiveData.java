package com.starrail.sim;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** World-level tombstones let offline players prune destroyed beacon coordinates on next sync. */
public final class StarRailBeaconArchiveData extends SavedData {
    private static final String DATA_NAME = "starrail_sim_path_beacon_archive";
    private static final String REMOVED_POSITIONS_TAG = "removed_positions";

    private final Set<Long> removedPositions = new HashSet<>();

    public static StarRailBeaconArchiveData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                StarRailBeaconArchiveData::load,
                StarRailBeaconArchiveData::new,
                DATA_NAME);
    }

    private static StarRailBeaconArchiveData load(CompoundTag tag) {
        StarRailBeaconArchiveData data = new StarRailBeaconArchiveData();
        if (tag.contains(REMOVED_POSITIONS_TAG, 9)) {
            ListTag positions = tag.getList(REMOVED_POSITIONS_TAG, 4);
            for (int index = 0; index < positions.size(); index++) {
                data.removedPositions.add(((LongTag) positions.get(index)).getAsLong());
            }
        }
        return data;
    }

    public boolean markRemoved(long position) {
        boolean changed = removedPositions.add(position);
        if (changed) setDirty();
        return changed;
    }

    public boolean markPresent(long position) {
        boolean changed = removedPositions.remove(position);
        if (changed) setDirty();
        return changed;
    }

    public Set<Long> getRemovedPositions() {
        return Collections.unmodifiableSet(removedPositions);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag positions = new ListTag();
        for (long position : removedPositions) positions.add(LongTag.valueOf(position));
        tag.put(REMOVED_POSITIONS_TAG, positions);
        return tag;
    }
}
