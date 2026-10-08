package com.starrail.sim.client;

import com.starrail.sim.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Responsive native trace screen, using the selected path's HTML prototype geometry. */
public final class StarRailTraceScreen extends StarRailStyledScreen {
    private final StarRailPath path;
    private final StarRailTraceLayouts.Layout layout;
    private final StarRailSmoothScroll scroll = new StarRailSmoothScroll();
    private int selected, left, top, right, bottom, detailLeft;
    private int treeLeft, treeTop, treeWidth, treeHeight;
    private double scale, originX, originY, selectedAt, changedAt;
    private int previousMask = -1, changedMask;
    private boolean confirmation, resetAll, pending;
    private Button unlock, rollback, reset;

    public StarRailTraceScreen() { this(activePath()); }
    private StarRailTraceScreen(StarRailPath path) {
        super(Component.literal("行迹 · " + path.getDisplayName()));
        this.path = path;
        this.layout = StarRailTraceLayouts.get(path.isRealPath() ? path : StarRailPath.HUNT);
    }
    private static StarRailPath activePath() {
        StarRailPath current = StarRailPathClientState.getCurrentPath();
        return current;
    }
    @Override protected void init() {
        super.init();
        left = StarRailUiStyle.panelLeft(width); top = StarRailUiStyle.panelTop(height);
        right = left + StarRailUiStyle.panelWidth(width); bottom = top + StarRailUiStyle.panelHeight(height);
        detailLeft = right - 216;
        treeLeft = left + 164; treeTop = top + 60;
        treeWidth = detailLeft - treeLeft - 10; treeHeight = bottom - treeTop - 24;
        scale = Math.min(treeWidth / (double)layout.canvasWidth(), treeHeight / (double)layout.canvasHeight());
        originX = treeLeft + (treeWidth - layout.canvasWidth() * scale) / 2;
        originY = treeTop + (treeHeight - layout.canvasHeight() * scale) / 2;
        for (Button button : StarRailUiStyle.createCharacterNavigation(left, top, right-left, bottom-top, 2, index -> {
            switch (index) {
                case 0 -> minecraft.setScreen(new StarRailCharacterScreen());
                case 1 -> minecraft.setScreen(new StarRailLightConeScreen());
                case 4 -> minecraft.setScreen(new StarRailPathScreen());
                case 5 -> minecraft.setScreen(new StarRailGuideScreen());
            }
        })) addRenderableWidget(button);
        unlock = addRenderableWidget(StarRailUiStyle.outlinedButton(Component.literal("解锁行迹"), b -> send(TraceActionPacket.Action.UNLOCK, selected), detailLeft+10, bottom-62, 188, 22));
        rollback = addRenderableWidget(StarRailUiStyle.outlinedButton(Component.literal("回退节点"), b -> { confirmation = true; resetAll = false; }, detailLeft+10, bottom-34, 90, 22));
        reset = addRenderableWidget(StarRailUiStyle.outlinedButton(Component.literal("重置行迹"), b -> { confirmation = true; resetAll = true; }, detailLeft+108, bottom-34, 90, 22));
        send(TraceActionPacket.Action.REQUEST, -1);
    }
    private void send(TraceActionPacket.Action action, int node) {
        if (action != TraceActionPacket.Action.REQUEST) pending = true;
        StarRailNetwork.CHANNEL.sendToServer(new TraceActionPacket(action, path, node));
    }
    public void acknowledge() { pending = false; }
    private int materialCount() {
        return minecraft.player == null ? 0 : StarRailTraceService.count(minecraft.player, StarRailTraceMaterials.get(path));
    }
    @Override protected void renderPage(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        StarRailUiStyle.renderBackdrop(g, width, height);
        StarRailUiStyle.renderPanel(g, left, top, right, bottom);
        StarRailUiStyle.renderHeader(g, title, left, top);
        if (!path.isRealPath()) {
            g.drawString(font, "当前未踏上命途", detailLeft + 10, top + 80, StarRailUiStyle.GOLD_ACCENT);
            g.drawString(font, "踏上命途后，行迹树将在此显示。", detailLeft + 10, top + 101,
                    StarRailUiStyle.MUTED_COLOR);
            unlock.active = false;
            rollback.active = false;
            reset.active = false;
            super.renderPage(g, mouseX, mouseY, partialTick);
            return;
        }
        int mask = StarRailTraceClientState.mask(path), rank = StarRailTraceClientState.rank(path);
        double now = StarRailCosmicUi.seconds();
        if (mask != previousMask) {
            changedMask = previousMask < 0 ? 0 : mask ^ previousMask;
            previousMask = mask; changedAt = now;
        }
        // Match the HTML emblem's centered 68%-by-98% contain box. Every path
        // has its own proportion-preserving icon texture and accent color.
        double framebufferScale = uiScaleFactor() * minecraft.getWindow().getGuiScale();
        double htmlMaxWidth = 520.0 / framebufferScale;
        double htmlMaxHeight = 720.0 / framebufferScale;
        double aspect = layout.textureWidth() / (double)layout.textureHeight();
        double iconWidth = Math.min(Math.min(Math.min(treeWidth * .68, htmlMaxWidth),
                treeHeight * .98 * aspect), htmlMaxHeight * aspect);
        int emblemWidth = Math.max(1, (int)Math.round(iconWidth));
        int emblemHeight = Math.max(1, (int)Math.round(iconWidth / aspect));
        int emblemX = (int)Math.round(treeLeft + treeWidth * .5 - emblemWidth * .5);
        int emblemY = (int)Math.round(treeTop + treeHeight * .49 - emblemHeight * .5);
        ResourceLocation watermark = new ResourceLocation(StarRailSimMod.MOD_ID,
                "textures/gui/traces/" + path.getId() + ".png");
        g.flush();
        float tintRed=path==StarRailPath.HUNT?.82F:1F;
        float tintGreen=path==StarRailPath.HUNT?.94F:1F;
        RenderSystem.enableBlend(); RenderSystem.setShaderColor(tintRed, tintGreen, 1F, .14F);
        // Keep the destination size separate from the source region: otherwise
        // GuiGraphics samples only the upper-left portion of the watermark.
        g.blit(watermark, emblemX, emblemY, emblemWidth, emblemHeight, 0F, 0F,
                layout.textureWidth(), layout.textureHeight(), layout.textureWidth(), layout.textureHeight());
        g.flush(); RenderSystem.setShaderColor(1,1,1,1);
        g.drawManaged(() -> {
            int strokeIndex = 0;
            for (StarRailTraceLayouts.Stroke stroke : layout.strokes()) {
                for (StarRailTraceLayouts.Segment segment : stroke.segments()) {
                    drawStrokeSegment(g, segment, stroke.core(), now, strokeIndex++);
                }
            }
            for (int i=0;i<StarRailTraces.nodes(path).length;i++) drawNode(g,i,mask,mouseX,mouseY,now);
        });
        drawDetails(g,mask,rank,mouseX,mouseY);
        boolean activePath = StarRailPathClientState.getCurrentPath() == path;
        unlock.active = !pending && !confirmation && activePath && StarRailTraces.canUnlock(path,mask,rank,selected) && materialCount() >= StarRailTraces.nodes(path)[selected].cost();
        rollback.active = !pending && !confirmation && activePath && StarRailTraces.has(mask,selected);
        reset.active = !pending && !confirmation && activePath && mask != 0;
        super.renderPage(g,mouseX,mouseY,partialTick);
        if (confirmation) drawConfirmation(g,mask);
    }
    private void ring(GuiGraphics g,double x,double y,double radius,int color) {
        for(int i=0;i<96;i++) {
            double a=i*Math.PI*2/96,b=(i+1)*Math.PI*2/96;
            StarRailCosmicUi.line(g,x+Math.cos(a)*radius,y+Math.sin(a)*radius,x+Math.cos(b)*radius,y+Math.sin(b)*radius,color);
        }
    }
    private void drawStrokeSegment(GuiGraphics g, StarRailTraceLayouts.Segment segment,
                                   boolean core, double time, int offset) {
        double head=(time*.19+offset*.13)%1, px=segment.x1(), py=segment.y1();
        int baseColor=(core?0x9A000000:0x72000000)|(layout.accent()&0xFFFFFF);
        int r=Math.min(255,((layout.accent()>>16)&255)+48);
        int green=Math.min(255,((layout.accent()>>8)&255)+48);
        int blue=Math.min(255,(layout.accent()&255)+48);
        int flowColor=0xD8000000|(r<<16)|(green<<8)|blue;
        for(int i=1;i<=70;i++) {
            double t=i/70.0,u=1-t,x,y;
            if (segment.kind()==0) {
                x=u*segment.x1()+t*segment.x2(); y=u*segment.y1()+t*segment.y2();
            } else if (segment.kind()==1) {
                x=u*u*segment.x1()+2*u*t*segment.c1x()+t*t*segment.x2();
                y=u*u*segment.y1()+2*u*t*segment.c1y()+t*t*segment.y2();
            } else {
                x=u*u*u*segment.x1()+3*u*u*t*segment.c1x()+3*u*t*t*segment.c2x()+t*t*t*segment.x2();
                y=u*u*u*segment.y1()+3*u*u*t*segment.c1y()+3*u*t*t*segment.c2y()+t*t*t*segment.y2();
            }
            double distance=Math.abs(t-head);
            int color=distance < .06 ? flowColor : baseColor;
            StarRailCosmicUi.line(g,originX+px*scale,originY+py*scale,originX+x*scale,originY+y*scale,color);
            px=x;py=y;
        }
    }
    private void drawNode(GuiGraphics g,int i,int mask,int mx,int my,double now) {
        var node=StarRailTraces.nodes(path)[i]; int x=(int)(originX+node.x()*scale),y=(int)(originY+node.y()*scale);
        boolean owned=StarRailTraces.has(mask,i),hover=Math.hypot(mx-x,my-y)<15;
        int color=owned ? 0xFFE2CA89 : 0xFF000000 | layout.accent();
        StarRailCosmicUi.ellipse(g,x,y,11,11,0xEE102038);
        ring(g,x,y,i>=6?13:11,color);
        if(i==selected || hover) {
            double selectionPulse = i == selected ? Math.max(0, 1-(now-selectedAt)/.4)*5 : 0;
            ring(g,x,y,16+Math.sin(now*3)*.6+selectionPulse,0xFFFFFFFF);
            ring(g,x,y,19,(0x48000000|layout.accent()));
        }
        if((changedMask&(1<<i))!=0 && now-changedAt<.8) {
            double t=(now-changedAt)/.8;
            ring(g,x,y,owned?13+t*22:35-t*22,((int)(220*(1-t))<<24)|0xB7E5FF);
        }
        if(i>=6) {
            StarRailCosmicUi.line(g,x-5,y,x+5,y,color); StarRailCosmicUi.line(g,x,y-6,x,y+6,color);
            StarRailCosmicUi.line(g,x-4,y-4,x+4,y+4,color); StarRailCosmicUi.line(g,x-4,y+4,x+4,y-4,color);
        } else if(node.stat()==1) {
            StarRailCosmicUi.line(g,x-4,y+4,x+5,y-5,color);
            StarRailCosmicUi.line(g,x+1,y-5,x+5,y-5,color); StarRailCosmicUi.line(g,x+5,y-5,x+5,y-1,color);
        } else {
            int r=node.stat()==0?5:7;
            StarRailCosmicUi.line(g,x,y-r,x+r,y,color);StarRailCosmicUi.line(g,x+r,y,x,y+r,color);
            StarRailCosmicUi.line(g,x,y+r,x-r,y,color);StarRailCosmicUi.line(g,x-r,y,x,y-r,color);
        }
    }
    private void drawDetails(GuiGraphics g,int mask,int rank,int mx,int my) {
        int x=detailLeft+10,y=top+68,w=188;
        g.fill(detailLeft,top+58,right-8,bottom-8,0x6610192D);
        g.drawString(font,layout.heading(),x,y,0xFF000000|layout.accent());y+=23;
        g.drawString(font,path.getDisplayName()+"阶位  "+rank+" / 7",x,y,StarRailUiStyle.MUTED_COLOR);y+=20;
        g.drawString(font,"已解锁  "+Integer.bitCount(mask)+" / 9",x,y,StarRailUiStyle.VALUE_COLOR);y+=24;
        g.fill(x,y,x+w,y+1,0x506E8CA8); y+=13;
        var tree=StarRailTraces.nodes(path);
        var node=tree[selected];
        g.drawString(font,node.name(),x,y,StarRailUiStyle.GOLD_ACCENT);y+=21;
        int textTop=y,textBottom=bottom-128;
        String condition=StarRailTraces.has(mask,selected)?"已解锁":rank<node.rank()?"阶位不足":node.prerequisite()>=0&&!StarRailTraces.has(mask,node.prerequisite())?"需先解锁前置节点":"可解锁";
        StringBuilder bodyBuilder=new StringBuilder(node.description())
                .append("\n\n所需阶位：").append(node.rank())
                .append("\n前置行迹：").append(node.prerequisite()<0?"无":tree[node.prerequisite()].name())
                .append("\n状态：").append(condition).append("\n\n行迹累计加成");
        for(int stat:StarRailTraces.stats(path)) bodyBuilder.append("\n").append(StarRailTraces.statName(stat))
                .append(" +").append(percent(StarRailTraces.bonus(path,mask,stat)));
        String body=bodyBuilder.toString();
        var lines=font.split(Component.literal(body),w-10); int contentHeight=lines.size()*14;
        scroll.bounds(contentHeight-(textBottom-textTop));scroll.advance();
        clip(g,x,textTop,x+w,textBottom);
        int row=textTop-(int)scroll.position();
        for(var line:lines) { g.drawString(font,line,x,row,StarRailUiStyle.VALUE_COLOR);row+=14; }
        g.disableScissor();renderScrollBar(g,scroll,x+w-2,textTop,textBottom,contentHeight,mx,my);
        y=bottom-116;
        g.renderItem(new net.minecraft.world.item.ItemStack(StarRailTraceMaterials.get(path)),x,y);
        g.drawString(font,StarRailTraceMaterials.displayName(path),x+22,y+2,StarRailUiStyle.VALUE_COLOR);
        g.drawString(font,"持有 "+materialCount()+"  /  需要 "+node.cost(),x+22,y+17,StarRailUiStyle.MUTED_COLOR);
        if(StarRailPathClientState.getCurrentPath()!=path) g.drawString(font,"踏上"+path.getDisplayName()+"命途后可解锁",x,bottom-77,StarRailUiStyle.GOLD_ACCENT);
    }
    private String percent(double value) { return Math.round(value*100)+"%"; }
    private void drawConfirmation(GuiGraphics g,int mask) {
        g.fill(0,0,width,height,0xC0080D1C);
        int x=width/2-155,y=height/2-60;
        g.fill(x,y,x+310,y+120,0xFF16263C);g.renderOutline(x,y,310,120,0xFF8EBCE3);
        g.drawCenteredString(font,resetAll?"重置全部"+path.getDisplayName()+"行迹？":"回退当前节点及其依赖节点？",width/2,y+18,StarRailUiStyle.VALUE_COLOR);
        int next=resetAll?0:StarRailTraces.rollback(path,mask,selected);
        g.drawCenteredString(font,"返还 "+(StarRailTraces.cost(path,mask)-StarRailTraces.cost(path,next))+" 个"+StarRailTraceMaterials.displayName(path),width/2,y+43,StarRailUiStyle.GOLD_ACCENT);
        g.renderOutline(x+24,y+77,118,25,0xFF85BEEB);g.renderOutline(x+168,y+77,118,25,0xFF85BEEB);
        g.drawCenteredString(font,"确认回退",x+83,y+85,StarRailUiStyle.VALUE_COLOR);
        g.drawCenteredString(font,"取消",x+227,y+85,StarRailUiStyle.VALUE_COLOR);
    }
    @Override protected boolean logicalMouseClicked(double x,double y,int button) {
        if (!path.isRealPath()) return super.logicalMouseClicked(x, y, button);
        if(confirmation) {
            int left=width/2-155,top=height/2-60;
            if(button==0&&y>=top+77&&y<=top+102) {
                if(x>=left+24&&x<=left+142) {send(resetAll?TraceActionPacket.Action.RESET:TraceActionPacket.Action.ROLLBACK,selected);confirmation=false;}
                else if(x>=left+168&&x<=left+286) confirmation=false;
            }
            return true;
        }
        if(button==0) for(int i=0;i<StarRailTraces.nodes(path).length;i++) {
            var node=StarRailTraces.nodes(path)[i];
            if(Math.hypot(x-originX-node.x()*scale,y-originY-node.y()*scale)<16) {
                selected=i;selectedAt=StarRailCosmicUi.seconds();scroll.reset();return true;
            }
        }
        return super.logicalMouseClicked(x,y,button);
    }
    @Override protected boolean logicalMouseScrolled(double x,double y,double amount) {
        if(confirmation)return true;
        if(x>=detailLeft) {scroll.wheel(amount,32);return true;}
        return super.logicalMouseScrolled(x,y,amount);
    }
    @Override public boolean keyPressed(int key,int scan,int modifiers) {
        if(key==256&&confirmation){confirmation=false;return true;}
        return super.keyPressed(key,scan,modifiers);
    }
}
