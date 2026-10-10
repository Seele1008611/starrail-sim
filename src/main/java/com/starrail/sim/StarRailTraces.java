package com.starrail.sim;

import java.util.EnumMap;
import java.util.Map;

/** Shared path-specific trace definitions. Node IDs remain stable in saves/network packets. */
public final class StarRailTraces {
    public static final int CRIT_RATE = 0;
    public static final int ATTACK = 1;
    public static final int CRIT_DAMAGE = 2;
    public static final int MAX_HEALTH = 3;
    public static final int ARMOR = 4;
    public static final int ARMOR_TOUGHNESS = 5;
    public static final int EFFECT_HIT_RATE = 6;
    public static final int BREAK_EFFECT = 7;
    public static final int HEALING_EFFECT = 8;

    public record Node(String name, String description, int rank, int cost, int prerequisite,
                       int stat, double bonus, int x, int y) { }

    private static final Map<StarRailPath, Node[]> TREES = new EnumMap<>(StarRailPath.class);
    private static final Map<StarRailPath, int[]> STATS = new EnumMap<>(StarRailPath.class);
    public static final Node[] HUNT;

    static {
        register(StarRailPath.HUNT,
                new String[]{"校准准星", "迅捷箭矢", "致命落点", "弱点洞察", "追猎锋芒", "终局瞄准"},
                new int[]{CRIT_RATE, ATTACK, CRIT_DAMAGE}, new int[]{10, 50, 50},
                new int[][]{{480,332},{337,408},{480,488},{630,408},{480,610},{480,763},{480,225},{322,620},{633,620}},
                new String[][]{
                    {"精准追击", "猎意达到 3 层并触发追猎一击时，该次攻击获得 +5% 暴击率。", "5"},
                    {"追猎余势", "追猎一击命中后，4 秒内对同一目标的下一次攻击获得 +8% 暴击伤害。", "5"},
                    {"逆时猎杀", "逆时追猎冷却由 8 秒降低至 5 秒。", "7"}
                });
        register(StarRailPath.PRESERVATION,
                new String[]{"盾面淬炼", "坚壁加固", "壁垒核心", "护缘叠层", "稳固阵列", "不破之盾"},
                new int[]{ARMOR, ARMOR_TOUGHNESS}, new int[]{60, 60},
                new int[][]{{532,262},{387,421},{532,403},{677,421},{532,564},{532,674},{532,161},{390,683},{678,683}},
                new String[][]{
                    {"守势余裕", "护壁生效时，首次成功格挡额外获得一层短暂吸收护盾；每次护壁限触发一次。", "5"},
                    {"反震蓄势", "护壁反震命中后，下一次格挡所需的护卫层数减少 1 层。", "6"},
                    {"壁垒共鸣", "强化护壁期间，反震伤害提高 20%。", "7"}
                });
        register(StarRailPath.ABUNDANCE,
                new String[]{"生命萌芽", "回春脉络", "丰沛根系", "甘露回响", "生生不息", "慈泽满溢"},
                new int[]{MAX_HEALTH, HEALING_EFFECT}, new int[]{60, 60},
                new int[][]{{463,284},{324,393},{463,424},{602,393},{463,562},{541,697},{463,70},{245,610},{681,610}},
                new String[][]{
                    {"回春余韵", "生命值低于 50% 时受到治疗，额外获得持续 4 秒的缓慢生命恢复。", "4"},
                    {"溢疗化生", "溢出治疗转化为吸收量的比例提高 15%。", "5"},
                    {"不息庇护", "抵挡致死伤害后，额外恢复最大生命值的 10%。", "7"}
                });
        register(StarRailPath.DESTRUCTION,
                new String[]{"余烬攻势", "烈火锻锋", "焦灼核心", "逆燃战意", "焚身强袭", "终焉余烬"},
                new int[]{CRIT_DAMAGE, ATTACK}, new int[]{60, 60},
                new int[][]{{514,257},{349,373},{514,396},{680,373},{514,549},{514,680},{514,153},{335,566},{692,566}},
                new String[][]{
                    {"余烬不熄", "怒火层数达到上限后，持续时间延长 2 秒。", "5"},
                    {"破釜一击", "消耗满层怒火的强化攻击额外获得 +10% 暴击伤害。", "6"},
                    {"绝境回燃", "绝境强化触发后，下一次攻击额外造成一次小范围灼烈冲击。", "7"}
                });
        register(StarRailPath.ERUDITION,
                new String[]{"星图洞察", "并行演算", "临界精度", "回响推演", "知识汇流", "终局定理"},
                new int[]{CRIT_RATE, CRIT_DAMAGE, ATTACK}, new int[]{10, 50, 50},
                new int[][]{{461,252},{320,418},{461,418},{600,418},{461,650},{324,636},{461,72},{205,418},{714,418}},
                new String[][]{
                    {"多重推演", "知识回响额外波及 1 个目标。", "4"},
                    {"回响增幅", "知识回响造成的伤害提高 15%。", "5"},
                    {"群星定论", "命中三个不同敌人触发强化攻击后，短时间内下一次知识回响范围扩大。", "7"}
                });
        register(StarRailPath.NIHILITY,
                new String[]{"侵蚀刻印", "暗潮命中", "虚空增压", "裂隙破击", "沉降攻势", "终末扩散"},
                new int[]{EFFECT_HIT_RATE, ATTACK, BREAK_EFFECT}, new int[]{50, 50, 50},
                new int[][]{{532,183},{375,320},{532,311},{688,325},{532,461},{532,570},{532,56},{247,276},{807,276}},
                new String[][]{
                    {"侵蚀余波", "侵蚀首次扩散时，额外传递给 1 个附近目标。", "5"},
                    {"持续崩解", "对已被侵蚀标记的目标造成的持续伤害提高 15%。", "6"},
                    {"虚空回响", "击杀被自己标记的敌人触发爆发后，周围目标额外承受一次较弱的侵蚀。", "7"}
                });
        register(StarRailPath.HARMONY,
                new String[]{"和弦护持", "生命协奏", "韧性节拍", "共鸣增幅", "交响壁垒", "终曲守望"},
                new int[]{ARMOR, MAX_HEALTH, ARMOR_TOUGHNESS}, new int[]{50, 50, 50},
                new int[][]{{362,355},{501,330},{639,356},{500,461},{500,593},{500,697},{500,179},{178,425},{830,427}},
                new String[][]{
                    {"延音共鸣", "共鸣持续时间额外延长 4 秒。", "4"},
                    {"协奏强袭", "共鸣期间可触发的增伤攻击次数增加 1 次。", "5"},
                    {"终曲回护", "共鸣期间击败敌人时，额外恢复最大生命值的 5%。", "6"}
                });
        register(StarRailPath.REMEMBRANCE,
                new String[]{"初始记录", "记忆刻痕", "回响增幅", "追忆之环", "长存印记", "终章回响"},
                new int[]{ATTACK, CRIT_DAMAGE}, new int[]{60, 60},
                new int[][]{{493,236},{276,436},{356,572},{713,436},{630,572},{493,617},{342,290},{849,436},{493,747}},
                new String[][]{
                    {"延续记录", "记忆记录窗口额外延长 3 秒。", "4"},
                    {"回忆追击", "记忆回响后，对该目标的下一击额外获得 +10% 攻击力。", "5"},
                    {"深层回响", "强化回响触发所需的回响累计次数减少 1 次。", "7"}
                });
        register(StarRailPath.ELATION,
                new String[]{"欢声起势", "连击节拍", "奇趣增幅", "笑意回环", "高潮迭起", "盛宴终章"},
                new int[]{CRIT_RATE, ATTACK, CRIT_DAMAGE}, new int[]{10, 50, 50},
                new int[][]{{594,94},{450,148},{738,148},{594,265},{594,410},{594,558},{594,725},{306,237},{878,237}},
                new String[][]{
                    {"余兴未散", "连击中断前的等待窗口额外延长 2 秒。", "4"},
                    {"欢愉加码", "欢愉爆发造成的伤害提高 15%。", "5"},
                    {"盛宴续曲", "强化爆发命中后，后续攻击强化次数额外增加 2 次。", "7"}
                });
        HUNT = TREES.get(StarRailPath.HUNT);
    }

    private StarRailTraces() { }

    private static void register(StarRailPath path, String[] names, int[] stats, int[] totals,
                                 int[][] positions, String[][] passives) {
        int[] assigned = stats.length == 2
                ? new int[]{stats[0], stats[1], stats[0], stats[1], stats[0], stats[1]}
                : new int[]{stats[0], stats[1], stats[2], stats[0], stats[1], stats[2]};
        int[] counts = new int[stats.length];
        for (int stat : assigned) for (int i = 0; i < stats.length; i++) if (stats[i] == stat) counts[i]++;
        Node[] nodes = new Node[9];
        int[] ranks = {1, 3, 5, 2, 4, 6};
        for (int i = 0; i < 6; i++) {
            int stat = assigned[i], total = 0, statIndex = 0;
            for (int j = 0; j < stats.length; j++) if (stats[j] == stat) { total = totals[j]; statIndex = j; }
            double amount = total / (double) counts[statIndex] / 100.0;
            int prerequisite = i % 3 == 0 ? -1 : i - 1;
            nodes[i] = new Node(names[i], statName(stat) + " +" + Math.round(amount * 100) + "%",
                    ranks[i], ranks[i], prerequisite, stat, amount, positions[i][0], positions[i][1]);
        }
        for (int i = 0; i < 3; i++) {
            int node = i + 6;
            int rank = Integer.parseInt(passives[i][2]);
            nodes[node] = new Node(passives[i][0], passives[i][1], rank, i + 3,
                    i == 0 ? -1 : node - 1, -1, 0, positions[node][0], positions[node][1]);
        }
        TREES.put(path, nodes);
        STATS.put(path, stats.clone());
    }

    public static String statName(int stat) {
        return switch (stat) {
            case CRIT_RATE -> "暴击率";
            case ATTACK -> "攻击力";
            case CRIT_DAMAGE -> "暴击伤害";
            case MAX_HEALTH -> "最大生命";
            case ARMOR -> "护甲值";
            case ARMOR_TOUGHNESS -> "护甲韧性";
            case EFFECT_HIT_RATE -> "效果命中";
            case BREAK_EFFECT -> "击破特攻";
            case HEALING_EFFECT -> "治疗效果";
            default -> "属性";
        };
    }

    public static Node[] nodes(StarRailPath path) { return TREES.getOrDefault(path, HUNT); }
    public static int[] stats(StarRailPath path) { return STATS.getOrDefault(path, STATS.get(StarRailPath.HUNT)).clone(); }
    public static boolean has(int mask, int node) { return node >= 0 && node < 9 && (mask & (1 << node)) != 0; }
    public static boolean canUnlock(int mask, int rank, int node) { return canUnlock(StarRailPath.HUNT, mask, rank, node); }
    public static boolean canUnlock(StarRailPath path, int mask, int rank, int node) {
        Node[] tree = nodes(path);
        if (node < 0 || node >= tree.length || has(mask, node)) return false;
        Node rule = tree[node];
        return rank >= rule.rank && (rule.prerequisite < 0 || has(mask, rule.prerequisite));
    }
    public static int rollback(int mask, int node) { return rollback(StarRailPath.HUNT, mask, node); }
    /** Remove descendants as well, so a saved tree never has an orphaned unlock. */
    public static int rollback(StarRailPath path, int mask, int node) {
        Node[] tree = nodes(path);
        if (!has(mask, node)) return mask;
        int result = mask & ~(1 << node);
        for (int i = 0; i < tree.length; i++) {
            if (has(result, i) && tree[i].prerequisite >= 0 && !has(result, tree[i].prerequisite)) {
                result = rollback(path, result, i);
            }
        }
        return result;
    }
    public static int cost(int mask) { return cost(StarRailPath.HUNT, mask); }
    public static int cost(StarRailPath path, int mask) {
        int total = 0; Node[] tree = nodes(path);
        for (int i = 0; i < tree.length; i++) if (has(mask, i)) total += tree[i].cost;
        return total;
    }
    public static double bonus(int mask, int stat) { return bonus(StarRailPath.HUNT, mask, stat); }
    public static double bonus(StarRailPath path, int mask, int stat) {
        double total = 0; Node[] tree = nodes(path);
        for (int i = 0; i < tree.length; i++) if (has(mask, i) && tree[i].stat == stat) total += tree[i].bonus;
        return total;
    }
}
