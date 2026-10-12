package com.starrail.sim;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/** Rebuilds relic-derived attributes from the server-authoritative Curios loadout. */
public final class StarRailRelicService {
    private static final String SET_CACHE_TAG = "starrail_sim_relic_set_pieces";
    private static final UUID MAX_HEALTH_FLAT = id("max_health.flat");
    private static final UUID MAX_HEALTH_PERCENT = id("max_health.percent");
    private static final UUID ATTACK_FLAT = id("attack.flat");
    private static final UUID ATTACK_PERCENT = id("attack.percent");
    private static final UUID ARMOR_FLAT = id("armor.flat");
    private static final UUID ARMOR_PERCENT = id("armor.percent");
    private static final UUID TOUGHNESS_FLAT = id("toughness.flat");
    private static final UUID TOUGHNESS_PERCENT = id("toughness.percent");
    private static final UUID CRIT_RATE = id("crit_rate");
    private static final UUID CRIT_DAMAGE = id("crit_damage");
    private static final UUID EFFECT_HIT = id("effect_hit");
    private static final UUID EFFECT_RESISTANCE = id("effect_resistance");
    private static final UUID BREAK_EFFECT = id("break_effect");
    private static final UUID HEALING_EFFECT = id("healing_effect");

    private StarRailRelicService() {}

    /** Replaces only this service's transient modifiers; player base values are untouched. */
    public static void refresh(ServerPlayer player) {
        if (player.level().isClientSide() || player.isRemoved()) return;

        Totals totals = collect(player);
        saveSetCounts(player, totals.pieces);
        int pieces;
        pieces = totals.pieces.getOrDefault(RelicStackData.SetId.PASSERBY, 0);
        if (pieces >= 2) totals.add(RelicStackData.Stat.HEAL, 15.0D);
        pieces = totals.pieces.getOrDefault(RelicStackData.SetId.MUSKETEER, 0);
        if (pieces >= 2) totals.add(RelicStackData.Stat.ATTACK_PCT, 12.0D);
        pieces = totals.pieces.getOrDefault(RelicStackData.SetId.KNIGHT, 0);
        if (pieces >= 2) totals.add(RelicStackData.Stat.ARMOR_PCT, 15.0D);
        pieces = totals.pieces.getOrDefault(RelicStackData.SetId.SPACE_STATION, 0);
        if (pieces >= 2) totals.add(RelicStackData.Stat.ATTACK_PCT, 24.0D);
        pieces = totals.pieces.getOrDefault(RelicStackData.SetId.FLEET, 0);
        if (pieces >= 2) totals.add(RelicStackData.Stat.HP_PCT, 20.0D);

        apply(player, Attributes.MAX_HEALTH, MAX_HEALTH_FLAT, MAX_HEALTH_PERCENT,
                "starrail_sim.relic.max_health", totals.get(RelicStackData.Stat.HP),
                totals.fraction(RelicStackData.Stat.HP_PCT));
        apply(player, Attributes.ATTACK_DAMAGE, ATTACK_FLAT, ATTACK_PERCENT,
                "starrail_sim.relic.attack", totals.get(RelicStackData.Stat.ATTACK),
                totals.fraction(RelicStackData.Stat.ATTACK_PCT));
        apply(player, Attributes.ARMOR, ARMOR_FLAT, ARMOR_PERCENT,
                "starrail_sim.relic.armor", totals.get(RelicStackData.Stat.ARMOR),
                totals.fraction(RelicStackData.Stat.ARMOR_PCT));
        apply(player, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_FLAT, TOUGHNESS_PERCENT,
                "starrail_sim.relic.armor_toughness", totals.get(RelicStackData.Stat.TOUGHNESS),
                totals.fraction(RelicStackData.Stat.TOUGHNESS_PCT));

        applyAddition(player, StarRailAttributes.CRIT_RATE.get(), CRIT_RATE,
                "starrail_sim.relic.crit_rate", totals.fraction(RelicStackData.Stat.CRIT));
        applyAddition(player, StarRailAttributes.CRIT_DAMAGE.get(), CRIT_DAMAGE,
                "starrail_sim.relic.crit_damage", totals.fraction(RelicStackData.Stat.CRIT_DAMAGE));
        applyAddition(player, StarRailAttributes.EFFECT_HIT_RATE.get(), EFFECT_HIT,
                "starrail_sim.relic.effect_hit", totals.fraction(RelicStackData.Stat.HIT));
        applyAddition(player, StarRailAttributes.EFFECT_RESISTANCE.get(), EFFECT_RESISTANCE,
                "starrail_sim.relic.effect_resistance", totals.fraction(RelicStackData.Stat.RESIST));
        applyAddition(player, StarRailAttributes.BREAK_EFFECT.get(), BREAK_EFFECT,
                "starrail_sim.relic.break_effect", totals.fraction(RelicStackData.Stat.BREAK));
        applyAddition(player, StarRailAttributes.HEALING_EFFECT.get(), HEALING_EFFECT,
                "starrail_sim.relic.healing_effect", totals.fraction(RelicStackData.Stat.HEAL));
    }

    /** Current number of valid equipped pieces, used by later combat-triggered set effects. */
    public static int equippedPieces(ServerPlayer player, RelicStackData.SetId set) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(SET_CACHE_TAG, Tag.TAG_COMPOUND)) {
            Totals totals = collect(player);
            saveSetCounts(player, totals.pieces);
        }
        return root.getCompound(SET_CACHE_TAG).getInt(set.id());
    }

    /** Applies the confirmed Knight four-piece shield bonus to a shield grant. */
    public static float boostShieldAmount(ServerPlayer player, float amount) {
        return amount > 0.0F && equippedPieces(player, RelicStackData.SetId.KNIGHT) >= 4
                ? amount * 1.20F : amount;
    }

    /** Applies the confirmed Musketeer four-piece damage bonus to outgoing damage. */
    public static float boostDamage(ServerPlayer player, float amount) {
        return amount > 0.0F && equippedPieces(player, RelicStackData.SetId.MUSKETEER) >= 4
                ? amount * 1.10F : amount;
    }

    /** Applies the confirmed Passerby four-piece nearby-player heal on a hostile kill. */
    public static void onHostileKill(ServerPlayer player) {
        if (equippedPieces(player, RelicStackData.SetId.PASSERBY) < 4) return;
        float amount = player.getMaxHealth() * 0.05F;
        if (amount <= 0.0F) return;
        player.level().getEntitiesOfClass(ServerPlayer.class,
                        player.getBoundingBox().inflate(10.0D),
                        ally -> ally.distanceToSqr(player) <= 100.0D)
                .forEach(ally -> ally.heal(amount));
    }

    private static Totals collect(ServerPlayer player) {
        Totals totals = new Totals();
        CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
            for (RelicStackData.Part part : RelicStackData.Part.values()) {
                inventory.getStacksHandler(part.curiosSlot()).ifPresent(handler -> {
                    for (int slot = 0; slot < handler.getSlots(); slot++) {
                        ItemStack stack = handler.getStacks().getStackInSlot(slot);
                        RelicStackData.read(stack).ifPresent(data -> {
                            if (data.part() != part) return;
                            totals.pieces.merge(data.set(), 1, Integer::sum);
                            totals.add(data.mainStat(), data.mainValue());
                            for (RelicStackData.SubStat substat : data.substats()) {
                                totals.add(substat.stat(), substat.value());
                            }
                        });
                    }
                });
            }
        });
        return totals;
    }

    private static void apply(ServerPlayer player, Attribute attribute, UUID flatId, UUID percentId,
                              String name, double flat, double percent) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        applyModifier(instance, flatId, name + ".flat", flat, AttributeModifier.Operation.ADDITION);
        // Percent pieces and their matching set bonus are summed first, so a full loadout
        // contributes one multiplier instead of compounding once per relic.
        applyModifier(instance, percentId, name + ".percent", percent,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    private static void applyAddition(ServerPlayer player, Attribute attribute, UUID id,
                                      String name, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) applyModifier(instance, id, name, amount,
                AttributeModifier.Operation.ADDITION);
    }

    private static void applyModifier(AttributeInstance instance, UUID id, String name,
                                      double amount, AttributeModifier.Operation operation) {
        AttributeModifier existing = instance.getModifier(id);
        if (amount == 0.0D) {
            if (existing != null) instance.removeModifier(existing);
            return;
        }
        if (existing != null && Double.compare(existing.getAmount(), amount) == 0
                && existing.getOperation() == operation) return;
        if (existing != null) instance.removeModifier(existing);
        instance.addTransientModifier(new AttributeModifier(id, name, amount, operation));
    }

    private static UUID id(String suffix) {
        return UUID.nameUUIDFromBytes((StarRailSimMod.MOD_ID + ":relic:" + suffix)
                .getBytes(StandardCharsets.UTF_8));
    }

    private static void saveSetCounts(ServerPlayer player,
                                      Map<RelicStackData.SetId, Integer> pieces) {
        CompoundTag cached = new CompoundTag();
        for (RelicStackData.SetId set : RelicStackData.SetId.values()) {
            cached.putInt(set.id(), pieces.getOrDefault(set, 0));
        }
        player.getPersistentData().put(SET_CACHE_TAG, cached);
    }

    private static final class Totals {
        private final EnumMap<RelicStackData.Stat, Double> values =
                new EnumMap<>(RelicStackData.Stat.class);
        private final EnumMap<RelicStackData.SetId, Integer> pieces =
                new EnumMap<>(RelicStackData.SetId.class);

        private void add(RelicStackData.Stat stat, double amount) {
            if (!Double.isFinite(amount) || amount <= 0.0D) return;
            values.merge(stat, Math.min(1_000_000.0D, amount), Double::sum);
        }

        private double get(RelicStackData.Stat stat) {
            return values.getOrDefault(stat, 0.0D);
        }

        private double fraction(RelicStackData.Stat stat) {
            return get(stat) / 100.0D;
        }
    }
}
