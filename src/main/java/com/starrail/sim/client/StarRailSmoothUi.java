package com.starrail.sim.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import com.starrail.sim.StarRailSimMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;

import java.io.IOException;

/** Client-only distance field shapes, initially used by the visual comparison screen. */
public final class StarRailSmoothUi {
    private static ShaderInstance shader;

    private StarRailSmoothUi() { }

    public static void register(RegisterShadersEvent event) {
        shader = null;
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    new ResourceLocation(StarRailSimMod.MOD_ID, "smooth_ui"),
                    DefaultVertexFormat.POSITION_TEX), loaded -> shader = loaded);
        } catch (IOException | RuntimeException exception) {
            LogUtils.getLogger().warn("Smooth UI shader unavailable; using ordinary shapes", exception);
        }
    }

    public static boolean available() { return shader != null; }

    /** Filter only this decorative GUI image; restore its sampling for other renderers. */
    public static void image(GuiGraphics graphics, ResourceLocation texture, int x, int y,
            int width, int height, int sourceWidth, int sourceHeight) {
        graphics.flush();
        int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        net.minecraft.client.Minecraft.getInstance().getTextureManager().getTexture(texture).bind();
        int min = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER);
        int mag = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER);
        try {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            graphics.blit(texture,x,y,width,height,0F,0F,sourceWidth,sourceHeight,sourceWidth,sourceHeight);
            graphics.flush();
        } finally {
            net.minecraft.client.Minecraft.getInstance().getTextureManager().getTexture(texture).bind();
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, min);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, mag);
            RenderSystem.bindTexture(previous);
        }
    }

    /** A capsule keeps diagonal strokes at fractional coordinates with round, antialiased ends. */
    public static void line(GuiGraphics graphics, double x1, double y1, double x2, double y2,
            float stroke, int color) {
        float length = (float) Math.hypot(x2 - x1, y2 - y1);
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(x1, y1, 0);
            graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotation((float) Math.atan2(y2-y1, x2-x1)));
            roundedRect(graphics, -stroke/2, -stroke/2, length + stroke, stroke, stroke/2, 0, color, 0);
        } finally { graphics.pose().popPose(); }
    }

    public static void ellipse(GuiGraphics graphics, double cx, double cy, double rx, double ry, int color) {
        if (rx <= 0 || ry <= 0) return;
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(cx, cy, 0);
            graphics.pose().scale(1, (float)(ry/rx), 1);
            circle(graphics, 0, 0, (float)rx, 0, color, 0);
        } finally { graphics.pose().popPose(); }
    }

    public static void roundedRect(GuiGraphics graphics, float x, float y, float width,
            float height, float radius, float borderWidth, int fill, int border) {
        shape(graphics, x, y, width, height, radius, borderWidth, fill, border, 0, 7);
    }

    private static void shape(GuiGraphics graphics, float x, float y, float width,
            float height, float radius, float borderWidth, int fill, int border, float start, float length) {
        if (width <= 0 || height <= 0) return;
        ShaderInstance current = shader;
        if (current == null) {
            StarRailUiStyle.roundedOutline(graphics, Math.round(x), Math.round(y),
                    Math.round(width), Math.round(height), fill, border);
            return;
        }
        graphics.flush();
        ShaderInstance previousShader = RenderSystem.getShader();
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        int srcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        int dstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int srcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        int dstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        int equation = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_RGB);
        int alphaEquation = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_ALPHA);
        try {
            current.safeGetUniform("Size").set(width, height);
            current.safeGetUniform("Radius").set(Math.max(0, Math.min(radius, Math.min(width, height) / 2)));
            current.safeGetUniform("BorderWidth").set(Math.max(0, borderWidth));
            current.safeGetUniform("ArcStart").set(start);
            current.safeGetUniform("ArcLength").set(length);
            color(current, "FillColor", fill);
            color(current, "BorderColor", border);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(() -> current);
            // Allow outside coverage rather than clipping the antialiased edge at the quad.
            Matrix4f matrix = graphics.pose().last().pose();
            float scale = Math.min(matrix.getColumn(0, new org.joml.Vector4f()).length(),
                    matrix.getColumn(1, new org.joml.Vector4f()).length());
            float padding = 2 / Math.max(.01F, scale);
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            vertex(buffer, matrix, x, y, -padding, height + padding);
            vertex(buffer, matrix, x, y, width + padding, height + padding);
            vertex(buffer, matrix, x, y, width + padding, -padding);
            vertex(buffer, matrix, x, y, -padding, -padding);
            BufferUploader.drawWithShader(buffer.end());
        } finally {
            RenderSystem.setShader(() -> previousShader);
            RenderSystem.blendEquation(equation);
            if (alphaEquation != equation) GL20.glBlendEquationSeparate(equation, alphaEquation);
            RenderSystem.blendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
            if (blend) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
            if (depth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
        }
    }

    public static void circle(GuiGraphics graphics, float x, float y, float radius,
            float borderWidth, int fill, int border) {
        roundedRect(graphics, x - radius, y - radius, radius * 2, radius * 2,
                radius, borderWidth, fill, border);
    }

    public static void arc(GuiGraphics graphics, float x, float y, float radius, float start,
            float length, float stroke, int color) {
        if (length <= 0 || radius <= 0) return;
        float outer = radius + stroke / 2;
        shape(graphics, x - outer, y - outer, outer * 2, outer * 2, outer, stroke,
                0, color, start, length);
    }

    private static void color(ShaderInstance current, String name, int argb) {
        current.safeGetUniform(name).set((argb >> 16 & 255) / 255F, (argb >> 8 & 255) / 255F,
                (argb & 255) / 255F, (argb >>> 24) / 255F);
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, float u, float v) {
        buffer.vertex(matrix, x + u, y + v, 0).uv(u, v).endVertex();
    }
}
