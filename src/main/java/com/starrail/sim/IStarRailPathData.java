package com.starrail.sim;

/**
 * 模组代码说明：玩家命途能力数据接口，规定命途、试炼进度及各命途战斗状态的读写入口。
 */

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/** Persistent player state for path selection and trial progress. */
public interface IStarRailPathData {
    StarRailPath getCurrentPath();

    void setCurrentPath(StarRailPath path);

    StarRailPath getTrialPath();

    void setTrialPath(StarRailPath path);

    /** The rank being attempted by an automatic breakthrough trial. */
    StarRailPathRank getTrialRank();

    void setTrialRank(StarRailPathRank rank);

    default boolean isRankTrial() {
        return getTrialRank() != StarRailPathRank.UNALIGNED;
    }

    boolean isPathUnlocked();

    void setPathUnlocked(boolean unlocked);

    /** 当前玩家已寻迹到、可用于校验试炼凭证的命途遗迹。 */
    StarRailPath getSoughtPath();

    int getSoughtX();

    int getSoughtY();

    int getSoughtZ();

    void setSoughtRuin(StarRailPath path, int x, int y, int z);

    void clearSoughtRuin();

    StarRailPathRank getPathRank(StarRailPath path);

    void setPathRank(StarRailPath path, StarRailPathRank rank);

    int getPracticeProgress(StarRailPath path);

    void setPracticeProgress(StarRailPath path, int progress);

    /** Number of post-pinnacle 120-practice milestones completed for a path. */
    int getPinnaclePracticeCount(StarRailPath path);

    void setPinnaclePracticeCount(StarRailPath path, int count);

    default StarRailPathRank getCurrentPathRank() {
        return getPathRank(getCurrentPath());
    }

    long getTrialStartTick();

    void setTrialStartTick(long tick);

    int getObjective1Progress();

    void setObjective1Progress(int progress);

    int getObjective2Progress();

    void setObjective2Progress(int progress);

    long getLastHuntKillTick();

    void setLastHuntKillTick(long tick);

    int getHuntIntentStacks();

    void setHuntIntentStacks(int stacks);

    long getHuntIntentExpireTick();

    void setHuntIntentExpireTick(long tick);

    long getHuntFinisherCooldownTick();

    void setHuntFinisherCooldownTick(long tick);

    int getPreservationGuardStacks();

    void setPreservationGuardStacks(int stacks);

    long getPreservationGuardExpireTick();

    void setPreservationGuardExpireTick(long tick);

    long getPreservationBarrierCooldownTick();

    void setPreservationBarrierCooldownTick(long tick);

    long getPreservationBarrierExpireTick();

    void setPreservationBarrierExpireTick(long tick);

    long getPreservationNextShieldTick();

    void setPreservationNextShieldTick(long tick);

    int getDestructionWrathStacks();

    void setDestructionWrathStacks(int stacks);

    long getDestructionWrathExpireTick();

    void setDestructionWrathExpireTick(long tick);

    double getDestructionAttackBonus();

    void setDestructionAttackBonus(double bonus);

    long getDestructionAttackBonusExpireTick();

    void setDestructionAttackBonusExpireTick(long tick);

    long getDestructionDesperationCooldownTick();

    void setDestructionDesperationCooldownTick(long tick);

    long getEruditionHitTick();

    void setEruditionHitTick(long tick);

    int getEruditionHitCount();

    void setEruditionHitCount(int count);

    long getLastEruditionMultiHitTick();

    void setLastEruditionMultiHitTick(long tick);

    long getEruditionEchoCooldownTick();

    void setEruditionEchoCooldownTick(long tick);

    long getEruditionAnalysisExpireTick();

    void setEruditionAnalysisExpireTick(long tick);

    long getEruditionAnalysisWindowExpireTick();

    void setEruditionAnalysisWindowExpireTick(long tick);

    boolean hasEruditionAnalysisTarget(UUID targetId);

    boolean addEruditionAnalysisTarget(UUID targetId);

    UUID getEruditionAnalysisTarget(int index);

    void clearEruditionAnalysisTargets();

    long getEruditionFinalCooldownTick();

    void setEruditionFinalCooldownTick(long tick);

    UUID getRemembranceTargetId();

    void setRemembranceTargetId(UUID targetId);

    long getRemembranceTargetTick();

    void setRemembranceTargetTick(long tick);

    int getElationCombo();

    void setElationCombo(int combo);

    long getElationLastHitTick();

    void setElationLastHitTick(long tick);

    boolean isTrialComplete();

    void resetTrial();

    void copyFrom(IStarRailPathData other);

    CompoundTag serializeNBT();

    void deserializeNBT(CompoundTag tag);
}
