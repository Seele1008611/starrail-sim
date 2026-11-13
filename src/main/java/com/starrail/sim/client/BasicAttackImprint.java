package com.starrail.sim.client;

import com.starrail.sim.StarRailPath;
import net.minecraft.world.phys.Vec3;
import static com.starrail.sim.client.PathVisualGeometry.*;

/** Eight compact hit-seal timelines. Remembrance keeps its existing separate record/echo effect. */
final class BasicAttackImprint {
    static final double LIFE=.76;
    static void draw(StarRailPath path,double age,int detail,Vec3 camera,int main,int shade,int accent,MonsterCombatEffects.Sink sink) {
        if(age<0 || age>=LIFE || !path.isRealPath() || path==StarRailPath.REMEMBRANCE)return;
        double reveal=smooth(age/.09),fade=smooth((LIFE-age)/.27),scatter=smooth((age-.3)/.46);
        PathVisualGeometry g=new PathVisualGeometry(sink,camera,detail,PathSkillSpectacle.material(path),age*3,reveal*fade,detail<=12?150:280);
        Vec3 center=Vec3.ZERO;
        switch(path) {
            case PRESERVATION -> {
                double radius=.27+.035*reveal;
                g.polygon(center,X,Y,radius,6,Math.PI/6,.018,main,.85);
                g.polygon(center.add(0,0,-.015),X,Y,radius*.7,6,Math.PI/6,.009,accent,.7);
                // Six gold panes break away from a central protective crest.
                for(int i=0;i<6;i++) {
                    double a=i*Math.PI/3;
                    Vec3 dir=new Vec3(Math.cos(a),Math.sin(a),0),node=dir.scale(radius+scatter*.16);
                    g.crystal(node,dir,new Vec3(-dir.y,dir.x,0),.055,main,.6);
                    g.stroke(dir.scale(.12),dir.scale(.21),.012,accent,.65);
                }
                g.stroke(new Vec3(-.1,.08,-.02),new Vec3(0,-.13,-.02),.022,0xffe8af,.9);
                g.stroke(new Vec3(0,-.13,-.02),new Vec3(.1,.08,-.02),.022,0xffe8af,.9);
            }
            case DESTRUCTION -> {
                for(int i=0;i<3;i++) {
                    double x=(i-1)*.13;
                    Vec3 a=new Vec3(x-.14,-.24,0),b=new Vec3(x+.02,-.03,-.02),c=new Vec3(x-.04,.06,-.02),d=new Vec3(x+.16,.28,0);
                    g.stroke(a,b,.04,shade,.45);g.stroke(b,c,.04,shade,.45);g.stroke(c,d,.04,shade,.45);
                    g.stroke(a,b,.014,main,.9);g.stroke(b,c,.014,accent,.85);g.stroke(c,d,.014,main,.9);
                }
                for(int i=0;i<5;i++) {
                    double a=i*2.3999;Vec3 node=new Vec3(Math.cos(a)*(.18+scatter*.26),Math.sin(a)*(.18+scatter*.26)-scatter*.08,-.05);
                    g.crystal(node,Y,X,.035,accent,.8);
                }
            }
            case HUNT -> {
                double split=.03+scatter*.18;
                for(int i=-1;i<=1;i++) {
                    double d=i*split;
                    Vec3 tail=new Vec3(-.27+d,-.21-d,-.01),tip=new Vec3(.27+d,.24-d,-.01);
                    g.stroke(tail,tip,.018,i==0?0xd9f8ff:main,.85);
                    g.stroke(tip,tip.add(-.15,-.015,0),.015,accent,.85);
                    g.stroke(tip,tip.add(-.015,-.15,0),.015,accent,.85);
                }
                for(int i=0;i<4;i++) {
                    double a=i*Math.PI/2;
                    Vec3 dir=new Vec3(Math.cos(a),Math.sin(a),0);
                    g.stroke(dir.scale(.32+scatter*.1),dir.scale(.38+scatter*.1),.012,main,.65);
                }
            }
            case ERUDITION -> {
                double turn=age*.6;
                g.polygon(center,X,Y,.25,4,turn+Math.PI/4,.014,main,.85);
                g.polygon(center.add(0,0,-.02),X,Y,.15,3,-turn,.012,accent,.85);
                for(int i=0;i<4;i++) {
                    double a=turn+i*Math.PI/2+Math.PI/4;
                    Vec3 node=new Vec3(Math.cos(a)*(.32+scatter*.1),Math.sin(a)*(.32+scatter*.1),-.025);
                    g.crystal(node,Y,X,.045,accent,.65);
                    g.stroke(center,node,.008,main,.45);
                    Vec3 corner=node.add(i%2==0?.06:-.06,.06,0);
                    g.stroke(node,corner,.012,accent,.8);
                }
                g.arc(center,X,Y,.34,age*2,Math.PI*.75,.01,main,.5);
            }
            case HARMONY -> {
                for(int row=0;row<3;row++) {
                    final double y=(row-1)*.1;
                    g.path(f->new Vec3((f-.5)*.65,y+Math.sin(f*Math.PI*2-age*8)*.035,0),.01,row==1?accent:main,.65);
                }
                for(int i=0;i<3;i++) {
                    double x=(i-1)*.18,phase=i*2+age*5;
                    Vec3 note=new Vec3(x,Math.sin(phase)*.1+scatter*.12,-.04);
                    g.crystal(note,new Vec3(.45,1,0).normalize(),X,.06,i==1?accent:main,.7);
                    g.stroke(note,note.add(0,.14,0),.011,accent,.75);
                }
                g.arc(center,X,Y,.34+scatter*.09,-age,Math.PI*.7,.012,main,.5);
            }
            case NIHILITY -> {
                double radius=.3-scatter*.11;
                for(int i=0;i<3;i++)g.arc(center,X,Y,radius,i*Math.PI*2/3-age*.7,Math.PI*.48,.023,main,.8);
                g.arc(center.add(0,0,-.01),X,Y,radius*.74,age,Math.PI*1.4,.012,0xc2a0ef,.7);
                for(int i=0;i<4;i++) {
                    final double start=i*Math.PI/2;
                    g.path(f->new Vec3(Math.cos(start-f*1.3)*(.08+f*.24)*(1-scatter*.4),Math.sin(start-f*1.3)*(.08+f*.24)*(1-scatter*.4),-.03),.009,accent,.75);
                }
                g.crystal(center,Y,X,.07,shade,.8);
            }
            case ABUNDANCE -> {
                for(int i=0;i<5;i++) {
                    double a=i*Math.PI*2/5+Math.PI/2;
                    Vec3 dir=new Vec3(Math.cos(a),Math.sin(a),0),across=new Vec3(-Math.sin(a),Math.cos(a),0);
                    Vec3 root=dir.scale(.025+scatter*.055),tip=dir.scale(.3+scatter*.07),mid=root.lerp(tip,.55);
                    g.face(main,.3,root,mid.add(across.scale(.075)),tip,mid.subtract(across.scale(.075)));
                    g.stroke(root,tip,.012,accent,.9);
                    g.stroke(mid,mid.add(across.scale(.045)).add(dir.scale(.06)),.008,accent,.6);
                }
                g.crystal(center.add(0,0,-.03),Y,X,.07,0xe3f2ae,.7);
                g.arc(center,X,Y,.34+scatter*.06,-age*.2,Math.PI*1.5,.009,main,.55);
            }
            case ELATION -> {
                for(int i=0;i<4;i++) {
                    double a=i*Math.PI/2+age*.5;
                    Vec3 node=new Vec3(Math.cos(a)*(.18+scatter*.2),Math.sin(a)*(.18+scatter*.2)-scatter*scatter*.1,-.03);
                    Vec3 axis=new Vec3(Math.cos(a+age*3),Math.sin(a+age*3),0),side=new Vec3(-axis.y,axis.x,0);
                    int color=i%2==0?main:accent;
                    g.crystal(node,axis,side,.07,color,.75);
                    g.stroke(node.subtract(axis.scale(.13)),node.subtract(axis.scale(.08)),.012,color,.7);
                }
                g.star(center,X,Y,.23,4,Math.PI/4,0xffe8b4,.85);
                g.arc(center,X,Y,.3+scatter*.1,age*2,Math.PI*.7,.012,main,.6);
            }
            default -> {}
        }
    }
}
