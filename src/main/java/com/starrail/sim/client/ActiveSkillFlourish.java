package com.starrail.sim.client;

import com.starrail.sim.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import static com.starrail.sim.client.ActiveSkillClient.line;

/** Richer active-skill compositions. These never reschedule a hit or add gameplay effects. */
final class ActiveSkillFlourish {
    private static final Vec3 UP=new Vec3(0,1,0);
    private static double clamp(double t) {return Math.max(0,Math.min(1,t));}
    private static Vec3 radial(double a,double r,double y) {return new Vec3(Math.cos(a)*r,y,Math.sin(a)*r);}
    private static Vec3 position(Entity e,float partial) {return new Vec3(e.xOld+(e.getX()-e.xOld)*partial,e.yOld+(e.getY()-e.yOld)*partial,e.zOld+(e.getZ()-e.zOld)*partial);}
    static void spectacle(ActiveSkillFxPacket p,double age,Vec3 camera,float partial,MonsterCombatEffects.Sink sink,ClientLevel level) {
        Entity owner=level.getEntity(p.sourceId());Vec3 feet=p.to(),body=feet.add(0,1,0);
        if(p.stage()==0 && (p.path()==StarRailPath.PRESERVATION || p.path()==StarRailPath.HARMONY || p.path()==StarRailPath.ABUNDANCE || p.path()==StarRailPath.ELATION)) {
            if(owner==null || owner.isRemoved() || !owner.isAlive())return;
            feet=position(owner,partial);body=feet.add(0,owner.getBbHeight()*.55,0);
        }
        if(p.path()==StarRailPath.REMEMBRANCE) {
            Entity target=level.getEntity(p.targetId());
            body=p.stage()==0 && target!=null && !target.isRemoved()?position(target,partial).add(0,target.getBbHeight()*.6,0):p.to().add(0,.7,0);
        }
        int detail=switch(Minecraft.getInstance().options.particles().get()) {case MINIMAL->10;case DECREASED->16;default->24;};
        PathSkillSpectacle.draw(p,age,ExtraActiveSkillVisuals.life(p),feet,body,camera,detail,ExtraActiveSkillVisuals.remaining(p.sourceId()),sink);
    }
    static void hunt(ActiveSkillFxPacket p,double age,Vec3 camera,MonsterCombatEffects.Sink sink) {
        double fade=Math.pow(clamp(1-age/1.45),.75),flash=clamp(1-age/.35);
        Vec3 axis=p.to().subtract(p.from()).normalize();if(axis.lengthSqr()<.01)return;
        Vec3 side=axis.cross(UP).normalize();if(side.lengthSqr()<.01)side=new Vec3(1,0,0);
        Vec3 up=side.cross(axis).normalize();
        // Instant trace plus a long luminous afterimage; no slow projectile arriving after the real hit.
        Vec3 muzzle=p.from().add(side.scale(.1)).subtract(UP.scale(.13));
        line(sink,muzzle,p.to(),camera,.105,0x24749f,fade*.24,1,age*4);
        line(sink,muzzle,p.to(),camera,.045,0x69ddff,fade*.75,1,age*4);
        line(sink,muzzle,p.to(),camera,.012,0xe1fcff,fade*.9,1,age*4);
        for(int i=0;i<6;i++) {
            double f=(i+1)/8.0;Vec3 node=muzzle.lerp(p.to(),f),root=node.subtract(axis.scale(.22));
            line(sink,root.add(side.scale(.1)),node,camera,.014,0xa8f2ff,fade*.6,1,i+age*6);
            line(sink,root.subtract(side.scale(.1)),node,camera,.014,0xa8f2ff,fade*.6,1,i+age*6);
        }
        for(int blade=0;blade<4;blade++) {
            Vec3 wing=blade<2?side.scale(blade==0?1:-1):up.scale(blade==2?1:-1);
            Vec3 rear=p.to().subtract(axis.scale(.6));
            Vec3 outer=rear.add(wing.scale(.22));
            sink.face(0x64cfee,fade*.42,1,blade+age,p.to(),outer,rear.subtract(axis.scale(.14)),p.to());
            line(sink,outer,p.to(),camera,.024,0xd7faff,fade*.95,1,blade+age);
        }
        hoop(sink,p.from().add(axis.scale(.75)),side,up,.19,.025,0x8cecff,flash*.7,1,20,camera,age);
        if(p.hit()) {
            hoop(sink,p.to(),side,up,.12+Math.min(.6,age*.9),.02,0x8cecff,fade*.65,1,24,camera,age);
            for(int i=0;i<8;i++) {
                double a=i*Math.PI/4;Vec3 dir=side.scale(Math.cos(a)).add(up.scale(Math.sin(a)));
                line(sink,p.to().add(dir.scale(.1+age*.35)),p.to().add(dir.scale(.3+age*.5)),camera,.018,0xc2f7ff,fade*.8,1,i+age);
            }
        }
    }
    static void draw(ActiveSkillFxPacket p,double age,Vec3 camera,float partial,MonsterCombatEffects.Sink sink,ClientLevel level) {
        if(p.path()==StarRailPath.HUNT)return;
        Minecraft mc=Minecraft.getInstance();int n=mc.options.particles().get()==ParticleStatus.MINIMAL?12:24;
        boolean cast=p.stage()==0;double fade=clamp(1-age/ExtraActiveSkillVisuals.life(p));
        Entity owner=level.getEntity(p.sourceId());Vec3 feet=p.to();
        Vec3 body=owner==null?feet.add(0,1,0):position(owner,partial).add(0,owner.getBbHeight()*.55,0);
        if(cast && (p.path()==StarRailPath.PRESERVATION || p.path()==StarRailPath.HARMONY || p.path()==StarRailPath.ABUNDANCE || p.path()==StarRailPath.ELATION)) {
            if(owner==null || owner.isRemoved() || !owner.isAlive())return;feet=position(owner,partial);
        }
        switch(p.path()) {
            case PRESERVATION -> {
                double open=clamp(age/.18),r=.85*open;
                hoop(sink,body.add(0,-.45,0),new Vec3(1,0,0),new Vec3(0,0,1),r,.02,0xffd17b,fade*.7,2,n,camera,age);
                hoop(sink,body.add(0,.5,0),new Vec3(1,0,0),new Vec3(0,0,1),r*.9,.015,0xffe7ac,fade*.6,2,n,camera,-age);
                for(int i=0;i<6;i++) {
                    double a=i*Math.PI/3;Vec3 root=body.add(radial(a,r,0));
                    Vec3 tangent=radial(a+Math.PI/2,.17,0);
                    line(sink,root.subtract(tangent).add(0,-.22,0),root.add(0,.14,0),camera,.014,0xffd17b,fade*.7,2,i+age);
                    line(sink,root.add(0,.14,0),root.add(tangent).add(0,-.22,0),camera,.014,0xffe7ac,fade*.7,2,i+age);
                }
            }
            case DESTRUCTION -> {
                for(int i=0;i<8;i++) {
                    double a=i*Math.PI/4;Vec3 base=MonsterCombatEffects.surface(feet,Math.cos(a)*1.2,Math.sin(a)*1.2,mc);if(base==null)continue;
                    Vec3 tip=base.add(radial(a,.5+age*.5,(1-age/1.2)*.9));
                    Vec3 side=radial(a+Math.PI/2,.09,0);
                    sink.face(0xf06b4e,fade*.3,4,i+age,base.subtract(side),base.add(side),tip,tip);
                    line(sink,base,tip,camera,.022,0xffbd68,fade*.8,4,i+age*4);
                    shard(sink,tip.add(0,.15,0),.045,0xffc77a,fade*.7,4,camera,i+age);
                }
                hoop(sink,feet.add(0,.12,0),new Vec3(1,0,0),new Vec3(0,0,1),4,.025,0xffb45d,clamp(1-age/.5)*.7,4,n,camera,age);
            }
            case ERUDITION -> {
                Vec3 core=feet.add(0,cast?.7+age:1.5,0);
                hoop(sink,core,new Vec3(1,0,0),new Vec3(0,.5,.86),.7,.014,0xd5aaff,cast?.6:fade*.65,5,n,camera,age);
                hoop(sink,core,new Vec3(1,0,0),new Vec3(0,-.5,.86),.95,.012,0xb888ee,cast?.5:fade*.55,5,n,camera,-age);
                for(int i=0;i<3;i++) {
                    Vec3 a=feet.add(radial(i*Math.PI*2/3,2.7,.12)),b=feet.add(radial((i+1)*Math.PI*2/3,2.7,.12));
                    line(sink,a,b,camera,.013,0xbc8af4,cast?.4:fade*.6,5,i+age);
                }
                if(!cast)for(int i=0;i<8;i++)shard(sink,core.add(radial(i*Math.PI/4,.5+age*.9,age*.25)),.07,0xe3c8ff,fade*.75,5,camera,i+age);
            }
            case HARMONY -> {
                if(cast) {
                    double wave=age<.7?age/.7*8:0;
                    if(wave>0)hoop(sink,feet.add(0,.12,0),new Vec3(1,0,0),new Vec3(0,0,1),wave,.014,0xeeb3d3,clamp(1-age/.7)*.4,7,n,camera,age);
                    for(int i=0;i<3;i++)curve(sink,body.add(radial(i*2.1,.6,-.3)),body.add(radial(i*2.1+1.8,.75,.5)),.2,0xffc9e1,.45,7,n,camera,age+i);
                } else {
                    curve(sink,p.from(),p.to(),.65,0xeea5ce,fade*.7,7,n,camera,age*2);
                    for(int i=0;i<3;i++)shard(sink,p.to().add(radial(i*2.1+age,.25+age*.2,.15)),.055,0xffdf9e,fade*.8,7,camera,i+age);
                }
            }
            case NIHILITY -> {
                for(int i=0;i<6;i++) {
                    double a=i*Math.PI/3;Vec3 root=MonsterCombatEffects.surface(feet,Math.cos(a)*2.9,Math.sin(a)*2.9,mc);if(root==null)continue;
                    Vec3 tip=root.add(radial(a+.4,.25,cast?.45+Math.sin(age*2+i)*.1:.6*fade));
                    line(sink,root,tip,camera,.018,0xb58bea,cast?.45:fade*.7,6,i+age);
                    if(cast)line(sink,tip,tip.add(radial(a-1,.2,-.2)),camera,.012,0x6d449a,.4,6,i-age);
                }
                if(!cast)hoop(sink,feet.add(0,.2,0),new Vec3(1,0,0),new Vec3(0,0,1),3.7*Math.max(.1,1-age/.7),.045,0xc39af6,fade*.55,6,n,camera,age);
            }
            case ABUNDANCE -> {
                Vec3 center=cast?feet.add(0,.15,0):p.to();
                for(int i=0;i<8;i++) {
                    double a=i*Math.PI/4+age*.2;Vec3 tip=center.add(radial(a,cast?.95:.45,.35+Math.sin(age*2+i)*.08));
                    Vec3 side=radial(a+Math.PI/2,.15,0);
                    sink.face(0x89dca2,(cast?.14:fade*.25),3,i-age,center,tip.add(side),tip.add(0,.25,0),tip.subtract(side));
                    curve(sink,center,tip,.15,0xe1efa7,cast?.5:fade*.75,3,8,camera,i+age);
                }
                if(!cast && p.from().distanceToSqr(p.to())>1)curve(sink,p.from(),p.to(),.5,0x9ce5b2,fade*.55,3,n,camera,age);
            }
            case REMEMBRANCE -> {
                Entity target=level.getEntity(p.targetId());Vec3 center=cast&&target!=null?position(target,partial).add(0,target.getBbHeight()*.6,0):p.to().add(0,.7,0);
                for(int i=0;i<4;i++) {
                    double a=i*Math.PI/2+Math.PI/4;Vec3 edge=center.add(new Vec3(Math.cos(a),Math.sin(a),0).scale(cast?.65:.65*Math.max(.1,1-age/.6)));
                    shard(sink,edge,.1,0xa4e7f4,cast?.38:fade*.7,8,camera,i+age);
                    if(!cast && p.hit())line(sink,edge,center,camera,.012,0xdcfcff,fade*.7,8,i-age);
                }
            }
            case ELATION -> {
                if(cast) {
                    int beats=ExtraActiveSkillVisuals.remaining(p.sourceId());
                    for(int i=0;i<beats;i++) {
                        Vec3 point=body.add(radial(age*.9+i*Math.PI*2/3,.78,.25));
                        hoop(sink,point,new Vec3(1,0,0),UP,.12,.012,i%2==0?0xffacd0:0xffd581,.65,9,4,camera,i+age);
                        shard(sink,point,.045,0xfff0c1,.7,9,camera,i+age);
                    }
                } else if(p.hit()) {
                    for(int i=0;i<6;i++) {
                        double a=i*Math.PI/3;Vec3 end=p.to().add(radial(a,2.2,.4+Math.sin(a)*.3));
                        curve(sink,p.to().add(0,.7,0),end,.35,i%2==0?0xff82b7:0xffd277,fade*.75,9,12,camera,i+age);
                    }
                }
            }
            default -> {}
        }
    }
    private static void curve(MonsterCombatEffects.Sink sink,Vec3 a,Vec3 b,double lift,int color,double alpha,int material,int n,Vec3 camera,double time) {
        for(int i=0;i<n;i++) {
            double f=(double)i/n,g=(double)(i+1)/n;
            line(sink,a.lerp(b,f).add(0,Math.sin(f*Math.PI)*lift,0),a.lerp(b,g).add(0,Math.sin(g*Math.PI)*lift,0),camera,.012,color,alpha,material,time+f*2);
        }
    }
    private static void hoop(MonsterCombatEffects.Sink sink,Vec3 center,Vec3 u,Vec3 v,double r,double width,int color,double alpha,int material,int n,Vec3 camera,double time) {
        for(int i=0;i<n;i++) {
            double a=i*Math.PI*2/n,b=(i+1)*Math.PI*2/n;
            line(sink,center.add(u.scale(Math.cos(a)*r)).add(v.scale(Math.sin(a)*r)),center.add(u.scale(Math.cos(b)*r)).add(v.scale(Math.sin(b)*r)),camera,width,color,alpha,material,time+a);
        }
    }
    private static void shard(MonsterCombatEffects.Sink sink,Vec3 center,double size,int color,double alpha,int material,Vec3 camera,double time) {
        Vec3 side=center.subtract(camera).cross(UP).normalize().scale(size*.55);if(side.lengthSqr()<1e-8)side=new Vec3(size*.55,0,0);
        sink.face(color,alpha,material,time,center.add(0,size,0),center.add(side),center.add(0,-size,0),center.subtract(side));
    }
}
