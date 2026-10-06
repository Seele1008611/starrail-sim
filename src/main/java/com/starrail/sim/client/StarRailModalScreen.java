package com.starrail.sim.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Native modal: the underlying scene stays visible, while only this screen receives input. */
abstract class StarRailModalScreen extends StarRailStyledScreen {
    protected static final int INK = 0xFF303441;
    protected static final int SECONDARY_INK = 0xFF626978;
    protected final StarRailStyledScreen parent;
    protected int modalLeft, modalRight, modalTop, modalBottom, bodyTop;
    private long openedAt;

    StarRailModalScreen(Component title, StarRailStyledScreen parent) {
        super(title);
        this.parent = parent;
    }

    protected int preferredWidth() { return 500; }
    protected int preferredHeight() { return 350; }
    @Override protected boolean showGlobalClose() { return false; }

    @Override
    protected void init() {
        // Both screens share the same logical canvas after a window/GUI-scale change.
        parent.resize(minecraft, width, height);
        super.init();
        int w = Math.min(preferredWidth(), width - 60);
        int h = Math.min(preferredHeight(), height - 60);
        modalLeft = (width - w) / 2;
        modalRight = modalLeft + w;
        modalTop = (height - h) / 2;
        modalBottom = modalTop + h;
        bodyTop = modalTop + 43;
        openedAt = System.nanoTime();
        addRenderableWidget(StarRailUiStyle.modalCloseButton(ignored -> onClose(), modalRight - 33, modalTop + 10));
    }

    @Override
    protected final void renderPage(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        parent.renderPage(graphics, -1000, -1000, partialTick);
        graphics.flush();
        graphics.pose().pushPose();
        // Player and cone renderers use GUI depth; lift the whole modal above that scene.
        graphics.pose().translate(0, 0, 500);
        double entered = Math.min(1, (System.nanoTime() - openedAt) / 200_000_000.0);
        graphics.fill(0, 0, width, height, (int) (110 * entered) << 24 | 0x050812);
        graphics.fill(modalLeft + 4, modalTop + 5, modalRight + 4, modalBottom + 5, 0x55000000);
        graphics.fill(modalLeft, modalTop, modalRight, modalBottom, 0xFFE0E1E7);
        graphics.drawString(font, title, modalLeft + 17, modalTop + 17, INK, false);
        graphics.fill(modalLeft + 16, modalTop + 37, modalRight - 16, modalTop + 38, 0x40707785);
        renderModal(graphics, mouseX, mouseY, partialTick);
        super.renderPage(graphics, mouseX, mouseY, partialTick);
        graphics.flush();
        graphics.pose().popPose();
    }

    protected abstract void renderModal(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);

    @Override
    protected boolean logicalMouseClicked(double x, double y, int button) {
        if (button == 0 && (x < modalLeft || x > modalRight || y < modalTop || y > modalBottom)) {
            onClose();
            return true;
        }
        super.logicalMouseClicked(x, y, button);
        return true; // Never forward a modal click to the underlying navigation.
    }

    @Override public void onClose() { minecraft.setScreen(parent); }
}
