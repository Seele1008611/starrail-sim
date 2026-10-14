package com.starrail.sim.client;

import com.starrail.sim.RelicTrialMenu;
import com.starrail.sim.RelicTrialSet;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

/** Compact in-game selector that previews the current set and starts a test fight. */
public final class RelicTrialScreen extends AbstractContainerScreen<RelicTrialMenu> {
    private static final int PICKER_ROW_HEIGHT = 19;
    private static final int PICKER_VISIBLE_ROWS = 7;
    private static final double PICKER_OPEN_SECONDS = .26;
    private final double[] pickerHover = new double[RelicTrialSet.values().length];
    private Button setSelector;
    private Button startButton;
    private boolean setListOpen;
    private long setListOpenedAt;
    private long pickerFrame;
    private int pickerFocused;
    private int pickerScrollOffset;

    public RelicTrialScreen(RelicTrialMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 310;
        this.imageHeight = 224;
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();
        font = StarRailUiFonts.forMod();
        setListOpen = false;
        setSelector = StarRailUiStyle.button(setSelectorLabel(), ignored -> toggleSetList(),
                leftPos + 16, topPos + 75, imageWidth - 32, 27);
        StarRailUiStyle.setSelected(setSelector, true);
        addRenderableWidget(setSelector);
        startButton = StarRailUiStyle.button(Component.translatable("ui.starrail_sim.arena.start"),
                        ignored -> click(menu.battleActive()
                                ? RelicTrialMenu.BUTTON_ABORT : RelicTrialMenu.BUTTON_START),
                leftPos + 16, topPos + 183, imageWidth - 32, 24);
        addRenderableWidget(startButton);
        refreshButtons();
    }

    private void click(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private void toggleSetList() {
        if (menu.battleActive()) return;
        setListOpen = !setListOpen;
        setListOpenedAt = System.nanoTime();
        pickerFocused = menu.selectedSetIndex();
        keepFocusedSetVisible();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshButtons();
    }

    private void refreshButtons() {
        if (setSelector != null) {
            setSelector.active = !menu.battleActive();
            setSelector.setMessage(setSelectorLabel());
            StarRailUiStyle.setSelected(setSelector, true);
        }
        if (menu.battleActive()) setListOpen = false;
        if (startButton != null) {
            startButton.active = true;
            String key = menu.battleActive() ? "ui.starrail_sim.arena.cancel"
                    : menu.battleComplete() ? "ui.starrail_sim.arena.retry"
                    : "ui.starrail_sim.arena.start";
            startButton.setMessage(Component.translatable(key));
            StarRailUiStyle.setSelected(startButton, !menu.battleActive());
        }
    }

    private Component setSelectorLabel() {
        RelicTrialSet set = RelicTrialSet.byIndex(menu.selectedSetIndex());
        return Component.translatable("ui.starrail_sim.arena.set_picker",
                set.displayName(), Component.translatable(set.inner()
                        ? "ui.starrail_sim.arena.set_inner" : "ui.starrail_sim.arena.set_outer"));
    }

    private static Component setOptionLabel(RelicTrialSet set) {
        return Component.translatable("ui.starrail_sim.arena.set_option", set.displayName(),
                Component.translatable(set.inner() ? "ui.starrail_sim.arena.set_inner"
                        : "ui.starrail_sim.arena.set_outer"));
    }

    private int visibleSetRows() {
        return Math.min(PICKER_VISIBLE_ROWS, RelicTrialSet.values().length);
    }

    private void keepFocusedSetVisible() {
        int visible = visibleSetRows();
        if (pickerFocused < pickerScrollOffset) pickerScrollOffset = pickerFocused;
        if (pickerFocused >= pickerScrollOffset + visible) {
            pickerScrollOffset = pickerFocused - visible + 1;
        }
        pickerScrollOffset = Math.max(0, Math.min(
                RelicTrialSet.values().length - visible, pickerScrollOffset));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (setListOpen && button == 0) {
            int listX = leftPos + 11, listY = topPos + 71, listWidth = imageWidth - 22;
            int listHeight = PICKER_ROW_HEIGHT * visibleSetRows();
            if (mouseX >= listX && mouseX < listX + listWidth
                    && mouseY >= listY && mouseY < listY + listHeight) {
                int index = pickerScrollOffset + (int) (mouseY - listY) / PICKER_ROW_HEIGHT;
                if (index >= 0 && index < RelicTrialSet.values().length) click(index);
                setListOpen = false;
                return true;
            }
            setListOpen = false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (setListOpen) {
            int x = leftPos + 11, y = topPos + 71, width = imageWidth - 22;
            int height = PICKER_ROW_HEIGHT * visibleSetRows();
            if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
                int maxOffset = RelicTrialSet.values().length - visibleSetRows();
                pickerScrollOffset = Math.max(0, Math.min(maxOffset,
                        pickerScrollOffset - (int) Math.signum(delta)));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (setListOpen && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            setListOpen = false;
            return true;
        }
        if (setListOpen && keyCode == GLFW.GLFW_KEY_UP) {
            pickerFocused = Math.max(0, pickerFocused - 1);
            keepFocusedSetVisible();
            return true;
        }
        if (setListOpen && keyCode == GLFW.GLFW_KEY_DOWN) {
            pickerFocused = Math.min(RelicTrialSet.values().length - 1, pickerFocused + 1);
            keepFocusedSetVisible();
            return true;
        }
        if (setListOpen && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER
                || keyCode == GLFW.GLFW_KEY_SPACE)) {
            click(pickerFocused);
            setListOpen = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (setListOpen) renderSetList(graphics, mouseX, mouseY);
    }

    private void renderSetList(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = leftPos + 11, y = topPos + 71, width = imageWidth - 22;
        int visibleRows = visibleSetRows();
        int height = PICKER_ROW_HEIGHT * visibleRows;
        long now = System.nanoTime();
        double dt = pickerFrame == 0 ? 0 : Math.min(.1, (now - pickerFrame) / 1_000_000_000D);
        pickerFrame = now;
        double age = Math.max(0, (now - setListOpenedAt) / 1_000_000_000D);
        double reveal = StarRailUiStyle.reducedMotion()?1:Math.min(1, age / PICKER_OPEN_SECONDS);
        reveal = reveal * reveal * (3 - 2 * reveal);
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 420);
        graphics.fill(x + 3, y + 3, x + width + 3, y + height + 3, 0x90000000);
        int panelFill = (int) (238 * reveal) << 24 | 0x101B2E;
        int panelBorder = (int) (210 * reveal) << 24 | 0xE7C77E;
        if (StarRailUiStyle.smoothEnabled()) {
            StarRailSmoothUi.roundedRect(graphics, x, y, width, height, 8, 1, panelFill, panelBorder);
        } else {
            graphics.fill(x, y, x + width, y + height, panelFill);
            graphics.renderOutline(x, y, width, height, panelBorder);
        }
        for (int row = 0; row < visibleRows; row++) {
            int index = pickerScrollOffset + row;
            int rowY = y + row * PICKER_ROW_HEIGHT;
            boolean hover = mouseX >= x && mouseX < x + width
                    && mouseY >= rowY && mouseY < rowY + PICKER_ROW_HEIGHT;
            if (StarRailUiStyle.reducedMotion()) pickerHover[index] = hover ? 1 : 0;
            else pickerHover[index] += ((hover ? 1 : 0) - pickerHover[index])
                    * (1 - Math.exp(-dt / .16));
            double rowAge = Math.max(0, age - row * .024);
            double rowReveal = StarRailUiStyle.reducedMotion()?1:Math.min(1, rowAge / .22);
            rowReveal = rowReveal * rowReveal * (3 - 2 * rowReveal);
            int selected = menu.selectedSetIndex() == index ? 1 : 0;
            double highlight = Math.max(Math.max(selected * .6, pickerHover[index]),
                    setListOpen && index == pickerFocused ? .45 : 0);
            if (highlight > .01) {
                int color = selected == 1 ? 0xE7C77E : 0x55D6D2;
                graphics.fill(x + 3, rowY + 1, x + width - 3, rowY + PICKER_ROW_HEIGHT - 1,
                        (int) (58 * highlight * rowReveal) << 24 | color);
            }
            if (index > 0) graphics.fill(x + 10, rowY, x + width - 10, rowY + 1,
                    (int) (42 * rowReveal) << 24 | 0x9EACC2);
            Component label = setOptionLabel(RelicTrialSet.values()[index]);
            int textColor = index == menu.selectedSetIndex() ? 0xFFE7C77E : 0xFFE4EAF2;
            String clipped = StarRailUiFonts.forMod().plainSubstrByWidth(label.getString(), width - 48);
            graphics.drawString(StarRailUiFonts.forMod(), clipped, x + 14,
                    rowY + (PICKER_ROW_HEIGHT - font.lineHeight) / 2,
                    (int) (255 * rowReveal) << 24 | textColor & 0xFFFFFF);
            if (index == menu.selectedSetIndex()) {
                graphics.drawString(StarRailUiFonts.forMod(), "✓", x + width - 18,
                        rowY + (PICKER_ROW_HEIGHT - font.lineHeight) / 2,
                        (int) (255 * rowReveal) << 24 | 0xE7C77E);
            }
        }
        if (RelicTrialSet.values().length > visibleRows) {
            int trackTop = y + 5, trackBottom = y + height - 5;
            int trackHeight = trackBottom - trackTop;
            int thumbHeight = Math.max(12, trackHeight * visibleRows / RelicTrialSet.values().length);
            int maxOffset = RelicTrialSet.values().length - visibleRows;
            int thumbTop = trackTop + (trackHeight - thumbHeight) * pickerScrollOffset / maxOffset;
            graphics.fill(x + width - 4, trackTop, x + width - 3, trackBottom, 0x5579899C);
            graphics.fill(x + width - 5, thumbTop, x + width - 2, thumbTop + thumbHeight, 0xDDE7C77E);
        }
        graphics.pose().popPose();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xEE0B1424);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 2, 0xFF56CDE0);
        graphics.fill(leftPos, topPos + 54, leftPos + imageWidth, topPos + 55, 0xFF28384D);
        graphics.fill(leftPos + 11, topPos + 70, leftPos + imageWidth - 11,
                topPos + 169, 0x66344A61);
        graphics.renderOutline(leftPos + 11, topPos + 70, imageWidth - 22, 99, 0x503E6B83);
        graphics.fill(leftPos + 11, topPos + 176, leftPos + imageWidth - 11,
                topPos + 177, 0xFF28384D);
        graphics.fill(leftPos, topPos + imageHeight - 1,
                leftPos + imageWidth, topPos + imageHeight, 0xFF28384D);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 16, 13, 0xF2C96D, false);
        graphics.drawString(font, Component.translatable("ui.starrail_sim.arena.choose_set"),
                16, 34, 0xE4EAF5, false);
        int currentRank = StarRailPathClientState.getCurrentPathRank().getLevel();
        Component rules;
        if (menu.battleActive()) {
            rules = Component.translatable("ui.starrail_sim.arena.rules", menu.challengeRank());
        } else if (currentRank <= 0) {
            rules = Component.translatable("ui.starrail_sim.arena.rules_unaligned");
        } else {
            rules = Component.translatable("ui.starrail_sim.arena.rules_preview", currentRank,
                    rewardOdds(currentRank));
        }
        graphics.drawString(font, rules, 16, 60, 0x8EDBE4, false);

        Component status;
        int statusColor;
        if (menu.battleActive()) {
            status = Component.translatable("ui.starrail_sim.arena.wave_status",
                    menu.waveIndex(), menu.aliveCount());
            statusColor = 0xFFFFA477;
        } else if (menu.battleComplete()) {
            status = Component.translatable("ui.starrail_sim.arena.completed", menu.rewardCount());
            statusColor = 0xFF9BE29E;
        } else {
            status = Component.translatable("ui.starrail_sim.arena.ready");
            statusColor = 0xFFB9C5D8;
        }
        graphics.drawString(font, status, 16, 169, statusColor, false);
    }

    private static String rewardOdds(int rank) {
        return switch (rank) {
            case 1 -> "2★50%/3★50%";
            case 2 -> "3★80%/4★20%";
            case 3 -> "3★40%/4★60%";
            case 4 -> "4★80%/5★20%";
            default -> "5★100%";
        };
    }

}
