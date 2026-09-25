package com.starrail.sim;

import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-side combat rules for Star Rail-inspired damage mechanics. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarRailCombatEvents {
    private StarRailCombatEvents() {
    }

    /** Resolves a player-owned melee or projectile damage source. */
    public static ServerPlayer getPlayerAttacker(DamageSource source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return null;
        }
        if (source.getDirectEntity() == player) {
            return player;
        }
        if (source.getDirectEntity() instanceof Projectile projectile
                && projectile.getOwner() == player) {
            return player;
        }
        return null;
    }

    /** Applies the shared combat and light-cone pipeline to melee and projectiles. */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0.0F
                || event.getEntity().level().isClientSide()) {
            return;
        }
        ServerPlayer player = getPlayerAttacker(event.getSource());
        if (player == null) {
            return;
        }

        if (event.getSource().getDirectEntity() instanceof Projectile) {
            double projectileAttackMultiplier =
                    StarRailAttributes.getProjectileAttackDamageMultiplier(player);
            if (projectileAttackMultiplier != 1.0D) {
                event.setAmount(event.getAmount() * (float) projectileAttackMultiplier);
            }
        }

        double critRate = Mth.clamp(
                StarRailAttributes.getCritRate(player)
                        + StarRailLightConeService.rainNeverStopsCritRateBonus(
                                player, event.getEntity()), 0.0D, 1.0D);
        StarRailLightConeService.onAttack(player);
        boolean critical = player.getRandom().nextDouble() < critRate;
        if (!critical) {
            StarRailLightConeService.onNonCriticalAttack(player);
        }
        final float[] pursuitMultiplier = {1.0F};
        final float[] destructionMultiplier = {1.0F};
        final float[] eruditionMultiplier = {1.0F};
        final float[] harmonyMultiplier = {1.0F};
        if (event.getEntity() instanceof Monster) {
            player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
                pursuitMultiplier[0] = StarRailHuntService.consumePursuitStrike(player, data);
                destructionMultiplier[0] = StarRailDestructionService.consumeAttackMultiplier(
                        player, data);
                eruditionMultiplier[0] = StarRailEruditionService.consumeAttackMultiplier(
                        player, data);
                harmonyMultiplier[0] = StarRailHarmonyService.consumeAttackMultiplier(
                        player, data);
                if (critical && pursuitMultiplier[0] == 1.0F) {
                    StarRailHuntService.onCriticalHit(player, data);
                }
            });
        }
        if (critical) {
            double bonusCritDamage = Mth.clamp(
                    StarRailAttributes.getCritDamage(player)
                            + StarRailLightConeService.infernoCritDamageBonus(
                                    player, event.getEntity())
                            + StarRailLightConeService.tamedCritDamageBonus(
                                    player, event.getEntity())
                            + StarRailLightConeService.thoughtTrainingCritDamageBonus(
                                    player, event.getEntity()),
                    0.0D, 10.0D);
            float multiplier = (float) (1.0D + bonusCritDamage);
            event.setAmount(event.getAmount() * multiplier);

            // Reuse the vanilla critical-hit particles and sound as feedback.
            player.crit(event.getEntity());
        }

        if (pursuitMultiplier[0] > 1.0F) {
            event.setAmount(event.getAmount() * pursuitMultiplier[0]);
        }

        if (destructionMultiplier[0] > 1.0F) {
            event.setAmount(event.getAmount() * destructionMultiplier[0]);
        }

        if (eruditionMultiplier[0] > 1.0F) {
            event.setAmount(event.getAmount() * eruditionMultiplier[0]);
        }

        if (harmonyMultiplier[0] > 1.0F) {
            event.setAmount(event.getAmount() * harmonyMultiplier[0]);
        }

        double lightConeMultiplier = StarRailLightConeService.lightConeDamageMultiplier(
                player, event.getEntity());
        if (lightConeMultiplier > 1.0D) {
            event.setAmount(event.getAmount() * (float) lightConeMultiplier);
        }

        if (event.getEntity() instanceof Monster target) {
            player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data ->
                    StarRailEruditionService.onPlayerAttack(
                            player, target, event.getAmount(), data));
        }

        boolean toughnessBroken = StarRailToughnessService.onPlayerAttack(
                player, event.getEntity());
        StarRailLightConeService.onToughnessBreak(
                player, event.getEntity(), toughnessBroken);

        StarRailLightConeService.onTargetHit(player, event.getEntity());

        StarRailNetwork.sendDamageNumber(
                player, event.getEntity(), event.getAmount(), critical);
    }

    /** The rainbow cone counts each landed player hit exactly once, melee or projectile. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void recordMayRainbowAttack(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide() || event.isCanceled()
                || event.getAmount() <= 0.0F) {
            return;
        }
        ServerPlayer player = getPlayerAttacker(event.getSource());
        if (player == null) {
            return;
        }
        StarRailLightConeService.onMayRainbowStayInTheSkyAttack(player, event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applyNihilityVulnerability(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide() || event.getAmount() <= 0.0F) {
            return;
        }
        double multiplier = StarRailLightConeService.incomingLightConeVulnerabilityMultiplier(
                event.getEntity());
        if (multiplier > 1.0D) {
            event.setAmount(event.getAmount() * (float) multiplier);
        }
    }

    /** Records actual wearer health loss and adds the cones' post-mitigation bonus damage. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applyThornBonusDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide() || event.isCanceled()) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer wearer) {
            StarRailLightConeService.recordTimeWaitsForNoOneHealthLoss(
                    wearer, event.getAmount());
            StarRailLightConeService.onMakeFarewellMoreBeautifulHealthLost(
                    wearer, event.getAmount());
        }
        if (!(event.getEntity() instanceof Monster)) {
            return;
        }
        ServerPlayer player = getPlayerAttacker(event.getSource());
        if (player == null) {
            return;
        }
        double bonus = StarRailLightConeService.echoesOfTheCoffinBonusDamage(player);
        if (bonus > 0.0D) {
            event.setAmount(event.getAmount() + (float) bonus);
        }
        double recordedLossBonus = StarRailLightConeService.timeWaitsForNoOneBonusDamage(player);
        if (recordedLossBonus > 0.0D) {
            event.setAmount(event.getAmount() + (float) recordedLossBonus);
        }
        double rainbowBurst = StarRailLightConeService.consumeMayRainbowBonusDamage(
                player, event.getEntity());
        if (rainbowBurst > 0.0D) {
            event.setAmount(event.getAmount() + (float) rainbowBurst);
        }
    }
}
