package com.starrail.sim.client;

import com.starrail.sim.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.*;
import static com.starrail.sim.client.MonsterCombatEffects.*;

/** Remaining content-three actors; shares the original face budget, depth and shader pipeline. */
final class ExtraMonsterCombatEffects {
    private static final int PURPLE=0x9857f5,PALE=0xe4b8ff,GOLD=0xf3cc68;
    private static final Vec3 UP=new Vec3(0,1,0);
    private record Section(double tick,Vec3[] edges) {}
    private static final class Body {
        Vec3[] edges;
        Vec3 core;
        double captured=-1000;
        int state=-1;
        final Deque<Section> sections=new ArrayDeque<>();
    }
    private record Point(double tick,Vec3 position) {}
    private static final class Bolt {
        int id;
        double seen;
        boolean collided;
        final Deque<Point> points=new ArrayDeque<>();
    }
    private static final Map<LivingEntity,Body> BODIES=new WeakHashMap<>();
    private static final Map<UUID,Bolt> BOLTS=new HashMap<>();
    private static ClientLevel level;
    static void clear() {BODIES.clear();BOLTS.clear();level=null;}
    private static void sync(Minecraft mc) {
        if(level!=mc.level) {clear();level=mc.level;}
    }
    private static boolean relevant(LivingEntity mob) {
        return mob instanceof DistorterVoidranger || mob instanceof TramplerVoidranger
                || mob instanceof EliminatorVoidranger || mob instanceof StarveinConstructGuardian;
    }
    static void capture(LivingEntity mob,BlockbenchRig rig,Matrix4f[] pose,float scale,float partial,float melee) {
        if(!relevant(mob))return;
        Minecraft mc=Minecraft.getInstance();sync(mc);
        if(level==null || mc.player==null || mob.distanceToSqr(mc.player)>48*48)return;
        Body b=BODIES.computeIfAbsent(mob,k->new Body());
        Vec3[] edges=new Vec3[4];
        for(var geo:rig.geometry) {
            String name=rig.nodes.get(geo.node()).name();
            boolean fist=name.endsWith("握拳") || name.endsWith("重拳")
                    || name.endsWith("握弩手甲") || name.endsWith("拳指主块");
            boolean core=name.equals("弩机中心紫晶") || name.equals("前胸翠色晶体") || name.equals("八面棱星晶");
            if(!fist && !core)continue;
            float[] v=geo.vertices();Vector3f center=new Vector3f();
            float min=Float.POSITIVE_INFINITY,max=Float.NEGATIVE_INFINITY;
            for(int i=0;i<v.length;i+=8) {center.add(v[i],v[i+1],v[i+2]);min=Math.min(min,v[i+1]);max=Math.max(max,v[i+1]);}
            center.div(v.length/8);
            if(core)b.core=point(mob,pose[geo.node()],new Vector3f(center),scale,partial);
            if(fist) {
                int side=name.startsWith("左")?0:1;
                edges[side*2]=point(mob,pose[geo.node()],new Vector3f(center.x,min,center.z),scale,partial);
                edges[side*2+1]=point(mob,pose[geo.node()],new Vector3f(center.x,max,center.z),scale,partial);
            }
        }
        double now=level.getGameTime()+partial;
        int state=mob instanceof VoidrangerMob v?v.getAttackState():((StarveinConstructGuardian)mob).getAttackState();
        boolean blocked=mob.isDeadOrDying() || mob instanceof VoidrangerMob v && (v.hurtTime>0 || state==VoidrangerMob.ATTACK_STAGGER)
                || mob instanceof StarveinConstructGuardian g && g.getStaggerTicks()>0;
        if(blocked || state!=b.state)b.sections.clear();
        boolean swinging=mob instanceof VoidrangerMob?state==VoidrangerMob.ATTACK_IDLE && melee>.03f:
                state==StarveinConstructGuardian.ATTACK_STAR_TRAIL_SWEEP
                        && ((StarveinConstructGuardian)mob).getAttackAnimationTicks(partial)>=12
                        && ((StarveinConstructGuardian)mob).getAttackAnimationTicks(partial)<=22;
        if(!blocked && swinging && Arrays.stream(edges).allMatch(Objects::nonNull) && !mc.isPaused()) {
            Section old=b.sections.peekLast();
            if(old!=null && (now-old.tick()>2 || old.edges()[0].distanceToSqr(edges[0])>9)) {b.sections.clear();old=null;}
            if(old==null || now-old.tick()>.3)b.sections.add(new Section(now,edges));
            while(b.sections.size()>14)b.sections.removeFirst();
        }
        b.edges=edges;b.captured=now;b.state=state;
    }
    private static Vec3 point(LivingEntity mob,Matrix4f matrix,Vector3f local,float scale,float partial) {
        matrix.transformPosition(local);
        if(mob instanceof TramplerVoidranger)local.z-=10.5f; // Same centering as its renderer.
        return MonsterCombatEffects.world(mob,local,scale,partial);
    }
    static void accept(MonsterCombatPacket packet) {
        sync(Minecraft.getInstance());
        if(packet.kind()==MonsterCombatPacket.Kind.BOLT_IMPACT)
            for(Bolt bolt:BOLTS.values())if(bolt.id==packet.sourceId()) {
                bolt.collided=true;bolt.seen=packet.tick();
                // Remove any client extrapolation past the actual server collision.
                while(!bolt.points.isEmpty() && bolt.points.peekLast().tick()>=packet.tick())bolt.points.removeLast();
                bolt.points.add(new Point(packet.tick(),packet.point()));
            }
    }
    static int draw(int detail,Vec3 camera,Minecraft mc,float partial,MonsterCombatEffects.Sink sink) {
        sync(mc);if(level==null)return 0;
        double now=level.getGameTime()+partial;
        BODIES.entrySet().removeIf(e->e.getKey().isRemoved() || e.getKey().isDeadOrDying() || e.getKey().level()!=level);
        for(Entity e:level.entitiesForRendering()) {
            if(e instanceof LivingEntity mob && relevant(mob) && mob.isAlive())BODIES.computeIfAbsent(mob,k->new Body());
            if(e instanceof VoidrangerBoltEntity bolt && e.distanceToSqr(camera)<48*48)track(bolt,now,partial);
        }
        var bodies=new ArrayList<>(BODIES.entrySet());bodies.sort(Comparator.comparingDouble(e->e.getKey().distanceToSqr(camera)));
        int count=0,limit=detail<=10?6:12;
        for(var entry:bodies) {
            LivingEntity mob=entry.getKey();Body b=entry.getValue();
            if(mob.distanceToSqr(camera)>48*48) {b.sections.clear();continue;}
            if(mob instanceof VoidrangerMob v && (v.hurtTime>0 || v.getAttackState()==VoidrangerMob.ATTACK_STAGGER)
                    || mob instanceof StarveinConstructGuardian g && g.getStaggerTicks()>0) {b.sections.clear();continue;}
            while(!b.sections.isEmpty() && now-b.sections.peekFirst().tick()>5)b.sections.removeFirst();
            boolean special=mob instanceof VoidrangerMob v?!(v instanceof DistorterVoidranger) && v.getAttackState()==VoidrangerMob.ATTACK_TELEGRAPH:
                    ((StarveinConstructGuardian)mob).getAttackState()!=StarveinConstructGuardian.ATTACK_NONE;
            if(!special && b.sections.isEmpty())continue;
            if(count++>=limit)continue;
            int color=mob instanceof StarveinConstructGuardian g?MonsterCombatVfx.color(g):PURPLE;
            Section prev=null;
            for(Section sample:b.sections) {
                if(prev!=null)for(int side=0;side<2;side++) {
                    int k=side*2;double fade=Math.pow(Math.max(0,1-(now-sample.tick())/5),1.3);
                    face(sink,color,fade*.5,material(color),sample.tick()*.1,prev.edges()[k],prev.edges()[k+1],sample.edges()[k+1],sample.edges()[k]);
                    stroke(sink,prev.edges()[k],sample.edges()[k],.018,color,fade*.8,camera,now*.1);
                }
                prev=sample;
            }
            if(mob instanceof TramplerVoidranger v && special) {
                double charge=Math.min(1,v.getAttackAnimationPhaseTicks(partial)/28);
                groundRing(sink,v.position(),3.5,.035,.35+charge*.4,detail*3,0,Math.PI*2,mc,PURPLE);
                groundRing(sink,v.position(),1.1-.55*charge,.07,.6,detail*2,0,Math.PI*2,mc,GOLD);
                if(b.core!=null && now-b.captured<2)halo(sink,b.core,.22,charge,PURPLE,detail,camera,now);
            } else if(mob instanceof EliminatorVoidranger v && special && v.hasAttackMark()) {
                double charge=Math.min(1,v.getAttackAnimationPhaseTicks(partial)/24);
                Vec3 target=v.getAttackMark().add(0,.1,0),start=v.position().add(0,v.getBbHeight()*.68,0);
                if(b.core!=null && now-b.captured<2)halo(sink,b.core,.18,charge,PURPLE,detail,camera,now);
                // This marks a shooting direction, never a damaging area disk.
                Vec3 side=target.subtract(start).cross(UP).normalize().scale(.3);
                stroke(sink,target.subtract(side),target.add(side),.015,PALE,.6,camera,charge);
                stroke(sink,target.add(0,-.25,0),target.add(0,.25,0),.015,PALE,.6,camera,charge);
                var hit=level.clip(new ClipContext(start,target,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,mc.player));
                Vec3 end=hit.getType()==HitResult.Type.MISS?target:hit.getLocation();
                for(int i=0;i<8;i++)stroke(sink,start.lerp(end,(i+.15)/8),start.lerp(end,(i+.45)/8),.008,PURPLE,.24+charge*.15,camera,i+charge);
            } else if(mob instanceof StarveinConstructGuardian g && special) {
                double tick=g.getAttackAnimationTicks(partial);
                if(g.getAttackState()==StarveinConstructGuardian.ATTACK_CORE_WAVE && tick<30) {
                    double charge=Math.min(1,tick/30);
                    groundRing(sink,g.getVisualWaveCenter(),g.getVisualWaveRadius(),.045,.4+charge*.35,detail*3,0,Math.PI*2,mc,color);
                    groundRing(sink,g.getVisualWaveCenter(),g.getVisualWaveRadius()-.18,.06,.7,detail*3,0,Math.PI*2*charge,mc,color);
                    if(b.core!=null && now-b.captured<2)halo(sink,b.core,.3,charge,color,detail,camera,now);
                    // Discrete star points keep the shared warning language across path profiles.
                    for(int i=0;i<6;i++) {
                        double a=i*Math.PI/3;Vec3 p=surface(g.getVisualWaveCenter(),Math.cos(a)*.65,Math.sin(a)*.65,mc);
                        if(p!=null)stroke(sink,p,p.add(0,.12+charge*.15,0),.025,color,.6,camera,i);
                    }
                } else if(g.getAttackState()==StarveinConstructGuardian.ATTACK_STAR_TRAIL_SWEEP && tick<16) {
                    // radial() uses cos(X),sin(Z); convert the authoritative facing to that basis.
                    double a=Math.atan2(Math.cos(Math.toRadians(g.getYRot())),-Math.sin(Math.toRadians(g.getYRot())));
                    double half=Math.acos(.2);
                    groundRing(sink,g.position(),4.6,.035,.52,detail*3,a-half,a+half,mc,color);
                    for(double angle:new double[]{a-half,a+half}) {
                        Vec3 end=surface(g.position(),Math.cos(angle)*4.6,Math.sin(angle)*4.6,mc);
                        Vec3 root=surface(g.position(),0,0,mc);
                        if(root!=null&&end!=null)stroke(sink,root,end,.012,color,.35,camera,now*.1);
                    }
                }
            }
        }
        BOLTS.entrySet().removeIf(e->now-e.getValue().seen>6);
        int rendered=0;
        var bolts=new ArrayList<>(BOLTS.values());
        bolts.sort(Comparator.comparingDouble(b->b.points.isEmpty()?Double.MAX_VALUE:b.points.peekLast().position().distanceToSqr(camera)));
        for(Bolt b:bolts) {
            if(rendered++>=(detail<=10?12:32))break;
            Point old=null;
            for(Point p:b.points) {
                if(old!=null) {
                    double fade=Math.max(0,1-(now-p.tick())/6);
                    if(p.position().distanceToSqr(camera)<48*48)stroke(sink,old.position(),p.position(),.035,PURPLE,.65*fade,camera,p.tick()*.1);
                }
                old=p;
            }
        }
        return Math.min(count,limit)+BOLTS.size();
    }
    private static void track(VoidrangerBoltEntity entity,double now,float partial) {
        if(BOLTS.size()>=64 && !BOLTS.containsKey(entity.getUUID())) {
            UUID oldest=BOLTS.entrySet().stream().min(Comparator.comparingDouble(e->e.getValue().seen)).orElseThrow().getKey();
            BOLTS.remove(oldest);
        }
        Bolt bolt=BOLTS.computeIfAbsent(entity.getUUID(),k->new Bolt());bolt.id=entity.getId();
        if(bolt.collided)return;
        Vec3 p=new Vec3(Mth.lerp(partial,entity.xOld,entity.getX()),Mth.lerp(partial,entity.yOld,entity.getY()),Mth.lerp(partial,entity.zOld,entity.getZ()));
        Point last=bolt.points.peekLast();
        if(last!=null && last.position().distanceToSqr(p)>9)bolt.points.clear();
        if(last==null || now-last.tick()>.25)bolt.points.add(new Point(now,p));
        while(bolt.points.size()>16 || !bolt.points.isEmpty() && now-bolt.points.peekFirst().tick()>6)bolt.points.removeFirst();
        bolt.seen=now;
    }
    private static void halo(MonsterCombatEffects.Sink sink,Vec3 center,double radius,double charge,int color,int detail,Vec3 camera,double now) {
        for(int ring=0;ring<2;ring++)for(int i=0;i<detail;i++) {
            double a=i*Math.PI*2/detail+now*.07*(ring==0?1:-1),b=a+Math.PI*1.5/detail;
            Vec3 p=new Vec3(Math.cos(a)*radius,Math.sin(a)*radius,(ring==0?1:-1)*Math.sin(a)*radius*.5);
            Vec3 q=new Vec3(Math.cos(b)*radius,Math.sin(b)*radius,(ring==0?1:-1)*Math.sin(b)*radius*.5);
            stroke(sink,center.add(p),center.add(q),.013,color,.3+charge*.45,camera,a);
        }
    }
    static void burst(MonsterCombatPacket packet,double t,int detail,Vec3 camera,Minecraft mc,MonsterCombatEffects.Sink sink) {
        Vec3 center=packet.point();int color=packet.color();double fade=Math.max(0,1-t/.65);
        switch(packet.kind()) {
            case STOMP,GUARDIAN_WAVE -> {
                double r=packet.radius();
                groundRing(sink,center,r,.065,fade*.8,detail*3,0,Math.PI*2,mc,color);
                groundRing(sink,center,r*Math.max(.02,1-t/.5),.1,fade*.65,detail*3,0,Math.PI*2,mc,color);
                for(int i=0;i<8;i++) {
                    double a=i*Math.PI/4;
                    Vec3 p=surface(center,Math.cos(a)*r*.3,Math.sin(a)*r*.3,mc),q=surface(center,Math.cos(a)*r*.88,Math.sin(a)*r*.88,mc);
                    if(p!=null&&q!=null)stroke(sink,p,q,.025,color,.6*fade,camera,t+i);
                }
                if(packet.kind()==MonsterCombatPacket.Kind.GUARDIAN_WAVE) {
                    halo(sink,center.add(0,.45,0),.3+t*.3,fade,color,detail,camera,level.getGameTime());
                    Entity source=level.getEntity(packet.sourceId());
                    if(source instanceof StarveinConstructGuardian guardian && t<.18) {
                        Body body=BODIES.get(guardian);
                        Vec3 core=body!=null && body.core!=null && level.getGameTime()-body.captured<2
                                ?body.core:guardian.position().add(0,guardian.getBbHeight()*.6,0);
                        // A whole-length flash connects the core to the completed pulse, with no delayed hit front.
                        for(int i=0;i<12;i++) {
                            double f=(double)i/12,g=(double)(i+1)/12;
                            Vec3 p=core.lerp(center.add(0,.1,0),f).add(0,Math.sin(f*Math.PI)*.3,0);
                            Vec3 q=core.lerp(center.add(0,.1,0),g).add(0,Math.sin(g*Math.PI)*.3,0);
                            stroke(sink,p,q,.035,color,(1-t/.18)*.65,camera,i+t);
                        }
                    }
                }
            }
            case GUARDIAN_SWEEP -> {
                double a=Math.atan2(packet.normal().z,packet.normal().x),half=Math.acos(.2);
                groundRing(sink,center,packet.radius(),.065,fade*.75,detail*3,a-half,a+half,mc,color);
            }
            case BOLT_IMPACT -> {
                Vec3 normal=packet.normal().normalize(),side=normal.cross(UP).normalize();
                if(side.lengthSqr()<.1)side=new Vec3(1,0,0);
                Vec3 across=normal.cross(side).normalize();center=center.add(normal.scale(.025));
                for(int i=0;i<8;i++) {
                    double a=i*Math.PI/4;Vec3 dir=side.scale(Math.cos(a)).add(across.scale(Math.sin(a)));
                    stroke(sink,center.add(dir.scale(.04+t*.4)),center.add(dir.scale(.24+t*.55)),.018,PALE,fade*.8,camera,i+t);
                }
            }
            case BOLT_RELEASE -> {
                Vec3 axis=packet.normal().normalize();
                stroke(sink,center,center.add(axis.scale(.45)),.06,PURPLE,Math.max(0,1-t/.18)*.8,camera,t);
            }
            default -> {}
        }
    }
}
