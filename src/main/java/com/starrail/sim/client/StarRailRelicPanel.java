package com.starrail.sim.client;

import com.starrail.sim.StarRailRelicOperationService;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntConsumer;
import org.lwjgl.glfw.GLFW;

import static com.starrail.sim.client.StarRailRelicUiData.*;

/** Relic section of the existing character screen, not a separate navigation screen. */
final class StarRailRelicPanel {
    private enum View { EQUIPMENT, ENHANCE, SALVAGE, FORGE, REROLL }
    private static final int GOLD = StarRailUiStyle.GOLD_ACCENT, CYAN = StarRailUiStyle.CYAN_ACCENT;
    private static final int TEXT = 0xFFE4EAF2, MUTED = 0xFFA9B9CB;
    private static final int CARD_HEIGHT = 90, CARD_STEP = 96, CARD_COLUMNS = 3;
    private final StarRailCharacterScreen host;
    private StarRailRelicUiData data = StarRailRelicClientData.INSTANCE;
    private Snapshot state = data.snapshot();
    private View view = View.EQUIPMENT;
    private long selected = -1;
    private int left, top, right, bottom, centerRight, detailLeft, stageTop, stageBottom, gridTop, gridBottom;
    private int inventoryLeft, inventoryRight;
    private int partFilter = -1, setFilter = -1, starsFilter, statFilter = -1, stateFilter, sort;
    private int forgeSet, forgePart, forgeMain, enhanceStep = 3;
    private boolean onlyLocked, targetForge, targetForgeSubs, remainsMode, filtersOpen;
    private List<Stat> forgeSubstats = new ArrayList<>();
    private String query = "", notice = "";
    private double changedAt, selectedAt, flashAt = -10;
    private int flashColor = GOLD;
    private final StarRailSmoothScroll inventoryScroll = new StarRailSmoothScroll(), detailScroll = new StarRailSmoothScroll();
    private final List<Tile> cards = new ArrayList<>();
    private final EnumMap<Part, Double> slotPressed = new EnumMap<>(Part.class);
    private EditBox search;
    private ChoiceList choiceList;

    StarRailRelicPanel(StarRailCharacterScreen host) { this.host = host; }
    private Font font() { return Minecraft.getInstance().font; }
    private Entry item() { return state.find(selected); }
    private double now() { return StarRailCosmicUi.seconds(); }
    void layout(int left, int top, int right, int bottom) {
        this.left = left; this.top = top; this.right = right; this.bottom = bottom;
        // Share the character UI's 800 x 450 scale; fit columns without shrinking text.
        detailLeft = right - 149;
        inventoryRight = detailLeft - 10;
        inventoryLeft = inventoryRight - 178;
        centerRight = inventoryLeft - 10;
        stageTop = top + 52; stageBottom = bottom;
        gridTop = stageTop + (filtersOpen ? 166 : 80); gridBottom = bottom - 6;
    }
    void buildWidgets() {
        choiceList = null;
        state = data.snapshot();
        int width = centerRight - left, tabWidth = (width - 16) / 5;
        for (View tab : View.values()) {
            Button b = button(tr("view." + tab.name().toLowerCase(Locale.ROOT)), left + tab.ordinal() * (tabWidth + 4), top + 31, tabWidth, 18,
                    () -> { view = tab; changedAt = now(); host.rebuildRelicWidgets(); });
            StarRailUiStyle.setSelected(b, view == tab);
        }
        if (!FMLEnvironment.production) button(tr(data.preview() ? "leave_preview" : "enter_preview"), detailLeft, top - 21, right - detailLeft, 18, () -> {
            if (data.preview()) {
                data = StarRailRelicClientData.INSTANCE;
                StarRailRelicClientData.requestSnapshot();
            } else data = new StarRailRelicPreviewData();
            state = data.snapshot();
            selected = state.inventory().isEmpty() ? -1 : state.inventory().get(0).id();
            view = View.EQUIPMENT; clearFilters(); detailScroll.reset(); changedAt = now(); host.rebuildRelicWidgets();
        });
        if (view == View.EQUIPMENT) {
            for (Part part : Part.values()) {
                double[] p = slotPosition(part); int diameter = part.inner() ? 44 : 58;
                Tile tile = new Tile(Button.builder(Component.literal(part.label()), b -> {
                    slotPressed.put(part, now()); Entry e = state.equipped(part);
                    if (e != null) select(e); else { partFilter = part.ordinal(); inventoryScroll.reset(); host.rebuildRelicWidgets(); }
                }).bounds((int) p[0] - diameter / 2, (int) p[1] - diameter / 2, diameter, diameter), part, null);
                host.addRelicWidget(tile);
            }
            button(tr("contributions"), (left + centerRight) / 2 - 42, stageBottom - 18, 84, 16, this::showContributions);
        } else if (view == View.FORGE) buildForge();
        else buildWorkbench();
        buildFilters(); refreshCards();
        Entry i = item();
        Button equip = button(tr(i != null && state.worn(i) ? "unequip" : "equip"), detailLeft, bottom - 22, (right - detailLeft - 6) / 2, 20, () -> {
            Entry current = item(); if (current == null) return;
            Entry worn = state.equipped(current.part()); Command c = Command.item(Action.EQUIP, current.id(), 0);
            if (worn != null && worn.id() != current.id()) confirm(tr("replace"), comparison(worn, current), c); else perform(c);
        });
        equip.active = state.editable() && i != null && !i.pending();
        Button lock = button(tr(i != null && i.locked() ? "unlock" : "lock"), detailLeft + (right - detailLeft + 6) / 2, bottom - 22, (right - detailLeft - 6) / 2, 20,
                () -> { if (item() != null) perform(Command.item(Action.LOCK, selected, 0)); });
        lock.active = state.editable() && i != null && !i.pending();
    }
    private Button button(String label, int x, int y, int w, int h, Runnable action) {
        Button b = StarRailUiStyle.outlinedButton(Component.literal(label), ignored -> action.run(), x, y, w, h);
        b.setTooltip(Tooltip.create(Component.literal(label)));
        host.addRelicWidget(b); return b;
    }
    private Button selector(String label, int x, int y, int w, int h, List<String> options,
                            int selectedIndex, IntConsumer choose) {
        Button b = button(label + " ▾", x, y, w, h,
                () -> { if (!options.isEmpty()) choiceList = new ChoiceList(x, y, w, h, options, selectedIndex, choose); });
        b.active = !options.isEmpty();
        return b;
    }
    private List<String> withAll(String label, List<String> options) {
        List<String> result = new ArrayList<>(); result.add(label); result.addAll(options); return result;
    }
    private void filterChanged() { inventoryScroll.reset(); host.rebuildRelicWidgets(); }
    private void buildFilters() {
        int x = inventoryLeft + 8, w = inventoryRight - inventoryLeft - 16;
        gridTop = stageTop + (filtersOpen ? 166 : 80);
        search = new EditBox(font(), x, stageTop + 28, w, 18, Component.literal(tr("search")));
        search.setMaxLength(60); search.setValue(query); search.setHint(Component.literal(tr("search")));
        search.setResponder(text -> { query = text; inventoryScroll.reset(); refreshCards(); }); host.addRelicWidget(search);
        button(tr("filters") + (filtersOpen ? " −" : " +"), x, stageTop + 54, w, 18,
                () -> { filtersOpen = !filtersOpen; host.rebuildRelicWidgets(); });
        if (!filtersOpen) return;
        int unit = (w - 5) / 2, y = stageTop + 78;
        selector(partFilter < 0 ? tr("all_parts") : Part.values()[partFilter].label(), x, y, unit, 17,
                withAll(tr("all_parts"), java.util.Arrays.stream(Part.values()).map(Part::label).toList()), partFilter + 1,
                n -> { partFilter = n - 1; filterChanged(); });
        selector(setFilter < 0 ? tr("all_sets") : SETS.get(setFilter).label(), x + unit + 5, y, unit, 17,
                withAll(tr("all_sets"), SETS.stream().map(SetInfo::label).toList()), setFilter + 1,
                n -> { setFilter = n - 1; filterChanged(); });
        selector(starsFilter == 0 ? tr("all_stars") : tr("stars", starsFilter), x, y + 21, unit, 17,
                List.of(tr("all_stars"), tr("stars", 2), tr("stars", 3), tr("stars", 4), tr("stars", 5)), starsFilter == 0 ? 0 : starsFilter - 1,
                n -> { starsFilter = n == 0 ? 0 : n + 1; filterChanged(); });
        List<Stat> stats = mainOptions();
        selector(statFilter < 0 ? tr("all_mains") : stats.get(statFilter).label(), x + unit + 5, y + 21, unit, 17,
                withAll(tr("all_mains"), stats.stream().map(Stat::label).toList()), statFilter + 1,
                n -> { statFilter = n - 1; filterChanged(); });
        selector(tr("filter_state." + stateFilter), x, y + 42, unit, 17,
                java.util.stream.IntStream.range(0, 4).mapToObj(n -> tr("filter_state." + n)).toList(), stateFilter,
                n -> { stateFilter = n; filterChanged(); });
        selector(tr("sort." + sort), x + unit + 5, y + 42, unit, 17,
                java.util.stream.IntStream.range(0, 4).mapToObj(n -> tr("sort." + n)).toList(), sort,
                n -> { sort = n; filterChanged(); });
        Button locked = button(tr("locked_filter"), x, y + 63, unit, 17,
                () -> { onlyLocked = !onlyLocked; inventoryScroll.reset(); host.rebuildRelicWidgets(); });
        StarRailUiStyle.setSelected(locked, onlyLocked);
        button(tr("clear"), x + unit + 5, y + 63, unit, 17, () -> { clearFilters(); host.rebuildRelicWidgets(); });
    }
    private List<Stat> mainOptions() { return java.util.Arrays.stream(Part.values()).flatMap(p -> mains(p).stream()).distinct().toList(); }
    private void clearFilters() { partFilter = setFilter = statFilter = -1; starsFilter = stateFilter = sort = 0; onlyLocked = false; query = ""; inventoryScroll.reset(); }
    private List<Entry> filtered() {
        String q = query.strip().toLowerCase(Locale.ROOT);
        Comparator<Entry> comparator = switch (sort) {
            case 1 -> Comparator.comparingInt(Entry::level).reversed().thenComparing(Comparator.comparingInt(Entry::stars).reversed());
            case 2 -> Comparator.comparingLong(Entry::id).reversed();
            case 3 -> Comparator.comparingInt((Entry i) -> i.part().ordinal()).thenComparing(Comparator.comparingInt(Entry::stars).reversed());
            default -> Comparator.comparingInt(Entry::stars).reversed().thenComparing(Comparator.comparingInt(Entry::level).reversed());
        };
        return state.inventory().stream().filter(i -> (partFilter < 0 || i.part().ordinal() == partFilter)
                && (setFilter < 0 || i.setIndex() == setFilter) && (starsFilter == 0 || i.stars() == starsFilter)
                && (statFilter < 0 || i.main() == mainOptions().get(statFilter)) && (!onlyLocked || i.locked())
                && (stateFilter == 0 || stateFilter == 1 && state.worn(i) || stateFilter == 2 && !state.worn(i) || stateFilter == 3 && i.pending())
                && (q.isEmpty() || (i.name() + SETS.get(i.setIndex()).label()).toLowerCase(Locale.ROOT).contains(q))).sorted(comparator).toList();
    }
    private void refreshCards() {
        Map<Long, Tile> previous = new java.util.HashMap<>();
        for (Tile card : cards) { previous.put(card.card.id(), card); host.removeRelicWidget(card); }
        cards.clear();
        List<Entry> list = filtered(); int w = (inventoryRight - inventoryLeft - 16 - (CARD_COLUMNS - 1) * 5) / CARD_COLUMNS;
        for (int n = 0; n < list.size(); n++) {
            Entry entry = list.get(n); Tile card = previous.get(entry.id());
            int x = inventoryLeft + 8 + n % CARD_COLUMNS * (w + 5), y = gridTop + n / CARD_COLUMNS * CARD_STEP;
            if (card == null) card = new Tile(Button.builder(Component.literal(entry.name()), b -> select(state.find(entry.id())))
                    .bounds(x, y, w, CARD_HEIGHT), null, entry);
            else { card.card = entry; card.setX(x); card.setY(y); card.setWidth(w); }
            card.updateTooltip();
            cards.add(card); host.addRelicWidget(card);
        }
        inventoryScroll.bounds(Math.max(0, (list.size() + CARD_COLUMNS - 1) / CARD_COLUMNS * CARD_STEP
                - (CARD_STEP - CARD_HEIGHT) - (gridBottom - gridTop)));
    }
    private void select(Entry entry) { if (entry == null) return; selected = entry.id(); selectedAt = now(); detailScroll.reset(); host.rebuildRelicWidgets(); }
    private void buildWorkbench() {
        Entry i = item(); if (i == null) return;
        int y = stageBottom - 24, w = centerRight - left;
        if (view == View.ENHANCE) {
            int[] steps = {1, 3, 15};
            selector(tr(enhanceStep == 15 ? "to_cap" : "plus_levels", enhanceStep), left, y, w * 2 / 3 - 4, 20,
                    List.of(tr("plus_levels", 1), tr("plus_levels", 3), tr("to_cap")), enhanceStep == 15 ? 2 : enhanceStep == 3 ? 1 : 0,
                    n -> { enhanceStep = steps[n]; host.rebuildRelicWidgets(); });
            int target = Math.min(i.cap(), i.level() + enhanceStep);
            int cost = StarRailRelicPreviewRules.crystalCost(i.stars(), i.level(), target);
            Button b = button(tr("enhance"), left + w * 2 / 3, y, w / 3, 20, () -> perform(Command.item(Action.ENHANCE, selected, target)));
            b.active = state.editable() && !i.pending() && cost > 0 && state.material(Material.CRYSTAL) >= cost;
        } else if (view == View.SALVAGE) {
            if (i.stars() != 5) remainsMode = false;
            selector((remainsMode ? Material.REMAINS : Material.CRYSTAL).label(), left, y, w * 2 / 3 - 4, 20,
                    i.stars() == 5 ? List.of(Material.CRYSTAL.label(), Material.REMAINS.label()) : List.of(Material.CRYSTAL.label()), remainsMode ? 1 : 0,
                    n -> { remainsMode = n == 1; host.rebuildRelicWidgets(); });
            Button b = button(tr("salvage"), left + w * 2 / 3, y, w / 3, 20, () -> confirm(tr("salvage"), List.of(i.name(), tr("salvage_gain", salvageGain(i), (remainsMode ? Material.REMAINS : Material.CRYSTAL).label()), tr(remainsMode ? "forfeit_xp" : "remove_item")), Command.item(remainsMode ? Action.SALVAGE_REMAINS : Action.SALVAGE_CRYSTAL, selected, 0)));
            b.active = state.editable() && !i.locked() && !state.worn(i) && !i.pending();
        } else if (i.pending()) {
            button(tr("keep_old"), left, y, w / 2 - 4, 20, () -> perform(Command.item(Action.KEEP_OLD, selected, 0)));
            button(tr("take_new"), left + w / 2, y, w / 2, 20, () -> perform(Command.item(Action.TAKE_NEW, selected, 0)));
        } else {
            Button b = button(tr("reroll"), left, y, w / 2 - 4, 20, () -> confirm(tr("reroll"), List.of(i.name(), tr("dice_warning")), Command.item(Action.REROLL, selected, 0)));
            b.active = state.editable() && i.stars() == 5 && i.level() == 15 && !i.locked() && !state.worn(i) && state.material(Material.DICE) >= 1;
            List<Entry> eligible = state.inventory().stream().filter(e -> e.stars() == 5 && e.level() == 15 && !e.locked() && !state.worn(e) && !e.pending()).toList();
            selector(tr("pick_eligible"), left + w / 2, y, w / 2, 20,
                    eligible.stream().map(e -> e.name() + " · " + e.part().label()).toList(), eligible.indexOf(i), n -> select(eligible.get(n)));
        }
    }
    private int salvageGain(Entry i) {
        return remainsMode ? 10 : StarRailRelicPreviewRules.crystalSalvage(i.stars(), i.level());
    }
    private Part forgePart() {
        List<Part> parts = java.util.Arrays.stream(Part.values()).filter(p -> p.inner() == SETS.get(forgeSet).inner()).toList();
        forgePart %= parts.size(); return parts.get(forgePart);
    }
    private void buildForge() {
        int w = centerRight - left, half = (w - 6) / 2, y = stageTop + 48; Part part = forgePart();
        List<Stat> pool = mains(part); forgeMain %= pool.size(); if (pool.size() == 1) targetForge = false;
        normalizeForgeSubstats(part, pool);
        selector(SETS.get(forgeSet).label(), left, y, half, 19, SETS.stream().map(SetInfo::label).toList(), forgeSet,
                n -> { forgeSet = n; forgePart = forgeMain = 0; host.rebuildRelicWidgets(); });
        List<Part> parts = java.util.Arrays.stream(Part.values()).filter(p -> p.inner() == SETS.get(forgeSet).inner()).toList();
        selector(part.label(), left + half + 6, y, half, 19, parts.stream().map(Part::label).toList(), forgePart,
                n -> { forgePart = n; forgeMain = 0; host.rebuildRelicWidgets(); });
        Button mode = selector(tr(pool.size() == 1 ? "fixed_main" : targetForge ? "target_forge" : "normal_forge"), left, y + 23, half, 19,
                List.of(tr("normal_forge"), tr("target_forge")), targetForge ? 1 : 0,
                n -> { targetForge = n == 1; normalizeForgeSubstats(part, pool); host.rebuildRelicWidgets(); }); mode.active = pool.size() > 1;
        Button main = selector(targetForge || pool.size() == 1 ? pool.get(forgeMain).label() : tr("random_main"), left + half + 6, y + 23, half, 19,
                pool.stream().map(Stat::label).toList(), forgeMain,
                n -> { forgeMain = n; normalizeForgeSubstats(part, pool); host.rebuildRelicWidgets(); }); main.active = targetForge;
        Button subMode = selector(tr(targetForgeSubs ? "target_substats" : "random_substats"), left, y + 46, w, 19,
                List.of(tr("random_substats"), tr("target_substats")), targetForgeSubs ? 1 : 0,
                n -> { targetForgeSubs = n == 1; host.rebuildRelicWidgets(); });
        if (targetForgeSubs) {
            List<Stat> allowed = forgeSubstatOptions(part, pool);
            int subWidth = (w - 6) / 2;
            for (int index = 0; index < 4; index++) {
                final int subIndex = index;
                List<Stat> options = allowed.stream().filter(stat -> {
                    boolean usedElsewhere = false;
                    for (int other = 0; other < forgeSubstats.size(); other++) {
                        if (other != subIndex && forgeSubstats.get(other) == stat) usedElsewhere = true;
                    }
                    if (usedElsewhere) return false;
                    if (!targetForge && part != Part.HEAD && part != Part.HANDS) {
                        List<Stat> proposed = new ArrayList<>(forgeSubstats);
                        proposed.set(subIndex, stat);
                        return pool.stream().anyMatch(mainStat -> !proposed.contains(mainStat));
                    }
                    return true;
                }).toList();
                int optionIndex = Math.max(0, options.indexOf(forgeSubstats.get(index)));
                int x = left + index % 2 * (subWidth + 6), subY = y + 69 + index / 2 * 21;
                selector(forgeSubstats.get(index).label(), x, subY, subWidth, 19,
                        options.stream().map(Stat::label).toList(), optionIndex,
                        n -> { forgeSubstats.set(subIndex, options.get(n)); host.rebuildRelicWidgets(); });
            }
        }
        List<Stat> targetedSubs = targetForgeSubs ? List.copyOf(forgeSubstats) : List.of();
        String mainSummary = targetForge ? tr("target_main_summary", pool.get(forgeMain).label())
                : pool.size() == 1 ? tr("fixed_main_summary", pool.get(forgeMain).label()) : tr("random_main");
        List<String> details = new ArrayList<>(List.of(SETS.get(forgeSet).label(), part.label(),
                mainSummary,
                targetForgeSubs ? tr("target_substats_summary", String.join("、", targetedSubs.stream().map(Stat::label).toList())) : tr("random_substats"),
                forgeCostLabel()));
        Command forgeCommand = new Command(Action.FORGE, -1, targetForge ? 1 : 0, forgeSet, part,
                pool.get(forgeMain), targetedSubs);
        Button forge = button(tr("forge"), left, stageBottom - 24, w, 20,
                () -> confirm(tr("forge"), details, forgeCommand));
        forge.active = state.editable() && state.material(Material.REMAINS) >= 100
                && (!targetForge || state.material(Material.RESIN) > 0)
                && (!targetForgeSubs || state.material(Material.INTERFERENCE_KEY) > 0);
        List<String> craftOptions = List.of(
                tr("craft_resin") + " · " + tr("craft_cost", 200),
                tr("craft_dice") + " · " + tr("craft_cost", 300),
                tr("craft_key") + " · " + tr("craft_cost", StarRailRelicOperationService.INTERFERENCE_KEY_REMAINS_COST));
        Button craft = selector(tr("craft_materials"), left, stageBottom - 47, w, 19, craftOptions, 0, n -> {
            Action action = n == 0 ? Action.CRAFT_RESIN : n == 1 ? Action.CRAFT_DICE : Action.CRAFT_KEY;
            int cost = n == 0 ? 200 : n == 1 ? 300 : StarRailRelicOperationService.INTERFERENCE_KEY_REMAINS_COST;
            confirm(tr("craft_materials"), List.of(craftOptions.get(n)), Command.item(action, -1, cost));
        });
        craft.active = state.editable() && state.material(Material.REMAINS) >= 200;
    }

    private String forgeCostLabel() {
        if (targetForge && targetForgeSubs) return tr("target_all_cost");
        if (targetForgeSubs) return tr("target_substats_cost");
        return tr(targetForge ? "target_cost" : "normal_cost");
    }

    private List<Stat> forgeSubstatOptions(Part part, List<Stat> mainPool) {
        Stat excludedMain = targetForge ? mainPool.get(forgeMain)
                : part == Part.HEAD ? Stat.HP : part == Part.HANDS ? Stat.ATTACK : null;
        return java.util.Arrays.stream(Stat.values()).filter(stat -> stat != excludedMain).toList();
    }

    private void normalizeForgeSubstats(Part part, List<Stat> mainPool) {
        List<Stat> allowed = forgeSubstatOptions(part, mainPool);
        List<Stat> normalized = new ArrayList<>(4);
        for (Stat stat : forgeSubstats) if (allowed.contains(stat) && !normalized.contains(stat) && normalized.size() < 4) normalized.add(stat);
        for (Stat stat : allowed) if (normalized.size() < 4 && !normalized.contains(stat)) normalized.add(stat);
        forgeSubstats = normalized;
    }
    private void perform(Command command) {
        Entry before = item();
        boolean localPreview = data.preview();
        Feedback result = data.perform(command);
        if (!localPreview) {
            notice = tr(result.key());
            flashAt = now(); flashColor = result.accepted() ? GOLD : 0xFFE9A399;
            return;
        }
        state = data.snapshot(); notice = tr(result.key());
        flashAt = now(); flashColor = result.accepted() ? GOLD : 0xFFE9A399;
        if (command.action() == Action.FORGE && result.accepted()) { selected = state.inventory().get(state.inventory().size() - 1).id(); clearFilters(); }
        else if (item() == null) selected = state.inventory().isEmpty() ? -1 : state.inventory().get(0).id();
        host.rebuildRelicWidgets();
        if (command.action() == Action.ENHANCE && result.accepted() && before != null) {
            List<String> rows = comparison(before, item()); rows.add(0, tr("level_change", before.level(), item().level()));
            dialog(tr("enhanced"), rows, null);
        }
    }

    void onServerSnapshotUpdated(String resultKey, boolean accepted) {
        if (data.preview()) return;
        state = data.snapshot();
        if (state.find(selected) == null) selected = state.inventory().isEmpty() ? -1 : state.inventory().get(0).id();
        if (!resultKey.isBlank()) {
            notice = Component.translatable(resultKey).getString();
            flashAt = now();
            flashColor = accepted ? GOLD : 0xFFE9A399;
        }
        host.rebuildRelicWidgets();
    }
    private void confirm(String title, List<String> lines, Command command) { dialog(title, lines, () -> perform(command)); }
    private void dialog(String title, List<String> lines, Runnable action) { Minecraft.getInstance().setScreen(new RelicDialog(host, title, lines, action)); }
    private void showContributions() {
        EnumMap<Stat, Double> totals = new EnumMap<>(Stat.class); state.equipped().values().forEach(id -> values(state.find(id)).forEach((stat,v) -> totals.merge(stat,v,Double::sum)));
        List<String> rows = new ArrayList<>(); totals.forEach((stat,v) -> rows.add(stat.label() + "  +" + stat.format(v))); rows.add(tr("contribution_note")); dialog(tr("contributions"), rows, null);
    }
    private Map<Stat, Double> values(Entry i) {
        EnumMap<Stat, Double> map = new EnumMap<>(Stat.class); if (i == null) return map;
        map.put(i.main(), i.mainValue()); i.subs().forEach(s -> map.merge(s.stat(), s.value(), Double::sum)); return map;
    }
    private List<String> comparison(Entry before, Entry after) {
        List<String> lines = new ArrayList<>(); Map<Stat, Double> a = values(before), b = values(after);
        for (Stat stat : Stat.values()) if (a.containsKey(stat) || b.containsKey(stat)) {
            double diff = b.getOrDefault(stat,0D) - a.getOrDefault(stat,0D);
            lines.add(stat.label() + "  " + stat.format(a.getOrDefault(stat,0D)) + " → " + stat.format(b.getOrDefault(stat,0D)) + "  (" + (diff > 0 ? "+" : "") + stat.format(diff) + ")");
        }
        return lines;
    }
    void render(GuiGraphics g, int mouseX, int mouseY) {
        inventoryScroll.advance(); detailScroll.advance();
        int width = centerRight - left, resourceWidth = (inventoryRight - left - 16) / Material.values().length;
        for (Material material : Material.values()) {
            int x = left + material.ordinal() * (resourceWidth + 4);
            panel(g,x,top,x+resourceWidth,top+25); image(g,material.image(),x+3,top+3,19);
            fitted(g,material.label(),x+25,top+3,resourceWidth-28,MUTED); text(g,Integer.toString(state.material(material)),x+25,top+14,GOLD);
        }
        panel(g,left,stageTop,centerRight,stageBottom); panel(g,detailLeft,top+27,right,bottom);
        panel(g,inventoryLeft,stageTop,inventoryRight,bottom);
        g.drawManaged(() -> { if (view == View.EQUIPMENT) drawOrbits(g); });
        if (view == View.EQUIPMENT) drawSets(g); else if (view == View.FORGE) drawForge(g); else drawWorkbench(g);
        drawDetail(g, mouseX, mouseY);
        fitted(g,tr("inventory_count",filtered().size(),state.inventory().size()),inventoryLeft+8,stageTop+10,inventoryRight-inventoryLeft-16,GOLD);
        drawInventory(g, mouseX, mouseY);
        if (data.preview()) fitted(g,tr("preview_badge"),detailLeft,top+15,right-detailLeft,CYAN);
        if (!notice.isEmpty() && now() - flashAt < 3) fitted(g,notice,left,stageTop+4,width,flashColor);
        if (now()-changedAt < .3) g.fill(left,stageTop,centerRight,stageBottom,alpha(0xFF091222,Math.max(0,1-(now()-changedAt)/.3)*.35));
        if (now()-flashAt < .7) g.drawManaged(() -> burst(g,(left+centerRight)/2,(stageTop+stageBottom)/2,now()-flashAt,flashColor));
    }
    private void drawSets(GuiGraphics g) {
        int y = stageBottom - 40; Map<Integer, Integer> counts = new java.util.LinkedHashMap<>();
        for (Long id : state.equipped().values()) { Entry i=state.find(id); if(i!=null)counts.merge(i.setIndex(),1,Integer::sum); }
        if (counts.isEmpty()) fitted(g,tr("empty_equipment"),left+8,y,centerRight-left-16,MUTED);
        else { int n=0; for(var entry:counts.entrySet()) { if(n>=2)break; SetInfo set=SETS.get(entry.getKey());
            fitted(g,set.label()+"  "+entry.getValue()+"/"+(set.inner()?2:4),left+8+(centerRight-left)/2*n,y,(centerRight-left)/2-12,GOLD);n++; } }
    }
    private void drawForge(GuiGraphics g) {
        Part part=forgePart(); Entry sample=new Entry(0,forgeSet,part,5,0,mains(part).get(0),0,List.of(),false,List.of());
        image(g,sample.image(),left+10,stageTop+6,38); fitted(g,sample.name(),left+58,stageTop+10,centerRight-left-66,GOLD);
        text(g,"★★★★★  +0",left+58,stageTop+26,GOLD);
        fitted(g,forgeCostLabel(),left+7,stageBottom-68,centerRight-left-14,MUTED);
    }
    private void drawWorkbench(GuiGraphics g) {
        Entry i=item(); if(i==null){fitted(g,tr("select_item"),left+10,stageTop+30,centerRight-left-20,MUTED);return;}
        image(g,i.image(),left+9,stageTop+9,38); fitted(g,i.name(),left+57,stageTop+10,centerRight-left-65,GOLD);
        text(g,tr("item_meta",i.stars(),i.level(),i.cap()),left+57,stageTop+26,MUTED);
        int y=stageTop+56;
        if(view==View.ENHANCE){
            int target=Math.min(i.cap(),i.level()+enhanceStep);
            fitted(g,tr("main_change",i.main().label(),i.main().format(i.mainValue()),
                    i.main().format(StarRailRelicPreviewRules.mainValue(i.main(), i.stars(), target))),left+8,y,centerRight-left-16,CYAN);
            int count=i.cap()/3,unit=(centerRight-left-20)/count;
            for(int n=1;n<=count;n++){int x=left+10+(n-1)*unit,level=n*3; int color=level<=i.level()?CYAN:level<=target?GOLD:MUTED;
                g.renderOutline(x,y+18,unit-5,22,color);text(g,"+"+level,x+5,y+25,color);}
            fitted(g,tr("demo_cost",StarRailRelicPreviewRules.crystalCost(i.stars(), i.level(), target)),left+8,stageBottom-40,centerRight-left-16,GOLD);
            if(i.pending()) fitted(g,tr("pending"),left+8,y+47,centerRight-left-16,MUTED);
        }else if(view==View.SALVAGE){
            fitted(g,tr("salvage_gain",salvageGain(i),(remainsMode?Material.REMAINS:Material.CRYSTAL).label()),left+8,y,centerRight-left-16,GOLD);
            fitted(g,tr(i.pending()?"pending":i.locked()||state.worn(i)?"protected":remainsMode?"forfeit_xp":"remove_item"),left+8,y+22,centerRight-left-16,MUTED);
        }else if(i.pending()){
            for(int n=0;n<i.subs().size();n++){SubStat old=i.subs().get(n),candidate=i.candidate().get(n);fitted(g,old.stat().label()+"  "+old.stat().format(old.value())+" → "+old.stat().format(candidate.value()),left+8,y+n*15,centerRight-left-16,candidate.value()>=old.value()?CYAN:0xFFE9A399);}
        }else{fitted(g,tr("reroll_requirement"),left+8,y,centerRight-left-16,MUTED);fitted(g,tr("dice_warning"),left+8,y+23,centerRight-left-16,GOLD);}
    }
    private void drawDetail(GuiGraphics g, int mouseX, int mouseY) {
        Entry i=item(); if(i==null){fitted(g,tr("select_item"),detailLeft+10,top+70,right-detailLeft-20,MUTED);return;}
        image(g,i.image(),(detailLeft+right)/2-27,top+35,54);
        fitted(g,i.name(),detailLeft+9,top+93,right-detailLeft-18,rarity(i.stars()));
        text(g,"★".repeat(i.stars())+"  +"+i.level()+" / "+i.cap(),detailLeft+9,top+108,GOLD);
        fitted(g,i.part().label()+" · "+(state.worn(i)?tr("worn"):i.locked()?tr("locked"):tr("unworn")),detailLeft+9,top+123,right-detailLeft-18,MUTED);
        int bodyTop=top+143,bodyBottom=bottom-30;host.clip(g,detailLeft,bodyTop,right,bodyBottom);
        int y=bodyTop-(int)detailScroll.position();
        detailRow(g,tr("main"),i.main().label()+" +"+i.main().format(i.mainValue()),y,GOLD);y+=32;
        text(g,tr("subs",i.subs().size()),detailLeft+9,y,GOLD);y+=16;
        for(SubStat sub:i.subs()){detailRow(g,sub.stat().label(),"+"+sub.stat().format(sub.value()),y,CYAN);y+=30;}
        fitted(g,SETS.get(i.setIndex()).label(),detailLeft+9,y,right-detailLeft-18,GOLD);y+=17;
        int setIndex=i.setIndex(), setCount=setCount(setIndex);
        text(g,tr("set_two_count",setCount),detailLeft+9,y,GOLD);y+=15;
        String twoEffect=tr("set_effect."+SETS.get(setIndex).id()+".two");
        wrapped(g,twoEffect,detailLeft+9,y,right-detailLeft-18,TEXT);
        y+=Math.max(13,font().split(Component.literal(twoEffect),right-detailLeft-18).size()*13)+7;
        if(!i.part().inner()){
            text(g,tr("set_four_count",setCount),detailLeft+9,y,GOLD);y+=15;
            String fourEffect=tr("set_effect."+SETS.get(setIndex).id()+".four");
            wrapped(g,fourEffect,detailLeft+9,y,right-detailLeft-18,TEXT);
            y+=Math.max(13,font().split(Component.literal(fourEffect),right-detailLeft-18).size()*13)+7;
        }
        if(!state.worn(i)){
            text(g,tr("compare"),detailLeft+9,y,GOLD);y+=17;
            Map<Stat,Double>a=values(state.equipped(i.part())),b=values(i);
            for(Stat stat:Stat.values())if(a.containsKey(stat)||b.containsKey(stat)){double diff=b.getOrDefault(stat,0D)-a.getOrDefault(stat,0D);
                detailRow(g,stat.label(),(diff>0?"+":"")+stat.format(diff),y,diff<0?0xFFE9A399:CYAN);y+=30;}
        }
        g.disableScissor();int contentHeight=y+(int)detailScroll.position()-bodyTop;
        detailScroll.bounds(Math.max(0,contentHeight-(bodyBottom-bodyTop)));host.renderScrollBar(g,detailScroll,right-3,bodyTop,bodyBottom,contentHeight,mouseX,mouseY);
    }
    private void detailRow(GuiGraphics g,String label,String value,int y,int color){fitted(g,label,detailLeft+9,y,right-detailLeft-20,MUTED);fitted(g,value,detailLeft+9,y+12,right-detailLeft-20,color);}
    private int setCount(int setIndex){
        int count=0;
        for(long id:state.equipped().values()){Entry equipped=state.find(id);if(equipped!=null&&equipped.setIndex()==setIndex)count++;}
        return count;
    }
    private void drawInventory(GuiGraphics g, int mouseX, int mouseY) {
        int offset=(int)inventoryScroll.position();
        for(int n=0;n<cards.size();n++){Tile card=cards.get(n);card.setY(gridTop+n/CARD_COLUMNS*CARD_STEP-offset);card.visible=card.getY()+CARD_HEIGHT>gridTop&&card.getY()<gridBottom;card.active=card.visible;}
        int height=Math.max(0,(cards.size()+CARD_COLUMNS-1)/CARD_COLUMNS*CARD_STEP-(CARD_STEP-CARD_HEIGHT));
        host.renderScrollBar(g,inventoryScroll,inventoryRight-3,gridTop,gridBottom,height,mouseX,mouseY);
        if(cards.isEmpty())wrapped(g,tr(state.inventory().isEmpty()?"empty_inventory":"empty_filter"),inventoryLeft+8,gridTop+25,inventoryRight-inventoryLeft-16,MUTED);
    }
    boolean scroll(double x,double y,double amount){
        if(choiceList!=null){choiceList.wheel(amount);return true;}
        if(y<top||y>bottom)return false;
        if(x>=detailLeft&&x<=right){detailScroll.wheel(amount,28);return true;}
        if(x>=inventoryLeft&&x<=inventoryRight&&y>=gridTop&&y<=gridBottom){inventoryScroll.wheel(amount,32);return true;}
        return false;
    }
    boolean choicesOpen() { return choiceList != null; }
    boolean clickChoices(double x, double y, int mouseButton) {
        if (choiceList == null) return false;
        ChoiceList list = choiceList;
        if (mouseButton == 0 && x >= list.x && x < list.x + list.width && y >= list.y && y < list.y + list.height) {
            int index = list.first + (int) (y - list.y) / ChoiceList.ROW;
            if (index < list.options.size()) { choiceList = null; list.choose.accept(index); }
        } else choiceList = null;
        return true;
    }
    boolean keyChoices(int key) {
        if (choiceList == null) return false;
        ChoiceList list = choiceList;
        switch (key) {
            case GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_TAB -> choiceList = null;
            case GLFW.GLFW_KEY_UP -> list.focus(list.focused - 1);
            case GLFW.GLFW_KEY_DOWN -> list.focus(list.focused + 1);
            case GLFW.GLFW_KEY_HOME -> list.focus(0);
            case GLFW.GLFW_KEY_END -> list.focus(list.options.size() - 1);
            case GLFW.GLFW_KEY_PAGE_UP -> list.focus(list.focused - list.rows);
            case GLFW.GLFW_KEY_PAGE_DOWN -> list.focus(list.focused + list.rows);
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_SPACE -> {
                choiceList = null; list.choose.accept(list.focused);
            }
            default -> { }
        }
        return true;
    }
    void renderChoices(GuiGraphics g, int mouseX, int mouseY) {
        if (choiceList == null) return;
        g.pose().pushPose();
        g.pose().translate(0, 0, 400);
        choiceList.render(g, mouseX, mouseY);
        g.pose().popPose();
    }
    /** Floating selection list in the same logical coordinates as the character screen. */
    private final class ChoiceList {
        private static final int ROW = 20;
        private final List<String> options;
        private final IntConsumer choose;
        private final int x, y, width, height, rows, selectedIndex;
        private final double openedAt = now();
        private int first, focused;
        ChoiceList(int anchorX, int anchorY, int anchorWidth, int anchorHeight,
                   List<String> options, int selectedIndex, IntConsumer choose) {
            this.options = List.copyOf(options); this.choose = choose; this.selectedIndex = selectedIndex;
            rows = Math.min(options.size(), Math.min(8, Math.max(1, (host.height - 24) / ROW)));
            height = rows * ROW;
            int labelWidth = options.stream().mapToInt(font()::width).max().orElse(0) + 28;
            width = Math.min(host.width - 24, Math.max(anchorWidth, labelWidth));
            x = Math.max(12, Math.min(anchorX, host.width - width - 12));
            int below = anchorY + anchorHeight + 3;
            y = Math.max(12, Math.min(below + height <= host.height - 12 ? below : anchorY - height - 3, host.height - height - 12));
            focused = Math.max(0, Math.min(options.size() - 1, selectedIndex));
            first = Math.max(0, Math.min(focused - rows / 2, options.size() - rows));
        }
        void focus(int index) {
            focused = Math.max(0, Math.min(options.size() - 1, index));
            if (focused < first) first = focused;
            if (focused >= first + rows) first = focused - rows + 1;
        }
        void wheel(double amount) {
            int step = (int) Math.ceil(Math.abs(amount));
            first = Math.max(0, Math.min(options.size() - rows, first + (amount > 0 ? -step : step)));
            focused = Math.max(first, Math.min(first + rows - 1, focused));
        }
        void render(GuiGraphics g, int mouseX, int mouseY) {
            double reveal = Math.min(1, Math.max(0, (now() - openedAt) / .12));
            g.fill(x + 3, y + 3, x + width + 3, y + height + 3, 0x80000000);
            g.fill(x, y, x + width, y + height, 0xFF101B2E);
            g.renderOutline(x, y, width, height, alpha(GOLD, .6 + .4 * reveal));
            for (int row = 0; row < rows; row++) {
                int index = first + row, rowY = y + row * ROW;
                boolean hover = mouseX >= x && mouseX < x + width && mouseY >= rowY && mouseY < rowY + ROW;
                if (hover || index == focused) g.fill(x + 1, rowY + 1, x + width - 1, rowY + ROW - 1, 0xFF263950);
                if (index == selectedIndex) text(g, "✓", x + 5, rowY + 6, GOLD);
                ellipsis(g, options.get(index), x + 17, rowY + 6, width - 24, index == selectedIndex ? GOLD : TEXT);
            }
            if (options.size() > rows) {
                int track = height - 4, thumb = Math.max(8, track * rows / options.size());
                int thumbY = y + 2 + (track - thumb) * first / (options.size() - rows);
                g.fill(x + width - 3, y + 2, x + width - 1, y + height - 2, 0xFF263950);
                g.fill(x + width - 3, thumbY, x + width - 1, thumbY + thumb, GOLD);
            }
        }
    }
    private double orbitY() { return (stageTop + stageBottom - 60) / 2D; }
    private double orbitRadius() { return Math.min(116, Math.min((centerRight-left-24)/2D,(stageBottom-stageTop-70)/2D)); }
    private double[] slotPosition(Part part) {
        double cx = (left + centerRight) / 2D, cy = orbitY();
        double dx = orbitRadius() * .66, dy = dx;
        return switch (part) {
            case HEAD -> new double[]{cx-dx, cy-dy};
            case HANDS -> new double[]{cx+dx, cy-dy};
            case BODY -> new double[]{cx-dx, cy+dy};
            case FEET -> new double[]{cx+dx, cy+dy};
            case SPHERE -> new double[]{cx-27, cy-27};
            case ROPE -> new double[]{cx+27, cy+27};
        };
    }
    private void drawOrbits(GuiGraphics g) {
        double cx=(left+centerRight)/2D, cy=orbitY(), radius=orbitRadius(), t=now();
        ring(g,cx,cy,radius,0,Math.PI*2,0x407EAFBB);
        ring(g,cx,cy,radius,t*Math.PI/12,.65,0xB0E7C77E);
        ring(g,cx,cy,radius,t*Math.PI/12+Math.PI,.65,0xB0E7C77E);
        ring(g,cx,cy,58,0,Math.PI*2,0x3055D6D2);
        ring(g,cx,cy,58,-t*Math.PI/8,.8,0xB055D6D2);
        g.drawCenteredString(font(),tr("inner_label"),(int)cx,(int)cy-72,CYAN);
        for(int n=0;n<32;n++) {
            double a=n*Math.PI/16, tick=n%4==0?8:5;
            StarRailCosmicUi.line(g,cx+Math.cos(a)*(radius+3),cy+Math.sin(a)*(radius+3),
                    cx+Math.cos(a)*(radius+tick),cy+Math.sin(a)*(radius+tick),0x607EAFBB);
        }
    }
    private static void ring(GuiGraphics g,double x,double y,double r,double start,double length,int color){int steps=Math.max(6,(int)(r*length/2));for(int n=0;n<steps;n++){double a=start+length*n/steps,b=start+length*(n+1)/steps;StarRailCosmicUi.line(g,x+Math.cos(a)*r,y+Math.sin(a)*r,x+Math.cos(b)*r,y+Math.sin(b)*r,color);}}
    private static int alpha(int color,double opacity){return Math.max(0,Math.min(255,(int)(opacity*255)))<<24|(color&0xFFFFFF);}
    private static int rarity(int stars){return switch(stars){case 2->0xFF91BE9A;case 3->0xFF73B6DD;case 4->0xFFBB9CDE;default->GOLD;};}
    private static void panel(GuiGraphics g,int l,int t,int r,int b){g.fill(l,t,r,b,0xB0101A2D);g.renderOutline(l,t,r-l,b-t,0x5034435B);}
    private void fitted(GuiGraphics g,String text,int x,int y,int w,int color){ellipsis(g,text,x,y,w,color);}
    private void wrapped(GuiGraphics g,String label,int x,int y,int w,int color){
        for(var line:font().split(Component.literal(label),Math.max(1,w))){g.drawString(font(),line,x,y,color,false);y+=13;}
    }
    private void ellipsis(GuiGraphics g,String label,int x,int y,int w,int color){
        String display=font().width(label)<=w?label:font().plainSubstrByWidth(label,Math.max(0,w-font().width("…")))+"…";
        text(g,display,x,y,color);
    }
    private void text(GuiGraphics g,String text,int x,int y,int color){g.drawString(font(),text,x,y,color,false);}
    private static void image(GuiGraphics g,ResourceLocation texture,int x,int y,int size){RenderSystem.enableBlend();g.blit(texture,x,y,size,size,0F,0F,64,64,64,64);}
    private static void burst(GuiGraphics g,double x,double y,double age,int color){if(age<0||age>.7)return;ring(g,x,y,12+age*65,0,Math.PI*2,alpha(color,(1-age/.7)*.7));for(int n=0;n<12;n++){double a=n*Math.PI/6,r=12+age*(35+n%3*18);int px=(int)(x+Math.cos(a)*r),py=(int)(y+Math.sin(a)*r);g.fill(px,py,px+2,py+2,alpha(color,1-age/.7));}}

    private final class Tile extends Button {
        private final Part part; private Entry card;
        Tile(Button.Builder builder,Part part,Entry card){
            super(builder);this.part=part;this.card=card;
            updateTooltip();
        }
        void updateTooltip() {
            String label=part!=null?part.label():card.name()+" · "+card.part().label()+" · "+card.main().label()+" "+card.main().format(card.mainValue());
            if(card!=null)label+=" · "+tr(card.pending()?"pending":state.worn(card)?"worn":card.locked()?"locked":"unworn");
            setTooltip(Tooltip.create(Component.literal(label)));
        }
        @Override protected boolean clicked(double x,double y){if(card!=null&&(y<gridTop||y>=gridBottom))return false;if(part!=null){double dx=x-getX()-width/2D,dy=y-getY()-height/2D;if(dx*dx+dy*dy>width*width/4D)return false;}return super.clicked(x,y);}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float partialTick){
            boolean hover=isHoveredOrFocused();
            if(part!=null){Entry i=state.equipped(part);double x=getX()+width/2D,y=getY()+height/2D,r=width/2D,t=now();int color=part.inner()?CYAN:i==null?MUTED:rarity(i.stars());boolean chosen=i!=null&&i.id()==selected;
                g.drawManaged(()->{StarRailCosmicUi.ellipse(g,x,y,r,r,0xFF152337);ring(g,x,y,r,0,Math.PI*2,color);double phase=t*Math.PI*2/(part.inner()?-9:12)+part.ordinal();ring(g,x,y,r+2,phase,.9,alpha(color,.8));ring(g,x,y,r+2,phase+Math.PI,.9,alpha(color,.8));ring(g,x,y,r+4,0,Math.PI*2,alpha(color,.15+.15*(1+Math.sin(t*1.2+part.ordinal()))));if(chosen)ring(g,x,y,r+5,0,Math.PI*2,GOLD);double age=t-slotPressed.getOrDefault(part,-10D);if(age<.45)ring(g,x,y,r+5+age*12,0,Math.PI*2,alpha(GOLD,1-age/.45));});
                if(i!=null){int size=part.inner()?32:44,dy=hover?0:(int)Math.round(Math.sin(t*1.2+part.ordinal())*1.5);image(g,i.image(),(int)x-size/2,(int)y-size/2+dy,size);text(g,"+"+i.level(),getX()+width-20,getY()-2,GOLD);}else text(g,"+",(int)x-3,(int)y-4,MUTED);
                fitted(g,part.label(),getX()-6,getY()+height+4,width+12,MUTED);
            }else{
                host.clip(g,inventoryLeft+6,gridTop,inventoryRight-6,gridBottom);
                int color=rarity(card.stars()), x=getX(), y=getY();
                panel(g,x,y,x+width,y+height);
                g.renderOutline(x,y,width,height,card.id()==selected?GOLD:hover?CYAN:color);
                image(g,card.image(),x+width/2-18,y+16-(hover?2:0),36);
                text(g,"+"+card.level(),x+width-21,y+4,color);
                if(card.locked()||state.worn(card)||card.pending())
                    g.fill(x+4,y+4,x+7,y+7,card.pending()?CYAN:GOLD);
                fitted(g,"★".repeat(card.stars()),x+4,y+54,width-8,color);
                ellipsis(g,card.name(),x+4,y+66,width-8,TEXT);
                ellipsis(g,card.main().label()+" "+card.main().format(card.mainValue()),x+4,y+78,width-8,CYAN);
                if(card.id()==selected&&now()-selectedAt<.4)
                    g.renderOutline(x+1,y+1,width-2,height-2,alpha(GOLD,1-(now()-selectedAt)/.4));
                g.disableScissor();
            }
        }
    }
    private static final class RelicDialog extends StarRailModalScreen {
        private final List<String> rows;private final Runnable action;private final StarRailSmoothScroll scroll=new StarRailSmoothScroll();
        RelicDialog(StarRailStyledScreen parent,String title,List<String> rows,Runnable action){super(Component.literal(title),parent);this.rows=List.copyOf(rows);this.action=action;}
        @Override protected int preferredWidth(){return 460;}
        @Override protected int preferredHeight(){return 330;}
        @Override protected double referenceWidth(){return parent.referenceWidth();}
        @Override protected double referenceHeight(){return parent.referenceHeight();}
        @Override protected void init(){super.init();addRenderableWidget(StarRailUiStyle.outlinedButton(Component.literal(tr(action==null?"done":"confirm")),b->{onClose();if(action!=null)action.run();},modalRight-118,modalBottom-32,100,20));if(action!=null)addRenderableWidget(StarRailUiStyle.outlinedButton(Component.literal(tr("cancel")),b->onClose(),modalRight-220,modalBottom-32,94,20));}
        @Override protected void renderModal(GuiGraphics g,int mx,int my,float pt){scroll.advance();clip(g,modalLeft+14,bodyTop,modalRight-14,modalBottom-44);int y=bodyTop+4-(int)scroll.position();for(String row:rows){for(var line:font.split(Component.literal(row),modalRight-modalLeft-40)){g.drawString(font,line,modalLeft+20,y,INK,false);y+=13;}y+=7;}g.disableScissor();int h=y+(int)scroll.position()-bodyTop;scroll.bounds(Math.max(0,h-(modalBottom-44-bodyTop)));renderScrollBar(g,scroll,modalRight-12,bodyTop,modalBottom-44,h,mx,my);}
        @Override protected boolean logicalMouseScrolled(double x,double y,double amount){scroll.wheel(amount,26);return true;}
    }
}
