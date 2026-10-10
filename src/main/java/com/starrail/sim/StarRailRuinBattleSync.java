package com.starrail.sim;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.warden.Warden;

/** Four updates per second to nearby viewers; no chunk loading and no global broadcast. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID)
public final class StarRailRuinBattleSync {
    private static final Set<ServerPlayer> VISIBLE = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<ServerPlayer> ANCHOR_VISIBLE = Collections.newSetFromMap(new WeakHashMap<>());
    private record Target(ResourceLocation dimension, UUID uuid, long until) {}
    private static final Map<ServerPlayer, Target> TARGETS = new WeakHashMap<>();

    private StarRailRuinBattleSync() {}

    /** Melee and player-owned projectiles share the actual damage-source selection. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void hit(LivingDamageEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0 || event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof Warden)) return;
        ServerPlayer player = StarRailCombatEvents.getPlayerAttacker(event.getSource());
        if (player != null) TARGETS.put(player, new Target(player.level().dimension().location(),
                event.getEntity().getUUID(), player.level().getGameTime() + 100));
    }

    static UUID preferredTarget(ServerPlayer player) {
        Target target = TARGETS.get(player);
        if (target == null) return null;
        if (!target.dimension().equals(player.level().dimension().location())
                || player.level().getGameTime() >= target.until()) {
            TARGETS.remove(player);
            return null;
        }
        return target.uuid();
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 5 != 0) return;
        syncAnchor(player);
        var guard = StarRailRuinGuardService.battleGuard(player);
        RuinBattlePacket packet;
        if (guard == null) {
            if (!VISIBLE.remove(player)) return;
            packet = RuinBattlePacket.hidden(player.level().dimension().location());
        } else {
            if (VISIBLE.add(player) && !guard.rematch()) {
                StarRailNetwork.sendRuinNotification(player, guard.path().isRealPath()
                        ? net.minecraft.network.chat.Component.translatable(
                            "message.starrail_sim.ruin_anchor.encounter", StarRailRuinContent.pathName(guard.path()))
                        : net.minecraft.network.chat.Component.translatable(
                            "message.starrail_sim.ruin_anchor.ordinary_encounter"));
            }
            var entity = guard.entity();
            var toughness = StarRailToughnessService.view(entity);
            packet = new RuinBattlePacket(player.level().dimension().location(), entity.getId(),
                    entity.getUUID(), entity.getDisplayName(), guard.path(), entity.getHealth(),
                    entity.getMaxHealth(), toughness.current(), toughness.max(),
                    toughness.phase(), toughness.remainingTicks());
        }
        StarRailNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        VISIBLE.remove(event.getEntity());
        ANCHOR_VISIBLE.remove(event.getEntity());
        TARGETS.remove(event.getEntity());
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) {
        VISIBLE.clear(); ANCHOR_VISIBLE.clear(); TARGETS.clear();
    }

    private static void syncAnchor(ServerPlayer player) {
        var level = player.serverLevel();
        RuinAnchorPacket packet = null;
        if (player.isAlive() && !player.isSpectator()) {
            var start = player.getEyePosition();
            var end = start.add(player.getLookAngle().scale(5));
            // Never raycast into an unloaded neighbour or request a client-provided chunk.
            if (loadedRay(level, start, end)) {
                var hit = level.clip(new net.minecraft.world.level.ClipContext(start, end,
                        net.minecraft.world.level.ClipContext.Block.OUTLINE,
                        net.minecraft.world.level.ClipContext.Fluid.NONE, player));
                var pos = hit.getBlockPos();
                if (hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                        && level.hasChunkAt(pos)
                        && level.getBlockState(pos).getBlock() instanceof PathRuinAnchorBlock block) {
                    var view = StarRailRuinGuardService.anchorView(level, pos, block.path());
                    packet = new RuinAnchorPacket(level.dimension().location(), true, pos,
                            view.path(), view.phase(), view.remainingTicks());
                }
            }
        }
        if (packet == null) {
            if (!ANCHOR_VISIBLE.remove(player)) return;
            packet = RuinAnchorPacket.hidden(level.dimension().location());
        } else ANCHOR_VISIBLE.add(player);
        StarRailNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    private static boolean loadedRay(net.minecraft.server.level.ServerLevel level,
                                     net.minecraft.world.phys.Vec3 start, net.minecraft.world.phys.Vec3 end) {
        var first = net.minecraft.core.BlockPos.containing(start);
        var last = net.minecraft.core.BlockPos.containing(end);
        for (int x = Math.min(first.getX(), last.getX()) >> 4;
                x <= (Math.max(first.getX(), last.getX()) >> 4); x++) {
            for (int z = Math.min(first.getZ(), last.getZ()) >> 4;
                    z <= (Math.max(first.getZ(), last.getZ()) >> 4); z++) {
                if (!level.hasChunkAt(new net.minecraft.core.BlockPos(x << 4, first.getY(), z << 4)))
                    return false;
            }
        }
        return true;
    }
}
