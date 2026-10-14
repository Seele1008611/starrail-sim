package com.starrail.sim;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/** Rebuilds relic-derived attributes from the server-authoritative Curios loadout. */
public final class StarRailRelicService {
    private static final String SET_CACHE_TAG = "starrail_sim_relic_set_pieces";
    private static final String GUARD_HEAL_COOLDOWN_TAG = "starrail_sim_guard_heal_cooldown";
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
        int pieces = totals.pieces.getOrDefault(RelicStackData.SetId.PASSERBY, 0);
        if (pieces >= 2) totals.add(RelicStackData.Stat.HEAL, 15.0D);
        if (has(totals, RelicStackData.SetId.MUSKETEER, 2)) totals.add(RelicStackData.Stat.ATTACK_PCT, 12.0D);
        if (has(totals, RelicStackData.SetId.KNIGHT, 2)) totals.add(RelicStackData.Stat.ARMOR_PCT, 15.0D);
        if (has(totals, RelicStackData.SetId.SPACE_STATION, 2)) totals.add(RelicStackData.Stat.ATTACK_PCT, 24.0D);
        if (has(totals, RelicStackData.SetId.FLEET, 2)) totals.add(RelicStackData.Stat.HP_PCT, 20.0D);

        if (has(totals, RelicStackData.SetId.GENIUS, 2)) totals.add(RelicStackData.Stat.ATTACK_PCT, 15.0D);
        if (has(totals, RelicStackData.SetId.DEAD_WATER_PIONEER, 4)) {
            totals.add(RelicStackData.Stat.CRIT, 4.0D);
            totals.add(RelicStackData.Stat.CRIT_DAMAGE, 24.0D);
        }
        if (has(totals, RelicStackData.SetId.IRON_CAVALRY, 2)) totals.add(RelicStackData.Stat.BREAK, 25.0D);
        if (has(totals, RelicStackData.SetId.VALOROUS, 2)
                || has(totals, RelicStackData.SetId.HERO, 2)
                || has(totals, RelicStackData.SetId.NAVIGATOR, 2)) {
            if (has(totals, RelicStackData.SetId.VALOROUS, 2)) totals.add(RelicStackData.Stat.ATTACK_PCT, 12.0D);
            if (has(totals, RelicStackData.SetId.HERO, 2)) totals.add(RelicStackData.Stat.ATTACK_PCT, 12.0D);
            if (has(totals, RelicStackData.SetId.NAVIGATOR, 2)) totals.add(RelicStackData.Stat.ATTACK_PCT, 12.0D);
        }
        if (has(totals, RelicStackData.SetId.VALOROUS, 4)) totals.add(RelicStackData.Stat.CRIT, 6.0D);
        if (has(totals, RelicStackData.SetId.SCHOLAR, 2)
                || has(totals, RelicStackData.SetId.SAVIOR, 2)) {
            if (has(totals, RelicStackData.SetId.SCHOLAR, 2)) totals.add(RelicStackData.Stat.CRIT, 8.0D);
            if (has(totals, RelicStackData.SetId.SAVIOR, 2)) totals.add(RelicStackData.Stat.CRIT, 8.0D);
        }
        if (has(totals, RelicStackData.SetId.HERO, 4)) {
            totals.add(RelicStackData.Stat.ATTACK_PCT, 8.0D);
            totals.add(RelicStackData.Stat.CRIT_DAMAGE, 30.0D);
        }
        if (has(totals, RelicStackData.SetId.SAVIOR, 4)) totals.add(RelicStackData.Stat.HP_PCT, 24.0D);
        if (has(totals, RelicStackData.SetId.MAGICAL_GIRL, 2)) totals.add(RelicStackData.Stat.CRIT_DAMAGE, 16.0D);
        if (has(totals, RelicStackData.SetId.NAVIGATOR, 4)) totals.add(RelicStackData.Stat.CRIT_DAMAGE, 20.0D);
        if (has(totals, RelicStackData.SetId.BELOBOG, 2)) totals.add(RelicStackData.Stat.ARMOR_PCT, 30.0D);
        if (has(totals, RelicStackData.SetId.PENACONY, 2)) totals.add(RelicStackData.Stat.HP_PCT, 15.0D);
        if (has(totals, RelicStackData.SetId.AMPHOREUS, 2)) {
            totals.add(RelicStackData.Stat.CRIT, 8.0D);
            totals.add(RelicStackData.Stat.CRIT_DAMAGE, 24.0D);
        }
        if (has(totals, RelicStackData.SetId.LIVE_STREAM, 2)) totals.add(RelicStackData.Stat.CRIT_DAMAGE, 32.0D);

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

    /** Adds active set damage bonuses to outgoing damage. */
    public static float boostDamage(ServerPlayer player, float amount) {
        return boostDamage(player, null, amount);
    }

    /** Adds all active relic damage bonuses before vanilla armor is resolved. */
    public static float boostDamage(ServerPlayer player, LivingEntity target, float amount) {
        if (amount <= 0.0F) return amount;
        double bonus = 0.0D;
        if (equippedPieces(player, RelicStackData.SetId.MUSKETEER) >= 4) bonus += 0.10D;
        if (equippedPieces(player, RelicStackData.SetId.GENIUS) >= 2) bonus += 0.15D;
        if (target != null && equippedPieces(player, RelicStackData.SetId.DEAD_WATER_PIONEER) >= 2
                && target.getActiveEffects().stream().anyMatch(effect -> !effect.getEffect().isBeneficial())) {
            bonus += 0.12D;
        }
        if (equippedPieces(player, RelicStackData.SetId.IRON_CAVALRY) >= 4) bonus += 0.15D;
        if (equippedPieces(player, RelicStackData.SetId.VALOROUS) >= 4) bonus += 0.18D;
        if (equippedPieces(player, RelicStackData.SetId.SCHOLAR) >= 4) bonus += 0.20D;
        if (equippedPieces(player, RelicStackData.SetId.SAVIOR) >= 4) bonus += 0.15D;
        if (equippedPieces(player, RelicStackData.SetId.NAVIGATOR) >= 4) bonus += 0.18D;
        if (hasPenaconyDamageAura(player)) bonus += 0.10D;
        return amount * (float) (1.0D + bonus);
    }

    /** Converts armor penetration into an equivalent pre-armor amount. */
    public static float applyArmorPenetration(ServerPlayer attacker, LivingEntity target, float amount) {
        if (amount <= 0.0F) return amount;
        double penetration = 0.0D;
        if (equippedPieces(attacker, RelicStackData.SetId.GENIUS) >= 4) penetration += 0.20D;
        if (equippedPieces(attacker, RelicStackData.SetId.IRON_CAVALRY) >= 4) penetration += 0.10D;
        if (equippedPieces(attacker, RelicStackData.SetId.MAGICAL_GIRL) >= 4) penetration += 0.20D;
        if (penetration <= 0.0D || target.getAttribute(Attributes.ARMOR) == null) return amount;
        float armor = (float) target.getAttributeValue(Attributes.ARMOR);
        float toughness = (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        if (armor <= 0.0F) return amount;
        float desiredAfterArmor = CombatRules.getDamageAfterAbsorb(amount,
                armor * (float) Math.max(0.0D, 1.0D - Math.min(0.9D, penetration)), toughness);
        float low = amount;
        float high = Math.max(amount, 1.0F);
        for (int i = 0; i < 20 && CombatRules.getDamageAfterAbsorb(high, armor, toughness)
                < desiredAfterArmor && high < 1.0E9F; i++) {
            high *= 2.0F;
        }
        for (int i = 0; i < 28; i++) {
            float mid = (low + high) * 0.5F;
            if (CombatRules.getDamageAfterAbsorb(mid, armor, toughness) < desiredAfterArmor) low = mid;
            else high = mid;
        }
        return (low + high) * 0.5F;
    }

    /** Reduces incoming damage when the wearer has at least two Guard pieces. */
    public static float reduceIncomingDamage(ServerPlayer player, float amount) {
        return amount > 0.0F && equippedPieces(player, RelicStackData.SetId.GUARD) >= 2
                ? amount * 0.85F : amount;
    }

    /** Four-piece Guard auto-heal: 15% max health at or below 30%, once per 1200 ticks. */
    public static void tickGuardRecovery(ServerPlayer player) {
        if (!player.isAlive() || equippedPieces(player, RelicStackData.SetId.GUARD) < 4) return;
        double maxHealth = player.getMaxHealth();
        if (maxHealth <= 0.0D || player.getHealth() > maxHealth * 0.30D) return;
        long now = player.getServer().overworld().getGameTime();
        CompoundTag data = player.getPersistentData();
        if (now < data.getLong(GUARD_HEAL_COOLDOWN_TAG)) return;
        player.heal((float) (maxHealth * 0.15D));
        data.putLong(GUARD_HEAL_COOLDOWN_TAG, now + 1200L);
    }

    private static boolean hasPenaconyDamageAura(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(ServerPlayer.class,
                        player.getBoundingBox().inflate(10.0D),
                        candidate -> candidate.distanceToSqr(player) <= 100.0D
                                && equippedPieces(candidate, RelicStackData.SetId.PENACONY) >= 2)
                .size() > 0;
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

    private static boolean has(Totals totals, RelicStackData.SetId set, int pieces) {
        return totals.pieces.getOrDefault(set, 0) >= pieces;
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
