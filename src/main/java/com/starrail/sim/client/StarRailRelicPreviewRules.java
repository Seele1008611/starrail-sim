package com.starrail.sim.client;

import java.util.Random;

import static com.starrail.sim.client.StarRailRelicUiData.Stat;

/** Confirmed relic growth values used only by the disposable UI preview. */
final class StarRailRelicPreviewRules {
    private static final int[][] XP_PER_LEVEL = {
            {}, {},
            {100, 100, 100, 200, 200, 200},
            {100, 100, 100, 200, 200, 200, 300, 300, 300},
            {100, 100, 200, 200, 300, 300, 400, 400, 500, 500, 600, 600},
            {100, 100, 200, 200, 300, 300, 400, 400, 500, 600, 700, 800, 900, 1000, 1200}
    };

    private StarRailRelicPreviewRules() {}

    static double quality(int stars) {
        return switch (stars) {
            case 2 -> 0.4;
            case 3 -> 0.6;
            case 4 -> 0.8;
            case 5 -> 1.0;
            default -> throw new IllegalArgumentException("Unsupported relic rarity: " + stars);
        };
    }

    static double mainValue(Stat stat, int stars, int level) {
        double base;
        double perLevel;
        switch (stat) {
            case HP -> { base = 3; perLevel = 0.8; }
            case ATTACK -> { base = 0.75; perLevel = 17.0 / 60.0; }
            case HP_PCT, ATTACK_PCT -> { base = 8; perLevel = 2; }
            case ARMOR_PCT -> { base = 10; perLevel = 2.5; }
            case CRIT -> { base = 5; perLevel = 1; }
            case CRIT_DAMAGE -> { base = 10; perLevel = 2; }
            case HIT -> { base = 8; perLevel = 2; }
            case HEAL -> { base = 6; perLevel = 1.6; }
            case BREAK -> { base = 12; perLevel = 3.2; }
            default -> throw new IllegalArgumentException("Stat cannot be a relic main stat: " + stat);
        }
        return quality(stars) * (base + perLevel * level);
    }

    static int initialSubstatCount(int stars, Random random) {
        if (stars == 2) return 0;
        boolean higherCount = random.nextInt(100) >= 80;
        return switch (stars) {
            case 3 -> higherCount ? 2 : 1;
            case 4 -> higherCount ? 3 : 2;
            case 5 -> higherCount ? 4 : 3;
            default -> throw new IllegalArgumentException("Unsupported relic rarity: " + stars);
        };
    }

    static double substatRoll(Stat stat, int stars, Random random) {
        double middle = switch (stat) {
            case HP -> 1.2;
            case HP_PCT, ATTACK_PCT, ARMOR_PCT, CRIT_DAMAGE -> 3.0;
            case ATTACK -> 0.3;
            case ARMOR -> 0.5;
            case TOUGHNESS -> 0.2;
            case TOUGHNESS_PCT -> 2.0;
            case CRIT -> 1.5;
            case HIT, RESIST, HEAL -> 2.0;
            case BREAK -> 3.0;
        };
        double tier = switch (random.nextInt(3)) {
            case 0 -> 0.8;
            case 1 -> 1.0;
            default -> 1.2;
        };
        return middle * quality(stars) * tier;
    }

    static int experienceToLevel(int stars, int level) {
        if (stars < 2 || stars > 5 || level < 0 || level > XP_PER_LEVEL[stars].length) {
            throw new IllegalArgumentException("Invalid relic rarity/level: " + stars + "/" + level);
        }
        int experience = 0;
        for (int n = 0; n < level; n++) experience += XP_PER_LEVEL[stars][n];
        return experience;
    }

    static int crystalCost(int stars, int currentLevel, int targetLevel) {
        return (experienceToLevel(stars, targetLevel) - experienceToLevel(stars, currentLevel)) / 100;
    }

    static int crystalSalvage(int stars, int level) {
        int base = stars <= 3 ? 1 : stars == 4 ? 3 : 5;
        return base + experienceToLevel(stars, level) * 80 / 10_000;
    }
}
