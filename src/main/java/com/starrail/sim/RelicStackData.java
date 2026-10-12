package com.starrail.sim;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Versioned, server-readable state stored on every individual relic stack. */
public final class RelicStackData {
    public static final int DATA_VERSION = 1;
    private static final String ROOT = "StarRailRelic";
    private static final int[][] XP_TO_NEXT_LEVEL = {
            {}, {},
            {100, 100, 100, 200, 200, 200},
            {100, 100, 100, 200, 200, 200, 300, 300, 300},
            {100, 100, 200, 200, 300, 300, 400, 400, 500, 500, 600, 600},
            {100, 100, 200, 200, 300, 300, 400, 400, 500, 600, 700, 800, 900, 1000, 1200}
    };

    private RelicStackData() {}

    public enum SetId {
        MUSKETEER("musketeer", false), PASSERBY("passerby", false), KNIGHT("knight", false),
        SPACE_STATION("space_station", true), FLEET("fleet", true);

        private final String id;
        private final boolean inner;

        SetId(String id, boolean inner) {
            this.id = id;
            this.inner = inner;
        }

        public String id() { return id; }
        public boolean inner() { return inner; }

        public static Optional<SetId> byId(String id) {
            for (SetId set : values()) if (set.id.equals(id)) return Optional.of(set);
            return Optional.empty();
        }
    }

    public enum Part {
        HEAD("head", "relic_head", false), HANDS("hands", "relic_hands", false),
        BODY("body", "relic_body", false), FEET("feet", "relic_feet", false),
        SPHERE("sphere", "planar_sphere", true), ROPE("rope", "link_rope", true);

        private final String id;
        private final String curiosSlot;
        private final boolean inner;

        Part(String id, String curiosSlot, boolean inner) {
            this.id = id;
            this.curiosSlot = curiosSlot;
            this.inner = inner;
        }

        public String id() { return id; }
        public String curiosSlot() { return curiosSlot; }
        public boolean inner() { return inner; }

        public Stat defaultMainStat() {
            return switch (this) {
                case HEAD -> Stat.HP;
                case HANDS -> Stat.ATTACK;
                case BODY -> Stat.HP_PCT;
                case FEET, SPHERE -> Stat.ATTACK_PCT;
                case ROPE -> Stat.BREAK;
            };
        }

        public boolean acceptsMainStat(Stat stat) {
            return switch (this) {
                case HEAD -> stat == Stat.HP;
                case HANDS -> stat == Stat.ATTACK;
                case BODY -> stat == Stat.HP_PCT || stat == Stat.ATTACK_PCT || stat == Stat.ARMOR_PCT
                        || stat == Stat.CRIT || stat == Stat.CRIT_DAMAGE || stat == Stat.HIT || stat == Stat.HEAL;
                case FEET, SPHERE -> stat == Stat.HP_PCT || stat == Stat.ATTACK_PCT || stat == Stat.ARMOR_PCT;
                case ROPE -> stat == Stat.HP_PCT || stat == Stat.ATTACK_PCT || stat == Stat.ARMOR_PCT
                        || stat == Stat.BREAK;
            };
        }

        public static Optional<Part> byId(String id) {
            for (Part part : values()) if (part.id.equals(id)) return Optional.of(part);
            return Optional.empty();
        }
    }

    /** Stat ids intentionally align with the UI's localized stat suffixes. */
    public enum Stat {
        HP("hp", false, 3.0, 0.8), HP_PCT("hp_pct", true, 8.0, 2.0),
        ATTACK("attack", false, 0.75, 17.0 / 60.0), ATTACK_PCT("attack_pct", true, 8.0, 2.0),
        ARMOR("armor", false, 0.0, 0.0), ARMOR_PCT("armor_pct", true, 10.0, 2.5),
        TOUGHNESS("toughness", false, 0.0, 0.0), TOUGHNESS_PCT("toughness_pct", true, 0.0, 0.0),
        CRIT("crit", true, 5.0, 1.0), CRIT_DAMAGE("crit_damage", true, 10.0, 2.0),
        HIT("hit", true, 8.0, 2.0), RESIST("resist", true, 0.0, 0.0),
        BREAK("break", true, 12.0, 3.2), HEAL("heal", true, 6.0, 1.6);

        private final String id;
        private final boolean percent;
        private final double mainBase;
        private final double mainPerLevel;

        Stat(String id, boolean percent, double mainBase, double mainPerLevel) {
            this.id = id;
            this.percent = percent;
            this.mainBase = mainBase;
            this.mainPerLevel = mainPerLevel;
        }

        public String id() { return id; }
        public boolean percent() { return percent; }
        public boolean canBeMain() { return mainBase != 0.0 || mainPerLevel != 0.0; }

        public double mainValue(int stars, int level) {
            if (!canBeMain() || stars < 2 || stars > 5 || level < 0 || level > maxLevel(stars)) {
                throw new IllegalArgumentException("Invalid relic main stat request: " + this + " " + stars + "/" + level);
            }
            double quality = switch (stars) {
                case 2 -> 0.4;
                case 3 -> 0.6;
                case 4 -> 0.8;
                default -> 1.0;
            };
            return quality * (mainBase + mainPerLevel * level);
        }

        public static Optional<Stat> byId(String id) {
            for (Stat stat : values()) if (stat.id.equals(id)) return Optional.of(stat);
            return Optional.empty();
        }
    }

    /** Initial roll plus independently persisted enhancement rolls. */
    public record SubStat(Stat stat, double baseValue, int firstLevel, List<Double> upgradeRolls) {
        public SubStat {
            if (stat == null || !Double.isFinite(baseValue) || baseValue < 0 || firstLevel < 0
                    || firstLevel > 15 || firstLevel % 3 != 0) {
                throw new IllegalArgumentException("Invalid relic substat");
            }
            upgradeRolls = List.copyOf(upgradeRolls);
            if (upgradeRolls.stream().anyMatch(value -> value == null || !Double.isFinite(value) || value < 0)) {
                throw new IllegalArgumentException("Invalid relic substat roll");
            }
        }

        public double value() {
            return baseValue + upgradeRolls.stream().mapToDouble(Double::doubleValue).sum();
        }
    }

    /** A pending reroll is stored on the relic so logout cannot generate a new candidate. */
    public record Data(UUID instanceId, SetId set, Part part, int stars, int level, int experience,
                       Stat mainStat, List<SubStat> substats, boolean locked, int rerolls,
                       long operationVersion, List<SubStat> pendingReroll) {
        public Data {
            if (instanceId == null || set == null || part == null || mainStat == null
                    || set.inner() != part.inner() || stars < 2 || stars > 5
                    || level < 0 || level > RelicStackData.maxLevel(stars)
                    || experience < xpAtLevel(stars, level)
                    || experience > xpAtLevel(stars, RelicStackData.maxLevel(stars))
                    || !part.acceptsMainStat(mainStat)
                    || rerolls < 0 || operationVersion < 0) {
                throw new IllegalArgumentException("Invalid relic data");
            }
            int nextLevelExperience = level == RelicStackData.maxLevel(stars)
                    ? xpAtLevel(stars, level) : xpAtLevel(stars, level + 1);
            if (level < RelicStackData.maxLevel(stars) && experience >= nextLevelExperience) {
                throw new IllegalArgumentException("Relic level does not match experience");
            }
            substats = List.copyOf(substats);
            pendingReroll = List.copyOf(pendingReroll);
            validateSubstats(substats, mainStat, level, RelicStackData.maxLevel(stars));
            if (!pendingReroll.isEmpty()) {
                if (stars != 5 || level != RelicStackData.maxLevel(stars) || locked) {
                    throw new IllegalArgumentException("Invalid pending relic reroll state");
                }
                validateSubstats(pendingReroll, mainStat, level, RelicStackData.maxLevel(stars));
                Set<Stat> currentIds = new HashSet<>();
                Set<Stat> candidateIds = new HashSet<>();
                substats.forEach(value -> currentIds.add(value.stat()));
                pendingReroll.forEach(value -> candidateIds.add(value.stat()));
                if (!currentIds.equals(candidateIds)) throw new IllegalArgumentException("Reroll changed relic substat ids");
                for (SubStat current : substats) {
                    SubStat candidate = pendingReroll.stream()
                            .filter(value -> value.stat() == current.stat()).findFirst().orElseThrow();
                    if (candidate.baseValue() != current.baseValue()
                            || candidate.firstLevel() != current.firstLevel()) {
                        throw new IllegalArgumentException("Reroll changed relic base substat data");
                    }
                }
            }
        }

        public double mainValue() { return mainStat.mainValue(stars, level); }
        public int maxLevel() { return RelicStackData.maxLevel(stars); }
        public boolean hasPendingReroll() { return !pendingReroll.isEmpty(); }
    }

    public static Data fresh(SetId set, Part part, int stars, Stat mainStat) {
        return new Data(UUID.randomUUID(), set, part, stars, 0, 0, mainStat,
                List.of(), false, 0, 0L, List.of());
    }

    /** Creates a server-rollable relic using the confirmed main-stat and substat weights. */
    public static Data generate(SetId set, Part part, int stars, RandomSource random) {
        return generate(set, part, stars, null, List.of(), random);
    }

    public static Data generate(SetId set, Part part, int stars, Stat forcedMainStat, RandomSource random) {
        return generate(set, part, stars, forcedMainStat, List.of(), random);
    }

    /** Creates a relic with optional deterministic main stat and four deterministic substat types. */
    public static Data generate(SetId set, Part part, int stars, Stat forcedMainStat,
                                List<Stat> forcedSubstats, RandomSource random) {
        if (set == null || part == null || random == null || set.inner() != part.inner()
                || stars < 2 || stars > 5) {
            throw new IllegalArgumentException("Invalid relic generation request");
        }
        List<Stat> selectedSubstats = forcedSubstats == null ? List.of() : List.copyOf(forcedSubstats);
        if (!selectedSubstats.isEmpty() && selectedSubstats.size() != 4) {
            throw new IllegalArgumentException("Targeted synthesis must specify four substat types");
        }
        Set<Stat> uniqueSubstats = new HashSet<>(selectedSubstats);
        if (uniqueSubstats.size() != selectedSubstats.size()) {
            throw new IllegalArgumentException("Targeted substats must be unique");
        }
        List<Stat> mains = new ArrayList<>();
        for (Stat stat : Stat.values()) {
            if (part.acceptsMainStat(stat) && !uniqueSubstats.contains(stat)) mains.add(stat);
        }
        if (mains.isEmpty()) throw new IllegalArgumentException("No legal main stat remains");
        Stat mainStat = forcedMainStat == null ? mains.get(random.nextInt(mains.size())) : forcedMainStat;
        if (!part.acceptsMainStat(mainStat) || uniqueSubstats.contains(mainStat)) {
            throw new IllegalArgumentException("Invalid or conflicting relic main stat");
        }

        int initialCount = selectedSubstats.isEmpty() ? initialSubstatCount(stars, random) : 4;
        List<SubStat> substats = new ArrayList<>(4);
        for (int i = 0; i < initialCount; i++) {
            Stat stat = selectedSubstats.isEmpty()
                    ? chooseNewSubstat(mainStat, substats, random)
                    : selectedSubstats.get(i);
            substats.add(new SubStat(stat, rollSubstat(stat, stars, random), 0, List.of()));
        }
        return new Data(UUID.randomUUID(), set, part, stars, 0, 0, mainStat,
                substats, false, 0, 0L, List.of());
    }

    /** Adds relic XP and resolves every crossed +3 substat node exactly once. */
    public static Data addExperience(Data data, int offeredExperience, RandomSource random) {
        if (data == null || random == null || offeredExperience < 0) {
            throw new IllegalArgumentException("Invalid relic enhancement request");
        }
        if (offeredExperience == 0 || data.level() >= maxLevel(data.stars())) return data;
        if (data.hasPendingReroll()) throw new IllegalStateException("Resolve the pending reroll first");

        int maxExperience = xpAtLevel(data.stars(), maxLevel(data.stars()));
        int acceptedExperience = Math.min(offeredExperience, maxExperience - data.experience());
        if (acceptedExperience <= 0) return data;
        int newExperience = data.experience() + acceptedExperience;
        int newLevel = data.level();
        while (newLevel < maxLevel(data.stars())
                && newExperience >= xpAtLevel(data.stars(), newLevel + 1)) {
            newLevel++;
        }

        List<SubStat> substats = new ArrayList<>(data.substats());
        for (int level = data.level() + 1; level <= newLevel; level++) {
            if (level % 3 != 0) continue;
            if (substats.size() < 4) {
                Stat stat = chooseNewSubstat(data.mainStat(), substats, random);
                substats.add(new SubStat(stat, rollSubstat(stat, data.stars(), random), level, List.of()));
            } else {
                int index = random.nextInt(substats.size());
                SubStat old = substats.get(index);
                List<Double> upgrades = new ArrayList<>(old.upgradeRolls());
                upgrades.add(rollSubstat(old.stat(), data.stars(), random));
                substats.set(index, new SubStat(old.stat(), old.baseValue(), old.firstLevel(), upgrades));
            }
        }
        return new Data(data.instanceId(), data.set(), data.part(), data.stars(), newLevel,
                newExperience, data.mainStat(), substats, data.locked(), data.rerolls(),
                data.operationVersion() + 1, List.of());
    }

    public static Data withLocked(Data data, boolean locked) {
        if (data.hasPendingReroll()) throw new IllegalStateException("Resolve the pending reroll first");
        return new Data(data.instanceId(), data.set(), data.part(), data.stars(), data.level(),
                data.experience(), data.mainStat(), data.substats(), locked, data.rerolls(),
                data.operationVersion() + 1, List.of());
    }

    /** Advances the stale-request guard when a relic changes equipment state. */
    public static Data touch(Data data) {
        return new Data(data.instanceId(), data.set(), data.part(), data.stars(), data.level(),
                data.experience(), data.mainStat(), data.substats(), data.locked(), data.rerolls(),
                data.operationVersion() + 1, data.pendingReroll());
    }

    /** Generates and persists one reroll candidate while preserving all four substat identities and bases. */
    public static Data prepareReroll(Data data, RandomSource random) {
        if (data.stars() != 5 || data.level() != maxLevel(5) || data.locked()
                || data.hasPendingReroll() || data.substats().size() != 4) {
            throw new IllegalStateException("Relic is not eligible for a substat reroll");
        }
        List<SubStat> candidate = new ArrayList<>(4);
        for (SubStat substat : data.substats()) {
            candidate.add(new SubStat(substat.stat(), substat.baseValue(), substat.firstLevel(), List.of()));
        }
        boolean startedWithFour = data.substats().stream().allMatch(substat -> substat.firstLevel() == 0);
        int enhancementEvents = startedWithFour ? 5 : 4;
        for (int i = 0; i < enhancementEvents; i++) {
            int index = random.nextInt(candidate.size());
            SubStat old = candidate.get(index);
            List<Double> rolls = new ArrayList<>(old.upgradeRolls());
            rolls.add(rollSubstat(old.stat(), data.stars(), random));
            candidate.set(index, new SubStat(old.stat(), old.baseValue(), old.firstLevel(), rolls));
        }
        return new Data(data.instanceId(), data.set(), data.part(), data.stars(), data.level(),
                data.experience(), data.mainStat(), data.substats(), data.locked(), data.rerolls() + 1,
                data.operationVersion() + 1, candidate);
    }

    public static Data resolveReroll(Data data, boolean acceptCandidate) {
        if (!data.hasPendingReroll()) throw new IllegalStateException("No pending reroll candidate");
        return new Data(data.instanceId(), data.set(), data.part(), data.stars(), data.level(),
                data.experience(), data.mainStat(), acceptCandidate ? data.pendingReroll() : data.substats(),
                data.locked(), data.rerolls(), data.operationVersion() + 1, List.of());
    }

    private static int initialSubstatCount(int stars, RandomSource random) {
        if (stars == 2) return 0;
        boolean extra = random.nextInt(100) >= 80;
        return switch (stars) {
            case 3 -> extra ? 2 : 1;
            case 4 -> extra ? 3 : 2;
            case 5 -> extra ? 4 : 3;
            default -> throw new IllegalArgumentException("Unsupported relic rarity: " + stars);
        };
    }

    private static Stat chooseNewSubstat(Stat mainStat, List<SubStat> current, RandomSource random) {
        List<Stat> eligible = new ArrayList<>();
        Set<Stat> excluded = new HashSet<>();
        excluded.add(mainStat);
        current.forEach(substat -> excluded.add(substat.stat()));
        for (Stat stat : Stat.values()) if (!excluded.contains(stat)) eligible.add(stat);
        if (eligible.isEmpty()) throw new IllegalStateException("No eligible relic substats remain");
        return eligible.get(random.nextInt(eligible.size()));
    }

    private static double rollSubstat(Stat stat, int stars, RandomSource random) {
        double middle = switch (stat) {
            case HP -> 1.2;
            case HP_PCT, ATTACK_PCT, ARMOR_PCT, CRIT_DAMAGE -> 3.0;
            case ATTACK -> 0.3;
            case ARMOR -> 0.5;
            case TOUGHNESS -> 0.2;
            case TOUGHNESS_PCT -> 2.0;
            case CRIT -> 1.5;
            case HIT, RESIST, HEAL -> 2.0;
            case BREAK -> 3.0;
        };
        double quality = switch (stars) {
            case 2 -> 0.4;
            case 3 -> 0.6;
            case 4 -> 0.8;
            case 5 -> 1.0;
            default -> throw new IllegalArgumentException("Unsupported relic rarity: " + stars);
        };
        double tier = switch (random.nextInt(3)) {
            case 0 -> 0.8;
            case 1 -> 1.0;
            default -> 1.2;
        };
        return middle * quality * tier;
    }

    public static Optional<Data> read(ItemStack stack) {
        if (!(stack.getItem() instanceof RelicItem relic)) return Optional.empty();
        CompoundTag root = stack.getTagElement(ROOT);
        return decode(root).filter(data -> relic.matches(data.set(), data.part()));
    }

    public static void write(ItemStack stack, Data data) {
        if (!(stack.getItem() instanceof RelicItem relic) || !relic.matches(data.set(), data.part())) {
            throw new IllegalArgumentException("Relic data does not match the registered item");
        }
        stack.getOrCreateTag().put(ROOT, encode(data));
    }

    /** NBT representation shared by item persistence and the bounded player relic snapshot. */
    public static CompoundTag encode(Data data) {
        CompoundTag root = new CompoundTag();
        root.putInt("DataVersion", DATA_VERSION);
        root.putUUID("InstanceId", data.instanceId());
        root.putString("Set", data.set().id());
        root.putString("Part", data.part().id());
        root.putInt("Stars", data.stars());
        root.putInt("Level", data.level());
        root.putInt("Experience", data.experience());
        root.putString("MainStat", data.mainStat().id());
        root.put("Substats", writeSubstats(data.substats()));
        root.putBoolean("Locked", data.locked());
        root.putInt("Rerolls", data.rerolls());
        root.putLong("OperationVersion", data.operationVersion());
        root.put("PendingReroll", writeSubstats(data.pendingReroll()));
        return root;
    }

    /** Decodes a validated relic snapshot entry without requiring an ItemStack on the client. */
    public static Optional<Data> decode(CompoundTag root) {
        if (root == null || root.getInt("DataVersion") != DATA_VERSION) return Optional.empty();
        try {
            SetId set = SetId.byId(root.getString("Set")).orElseThrow();
            Part part = Part.byId(root.getString("Part")).orElseThrow();
            Stat main = Stat.byId(root.getString("MainStat")).orElseThrow();
            if (set.inner() != part.inner() || !root.hasUUID("InstanceId")) return Optional.empty();
            return Optional.of(new Data(root.getUUID("InstanceId"), set, part,
                    root.getInt("Stars"), root.getInt("Level"), root.getInt("Experience"), main,
                    readSubstats(root.getList("Substats", Tag.TAG_COMPOUND)), root.getBoolean("Locked"),
                    root.getInt("Rerolls"), root.getLong("OperationVersion"),
                    readSubstats(root.getList("PendingReroll", Tag.TAG_COMPOUND))));
        } catch (RuntimeException invalidData) {
            return Optional.empty();
        }
    }

    public static int maxLevel(int stars) {
        if (stars < 2 || stars > 5) throw new IllegalArgumentException("Unsupported relic rarity: " + stars);
        return XP_TO_NEXT_LEVEL[stars].length;
    }

    public static int xpAtLevel(int stars, int level) {
        int cap = maxLevel(stars);
        if (level < 0 || level > cap) throw new IllegalArgumentException("Invalid relic level: " + level);
        int total = 0;
        for (int i = 0; i < level; i++) total += XP_TO_NEXT_LEVEL[stars][i];
        return total;
    }

    private static void validateSubstats(List<SubStat> stats, Stat mainStat, int level, int cap) {
        if (stats.size() > 4) throw new IllegalArgumentException("Too many relic substats");
        Set<Stat> seen = new HashSet<>();
        for (SubStat substat : stats) {
            if (substat.stat() == mainStat || !seen.add(substat.stat())
                    || substat.firstLevel() > cap || substat.firstLevel() > level
                    || substat.upgradeRolls().size() > (level - substat.firstLevel()) / 3) {
                throw new IllegalArgumentException("Invalid or repeated relic substat");
            }
        }
    }

    private static ListTag writeSubstats(List<SubStat> stats) {
        ListTag result = new ListTag();
        for (SubStat stat : stats) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Stat", stat.stat().id());
            entry.putDouble("Base", stat.baseValue());
            entry.putInt("FirstLevel", stat.firstLevel());
            ListTag upgrades = new ListTag();
            stat.upgradeRolls().forEach(value -> upgrades.add(DoubleTag.valueOf(value)));
            entry.put("Upgrades", upgrades);
            result.add(entry);
        }
        return result;
    }

    private static List<SubStat> readSubstats(ListTag entries) {
        List<SubStat> result = new ArrayList<>(entries.size());
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            Stat stat = Stat.byId(entry.getString("Stat")).orElseThrow();
            ListTag upgrades = entry.getList("Upgrades", Tag.TAG_DOUBLE);
            List<Double> rolls = new ArrayList<>(upgrades.size());
            for (int j = 0; j < upgrades.size(); j++) rolls.add(upgrades.getDouble(j));
            result.add(new SubStat(stat, entry.getDouble("Base"), entry.getInt("FirstLevel"), rolls));
        }
        return result;
    }

    public static String format(double value) {
        String formatted = String.format(Locale.ROOT, "%.2f", value)
                .replaceAll("0+$", "").replaceAll("\\.$", "");
        return formatted;
    }
}
