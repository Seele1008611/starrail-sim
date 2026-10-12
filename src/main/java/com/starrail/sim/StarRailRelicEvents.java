package com.starrail.sim;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

/** Keeps relic attribute modifiers synchronized with the server-side Curios loadout. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarRailRelicEvents {
    private StarRailRelicEvents() {}

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        boolean relicSlot = false;
        for (RelicStackData.Part part : RelicStackData.Part.values()) {
            if (part.curiosSlot().equals(event.getIdentifier())) {
                relicSlot = true;
                break;
            }
        }
        if (relicSlot) StarRailRelicService.refresh(player);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        refresh(event);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        refresh(event);
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        refresh(event);
    }

    @SubscribeEvent
    public static void onHostileDeath(LivingDeathEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide()
                || event.getEntity().getType().getCategory() != MobCategory.MONSTER) return;
        ServerPlayer player = StarRailCombatEvents.getPlayerAttacker(event.getSource());
        if (player == null || StarRailOwnedDamage.isSecondary(player)) return;
        StarRailRelicService.onHostileKill(player);
    }

    private static void refresh(PlayerEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) StarRailRelicService.refresh(player);
    }
}
