package com.starrail.sim;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/** Applies path attributes, including rank-based cumulative effects. */
public final class StarRailPathEffects {
    private static final UUID HUNT_CRIT_RATE_MODIFIER =
            UUID.fromString("36c7a7bb-35a7-4a9b-8a8e-6d19a6de4f01");
    private static final UUID HUNT_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0be5f9db-5111-4c0d-9f4b-9f9b7f74db1b");
    private static final UUID HUNT_ATTACK_DAMAGE_MODIFIER =
            UUID.fromString("a4e1eaf0-3a58-4ef0-9be7-2d3cb3d0ad62");
    private static final UUID PRESERVATION_ARMOR_MODIFIER =
            UUID.fromString("a84fdf56-63d9-4f42-9690-f5f2813f1a16");
    private static final UUID PRESERVATION_ARMOR_TOUGHNESS_MODIFIER =
            UUID.fromString("c3e3f3f2-83d6-4b3d-a5b7-4d2a6c8ef901");
    private static final UUID ABUNDANCE_HEALTH_MODIFIER =
            UUID.fromString("f8c1e99e-2f33-4ea2-9e63-f2f32c3b9f72");
    private static final UUID ABUNDANCE_HEALING_EFFECT_MODIFIER =
            UUID.fromString("8b4e6d21-7f30-4a95-b2c8-1d6e9f037452");
    private static final UUID DESTRUCTION_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("6d8bbf91-92ee-4bce-bbe1-1c83bdf3f6e4");
    private static final UUID DESTRUCTION_ATTACK_DAMAGE_MODIFIER =
            UUID.fromString("d7c1a2e4-6b3f-4e98-a4d7-2f6c8b1e9053");
    private static final UUID ERUDITION_ATTACK_DAMAGE_MODIFIER =
            UUID.fromString("4d3a1e87-3b88-4f58-9f2c-6a2c2b7f8101");
    private static final UUID ERUDITION_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("b0a654a5-17dc-4f0d-9a2c-8af0bc1d7e73");
    private static final UUID ERUDITION_CRIT_RATE_MODIFIER =
            UUID.fromString("d8e7c1a2-4b67-4f39-9c5a-2e6d7b8f9012");
    private static final UUID NIHILITY_EFFECT_HIT_RATE_MODIFIER =
            UUID.fromString("9df4c3d2-9e8f-4d63-9d5e-bb50dc53c1e4");
    private static final UUID NIHILITY_ATTACK_DAMAGE_MODIFIER =
            UUID.fromString("a7d2e9f4-6c18-4b53-9e70-1f8a2c6d9043");
    private static final UUID NIHILITY_BREAK_EFFECT_MODIFIER =
            UUID.fromString("c1f5b8e2-7a34-4d69-8c20-5e9b3f1a7264");
    /** Harmony percentage bonus: defensive and deliberately not movement speed. */
    private static final UUID HARMONY_ARMOR_MODIFIER =
            UUID.fromString("f0e0b14a-5e0d-49bb-9d35-5e6bdfcb89fe");
    private static final UUID HARMONY_MAX_HEALTH_MODIFIER =
            UUID.fromString("2a3c8d95-4b16-4a71-9e30-6f7c2d1b8054");
    private static final UUID HARMONY_ARMOR_TOUGHNESS_MODIFIER =
            UUID.fromString("5e71c4a2-9d83-4f06-b2c8-1a6e7d930245");
    /** Old UUID kept only to remove the previous movement-speed bonus. */
    private static final UUID HARMONY_MOVEMENT_SPEED_MODIFIER =
            UUID.fromString("ca1dbf49-b0d6-4c73-94f6-4e0e8be5e00f");
    private static final UUID REMEMBRANCE_ATTACK_DAMAGE_MODIFIER =
            UUID.fromString("e423e8ae-58a0-4685-8dbb-d2fb924b5cf6");
    private static final UUID REMEMBRANCE_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("b7d2f6a4-91c3-4e58-8a0f-6d2b9c174503");
    private static final UUID ELATION_CRIT_RATE_MODIFIER =
            UUID.fromString("7c6f752a-f616-4a0b-9e0c-38f9b7f0112d");
    private static final UUID ELATION_ATTACK_DAMAGE_MODIFIER =
            UUID.fromString("0b84e0bf-9a62-44dd-a5d9-f2b8c4c9e731");
    private static final UUID ELATION_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("5c0d7f16-2a89-4d3e-8b54-9e7a1f6c203d");
    private static final UUID PINNACLE_PRACTICE_ATTACK_DAMAGE_MODIFIER =
            UUID.fromString("c2a7e1f4-8b35-4d92-a6c1-7e0f5b3d9148");

    private StarRailPathEffects() {
    }

    public static void refresh(Player player, StarRailPath path) {
        StarRailPathRank rank = player.getCapability(StarRailPathCapability.PATH_DATA)
                .map(data -> data.getPathRank(path))
                .orElse(StarRailPathRank.UNALIGNED);
        int pinnaclePracticeCount = player.getCapability(StarRailPathCapability.PATH_DATA)
                .map(data -> data.getPinnaclePracticeCount(path))
                .orElse(0);
        refresh(player, path, rank, pinnaclePracticeCount);
    }

    public static void refresh(Player player, StarRailPath path, StarRailPathRank rank) {
        int pinnaclePracticeCount = player.getCapability(StarRailPathCapability.PATH_DATA)
                .map(data -> data.getPinnaclePracticeCount(path))
                .orElse(0);
        refresh(player, path, rank, pinnaclePracticeCount);
    }

    public static void refresh(Player player, StarRailPath path, StarRailPathRank rank,
                               int pinnaclePracticeCount) {
        int level = rank == null ? 0 : rank.getLevel();

        AttributeInstance critRate = player.getAttribute(StarRailAttributes.CRIT_RATE.get());
        if (critRate != null) {
            applyModifier(critRate, HUNT_CRIT_RATE_MODIFIER,
                    "starrail_sim.path.hunt.crit_rate", huntCritRate(level),
                    AttributeModifier.Operation.ADDITION, path == StarRailPath.HUNT);
        }

        AttributeInstance huntCritDamage =
                player.getAttribute(StarRailAttributes.CRIT_DAMAGE.get());
        if (huntCritDamage != null) {
            applyModifier(huntCritDamage, HUNT_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.path.hunt.crit_damage", huntCritDamage(level),
                    AttributeModifier.Operation.ADDITION, path == StarRailPath.HUNT);
        }

        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            applyModifier(armor, PRESERVATION_ARMOR_MODIFIER,
                    "starrail_sim.path.preservation.armor", preservationArmor(level),
                    AttributeModifier.Operation.MULTIPLY_TOTAL, path == StarRailPath.PRESERVATION);
            applyModifier(armor, HARMONY_ARMOR_MODIFIER,
                    "starrail_sim.path.harmony.armor", harmonyArmor(level),
                    AttributeModifier.Operation.MULTIPLY_TOTAL, path == StarRailPath.HARMONY);
        }

        AttributeInstance armorToughness = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armorToughness != null) {
            applyModifier(armorToughness, PRESERVATION_ARMOR_TOUGHNESS_MODIFIER,
                    "starrail_sim.path.preservation.armor_toughness",
                    preservationArmorToughness(level),
                    AttributeModifier.Operation.MULTIPLY_TOTAL,
                    path == StarRailPath.PRESERVATION);
            applyModifier(armorToughness, HARMONY_ARMOR_TOUGHNESS_MODIFIER,
                    "starrail_sim.path.harmony.armor_toughness",
                    harmonyArmorToughness(level),
                    AttributeModifier.Operation.MULTIPLY_TOTAL,
                    path == StarRailPath.HARMONY);
        }

        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            applyModifier(maxHealth, ABUNDANCE_HEALTH_MODIFIER,
                    "starrail_sim.path.abundance.max_health",
                    abundanceMaxHealth(level),
                    AttributeModifier.Operation.MULTIPLY_TOTAL,
                    path == StarRailPath.ABUNDANCE);
            applyModifier(maxHealth, HARMONY_MAX_HEALTH_MODIFIER,
                    "starrail_sim.path.harmony.max_health",
                    harmonyMaxHealth(level),
                    AttributeModifier.Operation.MULTIPLY_TOTAL,
                    path == StarRailPath.HARMONY);
        }

        AttributeInstance healingEffect = player.getAttribute(
                StarRailAttributes.HEALING_EFFECT.get());
        if (healingEffect != null) {
            applyModifier(healingEffect, ABUNDANCE_HEALING_EFFECT_MODIFIER,
                    "starrail_sim.path.abundance.healing_effect",
                    abundanceHealingEffect(level),
                    AttributeModifier.Operation.ADDITION,
                    path == StarRailPath.ABUNDANCE);
        }

        AttributeInstance critDamage = player.getAttribute(StarRailAttributes.CRIT_DAMAGE.get());
        if (critDamage != null) {
            applyModifier(critDamage, DESTRUCTION_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.path.destruction.crit_damage",
                    destructionCritDamage(level),
                    AttributeModifier.Operation.ADDITION,
                    path == StarRailPath.DESTRUCTION);
        }

        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            // Remove the temporary attack-speed implementation from the first
            // Erudition prototype, including on worlds that already used it.
            AttributeModifier oldEruditionModifier = attackSpeed.getModifier(
                    ERUDITION_CRIT_DAMAGE_MODIFIER);
            if (oldEruditionModifier != null) {
                attackSpeed.removeModifier(oldEruditionModifier);
            }
        }

        AttributeInstance eruditionCritRate =
                player.getAttribute(StarRailAttributes.CRIT_RATE.get());
        if (eruditionCritRate != null) {
            applyModifier(eruditionCritRate, ERUDITION_CRIT_RATE_MODIFIER,
                    "starrail_sim.path.erudition.crit_rate",
                    eruditionCritRate(level),
                    AttributeModifier.Operation.ADDITION,
                    path == StarRailPath.ERUDITION);
        }

        AttributeInstance eruditionCritDamage =
                player.getAttribute(StarRailAttributes.CRIT_DAMAGE.get());
        if (eruditionCritDamage != null) {
            applyModifier(eruditionCritDamage, ERUDITION_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.path.erudition.crit_damage",
                    eruditionCritDamage(level),
                    AttributeModifier.Operation.ADDITION,
                    path == StarRailPath.ERUDITION);
        }

        AttributeInstance effectHitRate =
                player.getAttribute(StarRailAttributes.EFFECT_HIT_RATE.get());
        if (effectHitRate != null) {
            applyModifier(effectHitRate, NIHILITY_EFFECT_HIT_RATE_MODIFIER,
                    "starrail_sim.path.nihility.effect_hit_rate",
                    nihilityEffectHitRate(level),
                    AttributeModifier.Operation.ADDITION,
                    path == StarRailPath.NIHILITY);
        }

        AttributeInstance breakEffect =
                player.getAttribute(StarRailAttributes.BREAK_EFFECT.get());
        if (breakEffect != null) {
            applyModifier(breakEffect, NIHILITY_BREAK_EFFECT_MODIFIER,
                    "starrail_sim.path.nihility.break_effect",
                    nihilityBreakEffect(level),
                    AttributeModifier.Operation.ADDITION,
                    path == StarRailPath.NIHILITY);
        }

        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            AttributeModifier oldHarmonyModifier =
                    movementSpeed.getModifier(HARMONY_MOVEMENT_SPEED_MODIFIER);
            if (oldHarmonyModifier != null) {
                movementSpeed.removeModifier(oldHarmonyModifier);
            }
        }

        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            applyModifier(attackDamage, HUNT_ATTACK_DAMAGE_MODIFIER,
                    "starrail_sim.path.hunt.attack_damage", huntAttackDamage(level),
                    AttributeModifier.Operation.MULTIPLY_TOTAL, path == StarRailPath.HUNT);
            applyModifier(attackDamage, DESTRUCTION_ATTACK_DAMAGE_MODIFIER,
                    "starrail_sim.path.destruction.attack_damage",
                    destructionAttackDamage(level),
                    AttributeModifier.Operation.MULTIPLY_TOTAL,
                    path == StarRailPath.DESTRUCTION);
            applyModifier(attackDamage, ERUDITION_ATTACK_DAMAGE_MODIFIER,
                    "starrail_sim.path.erudition.attack_damage",
                    eruditionAttackDamage(level),
                    AttributeModifier.Operation.MULTIPLY_TOTAL,
                    path == StarRailPath.ERUDITION);
            applyModifier(attackDamage, NIHILITY_ATTACK_DAMAGE_MODIFIER,
                    "starrail_sim.path.nihility.attack_damage",
                    nihilityAttackDamage(level),
                    AttributeModifier.Operation.MULTIPLY_TOTAL,
                    path == StarRailPath.NIHILITY);
            applyModifier(attackDamage, REMEMBRANCE_ATTACK_DAMAGE_MODIFIER,
                    "starrail_sim.path.remembrance.attack_damage",
                    remembranceAttackDamage(level),
                    AttributeModifier.Operation.MULTIPLY_TOTAL,
                    path == StarRailPath.REMEMBRANCE);
            applyModifier(attackDamage, ELATION_ATTACK_DAMAGE_MODIFIER,
                    "starrail_sim.path.elation.attack_damage",
                    elationAttackDamage(level),
                    AttributeModifier.Operation.MULTIPLY_TOTAL,
                    path == StarRailPath.ELATION);
            applyModifier(attackDamage, PINNACLE_PRACTICE_ATTACK_DAMAGE_MODIFIER,
                    "starrail_sim.path.pinnacle_practice_attack_damage",
                    StarRailPathProgress.pinnacleAttackBonus(pinnaclePracticeCount),
                    AttributeModifier.Operation.MULTIPLY_TOTAL,
                    path.isRealPath() && level >= StarRailPathRank.PATH_PINNACLE.getLevel());
        }

        AttributeInstance remembranceCritDamage = player.getAttribute(
                StarRailAttributes.CRIT_DAMAGE.get());
        if (remembranceCritDamage != null) {
            applyModifier(remembranceCritDamage, REMEMBRANCE_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.path.remembrance.crit_damage",
                    remembranceCritDamage(level),
                    AttributeModifier.Operation.ADDITION,
                    path == StarRailPath.REMEMBRANCE);
        }

        AttributeInstance elationCritDamage = player.getAttribute(
                StarRailAttributes.CRIT_DAMAGE.get());
        if (elationCritDamage != null) {
            applyModifier(elationCritDamage, ELATION_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.path.elation.crit_damage",
                    elationCritDamage(level),
                    AttributeModifier.Operation.ADDITION,
                    path == StarRailPath.ELATION);
        }

        AttributeInstance critRateAfterElation =
                player.getAttribute(StarRailAttributes.CRIT_RATE.get());
        if (critRateAfterElation != null) {
            applyModifier(critRateAfterElation, ELATION_CRIT_RATE_MODIFIER,
                    "starrail_sim.path.elation.crit_rate",
                    elationCritRate(level),
                    AttributeModifier.Operation.ADDITION,
                    path == StarRailPath.ELATION);
        }
    }

    private static double huntCritRate(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.15D
                : level >= StarRailPathRank.PRACTICE.getLevel() ? 0.10D
                : level >= StarRailPathRank.PATHFARING.getLevel() ? 0.05D : 0.0D;
    }

    private static double huntCritDamage(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.30D
                : level >= StarRailPathRank.DEEP_PRACTICE.getLevel() ? 0.20D
                : level >= StarRailPathRank.GLIMPSE.getLevel() ? 0.10D : 0.0D;
    }

    private static double huntAttackDamage(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.30D
                : level >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel() ? 0.20D
                : level >= StarRailPathRank.RESONANCE.getLevel() ? 0.10D : 0.0D;
    }

    private static double destructionCritDamage(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.40D
                : level >= StarRailPathRank.DEEP_PRACTICE.getLevel() ? 0.30D
                : level >= StarRailPathRank.RESONANCE.getLevel() ? 0.20D
                : level >= StarRailPathRank.PATHFARING.getLevel() ? 0.10D : 0.0D;
    }

    private static double destructionAttackDamage(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.20D
                : level >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel() ? 0.15D
                : level >= StarRailPathRank.PRACTICE.getLevel() ? 0.10D
                : level >= StarRailPathRank.GLIMPSE.getLevel() ? 0.05D : 0.0D;
    }

    private static double eruditionAttackDamage(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.25D
                : level >= StarRailPathRank.DEEP_PRACTICE.getLevel() ? 0.15D
                : level >= StarRailPathRank.RESONANCE.getLevel() ? 0.10D
                : level >= StarRailPathRank.PATHFARING.getLevel() ? 0.05D : 0.0D;
    }

    private static double eruditionCritDamage(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.25D
                : level >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel() ? 0.15D
                : level >= StarRailPathRank.GLIMPSE.getLevel() ? 0.05D : 0.0D;
    }

    private static double eruditionCritRate(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.10D
                : level >= StarRailPathRank.PRACTICE.getLevel() ? 0.05D : 0.0D;
    }

    private static double nihilityEffectHitRate(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.30D
                : level >= StarRailPathRank.DEEP_PRACTICE.getLevel() ? 0.20D
                : level >= StarRailPathRank.RESONANCE.getLevel() ? 0.15D
                : level >= StarRailPathRank.PATHFARING.getLevel() ? 0.10D : 0.0D;
    }

    private static double nihilityAttackDamage(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.20D
                : level >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel() ? 0.15D
                : level >= StarRailPathRank.PRACTICE.getLevel() ? 0.10D
                : level >= StarRailPathRank.GLIMPSE.getLevel() ? 0.05D : 0.0D;
    }

    private static double nihilityBreakEffect(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.10D : 0.0D;
    }

    private static double harmonyArmor(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.10D
                : level >= StarRailPathRank.PRACTICE.getLevel() ? 0.05D
                : level >= StarRailPathRank.PATHFARING.getLevel() ? 0.02D : 0.0D;
    }

    private static double remembranceAttackDamage(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.25D
                : level >= StarRailPathRank.DEEP_PRACTICE.getLevel() ? 0.15D
                : level >= StarRailPathRank.RESONANCE.getLevel() ? 0.10D
                : level >= StarRailPathRank.PATHFARING.getLevel() ? 0.05D : 0.0D;
    }

    private static double remembranceCritDamage(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.25D
                : level >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel() ? 0.15D
                : level >= StarRailPathRank.PRACTICE.getLevel() ? 0.10D
                : level >= StarRailPathRank.GLIMPSE.getLevel() ? 0.05D : 0.0D;
    }

    private static double elationCritRate(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.09D
                : level >= StarRailPathRank.PRACTICE.getLevel() ? 0.06D
                : level >= StarRailPathRank.PATHFARING.getLevel() ? 0.03D : 0.0D;
    }

    private static double elationAttackDamage(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.15D
                : level >= StarRailPathRank.DEEP_PRACTICE.getLevel() ? 0.10D
                : level >= StarRailPathRank.GLIMPSE.getLevel() ? 0.05D : 0.0D;
    }

    private static double elationCritDamage(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.15D
                : level >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel() ? 0.10D
                : level >= StarRailPathRank.RESONANCE.getLevel() ? 0.05D : 0.0D;
    }

    private static double abundanceMaxHealth(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.15D
                : level >= StarRailPathRank.DEEP_PRACTICE.getLevel() ? 0.10D
                : level >= StarRailPathRank.RESONANCE.getLevel() ? 0.05D
                : level >= StarRailPathRank.PATHFARING.getLevel() ? 0.02D : 0.0D;
    }

    private static double abundanceHealingEffect(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.20D
                : level >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel() ? 0.15D
                : level >= StarRailPathRank.PRACTICE.getLevel() ? 0.10D
                : level >= StarRailPathRank.GLIMPSE.getLevel() ? 0.05D : 0.0D;
    }

    private static double harmonyMaxHealth(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.11D
                : level >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel() ? 0.06D
                : level >= StarRailPathRank.GLIMPSE.getLevel() ? 0.03D : 0.0D;
    }

    private static double harmonyArmorToughness(int level) {
        return level >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel() ? 0.04D
                : level >= StarRailPathRank.RESONANCE.getLevel() ? 0.02D : 0.0D;
    }

    private static double preservationArmor(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.25D
                : level >= StarRailPathRank.DEEP_PRACTICE.getLevel() ? 0.20D
                : level >= StarRailPathRank.RESONANCE.getLevel() ? 0.15D
                : level >= StarRailPathRank.PATHFARING.getLevel() ? 0.10D : 0.0D;
    }

    private static double preservationArmorToughness(int level) {
        return level >= StarRailPathRank.PATH_PINNACLE.getLevel() ? 0.25D
                : level >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel() ? 0.20D
                : level >= StarRailPathRank.DEEP_PRACTICE.getLevel() ? 0.15D
                : level >= StarRailPathRank.GLIMPSE.getLevel() ? 0.10D : 0.0D;
    }

    private static void applyModifier(AttributeInstance instance, UUID id, String name,
                                      double amount, AttributeModifier.Operation operation,
                                      boolean active) {
        AttributeModifier existing = instance.getModifier(id);
        if (!active || amount == 0.0D) {
            if (existing != null) {
                instance.removeModifier(existing);
            }
            return;
        }
        if (existing != null
                && Double.compare(existing.getAmount(), amount) == 0
                && existing.getOperation() == operation) {
            return;
        }
        if (existing != null) {
            instance.removeModifier(existing);
        }
        instance.addPermanentModifier(new AttributeModifier(id, name, amount, operation));
    }
}
