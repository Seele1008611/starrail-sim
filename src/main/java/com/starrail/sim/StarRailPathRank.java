package com.starrail.sim;

/**
 * 模组代码说明：命途阶位枚举，提供阶位顺序和界面显示所需信息。
 */

import java.util.Arrays;

/** Seven-step path depth framework. Numeric effects are intentionally defined later. */
public enum StarRailPathRank {
    UNALIGNED(0, "unaligned", "rank.starrail_sim.unaligned"),
    PATHFARING(1, "pathfaring", "rank.starrail_sim.pathfaring"),
    GLIMPSE(2, "glimpse", "rank.starrail_sim.glimpse"),
    RESONANCE(3, "resonance", "rank.starrail_sim.resonance"),
    PRACTICE(4, "practice", "rank.starrail_sim.practice"),
    DEEP_PRACTICE(5, "deep_practice", "rank.starrail_sim.deep_practice"),
    HIGH_PATHSTRIDER(6, "high_pathstrider", "rank.starrail_sim.high_pathstrider"),
    PATH_PINNACLE(7, "path_pinnacle", "rank.starrail_sim.path_pinnacle");

    private final int level;
    private final String id;
    private final String translationKey;

    StarRailPathRank(int level, String id, String translationKey) {
        this.level = level;
        this.id = id;
        this.translationKey = translationKey;
    }

    public int getLevel() {
        return level;
    }

    public String getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public static StarRailPathRank byId(String id) {
        return Arrays.stream(values())
                .filter(rank -> rank.id.equalsIgnoreCase(id))
                .findFirst()
                .orElse(UNALIGNED);
    }

    public static StarRailPathRank fromLevel(int level) {
        return Arrays.stream(values())
                .filter(rank -> rank.level == level)
                .findFirst()
                .orElse(UNALIGNED);
    }
}
