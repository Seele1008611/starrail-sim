package com.starrail.sim.client;

/**
 * 模组代码说明：客户端界面类，构建对应页面并处理玩家的界面交互。
 */

import com.starrail.sim.PathActionPacket;
import com.starrail.sim.StarRailNetwork;
import com.starrail.sim.StarRailPath;
import com.starrail.sim.StarRailPathProgress;
import com.starrail.sim.StarRailPathRank;
import com.starrail.sim.StarRailPathRules;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;

import java.util.EnumMap;
import java.util.Map;

/** Read-only in-game guide for paths, practice, and rank breakthroughs. */
public final class StarRailGuideScreen extends StarRailStyledScreen {
    private enum Page {
        CURRENT,
        PATHS,
        RANKS,
        HELP
    }

    private static final StarRailPath[] PATHS = {
            StarRailPath.PRESERVATION, StarRailPath.DESTRUCTION, StarRailPath.HUNT,
            StarRailPath.ERUDITION, StarRailPath.HARMONY, StarRailPath.NIHILITY,
            StarRailPath.ABUNDANCE, StarRailPath.REMEMBRANCE, StarRailPath.ELATION
    };
    private static final StarRailPathRank[] RANKS = {
            StarRailPathRank.PATHFARING, StarRailPathRank.GLIMPSE,
            StarRailPathRank.RESONANCE, StarRailPathRank.PRACTICE,
            StarRailPathRank.DEEP_PRACTICE, StarRailPathRank.HIGH_PATHSTRIDER,
            StarRailPathRank.PATH_PINNACLE
    };

    private final Map<StarRailPath, Button> pathButtons = new EnumMap<>(StarRailPath.class);
    private final Button[] navigationButtons = new Button[
            StarRailUiStyle.CHARACTER_NAVIGATION_KEYS.length];
    private final Button[] tabButtons = new Button[4];
    private Page page = Page.CURRENT;
    private StarRailPath selectedPath = StarRailPath.HUNT;
    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;
    private int contentLeft;
    private int contentRight;
    private int contentTop;
    private int pathGridTop;
    private int detailX;
    private boolean compactLayout;
    private final StarRailSmoothScroll bodyScroll = new StarRailSmoothScroll();
    private int bodyBottom;
    private boolean initialPathSelection = true;
    private Button openCurrentPath;
    private long sectionChangedAt;

    public StarRailGuideScreen() {
        super(Component.translatable("screen.starrail_sim.guide"));
    }

    public StarRailGuideScreen(StarRailPath path) {
        this();
        selectedPath = path;
        page = Page.PATHS;
        initialPathSelection = false;
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
        contentLeft = panelLeft + (compactLayout ? 12 + 116 + 16 : 20 + 132 + 24);
        contentRight = panelRight - (compactLayout ? 12 : 20);
        contentTop = panelTop + 100;
        pathGridTop = contentTop + 34;
        int pathColumns = 2;
        int pathColumnGap = compactLayout ? 6 : 8;
        int availableContentWidth = contentRight - contentLeft;
        int pathSelectorWidth = Math.min(compactLayout ? 180 : 198,
                Math.max(150, availableContentWidth * 42 / 100));
        int pathButtonWidth = (pathSelectorWidth - pathColumnGap) / pathColumns;
        detailX = contentLeft + pathSelectorWidth + (compactLayout ? 12 : 16);
        Button[] createdNavigation = StarRailUiStyle.createCharacterNavigation(
                panelLeft, panelTop, panelWidth, panelHeight, 5, index -> {
                    if (index == 0) {
                        minecraft.setScreen(new StarRailCharacterScreen());
                    } else if (index == 1) {
                        minecraft.setScreen(new StarRailLightConeScreen());
                    } else if (index == 4) {
                        minecraft.setScreen(new StarRailPathScreen());
                    }
                });
        for (int index = 0; index < navigationButtons.length; index++) {
            navigationButtons[index] = addRenderableWidget(createdNavigation[index]);
        }

        StarRailPath current = StarRailPathClientState.getCurrentPath();
        if (initialPathSelection && current.isRealPath()) {
            selectedPath = current;
        }
        initialPathSelection = false;

        int tabGap = 6;
        int tabWidth = (contentRight - contentLeft - tabGap * 3) / 4;
        String[] tabKeys = {
                "screen.starrail_sim.guide_current",
                "screen.starrail_sim.guide_paths",
                "screen.starrail_sim.guide_ranks",
                "screen.starrail_sim.guide_help"
        };
        for (int index = 0; index < tabButtons.length; index++) {
            int tabIndex = index;
            tabButtons[index] = addRenderableWidget(StarRailUiStyle.tabButton(
                            Component.translatable(tabKeys[index]),
                            ignored -> setPage(Page.values()[tabIndex]),
                            contentLeft + index * (tabWidth + tabGap),
                            panelTop + 65, tabWidth, 24));
        }

        for (int index = 0; index < PATHS.length; index++) {
            StarRailPath path = PATHS[index];
            int column = index % pathColumns;
            int row = index / pathColumns;
            Button button = addRenderableWidget(StarRailUiStyle.pathCard(
                            path,
                            ignored -> { selectedPath = path; bodyScroll.reset(); sectionChangedAt = System.nanoTime(); },
                            contentLeft + column * (pathButtonWidth + pathColumnGap),
                            pathGridTop + row * 43, pathButtonWidth, 36));
            pathButtons.put(path, button);
        }

        openCurrentPath = addRenderableWidget(StarRailUiStyle.outlinedButton(
                Component.translatable("ui.starrail_sim.guide.open_path"),
                ignored -> minecraft.setScreen(new StarRailPathScreen()),
                contentLeft, panelBottom - 65, detailX - contentLeft - 16, 24));
        StarRailNetwork.CHANNEL.sendToServer(
                new PathActionPacket(PathActionPacket.Action.REQUEST_STATE));
        updateVisibility();
    }

    private void setPage(Page nextPage) {
        page = nextPage;
        sectionChangedAt = System.nanoTime();
        bodyScroll.reset();
        updateVisibility();
    }







    private void updateVisibility() {
        boolean pathsVisible = page == Page.PATHS;
        if (openCurrentPath != null) openCurrentPath.visible = page == Page.CURRENT;
        for (Button button : pathButtons.values()) {
            button.visible = pathsVisible;
            button.active = pathsVisible;
        }
        for (int index = 0; index < tabButtons.length; index++) {
            tabButtons[index].active = Page.values()[index] != page;
            StarRailUiStyle.setSelected(tabButtons[index], Page.values()[index] == page);
        }
        pathButtons.forEach((path, button) -> StarRailUiStyle.setSelected(button, path == selectedPath));

    }

    @Override
    protected void renderPage(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        StarRailUiStyle.renderBackdrop(graphics, width, height);
        StarRailUiStyle.renderPanel(graphics, panelLeft, panelTop, panelRight, panelBottom);
        StarRailUiStyle.renderHeader(graphics, title, panelLeft, panelTop);

        bodyScroll.advance();
        if (page == Page.CURRENT) renderCurrentCard(graphics);
        int clipLeft = page == Page.PATHS || page == Page.CURRENT ? detailX : contentLeft;
        clip(graphics, clipLeft, contentTop, contentRight, panelBottom - 42);
        graphics.pose().pushPose();
        double entering = Math.min(1, (System.nanoTime() - sectionChangedAt) / 180_000_000.0);
        graphics.pose().translate(0, -bodyScroll.position() + 5 * (1 - entering), 0);
        bodyBottom = contentTop;
        switch (page) {
            case CURRENT -> renderCurrent(graphics);
            case PATHS -> renderPaths(graphics);
            case RANKS -> renderRanks(graphics);
            case HELP -> renderHelp(graphics);
        }
        graphics.pose().popPose();
        graphics.disableScissor();
        bodyScroll.bounds(bodyBottom - (panelBottom - 42) + 16);
        renderScrollBar(graphics, bodyScroll, contentRight - 2, contentTop, panelBottom - 42,
                bodyBottom - contentTop + 16, mouseX, mouseY);
        if (page == Page.PATHS) {
            graphics.drawString(font, Component.translatable("guide.starrail_sim.paths.select"),
                    contentLeft, contentTop, StarRailUiStyle.GOLD_ACCENT);
        }
        super.renderPage(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected boolean logicalMouseScrolled(double x, double y, double amount) {
        int left = page == Page.PATHS || page == Page.CURRENT ? detailX : contentLeft;
        if (x >= left && x <= contentRight && y >= contentTop && y < panelBottom - 42) {
            bodyScroll.wheel(amount, 24);
            return true;
        }
        return super.logicalMouseScrolled(x, y, amount);
    }

    /** Fixed identity card; it is padded and never uses article-heading underlines. */
    private void renderCurrentCard(GuiGraphics graphics) {
        int x = contentLeft, right = detailX - 16;
        StarRailPath path = StarRailPathClientState.getCurrentPath();
        Component status = path.isRealPath()
                ? Component.translatable("guide.starrail_sim.current.path", path.getDisplayName())
                : Component.translatable(StarRailPathClientState.isUnlocked()
                        ? "guide.starrail_sim.current.unaligned" : "guide.starrail_sim.current.locked");
        Component rank = Component.translatable("guide.starrail_sim.current.rank",
                Component.translatable(StarRailPathClientState.getCurrentPathRank().getTranslationKey()));
        Component progress = Component.translatable("guide.starrail_sim.current.practice",
                path.getDisplayName(), StarRailPathClientState.getPracticeProgress(),
                StarRailPathClientState.getPracticeTarget());
        if (StarRailPathClientState.getCurrentPathRank() == StarRailPathRank.PATH_PINNACLE) {
            progress = Component.translatable("guide.starrail_sim.current.pinnacle_practice",
                    StarRailPathClientState.getPracticeProgress(), StarRailPathProgress.PINNACLE_PRACTICE_TARGET,
                    StarRailPathClientState.getPinnaclePracticeCount(), StarRailPathClientState.getPinnaclePracticeMax());
        }
        int textWidth = right - x - 28;
        int lines = font.split(Component.literal(StarRailUiStyle.readableText(status)), textWidth).size();
        if (path.isRealPath()) lines += font.split(rank, textWidth).size()
                + font.split(Component.literal(StarRailUiStyle.readableText(progress)), textWidth).size();
        int cardHeight = 32 + lines * 14 + (path.isRealPath() ? 38 : 0);
        graphics.fill(x, contentTop, right, contentTop + cardHeight, 0x282A3550);
        graphics.fill(x, contentTop, x + 2, contentTop + cardHeight, StarRailUiStyle.GOLD_ACCENT);
        int y = drawCardText(graphics, status, x + 14, contentTop + 14, textWidth, StarRailUiStyle.GOLD_ACCENT);
        if (path.isRealPath()) {
            y = drawCardText(graphics, rank, x + 14, y + 10, textWidth, StarRailUiStyle.VALUE_COLOR);
            y = drawCardText(graphics, progress, x + 14, y + 10, textWidth, StarRailUiStyle.MUTED_COLOR);
            int target = Math.max(1, StarRailPathClientState.getPracticeTarget());
            int filled = (int) (textWidth * Math.min(1, StarRailPathClientState.getPracticeProgress() / (double) target));
            graphics.fill(x + 14, y + 8, right - 14, y + 11, 0x40475770);
            graphics.fill(x + 14, y + 8, x + 14 + filled, y + 11, StarRailUiStyle.GOLD_ACCENT);
        }
    }

    private int drawCardText(GuiGraphics graphics, Component text, int x, int y, int width, int color) {
        for (FormattedCharSequence line : font.split(Component.literal(StarRailUiStyle.readableText(text)), width)) {
            graphics.drawString(font, line, x, y, color);
            y += 14;
        }
        return y;
    }

    private void renderCurrent(GuiGraphics graphics) {
        int x = detailX;
        int y = contentTop;
        int textWidth = contentRight - detailX - 10;
        StarRailPath path = StarRailPathClientState.getCurrentPath();
        if (!path.isRealPath()) {
            drawLine(graphics, Component.translatable("guide.starrail_sim.current.next"), x, y);
            drawGuideBody(graphics, Component.translatable("guide.starrail_sim.current.hint"),
                    x + 8, y + 25, textWidth - 8, StarRailUiStyle.MUTED_COLOR);
            return;
        }
        int detailY = contentTop;
        int nextY = detailY;
        nextY = drawArticle(graphics, Component.translatable("guide.starrail_sim.current.core"),
                Component.translatable(coreKey(path)), x, nextY, textWidth) + 12;
        nextY = drawArticle(graphics, Component.translatable("guide.starrail_sim.current.effect"),
                currentRankEffect(path), x, nextY, textWidth) + 12;

        if (StarRailPathClientState.getTrialPath().isRealPath()) {
            drawLine(graphics, Component.translatable("guide.starrail_sim.current.trial",
                    Component.translatable(StarRailPathClientState.getTrialRank()
                            .getTranslationKey())), x, nextY);
            nextY += 20;
            nextY = drawWrapped(graphics,
                    trialObjective(StarRailPathClientState.getTrialPath(), 1),
                    x + 8, nextY, textWidth - 16,
                    StarRailUiStyle.VALUE_COLOR) + 2;
            nextY = drawWrapped(graphics,
                    trialObjective(StarRailPathClientState.getTrialPath(), 2),
                    x + 8, nextY, textWidth - 16,
                    StarRailUiStyle.VALUE_COLOR) + 4;
            drawLine(graphics, Component.translatable("screen.starrail_sim.trial_time",
                    StarRailPathClientState.getSecondsRemaining()), x, nextY);
        } else {
            drawLine(graphics, Component.translatable("guide.starrail_sim.current.next"), x,
                    nextY);
            drawWrapped(graphics, Component.translatable(practiceKey(path)), x + 8,
                    nextY + 20, textWidth - 16,
                    StarRailUiStyle.MUTED_COLOR);
        }
    }

    private Component currentRankEffect(StarRailPath path) {
        String[] rows = StarRailUiStyle.readableText(Component.translatable(effectKey(path))).split("\n");
        int rank = StarRailPathClientState.getCurrentPathRank().getLevel();
        for (String row : rows) {
            if (row.startsWith(rank + "级：") || row.startsWith("R" + rank + ":")) {
                return Component.literal(rows[0] + "\n" + row);
            }
        }
        return Component.translatable(effectKey(path));
    }

    private void renderPaths(GuiGraphics graphics) {
        int y = contentTop;
        int textWidth = contentRight - detailX - 10;
        drawLine(graphics, Component.translatable("guide.starrail_sim.path.title", selectedPath.getDisplayName()), detailX, y);
        y += 28;
        String[][] sections = {
                {"guide.starrail_sim.path.core", coreKey(selectedPath)},
                {"guide.starrail_sim.path.goals", goalKey(selectedPath)},
                {"guide.starrail_sim.path.practice", practiceKey(selectedPath)},
                {"guide.starrail_sim.path.mechanics_intro", mechanicsKey(selectedPath)},
                {"guide.starrail_sim.path.effects", effectKey(selectedPath)},
                {"guide.starrail_sim.path.upgrade", "guide.starrail_sim.path.rank_rule"},
                {"guide.starrail_sim.path.tips", tipsKey(selectedPath)}
        };
        for (String[] section : sections) {
            Component heading = Component.translatable(section[0]);
            if (section[0].equals("guide.starrail_sim.path.goals")) {
                heading = Component.translatable("ui.starrail_sim.guide.initial_trial",
                        StarRailPathRules.trialTimeLimit(selectedPath, StarRailPathRank.UNALIGNED) / 20);
            }
            y = drawArticle(graphics, heading, Component.translatable(section[1]),
                    detailX, y, textWidth) + 14;
        }
    }

    private int drawArticle(GuiGraphics graphics, Component heading, Component body, int x, int y, int width) {
        String[] paragraphs = StarRailUiStyle.readableText(body).split("\n", -1);
        int bodyHeight = 0;
        for (String paragraph : paragraphs) {
            bodyHeight += paragraph.isBlank() ? 6
                    : font.split(Component.literal(paragraph), width - 24).size() * 12 + 4;
        }
        int bottom = y + 35 + bodyHeight;
        graphics.fill(x, y, x + width, bottom, 0x18263046);
        graphics.fill(x, y, x + 2, bottom, 0x505C778F);
        graphics.drawString(font, heading, x + 12, y + 10, StarRailUiStyle.GOLD_ACCENT);
        graphics.fill(x + 12, y + 25, x + width - 12, y + 26, 0x3098A8C5);
        drawGuideBody(graphics, body, x + 12, y + 35, width - 24, StarRailUiStyle.MUTED_COLOR);
        bodyBottom = Math.max(bodyBottom, bottom);
        return Math.max(bottom, bodyBottom) + 4;
    }

    /** Keeps intentional guide paragraphs visually distinct while wrapping them. */
    private int drawGuideBody(GuiGraphics graphics, Component text, int x, int y,
                              int maxWidth, int color) {
        int lineY = y;
        String bodyText = StarRailUiStyle.readableText(text);
        String[] paragraphs = bodyText.split("\n", -1);
        for (String paragraph : paragraphs) {
            if (paragraph.isBlank()) {
                lineY += 6;
                continue;
            }
            lineY = drawWrapped(graphics, Component.literal(paragraph), x, lineY,
                    maxWidth, color) + 4;
        }
        return lineY;
    }

    private void renderRanks(GuiGraphics graphics) {
        int x = contentLeft, textWidth = contentRight - contentLeft - 10;
        int y = drawGuideBody(graphics, Component.translatable("guide.starrail_sim.ranks.intro"),
                x, contentTop, textWidth, StarRailUiStyle.MUTED_COLOR) + 18;
        StarRailPath current = StarRailPathClientState.getCurrentPath();
        if (current.isRealPath()) {
            y = drawGuideBody(graphics, Component.translatable("guide.starrail_sim.ranks.practice_method",
                    Component.translatable(practiceKey(current))), x, y, textWidth, StarRailUiStyle.MUTED_COLOR) + 18;
        }
        int rankWidth = textWidth * 28 / 100;
        int targetWidth = textWidth * 23 / 100;
        int actionWidth = textWidth - rankWidth - targetWidth;
        graphics.fill(x, y - 5, x + textWidth, y + 18, 0x303F4966);
        graphics.drawString(font, Component.translatable("ui.starrail_sim.guide.rank_column"), x + 7, y,
                StarRailUiStyle.GOLD_ACCENT);
        graphics.drawString(font, Component.translatable("ui.starrail_sim.guide.target_column"), x + rankWidth + 7, y,
                StarRailUiStyle.GOLD_ACCENT);
        graphics.drawString(font, Component.translatable("ui.starrail_sim.guide.advance_column"),
                x + rankWidth + targetWidth + 7, y, StarRailUiStyle.GOLD_ACCENT);
        y += 26;
        for (StarRailPathRank rank : RANKS) {
            boolean pinnacle = rank == StarRailPathRank.PATH_PINNACLE;
            Component name = Component.literal(rank.getLevel() + " · ")
                    .append(Component.translatable(rank.getTranslationKey()));
            Component target = Component.translatable(pinnacle
                    ? "ui.starrail_sim.guide.pinnacle_target" : "ui.starrail_sim.guide.rank_target",
                    StarRailPathProgress.targetFor(rank));
            Component action = Component.translatable("ui.starrail_sim.guide." +
                    (rank.getLevel() == 1 ? "direct_advance" : pinnacle ? "pinnacle_reward" : "trial_advance"));
            int rowHeight = 10 + 12 * Math.max(font.split(name, rankWidth - 14).size(),
                    Math.max(font.split(target, targetWidth - 14).size(), font.split(action, actionWidth - 14).size()));
            graphics.fill(x, y - 4, x + textWidth, y + rowHeight - 4,
                    rank.getLevel() % 2 == 1 ? 0x222A3550 : 0x122A3550);
            drawWrapped(graphics, name, x + 7, y, rankWidth - 14, StarRailUiStyle.VALUE_COLOR);
            drawWrapped(graphics, target, x + rankWidth + 7, y, targetWidth - 14, StarRailUiStyle.CYAN_ACCENT);
            drawWrapped(graphics, action, x + rankWidth + targetWidth + 7, y, actionWidth - 14,
                    StarRailUiStyle.MUTED_COLOR);
            y += rowHeight;
        }
        drawLine(graphics, Component.translatable("ui.starrail_sim.guide.trial_heading"), x, y + 18);
        drawGuideBody(graphics, Component.translatable("guide.starrail_sim.ranks.trials"),
                x, y + 44, textWidth, StarRailUiStyle.MUTED_COLOR);
    }

    private void renderHelp(GuiGraphics graphics) {
        String[][] groups = {
                {"title", "open_current", "path", "practice", "trial", "progress", "rollback"},
                {"combat_title", "damage", "toughness", "break", "effect_hit", "effect_resistance", "knockback_resistance"},
                {"interface_title", "attributes", "practice_detail", "path_switch", "notifications", "light_cone"}
        };
        int y = contentTop, textWidth = contentRight - contentLeft - 10;
        for (String[] group : groups) {
            drawLine(graphics, Component.translatable("guide.starrail_sim.help." + group[0]), contentLeft, y);
            y += 24;
            for (int i = 1; i < group.length; i++) {
                y = drawGuideBody(graphics, Component.translatable("guide.starrail_sim.help." + group[i]),
                        contentLeft, y, textWidth, StarRailUiStyle.MUTED_COLOR) + 12;
            }
            y += 14;
        }
    }

    private void drawLine(GuiGraphics graphics, Component text, int x, int y) {
        bodyBottom = Math.max(bodyBottom, y + 12);
        graphics.drawString(font, Component.literal(StarRailUiStyle.readableText(text)), x, y,
                StarRailUiStyle.GOLD_ACCENT);
        graphics.fill(x, y + 15, contentRight - 10, y + 16, 0x30A3B1CB);
        bodyBottom = Math.max(bodyBottom, y + 20);
    }

    private int drawWrapped(GuiGraphics graphics, Component text, int x, int y,
                            int maxWidth, int color) {
        int lineY = y;
        for (FormattedCharSequence line : font.split(StarRailUiStyle.emphasizeNumbers(text), Math.max(24, maxWidth))) {
            graphics.drawString(font, line, x, lineY, color);
            lineY += 12;
        }
        bodyBottom = Math.max(bodyBottom, lineY);
        return lineY;
    }

    private static int practiceTarget(StarRailPathRank rank) {
        return switch (rank) {
            case PATHFARING -> 5;
            case GLIMPSE -> 8;
            case RESONANCE -> 12;
            case PRACTICE -> 30;
            case DEEP_PRACTICE -> 60;
            case HIGH_PATHSTRIDER -> 120;
            case PATH_PINNACLE -> StarRailPathProgress.PINNACLE_PRACTICE_TARGET;
            default -> 0;
        };
    }

    private static String coreKey(StarRailPath path) {
        return "guide.starrail_sim.path." + path.getId() + ".core";
    }

    private static String goalKey(StarRailPath path) {
        return "guide.starrail_sim.path." + path.getId() + ".goals";
    }

    private static String practiceKey(StarRailPath path) {
        return "guide.starrail_sim.path." + path.getId() + ".practice";
    }

    private static String effectKey(StarRailPath path) {
        return "guide.starrail_sim.path." + path.getId() + ".effect";
    }

    private static String tipsKey(StarRailPath path) {
        return "guide.starrail_sim.path." + path.getId() + ".tips";
    }

    private static String mechanicsKey(StarRailPath path) {
        return "guide.starrail_sim.path." + path.getId() + ".mechanics";
    }

    private static Component trialObjective(StarRailPath path, int objective) {
        String key = switch (path) {
            case PRESERVATION -> objective == 1
                    ? "screen.starrail_sim.preservation_block"
                    : "screen.starrail_sim.preservation_damage";
            case ABUNDANCE -> objective == 1
                    ? "screen.starrail_sim.abundance_healing"
                    : "screen.starrail_sim.abundance_events";
            case DESTRUCTION -> objective == 1
                    ? "screen.starrail_sim.destruction_kills"
                    : "screen.starrail_sim.destruction_damage";
            case ERUDITION -> objective == 1
                    ? "screen.starrail_sim.erudition_multi_hit"
                    : "screen.starrail_sim.erudition_multi_hit";
            case NIHILITY -> objective == 1
                    ? "screen.starrail_sim.nihility_marks"
                    : "screen.starrail_sim.nihility_kills";
            case HARMONY -> objective == 1
                    ? "screen.starrail_sim.harmony_food"
                    : "screen.starrail_sim.harmony_kills";
            case REMEMBRANCE -> objective == 1
                    ? "screen.starrail_sim.remembrance_records"
                    : "screen.starrail_sim.remembrance_echoes";
            case ELATION -> objective == 1
                    ? "screen.starrail_sim.elation_hits"
                    : "screen.starrail_sim.elation_bursts";
            default -> objective == 1
                    ? "screen.starrail_sim.hunt_kills"
                    : "screen.starrail_sim.hunt_streak";
        };
        int progress = objective == 1
                ? StarRailPathClientState.getObjective1()
                : StarRailPathClientState.getObjective2();
        int target = objective == 1
                ? StarRailPathRules.objective1Target(path, StarRailPathClientState.getTrialRank())
                : StarRailPathRules.objective2Target(path, StarRailPathClientState.getTrialRank());
        return Component.translatable(key, progress, target);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
