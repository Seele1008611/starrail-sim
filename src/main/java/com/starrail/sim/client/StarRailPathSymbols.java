package com.starrail.sim.client;

import com.starrail.sim.StarRailPath;
import net.minecraft.client.gui.GuiGraphics;

/** Small native path emblems, independent of fonts and screen resolution. */
final class StarRailPathSymbols {
    private StarRailPathSymbols() { }
    static void draw(GuiGraphics g, StarRailPath path, int x, int y, int radius, int color) {
        g.drawManaged(() -> {
            switch (path) {
                case PRESERVATION -> polygon(g, x, y, radius, color,
                        -1,-.65, 0,-1, 1,-.65, .85,.35, 0,1, -.85,.35, -1,-.65);
                case DESTRUCTION -> {
                    polygon(g, x,y,radius,color,-.8,-.8,0,-.35,.65,-1,.35,-.1,1,.65,.15,.35,-.4,1,-.35,.1,-1,-.15,-.8,-.8);
                    stroke(g,x,y,radius,color,-.75,.75,.7,-.7);
                }
                case HUNT -> {
                    stroke(g,x,y,radius,color,-1,.8,.9,-.8);
                    polygon(g,x,y,radius,color,-.2,-.9,1,-1,.8,.2);
                    stroke(g,x,y,radius,color,-1,0,-.45,.5);
                }
                case ERUDITION -> {
                    polygon(g,x,y,radius,color,0,-1,1,0,0,1,-1,0,0,-1);
                    stroke(g,x,y,radius,color,-.55,0,.55,0);
                    stroke(g,x,y,radius,color,0,-.55,0,.55);
                }
                case HARMONY -> {
                    for (int i=0;i<6;i++) {
                        double a=i*Math.PI/3;
                        stroke(g,x,y,radius,color,Math.cos(a)*.25,Math.sin(a)*.25,Math.cos(a),Math.sin(a));
                    }
                    StarRailCosmicUi.orbit(g,x,y,radius*.45,color);
                }
                case NIHILITY -> {
                    StarRailCosmicUi.orbit(g,x,y,radius,color);
                    StarRailCosmicUi.orbit(g,x,y,radius*.45,color);
                    stroke(g,x,y,radius,color,-.8,.8,.8,-.8);
                }
                case ABUNDANCE -> {
                    stroke(g,x,y,radius,color,0,-.8,0,1);
                    polygon(g,x,y,radius,color,0,.2,-1,-.5,-.5,-.8,0,.2,1,-.5,.5,-.8,0,.2);
                    stroke(g,x,y,radius,color,-.7,.8,0,.4);
                    stroke(g,x,y,radius,color,0,.4,.7,.8);
                }
                case REMEMBRANCE -> {
                    polygon(g,x,y,radius,color,0,-1,.65,-.25,.65,.45,0,1,-.65,.45,-.65,-.25,0,-1);
                    stroke(g,x,y,radius,color,0,-1,0,1);
                    stroke(g,x,y,radius,color,-.65,-.25,.65,.45);
                }
                case ELATION -> {
                    polygon(g,x,y,radius,color,-1,-.5,-.75,.55,0,1,.75,.55,1,-.5,-1,-.5);
                    stroke(g,x,y,radius,color,-.65,-.1,-.25,-.1);
                    stroke(g,x,y,radius,color,.25,-.1,.65,-.1);
                    polygon(g,x,y,radius,color,-.45,.35,0,.6,.45,.35);
                }
                default -> { }
            }
        });
    }
    private static void polygon(GuiGraphics g,int x,int y,int r,int color,double... p) {
        for(int i=0;i+3<p.length;i+=2) stroke(g,x,y,r,color,p[i],p[i+1],p[i+2],p[i+3]);
    }
    private static void stroke(GuiGraphics g,int x,int y,int r,int color,double ax,double ay,double bx,double by) {
        StarRailCosmicUi.line(g,x+ax*r,y+ay*r,x+bx*r,y+by*r,color);
    }
}
