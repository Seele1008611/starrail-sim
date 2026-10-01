package com.starrail.sim.client;

/**
 * 模组代码说明：客户端界面类，构建对应页面并处理玩家的界面交互。
 */

import com.starrail.sim.StarRailAttributes;
import com.starrail.sim.StarRailPath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/** Main character overview inspired by the compact character screens of RPGs. */
public final class StarRailCharacterScreen extends Screen {
    private final Button[] navigationButtons = new Button[
            StarRailUiStyle.CHARACTER_NAVIGATION_KEYS.length];
    private Button attributeDetailsButton;
    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;
    private int navLeft;
    private int navWidth;
    private int contentLeft;
    private int contentRight;
    private int rightLeft;
    private int lightConeHeadingY;
    private int lightConeBoxY;
    private boolean compactLayout;

    public StarRailCharacterScreen() {
        super(Component.translatable("screen.starrail_sim.character_detail"));
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

        navLeft = panelLeft + (compactLayout ? 12 : 20);
        navWidth = compactLayout ? 116 : 132;
        contentLeft = navLeft + navWidth + (compactLayout ? 16 : 24);
        contentRight = panelRight - (compactLayout ? 12 : 20);
        int contentWidth = contentRight - contentLeft;
        int minimumRightWidth = compactLayout ? 170 : 230;
        rightLeft = Math.min(contentLeft + Math.max(compactLayout ? 150 : 190,
                        contentWidth * 5 / 9), contentRight - minimumRightWidth);

        int navTop = panelTop + (compactLayout ? 72 : 80);
        int navHeight = compactLayout ? 21 : 24;
        int navGap = compactLayout ? 5 : 8;
        Button[] createdNavigation = StarRailUiStyle.createCharacterNavigation(
                panelLeft, panelTop, panelWidth, panelHeight, 0, this::openSection);
        for (int index = 0; index < navigationButtons.length; index++) {
            navigationButtons[index] = addRenderableWidget(createdNavigation[index]);
        }

        int rowHeight = compactLayout ? 23 : 26;
        lightConeHeadingY = panelTop + 70 + 20 + 18 + 18 + 28 + 20
                + rowHeight * 3 + 14;
        lightConeBoxY = lightConeHeadingY;
        int detailsY = lightConeHeadingY + 24;
        attributeDetailsButton = addRenderableWidget(StarRailUiStyle.button(
                Component.translatable("screen.starrail_sim.character_open_details"),
                ignored -> minecraft.setScreen(new StarRailAttributeScreen()),
                rightLeft, detailsY,
                contentRight - rightLeft, compactLayout ? 21 : 23));
    }

    private void openSection(int index) {
        if (index == 1) {
            minecraft.setScreen(new StarRailLightConeScreen());
        } else if (index == 4) {
            minecraft.setScreen(new StarRailPathScreen());
        } else if (index == 5) {
            minecraft.setScreen(new StarRailGuideScreen());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        StarRailUiStyle.renderBackdrop(graphics, width, height);
        StarRailUiStyle.renderPanel(graphics, panelLeft, panelTop, panelRight, panelBottom);

        Player player = Minecraft.getInstance().player;
        StarRailPath path = StarRailPathClientState.getCurrentPath();
        Component pathName = path.isRealPath()
                ? Component.literal(path.getDisplayName())
                : Component.translatable("screen.starrail_sim.character_unaligned");

        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.character_detail"), panelLeft + 24,
                panelTop + 10, StarRailUiStyle.VALUE_COLOR);
        graphics.drawString(font, Component.literal("✦ ").append(pathName),
                panelLeft + 24, panelTop + 32, path.isRealPath()
                        ? StarRailUiStyle.GOLD_ACCENT : StarRailUiStyle.MUTED_COLOR);
        graphics.fill(contentLeft - 12, panelTop + 52, contentRight,
                panelTop + 53, StarRailUiStyle.DIVIDER_COLOR);

        if (player != null) {
            renderPlayerModel(graphics, player, mouseX, mouseY);
            renderPlayerSummary(graphics, player, path);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderPlayerModel(GuiGraphics graphics, Player player, int mouseX, int mouseY) {
        int modelCenter = (contentLeft + rightLeft) / 2;
        int modelBottom = panelBottom - (compactLayout ? 28 : 36);
        int modelTop = panelTop + 78;
        int scale = compactLayout ? 62 : 82;
        graphics.fill(contentLeft, modelTop, rightLeft - 12, modelBottom,
                StarRailUiStyle.PANEL_INNER);
        graphics.drawCenteredString(font,
                Component.translatable("screen.starrail_sim.character_model"),
                modelCenter, modelTop + 12, StarRailUiStyle.MUTED_COLOR);
        Pose originalPose = player.getPose();
        boolean originalShiftKeyDown = player.isShiftKeyDown();
        player.setPose(Pose.STANDING);
        player.setShiftKeyDown(false);
        try {
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, modelCenter,
                    modelBottom - 10, scale, 0.0F, 0.0F, player);
        } finally {
            player.setPose(originalPose);
            player.setShiftKeyDown(originalShiftKeyDown);
        }
    }

    private void renderPlayerSummary(GuiGraphics graphics, Player player, StarRailPath path) {
        int x = rightLeft;
        int width = contentRight - rightLeft;
        int y = panelTop + 70;
        graphics.drawString(font, player.getName(), x, y, StarRailUiStyle.VALUE_COLOR);
        y += 20;
        drawSummaryLine(graphics, "screen.starrail_sim.character_current_path",
                path.isRealPath() ? Component.literal(path.getDisplayName())
                        : Component.translatable("screen.starrail_sim.character_unaligned"),
                x, y, width);
        y += 18;
        drawSummaryLine(graphics, "screen.starrail_sim.character_rank",
                path.isRealPath() ? Component.translatable(
                        StarRailPathClientState.getCurrentPathRank().getTranslationKey())
                        : Component.translatable("rank.starrail_sim.unaligned"),
                x, y, width);
        y += 28;

        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.character_core_stats"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 20;
        int rowHeight = compactLayout ? 23 : 26;
        int columnGap = 8;
        int columnWidth = (width - columnGap) / 2;
        drawStat(graphics, x, y, columnWidth, "ui.starrail_sim.current_max_health",
                format("%.1f", player.getAttributeValue(Attributes.MAX_HEALTH)));
        drawStat(graphics, x + columnWidth + columnGap, y, columnWidth,
                "ui.starrail_sim.attack_damage",
                format("%.1f", StarRailAttributes.getAttackDamage(player)));
        y += rowHeight;
        drawStat(graphics, x, y, columnWidth, "ui.starrail_sim.armor",
                format("%.1f", player.getAttributeValue(Attributes.ARMOR)));
        drawStat(graphics, x + columnWidth + columnGap, y, columnWidth,
                "ui.starrail_sim.crit_rate",
                formatPercent(StarRailAttributes.getCritRate(player)));
        y += rowHeight;
        drawStat(graphics, x, y, columnWidth, "ui.starrail_sim.crit_damage",
                formatPercent(StarRailAttributes.getCritDamage(player)));
        drawStat(graphics, x + columnWidth + columnGap, y, columnWidth,
                "ui.starrail_sim.movement_speed",
                format("%.3f", player.getAttributeValue(Attributes.MOVEMENT_SPEED)));
        y = lightConeHeadingY;
        Component lightConeLabel = Component.translatable(
                "screen.starrail_sim.character_equipped_light_cone");
        ItemStack lightCone = StarRailLightConeClientData.findEquipped(player);
        Component lightConeValue = lightCone.isEmpty()
                ? Component.translatable("screen.starrail_sim.character_no_light_cone")
                : lightCone.getHoverName();
        graphics.drawString(font, lightConeLabel, x, y, StarRailUiStyle.CYAN_ACCENT);
        graphics.drawString(font, lightConeValue,
                contentRight - font.width(lightConeValue), lightConeBoxY,
                StarRailUiStyle.MUTED_COLOR);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= rightLeft && mouseX <= contentRight
                && mouseY >= lightConeHeadingY - 3 && mouseY <= lightConeHeadingY + 20) {
            minecraft.setScreen(new StarRailLightConeScreen());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawSummaryLine(GuiGraphics graphics, String labelKey, Component value,
            int x, int y, int width) {
        graphics.drawString(font, Component.translatable(labelKey), x, y,
                StarRailUiStyle.MUTED_COLOR);
        graphics.drawString(font, value, x + width - font.width(value), y,
                StarRailUiStyle.VALUE_COLOR);
    }

    private void drawStat(GuiGraphics graphics, int x, int y, int width, String labelKey,
            String value) {
        graphics.fill(x, y, x + width, y + (compactLayout ? 20 : 22),
                StarRailUiStyle.PANEL_INNER);
        Component label = Component.translatable(labelKey);
        graphics.drawString(font, label, x + 8, y + 6, StarRailUiStyle.MUTED_COLOR);
        graphics.drawString(font, Component.literal(value),
                x + width - font.width(value) - 8, y + 6,
                StarRailUiStyle.VALUE_COLOR);
    }

    private static String formatPercent(double value) {
        return format("%.1f%%", value * 100.0D);
    }

    private static String format(String pattern, Object... args) {
        return String.format(Locale.ROOT, pattern, args);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
