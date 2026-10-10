package com.starrail.sim.client;

import com.starrail.sim.RuinBattlePacket;
import com.starrail.sim.StarRailPath;
import com.starrail.sim.StarRailRuinGuardService;
import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.StarRailToughnessService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;

/** Compact battle panel using the existing navy, gold and path-colour visual language. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, value = Dist.CLIENT)
public final class StarRailRuinBattleHud {
    private static final float PANEL_SCALE = .85F;
    private static RuinBattlePacket state;
    private static ClientLevel world;
    private static int age;
    private static float displayedHealth;
    private static float displayedToughness;

    private StarRailRuinBattleHud() {}

    private static void clear() {
        state = null;
        world = null;
        age = 0;
    }

    public static void accept(RuinBattlePacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null
                || !mc.level.dimension().location().equals(packet.dimension())) return;
        if (packet.entityId() < 0) { clear(); return; }
        if (!Float.isFinite(packet.health()) || !Float.isFinite(packet.maxHealth())
                || !Float.isFinite(packet.toughness()) || !Float.isFinite(packet.maxToughness())
                || packet.maxHealth() <= 0 || packet.maxToughness() <= 0) return;
        if (state == null || world != mc.level || !state.entityUuid().equals(packet.entityUuid())) {
            displayedHealth = ratio(packet.health(), packet.maxHealth());
            displayedToughness = ratio(packet.toughness(), packet.maxToughness());
        }
        state = packet;
        world = mc.level;
        age = 0;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || world != mc.level || !mc.player.isAlive()) {
            clear(); return;
        }
        if (state == null || mc.isPaused()) return;
        if (++age > 30) { clear(); return; }
        displayedHealth = Mth.lerp(.4F, displayedHealth, ratio(state.health(), state.maxHealth()));
        displayedToughness = Mth.lerp(.4F, displayedToughness, ratio(state.toughness(), state.maxToughness()));
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { clear(); }

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type() || state == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != world || mc.player == null || !mc.player.isAlive()
                || mc.options.hideGui || mc.screen != null || mc.player.isSpectator()) return;
        var entity = mc.level.getEntity(state.entityId());
        if (!(entity instanceof LivingEntity living) || !entity.getUUID().equals(state.entityUuid())
                || !living.isAlive() || living.isRemoved()
                || mc.player.distanceToSqr(living) > StarRailRuinGuardService.BATTLE_VIEW_RANGE
                    * StarRailRuinGuardService.BATTLE_VIEW_RANGE) return;
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int width = Math.min(300, (int) ((screenWidth - 20) / PANEL_SCALE));
        if (width < 130 || event.getWindow().getGuiScaledHeight() < 120) return;
        int x = 0;
        int y = 0;
        GuiGraphics g = event.getGuiGraphics();
        g.pose().pushPose();
        g.pose().translate((screenWidth - width * PANEL_SCALE) / 2F, 10, 0);
        g.pose().scale(PANEL_SCALE, PANEL_SCALE, 1);
        int accent = colour(state.path());
        g.fill(x, y, x + width, y + 66, 0xD90B1427);
        g.fill(x, y, x + width, y + 1, accent);
        g.fill(x, y + 65, x + width, y + 66, 0x80E7C77E);
        Component name = state.path().isRealPath()
                ? Component.translatable("ui.starrail_sim.ruin_battle.path_guard",
                    Component.translatable("path.starrail_sim." + state.path().getId()), state.name())
                : state.name();
        StarRailUiStyle.fittedText(g, name, x + 8, y + 6, width - 16, 0xFFE7C77E);
        row(g, x + 8, y + 21, width - 16, Component.translatable("ui.starrail_sim.ruin_battle.health"),
                state.health(), state.maxHealth(), displayedHealth, 0xFFE7C77E);
        boolean broken = state.phase() == StarRailToughnessService.Phase.BROKEN;
        row(g, x + 8, y + 37, width - 16, Component.translatable("ui.starrail_sim.ruin_battle.toughness"),
                state.toughness(), state.maxToughness(), displayedToughness,
                broken ? 0xFFEB977E : accent);
        Component phase = switch (state.phase()) {
            case NORMAL -> Component.translatable("ui.starrail_sim.ruin_battle.normal");
            case BROKEN -> Component.translatable("ui.starrail_sim.ruin_battle.broken", seconds());
            case RECOVERING -> Component.translatable("ui.starrail_sim.ruin_battle.recovering", seconds());
        };
        StarRailUiStyle.fittedText(g, phase, x + 8, y + 53, width - 16,
                broken ? 0xFFEB977E : StarRailUiStyle.MUTED_COLOR);
        g.pose().popPose();
    }

    private static int seconds() { return Math.max(0, (state.remainingTicks() - age + 19) / 20); }

    private static float ratio(float value, float max) { return Mth.clamp(value / max, 0, 1); }

    private static void row(GuiGraphics g, int x, int y, int width, Component label,
                            float value, float max, float fraction, int colour) {
        var font = Minecraft.getInstance().font;
        String text = String.format(Locale.ROOT, "%.1f / %.1f", Math.max(0, value), max);
        g.drawString(font, label, x, y, 0xFFD9E3EB, false);
        g.drawString(font, text, x + width - font.width(text), y, 0xFFFFFFFF, false);
        g.fill(x, y + 10, x + width, y + 13, 0xFF29354A);
        int fill = Math.round(width * fraction);
        if (fill > 0) g.fill(x, y + 10, x + fill, y + 13, colour);
    }

    static boolean hasPanel() { return state != null; }

    static int panelBottom() { return 10 + (int) Math.ceil(66 * PANEL_SCALE); }

    static int colour(StarRailPath path) {
        return 0xFF000000 | switch (path) {
            case HUNT -> 0x67CEF2;
            case PRESERVATION -> 0xEDB64E;
            case DESTRUCTION -> 0xFA795D;
            case ERUDITION -> 0xBD87F5;
            case NIHILITY -> 0x956AE1;
            case HARMONY -> 0xEE97CA;
            case ABUNDANCE -> 0x77D99B;
            case REMEMBRANCE -> 0x8ADDF0;
            case ELATION -> 0xFF82B7;
            default -> 0xB9D4E6;
        };
    }
}
