package com.starrail.sim;

/**
 * 模组代码说明：监听玩家加入、重生、维度变化等生命周期事件，并维护命途状态。
 */

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
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

            StarRailPathTemporaryState.tick(player, data);
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
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data ->
                StarRailPathEffects.refresh(player, data.getCurrentPath()));
        StarRailLightConeService.refresh(player);
        StarRailPathService.sync(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttack(ShieldBlockEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getDamageSource().getEntity() instanceof Monster attacker)
                || event.getBlockedDamage() <= 0) return;
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            StarRailPracticeService.combat(player);
            StarRailRankTrialService.blocked(player, data);
            float damage = StarRailPreservationService.onBlockedAttack(player, data, attacker);
            if (damage > 0 && StarRailOwnedDamage.hurt(player, attacker, damage, false))
                StarRailCombatVfx.emit(player, player, attacker, CombatVfxPacket.Kind.PRESERVATION_COUNTER,
                        event.getDamageSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile);
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0) return;
        StarRailLightConeService.onPlayerDamaged(player);
        if (event.getSource().getEntity() != null) StarRailLightConeService.onMomentOfVictoryAttacked(player);
        player.getPersistentData().remove("starrail_absorption_before_hit");
        if (!(event.getSource().getEntity() instanceof Monster)) return;
        StarRailLightConeService.onPlayerAttacked(player);
        player.getPersistentData().putFloat("starrail_absorption_before_hit", player.getAbsorptionAmount());
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide()) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            float beforeAbsorption = player.getPersistentData().getFloat("starrail_absorption_before_hit");
            player.getPersistentData().remove("starrail_absorption_before_hit");
            float absorbed = Math.max(0, beforeAbsorption - player.getAbsorptionAmount());
            player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
                if (event.getAmount() > 0 && StarRailAbundanceService.tryEmergencyRecovery(player, data, event.getAmount()))
                    event.setAmount(0);
                float healthDamage = Math.min(player.getHealth(), Math.max(0, event.getAmount()));
                if (!(event.getSource().getEntity() instanceof Monster) || healthDamage + absorbed <= 0) return;
                boolean low = player.getHealth() <= player.getMaxHealth() * .5F
                        || player.getHealth() - healthDamage <= player.getMaxHealth() * .5F;
                StarRailPracticeService.hurt(player, data, healthDamage, absorbed, low);
                StarRailRankTrialService.hurt(player, data, healthDamage + absorbed, low);
                StarRailDestructionService.onHostileDamage(player, data, healthDamage);
            });
        } else if (event.getEntity() instanceof Monster target && event.getAmount() > 0) {
            ServerPlayer player = StarRailCombatEvents.getPlayerAttacker(event.getSource());
            if (player != null && !StarRailOwnedDamage.isSecondary(player) && !player.getPersistentData().getBoolean("trace_destruction_splash_active"))
                player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data ->
                        StarRailPracticeService.duringAttack(event.getSource(), () ->
                                StarRailRankTrialService.hit(player, data, target, event.getSource(), event.getAmount())));
        }
    }



    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0) return;
        float amount = event.getAmount() * (float) (1 + Math.max(0,
                StarRailAttributes.getValue(player, StarRailAttributes.HEALING_EFFECT, 0)));
        event.setAmount(amount);
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            if (data.getCurrentPath() == StarRailPath.ABUNDANCE) StarRailAbundanceService.onHealing(player, data, amount);
            float actual = Math.min(amount, Math.max(0, player.getMaxHealth() - player.getHealth()));
            if (actual > 0) StarRailRankTrialService.healing(player, data, actual);
        });
    }

    @SubscribeEvent
    public static void onHarmonyFood(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().level().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)
                || !event.getItem().isEdible()) return;
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            if (data.getCurrentPath() != StarRailPath.HARMONY && data.getTrialPath() != StarRailPath.HARMONY) return;
            StarRailHarmonyService.startResonance(player, data.getPathRank(StarRailPath.HARMONY).getLevel());
            StarRailRankTrialService.food(player, data);
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRemembranceAttack(LivingHurtEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide()
                || event.getAmount() <= 0.0F
                || !(event.getEntity() instanceof Monster target)) {
            return;
        }
        ServerPlayer player = StarRailCombatEvents.getPlayerAttacker(event.getSource());
        if (player == null || StarRailOwnedDamage.isSecondary(player)) {
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
            boolean alreadyRecorded = target.getUUID().equals(data.getRemembranceTargetId())
                    && currentTick - data.getRemembranceTargetTick() <= StarRailRemembranceService.memoryWindow(player, rank);
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
                if (active) StarRailCombatVfx.stage(player, target, event.getSource(), eternalEcho
                        ? CombatVfxPacket.Kind.REMEMBRANCE_ETERNAL : CombatVfxPacket.Kind.REMEMBRANCE_ECHO);
                StarRailRemembranceService.lockEcho(player, rank);
                if (active && rank >= StarRailPathRank.HIGH_PATHSTRIDER.getLevel()) {
                    data.setRemembranceTargetId(target.getUUID());
                    data.setRemembranceTargetTick(currentTick);
                } else {
                    data.setRemembranceTargetId(null);
                    data.setRemembranceTargetTick(-1L);
                }
                StarRailRankTrialService.queue(player, target, "echo", 1);
            } else {
                data.setRemembranceTargetId(target.getUUID());
                data.setRemembranceTargetTick(currentTick);
                if (active) StarRailCombatVfx.stage(player, target, event.getSource(), CombatVfxPacket.Kind.REMEMBRANCE_RECORD);
                target.addEffect(new MobEffectInstance(
                        MobEffects.GLOWING, StarRailRemembranceService.memoryWindow(player, rank),
                        0, false, false, true));
                if (active && rank >= StarRailPathRank.PRACTICE.getLevel()) {
                    StarRailPathMessages.send(player, StarRailPath.REMEMBRANCE,
                            Component.translatable("message.starrail_sim.remembrance_marked"));
                }
                if (!alreadyRecorded)
                    StarRailRankTrialService.queue(player, target, "record", 1);

            }

        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onElationAttack(LivingHurtEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide()
                || event.getAmount() <= 0.0F
                || !(event.getEntity() instanceof Monster)) {
            return;
        }
        ServerPlayer player = StarRailCombatEvents.getPlayerAttacker(event.getSource());
        if (player == null || StarRailOwnedDamage.isSecondary(player)) {
            return;
        }

        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            boolean trial = data.getTrialPath() == StarRailPath.ELATION;
            boolean active = data.getCurrentPath() == StarRailPath.ELATION;
            if (!trial && !active) {
                return;
            }

            long currentTick = player.level().getGameTime();
            int rank = active || data.isRankTrial()
                    ? data.getPathRank(StarRailPath.ELATION).getLevel() : 0;
            int combo = data.getElationLastHitTick() >= 0L
                    && currentTick - data.getElationLastHitTick()
                    <= StarRailElationService.comboWindow(player, rank)
                    ? data.getElationCombo() + 1 : 1;
            data.setElationCombo(combo);
            data.setElationLastHitTick(currentTick);
            StarRailRankTrialService.queue(player, event.getEntity(), "combo", combo);

            if (active) {
                event.setAmount(event.getAmount()
                        * StarRailElationService.consumeAfterglow(player, data));
            }

            if (combo % 3 == 0) {
                event.setAmount(event.getAmount()
                        + StarRailElationService.burstBonus(player, rank));
                StarRailRankTrialService.queue(player, event.getEntity(), "burst", 1);
                if (!trial) {
                    if (rank >= StarRailPathRank.DEEP_PRACTICE.getLevel()) {
                        StarRailPathMessages.send(player, StarRailPath.ELATION,
                                Component.translatable(
                                        "message.starrail_sim.elation_combo_burst"));
                    }
                    StarRailCombatVfx.stage(player, event.getEntity(), event.getSource(), CombatVfxPacket.Kind.ELATION_BURST);
                    StarRailElationService.rollJoyDice(player, rank);
                    StarRailElationService.startAfterglow(player, rank);
                    if (combo % 6 == 0
                            && StarRailElationService.tryGrandBurst(player, rank)) {
                        event.setAmount(event.getAmount() * 1.50F);
                        StarRailElationService.rollDistinctJoyDice(player, rank);
                        StarRailElationService.sendGrandBurst(player);
                        StarRailCombatVfx.stage(player, event.getEntity(), event.getSource(), CombatVfxPacket.Kind.ELATION_GRAND);
                    }
                }
            }

        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide()) return;
        if (event.getEntity() instanceof ServerPlayer dead) { StarRailPathTemporaryState.clear(dead, false); return; }
        if (!(event.getEntity() instanceof Monster)) return;
        ServerPlayer credited = StarRailCombatEvents.getPlayerAttacker(event.getSource());
        if (credited == null) credited = StarRailDebuffService.getNihilityMarkOwner(event.getEntity());
        final ServerPlayer player = credited;
        if (player == null) return;
        String paid = "starrail_death_recorded_" + player.getUUID();
        if (event.getEntity().getPersistentData().getBoolean(paid)) return;
        event.getEntity().getPersistentData().putBoolean(paid, true);
        player.getCapability(StarRailPathCapability.PATH_DATA).ifPresent(data -> {
            if (data.getCurrentPath() == StarRailPath.HARMONY || data.getTrialPath() == StarRailPath.HARMONY)
                StarRailHarmonyService.onResonanceKill(player, data);
            StarRailPracticeService.duringAttack(event.getSource(), () -> {
                StarRailRankTrialService.killed(player, data, event.getEntity());
                StarRailDebuffService.onNihilityMarkedDeath(player, event.getEntity());
            });
            StarRailHuntService.onMonsterKilled(player, data);
            StarRailLightConeService.onMonsterKilled(player);
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
