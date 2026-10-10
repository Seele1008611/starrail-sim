package com.starrail.sim.client;

import com.starrail.sim.PathRuinAnchorBlock;
import com.starrail.sim.PathRuinSummonKeyItem;
import com.starrail.sim.RuinAnchorPacket;
import com.starrail.sim.StarRailRuinContent;
import com.starrail.sim.StarRailRuinGuardService;
import com.starrail.sim.StarRailSimMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** A small look-at panel, stacked below the battle panel when both are present. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, value = Dist.CLIENT)
public final class StarRailRuinAnchorHud {
    private static RuinAnchorPacket state;
    private static ClientLevel world;
    private static int age;

    private StarRailRuinAnchorHud() {}
    private static void clear() { state = null; world = null; age = 0; }

    public static void accept(RuinAnchorPacket packet) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null
                || !mc.level.dimension().location().equals(packet.dimension())) return;
        if (!packet.visible()) { clear(); return; }
        if (!packet.path().isRealPath()) return;
        state = packet; world = mc.level; age = 0;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var mc = Minecraft.getInstance();
        if (world != mc.level || mc.player == null || !mc.player.isAlive()) { clear(); return; }
        if (state != null && !mc.isPaused() && ++age > 30) clear();
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { clear(); }

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type() || state == null) return;
        var mc = Minecraft.getInstance();
        if (mc.level != world || mc.player == null || !mc.player.isAlive() || mc.player.isSpectator()
                || mc.options.hideGui || mc.screen != null
                || !(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK
                || !hit.getBlockPos().equals(state.pos()) || !mc.level.hasChunkAt(state.pos())
                || !(mc.level.getBlockState(state.pos()).getBlock() instanceof PathRuinAnchorBlock block)
                || block.path() != state.path()) return;
        int width = Math.min(300, event.getWindow().getGuiScaledWidth() - 20);
        if (width < 130 || event.getWindow().getGuiScaledHeight() < 240) return;
        int x = (event.getWindow().getGuiScaledWidth() - width) / 2;
        int y = StarRailRuinBattleHud.hasPanel() ? StarRailRuinBattleHud.panelBottom() + 2 : 10;
        var g = event.getGuiGraphics();
        int colour = StarRailRuinBattleHud.colour(state.path());
        g.fill(x, y, x + width, y + 35, 0xD90B1427);
        g.fill(x, y, x + width, y + 1, colour);
        StarRailUiStyle.fittedText(g, Component.translatable("ui.starrail_sim.ruin_anchor.title",
                StarRailRuinContent.pathName(state.path())), x + 8, y + 3, width - 16, 0xFFE7C77E);
        Component phase = state.phase() == StarRailRuinGuardService.AnchorPhase.COOLDOWN
                ? Component.translatable("ui.starrail_sim.ruin_anchor.cooldown",
                    Math.max(0, (state.remainingTicks() - age + 19) / 20))
                : Component.translatable("ui.starrail_sim.ruin_anchor."
                    + state.phase().name().toLowerCase(java.util.Locale.ROOT));
        StarRailUiStyle.fittedText(g, phase, x + 8, y + 14, width - 16, 0xFFFFFFFF);
        var held = mc.player.getMainHandItem();
        boolean matched = held.is(StarRailRuinContent.key(state.path()).get());
        Component key = matched
                ? Component.translatable(state.phase() == StarRailRuinGuardService.AnchorPhase.READY
                    ? "ui.starrail_sim.ruin_anchor.use_key" : "ui.starrail_sim.ruin_anchor.matched_key")
                : held.getItem() instanceof PathRuinSummonKeyItem
                    ? Component.translatable("ui.starrail_sim.ruin_anchor.wrong_key")
                    : Component.translatable("ui.starrail_sim.ruin_anchor.required_key",
                        StarRailRuinContent.key(state.path()).get().getDefaultInstance().getHoverName());
        StarRailUiStyle.fittedText(g, key, x + 8, y + 24, width - 16,
                matched ? 0xFF77D99B : StarRailUiStyle.MUTED_COLOR);
    }
}
