package com.starrail.sim.client;

import com.starrail.sim.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;
import static com.starrail.sim.client.ActiveSkillClient.line;

/** Distinct seven-path active compositions using the existing validated Path materials. */
final class ExtraActiveSkillVisuals {
    private record Beats(long start,int remaining) {}
    private static final Map<Integer,Beats> BEATS=new HashMap<>();
    static void clear() {BEATS.clear();}
    static double life(ActiveSkillFxPacket p) {
        if(p.stage()!=0)return p.stage()==5?.95:1.2;
        return switch(p.path()) {case NIHILITY->6;case HARMONY->8;case ABUNDANCE->3;case REMEMBRANCE,ELATION->5;case ERUDITION->.6;case HUNT->1.5;case PRESERVATION->1.6;default->1;};
    }
    static void accept(ActiveSkillFxPacket p) {
        if(p.path()!=StarRailPath.ELATION)return;
        if(p.stage()==0) {
            Beats old=BEATS.get(p.sourceId());if(old==null || old.start()!=p.tick())BEATS.put(p.sourceId(),new Beats(p.tick(),3));
        } else if(p.stage()==5) {
            Beats old=BEATS.get(p.sourceId());if(old!=null)BEATS.put(p.sourceId(),new Beats(old.start(),Math.max(0,old.remaining()-1)));
        } else if(p.stage()==3)BEATS.remove(p.sourceId());
    }
    static int remaining(int id) {Beats b=BEATS.get(id);return b==null?3:b.remaining();}
    private static Vec3 position(Entity e,float partial) {
        return new Vec3(e.xOld+(e.getX()-e.xOld)*partial,e.yOld+(e.getY()-e.yOld)*partial,e.zOld+(e.getZ()-e.zOld)*partial);
    }
    static void draw(ActiveSkillFxPacket p,double age,Vec3 camera,float partial,MonsterCombatEffects.Sink sink,ClientLevel level) {
        Minecraft mc=Minecraft.getInstance();int n=mc.options.particles().get()==ParticleStatus.MINIMAL?16:32;
        boolean cast=p.stage()==0;double fade=Math.max(0,1-age/life(p));
        Entity owner=level.getEntity(p.sourceId());Vec3 feet=p.to();
        Vec3 body=owner==null?feet.add(0,1,0):position(owner,partial).add(0,owner.getBbHeight()*.55,0);
        if(cast && (p.path()==StarRailPath.HARMONY || p.path()==StarRailPath.ABUNDANCE || p.path()==StarRailPath.ELATION)) {
            if(owner==null || owner.isRemoved() || !owner.isAlive())return;
            feet=position(owner,partial);
        }
        switch(p.path()) {
            case DESTRUCTION -> {
                ring(sink,feet,.2+Math.min(1,age/.25)*4,.12,0xfa795d,fade*.8,4,n,camera,age,mc);
                for(int i=0;i<10;i++) {
                    double a=i*Math.PI/5;Vec3 dir=new Vec3(Math.cos(a),0,Math.sin(a));
                    Vec3 from=MonsterCombatEffects.surface(feet,dir.x*.35,dir.z*.35,mc),to=MonsterCombatEffects.surface(feet,dir.x*3.8,dir.z*3.8,mc);
                    if(from!=null&&to!=null)line(sink,from,to,camera,.025,0xffbd68,fade*.8,4,i+age*3);
                }
            }
            case ERUDITION -> {
                double charge=Math.min(1,age/.6);
                ring(sink,feet,4,.04,0xbd87f5,cast?.65:fade*.8,5,n,camera,age,mc);
                for(int i=0;i<6;i++) {
                    double a=i*Math.PI/3;Vec3 base=MonsterCombatEffects.surface(feet,Math.cos(a)*2.2,Math.sin(a)*2.2,mc);
                    if(base==null)continue;
                    Vec3 node=base.add(0,cast?.4+charge*.6:2.8,0);
                    diamond(sink,node,.13,0xe2c1ff,cast?.7:fade,5,camera,i+age);
                    if(!cast)line(sink,node,base,camera,.07,0xc497ff,fade*.75,5,i+age);
                    if(cast)line(sink,node,feet.add(0,.15,0),camera,.009,0xb48deb,.35,5,i+age);
                }
            }
            case HARMONY -> {
                if(cast) {
                    for(int ring=0;ring<3;ring++) {
                        double y=.25+ring*.42;
                        orbit(sink,feet.add(0,y,0),.65+ring*.16,0xee97ca,.42,7,n,camera,age*(ring%2==0?1:-1));
                    }
                    for(int i=0;i<5;i++)diamond(sink,body.add(Math.cos(age+i*1.25)*.85,.35+Math.sin(age*2+i)*.12,Math.sin(age+i*1.25)*.85),.06,0xffdeb0,.6,7,camera,i+age);
                } else {
                    line(sink,p.from(),p.to(),camera,.018,0xee97ca,fade*.55,7,age*2);
                    orbit(sink,p.to(),.3+age*.35,0xffd4a1,fade*.8,7,n,camera,age);
                }
            }
            case NIHILITY -> {
                ring(sink,feet,4,.045,0x956ae1,cast?.6:fade*.9,6,n,camera,age,mc);
                if(cast)for(int i=0;i<8;i++) {
                    double a=i*Math.PI/4;Vec3 base=MonsterCombatEffects.surface(feet,Math.cos(a)*2.8,Math.sin(a)*2.8,mc);
                    if(base!=null)diamond(sink,base.add(0,.25+(1-(age+i*.13)%1)*.6,0),.09,0x7952b5,.45,6,camera,i+age);
                }
                else ring(sink,feet,4*Math.max(.02,1-age/.65),.1,0xb58aeb,fade*.6,6,n,camera,age,mc);
            }
            case ABUNDANCE -> {
                Vec3 center=cast?feet.add(0,.1,0):p.to();
                for(int i=0;i<8;i++) {
                    double a=i*Math.PI/4,r=cast?.55:.3+age*.35;
                    Vec3 tip=center.add(Math.cos(a)*r,.25+(cast?Math.sin(age*2)*.08:age*.25),Math.sin(a)*r);
                    Vec3 side=new Vec3(-Math.sin(a)*.16,0,Math.cos(a)*.16);
                    sink.face(0x77d99b,(cast?.24:fade*.35),3,i+age,center,tip.add(side),tip.add(0,.25,0),tip.subtract(side));
                    line(sink,center,tip,camera,.015,0xe2f5ad,cast?.55:fade*.8,3,i+age);
                }
                if(!cast)for(int i=0;i<5;i++)diamond(sink,center.add(Math.cos(i*1.25+age)*.35,age*.65,Math.sin(i*1.25+age)*.35),.045,0xd9eea5,fade,3,camera,i+age);
            }
            case REMEMBRANCE -> {
                Entity target=level.getEntity(p.targetId());Vec3 center=cast&&target!=null?position(target,partial).add(0,target.getBbHeight()*.6,0):p.to().add(0,.7,0);
                double radius=cast?.6:.6*Math.max(.1,1-age/.6);
                orbit(sink,center,radius,0x8addf0,cast?.55:fade*.8,8,n,camera,age);
                for(int i=0;i<4;i++) {
                    double a=i*Math.PI/2+Math.PI/4;Vec3 axis=new Vec3(Math.cos(a),Math.sin(a),0);
                    Vec3 start=center.add(axis.scale(radius));
                    line(sink,start,start.add(axis.scale(cast?.14:.35)),camera,.02,0xb9f5ff,cast?.7:fade,8,i+age);
                }
                if(!cast && p.hit())for(int i=0;i<3;i++) {
                    Vec3 a=center.add(-.4,(-1+i)*.22,.03),b=center.add(.4,(-1+i)*.22,.03);
                    line(sink,a,b,camera,.025,0xa9ecff,fade,8,i+age);
                }
            }
            case ELATION -> {
                if(cast) {
                    Beats beat=BEATS.get(p.sourceId());int remaining=beat==null?3:beat.remaining();
                    for(int i=0;i<remaining;i++) {
                        double a=age*.9+i*Math.PI*2/3;
                        diamond(sink,body.add(Math.cos(a)*.75,.2+Math.sin(age*2+i)*.1,Math.sin(a)*.75),.1,i%2==0?0xff82b7:0xffd277,.7,9,camera,i+age);
                    }
                } else {
                    Vec3 center=p.to().add(0,.65,0);
                    for(int i=0;i<(p.hit()?12:6);i++) {
                        double a=i*Math.PI/6;Vec3 dir=new Vec3(Math.cos(a),Math.sin(a),Math.sin(a*3)*.4);
                        diamond(sink,center.add(dir.scale(.2+age*(p.hit()?2:1))),.055,i%2==0?0xff82b7:0xffd277,fade,9,camera,i+age);
                    }
                    if(p.hit())ring(sink,p.to(),3*Math.min(1,age/.2),.065,0xff82b7,fade*.7,9,n,camera,age,mc);
                }
            }
            default -> {}
        }
    }
    private static void diamond(MonsterCombatEffects.Sink sink,Vec3 center,double size,int color,double alpha,int material,Vec3 camera,double u) {
        Vec3 normal=center.subtract(camera).normalize(),side=normal.cross(new Vec3(0,1,0)).normalize().scale(size*.55);
        sink.face(color,alpha,material,u,center.add(0,size,0),center.add(side),center.add(0,-size,0),center.subtract(side));
    }
    private static void orbit(MonsterCombatEffects.Sink sink,Vec3 center,double radius,int color,double alpha,int material,int n,Vec3 camera,double time) {
        for(int i=0;i<n;i++) {
            double a=i*Math.PI*2/n+time,b=a+Math.PI*1.7/n;
            line(sink,center.add(Math.cos(a)*radius,Math.sin(a)*radius*.5,Math.sin(a)*radius),center.add(Math.cos(b)*radius,Math.sin(b)*radius*.5,Math.sin(b)*radius),camera,.012,color,alpha,material,a);
        }
    }
    private static void ring(MonsterCombatEffects.Sink sink,Vec3 center,double radius,double width,int color,double alpha,int material,int n,Vec3 camera,double time,Minecraft mc) {
        for(int i=0;i<n;i++) {
            double a=i*Math.PI*2/n,b=(i+1)*Math.PI*2/n;
            Vec3 p=MonsterCombatEffects.surface(center,Math.cos(a)*(radius-width),Math.sin(a)*(radius-width),mc),q=MonsterCombatEffects.surface(center,Math.cos(a)*(radius+width),Math.sin(a)*(radius+width),mc),
                    r=MonsterCombatEffects.surface(center,Math.cos(b)*(radius+width),Math.sin(b)*(radius+width),mc),s=MonsterCombatEffects.surface(center,Math.cos(b)*(radius-width),Math.sin(b)*(radius-width),mc);
            if(p!=null&&q!=null&&r!=null&&s!=null && Math.abs(p.y-r.y)<.55)sink.face(color,alpha,material,a+time,p,q,r,s);
        }
    }
}
