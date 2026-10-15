package com.starrail.sim.client;

import com.starrail.sim.PathArchiveNamePacket;
import com.starrail.sim.StarRailNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Small editor opened by clicking a coordinate in the travel archive. */
public final class StarRailArchiveRenameScreen extends StarRailStyledScreen {
    private final Screen parent;
    private final String recordKey;
    private final String currentName;
    private final String defaultName;
    private int left, top, right, bottom;
    private EditBox nameInput;

    public StarRailArchiveRenameScreen(Screen parent, String recordKey,
                                       String currentName, String defaultName) {
        super(Component.translatable("screen.starrail_sim.archive_rename"));
        this.parent = parent;
        this.recordKey = recordKey;
        this.currentName = currentName == null ? "" : currentName;
        this.defaultName = defaultName == null ? "" : defaultName;
    }

    @Override
    protected void init() {
        super.init();
        left = StarRailUiStyle.panelLeft(width);
        top = StarRailUiStyle.panelTop(height);
        right = left + StarRailUiStyle.panelWidth(width);
        bottom = top + StarRailUiStyle.panelHeight(height);

        nameInput = new EditBox(font, left + 28, top + 112, right - left - 56, 24,
                Component.translatable("ui.starrail_sim.archive_rename.input"));
        nameInput.setMaxLength(32);
        nameInput.setValue(currentName);
        nameInput.setHint(Component.translatable("ui.starrail_sim.archive_rename.hint"));
        addRenderableWidget(nameInput);
        setInitialFocus(nameInput);

        int buttonY = top + 166;
        int buttonWidth = (right - left - 68) / 2;
        addRenderableWidget(StarRailUiStyle.outlinedButton(
                Component.translatable("ui.starrail_sim.archive_rename.save"),
                ignored -> save(), left + 24, buttonY, buttonWidth, 26));
        addRenderableWidget(StarRailUiStyle.outlinedButton(
                Component.translatable("gui.cancel"),
                ignored -> onClose(), left + 44 + buttonWidth, buttonY, buttonWidth, 26));
    }

    private void save() {
        StarRailNetwork.CHANNEL.sendToServer(new PathArchiveNamePacket(recordKey, nameInput.getValue()));
        minecraft.setScreen(parent);
    }

    @Override
    protected void renderPage(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        StarRailUiStyle.renderBackdrop(graphics, width, height);
        StarRailUiStyle.renderPanel(graphics, left, top, right, bottom);
        graphics.drawString(font, title, left + 24, top + 24, StarRailUiStyle.GOLD_ACCENT);
        graphics.drawString(font, Component.translatable("ui.starrail_sim.archive_rename.target",
                defaultName), left + 24, top + 62, StarRailUiStyle.VALUE_COLOR);
        graphics.drawString(font, Component.translatable("ui.starrail_sim.archive_rename.description"),
                left + 24, top + 86, StarRailUiStyle.MUTED_COLOR);
        super.renderPage(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
