package com.starrail.sim.client;

import com.mojang.blaze3d.font.GlyphInfo;
import com.starrail.sim.StarRailStatusConfig;
import com.starrail.sim.mixin.client.FontAccessor;
import com.starrail.sim.mixin.client.MinecraftFontAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;

import java.util.List;

/** Shared measurements and native fallback for mod UI and the optional global font. */
public final class StarRailUiFonts {
    private static final ResourceLocation SOURCE = new ResourceLocation("starrail_sim", "source_han_sans");
    private static Font original, originalFishy;
    private static DisplayFont display, globalDisplay;
    private static SourceSet combined;

    private StarRailUiFonts() { }

    public static Font original() {
        ensure();
        return original;
    }

    private static void ensure() {
        if (original != null) return;
        Minecraft minecraft = Minecraft.getInstance();
        original = minecraft.font;
        originalFishy = minecraft.fontFilterFishy;
        combined = new SourceSet();
        display = new DisplayFont(false);
        globalDisplay = new DisplayFont(true);
    }

    public static Font forMod() {
        ensure();
        return StarRailStatusConfig.FONT_SCOPE.get() != StarRailStatusConfig.FontScope.ORIGINAL
                && StarRailSharpText.available() && !(Minecraft.getInstance().screen instanceof StarRailDisplayPreviewScreen)
                ? display : original;
    }

    public static void updateGlobalFont() {
        ensure();
        combined.metrics.clear();
        boolean global = StarRailStatusConfig.FONT_SCOPE.get() == StarRailStatusConfig.FontScope.GLOBAL
                && StarRailSharpText.available();
        var access = (MinecraftFontAccessor) (Object) Minecraft.getInstance();
        access.starrail$setFont(global ? globalDisplay : original);
        access.starrail$setFilteredFont(global ? globalDisplay : originalFishy);
    }

    public static boolean replaces(Font font) {
        if (Minecraft.getInstance().screen instanceof StarRailDisplayPreviewScreen) return false;
        return font instanceof DisplayFont selected && selected.enabled();
    }

    private static FontSet nativeSet(ResourceLocation id) {
        return ((FontAccessor) (Object) original).starrail$getFontSet(id);
    }

    private static boolean enabled(boolean global) {
        return StarRailSharpText.available() && (global
                ? StarRailStatusConfig.FONT_SCOPE.get() == StarRailStatusConfig.FontScope.GLOBAL
                : StarRailStatusConfig.FONT_SCOPE.get() != StarRailStatusConfig.FontScope.ORIGINAL);
    }

    private static FontSet set(ResourceLocation id, boolean global) {
        return id.equals(Style.DEFAULT_FONT) && enabled(global) ? combined : nativeSet(id);
    }

    private static float density() {
        Minecraft minecraft = Minecraft.getInstance();
        double scale = minecraft.getWindow().getGuiScale();
        if (minecraft.screen instanceof StarRailStyledScreen screen) scale *= screen.uiScaleFactor();
        return (float) Math.max(.05, scale);
    }

    private static final class DisplayFont extends Font {
        private final boolean global;
        private final StringSplitter metrics;
        DisplayFont(boolean global) {
            super(id -> set(id, global), false);
            this.global = global;
            metrics = new StringSplitter((cp, style) -> {
            float width = enabled() && (style.getFont().equals(Style.DEFAULT_FONT) || style.getFont().equals(SOURCE))
                    ? StarRailSharpText.advance(cp, style, density()) : -1;
            return width >= 0 ? width : set(style.getFont(), global).getGlyphInfo(cp, false).getAdvance(style.isBold());
            });
        }
        private boolean enabled() { return StarRailUiFonts.enabled(global); }
        @Override public int width(String text) { return (int) Math.ceil(metrics.stringWidth(text)); }
        @Override public int width(FormattedText text) { return (int) Math.ceil(metrics.stringWidth(text)); }
        @Override public int width(FormattedCharSequence text) { return (int) Math.ceil(metrics.stringWidth(text)); }
        @Override public StringSplitter getSplitter() { return metrics; }
        @Override public String plainSubstrByWidth(String text, int width) { return metrics.plainHeadByWidth(text, width, Style.EMPTY); }
        @Override public String plainSubstrByWidth(String text, int width, boolean tail) {
            return tail ? metrics.plainTailByWidth(text, width, Style.EMPTY) : plainSubstrByWidth(text, width);
        }
        @Override public FormattedText substrByWidth(FormattedText text, int width) {
            return metrics.headByWidth(text, width, Style.EMPTY);
        }
        @Override public List<FormattedCharSequence> split(FormattedText text, int width) {
            return metrics.splitLines(text, width, Style.EMPTY).stream()
                    .map(Language.getInstance()::getVisualOrder).toList();
        }
        @Override public int wordWrapHeight(FormattedText text, int width) { return split(text, width).size() * lineHeight; }
        @Override public int wordWrapHeight(String text, int width) {
            return metrics.splitLines(text, width, Style.EMPTY).size() * lineHeight;
        }
        private boolean draw2d(net.minecraft.network.chat.Component text, float x, float y, int color, Matrix4f pose,
                MultiBufferSource buffers, int background) {
            if (!enabled() || background != 0 || Math.abs(RenderSystem.getProjectionMatrix().m33() - 1) > .0001F
                    || !(buffers instanceof MultiBufferSource.BufferSource source)) return false;
            if (Minecraft.getInstance().screen instanceof StarRailDisplayPreviewScreen) return false;
            GuiGraphics graphics = new GuiGraphics(Minecraft.getInstance(), source);
            graphics.pose().mulPoseMatrix(pose);
            return StarRailSharpText.draw(graphics, text, x, y, color, false, false);
        }
        @Override public int drawInBatch(net.minecraft.network.chat.Component text, float x, float y, int color,
                boolean shadow, Matrix4f pose, MultiBufferSource buffers, DisplayMode mode, int background, int light) {
            return draw2d(text, x, y, color, pose, buffers, background)
                    ? (int) (x + width(text)) : super.drawInBatch(text, x, y, color, shadow, pose, buffers, mode, background, light);
        }
        @Override public int drawInBatch(String text, float x, float y, int color, boolean shadow,
                Matrix4f pose, MultiBufferSource buffers, DisplayMode mode, int background, int light) {
            return drawInBatch(text, x, y, color, shadow, pose, buffers, mode, background, light, isBidirectional());
        }
        @Override public int drawInBatch(String text, float x, float y, int color, boolean shadow,
                Matrix4f pose, MultiBufferSource buffers, DisplayMode mode, int background, int light, boolean bidi) {
            return draw2d(legacy(text), x, y, color, pose, buffers, background)
                    ? (int) (x + width(text)) : super.drawInBatch(text, x, y, color, shadow, pose, buffers, mode, background, light, bidi);
        }
        @Override public int drawInBatch(FormattedCharSequence text, float x, float y, int color, boolean shadow,
                Matrix4f pose, MultiBufferSource buffers, DisplayMode mode, int background, int light) {
            return draw2d(component(text), x, y, color, pose, buffers, background)
                    ? (int) (x + width(text)) : super.drawInBatch(text, x, y, color, shadow, pose, buffers, mode, background, light);
        }
    }

    public static net.minecraft.network.chat.Component legacy(String text) {
        return component(sink -> net.minecraft.util.StringDecomposer.iterateFormatted(text, Style.EMPTY, sink));
    }

    public static net.minecraft.network.chat.Component component(FormattedCharSequence sequence) {
        var result = net.minecraft.network.chat.Component.empty();
        StringBuilder run = new StringBuilder();
        Style[] previous = {null};
        sequence.accept((index, style, cp) -> {
            if (previous[0] != null && !previous[0].equals(style)) {
                result.append(net.minecraft.network.chat.Component.literal(run.toString()).setStyle(previous[0])); run.setLength(0);
            }
            previous[0] = style; run.appendCodePoint(cp); return true;
        });
        if (!run.isEmpty()) result.append(net.minecraft.network.chat.Component.literal(run.toString()).setStyle(previous[0]));
        return result;
    }

    /** Keep custom resource-pack icons in the original set rather than losing them to the CJK font. */
    private static final class SourceSet extends FontSet {
        private final java.util.LinkedHashMap<Integer, GlyphInfo> metrics = new java.util.LinkedHashMap<>();
        SourceSet() { super(Minecraft.getInstance().getTextureManager(), SOURCE); }
        private FontSet source(int cp) { return nativeSet(StarRailSharpText.supports(cp) ? SOURCE : Style.DEFAULT_FONT); }
        @Override public GlyphInfo getGlyphInfo(int cp, boolean fishy) {
            if (!StarRailSharpText.supports(cp)) return source(cp).getGlyphInfo(cp, fishy);
            GlyphInfo cached = metrics.get(cp);
            if (cached == null) {
                GlyphInfo nativeInfo = source(cp).getGlyphInfo(cp, fishy);
                cached = new GlyphInfo() {
                    @Override public float getAdvance() { return getAdvance(false); }
                    @Override public float getAdvance(boolean bold) {
                        return StarRailSharpText.advance(cp, Style.EMPTY.withBold(bold), density());
                    }
                    @Override public float getBoldOffset() { return nativeInfo.getBoldOffset(); }
                    @Override public float getShadowOffset() { return nativeInfo.getShadowOffset(); }
                    @Override public BakedGlyph bake(java.util.function.Function<com.mojang.blaze3d.font.SheetGlyphInfo, BakedGlyph> bake) {
                        return nativeInfo.bake(bake);
                    }
                };
                metrics.put(cp, cached);
                if (metrics.size() > 2048) metrics.remove(metrics.keySet().iterator().next());
            }
            return cached;
        }
        @Override public BakedGlyph getGlyph(int cp) { return source(cp).getGlyph(cp); }
        @Override public BakedGlyph getRandomGlyph(GlyphInfo info) { return nativeSet(Style.DEFAULT_FONT).getRandomGlyph(info); }
        @Override public BakedGlyph whiteGlyph() { return nativeSet(Style.DEFAULT_FONT).whiteGlyph(); }
    }
}
