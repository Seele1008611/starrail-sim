package com.starrail.sim.client;

import com.starrail.sim.StarRailAttributes;
import com.starrail.sim.StarRailPath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

/** Read-only character attributes displayed with vanilla-style button frames. */
public final class StarRailAttributeScreen extends Screen {
    private static final int STAT_HEIGHT = 25;
    private static final int ROW_GAP = 30;

    private final Button[] navigationButtons = new Button[
            StarRailUiStyle.CHARACTER_NAVIGATION_KEYS.length];
    private final Button[] statButtons = new Button[13];
    private final String[] statDetails = new String[13];
    private int selectedStat = -1;
    private int leftColumn;
    private int rightColumn;
    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;
    private int contentLeft;
    private int contentRight;
    private int gridTop;
    private int gridColumns;
    private int statHeight;
    private int rowStep;
    private boolean compactLayout;

    public StarRailAttributeScreen() {
        super(Component.translatable("screen.starrail_sim.attributes"));
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
        Button[] createdNavigation = StarRailUiStyle.createCharacterNavigation(
                panelLeft, panelTop, panelWidth, panelHeight, 0, index -> {
                    if (index == 1) {
                        minecraft.setScreen(new StarRailLightConeScreen());
                    } else if (index == 4) {
                        minecraft.setScreen(new StarRailPathScreen());
                    } else if (index == 5) {
                        minecraft.setScreen(new StarRailGuideScreen());
                    }
                });
        for (int index = 0; index < navigationButtons.length; index++) {
            navigationButtons[index] = addRenderableWidget(createdNavigation[index]);
        }
        if (compactLayout) {
            contentLeft = panelLeft + 12 + 116 + 16;
            contentRight = panelRight - 12;
            gridTop = panelTop + 130;
            gridColumns = 2;
        } else {
            int contentWidthLeft = panelLeft + 20 + 132 + 24;
            contentLeft = contentWidthLeft;
            contentRight = panelRight - 22;
            gridTop = panelTop + 114;
            gridColumns = 2;
        }

        int columnGap = compactLayout ? 7 : 12;
        int columnWidth = (contentRight - contentLeft - columnGap * (gridColumns - 1))
                / gridColumns;
        leftColumn = contentLeft;
        rightColumn = leftColumn + columnWidth + columnGap;
        int rows = (statButtons.length + gridColumns - 1) / gridColumns;
        statHeight = compactLayout ? 20 : STAT_HEIGHT;
        int availableHeight = panelBottom - 30 - gridTop - 8;
        int maxStep = rows > 1 ? (availableHeight - statHeight) / (rows - 1) : statHeight;
        int desiredStep = compactLayout ? 23 : ROW_GAP;
        rowStep = Math.max(statHeight, Math.min(desiredStep, maxStep));

        for (int index = 0; index < statButtons.length; index++) {
            int column = index % gridColumns;
            int row = index / gridColumns;
            int statIndex = index;
            statButtons[index] = addRenderableWidget(StarRailUiStyle.button(
                            Component.empty(), ignored -> {
                                selectedStat = statIndex;
                    }, contentLeft + column * (columnWidth + columnGap),
                            gridTop + row * rowStep, columnWidth, statHeight)
                    );
        }

    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        StarRailUiStyle.renderBackdrop(graphics, width, height);
        StarRailUiStyle.renderPanel(graphics, panelLeft, panelTop, panelRight, panelBottom);
        graphics.drawString(font, title, panelLeft + 24, panelTop + 10,
                StarRailUiStyle.VALUE_COLOR);
        int statusY = compactLayout ? panelTop + 82 : panelTop + 64;
        graphics.drawString(font, Component.literal("STATUS"),
                contentLeft, statusY, StarRailUiStyle.CYAN_ACCENT);
        graphics.fill(contentLeft + 48, statusY + 4, contentRight,
                statusY + 5, StarRailUiStyle.DIVIDER_COLOR);
        int sectionY = compactLayout ? panelTop + 101 : panelTop + 82;
        graphics.drawString(font, Component.translatable(
                        "screen.starrail_sim.function_attributes"),
                contentLeft, sectionY, StarRailUiStyle.VALUE_COLOR);
        Player identityPlayer = Minecraft.getInstance().player;
        if (identityPlayer != null) {
            StarRailPath path = StarRailPathClientState.getCurrentPath();
            Component pathText = path.isRealPath()
                    ? Component.literal(path.getDisplayName() + " · ")
                    .append(Component.translatable(StarRailPathClientState
                            .getCurrentPathRank().getTranslationKey()))
                    : Component.translatable("guide.starrail_sim.current.unaligned");
            graphics.drawString(font, pathText, contentRight - font.width(pathText),
                    sectionY, StarRailUiStyle.MUTED_COLOR);
        }

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            setStat(0, "ui.starrail_sim.current_max_health",
                    format("%.1f / %.1f", player.getHealth(),
                            player.getAttributeValue(Attributes.MAX_HEALTH)),
                    format("当前生命值 %.1f / 最大生命值 %.1f",
                            player.getHealth(), player.getAttributeValue(Attributes.MAX_HEALTH)));
            statDetails[0] = describeHealth(player);
            statButtons[0].setTooltip(Tooltip.create(Component.translatable(
                    "screen.starrail_sim.attribute_detail", statDetails[0])));
            setStat(1, "ui.starrail_sim.attack_damage",
                    format("%.1f", StarRailAttributes.getAttackDamage(player)),
                    describeAttribute(player, Attributes.ATTACK_DAMAGE,
                            StarRailAttributes.getAttackDamage(player), false));
            setStat(2, "ui.starrail_sim.armor",
                    format("%.1f", player.getAttributeValue(Attributes.ARMOR)),
                    describeAttribute(player, Attributes.ARMOR,
                            player.getAttributeValue(Attributes.ARMOR), false));
            setStat(3, "ui.starrail_sim.armor_toughness",
                    format("%.1f", player.getAttributeValue(Attributes.ARMOR_TOUGHNESS)),
                    describeAttribute(player, Attributes.ARMOR_TOUGHNESS,
                            player.getAttributeValue(Attributes.ARMOR_TOUGHNESS), false));
            setStat(4, "ui.starrail_sim.movement_speed",
                    format("%.3f", player.getAttributeValue(Attributes.MOVEMENT_SPEED)),
                    describeAttribute(player, Attributes.MOVEMENT_SPEED,
                            player.getAttributeValue(Attributes.MOVEMENT_SPEED), false));
            setStat(5, "ui.starrail_sim.attack_speed",
                    format("%.3f", player.getAttributeValue(Attributes.ATTACK_SPEED)),
                    describeAttribute(player, Attributes.ATTACK_SPEED,
                            player.getAttributeValue(Attributes.ATTACK_SPEED), false));

            setPercentStat(6, "ui.starrail_sim.crit_rate",
                    StarRailAttributes.getCritRate(player));
            setPercentStat(7, "ui.starrail_sim.crit_damage",
                    StarRailAttributes.getCritDamage(player));
            setPercentStat(8, "ui.starrail_sim.break_effect",
                    StarRailAttributes.getValue(player, StarRailAttributes.BREAK_EFFECT, 0.0D));
            setPercentStat(9, "ui.starrail_sim.effect_hit_rate",
                    StarRailAttributes.getValue(player, StarRailAttributes.EFFECT_HIT_RATE, 0.0D));
            setPercentStat(10, "ui.starrail_sim.effect_resistance",
                    StarRailAttributes.getValue(player, StarRailAttributes.EFFECT_RESISTANCE, 0.0D));
            setStat(11, "ui.starrail_sim.knockback_resistance",
                    format("%.3f", player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)),
                    describeAttribute(player, Attributes.KNOCKBACK_RESISTANCE,
                            player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), false));
            setPercentStat(12, "ui.starrail_sim.healing_effect",
                    StarRailAttributes.getValue(player, StarRailAttributes.HEALING_EFFECT, 0.0D));
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void setStat(int index, String labelKey, String value, String detail) {
        statDetails[index] = detail;
        statButtons[index].setMessage(Component.translatable(labelKey)
                .append("    ").append(value));
        statButtons[index].setTooltip(Tooltip.create(Component.translatable(
                "screen.starrail_sim.attribute_detail", detail)));
    }

    private void setPercentStat(int index, String labelKey, double value) {
        Player player = Minecraft.getInstance().player;
        String detail = player == null ? format("最终值 %.1f%%", value * 100.0D)
                : describeAttribute(player, customAttribute(index), value, true);
        setStat(index, labelKey, format("%.1f%%", value * 100.0D), detail);
    }

    private static String describeHealth(Player player) {
        double current = player.getHealth();
        double maximum = player.getAttributeValue(Attributes.MAX_HEALTH);
        var instance = player.getAttribute(Attributes.MAX_HEALTH);
        if (instance == null) {
            return format("当前生命值 %.1f / %.1f", current, maximum);
        }
        double base = instance.getBaseValue();
        return format("当前生命值 %.1f / %.1f；基础最大值 %.1f，加成 %+.1f，最终最大值 %.1f",
                current, maximum, base, maximum - base, maximum);
    }

    private net.minecraft.world.entity.ai.attributes.Attribute customAttribute(int index) {
        return switch (index) {
            case 6 -> StarRailAttributes.CRIT_RATE.get();
            case 7 -> StarRailAttributes.CRIT_DAMAGE.get();
            case 8 -> StarRailAttributes.BREAK_EFFECT.get();
            case 9 -> StarRailAttributes.EFFECT_HIT_RATE.get();
            case 10 -> StarRailAttributes.EFFECT_RESISTANCE.get();
            case 12 -> StarRailAttributes.HEALING_EFFECT.get();
            default -> throw new IllegalArgumentException("Unknown custom stat index: " + index);
        };
    }

    private static String describeAttribute(Player player,
                                            net.minecraft.world.entity.ai.attributes.Attribute attribute,
                                            double effective, boolean percent) {
        var instance = player.getAttribute(attribute);
        if (instance == null) {
            return format("最终值 %.1f", effective);
        }
        double base = instance.getBaseValue();
        double bonus = effective - base;
        if (percent) {
            return format("基础值 %.1f%%，加成 %+.1f%%，最终值 %.1f%%",
                    base * 100.0D, bonus * 100.0D, effective * 100.0D);
        }
        return format("基础值 %.3f，加成 %+.3f，最终值 %.3f", base, bonus, effective);
    }

    private static String format(String pattern, Object... args) {
        return String.format(Locale.ROOT, pattern, args);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
