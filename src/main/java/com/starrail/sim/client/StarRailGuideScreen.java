package com.starrail.sim.client;

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
public final class StarRailGuideScreen extends Screen {
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
    private Button rankPreviousButton;
    private Button rankNextButton;
    private Button pathPreviousButton;
    private Button pathNextButton;
    private Button helpPreviousButton;
    private Button helpNextButton;
    private Page page = Page.CURRENT;
    private StarRailPath selectedPath = StarRailPath.HUNT;
    private int rankPage;
    private int pathDetailPage;
    private int helpPage;
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

    public StarRailGuideScreen() {
        super(Component.translatable("screen.starrail_sim.guide"));
    }

    @Override
    protected void init() {
        int panelWidth = StarRailUiStyle.panelWidth(width);
        int panelHeight = StarRailUiStyle.panelHeight(height);
        panelLeft = StarRailUiStyle.panelLeft(width);
        panelTop = StarRailUiStyle.panelTop(height);
        panelRight = panelLeft + panelWidth;
        panelBottom = panelTop + panelHeight;
        compactLayout = StarRailUiStyle.isCompact(panelWidth, panelHeight);
        contentLeft = panelLeft + (compactLayout ? 12 + 116 + 16 : 20 + 132 + 24);
        contentRight = panelRight - (compactLayout ? 12 : 20);
        contentTop = panelTop + (compactLayout ? 92 : 70);
        pathGridTop = contentTop + 34;
        int pathColumns = 2;
        int pathColumnGap = compactLayout ? 6 : 8;
        int availableContentWidth = contentRight - contentLeft;
        int pathSelectorWidth = Math.min(compactLayout ? 190 : 230,
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
        if (current.isRealPath()) {
            selectedPath = current;
        }

        int tabGap = 6;
        int tabWidth = Math.max(86, (panelWidth - 40 - tabGap * 3) / 4);
        String[] tabKeys = {
                "screen.starrail_sim.guide_current",
                "screen.starrail_sim.guide_paths",
                "screen.starrail_sim.guide_ranks",
                "screen.starrail_sim.guide_help"
        };
        for (int index = 0; index < tabButtons.length; index++) {
            int tabIndex = index;
            tabButtons[index] = addRenderableWidget(StarRailUiStyle.button(
                            Component.translatable(tabKeys[index]),
                            ignored -> setPage(Page.values()[tabIndex]),
                            panelLeft + 20 + index * (tabWidth + tabGap),
                            panelTop + 28, tabWidth, 22));
        }

        for (int index = 0; index < PATHS.length; index++) {
            StarRailPath path = PATHS[index];
            int column = index % pathColumns;
            int row = index / pathColumns;
            Button button = addRenderableWidget(StarRailUiStyle.button(
                            Component.literal(path.getDisplayName()),
                            ignored -> selectedPath = path,
                            contentLeft + column * (pathButtonWidth + pathColumnGap),
                            pathGridTop + row * 30, pathButtonWidth, 22));
            pathButtons.put(path, button);
        }

        int rankPageY = panelBottom - 30;
        rankPreviousButton = addRenderableWidget(StarRailUiStyle.button(
                Component.translatable("screen.starrail_sim.guide_previous_page"),
                ignored -> setRankPage(rankPage - 1), panelLeft + 20, rankPageY, 100, 20));
        rankNextButton = addRenderableWidget(StarRailUiStyle.button(
                Component.translatable("screen.starrail_sim.guide_next_page"),
                ignored -> setRankPage(rankPage + 1), panelLeft + 126, rankPageY, 100, 20));
        pathPreviousButton = addRenderableWidget(StarRailUiStyle.button(
                Component.translatable("screen.starrail_sim.guide_previous_page"),
                ignored -> setPathDetailPage(pathDetailPage - 1), panelLeft + 20, rankPageY, 100, 20));
        pathNextButton = addRenderableWidget(StarRailUiStyle.button(
                Component.translatable("screen.starrail_sim.guide_next_page"),
                ignored -> setPathDetailPage(pathDetailPage + 1), panelLeft + 126, rankPageY, 100, 20));
        helpPreviousButton = addRenderableWidget(StarRailUiStyle.button(
                Component.translatable("screen.starrail_sim.guide_previous_page"),
                ignored -> setHelpPage(helpPage - 1), panelLeft + 20, rankPageY, 100, 20));
        helpNextButton = addRenderableWidget(StarRailUiStyle.button(
                Component.translatable("screen.starrail_sim.guide_next_page"),
                ignored -> setHelpPage(helpPage + 1), panelLeft + 126, rankPageY, 100, 20));

        StarRailNetwork.CHANNEL.sendToServer(
                new PathActionPacket(PathActionPacket.Action.REQUEST_STATE));
        updateVisibility();
    }

    private void setPage(Page nextPage) {
        page = nextPage;
        updateVisibility();
    }

    private void setRankPage(int nextPage) {
        rankPage = Math.max(0, Math.min(1, nextPage));
        updateVisibility();
    }

    private void setPathDetailPage(int nextPage) {
        pathDetailPage = Math.max(0, Math.min(7, nextPage));
        updateVisibility();
    }

    private void setHelpPage(int nextPage) {
        helpPage = Math.max(0, Math.min(3, nextPage));
        updateVisibility();
    }

    private void updateVisibility() {
        boolean pathsVisible = page == Page.PATHS;
        for (Button button : pathButtons.values()) {
            button.visible = pathsVisible;
            button.active = pathsVisible;
        }
        for (int index = 0; index < tabButtons.length; index++) {
            tabButtons[index].active = Page.values()[index] != page;
        }
        boolean ranksVisible = page == Page.RANKS;
        if (rankPreviousButton != null) {
            rankPreviousButton.visible = ranksVisible;
            rankPreviousButton.active = ranksVisible && rankPage > 0;
        }
        if (rankNextButton != null) {
            rankNextButton.visible = ranksVisible;
            rankNextButton.active = ranksVisible && rankPage < 1;
        }
        if (pathPreviousButton != null) {
            pathPreviousButton.visible = pathsVisible;
            pathPreviousButton.active = pathsVisible && pathDetailPage > 0;
        }
        if (pathNextButton != null) {
            pathNextButton.visible = pathsVisible;
            pathNextButton.active = pathsVisible && pathDetailPage < 7;
        }
        boolean helpVisible = page == Page.HELP;
        if (helpPreviousButton != null) {
            helpPreviousButton.visible = helpVisible;
            helpPreviousButton.active = helpVisible && helpPage > 0;
        }
        if (helpNextButton != null) {
            helpNextButton.visible = helpVisible;
            helpNextButton.active = helpVisible && helpPage < 3;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        StarRailUiStyle.renderBackdrop(graphics, width, height);
        StarRailUiStyle.renderPanel(graphics, panelLeft, panelTop, panelRight, panelBottom);
        graphics.drawCenteredString(font, title, width / 2, panelTop + 10,
                StarRailUiStyle.VALUE_COLOR);

        switch (page) {
            case CURRENT -> renderCurrent(graphics);
            case PATHS -> renderPaths(graphics);
            case RANKS -> renderRanks(graphics);
            case HELP -> renderHelp(graphics);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderCurrent(GuiGraphics graphics) {
        int x = contentLeft;
        int y = contentTop;
        int textWidth = contentRight - contentLeft;
        StarRailPath path = StarRailPathClientState.getCurrentPath();
        if (!path.isRealPath()) {
            drawLine(graphics, Component.translatable(
                    StarRailPathClientState.isUnlocked()
                            ? "guide.starrail_sim.current.unaligned"
                            : "guide.starrail_sim.current.locked"), x, y);
            drawWrapped(graphics, Component.translatable("guide.starrail_sim.current.hint"),
                    x, y + 30, textWidth, StarRailUiStyle.MUTED_COLOR);
            return;
        }

        drawLine(graphics, Component.translatable("guide.starrail_sim.current.path",
                path.getDisplayName()), x, y);
        drawLine(graphics, Component.translatable("guide.starrail_sim.current.rank",
                Component.translatable(StarRailPathClientState.getCurrentPathRank()
                        .getTranslationKey())), x, y + 24);
        if (StarRailPathClientState.getCurrentPathRank() == StarRailPathRank.PATH_PINNACLE) {
            drawLine(graphics, Component.translatable(
                    "guide.starrail_sim.current.pinnacle_practice",
                    StarRailPathClientState.getPracticeProgress(),
                    StarRailPathProgress.PINNACLE_PRACTICE_TARGET,
                    StarRailPathClientState.getPinnaclePracticeCount(),
                    StarRailPathClientState.getPinnaclePracticeMax()), x, y + 48);
        } else {
            drawLine(graphics, Component.translatable("guide.starrail_sim.current.practice",
                    path.getDisplayName(), StarRailPathClientState.getPracticeProgress(),
                    StarRailPathClientState.getPracticeTarget()), x, y + 48);
        }

        int detailY = y + 82;
        int nextY = detailY;
        drawLine(graphics, Component.translatable("guide.starrail_sim.current.core"), x, nextY);
        nextY += 20;
        nextY = drawWrapped(graphics, Component.translatable(coreKey(path)), x + 8, nextY,
                textWidth - 16, StarRailUiStyle.MUTED_COLOR) + 8;
        drawLine(graphics, Component.translatable("guide.starrail_sim.current.effect"), x,
                nextY);
        nextY += 20;
        nextY = drawWrapped(graphics, Component.translatable(effectKey(path)), x + 8, nextY,
                textWidth - 16, StarRailUiStyle.MUTED_COLOR) + 8;

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

    private void renderPaths(GuiGraphics graphics) {
        int y = contentTop;
        drawLine(graphics, Component.translatable("guide.starrail_sim.paths.select"),
                contentLeft, y);
        drawLine(graphics, Component.translatable("guide.starrail_sim.path.title",
                selectedPath.getDisplayName()), detailX, y);
        int detailY = y + 28;
        int detailWidth = contentRight - detailX - 4;
        switch (pathDetailPage) {
            case 0 -> renderPathSection(graphics, "guide.starrail_sim.path.core",
                    coreKey(selectedPath), detailX, detailY, detailWidth,
                    StarRailUiStyle.MUTED_COLOR);
            case 1 -> renderPathSection(graphics, "guide.starrail_sim.path.goals",
                    goalKey(selectedPath), detailX, detailY, detailWidth,
                    StarRailUiStyle.VALUE_COLOR);
            case 2 -> renderPathSection(graphics, "guide.starrail_sim.path.practice",
                    practiceKey(selectedPath), detailX, detailY, detailWidth,
                    StarRailUiStyle.MUTED_COLOR);
            case 3 -> renderPathMechanics(graphics, detailX, detailY, detailWidth, false);
            case 4 -> renderPathMechanics(graphics, detailX, detailY, detailWidth, true);
            case 5 -> renderPathSection(graphics, "guide.starrail_sim.path.effects",
                    effectKey(selectedPath), detailX, detailY, detailWidth,
                    StarRailUiStyle.VALUE_COLOR);
            case 6 -> renderPathSection(graphics, "guide.starrail_sim.path.upgrade",
                    "guide.starrail_sim.path.rank_rule", detailX, detailY, detailWidth,
                    StarRailUiStyle.MUTED_COLOR);
            case 7 -> renderPathSection(graphics, "guide.starrail_sim.path.tips",
                    tipsKey(selectedPath), detailX, detailY, detailWidth,
                    StarRailUiStyle.VALUE_COLOR);
            default -> { }
        }
        drawLine(graphics, Component.translatable("screen.starrail_sim.guide_page",
                pathDetailPage + 1, 8), contentRight - 80, panelBottom - 30);
    }

    private void renderPathMechanics(GuiGraphics graphics, int x, int y, int width,
                                     boolean advanced) {
        String titleKey = advanced
                ? "guide.starrail_sim.path.mechanics_advanced"
                : "guide.starrail_sim.path.mechanics_intro";
        drawLine(graphics, Component.translatable(titleKey), x, y);
        String text = Component.translatable(mechanicsKey(selectedPath)).getString()
                .replace("\\n", "\n");
        String[] paragraphs = text.split("\n", -1);
        int splitAt = Math.max(1, (paragraphs.length + 1) / 2);
        int start = advanced ? splitAt : 0;
        int end = advanced ? paragraphs.length : splitAt;
        StringBuilder pageText = new StringBuilder();
        for (int index = start; index < end; index++) {
            if (pageText.length() > 0) {
                pageText.append('\n');
            }
            pageText.append(paragraphs[index]);
        }
        graphics.enableScissor(x, y + 24, x + width, panelBottom - 42);
        drawGuideBody(graphics, Component.literal(pageText.toString()), x + 8, y + 28,
                width - 16, StarRailUiStyle.MUTED_COLOR);
        graphics.disableScissor();
    }

    private void renderPathSection(GuiGraphics graphics, String titleKey, String bodyKey,
            int x, int y, int width, int color) {
        drawLine(graphics, Component.translatable(titleKey), x, y);
        graphics.enableScissor(x, y + 24, x + width, panelBottom - 42);
        drawGuideBody(graphics, Component.translatable(bodyKey), x + 8, y + 28,
                width - 16, color);
        graphics.disableScissor();
    }

    /** Keeps intentional guide paragraphs visually distinct while wrapping them. */
    private int drawGuideBody(GuiGraphics graphics, Component text, int x, int y,
                              int maxWidth, int color) {
        int lineY = y;
        String bodyText = text.getString().replace("\\n", "\n");
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
        int x = contentLeft;
        int y = contentTop;
        int textWidth = contentRight - contentLeft;
        drawWrapped(graphics, Component.translatable("guide.starrail_sim.ranks.intro"),
                x, y, textWidth, StarRailUiStyle.MUTED_COLOR);
        int rowY = y + 34;
        if (rankPage == 0) {
            StarRailPath currentPath = StarRailPathClientState.getCurrentPath();
            if (currentPath.isRealPath()) {
                drawLine(graphics, Component.translatable(
                        "guide.starrail_sim.ranks.current_path", currentPath.getDisplayName()),
                        x, rowY);
                rowY += 20;
                rowY = drawWrapped(graphics, Component.translatable(
                                "guide.starrail_sim.ranks.practice_method",
                                Component.translatable(practiceKey(currentPath))),
                        x + 8, rowY, textWidth - 16,
                        StarRailUiStyle.VALUE_COLOR) + 10;
            } else {
                rowY = drawWrapped(graphics, Component.translatable(
                                "guide.starrail_sim.ranks.unaligned"),
                        x, rowY, textWidth,
                        StarRailUiStyle.MUTED_COLOR) + 10;
            }
        }
        StarRailPathRank currentRank = StarRailPathClientState.getCurrentPathRank();
        int start = rankPage == 0 ? 0 : 4;
        int end = rankPage == 0 ? 4 : RANKS.length;
        for (int index = start; index < end; index++) {
            StarRailPathRank rank = RANKS[index];
            int target = practiceTarget(rank);
            Component targetText = rank == StarRailPathRank.PATH_PINNACLE
                    ? Component.translatable("guide.starrail_sim.ranks.pinnacle",
                    StarRailPathProgress.PINNACLE_PRACTICE_TARGET,
                    StarRailPathProgress.MAX_PINNACLE_PRACTICE_COUNT)
                    : target > 0
                    ? Component.translatable("guide.starrail_sim.ranks.practice", target)
                    : Component.translatable("guide.starrail_sim.ranks.max");
            int color = rank == currentRank
                    ? StarRailUiStyle.VALUE_COLOR : StarRailUiStyle.MUTED_COLOR;
            graphics.drawString(font, Component.literal(rank.getLevel() + ". ")
                    .append(Component.translatable(rank.getTranslationKey()))
                    .append("  ").append(targetText), x, rowY, color);
            rowY += 25;
        }
        if (rankPage == 1) {
            drawWrapped(graphics, Component.translatable("guide.starrail_sim.ranks.trials"),
                    x, rowY + 4, textWidth,
                    StarRailUiStyle.MUTED_COLOR);
        }
        drawLine(graphics, Component.translatable("screen.starrail_sim.guide_page",
                rankPage + 1, 2), contentRight - 80, panelBottom - 30);
    }

    private void renderHelp(GuiGraphics graphics) {
        int x = contentLeft;
        int y = contentTop;
        int textWidth = contentRight - contentLeft;
        String pageTitle = switch (helpPage) {
            case 1 -> "guide.starrail_sim.help.combat_title";
            case 2 -> "guide.starrail_sim.help.combat_title";
            case 3 -> "guide.starrail_sim.help.interface_title";
            default -> "guide.starrail_sim.help.title";
        };
        String[] keys = switch (helpPage) {
            case 1 -> new String[] {
                    "guide.starrail_sim.help.damage",
                    "guide.starrail_sim.help.toughness",
                    "guide.starrail_sim.help.break"
            };
            case 2 -> new String[] {
                    "guide.starrail_sim.help.effect_hit",
                    "guide.starrail_sim.help.effect_resistance",
                    "guide.starrail_sim.help.knockback_resistance"
            };
            case 3 -> new String[] {
                    "guide.starrail_sim.help.attributes",
                    "guide.starrail_sim.help.practice_detail",
                    "guide.starrail_sim.help.path_switch",
                    "guide.starrail_sim.help.notifications",
                    "guide.starrail_sim.help.light_cone"
            };
            default -> new String[] {
                    "guide.starrail_sim.help.open_current",
                    "guide.starrail_sim.help.path",
                    "guide.starrail_sim.help.practice",
                    "guide.starrail_sim.help.trial",
                    "guide.starrail_sim.help.progress",
                    "guide.starrail_sim.help.rollback"
            };
        };
        drawLine(graphics, Component.translatable(pageTitle), x, y);
        int lineY = y + 32;
        for (String key : keys) {
            lineY = drawWrapped(graphics, Component.translatable(key), x, lineY,
                    textWidth, StarRailUiStyle.MUTED_COLOR);
            lineY += 10;
        }
        drawLine(graphics, Component.translatable("screen.starrail_sim.guide_page",
                helpPage + 1, 4), contentRight - 80, panelBottom - 30);
    }

    private void drawLine(GuiGraphics graphics, Component text, int x, int y) {
        graphics.drawString(font, text, x, y, StarRailUiStyle.VALUE_COLOR);
    }

    private int drawWrapped(GuiGraphics graphics, Component text, int x, int y,
                            int maxWidth, int color) {
        int lineY = y;
        for (FormattedCharSequence line : font.split(text, Math.max(80, maxWidth))) {
            graphics.drawString(font, line, x, lineY, color);
            lineY += 12;
        }
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
