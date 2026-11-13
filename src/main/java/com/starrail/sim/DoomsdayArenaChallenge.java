package com.starrail.sim;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID)
public final class DoomsdayArenaChallenge {
    private DoomsdayArenaChallenge() {}

    @SubscribeEvent
    public static void activate(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !(event.getLevel() instanceof ServerLevel level)
                || !event.getItemStack().isEmpty() || event.getEntity().isSpectator()) return;
        var state = level.getBlockState(event.getPos());
        if (!state.is(StarRailSimMod.DOOMSDAY_SUMMONING_CONSOLE.get()) && !state.is(Blocks.LODESTONE)) return;
        var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .get(DoomsdayArenaStructure.KEY);
        if (structure == null) return;
        var start = level.structureManager().getStructureAt(event.getPos(), structure);
        if (!start.isValid()) return;
        for (var piece : start.getPieces()) {
            if (!(piece instanceof DoomsdayArenaPiece arena) || !arena.beacon().equals(event.getPos())) continue;
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            if (level.getDifficulty() == Difficulty.PEACEFUL) {
                event.getEntity().displayClientMessage(Component.translatable("message.starrail_sim.doomsday_arena.peaceful"), true);
                return;
            }
            var pos = arena.spawn();
            var area = new AABB(pos).inflate(8, 5, 8);
            if (!level.getEntitiesOfClass(DoomsdayBeast.class, area, boss -> !boss.isRemoved()).isEmpty()) {
                event.getEntity().displayClientMessage(Component.translatable("message.starrail_sim.doomsday_arena.active"), true);
                return;
            }
            var boss = StarveinConstructEntities.DOOMSDAY_BEAST.get().create(level);
            if (boss != null) {
                boss.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 180, 0);
                if (level.addFreshEntity(boss)) event.getEntity().displayClientMessage(
                        Component.translatable("message.starrail_sim.doomsday_arena.started"), true);
            }
            return;
        }
    }
}
