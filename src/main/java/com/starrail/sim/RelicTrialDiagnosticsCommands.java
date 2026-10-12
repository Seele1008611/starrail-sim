package com.starrail.sim;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Read-only administrator diagnostics for the relic trial's loaded mob pool. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RelicTrialDiagnosticsCommands {
    private static final int PAGE_SIZE = 12;

    private RelicTrialDiagnosticsCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("starrail")
                .then(Commands.literal("arena")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("pool")
                                .executes(context -> showPool(context, "all", 1))
                                .then(poolFilter("all"))
                                .then(poolFilter("normal"))
                                .then(poolFilter("ranged")))
                        .then(Commands.literal("inspect")
                                .then(Commands.argument("entity", ResourceLocationArgument.id())
                                        .executes(RelicTrialDiagnosticsCommands::inspect)))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> poolFilter(String filter) {
        return Commands.literal(filter)
                .executes(context -> showPool(context, filter, 1))
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .executes(context -> showPool(context, filter,
                                IntegerArgumentType.getInteger(context, "page"))));
    }

    private static int showPool(CommandContext<CommandSourceStack> context,
                                String filter, int page) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        List<EntityType<?>> candidates = RelicTrialBattleService.eligibleTypes(level);
        List<EntityType<?>> filtered = new ArrayList<>();
        int normalCount = 0;
        int rangedCount = 0;
        for (EntityType<?> type : candidates) {
            boolean ranged = RelicTrialBattleService.isRangedType(level, type);
            if (ranged) rangedCount++;
            else normalCount++;
            if (filter.equals("all") || filter.equals("ranged") == ranged) filtered.add(type);
        }

        int pages = Math.max(1, (filtered.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        if (page > pages) {
            source.sendFailure(net.minecraft.network.chat.Component.translatable(
                    "message.starrail_sim.arena.diag.page_invalid", pages));
            return 0;
        }
        String filterKey = "message.starrail_sim.arena.diag.filter." + filter;
        final int totalNormal = normalCount;
        final int totalRanged = rangedCount;
        source.sendSuccess(() -> net.minecraft.network.chat.Component.translatable(
                "message.starrail_sim.arena.diag.pool_header",
                net.minecraft.network.chat.Component.translatable(filterKey), filtered.size(),
                totalNormal, totalRanged, page, pages), false);
        int start = (page - 1) * PAGE_SIZE;
        for (int index = start; index < Math.min(start + PAGE_SIZE, filtered.size()); index++) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(filtered.get(index));
            if (id != null) {
                source.sendSuccess(() -> net.minecraft.network.chat.Component.translatable(
                        "message.starrail_sim.arena.diag.pool_entry", id.toString()), false);
            }
        }
        return filtered.size();
    }

    private static int inspect(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ResourceLocation id = ResourceLocationArgument.getId(context, "entity");
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(id);
        if (type == null) {
            source.sendFailure(net.minecraft.network.chat.Component.translatable(
                    "message.starrail_sim.arena.diag.unknown", id.toString()));
            return 0;
        }

        RelicTrialBattleService.TypeInspection result = RelicTrialBattleService.inspectType(
                source.getLevel(), type);
        source.sendSuccess(() -> net.minecraft.network.chat.Component.translatable(
                "message.starrail_sim.arena.diag.inspect_header", id.toString()), false);
        source.sendSuccess(() -> net.minecraft.network.chat.Component.translatable(
                "message.starrail_sim.arena.diag.inspect_result",
                net.minecraft.network.chat.Component.translatable(result.eligible()
                        ? "message.starrail_sim.arena.diag.included"
                        : "message.starrail_sim.arena.diag.excluded"),
                net.minecraft.network.chat.Component.translatable(
                        "message.starrail_sim.arena.diag.reason." + result.reason())), false);
        source.sendSuccess(() -> net.minecraft.network.chat.Component.translatable(
                "message.starrail_sim.arena.diag.category",
                net.minecraft.network.chat.Component.translatable(
                        "message.starrail_sim.arena.diag.category."
                                + result.category().name().toLowerCase(Locale.ROOT))), false);
        source.sendSuccess(() -> net.minecraft.network.chat.Component.translatable(
                "message.starrail_sim.arena.diag.tags", yesNo(result.forgeBoss()),
                yesNo(result.allowedTag()), yesNo(result.excludedTag()), yesNo(result.ranged())), false);
        source.sendSuccess(() -> net.minecraft.network.chat.Component.translatable(
                "message.starrail_sim.arena.diag.limits",
                format(StarRailStatusConfig.ARENA_MAX_MOB_HEALTH.get()),
                format(StarRailStatusConfig.ARENA_MAX_MOB_WIDTH.get()),
                format(StarRailStatusConfig.ARENA_MAX_MOB_HEIGHT.get())), false);
        if (result.maxHealth() >= 0.0D) {
            String health = String.format(Locale.ROOT, "%.1f", result.maxHealth());
            String width = String.format(Locale.ROOT, "%.2f", result.width());
            String height = String.format(Locale.ROOT, "%.2f", result.height());
            source.sendSuccess(() -> net.minecraft.network.chat.Component.translatable(
                    "message.starrail_sim.arena.diag.dimensions", health, width, height), false);
        }
        return 1;
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static net.minecraft.network.chat.Component yesNo(boolean value) {
        return net.minecraft.network.chat.Component.translatable(value
                ? "message.starrail_sim.arena.diag.yes"
                : "message.starrail_sim.arena.diag.no");
    }
}
