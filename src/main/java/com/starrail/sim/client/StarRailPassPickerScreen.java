package com.starrail.sim.client;

import com.starrail.sim.StarRailLightConePools;
import com.starrail.sim.StarRailNetwork;
import com.starrail.sim.StarRailPassSelectionPacket;
import com.starrail.sim.StarRailPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Selection screen shown when a player uses a Star Rail Special Pass. */
public final class StarRailPassPickerScreen extends Screen {
    private static final int COLUMNS = 2;
    private static final int CARD_HEIGHT = 30;
    private static final int CARD_STEP = 36;

    private final StarRailPath path;
    private final InteractionHand hand;
    private final List<Item> lightCones;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;
    private int listTop;
    private int listBottom;
    private int cardWidth;
    private int scrollRow;

    public StarRailPassPickerScreen(StarRailPath path, InteractionHand hand) {
        super(Component.translatable("screen.starrail_sim.pass_picker.title",
                Component.translatable("path.starrail_sim." + path.getId())));
        this.path = path;
        this.hand = hand;
        this.lightCones = StarRailLightConePools.forPath(path);
    }

    @Override
    protected void init() {
        panelWidth = Math.min(560, Math.max(220, width - 20));
        panelHeight = Math.min(390, Math.max(180, height - 20));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        listTop = panelTop + 44;
        listBottom = panelTop + panelHeight - 36;
        cardWidth = (panelWidth - 40) / COLUMNS;
        scrollRow = Math.min(scrollRow, maxScrollRow());

        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(panelLeft + (panelWidth - 100) / 2,
                        panelTop + panelHeight - 27, 100, 18)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight,
                0xF0181B24);
        graphics.renderOutline(panelLeft, panelTop, panelWidth, panelHeight, 0xFF8BBCC8);
        graphics.drawCenteredString(font, title, width / 2, panelTop + 12, 0xFFF3F4F7);

        String hint = Component.translatable("screen.starrail_sim.pass_picker.hint").getString();
        graphics.drawCenteredString(font, hint, width / 2, panelTop + 28, 0xFFB9C0CC);

        if (lightCones.isEmpty()) {
            graphics.drawCenteredString(font,
                    Component.translatable("screen.starrail_sim.pass_picker.empty"),
                    width / 2, (listTop + listBottom) / 2 - 4, 0xFFDCDEE5);
        } else {
            int visibleRows = visibleRows();
            int firstIndex = scrollRow * COLUMNS;
            int lastIndex = Math.min(lightCones.size(), (scrollRow + visibleRows) * COLUMNS);
            ItemStack hovered = ItemStack.EMPTY;
            for (int index = firstIndex; index < lastIndex; index++) {
                int row = index / COLUMNS - scrollRow;
                int column = index % COLUMNS;
                int x = panelLeft + 16 + column * (cardWidth + 8);
                int y = listTop + row * CARD_STEP;
                boolean isHovered = mouseX >= x && mouseX < x + cardWidth
                        && mouseY >= y && mouseY < y + CARD_HEIGHT;
                graphics.fill(x, y, x + cardWidth, y + CARD_HEIGHT,
                        isHovered ? 0xFF38414F : 0xFF282E39);
                graphics.renderOutline(x, y, cardWidth, CARD_HEIGHT,
                        isHovered ? 0xFF8BBCC8 : 0xFF4E5A68);

                ItemStack stack = new ItemStack(lightCones.get(index));
                graphics.renderItem(stack, x + 6, y + 6);
                String name = font.plainSubstrByWidth(stack.getHoverName().getString(), cardWidth - 34);
                graphics.drawString(font, name, x + 28, y + 11, 0xFFF0F1F5, false);
                if (isHovered) {
                    hovered = stack;
                }
            }
            if (!hovered.isEmpty()) {
                graphics.renderTooltip(font, hovered, mouseX, mouseY);
            }
            if (maxScrollRow() > 0) {
                graphics.drawString(font,
                        Component.translatable("screen.starrail_sim.pass_picker.scroll"),
                        panelLeft + panelWidth - font.width(Component.translatable(
                                "screen.starrail_sim.pass_picker.scroll")) - 14,
                        panelTop + panelHeight - 22, 0xFFB9C0CC, false);
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseY >= listTop && mouseY < listBottom) {
            int columnStep = cardWidth + 8;
            int relativeX = (int) mouseX - (panelLeft + 16);
            if (relativeX >= 0 && relativeX < COLUMNS * columnStep) {
                int column = relativeX / columnStep;
                int withinCardX = relativeX % columnStep;
                if (column < COLUMNS && withinCardX < cardWidth) {
                    int visibleRow = ((int) mouseY - listTop) / CARD_STEP;
                    int withinCardY = ((int) mouseY - listTop) % CARD_STEP;
                    int index = (scrollRow + visibleRow) * COLUMNS + column;
                    if (withinCardY < CARD_HEIGHT && index >= 0 && index < lightCones.size()) {
                        ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS
                                .getKey(lightCones.get(index));
                        if (itemId != null) {
                            StarRailNetwork.CHANNEL.sendToServer(
                                    new StarRailPassSelectionPacket(path, hand, itemId));
                            onClose();
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= panelLeft && mouseX < panelLeft + panelWidth
                && mouseY >= listTop && mouseY < listBottom && maxScrollRow() > 0) {
            scrollRow = Math.max(0, Math.min(maxScrollRow(),
                    scrollRow - (int) Math.signum(delta)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private int visibleRows() {
        return Math.max(1, (listBottom - listTop) / CARD_STEP);
    }

    private int maxScrollRow() {
        int totalRows = (lightCones.size() + COLUMNS - 1) / COLUMNS;
        return Math.max(0, totalRows - visibleRows());
    }
}
