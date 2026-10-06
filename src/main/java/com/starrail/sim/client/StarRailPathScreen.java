package com.starrail.sim.client;

/**
 * 模组代码说明：客户端界面类，构建对应页面并处理玩家的界面交互。
 */

import com.starrail.sim.PathActionPacket;
import com.starrail.sim.StarRailNetwork;
import com.starrail.sim.StarRailPath;
import com.starrail.sim.StarRailPathRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.EnumMap;
import java.util.Map;
import com.starrail.sim.StarRailPathRank;
import com.starrail.sim.StarRailPathProgress;
import net.minecraft.util.FormattedCharSequence;

/** First player-facing path selection and trial status screen. */
public final class StarRailPathScreen extends StarRailStyledScreen {
    private static final int MAX_BUTTON_WIDTH = 260;
    private static final int BUTTON_HEIGHT = 22;
    private static final int COLUMN_GAP = 18;
    private static final int ROW_GAP = 10;
    private static final int SIDE_MARGIN = 16;

    private final Map<StarRailPath, Button> pathButtons = new EnumMap<>(StarRailPath.class);
    private final Button[] navigationButtons = new Button[
            StarRailUiStyle.CHARACTER_NAVIGATION_KEYS.length];
    private StarRailPath selectedSeekPath = StarRailPath.NONE;
    private Button confirmButton;
    private Button resetButton;
    private int gridLeft;
    private int buttonWidth;
    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;
    private int contentLeft;
    private int contentRight;
    private int pathGridTop;
    private boolean compactLayout;
    private boolean growthPage = true;
    private StarRailPath browsedPath = StarRailPath.HUNT;
    private int browsedRank = 1;
    private final Button[] rankButtons = new Button[7];
    private Button growthTab;
    private Button seekTab;
    private int detailLeft;
    private final StarRailSmoothScroll bodyScroll = new StarRailSmoothScroll();
    private int bodyHeight;
    private boolean initialSelection = true;
    private long stageChangedAt;

    public StarRailPathScreen() {
        super(Component.translatable("screen.starrail_sim.paths"));
    }

    @Override
    protected void init() {
        super.init();
        int panelWidth = StarRailUiStyle.panelWidth(width);
        int panelHeight = StarRailUiStyle.panelHeight(height);
        panelLeft = StarRailUiStyle.panelLeft(width);
        panelTop = StarRailUiStyle.panelTop(height);
        panelRight = panelLeft + panelWidth;
        panelBottom = panelTop + panelHeight;
        compactLayout = StarRailUiStyle.isCompact(panelWidth, panelHeight);
        if (initialSelection) {
            if (StarRailPathClientState.getCurrentPath().isRealPath()) {
                browsedPath = StarRailPathClientState.getCurrentPath();
                browsedRank = StarRailPathClientState.getCurrentPathRank().getLevel();
            }
            initialSelection = false;
        }
        Button[] createdNavigation = StarRailUiStyle.createCharacterNavigation(
                panelLeft, panelTop, panelWidth, panelHeight, 4, index -> {
                    if (index == 0) {
                        minecraft.setScreen(new StarRailCharacterScreen());
                    } else if (index == 1) {
                        minecraft.setScreen(new StarRailLightConeScreen());
                    } else if (index == 5) {
                        minecraft.setScreen(new StarRailGuideScreen());
                    }
                });
        for (int index = 0; index < navigationButtons.length; index++) {
            navigationButtons[index] = addRenderableWidget(createdNavigation[index]);
        }
        contentLeft = panelLeft + (compactLayout ? 12 + 116 + 16 : 20 + 132 + 24);
        contentRight = panelRight - (compactLayout ? 12 : 20);
        pathGridTop = panelTop + 115;
        growthTab = addRenderableWidget(StarRailUiStyle.tabButton(
                Component.translatable("screen.starrail_sim.guide_ranks"),
                ignored -> { growthPage = true; bodyScroll.reset(); }, contentLeft, panelTop + 65, 110, 24));
        seekTab = addRenderableWidget(StarRailUiStyle.tabButton(
                Component.translatable("ui.starrail_sim.path.seek_tab"),
                ignored -> { growthPage = false; bodyScroll.reset(); }, contentLeft + 120, panelTop + 65, 110, 24));

        buttonWidth = 62;
        detailLeft = contentLeft + 204;
        gridLeft = contentLeft;
        pathButtons.clear();

        StarRailPath[] paths = {
                StarRailPath.PRESERVATION, StarRailPath.DESTRUCTION, StarRailPath.HUNT,
                StarRailPath.ERUDITION, StarRailPath.HARMONY, StarRailPath.NIHILITY,
                StarRailPath.ABUNDANCE, StarRailPath.REMEMBRANCE, StarRailPath.ELATION
        };
        for (int index = 0; index < paths.length; index++) {
            StarRailPath path = paths[index];
            int column = index % 3;
            int row = index / 3;
            Button button = StarRailUiStyle.pathCard(path, ignored -> selectPath(path),
                            gridLeft + column * (buttonWidth + 6),
                            pathGridTop + row * 64,
                            buttonWidth, 56);
            pathButtons.put(path, button);
            addRenderableWidget(button);
        }
        int rankWidth = Math.max(24, (contentRight - detailLeft - 36) / 7);
        for (int index = 0; index < rankButtons.length; index++) {
            final int level = index + 1;
            rankButtons[index] = addRenderableWidget(StarRailUiStyle.rankNode(
                    Component.literal(Integer.toString(level) + "\n")
                            .append(Component.translatable(StarRailPathRank.fromLevel(level).getTranslationKey())),
                    ignored -> { browsedRank = level; bodyScroll.reset(); stageChangedAt = System.nanoTime(); },
                    detailLeft + index * (rankWidth + 6), pathGridTop + 26, rankWidth, 42));
        }

        int actionGap = 10;
        int actionWidth = (contentRight - contentLeft - actionGap) / 2;
        int actionY = panelBottom - 30;
        int actionLeft = contentLeft;
        confirmButton = addRenderableWidget(StarRailUiStyle.button(
                        Component.translatable("screen.starrail_sim.confirm_seek"),
                        ignored -> confirmSelection(), actionLeft, actionY,
                        actionWidth, 22));
        resetButton = addRenderableWidget(StarRailUiStyle.button(
                        Component.translatable("screen.starrail_sim.reset_path"),
                        ignored -> resetPath(), actionLeft + actionWidth + actionGap,
                        actionY, actionWidth, 22));
        addRenderableWidget(StarRailUiStyle.outlinedButton(
                Component.translatable("ui.starrail_sim.path.open_guide"),
                ignored -> minecraft.setScreen(new StarRailGuideScreen(!growthPage && selectedSeekPath.isRealPath()
                        ? selectedSeekPath : browsedPath)),
                gridLeft, pathGridTop + 200, 198, 24));
        StarRailNetwork.CHANNEL.sendToServer(
                new PathActionPacket(PathActionPacket.Action.REQUEST_STATE));
    }

    private Component pathLabel(StarRailPath path) {
        if (StarRailPathRules.isImplemented(path)) {
            return Component.literal(path.getDisplayName());
        }
        return Component.literal(path.getDisplayName() + "（未开放）");
    }

    private void selectPath(StarRailPath path) {
        if (StarRailPathRules.isImplemented(path)) {
            if (growthPage) { browsedPath = path; bodyScroll.reset(); stageChangedAt = System.nanoTime(); }
            else selectedSeekPath = path;
            pathButtons.forEach((buttonPath, button) -> button.setMessage(pathLabel(buttonPath)));
        }
    }

    private void confirmSelection() {
        if (selectedSeekPath.isRealPath()) {
            StarRailNetwork.CHANNEL.sendToServer(new PathActionPacket(
                    PathActionPacket.Action.SEEK_PATH, selectedSeekPath));
        }
    }

    private void resetPath() {
        Minecraft.getInstance().setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        selectedSeekPath = StarRailPath.NONE;
                        pathButtons.forEach((path, button) -> button.setMessage(pathLabel(path)));
                        StarRailNetwork.CHANNEL.sendToServer(new PathActionPacket(
                                PathActionPacket.Action.RESET_PATH));
                    }
                    Minecraft.getInstance().setScreen(this);
                },
                Component.translatable("screen.starrail_sim.reset_path_title"),
                Component.translatable("screen.starrail_sim.reset_path_confirm")));
    }

    @Override
    protected void renderPage(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        StarRailUiStyle.renderBackdrop(graphics, width, height);
        StarRailUiStyle.renderPanel(graphics, panelLeft, panelTop, panelRight, panelBottom);
        StarRailUiStyle.renderHeader(graphics, title, panelLeft, panelTop);
        if (growthPage) renderGrowth(graphics, mouseX, mouseY);
        else drawStatus(graphics);
        updateButtonStates();
        super.renderPage(graphics, mouseX, mouseY, partialTick);
    }

    private void renderGrowth(GuiGraphics graphics, int mouseX, int mouseY) {
        bodyScroll.advance();
        StarRailPathRank rank = StarRailPathRank.fromLevel(browsedRank);
        boolean currentPath = browsedPath == StarRailPathClientState.getCurrentPath();
        boolean currentStage = currentPath && rank == StarRailPathClientState.getCurrentPathRank();
        graphics.drawString(font, Component.literal(browsedPath.getDisplayName()), detailLeft,
                pathGridTop, StarRailUiStyle.GOLD_ACCENT);
        graphics.drawString(font, Component.translatable(currentPath
                ? "ui.starrail_sim.path.current_path" : "ui.starrail_sim.path.browsing"),
                detailLeft + 60, pathGridTop, StarRailUiStyle.MUTED_COLOR);
        // Draw only the gaps between circular nodes, leaving each node's center clear.
        int reached = currentPath ? StarRailPathClientState.getCurrentPathRank().getLevel() : 0;
        for (int i = 0; i < rankButtons.length - 1; i++) {
            Button a = rankButtons[i], b = rankButtons[i + 1];
            int radius = Math.min(12, Math.max(8, a.getWidth() / 2 - 3));
            graphics.fill(a.getX() + a.getWidth() / 2 + radius + 1, a.getY() + 14,
                    b.getX() + b.getWidth() / 2 - radius, a.getY() + 15,
                    i + 2 <= reached ? 0x8055D6D2 : 0x4097A6BF);
        }
        int bodyTop = pathGridTop + 82;
        int bodyBottom = panelBottom - 22;
        int right = contentRight - 10;
        clip(graphics, detailLeft, bodyTop, contentRight, bodyBottom);
        graphics.pose().pushPose();
        double transition = Math.min(1, (System.nanoTime() - stageChangedAt) / 180_000_000.0);
        graphics.pose().translate(0, -bodyScroll.position() + 5 * (1 - transition), 0);
        int y = bodyTop;
        graphics.fill(detailLeft, y, right, y + 50, 0x282A3550);
        graphics.fill(detailLeft, y, detailLeft + 2, y + 50, StarRailUiStyle.GOLD_ACCENT);
        graphics.pose().pushPose();
        graphics.pose().translate(detailLeft + 10, y + 13, 0);
        graphics.pose().scale(2, 2, 1);
        graphics.drawString(font, String.format(java.util.Locale.ROOT, "%02d", browsedRank), 0, 0,
                StarRailUiStyle.GOLD_ACCENT, false);
        graphics.pose().popPose();
        graphics.drawString(font, Component.translatable(currentStage
                ? "ui.starrail_sim.path.current_stage" : "ui.starrail_sim.path.stage_preview"),
                detailLeft + 49, y + 9, StarRailUiStyle.MUTED_COLOR);
        graphics.drawString(font, Component.translatable(rank.getTranslationKey()),
                detailLeft + 49, y + 27, StarRailUiStyle.VALUE_COLOR);
        y += 62;
        int target = StarRailPathProgress.targetFor(rank);
        String stageKey = browsedRank == 1 ? "first_stage" : browsedRank == 7 ? "pinnacle_stage" : "next_stage";
        y = drawGrowthText(graphics, Component.translatable("ui.starrail_sim.path." + stageKey, target),
                y, StarRailUiStyle.MUTED_COLOR);
        if (browsedRank > 1 && browsedRank < 7) {
            StarRailPathRank next = StarRailPathProgress.nextRank(rank);
            y = drawGrowthText(graphics, Component.translatable("ui.starrail_sim.path.next_trial",
                    Component.translatable(next.getTranslationKey()),
                    StarRailPathRules.trialTimeLimit(browsedPath, next) / 20,
                    StarRailPathRules.objective1Target(browsedPath, next),
                    StarRailPathRules.objective2Target(browsedPath, next)), y, StarRailUiStyle.MUTED_COLOR);
        }
        if (currentStage) {
            y += 5;
            y = drawGrowthText(graphics, Component.translatable("screen.starrail_sim.path_practice_progress",
                    StarRailPathClientState.getPracticeProgress(), target), y, StarRailUiStyle.GOLD_ACCENT);
            graphics.fill(detailLeft, y, right, y + 3, 0x4044546F);
            int progressWidth = (int) ((right - detailLeft) * Math.min(1,
                    StarRailPathClientState.getPracticeProgress() / (double) Math.max(1, target)));
            graphics.fill(detailLeft, y, detailLeft + progressWidth, y + 3, StarRailUiStyle.GOLD_ACCENT);
            y += 13;
            if (browsedRank == 7) {
                y = drawGrowthText(graphics, Component.translatable("ui.starrail_sim.path.pinnacle_count",
                        StarRailPathClientState.getPinnaclePracticeCount(),
                        StarRailPathClientState.getPinnaclePracticeMax()), y, StarRailUiStyle.MUTED_COLOR);
            }
        }
        y = drawGrowthHeading(graphics, "bonuses", y + 10);
        // The guide owns the cumulative values; show only the row for the selected rank.
        String effects = StarRailUiStyle.readableText(Component.translatable(
                "guide.starrail_sim.path." + browsedPath.getId() + ".effect"));
        String[] rows = effects.split("\n");
        String[] names = new String[0];
        if (rows.length > 0) {
            java.util.regex.Matcher labels = java.util.regex.Pattern.compile("[（(]([^）)]+)[）)]").matcher(rows[0]);
            if (labels.find()) names = labels.group(1).split("/");
        }
        String prefix = browsedRank + "级";
        for (String row : rows) {
            if (!row.startsWith(prefix + "：") && !row.startsWith("R" + browsedRank + ":")) continue;
            String[] values = row.substring(Math.max(row.indexOf('：'), row.indexOf(':')) + 1).trim().split("/");
            for (int i = 0; i < values.length; i++) {
                graphics.fill(detailLeft, y - 3, right, y + 15, i % 2 == 0 ? 0x202A3550 : 0x102A3550);
                graphics.drawString(font, i < names.length ? names[i] : "", detailLeft + 7, y,
                        StarRailUiStyle.MUTED_COLOR);
                String value = "+" + values[i].trim();
                graphics.drawString(font, value, right - 7 - font.width(value), y,
                        StarRailUiStyle.CYAN_ACCENT);
                y += 23;
            }
            break;
        }
        y = drawGrowthText(graphics, Component.translatable("ui.starrail_sim.path.cumulative_note"),
                y + 2, StarRailUiStyle.MUTED_COLOR);
        y = drawGrowthHeading(graphics, "practice", y + 10);
        y = drawGrowthText(graphics, Component.translatable(
                "guide.starrail_sim.path." + browsedPath.getId() + ".practice"), y, StarRailUiStyle.MUTED_COLOR);
        y = drawGrowthHeading(graphics, "mechanics", y + 10);
        y = drawGrowthText(graphics, Component.translatable(
                "guide.starrail_sim.path." + browsedPath.getId() + ".mechanics"), y, StarRailUiStyle.MUTED_COLOR);
        graphics.pose().popPose();
        graphics.disableScissor();
        bodyHeight = y - bodyTop + 12;
        bodyScroll.bounds(bodyHeight - (bodyBottom - bodyTop));
        renderScrollBar(graphics, bodyScroll, contentRight - 2, bodyTop, bodyBottom, bodyHeight, mouseX, mouseY);
        int noteY = pathGridTop + 242;
        for (FormattedCharSequence line : font.split(Component.translatable(
                "ui.starrail_sim.path.selection_note"), 192)) {
            graphics.drawString(font, line, gridLeft, noteY, StarRailUiStyle.MUTED_COLOR);
            noteY += 14;
        }
    }

    private int drawGrowthHeading(GuiGraphics graphics, String key, int y) {
        graphics.drawString(font, Component.translatable("ui.starrail_sim.path." + key),
                detailLeft, y, StarRailUiStyle.GOLD_ACCENT);
        graphics.fill(detailLeft, y + 14, contentRight - 10, y + 15, StarRailUiStyle.DIVIDER_COLOR);
        return y + 25;
    }

    private int drawGrowthText(GuiGraphics graphics, Component text, int y, int color) {
        for (String paragraph : StarRailUiStyle.readableText(text).split("\n", -1)) {
            if (paragraph.isBlank()) { y += 7; continue; }
            for (FormattedCharSequence line : font.split(StarRailUiStyle.emphasizeNumbers(Component.literal(paragraph)), contentRight - detailLeft - 12)) {
                graphics.drawString(font, line, detailLeft, y, color);
                y += 14;
            }
            y += 5;
        }
        return y;
    }

    @Override
    protected boolean logicalMouseScrolled(double x, double y, double amount) {
        if (growthPage && x >= detailLeft && x < contentRight && y >= pathGridTop + 82 && y < panelBottom - 22) {
            bodyScroll.wheel(amount, 24);
            return true;
        }
        return super.logicalMouseScrolled(x, y, amount);
    }

    private void drawStatus(GuiGraphics graphics) {
        Component status;
        if (!StarRailPathClientState.isUnlocked()) {
            status = Component.translatable("screen.starrail_sim.path_locked");
        } else if (StarRailPathClientState.getTrialPath().isRealPath()
                && StarRailPathClientState.getTrialRank()
                != com.starrail.sim.StarRailPathRank.UNALIGNED) {
            drawTrialProgress(graphics, StarRailPathClientState.getTrialPath());
            return;
        } else if (StarRailPathClientState.getCurrentPath().isRealPath()) {
            status = Component.translatable(
                    "screen.starrail_sim.current_path_rank",
                    StarRailPathClientState.getCurrentPath().getDisplayName(),
                    Component.translatable(StarRailPathClientState.getCurrentPathRank()
                            .getTranslationKey()));
        } else if (StarRailPathClientState.getSoughtPath().isRealPath()) {
            status = Component.translatable("screen.starrail_sim.sought_ruin",
                    StarRailPathClientState.getSoughtPath().getDisplayName(),
                    StarRailPathClientState.getSoughtX(),
                    StarRailPathClientState.getSoughtY(),
                    StarRailPathClientState.getSoughtZ());
        } else if (selectedSeekPath.isRealPath()) {
            status = Component.translatable("screen.starrail_sim.path_seek_selected",
                    selectedSeekPath.getDisplayName());
        } else if (StarRailPathClientState.getTrialPath().isRealPath()) {
            drawTrialProgress(graphics, StarRailPathClientState.getTrialPath());
            return;
        } else {
            status = Component.translatable("screen.starrail_sim.choose_path");
        }
        int color = StarRailPathClientState.isUnlocked()
                ? StarRailUiStyle.VALUE_COLOR : StarRailUiStyle.MUTED_COLOR;
        int statusY = pathGridTop;
        int center = (detailLeft + contentRight) / 2;
        int lineY = statusY;
        for (FormattedCharSequence line : font.split(status, contentRight - detailLeft - 8)) {
            graphics.drawString(font, line, detailLeft, lineY, color);
            lineY += 13;
        }
        if (StarRailPathClientState.getSoughtPath().isRealPath()) {
            graphics.drawCenteredString(font,
                    Component.translatable("screen.starrail_sim.sought_ruin_hint"),
                    center, lineY + 8, StarRailUiStyle.MUTED_COLOR);
        }
        if (StarRailPathClientState.getCurrentPath().isRealPath()) {
            Component practice = StarRailPathClientState.getCurrentPathRank()
                    == com.starrail.sim.StarRailPathRank.PATH_PINNACLE
                    ? Component.translatable("screen.starrail_sim.path_pinnacle_practice",
                    StarRailPathClientState.getPracticeProgress(),
                    com.starrail.sim.StarRailPathProgress.PINNACLE_PRACTICE_TARGET,
                    StarRailPathClientState.getPinnaclePracticeCount(),
                    StarRailPathClientState.getPinnaclePracticeMax())
                    : StarRailPathClientState.getPracticeTarget() > 0
                    ? Component.translatable("screen.starrail_sim.path_practice_progress",
                    StarRailPathClientState.getPracticeProgress(),
                    StarRailPathClientState.getPracticeTarget())
                    : Component.translatable("screen.starrail_sim.path_practice_locked");
            graphics.drawCenteredString(font, practice, center, statusY + 13,
                    StarRailUiStyle.MUTED_COLOR);
        }
    }

    private void drawTrialProgress(GuiGraphics graphics, StarRailPath path) {
        String objective1Key = switch (path) {
            case PRESERVATION -> "screen.starrail_sim.preservation_block";
            case ABUNDANCE -> "screen.starrail_sim.abundance_healing";
            case DESTRUCTION -> "screen.starrail_sim.destruction_kills";
            case ERUDITION -> "screen.starrail_sim.erudition_multi_hit";
            case NIHILITY -> "screen.starrail_sim.nihility_marks";
            case HARMONY -> "screen.starrail_sim.harmony_food";
            case REMEMBRANCE -> "screen.starrail_sim.remembrance_records";
            case ELATION -> "screen.starrail_sim.elation_hits";
            default -> "screen.starrail_sim.hunt_kills";
        };
        String objective2Key = switch (path) {
            case PRESERVATION -> "screen.starrail_sim.preservation_damage";
            case ABUNDANCE -> "screen.starrail_sim.abundance_events";
            case DESTRUCTION -> "screen.starrail_sim.destruction_damage";
            case ERUDITION -> "screen.starrail_sim.erudition_multi_hit";
            case NIHILITY -> "screen.starrail_sim.nihility_kills";
            case HARMONY -> "screen.starrail_sim.harmony_kills";
            case REMEMBRANCE -> "screen.starrail_sim.remembrance_echoes";
            case ELATION -> "screen.starrail_sim.elation_bursts";
            default -> "screen.starrail_sim.hunt_streak";
        };
        int progressY = pathGridTop + 20;
        int center = (detailLeft + contentRight) / 2;
        graphics.drawCenteredString(font, Component.translatable(objective1Key,
                StarRailPathClientState.getObjective1(),
                StarRailPathRules.objective1Target(path,
                        StarRailPathClientState.getTrialRank())), center, progressY,
                StarRailUiStyle.VALUE_COLOR);
        graphics.drawCenteredString(font, Component.translatable(objective2Key,
                StarRailPathClientState.getObjective2(),
                StarRailPathRules.objective2Target(path,
                        StarRailPathClientState.getTrialRank())), center, progressY + 13,
                StarRailUiStyle.VALUE_COLOR);
        if (StarRailPathClientState.getTrialRank()
                != com.starrail.sim.StarRailPathRank.UNALIGNED) {
            graphics.drawCenteredString(font, Component.translatable(
                    "screen.starrail_sim.rank_trial_status",
                    Component.translatable(StarRailPathClientState.getTrialRank()
                    .getTranslationKey())), center, progressY - 16,
                    StarRailUiStyle.VALUE_COLOR);
        }
        graphics.drawCenteredString(font, Component.translatable(
                "screen.starrail_sim.trial_time",
                StarRailPathClientState.getSecondsRemaining()), center, progressY + 26,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void updateButtonStates() {
        boolean canStart = StarRailPathClientState.isUnlocked()
                && !StarRailPathClientState.getCurrentPath().isRealPath()
                && !StarRailPathClientState.getTrialPath().isRealPath();
        boolean canSeek = canStart && !StarRailPathClientState.getSoughtPath().isRealPath();
        for (StarRailPath path : new StarRailPath[]{StarRailPath.HUNT,
                StarRailPath.PRESERVATION, StarRailPath.ABUNDANCE,
                StarRailPath.DESTRUCTION, StarRailPath.ERUDITION,
                StarRailPath.NIHILITY, StarRailPath.HARMONY,
                StarRailPath.REMEMBRANCE, StarRailPath.ELATION}) {
            Button button = pathButtons.get(path);
            if (button != null) {
                button.active = growthPage || canSeek;
                StarRailUiStyle.setSelected(button, path == (growthPage ? browsedPath : selectedSeekPath));
            }
        }

        if (confirmButton != null) {
            // 初始试炼完成时由服务端自动踏上命途；此按钮只负责确认开始寻迹。
            confirmButton.visible = !growthPage;
            confirmButton.setMessage(Component.translatable("screen.starrail_sim.confirm_seek"));
            confirmButton.active = canSeek && selectedSeekPath.isRealPath();
        }
        if (resetButton != null) {
            resetButton.visible = !growthPage;
            resetButton.active = StarRailPathClientState.isUnlocked()
                    && StarRailPathClientState.getCurrentPath().isRealPath()
                    && !StarRailPathClientState.getTrialPath().isRealPath();
        }
        StarRailUiStyle.setSelected(growthTab, growthPage);
        StarRailUiStyle.setSelected(seekTab, !growthPage);
        for (int index = 0; index < rankButtons.length; index++) {
            rankButtons[index].visible = growthPage;
            StarRailUiStyle.setSelected(rankButtons[index], browsedRank == index + 1);
            int currentLevel = browsedPath == StarRailPathClientState.getCurrentPath()
                    ? StarRailPathClientState.getCurrentPathRank().getLevel() : 0;
            StarRailUiStyle.setRankProgress(rankButtons[index], index + 1 <= currentLevel,
                    index + 1 == currentLevel);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
