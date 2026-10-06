package com.starrail.sim.client;

import net.minecraft.client.gui.GuiGraphics;

/** Frame-time based, bounded scrolling shared by text panels and modal lists. */
public final class StarRailSmoothScroll {
    private double position;
    private double target;
    private double maximum;
    private long lastFrame;
    private boolean barVisible;
    private boolean dragging;
    private int barX, barTop, barBottom, thumbHeight;
    private double dragOffset;


    public double position() { return position; }

    public void reset() {
        position = target = maximum = 0;
        lastFrame = 0;
        cancelInteraction();
    }

    public void bounds(double maximum) {
        this.maximum = Math.max(0, maximum);
        target = clamp(target);
        position = clamp(position);
    }

    public void wheel(double amount, double step) {
        if (dragging) return;
        // Reverse immediately from the visible position when changing direction.
        double delta = -amount * step;
        if ((target - position) * delta < 0) target = position;
        target = clamp(target + delta);
    }

    public void advance() {
        long now = System.nanoTime();
        double elapsed = lastFrame == 0 ? 0 : Math.min(.1, (now - lastFrame) / 1_000_000_000.0);
        lastFrame = now;
        position += (target - position) * (1 - Math.exp(-elapsed / .055));
        if (Math.abs(target - position) < .05) position = target;
        position = clamp(position);
    }

    void hideBar() { barVisible = false; }

    void cancelInteraction() {
        dragging = false;
        barVisible = false;
    }

    void renderBar(GuiGraphics graphics, int x, int top, int bottom, int contentHeight,
            int mouseX, int mouseY) {
        barX = x;
        barTop = top;
        barBottom = bottom;
        int available = bottom - top;
        barVisible = contentHeight > available && available > 0;
        if (!barVisible) { dragging = false; return; }
        bounds(contentHeight - available);
        thumbHeight = Math.min(available, Math.max(18, available * available / contentHeight));
        int thumbTop = (int) Math.round(thumbTop());
        boolean hovered = mouseX >= x - 5 && mouseX <= x + 6 && mouseY >= top && mouseY < bottom;
        graphics.fill(x, top, x + 1, bottom, hovered || dragging ? 0x609AAAC4 : 0x30BDC4DD);
        int halfWidth = hovered || dragging ? 2 : 1;
        graphics.fill(x - halfWidth, thumbTop, x + halfWidth + 1, thumbTop + thumbHeight,
                dragging ? StarRailUiStyle.GOLD_ACCENT : hovered ? 0xD0E7C77E : 0xA0D6C395);
    }

    boolean press(double x, double y) {
        if (!barVisible || maximum <= 0 || x < barX - 5 || x > barX + 6
                || y < barTop || y >= barBottom) return false;
        double thumbTop = thumbTop();
        dragOffset = y >= thumbTop && y < thumbTop + thumbHeight ? y - thumbTop : thumbHeight / 2.0;
        dragging = true;
        drag(y);
        return true;
    }

    boolean drag(double y) {
        if (!dragging || !barVisible) { dragging = false; return false; }
        int travel = barBottom - barTop - thumbHeight;
        position = target = travel <= 0 ? 0 : clamp((y - barTop - dragOffset) / travel * maximum);
        lastFrame = System.nanoTime();
        return true;
    }

    boolean release() {
        boolean wasDragging = dragging;
        dragging = false;
        return wasDragging;
    }

    private double thumbTop() {
        return barTop + (barBottom - barTop - thumbHeight) * position / Math.max(1, maximum);
    }

    private double clamp(double value) { return Math.max(0, Math.min(maximum, value)); }
}
