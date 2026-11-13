package com.starrail.sim.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.StarRailStatusConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.*;

/** Samples the final rendered item transform; never guesses a swing from the player's yaw. */
@Mod.EventBusSubscriber(modid=StarRailSimMod.MOD_ID, value=Dist.CLIENT)
public final class StarRailHandTrails {
    private static final Deque<Context> CONTEXTS=new ArrayDeque<>();
    private static final Map<Key,History> HISTORIES=new HashMap<>();
    // A private builder avoids invalidating the item renderer's in-flight VertexConsumer.
    private static MultiBufferSource.BufferSource trailBuffers=MultiBufferSource.immediate(new BufferBuilder(16384));
    private static final List<Draw> WORLD_EMISSION=new ArrayList<>(),HAND_EMISSION=new ArrayList<>();
    private static ClientLevel level;
    private static Object cameraMode;
    private static final Map<String,Integer> COLORS=Map.of(
        "path_dawn_gold",0xffcf45,"path_dawncopper",0xee9871,"path_suniron",0xd7e0ed,
        "path_starmarrow",0x47cce8,"path_stellarjade",0xf4c6df,"path_ancientdream",0x9b87f4);
    private record Key(int entity,boolean left,boolean first) { }
    private record Sample(Vec3 inner,Vec3 tip,double time,int stroke) { }
    private record Profile(int color,boolean rift,double x0,double y0,double x1,double y1) { }
    private record Quad(Vec3 a,Vec3 b,Vec3 c,Vec3 d,int color,double alpha,float ageA,float ageB,float lo,float hi) { }
    private record Draw(List<Quad> quads,Matrix4f projection,Quaternionf inverseCamera,Vec3 camera,float time,boolean first,boolean enhanced) { }
    private static final class History {
        final Deque<Sample> samples=new ArrayDeque<>();
        Item item;
        Vec3 ownerPosition;
        double lastSeen,lastSample=-100;
        float progress;
        boolean swinging;
        int stroke;
    }
    private static final class Context {
        final LivingEntity entity;
        final ItemStack stack;
        final ItemDisplayContext display;
        final boolean left;
        boolean captured;
        Draw draw;
        Context(LivingEntity entity,ItemStack stack,ItemDisplayContext display,boolean left) {
            this.entity=entity;this.stack=stack;this.display=display;this.left=left;
        }
    }
    private static final class TrailType extends RenderType {
        private TrailType() {super("starrail_hand_trail",DefaultVertexFormat.POSITION_COLOR_TEX,VertexFormat.Mode.QUADS,16384,false,true,()->{},()->{});}
        static final RenderType TYPE=create("starrail_hand_trail",DefaultVertexFormat.POSITION_COLOR_TEX,VertexFormat.Mode.QUADS,16384,false,true,
            CompositeState.builder().setShaderState(new ShaderStateShard(StarRailTrailShaders::current)).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE)
                .setOutputState(new OutputStateShard("hand_trail_target",StarRailCombatPost::bindGeometryOutput,()->{})).createCompositeState(false));
    }
    private StarRailHandTrails() { }
    public static void begin(LivingEntity entity,ItemStack stack,ItemDisplayContext display,boolean left) {
        if(CONTEXTS.size()>16)CONTEXTS.clear();
        CONTEXTS.push(new Context(entity,stack,display,left));
    }
    public static void end() {
        if(CONTEXTS.isEmpty())return;
        Context context=CONTEXTS.pop();
        if(context.draw!=null) {
            var shader=RenderSystem.getShader();
            try {
                try {submit(context.draw,false);}
                catch(RuntimeException exception) {
                    StarRailTrailShaders.disable(exception);
                    trailBuffers=MultiBufferSource.immediate(new BufferBuilder(16384));
                    submit(context.draw,false);
                }
                List<Draw> queue=context.draw.first?HAND_EMISSION:WORLD_EMISSION;
                if(context.draw.enhanced&&StarRailTrailShaders.enhanced()&&queue.size()<64)queue.add(context.draw);
            }
            finally {RenderSystem.setShader(()->shader);}
        }
    }
    public static void clear() {HISTORIES.clear();WORLD_EMISSION.clear();HAND_EMISSION.clear();}
    static int activeHistories() {return HISTORIES.size();}
    private static double now(Minecraft mc) {return mc.level.getGameTime()+mc.getFrameTime();}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        Minecraft mc=Minecraft.getInstance();
        WORLD_EMISSION.clear();HAND_EMISSION.clear();
        if(mc.level!=level||mc.player==null||cameraMode!=mc.options.getCameraType()||!StarRailStatusConfig.HAND_TRAILS.get()) {
            clear();level=mc.level;cameraMode=mc.options.getCameraType();
        }
        if(mc.level!=null) {double time=now(mc);HISTORIES.entrySet().removeIf(e->{
            var entity=mc.level.getEntity(e.getKey().entity);
            if(!(entity instanceof Player player)||!player.isAlive())return true;
            InteractionHand hand=(e.getKey().left==(player.getMainArm()==HumanoidArm.LEFT))?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND;
            return time-e.getValue().lastSeen>5||player.getItemInHand(hand).getItem()!=e.getValue().item;
        });}
    }
    private static Profile profile(ItemStack stack) {
        var id=ForgeRegistries.ITEMS.getKey(stack.getItem());
        if(id==null||!id.getNamespace().equals(StarRailSimMod.MOD_ID))return null;
        String name=id.getPath();
        if(name.equals("void_rift_blade"))return new Profile(0xb07aff,true,.42,.42,.83,.89);
        int split=name.lastIndexOf('_');if(split<0)return null;
        Integer color=COLORS.get(name.substring(0,split));if(color==null)return null;
        return switch(name.substring(split+1)) {
            case "sword" -> new Profile(color,false,.45,.45,.83,.89);
            case "pickaxe" -> new Profile(color,false,.35,.81,.83,.86);
            case "axe" -> new Profile(color,false,.49,.65,.84,.87);
            case "shovel" -> new Profile(color,false,.59,.65,.82,.87);
            case "hoe" -> new Profile(color,false,.42,.80,.82,.87);
            default -> null;
        };
    }
    public static void capture(PoseStack poses) {
        Context context=CONTEXTS.peek();
        if(context==null||context.captured)return;
        context.captured=true; // Multiple render layers and glint must not create duplicate samples.
        Minecraft mc=Minecraft.getInstance();
        if(mc.level==null||mc.player==null||!StarRailStatusConfig.HAND_TRAILS.get()
            ||!(context.entity instanceof Player player)||!player.isAlive()||player.isSpectator())return;
        boolean first=context.display==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||context.display==ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean third=context.display==ItemDisplayContext.THIRD_PERSON_LEFT_HAND||context.display==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        if(!first&&!third)return;
        // Do not accumulate a separate self-body trail when the camera is in first person.
        if(third&&player==mc.player&&mc.options.getCameraType().isFirstPerson())return;
        Profile profile=profile(context.stack);if(profile==null)return;
        if(mc.level!=level||cameraMode!=mc.options.getCameraType()) {clear();level=mc.level;cameraMode=mc.options.getCameraType();}
        if(HISTORIES.size()>=128)HISTORIES.clear();
        Key key=new Key(player.getId(),context.left,first);
        History history=HISTORIES.computeIfAbsent(key,k->new History());
        boolean enhanced=StarRailTrailShaders.enhanced();
        double time=now(mc),life=enhanced?(first?3.8:4.8):(first?2.8:3.5);
        boolean moved=history.ownerPosition!=null&&history.ownerPosition.distanceToSqr(player.position())>9;
        if(history.item!=context.stack.getItem()||moved||time-history.lastSeen>5) {
            history.samples.clear();history.swinging=false;history.lastSample=-100;
        }
        history.item=context.stack.getItem();history.ownerPosition=player.position();history.lastSeen=time;
        history.samples.removeIf(s->time-s.time>life);
        InteractionHand hand=player.swingingArm==null?InteractionHand.MAIN_HAND:player.swingingArm;
        HumanoidArm arm=hand==InteractionHand.MAIN_HAND?player.getMainArm():player.getMainArm().getOpposite();
        float progress=player.getAttackAnim(mc.getFrameTime());
        boolean swinging=player.swinging&&(context.left==(arm==HumanoidArm.LEFT))&&!player.isUsingItem();
        if(swinging) {
            if(!history.swinging||progress+.25F<history.progress)history.stroke++;
            Matrix4f transform=new Matrix4f(RenderSystem.getModelViewMatrix()).mul(poses.last().pose());
            Vec3 inner=toWorld(transform,profile.x0,profile.y0,mc),tip=toWorld(transform,profile.x1,profile.y1,mc);
            Sample last=history.samples.peekLast();
            if(last!=null&&(tip.distanceToSqr(last.tip)>4||inner.distanceToSqr(last.inner)>4)) {
                history.samples.clear();history.stroke++;
            }
            boolean minimal=mc.options.particles().get()==net.minecraft.client.ParticleStatus.MINIMAL;
            if(!mc.isPaused()&&time-history.lastSample>=life/(minimal?9:20)) {
                history.samples.addLast(new Sample(inner,tip,time,history.stroke));history.lastSample=time;
                int max=minimal?10:24;
                while(history.samples.size()>max)history.samples.removeFirst();
            }
        }
        history.swinging=swinging;history.progress=progress;
        if(history.samples.size()<2)return;
        // Convert history back to the current pass's eye coordinates, including hand FOV/bobbing.
        Quaternionf inverseCamera=new Quaternionf(mc.gameRenderer.getMainCamera().rotation()).conjugate();
        List<Quad> quads=new ArrayList<>();
        double strength=StarRailStatusConfig.HAND_TRAIL_STRENGTH.get()*(first?.68:1);
        List<Sample> samples=new ArrayList<>(history.samples);
        int subdivisions=mc.options.particles().get()==net.minecraft.client.ParticleStatus.MINIMAL?1:3;
        for(int i=1;i<samples.size();i++) {
            Sample previous=samples.get(i-1),sample=samples.get(i);
            if(previous.stroke==sample.stroke&&sample.tip.distanceToSqr(previous.tip)>.000015) {
                Sample before=i>1&&samples.get(i-2).stroke==sample.stroke?samples.get(i-2):previous;
                Sample after=i+1<samples.size()&&samples.get(i+1).stroke==sample.stroke?samples.get(i+1):sample;
                for(int step=0;step<(enhanced?subdivisions:1);step++) {
                    Sample a=enhanced?interpolate(before,previous,sample,after,step/(double)subdivisions):previous;
                    Sample b=enhanced?interpolate(before,previous,sample,after,(step+1D)/subdivisions):sample;
                    double fade=Math.max(0,1-(time-a.time)/life),alpha=fade*fade*strength;
                    if(enhanced)band(quads,a,b,-.08,1.28,profile.color,.86*alpha,time,life,profile.rift);
                    else {
                        band(quads,a,b,.08,1.12,profile.color,.12*alpha,time,life,false);
                        band(quads,a,b,.25,.96,profile.color,.40*alpha,time,life,false);
                        band(quads,a,b,.86,1.0,0xf2f7ff,.58*alpha,time,life,false);
                        if(profile.rift)band(quads,a,b,.38,.51,0x7034ad,.62*alpha,time,life,false);
                    }
                }
            }
        }
        if(!quads.isEmpty())context.draw=new Draw(quads,new Matrix4f(RenderSystem.getProjectionMatrix()),inverseCamera,
            mc.gameRenderer.getMainCamera().getPosition(),(float)(time%72000/20D),first,enhanced);
    }
    private static Vec3 toWorld(Matrix4f transform,double x,double y,Minecraft mc) {
        Vector3f point=transform.transformPosition(new Vector3f((float)x,(float)y,.5F));
        point.rotate(mc.gameRenderer.getMainCamera().rotation());
        return mc.gameRenderer.getMainCamera().getPosition().add(point.x,point.y,point.z);
    }
    private static Sample interpolate(Sample p0,Sample p1,Sample p2,Sample p3,double t) {
        return new Sample(smooth(p0.inner,p1.inner,p2.inner,p3.inner,t),smooth(p0.tip,p1.tip,p2.tip,p3.tip,t),
            p1.time+(p2.time-p1.time)*t,p1.stroke);
    }
    private static Vec3 smooth(Vec3 p0,Vec3 p1,Vec3 p2,Vec3 p3,double t) {
        Vec3 linear=p1.lerp(p2,t);
        Vec3 curve=p1.scale(2).add(p2.subtract(p0).scale(t)).add(p0.scale(2).subtract(p1.scale(5)).add(p2.scale(4)).subtract(p3).scale(t*t))
            .add(p3.subtract(p0).add(p1.subtract(p2).scale(3)).scale(t*t*t)).scale(.5);
        Vec3 delta=curve.subtract(linear);
        return linear.add(delta.lengthSqr()>.0036?delta.normalize().scale(.06):delta);
    }
    private static void band(List<Quad> out,Sample a,Sample b,double lo,double hi,int color,double alpha,double time,double life,boolean rift) {
        out.add(new Quad(a.inner.lerp(a.tip,lo),a.inner.lerp(a.tip,hi),b.inner.lerp(b.tip,hi),b.inner.lerp(b.tip,lo),color,alpha,
            (float)((time-a.time)/life),(float)((time-b.time)/life),(float)(lo+(rift?2:0)),(float)(hi+(rift?2:0))));
    }
    private static void submit(Draw draw,boolean emission) {
        StarRailTrailShaders.prepare(draw.time,emission);
        Matrix4f projection=new Matrix4f(RenderSystem.getProjectionMatrix());
        var sorting=RenderSystem.getVertexSorting();
        try {
            RenderSystem.setProjectionMatrix(draw.projection,sorting);
            Matrix4f inverseModelView=new Matrix4f(RenderSystem.getModelViewMatrix()).invert();
            VertexConsumer vertices=trailBuffers.getBuffer(TrailType.TYPE);
            for(Quad q:draw.quads) {
                vertex(vertices,q.a,q.color,q.alpha,q.ageA,q.lo,draw,inverseModelView);
                vertex(vertices,q.b,q.color,q.alpha,q.ageA,q.hi,draw,inverseModelView);
                vertex(vertices,q.c,q.color,q.alpha,q.ageB,q.hi,draw,inverseModelView);
                vertex(vertices,q.d,q.color,q.alpha,q.ageB,q.lo,draw,inverseModelView);
            }
            trailBuffers.endBatch(TrailType.TYPE);
        } finally {RenderSystem.setProjectionMatrix(projection,sorting);}
    }
    private static void vertex(VertexConsumer out,Vec3 point,int color,double alpha,float age,float across,Draw draw,Matrix4f modelView) {
        Vec3 relative=point.subtract(draw.camera);
        Quaternionf rotation=draw.inverseCamera;
        Vector3f eye=new Vector3f((float)relative.x,(float)relative.y,(float)relative.z).rotate(rotation);
        modelView.transformPosition(eye);
        out.vertex(eye.x,eye.y,eye.z).color((color>>16&255)/255F,(color>>8&255)/255F,(color&255)/255F,(float)Math.min(.9,alpha)).uv(age,across).endVertex();
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void worldBloom(RenderLevelStageEvent event) {
        if(event.getStage()==RenderLevelStageEvent.Stage.AFTER_LEVEL)flushBloom(false);
    }
    public static void flushBloom(boolean first) {
        List<Draw> queue=first?HAND_EMISSION:WORLD_EMISSION;
        if(queue.isEmpty())return;
        List<Draw> draws=new ArrayList<>(queue);queue.clear();
        if(!StarRailTrailShaders.enhanced()||!StarRailStatusConfig.HAND_TRAILS.get())return;
        try {
            StarRailCombatPost.render(()->{
                int budget=4096;
                for(Draw draw:draws) {if(draw.quads.size()>budget)continue;budget-=draw.quads.size();submit(draw,true);}
            },List.of(),true);
        } finally {StarRailTrailShaders.prepare(0,false);}
    }
}
