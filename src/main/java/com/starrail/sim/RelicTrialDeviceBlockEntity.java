package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Saves the chosen relic set and one active duel session on the arena device. */
public final class RelicTrialDeviceBlockEntity extends BlockEntity {
    private static final int DATA_SIZE = 9;
    private static final int NEXT_WAVE_DELAY_TICKS = 60;
    private static final int ARENA_HALF_SIZE = 10;

    private final SimpleContainerData data = new SimpleContainerData(DATA_SIZE);
    private final List<UUID> activeMobIds = new ArrayList<>();
    private final Set<UUID> participants = new HashSet<>();
    private int selectedSetIndex;
    private int challengeRank = 1;
    private int waveIndex;
    private int aliveCount;
    private int transitionTicks;
    private int ownerAwayTicks;
    private boolean battleActive;
    private boolean battleComplete;
    private UUID ownerId;
    private UUID sessionId;
    private final List<ItemStack> plannedRewards = new ArrayList<>();
    private int rewardStars;
    private int rewardPartIndex = -1;
    private int rewardCount;

    public RelicTrialDeviceBlockEntity(BlockPos pos, BlockState state) {
        super(StarRailSimMod.RELIC_TRIAL_DEVICE_ENTITY.get(), pos, state);
        syncData();
    }

    public ContainerData containerData() {
        return data;
    }

    public int selectedSetIndex() {
        return selectedSetIndex;
    }

    public int challengeRank() {
        return challengeRank;
    }

    public int waveIndex() {
        return waveIndex;
    }

    public int aliveCount() {
        return aliveCount;
    }

    public boolean isBattleActive() {
        return battleActive;
    }

    public boolean isBattleComplete() {
        return battleComplete;
    }

    public int rewardStars() { return rewardStars; }
    public int rewardPartIndex() { return rewardPartIndex; }
    public int rewardCount() { return rewardCount; }

    public UUID sessionId() {
        return sessionId;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public List<UUID> activeMobIds() {
        return List.copyOf(activeMobIds);
    }

    public void setSelectedSetIndex(int index) {
        if (battleActive) return;
        selectedSetIndex = RelicTrialSet.clampIndex(index);
        setChanged();
        syncData();
        updateClients();
    }

    void beginBattle(UUID owner, int rank, int setIndex, List<ItemStack> rewards) {
        if (rewards.isEmpty() || rewards.size() > 3) {
            throw new IllegalArgumentException("Arena reward count must be between 1 and 3");
        }
        ownerId = owner;
        sessionId = UUID.randomUUID();
        challengeRank = Math.max(1, Math.min(7, rank));
        selectedSetIndex = RelicTrialSet.clampIndex(setIndex);
        plannedRewards.clear();
        for (ItemStack reward : rewards) {
            RelicStackData.read(reward).orElseThrow(
                    () -> new IllegalArgumentException("Arena reward must be a valid relic"));
            plannedRewards.add(reward.copy());
        }
        rewardCount = plannedRewards.size();
        RelicStackData.Data rewardData = RelicStackData.read(plannedRewards.get(0)).orElseThrow();
        rewardStars = rewardData.stars();
        rewardPartIndex = rewardData.part().ordinal();
        waveIndex = 0;
        aliveCount = 0;
        transitionTicks = 0;
        ownerAwayTicks = 0;
        battleActive = true;
        battleComplete = false;
        activeMobIds.clear();
        participants.clear();
        participants.add(owner);
        setChanged();
        syncData();
        updateClients();
    }

    void setWaveSpawned(int index, List<UUID> spawnedIds) {
        waveIndex = index;
        aliveCount = spawnedIds.size();
        activeMobIds.clear();
        activeMobIds.addAll(spawnedIds);
        transitionTicks = 0;
        setChanged();
        syncData();
        updateClients();
    }

    void abortBattle() {
        battleActive = false;
        battleComplete = false;
        waveIndex = 0;
        aliveCount = 0;
        transitionTicks = 0;
        ownerAwayTicks = 0;
        activeMobIds.clear();
        participants.clear();
        ownerId = null;
        sessionId = null;
        plannedRewards.clear();
        rewardStars = 0;
        rewardPartIndex = -1;
        rewardCount = 0;
        setChanged();
        syncData();
        updateClients();
    }

    private void completeBattle(ServerLevel serverLevel, BlockPos deathPos) {
        if (!battleActive || ownerId == null || plannedRewards.isEmpty()) return;
        List<ItemEntity> drops = new ArrayList<>(plannedRewards.size());
        for (int index = 0; index < plannedRewards.size(); index++) {
            ItemStack reward = plannedRewards.get(index);
            if (RelicStackData.read(reward).isEmpty()) {
                RelicTrialBattleService.abort(serverLevel, this, Component.translatable(
                        "message.starrail_sim.arena.reward_failed"));
                return;
            }
            double angle = Math.PI * 2.0D * index / Math.max(1, plannedRewards.size());
            ItemEntity drop = new ItemEntity(serverLevel,
                    deathPos.getX() + 0.5D + Math.cos(angle) * 0.35D,
                    deathPos.getY() + 0.25D,
                    deathPos.getZ() + 0.5D + Math.sin(angle) * 0.35D, reward.copy());
            drop.setTarget(ownerId);
            drop.setThrower(ownerId);
            drop.setPickUpDelay(10);
            drops.add(drop);
        }
        boolean allSpawned = true;
        for (ItemEntity drop : drops) {
            if (!serverLevel.addFreshEntity(drop)) {
                allSpawned = false;
                break;
            }
        }
        if (!allSpawned) {
            drops.forEach(Entity::discard);
            RelicTrialBattleService.abort(serverLevel, this, Component.translatable(
                    "message.starrail_sim.arena.reward_failed"));
            return;
        }
        battleActive = false;
        battleComplete = true;
        aliveCount = 0;
        transitionTicks = 0;
        ownerAwayTicks = 0;
        activeMobIds.clear();
        participants.clear();
        plannedRewards.clear();
        setChanged();
        syncData();
        updateClients();
        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(ownerId);
        if (player != null) {
            player.displayClientMessage(Component.translatable(
                    "message.starrail_sim.arena.victory", rewardCount,
                    RelicTrialSet.byIndex(selectedSetIndex).displayName()), false);
        }
    }

    void registerParticipant(UUID hitSession, ServerPlayer player) {
        if (!battleActive || sessionId == null || !sessionId.equals(hitSession)
                || player == null || player.level() != level
                || player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) > 32.0D * 32.0D
                || participants.contains(player.getUUID())) return;
        participants.add(player.getUUID());
        setChanged();
    }

    void tickOwnerPresence(ServerLevel serverLevel) {
        if (!battleActive || ownerId == null) return;
        ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerId);
        // A disconnected owner pauses the session; reconnecting resumes the same saved battle.
        if (owner == null) return;
        if (!owner.isAlive()) {
            RelicTrialBattleService.abort(serverLevel, this, Component.translatable(
                    "message.starrail_sim.arena.owner_dead"));
            return;
        }
        boolean present = owner.level() == serverLevel
                && owner.distanceToSqr(worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 32.0D * 32.0D;
        if (present) {
            if (ownerAwayTicks != 0) {
                ownerAwayTicks = 0;
                setChanged();
            }
            return;
        }
        ownerAwayTicks++;
        if (ownerAwayTicks >= 200) {
            RelicTrialBattleService.abort(serverLevel, this, Component.translatable(
                    "message.starrail_sim.arena.owner_left"));
        } else if (ownerAwayTicks % 20 == 0) {
            setChanged();
        }
    }

    void onTaggedMobKilled(UUID killedSession, UUID mobId, UUID killerId, BlockPos deathPos) {
        if (!battleActive || sessionId == null || !sessionId.equals(killedSession)
                || !activeMobIds.remove(mobId)) return;
        aliveCount = Math.max(0, aliveCount - 1);
        if (aliveCount == 0) {
            if (waveIndex >= 3) {
                if (killerId == null || !participants.contains(killerId)) {
                    if (level instanceof ServerLevel serverLevel) {
                        RelicTrialBattleService.abort(serverLevel, this, Component.translatable(
                                "message.starrail_sim.arena.unqualified_kill"));
                    }
                } else if (level instanceof ServerLevel serverLevel) {
                    completeBattle(serverLevel, deathPos);
                }
            } else {
                transitionTicks = NEXT_WAVE_DELAY_TICKS;
                setChanged();
                syncData();
                updateClients();
                if (level instanceof ServerLevel serverLevel && ownerId != null) {
                    ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(ownerId);
                    if (player != null) {
                        player.displayClientMessage(Component.translatable(
                                "message.starrail_sim.arena.wave_cleared", waveIndex), true);
                    }
                }
            }
        } else {
            setChanged();
            syncData();
            updateClients();
        }
    }

    private void syncData() {
        data.set(0, selectedSetIndex);
        data.set(1, challengeRank);
        data.set(2, waveIndex);
        data.set(3, aliveCount);
        data.set(4, battleActive ? 1 : 0);
        data.set(5, battleComplete ? 1 : 0);
        data.set(6, rewardStars);
        data.set(7, rewardPartIndex);
        data.set(8, rewardCount);
    }

    private void updateClients() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    static void serverTick(Level level, BlockPos pos, BlockState state,
                           BlockEntity rawDevice) {
        if (!(level instanceof ServerLevel serverLevel)
                || !(rawDevice instanceof RelicTrialDeviceBlockEntity device)
                || !device.battleActive) return;
        RelicTrialBattleService.keepMobsInsideArena(serverLevel, device, ARENA_HALF_SIZE);
        device.tickOwnerPresence(serverLevel);
        if (!device.battleActive || device.transitionTicks <= 0) return;
        device.transitionTicks--;
        if (device.transitionTicks == 0) {
            if (!RelicTrialBattleService.spawnWave(serverLevel, device, device.waveIndex + 1)) {
                RelicTrialBattleService.abort(serverLevel, device, Component.translatable(
                        "message.starrail_sim.arena.spawn_failed"));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("SelectedSet", selectedSetIndex);
        tag.putInt("ChallengeRank", challengeRank);
        tag.putInt("Wave", waveIndex);
        tag.putInt("Alive", aliveCount);
        tag.putInt("TransitionTicks", transitionTicks);
        tag.putInt("OwnerAwayTicks", ownerAwayTicks);
        tag.putBoolean("BattleActive", battleActive);
        tag.putBoolean("BattleComplete", battleComplete);
        tag.putInt("RewardStars", rewardStars);
        tag.putInt("RewardPart", rewardPartIndex);
        tag.putInt("RewardCount", rewardCount);
        ListTag rewardTags = new ListTag();
        for (ItemStack reward : plannedRewards) rewardTags.add(reward.save(new CompoundTag()));
        tag.put("PlannedRewards", rewardTags);
        if (ownerId != null) tag.putUUID("Owner", ownerId);
        if (sessionId != null) tag.putUUID("Session", sessionId);
        ListTag mobs = new ListTag();
        for (UUID mobId : activeMobIds) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Id", mobId);
            mobs.add(entry);
        }
        tag.put("ActiveMobs", mobs);
        ListTag participantTags = new ListTag();
        for (UUID participant : participants) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Id", participant);
            participantTags.add(entry);
        }
        tag.put("Participants", participantTags);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        selectedSetIndex = RelicTrialSet.clampIndex(tag.getInt("SelectedSet"));
        challengeRank = Math.max(1, Math.min(7, tag.getInt("ChallengeRank")));
        waveIndex = Math.max(0, Math.min(3, tag.getInt("Wave")));
        aliveCount = Math.max(0, tag.getInt("Alive"));
        transitionTicks = Math.max(0, tag.getInt("TransitionTicks"));
        ownerAwayTicks = Math.max(0, Math.min(199, tag.getInt("OwnerAwayTicks")));
        battleActive = tag.getBoolean("BattleActive");
        battleComplete = tag.getBoolean("BattleComplete");
        rewardStars = Math.max(0, Math.min(5, tag.getInt("RewardStars")));
        rewardPartIndex = tag.contains("RewardPart")
                ? Math.max(-1, Math.min(RelicStackData.Part.values().length - 1,
                tag.getInt("RewardPart"))) : -1;
        plannedRewards.clear();
        if (tag.contains("PlannedRewards", Tag.TAG_LIST)) {
            ListTag rewardTags = tag.getList("PlannedRewards", Tag.TAG_COMPOUND);
            for (int i = 0; i < rewardTags.size() && plannedRewards.size() < 3; i++) {
                ItemStack reward = ItemStack.of(rewardTags.getCompound(i));
                if (!reward.isEmpty()) plannedRewards.add(reward);
            }
        } else if (tag.contains("PlannedReward", Tag.TAG_COMPOUND)) {
            // Migrate an in-progress world saved by the single-reward prototype.
            ItemStack legacyReward = ItemStack.of(tag.getCompound("PlannedReward"));
            if (!legacyReward.isEmpty()) plannedRewards.add(legacyReward);
        }
        rewardCount = Math.max(0, Math.min(3, tag.getInt("RewardCount")));
        if (rewardCount == 0) rewardCount = plannedRewards.size();
        if (!plannedRewards.isEmpty()) {
            RelicStackData.read(plannedRewards.get(0)).ifPresent(firstReward -> {
                rewardStars = firstReward.stars();
                rewardPartIndex = firstReward.part().ordinal();
            });
        }
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        sessionId = tag.hasUUID("Session") ? tag.getUUID("Session") : null;
        activeMobIds.clear();
        ListTag mobs = tag.getList("ActiveMobs", Tag.TAG_COMPOUND);
        for (int i = 0; i < mobs.size(); i++) {
            CompoundTag entry = mobs.getCompound(i);
            if (entry.hasUUID("Id")) activeMobIds.add(entry.getUUID("Id"));
        }
        participants.clear();
        ListTag participantTags = tag.getList("Participants", Tag.TAG_COMPOUND);
        for (int i = 0; i < participantTags.size(); i++) {
            CompoundTag entry = participantTags.getCompound(i);
            if (entry.hasUUID("Id")) participants.add(entry.getUUID("Id"));
        }
        if (ownerId != null) participants.add(ownerId);
        if (battleActive && (ownerId == null || sessionId == null || plannedRewards.isEmpty())) {
            battleActive = false;
            battleComplete = false;
            waveIndex = 0;
            aliveCount = 0;
            transitionTicks = 0;
            activeMobIds.clear();
            participants.clear();
            ownerId = null;
            sessionId = null;
            plannedRewards.clear();
            rewardCount = 0;
        }
        syncData();
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("SelectedSet", selectedSetIndex);
        tag.putInt("ChallengeRank", challengeRank);
        tag.putInt("Wave", waveIndex);
        tag.putInt("Alive", aliveCount);
        tag.putBoolean("BattleActive", battleActive);
        tag.putBoolean("BattleComplete", battleComplete);
        tag.putInt("RewardStars", rewardStars);
        tag.putInt("RewardPart", rewardPartIndex);
        tag.putInt("RewardCount", rewardCount);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        selectedSetIndex = RelicTrialSet.clampIndex(tag.getInt("SelectedSet"));
        challengeRank = Math.max(1, Math.min(7, tag.getInt("ChallengeRank")));
        waveIndex = Math.max(0, Math.min(3, tag.getInt("Wave")));
        aliveCount = Math.max(0, tag.getInt("Alive"));
        battleActive = tag.getBoolean("BattleActive");
        battleComplete = tag.getBoolean("BattleComplete");
        rewardStars = Math.max(0, Math.min(5, tag.getInt("RewardStars")));
        rewardPartIndex = tag.contains("RewardPart")
                ? Math.max(-1, Math.min(RelicStackData.Part.values().length - 1,
                tag.getInt("RewardPart"))) : -1;
        rewardCount = Math.max(0, Math.min(3, tag.getInt("RewardCount")));
        syncData();
    }
}
