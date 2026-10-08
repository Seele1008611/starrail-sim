package com.starrail.sim.client;

/**
 * 模组代码说明：集中定义模组界面使用的颜色、边框和文字绘制样式。
 */

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
    public static final int PANEL_BACKGROUND = 0x18182238;
    public static final int PANEL_INNER = 0x18212B45;
    public static final int CYAN_ACCENT = 0xFF55D6D2;
    public static final int GOLD_ACCENT = 0xFFE7C77E;
    public static final int DIVIDER_COLOR = 0x40B8BED7;
    public static final int VALUE_COLOR = 0xFFFFFFFF;
    public static final int MUTED_COLOR = 0xFFB7C5CE;
    public static final String[] CHARACTER_NAVIGATION_KEYS = {
            "ui.starrail_sim.nav.details",
            "ui.starrail_sim.nav.light_cone",
            "ui.starrail_sim.nav.traces",
            "ui.starrail_sim.nav.relics",
            "ui.starrail_sim.nav.paths",
            "ui.starrail_sim.nav.guide"
    };

    private StarRailUiStyle() {
    }
    private static long navigationPressedAt;
    private static String navigationPressedLabel = "";

    public static void renderBackdrop(GuiGraphics graphics, int width, int height) {
        StarRailCosmicUi.backdrop(graphics, width, height);
    }

    public static int panelWidth(int screenWidth) {
        return Math.max(1, Math.min(1100, screenWidth - PANEL_MARGIN * 2));
    }

    public static int panelHeight(int screenHeight) {
        return Math.max(1, Math.min(600, screenHeight - PANEL_MARGIN * 2));
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
        int top = panelTop + 72;
        int buttonHeight = 26;
        int gap = 8;
        for (int index = 0; index < buttons.length; index++) {
            int buttonIndex = index;
            buttons[index] = button(
                    Component.translatable(CHARACTER_NAVIGATION_KEYS[index]),
                    ignored -> onNavigate.accept(buttonIndex), left,
                    top + index * (buttonHeight + gap), buttonWidth, buttonHeight);
            ((StarRailButton) buttons[index]).navigation = true;
            ((StarRailButton) buttons[index]).navigationIndex = index;
            setSelected(buttons[index], index == selected);
        }
        buttons[2].active = true;
        buttons[3].active = false;
        return buttons;
    }

    public static void renderPanel(GuiGraphics graphics, int left, int top,
                                   int right, int bottom) {
        // The HTML reference uses an open composition; sections own their fine dividers.
    }

    public static void renderHeader(GuiGraphics graphics, Component title, int left, int top) {
        int x = left + 22, y = top + 20;
        StarRailCosmicUi.navigationIcon(graphics, 4, x, y, GOLD_ACCENT);
        graphics.drawString(Minecraft.getInstance().font, title, x + 17, y - 9, GOLD_ACCENT);
        com.starrail.sim.StarRailPath path = StarRailPathClientState.getCurrentPath();
        Component identity = path.isRealPath() ? Component.literal(path.getDisplayName())
                : Component.translatable("screen.starrail_sim.character_unaligned");
        if (Minecraft.getInstance().player != null) {
            identity = identity.copy().append(" / ").append(Minecraft.getInstance().player.getName());
        }
        graphics.drawString(Minecraft.getInstance().font, identity, x + 17, y + 5, VALUE_COLOR);
    }

    public static Button button(Component message, Button.OnPress onPress,
                                 int x, int y, int width, int height) {
        return Button.builder(message, onPress)
                .bounds(x, y, width, height)
                .build(StarRailButton::new);
    }

    public static Button pathCard(com.starrail.sim.StarRailPath path, Button.OnPress action,
            int x, int y, int width, int height) {
        Button button = button(Component.literal(path.getDisplayName()), action, x, y, width, height);
        ((StarRailButton) button).path = path;
        return button;
    }

    public static Button tabButton(Component message, Button.OnPress action,
            int x, int y, int width, int height) {
        Button button = button(message, action, x, y, width, height);
        ((StarRailButton) button).tab = true;
        return button;
    }

    public static Button rankNode(Component message, Button.OnPress action,
            int x, int y, int width, int height) {
        Button button = button(message, action, x, y, width, height);
        ((StarRailButton) button).rankNode = true;
        return button;
    }

    public static void setRankProgress(Button button, boolean reached, boolean current) {
        if (button instanceof StarRailButton styled) {
            styled.rankReached = reached;
            styled.rankCurrent = current;
        }
    }

    public static Button outlinedButton(Component message, Button.OnPress action,
            int x, int y, int width, int height) {
        Button button = button(message, action, x, y, width, height);
        ((StarRailButton) button).outlined = true;
        return button;
    }

    public static Button closeButton(Button.OnPress action, int x, int y) {
        Button button = button(Component.literal("×"), action, x, y, 22, 22);
        ((StarRailButton) button).closeCross = true;
        return button;
    }

    public static Button modalCloseButton(Button.OnPress action, int x, int y) {
        Button button = closeButton(action, x, y);
        ((StarRailButton) button).closeColor = 0xFF303441;
        return button;
    }

    private static void roundedOutline(GuiGraphics graphics, int x, int y, int width, int height,
            int fill, int border) {
        int r = height / 2;
        graphics.drawManaged(() -> {
            for (int row = 0; row < height; row++) {
                double dy = row + .5 - height / 2.0;
                int inset = (int) Math.round(r - Math.sqrt(Math.max(0, r * r - dy * dy)));
                graphics.fill(x + inset, y + row, x + width - inset, y + row + 1, fill);
            }
            StarRailCosmicUi.line(graphics, x + r, y, x + width - r, y, border);
            StarRailCosmicUi.line(graphics, x + r, y + height - 1, x + width - r, y + height - 1, border);
            for (int i = 0; i < 24; i++) {
                double a = Math.PI / 2 + i * Math.PI / 24, b = a + Math.PI / 24;
                StarRailCosmicUi.line(graphics, x + r + Math.cos(a) * (r - .5), y + r + Math.sin(a) * (r - .5),
                        x + r + Math.cos(b) * (r - .5), y + r + Math.sin(b) * (r - .5), border);
                StarRailCosmicUi.line(graphics, x + width - r - Math.cos(a) * (r - .5), y + r + Math.sin(a) * (r - .5),
                        x + width - r - Math.cos(b) * (r - .5), y + r + Math.sin(b) * (r - .5), border);
            }
        });
    }

    /** Interpolate every channel, including transparency, without altering hit boxes. */
    private static int blend(int from, int to, double progress) {
        double t = Math.max(0, Math.min(1, progress));
        int result = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int a = from >>> shift & 255, b = to >>> shift & 255;
            result |= (int) Math.round(a + (b - a) * t) << shift;
        }
        return result;
    }

    public static void fittedText(GuiGraphics graphics, Component text, int x, int y,
            int availableWidth, int color) {
        if (availableWidth <= 0) return;
        var font = Minecraft.getInstance().font;
        float scale = Math.min(1F, availableWidth / (float) Math.max(1, font.width(text)));
        graphics.pose().pushPose();
        graphics.pose().translate(x, y + (1 - scale) * 4, 0);
        graphics.pose().scale(scale, scale, 1);
        graphics.drawString(font, text, 0, 0, color);
        graphics.pose().popPose();
    }

    public static void setSelected(Button button, boolean selected) {
        if (button instanceof StarRailButton styled) styled.selected = selected;
    }

    private static final java.util.regex.Pattern GUIDE_NUMBER = java.util.regex.Pattern.compile(
            "[+−-]?\\d+(?:\\.\\d+)?(?:%|秒|点|次|格|层|级)?");

    public static Component emphasizeNumbers(Component text) {
        String value = readableText(text);
        java.util.regex.Matcher matcher = GUIDE_NUMBER.matcher(value);
        net.minecraft.network.chat.MutableComponent result = Component.empty();
        int previous = 0;
        while (matcher.find()) {
            result.append(Component.literal(value.substring(previous, matcher.start())));
            result.append(Component.literal(matcher.group()).withStyle(style -> style.withColor(CYAN_ACCENT & 0xFFFFFF)));
            previous = matcher.end();
        }
        return result.append(Component.literal(value.substring(previous)));
    }

    public static String readableText(Component text) {
        return text.getString().replace("\\n", "\n").replace("%%", "%");
    }

    public static void statIcon(GuiGraphics graphics, int index, int x, int y, int color) {
        if (index == 0) {
            StarRailCosmicUi.line(graphics, x - 4, y - 2, x, y + 3, color);
            StarRailCosmicUi.line(graphics, x, y + 3, x + 4, y - 2, color);
            StarRailCosmicUi.line(graphics, x - 4, y - 2, x - 2, y - 4, color);
            StarRailCosmicUi.line(graphics, x - 2, y - 4, x, y - 2, color);
            StarRailCosmicUi.line(graphics, x, y - 2, x + 2, y - 4, color);
            StarRailCosmicUi.line(graphics, x + 2, y - 4, x + 4, y - 2, color);
        } else if (index == 1) {
            StarRailCosmicUi.line(graphics, x - 3, y + 3, x + 3, y - 3, color);
            StarRailCosmicUi.line(graphics, x - 3, y, x, y + 3, color);
            StarRailCosmicUi.line(graphics, x + 3, y - 3, x, y - 3, color);
        } else if (index == 2 || index == 3) {
            StarRailCosmicUi.line(graphics, x - 3, y - 3, x + 3, y - 3, color);
            StarRailCosmicUi.line(graphics, x - 3, y - 3, x - 3, y + 1, color);
            StarRailCosmicUi.line(graphics, x + 3, y - 3, x + 3, y + 1, color);
            StarRailCosmicUi.line(graphics, x - 3, y + 1, x, y + 4, color);
            StarRailCosmicUi.line(graphics, x + 3, y + 1, x, y + 4, color);
            if (index == 3) StarRailCosmicUi.line(graphics, x, y - 2, x, y + 2, color);
        } else if (index == 4) {
            StarRailCosmicUi.line(graphics, x + 2, y - 4, x - 2, y, color);
            StarRailCosmicUi.line(graphics, x - 2, y, x + 2, y, color);
            StarRailCosmicUi.line(graphics, x + 2, y, x - 2, y + 4, color);
        } else {
            int r = index == 5 ? 3 : 4;
            StarRailCosmicUi.line(graphics, x, y - r, x + r, y, color);
            StarRailCosmicUi.line(graphics, x + r, y, x, y + r, color);
            StarRailCosmicUi.line(graphics, x, y + r, x - r, y, color);
            StarRailCosmicUi.line(graphics, x - r, y, x, y - r, color);
        }
    }

    public static void scrollBar(GuiGraphics graphics, int x, int top, int bottom,
            double scroll, int contentHeight) {
        int available = bottom - top;
        if (contentHeight <= available || available <= 0) return;
        int thumb = Math.max(18, available * available / contentHeight);
        int thumbTop = top + (int) ((available - thumb) * scroll / (contentHeight - available));
        graphics.fill(x, top, x + 1, bottom, 0x30BDC4DD);
        graphics.fill(x - 1, thumbTop, x + 2, thumbTop + thumb, 0xA0D6C395);
    }

    private static final class StarRailButton extends Button {
        private com.starrail.sim.StarRailPath path = com.starrail.sim.StarRailPath.NONE;
        private boolean selected;
        private boolean tab;
        private boolean rankNode;
        private boolean rankReached;
        private boolean rankCurrent;
        private boolean outlined;
        private boolean closeCross;
        private int closeColor = VALUE_COLOR;
        private boolean navigation;
        private int navigationIndex;
        private long pressedAt;
        private long hoverFrame;
        private double hoverAmount;
        private boolean pointerPress;
        private double pressX, pressY;
        private StarRailButton(Button.Builder builder) {
            super(builder);
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            pointerPress = true;
            pressX = Math.max(0, Math.min(width, x - getX()));
            pressY = Math.max(0, Math.min(height, y - getY()));
            try { return super.mouseClicked(x, y, button); }
            finally { pointerPress = false; }
        }

        private void ripple(GuiGraphics graphics, double elapsed) {
            if (elapsed >= .5) return;
            double progress = elapsed / .5;
            double radius = Math.hypot(width, height) * (1 - Math.pow(1 - progress, 3));
            int alpha = (int) (35 * (1 - progress));
            graphics.drawManaged(() -> {
                for (int row = 1; row < height - 1; row++) {
                    double dy = row - pressY;
                    if (Math.abs(dy) > radius) continue;
                    double dx = Math.sqrt(Math.max(0, radius * radius - dy * dy));
                    int left = Math.max(1, (int) (pressX - dx));
                    int right = Math.min(width - 1, (int) (pressX + dx));
                    if (right > left) graphics.fill(getX() + left, getY() + row,
                            getX() + right, getY() + row + 1, alpha << 24 | 0xF0D8A0);
                }
            });
        }

        @Override
        public void onPress() {
            if (!pointerPress) { pressX = width / 2.0; pressY = height / 2.0; }
            pressedAt = System.nanoTime();
            if (navigation) {
                navigationPressedAt = pressedAt;
                navigationPressedLabel = getMessage().getString();
            }
            super.onPress();
        }

        private void renderPathCard(GuiGraphics graphics, boolean hovered, double elapsed) {
            int x=getX(), y=getY();
            boolean current = path == StarRailPathClientState.getCurrentPath();
            int color = selected ? GOLD_ACCENT : active ? VALUE_COLOR : 0xFF657086;
            int fill = selected ? 0x363E3B43 : blend(0x18273146, 0x3040506D, hoverAmount);
            graphics.fill(x,y,x+width,y+height,fill);
            if (selected || hoverAmount > .01) {
                graphics.renderOutline(x,y,width,height,selected ? 0xC0E7C77E : blend(0x00798BA8, 0x60798BA8, hoverAmount));
            }
            graphics.fill(x,y+height-1,x+width,y+height,selected ? GOLD_ACCENT : DIVIDER_COLOR);
            boolean tall = height >= 50;
            int cx=tall ? x+width/2 : x+15, cy=tall ? y+14 : y+height/2;
            StarRailPathSymbols.draw(graphics,path,cx,cy,tall ? 8 : 7,color);
            String label=Minecraft.getInstance().font.plainSubstrByWidth(getMessage().getString(),
                    width-(tall ? 10 : 36));
            if (tall) {
                graphics.drawCenteredString(Minecraft.getInstance().font,Component.literal(label),x+width/2,y+29,color);
                if (current) graphics.drawCenteredString(Minecraft.getInstance().font,
                        Component.translatable("ui.starrail_sim.path.current_marker"),x+width/2,y+43,CYAN_ACCENT);
            } else {
                graphics.drawString(Minecraft.getInstance().font,label,x+31,y+(height-9)/2,color);
            }
            if(current) {
                graphics.fill(x+width-6,y+4,x+width-3,y+7,CYAN_ACCENT);
            }
            ripple(graphics, elapsed);
        }

        private void renderRankNode(GuiGraphics graphics, boolean hovered, double elapsed) {
            int cx = getX() + width / 2, cy = getY() + 14;
            int radius = Math.min(12, Math.max(8, width / 2 - 3));
            int color = selected ? GOLD_ACCENT : rankReached ? CYAN_ACCENT : MUTED_COLOR;
            graphics.drawManaged(() -> {
                if (selected || hoverAmount > .01) StarRailCosmicUi.ellipse(graphics, cx, cy,
                        radius + 3, radius + 3, selected ? 0x20E7C77E : blend(0x007EAAC4, 0x147EAAC4, hoverAmount));
                StarRailCosmicUi.ellipse(graphics, cx, cy, radius, radius, 0xFF151E31);
                for (int i = 0; i < 64; i++) {
                    double a = i * Math.PI * 2 / 64, b = (i + 1) * Math.PI * 2 / 64;
                    StarRailCosmicUi.line(graphics, cx + Math.cos(a) * radius, cy + Math.sin(a) * radius,
                            cx + Math.cos(b) * radius, cy + Math.sin(b) * radius,
                            selected ? GOLD_ACCENT : rankReached ? 0xA055D6D2 : 0x7097A6BF);
                }
                if (rankCurrent) graphics.fill(cx - 2, cy + radius - 1, cx + 3, cy + radius + 2, CYAN_ACCENT);
                if (elapsed < .45) StarRailCosmicUi.orbit(graphics, cx, cy,
                        radius + 2 + elapsed * 10, (int) (110 * (1 - elapsed / .45)) << 24 | 0xE7C77E);
            });
            String[] lines = getMessage().getString().split("\n");
            graphics.drawCenteredString(Minecraft.getInstance().font, Component.literal(lines[0]), cx, cy - 4, color);
            if (lines.length > 1) {
                var font = Minecraft.getInstance().font;
                // Fit complete localized rank names, rather than truncating them at narrow widths.
                float scale = Math.min(1F, (width - 2F) / Math.max(1, font.width(lines[1])));
                graphics.pose().pushPose();
                graphics.pose().translate(cx, getY() + 32, 0);
                graphics.pose().scale(scale, scale, 1);
                graphics.drawCenteredString(font, Component.literal(lines[1]), 0, 0, color);
                graphics.pose().popPose();
            }
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY,
                                    float partialTick) {
            boolean highlighted = isHoveredOrFocused();
            long now = System.nanoTime();
            double dt = hoverFrame == 0 ? 0 : Math.min(.1, (now - hoverFrame) / 1_000_000_000.0);
            hoverFrame = now;
            hoverAmount += ((highlighted && active ? 1 : 0) - hoverAmount) * (1 - Math.exp(-dt / .065));
            if (!active) hoverAmount = 0;
            double elapsed = (System.nanoTime() - pressedAt) / 1_000_000_000.0;
            if (navigation && getMessage().getString().equals(navigationPressedLabel)) {
                elapsed = (System.nanoTime() - navigationPressedAt) / 1_000_000_000.0;
            }
            int pressed = active && elapsed < .1 ? 1 : 0;
            if (path.isRealPath()) {
                renderPathCard(graphics, highlighted, elapsed);
                return;
            }
            if (rankNode) {
                renderRankNode(graphics, highlighted, elapsed);
                return;
            }
            if (closeCross) {
                int color = blend(closeColor, CYAN_ACCENT, hoverAmount);
                int cx = getX() + width / 2, cy = getY() + height / 2;
                StarRailCosmicUi.line(graphics, cx - 4, cy - 4, cx + 4, cy + 4, color);
                StarRailCosmicUi.line(graphics, cx - 4, cy + 4, cx + 4, cy - 4, color);
                return;
            }
            if (tab) {
                roundedOutline(graphics, getX() + pressed, getY() + pressed,
                        width - pressed * 2, height - pressed * 2,
                        selected ? 0xFFE0E1E7 : blend(0x122C3550, 0x344F6079, hoverAmount),
                        selected ? 0xFFE0E1E7 : blend(0x209EACC2, 0x80E7C77E, hoverAmount));
            } else if (outlined) {
                roundedOutline(graphics, getX() + pressed, getY() + pressed,
                        width - pressed * 2, height - pressed * 2,
                        blend(0x162C3550, 0x344F6079, hoverAmount),
                        blend(0x709EACC2, GOLD_ACCENT, hoverAmount));
            } else if (!navigation) {
                graphics.fill(getX() + pressed, getY() + pressed,
                        getX() + width - pressed, getY() + height - pressed,
                        selected ? 0x40CBB574 : blend(PANEL_INNER, 0x344F6079, hoverAmount));
                graphics.fill(getX(), getY() + height - 1, getX() + width,
                        getY() + height, selected ? GOLD_ACCENT : DIVIDER_COLOR);
            } else {
                int cx = getX() + 12, cy = getY() + height / 2;
                if (selected || hoverAmount > .01) {
                    for (int glow = 3; glow > 0; glow--) {
                        StarRailCosmicUi.ellipse(graphics, cx, cy, 8 + glow, 8 + glow,
                                selected ? 0x12D6BE80 : blend(0x009EBDDF, 0x0A9EBDDF, hoverAmount));
                    }
                    StarRailCosmicUi.orbit(graphics, cx, cy, 10,
                            selected ? 0xC0D6BE80 : blend(0x009EBDDF, 0x709EBDDF, hoverAmount));
                }
                StarRailCosmicUi.navigationIcon(graphics, navigationIndex, cx, cy,
                        selected ? GOLD_ACCENT : active ? VALUE_COLOR : 0xFF657086);
            }
            int color = selected ? GOLD_ACCENT : active ? VALUE_COLOR : 0xFF657086;
            if (tab && selected) color = 0xFF303441;
            Component label = getMessage();
            int maxTextWidth = Math.max(10, width - (navigation ? 34 : 12));
            String[] lines = label.getString().split("\n");
            int lineTop = getY() + (height - lines.length * 12 + 3) / 2;
            for (int index = 0; index < lines.length; index++) {
                String text = Minecraft.getInstance().font.plainSubstrByWidth(lines[index], maxTextWidth);
                int lineColor = index == 0 ? color : selected ? GOLD_ACCENT : MUTED_COLOR;
                if (navigation) {
                    graphics.drawString(Minecraft.getInstance().font, text, getX() + 32,
                            lineTop + index * 12, lineColor);
                } else {
                    graphics.drawCenteredString(Minecraft.getInstance().font, Component.literal(text),
                            getX() + width / 2, lineTop + index * 12, lineColor);
                }
            }
            if (elapsed < .55 && (active || tab && selected)) {
                double progress = elapsed / .55;
                if (!navigation) ripple(graphics, elapsed);
                if (navigation) {
                    int end = getX() + 30 + (int) ((width - 32) * Math.min(1, progress / .6));
                    int start = progress < .6 ? getX() + 30
                            : getX() + 30 + (int) ((width - 32) * (progress - .6) / .4);
                    graphics.fill(start, getY() + height - 5, end + 1, getY() + height - 4, GOLD_ACCENT);
                    int dot = progress < .6 ? getX() + 30 : end;
                    graphics.fill(dot - 1, getY() + height - 6, dot + 2,
                            getY() + height - 3, GOLD_ACCENT);
                }
            }
        }
    }
}
