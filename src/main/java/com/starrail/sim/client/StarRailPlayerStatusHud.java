package com.starrail.sim.client;

import com.starrail.sim.PlayerStatusPacket;
import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.StarRailStatusConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Two transparent rows, exactly within the 182-unit main hotbar; no path or skill panel. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, value = Dist.CLIENT)
public final class StarRailPlayerStatusHud {
    private static ClientLevel world;
    private static LocalPlayer owner;
    private static float saturation;
    private static float xpLeft, xpRight, xpTop;
    private static boolean synced;
    private static float health, echo, lastHealth, lastAbsorption, lastFood;
    private static int echoHold, healTicks, shieldTicks, foodTicks;
    private static final DecimalFormat EXACT = new DecimalFormat("#,##0.#", DecimalFormatSymbols.getInstance(Locale.ROOT));

    public static void accept(PlayerStatusPacket packet) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !mc.player.getUUID().equals(packet.player())
                || !mc.level.dimension().location().equals(packet.dimension())
                || !Float.isFinite(packet.saturation())) return;
        ensureWorld(mc);
        saturation = Math.max(0, packet.saturation());
        synced = true;
    }
    private static void clear() {
        world = null; owner = null; synced = false; saturation = 0;
        health = echo = lastHealth = lastAbsorption = lastFood = 0;
        echoHold = healTicks = shieldTicks = foodTicks = 0;
    }
    private static void ensureWorld(Minecraft mc) {
        if (world == mc.level && owner == mc.player) return;
        clear(); world = mc.level; owner = mc.player;
        if (owner != null) {
            health = echo = ratio(owner.getHealth(), owner.getMaxHealth());
            lastHealth = owner.getHealth(); lastAbsorption = owner.getAbsorptionAmount();
            lastFood = owner.getFoodData().getFoodLevel();
            // A login/respawn packet may precede the client's new player entity. Ask only when ready.
            com.starrail.sim.StarRailNetwork.CHANNEL.sendToServer(
                    new com.starrail.sim.PlayerStatusRequestPacket(world.dimension().location()));
        }
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e) { clear(); }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) { clear(); return; }
        ensureWorld(mc);
        if (mc.isPaused()) return;
        float hp = owner.getHealth(), absorption = owner.getAbsorptionAmount();
        int food = owner.getFoodData().getFoodLevel();
        if (hp < lastHealth) { echo = Math.max(echo, health); echoHold = 10; }
        if (hp > lastHealth) healTicks = 8;
        if (absorption != lastAbsorption) shieldTicks = 8;
        if (food > lastFood) foodTicks = 8;
        health = Mth.lerp(.4F, health, ratio(hp, owner.getMaxHealth()));
        if (echoHold > 0) echoHold--; else echo = Mth.lerp(.2F, echo, health);
        if (healTicks > 0) healTicks--;
        if (shieldTicks > 0) shieldTicks--;
        if (foodTicks > 0) foodTicks--;
        lastHealth = hp; lastAbsorption = absorption; lastFood = food;
    }
    private static boolean visible() {
        var mc = Minecraft.getInstance();
        return StarRailStatusConfig.HUD.get() && mc.player != null && mc.gameMode != null
                && mc.getCameraEntity() == mc.player && mc.gameMode.canHurtPlayer()
                && !mc.player.isSpectator() && !mc.options.hideGui;
    }
    @SubscribeEvent public static void replace(RenderGuiOverlayEvent.Pre e) {
        if (!visible()) return;
        var overlay = e.getOverlay();
        if (overlay == VanillaGuiOverlay.PLAYER_HEALTH.type() || overlay == VanillaGuiOverlay.ARMOR_LEVEL.type()
                || overlay == VanillaGuiOverlay.FOOD_LEVEL.type() || overlay == VanillaGuiOverlay.AIR_LEVEL.type()
                || overlay == VanillaGuiOverlay.MOUNT_HEALTH.type() || overlay == VanillaGuiOverlay.JUMP_BAR.type())
            e.setCanceled(true);
    }
    @SubscribeEvent public static void render(RenderGuiOverlayEvent.Post e) {
        if (e.getOverlay() != VanillaGuiOverlay.EXPERIENCE_BAR.type() || !visible()) return;
        var mc = Minecraft.getInstance(); ensureWorld(mc);
        var p = mc.player; var g = e.getGuiGraphics();
        float scale = StarRailStatusConfig.SCALE.get().floatValue();
        int sw = e.getWindow().getGuiScaledWidth(), sh = e.getWindow().getGuiScaledHeight();
        // Two rows fit within 24 units; the central gap keeps ordinary XP levels readable.
        int x = StarRailStatusConfig.LEFT_CORNER.get() ? 8 : Math.round((sw - 182 * scale) / 2F);
        int y = sh - 53;
        if (mc.gui instanceof net.minecraftforge.client.gui.overlay.ForgeGui forgeGui) {
            // Reserve enough space for the vanilla selected-item name above the compact rows.
            forgeGui.leftHeight = Math.max(forgeGui.leftHeight, 68);
            forgeGui.rightHeight = Math.max(forgeGui.rightHeight, 68);
        }
        // Vanilla normally replaces experience with the wide jump bar. Keep experience in its
        // original slot; draw our HUD afterwards so it has priority over long level numbers.
        if (p.jumpableVehicle() != null && mc.gameMode.hasExperience()) {
            g.setColor(1, 1, 1, 1);
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
            mc.gui.renderExperienceBar(g, sw / 2 - 91);
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        }
        xpLeft = (sw / 2F - mc.font.width(Integer.toString(p.experienceLevel)) / 2F - x) / scale;
        xpRight = xpLeft + mc.font.width(Integer.toString(p.experienceLevel)) / scale;
        xpTop = (sh - 35F - y) / scale;
        g.pose().pushPose();
        g.pose().translate(x, y, 100);
        g.pose().scale(scale, scale, 1);
        int hpColour = p.getHealth() <= p.getMaxHealth() * .25F ? 0xFFEF8B7B : 0xFF85CEB1;
        if (p.hasEffect(net.minecraft.world.effect.MobEffects.POISON)) hpColour = 0xFFA4BC62;
        if (p.hasEffect(net.minecraft.world.effect.MobEffects.WITHER)) hpColour = 0xFFB0A1B9;
        int heartColour = p.hasEffect(net.minecraft.world.effect.MobEffects.POISON)
                || p.hasEffect(net.minecraft.world.effect.MobEffects.WITHER) ? hpColour : 0xFFF05A64;
        if (p.getHealth() <= p.getMaxHealth() * .25F && p.tickCount % 32 < 8) heartColour = 0xFFFFB8A1;
        icon(g, "heart", 0, 0, heartColour);
        text(g, number(p.getHealth()) + " / " + number(p.getMaxHealth()), 10, 0, 71, 0xFFE6EBED);
        bar(g, 0, 8, 81, echo, 0xFFCDB67C, 4);
        barFill(g, 0, 8, 81, health, healTicks > 0 ? 0xFFB1F2CE : hpColour, 4);
        float absorption = p.getAbsorptionAmount();
        if (absorption > 0) bar(g, 0, 13, 81, ratio(absorption, p.getMaxHealth()),
                shieldTicks > 0 ? 0xFFC0ECFA : 0xFF82C9E9, 2);
        int foodColour = p.getFoodData().getFoodLevel() <= 6 ? 0xFFE7AA74 : 0xFFD6BC79;
        icon(g, "food", 101, 0, 0xFFF0A14A);
        float sat = synced ? saturation : p.getFoodData().getSaturationLevel();
        text(g, p.getFoodData().getFoodLevel() + "/20", 111, 0, 34, 0xFFE6EBED);
        icon(g, "spark", 151, 0, 0xFFE2E96B);
        text(g, number(sat), 161, 0, 21, 0xFFCADF8E);
        bar(g, 101, 8, 81, p.getFoodData().getFoodLevel() / 20F,
                foodTicks > 0 ? 0xFFF3D895 : foodColour, 4);
        bar(g, 101, 13, 81, sat / 20F, 0xFFCADF8E, 2);
        icon(g, "armor", 101, 16, 0xFFE1E7EC);
        text(g, number(p.getAttributeValue(Attributes.ARMOR)), 111, 16, 30, 0xFFBDCFDA);
        icon(g, "tough", 151, 16, 0xFFCDD9EF);
        text(g, number(p.getAttributeValue(Attributes.ARMOR_TOUGHNESS)), 161, 16, 21, 0xFFBDCFDA);
        // Contextual values share the left lower row, never creating another row.
        boolean air = p.isUnderWater() || p.getAirSupply() < p.getMaxAirSupply();
        LivingEntity mount = p.getVehicle() instanceof LivingEntity living ? living : null;
        int count = (absorption > 0 ? 1 : 0) + (air ? 1 : 0) + (mount != null ? 1 : 0);
        int slot = count == 0 ? 81 : 81 / count, index = 0;
        if (absorption > 0) context(g, index++ * slot, slot, "shield", "+" + number(absorption), 0xFF82C9E9);
        if (air) context(g, index++ * slot, slot, "air", number(Math.max(0, p.getAirSupply()) / 20.0) + "s",
                p.getAirSupply() <= 60 ? 0xFFE7AA74 : 0xFF9CDCEA);
        if (mount != null) context(g, index * slot, slot, "mount",
                number(mount.getHealth()) + "/" + number(mount.getMaxHealth()),
                mount.getHealth() <= mount.getMaxHealth() * .25 ? 0xFFE7AA74 : 0xFFBDCFDA);
        if (p.jumpableVehicle() != null) {
            // Short charge meter sits in the central gap without covering the XP bar.
            bar(g, 82, 12, 18, p.getJumpRidingScale(), 0xFF98CBE4, 2);
        }
        g.pose().popPose();

    }
    private static void context(GuiGraphics g, int x, int width, String icon, String value, int color) {
        icon(g, icon, x, 16, color); text(g, value, x + 10, 16, width - 11, color);
    }
    private static String number(double v) {
        if (!Double.isFinite(v)) return "0";
        if (!StarRailStatusConfig.EXACT.get()) {
            if (Math.abs(v) >= 1_000_000) return EXACT.format(v / 1_000_000) + "M";
            if (Math.abs(v) >= 10_000) return EXACT.format(v / 1_000) + "K";
        }
        return EXACT.format(v);
    }
    private static void text(GuiGraphics g, String value, int x, int y, int width, int color) {
        var font = Minecraft.getInstance().font;
        // Vanilla glyphs have seven visible pixels, matching the seven-pixel status icons.
        // Shrink only when a long value would overflow its own state slot.
        float s = Math.min(1F, width / (float) Math.max(1, font.width(value)));
        maskExperience(g, x, y, Math.round(font.width(value) * s), 7);
        // The available width bounds fitting, not alignment: every value follows its symbol.
        g.pose().pushPose(); g.pose().translate(x, y, 0); g.pose().scale(s, s, 1);
        g.drawString(font, value, 0, 0, color, true); g.pose().popPose();
    }
    private static float ratio(float v, float max) { return max <= 0 ? 0 : Mth.clamp(v / max, 0, 1); }
    private static void maskExperience(GuiGraphics g, int x, int y, int width, int height) {
        if (Minecraft.getInstance().player.experienceLevel > 0
                && x < xpRight && x + width > xpLeft && y < xpTop + 9 && y + height > xpTop)
            g.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF101D2C);
    }
    private static void bar(GuiGraphics g, int x, int y, int w, float fraction, int color, int h) {
        // One-pixel dark frame gives contrast over sky, water and bright stone without a panel.
        g.fill(x, y - 1, x + w, y + h + 1, 0xD5101C28);
        g.fill(x + 1, y, x + w - 1, y + h, 0xEA30404D);
        barFill(g, x, y, w, fraction, color, h);
    }
    private static void barFill(GuiGraphics g, int x, int y, int w, float fraction, int color, int h) {
        int fill = Math.round((w - 2) * Mth.clamp(fraction, 0, 1));
        if (fill <= 0) return;
        int left = x + 1, end = left + fill;
        g.fill(left, y, end, y + h, color);
        g.fill(left, y, end, y + 1, tint(color, 1.18F));
        g.fill(left, y + h - 1, end, y + h, tint(color, .76F));
        if (fill >= 2) g.fill(end - 1, y, end, y + h, tint(color, 1.3F));
    }
    private static int tint(int color, float brightness) {
        int red = Math.min(255, Math.round(((color >> 16) & 255) * brightness));
        int green = Math.min(255, Math.round(((color >> 8) & 255) * brightness));
        int blue = Math.min(255, Math.round((color & 255) * brightness));
        return (color & 0xFF000000) | red << 16 | green << 8 | blue;
    }
    /** Solid pixel silhouettes, with shadows for visibility over bright world backgrounds. */
    private static void icon(GuiGraphics g, String type, int x, int y, int color) {
        String[] rows = switch (type) {
            case "heart" -> new String[]{"01101100", "12212210", "12222210", "11222110", "01121100", "00111000", "00010000"};
            case "armor" -> new String[]{"11000110", "12212210", "12222210", "01222100", "01222100", "01222100", "01111100"};
            case "shield" -> new String[]{"01111100", "12222210", "12333210", "12333210", "01232100", "00121000", "00010000"};
            case "tough" -> new String[]{"01111100", "12222210", "12313210", "12313210", "01212100", "00121000", "00010000"};
            case "food" -> new String[]{"00011100", "00122110", "00122210", "00011100", "00110000", "02200000", "02200000"};
            case "spark" -> new String[]{"00010000", "00121000", "01222100", "12222210", "01222100", "00121000", "00010000"};
            case "air" -> new String[]{"00111100", "01222110", "12211110", "12111110", "11111110", "01111100", "00111000"};
            default -> new String[]{"01111000", "12221000", "12311100", "01222110", "01111110", "01000100", "01000100"};
        };
        maskExperience(g, x, y, 8, 7);
        for (int r = 0; r < rows.length; r++) for (int c = 0; c < rows[r].length(); c++)
            if (rows[r].charAt(c) != '0') g.fill(x + c - 1, y + r - 1, x + c + 2, y + r + 2, 0xE5091220);
        for (int r = 0; r < rows.length; r++) for (int c = 0; c < rows[r].length(); c++) {
            char pixel = rows[r].charAt(c);
            if (pixel == '0') continue;
            int ink = pixel == '2' ? tint(color, 1.35F)
                    : pixel == '3' ? 0xFF56AEE5 : color;
            if (type.equals("food") && pixel == '2' && r >= 5) ink = 0xFFFFEBCD;
            g.fill(x + c, y + r, x + c + 1, y + r + 1, ink);
        }
    }
    private StarRailPlayerStatusHud() {}
}
