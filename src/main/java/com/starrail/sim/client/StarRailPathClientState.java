package com.starrail.sim.client;

/**
 * 模组代码说明：保存客户端收到的命途状态，供命途界面与提示显示使用。
 */

import com.starrail.sim.StarRailPath;
import com.starrail.sim.StarRailPathEffects;
import com.starrail.sim.StarRailPathProgress;
import com.starrail.sim.StarRailPathRank;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import java.util.Map;

/** Client-side snapshot for rendering path progression and the travel archive. */
public final class StarRailPathClientState {
    private static boolean unlocked;
    private static StarRailPath currentPath = StarRailPath.NONE;
    private static StarRailPathRank currentPathRank = StarRailPathRank.UNALIGNED;
    private static int practiceProgress;
    private static int practiceTarget;
    private static int pinnaclePracticeCount;
    private static StarRailPath trialPath = StarRailPath.NONE;
    private static StarRailPathRank trialRank = StarRailPathRank.UNALIGNED;
    private static int objective1;
    private static int objective2;
    private static int secondsRemaining;
    private static StarRailPath soughtPath = StarRailPath.NONE;
    private static int soughtX;
    private static int soughtY;
    private static int soughtZ;
    private static ResourceLocation soughtDimension = Level.OVERWORLD.location();
    private static int pathRegionMask;
    private static int pathRuinMask;
    private static int pathRuinPositionMask;
    private static long[] pathRuinPositions = new long[0];
    private static int pathBeaconCount;
    private static long[] pathBeaconPositions = new long[0];
    private static long[] pathArenaPositions = new long[0];
    private static boolean pathArenaDiscovered;
    private static int pathArenaVictories;
    private static Map<String, String> pathArchiveCustomNames = Map.of();

    private StarRailPathClientState() {
    }

    public static void update(boolean pathUnlocked, StarRailPath current,
                              StarRailPathRank currentRank, int currentPractice,
                              int currentPracticeTarget, int currentPinnaclePracticeCount,
                              StarRailPath trial,
                              StarRailPathRank breakthroughRank,
                              int firstObjective,
                              int secondObjective, int remaining,
                              StarRailPath lastSoughtPath, ResourceLocation lastSoughtDimension,
                              int lastSoughtX,
                              int lastSoughtY, int lastSoughtZ,
                              int discoveredRegionMask, int discoveredRuinMask,
                              int discoveredRuinPositionMask, long[] ruinPositions,
                              int discoveredBeaconCount, long[] beaconPositions,
                              long[] arenaPositions, boolean arenaDiscovered,
                              int arenaVictories, Map<String, String> archiveCustomNames) {
        unlocked = pathUnlocked;
        currentPath = current;
        currentPathRank = currentRank;
        practiceProgress = currentPractice;
        practiceTarget = currentPracticeTarget;
        pinnaclePracticeCount = currentPinnaclePracticeCount;
        trialPath = trial;
        trialRank = breakthroughRank;
        objective1 = firstObjective;
        objective2 = secondObjective;
        secondsRemaining = remaining;
        soughtPath = lastSoughtPath;
        soughtDimension = lastSoughtDimension == null
                ? Level.OVERWORLD.location() : lastSoughtDimension;
        soughtX = lastSoughtX;
        soughtY = lastSoughtY;
        soughtZ = lastSoughtZ;
        pathRegionMask = discoveredRegionMask;
        pathRuinMask = discoveredRuinMask;
        pathRuinPositionMask = discoveredRuinPositionMask;
        pathRuinPositions = ruinPositions.clone();
        pathBeaconCount = discoveredBeaconCount;
        pathBeaconPositions = beaconPositions.clone();
        pathArenaPositions = arenaPositions.clone();
        pathArenaDiscovered = arenaDiscovered;
        pathArenaVictories = arenaVictories;
        pathArchiveCustomNames = Map.copyOf(archiveCustomNames);

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            StarRailPathEffects.refresh(minecraft.player, currentPath, currentPathRank,
                    pinnaclePracticeCount);
        }
    }

    public static boolean isUnlocked() {
        return unlocked;
    }

    public static StarRailPath getCurrentPath() {
        return currentPath;
    }

    public static StarRailPathRank getCurrentPathRank() {
        return currentPathRank;
    }

    public static int getPracticeProgress() {
        return practiceProgress;
    }

    public static int getPracticeTarget() {
        return practiceTarget;
    }

    public static int getPinnaclePracticeCount() {
        return pinnaclePracticeCount;
    }

    public static int getPinnaclePracticeMax() {
        return StarRailPathProgress.MAX_PINNACLE_PRACTICE_COUNT;
    }

    public static StarRailPath getTrialPath() {
        return trialPath;
    }

    public static StarRailPathRank getTrialRank() {
        return trialRank;
    }

    public static int getObjective1() {
        return objective1;
    }

    public static int getObjective2() {
        return objective2;
    }

    public static int getSecondsRemaining() {
        return secondsRemaining;
    }

    public static StarRailPath getSoughtPath() {
        return soughtPath;
    }

    public static int getSoughtX() {
        return soughtX;
    }

    public static int getSoughtY() {
        return soughtY;
    }

    public static int getSoughtZ() {
        return soughtZ;
    }

    public static ResourceLocation getSoughtDimension() {
        return soughtDimension;
    }

    public static int getPathRegionMask() { return pathRegionMask; }
    public static int getPathRuinMask() { return pathRuinMask; }
    public static int getPathRuinPositionMask() { return pathRuinPositionMask; }
    public static long[] getPathRuinPositions() { return pathRuinPositions.clone(); }
    public static int getPathBeaconCount() { return pathBeaconCount; }
    public static long[] getPathBeaconPositions() { return pathBeaconPositions.clone(); }
    public static long[] getPathArenaPositions() { return pathArenaPositions.clone(); }
    public static boolean isPathArenaDiscovered() { return pathArenaDiscovered; }
    public static int getPathArenaVictories() { return pathArenaVictories; }
    public static String getArchiveCustomName(String recordKey) {
        return pathArchiveCustomNames.getOrDefault(recordKey, "");
    }
}
