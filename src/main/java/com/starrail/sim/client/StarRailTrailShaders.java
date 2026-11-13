package com.starrail.sim.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.StarRailStatusConfig;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;

/** Dedicated flowing blade material; registered instances are owned by Minecraft. */
public final class StarRailTrailShaders {
    private static ShaderInstance shader;
    private static boolean failed;
    private StarRailTrailShaders() { }
    public static void register(RegisterShadersEvent event) {
        shader=null;failed=false;StarRailHandTrails.clear();
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                new ResourceLocation(StarRailSimMod.MOD_ID,"hand_trail"),DefaultVertexFormat.POSITION_COLOR_TEX),loaded->{
                shader=loaded;
                if(loaded.getUniform("TrailTime")==null||loaded.getUniform("EmissionPass")==null)
                    disable(new IllegalStateException("Missing hand trail shader uniforms"));
            });
        } catch(Exception exception) {disable(exception);}
    }
    public static boolean enhanced() {return shader!=null&&!failed&&StarRailStatusConfig.HAND_TRAIL_SHADERS.get();}
    public static ShaderInstance current() {return enhanced()?shader:GameRenderer.getPositionColorShader();}
    public static void prepare(float time,boolean emission) {
        if(!enhanced())return;
        shader.safeGetUniform("TrailTime").set(time);
        shader.safeGetUniform("EmissionPass").set(emission?1F:0F);
    }
    public static void disable(Exception exception) {
        if(!failed)LogUtils.getLogger().warn("Hand trail material unavailable; using base ribbons until resource reload",exception);
        failed=true;
    }
}
