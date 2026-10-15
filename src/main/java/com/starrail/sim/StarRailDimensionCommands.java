package com.starrail.sim;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Player return command and operator navigation commands for The Path. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarRailDimensionCommands {
    private StarRailDimensionCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        var locate = Commands.literal("locate")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("relay").executes(StarRailDimensionCommands::locateRelay))
                .then(Commands.literal("region")
                        .then(Commands.literal("starrail_wilderness")
                                .executes(context -> locateBiome(context, "starrail_wilderness")))
                        .then(Commands.literal("echo_terminal")
                                .executes(context -> locateBiome(context, "echo_terminal"))))
                .then(Commands.literal("ordinary_ruin").executes(context ->
                        StarRailRuinLocateService.start(context.getSource(),
                                StarRailDimensionIds.THE_PATH.location())))
                .then(Commands.literal("relic_trial_arena").executes(context -> runInPath(
                        context, "locate structure starrail_sim:relic_trial_arena")))
                .then(Commands.literal("path_ruin").executes(StarRailDimensionCommands::locatePathRuin));

        event.getDispatcher().register(Commands.literal("starrail")
                .then(Commands.literal("dimension")
                        .then(Commands.literal("enter")
                                .requires(source -> source.hasPermission(2))
                                .executes(StarRailDimensionCommands::enter))
                        .then(Commands.literal("return")
                                .executes(StarRailDimensionCommands::returnToOrigin))
                        .then(locate)));
    }

    private static int enter(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("该命令只能由玩家执行。"));
            return 0;
        }
        return StarRailDimensionService.enter(player) ? 1 : 0;
    }

    private static int returnToOrigin(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("该命令只能由玩家执行。"));
            return 0;
        }
        return StarRailDimensionService.returnToOrigin(player) ? 1 : 0;
    }

    private static int locateRelay(CommandContext<CommandSourceStack> context) {
        BlockPos target = new BlockPos(0, 129, 0);
        String command = "/execute in starrail_sim:the_path run tp @s 0.5 129 0.5";
        context.getSource().sendSuccess(() -> Component.translatable(
                        "message.starrail_sim.dimension.relay_location",
                        target.getX(), target.getY(), target.getZ())
                .withStyle(style -> style.withClickEvent(
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))), false);
        return 1;
    }

    private static int locateBiome(CommandContext<CommandSourceStack> context, String biome) {
        return runInPath(context, "locate biome starrail_sim:" + biome);
    }

    private static int locatePathRuin(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("寻迹遗迹定位需要由玩家执行。"));
            return 0;
        }
        IStarRailPathData data = player.getCapability(StarRailPathCapability.PATH_DATA)
                .resolve().orElse(null);
        if (data == null || !data.getSoughtPath().isRealPath()) {
            source.sendFailure(Component.translatable("message.starrail_sim.dimension.no_path_ruin"));
            return 0;
        }
        String dimension = data.getSoughtDimension().toString();
        String command = "/execute in " + dimension + " run tp @s "
                + data.getSoughtX() + " " + data.getSoughtY() + " " + data.getSoughtZ();
        source.sendSuccess(() -> Component.translatable(
                        "message.starrail_sim.dimension.path_ruin_location",
                        data.getSoughtPath().getDisplayName(), dimension,
                        data.getSoughtX(), data.getSoughtY(), data.getSoughtZ())
                .withStyle(style -> style.withClickEvent(
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))), false);
        return 1;
    }

    private static int runInPath(CommandContext<CommandSourceStack> context, String command) {
        return context.getSource().getServer().getCommands().performPrefixedCommand(
                context.getSource(), "execute in starrail_sim:the_path run " + command);
    }
}
