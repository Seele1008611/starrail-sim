package com.starrail.sim.client;

import com.mojang.blaze3d.vertex.*;
import com.starrail.sim.CombatVfxPacket;
import com.starrail.sim.StarRailPath;
import com.starrail.sim.StarRailSimMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import java.util.*;
import java.util.function.DoubleFunction;

/** Server-confirmed combat visuals. All geometry is local to one interpolated entity anchor. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, value = Dist.CLIENT)
public final class StarRailCombatMesh {
    private static final Vec3 UP = new Vec3(0, 1, 0), X = new Vec3(1, 0, 0), Z = new Vec3(0, 0, 1);
    private static final List<Effect> EFFECTS = new ArrayList<>();
    private static final List<Face> FACES = new ArrayList<>();
    private static final MultiBufferSource.BufferSource BUFFERS = MultiBufferSource.immediate(new BufferBuilder(262144));
    private static ClientLevel activeLevel;
    private static long clock;
    private static Matrix4f worldViewPose;
    private record Face(Vec3 a, Vec3 b, Vec3 c, Vec3 d, int color, float alpha, double distance) {}
    private record Palette(int main, int shade, int accent) {}
    private static final class Effect {
        final CombatVfxPacket packet;
        final long start = clock;
        final Vec3 forward, side;
        Vec3 anchor;
        double bodyHeight;
        Effect(CombatVfxPacket packet) {
            this.packet = packet;
            anchor = packet.impact();
            bodyHeight=packet.impact().y-packet.floorY();
            Vec3 direction = packet.impact().subtract(packet.origin());
            Vec3 flat = new Vec3(direction.x, 0, direction.z).normalize();
            forward = flat.lengthSqr() < .01 ? Z : flat;
            side = forward.cross(UP).normalize();
        }
        Vec3 world(Vec3 local) {
            return anchor.add(side.scale(local.x)).add(0, local.y, 0).add(forward.scale(local.z));
        }
        Vec3 local(Vec3 world) {
            Vec3 delta = world.subtract(anchor);
            return new Vec3(delta.dot(side), delta.y, delta.dot(forward));
        }
    }
    /** Unlit colour geometry, ordinary alpha blending, world depth; no additive white bloom. */
    private static final class MeshType extends RenderType {
        private MeshType() {
            super("starrail_combat_mesh", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
                262144, false, false, () -> {}, () -> {});
        }
        static final RenderType TYPE = create("starrail_combat_mesh", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 262144, false, false,
            CompositeState.builder().setShaderState(POSITION_COLOR_SHADER)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL)
                .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE)
                .setOutputState(MAIN_TARGET).createCompositeState(false));
    }
    private StarRailCombatMesh() {}
    private static double clamp(double v) { return Math.max(0, Math.min(1, v)); }
    private static double ease(double v) { v = clamp(v); return v * v * (3 - 2 * v); }
    private static Vec3 v(double x, double y, double z) { return new Vec3(x, y, z); }
    private static Vec3 radial(double theta, double radius, double y) { return v(Math.cos(theta)*radius,y,Math.sin(theta)*radius); }
    private static boolean finite(Vec3 v) { return Double.isFinite(v.x) && Double.isFinite(v.y) && Double.isFinite(v.z); }
    private static Vec3 interpolate(Entity entity, float partial) {
        // Match LevelRenderer.renderEntity rather than the tick-history xo/yo/zo fields.
        return v(entity.xOld + (entity.getX()-entity.xOld)*partial,
                 entity.yOld + (entity.getY()-entity.yOld)*partial, entity.zOld + (entity.getZ()-entity.zOld)*partial);
    }
    private static Palette palette(StarRailPath path) {
        return switch(path) {
            case HUNT -> new Palette(0x67cef2,0x327caa,0xa8eaff);
            case PRESERVATION -> new Palette(0xedb64e,0x986322,0xd78d35);
            case ABUNDANCE -> new Palette(0x77d99b,0x39876a,0xd7eaa2);
            case DESTRUCTION -> new Palette(0xfa795d,0xa7413f,0xffb34e);
            case ERUDITION -> new Palette(0xbd87f5,0x7151a0,0xdabdff);
            case NIHILITY -> new Palette(0x956ae1,0x44365d,0x7150ae);
            case HARMONY -> new Palette(0xee97ca,0x995b8d,0xf0d18c);
            case REMEMBRANCE -> new Palette(0x8addf0,0x4b8fae,0xb6d3ff);
            case ELATION -> new Palette(0xff82b7,0xb84d85,0xffc77a);
            default -> new Palette(0xb9d4e6,0x667b99,0xe1eef5);
        };
    }
    public static void accept(CombatVfxPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        sync(mc);
        if(mc.level == null || mc.player == null || !finite(packet.origin()) || !finite(packet.impact())
            || !Double.isFinite(packet.floorY()) || Math.abs(packet.impact().y-packet.floorY())>32
            || mc.player.distanceToSqr(packet.impact())>48*48) return;
        int limit=mc.options.particles().get()==ParticleStatus.MINIMAL?12:24;
        if(EFFECTS.size()>=limit) {
            if(packet.kind()==CombatVfxPacket.Kind.TOUGHNESS_HIT)return;
            int replace=0;
            for(int i=0;i<EFFECTS.size();i++)if(EFFECTS.get(i).packet.kind()==CombatVfxPacket.Kind.TOUGHNESS_HIT) {replace=i;break;}
            EFFECTS.remove(replace);
        }
        EFFECTS.add(new Effect(packet));
    }
    private static void sync(Minecraft mc) {
        if(mc.level!=activeLevel || mc.player==null) { EFFECTS.clear(); FACES.clear(); worldViewPose=null; activeLevel=mc.level;clock=0; }
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        Minecraft mc=Minecraft.getInstance();sync(mc);
        if(mc.level==null||mc.isPaused())return;
        clock++;
        EFFECTS.removeIf(effect->clock-effect.start>50);
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent event) {
        // AFTER_LEVEL supplies the projection/bobbing stack in this Forge build.
        // Capture the actual entity view pose and composite with it after the world.
        if(event.getStage()==RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            sync(Minecraft.getInstance());
            worldViewPose=new Matrix4f(event.getPoseStack().last().pose());
            return;
        }
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_LEVEL)return;
        Matrix4f matrix=worldViewPose;
        worldViewPose=null;
        Minecraft mc=Minecraft.getInstance();sync(mc);
        if(mc.level==null||mc.player==null||EFFECTS.isEmpty()||matrix==null)return;
        float partial=mc.isPaused()?0:event.getPartialTick();
        Vec3 camera=event.getCamera().getPosition();
        ParticleStatus quality=mc.options.particles().get();
        int detail=quality==ParticleStatus.MINIMAL?10:quality==ParticleStatus.DECREASED?16:24;
        FACES.clear();
        for(Effect effect:EFFECTS) {
            Entity target=mc.level.getEntity(effect.packet.targetId());
            if(target!=null&&!target.isRemoved()) {
                effect.bodyHeight=target.getBbHeight()*.55;
                effect.anchor=interpolate(target,partial).add(0,effect.bodyHeight,0);
            }
            if(effect.anchor.distanceToSqr(camera)>48*48)continue;
            new Scene(effect,(clock-effect.start+partial)/20D,detail,camera,mc,partial).draw();
        }
        FACES.sort(Comparator.comparingDouble(Face::distance).reversed());
        PoseStack poses=event.getPoseStack();poses.pushPose();
        try {
            VertexConsumer vertices=BUFFERS.getBuffer(MeshType.TYPE);
            for(Face face:FACES) {
                vertex(vertices,matrix,face.a,camera,face.color,face.alpha);
                vertex(vertices,matrix,face.b,camera,face.color,face.alpha);
                vertex(vertices,matrix,face.c,camera,face.color,face.alpha);
                vertex(vertices,matrix,face.d,camera,face.color,face.alpha);
            }
        } finally { BUFFERS.endBatch(MeshType.TYPE);poses.popPose();FACES.clear(); }
    }
    private static void vertex(VertexConsumer out, Matrix4f pose, Vec3 point, Vec3 camera,int color,float alpha) {
        float near=(float)clamp((point.distanceTo(camera)-.15)/.5);
        Vec3 relative=point.subtract(camera);
        out.vertex(pose,(float)relative.x,(float)relative.y,(float)relative.z)
            .color(((color>>16)&255)/255F,((color>>8)&255)/255F,(color&255)/255F,alpha*near).endVertex();
    }
    private static final class Scene {
        final Effect effect;
        final CombatVfxPacket packet;
        final Palette p;
        final double age;
        final int detail;
        final boolean strong;
        final Vec3 camera, c=Vec3.ZERO, foot, source, sourceFoot, incoming;
        int faces;
        Scene(Effect effect,double age,int detail,Vec3 camera,Minecraft mc,float partial) {
            this.effect=effect;this.packet=effect.packet;this.age=age;this.detail=detail;this.camera=camera;
            p=palette(packet.path());strong=packet.kind().strong();
            foot=v(0,-effect.bodyHeight+.06,0);
            Entity owner=mc.level.getEntity(packet.sourceId());
            Vec3 from=packet.origin();
            if(owner!=null&&!owner.isRemoved())from=interpolate(owner,partial).add(0,owner.getBbHeight()*.55,0);
            source=packet.targetId()==packet.sourceId()?c:effect.local(from);
            sourceFoot=owner==null?source.add(0,foot.y,0):effect.local(interpolate(owner,partial).add(0,.06,0));
            // Keep the confirmed incoming direction independent of moving source/target anchors.
            Vec3 direction=packet.impact().subtract(packet.origin()).normalize();
            incoming=v(direction.dot(effect.side),direction.y,direction.dot(effect.forward));
        }
        void face(int color,double alpha,double birth,double life,Vec3... points) {
            double t=age-birth;
            if(t<0||t>=life||points.length<3||faces>=6000||FACES.size()>=48000)return;
            boolean hit=packet.kind()==CombatVfxPacket.Kind.TOUGHNESS_HIT;
            // All themes share stronger coloured silhouettes; low-alpha shell fills stay airy.
            double gain=hit?1.2:alpha<=.18?1.2:1.4;
            double fadeOut=Math.min(hit?.1:.16,life*.3);
            float opacity=(float)(Math.min(.94,alpha*gain)*ease(t/.045)*ease((life-t)/fadeOut));
            if(opacity<.002)return;
            Vec3 a=effect.world(points[0]),b=effect.world(points[1]),c=effect.world(points[2]),d=effect.world(points.length>3?points[3]:points[2]);
            double distance=a.add(b).add(c).add(d).scale(.25).distanceToSqr(camera);
            FACES.add(new Face(a,b,c,d,color,opacity,distance));faces++;
        }
        void line(List<Vec3> points,Vec3 normal,double width,int color,double alpha,double birth,double life) {
            if(age<birth||age>birth+life+.15)return;
            // Strengthen fine tracks without widening solid panels or scaling the whole effect.
            double lineWidth=width*(packet.kind()==CombatVfxPacket.Kind.TOUGHNESS_HIT?1.1:
                packet.path()==StarRailPath.HUNT?1:1.25);
            for(int i=0;i<points.size()-1;i++) {
                Vec3 a=points.get(i),b=points.get(i+1),tangent=b.subtract(a).normalize();
                Vec3 across=tangent.cross(normal).normalize();
                if(across.lengthSqr()<.01)across=tangent.cross(X).normalize();
                if(across.lengthSqr()<.01)across=tangent.cross(UP).normalize();
                double f0=i/(double)(points.size()-1),f1=(i+1D)/(points.size()-1);
                Vec3 wa=across.scale(lineWidth*(.22+.78*Math.sin(f0*Math.PI))),wb=across.scale(lineWidth*(.22+.78*Math.sin(f1*Math.PI)));
                face(color,alpha,birth+f0*.12,life,a.add(wa),a.subtract(wa),b.subtract(wb),b.add(wb));
            }
        }
        void line(double width,int color,double alpha,double birth,double life,Vec3 normal,Vec3... points) {
            line(Arrays.asList(points),normal,width,color,alpha,birth,life);
        }
        void curve(Vec3 root,DoubleFunction<Vec3> fn,Vec3 normal,double width,int color,double alpha,double birth,double life) {
            if(age<birth||age>birth+life+.15)return;
            List<Vec3> points=new ArrayList<>();
            for(int i=0;i<=detail;i++)points.add(root.add(fn.apply(i/(double)detail)));
            line(points,normal,width,color,alpha,birth,life);
        }
        void orbit(Vec3 root,double radius,double y,double birth,double life,int color,double alpha,double turn,double span) {
            curve(root,f->radial(turn+f*span,radius,y),UP,.022,color,alpha,birth,life);
        }
        void gem(Vec3 root,double size,int color,double birth,double life,double turn) {
            Vec3 top=root.add(0,size*1.5,0),bottom=root.add(0,-size*1.5,0);
            for(int i=0;i<4;i++) {
                Vec3 a=root.add(radial(turn+i*Math.PI/2,size,0)),b=root.add(radial(turn+(i+1)*Math.PI/2,size,0));
                face(color,.5,birth,life,top,a,b);face(p.shade,.45,birth,life,bottom,b,a);
            }
        }
        void leaf(Vec3 root,Vec3 axis,double width,int color,double birth,double life) {
            Vec3 delta=axis.scale(ease((age-birth)/.3)),tip=root.add(delta),mid=root.add(delta.scale(.55));
            Vec3 across=axis.cross(Z).normalize();if(across.lengthSqr()<.01)across=X;
            Vec3 l=mid.add(across.scale(width)),r=mid.subtract(across.scale(width));
            face(color,.3,birth,life,root,l,tip);face(p.shade,.32,birth,life,root,tip,r);
            line(.012,p.accent,.45,birth,life,across.cross(axis),root,tip);
        }
        void diamond(Vec3 root,Vec3 side,double size,int color,double birth,double life) {
            Vec3 top=root.add(0,size,0),bottom=root.add(0,-size,0),left=root.subtract(side.scale(size*.45)),right=root.add(side.scale(size*.45));
            face(color,.32,birth,life,top,left,bottom);face(p.shade,.42,birth,life,top,bottom,right);
            line(.012,p.accent,.45,birth,life,side.cross(UP),top,left,bottom,right,top);
        }
        void draw() {
            if(packet.kind()==CombatVfxPacket.Kind.TOUGHNESS_HIT) { toughness(false);return; }
            if(packet.kind()==CombatVfxPacket.Kind.TOUGHNESS_BREAK) { toughness(true);return; }
            if(packet.path()==StarRailPath.HUNT) { hunt();return; }
            entry();
            switch(packet.path()) {
                case PRESERVATION -> preservation();
                case DESTRUCTION -> destruction();
                case ERUDITION -> erudition();
                case NIHILITY -> nihility();
                case HARMONY -> harmony();
                case ABUNDANCE -> abundance();
                case REMEMBRANCE -> remembrance();
                case ELATION -> elation();
                default -> { }
            }
            secondary();
        }
        boolean self() {
            if(packet.targetId()==packet.sourceId())return true;
            return switch(packet.kind()) {
                case PRESERVATION_WALL, DESTRUCTION_RAGE, HARMONY_RESONANCE, HARMONY_CONCERT,
                     HARMONY_UNISON, ABUNDANCE_HEAL, ABUNDANCE_MANNA, ABUNDANCE_MERCY -> true;
                default -> false;
            };
        }
        void entry() {
            if(self())return;
            Vec3 dir=packet.ranged()?incoming:c.subtract(source).normalize();if(dir.lengthSqr()<.01)dir=Z;
            Vec3 flight=dir;
            if(packet.ranged()) {
                arrow(c.subtract(flight.scale(1.1*(1-ease(age/.2)))),flight,.42,0,.45,false);
                for(int k=0;k<(strong?3:2);k++) {
                    final int layer=k;
                    curve(c,f->flight.scale(-(1-f)*1.7).add(Math.sin(f*Math.PI)*(layer+1)*.08,
                        Math.sin(f*Math.PI*2+layer)*.1,0),UP,.018,p.main,.4,k*.035,.48);
                }
            } else for(int k=0;k<(strong?3:2);k++) {
                final int layer=k;
                curve(c,f->{double angle=-1.1+f*2.2+layer*.16;
                    return v(Math.sin(angle)*(.7+layer*.09),Math.cos(angle)*.35-.12,Math.cos(angle)*.35);
                },UP,.022,p.main,.4,.025+k*.045,.48);
            }
        }
        Vec3 arrowPoint(Vec3 tip,Vec3 dir,Vec3 side,Vec3 depth,double size,double length,double x,double z) {
            return tip.add(dir.scale(length*size)).add(side.scale(x*size)).add(depth.scale(z*size));
        }
        void arrow(Vec3 tip,Vec3 dir,double size,double birth,double life,boolean central) {
            Vec3 reference=Math.abs(dir.y)>.8?X:UP,depth=dir.cross(reference).normalize(),side=depth.cross(dir).normalize();
            Vec3 apex=arrowPoint(tip,dir,side,depth,size,.08,0,0);
            Vec3[] shoulders=new Vec3[4];
            for(int i=0;i<4;i++)shoulders[i]=arrowPoint(tip,dir,side,depth,size,-.57,Math.cos(i*Math.PI/2)*.23,Math.sin(i*Math.PI/2)*.23);
            for(int i=0;i<4;i++) {
                face(i%2==0?p.main:p.shade,central?.82:.66,birth,life,apex,shoulders[i],shoulders[(i+1)%4]);
                line(central?.014:.01,p.accent,central?.8:.42,birth,life,i%2==0?depth:side,shoulders[i],apex);
                double angle=i*Math.PI/2;
                Vec3 a=arrowPoint(tip,dir,side,depth,size,-1.78,Math.cos(angle)*.045,Math.sin(angle)*.045);
                Vec3 b=arrowPoint(tip,dir,side,depth,size,-2.22,Math.cos(angle)*.3,Math.sin(angle)*.3);
                Vec3 d=arrowPoint(tip,dir,side,depth,size,-2.05,Math.cos(angle)*.045,Math.sin(angle)*.045);
                face(i%2==0?p.main:p.shade,central?.62:.4,birth,life,a,b,d);
                line(.012*size,p.accent,central?.65:.35,birth,life,i%2==0?depth:side,b,a);
            }
            Vec3 tail=tip.subtract(dir.scale(2.35*size)),shaft=tip.subtract(dir.scale(.56*size));
            line(.062*size,p.main,central?.82:.65,birth,life,side,tail,shaft);
            line(.042*size,p.main,central?.72:.5,birth,life,depth,tail,shaft);
            if(central) {
                line(.012*size,p.accent,.86,birth,life,side,tail,shaft);
                for(int k=0;k<3;k++)for(int i=0;i<4;i++) {
                    double x=Math.cos(i*Math.PI/2),z=Math.sin(i*Math.PI/2),s=-.85-k*.4;
                    line(.013*size,p.accent,.55,birth+k*.02,life-k*.02,i%2==0?depth:side,
                        arrowPoint(tip,dir,side,depth,size,s-.13,x*.08,z*.08),
                        arrowPoint(tip,dir,side,depth,size,s,x*.13,z*.13),
                        arrowPoint(tip,dir,side,depth,size,s+.13,x*.08,z*.08));
                }
            }
        }
        void fall(Vec3 root,double birth,double size,boolean central,Vec3 offset) {
            double duration=central?.32:.27,hit=birth+duration,local=age-birth;
            huntImpact(root,hit,central);
            if(local<0||local>duration+(central?.48:.22))return;
            double t=clamp(local/duration);
            Vec3 tip=root.add(offset.scale(1-t*t)),dir=offset.scale(-1).normalize();
            Vec3 depth=dir.cross(Math.abs(dir.y)>.8?X:UP).normalize(),side=depth.cross(dir).normalize();
            arrow(tip,dir,size,birth,duration+(central?.43:.19),central);
            for(int sign:new int[]{-1,1}) {
                Vec3 shift=depth.scale(sign*.055*size);
                line(central?.03:.02,p.main,central?.55:.4,birth,duration+.18,depth,
                    tip.add(shift).subtract(dir.scale(.38)),tip.add(shift).subtract(dir.scale(central?2.8:1.65)));
                curve(tip,f->dir.scale(-(.3+f*(central?2.45:1.35)))
                    .add(depth.scale(sign*Math.sin(f*Math.PI)*(central?.25:.12)))
                    .add(side.scale(Math.sin(f*Math.PI*2)*.045)),depth,central?.018:.011,p.main,central?.4:.3,birth+.025,duration+.16);
            }
            for(int i=0;i<(central?12:5);i++) {
                double angle=i*2.39996,spread=central?.14:.075;
                Vec3 rootSpark=tip.subtract(dir.scale(.32+i*(central?.15:.13)))
                    .add(side.scale(Math.cos(angle)*spread)).add(depth.scale(Math.sin(angle)*spread));
                gem(rootSpark,i%3==0?.021:.012,i%3==0?p.accent:p.main,birth+i*.015,duration+.17,angle);
            }
            double burst=clamp((age-hit)/.24);
            for(int i=0;i<(central?8:3);i++) {
                double angle=i*Math.PI*2/(central?8:3);
                Vec3 back=dir.scale(-(.15+burst*.6)).add(side.scale(Math.cos(angle)*burst*.22)).add(depth.scale(Math.sin(angle)*burst*.22));
                line(.014,p.main,.45,hit,.28,depth,root.add(back.scale(.55)),root.add(back));
            }
        }
        void hunt() {
            int count=strong?8:4;
            double centralBirth=strong?1.05:.65;
            for(int i=0;i<count;i++) {
                int index=i%2==0?i/2:count/2+(i-1)/2;
                double angle=index*Math.PI*2/count+.32,birth=.04+i*(strong?.085:.1);
                Vec3 offset=radial(angle,strong?2.05:1.75,2.2),start=c.add(offset),tangent=radial(angle+Math.PI/2,1,0);
                for(int sign:new int[]{-1,1})line(.018,p.main,.48,Math.max(0,birth-.04),.22,offset.normalize(),
                    start.add(tangent.scale(sign*.22)),start.add(tangent.scale(sign*.13)).add(0,.1,0),start.add(tangent.scale(sign*.06)));
                fall(c,birth,(strong?.43:.38)*1.5,false,offset);
                double t=ease((age-birth-.27)/.22);
                Vec3 head=c.add(offset.scale(.24*(1-t)));
                line(.018,p.main,.32,birth+.27,.24,offset.cross(UP).normalize(),head,head.add(offset.scale(.1*(1-t))));
            }
            if(packet.ranged()) {
                Vec3 dir=incoming;if(dir.lengthSqr()<.01)dir=Z;
                arrow(c.subtract(dir.scale(1.7*(1-ease(age/.2)))),dir,.5,0,.45,false);
            }
            double charge=centralBirth-.22;
            gem(foot.add(0,2.55,0),.07,p.accent,centralBirth-.17,.25,0);
            for(int layer=0;layer<2;layer++)for(int i=0;i<6;i++) {
                double phase=clamp((age-charge)/.3),theta=i*Math.PI/3+layer*.25+phase*.22;
                double radius=(.5+layer*.18)*(1-phase*.35),height=2.65+layer*.35;
                orbit(foot,radius,height,charge+layer*.03,.42,layer==0?p.main:p.shade,.48,theta,.38);
                line(.012,p.accent,.4,charge+.04,.35,UP,foot.add(radial(theta,radius,height)),foot.add(radial(theta,radius*.65,height-.16)));
            }
            for(int i=0;i<4;i++)for(int segment=0;segment<3;segment++) {
                double theta=i*Math.PI/2+.3,radius=.25+segment*.045,birth=centralBirth+.04+segment*.055;
                double y=2.65*(1-ease((age-birth)/.3))+segment*.12;
                Vec3 root=foot.add(radial(theta,radius,y));
                line(.019,segment==1?p.accent:p.main,segment==1?.48:.35,birth,.48,radial(theta+Math.PI/2,1,0),root,root.add(0,.32,0));
            }
            fall(foot,centralBirth,strong?1.05:.76,true,v(0,2.9,0));
        }
        void huntImpact(Vec3 root,double birth,boolean central) {
            double elapsed=age-birth;
            if(elapsed<0||elapsed>(central?.85:.4))return;
            double expansion=ease(elapsed/.48),radius=(central?1.35:.34)*expansion;
            int count=central?8:4;
            for(int i=0;i<count;i++) {
                double theta=i*Math.PI*2/count;
                orbit(root,radius,.012,birth,.6,p.main,central?.64:.4,theta,.32);
                line(.019,p.accent,central?.54:.3,birth,.42,UP,root.add(radial(theta,radius*.7,0)),root.add(radial(theta,radius,.08)));
                gem(root.add(radial(theta,radius*.8,Math.sin(expansion*Math.PI)*.3)),central?.033:.018,p.main,birth+i*.012,.5,theta);
            }
            if(!central)return;
            for(int layer=0;layer<2;layer++)for(int i=0;i<8;i++) {
                double radius2=.3+ease((elapsed-layer*.075)/.6)*1.65;
                orbit(root,radius2,.018+layer*.025,birth+layer*.075,.8,layer==0?p.main:p.shade,layer==0?.52:.36,i*Math.PI/4+layer*.2,.22);
            }
            int fragments=detail<=10?10:strong?24:14;
            for(int i=0;i<fragments;i++) {
                double theta=i*2.39996,t=ease(elapsed/.65),reach=(.55+i%5*.19)*t;
                Vec3 base=root.add(radial(theta,reach,Math.sin(t*Math.PI)*(.28+i%4*.13)));
                Vec3 spine=radial(theta,.06,.12),edge=radial(theta+Math.PI/2,.035,0);
                face(i%3==0?p.shade:p.main,.52,birth+i*.006,.78,base,base.add(spine),base.add(edge));
                line(.012,p.main,.35,birth+.05,.7,Z,base,base.subtract(spine.scale(.65)));
            }
            for(int i=0;i<4;i++) {
                double theta=i*Math.PI/2+.3,t=ease(elapsed/.7);
                Vec3 center=root.add(radial(theta,.35+t*.55,.35+Math.sin(t*Math.PI)*.65)),side=radial(theta+Math.PI/2,1,0);
                line(.019,p.accent,.42,birth+.11,.68,radial(theta,1,0),center.subtract(side.scale(.08)),center.add(0,.18,0),center.add(side.scale(.08)));
                line(.027,p.main,.55,birth,.5,side,root.add(radial(theta,.09,0)),root.add(radial(theta,.3+expansion*.25,.65*(1-expansion))),root.add(radial(theta,.48+expansion*.35,.14)));
            }
        }
        void toughness(boolean broken) {
            if(!broken) {
                for(int sign:new int[]{-1,1})line(.025,p.main,.65,0,.35,Z,v(sign*.24,-.14,0),v(sign*.38,0,0),v(sign*.24,.14,0));
                Vec3 dir=packet.ranged()?incoming:v(.75,-.45,.25).normalize();
                for(int i=0;i<4;i++)gem(c.add(radial(i*Math.PI/2,.24+clamp(age/.3)*.18,0)),.014,p.main,.025+i*.01,.26,i);
                for(int i=0;i<3;i++) {
                    Vec3 root=dir.scale(.1+clamp(age/.24)*.24).add(0,(i-1)*.065,0);
                    line(.011,p.accent,.45,.015+i*.012,.22,UP,root,root.add(dir.scale(.1)));
                }
                return;
            }
            for(int i=0;i<8;i++) {
                double theta=i*Math.PI/4,dt=Math.PI/10;
                line(.026,p.main,.55,0,.28,radial(theta+Math.PI/2,1,0),foot.add(radial(theta,.56,.3)),foot.add(radial(theta,.65,.85)),foot.add(radial(theta,.5,1.35)));
                face(p.main,.13,0,.26,foot.add(radial(theta-dt,.62,.3)),foot.add(radial(theta+dt,.62,.3)),foot.add(radial(theta,.68,1.38)));
                double t=ease((age-.2)/.65),radius=.72+t*.95;
                Vec3 center=foot.add(radial(theta,radius,.75+i%3*.28+t*.25));
                diamond(center,radial(theta+Math.PI/2,1,0),.19*(1-t*.6),p.main,.18+i*.012,.9);
                gem(foot.add(radial(theta,.65+clamp((age-.18)/.65)*.9,.5+i%3*.4)),.09*(1-t*.5),p.main,.18+i*.014,.85,theta+age);
                orbit(foot,.7+ease((age-.2)/.65)*1.25,.025,.28+i*.012,.9,p.main,.4,theta,.3);
            }
            for(int i=0;i<12;i++) {
                double theta=i*Math.PI/6,t=ease((age-.32)/.75);
                Vec3 center=foot.add(radial(theta,.65+t*1.25,.4+i%4*.22+Math.sin(t*Math.PI)*.35));
                Vec3 side=radial(theta+Math.PI/2,.045*(1-t*.4),0);
                face(i%2==0?p.main:p.shade,.4,.3+i*.008,.95,center.add(side),center.subtract(side),center.add(radial(theta,.1,.16)));
                line(.012,p.main,.32,.32+i*.008,.85,UP,center,center.subtract(radial(theta,.14,0)));
            }
            for(int i=0;i<4;i++)orbit(foot,.4+clamp(age)*1.45+i*.07,.01,.22+i*.055,.8,p.main,.34,i*Math.PI/3,Math.PI*1.1);
        }
        void preservation() {
            boolean counter=packet.kind()==CombatVfxPacket.Kind.PRESERVATION_COUNTER;
            Vec3 owner=counter?source:c,base=counter?sourceFoot:foot;
            for(int sign:new int[]{-1,1})line(.08,p.main,.65,0,.95,Z,
                owner.add(0,.75,0),owner.add(sign*.54,.45,0),owner.add(sign*.5,-.38,0),owner.add(0,-.78,0));
            double grow=ease((age-.14)/.45);
            for(int i=0;i<8;i++) {
                double theta=i*Math.PI/4;
                Vec3 dir=radial(theta,1,0),side=radial(theta+Math.PI/2,1,0),center=owner.add(dir.scale(.95));
                diamond(center,side,.18,p.main,.08+i*.028,.85);
                line(.019,p.main,.5,.2+i*.025,.9,dir,center.add(0,-.24,0),center.add(0,-.38,0));
                for(int k=0;k<2;k++)orbit(owner,1.13+k*.12,-.65+k*1.2,.7+k*.1+i*.012,.95,p.main,.42,theta,.3);
            }
            for(int i=0;i<6;i++) {
                double theta=i*Math.PI/3;
                Vec3 dir=radial(theta,1,0),side=radial(theta+Math.PI/2,1,0),center=base.add(dir.scale(1.27));
                line(.024,p.main,.46,.26+i*.04,1.03,dir,center.subtract(side.scale(.36)),center.subtract(side.scale(.36)).add(0,.6*grow,0),
                    center.subtract(side.scale(.2)).add(0,.6*grow,0),center.subtract(side.scale(.2)).add(0,.82*grow,0),
                    center.add(side.scale(.2)).add(0,.82*grow,0),center.add(side.scale(.2)).add(0,.6*grow,0),
                    center.add(side.scale(.36)).add(0,.6*grow,0),center.add(side.scale(.36)));
                face(p.main,.07,.3+i*.04,.95,center.subtract(side.scale(.32)).add(0,.12,0),center.add(side.scale(.32)).add(0,.12,0),
                    center.add(side.scale(.32)).add(0,.5*grow,0),center.subtract(side.scale(.32)).add(0,.5*grow,0));
                gem(center.add(0,.88*grow,0),.045,p.accent,.4+i*.035,.9,theta+age*.15);
            }
            for(int layer=0;layer<2;layer++) {
                List<Vec3> hex=new ArrayList<>();for(int i=0;i<=6;i++)hex.add(base.add(radial(i*Math.PI/3,1.4+layer*.14,0)));
                line(hex,UP,.021,p.main,.32,.24+layer*.11,1.05);
            }
            if(counter) {
                line(.065,p.accent,.65,.22,.45,UP,owner,c);
                diamond(c,X,.45,p.main,.32,.85);
                for(int wave=0;wave<3;wave++)diamond(owner.lerp(c,ease((age-.38-wave*.09)/.36)),X,.28+wave*.045,p.accent,.38+wave*.09,.85);
            }
        }
        void destruction() {
            boolean rage=packet.kind()==CombatVfxPacket.Kind.DESTRUCTION_RAGE;
            int arms=rage?3:strong?7:5;double late=clamp(age-.55);
            for(int i=0;i<arms;i++) {
                double angle=i*Math.PI*2/arms,r=rage?.55:strong?1.55:.95;
                line(.06,p.main,.65,i*.025,.75,UP,foot,foot.add(radial(angle+.18,r*.55,0)),foot.add(radial(angle,r,0)));
                Vec3 base=foot.add(radial(angle,r*.7,0)),side=radial(angle+Math.PI/2,1,0);
                line(.1,p.main,.52,.12+i*.035,.65,side,base,base.add(.12,.35,0),base.add(-.09,.65,0),base.add(.04,rage?.6:strong?1.25:.85,0));
                if(!rage) {
                    leaf(foot.add(radial(angle,1.05,0)),radial(angle,.38,strong?1.65:1.1),.12,p.main,.28+i*.025,.87);
                    gem(base.add(radial(angle,late*.15,.4+late*.65)),.035,p.accent,.46+i*.025,.8,age*2+i);
                }
            }
            int count=rage?4:strong?10:7;
            for(int i=0;i<count;i++) {
                double theta=i*Math.PI*2/count,r=rage?.45:.75+ease((age-.65)/.8)*.7,h=rage?.48:strong?1.35:.85;
                Vec3 base=foot.add(radial(theta,r,0)),side=radial(theta+Math.PI/2,1,0);
                diamond(base.add(0,h*.45,0),side,rage?.09:.14,p.accent,.22+i*.025,.85);
                curve(base,f->radial(theta,f*.3,f*h).add(Math.sin(f*9+theta)*.06,0,0),side,.02,p.main,.42,.55+i*.018,.9);
            }
            if(!rage)for(int i=0;i<12;i++) {
                double theta=i*2.39996,t=clamp((age-.6)/1.1);
                Vec3 root=foot.add(radial(theta,.6+t*1.1,.25+Math.sin(t*Math.PI)*(i%3*.2+.3)));
                face(p.accent,.48,.6+i*.014,1.05,root,root.add(.04,.12,0),root.add(-.035,.025,.035));
            }
            if(strong)for(int layer=0;layer<2;layer++) {
                List<Vec3> jag=new ArrayList<>();for(int i=0;i<=16;i++)jag.add(foot.add(radial(i*Math.PI/8,(i%2==0?1.6:1.25)+layer*.15,0)));
                line(jag,UP,.022,p.main,.38,.38+layer*.12,.8);
            }
        }
        void erudition() {
            curve(c,f->v(Math.cos(f*Math.PI*2)*.78,Math.sin(f*Math.PI*2)*.4,0),Z,.04,p.main,.65,0,.85);
            diamond(c,X,.22,p.accent,.1,.85);
            for(int sx:new int[]{-1,1})for(int sy:new int[]{-1,1})line(.035,p.main,.6,.1,.85,Z,v(sx*.55,sy*.68,0),v(sx*.98,sy*.68,0),v(sx*.98,sy*.35,0));
            Vec3[][] axes={{X,UP},{v(1,.25,0),v(0,.4,1)},{v(.25,0,1),UP}};
            for(int layer=0;layer<(strong?3:2);layer++) {
                Vec3 x=axes[layer][0],y=axes[layer][1];double r=1.1+layer*.15,turn=age*(layer%2==0?.3:-.28);
                curve(c,f->x.scale(Math.cos(turn+f*Math.PI*1.78)*r).add(y.scale(Math.sin(turn+f*Math.PI*1.78)*r)),x.cross(y),.021,p.main,.36,.24+layer*.085,1);
                for(int i=0;i<detail;i++) {
                    double theta=turn+i*Math.PI*2/detail;Vec3 dir=x.scale(Math.cos(theta)).add(y.scale(Math.sin(theta)));
                    line(.014,i%3==0?p.accent:p.main,.48,.3+layer*.07+i*.006,.85,x.cross(y),dir.scale(r),dir.scale(r+(i%3==0?.13:.06)));
                }
            }
            for(int i=0;i<(strong?8:5);i++) {
                double theta=i*Math.PI*2/(strong?8:5)+age*.17;Vec3 root=radial(theta,1.25,.25+Math.sin(theta)*.35),side=radial(theta+Math.PI/2,1,0);
                diamond(root,side,.1,p.main,.2+i*.025,.85);
                for(int row=0;row<3;row++)line(.01,p.main,.4,.38+i*.025,.8,radial(theta,1,0),root.subtract(side.scale(.13)).add(0,.2+row*.05,0),root.add(side.scale(.08+row%2*.08)).add(0,.2+row*.05,0));
                line(.012,p.main,.35,.72+i*.025,.6,UP,root,root.add(0,.16,0),v(0,(i%3-1)*.17,0));
            }
            if(source.lengthSqr()>.5)line(.022,p.main,.4,.18,.65,UP,source,c);
        }
        void nihility() {
            double fold=clamp(age/1.3),r=1.38-fold*.28,late=ease((age-.65)/.8);
            for(int band=0;band<(strong?4:3);band++) {
                double spin=band*Math.PI/2-age*.7,bandR=r+band*.05,h=.3+band*.12;
                curve(c,f->{double t=spin+f*Math.PI*1.4;return v(Math.cos(t)*bandR,Math.sin(t)*h,Math.sin(t)*r*.75);},UP,band%2==0?.06:.025,band%2==0?p.main:p.shade,.38,.23+band*.07,.98);
            }
            for(int i=0;i<(strong?16:10);i++) {
                double theta=i*2.39996-age*.35,radius=1.45-late*.65,h=Math.sin(theta*1.7)*.6;
                diamond(radial(theta,radius,h),radial(theta+Math.PI/2,1,0),.1,p.shade,.3+i*.016,.85);
                curve(c,f->radial(theta-f*.75,radius*(1-f*.65),h*(1-f)),UP,.017,p.main,.35,.68+i*.012,.8);
            }
            for(int i=0;i<12;i++) {
                double theta=i*Math.PI/6-age*.35,dt=.17;
                face(p.shade,.36,.3+i*.012,.95,v(Math.cos(theta)*r,Math.sin(theta)*r*.58,0),v(Math.cos(theta+dt)*r,Math.sin(theta+dt)*r*.58,0),
                    v(Math.cos(theta+dt)*(r+.12),Math.sin(theta+dt)*(r+.12)*.58,0),v(Math.cos(theta)*(r+.12),Math.sin(theta)*(r+.12)*.58,0));
            }
            for(int i=0;i<6;i++) {double theta=i*Math.PI/3;
                curve(c,f->radial(theta+f*1.3,.28+f*.75,(f-.5)*.75),UP,.024,p.shade,.55,1+i*.03,.85);}
            if(packet.kind()==CombatVfxPacket.Kind.NIHILITY_SPREAD||packet.kind()==CombatVfxPacket.Kind.NIHILITY_END) {
                Vec3 delta=source.subtract(c);
                curve(c,f->delta.scale(f).add(0,Math.sin(f*Math.PI)*.4,0),UP,.02,p.main,.4,.22,.8);
            }
        }
        void harmony() {
            double late=clamp(age-.55);
            for(int sign:new int[]{-1,1})for(int row=0;row<(strong?5:3);row++) {
                final int index=row;
                curve(c,f->v(sign*(.4+f*1.55),Math.sin(f*Math.PI)*(.4+index*.07)+(index-2)*.14,
                    Math.sin(f*Math.PI)*.22+Math.sin(f*7-age*4)*.05),Z,.018,p.main,.4,.24+row*.065,.95);
                Vec3 note=v(sign*(1.38+row*.09),.1+row*.17+late*.18,.15);
                line(.024,p.accent,.5,.38+row*.055,.8,Z,note,note.add(.08,.28,0),note.add(.26,.22,0));
                gem(note,.035,p.accent,.38+row*.055,.8,row);
            }
            for(int i=0;i<(strong?8:5);i++) {
                double theta=i*Math.PI*2/(strong?8:5);Vec3 side=radial(theta+Math.PI/2,1,0),node=radial(theta,1.05,.25+ease((age-.65)/.8)*.65);
                line(.019,p.accent,.55,.3+i*.045,1,radial(theta,1,0),node,node.add(0,.25,0),node.add(side.scale(.17)).add(0,.2,0));
                gem(node,.034,p.accent,.3+i*.045,1,theta);
                curve(c,f->radial(theta+f*.9,1.02+Math.sin(f*Math.PI)*.22,(f-.3)*.7+late*.2),UP,.016,p.main,.4,.55+i*.03,.95);
            }
            for(int beat=0;beat<3;beat++)for(int sector=0;sector<6;sector++) {
                double theta=sector*Math.PI/3;Vec3 root=foot.add(radial(theta,1.1+beat*.2,.01));
                line(.021,p.accent,.55,.8+beat*.16,.62,radial(theta,1,0),root,root.add(0,.09+beat*.04,0));
                orbit(foot,.95+beat*.18,0,.32+beat*.13,.72,p.accent,.26,theta+age*.2,Math.PI*.33);
            }
            if(packet.kind()==CombatVfxPacket.Kind.HARMONY_CONCERT&&source.lengthSqr()>.1)line(.025,p.accent,.5,.12,.8,UP,source,source.lerp(c,.5).add(0,.35,0),c);
        }
        void abundance() {
            int count=strong?10:6;
            for(int sign:new int[]{-1,1})for(int branch=0;branch<(strong?4:3);branch++) {
                Vec3 start=v(sign*.32,-.65+branch*.33,.1),tip=v(sign*(.98+branch*.14),.05+branch*.43,.15);
                line(.025,p.main,.46,.25+branch*.06,.98,Z,start,start.add(sign*.24,.36,0),tip);
                leaf(tip,v(sign*.34,.32,.12),.12,p.main,.4+branch*.06,.9);
                gem(tip.add(0,.12,0),.025,p.accent,.53+branch*.055,.8,age);
            }
            for(int i=0;i<count;i++) {
                double theta=i*Math.PI*2/count;Vec3 dir=radial(theta,1,0),base=foot.add(dir.scale(.72));
                curve(base,f->dir.scale(Math.sin(f*Math.PI)*.27).add(0,f*1.55,0),radial(theta+Math.PI/2,1,0),.018,p.main,.4,.32+i*.03,1.15);
                for(int bud=0;bud<2;bud++)leaf(base.add(0,.55+bud*.55,0),dir.scale(.25).add(0,.18,0),.07,p.main,.52+i*.025+bud*.08,1.05);
                double t=ease((age-.62-i*.02)/.8);
                gem(radial(theta,.65*(1-t),1.25-t*1.1),.026,p.accent,.62+i*.02,1,theta);
            }
            if(strong)for(int i=0;i<12;i++) {
                double theta=i*Math.PI/6;Vec3 dir=radial(theta,1,0);
                leaf(foot.add(dir.scale(.12)),dir.scale(1.65).add(0,.25,0),.15,p.main,.32+i*.025,1.02);
                gem(foot.add(dir.scale(1.5)).add(0,.16,0),.035,p.accent,.58+i*.018,.75,theta);
            }
            if(packet.kind()==CombatVfxPacket.Kind.ABUNDANCE_MANNA)for(int i=0;i<6;i++) {
                double theta=i*Math.PI/3;diamond(radial(theta,.85,.2),radial(theta+Math.PI/2,1,0),.14,p.accent,.65+i*.03,.85);}
        }
        void remembrance() {
            int planes=strong?3:2;
            for(int i=0;i<planes;i++) {
                double theta=i*Math.PI/planes+age*.22;Vec3 side=radial(theta,1,0);
                List<Vec3> points=new ArrayList<>();double[][] shape={{-.65,.83},{.65,.83},{0,0},{-.65,-.83},{.65,-.83},{0,0},{-.65,.83}};
                for(double[] xy:shape)points.add(side.scale(xy[0]).add(0,xy[1],0));
                line(points,side.cross(UP),.023,p.main,.36,.28+i*.11,.92);
                for(int arm=0;arm<6;arm++) {
                    double angle=arm*Math.PI/3;Vec3 tip=side.scale(Math.cos(angle)*.75).add(0,Math.sin(angle)*.75,0);
                    line(.028,p.main,.5,.1+i*.14,.7,side.cross(UP),c,tip);
                    for(int sign:new int[]{-1,1})line(.017,p.main,.4,.18+i*.14,.65,side.cross(UP),tip.scale(.6),
                        tip.scale(.6).add(side.scale(sign*.13)).add(0,.11,0));
                }
            }
            for(int i=0;i<(strong?10:6);i++) {
                double theta=i*Math.PI*2/(strong?10:6);Vec3 side=radial(theta+Math.PI/2,1,0),root=radial(theta,.95+ease((age-.65)/.8)*.18,Math.sin(theta)*.45);
                diamond(root,side,.13,p.main,.26+i*.028,.85);
                for(int echo=0;echo<2;echo++) {
                    double r=.8+echo*.16;
                    curve(c,f->radial(theta+f*.42,r,.2+Math.sin(f*Math.PI)*.25),UP,.018,echo==0?p.main:p.shade,.4,.66+echo*.2+i*.015,.75);
                }
                gem(root.add(0,-ease((age-1.12)/.65)*.55,0),.018,p.accent,1.12+i*.015,.85,theta);
            }
        }
        /** Intermediate unfolding layer mirrors the HTML's shield, leaf and crystal structures. */
        void secondary() {
            double unfold=ease(age/.35),drift=clamp(age-.3)*.3;
            switch(packet.path()) {
                case PRESERVATION -> {
                    boolean counter=packet.kind()==CombatVfxPacket.Kind.PRESERVATION_COUNTER;
                    Vec3 owner=counter?source:c,base=counter?sourceFoot:foot;
                    for(int i=0;i<6;i++) {
                        double theta=i*Math.PI/3;Vec3 dir=radial(theta,1,0),side=radial(theta+Math.PI/2,1,0),center=owner.add(dir.scale(.65+unfold*.24));
                        List<Vec3> poly=List.of(center.subtract(side.scale(.18)).add(0,.45,0),center.add(side.scale(.18)).add(0,.45,0),center.add(side.scale(.25)),center.add(0,-.48,0),center.subtract(side.scale(.25)));
                        for(int j=0;j<poly.size();j++)face(j%2==0?p.main:p.shade,.12,.15+i*.04,.9,center,poly.get(j),poly.get((j+1)%poly.size()));
                        List<Vec3> outline=new ArrayList<>(poly);outline.add(poly.get(0));line(outline,dir,.022,p.main,.52,.15+i*.04,.9);
                    }
                    orbit(base,1.14,0,.28,.85,p.main,.4,age*.25,Math.PI*1.65);
                }
                case DESTRUCTION -> {
                    boolean rage=packet.kind()==CombatVfxPacket.Kind.DESTRUCTION_RAGE;int count=rage?3:strong?7:5;
                    for(int i=0;i<count;i++) {
                        double theta=i*Math.PI*2/count;Vec3 dir=radial(theta,1,0),base=foot.add(dir.scale(.6+unfold*.23));
                        leaf(base,dir.scale(.25).add(0,(strong?1.15:.7)*unfold,0),.12,p.main,.1+i*.035,.75);
                        if(!rage)curve(base,f->dir.scale(Math.sin(f*Math.PI)*.22).add(0,f*(strong?1.25:.8),0),radial(theta+Math.PI/2,1,0),.028,p.accent,.48,.23+i*.025,.65);
                    }
                }
                case ERUDITION -> {
                    for(int layer=0;layer<(strong?2:1);layer++) {
                        double radius=.85+layer*.18,phase=age*.45+layer*Math.PI;
                        curve(c,f->{double theta=f*Math.PI*1.6+phase;return v(Math.cos(theta)*radius,Math.sin(theta)*radius*.5,Math.sin(theta)*radius*.5);},v(0,1,1),.025,p.main,.4,.2+layer*.09,.8);
                        for(int i=0;i<4;i++) {double theta=i*Math.PI/2+age*.45;gem(v(Math.cos(theta)*radius,Math.sin(theta)*radius*.5,Math.sin(theta)*radius*.5),.045,p.accent,.23+i*.04,.7,theta);}
                    }
                    for(int i=0;i<6;i++) {double theta=i*Math.PI/3,r=.6+ease((age-.65)/.8)*.8;
                        curve(c,f->v(Math.cos(theta+f*.45)*r,Math.sin(theta+f*.45)*r,Math.sin((theta+f*.45)*2)*.28),Z,.018,p.accent,.45,.95+i*.02,.8);}
                }
                case NIHILITY -> {
                    for(int layer=0;layer<(strong?3:2);layer++) {
                        double phase=layer*Math.PI*2/3-age*.8,r=1.25-layer*.12-clamp(age)*.12;
                        curve(c,f->radial(phase+f*Math.PI*1.4,r,(f-.5)*.6),UP,.045,p.main,.38,.18+layer*.09,.85);
                        gem(radial(phase+1.2,r,.22),.04,p.main,.28+layer*.09,.7,age);
                    }
                }
                case HARMONY -> {
                    for(int sign:new int[]{-1,1})for(int layer=0;layer<(strong?3:2);layer++) {final int k=layer;
                        curve(c,f->v(sign*(.3+f*1.25),Math.sin(f*Math.PI)*.35+Math.sin(f*6.3-age*4)*.06-k*.14,Math.sin(f*Math.PI)*(.2+k*.06)),Z,.025,p.main,.45,.2+layer*.1,.85);}
                }
                case ABUNDANCE -> {
                    for(int sign:new int[]{-1,1})for(int i=0;i<3;i++)leaf(v(sign*(.45+i*.12),-.5+i*.43,.12),v(sign*.43,.32,.1),.13,p.main,.15+i*.08,.9);
                    if(strong)for(int i=0;i<8;i++) {double theta=i*Math.PI/4;leaf(foot.add(radial(theta,.08,0)),radial(theta,1.15,.32),.18,p.main,.18+i*.035,.95);}
                }
                case REMEMBRANCE -> {
                    for(int arm=0;arm<6;arm++) {
                        double theta=arm*Math.PI/3;Vec3 axis=v(Math.cos(theta),Math.sin(theta),0),across=v(-axis.y,axis.x,0),node=axis.scale(.45);
                        line(.026,p.main,.52,.18,.75,Z,node,axis.scale(.85));
                        for(int sign:new int[]{-1,1})line(.018,p.main,.46,.24,.7,Z,node.add(axis.scale(.15)),node.add(axis.scale(.06)).add(across.scale(sign*.15)));
                    }
                    if(strong)for(int i=0;i<6;i++){double theta=i*Math.PI/3;gem(radial(theta,.9+drift,.55+Math.sin(theta)*.25),.09,p.main,.3+i*.05,.85,age*.6+theta);}
                }
                case ELATION -> {
                    for(int layer=0;layer<(strong?3:2);layer++) {final int band=layer;
                        curve(c,f->radial(f*Math.PI*1.35+band*2.4+age*.5,1+f*.3,.65-f*1.1),UP,.04,layer%2==0?p.main:p.accent,.46,.18+layer*.12,.8);}
                }
                default -> { }
            }
        }
        void cube(Vec3 root,double size,double turn,double birth) {
            Vec3 side=radial(turn,size,0),forward=radial(turn+Math.PI/2,size,0),up=UP.scale(size);
            Vec3[] corners=new Vec3[8];for(int i=0;i<8;i++)corners[i]=root.add(side.scale((i&1)==0?-1:1)).add(up.scale((i&2)==0?-1:1)).add(forward.scale((i&4)==0?-1:1));
            int[][] quads={{0,1,3,2},{4,6,7,5},{0,4,5,1},{2,3,7,6},{0,2,6,4},{1,5,7,3}};
            for(int i=0;i<quads.length;i++) {
                int[] q=quads[i];face(i%2==0?p.main:p.shade,.17,birth,.95,corners[q[0]],corners[q[1]],corners[q[2]],corners[q[3]]);
                line(.012,p.accent,.5,birth,.95,UP,corners[q[0]],corners[q[1]],corners[q[2]],corners[q[3]],corners[q[0]]);
            }
            for(int i=0;i<3;i++)gem(root.add(side.scale(.25*(i-1))).add(forward.scale(1.06)),.022,p.accent,birth,.95,i);
        }
        void elation() {
            for(int i=0;i<(strong?3:2);i++) {
                final int band=i;
                curve(c,f->{double theta=band*2.1+f*Math.PI*1.5;return radial(theta,.4+f,.45-f);},UP,.055,i%2==0?p.main:p.accent,.55,i*.12,.75);
            }
            cube(v(.85,-.2,0),.18,age*.65,.22);
            if(strong)cube(v(-.9,.65,0),.15,-age*.5,.36);
            int cards=strong?5:3;
            for(int i=0;i<cards;i++) {
                double theta=(i-(cards-1)/2D)*.35+age*.12;
                Vec3 root=v(Math.sin(theta)*1.85,.9+Math.cos(theta)*.2,.22),side=v(Math.cos(theta),-.15*Math.sin(theta),Math.sin(theta)*.25);
                Vec3 l=root.subtract(side.scale(.16)),r=root.add(side.scale(.16));
                face(p.main,.1,.35+i*.045,.95,l.add(0,-.27,0),r.add(0,-.27,0),r.add(0,.27,0),l.add(0,.27,0));
                line(.02,i%2==0?p.main:p.accent,.5,.35+i*.045,.95,side.cross(UP),l.add(0,-.27,0),r.add(0,-.27,0),r.add(0,.27,0),l.add(0,.27,0),l.add(0,-.27,0));
                diamond(root,side,.08,p.accent,.43+i*.045,.85);
            }
            Vec3 mask=v(0,1.12,.2);
            curve(mask,f->v((f-.5)*.68,-Math.sin(f*Math.PI)*.2,0),Z,.026,p.main,.55,.5,.75);
            for(int sign:new int[]{-1,1})line(.02,p.accent,.52,.52,.72,Z,mask.add(sign*.1,.15,0),mask.add(sign*.18,.2,0),mask.add(sign*.26,.15,0));
            int count=strong?12:7;
            for(int i=0;i<count;i++) {
                double theta=i*2.39996,t=clamp((age-.45-i*.015)/1.05),r=.55+t*1.05;
                Vec3 root=radial(theta,r,.2+Math.sin(t*Math.PI)*(.4+i%3*.12)-t*.35),side=radial(theta+Math.PI/2,1,0);
                int color=i%4==0?p.accent:p.main;
                diamond(root,side,.07,color,.45+i*.015,.85);
                curve(root,f->side.scale(f*.17).add(Math.sin(f*7+age*3)*.05,-f*.2,0),radial(theta,1,0),.018,color,.45,.66+i*.02,.8);
            }
            for(int i=0;i<5;i++) {Vec3 root=radial(i*Math.PI*2/5,1.05,.9);
                line(.023,i%2==0?p.accent:p.main,.55,1.02+i*.035,.75,Z,root.add(-.06,.12,0),root,root.add(.06,.12,0));}
        }
    }
}
