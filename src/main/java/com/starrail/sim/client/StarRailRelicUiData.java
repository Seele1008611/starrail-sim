package com.starrail.sim.client;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Locale;

/** UI contract. The screen owns no ItemStacks, attribute modifiers or persistence. */
public interface StarRailRelicUiData {
    enum Part {
        HEAD, HANDS, BODY, FEET, SPHERE, ROPE;
        public boolean inner() { return ordinal() >= 4; }
        public String label() { return tr("part." + name().toLowerCase(Locale.ROOT)); }
    }
    enum Stat {
        HP(false), HP_PCT(true), ATTACK(false), ATTACK_PCT(true), ARMOR(false), ARMOR_PCT(true),
        CRIT(true), CRIT_DAMAGE(true), HIT(true), RESIST(true), BREAK(true), HEAL(true),
        TOUGHNESS(false), TOUGHNESS_PCT(true);
        public final boolean percent;
        Stat(boolean percent) { this.percent = percent; }
        public String label() { return tr("stat." + name().toLowerCase(Locale.ROOT)); }
        public String format(double value) {
            String text = String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
            return text + (percent ? "%" : "");
        }
    }
    enum Material {
        CRYSTAL("lost_crystal"), REMAINS("relic_remains"), RESIN("self_modeling_resin"),
        DICE("variable_dice"), INTERFERENCE_KEY("interference_key");
        public final String texture;
        Material(String texture) { this.texture = texture; }
        public String label() { return tr("material." + texture); }
        public ResourceLocation image() { return new ResourceLocation("starrail_sim", "textures/gui/relics/materials/" + texture + ".png"); }
    }
    enum Action { EQUIP, LOCK, ENHANCE, SALVAGE_CRYSTAL, SALVAGE_REMAINS, FORGE, CRAFT_RESIN, CRAFT_DICE, CRAFT_KEY, REROLL, KEEP_OLD, TAKE_NEW }
    record Command(Action action, long itemId, int value, int setIndex, Part part, Stat main,
                   List<Stat> targetSubstats) {
        public Command { targetSubstats = targetSubstats == null ? List.of() : List.copyOf(targetSubstats); }
        public Command(Action action, long itemId, int value, int setIndex, Part part, Stat main) {
            this(action, itemId, value, setIndex, part, main, List.of());
        }
        public static Command item(Action action, long id, int value) { return new Command(action, id, value, 0, Part.HEAD, Stat.HP); }
    }
    record SubStat(Stat stat, double base, double extra, int rolls) { public double value() { return base + extra; } }
    record Entry(long id, int setIndex, Part part, int stars, int level, Stat main, double mainValue,
                 List<SubStat> subs, boolean locked, List<SubStat> candidate, long version) {
        public Entry(long id, int setIndex, Part part, int stars, int level, Stat main, double mainValue,
                     List<SubStat> subs, boolean locked, List<SubStat> candidate) {
            this(id, setIndex, part, stars, level, main, mainValue, subs, locked, candidate, 0L);
        }
        public Entry { subs = List.copyOf(subs); candidate = List.copyOf(candidate); }
        public boolean pending() { return !candidate.isEmpty(); }
        public int cap() { return stars * 3; }
        public String name() { return tr("item." + SETS.get(setIndex).id() + "." + part.name().toLowerCase(Locale.ROOT)); }
        public ResourceLocation image() { return new ResourceLocation("starrail_sim", "textures/gui/relics/" + SETS.get(setIndex).id() + "/" + part.name().toLowerCase(Locale.ROOT) + ".png"); }
    }
    record SetInfo(String id, boolean inner) {
        public String label() { return tr("set." + id); }
    }
    List<SetInfo> SETS = List.of(new SetInfo("musketeer", false), new SetInfo("passerby", false),
            new SetInfo("knight", false), new SetInfo("space_station", true), new SetInfo("fleet", true),
            new SetInfo("guard", false), new SetInfo("genius", false),
            new SetInfo("dead_water_pioneer", false), new SetInfo("iron_cavalry", false),
            new SetInfo("valorous", false), new SetInfo("scholar", false), new SetInfo("hero", false),
            new SetInfo("savior", false), new SetInfo("magical_girl", false), new SetInfo("navigator", false),
            new SetInfo("belobog", true), new SetInfo("penacony", true), new SetInfo("amphoreus", true),
            new SetInfo("live_stream", true));
    record Snapshot(List<Entry> inventory, Map<Part, Long> equipped, Map<Material, Integer> materials, boolean editable,
                    Map<Integer,Map<Part,UUID>> presets,Set<UUID> ownedRelics) {
        public Snapshot(List<Entry> inventory,Map<Part,Long> equipped,Map<Material,Integer> materials,boolean editable){this(inventory,equipped,materials,editable,Map.of(),Set.of());}
        public Snapshot { inventory = List.copyOf(inventory); equipped = Map.copyOf(equipped); materials = Map.copyOf(materials);
            Map<Integer,Map<Part,UUID>> copy=new java.util.HashMap<>();presets.forEach((slot,items)->copy.put(slot,Map.copyOf(items)));presets=Map.copyOf(copy);ownedRelics=Set.copyOf(ownedRelics); }
        public int presetMissing(int slot){return (int)presets.getOrDefault(slot,Map.of()).values().stream().filter(id->!ownedRelics.contains(id)).count();}
        public Entry find(long id) { return inventory.stream().filter(i -> i.id() == id).findFirst().orElse(null); }
        public Entry equipped(Part part) { return find(equipped.getOrDefault(part, -1L)); }
        public boolean worn(Entry entry) { return equipped.getOrDefault(entry.part(), -1L) == entry.id(); }
        public int material(Material type) { return materials.getOrDefault(type, 0); }
    }
    record Feedback(boolean accepted, String key) {}
    Snapshot snapshot();
    Feedback perform(Command command);
    boolean preview();

    static String tr(String key, Object... args) { return Component.translatable("ui.starrail_sim.relic." + key, args).getString(); }
    static List<Stat> mains(Part part) {
        return switch (part) {
            case HEAD -> List.of(Stat.HP);
            case HANDS -> List.of(Stat.ATTACK);
            case BODY -> List.of(Stat.HP_PCT, Stat.ATTACK_PCT, Stat.ARMOR_PCT, Stat.CRIT, Stat.CRIT_DAMAGE, Stat.HIT, Stat.HEAL);
            case FEET, SPHERE -> List.of(Stat.HP_PCT, Stat.ATTACK_PCT, Stat.ARMOR_PCT);
            case ROPE -> List.of(Stat.HP_PCT, Stat.ATTACK_PCT, Stat.ARMOR_PCT, Stat.BREAK);
        };
    }
    static StarRailRelicUiData empty() {
        return new StarRailRelicUiData() {
            @Override public Snapshot snapshot() { return new Snapshot(List.of(), Map.of(), Map.of(), false); }
            @Override public Feedback perform(Command command) { return new Feedback(false, "not_ready"); }
            @Override public boolean preview() { return false; }
        };
    }
}
