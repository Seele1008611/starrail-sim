package com.starrail.sim;

/**
 * 模组代码说明：注册并处理命途相关的服务端指令入口。
 */

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

/** Temporary commands for testing the path framework before the selection GUI. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarRailPathCommands {
    private StarRailPathCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("starrail")
                .then(Commands.literal("path")
                        .then(Commands.literal("status")
                                .executes(StarRailPathCommands::status))
                        .then(Commands.literal("start")
                                .then(Commands.argument("path", StringArgumentType.word())
                                        .suggests(StarRailPathCommands::suggestPaths)
                                        .executes(StarRailPathCommands::start)))
                        .then(Commands.literal("confirm")
                                .executes(StarRailPathCommands::confirm))
                        .then(Commands.literal("abandon")
                                .executes(StarRailPathCommands::abandon))
                        .then(Commands.literal("debug")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("unlock")
                                        .executes(StarRailPathCommands::debugUnlock))
                                 .then(Commands.literal("complete_hunt")
                                         .executes(StarRailPathCommands::debugCompleteHunt))
                                 .then(Commands.literal("set_rank")
                                         .then(Commands.argument("level",
                                                         IntegerArgumentType.integer(1, 7))
                                                 .executes(StarRailPathCommands::debugSetRank)))
                                 .then(Commands.literal("start_rank_trial")
                                         .executes(StarRailPathCommands::debugStartRankTrial)))));
    }

    private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestPaths(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (StarRailPath path : StarRailPath.values()) {
            if (path.isRealPath()) {
                builder.suggest(path.getId());
            }
        }
        return builder.buildFuture();
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getPlayer(context);
        if (player == null) {
            return 0;
        }
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            context.getSource().sendSuccess(() -> Component.literal(
                    "命途状态：入口=" + (data.isPathUnlocked() ? "已解锁" : "未解锁")
                            + "，当前命途=" + data.getCurrentPath().getDisplayName()
                            + "，试炼=" + data.getTrialPath().getDisplayName()
                            + "，击杀=" + data.getObjective1Progress() + "/5、"
                            + "连续狩猎=" + data.getObjective2Progress() + "/2"), false);
        });
        return 1;
    }

    private static int start(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getPlayer(context);
        if (player == null) {
            return 0;
        }
        StarRailPath path = StarRailPath.byId(StringArgumentType.getString(context, "path"));
        if (!path.isRealPath()) {
            context.getSource().sendFailure(Component.literal("未知命途。"));
            return 0;
        }

        context.getSource().sendFailure(Component.translatable(
                "message.starrail_sim.trial_token_required"));
        return 0;
    }

    private static int confirm(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getPlayer(context);
        if (player == null) {
            return 0;
        }
        StarRailPathService.confirmTrial(player);
        return 1;
    }

    private static int abandon(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getPlayer(context);
        if (player == null) {
            return 0;
        }
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            data.resetTrial();
            context.getSource().sendSuccess(() -> Component.literal("已放弃当前命途试炼。"), false);
        });
        return 1;
    }

    private static int debugUnlock(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getPlayer(context);
        if (player == null) {
            return 0;
        }
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            data.setPathUnlocked(true);
            context.getSource().sendSuccess(() -> Component.literal("已解锁命途入口。"), false);
        });
        return 1;
    }

    private static int debugCompleteHunt(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getPlayer(context);
        if (player == null) {
            return 0;
        }
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            if (data.getTrialPath() == StarRailPath.HUNT) {
                data.setObjective1Progress(5);
                data.setObjective2Progress(2);
                context.getSource().sendSuccess(() -> Component.literal(
                        "已将巡猎试炼设置为完成。"), false);
            } else {
                context.getSource().sendFailure(Component.literal("请先开始巡猎试炼。"));
            }
        });
        return 1;
    }

    private static int debugSetRank(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getPlayer(context);
        if (player == null) {
            return 0;
        }
        int level = IntegerArgumentType.getInteger(context, "level");
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            if (!data.getCurrentPath().isRealPath()) {
                context.getSource().sendFailure(Component.literal("当前没有已踏上的命途。"));
                return;
            }
            data.resetTrial();
            data.setPathRank(data.getCurrentPath(), StarRailPathRank.fromLevel(level));
            data.setPracticeProgress(data.getCurrentPath(), 0);
            StarRailPathEffects.refresh(player, data.getCurrentPath());
            StarRailPathService.sync(player);
            context.getSource().sendSuccess(() -> Component.literal(
                    "已将当前命途等级设置为 " + level + "。"), false);
        });
        return 1;
    }

    private static int debugStartRankTrial(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getPlayer(context);
        if (player == null) {
            return 0;
        }
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            if (!data.getCurrentPath().isRealPath()) {
                context.getSource().sendFailure(Component.literal("当前没有已踏上的命途。"));
                return;
            }
            if (data.getCurrentPathRank() == StarRailPathRank.PATH_PINNACLE) {
                context.getSource().sendFailure(Component.literal("当前已经是命途极境。"));
                return;
            }
            StarRailPathProgress.beginRankTrial(player, data, data.getCurrentPath(),
                    data.getCurrentPathRank());
        });
        return 1;
    }

    private static ServerPlayer getPlayer(CommandContext<CommandSourceStack> context) {
        try {
            return context.getSource().getPlayerOrException();
        } catch (Exception ignored) {
            context.getSource().sendFailure(Component.literal("该命令只能由玩家执行。"));
            return null;
        }
    }
}
