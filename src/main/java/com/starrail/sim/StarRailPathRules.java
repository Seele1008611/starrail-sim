package com.starrail.sim;

/**
 * 模组代码说明：命途试炼规则表，定义已实现命途、目标数量和各阶位的目标缩放与时限。
 */

/** Shared rules for initial path trials and later breakthrough trials. */
public final class StarRailPathRules {
    public static final long TRIAL_TIME_LIMIT = 120L * 20L;
    public static final long HUNT_STREAK_WINDOW = 8L * 20L;
    public static final long REMEMBRANCE_WINDOW = 8L * 20L;
    public static final long ELATION_COMBO_WINDOW = 5L * 20L;

    private StarRailPathRules() {
    }

    // 判断该命途是否已经有完整试炼逻辑。
    public static boolean isImplemented(StarRailPath path) {
        return path == StarRailPath.HUNT
                || path == StarRailPath.PRESERVATION
                || path == StarRailPath.ABUNDANCE
                || path == StarRailPath.DESTRUCTION
                || path == StarRailPath.ERUDITION
                || path == StarRailPath.NIHILITY
                || path == StarRailPath.HARMONY
                || path == StarRailPath.REMEMBRANCE
                || path == StarRailPath.ELATION;
    }

    public static int objective1Target(StarRailPath path) {
        return objective1Target(path, StarRailPathRank.UNALIGNED);
    }

    // 按命途和试炼阶位取得第一项目标数量。
    public static int objective1Target(StarRailPath path, StarRailPathRank trialRank) {
        if (trialRank != null && trialRank.getLevel() >= 3) return breakthroughTarget(path, trialRank, 1);
        int base = switch (path) {
            case HUNT -> 5;
            case PRESERVATION -> 3;
            case ABUNDANCE -> 10;
            case DESTRUCTION -> 3;
            case ERUDITION -> 1;
            case NIHILITY -> 5;
            case HARMONY -> 3;
            case REMEMBRANCE -> 3;
            case ELATION -> 8;
            default -> 0;
        };
        return base;
    }

    public static int objective2Target(StarRailPath path) {
        return objective2Target(path, StarRailPathRank.UNALIGNED);
    }

    // 按命途和试炼阶位取得第二项目标数量。
    public static int objective2Target(StarRailPath path, StarRailPathRank trialRank) {
        if (trialRank != null && trialRank.getLevel() >= 3) return breakthroughTarget(path, trialRank, 2);
        int base = switch (path) {
            case HUNT -> 2;
            case PRESERVATION -> 5;
            case ABUNDANCE -> 3;
            case DESTRUCTION -> 10;
            case ERUDITION -> 3;
            case NIHILITY -> 3;
            case HARMONY -> 3;
            case REMEMBRANCE -> 2;
            case ELATION -> 2;
            default -> 0;
        };
        return base;
    }

    private static int breakthroughTarget(StarRailPath path, StarRailPathRank rank, int objective) {
        int index = Math.max(0, Math.min(4, rank.getLevel() - 3));
        int[] targets = switch (path) {
            case HUNT -> new int[]{8,10,15,20,25 , 2,3,4,5,6};
            case PRESERVATION -> new int[]{5,6,9,12,15 , 8,10,15,20,25};
            case ABUNDANCE -> new int[]{15,20,30,40,50 , 4,5,6,8,10};
            case DESTRUCTION -> new int[]{5,6,9,12,15 , 15,20,30,40,50};
            case ERUDITION -> new int[]{2,3,4,5,6 , 3,4,5,6,8};
            case NIHILITY -> new int[]{8,10,15,20,25 , 5,6,9,12,15};
            case HARMONY -> new int[]{2,3,3,4,5 , 5,6,9,12,15};
            case REMEMBRANCE -> new int[]{5,6,9,12,15 , 3,4,6,8,10};
            case ELATION -> new int[]{8,10,12,15,18 , 3,4,6,8,10};
            default -> new int[10];
        };
        return targets[index + (objective == 2 ? 5 : 0)];
    }

    public static long trialTimeLimit(StarRailPathRank trialRank) {
        return switch (trialRank == null ? StarRailPathRank.UNALIGNED : trialRank) {
            case RESONANCE -> 180L * 20L;
            case PRACTICE -> 300L * 20L;
            case DEEP_PRACTICE -> 480L * 20L;
            case HIGH_PATHSTRIDER -> 600L * 20L;
            case PATH_PINNACLE -> 900L * 20L;
            default -> TRIAL_TIME_LIMIT;
        };
    }

    public static long trialTimeLimit(StarRailPath path, StarRailPathRank trialRank) {
        if (path == StarRailPath.ERUDITION
                && (trialRank == null || trialRank == StarRailPathRank.UNALIGNED)) {
            return 30L * 20L;
        }
        return trialTimeLimit(trialRank);
    }
}
