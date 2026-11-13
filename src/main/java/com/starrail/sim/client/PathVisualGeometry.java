package com.starrail.sim.client;

import net.minecraft.world.phys.Vec3;
import java.util.function.DoubleFunction;

/** Stateless, bounded geometry shared by the active tableaux and ordinary-hit seals. */
final class PathVisualGeometry {
    static final Vec3 X=new Vec3(1,0,0),Y=new Vec3(0,1,0),Z=new Vec3(0,0,1);
    final MonsterCombatEffects.Sink sink;
    final Vec3 camera;
    final int detail,material,limit;
    final double time,opacity;
    int faces;

    PathVisualGeometry(MonsterCombatEffects.Sink sink,Vec3 camera,int detail,int material,double time,double opacity,int limit) {
        this.sink=sink;this.camera=camera;this.detail=Math.max(8,Math.min(24,detail));
        this.material=material;this.time=time;this.opacity=opacity;this.limit=limit;
    }
    static double clamp(double x) {return Math.max(0,Math.min(1,x));}
    static double smooth(double x) {x=clamp(x);return x*x*(3-2*x);}
    static Vec3 radial(double a,double radius,double y) {return new Vec3(Math.cos(a)*radius,y,Math.sin(a)*radius);}
    void face(int color,double alpha,Vec3 a,Vec3 b,Vec3 c,Vec3 d) {
        alpha*=opacity;
        if(alpha<.002 || faces>=limit)return;
        sink.face(color,Math.min(.9,alpha),material,time,a,b,c,d);faces++;
    }
    void stroke(Vec3 a,Vec3 b,double width,int color,double alpha) {
        Vec3 axis=b.subtract(a);if(axis.lengthSqr()<1e-10)return;
        Vec3 side=axis.cross(a.lerp(b,.5).subtract(camera)).normalize();
        if(side.lengthSqr()<1e-8)side=axis.cross(Y).normalize();
        if(side.lengthSqr()<1e-8)side=X;
        side=side.scale(width);
        face(color,alpha,a.subtract(side),a.add(side),b.add(side),b.subtract(side));
    }
    void path(DoubleFunction<Vec3> curve,double width,int color,double alpha) {
        Vec3 last=curve.apply(0);
        for(int i=1;i<=detail;i++) {Vec3 next=curve.apply(i/(double)detail);stroke(last,next,width,color,alpha);last=next;}
    }
    void arc(Vec3 center,Vec3 u,Vec3 v,double radius,double start,double span,double width,int color,double alpha) {
        path(f->center.add(u.scale(Math.cos(start+span*f)*radius)).add(v.scale(Math.sin(start+span*f)*radius)),width,color,alpha);
    }
    void polygon(Vec3 center,Vec3 u,Vec3 v,double radius,int sides,double turn,double width,int color,double alpha) {
        Vec3 first=null,last=null;
        for(int i=0;i<=sides;i++) {
            double a=turn+i*Math.PI*2/sides;
            Vec3 p=center.add(u.scale(Math.cos(a)*radius)).add(v.scale(Math.sin(a)*radius));
            if(first==null)first=p;else stroke(last,p,width,color,alpha);last=p;
        }
    }
    void ribbon(DoubleFunction<Vec3> curve,Vec3 across,double width,int color,double alpha) {
        for(int i=0;i<detail;i++) {
            double f=i/(double)detail,h=(i+1D)/detail;
            Vec3 a=curve.apply(f),b=curve.apply(h),wa=across.scale(width*Math.sin(f*Math.PI)),wb=across.scale(width*Math.sin(h*Math.PI));
            face(color,alpha,a.subtract(wa),a.add(wa),b.add(wb),b.subtract(wb));
        }
    }
    void crystal(Vec3 center,Vec3 axis,Vec3 side,double size,int color,double alpha) {
        Vec3 top=center.add(axis.scale(size)),bottom=center.subtract(axis.scale(size)),left=center.subtract(side.scale(size*.48)),right=center.add(side.scale(size*.48));
        face(color,alpha,top,left,bottom,right);
        stroke(top,left,.012,color,alpha);stroke(left,bottom,.012,color,alpha);
        stroke(bottom,right,.012,color,alpha);stroke(right,top,.012,color,alpha);
    }
    void star(Vec3 center,Vec3 u,Vec3 v,double radius,int rays,double turn,int color,double alpha) {
        for(int i=0;i<rays;i++) {
            double a=turn+i*Math.PI*2/rays;
            Vec3 d=u.scale(Math.cos(a)).add(v.scale(Math.sin(a)));
            stroke(center.add(d.scale(radius*.26)),center.add(d.scale(radius)),.016,color,alpha);
        }
    }
}
