package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import javax.annotation.Nullable;

/** Central selector and wave-start block for the relic-duel arena. */
public final class RelicTrialDeviceBlock extends Block implements EntityBlock {
    public RelicTrialDeviceBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RelicTrialDeviceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != StarRailSimMod.RELIC_TRIAL_DEVICE_ENTITY.get()) return null;
        return (tickLevel, pos, tickState, blockEntity) ->
                RelicTrialDeviceBlockEntity.serverTick(tickLevel, pos, tickState, blockEntity);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(level.getBlockEntity(pos) instanceof RelicTrialDeviceBlockEntity device)) {
            return InteractionResult.PASS;
        }
        StarRailExplorationService.discoverArena(serverPlayer, pos);
        MutableComponent title = Component.translatable("block.starrail_sim.relic_trial_device");
        NetworkHooks.openScreen(serverPlayer,
                new SimpleMenuProvider((id, inventory, ignored) ->
                        new RelicTrialMenu(id, inventory, pos, device), title),
                buffer -> buffer.writeBlockPos(pos));
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
                         boolean moving) {
        if (state.getBlock() != newState.getBlock() && level instanceof ServerLevel serverLevel
                && level.getBlockEntity(pos) instanceof RelicTrialDeviceBlockEntity device
                && device.isBattleActive()) {
            RelicTrialBattleService.abort(serverLevel, device,
                    Component.translatable("message.starrail_sim.arena.device_removed"));
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
