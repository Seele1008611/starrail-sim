package com.starrail.sim.client;

import com.starrail.sim.ActiveSkillFxPacket;
import com.starrail.sim.StarRailPath;
import net.minecraft.world.phys.Vec3;
import static com.starrail.sim.client.PathVisualGeometry.*;

/** Larger, staged Path compositions. No world writes, clocks, random state or gameplay callbacks. */
final class PathSkillSpectacle {
    static int material(StarRailPath path) {
        return switch(path) {case HUNT->1;case PRESERVATION->2;case ABUNDANCE->3;case DESTRUCTION->4;case ERUDITION->5;case NIHILITY->6;case HARMONY->7;case REMEMBRANCE->8;case ELATION->9;default->0;};
    }
    static void draw(ActiveSkillFxPacket packet,double age,double life,Vec3 feet,Vec3 body,Vec3 camera,int detail,int beats,MonsterCombatEffects.Sink sink) {
        if(age<0 || age>=life || packet.stage()==3 || !packet.path().isRealPath())return;
        boolean cast=packet.stage()==0;
        double fade=smooth((life-age)/Math.min(.55,life*.5))*smooth(age/.065);
        PathVisualGeometry g=new PathVisualGeometry(sink,camera,detail,material(packet.path()),age*2,fade,detail<=12?720:1500);
        double open=smooth(age/.28),burst=smooth(age/.12),after=clamp(1-age/.85);
        switch(packet.path()) {
            case HUNT -> hunt(g,packet,age,open);
            case PRESERVATION -> {
                // Six separated gold bastions: transparent panels, bevels and a rising coronet.
                double r=1.45*open;
                g.polygon(feet.add(0,.12,0),X,Z,r*1.08,6,0,.024,0xffd178,.8);
                for(int i=0;i<6;i++) {
                    double a=i*Math.PI/3;
                    Vec3 normal=radial(a,1,0),side=radial(a+Math.PI/2,1,0),base=feet.add(normal.scale(r));
                    Vec3 l=base.subtract(side.scale(.48)),h=base.add(side.scale(.48)),tip=base.add(0,2.5*open,0);
                    Vec3 tl=l.add(0,1.95*open,0),tr=h.add(0,1.95*open,0);
                    g.face(0xca8733,.07,l,h,tr,tl);
                    g.stroke(l,tl,.026,0xf2b94b,.85);g.stroke(h,tr,.026,0xf2b94b,.85);
                    g.stroke(tl,tip,.022,0xffe9ab,.9);g.stroke(tip,tr,.022,0xffe9ab,.9);
                    g.crystal(base.add(0,1.2,0),Y,side,.23,0xffdb80,.35);
                    g.stroke(base.add(0,.15,0),base.add(0,.72,0),.055,0xffce65,.35);
                }
                g.arc(feet.add(0,2.65*open,0),X,Z,r*.68,age*.5,Math.PI*2,.025,0xffe4a1,.7);
                g.star(feet.add(0,.13,0),X,Z,r*.85,6,0,0xffe4a1,.6);
            }
            case DESTRUCTION -> {
                // A crown of tilted cinder blades erupts out of radial fissures.
                double r=3.7*burst;
                for(int i=0;i<8;i++) {
                    double a=i*Math.PI/4;
                    Vec3 root=feet.add(radial(a,2.1*open,.2)),side=radial(a+Math.PI/2,.28,0);
                    Vec3 tip=root.add(radial(a,.7,2.6*after+.15));
                    g.face(0xcf483f,.18,root.subtract(side),root.add(side),tip,tip);
                    g.stroke(root,tip,.038,0xffc06b,.9);
                    Vec3 end=feet.add(radial(a,r,.12));
                    g.stroke(feet.add(0,.13,0),end,.025,0xff8b4d,.8);
                    g.stroke(end,end.add(radial(a+.6,.4,.2+age*.5)),.02,0xffe0a1,.8);
                }
                for(int i=0;i<3;i++) {
                    double radius=(.4+clamp(age*2-i*.14)*3.4);
                    g.arc(feet.add(0,.15+i*.25,0),X,Z,radius,age*.2,Math.PI*2,.035-i*.008,0xfca15b,after*.65);
                }
                g.ribbon(f->feet.add(radial(f*Math.PI*3-age*2,.3+f*1.3,f*3.7*after)),X,.12,0xff753f,.28);
            }
            case ERUDITION -> {
                // A tall suspended orrery converges only when the authoritative blast arrives.
                Vec3 core=feet.add(0,cast?2.4:3.6,0);
                double radius=cast?2.8*open:2.8+age*.5;
                for(int layer=0;layer<3;layer++) {
                    double a=layer*Math.PI/3;
                    Vec3 u=radial(a,1,0),v=new Vec3(-Math.sin(a)*.5,.866,Math.cos(a)*.5);
                    g.arc(core,u,v,radius*(1-layer*.13),age*(layer%2==0?.45:-.6),Math.PI*2,.02,0xc798fc,.65);
                }
                for(int i=0;i<6;i++) {
                    double a=i*Math.PI/3+age*.25;
                    Vec3 node=core.add(radial(a,radius,.2*Math.sin(a*2))),base=feet.add(radial(a,2.7,.15));
                    g.polygon(node,radial(a+Math.PI/2,1,0),Y,.28,4,Math.PI/4,.018,0xe9d2ff,.85);
                    g.stroke(node,core,.012,0x926ee0,.45);
                    if(!cast) {g.stroke(node,base,.065,0xd7b5ff,.8);g.star(base,X,Z,.6,6,age,0xe9dbff,.7);}
                }
                g.crystal(core,Y,X,cast?.25+age*.4:.35+age*.2,0xf0ddff,.7);
                if(!cast) {g.stroke(feet.add(0,.2,0),core.add(0,2,0),.15,0x9e77f4,.4);g.stroke(feet.add(0,.2,0),core.add(0,2,0),.035,0xf4e8ff,.9);}
            }
            case HARMONY -> {
                if(cast) {
                    // Three golden-pink staves travel upward through a rotating spatial score.
                    double intro=clamp(1-age/1.4),radius=1.15+intro*1.25;
                    for(int row=0;row<3;row++) {
                        final int index=row;
                        g.ribbon(f->feet.add(radial(f*Math.PI*2+age*(index%2==0?.8:-.8),radius,.6+index*.55+Math.sin(f*Math.PI*2)*.35)),Y,.055,0xf6b0d8,.24);
                        g.arc(feet.add(0,.4+row*.55,0),X,Z,radius,age+row,Math.PI*1.5,.018,0xffd99c,.55);
                    }
                    for(int i=0;i<6;i++) {
                        double a=i*Math.PI/3+age*.6;
                        Vec3 node=feet.add(radial(a,radius,1.3+Math.sin(age*2+i)*.3));
                        g.crystal(node,Y,radial(a+Math.PI/2,1,0),.14,0xffdbaf,.55);
                    }
                } else {
                    g.ribbon(f->packet.from().lerp(packet.to(),f).add(0,Math.sin(f*Math.PI)*(.6+.1*Math.sin(age*3)),0),Y,.09,0xffb3db,.38);
                    g.star(packet.to(),X,Y,.6+age*.3,6,age*.4,0xffe2b1,.7);
                }
            }
            case NIHILITY -> {
                // An open black-violet eclipse, not an opaque screen or a solid fake obstacle.
                Vec3 core=feet.add(0,1.5,0);
                double radius=cast?1.25*open:1.25+age*.7;
                g.arc(core,X,Y,radius,-age*.45,Math.PI*1.75,.11,0x332346,.3);
                g.arc(core,X,Y,radius*.91,-age*.45,Math.PI*1.75,.02,0xc391f6,.75);
                for(int i=0;i<5;i++) {
                    final double a=i*Math.PI*2/5;
                    g.path(f->core.add(radial(a-f*1.8-age*.6,(.3+f*3.1)*open,-1.25+f*.8)),.018,0xad80e9,.55);
                    Vec3 edge=feet.add(radial(a,3.2,.3));
                    g.crystal(edge.add(0,.1+Math.sin(age*1.8+i)*.1,0),Y,radial(a+Math.PI/2,1,0),.18,0x8a63c2,.45);
                }
                if(!cast)g.star(core,X,Y,2*after+.1,8,-age,0xd7b9ff,.65);
            }
            case ABUNDANCE -> {
                Vec3 center=cast?feet.add(0,.15,0):packet.to();
                double radius=cast?1.8*open:.5+age*.5;
                for(int tier=0;tier<2;tier++)for(int i=0;i<6;i++) {
                    double a=i*Math.PI/3+tier*Math.PI/6+age*.13;
                    Vec3 tip=center.add(radial(a,radius*(tier==0?1:.7),tier==0?.65:1.15)),mid=center.lerp(tip,.5),side=radial(a+Math.PI/2,.28,0);
                    g.face(tier==0?0x70d9a3:0xd8e5a0,.16,center,mid.add(side),tip,mid.subtract(side));
                    g.path(f->center.lerp(tip,f).add(0,Math.sin(f*Math.PI)*.3,0),.014,0xe9efb4,.7);
                    if(cast && i%2==0) {
                        final double angle=a;
                        g.ribbon(f->center.add(radial(angle+f*1.3+age*.35,radius*(1-f*.5),f*2.8)),radial(a,1,0),.06,0x9ee5ad,.22);
                    }
                }
                g.arc(center.add(0,.05,0),X,Z,radius*1.15,age*.2,Math.PI*2,.018,0xe5e7a1,.65);
                if(!cast)g.star(center,X,Y,radius*.75,6,age*.4,0xddffda,.65);
            }
            case REMEMBRANCE -> {
                // A moving hourglass cage, then mirrors fan away from the real recorded target.
                Vec3 center=body;
                double spread=cast?1:1+age*1.2;
                for(int i=0;i<6;i++) {
                    double a=i*Math.PI/3+age*.12;
                    Vec3 side=radial(a+Math.PI/2,1,0),mid=center.add(radial(a,.95*spread,0));
                    Vec3 top=mid.add(0,.85,0),bottom=mid.add(0,-.85,0),left=mid.subtract(side.scale(.23)),right=mid.add(side.scale(.23));
                    g.face(0x81b8d9,.07,top,left,bottom,right);
                    g.stroke(top,left,.019,0xc6f6ff,.8);g.stroke(left,bottom,.019,0x86d5ef,.7);
                    g.stroke(bottom,right,.019,0xc6f6ff,.8);g.stroke(right,top,.019,0x86d5ef,.7);
                    if(!cast)g.stroke(mid,center,.014,0xd9faff,.6);
                }
                g.polygon(center.add(0,1,0),X,Z,1.1*spread,6,age*.12,.02,0xc0efff,.7);
                g.polygon(center.add(0,-1,0),X,Z,1.1*spread,6,age*.12,.02,0x90d2f5,.7);
                if(cast) {
                    double scan=(age%1.5)/1.5;
                    g.arc(center.add(0,-.8+scan*1.6,0),X,Z,.7,0,Math.PI*2,.012,0xdcfaff,.45);
                } else if(packet.hit())g.star(center,X,Y,1.8*burst,8,Math.PI/8,0xe0faff,.8);
            }
            case ELATION -> {
                if(cast) {
                    for(int i=0;i<beats;i++) {
                        double a=i*Math.PI*2/3+age*.6;
                        Vec3 point=body.add(radial(a,1.2,.4+Math.sin(age*2+i)*.12));
                        g.polygon(point,radial(a+Math.PI/2,1,0),Y,.25,4,age*.5,.022,0xffc77d,.8);
                        g.crystal(point,Y,radial(a+Math.PI/2,1,0),.12,0xff95c8,.6);
                        g.path(f->point.add(radial(a+f*2,.22,-f*.5)),.015,0xfbb9df,.5);
                    }
                } else {
                    Vec3 center=packet.to().add(0,.6,0);
                    int count=packet.hit()?10:5;
                    for(int i=0;i<count;i++) {
                        final double a=i*Math.PI*2/count;
                        int color=i%3==0?0xffd88b:i%3==1?0xff8ac3:0xa5d9ff;
                        g.ribbon(f->center.add(new Vec3(Math.cos(a)*f,Math.sin(a)*f,Math.sin(f*Math.PI)*.25).scale((packet.hit()?3:1.1)*burst)),Z,.075,color,.45);
                        Vec3 confetti=center.add(Math.cos(a)*(age*1.7+.2),Math.sin(a)*(age*1.7+.2)-age*age*.5,Math.sin(a*3)*.35);
                        g.crystal(confetti,new Vec3(Math.sin(age*4+i),Math.cos(age*4+i),0),Z,.075,color,.65);
                    }
                }
            }
            default -> {}
        }
    }
    private static void hunt(PathVisualGeometry g,ActiveSkillFxPacket packet,double age,double open) {
        Vec3 axis=packet.to().subtract(packet.from()).normalize();if(axis.lengthSqr()<.01)return;
        Vec3 side=axis.cross(Y).normalize();if(side.lengthSqr()<.01)side=X;
        Vec3 up=side.cross(axis).normalize(),u=side,v=up;
        // Gates and parallel arrow vanes leave the instant-hit axis readable from the side AND the caster.
        for(int i=0;i<3;i++) {
            Vec3 gate=packet.from().lerp(packet.to(),(i+1)/4.0);
            g.polygon(gate,u,v,(.25+i*.1)*open,4,Math.PI/4+age*.12,.018,0x9dedff,.5);
            for(int j=0;j<4;j++) {
                Vec3 dir=u.scale(Math.cos(j*Math.PI/2)).add(v.scale(Math.sin(j*Math.PI/2)));
                Vec3 root=gate.add(dir.scale(.32+i*.1));
                g.stroke(root.subtract(axis.scale(.5)),root,.024,0x53c9ef,.8);
                g.stroke(root,root.subtract(axis.scale(.13)).add(dir.scale(.1)),.016,0xe6fbff,.85);
            }
        }
        Vec3 a=packet.from(),b=packet.to();
        g.ribbon(f->a.lerp(b,f).add(u.scale(Math.sin(f*Math.PI*3-age*3)*.16)),v,.085,0x55bfe8,.24);
        if(packet.hit()) {
            double r=.45+clamp(age/.65)*.85;
            g.arc(b,u,v,r,age*.4,Math.PI*2,.026,0x8ce8ff,.65);
            g.star(b,u,v,r*1.25,8,Math.PI/8,0xc4f6ff,.85);
            for(int i=0;i<4;i++) {
                double angle=i*Math.PI/2+Math.PI/4;
                Vec3 dir=u.scale(Math.cos(angle)).add(v.scale(Math.sin(angle))),node=b.add(dir.scale(r*.8));
                g.crystal(node,dir,axis,.16,0x97e8ff,.65);
            }
        }
    }
}
