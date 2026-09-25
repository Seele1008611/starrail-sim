package com.starrail.sim.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

/** Shared visual language for the mod's screens. */
public final class StarRailUiStyle {
    public static final int STANDARD_PANEL_WIDTH = 820;
    public static final int STANDARD_PANEL_HEIGHT = 500;
    public static final int PANEL_MARGIN = 12;
    public static final int BACKGROUND_OVERLAY = 0x82061118;
    public static final int PANEL_BACKGROUND = 0xC00A1622;
    public static final int PANEL_INNER = 0x60152230;
    public static final int CYAN_ACCENT = 0xFF55D6D2;
    public static final int GOLD_ACCENT = 0xFFE5B96A;
    public static final int DIVIDER_COLOR = 0x6645BFC2;
    public static final int VALUE_COLOR = 0xFFFFFFFF;
    public static final int MUTED_COLOR = 0xFFB7C5CE;
    public static final String[] CHARACTER_NAVIGATION_KEYS = {
            "screen.starrail_sim.character_detail",
            "screen.starrail_sim.character_light_cone",
            "screen.starrail_sim.character_skill_tree",
            "screen.starrail_sim.character_relics",
            "screen.starrail_sim.character_path_level",
            "screen.starrail_sim.character_path_guide"
    };

    private StarRailUiStyle() {
    }

    public static void renderBackdrop(GuiGraphics graphics, int width, int height) {
        graphics.fill(0, 0, width, height, BACKGROUND_OVERLAY);
    }

    public static int panelWidth(int screenWidth) {
        return Math.min(STANDARD_PANEL_WIDTH, screenWidth - PANEL_MARGIN * 2);
    }

    public static int panelHeight(int screenHeight) {
        return Math.min(STANDARD_PANEL_HEIGHT, screenHeight - PANEL_MARGIN * 2);
    }

    public static int panelLeft(int screenWidth) {
        return (screenWidth - panelWidth(screenWidth)) / 2;
    }

    public static int panelTop(int screenHeight) {
        return (screenHeight - panelHeight(screenHeight)) / 2;
    }

    public static boolean isCompact(int panelWidth, int panelHeight) {
        return panelWidth < 700 || panelHeight < 430;
    }

    /** Adds the shared Attribute/Path/Guide/Light Cone navigation. */
    public static Button[] createNavigation(int panelLeft, int panelTop, int panelWidth,
                                            int panelHeight, int selected,
                                            IntConsumer onNavigate) {
        Button[] buttons = new Button[4];
        boolean compact = isCompact(panelWidth, panelHeight);
        String[] labels = {
                "screen.starrail_sim.function_attributes",
                "screen.starrail_sim.function_paths",
                "screen.starrail_sim.function_guide",
                "screen.starrail_sim.function_light_cones"
        };
        if (compact) {
            int gap = 5;
            int left = panelLeft + 12;
            int width = (panelWidth - 24 - gap * 3) / 4;
            int y = panelTop + 58;
            for (int index = 0; index < buttons.length; index++) {
                int buttonIndex = index;
                buttons[index] = button(
                        Component.translatable(labels[index]),
                        ignored -> onNavigate.accept(buttonIndex),
                        left + (width + gap) * index, y, width, 21);
            }
        } else {
            int left = panelLeft + 20;
            int y = panelTop + 78;
            int width = 124;
            for (int index = 0; index < buttons.length; index++) {
                int buttonIndex = index;
                buttons[index] = button(
                        Component.translatable(labels[index]),
                        ignored -> onNavigate.accept(buttonIndex),
                        left, y + 30 * index, width, 24);
            }
        }
        buttons[selected].active = false;
        buttons[3].active = false;
        return buttons;
    }

    /** Adds the persistent left-side character navigation used by every main screen. */
    public static Button[] createCharacterNavigation(int panelLeft, int panelTop,
            int panelWidth, int panelHeight, int selected, IntConsumer onNavigate) {
        Button[] buttons = new Button[CHARACTER_NAVIGATION_KEYS.length];
        boolean compact = isCompact(panelWidth, panelHeight);
        int left = panelLeft + (compact ? 12 : 20);
        int buttonWidth = compact ? 116 : 132;
        int top = panelTop + (compact ? 72 : 80);
        int buttonHeight = compact ? 21 : 24;
        int gap = compact ? 5 : 8;
        for (int index = 0; index < buttons.length; index++) {
            int buttonIndex = index;
            buttons[index] = button(
                    Component.translatable(CHARACTER_NAVIGATION_KEYS[index]),
                    ignored -> onNavigate.accept(buttonIndex), left,
                    top + index * (buttonHeight + gap), buttonWidth, buttonHeight);
        }
        buttons[selected].active = false;
        buttons[2].active = false;
        buttons[3].active = false;
        return buttons;
    }

    public static void renderPanel(GuiGraphics graphics, int left, int top,
                                   int right, int bottom) {
        graphics.fill(left - 3, top - 3, right + 3, bottom + 3, 0x50000000);
        graphics.fill(left, top, right, bottom, PANEL_BACKGROUND);
        graphics.fill(left + 1, top + 1, right - 1, bottom - 1, PANEL_INNER);
        graphics.fill(left, top, right, top + 2, CYAN_ACCENT);
        graphics.fill(left, top + 2, left + 96, top + 4, GOLD_ACCENT);
        graphics.fill(right - 96, bottom - 4, right, bottom - 2, GOLD_ACCENT);
        graphics.fill(left, bottom - 2, right, bottom, CYAN_ACCENT);
        graphics.fill(left + 20, top + 52, right - 20, top + 53, DIVIDER_COLOR);
    }

    public static Button button(Component message, Button.OnPress onPress,
                                 int x, int y, int width, int height) {
        return Button.builder(message, onPress)
                .bounds(x, y, width, height)
                .build(StarRailButton::new);
    }

    private static final class StarRailButton extends Button {
        private StarRailButton(Button.Builder builder) {
            super(builder);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY,
                                    float partialTick) {
            boolean highlighted = isHoveredOrFocused();
            int border = !active ? 0xFF40505A
                    : highlighted ? CYAN_ACCENT : 0xFF55727A;
            int background = !active ? 0x80303A42
                    : highlighted ? 0xD02A5962 : 0xB0122A36;
            graphics.fill(getX(), getY(), getX() + width, getY() + height, border);
            graphics.fill(getX() + 1, getY() + 1,
                    getX() + width - 1, getY() + height - 1, background);
            if (highlighted && active) {
                graphics.fill(getX() + 2, getY() + height - 3,
                        getX() + width - 2, getY() + height - 1, GOLD_ACCENT);
            }
            int color = active ? VALUE_COLOR : 0xFF7C8990;
            graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                    getX() + width / 2,
                    getY() + (height - Minecraft.getInstance().font.lineHeight) / 2,
                    color);
        }
    }
}
