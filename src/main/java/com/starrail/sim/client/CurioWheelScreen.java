package com.starrail.sim.client;

import com.starrail.sim.CurioUsePacket;
import com.starrail.sim.PrototypeCurios;
import com.starrail.sim.StarRailNetwork;
import com.starrail.sim.StarRailSimMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, value = Dist.CLIENT)
public final class CurioWheelScreen extends Screen {
    private static final int[] X = {0, -56, 56};
    private static final int[] Y = {-59, 27, 27};
    private static final int HALF_SLOT = 17;

    private CurioWheelScreen() {
        super(Component.translatable("screen.starrail_sim.curio_wheel"));
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getInstance();
        while (StarRailClient.USE_CURIO.consumeClick()) {
            if (client.screen == null && client.player != null && client.player.isAlive()
                    && !client.player.isSpectator() && StarRailClient.USE_CURIO.isDown()) {
                client.setScreen(new CurioWheelScreen());
            }
        }
    }

    @Override
    protected void init() {
        minecraft.mouseHandler.setIgnoreFirstMove();
        org.lwjgl.glfw.GLFW.glfwSetCursorPos(minecraft.getWindow().getWindow(),
                minecraft.getWindow().getScreenWidth() / 2.0, minecraft.getWindow().getScreenHeight() / 2.0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x5508101a);
        int hovered = hovered(mouseX, mouseY);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 96, 0xfff1db);
        for (int slot = 0; slot < X.length; slot++) {
            int x = width / 2 + X[slot], y = height / 2 + Y[slot];
            ItemStack stack = PrototypeCurios.equipped(minecraft.player, slot);
            int frame = hovered == slot ? 0xffeac67a : 0xff52647a;
            graphics.fill(x - HALF_SLOT - 1, y - HALF_SLOT - 1, x + HALF_SLOT + 1, y + HALF_SLOT + 1, frame);
            graphics.fill(x - HALF_SLOT, y - HALF_SLOT, x + HALF_SLOT, y + HALF_SLOT, 0xff192737);
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, x - 8, y - 8);
                float cooldown = minecraft.player.getCooldowns().getCooldownPercent(stack.getItem(), partialTick);
                if (cooldown > 0) graphics.fill(x - 8, y + 8 - Math.round(cooldown * 16), x + 8, y + 8, 0xa0000000);
            }
            int remaining = com.starrail.sim.CurioEffects.remaining(stack);
            String label = Integer.toString(slot + 1);
            if (remaining >= 0) label += " · " + remaining;
            graphics.drawCenteredString(font, label, x, y + 21, 0xdde9f3);
        }
        ItemStack selected = PrototypeCurios.equipped(minecraft.player, hovered);
        Component hint = selected.isEmpty() ? Component.translatable("screen.starrail_sim.curio_wheel.hint")
                : selected.getHoverName().copy().append(" · ").append(Component.translatable(
                        !PrototypeCurios.isActive(selected) ? "screen.starrail_sim.curio_wheel.passive"
                        : minecraft.player.getCooldowns().isOnCooldown(selected.getItem())
                                ? "screen.starrail_sim.curio_wheel.cooldown" : "screen.starrail_sim.curio_wheel.ready"));
        graphics.drawCenteredString(font, hint, width / 2, height / 2 + 61, 0xffffff);
    }

    @Override
    public boolean keyReleased(int key, int scan, int modifiers) {
        if (!StarRailClient.USE_CURIO.matches(key, scan)) return super.keyReleased(key, scan, modifiers);
        release();
        return true;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (!StarRailClient.USE_CURIO.matchesMouse(button)) return super.mouseReleased(x, y, button);
        release();
        return true;
    }

    private void release() {
        var window = minecraft.getWindow();
        int slot = hovered(minecraft.mouseHandler.xpos() * width / window.getScreenWidth(),
                minecraft.mouseHandler.ypos() * height / window.getScreenHeight());
        ItemStack stack = PrototypeCurios.equipped(minecraft.player, slot);
        if (PrototypeCurios.isActive(stack) && !minecraft.player.getCooldowns().isOnCooldown(stack.getItem())) {
            StarRailNetwork.CHANNEL.sendToServer(new CurioUsePacket(slot, Item.getId(stack.getItem())));
        } else if (slot < 0) {
            StarRailNetwork.CHANNEL.sendToServer(new CurioUsePacket(255, 0));
        }
        onClose();
    }

    private int hovered(double x, double y) {
        for (int slot = 0; slot < X.length; slot++) {
            if (Math.abs(x - width / 2 - X[slot]) <= HALF_SLOT
                    && Math.abs(y - height / 2 - Y[slot]) <= HALF_SLOT) return slot;
        }
        return -1;
    }
}
