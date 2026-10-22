package com.starrail.sim;

import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Awards mod advancements after server-side gameplay conditions have succeeded. */
public final class StarRailAdvancementService {
    private static final String SERVER_CONFIRMED_CRITERION = "server_confirmed";

    private StarRailAdvancementService() {
    }

    public static void award(ServerPlayer player, String advancementPath) {
        awardCriterion(player, advancementPath, SERVER_CONFIRMED_CRITERION);
    }

    /** Awards the sole criterion for a known, evidence-backed legacy migration. */
    public static void awardExistingCriterion(ServerPlayer player, String advancementPath) {
        if (player == null || player.getServer() == null) return;

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                StarRailSimMod.MOD_ID, advancementPath);
        Advancement advancement = player.getServer().getAdvancements().getAdvancement(id);
        if (advancement == null || advancement.getCriteria().size() != 1) return;

        String criterion = advancement.getCriteria().keySet().iterator().next();
        player.getAdvancements().award(advancement, criterion);
    }

    private static void awardCriterion(ServerPlayer player, String advancementPath, String criterion) {
        if (player == null || player.getServer() == null) return;

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                StarRailSimMod.MOD_ID, advancementPath);
        Advancement advancement = player.getServer().getAdvancements().getAdvancement(id);
        if (advancement != null) {
            player.getAdvancements().award(advancement, criterion);
        }
    }
}
