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
                        .then(Commands.literal("sunmeadow")
                                .executes(context -> locateBiome(context, "sunmeadow")))
                        .then(Commands.literal("glimmerwood")
                                .executes(context -> locateBiome(context, "glimmerwood"))))
                .then(Commands.literal("ore")
                        .then(Commands.literal("starcoal").executes(context -> locateOre(context, "starcoal")))
                        .then(Commands.literal("dawncopper").executes(context -> locateOre(context, "dawncopper")))
                        .then(Commands.literal("suniron").executes(context -> locateOre(context, "suniron")))
                        .then(Commands.literal("dawn_gold").executes(context -> locateOre(context, "dawn_gold")))
                        .then(Commands.literal("resonance").executes(context -> locateOre(context, "resonance")))
                        .then(Commands.literal("memory").executes(context -> locateOre(context, "memory")))
                        .then(Commands.literal("starmarrow").executes(context -> locateOre(context, "starmarrow")))
                        .then(Commands.literal("stellarjade").executes(context -> locateOre(context, "stellarjade")))
                        .then(Commands.literal("ancientdream").executes(context -> locateOre(context, "ancientdream")))
                        .then(Commands.literal("cancel").executes(context ->
                                StarRailPathOreLocateService.cancel(context.getSource()))))
                .then(Commands.literal("ordinary_ruin").executes(context ->
                        locatePathStructure(context, false)))
                .then(Commands.literal("relic_trial_arena").executes(context ->
                        locatePathStructure(context, true)))
                .then(Commands.literal("cancel").executes(context ->
                        StarRailRuinLocateService.cancel(context.getSource())))
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

    private static int locateOre(CommandContext<CommandSourceStack> context, String ore) {
        return StarRailPathOreLocateService.start(context.getSource(), ore);
    }

    private static int locatePathStructure(CommandContext<CommandSourceStack> context,
                                           boolean arena) {
        CommandSourceStack source = context.getSource();
        if (source.getPlayer() == null
                || !source.getLevel().dimension().equals(StarRailDimensionIds.THE_PATH)) {
            source.sendFailure(Component.literal(
                    "请在命途遗境维度中由玩家执行此结构定位命令。"));
            return 0;
        }
        return arena ? StarRailRuinLocateService.startArena(source, null)
                : StarRailRuinLocateService.start(source);
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
