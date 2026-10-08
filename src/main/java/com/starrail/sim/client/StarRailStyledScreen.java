package com.starrail.sim.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Shared logical coordinates keep rendering and pointer hit boxes aligned at every GUI scale. */
public abstract class StarRailStyledScreen extends Screen {
    private double uiScale = 1.0;
    private final java.util.List<StarRailSmoothScroll> scrollPanels = new java.util.ArrayList<>();

    protected StarRailStyledScreen(Component title) { super(title); }

    @Override
    protected void init() {
        scrollPanels.forEach(StarRailSmoothScroll::cancelInteraction);
        scrollPanels.clear();
        int actualWidth = width, actualHeight = height;
        uiScale = Math.max(.01, Math.min(actualWidth / 800.0, actualHeight / 450.0));
        width = (int) Math.round(actualWidth / uiScale);
        height = (int) Math.round(actualHeight / uiScale);
        if (showGlobalClose()) addRenderableWidget(StarRailUiStyle.closeButton(ignored -> onClose(), StarRailUiStyle.panelLeft(width) + StarRailUiStyle.panelWidth(width) - 24,
                StarRailUiStyle.panelTop(height) + 4));
    }

    protected boolean showGlobalClose() { return true; }

    /** Scale from this screen's responsive logical coordinates to framebuffer pixels. */
    protected final double uiScaleFactor() { return uiScale; }

    @Override
    public final void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        scrollPanels.forEach(StarRailSmoothScroll::hideBar);
        graphics.pose().pushPose();
        graphics.pose().scale((float) uiScale, (float) uiScale, 1F);
        try {
            renderPage(graphics, (int) (mouseX / uiScale), (int) (mouseY / uiScale), partialTick);
        } finally { graphics.pose().popPose(); }
    }

    protected void renderPage(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    protected final void clip(GuiGraphics graphics, int left, int top, int right, int bottom) {
        graphics.enableScissor((int) Math.floor(left * uiScale), (int) Math.floor(top * uiScale),
                (int) Math.ceil(right * uiScale), (int) Math.ceil(bottom * uiScale));
    }

    protected final void renderScrollBar(GuiGraphics graphics, StarRailSmoothScroll scroll,
            int x, int top, int bottom, int contentHeight, int mouseX, int mouseY) {
        if (!scrollPanels.contains(scroll)) scrollPanels.add(scroll);
        scroll.renderBar(graphics, x, top, bottom, contentHeight, mouseX, mouseY);
    }

    @Override
    public final boolean mouseClicked(double x, double y, int button) {
        if (button == 0) {
            for (StarRailSmoothScroll scroll : scrollPanels) {
                if (scroll.press(x / uiScale, y / uiScale)) return true;
            }
        }
        return logicalMouseClicked(x / uiScale, y / uiScale, button);
    }
    protected boolean logicalMouseClicked(double x, double y, int button) {
        return super.mouseClicked(x, y, button);
    }
    @Override
    public final boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (button == 0) {
            for (StarRailSmoothScroll scroll : scrollPanels) {
                if (scroll.drag(y / uiScale)) return true;
            }
        }
        return logicalMouseDragged(x / uiScale, y / uiScale, button, dx / uiScale, dy / uiScale);
    }
    protected boolean logicalMouseDragged(double x, double y, int button, double dx, double dy) {
        return super.mouseDragged(x, y, button, dx, dy);
    }
    @Override
    public final boolean mouseReleased(double x, double y, int button) {
        if (button == 0) {
            boolean handled = false;
            for (StarRailSmoothScroll scroll : scrollPanels) handled |= scroll.release();
            if (handled) return true;
        }
        return logicalMouseReleased(x / uiScale, y / uiScale, button);
    }
    protected boolean logicalMouseReleased(double x, double y, int button) {
        return super.mouseReleased(x, y, button);
    }
    @Override
    public final boolean mouseScrolled(double x, double y, double amount) {
        return logicalMouseScrolled(x / uiScale, y / uiScale, amount);
    }
    protected boolean logicalMouseScrolled(double x, double y, double amount) {
        return super.mouseScrolled(x, y, amount);
    }
    @Override
    public void mouseMoved(double x, double y) { super.mouseMoved(x / uiScale, y / uiScale); }
    @Override
    public boolean isPauseScreen() { return false; }
}
