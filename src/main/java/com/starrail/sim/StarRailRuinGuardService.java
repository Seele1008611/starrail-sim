package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 浮空遗迹守卫室：击败守卫后才绑定对应命途战利品，防止自动化提前提取。 */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID)
public final class StarRailRuinGuardService extends SavedData {
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    private static final String NAME = "starrail_sim_ruin_guards";
    private static final String ORIGIN_TAG = "StarRailGuardOrigin";
    private static final String VARIANT_TAG = "StarRailGuardVariant";
    private static final String ARCHIVE_VARIANT_TAG = "StarRailArchiveRuinVariant";
    private static final String MIGRATION_SOURCE_TAG = "StarRailGuardianMigrationFrom";
    private static final long SUMMON_COOLDOWN_TICKS = 20L * 30L;
    // 世界生成可能在工作线程执行，只排队；SavedData 和实体操作留给服务器主线程。
    private static final Map<ServerLevel, Set<BlockPos>> QUEUED = new ConcurrentHashMap<>();
    private static final Map<ServerLevel, Set<UUID>> PENDING_GUARDIAN_RECONCILES = new ConcurrentHashMap<>();
    private final Map<BlockPos, Guard> guards = new HashMap<>();
    private final Map<UUID, net.minecraft.world.entity.LivingEntity> pendingDeaths = new HashMap<>();
    private record DropRoll(LivingDropsEvent event, ItemEntity material, StarRailPath path) {}
    private final Map<UUID, DropRoll> pendingDrops = new HashMap<>();

    private static final class Guard {
        UUID entity;
        UUID legacyEntity;
        String variantId = "ordinary";
        boolean constructGuardian = true;
        // Retained only to read the accepted Phase 4 prototype saves.
        boolean constructPrototype;
        boolean defeated;
        boolean rewarded;
        boolean respawn;
        boolean summonActive;
        boolean anchorInstalled;
        long summonCooldownUntil;
        int openedGate;
    }

    public static StarRailRuinGuardService get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(StarRailRuinGuardService::load,
                StarRailRuinGuardService::new, NAME);
    }

    private static StarRailRuinGuardService load(CompoundTag tag) {
        var data = new StarRailRuinGuardService();
        ListTag list = tag.getList("Guards", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            Guard guard = new Guard();
            if (entry.hasUUID("Entity")) guard.entity = entry.getUUID("Entity");
            if (entry.hasUUID("LegacyEntity")) guard.legacyEntity = entry.getUUID("LegacyEntity");
            if (entry.contains("Variant")) guard.variantId = entry.getString("Variant");
            guard.constructPrototype = entry.getBoolean("ConstructPrototype");
            guard.constructGuardian = entry.contains("ConstructGuardian")
                    ? entry.getBoolean("ConstructGuardian")
                    : guard.constructPrototype || !entry.hasUUID("Entity");
            guard.defeated = entry.getBoolean("Defeated");
            guard.rewarded = entry.getBoolean("Rewarded");
            guard.respawn = entry.getBoolean("Respawn");
            guard.summonActive = entry.getBoolean("SummonActive");
            guard.anchorInstalled = entry.contains("AnchorInstalled")
                    ? entry.getBoolean("AnchorInstalled") : entry.getBoolean("HuntAnchorInstalled");
            guard.summonCooldownUntil = entry.getLong("SummonCooldownUntil");
            guard.openedGate = entry.getInt("OpenedGate");
            data.guards.put(BlockPos.of(entry.getLong("Origin")), guard);
            LOGGER.debug("Ruin guard restored: origin={}, variant={}, uuid={}, respawn={}, defeated={}",
                    BlockPos.of(entry.getLong("Origin")), guard.variantId, guard.entity,
                    guard.respawn, guard.defeated);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        guards.forEach((origin, guard) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Origin", origin.asLong());
            if (guard.entity != null) entry.putUUID("Entity", guard.entity);
            if (guard.legacyEntity != null) entry.putUUID("LegacyEntity", guard.legacyEntity);
            entry.putString("Variant", guard.variantId);
            entry.putBoolean("ConstructGuardian", guard.constructGuardian);
            entry.putBoolean("ConstructPrototype", guard.constructPrototype);
            entry.putBoolean("Defeated", guard.defeated);
            entry.putBoolean("Rewarded", guard.rewarded);
            entry.putBoolean("Respawn", guard.respawn);
            entry.putBoolean("SummonActive", guard.summonActive);
            entry.putBoolean("AnchorInstalled", guard.anchorInstalled);
            entry.putLong("SummonCooldownUntil", guard.summonCooldownUntil);
            entry.putInt("OpenedGate", guard.openedGate);
            list.add(entry);
        });
        tag.put("Guards", list);
        return tag;
    }

    /** 标记新宝箱；没有战利品表，也没有奖励，漏斗无法提前获得奖励。 */
    public static void markChest(ChestBlockEntity chest, BlockPos origin, String variantId) {
        chest.getPersistentData().putLong(ORIGIN_TAG, origin.asLong());
        chest.getPersistentData().putString(VARIANT_TAG, variantId);
        chest.getPersistentData().putString(ARCHIVE_VARIANT_TAG, variantId);
        chest.setChanged();
    }

    /** Keep ruin identity after the loot table has been consumed by its first opening. */
    public static void markArchiveChest(ChestBlockEntity chest, String variantId) {
        chest.getPersistentData().putString(ARCHIVE_VARIANT_TAG, variantId);
        chest.setChanged();
    }

    public static String getArchiveRuinId(ChestBlockEntity chest) {
        CompoundTag data = chest.getPersistentData();
        String variant = data.getString(ARCHIVE_VARIANT_TAG);
        return variant.isBlank() ? data.getString(VARIANT_TAG) : variant;
    }

    public static void queue(ServerLevel level, BlockPos origin) {
        QUEUED.computeIfAbsent(level, key -> ConcurrentHashMap.newKeySet()).add(origin.immutable());
    }

    public static void register(ServerLevel level, BlockPos origin, String variantId) {
        var data = get(level);
        if (!data.guards.containsKey(origin)) {
            Guard guard = new Guard();
            guard.variantId = variantId;
            data.guards.put(origin.immutable(), guard);
            LOGGER.debug("Ruin guard registered: {}", origin);
            data.setDirty();
        }
    }

    private static boolean matchesGuardian(Guard guard, Entity entity) {
        return guard.constructGuardian
                ? entity instanceof StarveinConstructGuardian
                : entity instanceof Warden;
    }

    /** 只读调试信息，便于核对守卫绑定与存档恢复，不提供跳过战斗的入口。 */
    public static int status(net.minecraft.commands.CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        BlockPos nearest = null;
        double distance = 160 * 160;
        for (BlockPos origin : get(level).guards.keySet()) {
            double dx = origin.getX() - source.getPosition().x;
            double dz = origin.getZ() - source.getPosition().z;
            if (dx * dx + dz * dz < distance) {
                nearest = origin;
                distance = dx * dx + dz * dz;
            }
        }
        if (nearest == null) {
            source.sendFailure(Component.literal("水平 160 格内没有已登记的遗迹守卫室。"));
            return 0;
        }
        Guard guard = get(level).guards.get(nearest);
        Entity entity = guard.entity == null ? null : level.getEntity(guard.entity);
        String state = guard.summonActive ? "重复挑战进行中" : guard.defeated ? "首次挑战已完成" : guard.entity == null || guard.respawn
                ? "等待生成（和平难度不会生成）" : entity == null ? "实体未加载，不能据此判断死亡" : "守卫在场";
        BlockPos origin = nearest;
        source.sendSuccess(() -> Component.literal("遗迹守卫室锚点 " + origin.toShortString()
                + "；" + state + "；UUID=" + guard.entity + "；奖励已解锁=" + guard.rewarded
                + "；召唤冷却剩余=" + Math.max(0L,
                        (guard.summonCooldownUntil - level.getGameTime() + 19L) / 20L) + "秒"
                + "；守卫类型=" + (guard.constructGuardian ? "星脉构装守卫" : "待迁移旧监守者")
                + "；已处理门格=" + Integer.bitCount(guard.openedGate) + "/25"), false);
        return 1;
    }

    private static BlockPos chestPos(BlockPos origin) { return origin.offset(0, 10, -21); }
    private static BlockPos summonAnchorPos(BlockPos origin) { return origin.offset(-2, 10, -21); }

    public enum AnchorPhase { UNBOUND, FIRST_CLEAR, READY, ACTIVE, COOLDOWN, WAITING, PEACEFUL }
    public record AnchorView(StarRailPath path, AnchorPhase phase, int remainingTicks) {}

    /** Read-only state shared by the look-at HUD and interaction validation. */
    public static AnchorView anchorView(ServerLevel level, BlockPos anchor, StarRailPath path) {
        Guard guard = get(level).guards.get(anchor.offset(2, -10, 21));
        if (guard == null || !guard.variantId.equals(path.getId()))
            return new AnchorView(path, AnchorPhase.UNBOUND, 0);
        if (level.getDifficulty() == Difficulty.PEACEFUL)
            return new AnchorView(path, AnchorPhase.PEACEFUL, 0);
        Entity found = guard.entity == null ? null : level.getEntity(guard.entity);
        boolean alive = found instanceof net.minecraft.world.entity.LivingEntity living
                && matchesGuardian(guard, living) && living.isAlive() && !living.isRemoved();
        if (guard.respawn || ((!guard.defeated || guard.summonActive) && !alive))
            return new AnchorView(path, AnchorPhase.WAITING, 0);
        if (!guard.defeated) return new AnchorView(path, AnchorPhase.FIRST_CLEAR, 0);
        if (guard.summonActive || alive) return new AnchorView(path, AnchorPhase.ACTIVE, 0);
        long remaining = guard.summonCooldownUntil - level.getGameTime();
        if (remaining > 0) return new AnchorView(path, AnchorPhase.COOLDOWN,
                (int) Math.min(SUMMON_COOLDOWN_TICKS, remaining));
        return new AnchorView(path, AnchorPhase.READY, 0);
    }

    public record BattleGuard(net.minecraft.world.entity.LivingEntity entity,
                              StarRailPath path, boolean rematch) {}
    public static final double BATTLE_VIEW_RANGE = 48.0;

    /** Only registered, living guards; UUID lookups do not load chunks or invent a victory. */
    public static BattleGuard battleGuard(net.minecraft.server.level.ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) return null;
        ServerLevel level = player.serverLevel();
        BattleGuard nearest = null;
        double distance = BATTLE_VIEW_RANGE * BATTLE_VIEW_RANGE;
        UUID attacked = StarRailRuinBattleSync.preferredTarget(player);
        for (Guard guard : get(level).guards.values()) {
            if ((guard.defeated && !guard.summonActive) || guard.entity == null) continue;
            Entity found = level.getEntity(guard.entity);
            if (!(found instanceof net.minecraft.world.entity.LivingEntity living)
                    || !matchesGuardian(guard, living) || !living.isAlive() || living.isRemoved()) continue;
            double candidateDistance = player.distanceToSqr(living);
            if (candidateDistance > BATTLE_VIEW_RANGE * BATTLE_VIEW_RANGE) continue;
            BattleGuard candidate = new BattleGuard(living, StarRailPath.byId(guard.variantId), guard.summonActive);
            if (found.getUUID().equals(attacked)) return candidate;
            if (candidateDistance < distance) {
                nearest = candidate;
                distance = candidateDistance;
            }
        }
        return nearest;
    }

    /** Activates only the matching path anchor generated inside its registered ruin. */
    public static boolean trySummonGuardian(ServerLevel level, BlockPos anchor,
            net.minecraft.world.entity.player.Player player, StarRailPath path) {
        BlockPos origin = anchor.offset(2, -10, 21);
        StarRailRuinGuardService data = get(level);
        Guard guard = data.guards.get(origin);
        if (guard == null || !guard.variantId.equals(path.getId())
                || !summonAnchorPos(origin).equals(anchor)) {
            player.displayClientMessage(Component.translatable(
                    "message.starrail_sim.ruin_anchor.unbound",
                    StarRailRuinContent.pathName(path)), true);
            return false;
        }
        if (!guard.defeated) {
            player.displayClientMessage(Component.translatable(
                    "message.starrail_sim.ruin_anchor.first_clear",
                    StarRailRuinContent.pathName(path)), true);
            return false;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            player.displayClientMessage(Component.translatable(
                    "message.starrail_sim.ruin_anchor.peaceful"), true);
            return false;
        }
        if (anchorView(level, anchor, path).phase() == AnchorPhase.WAITING) {
            player.displayClientMessage(Component.translatable(
                    "message.starrail_sim.ruin_anchor.waiting"), true);
            return false;
        }
        if (guard.summonActive) {
            player.displayClientMessage(Component.translatable(
                    "message.starrail_sim.ruin_anchor.active"), true);
            return false;
        }
        long remaining = guard.summonCooldownUntil - level.getGameTime();
        if (remaining > 0) {
            player.displayClientMessage(Component.translatable("message.starrail_sim.ruin_anchor.cooldown",
                    Math.max(1L, (remaining + 19L) / 20L)), true);
            return false;
        }

        Entity existing = guard.entity == null ? null : level.getEntity(guard.entity);
        if (existing instanceof net.minecraft.world.entity.LivingEntity living
                && matchesGuardian(guard, living) && !living.isDeadOrDying()) {
            guard.summonActive = true;
            data.setDirty();
            player.displayClientMessage(Component.translatable(
                    "message.starrail_sim.ruin_anchor.active"), true);
            return false;
        }
        guard.summonActive = true;
        guard.respawn = true;
        data.setDirty();
        if (!data.spawnGuardian(level, origin, guard)) {
            guard.summonActive = false;
            guard.respawn = false;
            data.setDirty();
            player.displayClientMessage(Component.translatable(
                    "message.starrail_sim.ruin_anchor.failed"), true);
            return false;
        }
        notifyNearby(level, origin, Component.translatable(
                "message.starrail_sim.ruin_anchor.summoned", StarRailRuinContent.pathName(path)));
        return true;
    }

    /** 载入已标记的宝箱即可恢复登记；旧版普通宝箱没有标记，不参与守卫机制。 */
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level
                && event.getChunk() instanceof net.minecraft.world.level.chunk.LevelChunk chunk) {
            for (var blockEntity : chunk.getBlockEntities().values()) {
                if (blockEntity instanceof ChestBlockEntity chest
                        && chest.getPersistentData().contains(ORIGIN_TAG)) {
                    BlockPos origin = BlockPos.of(chest.getPersistentData().getLong(ORIGIN_TAG));
                    queue(level, origin);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(net.minecraftforge.event.level.LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            QUEUED.remove(level);
            PENDING_GUARDIAN_RECONCILES.remove(level);
        }
    }

    /** 不把“UUID 暂时查不到”当成死亡：卸载区块不会重复刷守卫或误开宝箱。 */
    @SubscribeEvent
    public static void onTick(TickEvent.LevelTickEvent event) {
        if (!(event.level instanceof ServerLevel level)) return;
        if (event.phase == TickEvent.Phase.START) {
            StarRailRuinGuardService data = get(level);
            processPendingGuardianReconciles(level, data);
            cleanupMigratedWardens(level, data);
            return;
        }
        if (event.phase != TickEvent.Phase.END || level.getGameTime() % 20 != 0) return;
        var data = get(level);
        data.confirmDeaths(level);
        Set<BlockPos> queued = QUEUED.get(level);
        if (queued != null) {
            for (BlockPos origin : Set.copyOf(queued)) {
                if (!level.hasChunkAt(chestPos(origin))) continue;
                String variantId = "ordinary";
                if (level.getBlockEntity(chestPos(origin)) instanceof ChestBlockEntity chest
                        && chest.getPersistentData().contains(VARIANT_TAG)) {
                    variantId = chest.getPersistentData().getString(VARIANT_TAG);
                }
                register(level, origin, variantId);
                queued.remove(origin);
            }
        }
        data.guards.forEach((origin, guard) -> {
            StarRailPath path = StarRailPath.byId(guard.variantId);
            if (path.isRealPath() && !guard.anchorInstalled) {
                BlockPos anchor = summonAnchorPos(origin);
                if (level.hasChunkAt(anchor)) {
                    // 新蓝图已含锚点；旧结构只在空位补建，并且只执行一次。
                    if (level.getBlockState(anchor).isAir()) level.setBlockAndUpdate(anchor,
                            StarRailRuinContent.anchor(path).get().defaultBlockState());
                    guard.anchorInstalled = true;
                    data.setDirty();
                }
            }
            if (guard.defeated) {
                data.unlock(level, origin, guard);
                if (guard.summonActive && (guard.entity == null || guard.respawn))
                    data.spawnGuardian(level, origin, guard);
                return;
            }
            if (guard.entity == null || guard.respawn) data.spawnGuardian(level, origin, guard);
        });
    }

    private boolean spawnGuardian(ServerLevel level, BlockPos origin, Guard guard) {
        BlockPos spawn = origin.offset(-2, 8, -10);
        if (!level.hasChunkAt(spawn) || !level.hasChunkAt(chestPos(origin))
                || level.getDifficulty() == Difficulty.PEACEFUL) return false;
        StarveinConstructGuardian created = StarveinConstructEntities
                .STARVEIN_CONSTRUCT_GUARDIAN.get().create(level);
        if (created == null) return false;
        created.moveTo(spawn.getX() + .5, spawn.getY(), spawn.getZ() + .5, 0, 0);
        created.configureForPath(StarRailPath.byId(guard.variantId));
        // This is a new first battle or replay, so initialize it at its configured cap.
        // Keep this out of configureForPath: that method also runs on saved, damaged mobs.
        created.setHealth(created.getMaxHealth());
        created.setCustomName(Component.translatable("entity.starrail_sim.starvein_construct_guardian"));
        created.setPersistenceRequired();
        created.getPersistentData().putLong(ORIGIN_TAG, origin.asLong());
        if (!level.addFreshEntity(created)) return false;
        LOGGER.debug("Ruin guard spawned: origin={}, previous={}, respawn={}, uuid={}",
                origin, guard.entity, guard.respawn, created.getUUID());
        guard.entity = created.getUUID();
        guard.constructGuardian = true;
        guard.constructPrototype = false;
        guard.respawn = false;
        setDirty();
        return true;
    }

    /**
     * Entity NBT and SavedData are written independently. Reconcile a tagged construct on
     * the next server tick so a crash between addFreshEntity and UUID binding can recover
     * without treating an unloaded UUID as a death.
     */
    private static void processPendingGuardianReconciles(ServerLevel level,
                                                          StarRailRuinGuardService data) {
        Set<UUID> pending = PENDING_GUARDIAN_RECONCILES.get(level);
        if (pending == null) return;
        for (UUID uuid : Set.copyOf(pending)) {
            Entity found = level.getEntity(uuid);
            if (!(found instanceof StarveinConstructGuardian guardian)
                    || guardian.isRemoved()) {
                pending.remove(uuid);
                continue;
            }
            CompoundTag persistent = guardian.getPersistentData();
            if (!persistent.contains(ORIGIN_TAG)) {
                pending.remove(uuid);
                continue;
            }
            BlockPos origin = BlockPos.of(persistent.getLong(ORIGIN_TAG));
            Guard guard = data.guards.get(origin);
            if (guard == null) continue;
            reconcileConstructGuardian(level, data, origin, guard, guardian);
            pending.remove(uuid);
        }
        if (pending.isEmpty()) PENDING_GUARDIAN_RECONCILES.remove(level, pending);
    }

    private static void reconcileConstructGuardian(ServerLevel level,
            StarRailRuinGuardService data, BlockPos origin, Guard guard,
            StarveinConstructGuardian guardian) {
        UUID uuid = guardian.getUUID();
        // A completed, inactive ruin must not be reopened by a stale entity saved during
        // the same tick as a victory.
        if (guard.defeated && !guard.summonActive) {
            if (uuid.equals(guard.entity)) guard.entity = null;
            guardian.discard();
            data.setDirty();
            return;
        }
        if (guard.constructGuardian && uuid.equals(guard.entity)) {
            guardian.configureForPath(StarRailPath.byId(guard.variantId));
            return;
        }

        CompoundTag persistent = guardian.getPersistentData();
        UUID migrationSource = persistent.hasUUID(MIGRATION_SOURCE_TAG)
                ? persistent.getUUID(MIGRATION_SOURCE_TAG) : null;
        boolean migratingBoundWarden = !guard.constructGuardian && guard.entity != null
                && guard.entity.equals(migrationSource);
        boolean firstSpawnWithoutBinding = guard.entity == null;
        boolean respawnWithoutOldEntity = guard.respawn && guard.entity != null
                && level.getEntity(guard.entity) == null;
        if (migratingBoundWarden || firstSpawnWithoutBinding || respawnWithoutOldEntity) {
            if (migratingBoundWarden) guard.legacyEntity = migrationSource;
            guard.entity = uuid;
            guard.constructGuardian = true;
            guard.constructPrototype = false;
            guard.respawn = false;
            guardian.configureForPath(StarRailPath.byId(guard.variantId));
            data.setDirty();
            return;
        }

        // If a crash saved both the intended replacement and an orphan copy, keep the
        // UUID already recorded in SavedData and discard only the extra marked copy.
        if (guard.constructGuardian && guard.legacyEntity != null
                && guard.legacyEntity.equals(migrationSource) && guard.entity != null
                && !guard.entity.equals(uuid)) {
            Entity bound = level.getEntity(guard.entity);
            if (bound instanceof StarveinConstructGuardian active
                    && BlockPos.of(active.getPersistentData().getLong(ORIGIN_TAG)).equals(origin)) {
                guardian.discard();
            }
        }
    }

    /** A migrated Warden is removed only after its replacement is loaded and UUID-bound. */
    private static void cleanupMigratedWardens(ServerLevel level,
                                                StarRailRuinGuardService data) {
        data.guards.forEach((origin, guard) -> {
            if (guard.legacyEntity == null) return;
            Entity found = level.getEntity(guard.legacyEntity);
            if (!(found instanceof Warden previous)
                    || !previous.getPersistentData().contains(ORIGIN_TAG)
                    || !origin.equals(BlockPos.of(previous.getPersistentData().getLong(ORIGIN_TAG)))) return;

            if (guard.defeated && !guard.summonActive && guard.entity == null) {
                previous.discard();
                return;
            }
            Entity bound = guard.entity == null ? null : level.getEntity(guard.entity);
            if (bound instanceof StarveinConstructGuardian guardian
                    && origin.equals(BlockPos.of(guardian.getPersistentData().getLong(ORIGIN_TAG)))) {
                previous.discard();
            }
        });
    }

    /** 修复早期原型保存的缺失出生记忆，仅作用于本模组已标记守卫。 */
    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof net.minecraft.world.entity.Mob mob)
                || !mob.getPersistentData().contains(ORIGIN_TAG)) return;
        mob.setPersistenceRequired();
        if (mob instanceof Warden warden) {
            migrateBoundWarden(level, warden);
        } else if (mob instanceof StarveinConstructGuardian guardian) {
            BlockPos origin = BlockPos.of(guardian.getPersistentData().getLong(ORIGIN_TAG));
            Guard guard = get(level).guards.get(origin);
            if (guard != null) guardian.configureForPath(StarRailPath.byId(guard.variantId));
            PENDING_GUARDIAN_RECONCILES.computeIfAbsent(level,
                    key -> ConcurrentHashMap.newKeySet()).add(guardian.getUUID());
        }
        if (mob instanceof Warden warden
                && !warden.getBrain().hasMemoryValue(net.minecraft.world.entity.ai.memory.MemoryModuleType.DIG_COOLDOWN)) {
            warden.getBrain().setMemoryWithExpiry(
                    net.minecraft.world.entity.ai.memory.MemoryModuleType.DIG_COOLDOWN,
                    net.minecraft.util.Unit.INSTANCE, 1200L);
        }
    }

    /**
     * Converts only a saved Warden whose UUID and persistent ruin origin both match the
     * registered guard. Unknown, natural, and merely nearby Wardens are left untouched.
     */
    private static void migrateBoundWarden(ServerLevel level, Warden previous) {
        CompoundTag oldData = previous.getPersistentData();
        if (!oldData.contains(ORIGIN_TAG)) return;
        BlockPos origin = BlockPos.of(oldData.getLong(ORIGIN_TAG));
        StarRailRuinGuardService data = get(level);
        Guard guard = data.guards.get(origin);
        if (guard == null || guard.constructGuardian || guard.entity == null
                || !previous.getUUID().equals(guard.entity)) return;

        // A saved live Warden after a recorded first victory is stale state, not permission
        // to reopen a cleared vault or start a second first-clear reward cycle.
        if (guard.defeated && !guard.summonActive) {
            guard.legacyEntity = previous.getUUID();
            guard.constructGuardian = true;
            guard.constructPrototype = false;
            guard.entity = null;
            guard.respawn = false;
            data.setDirty();
            previous.discard();
            return;
        }

        StarveinConstructGuardian replacement = StarveinConstructEntities
                .STARVEIN_CONSTRUCT_GUARDIAN.get().create(level);
        if (replacement == null) return;
        replacement.moveTo(previous.getX(), previous.getY(), previous.getZ(),
                previous.getYRot(), previous.getXRot());
        replacement.configureForPath(StarRailPath.byId(guard.variantId));
        float previousMaxHealth = previous.getMaxHealth();
        float healthFraction = previousMaxHealth > 0.0F
                ? Math.min(1.0F, Math.max(0.0F, previous.getHealth() / previousMaxHealth))
                : 1.0F;
        replacement.setHealth(replacement.getMaxHealth() * healthFraction);
        replacement.setDeltaMovement(previous.getDeltaMovement());
        replacement.setCustomName(Component.translatable(
                "entity.starrail_sim.starvein_construct_guardian"));
        replacement.setPersistenceRequired();
        replacement.getPersistentData().merge(oldData.copy());
        replacement.getPersistentData().putUUID(MIGRATION_SOURCE_TAG, previous.getUUID());
        guard.legacyEntity = previous.getUUID();
        data.setDirty();

        // Add the replacement before removing the old entity; if addition fails, the
        // registered Warden and its saved combat state remain available for another try.
        if (!level.addFreshEntity(replacement)) {
            guard.legacyEntity = null;
            data.setDirty();
            return;
        }
        guard.entity = replacement.getUUID();
        guard.constructGuardian = true;
        guard.constructPrototype = false;
        guard.respawn = false;
        data.setDirty();
        previous.discard();
        LOGGER.info("Migrated registered ruin guardian: origin={}, oldUuid={}, newUuid={}, defeated={}",
                origin, previous.getUUID(), replacement.getUUID(), guard.defeated);
    }

    /** 只有本遗迹绑定的守卫死亡才永久解锁，其他监守者与区块卸载不计入。 */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        net.minecraft.world.entity.LivingEntity guardian = event.getEntity();
        if (event.isCanceled() || !(guardian.level() instanceof ServerLevel level)
                || !guardian.getPersistentData().contains(ORIGIN_TAG)) return;
        var data = get(level);
        BlockPos origin = BlockPos.of(guardian.getPersistentData().getLong(ORIGIN_TAG));
        Guard guard = data.guards.get(origin);
        if (guard == null || !matchesGuardian(guard, guardian)
                || !guardian.getUUID().equals(guard.entity)
                || (guard.defeated && !guard.summonActive)) return;
        // 等事件分发结束后核对真实死亡，避免别的监听器取消死亡时提前发奖。
        data.pendingDeaths.put(guardian.getUUID(), guardian);
    }

    /** Each path guardian death gets one server-side roll for its matching material. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGuardDrops(LivingDropsEvent event) {
        net.minecraft.world.entity.LivingEntity guardian = event.getEntity();
        if (event.isCanceled() || !(guardian.level() instanceof ServerLevel level)
                || !guardian.getPersistentData().contains(ORIGIN_TAG)) return;
        BlockPos origin = BlockPos.of(guardian.getPersistentData().getLong(ORIGIN_TAG));
        var data = get(level);
        Guard guard = data.guards.get(origin);
        StarRailPath path = guard == null ? StarRailPath.NONE : StarRailPath.byId(guard.variantId);
        if (guard == null || !matchesGuardian(guard, guardian) || !path.isRealPath()
                || !guardian.getUUID().equals(guard.entity)
                || (guard.defeated && !guard.summonActive)
                || data.pendingDrops.containsKey(guardian.getUUID())) return;
        ItemEntity drop = null;
        if (level.getRandom().nextFloat() < 0.30F) {
            drop = new ItemEntity(level, guardian.getX(), guardian.getY() + 0.5D,
                guardian.getZ(), new net.minecraft.world.item.ItemStack(
                        StarRailTraceMaterials.get(path)));
            drop.setDefaultPickUpDelay();
            event.getDrops().add(drop);
        }
        // Keep the event reference until confirmation, so later cancellation/removal is respected.
        data.pendingDrops.put(guardian.getUUID(), new DropRoll(event, drop, path));
    }

    private static void notifyNearby(ServerLevel level, BlockPos origin, Component message) {
        for (var player : level.players()) if (player.isAlive() && player.distanceToSqr(
                origin.getX(), origin.getY() + 8, origin.getZ()) <= 96 * 96)
            StarRailNetwork.sendRuinNotification(player, message);
    }

    private void confirmDeaths(ServerLevel level) {
        for (net.minecraft.world.entity.LivingEntity guardian : pendingDeaths.values()) {
            if (guardian.getHealth() > 0 || !guardian.isDeadOrDying()) continue;
            BlockPos origin = BlockPos.of(guardian.getPersistentData().getLong(ORIGIN_TAG));
            Guard guard = guards.get(origin);
            if (guard == null || !matchesGuardian(guard, guardian)
                    || !guardian.getUUID().equals(guard.entity)
                    || (guard.defeated && !guard.summonActive)) continue;
            Component result;
            if (!guard.defeated) {
                guard.defeated = true;
                setDirty();
                unlock(level, origin, guard);
                result = Component.translatable("message.starrail_sim.ruin_anchor.first_victory");
            } else {
                guard.summonActive = false;
                guard.respawn = false;
                guard.summonCooldownUntil = level.getGameTime() + SUMMON_COOLDOWN_TICKS;
                setDirty();
                result = Component.translatable("message.starrail_sim.ruin_anchor.defeated");
            }
            DropRoll roll = pendingDrops.get(guardian.getUUID());
            if (roll != null) {
                boolean dropped = !roll.event().isCanceled() && roll.material() != null
                        && roll.event().getDrops().contains(roll.material());
                result = result.copy().append("\n").append(dropped
                        ? Component.translatable("message.starrail_sim.ruin_anchor.material_drop",
                            new net.minecraft.world.item.ItemStack(StarRailTraceMaterials.get(roll.path())).getHoverName())
                        : Component.translatable("message.starrail_sim.ruin_anchor.no_material"));
            } else if (StarRailPath.byId(guard.variantId).isRealPath()) {
                result = result.copy().append("\n").append(Component.translatable(
                        "message.starrail_sim.ruin_anchor.no_material"));
            }
            notifyNearby(level, origin, result);
        }
        pendingDeaths.clear();
        pendingDrops.clear();
    }

    /** 正常退出时先确认本 tick 的死亡，避免死亡与保存相邻时丢失通关状态。 */
    @SubscribeEvent
    public static void onStopping(net.minecraftforge.event.server.ServerStoppingEvent event) {
        for (ServerLevel level : event.getServer().getAllLevels()) get(level).confirmDeaths(level);
    }

    /** Peaceful removal and other discards are not victories; restore the selected guardian type. */
    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        if (event.getLevel() instanceof ServerLevel level) {
            Set<UUID> pending = PENDING_GUARDIAN_RECONCILES.get(level);
            if (pending != null) pending.remove(entity.getUUID());
        }
        if (entity instanceof net.minecraft.world.entity.LivingEntity
                && entity.getPersistentData().contains(ORIGIN_TAG))
            LOGGER.debug("Ruin guard leaving: uuid={}, reason={}", entity.getUUID(), entity.getRemovalReason());
        if (!(entity instanceof net.minecraft.world.entity.LivingEntity guardian)
                || !(event.getLevel() instanceof ServerLevel level)
                || entity.getRemovalReason() != Entity.RemovalReason.DISCARDED
                || !entity.getPersistentData().contains(ORIGIN_TAG)) return;
        var data = get(level);
        Guard guard = data.guards.get(BlockPos.of(entity.getPersistentData().getLong(ORIGIN_TAG)));
        if (guard != null && entity.getUUID().equals(guard.legacyEntity)
                && entity.getRemovalReason() == Entity.RemovalReason.DISCARDED) {
            guard.legacyEntity = null;
            data.setDirty();
        }
        if (guard != null && matchesGuardian(guard, guardian)
                && (!guard.defeated || guard.summonActive)
                && guardian.getUUID().equals(guard.entity)) {
            guard.respawn = true;
            data.setDirty();
        }
    }

    /** 开门支持分区块恢复；战利品表仅设置一次，取空宝箱后不会重新补奖。 */
    private void unlock(ServerLevel level, BlockPos origin, Guard guard) {
        for (int x = -2; x <= 2; x++) for (int y = 8; y <= 12; y++) {
            int bit = 1 << ((x + 2) * 5 + y - 8);
            if ((guard.openedGate & bit) != 0) continue;
            BlockPos gate = origin.offset(x, y, -18);
            if (level.hasChunkAt(gate)) {
                if (level.getBlockState(gate).is(Blocks.IRON_BARS)) level.removeBlock(gate, false);
                guard.openedGate |= bit;
                setDirty();
            }
        }
        BlockPos pos = chestPos(origin);
        if (!guard.rewarded && level.hasChunkAt(pos)) {
            // 爆炸/玩家破坏已受保护；管理员命令移除的箱子也可在获胜后恢复。
            if (!level.getBlockState(pos).is(Blocks.CHEST)) level.setBlockAndUpdate(pos,
                    Blocks.CHEST.defaultBlockState().setValue(
                            net.minecraft.world.level.block.ChestBlock.FACING,
                            net.minecraft.core.Direction.SOUTH));
            if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
                chest.setLootTable(new ResourceLocation(StarRailSimMod.MOD_ID,
                        "chests/ruin_" + guard.variantId), level.getRandom().nextLong());
                chest.setChanged();
                guard.rewarded = true;
                setDirty();
            }
        }
    }

    private static boolean locked(ServerLevel level, BlockPos pos, boolean includeGate) {
        if (includeGate && StarRailRuinContent.isAnchor(level.getBlockState(pos).getBlock())) return true;
        var data = get(level);
        for (var entry : data.guards.entrySet()) {
            if (includeGate && StarRailPath.byId(entry.getValue().variantId).isRealPath()
                    && pos.equals(summonAnchorPos(entry.getKey()))) return true;
            if (entry.getValue().defeated) continue;
            BlockPos origin = entry.getKey();
            if (pos.equals(chestPos(origin))) return true;
            if (includeGate && (isVaultBoundary(origin, pos)
                    || isGate(origin, pos))) return true;
        }
        // 世界生成后登记会延迟到下一 tick；等待登记期间也锁住密室外壳。
        Set<BlockPos> queued = QUEUED.get(level);
        if (queued != null) for (BlockPos origin : queued) {
            Guard guard = data.guards.get(origin);
            if (guard != null && guard.defeated) continue;
            if (pos.equals(chestPos(origin))) return true;
            if (includeGate && (isVaultBoundary(origin, pos)
                    || isGate(origin, pos))) return true;
        }
        // 刚生成、尚未进入下一秒登记的宝箱也立即保护。
        return level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof ChestBlockEntity chest
                && chest.getPersistentData().contains(ORIGIN_TAG)
                && !isDefeated(level, BlockPos.of(chest.getPersistentData().getLong(ORIGIN_TAG)));
    }

    /** 宝箱室外壳：局部 X=-12…12、Y=7…24、Z=-26…-18 的六个边界面。 */
    private static boolean isVaultBoundary(BlockPos origin, BlockPos pos) {
        int x = pos.getX() - origin.getX();
        int y = pos.getY() - origin.getY();
        int z = pos.getZ() - origin.getZ();
        return x >= -12 && x <= 12 && y >= 7 && y <= 24 && z >= -26 && z <= -18
                && (x == -12 || x == 12 || y == 7 || y == 24 || z == -26 || z == -18);
    }

    /** 门洞与外壳分开判断，确保五乘五格的门仍受严格守卫门禁保护。 */
    private static boolean isGate(BlockPos origin, BlockPos pos) {
        return pos.getZ() == origin.getZ() - 18
                && Math.abs(pos.getX() - origin.getX()) <= 2
                && pos.getY() >= origin.getY() + 8 && pos.getY() <= origin.getY() + 12;
    }

    private static boolean isDefeated(ServerLevel level, BlockPos origin) {
        Guard guard = get(level).guards.get(origin);
        return guard != null && guard.defeated;
    }

    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel() instanceof ServerLevel level && locked(level, event.getPos(), false)) {
            event.setCanceled(true);
            if (event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND)
                event.getEntity().displayClientMessage(Component.literal("击败本座遗迹的监守者后才能开启宝箱。"), true);
        }
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (event.getPlayer().getAbilities().instabuild) {
            // 创造模式拆除宝箱前先保存命途，避免队列登记前方块实体数据被移除。
            if (level.getBlockEntity(event.getPos()) instanceof ChestBlockEntity chest
                    && chest.getPersistentData().contains(ORIGIN_TAG)) {
                BlockPos origin = BlockPos.of(chest.getPersistentData().getLong(ORIGIN_TAG));
                String variantId = chest.getPersistentData().getString(VARIANT_TAG);
                register(level, origin, variantId.isBlank() ? "ordinary" : variantId);
            }
            return;
        }
        if (locked(level, event.getPos(), true))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof ServerLevel level)
            event.getAffectedBlocks().removeIf(pos -> locked(level, pos, true));
    }

    /** 防止旁边放普通箱子合并成大箱绕过交互锁；其他建筑方块仍可自由施工。 */
    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !event.getPlacedBlock().is(Blocks.CHEST)) return;
        for (var direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            if (locked(level, event.getPos().relative(direction), false)) {
                event.setCanceled(true);
                return;
            }
        }
    }
}
