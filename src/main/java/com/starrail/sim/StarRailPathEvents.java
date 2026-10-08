package com.starrail.sim;

/**
 * 模组代码说明：监听玩家加入、重生、维度变化等生命周期事件，并维护命途状态。
 */

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-side path unlock and playable path trial progression. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarRailPathEvents {
    private StarRailPathEvents() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            StarRailDebuffService.tick(event.getEntity());
            StarRailToughnessService.tick(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onMobEffectAdded(MobEffectEvent.Applicable event) {
        if (event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof ServerPlayer player)
                || event.getEffectInstance().getEffect().isBeneficial()) {
            return;
        }

        if (StarRailEffectService.rollPlayerEffectResistance(player)) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.player.level().isClientSide()
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            if (!data.isPathUnlocked() && hasDiamond(player)) {
                data.setPathUnlocked(true);
                StarRailPathService.sync(player);
            }

            if (data.getTrialPath().isRealPath()
                    && !data.isTrialComplete()
                    && player.level().getGameTime() - data.getTrialStartTick()
                    >= StarRailPathRules.trialTimeLimit(
                            data.getTrialPath(), data.getTrialRank())) {
                StarRailPath trialPath = data.getTrialPath();
                boolean rankTrial = data.isRankTrial();
                data.resetTrial();
                StarRailPathMessages.sendQueued(player, trialPath, Component.translatable(
                        rankTrial ? "message.starrail_sim.rank_trial_timeout"
                                : "message.starrail_sim.trial_timeout"));
                StarRailPathService.sync(player);
            }

            StarRailPreservationService.tick(player, data);
            StarRailDestructionService.tick(player, data);
            StarRailEruditionService.tick(player, data);
            StarRailHuntService.tick(player, data);
            StarRailHarmonyService.tick(player);
            StarRailPathEffects.refresh(player, data.getCurrentPath());
            StarRailLightConeService.refresh(player);
            StarRailLightConeService.tickSheHasClosedHerEyes(player);
        });
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        initializeBaseDefenseAttributes(player);
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data ->
                StarRailPathEffects.refresh(player, data.getCurrentPath()));
        StarRailLightConeService.refresh(player);
        StarRailPathService.sync(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            initializeBaseDefenseAttributes(player);
        }
    }

    private static void initializeBaseDefenseAttributes(ServerPlayer player) {
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null && armor.getBaseValue() != 2.0D) {
            armor.setBaseValue(2.0D);
        }

        AttributeInstance armorToughness = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armorToughness != null && armorToughness.getBaseValue() != 1.0D) {
            armorToughness.setBaseValue(1.0D);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getSource().getEntity() instanceof Monster attacker)
                || !player.isBlocking()) {
            return;
        }

            player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            if (data.getTrialPath() == StarRailPath.PRESERVATION) {
                recordObjective1(player, data, 1);
                completeOrSync(player, data);
            }
            if (!data.getTrialPath().isRealPath()
                    && data.getCurrentPath() == StarRailPath.PRESERVATION) {
                StarRailPathProgress.record(player, data, StarRailPath.PRESERVATION, 1);
            }
            float countershockDamage = StarRailPreservationService.onBlockedAttack(
                    player, data, attacker);
            if (countershockDamage > 0.0F) {
                // Countershock is a fixed armor-based effect; do not let it roll
                // the player's critical-hit formula a second time.
                attacker.hurt(player.damageSources().generic(), countershockDamage);
            }
        });
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof ServerPlayer player)
                || event.getAmount() <= 0.0F) {
            return;
        }

        StarRailLightConeService.onPlayerDamaged(player);
        if (event.getSource().getEntity() != null) {
            StarRailLightConeService.onMomentOfVictoryAttacked(player);
        }
        if (!(event.getSource().getEntity() instanceof Monster)) {
            return;
        }

        // Destruction light cones trigger from a real hostile hit, including
        // hits that are subsequently handled by the Preservation block logic.
        StarRailLightConeService.onPlayerAttacked(player);
        if (player.isBlocking()) {
            return;
        }

        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            StarRailPath trialPath = data.getTrialPath();
            if (trialPath == StarRailPath.DESTRUCTION
                    && player.getHealth() <= player.getMaxHealth() * 0.5F) {
                recordObjective2(player, data,
                        Math.max(1, (int) Math.ceil(event.getAmount())));
                completeOrSync(player, data);
            } else if (trialPath == StarRailPath.PRESERVATION) {
                recordObjective2(player, data,
                        Math.max(1, (int) Math.ceil(event.getAmount())));
                completeOrSync(player, data);
            }
            if (!trialPath.isRealPath()
                    && data.getCurrentPath() == StarRailPath.DESTRUCTION
                    && (player.getHealth() <= player.getMaxHealth() * 0.5F
                    || player.getHealth() - event.getAmount()
                    <= player.getMaxHealth() * 0.5F)) {
                StarRailPathProgress.record(player, data, StarRailPath.DESTRUCTION, 1);
            }
            StarRailDestructionService.onHostileDamage(player, data, event.getAmount());
            StarRailPreservationService.onUnblockedDamage(player, data, event.getAmount());
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSheHasClosedHerEyesLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof ServerPlayer player)
                || event.getAmount() <= 0.0F) {
            return;
        }
        StarRailLightConeService.onSheHasClosedHerEyesHealthLost(player);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof ServerPlayer player)
                || event.getAmount() <= 0.0F) {
            return;
        }

        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            if (StarRailAbundanceService.tryEmergencyRecovery(
                    player, data, event.getAmount())) {
                event.setAmount(0.0F);
            }
        });
    }

    @SubscribeEvent
    public static void onEruditionMultiHit(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()
                || event.getAmount() <= 0.0F
                || !(event.getEntity() instanceof Monster)) {
            return;
        }
        ServerPlayer player = StarRailCombatEvents.getPlayerAttacker(event.getSource());
        if (player == null) {
            return;
        }

        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            boolean trial = data.getTrialPath() == StarRailPath.ERUDITION;
            boolean active = data.getCurrentPath() == StarRailPath.ERUDITION;
            if (!trial && !active) {
                return;
            }

            long currentTick = player.level().getGameTime();
            if (data.getEruditionHitTick() != currentTick) {
                data.setEruditionHitTick(currentTick);
                data.setEruditionHitCount(0);
            }
            if (data.getEruditionHitCount() < 2) {
                data.setEruditionHitCount(data.getEruditionHitCount() + 1);
            }
            if (data.getEruditionHitCount() >= 2
                    && data.getLastEruditionMultiHitTick() != currentTick) {
                data.setLastEruditionMultiHitTick(currentTick);
                if (trial) {
                    recordObjective1(player, data, 1);
                    recordObjective2(player, data, 1);
                    completeOrSync(player, data);
                } else {
                    StarRailPathProgress.record(player, data, StarRailPath.ERUDITION, 1);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onNihilityAttack(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()
                || event.getAmount() <= 0.0F
                || !(event.getEntity() instanceof Monster target)) {
            return;
        }
        ServerPlayer player = StarRailCombatEvents.getPlayerAttacker(event.getSource());
        if (player == null) {
            return;
        }

        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            boolean trial = data.getTrialPath() == StarRailPath.NIHILITY;
            boolean active = data.getCurrentPath() == StarRailPath.NIHILITY;
            if (!trial && !active) {
                return;
            }
            if (StarRailDebuffService.applyNihilityMark(target, player)) {
                if (trial) {
                    recordObjective1(player, data, 1);
                    completeOrSync(player, data);
                } else {
                    StarRailPathProgress.record(player, data, StarRailPath.NIHILITY, 1);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof ServerPlayer player)
                || event.getAmount() <= 0.0F) {
            return;
        }

        final float amount = event.getAmount()
                * (float) (1.0D + Math.max(0.0D, StarRailAttributes.getValue(
                player, StarRailAttributes.HEALING_EFFECT, 0.0D)));
        event.setAmount(amount);

        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            boolean trial = data.getTrialPath() == StarRailPath.ABUNDANCE;
            boolean active = data.getCurrentPath() == StarRailPath.ABUNDANCE;
            if (!trial && !active) {
                return;
            }
            if (active) {
                StarRailAbundanceService.onHealing(player, data, amount);
            }
            float actualHealing = Math.min(amount,
                    Math.max(0.0F, player.getMaxHealth() - player.getHealth()));
            if (actualHealing <= 0.0F) {
                return;
            }
            if (trial) {
                recordObjective1(player, data,
                        Math.max(1, (int) Math.ceil(actualHealing)));
                recordObjective2(player, data, 1);
                completeOrSync(player, data);
            } else {
                StarRailPathProgress.record(player, data, StarRailPath.ABUNDANCE, 1);
            }
        });
    }

    @SubscribeEvent
    public static void onHarmonyFood(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof ServerPlayer player)
                || !event.getItem().isEdible()) {
            return;
        }

        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            boolean trial = data.getTrialPath() == StarRailPath.HARMONY;
            boolean active = data.getCurrentPath() == StarRailPath.HARMONY;
            if (!trial && !active) {
                return;
            }
            if (trial) {
                recordObjective1(player, data, 1);
            } else {
                StarRailPathProgress.record(player, data, StarRailPath.HARMONY, 1);
            }
            int rank = data.getPathRank(StarRailPath.HARMONY).getLevel();
            StarRailHarmonyService.startResonance(player, rank);
            if (trial) {
                completeOrSync(player, data);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRemembranceAttack(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()
                || event.getAmount() <= 0.0F
                || !(event.getEntity() instanceof Monster target)) {
            return;
        }
        ServerPlayer player = StarRailCombatEvents.getPlayerAttacker(event.getSource());
        if (player == null) {
            return;
        }

        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            boolean trial = data.getTrialPath() == StarRailPath.REMEMBRANCE;
            boolean active = data.getCurrentPath() == StarRailPath.REMEMBRANCE;
            if (!trial && !active) {
                return;
            }

            long currentTick = player.level().getGameTime();
            int rank = data.getPathRank(StarRailPath.REMEMBRANCE).getLevel();
            if (active) {
                event.setAmount(event.getAmount()
                        * StarRailRemembranceService.consumeAfterglow(player, target, data));
            }
            boolean sameTarget = target.getUUID().equals(data.getRemembranceTargetId())
                    && data.getRemembranceTargetTick() >= 0L
                    && currentTick - data.getRemembranceTargetTick()
                    <= StarRailRemembranceService.memoryWindow(player, rank)
                    && StarRailRemembranceService.canEcho(player, rank);
            if (sameTarget) {
                event.setAmount(event.getAmount()
                        + StarRailRemembranceService.echoBonus(rank));
                if (active && rank >= StarRailPathRank.DEEP_PRACTICE.getLevel()) {
                    StarRailRemembranceService.startAfterglow(player, target, rank);
                }
                StarRailPathMessages.send(player, StarRailPath.REMEMBRANCE,
                        Component.translatable("message.starrail_sim.remembrance_echo"));
                boolean eternalEcho = active
                        && StarRailRemembranceService.recordEcho(player, rank);
                if (eternalEcho) {
                    event.setAmount(event.getAmount() * 1.50F);
                    StarRailRemembranceService.sendEternalEcho(player);
                }
                StarRailRemembranceService.lockEcho(player, rank);
                if (active && rank >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel()) {
                    data.setRemembranceTargetId(target.getUUID());
                    data.setRemembranceTargetTick(currentTick);
                } else {
                    data.setRemembranceTargetId(null);
                    data.setRemembranceTargetTick(-1L);
                }
                if (trial) {
                    recordObjective2(player, data, 1);
                } else {
                    StarRailPathProgress.record(player, data, StarRailPath.REMEMBRANCE, 1);
                }
            } else {
                data.setRemembranceTargetId(target.getUUID());
                data.setRemembranceTargetTick(currentTick);
                target.addEffect(new MobEffectInstance(
                        MobEffects.GLOWING, StarRailRemembranceService.memoryWindow(player, rank),
                        0, false, false, true));
                if (active && rank >= StarRailPathRank.PRACTICE.getLevel()) {
                    StarRailPathMessages.send(player, StarRailPath.REMEMBRANCE,
                            Component.translatable("message.starrail_sim.remembrance_marked"));
                }
                if (trial) {
                    recordObjective1(player, data, 1);
                } else {
                    StarRailPathProgress.record(player, data, StarRailPath.REMEMBRANCE, 1);
                }
            }
            if (trial) {
                completeOrSync(player, data);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onElationAttack(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()
                || event.getAmount() <= 0.0F
                || !(event.getEntity() instanceof Monster)) {
            return;
        }
        ServerPlayer player = StarRailCombatEvents.getPlayerAttacker(event.getSource());
        if (player == null) {
            return;
        }

        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            boolean trial = data.getTrialPath() == StarRailPath.ELATION;
            boolean active = data.getCurrentPath() == StarRailPath.ELATION;
            if (!trial && !active) {
                return;
            }

            long currentTick = player.level().getGameTime();
            int rank = active
                    ? data.getPathRank(StarRailPath.ELATION).getLevel() : 0;
            int combo = data.getElationLastHitTick() >= 0L
                    && currentTick - data.getElationLastHitTick()
                    <= StarRailElationService.comboWindow(player, rank)
                    ? data.getElationCombo() + 1 : 1;
            data.setElationCombo(combo);
            data.setElationLastHitTick(currentTick);
            if (trial) {
                recordObjective1(player, data, 1);
            } else {
                StarRailPathProgress.record(player, data, StarRailPath.ELATION, 1);
            }

            if (active) {
                event.setAmount(event.getAmount()
                        * StarRailElationService.consumeAfterglow(player, data));
            }

            if (combo % 3 == 0) {
                event.setAmount(event.getAmount()
                        + StarRailElationService.burstBonus(player, rank));
                if (trial) {
                    recordObjective2(player, data, 1);
                } else {
                    if (rank >= StarRailPathRank.DEEP_PRACTICE.getLevel()) {
                        StarRailPathMessages.send(player, StarRailPath.ELATION,
                                Component.translatable(
                                        "message.starrail_sim.elation_combo_burst"));
                    }
                    StarRailElationService.rollJoyDice(player, rank);
                    StarRailElationService.startAfterglow(player, rank);
                    if (combo % 6 == 0
                            && StarRailElationService.tryGrandBurst(player, rank)) {
                        event.setAmount(event.getAmount() * 1.50F);
                        StarRailElationService.rollJoyDice(player, rank);
                        StarRailElationService.rollJoyDice(player, rank);
                        StarRailElationService.sendGrandBurst(player);
                    }
                }
            }
            if (trial) {
                completeOrSync(player, data);
            }
        });
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof Monster)) {
            return;
        }

        ServerPlayer creditedPlayer = StarRailCombatEvents.getPlayerAttacker(event.getSource());
        if (creditedPlayer == null) {
            creditedPlayer = StarRailDebuffService.getNihilityMarkOwner(event.getEntity());
        }
        final ServerPlayer player = creditedPlayer;
        if (player == null) {
            return;
        }

        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            if (data.getCurrentPath() == StarRailPath.HARMONY
                    || data.getTrialPath() == StarRailPath.HARMONY) {
                StarRailHarmonyService.onResonanceKill(player, data);
            }
            StarRailDebuffService.onNihilityMarkedDeath(player, event.getEntity());
            StarRailHuntService.onMonsterKilled(player, data);
            StarRailLightConeService.onMonsterKilled(player);
            if (!data.getTrialPath().isRealPath()) {
                StarRailPath currentPath = data.getCurrentPath();
                if (currentPath == StarRailPath.HUNT) {
                    StarRailPathProgress.record(player, data, currentPath, 1);
                } else if (currentPath == StarRailPath.DESTRUCTION
                        && player.getHealth() <= player.getMaxHealth() * 0.5F) {
                    StarRailPathProgress.record(player, data, currentPath, 1);
                } else if (currentPath == StarRailPath.NIHILITY
                        && StarRailDebuffService.isNihilityMarkOwnedBy(
                        event.getEntity(), player.getUUID())) {
                    StarRailPathProgress.record(player, data, currentPath, 1);
                } else if (currentPath == StarRailPath.HARMONY
                        && player.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                    StarRailPathProgress.record(player, data, currentPath, 1);
                }
                return;
            }
            if (data.getTrialPath() == StarRailPath.ERUDITION) {
                return;
            }
            if (data.getTrialPath() == StarRailPath.NIHILITY) {
                if (StarRailDebuffService.isNihilityMarkOwnedBy(
                        event.getEntity(), player.getUUID())) {
                    recordObjective2(player, data, 1);
                    completeOrSync(player, data);
                }
                return;
            }
            if (data.getTrialPath() == StarRailPath.HARMONY) {
                if (player.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                    recordObjective2(player, data, 1);
                    completeOrSync(player, data);
                }
                return;
            }
            if (data.getTrialPath() == StarRailPath.DESTRUCTION) {
                if (player.getHealth() <= player.getMaxHealth() * 0.5F) {
                    recordObjective1(player, data, 1);
                    completeOrSync(player, data);
                }
                return;
            }
            if (data.getTrialPath() != StarRailPath.HUNT) {
                return;
            }

            long currentTick = player.level().getGameTime();
            recordObjective1(player, data, 1);
            if (data.getLastHuntKillTick() >= data.getTrialStartTick()
                    && currentTick - data.getLastHuntKillTick()
                    <= StarRailPathRules.HUNT_STREAK_WINDOW) {
                recordObjective2(player, data, 1);
            }
            data.setLastHuntKillTick(currentTick);
            completeOrSync(player, data);
        });
    }

    private static void completeOrSync(ServerPlayer player, IStarRailPathData data) {
        if (data.isTrialComplete()) {
            if (data.isRankTrial()) {
                StarRailPathService.confirmRankTrial(player);
            } else {
                StarRailPathService.confirmTrial(player);
            }
        } else {
            StarRailPathService.sync(player);
        }
    }

    private static void recordObjective1(ServerPlayer player, IStarRailPathData data, int amount) {
        int before = data.getObjective1Progress();
        data.setObjective1Progress(before + Math.max(0, amount));
        if (data.getObjective1Progress() > before) {
            sendObjectiveProgress(player, data, 1);
        }
    }

    private static void recordObjective2(ServerPlayer player, IStarRailPathData data, int amount) {
        int before = data.getObjective2Progress();
        data.setObjective2Progress(before + Math.max(0, amount));
        if (data.getObjective2Progress() > before) {
            sendObjectiveProgress(player, data, 2);
        }
    }

    private static void sendObjectiveProgress(ServerPlayer player,
                                               IStarRailPathData data,
                                               int objective) {
        StarRailPath path = data.getTrialPath();
        String key = objective == 1
                ? objective1MessageKey(path) : objective2MessageKey(path);
        int progress = objective == 1
                ? data.getObjective1Progress() : data.getObjective2Progress();
        int target = objective == 1
                ? StarRailPathRules.objective1Target(path, data.getTrialRank())
                : StarRailPathRules.objective2Target(path, data.getTrialRank());
        StarRailPathMessages.sendQueued(player, path, Component.translatable(
                "message.starrail_sim.trial_progress",
                Component.translatable(key, progress, target)));
    }

    private static String objective1MessageKey(StarRailPath path) {
        return switch (path) {
            case PRESERVATION -> "screen.starrail_sim.preservation_block";
            case ABUNDANCE -> "screen.starrail_sim.abundance_healing";
            case DESTRUCTION -> "screen.starrail_sim.destruction_kills";
            case ERUDITION -> "screen.starrail_sim.erudition_kills";
            case NIHILITY -> "screen.starrail_sim.nihility_marks";
            case HARMONY -> "screen.starrail_sim.harmony_food";
            case REMEMBRANCE -> "screen.starrail_sim.remembrance_records";
            case ELATION -> "screen.starrail_sim.elation_hits";
            default -> "screen.starrail_sim.hunt_kills";
        };
    }

    private static String objective2MessageKey(StarRailPath path) {
        return switch (path) {
            case PRESERVATION -> "screen.starrail_sim.preservation_damage";
            case ABUNDANCE -> "screen.starrail_sim.abundance_events";
            case DESTRUCTION -> "screen.starrail_sim.destruction_damage";
            case ERUDITION -> "screen.starrail_sim.erudition_multi_hit";
            case NIHILITY -> "screen.starrail_sim.nihility_kills";
            case HARMONY -> "screen.starrail_sim.harmony_kills";
            case REMEMBRANCE -> "screen.starrail_sim.remembrance_echoes";
            case ELATION -> "screen.starrail_sim.elation_bursts";
            default -> "screen.starrail_sim.hunt_streak";
        };
    }

    private static boolean hasDiamond(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(Items.DIAMOND)) {
                return true;
            }
        }
        return false;
    }
}
