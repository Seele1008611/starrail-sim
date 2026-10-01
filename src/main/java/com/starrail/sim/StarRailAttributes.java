package com.starrail.sim;

/**
 * 模组代码说明：定义并注册模组需要附加到玩家身上的战斗属性。
 */

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Central registry and access point for the mod's combat attributes.
 *
 * <p>Vanilla attributes such as max health and attack damage remain the
 * source of truth for their respective values. This class owns the additional
 * Star Rail-inspired attributes so Light Cones, Paths, and the future stats UI
 * all use the same definitions.</p>
 */
public final class StarRailAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, StarRailSimMod.MOD_ID);

    /** Base critical hit chance: 0.05 means 5%. */
    public static final double DEFAULT_CRIT_RATE = 0.05D;

    /** Bonus critical damage: 0.50 means an additional 50% damage on a crit. */
    public static final double DEFAULT_CRIT_DAMAGE = 0.50D;

    public static final RegistryObject<Attribute> CRIT_RATE = register(
            "crit_rate", "attribute.starrail_sim.crit_rate", DEFAULT_CRIT_RATE, 1.0D);

    public static final RegistryObject<Attribute> CRIT_DAMAGE = register(
            "crit_damage", "attribute.starrail_sim.crit_damage", DEFAULT_CRIT_DAMAGE, 10.0D);

    public static final RegistryObject<Attribute> BREAK_EFFECT = register(
            "break_effect", "attribute.starrail_sim.break_effect", 0.0D, 10.0D);

    public static final RegistryObject<Attribute> EFFECT_HIT_RATE = register(
            "effect_hit_rate", "attribute.starrail_sim.effect_hit_rate", 0.0D, 10.0D);

    public static final RegistryObject<Attribute> EFFECT_RESISTANCE = register(
            "effect_resistance", "attribute.starrail_sim.effect_resistance", 0.0D, 10.0D);

    /** Bonus to healing received, stored as a fraction: +5% is 0.05. */
    public static final RegistryObject<Attribute> HEALING_EFFECT = register(
            "healing_effect", "attribute.starrail_sim.healing_effect", 0.0D, 10.0D);

    private StarRailAttributes() {
    }

    private static RegistryObject<Attribute> register(
            String id, String translationKey, double defaultValue, double maxValue) {
        return ATTRIBUTES.register(id, () -> new RangedAttribute(
                translationKey, defaultValue, 0.0D, maxValue).setSyncable(true));
    }

    /** Adds every custom combat attribute to players with its defined base value. */
    public static void addPlayerAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, CRIT_RATE.get(), DEFAULT_CRIT_RATE);
        event.add(EntityType.PLAYER, CRIT_DAMAGE.get(), DEFAULT_CRIT_DAMAGE);
        event.add(EntityType.PLAYER, BREAK_EFFECT.get(), 0.0D);
        event.add(EntityType.PLAYER, EFFECT_HIT_RATE.get(), 0.0D);
        event.add(EntityType.PLAYER, EFFECT_RESISTANCE.get(), 0.0D);
        event.add(EntityType.PLAYER, HEALING_EFFECT.get(), 0.0D);
    }

    /** Returns an attribute's effective value, or its default when unavailable. */
    public static double getValue(LivingEntity entity, RegistryObject<Attribute> attribute,
                                  double fallback) {
        var instance = entity.getAttribute(attribute.get());
        return instance == null ? fallback : instance.getValue();
    }

    public static double getCritRate(LivingEntity entity) {
        return getValue(entity, CRIT_RATE, DEFAULT_CRIT_RATE);
    }

    public static double getCritDamage(LivingEntity entity) {
        return getValue(entity, CRIT_DAMAGE, DEFAULT_CRIT_DAMAGE);
    }

    /**
     * Returns attack damage including the main-hand item's attack modifier.
     *
     * <p>The player attribute instance is still used as the base so modifiers
     * from paths, light cones, and other systems are preserved. The held item
     * is only added when its modifier is not already present in that instance,
     * avoiding double counting on versions or items that synchronize it.</p>
     */
    public static double getAttackDamage(Player player) {
        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance == null) {
            return 0.0D;
        }

        double value = instance.getValue();
        ItemStack mainHand = player.getMainHandItem();
        value = applyMissingItemModifiers(value, instance, mainHand,
                AttributeModifier.Operation.ADDITION);
        value = applyMissingItemModifiers(value, instance, mainHand,
                AttributeModifier.Operation.MULTIPLY_BASE);
        value = applyMissingItemModifiers(value, instance, mainHand,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        return value;
    }

    /**
     * Returns the attack-damage stat multiplier for projectiles, excluding the
     * currently held item's own attack-damage modifiers. Vanilla projectile
     * damage does not read the shooter's attack-damage attribute, so this
     * carries player/path/light-cone attack buffs into ranged hits without
     * treating a held sword as extra arrow damage.
     */
    public static double getProjectileAttackDamageMultiplier(Player player) {
        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance == null || instance.getBaseValue() <= 0.0D) {
            return 1.0D;
        }

        Set<UUID> heldItemModifiers = new HashSet<>();
        for (AttributeModifier modifier : player.getMainHandItem()
                .getAttributeModifiers(EquipmentSlot.MAINHAND)
                .get(Attributes.ATTACK_DAMAGE)) {
            heldItemModifiers.add(modifier.getId());
        }

        double baseValue = instance.getBaseValue();
        double additive = 0.0D;
        double multiplyBase = 0.0D;
        double multiplyTotal = 1.0D;
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (heldItemModifiers.contains(modifier.getId())) {
                continue;
            }
            switch (modifier.getOperation()) {
                case ADDITION -> additive += modifier.getAmount();
                case MULTIPLY_BASE -> multiplyBase += modifier.getAmount();
                case MULTIPLY_TOTAL -> multiplyTotal *= 1.0D + modifier.getAmount();
            }
        }

        double effectiveAttackDamage = (baseValue + additive
                + baseValue * multiplyBase) * multiplyTotal;
        return Math.max(0.0D, effectiveAttackDamage / baseValue);
    }

    private static double applyMissingItemModifiers(double value, AttributeInstance instance,
                                                    ItemStack stack,
                                                    AttributeModifier.Operation operation) {
        for (AttributeModifier modifier : stack
                .getAttributeModifiers(EquipmentSlot.MAINHAND)
                .get(Attributes.ATTACK_DAMAGE)) {
            if (modifier.getOperation() == operation
                    && instance.getModifier(modifier.getId()) == null) {
                value = switch (operation) {
                    case ADDITION -> value + modifier.getAmount();
                    case MULTIPLY_BASE -> value + instance.getBaseValue() * modifier.getAmount();
                    case MULTIPLY_TOTAL -> value * (1.0D + modifier.getAmount());
                };
            }
        }
        return value;
    }
}
