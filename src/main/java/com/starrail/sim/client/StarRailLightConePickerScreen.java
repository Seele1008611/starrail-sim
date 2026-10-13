package com.starrail.sim.client;

import com.starrail.sim.LightConeEquipPacket;
import com.starrail.sim.StarRailNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Inventory-backed picker for replacing the currently equipped light cone. */
final class StarRailLightConePickerScreen extends StarRailModalScreen {
    private static final TagKey<Item> LIGHT_CONES = ItemTags.create(
            new ResourceLocation("curios", "light_cone"));
    private static final int COLUMNS = 2;
    private static final int TILE_HEIGHT = 38;
    private static final int ROW_STEP = 44;
    private static final int TEXT = 0xFF303441;
    private static final int MUTED = 0xFF626978;

    private final List<Candidate> candidates = new ArrayList<>();
    private final StarRailSmoothScroll inventoryScroll = new StarRailSmoothScroll();
    private EditBox search;
    private String query = "";
    private int listLeft, listTop, listRight, listBottom, tileWidth;

    StarRailLightConePickerScreen(StarRailLightConeScreen parent) {
        super(Component.translatable("screen.starrail_sim.light_cone.picker.title"), parent);
    }

    @Override protected int preferredWidth() { return 620; }
    @Override protected int preferredHeight() { return 380; }

    @Override
    protected void init() {
        super.init();
        listLeft = modalLeft + 18;
        listRight = modalRight - 28;
        listTop = bodyTop + 28;
        listBottom = modalBottom - 36;
        tileWidth = Math.max(1, (listRight - listLeft - 8) / COLUMNS);
        inventoryScroll.reset();
        refreshCandidates();

        search = new EditBox(font, listLeft, bodyTop + 3, listRight - listLeft, 18,
                Component.translatable("screen.starrail_sim.light_cone.picker.search"));
        search.setMaxLength(64);
        search.setHint(Component.translatable("screen.starrail_sim.light_cone.picker.search"));
        search.setResponder(value -> {
            query = value;
            inventoryScroll.reset();
        });
        addRenderableWidget(search);

        Button unequip = StarRailUiStyle.outlinedButton(
                Component.translatable("screen.starrail_sim.light_cone.picker.unequip"),
                ignored -> {
                    StarRailNetwork.CHANNEL.sendToServer(new LightConeEquipPacket(-1, null, 0));
                    onClose();
                }, listLeft, modalBottom - 27, 126, 18);
        unequip.active = !StarRailLightConeClientData.findEquipped(Minecraft.getInstance().player).isEmpty();
        addRenderableWidget(unequip);
    }

    private void refreshCandidates() {
        candidates.clear();
        if (Minecraft.getInstance().player == null) return;
        var inventory = Minecraft.getInstance().player.getInventory();
        for (int slot = 0; slot < inventory.items.size(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || !stack.is(LIGHT_CONES)) continue;
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            int superimposition = stack.hasTag() ? stack.getTag().getInt("Superimposition") : 0;
            candidates.add(new Candidate(slot, stack.copy(), itemId, superimposition));
        }
    }

    private List<Candidate> filteredCandidates() {
        String normalized = query.strip().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) return List.copyOf(candidates);
        return candidates.stream().filter(candidate -> candidate.stack().getHoverName().getString()
                .toLowerCase(Locale.ROOT).contains(normalized)
                || candidate.itemId().toString().toLowerCase(Locale.ROOT).contains(normalized)).toList();
    }

    @Override
    protected void renderModal(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        List<Candidate> shown = filteredCandidates();
        int rowCount = (shown.size() + COLUMNS - 1) / COLUMNS;
        int contentHeight = rowCount * ROW_STEP;
        inventoryScroll.advance();
        inventoryScroll.bounds(Math.max(0, contentHeight - (listBottom - listTop)));

        clip(graphics, listLeft, listTop, listRight, listBottom);
        if (shown.isEmpty()) {
            Component empty = Component.translatable(query.isBlank()
                    ? "screen.starrail_sim.light_cone.picker.empty"
                    : "screen.starrail_sim.light_cone.picker.no_match");
            graphics.drawCenteredString(font, empty, (listLeft + listRight) / 2,
                    (listTop + listBottom) / 2 - 4, MUTED);
        } else {
            int offset = (int) inventoryScroll.position();
            for (int index = 0; index < shown.size(); index++) {
                Candidate candidate = shown.get(index);
                int x = listLeft + index % COLUMNS * (tileWidth + 8);
                int y = listTop + index / COLUMNS * ROW_STEP - offset;
                if (y + TILE_HEIGHT <= listTop || y >= listBottom) continue;
                boolean hovered = mouseX >= x && mouseX < x + tileWidth
                        && mouseY >= y && mouseY < y + TILE_HEIGHT;
                graphics.fill(x, y, x + tileWidth, y + TILE_HEIGHT,
                        hovered ? 0xFFE2E8EF : 0xFFF0F1F4);
                graphics.renderOutline(x, y, tileWidth, TILE_HEIGHT,
                        hovered ? StarRailUiStyle.CYAN_ACCENT : 0x40707785);
                graphics.renderItem(candidate.stack(), x + 7, y + 10);
                String name = candidate.stack().getHoverName().getString();
                name = font.plainSubstrByWidth(name, tileWidth - 43);
                graphics.drawString(font, name, x + 30, y + 6, TEXT, false);
                int level = Math.max(1, Math.min(5, candidate.superimposition() == 0
                        ? 1 : candidate.superimposition()));
                graphics.drawString(font,
                        Component.translatable("screen.starrail_sim.light_cone.picker.superimposition", level),
                        x + 30, y + 21, MUTED, false);
            }
        }
        graphics.disableScissor();
        renderScrollBar(graphics, inventoryScroll, listRight + 6, listTop, listBottom,
                contentHeight, mouseX, mouseY);
        graphics.drawString(font,
                Component.translatable("screen.starrail_sim.light_cone.picker.hint"),
                listLeft + 136, modalBottom - 21, MUTED, false);
    }

    @Override
    protected boolean logicalMouseClicked(double x, double y, int button) {
        if (button == 0 && x >= listLeft && x < listRight && y >= listTop && y < listBottom) {
            List<Candidate> shown = filteredCandidates();
            int column = (int) (x - listLeft) / (tileWidth + 8);
            int row = (int) ((y - listTop + inventoryScroll.position()) / ROW_STEP);
            int tileX = listLeft + column * (tileWidth + 8);
            int tileY = listTop + row * ROW_STEP - (int) inventoryScroll.position();
            int index = row * COLUMNS + column;
            if (column < COLUMNS && x < tileX + tileWidth && y < tileY + TILE_HEIGHT
                    && index >= 0 && index < shown.size()) {
                Candidate candidate = shown.get(index);
                StarRailNetwork.CHANNEL.sendToServer(new LightConeEquipPacket(candidate.inventorySlot(),
                        candidate.itemId(), candidate.superimposition()));
                onClose();
                return true;
            }
        }
        return super.logicalMouseClicked(x, y, button);
    }

    @Override
    protected boolean logicalMouseScrolled(double x, double y, double amount) {
        if (x >= listLeft && x <= listRight && y >= listTop && y < listBottom) {
            inventoryScroll.wheel(amount, ROW_STEP);
            return true;
        }
        return super.logicalMouseScrolled(x, y, amount);
    }

    private record Candidate(int inventorySlot, ItemStack stack, ResourceLocation itemId,
                             int superimposition) { }
}
