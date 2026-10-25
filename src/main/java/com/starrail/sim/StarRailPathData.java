package com.starrail.sim;

/**
 * 模组代码说明：玩家命途数据的默认实现，保存当前命途、试炼、阶位、修行进度和战斗效果所需的临时状态。
 */

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/** Default implementation of the player's persistent path state. */
public final class StarRailPathData implements IStarRailPathData {
    private final EnumMap<StarRailPath, Integer> traceMasks = new EnumMap<>(StarRailPath.class);
    @Override public int getTraceMask(StarRailPath path) { return traceMasks.getOrDefault(path, 0); }
    @Override public void setTraceMask(StarRailPath path, int mask) {
        if (path != null && path.isRealPath()) traceMasks.put(path, mask & 511);
    }

    private StarRailPath currentPath = StarRailPath.NONE;
    private StarRailPath trialPath = StarRailPath.NONE;
    private StarRailPathRank trialRank = StarRailPathRank.UNALIGNED;
    private boolean pathUnlocked;
    private final Set<String> discoveredPathRegions = new HashSet<>();
    private final Set<String> discoveredPathRuins = new HashSet<>();
    private final Map<String, Long> discoveredPathRuinPositions = new HashMap<>();
    private final Map<String, String> archiveCustomNames = new HashMap<>();
    private final Set<Long> discoveredPathBeacons = new HashSet<>();
    private final Set<Long> discoveredPathArenas = new HashSet<>();
    private boolean legacyPathArenaDiscovered;
    private int pathArenaVictories;
    private StarRailPath soughtPath = StarRailPath.NONE;
    private int soughtX;
    private int soughtY;
    private int soughtZ;
    private ResourceLocation soughtDimension = Level.OVERWORLD.location();
    private final EnumMap<StarRailPath, StarRailPathRank> pathRanks =
            new EnumMap<>(StarRailPath.class);
    private final EnumMap<StarRailPath, Integer> practiceProgress =
            new EnumMap<>(StarRailPath.class);
    private final EnumMap<StarRailPath, Integer> pinnaclePracticeCount =
            new EnumMap<>(StarRailPath.class);
    private long trialStartTick;
    private int objective1Progress;
    private int objective2Progress;
    private long lastHuntKillTick = -1L;
    private int huntIntentStacks;
    private long huntIntentExpireTick = -1L;
    private long huntFinisherCooldownTick = -1L;
    private int preservationGuardStacks;
    private long preservationGuardExpireTick = -1L;
    private long preservationBarrierCooldownTick = -1L;
    private long preservationBarrierExpireTick = -1L;
    private long preservationNextShieldTick = -1L;
    private int destructionWrathStacks;
    private long destructionWrathExpireTick = -1L;
    private double destructionAttackBonus;
    private long destructionAttackBonusExpireTick = -1L;
    private long destructionDesperationCooldownTick = -1L;
    private long eruditionHitTick = -1L;
    private int eruditionHitCount;
    private long lastEruditionMultiHitTick = -1L;
    private long eruditionEchoCooldownTick = -1L;
    private long eruditionAnalysisExpireTick = -1L;
    private long eruditionAnalysisWindowExpireTick = -1L;
    private final Set<UUID> eruditionAnalysisTargets = new HashSet<>();
    private long eruditionFinalCooldownTick = -1L;
    private UUID remembranceTargetId;
    private long remembranceTargetTick = -1L;
    private int elationCombo;
    private long elationLastHitTick = -1L;

    @Override
    public StarRailPath getCurrentPath() {
        return currentPath;
    }

    @Override
    public void setCurrentPath(StarRailPath path) {
        currentPath = path == null ? StarRailPath.NONE : path;
    }

    @Override
    public StarRailPath getTrialPath() {
        return trialPath;
    }

    @Override
    public void setTrialPath(StarRailPath path) {
        trialPath = path == null ? StarRailPath.NONE : path;
    }

    @Override
    public StarRailPathRank getTrialRank() {
        return trialRank;
    }

    @Override
    public void setTrialRank(StarRailPathRank rank) {
        trialRank = rank == null ? StarRailPathRank.UNALIGNED : rank;
    }

    @Override
    public boolean isPathUnlocked() {
        return pathUnlocked;
    }

    @Override
    public void setPathUnlocked(boolean unlocked) {
        pathUnlocked = unlocked;
    }

    @Override public Set<String> getDiscoveredPathRegions() {
        return Collections.unmodifiableSet(discoveredPathRegions);
    }
    @Override public boolean addDiscoveredPathRegion(String regionId) {
        return regionId != null && discoveredPathRegions.add(regionId);
    }
    @Override public Set<String> getDiscoveredPathRuins() {
        return Collections.unmodifiableSet(discoveredPathRuins);
    }
    @Override public boolean addDiscoveredPathRuin(String ruinId) {
        return ruinId != null && discoveredPathRuins.add(ruinId);
    }
    @Override public Map<String, Long> getDiscoveredPathRuinPositions() {
        return Collections.unmodifiableMap(discoveredPathRuinPositions);
    }
    @Override public boolean addDiscoveredPathRuinPosition(String ruinId, long position) {
        return ruinId != null && discoveredPathRuinPositions.putIfAbsent(ruinId, position) == null;
    }
    @Override public Map<String, String> getArchiveCustomNames() {
        return Collections.unmodifiableMap(archiveCustomNames);
    }
    @Override public void setArchiveCustomName(String recordKey, String name) {
        if (recordKey == null || recordKey.isBlank()) return;
        if (name == null || name.isBlank()) archiveCustomNames.remove(recordKey);
        else archiveCustomNames.put(recordKey, name);
    }
    @Override public Set<Long> getDiscoveredPathBeacons() {
        return Collections.unmodifiableSet(discoveredPathBeacons);
    }
    @Override public boolean addDiscoveredPathBeacon(long position) {
        return discoveredPathBeacons.add(position);
    }
    @Override public boolean removeDiscoveredPathBeacon(long position) {
        boolean removed = discoveredPathBeacons.remove(position);
        if (removed) archiveCustomNames.remove("beacon:" + position);
        return removed;
    }
    @Override public Set<Long> getDiscoveredPathArenas() {
        return Collections.unmodifiableSet(discoveredPathArenas);
    }
    @Override public boolean addDiscoveredPathArena(long position) {
        return discoveredPathArenas.add(position);
    }
    @Override public int getPathArenaVictories() { return pathArenaVictories; }
    @Override public void recordPathArenaVictory() {
        if (pathArenaVictories < Integer.MAX_VALUE) pathArenaVictories++;
    }
    @Override public boolean isPathArenaDiscovered() {
        return legacyPathArenaDiscovered || !discoveredPathArenas.isEmpty();
    }

    @Override
    public StarRailPath getSoughtPath() {
        return soughtPath;
    }

    @Override
    public int getSoughtX() {
        return soughtX;
    }

    @Override
    public int getSoughtY() {
        return soughtY;
    }

    @Override
    public int getSoughtZ() {
        return soughtZ;
    }

    @Override
    public ResourceLocation getSoughtDimension() {
        return soughtDimension;
    }

    @Override
    public void setSoughtRuin(StarRailPath path, int x, int y, int z) {
        setSoughtRuin(path, Level.OVERWORLD.location(), x, y, z);
    }

    @Override
    public void setSoughtRuin(StarRailPath path, ResourceLocation dimension, int x, int y, int z) {
        if (path == null || !path.isRealPath()) {
            clearSoughtRuin();
            return;
        }
        soughtPath = path;
        soughtDimension = dimension == null ? Level.OVERWORLD.location() : dimension;
        soughtX = x;
        soughtY = y;
        soughtZ = z;
    }

    @Override
    public void clearSoughtRuin() {
        soughtPath = StarRailPath.NONE;
        soughtX = 0;
        soughtY = 0;
        soughtZ = 0;
        soughtDimension = Level.OVERWORLD.location();
    }

    @Override
    public StarRailPathRank getPathRank(StarRailPath path) {
        if (path == null || !path.isRealPath()) {
            return StarRailPathRank.UNALIGNED;
        }
        return pathRanks.getOrDefault(path, StarRailPathRank.UNALIGNED);
    }

    @Override
    public void setPathRank(StarRailPath path, StarRailPathRank rank) {
        if (path == null || !path.isRealPath()) {
            return;
        }
        if (rank == null || rank == StarRailPathRank.UNALIGNED) {
            pathRanks.remove(path);
        } else {
            pathRanks.put(path, rank);
        }
    }

    @Override
    public int getPracticeProgress(StarRailPath path) {
        if (path == null || !path.isRealPath()) {
            return 0;
        }
        return practiceProgress.getOrDefault(path, 0);
    }

    @Override
    public void setPracticeProgress(StarRailPath path, int progress) {
        if (path == null || !path.isRealPath()) {
            return;
        }
        if (progress <= 0) {
            practiceProgress.remove(path);
        } else {
            practiceProgress.put(path, progress);
        }
    }

    @Override
    public int getPinnaclePracticeCount(StarRailPath path) {
        if (path == null || !path.isRealPath()) {
            return 0;
        }
        return pinnaclePracticeCount.getOrDefault(path, 0);
    }

    @Override
    public void setPinnaclePracticeCount(StarRailPath path, int count) {
        if (path == null || !path.isRealPath()) {
            return;
        }
        int bounded = Math.max(0, Math.min(
                StarRailPathProgress.MAX_PINNACLE_PRACTICE_COUNT, count));
        if (bounded == 0) {
            pinnaclePracticeCount.remove(path);
        } else {
            pinnaclePracticeCount.put(path, bounded);
        }
    }

    @Override
    public long getTrialStartTick() {
        return trialStartTick;
    }

    @Override
    public void setTrialStartTick(long tick) {
        trialStartTick = tick;
    }

    @Override
    public int getObjective1Progress() {
        return objective1Progress;
    }

    @Override
    public void setObjective1Progress(int progress) {
        objective1Progress = Math.max(0, Math.min(
                StarRailPathRules.objective1Target(trialPath, trialRank), progress));
    }

    @Override
    public int getObjective2Progress() {
        return objective2Progress;
    }

    @Override
    public void setObjective2Progress(int progress) {
        objective2Progress = Math.max(0, Math.min(
                StarRailPathRules.objective2Target(trialPath, trialRank), progress));
    }

    @Override
    public long getLastHuntKillTick() {
        return lastHuntKillTick;
    }

    @Override
    public void setLastHuntKillTick(long tick) {
        lastHuntKillTick = tick;
    }

    @Override
    public int getHuntIntentStacks() {
        return huntIntentStacks;
    }

    @Override
    public void setHuntIntentStacks(int stacks) {
        huntIntentStacks = Math.max(0, Math.min(3, stacks));
    }

    @Override
    public long getHuntIntentExpireTick() {
        return huntIntentExpireTick;
    }

    @Override
    public void setHuntIntentExpireTick(long tick) {
        huntIntentExpireTick = tick;
    }

    @Override
    public long getHuntFinisherCooldownTick() {
        return huntFinisherCooldownTick;
    }

    @Override
    public void setHuntFinisherCooldownTick(long tick) {
        huntFinisherCooldownTick = tick;
    }

    @Override
    public int getPreservationGuardStacks() {
        return preservationGuardStacks;
    }

    @Override
    public void setPreservationGuardStacks(int stacks) {
        preservationGuardStacks = Math.max(0, Math.min(3, stacks));
    }

    @Override
    public long getPreservationGuardExpireTick() {
        return preservationGuardExpireTick;
    }

    @Override
    public void setPreservationGuardExpireTick(long tick) {
        preservationGuardExpireTick = tick;
    }

    @Override
    public long getPreservationBarrierCooldownTick() {
        return preservationBarrierCooldownTick;
    }

    @Override
    public void setPreservationBarrierCooldownTick(long tick) {
        preservationBarrierCooldownTick = tick;
    }

    @Override
    public long getPreservationBarrierExpireTick() {
        return preservationBarrierExpireTick;
    }

    @Override
    public void setPreservationBarrierExpireTick(long tick) {
        preservationBarrierExpireTick = tick;
    }

    @Override
    public long getPreservationNextShieldTick() {
        return preservationNextShieldTick;
    }

    @Override
    public void setPreservationNextShieldTick(long tick) {
        preservationNextShieldTick = tick;
    }

    @Override
    public int getDestructionWrathStacks() {
        return destructionWrathStacks;
    }

    @Override
    public void setDestructionWrathStacks(int stacks) {
        destructionWrathStacks = Math.max(0, Math.min(3, stacks));
    }

    @Override
    public long getDestructionWrathExpireTick() {
        return destructionWrathExpireTick;
    }

    @Override
    public void setDestructionWrathExpireTick(long tick) {
        destructionWrathExpireTick = tick;
    }

    @Override
    public double getDestructionAttackBonus() {
        return destructionAttackBonus;
    }

    @Override
    public void setDestructionAttackBonus(double bonus) {
        destructionAttackBonus = Math.max(0.0D, Math.min(0.50D, bonus));
    }

    @Override
    public long getDestructionAttackBonusExpireTick() {
        return destructionAttackBonusExpireTick;
    }

    @Override
    public void setDestructionAttackBonusExpireTick(long tick) {
        destructionAttackBonusExpireTick = tick;
    }

    @Override
    public long getDestructionDesperationCooldownTick() {
        return destructionDesperationCooldownTick;
    }

    @Override
    public void setDestructionDesperationCooldownTick(long tick) {
        destructionDesperationCooldownTick = tick;
    }

    @Override
    public long getEruditionHitTick() {
        return eruditionHitTick;
    }

    @Override
    public void setEruditionHitTick(long tick) {
        eruditionHitTick = tick;
    }

    @Override
    public int getEruditionHitCount() {
        return eruditionHitCount;
    }

    @Override
    public void setEruditionHitCount(int count) {
        eruditionHitCount = Math.max(0, count);
    }

    @Override
    public long getLastEruditionMultiHitTick() {
        return lastEruditionMultiHitTick;
    }

    @Override
    public void setLastEruditionMultiHitTick(long tick) {
        lastEruditionMultiHitTick = tick;
    }

    @Override
    public long getEruditionEchoCooldownTick() {
        return eruditionEchoCooldownTick;
    }

    @Override
    public void setEruditionEchoCooldownTick(long tick) {
        eruditionEchoCooldownTick = tick;
    }

    @Override
    public long getEruditionAnalysisExpireTick() {
        return eruditionAnalysisExpireTick;
    }

    @Override
    public void setEruditionAnalysisExpireTick(long tick) {
        eruditionAnalysisExpireTick = tick;
    }

    @Override
    public long getEruditionAnalysisWindowExpireTick() {
        return eruditionAnalysisWindowExpireTick;
    }

    @Override
    public void setEruditionAnalysisWindowExpireTick(long tick) {
        eruditionAnalysisWindowExpireTick = tick;
    }

    @Override
    public boolean hasEruditionAnalysisTarget(UUID targetId) {
        return targetId != null && eruditionAnalysisTargets.contains(targetId);
    }

    @Override
    public boolean addEruditionAnalysisTarget(UUID targetId) {
        return targetId != null && eruditionAnalysisTargets.add(targetId);
    }

    @Override
    public UUID getEruditionAnalysisTarget(int index) {
        if (index < 0 || index >= eruditionAnalysisTargets.size()) {
            return null;
        }
        return eruditionAnalysisTargets.stream().skip(index).findFirst().orElse(null);
    }

    @Override
    public void clearEruditionAnalysisTargets() {
        eruditionAnalysisTargets.clear();
    }

    @Override
    public long getEruditionFinalCooldownTick() {
        return eruditionFinalCooldownTick;
    }

    @Override
    public void setEruditionFinalCooldownTick(long tick) {
        eruditionFinalCooldownTick = tick;
    }

    @Override
    public UUID getRemembranceTargetId() {
        return remembranceTargetId;
    }

    @Override
    public void setRemembranceTargetId(UUID targetId) {
        remembranceTargetId = targetId;
    }

    @Override
    public long getRemembranceTargetTick() {
        return remembranceTargetTick;
    }

    @Override
    public void setRemembranceTargetTick(long tick) {
        remembranceTargetTick = tick;
    }

    @Override
    public int getElationCombo() {
        return elationCombo;
    }

    @Override
    public void setElationCombo(int combo) {
        elationCombo = Math.max(0, combo);
    }

    @Override
    public long getElationLastHitTick() {
        return elationLastHitTick;
    }

    @Override
    public void setElationLastHitTick(long tick) {
        elationLastHitTick = tick;
    }

    @Override
    public boolean isTrialComplete() {
        return StarRailPathRules.isImplemented(trialPath)
                && objective1Progress >= StarRailPathRules.objective1Target(trialPath, trialRank)
                && objective2Progress >= StarRailPathRules.objective2Target(trialPath, trialRank);
    }

    @Override
    public void resetTrial() {
        trialPath = StarRailPath.NONE;
        trialRank = StarRailPathRank.UNALIGNED;
        trialStartTick = 0L;
        objective1Progress = 0;
        objective2Progress = 0;
        lastHuntKillTick = -1L;
        huntIntentStacks = 0;
        huntIntentExpireTick = -1L;
        huntFinisherCooldownTick = -1L;
        preservationGuardStacks = 0;
        preservationGuardExpireTick = -1L;
        preservationBarrierCooldownTick = -1L;
        preservationBarrierExpireTick = -1L;
        preservationNextShieldTick = -1L;
        destructionWrathStacks = 0;
        destructionWrathExpireTick = -1L;
        destructionAttackBonus = 0.0D;
        destructionAttackBonusExpireTick = -1L;
        destructionDesperationCooldownTick = -1L;
        eruditionHitTick = -1L;
        eruditionHitCount = 0;
        lastEruditionMultiHitTick = -1L;
        eruditionEchoCooldownTick = -1L;
        eruditionAnalysisExpireTick = -1L;
        eruditionAnalysisWindowExpireTick = -1L;
        eruditionAnalysisTargets.clear();
        eruditionFinalCooldownTick = -1L;
        remembranceTargetId = null;
        remembranceTargetTick = -1L;
        elationCombo = 0;
        elationLastHitTick = -1L;
    }

    @Override
    public void copyFrom(IStarRailPathData other) {
        currentPath = other.getCurrentPath();
        trialPath = other.getTrialPath();
        trialRank = other.getTrialRank();
        pathUnlocked = other.isPathUnlocked();
        discoveredPathRegions.clear();
        discoveredPathRegions.addAll(other.getDiscoveredPathRegions());
        discoveredPathRuins.clear();
        discoveredPathRuins.addAll(other.getDiscoveredPathRuins());
        discoveredPathRuinPositions.clear();
        discoveredPathRuinPositions.putAll(other.getDiscoveredPathRuinPositions());
        archiveCustomNames.clear();
        archiveCustomNames.putAll(other.getArchiveCustomNames());
        discoveredPathBeacons.clear();
        discoveredPathBeacons.addAll(other.getDiscoveredPathBeacons());
        discoveredPathArenas.clear();
        discoveredPathArenas.addAll(other.getDiscoveredPathArenas());
        legacyPathArenaDiscovered = other.isPathArenaDiscovered()
                && discoveredPathArenas.isEmpty();
        pathArenaVictories = other.getPathArenaVictories();
        setSoughtRuin(other.getSoughtPath(), other.getSoughtDimension(),
                other.getSoughtX(), other.getSoughtY(),
                other.getSoughtZ());
        traceMasks.clear();
        pathRanks.clear();
        practiceProgress.clear();
        pinnaclePracticeCount.clear();
        for (StarRailPath path : StarRailPath.values()) {
            if (path.isRealPath()) {
                setTraceMask(path, other.getTraceMask(path));
                setPathRank(path, other.getPathRank(path));
                setPracticeProgress(path, other.getPracticeProgress(path));
                setPinnaclePracticeCount(path, other.getPinnaclePracticeCount(path));
            }
        }
        trialStartTick = other.getTrialStartTick();
        objective1Progress = other.getObjective1Progress();
        objective2Progress = other.getObjective2Progress();
        lastHuntKillTick = other.getLastHuntKillTick();
        huntIntentStacks = other.getHuntIntentStacks();
        huntIntentExpireTick = other.getHuntIntentExpireTick();
        huntFinisherCooldownTick = other.getHuntFinisherCooldownTick();
        preservationGuardStacks = other.getPreservationGuardStacks();
        preservationGuardExpireTick = other.getPreservationGuardExpireTick();
        preservationBarrierCooldownTick = other.getPreservationBarrierCooldownTick();
        preservationBarrierExpireTick = other.getPreservationBarrierExpireTick();
        preservationNextShieldTick = other.getPreservationNextShieldTick();
        destructionWrathStacks = other.getDestructionWrathStacks();
        destructionWrathExpireTick = other.getDestructionWrathExpireTick();
        destructionAttackBonus = other.getDestructionAttackBonus();
        destructionAttackBonusExpireTick = other.getDestructionAttackBonusExpireTick();
        destructionDesperationCooldownTick = other.getDestructionDesperationCooldownTick();
        eruditionHitTick = other.getEruditionHitTick();
        eruditionHitCount = other.getEruditionHitCount();
        lastEruditionMultiHitTick = other.getLastEruditionMultiHitTick();
        eruditionEchoCooldownTick = other.getEruditionEchoCooldownTick();
        eruditionAnalysisExpireTick = other.getEruditionAnalysisExpireTick();
        eruditionAnalysisWindowExpireTick = other.getEruditionAnalysisWindowExpireTick();
        eruditionAnalysisTargets.clear();
        for (int index = 0; index < 16; index++) {
            UUID targetId = other.getEruditionAnalysisTarget(index);
            if (targetId == null) {
                break;
            }
            eruditionAnalysisTargets.add(targetId);
        }
        eruditionFinalCooldownTick = other.getEruditionFinalCooldownTick();
        remembranceTargetId = other.getRemembranceTargetId();
        remembranceTargetTick = other.getRemembranceTargetTick();
        elationCombo = other.getElationCombo();
        elationLastHitTick = other.getElationLastHitTick();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("current_path", currentPath.getId());
        tag.putString("trial_path", trialPath.getId());
        tag.putInt("trial_rank", trialRank.getLevel());
        tag.putBoolean("path_unlocked", pathUnlocked);
        ListTag pathRegions = new ListTag();
        for (String region : discoveredPathRegions) pathRegions.add(net.minecraft.nbt.StringTag.valueOf(region));
        tag.put("path_regions_discovered", pathRegions);
        ListTag pathRuins = new ListTag();
        for (String ruin : discoveredPathRuins) pathRuins.add(net.minecraft.nbt.StringTag.valueOf(ruin));
        tag.put("path_ruins_discovered", pathRuins);
        CompoundTag pathRuinPositions = new CompoundTag();
        for (Map.Entry<String, Long> entry : discoveredPathRuinPositions.entrySet()) {
            pathRuinPositions.putLong(entry.getKey(), entry.getValue());
        }
        tag.put("path_ruin_positions", pathRuinPositions);
        CompoundTag archiveNames = new CompoundTag();
        for (Map.Entry<String, String> entry : archiveCustomNames.entrySet()) {
            archiveNames.putString(entry.getKey(), entry.getValue());
        }
        tag.put("path_archive_custom_names", archiveNames);
        ListTag pathBeacons = new ListTag();
        for (long beacon : discoveredPathBeacons) pathBeacons.add(net.minecraft.nbt.LongTag.valueOf(beacon));
        tag.put("path_beacons_discovered", pathBeacons);
        ListTag pathArenas = new ListTag();
        for (long arena : discoveredPathArenas) pathArenas.add(net.minecraft.nbt.LongTag.valueOf(arena));
        tag.put("path_arenas_discovered", pathArenas);
        tag.putBoolean("path_arena_discovered", isPathArenaDiscovered());
        tag.putInt("path_arena_victories", pathArenaVictories);
        tag.putString("sought_path", soughtPath.getId());
        tag.putInt("sought_x", soughtX);
        tag.putInt("sought_y", soughtY);
        tag.putInt("sought_z", soughtZ);
        tag.putString("sought_dimension", soughtDimension.toString());
        CompoundTag rankTag = new CompoundTag();
        for (StarRailPath path : StarRailPath.values()) {
            if (path.isRealPath()) {
                StarRailPathRank rank = getPathRank(path);
                if (rank != StarRailPathRank.UNALIGNED) {
                    rankTag.putInt(path.getId(), rank.getLevel());
                }
            }
        }
        tag.put("path_ranks", rankTag);
        CompoundTag traces = new CompoundTag();
        for (StarRailPath path : StarRailPath.values()) if (path.isRealPath()) traces.putInt(path.getId(), getTraceMask(path));
        tag.put("path_traces", traces);
        CompoundTag practiceTag = new CompoundTag();
        for (StarRailPath path : StarRailPath.values()) {
            if (path.isRealPath()) {
                int progress = getPracticeProgress(path);
                if (progress > 0) {
                    practiceTag.putInt(path.getId(), progress);
                }
            }
        }
        tag.put("path_practice", practiceTag);
        CompoundTag pinnaclePracticeTag = new CompoundTag();
        for (StarRailPath path : StarRailPath.values()) {
            if (path.isRealPath()) {
                int count = getPinnaclePracticeCount(path);
                if (count > 0) {
                    pinnaclePracticeTag.putInt(path.getId(), count);
                }
            }
        }
        tag.put("path_pinnacle_practice", pinnaclePracticeTag);
        tag.putLong("trial_start_tick", trialStartTick);
        tag.putInt("objective_1", objective1Progress);
        tag.putInt("objective_2", objective2Progress);
        tag.putLong("last_hunt_kill_tick", lastHuntKillTick);
        tag.putInt("hunt_intent_stacks", huntIntentStacks);
        tag.putLong("hunt_intent_expire_tick", huntIntentExpireTick);
        tag.putLong("hunt_finisher_cooldown_tick", huntFinisherCooldownTick);
        tag.putInt("preservation_guard_stacks", preservationGuardStacks);
        tag.putLong("preservation_guard_expire_tick", preservationGuardExpireTick);
        tag.putLong("preservation_barrier_cooldown_tick", preservationBarrierCooldownTick);
        tag.putLong("preservation_barrier_expire_tick", preservationBarrierExpireTick);
        tag.putLong("preservation_next_shield_tick", preservationNextShieldTick);
        tag.putInt("destruction_wrath_stacks", destructionWrathStacks);
        tag.putLong("destruction_wrath_expire_tick", destructionWrathExpireTick);
        tag.putDouble("destruction_attack_bonus", destructionAttackBonus);
        tag.putLong("destruction_attack_bonus_expire_tick", destructionAttackBonusExpireTick);
        tag.putLong("destruction_desperation_cooldown_tick",
                destructionDesperationCooldownTick);
        tag.putLong("erudition_hit_tick", eruditionHitTick);
        tag.putInt("erudition_hit_count", eruditionHitCount);
        tag.putLong("last_erudition_multi_hit_tick", lastEruditionMultiHitTick);
        tag.putLong("erudition_echo_cooldown_tick", eruditionEchoCooldownTick);
        tag.putLong("erudition_analysis_expire_tick", eruditionAnalysisExpireTick);
        tag.putLong("erudition_analysis_window_expire_tick", eruditionAnalysisWindowExpireTick);
        ListTag analysisTargets = new ListTag();
        for (UUID targetId : eruditionAnalysisTargets) {
            CompoundTag targetTag = new CompoundTag();
            targetTag.putUUID("id", targetId);
            analysisTargets.add(targetTag);
        }
        tag.put("erudition_analysis_targets", analysisTargets);
        tag.putLong("erudition_final_cooldown_tick", eruditionFinalCooldownTick);
        if (remembranceTargetId != null) {
            tag.putUUID("remembrance_target_id", remembranceTargetId);
        }
        tag.putLong("remembrance_target_tick", remembranceTargetTick);
        tag.putInt("elation_combo", elationCombo);
        tag.putLong("elation_last_hit_tick", elationLastHitTick);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        traceMasks.clear();
        CompoundTag traces = tag.getCompound("path_traces");
        for (StarRailPath path : StarRailPath.values()) if (path.isRealPath()) setTraceMask(path, traces.getInt(path.getId()));
        currentPath = StarRailPath.byId(tag.getString("current_path"));
        trialPath = StarRailPath.byId(tag.getString("trial_path"));
        trialRank = tag.contains("trial_rank")
                ? StarRailPathRank.fromLevel(tag.getInt("trial_rank"))
                : StarRailPathRank.UNALIGNED;
        pathUnlocked = tag.getBoolean("path_unlocked");
        discoveredPathRegions.clear();
        if (tag.contains("path_regions_discovered", 9)) {
            ListTag regions = tag.getList("path_regions_discovered", 8);
            for (int i = 0; i < regions.size(); i++) {
                discoveredPathRegions.add(regions.getString(i));
            }
        }
        discoveredPathRuins.clear();
        if (tag.contains("path_ruins_discovered", 9)) {
            ListTag ruins = tag.getList("path_ruins_discovered", 8);
            for (int i = 0; i < ruins.size(); i++) discoveredPathRuins.add(ruins.getString(i));
        }
        discoveredPathRuinPositions.clear();
        if (tag.contains("path_ruin_positions", 10)) {
            CompoundTag ruinPositions = tag.getCompound("path_ruin_positions");
            for (String ruinId : ruinPositions.getAllKeys()) {
                if (discoveredPathRuins.contains(ruinId)) {
                    discoveredPathRuinPositions.put(ruinId, ruinPositions.getLong(ruinId));
                }
            }
        }
        archiveCustomNames.clear();
        if (tag.contains("path_archive_custom_names", 10)) {
            CompoundTag archiveNames = tag.getCompound("path_archive_custom_names");
            for (String key : archiveNames.getAllKeys()) {
                String value = archiveNames.getString(key);
                if (!value.isBlank() && value.length() <= 64) archiveCustomNames.put(key, value);
            }
        }
        discoveredPathBeacons.clear();
        if (tag.contains("path_beacons_discovered", 9)) {
            ListTag beacons = tag.getList("path_beacons_discovered", 4);
            for (int i = 0; i < beacons.size(); i++) {
                discoveredPathBeacons.add(((net.minecraft.nbt.LongTag) beacons.get(i)).getAsLong());
            }
        }
        discoveredPathArenas.clear();
        if (tag.contains("path_arenas_discovered", 9)) {
            ListTag arenas = tag.getList("path_arenas_discovered", 4);
            for (int i = 0; i < arenas.size(); i++) {
                discoveredPathArenas.add(((net.minecraft.nbt.LongTag) arenas.get(i)).getAsLong());
            }
        }
        legacyPathArenaDiscovered = tag.getBoolean("path_arena_discovered")
                && discoveredPathArenas.isEmpty();
        pathArenaVictories = Math.max(0, tag.getInt("path_arena_victories"));
        soughtPath = StarRailPath.byId(tag.getString("sought_path"));
        soughtX = tag.getInt("sought_x");
        soughtY = tag.getInt("sought_y");
        soughtZ = tag.getInt("sought_z");
        ResourceLocation savedSoughtDimension = ResourceLocation.tryParse(
                tag.getString("sought_dimension"));
        soughtDimension = savedSoughtDimension == null
                ? Level.OVERWORLD.location() : savedSoughtDimension;
        pathRanks.clear();
        if (tag.contains("path_ranks")) {
            CompoundTag rankTag = tag.getCompound("path_ranks");
            for (StarRailPath path : StarRailPath.values()) {
                if (path.isRealPath() && rankTag.contains(path.getId())) {
                    setPathRank(path,
                            StarRailPathRank.fromLevel(rankTag.getInt(path.getId())));
                }
            }
        }
        practiceProgress.clear();
        if (tag.contains("path_practice")) {
            CompoundTag practiceTag = tag.getCompound("path_practice");
            for (StarRailPath path : StarRailPath.values()) {
                if (path.isRealPath() && practiceTag.contains(path.getId())) {
                    setPracticeProgress(path, practiceTag.getInt(path.getId()));
                }
            }
        }
        pinnaclePracticeCount.clear();
        if (tag.contains("path_pinnacle_practice")) {
            CompoundTag pinnaclePracticeTag = tag.getCompound("path_pinnacle_practice");
            for (StarRailPath path : StarRailPath.values()) {
                if (path.isRealPath() && pinnaclePracticeTag.contains(path.getId())) {
                    setPinnaclePracticeCount(path,
                            pinnaclePracticeTag.getInt(path.getId()));
                }
            }
        }
        // Existing worlds created before the rank framework count as level one.
        if (currentPath.isRealPath()
                && getPathRank(currentPath) == StarRailPathRank.UNALIGNED) {
            setPathRank(currentPath, StarRailPathRank.PATHFARING);
        }
        trialStartTick = tag.getLong("trial_start_tick");
        objective1Progress = tag.getInt("objective_1");
        objective2Progress = tag.getInt("objective_2");
        lastHuntKillTick = tag.contains("last_hunt_kill_tick")
                ? tag.getLong("last_hunt_kill_tick") : -1L;
        setHuntIntentStacks(tag.getInt("hunt_intent_stacks"));
        huntIntentExpireTick = tag.contains("hunt_intent_expire_tick")
                ? tag.getLong("hunt_intent_expire_tick") : -1L;
        huntFinisherCooldownTick = tag.contains("hunt_finisher_cooldown_tick")
                ? tag.getLong("hunt_finisher_cooldown_tick") : -1L;
        setPreservationGuardStacks(tag.getInt("preservation_guard_stacks"));
        preservationGuardExpireTick = tag.contains("preservation_guard_expire_tick")
                ? tag.getLong("preservation_guard_expire_tick") : -1L;
        preservationBarrierCooldownTick = tag.contains("preservation_barrier_cooldown_tick")
                ? tag.getLong("preservation_barrier_cooldown_tick") : -1L;
        preservationBarrierExpireTick = tag.contains("preservation_barrier_expire_tick")
                ? tag.getLong("preservation_barrier_expire_tick") : -1L;
        preservationNextShieldTick = tag.contains("preservation_next_shield_tick")
                ? tag.getLong("preservation_next_shield_tick") : -1L;
        setDestructionWrathStacks(tag.getInt("destruction_wrath_stacks"));
        destructionWrathExpireTick = tag.contains("destruction_wrath_expire_tick")
                ? tag.getLong("destruction_wrath_expire_tick") : -1L;
        setDestructionAttackBonus(tag.getDouble("destruction_attack_bonus"));
        destructionAttackBonusExpireTick = tag.contains("destruction_attack_bonus_expire_tick")
                ? tag.getLong("destruction_attack_bonus_expire_tick") : -1L;
        destructionDesperationCooldownTick = tag.contains(
                "destruction_desperation_cooldown_tick")
                ? tag.getLong("destruction_desperation_cooldown_tick") : -1L;
        eruditionHitTick = tag.contains("erudition_hit_tick")
                ? tag.getLong("erudition_hit_tick") : -1L;
        eruditionHitCount = tag.getInt("erudition_hit_count");
        lastEruditionMultiHitTick = tag.contains("last_erudition_multi_hit_tick")
                ? tag.getLong("last_erudition_multi_hit_tick") : -1L;
        eruditionEchoCooldownTick = tag.contains("erudition_echo_cooldown_tick")
                ? tag.getLong("erudition_echo_cooldown_tick") : -1L;
        eruditionAnalysisExpireTick = tag.contains("erudition_analysis_expire_tick")
                ? tag.getLong("erudition_analysis_expire_tick") : -1L;
        eruditionAnalysisWindowExpireTick = tag.contains(
                "erudition_analysis_window_expire_tick")
                ? tag.getLong("erudition_analysis_window_expire_tick") : -1L;
        eruditionAnalysisTargets.clear();
        if (tag.contains("erudition_analysis_targets")) {
            ListTag analysisTargets = tag.getList("erudition_analysis_targets", 10);
            for (int index = 0; index < analysisTargets.size(); index++) {
                CompoundTag targetTag = analysisTargets.getCompound(index);
                if (targetTag.hasUUID("id")) {
                    eruditionAnalysisTargets.add(targetTag.getUUID("id"));
                }
            }
        }
        eruditionFinalCooldownTick = tag.contains("erudition_final_cooldown_tick")
                ? tag.getLong("erudition_final_cooldown_tick") : -1L;
        remembranceTargetId = tag.hasUUID("remembrance_target_id")
                ? tag.getUUID("remembrance_target_id") : null;
        remembranceTargetTick = tag.contains("remembrance_target_tick")
                ? tag.getLong("remembrance_target_tick") : -1L;
        elationCombo = tag.getInt("elation_combo");
        elationLastHitTick = tag.contains("elation_last_hit_tick")
                ? tag.getLong("elation_last_hit_tick") : -1L;
    }
}
