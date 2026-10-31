package com.starrail.sim;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Shared rank-based stat growth granted by every active path. */
public final class StarRailPathGrowth {
    private StarRailPathGrowth() {
    }

    public static int clampLevel(int level) {
        return Math.max(0, Math.min(7, level));
    }

    public static double regenerationFraction(int level) {
        return clampLevel(level) * 1.2D / 100.0D;
    }

    public static double maxHealthBonus(int level) {
        return clampLevel(level) * 0.48D / 7.0D;
    }

    /** Flat max-health growth shared by every path, reaching +48 at rank 7. */
    public static double maxHealthFlatBonus(int level) {
        return clampLevel(level) * 48.0D / 7.0D;
    }

    public static double attackDamageBonus(int level) {
        return clampLevel(level) * 1.2D;
    }

    public static double armorBonus(int level) {
        return clampLevel(level) * 1.2D;
    }

    public static double armorPercentBonus(int level) {
        return clampLevel(level) * 1.2D / 100.0D;
    }

    public static double armorToughnessBonus(int level) {
        return clampLevel(level) * 3.6D / 7.0D;
    }

    public static double armorToughnessPercentBonus(int level) {
        return clampLevel(level) * 3.6D / 700.0D;
    }

    public static Component describeRank(int level) {
        int rank = clampLevel(level);
        if (rank == 0) {
            return Component.translatable("ui.starrail_sim.path.shared_growth_unaligned");
        }
        return Component.translatable("ui.starrail_sim.path.shared_growth_row", rank,
                format(regenerationFraction(rank) * 100.0D) + "%",
                format(maxHealthFlatBonus(rank)),
                format(maxHealthBonus(rank) * 100.0D) + "%",
                format(attackDamageBonus(rank)),
                format(armorBonus(rank)),
                format(armorPercentBonus(rank) * 100.0D) + "%",
                format(armorToughnessBonus(rank)),
                format(armorToughnessPercentBonus(rank) * 100.0D) + "%");
    }

    public static Component describeAllRanks() {
        MutableComponent result = Component.empty();
        for (int rank = 1; rank <= 7; rank++) {
            if (rank > 1) {
                result.append(Component.literal("\n"));
            }
            result.append(describeRank(rank));
        }
        return result;
    }

    private static String format(double value) {
        DecimalFormat format = new DecimalFormat("0.##",
                DecimalFormatSymbols.getInstance(Locale.ROOT));
        return format.format(value);
    }
}
