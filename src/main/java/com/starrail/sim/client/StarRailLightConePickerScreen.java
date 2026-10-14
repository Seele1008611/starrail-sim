package com.starrail.sim.client;

import com.starrail.sim.LightConeEquipPacket;
import com.starrail.sim.StarRailNetwork;
import com.starrail.sim.StarRailPath;
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
    private static final int FILTER_HEIGHT = 18;
    private static final int FILTER_OPTION_HEIGHT = 18;
    private static final int TEXT = 0xFF303441;
    private static final int MUTED = 0xFF626978;

    private final List<Candidate> candidates = new ArrayList<>();
    private final StarRailSmoothScroll inventoryScroll = new StarRailSmoothScroll();
    private EditBox search;
    private String query = "";
    private String pathFilter = "";
    private boolean pathMenuOpen;
    private int listLeft, listTop, listRight, listBottom, tileWidth, pathFilterTop;

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
        pathFilterTop = bodyTop + 25;
        listTop = bodyTop + 49;
        listBottom = modalBottom - 36;
        tileWidth = Math.max(1, (listRight - listLeft - 8) / COLUMNS);
        inventoryScroll.reset();
        pathMenuOpen = false;
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
            candidates.add(new Candidate(slot, stack.copy(), itemId, superimposition, pathFor(itemId)));
        }
    }

    private List<Candidate> filteredCandidates() {
        String normalized = query.strip().toLowerCase(Locale.ROOT);
        return candidates.stream()
                .filter(candidate -> pathFilter.isEmpty() || pathFilter.equals(candidate.pathId()))
                .filter(candidate -> normalized.isEmpty()
                        || candidate.stack().getHoverName().getString().toLowerCase(Locale.ROOT).contains(normalized)
                        || candidate.itemId().toString().toLowerCase(Locale.ROOT).contains(normalized))
                .toList();
    }

    /** Reads the Path named by the cone's existing localized effect header. */
    private static String pathFor(ResourceLocation itemId) {
        if (!"starrail_sim".equals(itemId.getNamespace())) return "other";
        String header = Component.translatable("tooltip.starrail_sim."
                + itemId.getPath() + ".special_header").getString();
        for (StarRailPath path : StarRailPath.values()) {
            if (path.isRealPath() && header.contains(Component.translatable(
                    "path.starrail_sim." + path.getId()).getString())) {
                return path.getId();
            }
        }
        return "other";
    }

    private List<String> pathOptions() {
        List<String> options = new ArrayList<>();
        options.add("");
        for (StarRailPath path : StarRailPath.values()) {
            if (path.isRealPath()) options.add(path.getId());
        }
        options.add("other");
        return options;
    }

    private Component pathLabel(String pathId) {
        if (pathId.isEmpty()) {
            return Component.translatable("screen.starrail_sim.light_cone.picker.path.all");
        }
        if (pathId.equals("other")) {
            return Component.translatable("screen.starrail_sim.light_cone.picker.path.other");
        }
        return Component.translatable("path.starrail_sim." + pathId);
    }

    private int pathMenuBottom() {
        return Math.min(pathFilterTop + FILTER_HEIGHT + pathOptions().size() * FILTER_OPTION_HEIGHT,
                modalBottom - 32);
    }

    private boolean insidePathFilter(double x, double y) {
        return x >= listLeft && x < listRight && y >= pathFilterTop
                && y < pathFilterTop + FILTER_HEIGHT;
    }

    private void renderPathFilter(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 300.0F);
        try {
            graphics.fill(listLeft, pathFilterTop, listRight, pathFilterTop + FILTER_HEIGHT, 0xFFF0F1F4);
            graphics.renderOutline(listLeft, pathFilterTop, listRight - listLeft, FILTER_HEIGHT,
                    pathMenuOpen ? StarRailUiStyle.CYAN_ACCENT : 0x40707785);
            graphics.drawString(font, pathLabel(pathFilter), listLeft + 7, pathFilterTop + 5, TEXT, false);
            graphics.drawString(font, pathMenuOpen ? "▴" : "▾", listRight - 14, pathFilterTop + 4,
                    StarRailUiStyle.CYAN_ACCENT, false);

            if (pathMenuOpen) {
                List<String> options = pathOptions();
                int menuTop = pathFilterTop + FILTER_HEIGHT;
                int menuBottom = pathMenuBottom();
                graphics.fill(listLeft, menuTop, listRight, menuBottom, 0xFFF0F1F4);
                graphics.renderOutline(listLeft, menuTop, listRight - listLeft, menuBottom - menuTop,
                        0x80707785);
                clip(graphics, listLeft, menuTop, listRight, menuBottom);
                for (int index = 0; index < options.size(); index++) {
                    int y = menuTop + index * FILTER_OPTION_HEIGHT;
                    if (y >= menuBottom) break;
                    boolean selected = options.get(index).equals(pathFilter);
                    boolean hovered = mouseX >= listLeft && mouseX < listRight
                            && mouseY >= y && mouseY < y + FILTER_OPTION_HEIGHT;
                    if (selected || hovered) {
                        graphics.fill(listLeft + 1, y, listRight - 1,
                                Math.min(y + FILTER_OPTION_HEIGHT, menuBottom),
                                selected ? 0xFFD8E9EE : 0xFFE3E8ED);
                    }
                    graphics.drawString(font, pathLabel(options.get(index)), listLeft + 7, y + 5,
                            selected ? StarRailUiStyle.CYAN_ACCENT : TEXT, false);
                }
                graphics.disableScissor();
            }
        } finally {
            graphics.flush();
            graphics.pose().popPose();
        }
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
            Component empty = Component.translatable(candidates.isEmpty()
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
        renderPathFilter(graphics, mouseX, mouseY);
    }

    @Override
    protected boolean overlayMouseClicked(double x, double y, int button) {
        if (button != 0) return false;
        if (pathMenuOpen) {
            int menuTop = pathFilterTop + FILTER_HEIGHT;
            int menuBottom = pathMenuBottom();
            if (x >= listLeft && x < listRight && y >= menuTop && y < menuBottom) {
                int index = (int) (y - menuTop) / FILTER_OPTION_HEIGHT;
                List<String> options = pathOptions();
                if (index >= 0 && index < options.size()) {
                    pathFilter = options.get(index);
                    inventoryScroll.reset();
                }
                pathMenuOpen = false;
                return true;
            }
            pathMenuOpen = false;
            if (insidePathFilter(x, y)) pathMenuOpen = true;
            return true;
        }
        if (insidePathFilter(x, y)) {
            pathMenuOpen = true;
            return true;
        }
        return false;
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
        if (pathMenuOpen) return true;
        if (x >= listLeft && x <= listRight && y >= listTop && y < listBottom) {
            inventoryScroll.wheel(amount, ROW_STEP);
            return true;
        }
        return super.logicalMouseScrolled(x, y, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (pathMenuOpen && keyCode == 256) {
            pathMenuOpen = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private record Candidate(int inventorySlot, ItemStack stack, ResourceLocation itemId,
                             int superimposition, String pathId) { }
}
