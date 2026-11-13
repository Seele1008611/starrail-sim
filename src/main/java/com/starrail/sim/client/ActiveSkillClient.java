package com.starrail.sim.client;

import com.starrail.sim.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** New skill UI only; leaves the existing character/relic attribute statistics untouched. */
@Mod.EventBusSubscriber(modid=StarRailSimMod.MOD_ID,value=Dist.CLIENT)
public final class ActiveSkillClient {
    private static ClientLevel level;
    private static ActiveSkillStatePacket state;
    private static int cooldown,duration,activeTicks;
    private static boolean held;
    private static final List<ActiveSkillFxPacket> EFFECTS=new ArrayList<>();
    private ActiveSkillClient() {}
    private static void ensure() {
        Minecraft mc=Minecraft.getInstance();
        if(level!=mc.level || mc.player==null) {level=mc.level;state=null;cooldown=duration=activeTicks=0;held=false;EFFECTS.clear();ExtraActiveSkillVisuals.clear();}
    }
    public static void state(ActiveSkillStatePacket packet) {
        ensure();Minecraft mc=Minecraft.getInstance();
        if(mc.player==null || level==null || !mc.player.getUUID().equals(packet.player()) || !level.dimension().location().equals(packet.dimension()))return;
        state=packet;cooldown=Math.max(0,packet.cooldown());duration=Math.max(0,packet.duration());activeTicks=Math.max(0,packet.activeTicks());
    }
    public static void effect(ActiveSkillFxPacket packet) {
        ensure();Minecraft mc=Minecraft.getInstance();
        if(level==null || mc.player==null || !level.dimension().location().equals(packet.dimension())
                || !finite(packet.from()) || !finite(packet.to()) || packet.from().distanceToSqr(packet.to())>(packet.path()==StarRailPath.HUNT?20*20:64*64)
                || mc.player.distanceToSqr(packet.to())>48*48)return;
        if(packet.stage()==3) {EFFECTS.removeIf(p->p.sourceId()==packet.sourceId() && p.path()==packet.path() && p.stage()==0);ExtraActiveSkillVisuals.accept(packet);return;}
        double age=level.getGameTime()-packet.tick();
        if(age< -5 || age>ExtraActiveSkillVisuals.life(packet)*20)return;
        boolean newCast = packet.stage()==0 && age <= 3 && EFFECTS.stream().noneMatch(p -> p.stage()==0 && p.path()==packet.path() && p.sourceId()==packet.sourceId() && p.tick()==packet.tick());
        boolean destructionHit = packet.path()==StarRailPath.DESTRUCTION && packet.stage()==1 && packet.hit() && age <= 3;
        if ((newCast || destructionHit) && packet.path().isRealPath())
            level.playLocalSound(packet.from().x,packet.from().y,packet.from().z,StarRailCombatSounds.active(packet.path()),net.minecraft.sounds.SoundSource.PLAYERS,.7F,1F,false);
        ExtraActiveSkillVisuals.accept(packet);
        if(packet.stage()==0)EFFECTS.removeIf(p->p.stage()==0 && p.path()==packet.path() && p.sourceId()==packet.sourceId());
        if(EFFECTS.size()>=32) {
            int victim=0;for(int i=0;i<EFFECTS.size();i++)if(EFFECTS.get(i).stage()!=0) {victim=i;break;}
            EFFECTS.remove(victim);
        }
        EFFECTS.add(packet);
    }
    private static boolean finite(Vec3 p) {return Double.isFinite(p.x)&&Double.isFinite(p.y)&&Double.isFinite(p.z);}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        ensure();Minecraft mc=Minecraft.getInstance();
        boolean down=StarRailClient.ACTIVE_SKILL.isDown();
        while(StarRailClient.ACTIVE_SKILL.consumeClick()) {} // Drain queued key repeats, including in menus.
        if(down && !held && mc.player!=null && mc.screen==null && mc.isWindowActive() && !mc.isPaused())
            StarRailNetwork.CHANNEL.sendToServer(new ActiveSkillRequestPacket());
        held=down;
        if(level!=null && !mc.isPaused()) {if(cooldown>0)cooldown--;if(duration>0)duration--;if(activeTicks>0)activeTicks--;}
    }
    @SubscribeEvent public static void hud(RenderGuiEvent.Post event) {
        ensure();Minecraft mc=Minecraft.getInstance();
        if(state==null || mc.player==null || mc.options.hideGui || !StarRailStatusConfig.HUD.get() || !state.path().isRealPath())return;
        Component status=state.rank()<5?Component.translatable("skill.starrail_sim.locked"):
                !ActivePathSkills.supported(state.path())?Component.translatable("skill.starrail_sim.prototype"):
                cooldown>0?Component.translatable("skill.starrail_sim.cooldown",String.format(Locale.ROOT,"%.1f",cooldown/20f)):
                Component.translatable("skill.starrail_sim.ready");
        Component row=Component.translatable("skill.starrail_sim.hud",StarRailClient.ACTIVE_SKILL.getTranslatedKeyMessage(),ActivePathSkills.name(state.path()),status);
        int width=event.getGuiGraphics().guiWidth(),height=event.getGuiGraphics().guiHeight();
        int x=Math.max(4,width-8-mc.font.width(row)),y=width<480?height/2+28:height-51;
        event.getGuiGraphics().drawString(mc.font,row,x,y,0xffdfcda0,true);
        if(activeTicks>0) {
            Component window=Component.translatable("skill.starrail_sim.window",String.format(Locale.ROOT,"%.1f",activeTicks/20f));
            if(state.path()==StarRailPath.ELATION)window=Component.translatable("skill.starrail_sim.charges",state.charges(),String.format(Locale.ROOT,"%.1f",activeTicks/20f));
            if(state.path()==StarRailPath.REMEMBRANCE)window=Component.translatable("skill.starrail_sim.recorded",String.format(Locale.ROOT,"%.1f",state.recordedDamage()),String.format(Locale.ROOT,"%.1f",activeTicks/20f));
            event.getGuiGraphics().drawString(mc.font,window,Math.max(4,width-8-mc.font.width(window)),y-11,0xffacd9eb,true);
        }
        if(duration>0 && state.shield()>0) {
            Component shield=Component.translatable("skill.starrail_sim.shield",String.format(Locale.ROOT,"%.1f",state.shield()),String.format(Locale.ROOT,"%.1f",duration/20f));
            event.getGuiGraphics().drawString(mc.font,shield,Math.max(4,width-8-mc.font.width(shield)),y-11,0xffffd269,true);
        }
    }
    static void draw(Vec3 camera,float partial,MonsterCombatEffects.Sink sink) {
        ensure();if(level==null)return;
        double now=level.getGameTime()+partial;
        EFFECTS.removeIf(p->now-p.tick()>ExtraActiveSkillVisuals.life(p)*20 || now-p.tick()< -5);
        for(var p:EFFECTS) {
            double age=(now-p.tick())/20;
            if(age<0 || age>ExtraActiveSkillVisuals.life(p) || p.to().distanceToSqr(camera)>48*48)continue;
            if(p.path()==StarRailPath.HUNT) {
                ActiveSkillFlourish.hunt(p,age,camera,sink);
            } else if(p.path()==StarRailPath.PRESERVATION) {
                Entity owner=level.getEntity(p.sourceId());if(owner==null || owner.isRemoved() || !owner.isAlive())continue;
                Vec3 center=new Vec3(owner.xOld+(owner.getX()-owner.xOld)*partial,owner.yOld+(owner.getY()-owner.yOld)*partial+owner.getBbHeight()*.55,owner.zOld+(owner.getZ()-owner.zOld)*partial);
                double fade=Math.min(1,age/.08)*Math.max(0,1-age/1.5),radius=.5+Math.min(1,age/.25)*.35;
                for(int i=0;i<6;i++) {
                    double a=i*Math.PI/3,b=a+.7;
                    Vec3 left=center.add(Math.cos(a)*radius,-.4,Math.sin(a)*radius),right=center.add(Math.cos(b)*radius,-.4,Math.sin(b)*radius);
                    Vec3 top=left.add(0,.9,0),topRight=right.add(0,.9,0);
                    sink.face(0xd7a64f,.18*fade,2,i+age,left,right,topRight,top);
                    line(sink,left,top,camera,.018,0xffd77e,fade*.8,2,i+age);
                    line(sink,top,topRight,camera,.018,0xffe4a3,fade*.8,2,i+age);
                }
            } else ExtraActiveSkillVisuals.draw(p,age,camera,partial,sink,level);
            ActiveSkillFlourish.draw(p,age,camera,partial,sink,level);
            ActiveSkillFlourish.spectacle(p,age,camera,partial,sink,level);
        }
    }
    static void line(MonsterCombatEffects.Sink sink,Vec3 a,Vec3 b,Vec3 camera,double width,int color,double alpha,int material,double u) {
        if(alpha<=.002)return;
        Vec3 axis=b.subtract(a),side=axis.cross(a.lerp(b,.5).subtract(camera)).normalize();
        if(side.lengthSqr()<1e-8)side=axis.cross(new Vec3(0,1,0)).normalize();
        if(side.lengthSqr()<1e-8)side=new Vec3(1,0,0);
        side=side.scale(width);
        sink.face(color,alpha,material,u,a.subtract(side),a.add(side),b.add(side),b.subtract(side));
    }
}
