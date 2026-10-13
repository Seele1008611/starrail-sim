package com.starrail.sim.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Side-by-side font and shape sample; does not change global display settings. */
public final class StarRailDisplayPreviewScreen extends StarRailStyledScreen {
    private static final ResourceLocation SOURCE_HAN = new ResourceLocation("starrail_sim", "source_han_sans");
    private static final ResourceLocation DEFAULT = new ResourceLocation("minecraft", "default");
    private static final float[] TEXT_SCALES = {1, 1.15F, 1.3F, 1.5F};
    private final Screen parent;
    private final long openedAt = System.nanoTime();
    private final Button[] sizeButtons = new Button[TEXT_SCALES.length];
    private int sizeIndex;
    private int legacyClicks;
    private int smoothClicks;
    private int left, top, columnWidth, columnTop, panelBottom;

    public StarRailDisplayPreviewScreen(Screen parent) {
        super(Component.translatable("screen.starrail_sim.display_preview"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        left = StarRailUiStyle.panelLeft(width) + 18;
        top = StarRailUiStyle.panelTop(height);
        panelBottom = top + StarRailUiStyle.panelHeight(height);
        columnTop = top + 72;
        columnWidth = (StarRailUiStyle.panelWidth(width) - 52) / 2;
        addRenderableWidget(StarRailUiStyle.button(Component.translatable("ui.starrail_sim.display.try_button"),
                ignored -> legacyClicks++, left + (columnWidth - 190) / 2, columnTop + 252, 190, 26));
        addRenderableWidget(new SmoothPreviewButton(left + columnWidth + 16 + (columnWidth - 190) / 2,
                columnTop + 252, 190, 26, ignored -> smoothClicks++));
        int sizesLeft = left + 112;
        for (int index = 0; index < sizeButtons.length; index++) {
            int selected = index;
            sizeButtons[index] = addRenderableWidget(StarRailUiStyle.button(
                    Component.literal(Math.round(TEXT_SCALES[index] * 100) + "%"),
                    ignored -> { sizeIndex = selected; updateSizes(); },
                    sizesLeft + index * 60, panelBottom - 36, 54, 24));
        }
        addRenderableWidget(StarRailUiStyle.button(Component.translatable("gui.back"),
                ignored -> onClose(), left + columnWidth * 2 + 16 - 106, panelBottom - 36, 106, 24));
        updateSizes();
    }

    private void updateSizes() {
        for (int index = 0; index < sizeButtons.length; index++)
            StarRailUiStyle.setSelected(sizeButtons[index], index == sizeIndex);
    }

    @Override
    protected void renderPage(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        StarRailUiStyle.renderBackdrop(graphics, width, height);
        graphics.drawString(font, title, left, top + 20, StarRailUiStyle.GOLD_ACCENT, false);
        graphics.drawString(font, Component.translatable("ui.starrail_sim.display.preview_hint"),
                left, top + 43, StarRailUiStyle.MUTED_COLOR, false);
        column(graphics, left, false, legacyClicks);
        column(graphics, left + columnWidth + 16, true, smoothClicks);
        graphics.drawString(font, Component.translatable("ui.starrail_sim.display.text_size"),
                left, panelBottom - 28, StarRailUiStyle.MUTED_COLOR, false);
        graphics.drawString(font, Component.translatable(!StarRailSharpText.available()
                        ? "ui.starrail_sim.display.font_fallback" : StarRailSmoothUi.available()
                        ? "ui.starrail_sim.display.shader_ready" : "ui.starrail_sim.display.shader_fallback"),
                left, panelBottom - 52, StarRailSmoothUi.available()
                        ? StarRailUiStyle.CYAN_ACCENT : StarRailUiStyle.GOLD_ACCENT, false);
        super.renderPage(graphics, mouseX, mouseY, partialTick);
    }

    private void column(GuiGraphics graphics, int x, boolean smooth, int clicks) {
        graphics.fill(x, columnTop, x + columnWidth, columnTop + 300, 0xB80D182A);
        graphics.drawString(font, Component.translatable(smooth
                        ? "ui.starrail_sim.display.source_han" : "ui.starrail_sim.display.vanilla"),
                x + 14, columnTop + 12, smooth ? StarRailUiStyle.CYAN_ACCENT : StarRailUiStyle.GOLD_ACCENT, false);
        Component[] samples = {
                Component.translatable("ui.starrail_sim.display.sample_cn"),
                Component.translatable("ui.starrail_sim.display.sample_stats"),
                Component.translatable("ui.starrail_sim.display.sample_mixed"),
                Component.translatable("ui.starrail_sim.display.sample_modifiers").withStyle(ChatFormatting.AQUA),
                Component.empty().append(Component.translatable("ui.starrail_sim.display.sample_bold")
                        .withStyle(ChatFormatting.BOLD)).append(" / ")
                        .append(Component.translatable("ui.starrail_sim.display.sample_italic")
                                .withStyle(ChatFormatting.ITALIC))
        };
        float scale = TEXT_SCALES[sizeIndex];
        for (int index = 0; index < samples.length; index++) {
            text(graphics, samples[index], x + 14, columnTop + 36 + index * 18 * scale,
                    scale, smooth, StarRailUiStyle.VALUE_COLOR);
        }
        float cx = x + columnWidth / 2F, cy = columnTop + 210;
        int accent = smooth ? StarRailUiStyle.CYAN_ACCENT : StarRailUiStyle.GOLD_ACCENT;
        if (smooth) {
            StarRailSmoothUi.circle(graphics, cx, cy, 28, 1, 0x401F3548, accent);
        } else {
            graphics.drawManaged(() -> {
                StarRailCosmicUi.ellipse(graphics, cx, cy, 28, 28, 0x401F3548);
                for (int index = 0; index < 64; index++) {
                    double a = index * Math.PI * 2 / 64, b = (index + 1) * Math.PI * 2 / 64;
                    StarRailCosmicUi.line(graphics, cx + Math.cos(a) * 28, cy + Math.sin(a) * 28,
                            cx + Math.cos(b) * 28, cy + Math.sin(b) * 28, accent);
                }
            });
        }
        // One uninterrupted clock drives both samples, even after a click or size change.
        double phase = (System.nanoTime() - openedAt) / 1_000_000_000.0 * Math.PI / 12;
        for (int index = 0; index < 3; index++) {
            double angle = phase + index * Math.PI * 2 / 3;
            float px = cx + (float) Math.cos(angle) * 36, py = cy + (float) Math.sin(angle) * 36;
            if (smooth) StarRailSmoothUi.circle(graphics, px, py, 1.3F, 0, accent, accent);
            else graphics.fill(Math.round(px), Math.round(py), Math.round(px) + 2, Math.round(py) + 2, accent);
        }
        if (smooth) {
            graphics.pose().pushPose();
            graphics.pose().translate(cx, cy, 0);
            graphics.pose().scale(1.5F, 1.5F, 1);
            boolean drawn = StarRailSharpText.draw(graphics, Component.literal("7"), 0, 0, accent, true, true);
            graphics.pose().popPose();
            if (!drawn) StarRailGlyphCenter.draw(graphics, font, SOURCE_HAN, '7', cx, cy, 1.5F, accent);
        } else {
            StarRailGlyphCenter.draw(graphics, font, DEFAULT, '7', cx, cy, 1.5F, accent);
        }
        text(graphics, Component.translatable("ui.starrail_sim.display.click_count", clicks),
                cx, columnTop + 285, 1, smooth, StarRailUiStyle.MUTED_COLOR, true);
    }

    private void text(GuiGraphics graphics, Component value, float x, float y, float scale,
            boolean smooth, int color) { text(graphics, value, x, y, scale, smooth, color, false); }

    private void text(GuiGraphics graphics, Component value, float x, float y, float scale,
            boolean smooth, int color, boolean centered) {
        Component styled = value.copy().withStyle(style -> style.withFont(smooth ? SOURCE_HAN : DEFAULT));
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1);
        if (!smooth || !StarRailSharpText.draw(graphics, styled, 0, 0, color, centered, false)) {
            font.drawInBatch(styled, centered ? -font.width(styled) / 2F : 0, 0, color, false,
                    graphics.pose().last().pose(), graphics.bufferSource(),
                    net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 15728880);
        }
        graphics.pose().popPose();
    }

    @Override public void onClose() { minecraft.setScreen(parent); }

    private static final class SmoothPreviewButton extends Button {
        private long lastFrame;
        private long pressedAt;
        private float hover;

        private SmoothPreviewButton(int x, int y, int width, int height, OnPress action) {
            super(Button.builder(Component.translatable("ui.starrail_sim.display.try_button")
                    .withStyle(style -> style.withFont(SOURCE_HAN)), action).bounds(x, y, width, height));
        }

        @Override public void onPress() { pressedAt = System.nanoTime(); super.onPress(); }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            long now = System.nanoTime();
            double delta = lastFrame == 0 ? 0 : Math.min(.1, (now - lastFrame) / 1_000_000_000.0);
            lastFrame = now;
            hover += ((isHoveredOrFocused() ? 1 : 0) - hover) * (float) (1 - Math.exp(-delta / .16));
            float press = pressedAt == 0 ? 0 : Math.max(0, 1 - (now - pressedAt) / 360_000_000F);
            int fill = (int) (20 + hover * 24 + press * 28) << 24 | 0x55D6D2;
            int border = (int) (115 + hover * 140) << 24 | 0x55D6D2;
            StarRailSmoothUi.roundedRect(graphics, getX() + press, getY() + press,
                    width - press * 2, height - press * 2, height / 2F, 1, fill, border);
            if (!StarRailSharpText.draw(graphics, getMessage(), getX() + width / 2F,
                    getY() + height / 2F + press, StarRailUiStyle.VALUE_COLOR, true, true)) {
                var font = Minecraft.getInstance().font;
                graphics.drawString(font, getMessage(), getX() + (width - font.width(getMessage())) / 2,
                        getY() + (height - 9) / 2 + Math.round(press), StarRailUiStyle.VALUE_COLOR, false);
            }
        }
    }
}
