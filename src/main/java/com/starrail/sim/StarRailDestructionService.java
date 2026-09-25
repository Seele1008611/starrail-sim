package com.starrail.sim;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Server-side state and combat rules for the Destruction path. */
public final class StarRailDestructionService {
    private static final long WRATH_DURATION = 8L * 20L;
    private static final long EMPOWERED_ATTACK_DURATION = 8L * 20L;
    private static final long DESPERATION_COOLDOWN = 30L * 20L;
    private static final long DESPERATION_RESISTANCE_DURATION = 3L * 20L;

    private StarRailDestructionService() {
    }

    /** Returns the multiplier for a direct attack against a hostile mob. */
    public static float consumeAttackMultiplier(ServerPlayer player,
                                                IStarRailPathData data) {
        if (data.getCurrentPath() != StarRailPath.DESTRUCTION) {
            return 1.0F;
        }

        long currentTick = player.level().getGameTime();
        expireAttackBonus(player, data, currentTick);
        int level = data.getCurrentPathRank().getLevel();
        float multiplier = 1.0F;
        if (level >= StarRailPathRank.PRACTICE.getLevel()
                && isBelowHalfHealth(player)) {
            multiplier *= 1.10F;
            StarRailPathMessages.send(player, StarRailPath.DESTRUCTION,
                    Component.translatable(
                            "message.starrail_sim.destruction_blood_battle"));
        }

        double bonus = data.getDestructionAttackBonus();
        if (bonus > 0.0D) {
            multiplier *= (float) (1.0D + bonus);
            data.setDestructionAttackBonus(0.0D);
            data.setDestructionAttackBonusExpireTick(-1L);
            StarRailPathMessages.send(player, StarRailPath.DESTRUCTION,
                    Component.translatable(
                            "message.starrail_sim.destruction_empowered_attack"));
        }
        return multiplier;
    }

    /** Records hostile damage taken while entering or remaining in low health. */
    public static void onHostileDamage(ServerPlayer player, IStarRailPathData data,
                                       float amount) {
        if (data.getCurrentPath() != StarRailPath.DESTRUCTION
                || amount <= 0.0F
                || data.getCurrentPathRank().getLevel()
                < StarRailPathRank.DEEP_PRACTICE.getLevel()) {
            return;
        }

        float threshold = player.getMaxHealth() * 0.50F;
        if (player.getHealth() > threshold
                && player.getHealth() - amount > threshold) {
            return;
        }

        long currentTick = player.level().getGameTime();
        int stacks = data.getDestructionWrathExpireTick() > currentTick
                ? data.getDestructionWrathStacks() : 0;
        stacks = Math.min(3, stacks + 1);
        data.setDestructionWrathStacks(stacks);
        data.setDestructionWrathExpireTick(currentTick + WRATH_DURATION);
        StarRailPathMessages.send(player, StarRailPath.DESTRUCTION,
                Component.translatable("message.starrail_sim.destruction_wrath",
                        stacks, 3, (WRATH_DURATION + 19L) / 20L));

        if (stacks == 3 && data.getCurrentPathRank().getLevel()
                >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel()) {
            data.setDestructionWrathStacks(0);
            data.setDestructionWrathExpireTick(-1L);
            data.setDestructionAttackBonus(0.35D);
            data.setDestructionAttackBonusExpireTick(
                    currentTick + EMPOWERED_ATTACK_DURATION);
            StarRailPathMessages.send(player, StarRailPath.DESTRUCTION,
                    Component.translatable(
                            "message.starrail_sim.destruction_burning"));
        }
    }

    /** Handles stack, attack-bonus, and rank-seven cooldown expiration. */
    public static void tick(ServerPlayer player, IStarRailPathData data) {
        if (data.getCurrentPath() != StarRailPath.DESTRUCTION) {
            clear(data);
            return;
        }

        long currentTick = player.level().getGameTime();
        if (data.getDestructionWrathStacks() > 0
                && data.getDestructionWrathExpireTick() <= currentTick) {
            data.setDestructionWrathStacks(0);
            data.setDestructionWrathExpireTick(-1L);
            StarRailPathMessages.send(player, StarRailPath.DESTRUCTION,
                    Component.translatable(
                            "message.starrail_sim.destruction_wrath_expired"));
        }
        expireAttackBonus(player, data, currentTick);

        if (data.getCurrentPathRank().getLevel()
                >= StarRailPathRank.PATH_PINNACLE.getLevel()
                && isBelowQuarterHealth(player)
                && data.getDestructionDesperationCooldownTick() <= currentTick) {
            data.setDestructionDesperationCooldownTick(
                    currentTick + DESPERATION_COOLDOWN);
            data.setDestructionAttackBonus(0.50D);
            data.setDestructionAttackBonusExpireTick(
                    currentTick + EMPOWERED_ATTACK_DURATION);
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    (int) DESPERATION_RESISTANCE_DURATION, 0,
                    false, true, true));
            StarRailPathMessages.send(player, StarRailPath.DESTRUCTION,
                    Component.translatable(
                            "message.starrail_sim.destruction_desperation"));
        }
    }

    /** Clears all temporary Destruction state when the path changes. */
    public static void clear(IStarRailPathData data) {
        data.setDestructionWrathStacks(0);
        data.setDestructionWrathExpireTick(-1L);
        data.setDestructionAttackBonus(0.0D);
        data.setDestructionAttackBonusExpireTick(-1L);
        data.setDestructionDesperationCooldownTick(-1L);
    }

    private static void expireAttackBonus(ServerPlayer player, IStarRailPathData data,
                                          long currentTick) {
        if (data.getDestructionAttackBonus() > 0.0D
                && data.getDestructionAttackBonusExpireTick() <= currentTick) {
            data.setDestructionAttackBonus(0.0D);
            data.setDestructionAttackBonusExpireTick(-1L);
            StarRailPathMessages.send(player, StarRailPath.DESTRUCTION,
                    Component.translatable(
                            "message.starrail_sim.destruction_empowered_expired"));
        }
    }

    private static boolean isBelowHalfHealth(ServerPlayer player) {
        return player.getHealth() <= player.getMaxHealth() * 0.50F;
    }

    private static boolean isBelowQuarterHealth(ServerPlayer player) {
        return player.getHealth() <= player.getMaxHealth() * 0.25F;
    }
}
