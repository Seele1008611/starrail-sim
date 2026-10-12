package com.starrail.sim.client;

import com.starrail.sim.RelicTrialMenu;
import com.starrail.sim.RelicTrialSet;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/** Compact in-game selector that previews the current set and starts a test fight. */
public final class RelicTrialScreen extends AbstractContainerScreen<RelicTrialMenu> {
    private final List<Button> setButtons = new ArrayList<>();
    private Button startButton;

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
        setButtons.clear();
        RelicTrialSet[] sets = RelicTrialSet.values();
        for (int index = 0; index < sets.length; index++) {
            final int selected = index;
            int column = index % 2;
            int row = index / 2;
            Button button = StarRailUiStyle.button(setButtonLabel(sets[index]),
                    ignored -> click(selected), leftPos + 16 + column * 140,
                    topPos + 75 + row * 31, 134, 27);
            setButtons.add(button);
            addRenderableWidget(button);
        }
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

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshButtons();
    }

    private void refreshButtons() {
        for (int index = 0; index < setButtons.size(); index++) {
            setButtons.get(index).active = !menu.battleActive();
            StarRailUiStyle.setSelected(setButtons.get(index), index == menu.selectedSetIndex());
            setButtons.get(index).setMessage(setButtonLabel(RelicTrialSet.values()[index]));
        }
        if (startButton != null) {
            startButton.active = true;
            String key = menu.battleActive() ? "ui.starrail_sim.arena.cancel"
                    : menu.battleComplete() ? "ui.starrail_sim.arena.retry"
                    : "ui.starrail_sim.arena.start";
            startButton.setMessage(Component.translatable(key));
            StarRailUiStyle.setSelected(startButton, !menu.battleActive());
        }
    }

    private static Component setButtonLabel(RelicTrialSet set) {
        return Component.translatable("ui.starrail_sim.arena.set_card", set.displayName(),
                Component.translatable(set.inner() ? "ui.starrail_sim.arena.set_inner"
                        : "ui.starrail_sim.arena.set_outer"));
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
        graphics.drawString(font, Component.translatable("ui.starrail_sim.arena.selected_set",
                RelicTrialSet.byIndex(menu.selectedSetIndex()).displayName()),
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
