package com.starrail.sim;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;

/** One-time, evidence-based advancement backfill for players upgrading existing saves. */
public final class StarRailAdvancementMigrationService {
    private static final String MIGRATION_KEY = "starrail_sim_advancement_migration_version";
    private static final int MIGRATION_VERSION = 2;
    private static final TagKey<Item> LIGHT_CONES = ItemTags.create(
            new ResourceLocation("curios", "light_cone"));

    private static final List<String> PATH_REGIONS = List.of(
            "starlight_grove", "sunmeadow", "star_tide_ocean", "clear_sand_beach", "starvein_cavern",
            "star_scree_highlands", "stilltide_marsh");

    private StarRailAdvancementMigrationService() { }

    public static void migrate(ServerPlayer player, IStarRailPathData data) {
        if (player.getPersistentData().getInt(MIGRATION_KEY) >= MIGRATION_VERSION) return;

        boolean pathEvidence = player.level().dimension().equals(StarRailDimensionIds.THE_PATH)
                || !data.getDiscoveredPathRegions().isEmpty()
                || !data.getDiscoveredPathRuins().isEmpty()
                || !data.getDiscoveredPathBeacons().isEmpty()
                || data.isPathArenaDiscovered()
                || data.getPathArenaVictories() > 0;

        for (String region : PATH_REGIONS) {
            if (data.getDiscoveredPathRegions().contains(region)) {
                award(player, "exploration/" + region);
                pathEvidence = true;
            }
        }
        if (data.getDiscoveredPathRuins().contains("ordinary")) {
            award(player, "exploration/ordinary_ruin");
            pathEvidence = true;
        }
        if (data.isPathArenaDiscovered()) {
            award(player, "exploration/arena_discovered");
            pathEvidence = true;
        }
        if (data.getPathArenaVictories() > 0) {
            award(player, "exploration/arena_victory");
            pathEvidence = true;
        }

        boolean confirmedPath = false;
        for (StarRailPath path : StarRailPath.values()) {
            if (!path.isRealPath()) continue;

            StarRailPathRank rank = data.getPathRank(path);
            boolean selected = data.getCurrentPath() == path;
            if (!selected && rank == StarRailPathRank.UNALIGNED) continue;

            confirmedPath = true;
            award(player, "path/first");
            award(player, "path/" + path.getId());
            if (rank.getLevel() >= StarRailPathRank.GLIMPSE.getLevel()) {
                award(player, "path/glimpse");
            }
            if (rank.getLevel() >= StarRailPathRank.PRACTICE.getLevel()) {
                award(player, "path/practice");
            }
            if (rank.getLevel() >= StarRailPathRank.PATH_PINNACLE.getLevel()) {
                award(player, "path/pinnacle");
            }
        }
        pathEvidence |= confirmedPath;
        if (pathEvidence) award(player, "journey/the_path");

        awardIfOwned(player, PathMineralContent.STARCOAL.get(), "materials/starcoal");
        awardIfOwned(player, PathMineralContent.SUNIRON_INGOT.get(), "materials/suniron");
        awardIfOwned(player, PathMineralContent.STARMARROW.get(), "materials/starmarrow");
        awardIfOwned(player, PathMineralContent.STELLARJADE_INGOT.get(), "materials/stellarjade");
        awardIfOwned(player, PathMineralContent.ANCIENTDREAM_INGOT.get(), "materials/ancientdream");
        awardIfOwned(player, PathCaveContent.STARVEIN_CRYSTAL_CLUSTER.get().asItem(),
                "cavern/crystal_cluster");
        awardIfOwned(player, PathEcologyContent.FORESTBEAN_POD.get(), "ecology/forestbean");
        awardIfOwned(player, PathEcologyContent.ROASTED_STAR_TIDE_ALGAE.get(), "ecology/roasted_algae");
        awardIfOwned(player, PathEcologyContent.CLEAR_SAND_BELLFLOWER.get().asItem(),
                "ecology/clear_sand_bellflower");

        if (hasLightCone(player)) {
            award(player, "light_cone/collected");
            if (hasEquippedLightCone(player)) award(player, "light_cone/equipped");
        }

        // The marker is written only after all evidence has been processed. Awards are idempotent,
        // so a save interrupted before this write can safely retry the backfill on the next login.
        player.getPersistentData().putInt(MIGRATION_KEY, MIGRATION_VERSION);
    }

    private static void awardIfOwned(ServerPlayer player, Item item, String advancement) {
        if (owns(player, item)) award(player, advancement);
    }

    private static boolean owns(ServerPlayer player, Item item) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) return true;
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (stack.is(item)) return true;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.is(item)) return true;
        }
        return false;
    }

    private static boolean hasLightCone(ServerPlayer player) {
        if (ownsTagged(player.getInventory().items)
                || ownsTagged(player.getInventory().armor)
                || ownsTagged(player.getInventory().offhand)) {
            return true;
        }
        return hasCurioLightCone(player);
    }

    private static boolean hasEquippedLightCone(ServerPlayer player) {
        return hasCurioLightCone(player);
    }

    private static boolean ownsTagged(Iterable<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (stack.is(LIGHT_CONES)) return true;
        }
        return false;
    }

    /** Checks the Curios light-cone handler; normal inventory stacks are checked separately. */
    private static boolean hasCurioLightCone(ServerPlayer player) {
        boolean[] found = {false};
        CuriosApi.getCuriosInventory(player).ifPresent(inventory ->
                inventory.getStacksHandler("light_cone").ifPresent(handler ->
                        inspectCurioSlots(handler.getStacks(), found)));
        return found[0];
    }

    private static void inspectCurioSlots(IItemHandler handler, boolean[] found) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.is(LIGHT_CONES)) {
                found[0] = true;
                return;
            }
        }
    }

    private static void award(ServerPlayer player, String path) {
        StarRailAdvancementService.awardExistingCriterion(player, path);
    }
}
