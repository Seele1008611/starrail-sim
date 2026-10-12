package com.starrail.sim.client;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

import static com.starrail.sim.client.StarRailRelicUiData.mains;

/** Disposable client-only HTML-style examples. Never reads or writes the world/player. */
final class StarRailRelicPreviewData implements StarRailRelicUiData {
    private final List<Mutable> items = new ArrayList<>();
    private final EnumMap<Part, Long> equipped = new EnumMap<>(Part.class);
    private final EnumMap<Material, Integer> materials = new EnumMap<>(Material.class);
    private final Random random = new Random();
    private long sequence = 100;
    private static final class Mutable {
        long id; int set, stars, level; Part part; Stat main; boolean locked;
        List<SubStat> subs = new ArrayList<>(), candidate = new ArrayList<>();
        Entry entry() { return new Entry(id, set, part, stars, level, main, mainValue(this), subs, locked, candidate); }
    }
    StarRailRelicPreviewData() {
        for (int set = 0; set < SETS.size(); set++) for (Part part : Part.values()) {
            if (part.inner() != SETS.get(set).inner()) continue;
            int stars = set == 1 && part.ordinal() < 3 ? part.ordinal() + 2 : 5;
            int level = set == 2 && (part == Part.HEAD || part == Part.HANDS) ? 15 : set == 0 ? 9 : set == 1 ? 0 : 6;
            Mutable item = create(set, part, stars, mains(part).get(0));
            advance(item, Math.min(level, stars * 3));
            if (set == 0 || set == 4) equipped.put(part, item.id);
        }
        materials.put(Material.CRYSTAL, 120); materials.put(Material.REMAINS, 650);
        materials.put(Material.RESIN, 3); materials.put(Material.DICE, 4); materials.put(Material.INTERFERENCE_KEY, 1);
    }
    @Override public boolean preview() { return true; }
    @Override public Snapshot snapshot() { return new Snapshot(items.stream().map(Mutable::entry).toList(), equipped, materials, true); }
    private Mutable create(int set, Part part, int stars, Stat main) { return create(set, part, stars, main, List.of()); }
    private Mutable create(int set, Part part, int stars, Stat main, List<Stat> forcedSubstats) {
        Mutable i = new Mutable(); i.id = ++sequence; i.set = set; i.part = part; i.stars = stars; i.main = main;
        if (forcedSubstats.size() == 4) {
            for (Stat stat : forcedSubstats) i.subs.add(new SubStat(stat,
                    StarRailRelicPreviewRules.substatRoll(stat, stars, random), 0, 0));
        } else {
            int initial = StarRailRelicPreviewRules.initialSubstatCount(stars, random);
            for (int n = 0; n < initial; n++) addSub(i);
        }
        items.add(i); return i;
    }
    private void addSub(Mutable item) {
        List<Stat> pool = new ArrayList<>(List.of(Stat.values()));
        pool.remove(item.main); item.subs.forEach(s -> pool.remove(s.stat()));
        Stat stat = pool.get(random.nextInt(pool.size()));
        item.subs.add(new SubStat(stat, StarRailRelicPreviewRules.substatRoll(stat, item.stars, random), 0, 0));
    }
    private void advance(Mutable item, int target) {
        for (int n = item.level + 1; n <= target; n++) if (n % 3 == 0) {
            if (item.subs.size() < 4) addSub(item);
            else {
                int index = random.nextInt(4); SubStat s = item.subs.get(index);
                item.subs.set(index, new SubStat(s.stat(), s.base(),
                        s.extra() + StarRailRelicPreviewRules.substatRoll(s.stat(), item.stars, random), s.rolls() + 1));
            }
        }
        item.level = target;
    }
    private static double mainValue(Mutable i) {
        return StarRailRelicPreviewRules.mainValue(i.main, i.stars, i.level);
    }
    private int count(Material type) { return materials.getOrDefault(type, 0); }
    private boolean pay(Material type, int amount) { if (amount < 0 || count(type) < amount) return false; materials.put(type, count(type) - amount); return true; }
    private Feedback ok(String key) { return new Feedback(true, key); }
    private Feedback no(String key) { return new Feedback(false, key); }
    @Override public Feedback perform(Command c) {
        Mutable i = items.stream().filter(entry -> entry.id == c.itemId()).findFirst().orElse(null);
        if (c.action() == Action.CRAFT_DICE || c.action() == Action.CRAFT_RESIN || c.action() == Action.CRAFT_KEY) {
            Material type = c.action() == Action.CRAFT_DICE ? Material.DICE
                    : c.action() == Action.CRAFT_KEY ? Material.INTERFERENCE_KEY : Material.RESIN;
            int cost = c.action() == Action.CRAFT_DICE ? 300
                    : c.action() == Action.CRAFT_KEY ? 500 : 200;
            if (!pay(Material.REMAINS, cost)) return no("insufficient");
            materials.put(type, count(type) + 1); return ok("crafted");
        }
        if (c.action() == Action.FORGE) {
            if (c.setIndex() < 0 || c.setIndex() >= SETS.size() || c.part() == null || c.part().inner() != SETS.get(c.setIndex()).inner()) return no("invalid");
            List<Stat> pool = mains(c.part()); boolean target = c.value() == 1;
            if (target && (pool.size() == 1 || !pool.contains(c.main()))) return no("invalid");
            List<Stat> targetedSubs = c.targetSubstats();
            if (!targetedSubs.isEmpty() && targetedSubs.size() != 4) return no("invalid");
            if (targetedSubs.stream().distinct().count() != targetedSubs.size()) return no("invalid");
            if (target && targetedSubs.contains(c.main())) return no("invalid");
            boolean fixedMain = c.part() == Part.HEAD || c.part() == Part.HANDS;
            Stat fixedStat = c.part() == Part.HEAD ? Stat.HP : c.part() == Part.HANDS ? Stat.ATTACK : null;
            if (!target && fixedMain && targetedSubs.contains(fixedStat)) return no("invalid");
            List<Stat> legalMains = pool.stream().filter(stat -> !targetedSubs.contains(stat)).toList();
            if (legalMains.isEmpty()) return no("invalid");
            if (count(Material.REMAINS) < 100 || target && count(Material.RESIN) < 1
                    || !targetedSubs.isEmpty() && count(Material.INTERFERENCE_KEY) < 1) return no("insufficient");
            pay(Material.REMAINS, 100); if (target) pay(Material.RESIN, 1);
            if (!targetedSubs.isEmpty()) pay(Material.INTERFERENCE_KEY, 1);
            Stat main = target ? c.main() : legalMains.get(random.nextInt(legalMains.size()));
            create(c.setIndex(), c.part(), 5, main, targetedSubs); return ok("forged");
        }
        if (i == null) return no("select_item");
        if (!i.candidate.isEmpty() && c.action() != Action.KEEP_OLD && c.action() != Action.TAKE_NEW) return no("pending");
        boolean worn = equipped.getOrDefault(i.part, -1L) == i.id;
        switch (c.action()) {
            case EQUIP -> { if (worn) equipped.remove(i.part); else equipped.put(i.part, i.id); return ok(worn ? "unequipped" : "equipped"); }
            case LOCK -> { i.locked = !i.locked; return ok(i.locked ? "locked" : "unlocked"); }
            case ENHANCE -> {
                int target = Math.min(c.value(), i.stars * 3);
                int cost = StarRailRelicPreviewRules.crystalCost(i.stars, i.level, target);
                if (cost <= 0) return no("max_level");
                if (!pay(Material.CRYSTAL, cost)) return no("insufficient");
                advance(i, target); return ok("enhanced");
            }
            case SALVAGE_CRYSTAL, SALVAGE_REMAINS -> {
                if (worn || i.locked) return no("protected");
                if (c.action() == Action.SALVAGE_REMAINS && i.stars != 5) return no("five_only");
                Material type = c.action() == Action.SALVAGE_REMAINS ? Material.REMAINS : Material.CRYSTAL;
                int gain = type == Material.REMAINS ? 10 : StarRailRelicPreviewRules.crystalSalvage(i.stars, i.level);
                items.remove(i); materials.put(type, count(type) + gain); return ok("salvaged");
            }
            case REROLL -> {
                if (i.stars != 5 || i.level != 15 || worn || i.locked) return no("reroll_requirement");
                if (!pay(Material.DICE, 1)) return no("insufficient");
                int rolls = i.subs.stream().mapToInt(SubStat::rolls).sum();
                i.candidate = new ArrayList<>(i.subs.stream().map(s -> new SubStat(s.stat(), s.base(), 0, 0)).toList());
                for (int n = 0; n < rolls; n++) {
                    int index = random.nextInt(4); SubStat s = i.candidate.get(index);
                    double extra = StarRailRelicPreviewRules.substatRoll(s.stat(), i.stars, random);
                    i.candidate.set(index, new SubStat(s.stat(), s.base(), s.extra() + extra, s.rolls() + 1));
                }
                return ok("candidate_ready");
            }
            case KEEP_OLD, TAKE_NEW -> {
                if (i.candidate.isEmpty()) return no("invalid");
                if (c.action() == Action.TAKE_NEW) i.subs = new ArrayList<>(i.candidate);
                i.candidate.clear(); return ok("chosen");
            }
            default -> { return no("invalid"); }
        }
    }
}
