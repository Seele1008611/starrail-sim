package com.starrail.sim.client;

import com.starrail.sim.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.*;

/** First content-three prototypes. All ribbons use the actually rendered native rig pose. */
public final class MonsterCombatEffects {
    @FunctionalInterface interface Sink {
        void face(int color, double alpha, int material, double u, Vec3 a, Vec3 b, Vec3 c, Vec3 d);
    }
    private static final int VIOLET=0x9857f5, LIGHT=0xe4b8ff, DARK=0x49226c, GOLD=0xf3cc68;
    private static final Vec3 UP=new Vec3(0,1,0);
    private record Sample(double tick, Vec3[] edges) {}
    private static final class Body {
        Vec3[] edges,hands;
        double captured=-1000;
        final Deque<Sample> trail=new ArrayDeque<>();
        int previousState=-1;
        float previousPhase;
    }
    private record Burst(MonsterCombatPacket packet) {}
    private static final Map<VoidrangerMob,Body> BODIES=new WeakHashMap<>();
    private static final List<Burst> BURSTS=new ArrayList<>();
    private record GroundKey(int x,int y,int z) {}
    private record Ground(double height,long tick) {}
    private static final Map<GroundKey,Ground> GROUND=new HashMap<>();
    private static ClientLevel level;
    private static int faces,active;
    static int activeEffects() { return active; }
    private MonsterCombatEffects() {}
    private static void sync(Minecraft mc) {
        if(level!=mc.level || mc.player==null) { BODIES.clear();BURSTS.clear();GROUND.clear();ExtraMonsterCombatEffects.clear();active=0;level=mc.level; }
    }
    static boolean relevant(VoidrangerMob mob) {
        return mob instanceof ReaverVoidranger || mob instanceof DistorterVoidranger;
    }
    public static void accept(MonsterCombatPacket packet) {
        Minecraft mc=Minecraft.getInstance();sync(mc);
        if(level==null || mc.player==null || !level.dimension().location().equals(packet.dimension())
                || !finite(packet.point()) || !finite(packet.normal()) || !Double.isFinite(packet.radius()) || packet.radius()<0 || packet.radius()>8 || mc.player.distanceToSqr(packet.point())>48*48) return;
        long age=level.getGameTime()-packet.tick();
        if(age< -5 || age>(packet.kind()==MonsterCombatPacket.Kind.CONFIRMED_HIT?6:16)) return;
        int cap=mc.options.particles().get()==ParticleStatus.MINIMAL?12:32;
        if(BURSTS.size()>=cap) BURSTS.remove(0);
        BURSTS.add(new Burst(packet));
        ExtraMonsterCombatEffects.accept(packet);
        // Small secondary motes, not the primary silhouette or danger boundary.
        int count=mc.options.particles().get()==ParticleStatus.MINIMAL?2:packet.kind()==MonsterCombatPacket.Kind.DISTORTER_PULSE?12:6;
        for(int i=0;i<count;i++) {
            double a=i*2.39996;
            Vec3 p=packet.point();
            level.addParticle(new DustParticleOptions(new Vector3f(((packet.color()>>16)&255)/255f,((packet.color()>>8)&255)/255f,(packet.color()&255)/255f),.7f),
                    p.x+Math.cos(a)*.2,p.y+.12,p.z+Math.sin(a)*.2,Math.cos(a)*.05,.025,Math.sin(a)*.05);
        }
    }
    private static boolean finite(Vec3 v) { return Double.isFinite(v.x)&&Double.isFinite(v.y)&&Double.isFinite(v.z); }

    /** Called after native animation sampling, so shoulders, arms and blade pivots are identical. */
    static void capture(VoidrangerMob mob, BlockbenchRig rig, Matrix4f[] pose, float scale,
                        float partial, float melee) {
        Minecraft mc=Minecraft.getInstance();sync(mc);
        ExtraMonsterCombatEffects.capture(mob,rig,pose,scale,partial,melee);
        if(!relevant(mob))return;
        if(level==null || mob.distanceToSqr(mc.player)>48*48)return;
        Body body=BODIES.computeIfAbsent(mob,k->new Body());
        double now=level.getGameTime()+partial;
        Vec3[] edges=mob instanceof ReaverVoidranger?new Vec3[4]:new Vec3[0];
        Vec3[] hands=new Vec3[2];
        for(var geo:rig.geometry) {
            String name=rig.nodes.get(geo.node()).name();
            int side=name.startsWith("左")?0:1;
            if(name.endsWith("金色刃沿") && edges.length>0) {
                float[] v=geo.vertices(); float lo=Float.POSITIVE_INFINITY,hi=Float.NEGATIVE_INFINITY;
                for(int i=0;i<v.length;i+=8) {lo=Math.min(lo,v[i+1]);hi=Math.max(hi,v[i+1]);}
                Vector3f low=new Vector3f(),high=new Vector3f();int lc=0,hc=0;
                for(int i=0;i<v.length;i+=8) {
                    if(Math.abs(v[i+1]-lo)<.001) {low.add(v[i],v[i+1],v[i+2]);lc++;}
                    if(Math.abs(v[i+1]-hi)<.001) {high.add(v[i],v[i+1],v[i+2]);hc++;}
                }
                edges[side*2]=world(mob,pose[geo.node()].transformPosition(low.div(lc)),scale,partial);
                edges[side*2+1]=world(mob,pose[geo.node()].transformPosition(high.div(hc)),scale,partial);
            }
            if(name.endsWith("握拳") || name.endsWith("拳部")) {
                Vector3f center=new Vector3f();float[] v=geo.vertices();
                for(int i=0;i<v.length;i+=8) center.add(v[i],v[i+1],v[i+2]);
                hands[side]=world(mob,pose[geo.node()].transformPosition(center.div(v.length/8)),scale,partial);
            }
        }
        body.edges=edges;body.hands=hands;body.captured=now;
        int state=mob.getAttackState();float phase=mob.getAttackAnimationPhaseTicks(partial);
        if(mob.hurtTime>0 || mob.isDeadOrDying() || state==VoidrangerMob.ATTACK_STAGGER
                || state==VoidrangerMob.ATTACK_TELEGRAPH && body.previousState!=state
                || phase<body.previousPhase && state==body.previousState) body.trail.clear();
        boolean slash=state==VoidrangerMob.ATTACK_EXECUTION
                || state==VoidrangerMob.ATTACK_IDLE && melee>.03f;
        if(edges.length==4 && Arrays.stream(edges).allMatch(Objects::nonNull) && slash && !mc.isPaused()) {
            Sample last=body.trail.peekLast();
            if(last!=null && last.edges()[0].distanceToSqr(edges[0])>6.25)body.trail.clear();
            if(last==null || now-last.tick()>.3)body.trail.addLast(new Sample(now,edges));
            while(body.trail.size()>14)body.trail.removeFirst();
        }
        body.previousState=state;body.previousPhase=phase;
    }
    static Vec3 world(net.minecraft.world.entity.LivingEntity mob, Vector3f p, float scale, float partial) {
        double yaw=Math.toRadians(Mth.rotLerp(partial,mob.yBodyRotO,mob.yBodyRot));
        double x=p.x*scale,z=p.z*scale;
        return new Vec3(Mth.lerp(partial,mob.xOld,mob.getX())-Math.cos(yaw)*x+Math.sin(yaw)*z,
                Mth.lerp(partial,mob.yOld,mob.getY())+p.y*scale,
                Mth.lerp(partial,mob.zOld,mob.getZ())-Math.sin(yaw)*x-Math.cos(yaw)*z);
    }

    static void draw(int detail, Vec3 camera, Minecraft mc, float partial, Sink sink) {
        sync(mc);if(level==null || mc.player==null)return;
        double now=level.getGameTime()+partial;faces=0;active=0;
        BODIES.entrySet().removeIf(e->e.getKey().isRemoved() || e.getKey().level()!=level || e.getKey().isDeadOrDying());
        // The caster may be outside the camera frustum while its locked danger mark is visible.
        // Discover live states independently of model rendering; only hand/blade anchors need capture.
        for(Entity entity:level.entitiesForRendering())
            if(entity instanceof VoidrangerMob mob && relevant(mob) && mob.isAlive())
                BODIES.computeIfAbsent(mob,k->new Body());
        List<Map.Entry<VoidrangerMob,Body>> bodies=new ArrayList<>(BODIES.entrySet());
        bodies.sort(Comparator.comparingDouble(e->e.getKey().distanceToSqr(camera)));
        int drawn=0,limit=detail<=10?6:12;
        for(var entry:bodies) {
            VoidrangerMob mob=entry.getKey();Body b=entry.getValue();
            if(mob.distanceToSqr(camera)>48*48) {b.trail.clear();continue;}
            if(mob.hurtTime>0) {b.trail.clear();continue;}
            if(now-b.captured>2)b.trail.clear();
            if(mob.getAttackState()!=VoidrangerMob.ATTACK_TELEGRAPH
                    && mob.getAttackState()!=VoidrangerMob.ATTACK_EXECUTION && b.trail.isEmpty())continue;
            if(drawn++>=limit)continue;
            if(mob.hurtTime>0 || mob.getAttackState()==VoidrangerMob.ATTACK_STAGGER)b.trail.clear();
            while(!b.trail.isEmpty() && now-b.trail.peekFirst().tick()>5)b.trail.removeFirst();
            if(mob.getAttackState()==VoidrangerMob.ATTACK_TELEGRAPH
                    || mob.getAttackState()==VoidrangerMob.ATTACK_EXECUTION || !b.trail.isEmpty())active++;
            if(mob instanceof ReaverVoidranger) {
                if(mob.getAttackState()==VoidrangerMob.ATTACK_TELEGRAPH) {
                    double charge=Math.min(1,mob.getAttackAnimationPhaseTicks(partial)/16);
                    if(b.edges!=null && now-b.captured<=2)for(int side=0;side<2;side++) {
                        Vec3 a=b.edges[side*2],c=b.edges[side*2+1];if(a==null||c==null)continue;
                        stroke(sink,a,c,.022+charge*.028,GOLD,.45+charge*.35,camera,now*.09);
                        Vec3 mid=a.lerp(c,.5);
                        orbit(sink,mid,.12+.12*(1-charge),charge,detail,camera,false);
                    }
                }
                Sample previous=null;
                for(Sample s:b.trail) {
                    if(previous!=null)for(int side=0;side<2;side++) {
                        int k=side*2;double fade=Math.pow(Math.max(0,1-(now-s.tick())/5),1.4);
                        face(sink,VIOLET,.48*fade,10,s.tick()*.14,previous.edges()[k],previous.edges()[k+1],s.edges()[k+1],s.edges()[k]);
                        stroke(sink,previous.edges()[k],s.edges()[k],.017,GOLD,.72*fade,camera,s.tick()*.13);
                        stroke(sink,previous.edges()[k+1],s.edges()[k+1],.012,LIGHT,.48*fade,camera,s.tick()*.13);
                    }
                    previous=s;
                }
            } else if(mob.getAttackState()==VoidrangerMob.ATTACK_TELEGRAPH && mob.hasAttackMark()) {
                double charge=Math.min(1,mob.getAttackAnimationPhaseTicks(partial)/30);
                if(b.hands!=null && now-b.captured<=2)
                    for(Vec3 hand:b.hands)if(hand!=null)orbit(sink,hand,.2,charge,detail,camera,true);
                marker(sink,mob.getAttackMark(),charge,detail,camera,mc);
            }
        }
        active += DoomsdayBeastVisuals.draw(detail, camera, mc, partial, sink);
        active+=ExtraMonsterCombatEffects.draw(detail,camera,mc,partial,sink);
        BURSTS.removeIf(b->now-b.packet().tick()>16 || now-b.packet().tick()< -5);
        active+=BURSTS.size();
        for(Burst burst:BURSTS) {
            var p=burst.packet();double t=(now-p.tick())/20;
            if(t<0 || p.point().distanceToSqr(camera)>48*48)continue;
            if(p.kind()==MonsterCombatPacket.Kind.DISTORTER_PULSE) {
                double fade=Math.max(0,1-t/.65);
                // Damage was instantaneous over the entire radius. The rim flashes at full size;
                // the shrinking inner shell is decorative aftermath, not a travelling hit front.
                groundRing(sink,p.point(),2.5,.055,.85*fade,detail*3,0,Math.PI*2,mc,LIGHT);
                double r=Math.max(.05,2.5*(1-t/.5));
                groundRing(sink,p.point(),r,.09,.52*fade,detail*2,0,Math.PI*2,mc,VIOLET);
                for(int i=0;i<6;i++) {
                    double a=i*Math.PI/3+t*1.5;
                    Vec3 ground=surface(p.point(),Math.cos(a)*r,Math.sin(a)*r,mc);
                    if(ground!=null)stroke(sink,ground,ground.add(0,.22+fade*.4,0),.025,VIOLET,fade*.6,camera,t+i);
                }
            } else if(p.kind()==MonsterCombatPacket.Kind.CONFIRMED_HIT) {
                double fade=Math.max(0,1-t/.24),r=.08+t*1.4;
                for(int i=0;i<6;i++) {
                    double a=i*Math.PI/3;
                    Vec3 dir=new Vec3(Math.cos(a),Math.sin(a),Math.sin(a*2)*.35);
                    stroke(sink,p.point().add(dir.scale(r)),p.point().add(dir.scale(r+.15)),.018,material(p.color())==11?p.color():LIGHT,fade*.85,camera,t+i);
                }
            } else ExtraMonsterCombatEffects.burst(p,t,detail,camera,mc,sink);
        }
    }
    private static void marker(Sink sink,Vec3 mark,double charge,int detail,Vec3 camera,Minecraft mc) {
        groundRing(sink,mark,2.5,.035,.45+charge*.3,detail*3,0,Math.PI*2,mc,VIOLET);
        groundRing(sink,mark,2.35,.065,.75,detail*3,-Math.PI/2,-Math.PI/2+Math.PI*2*charge,mc,LIGHT);
        for(int i=0;i<8;i++) {
            double a=i*Math.PI/4;Vec3 p=surface(mark,Math.cos(a)*2.5,Math.sin(a)*2.5,mc);
            Vec3 q=surface(mark,Math.cos(a)*2.27,Math.sin(a)*2.27,mc);
            if(p!=null&&q!=null)stroke(sink,p,q,.025,GOLD,.75,camera,i);
        }
        // Four broken inward brackets rather than an opaque filled danger disk.
        for(int i=0;i<4;i++) {
            double a=i*Math.PI/2+Math.PI/4;
            Vec3 p=surface(mark,Math.cos(a)*.4,Math.sin(a)*.4,mc);
            Vec3 q=surface(mark,Math.cos(a)*.7,Math.sin(a)*.7,mc);
            if(p!=null&&q!=null)stroke(sink,p,q,.025,LIGHT,.6,camera,i);
        }
    }
    private static void orbit(Sink sink,Vec3 center,double radius,double charge,int detail,Vec3 camera,boolean cast) {
        int n=Math.max(10,detail);double time=(level.getGameTime()+Minecraft.getInstance().getFrameTime())/20;
        for(int layer=0;layer<2;layer++)for(int i=0;i<n;i++) {
            double a=(double)i/n*Math.PI*2+time*(layer==0?2:-2),b=a+Math.PI*1.5/n;
            Vec3 u=new Vec3(Math.cos(a)*radius,Math.sin(a)*radius,Math.sin(a)*radius*.35);
            Vec3 v=new Vec3(Math.cos(b)*radius,Math.sin(b)*radius,Math.sin(b)*radius*.35);
            if(layer==1){u=new Vec3(u.x,u.z,u.y);v=new Vec3(v.x,v.z,v.y);}
            stroke(sink,center.add(u),center.add(v),cast?.012:.009,layer==0?VIOLET:GOLD,.3+charge*.4,camera,a);
        }
    }
    static Vec3 surface(Vec3 center,double dx,double dz,Minecraft mc) {
        Vec3 top=center.add(dx,.6,dz),bottom=center.add(dx,-1.1,dz);
        GroundKey key=new GroundKey(Mth.floor(top.x*8),Mth.floor(top.y*8),Mth.floor(top.z*8));
        Ground cached=GROUND.get(key);long tick=level.getGameTime();
        if(cached==null || tick-cached.tick()>4) {
            var hit=level.clip(new ClipContext(top,bottom,ClipContext.Block.COLLIDER,ClipContext.Fluid.ANY,mc.player));
            double height=hit.getType()==HitResult.Type.MISS || hit.getDirection()!=net.minecraft.core.Direction.UP
                    ?Double.NaN:hit.getLocation().y+.028;
            cached=new Ground(height,tick);
            if(GROUND.size()>=4096)GROUND.clear();
            GROUND.put(key,cached);
        }
        return Double.isNaN(cached.height())?null:new Vec3(top.x,cached.height(),top.z);
    }
    static void groundRing(Sink sink,Vec3 center,double radius,double width,double alpha,int count,
                                   double start,double end,Minecraft mc,int color) {
        for(int i=0;i<count;i++) {
            double a=start+(end-start)*i/count,b=start+(end-start)*(i+1)/count;
            Vec3 p=surface(center,Math.cos(a)*(radius-width),Math.sin(a)*(radius-width),mc);
            Vec3 q=surface(center,Math.cos(a)*(radius+width),Math.sin(a)*(radius+width),mc);
            Vec3 r=surface(center,Math.cos(b)*(radius+width),Math.sin(b)*(radius+width),mc);
            Vec3 s=surface(center,Math.cos(b)*(radius-width),Math.sin(b)*(radius-width),mc);
            if(p!=null&&q!=null&&r!=null&&s!=null && Math.abs(p.y-r.y)<.55)
                face(sink,color,alpha,material(color),a,p,q,r,s);
        }
    }
    static int material(int color) {
        return switch(color) {case 0x64e1e5,0x80e9c5,0x8ad9ed,0xa8c5ff,0xc8fff5 -> 11;default -> 10;};
    }
    static void stroke(Sink sink,Vec3 a,Vec3 b,double width,int color,double alpha,Vec3 camera,double u) {
        Vec3 normal=a.lerp(b,.5).subtract(camera).normalize();
        Vec3 side=b.subtract(a).cross(normal).normalize().scale(width);
        if(side.lengthSqr()<1e-9)side=UP.scale(width);
        face(sink,color,alpha,material(color),u,a.subtract(side),a.add(side),b.add(side),b.subtract(side));
    }
    static void face(Sink sink,int color,double alpha,int material,double u,Vec3 a,Vec3 b,Vec3 c,Vec3 d) {
        if(alpha<.003 || faces>=6000 || !finite(a)||!finite(b)||!finite(c)||!finite(d))return;
        faces++;sink.face(color,Math.min(.9,alpha),material,u,a,b,c,d);
    }
}
