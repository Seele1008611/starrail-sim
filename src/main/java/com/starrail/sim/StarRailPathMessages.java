package com.starrail.sim;

/**
 * 模组代码说明：统一处理命途提示文本和消息发送方式。
 */

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

/** Sends combat notifications with a consistent path identity color. */
public final class StarRailPathMessages {
    private static final Set<String> TRIAL_NOTIFICATIONS = Set.of(
            "message.starrail_sim.preservation_started",
            "message.starrail_sim.abundance_started",
            "message.starrail_sim.destruction_started",
            "message.starrail_sim.erudition_started",
            "message.starrail_sim.nihility_started",
            "message.starrail_sim.harmony_started",
            "message.starrail_sim.remembrance_started",
            "message.starrail_sim.elation_started",
            "message.starrail_sim.hunt_started",
            "message.starrail_sim.preservation_confirmed",
            "message.starrail_sim.abundance_confirmed",
            "message.starrail_sim.destruction_confirmed",
            "message.starrail_sim.erudition_confirmed",
            "message.starrail_sim.nihility_confirmed",
            "message.starrail_sim.harmony_confirmed",
            "message.starrail_sim.remembrance_confirmed",
            "message.starrail_sim.elation_confirmed",
            "message.starrail_sim.hunt_confirmed",
            "message.starrail_sim.rank_trial_started",
            "message.starrail_sim.rank_trial_auto_complete",
            "message.starrail_sim.rank_trial_timeout",
            "message.starrail_sim.trial_progress",
            "message.starrail_sim.trial_timeout",
            "message.starrail_sim.trial_not_complete",
            "message.starrail_sim.trial_already_active",
            "message.starrail_sim.path_locked",
            "message.starrail_sim.path_already_chosen",
            "message.starrail_sim.path_unavailable",
            "message.starrail_sim.path_seek_overworld_only",
            "message.starrail_sim.path_seek_pending",
            "message.starrail_sim.path_seek_no_spaced_site",
            "message.starrail_sim.path_rank_up");

    private static final Set<String> SKILL_NOTIFICATIONS = Set.of(
            "message.starrail_sim.abundance_emergency",
            "message.starrail_sim.abundance_lifeblood",
            "message.starrail_sim.abundance_manna",
            "message.starrail_sim.abundance_rejuvenation",
            "message.starrail_sim.destruction_empowered_attack",
            "message.starrail_sim.destruction_blood_battle",
            "message.starrail_sim.destruction_burning",
            "message.starrail_sim.destruction_desperation",
            "message.starrail_sim.erudition_analysis_consumed",
            "message.starrail_sim.erudition_echo",
            "message.starrail_sim.erudition_final",
            "message.starrail_sim.erudition_analysis",
            "message.starrail_sim.elation_dice",
            "message.starrail_sim.elation_combo_burst",
            "message.starrail_sim.elation_afterglow",
            "message.starrail_sim.elation_grand_burst",
            "message.starrail_sim.hunt_reverse_pursuit",
            "message.starrail_sim.hunt_pursuit_strike",
            "message.starrail_sim.hunt_intent_extended",
            "message.starrail_sim.preservation_countershock",
            "message.starrail_sim.preservation_fortress",
            "message.starrail_sim.preservation_barrier",
            "message.starrail_sim.harmony_absorption",
            "message.starrail_sim.harmony_resonance_started",
            "message.starrail_sim.harmony_afterglow",
            "message.starrail_sim.harmony_concert_echo",
            "message.starrail_sim.harmony_resonance_extended",
            "message.starrail_sim.destruction_wrath",
            "message.starrail_sim.hunt_intent",
            "message.starrail_sim.preservation_guard",
            "message.starrail_sim.nihility_diffusion",
            "message.starrail_sim.nihility_final",
            "message.starrail_sim.nihility_deep_erosion",
            "message.starrail_sim.nihility_pain_echo",
            "message.starrail_sim.remembrance_marked",
            "message.starrail_sim.remembrance_echo",
            "message.starrail_sim.remembrance_afterglow",
            "message.starrail_sim.remembrance_eternal_echo");

    private StarRailPathMessages() {
    }

    public static void send(ServerPlayer player, StarRailPath path, Component message) {
        if (!hasAllowedKey(message, SKILL_NOTIFICATIONS)) {
            return;
        }
        StarRailNetwork.sendCombatNotification(player,
                message.copy().withStyle(color(path)), true);
    }

    public static void sendQueued(ServerPlayer player, StarRailPath path,
                                  Component message) {
        if (!hasAllowedKey(message, TRIAL_NOTIFICATIONS)) {
            return;
        }
        StarRailNetwork.sendNotification(player, message.copy().withStyle(color(path)));
    }

    private static boolean hasAllowedKey(Component message, Set<String> allowedKeys) {
        return message.getContents() instanceof TranslatableContents translated
                && allowedKeys.contains(translated.getKey());
    }

    private static ChatFormatting color(StarRailPath path) {
        return switch (path == null ? StarRailPath.NONE : path) {
            case PRESERVATION -> ChatFormatting.YELLOW;
            case DESTRUCTION -> ChatFormatting.GOLD;
            case HUNT -> ChatFormatting.BLUE;
            case ERUDITION -> ChatFormatting.DARK_PURPLE;
            case HARMONY -> ChatFormatting.LIGHT_PURPLE;
            case NIHILITY -> ChatFormatting.DARK_GRAY;
            case ABUNDANCE -> ChatFormatting.GREEN;
            case REMEMBRANCE -> ChatFormatting.AQUA;
            case ELATION -> ChatFormatting.RED;
            default -> ChatFormatting.WHITE;
        };
    }
}
