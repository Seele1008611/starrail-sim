package com.starrail.sim.client;

import com.starrail.sim.StarRailAttributes;
import com.starrail.sim.AttributeSourcesPacket;
import com.starrail.sim.AttributeSourcesRequestPacket;
import com.starrail.sim.StarRailNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import java.util.Locale;

/** Read-only grouped attributes in the same light modal used by the HTML reference. */
public final class StarRailAttributeScreen extends StarRailModalScreen {
    private static final String[] LABELS = {
        "current_max_health", "attack_damage", "armor", "armor_toughness", "movement_speed", "attack_speed",
        "crit_rate", "crit_damage", "break_effect", "effect_hit_rate", "effect_resistance", "knockback_resistance", "healing_effect"
    };
    private final int[] rowPositions = new int[LABELS.length];
    private final double[] values = new double[LABELS.length];
    private final double[] bases = new double[LABELS.length];
    private final StarRailSmoothScroll scroll = new StarRailSmoothScroll();
    private int selected = -1;
    private int contentHeight;
    private AttributeSourcesPacket snapshot;
    private int refreshTicks;
    private Button back;
    private static final String[] SOURCES={"path","traces","light_cone","equipment","relics","effects","temporary","other"};

    @Override protected void init() {
        super.init();snapshot=null;refreshTicks=0;
        back=addRenderableWidget(StarRailUiStyle.outlinedButton(Component.translatable("ui.starrail_sim.attributes.back"),
                ignored->{selected=-1;scroll.reset();},modalLeft+17,modalBottom-36,120,22));
        request();
    }
    private void request() {
        if(minecraft.player!=null && minecraft.getConnection()!=null)
            StarRailNetwork.CHANNEL.sendToServer(new AttributeSourcesRequestPacket());
    }
    @Override public void tick() {super.tick();if(++refreshTicks>=20){refreshTicks=0;request();}}
    public static void accept(AttributeSourcesPacket packet) {
        Minecraft mc=Minecraft.getInstance();
        if(mc.player!=null && packet.owner().equals(mc.player.getUUID()) && mc.screen instanceof StarRailAttributeScreen screen)
            screen.snapshot=packet;
    }


    public StarRailAttributeScreen(StarRailStyledScreen parent) {
        super(Component.translatable("screen.starrail_sim.attributes"), parent);
    }

    private boolean percent(int index) { return index >= 6 && index != 11; }
    private String number(int index, double value) {
        return String.format(Locale.ROOT, percent(index) ? "%.1f%%" : index == 4 || index == 5 || index == 11
                ? "%.3f" : "%.1f", percent(index) ? value * 100 : value);
    }

    private void refresh(Player player) {
        Attribute[] attributes = {
            Attributes.MAX_HEALTH, Attributes.ATTACK_DAMAGE, Attributes.ARMOR, Attributes.ARMOR_TOUGHNESS,
            Attributes.MOVEMENT_SPEED, Attributes.ATTACK_SPEED,
            StarRailAttributes.CRIT_RATE.get(), StarRailAttributes.CRIT_DAMAGE.get(), StarRailAttributes.BREAK_EFFECT.get(),
            StarRailAttributes.EFFECT_HIT_RATE.get(), StarRailAttributes.EFFECT_RESISTANCE.get(),
            Attributes.KNOCKBACK_RESISTANCE, StarRailAttributes.HEALING_EFFECT.get()
        };
        for (int i = 0; i < attributes.length; i++) {
            var instance = player.getAttribute(attributes[i]);
            values[i] = instance == null ? 0 : instance.getValue();
            bases[i] = instance == null ? 0 : instance.getBaseValue();
        }
        values[1] = StarRailAttributes.getAttackDamage(player);
        values[6] = StarRailAttributes.getCritRate(player);
        values[7] = StarRailAttributes.getCritDamage(player);
    }

    @Override
    protected void renderModal(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Player player = minecraft.player;
        if (player != null) refresh(player);
        if(snapshot!=null)for(int i=0;i<LABELS.length;i++) {
            values[i]=snapshot.rows().get(i).total();bases[i]=snapshot.rows().get(i).base();
        }
        back.visible=selected>=0;back.active=selected>=0;
        if(selected>=0){renderSources(graphics,mouseX,mouseY);return;}
        int left = modalLeft + 17, right = modalRight - 17;
        int bottom = modalBottom - 68;
        scroll.advance();
        clip(graphics, left, bodyTop, right, bottom);
        graphics.pose().pushPose();
        graphics.pose().translate(0, -scroll.position(), 0);
        int y = bodyTop + 7;
        for (int i = 0; i < LABELS.length; i++) {
            if (i == 0 || i == 6) {
                graphics.drawString(font, Component.translatable(i == 0
                        ? "ui.starrail_sim.attributes.basic" : "ui.starrail_sim.attributes.advanced"),
                        left + 2, y, SECONDARY_INK, false);
                y += 23;
            }
            rowPositions[i] = y;
            boolean hovered = mouseX >= left && mouseX < right && mouseY >= bodyTop && mouseY < bottom
                    && mouseY >= y - scroll.position() && mouseY < y - scroll.position() + 22;
            graphics.fill(left, y, right, y + 22, selected == i || hovered ? 0x22627799
                    : i % 2 == 0 ? 0x10707785 : 0x00707785);
            int icon = i < 5 ? i : i == 6 ? 5 : 6;
            StarRailUiStyle.statIcon(graphics, icon, left + 10, y + 11, SECONDARY_INK);
            graphics.drawString(font, Component.translatable("ui.starrail_sim." + LABELS[i]),
                    left + 24, y + 7, INK, false);
            // Every row separates the base value from the total bonus, including advanced attributes.
            String primary = number(i, bases[i]);
            graphics.drawString(font, primary, right - 114 - font.width(primary), y + 7, INK, false);
            double bonus = values[i] - bases[i];
            if (Math.abs(bonus) > .00001) {
                String extra = (bonus > 0 ? "+" : "") + number(i, bonus);
                graphics.drawString(font, extra, right - 27 - font.width(extra), y + 7,
                        bonus >= 0 ? 0xFF168EB9 : 0xFFA14A60, false);
            }
            StarRailCosmicUi.ellipse(graphics, right - 9, y + 11, 4, 4, 0xFF363945);
            graphics.drawString(font, "?", right - 11, y + 7, 0xFFF4F4F7, false);
            y += 25;
        }
        graphics.pose().popPose();
        graphics.disableScissor();
        contentHeight = y - bodyTop + 8;
        scroll.bounds(contentHeight - (bottom - bodyTop));
        renderScrollBar(graphics, scroll, modalRight - 10, bodyTop, bottom, contentHeight, mouseX, mouseY);
        graphics.fill(left, bottom + 9, right, bottom + 10, 0x30707785);
        Component help = Component.translatable("ui.starrail_sim.attributes.help");
        int helpY = bottom + 18;
        for (FormattedCharSequence line : font.split(help, right - left)) {
            graphics.drawString(font, line, left, helpY, SECONDARY_INK, false);
            helpY += 12;
        }
    }

    private void renderSources(GuiGraphics g,int mx,int my) {
        int left=modalLeft+17,right=modalRight-17,bottom=modalBottom-50;
        scroll.advance();clip(g,left,bodyTop,right,bottom);
        int y=bodyTop+7-(int)scroll.position();
        g.drawString(font,Component.translatable("ui.starrail_sim."+LABELS[selected]),left,y,INK,false);y+=21;
        if(snapshot==null) {
            g.drawString(font,Component.translatable("ui.starrail_sim.attributes.loading"),left,y,SECONDARY_INK,false);
            g.disableScissor();return;
        }
        var row=snapshot.rows().get(selected);
        y=sourceLine(g,left,right,y,Component.translatable("ui.starrail_sim.attributes.source.base"),number(selected,row.base()));
        for(int i=0;i<SOURCES.length;i++) {
            var group=row.groups().get(i);StringBuilder value=new StringBuilder();
            if(Math.abs(group.addition())>.000001)value.append(signed(number(selected,group.addition()),group.addition()));
            if(Math.abs(group.basePercent())>.000001)append(value,
                    Component.translatable("ui.starrail_sim.attributes.base_percent",signed(String.format(Locale.ROOT,"%.1f%%",group.basePercent()*100),group.basePercent())).getString());
            if(Math.abs(group.totalFactor()-1)>.000001)append(value,
                    Component.translatable("ui.starrail_sim.attributes.total_factor",String.format(Locale.ROOT,"%.4f",group.totalFactor())).getString());
            if(value.isEmpty())value.append("—");
            y=sourceLine(g,left,right,y,Component.translatable("ui.starrail_sim.attributes.source."+SOURCES[i]),value.toString());
        }
        y+=7;
        y=sourceLine(g,left,right,y,Component.translatable("ui.starrail_sim.attributes.final"),number(selected,row.total()));
        if(Math.abs(row.raw()-row.total())>.00001)
            y=sourceLine(g,left,right,y,Component.translatable("ui.starrail_sim.attributes.limit_adjustment"),
                    signed(number(selected,row.total()-row.raw()),row.total()-row.raw()));
        y+=8;
        for(String key:new String[]{"formula","source_note"}) {
            for(var line:font.split(Component.translatable("ui.starrail_sim.attributes."+key),right-left)) {
                g.drawString(font,line,left,y,SECONDARY_INK,false);y+=12;
            }y+=8;
        }
        g.disableScissor();contentHeight=y+(int)scroll.position()-bodyTop+8;
        scroll.bounds(contentHeight-(bottom-bodyTop));
        renderScrollBar(g,scroll,modalRight-10,bodyTop,bottom,contentHeight,mx,my);
    }
    private int sourceLine(GuiGraphics g,int left,int right,int y,Component label,String value) {
        g.fill(left,y,right,y+21,0x10707785);
        g.drawString(font,label,left+4,y+6,INK,false);
        var lines=font.split(Component.literal(value),right-left-155);
        int lineY=y+6;
        for(var line:lines){g.drawString(font,line,right-4-font.width(line),lineY,SECONDARY_INK,false);lineY+=12;}
        return y+Math.max(24,lines.size()*12+12);
    }
    private static String signed(String value,double number){return number>0?"+"+value:value;}
    private static void append(StringBuilder value,String part){if(!value.isEmpty())value.append(" · ");value.append(part);}

    @Override
    protected boolean logicalMouseClicked(double x, double y, int button) {
        if (selected<0 && button == 0 && x >= modalLeft + 17 && x < modalRight - 17
                && y >= bodyTop && y < modalBottom - 68) {
            for (int i = 0; i < rowPositions.length; i++) {
                if (y >= rowPositions[i] - scroll.position() && y < rowPositions[i] - scroll.position() + 22) {
                    selected = i;scroll.reset();
                    return true;
                }
            }
        }
        return super.logicalMouseClicked(x, y, button);
    }

    @Override
    protected boolean logicalMouseScrolled(double x, double y, double amount) {
        if (x >= modalLeft && x <= modalRight && y >= bodyTop && y < modalBottom - (selected>=0?50:68)) {
            scroll.wheel(amount, 25);
        }
        return true;
    }
}
