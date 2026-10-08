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
public final class StarRailCharacterScreen extends StarRailStyledScreen {
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
        super.init();
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
        rightLeft = contentRight - Math.min(240, Math.max(195, contentWidth * 32 / 100));

        Button[] createdNavigation = StarRailUiStyle.createCharacterNavigation(
                panelLeft, panelTop, panelWidth, panelHeight, 0, this::openSection);
        for (int index = 0; index < navigationButtons.length; index++) {
            navigationButtons[index] = addRenderableWidget(createdNavigation[index]);
        }

        lightConeHeadingY = panelBottom - 76;
        lightConeBoxY = lightConeHeadingY;
        int detailsY = panelTop + 150 + 21 * 7 + 16;
        attributeDetailsButton = addRenderableWidget(StarRailUiStyle.outlinedButton(
                Component.translatable("screen.starrail_sim.character_open_details"),
                ignored -> minecraft.setScreen(new StarRailAttributeScreen(this)),
                contentRight - 114, detailsY,
                114, 22));
    }

    private void openSection(int index) {
        if (index == 1) {
            minecraft.setScreen(new StarRailLightConeScreen());
        } else if (index == 2) {
            minecraft.setScreen(new StarRailTraceScreen());
        } else if (index == 4) {
            minecraft.setScreen(new StarRailPathScreen());
        } else if (index == 5) {
            minecraft.setScreen(new StarRailGuideScreen());
        }
    }

    @Override
    protected void renderPage(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        StarRailUiStyle.renderBackdrop(graphics, width, height);
        StarRailUiStyle.renderPanel(graphics, panelLeft, panelTop, panelRight, panelBottom);

        Player player = Minecraft.getInstance().player;
        StarRailPath path = StarRailPathClientState.getCurrentPath();
        StarRailUiStyle.renderHeader(graphics, title, panelLeft, panelTop);

        if (player != null) {
            renderPlayerModel(graphics, player, mouseX, mouseY);
            renderPlayerSummary(graphics, player, path, mouseX, mouseY);
        }

        super.renderPage(graphics, mouseX, mouseY, partialTick);
    }

    private void renderPlayerModel(GuiGraphics graphics, Player player, int mouseX, int mouseY) {
        int modelCenter = (contentLeft + rightLeft) / 2;
        int modelBottom = panelBottom - (compactLayout ? 28 : 36);
        int modelTop = panelTop + 78;
        int scale = Math.min((modelBottom - modelTop) / 3,
                (rightLeft - contentLeft - 18) / 2);
        StarRailCosmicUi.platform(graphics, modelCenter, modelBottom - 12,
                Math.max(100, Math.min(220, (rightLeft - contentLeft) * 2 / 3)), width);
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

    private void renderPlayerSummary(GuiGraphics graphics, Player player, StarRailPath path, int mouseX, int mouseY) {
        int x = rightLeft;
        int width = contentRight - rightLeft;
        int y = panelTop + 70;
        StarRailUiStyle.fittedText(graphics, player.getName(), x, y, width, StarRailUiStyle.VALUE_COLOR);
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

        graphics.fill(x, y - 2, contentRight, y + 13, 0x263D4764);
        graphics.drawCenteredString(font, Component.translatable(
                "screen.starrail_sim.character_core_stats"), (x + contentRight) / 2, y,
                StarRailUiStyle.VALUE_COLOR);
        y += 20;
        int rowHeight = 21;
        drawStat(graphics, x, y, width, "ui.starrail_sim.current_max_health",
                format("%.1f", player.getAttributeValue(Attributes.MAX_HEALTH)));
        y += rowHeight;
        drawStat(graphics, x, y, width,
                "ui.starrail_sim.attack_damage",
                format("%.1f", StarRailAttributes.getAttackDamage(player)));
        y += rowHeight;
        drawStat(graphics, x, y, width, "ui.starrail_sim.armor",
                format("%.1f", player.getAttributeValue(Attributes.ARMOR)));
        y += rowHeight;
        drawStat(graphics, x, y, width, "ui.starrail_sim.armor_toughness",
                format("%.1f", player.getAttributeValue(Attributes.ARMOR_TOUGHNESS)));
        y += rowHeight;
        drawStat(graphics, x, y, width, "ui.starrail_sim.movement_speed",
                format("%.3f", player.getAttributeValue(Attributes.MOVEMENT_SPEED)));
        y += rowHeight;
        drawStat(graphics, x, y, width,
                "ui.starrail_sim.crit_rate",
                formatPercent(StarRailAttributes.getCritRate(player)));
        y += rowHeight;
        drawStat(graphics, x, y, width, "ui.starrail_sim.crit_damage",
                formatPercent(StarRailAttributes.getCritDamage(player)));
        y = lightConeHeadingY;
        ItemStack lightCone = StarRailLightConeClientData.findEquipped(player);
        Component lightConeValue = lightCone.isEmpty()
                ? Component.translatable("screen.starrail_sim.character_no_light_cone")
                : lightCone.getHoverName();
        boolean hovered = mouseX >= x && mouseX <= contentRight && mouseY >= y && mouseY <= y + 60;
        graphics.fill(x, y, contentRight, y + 60, 0x303C4564);
        clip(graphics, x + 1, y + 1, contentRight - 1, y + 59);
        StarRailLightConeDisplay.banner(graphics, lightCone, x + 1, y + 1, width - 2, 42);
        graphics.fillGradient(x + 1, y + 1, contentRight - 1, y + 59, 0x0010172A, 0x3510172A);
        graphics.fill(x + 1, y + 43, contentRight - 1, y + 59, 0xA810172A);
        graphics.drawString(font, font.plainSubstrByWidth(lightConeValue.getString(), width - 16),
                x + 8, y + 47, StarRailUiStyle.VALUE_COLOR);
        graphics.disableScissor();
        int border = hovered ? StarRailUiStyle.GOLD_ACCENT : 0x809DAEC6;
        graphics.renderOutline(x, y, width, 60, border);
        if (hovered) {
            graphics.drawString(font, Component.literal("→"), contentRight - 16, y + 7,
                    StarRailUiStyle.GOLD_ACCENT);
        }
    }

    @Override
    protected boolean logicalMouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= rightLeft && mouseX <= contentRight
                && mouseY >= lightConeHeadingY && mouseY <= lightConeHeadingY + 60) {
            minecraft.setScreen(new StarRailLightConeScreen());
            return true;
        }
        return super.logicalMouseClicked(mouseX, mouseY, button);
    }

    private void drawSummaryLine(GuiGraphics graphics, String labelKey, Component value,
            int x, int y, int width) {
        int valueWidth = Math.min(font.width(value), width / 2);
        StarRailUiStyle.fittedText(graphics, Component.translatable(labelKey), x, y,
                width - valueWidth - 10, StarRailUiStyle.MUTED_COLOR);
        StarRailUiStyle.fittedText(graphics, value, x + width - valueWidth, y,
                valueWidth, StarRailUiStyle.VALUE_COLOR);
    }

    private void drawStat(GuiGraphics graphics, int x, int y, int width, String labelKey,
            String value) {
        graphics.fill(x, y, x + width, y + 20, 0x142C3550);
        Component label = Component.translatable(labelKey);
        int icon = labelKey.endsWith("max_health") ? 0 : labelKey.endsWith("attack_damage") ? 1
                : labelKey.endsWith("armor") ? 2 : labelKey.endsWith("armor_toughness") ? 3
                : labelKey.endsWith("movement_speed") ? 4 : labelKey.endsWith("crit_rate") ? 5 : 6;
        StarRailUiStyle.statIcon(graphics, icon, x + 8, y + 10, StarRailUiStyle.MUTED_COLOR);
        int valueWidth = Math.min(font.width(value), width / 2 - 5);
        StarRailUiStyle.fittedText(graphics, label, x + 20, y + 6,
                width - valueWidth - 35, StarRailUiStyle.MUTED_COLOR);
        StarRailUiStyle.fittedText(graphics, Component.literal(value), x + width - valueWidth - 5,
                y + 6, valueWidth, StarRailUiStyle.VALUE_COLOR);
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
