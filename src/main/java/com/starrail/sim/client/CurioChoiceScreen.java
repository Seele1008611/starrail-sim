package com.starrail.sim.client;

import com.starrail.sim.CurioChoicePacket;
import com.starrail.sim.CurioClaimPacket;
import com.starrail.sim.StarRailNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class CurioChoiceScreen extends Screen {
    private final CurioChoicePacket packet;

    private CurioChoiceScreen(CurioChoicePacket packet) {
        super(Component.translatable("screen.starrail_sim.curio_choice"));
        this.packet = packet;
    }

    public static void open(CurioChoicePacket packet) {
        var client = Minecraft.getInstance();
        if (client.screen == null) client.setScreen(new CurioChoiceScreen(packet));
    }

    @Override
    protected void init() {
        for (int i = 0; i < 2; i++) {
            final int index = i;
            var item = i == 0 ? packet.first() : packet.second();
            addRenderableWidget(Button.builder(item.getHoverName(), b -> {
                StarRailNetwork.CHANNEL.sendToServer(new CurioClaimPacket(packet.id(), index));
                onClose();
            }).bounds(width / 2 - 76, height / 2 - 15 + i * 29, 152, 24).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float tick) {
        graphics.fill(0,0,width,height,0xa0101724);
        graphics.drawCenteredString(font,title,width / 2,height / 2 - 40,0xffe7ad);
        super.render(graphics,x,y,tick);
        for (int i = 0; i < 2; i++) {
            var item = i == 0 ? packet.first() : packet.second();
            int left = width / 2 - 99, top = height / 2 - 11 + i * 29;
            graphics.renderItem(item,left,top);
            if (x >= left && x < left + 16 && y >= top && y < top + 16) {
                graphics.renderTooltip(font,item,x,y);
            }
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
