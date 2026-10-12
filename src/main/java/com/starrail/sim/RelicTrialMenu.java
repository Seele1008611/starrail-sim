package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Slotless menu that safely routes arena selector buttons through the server. */
public final class RelicTrialMenu extends AbstractContainerMenu {
    public static final int BUTTON_START = 5;
    public static final int BUTTON_ABORT = 6;
    private final BlockPos devicePos;
    private final ContainerData data;

    /** Client constructor. The block position is sent by NetworkHooks.openScreen. */
    public RelicTrialMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, buffer.readBlockPos(), new SimpleContainerData(8));
    }

    private RelicTrialMenu(int containerId, Inventory inventory, BlockPos pos,
                           ContainerData data) {
        super(StarRailSimMod.RELIC_TRIAL_MENU.get(), containerId);
        this.devicePos = pos;
        this.data = data;
        this.addDataSlots(data);
    }

    public RelicTrialMenu(int containerId, Inventory inventory, BlockPos pos,
                          RelicTrialDeviceBlockEntity device) {
        this(containerId, inventory, pos, device.containerData());
    }

    public int selectedSetIndex() { return data.get(0); }
    public int challengeRank() { return data.get(1); }
    public int waveIndex() { return data.get(2); }
    public int aliveCount() { return data.get(3); }
    public boolean battleActive() { return data.get(4) != 0; }
    public boolean battleComplete() { return data.get(5) != 0; }
    public int rewardStars() { return data.get(6); }
    public int rewardPartIndex() { return data.get(7); }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(player.level() instanceof ServerLevel level)
                || !stillValid(player)
                || !(level.getBlockEntity(devicePos) instanceof RelicTrialDeviceBlockEntity device)) {
            return false;
        }
        if (buttonId >= 0 && buttonId < RelicTrialSet.values().length) {
            if (!device.isBattleActive()) device.setSelectedSetIndex(buttonId);
            return true;
        }
        if (buttonId == BUTTON_START) {
            return RelicTrialBattleService.start(level, device, serverPlayer);
        }
        if (buttonId == BUTTON_ABORT && device.isBattleActive()) {
            RelicTrialBattleService.abort(level, device, net.minecraft.network.chat.Component.translatable(
                    "message.starrail_sim.arena.cancelled"));
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        BlockEntity entity = player.level().getBlockEntity(devicePos);
        return entity instanceof RelicTrialDeviceBlockEntity
                && player.distanceToSqr(devicePos.getX() + 0.5D,
                devicePos.getY() + 0.5D, devicePos.getZ() + 0.5D) <= 64.0D;
    }
}
