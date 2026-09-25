package com.starrail.sim.client;

import com.starrail.sim.StarRailSimMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Queued, centered notifications rendered above the player's HUD status bars. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class StarRailNotifications {
    private static final int MAX_QUEUED = 4;
    private static final int MAX_COMBAT_NOTICES = 5;
    private static final int MAX_VISIBLE_LINES = 6;
    private static final int DISPLAY_TICKS = 16;
    private static final int HORIZONTAL_PADDING = 10;
    private static final int VERTICAL_PADDING = 5;
    private static final int MAX_TEXT_WIDTH = 420;
    private static final ArrayDeque<QueuedNotification> QUEUE = new ArrayDeque<>();
    private static final LinkedHashMap<String, CombatNotice> COMBAT_NOTICES =
            new LinkedHashMap<>();
    private static QueuedNotification current;
    private static int remainingTicks;
    private static int combatRemainingTicks;

    private StarRailNotifications() {
    }

    public static void add(Component message) {
        if (message == null || message.getString().isBlank()) {
            return;
        }
        if (current != null && !current.combat()
                && current.message().getString().equals(message.getString())) {
            remainingTicks = DISPLAY_TICKS;
            current = new QueuedNotification(message, false, "", 1);
            return;
        }
        QueuedNotification notification = new QueuedNotification(message, false, "", 1);
        if (containsMessage(notification)) {
            return;
        }
        enqueue(notification, false);
        advanceIfNeeded();
    }

    /** Shows one short batch of distinct combat feedback instead of a long backlog. */
    public static void addCombat(Component message, String mergeKey, boolean priority) {
        if (message == null || message.getString().isBlank()) {
            return;
        }

        CombatNotice existing = COMBAT_NOTICES.get(mergeKey);
        if (existing != null) {
            COMBAT_NOTICES.put(mergeKey, new CombatNotice(message, mergeKey,
                    existing.priority() || priority, existing.occurrences() + 1));
        } else {
            if (COMBAT_NOTICES.size() >= MAX_COMBAT_NOTICES) {
                String removableKey = COMBAT_NOTICES.entrySet().stream()
                        .filter(entry -> !entry.getValue().priority())
                        .map(java.util.Map.Entry::getKey)
                        .findFirst().orElse(null);
                if (removableKey == null) {
                    if (!priority) {
                        return;
                    }
                    removableKey = COMBAT_NOTICES.keySet().iterator().next();
                }
                COMBAT_NOTICES.remove(removableKey);
            }
            COMBAT_NOTICES.put(mergeKey,
                    new CombatNotice(message, mergeKey, priority, 1));
        }
        combatRemainingTicks = DISPLAY_TICKS;
    }

    @SubscribeEvent
    public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) {
            return;
        }
        if (!COMBAT_NOTICES.isEmpty()) {
            if (--combatRemainingTicks <= 0) {
                COMBAT_NOTICES.clear();
                combatRemainingTicks = 0;
                advanceIfNeeded();
            }
            return;
        }
        if (current == null) {
            return;
        }
        if (--remainingTicks <= 0) {
            current = null;
            advanceIfNeeded();
        }
    }

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()
                || (COMBAT_NOTICES.isEmpty() && current == null)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();
        int maxWidth = Math.min(MAX_TEXT_WIDTH, screenWidth - 32);
        List<FormattedCharSequence> lines = new ArrayList<>();
        boolean truncated = false;
        if (!COMBAT_NOTICES.isEmpty()) {
            List<CombatNotice> notices = new ArrayList<>(COMBAT_NOTICES.values());
            notices.sort((first, second) ->
                    Boolean.compare(second.priority(), first.priority()));
            for (CombatNotice notice : notices) {
                List<FormattedCharSequence> noticeLines =
                        font.split(notice.visibleMessage(), maxWidth);
                for (FormattedCharSequence line : noticeLines) {
                    if (lines.size() >= MAX_VISIBLE_LINES) {
                        truncated = true;
                        break;
                    }
                    lines.add(line);
                }
                if (truncated) {
                    break;
                }
            }
            if (truncated && !lines.isEmpty()) {
                lines.set(lines.size() - 1,
                        font.split(Component.literal("…"), maxWidth).get(0));
            }
        } else {
            lines.addAll(font.split(current.message(), maxWidth));
        }
        if (lines.isEmpty()) {
            return;
        }

        int contentWidth = 0;
        for (FormattedCharSequence line : lines) {
            contentWidth = Math.max(contentWidth, font.width(line));
        }
        int boxWidth = contentWidth + HORIZONTAL_PADDING * 2;
        int lineHeight = font.lineHeight + 1;
        int boxHeight = lines.size() * lineHeight + VERTICAL_PADDING * 2;
        int left = (screenWidth - boxWidth) / 2;
        int top = screenHeight - 78 - boxHeight;
        GuiGraphics graphics = event.getGuiGraphics();
        graphics.fill(left, top, left + boxWidth, top + boxHeight, 0xB5101420);
        graphics.fill(left, top, left + boxWidth, top + 1, 0xFF55D6C2);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(font, lines.get(i),
                    (screenWidth - font.width(lines.get(i))) / 2,
                    top + VERTICAL_PADDING + i * lineHeight,
                    0xFFFFFFFF, true);
        }
    }

    private static void advanceIfNeeded() {
        if (COMBAT_NOTICES.isEmpty() && current == null && !QUEUE.isEmpty()) {
            current = QUEUE.removeFirst();
            remainingTicks = DISPLAY_TICKS;
        }
    }

    private static boolean containsMessage(QueuedNotification notification) {
        if (current != null && !current.combat()
                && current.message().getString().equals(notification.message().getString())) {
            return true;
        }
        for (QueuedNotification queued : QUEUE) {
            if (queued.message().getString().equals(notification.message().getString())) {
                return true;
            }
        }
        return false;
    }

    private static void enqueue(QueuedNotification notification, boolean first) {
        if (QUEUE.size() >= MAX_QUEUED) {
            if (first) {
                QUEUE.removeLast();
            } else {
                QUEUE.removeFirst();
            }
        }
        if (first) {
            QUEUE.addFirst(notification);
        } else {
            QUEUE.addLast(notification);
        }
    }

    private record QueuedNotification(Component message, boolean combat,
                                      String mergeKey, int occurrences) {
        private Component visibleMessage() {
            if (occurrences <= 1) {
                return message;
            }
            return message.copy().append(Component.literal(" ×" + occurrences)
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private record CombatNotice(Component message, String mergeKey,
                                boolean priority, int occurrences) {
        private Component visibleMessage() {
            if (occurrences <= 1) {
                return message;
            }
            return message.copy().append(Component.literal(" ×" + occurrences)
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
