package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/** Server-authoritative wave spawning and kill tracking for the relic arena prototype. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID)
public final class RelicTrialBattleService {
    private static final String DEVICE_POS_TAG = "StarRailRelicArenaDevice";
    private static final String SESSION_TAG = "StarRailRelicArenaSession";
    private static final String ELITE_TAG = "StarRailRelicArenaElite";
    private static final double MAX_DEFAULT_MOB_HEALTH = 160.0D;
    private static final double MAX_DEFAULT_MOB_WIDTH = 2.5D;
    private static final double MAX_DEFAULT_MOB_HEIGHT = 3.5D;
    private static final TagKey<EntityType<?>> ARENA_EXCLUDED = TagKey.create(
            Registries.ENTITY_TYPE, new ResourceLocation(StarRailSimMod.MOD_ID, "arena_excluded"));
    private static final TagKey<EntityType<?>> ARENA_RANGED = TagKey.create(
            Registries.ENTITY_TYPE, new ResourceLocation(StarRailSimMod.MOD_ID, "arena_ranged"));
    private static final TagKey<EntityType<?>> FORGE_BOSSES = TagKey.create(
            Registries.ENTITY_TYPE, new ResourceLocation("forge", "bosses"));
    private static final Random RANDOM = new Random();

    private RelicTrialBattleService() {
    }

    public static boolean start(ServerLevel level, RelicTrialDeviceBlockEntity device,
                                ServerPlayer player) {
        if (device.isBattleActive()) {
            player.displayClientMessage(Component.translatable(
                    "message.starrail_sim.arena.already_active"), true);
            return false;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            player.displayClientMessage(Component.translatable(
                    "message.starrail_sim.arena.peaceful"), true);
            return false;
        }

        int rank = player.getCapability(StarRailPathCapability.PATH_DATA)
                .map(data -> data.getCurrentPathRank().getLevel())
                .orElse(0);
        if (rank <= 0) {
            player.displayClientMessage(Component.translatable(
                    "message.starrail_sim.arena.unaligned"), true);
            return false;
        }
        rank = Math.min(7, rank);
        RelicTrialSet set = RelicTrialSet.byIndex(device.selectedSetIndex());
        int rewardStars = rollRewardStars(rank, player.getRandom().nextInt(100));
        RelicStackData.Part[] rewardParts = set.inner()
                ? new RelicStackData.Part[]{RelicStackData.Part.SPHERE, RelicStackData.Part.ROPE}
                : new RelicStackData.Part[]{RelicStackData.Part.HEAD, RelicStackData.Part.HANDS,
                RelicStackData.Part.BODY, RelicStackData.Part.FEET};
        RelicStackData.Part rewardPart = rewardParts[player.getRandom().nextInt(rewardParts.length)];
        ItemStack plannedReward = StarRailRelicItems.createGeneratedStack(
                RelicStackData.SetId.valueOf(set.name()), rewardPart, rewardStars, player.getRandom());
        device.beginBattle(player.getUUID(), rank, device.selectedSetIndex(), plannedReward);
        if (!spawnWave(level, device, 1)) {
            device.abortBattle();
            player.displayClientMessage(Component.translatable(
                    "message.starrail_sim.arena.spawn_failed"), true);
            return false;
        }
        player.displayClientMessage(Component.translatable(
                "message.starrail_sim.arena.started", rank,
                RelicTrialSet.byIndex(device.selectedSetIndex()).displayName()), false);
        return true;
    }

    private static int rollRewardStars(int rank, int roll) {
        return switch (rank) {
            case 1 -> roll < 50 ? 2 : 3;
            case 2 -> roll < 80 ? 3 : 4;
            case 3 -> roll < 40 ? 3 : 4;
            case 4 -> roll < 80 ? 4 : 5;
            default -> 5;
        };
    }

    /** Build and validate a complete wave before adding any of its mobs to the world. */
    static boolean spawnWave(ServerLevel level, RelicTrialDeviceBlockEntity device, int waveNumber) {
        if (!device.isBattleActive() || waveNumber < 1 || waveNumber > 3) return false;
        List<Composition> compositions = waveComposition(device.challengeRank(), waveNumber);
        List<EntityType<?>> candidates = eligibleTypes(level);
        List<EntityType<?>> normal = matchingTypes(level, candidates, Composition.NORMAL);
        List<EntityType<?>> ranged = matchingTypes(level, candidates, Composition.RANGED);
        if (normal.isEmpty()) normal = vanillaFallback(Composition.NORMAL);
        if (ranged.isEmpty()) ranged = vanillaFallback(Composition.RANGED);
        if (candidates.isEmpty()) candidates = vanillaFallback(Composition.NORMAL);

        BlockPos devicePos = device.getBlockPos();
        List<BlockPos> pads = new ArrayList<>(List.of(
                devicePos.offset(0, 0, -8),
                devicePos.offset(8, 0, 0),
                devicePos.offset(0, 0, 8),
                devicePos.offset(-8, 0, 0)));
        Collections.shuffle(pads, RANDOM);

        List<Mob> prepared = new ArrayList<>(4);
        for (int index = 0; index < compositions.size(); index++) {
            Composition composition = compositions.get(index);
            List<EntityType<?>> pool = composition == Composition.RANGED ? ranged
                    : composition == Composition.NORMAL ? normal : candidates;
            Mob mob = prepareMob(level, pool, pads.get(index),
                    composition == Composition.ELITE, device.challengeRank());
            if (mob == null) {
                prepared.forEach(Entity::discard);
                return false;
            }
            prepared.add(mob);
        }

        UUID session = device.sessionId();
        for (Mob mob : prepared) {
            mob.getPersistentData().putLong(DEVICE_POS_TAG, devicePos.asLong());
            mob.getPersistentData().putUUID(SESSION_TAG, session);
        }
        List<UUID> spawnedIds = new ArrayList<>(prepared.size());
        for (Mob mob : prepared) {
            if (!level.addFreshEntity(mob)) {
                prepared.forEach(Entity::discard);
                return false;
            }
            spawnedIds.add(mob.getUUID());
            ServerPlayer owner = device.ownerId() == null ? null
                    : level.getServer().getPlayerList().getPlayer(device.ownerId());
            if (owner != null) mob.setTarget(owner);
        }
        device.setWaveSpawned(waveNumber, spawnedIds);
        if (device.ownerId() != null) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(device.ownerId());
            if (owner != null) owner.displayClientMessage(Component.translatable(
                    "message.starrail_sim.arena.wave_started", waveNumber), true);
        }
        return true;
    }

    private static Mob prepareMob(ServerLevel level, List<EntityType<?>> pool, BlockPos pad,
                                  boolean elite, int rank) {
        if (pool.isEmpty()) return null;
        List<EntityType<?>> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, RANDOM);
        for (EntityType<?> type : shuffled) {
            Entity created;
            try {
                created = type.create(level);
            } catch (RuntimeException exception) {
                continue;
            }
            if (!(created instanceof Mob mob)) {
                if (created != null) created.discard();
                continue;
            }

            BlockPos spawnPos = findSpawnPosition(level, mob, pad);
            if (spawnPos == null) {
                mob.discard();
                continue;
            }
            mob.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(),
                    spawnPos.getZ() + 0.5D, RANDOM.nextFloat() * 360.0F, 0.0F);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos),
                    MobSpawnType.STRUCTURE, null, null);
            if (isOversizedOrBossLike(mob)) {
                mob.discard();
                continue;
            }
            if (elite) {
                mob.getPersistentData().putBoolean(ELITE_TAG, true);
                double healthMultiplier = rank >= 7 ? 2.5D : rank >= 5 ? 2.0D : 1.5D;
                double attackMultiplier = rank >= 7 ? 1.4D : rank >= 5 ? 1.3D : 1.2D;
                scaleAttribute(mob, Attributes.MAX_HEALTH, healthMultiplier);
                scaleAttribute(mob, Attributes.ATTACK_DAMAGE, attackMultiplier);
                mob.setHealth(mob.getMaxHealth());
            }
            mob.setPersistenceRequired();
            if (!level.noCollision(mob)) {
                mob.discard();
                continue;
            }
            return mob;
        }
        return null;
    }

    /** Conservative fallback for modded boss entities without Forge's boss tag. */
    private static boolean isOversizedOrBossLike(Mob mob) {
        return mob.getMaxHealth() > MAX_DEFAULT_MOB_HEALTH
                || mob.getBbWidth() > MAX_DEFAULT_MOB_WIDTH
                || mob.getBbHeight() > MAX_DEFAULT_MOB_HEIGHT;
    }

    private static BlockPos findSpawnPosition(ServerLevel level, Mob mob, BlockPos pad) {
        // Support both a device placed directly on the floor and one raised over a pedestal.
        // Ice and other walkable translucent blocks are valid support too, so test the
        // upward sturdy face instead of requiring an opaque render cube.
        int[] verticalOffsets = {-1, 0, 1, -2, 2};
        for (int dy : verticalOffsets) {
            for (int radius = 0; radius <= 2; radius++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                        BlockPos candidate = pad.offset(dx, dy, dz);
                        if (!level.hasChunkAt(candidate) || !level.getBlockState(candidate).isAir()
                                || !level.getFluidState(candidate).isEmpty()) continue;
                        BlockPos floorPos = candidate.below();
                        BlockState floor = level.getBlockState(floorPos);
                        if (!floor.isFaceSturdy(level, floorPos, Direction.UP)) continue;
                        mob.moveTo(candidate.getX() + 0.5D, candidate.getY(),
                                candidate.getZ() + 0.5D, 0.0F, 0.0F);
                        if (level.noCollision(mob)) return candidate;
                    }
                }
            }
        }
        return null;
    }

    private static void scaleAttribute(Mob mob,
                                       net.minecraft.world.entity.ai.attributes.Attribute attribute,
                                       double multiplier) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance != null) instance.setBaseValue(instance.getBaseValue() * multiplier);
    }

    private static List<EntityType<?>> eligibleTypes(ServerLevel level) {
        List<EntityType<?>> types = new ArrayList<>();
        var registry = level.registryAccess().registryOrThrow(Registries.ENTITY_TYPE);
        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            if (type.getCategory() != MobCategory.MONSTER) continue;
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
            if (id == null || isExcludedByEntityId(id) || hasTag(registry, id, FORGE_BOSSES)
                    || hasTag(registry, id, ARENA_EXCLUDED)) continue;
            types.add(type);
        }
        return types;
    }

    private static List<EntityType<?>> matchingTypes(ServerLevel level,
                                                      List<EntityType<?>> types,
                                                      Composition composition) {
        var registry = level.registryAccess().registryOrThrow(Registries.ENTITY_TYPE);
        List<EntityType<?>> matches = new ArrayList<>();
        for (EntityType<?> type : types) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
            if (id == null) continue;
            boolean ranged = hasTag(registry, id, ARENA_RANGED) || looksRanged(id);
            if ((composition == Composition.RANGED) == ranged) matches.add(type);
        }
        return matches;
    }

    private static boolean hasTag(net.minecraft.core.Registry<EntityType<?>> registry,
                                  ResourceLocation id, TagKey<EntityType<?>> tag) {
        var holder = registry.getHolder(ResourceKey.create(Registries.ENTITY_TYPE, id));
        return holder.isPresent() && holder.get().is(tag);
    }

    private static boolean isExcludedByEntityId(ResourceLocation id) {
        String path = id.getPath().toLowerCase(java.util.Locale.ROOT);
        return path.contains("boss") || path.contains("dragon") || path.contains("wither")
                || path.contains("warden") || path.contains("elder_guardian")
                || path.contains("ravager") || path.contains("creeper");
    }

    private static boolean looksRanged(ResourceLocation id) {
        String path = id.getPath().toLowerCase(java.util.Locale.ROOT);
        return path.contains("skeleton") || path.contains("archer") || path.contains("pillager")
                || path.contains("shooter") || path.contains("ranged") || path.contains("bow");
    }

    private static List<EntityType<?>> vanillaFallback(Composition composition) {
        if (composition == Composition.RANGED)
            return List.<EntityType<?>>of(EntityType.SKELETON, EntityType.STRAY);
        return List.<EntityType<?>>of(EntityType.ZOMBIE, EntityType.HUSK, EntityType.SPIDER);
    }

    private static List<Composition> waveComposition(int rank, int wave) {
        String[] rows;
        if (rank >= 7) {
            rows = new String[]{"NREE", "NEEE", "EEEE"};
        } else if (rank >= 5) {
            rows = new String[]{"NNRE", "NREE", "NEEE"};
        } else if (rank >= 3) {
            rows = new String[]{"NNNN", "NNRE", "NREE"};
        } else {
            rows = new String[]{"NNNN", "NNNR", "NNRR"};
        }
        String row = rows[Math.max(0, Math.min(rows.length - 1, wave - 1))];
        List<Composition> result = new ArrayList<>(4);
        for (int i = 0; i < row.length(); i++) {
            result.add(switch (row.charAt(i)) {
                case 'R' -> Composition.RANGED;
                case 'E' -> Composition.ELITE;
                default -> Composition.NORMAL;
            });
        }
        return result;
    }

    public static void abort(ServerLevel level, RelicTrialDeviceBlockEntity device,
                             Component reason) {
        for (UUID id : device.activeMobIds()) {
            Entity entity = level.getEntity(id);
            if (entity != null) entity.discard();
        }
        UUID ownerId = device.ownerId();
        device.abortBattle();
        if (ownerId != null) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
            if (owner != null) owner.displayClientMessage(reason, true);
        }
    }

    @SubscribeEvent
    public static void onArenaMobDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        CompoundTagView marker = CompoundTagView.from(event.getEntity());
        if (marker == null) return;
        BlockPos pos = BlockPos.of(marker.devicePos());
        if (level.getBlockEntity(pos) instanceof RelicTrialDeviceBlockEntity device) {
            ServerPlayer killer = StarRailCombatEvents.getPlayerAttacker(event.getSource());
            if (killer != null && killer.distanceToSqr(pos.getX() + 0.5D,
                    pos.getY() + 0.5D, pos.getZ() + 0.5D) > 32.0D * 32.0D) killer = null;
            device.onTaggedMobKilled(marker.sessionId(), event.getEntity().getUUID(),
                    killer == null ? null : killer.getUUID(), event.getEntity().blockPosition());
        }
    }

    @SubscribeEvent
    public static void onArenaMobHurt(LivingHurtEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0.0F
                || !(event.getEntity().level() instanceof ServerLevel level)) return;
        CompoundTagView marker = CompoundTagView.from(event.getEntity());
        if (marker == null) return;
        ServerPlayer attacker = StarRailCombatEvents.getPlayerAttacker(event.getSource());
        if (attacker == null) return;
        BlockPos pos = BlockPos.of(marker.devicePos());
        if (level.getBlockEntity(pos) instanceof RelicTrialDeviceBlockEntity device) {
            device.registerParticipant(marker.sessionId(), attacker);
        }
    }

    private enum Composition { NORMAL, RANGED, ELITE }

    private record CompoundTagView(long devicePos, UUID sessionId) {
        static CompoundTagView from(Entity entity) {
            var tag = entity.getPersistentData();
            if (!tag.contains(DEVICE_POS_TAG) || !tag.hasUUID(SESSION_TAG)) return null;
            return new CompoundTagView(tag.getLong(DEVICE_POS_TAG), tag.getUUID(SESSION_TAG));
        }
    }
}
