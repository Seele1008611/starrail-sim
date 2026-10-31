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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

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
                                .then(Commands.literal("set")
                                        .then(Commands.argument("path", StringArgumentType.word())
                                                .suggests(StarRailPathCommands::suggestPaths)
                                                .executes(StarRailPathCommands::debugSetPath)))
                                .then(Commands.literal("prepare")
                                        .then(Commands.argument("path", StringArgumentType.word())
                                                .suggests(StarRailPathCommands::suggestPaths)
                                                .executes(StarRailPathCommands::debugPreparePath)))
                                .then(Commands.literal("clear")
                                        .executes(StarRailPathCommands::debugClearPath))
                                .then(Commands.literal("unlock")
                                        .executes(StarRailPathCommands::debugUnlock))
                                 .then(Commands.literal("complete_hunt")
                                         .executes(StarRailPathCommands::debugCompleteHunt))
                                 .then(Commands.literal("set_rank")
                                         .then(Commands.argument("level",
                                                         IntegerArgumentType.integer(1, 7))
                                                 .executes(StarRailPathCommands::debugSetRank)))
                                 .then(Commands.literal("inspect")
                                         .executes(StarRailPathCommands::debugInspect))
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

    /** Directly selects a path for administrator testing; the trial/token flow stays unchanged. */
    private static int debugSetPath(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getPlayer(context);
        if (player == null) {
            return 0;
        }
        StarRailPath path = StarRailPath.byId(StringArgumentType.getString(context, "path"));
        if (!path.isRealPath()) {
            context.getSource().sendFailure(Component.literal("未知命途。"));
            return 0;
        }
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            data.setPathUnlocked(true);
            data.resetTrial();
            data.clearSoughtRuin();
            if (data.getPathRank(path) == StarRailPathRank.UNALIGNED) {
                data.setPathRank(path, StarRailPathRank.PATHFARING);
            }
            StarRailPathTemporaryState.clear(player);
            data.setCurrentPath(path);
            StarRailPathEffects.refresh(player, path);
            StarRailPathService.sync(player);
            context.getSource().sendSuccess(() -> Component.literal(
                    "测试命令已切换至" + path.getDisplayName() + "命途。原有阶位和行迹记录已保留。"), false);
        });
        return 1;
    }

    /** Prepares a path for full trace-tree testing, including rank and only the missing materials. */
    private static int debugPreparePath(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getPlayer(context);
        if (player == null) {
            return 0;
        }
        StarRailPath path = StarRailPath.byId(StringArgumentType.getString(context, "path"));
        if (!path.isRealPath()) {
            context.getSource().sendFailure(Component.literal("未知命途。"));
            return 0;
        }
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            data.setPathUnlocked(true);
            data.resetTrial();
            data.clearSoughtRuin();
            StarRailPathTemporaryState.clear(player);
            data.setCurrentPath(path);
            data.setPathRank(path, StarRailPathRank.PATH_PINNACLE);
            data.setPracticeProgress(path, 0);

            int allNodes = (1 << StarRailTraces.nodes(path).length) - 1;
            int missingMaterials = Math.max(0,
                    StarRailTraces.cost(path, allNodes) - StarRailTraces.cost(path, data.getTraceMask(path)));
            if (missingMaterials > 0) {
                ItemStack materials = new ItemStack(StarRailTraceMaterials.get(path), missingMaterials);
                if (!player.getInventory().add(materials)) {
                    player.drop(materials, false);
                }
                player.getInventory().setChanged();
                player.containerMenu.broadcastChanges();
            }

            StarRailPathEffects.refresh(player, path);
            StarRailPathService.sync(player);
            context.getSource().sendSuccess(() -> Component.literal(
                    "已准备" + path.getDisplayName() + "行迹测试：资深行者阶段、已有行迹保留，并补足解锁剩余节点所需材料。"), false);
        });
        return 1;
    }

    /** Returns to the no-path state without clearing any path rank or trace progress. */
    private static int debugClearPath(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getPlayer(context);
        if (player == null) {
            return 0;
        }
        StarRailPathService.resetPath(player);
        context.getSource().sendSuccess(() -> Component.literal(
                "测试状态已清除，当前回到未踏上命途；历史阶位和行迹记录保留。"), false);
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
                context.getSource().sendFailure(Component.literal("已达到当前开放的最高命途阶位。"));
                return;
            }
            StarRailPathProgress.beginRankTrial(player, data, data.getCurrentPath(),
                    data.getCurrentPathRank());
        });
        return 1;
    }

    /** Read-only snapshot for quickly checking the active path and trace bonuses in game. */
    private static int debugInspect(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getPlayer(context);
        if (player == null) {
            return 0;
        }
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            StarRailPath path = data.getCurrentPath();
            if (!path.isRealPath()) {
                context.getSource().sendFailure(Component.literal("当前没有已踏上的命途。"));
                return;
            }

            int rank = data.getCurrentPathRank().getLevel();
            int traceMask = data.getTraceMask(path);
            int traceCount = Integer.bitCount(traceMask);
            int pinnacleCount = data.getPinnaclePracticeCount(path);
            double pinnacleBonus = StarRailPathProgress.pinnacleAttackBonus(pinnacleCount);
            context.getSource().sendSuccess(() -> Component.literal(
                    "命途实时审查（只读）：" + path.getDisplayName() + "·"
                            + rank + "级；行迹 " + traceCount + "/9。"), false);
            context.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                    "巅峰修习：%d/%d 次，当前累计 +%s%%（每次 +12%%；%s）。",
                    pinnacleCount, StarRailPathProgress.MAX_PINNACLE_PRACTICE_COUNT,
                    formatAuditNumber(pinnacleBonus * 100.0D),
                    rank >= StarRailPathRank.PATH_PINNACLE.getLevel() ? "已生效" : "达到7级后生效")), false);

            StringJoiner traceBonuses = new StringJoiner("；");
            Set<Integer> reportedStats = new HashSet<>();
            for (int stat : StarRailTraces.stats(path)) {
                if (reportedStats.add(stat)) {
                    double bonus = StarRailTraces.bonus(path, traceMask, stat) * 100.0D;
                    traceBonuses.add(StarRailTraces.statName(stat) + " +"
                            + formatAuditNumber(bonus) + "%");
                }
            }
            context.getSource().sendSuccess(() -> Component.literal(
                    "已解锁行迹的静态加成：" + traceBonuses), false);
            context.getSource().sendSuccess(() -> Component.literal("实际面板（基础值 → 当前值）："), false);

            sendAttributeAudit(context, player, "攻击力", Attributes.ATTACK_DAMAGE, false);
            sendAttributeAudit(context, player, "最大生命", Attributes.MAX_HEALTH, false);
            sendAttributeAudit(context, player, "护甲值", Attributes.ARMOR, false);
            sendAttributeAudit(context, player, "护甲韧性", Attributes.ARMOR_TOUGHNESS, false);
            sendAttributeAudit(context, player, "暴击率", StarRailAttributes.CRIT_RATE.get(), true);
            sendAttributeAudit(context, player, "暴击伤害", StarRailAttributes.CRIT_DAMAGE.get(), true);
            sendAttributeAudit(context, player, "效果命中", StarRailAttributes.EFFECT_HIT_RATE.get(), true);
            sendAttributeAudit(context, player, "击破特攻", StarRailAttributes.BREAK_EFFECT.get(), true);
            sendAttributeAudit(context, player, "治疗效果", StarRailAttributes.HEALING_EFFECT.get(), true);
        });
        return 1;
    }

    private static void sendAttributeAudit(CommandContext<CommandSourceStack> context,
                                           ServerPlayer player, String label,
                                           Attribute attribute, boolean percentage) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        double scale = percentage ? 100.0D : 1.0D;
        String suffix = percentage ? "%" : "";
        context.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "%s：%s%s → %s%s", label,
                formatAuditNumber(instance.getBaseValue() * scale), suffix,
                formatAuditNumber(instance.getValue() * scale), suffix)), false);

        StringJoiner modifiers = new StringJoiner("；");
        instance.getModifiers().stream()
                .filter(modifier -> modifier.getName().startsWith("starrail_sim.path.")
                        || modifier.getName().endsWith(" traces"))
                .forEach(modifier -> modifiers.add(modifier.getName() + "="
                        + formatAuditNumber(modifier.getAmount()) + " ("
                        + modifier.getOperation().name() + ")"));
        if (modifiers.length() > 0) {
            context.getSource().sendSuccess(() -> Component.literal(
                    "  命途/行迹修饰项：" + modifiers), false);
        }
    }

    private static String formatAuditNumber(double value) {
        return String.format(Locale.ROOT, "%.3f", value).replaceAll("\\.?0+$", "");
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
