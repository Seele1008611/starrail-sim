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

/** First player-facing path selection and trial status screen. */
public final class StarRailPathScreen extends Screen {
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

    public StarRailPathScreen() {
        super(Component.translatable("screen.starrail_sim.paths"));
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
        selectedSeekPath = StarRailPath.NONE;
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
        pathGridTop = panelTop + (compactLayout ? 112 : 92);

        buttonWidth = Math.min(MAX_BUTTON_WIDTH,
                Math.max(100, (contentRight - contentLeft - COLUMN_GAP) / 2));
        gridLeft = contentLeft;
        pathButtons.clear();

        StarRailPath[] paths = {
                StarRailPath.PRESERVATION, StarRailPath.DESTRUCTION, StarRailPath.HUNT,
                StarRailPath.ERUDITION, StarRailPath.HARMONY, StarRailPath.NIHILITY,
                StarRailPath.ABUNDANCE, StarRailPath.REMEMBRANCE, StarRailPath.ELATION
        };
        for (int index = 0; index < paths.length; index++) {
            StarRailPath path = paths[index];
            int column = index % 2;
            int row = index / 2;
            Button button = StarRailUiStyle.button(pathLabel(path), ignored -> selectPath(path),
                            gridLeft + column * (buttonWidth + COLUMN_GAP),
                            pathGridTop + row * (BUTTON_HEIGHT + ROW_GAP),
                            buttonWidth, BUTTON_HEIGHT);
            pathButtons.put(path, button);
            addRenderableWidget(button);
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
        StarRailNetwork.CHANNEL.sendToServer(
                new PathActionPacket(PathActionPacket.Action.REQUEST_STATE));
    }

    private Component pathLabel(StarRailPath path) {
        if (StarRailPathRules.isImplemented(path)) {
            return Component.literal(path.getDisplayName()
                    + (path == selectedSeekPath ? "  [已选]" : ""));
        }
        return Component.literal(path.getDisplayName() + "（未开放）");
    }

    private void selectPath(StarRailPath path) {
        if (StarRailPathRules.isImplemented(path)) {
            selectedSeekPath = path;
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        StarRailUiStyle.renderBackdrop(graphics, width, height);
        StarRailUiStyle.renderPanel(graphics, panelLeft, panelTop, panelRight, panelBottom);
        graphics.drawCenteredString(font, title, (contentLeft + contentRight) / 2,
                panelTop + 10,
                StarRailUiStyle.VALUE_COLOR);
        drawStatus(graphics);
        updateButtonStates();
        super.render(graphics, mouseX, mouseY, partialTick);
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
        int statusY = panelTop + (compactLayout ? 84 : 45);
        int center = (contentLeft + contentRight) / 2;
        graphics.drawCenteredString(font, status, center, statusY, color);
        if (StarRailPathClientState.getSoughtPath().isRealPath()) {
            graphics.drawCenteredString(font,
                    Component.translatable("screen.starrail_sim.sought_ruin_hint"),
                    center, statusY + 13, StarRailUiStyle.MUTED_COLOR);
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
        int progressY = panelTop + (compactLayout ? 84 : 43);
        int center = (contentLeft + contentRight) / 2;
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
                            .getTranslationKey())), center, progressY,
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
                button.active = canSeek;
            }
        }

        if (confirmButton != null) {
            // 初始试炼完成时由服务端自动踏上命途；此按钮只负责确认开始寻迹。
            confirmButton.visible = true;
            confirmButton.setMessage(Component.translatable("screen.starrail_sim.confirm_seek"));
            confirmButton.active = canSeek && selectedSeekPath.isRealPath();
        }
        if (resetButton != null) {
            resetButton.active = StarRailPathClientState.isUnlocked()
                    && StarRailPathClientState.getCurrentPath().isRealPath()
                    && !StarRailPathClientState.getTrialPath().isRealPath();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
