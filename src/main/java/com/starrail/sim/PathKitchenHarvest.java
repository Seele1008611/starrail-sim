package com.starrail.sim;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID)
public final class PathKitchenHarvest {
    private static final ResourceLocation GRAINVALE = new ResourceLocation(StarRailSimMod.MOD_ID, "goldwind_grainvale");

    private PathKitchenHarvest() {}

    @SubscribeEvent
    public static void onHoney(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        var pos = event.getPos();
        if (!level.dimension().equals(StarRailDimensionIds.THE_PATH) || !level.getBiome(pos).is(GRAINVALE)
                || !event.getItemStack().is(PathKitchen.FLASK.get())) return;
        var state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BeehiveBlock hive)
                || state.getValue(BeehiveBlock.HONEY_LEVEL) < BeehiveBlock.MAX_HONEY_LEVELS) return;
        if (!level.isClientSide) {
            var player = event.getEntity();
            player.setItemInHand(event.getHand(), ItemUtils.createFilledResult(
                    event.getItemStack(), player, new ItemStack(PathKitchen.GRAIN_HONEY.get())));
            if (CampfireBlock.isSmokeyPos(level, pos)) hive.resetHoneyLevel(level, state, pos);
            else hive.releaseBeesAndResetHoneyLevel(level, state, pos, player, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
        }
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        event.setCanceled(true);
    }
}
