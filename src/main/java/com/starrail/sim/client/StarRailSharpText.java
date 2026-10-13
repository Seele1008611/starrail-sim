package com.starrail.sim.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.awt.Font;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/** Device pixel text for the 2D preview, with a bounded texture cache and vanilla fallback. */
public final class StarRailSharpText extends SimplePreparableReloadListener<Font> {
    public static final StarRailSharpText INSTANCE = new StarRailSharpText();
    private static final ResourceLocation FILE = new ResourceLocation("starrail_sim", "font/source_han_sans_cn_regular.otf");
    private static final ResourceLocation FONT_ID = new ResourceLocation("starrail_sim", "source_han_sans");
    private static final float EM_SIZE = 9.5F;
    private static final long MAX_BYTES = 16 * 1024 * 1024;
    private static final LinkedHashMap<Key, Line> CACHE = new LinkedHashMap<>(32, .75F, true);
    private static Font face;
    private static long cacheBytes;
    private static boolean warned;
    private record AdvanceKey(int codepoint, int style, int deviceSize64) { }
    private static final LinkedHashMap<AdvanceKey, Float> ADVANCES = new LinkedHashMap<>(64, .75F, true);
    private static final java.awt.font.FontRenderContext METRICS = new java.awt.font.FontRenderContext(null,
            java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON, java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

    private record Key(List<StarRailTextRasterizer.Run> runs, int deviceSize64) { }
    private record Line(ResourceLocation texture, int width, int height,
            int inkX, int inkY, int inkWidth, int inkHeight) {
        long bytes() { return (long) width * height * 4; }
    }

    private StarRailSharpText() { }

    @Override
    protected Font prepare(ResourceManager manager, ProfilerFiller profiler) {
        try (InputStream stream = manager.open(FILE)) {
            return Font.createFont(Font.TRUETYPE_FONT, stream);
        } catch (Exception | LinkageError exception) {
            LogUtils.getLogger().warn("Device pixel font unavailable; retaining vanilla font fallback", exception);
            return null;
        }
    }

    @Override
    protected void apply(Font prepared, ResourceManager manager, ProfilerFiller profiler) {
        clearCache();
        StarRailGlyphCenter.clearCache();
        face = prepared;
        warned = false;
        StarRailUiFonts.updateGlobalFont();
    }

    public static boolean available() { return face != null; }

    public static boolean supports(int codepoint) {
        return face != null && Character.getType(codepoint) != Character.PRIVATE_USE && face.canDisplay(codepoint);
    }

    /** The same device size and styled font used by the rasterizer also drive wrapping/cursors. */
    public static float advance(int codepoint, Style style, float density) {
        if (!supports(codepoint)) return -1;
        int flags = (style.isBold() ? Font.BOLD : 0) | (style.isItalic() ? Font.ITALIC : 0);
        int size64 = Math.round(EM_SIZE * density * 64);
        AdvanceKey key = new AdvanceKey(codepoint, flags, size64);
        Float pixels = ADVANCES.get(key);
        if (pixels == null) {
            pixels = (float) face.deriveFont(flags, size64 / 64F)
                    .getStringBounds(new String(Character.toChars(codepoint)), METRICS).getWidth();
            ADVANCES.put(key, pixels);
            if (ADVANCES.size() > 2048) ADVANCES.remove(ADVANCES.keySet().iterator().next());
        }
        return pixels / density;
    }

    /** x/y denote the visible top-left, horizontal center/top, or visible center respectively. */
    public static boolean draw(GuiGraphics graphics, Component text, float x, float y, int argb,
            boolean centerX, boolean centerY) {
        if (face == null) return false;
        var matrix = graphics.pose().last().pose();
        // The preview uses axis-aligned uniform transforms. Rotated/anisotropic text keeps its native path.
        if (Math.abs(matrix.m01()) > .0001F || Math.abs(matrix.m10()) > .0001F
                || matrix.m00() <= 0 || Math.abs(matrix.m00() - matrix.m11()) > .0001F) return false;
        float gui = (float) Minecraft.getInstance().getWindow().getGuiScale();
        float density = gui * matrix.m00();
        if (density < .05F || density > 16) return false;
        List<StarRailTextRasterizer.Run> runs = new ArrayList<>();
        boolean[] supported = {true};
        int alpha = argb >>> 24;
        if (alpha == 0) alpha = 255; // Match Font's treatment of 24-bit colors.
        final int opacity = alpha;
        text.visit((style, part) -> {
            if (part.isEmpty()) return Optional.<Void>empty();
            if (style.isObfuscated() || part.indexOf('\n') >= 0 || !part.codePoints().allMatch(StarRailSharpText::supports)
                    || (!style.getFont().equals(Style.DEFAULT_FONT) && !style.getFont().equals(FONT_ID)))
                supported[0] = false;
            int rgb = style.getColor() == null ? argb & 0xFFFFFF : style.getColor().getValue();
            int fontStyle = (style.isBold() ? Font.BOLD : 0) | (style.isItalic() ? Font.ITALIC : 0);
            runs.add(new StarRailTextRasterizer.Run(part, opacity << 24 | rgb, fontStyle,
                    style.isUnderlined(), style.isStrikethrough()));
            return Optional.<Void>empty();
        }, Style.EMPTY);
        if (!supported[0]) return false;
        if (runs.isEmpty() || text.getString().isBlank()) return true;
        Key key = new Key(List.copyOf(runs), Math.round(EM_SIZE * density * 64));
        Line line = CACHE.get(key);
        if (line == null) {
            try {
                line = create(key);
                if (line == null) return true;
                CACHE.put(key, line);
                cacheBytes += line.bytes();
                while (CACHE.size() > 128 || cacheBytes > MAX_BYTES) {
                    var oldest = CACHE.entrySet().iterator();
                    var entry = oldest.next();
                    cacheBytes -= entry.getValue().bytes();
                    Minecraft.getInstance().getTextureManager().release(entry.getValue().texture());
                    oldest.remove();
                }
            } catch (RuntimeException exception) {
                if (!warned) LogUtils.getLogger().warn("Device pixel text failed; using native fallback", exception);
                warned = true;
                return false;
            }
        }
        float anchorX = (matrix.m00() * x + matrix.m30()) * gui;
        float anchorY = (matrix.m11() * y + matrix.m31()) * gui;
        float offsetX = line.inkX() + (centerX ? line.inkWidth() / 2F : 0);
        float offsetY = line.inkY() + (centerY ? line.inkHeight() / 2F : 0);
        float pixelX = Math.round(anchorX - offsetX), pixelY = Math.round(anchorY - offsetY);
        graphics.flush();
        float[] previous = RenderSystem.getShaderColor().clone();
        boolean blending = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_BLEND);
        int srcRgb = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL14.GL_BLEND_SRC_RGB);
        int dstRgb = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL14.GL_BLEND_DST_RGB);
        int srcAlpha = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL14.GL_BLEND_SRC_ALPHA);
        int dstAlpha = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL14.GL_BLEND_DST_ALPHA);
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(x + (pixelX - anchorX) / density, y + (pixelY - anchorY) / density, 0);
            graphics.pose().scale(1 / density, 1 / density, 1);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
            // Every texel maps to exactly one framebuffer pixel. No reduced image is stretched afterward.
            graphics.blit(line.texture(), 0, 0, 0, 0, line.width(), line.height(), line.width(), line.height());
        } finally {
            graphics.pose().popPose();
            RenderSystem.setShaderColor(previous[0], previous[1], previous[2], previous[3]);
            RenderSystem.blendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
            if (!blending) RenderSystem.disableBlend();
        }
        return true;
    }

    private static Line create(Key key) {
        var raster = StarRailTextRasterizer.rasterize(face, key.runs(), key.deviceSize64() / 64F);
        if (raster == null) return null;
        var image = raster.image();
        NativeImage pixels = new NativeImage(image.getWidth(), image.getHeight(), false);
        DynamicTexture texture = null;
        try {
            for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int abgr = argb & 0xFF00FF00 | (argb >>> 16 & 255) | (argb & 255) << 16;
                pixels.setPixelRGBA(x, y, abgr);
            }
            texture = new DynamicTexture(pixels);
            texture.setFilter(false, false);
            ResourceLocation id = Minecraft.getInstance().getTextureManager().register("starrail_sim_sharp_text", texture);
            var ink = raster.ink();
            return new Line(id, image.getWidth(), image.getHeight(), ink.x, ink.y, ink.width, ink.height);
        } catch (RuntimeException exception) {
            if (texture == null) pixels.close(); else texture.close();
            throw exception;
        }
    }

    private static void clearCache() {
        for (Line line : CACHE.values()) Minecraft.getInstance().getTextureManager().release(line.texture());
        CACHE.clear();
        ADVANCES.clear();
        cacheBytes = 0;
    }
}
