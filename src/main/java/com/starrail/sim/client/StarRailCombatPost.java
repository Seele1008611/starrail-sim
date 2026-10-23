package com.starrail.sim.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.logging.LogUtils;
import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.StarRailStatusConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.util.List;

/** Selective HDR bloom and depth-gated world-anchored refraction, before the HUD. */
public final class StarRailCombatPost {
    /** style: 0 Hunt, 1 Preservation, 2 Abundance, then the remaining six paths. */
    public record Impulse(float x,float y,float depth,float radius,float phase,float strength,
                          float red,float green,float blue,float style) { }
    private static final long BUDGET=96L*1024*1024;
    private static ShaderInstance blur,composite;
    private static TextureTarget scene,emission,ping,pong;
    private static boolean failed,capturing,stencil;
    private static int lastPasses;
    private static long allocatedBytes;

    private StarRailCombatPost() { }

    public static void register(RegisterShadersEvent event) {
        release();failed=false;blur=null;composite=null;
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    new ResourceLocation(StarRailSimMod.MOD_ID,"combat_blur"),DefaultVertexFormat.POSITION_TEX),s->{
                        blur=s;if(s.getUniform("TexelStep")==null||s.getUniform("BlurRadius")==null)
                            fail(new IllegalStateException("Missing blur quality uniforms"));
                    });
            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    new ResourceLocation(StarRailSimMod.MOD_ID,"combat_composite"),DefaultVertexFormat.POSITION_TEX),s->{
                        composite=s;
                        for(String uniform:List.of("ScreenSize","Strength","Impact0","Impact1","Impact2","Impact3",
                                "ImpactShape0","ImpactShape1","ImpactShape2","ImpactShape3"))
                            if(s.getUniform(uniform)==null)fail(new IllegalStateException("Missing composite uniform: "+uniform));
                        for(int i=0;i<4;i++)if(s.getUniform("ImpactColor"+i)==null)
                            fail(new IllegalStateException("Missing composite impact color: "+i));
                    });
        } catch(Exception exception) { fail(exception); }
    }

    public static boolean available() { return !failed&&blur!=null&&composite!=null; }
    static int lastPasses() { return lastPasses; }
    static double allocatedMiB() { return allocatedBytes/(1024D*1024); }
    static void idle() { lastPasses=0; }
    public static void bindGeometryOutput() {
        (capturing?emission:Minecraft.getInstance().getMainRenderTarget()).bindWrite(true);
    }

    private static TextureTarget target(int w,int h,boolean depth,boolean hdr,boolean hasStencil) {
        TextureTarget target=new TextureTarget(w,h,depth,Minecraft.ON_OSX);
        try {
            if(depth&&hasStencil)target.enableStencil();
            if(hdr) {
                RenderSystem.bindTexture(target.getColorTextureId());
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL30.GL_RGBA16F,w,h,0,GL11.GL_RGBA,GL30.GL_HALF_FLOAT,(ByteBuffer)null);
            }
            target.setFilterMode(GL11.GL_LINEAR);
            target.setClearColor(0,0,0,0);target.bindWrite(false);target.checkStatus();
            return target;
        } catch(RuntimeException exception) { target.destroyBuffers();throw exception; }
    }

    private static void ensure(RenderTarget main) {
        ParticleStatus quality=Minecraft.getInstance().options.particles().get();
        double qualityScale=quality==ParticleStatus.MINIMAL?.5:quality==ParticleStatus.DECREASED?.75:1;
        double scale=Math.min(qualityScale,Math.sqrt(1920D*1080/(main.width*(double)main.height)));
        int w=Math.max(1,(int)Math.ceil(main.width*scale)),h=Math.max(1,(int)Math.ceil(main.height*scale));
        int bw=Math.max(1,(w+1)/2),bh=Math.max(1,(h+1)/2);
        if(scene!=null&&scene.width==main.width&&scene.height==main.height&&emission.width==w&&emission.height==h
                &&stencil==main.isStencilEnabled())return;
        release();
        // Include conservative depth/stencil storage and two RGBA16F blur images.
        long bytes=4L*main.width*main.height+(main.isStencilEnabled()?16L:12L)*w*h+16L*bw*bh;
        if(bytes>BUDGET)throw new IllegalStateException("Combat post targets exceed 96 MiB budget");
        scene=target(main.width,main.height,false,false,false);
        emission=target(w,h,true,true,main.isStencilEnabled());
        ping=target(bw,bh,false,true,false);pong=target(bw,bh,false,true,false);
        stencil=main.isStencilEnabled();allocatedBytes=bytes;
    }

    private static void copyColor(RenderTarget from,RenderTarget to) {
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,from.frameBufferId);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,to.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0,0,from.width,from.height,0,0,to.width,to.height,GL11.GL_COLOR_BUFFER_BIT,GL11.GL_NEAREST);
    }

    /** Main geometry is already drawn; the second geometry submission captures selected path emission. */
    public static void render(Runnable drawEmission,List<Impulse> impulses) {
        lastPasses=0;
        if(!available()||!StarRailCombatShaders.enhanced())return;
        Minecraft mc=Minecraft.getInstance();RenderTarget main=mc.getMainRenderTarget();
        if(!main.useDepth||main.getDepthTextureId()<0)return;
        ShaderInstance previous=RenderSystem.getShader();
        int[] textures={RenderSystem.getShaderTexture(0),RenderSystem.getShaderTexture(1),RenderSystem.getShaderTexture(2)};
        int activeTexture=GL11.glGetInteger(GL30.GL_ACTIVE_TEXTURE);
        boolean oldDepth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),oldBlend=GL11.glIsEnabled(GL11.GL_BLEND),oldCull=GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean oldDepthWrite=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int[] viewport=new int[4];GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);
        boolean copied=false;
        try {
            ensure(main);copyColor(main,scene);copied=true;
            emission.clear(Minecraft.ON_OSX);emission.copyDepthFrom(main);
            int error=GL11.glGetError();
            if(error!=GL11.GL_NO_ERROR)throw new IllegalStateException("Combat target/depth copy GL error: "+error);
            capturing=true;StarRailCombatShaders.emission(true);
            drawEmission.run();
            capturing=false;StarRailCombatShaders.emission(false);
            RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableBlend();RenderSystem.disableCull();
            ParticleStatus quality=mc.options.particles().get();
            float blurRadius=quality==ParticleStatus.MINIMAL?2F:quality==ParticleStatus.DECREASED?4F:6F;
            blur.setSampler("SourceSampler",emission.getColorTextureId());
            blur.safeGetUniform("BlurRadius").set(blurRadius);
            blur.safeGetUniform("TexelStep").set(2F/emission.width,0F);
            fullscreen(ping,blur);
            blur.setSampler("SourceSampler",ping.getColorTextureId());
            blur.safeGetUniform("BlurRadius").set(blurRadius);
            blur.safeGetUniform("TexelStep").set(0F,1F/ping.height);
            fullscreen(pong,blur);
            composite.setSampler("SceneSampler",scene.getColorTextureId());
            composite.setSampler("BloomSampler",pong.getColorTextureId());
            composite.setSampler("DepthSampler",emission.getDepthTextureId());
            composite.safeGetUniform("ScreenSize").set((float)main.width,(float)main.height);
            composite.safeGetUniform("Strength").set(StarRailStatusConfig.COMBAT_STRENGTH.get().floatValue());
            for(int i=0;i<4;i++) {
                Impulse impulse=i<impulses.size()?impulses.get(i):new Impulse(0,0,1,0,0,0,0,0,0,0);
                composite.safeGetUniform("Impact"+i).set(impulse.x,impulse.y,impulse.phase,impulse.strength);
                composite.safeGetUniform("ImpactShape"+i).set(impulse.radius,impulse.depth);
                composite.safeGetUniform("ImpactColor"+i).set(impulse.red,impulse.green,impulse.blue,impulse.style);
            }
            fullscreen(main,composite);
        } catch(RuntimeException exception) {
            // Restore the exact unprocessed world if a pass fails after the snapshot.
            if(copied)copyColor(scene,main);
            fail(exception);
            release();
        } finally {
            capturing=false;StarRailCombatShaders.emission(false);
            main.bindWrite(true);
            for(int i=0;i<textures.length;i++)RenderSystem.setShaderTexture(i,textures[i]);
            RenderSystem.activeTexture(activeTexture);
            if(oldDepth)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();
            if(oldCull)RenderSystem.enableCull();else RenderSystem.disableCull();
            if(oldBlend)RenderSystem.enableBlend();else RenderSystem.disableBlend();
            RenderSystem.depthMask(oldDepthWrite);RenderSystem.viewport(viewport[0],viewport[1],viewport[2],viewport[3]);
            RenderSystem.defaultBlendFunc();RenderSystem.setShader(()->previous);
        }
    }

    private static void fullscreen(RenderTarget out,ShaderInstance shader) {
        out.bindWrite(true);RenderSystem.setShader(()->shader);
        BufferBuilder builder=Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);
        builder.vertex(-1,-1,0).uv(0,0).endVertex();builder.vertex(1,-1,0).uv(1,0).endVertex();
        builder.vertex(1,1,0).uv(1,1).endVertex();builder.vertex(-1,1,0).uv(0,1).endVertex();
        BufferUploader.drawWithShader(builder.end());lastPasses++;
    }

    private static void fail(Exception exception) {
        if(!failed)LogUtils.getLogger().warn("Combat post-processing disabled until resource reload; retaining direct meshes",exception);
        failed=true;
    }

    public static void release() {
        capturing=false;lastPasses=0;allocatedBytes=0;
        for(TextureTarget target:new TextureTarget[]{scene,emission,ping,pong})if(target!=null)target.destroyBuffers();
        scene=null;emission=null;ping=null;pong=null;
    }
}
