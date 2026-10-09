package com.starrail.sim;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import java.util.Map;
import java.util.WeakHashMap;

/** Adds presentation to the existing pipeline without making new combat decisions. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID)
public final class StarRailCombatVfx {
    private record Pending(ServerPlayer player, DamageSource source, long tick,
                           float pursuit, int toughness, StarRailPath path, Vec3 origin,
                           Vec3 impact, double floorY, int targetId, boolean ranged, java.util.List<CombatVfxPacket.Kind> effects) {}
    private static final Map<LivingEntity, Pending> PENDING = new WeakHashMap<>();

    private StarRailCombatVfx() {}

    public static void queue(ServerPlayer player, LivingEntity target, DamageSource source,
                             float pursuit, int toughness) {
        prepare(player, target, source, pursuit, toughness, false);
    }

    private static void prepare(ServerPlayer player, LivingEntity target, DamageSource source,
                                float pursuit, int toughness, boolean forced) {
        if (!forced && pursuit <= 1 && toughness == 0 && !PENDING.containsKey(target)) return;
        long tick = target.level().getGameTime();
        PENDING.entrySet().removeIf(entry -> entry.getValue().player.level() != entry.getKey().level()
                || entry.getValue().tick != entry.getKey().level().getGameTime());
        boolean ranged = source.getDirectEntity() instanceof Projectile;
        Vec3 impact = target.position().add(0, target.getBbHeight() * .55, 0);
        Vec3 origin = player.getEyePosition();
        if (source.getDirectEntity() instanceof Projectile projectile) {
            Vec3 direction = projectile.getDeltaMovement().normalize();
            if (direction.lengthSqr() < .001) direction = impact.subtract(origin).normalize();
            // Only show the last stretch of the real incoming direction after impact.
            origin = impact.subtract(direction.scale(1.5));
        }
        StarRailPath path = player.getCapability(StarRailPathCapability.PATH_DATA)
                .map(IStarRailPathData::getCurrentPath).orElse(StarRailPath.NONE);
        Pending old = PENDING.get(target);
        var effects = old != null && old.source == source && old.tick == tick
                ? old.effects : new java.util.ArrayList<CombatVfxPacket.Kind>();
        PENDING.put(target, new Pending(player, source, tick, pursuit, toughness, path,
                origin, impact, target.getY(), target.getId(), ranged, effects));
    }

    /** Attaches a path trigger to the original damage source, including early event listeners. */
    public static void stage(ServerPlayer player, LivingEntity target, DamageSource source,
                             CombatVfxPacket.Kind kind) {
        Pending pending = PENDING.get(target);
        if (pending == null || pending.source != source || pending.tick != target.level().getGameTime()) {
            prepare(player, target, source, 0, 0, true);
            pending = PENDING.get(target);
            PENDING.put(target, new Pending(player, source, pending.tick, 0, 0, kind.path(),
                    pending.origin, pending.impact, pending.floorY, pending.targetId, pending.ranged, pending.effects));
        }
        if (kind == CombatVfxPacket.Kind.ELATION_GRAND)
            pending.effects.remove(CombatVfxPacket.Kind.ELATION_BURST);
        if (!pending.effects.contains(kind)) pending.effects.add(kind);
    }

    /** State changes and already accepted secondary damage emit at their actual completion point. */
    public static void emit(ServerPlayer player, LivingEntity origin, LivingEntity target,
                            CombatVfxPacket.Kind kind, boolean ranged) {
        if (player == null || target.level().isClientSide() || player.level() != target.level()) return;
        Vec3 p = target.position().add(0, target.getBbHeight() * .55, 0);
        Vec3 from = origin.position().add(0, origin.getBbHeight() * .55, 0);
        StarRailNetwork.CHANNEL.send(PacketDistributor.NEAR.with(() ->
                new PacketDistributor.TargetPoint(p.x, p.y, p.z, 48, player.level().dimension())),
                new CombatVfxPacket(kind, kind.path(), ranged, from, p, target.getY(), target.getId(), origin.getId()));
    }

    public static void self(ServerPlayer player, CombatVfxPacket.Kind kind) {
        emit(player, player, player, kind, false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void confirmDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        Pending pending = PENDING.get(event.getEntity());
        if (pending == null || pending.source != event.getSource()) return;
        PENDING.remove(event.getEntity());
        if (event.isCanceled() || event.getAmount() <= 0
                || pending.tick != event.getEntity().level().getGameTime()) return;
        if (pending.pursuit > 1) send(pending, pending.pursuit >= 1.5F
                ? CombatVfxPacket.Kind.HUNT_REVERSE : CombatVfxPacket.Kind.HUNT_STRIKE);
        for (CombatVfxPacket.Kind kind : pending.effects) send(pending, kind);
        if (pending.toughness != 0) send(pending, pending.toughness == 2
                ? CombatVfxPacket.Kind.TOUGHNESS_BREAK : CombatVfxPacket.Kind.TOUGHNESS_HIT);
    }

    private static void send(Pending pending, CombatVfxPacket.Kind kind) {
        Vec3 p = pending.impact;
        StarRailNetwork.CHANNEL.send(PacketDistributor.NEAR.with(() ->
                new PacketDistributor.TargetPoint(p.x, p.y, p.z, 48,
                        pending.player.level().dimension())),
                new CombatVfxPacket(kind, kind.path().isRealPath() ? kind.path() : pending.path,
                        pending.ranged, pending.origin, p, pending.floorY, pending.targetId, pending.player.getId()));
    }
}
