package com.starrail.sim.client;

import com.starrail.sim.KitchenMenu;
import com.starrail.sim.KitchenRecipe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;

public final class KitchenScreen extends AbstractContainerScreen<KitchenMenu> {
    private static final int LIST_X = 15, LIST_Y = 95, LIST_WIDTH = 163, ROW_HEIGHT = 18, VISIBLE_ROWS = 5;
    private int selection;
    private int firstRecipe;
    private int highlightedRecipe;
    private boolean expanded;
    private Button selector;
    private Button start;

    public KitchenScreen(KitchenMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        selection = menu.initialSelection();
        imageWidth = 192;
        imageHeight = 214;
        inventoryLabelX = 16;
        inventoryLabelY = 121;
    }

    @Override protected void init() {
        super.init();
        selector = addRenderableWidget(Button.builder(Component.empty(), b -> {
            expanded = !expanded;
            firstRecipe = Math.min(selection, maxOffset(menu.recipes().size()));
            highlightedRecipe = selection;
        }).bounds(leftPos + 15, topPos + 76, 113, 18).build());
        start = addRenderableWidget(Button.builder(Component.translatable("gui.starrail_sim.kitchen.start"), b -> {
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, selection);
        }).bounds(leftPos + 133, topPos + 76, 45, 18).build());
    }

    private static int maxOffset(int size) {
        return Math.max(0, size - VISIBLE_ROWS);
    }

    private KitchenRecipe selected(List<KitchenRecipe> recipes) {
        if (recipes.isEmpty()) return null;
        selection = Math.min(selection, recipes.size() - 1);
        return recipes.get(selection);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float partial) {
        KitchenRecipe recipe = selected(menu.recipes());
        selector.active = recipe != null;
        selector.setMessage(recipe == null ? Component.literal("—") : Component.literal(
                font.plainSubstrByWidth(recipe.output().getHoverName().getString(), 92) + " ▾"));
        start.active = !expanded && recipe != null && menu.duration() == 0 && menu.matches(recipe);
        renderBackground(graphics);
        super.render(graphics, x, y, partial);
        if (expanded) {
            renderRecipes(graphics, x, y);
            return;
        }
        renderTooltip(graphics, x, y);
        if (recipe != null && isHovering(15, 76, 113, 18, x, y)) {
            var lines = new java.util.ArrayList<Component>();
            lines.add(recipe.output().getHoverName());
            lines.add(processLabel(recipe));
            for (var input : recipe.inputs()) lines.add(Component.literal(input.getCount() + " × ").append(input.getHoverName()));
            graphics.renderComponentTooltip(font, lines, x, y);
        }
    }

    private void renderRecipes(GuiGraphics graphics, int mouseX, int mouseY) {
        var recipes = menu.recipes();
        firstRecipe = Math.min(firstRecipe, maxOffset(recipes.size()));
        int rows = Math.min(VISIBLE_ROWS, recipes.size());
        int x = leftPos + LIST_X, y = topPos + LIST_Y;
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        graphics.fill(x - 1, y - 1, x + LIST_WIDTH + 1, y + rows * ROW_HEIGHT + 1, 0xFF625C52);
        for (int row = 0; row < rows; row++) {
            int index = firstRecipe + row;
            int rowY = y + row * ROW_HEIGHT;
            boolean hovered = mouseX >= x && mouseX < x + LIST_WIDTH - 4 && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            graphics.fill(x, rowY, x + LIST_WIDTH, rowY + ROW_HEIGHT,
                    hovered ? 0xFFCDBF99 : index == highlightedRecipe ? 0xFFD8CDAF : 0xFFF0EADD);
            var recipe = recipes.get(index);
            graphics.renderItem(recipe.output(), x + 2, rowY + 1);
            graphics.drawString(font, font.plainSubstrByWidth(recipe.output().getHoverName().getString(), LIST_WIDTH - 25),
                    x + 20, rowY + 5, 0x303030, false);
        }
        if (recipes.size() > VISIBLE_ROWS) {
            int track = rows * ROW_HEIGHT;
            int thumb = Math.max(8, track * rows / recipes.size());
            int top = y + firstRecipe * (track - thumb) / maxOffset(recipes.size());
            graphics.fill(x + LIST_WIDTH - 4, y, x + LIST_WIDTH, y + track, 0xFFB1A897);
            graphics.fill(x + LIST_WIDTH - 4, top, x + LIST_WIDTH, top + thumb, 0xFF625C52);
        }
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (!expanded) return super.mouseClicked(x, y, button);
        var recipes = menu.recipes();
        int row = (int) Math.floor((y - topPos - LIST_Y) / ROW_HEIGHT);
        if (button == 0 && x >= leftPos + LIST_X && x < leftPos + LIST_X + LIST_WIDTH - 4
                && row >= 0 && row < VISIBLE_ROWS && firstRecipe + row < recipes.size()) {
            selection = firstRecipe + row;
        }
        expanded = false;
        return true;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double delta) {
        if (!expanded) return super.mouseScrolled(x, y, delta);
        firstRecipe = Math.max(0, Math.min(maxOffset(menu.recipes().size()), firstRecipe - (int) Math.signum(delta)));
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (!expanded) return super.keyPressed(key, scanCode, modifiers);
        int size = menu.recipes().size();
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            if (size > 0) selection = Math.min(highlightedRecipe, size - 1);
            expanded = false;
        } else if (key == GLFW.GLFW_KEY_ESCAPE || size == 0) {
            expanded = false;
        } else if (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN) {
            int direction = key == GLFW.GLFW_KEY_UP ? -1 : 1;
            highlightedRecipe = Math.max(0, Math.min(size - 1, highlightedRecipe + direction));
            if (highlightedRecipe < firstRecipe) firstRecipe = highlightedRecipe;
            if (highlightedRecipe >= firstRecipe + VISIBLE_ROWS) firstRecipe = highlightedRecipe - VISIBLE_ROWS + 1;
        }
        return true;
    }

    private static Component processLabel(KitchenRecipe recipe) {
        return Component.translatable("gui.starrail_sim.kitchen." + recipe.process().name().toLowerCase(Locale.ROOT));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, Component.translatable("gui.starrail_sim.kitchen.ingredients"), 16, 21, 0x303030, false);
        graphics.drawString(font, Component.translatable("gui.starrail_sim.kitchen.tool"), 82, 21, 0x303030, false);
        graphics.drawString(font, Component.translatable("gui.starrail_sim.kitchen.output"), 140, 21, 0x303030, false);
        KitchenRecipe recipe = selected(menu.recipes());
        if (recipe != null) {
            graphics.drawString(font, processLabel(recipe), 16, 100, 0x303030, false);
        }
        graphics.drawString(font, Component.translatable("gui.starrail_sim.kitchen.fuel", menu.fuel()), 104, 100, 0x303030, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partial, int x, int y) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFE1DBCB);
        for (SlotPosition slot : slotPositions()) {
            int sx = leftPos + slot.x, sy = topPos + slot.y;
            graphics.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF625C52);
            graphics.fill(sx, sy, sx + 16, sy + 16, 0xFFA9A295);
        }
        int length = menu.duration() <= 0 ? 0 : menu.progress() * 26 / menu.duration();
        graphics.fill(leftPos + 109, topPos + 38, leftPos + 135, topPos + 43, 0xFF625C52);
        graphics.fill(leftPos + 109, topPos + 38, leftPos + 109 + length, topPos + 43, 0xFFC8782D);
    }

    private List<SlotPosition> slotPositions() {
        return menu.slots.stream().map(slot -> new SlotPosition(slot.x, slot.y)).toList();
    }

    private record SlotPosition(int x, int y) {}
}
