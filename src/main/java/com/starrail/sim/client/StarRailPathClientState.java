package com.starrail.sim.client;

/**
 * 模组代码说明：保存客户端收到的命途状态，供命途界面与提示显示使用。
 */

import com.starrail.sim.StarRailPath;
import com.starrail.sim.StarRailPathEffects;
import com.starrail.sim.StarRailPathProgress;
import com.starrail.sim.StarRailPathRank;
import net.minecraft.client.Minecraft;

/** Client-side snapshot for rendering the path screen. */
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

    private StarRailPathClientState() {
    }

    public static void update(boolean pathUnlocked, StarRailPath current,
                              StarRailPathRank currentRank, int currentPractice,
                              int currentPracticeTarget, int currentPinnaclePracticeCount,
                              StarRailPath trial,
                              StarRailPathRank breakthroughRank,
                              int firstObjective,
                              int secondObjective, int remaining,
                              StarRailPath lastSoughtPath, int lastSoughtX,
                              int lastSoughtY, int lastSoughtZ) {
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
        soughtX = lastSoughtX;
        soughtY = lastSoughtY;
        soughtZ = lastSoughtZ;

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
}
