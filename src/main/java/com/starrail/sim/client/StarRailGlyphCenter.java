package com.starrail.sim.client;

import com.mojang.blaze3d.font.GlyphInfo;
import com.starrail.sim.mixin.client.BakedGlyphAccessor;
import com.starrail.sim.mixin.client.BitmapGlyphAccessor;
import com.starrail.sim.mixin.client.FontAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.WeakHashMap;

/** Visible glyph bounds exclude advance spacing and transparent rows in bitmap fonts. */
public final class StarRailGlyphCenter {
    private record Bounds(float left, float top, float right, float bottom) { }
    private static final Map<GlyphInfo, Bounds> CACHE = new WeakHashMap<>();

    private StarRailGlyphCenter() { }
    static void clearCache() { CACHE.clear(); }

    public static void draw(GuiGraphics graphics, Font font, ResourceLocation id, int codepoint,
            float x, float y, float scale, int color) {
        var set = ((FontAccessor) (Object) font).starrail$getFontSet(id);
        GlyphInfo info = set.getGlyphInfo(codepoint, false);
        Bounds bounds = CACHE.get(info);
        if (bounds == null) {
            var glyph = (BakedGlyphAccessor) (Object) set.getGlyph(codepoint);
            float left = glyph.starrail$left(), right = glyph.starrail$right();
            float top = glyph.starrail$up() - 3, bottom = glyph.starrail$down() - 3;
            bounds = new Bounds(left, top, right, bottom);
            if (info instanceof BitmapGlyphAccessor bitmap && bitmap.starrail$width() > 0 && bitmap.starrail$height() > 0) {
                int w = bitmap.starrail$width(), h = bitmap.starrail$height();
                int minX = w, minY = h, maxX = -1, maxY = -1;
                for (int py = 0; py < h; py++) for (int px = 0; px < w; px++) {
                    int alpha = bitmap.starrail$image().getLuminanceOrAlpha(
                            bitmap.starrail$offsetX() + px, bitmap.starrail$offsetY() + py) & 255;
                    if (alpha == 0) continue;
                    minX = Math.min(minX, px); minY = Math.min(minY, py);
                    maxX = Math.max(maxX, px); maxY = Math.max(maxY, py);
                }
                if (maxX >= minX) bounds = new Bounds(left + (right - left) * minX / w,
                        top + (bottom - top) * minY / h, left + (right - left) * (maxX + 1) / w,
                        top + (bottom - top) * (maxY + 1) / h);
            }
            CACHE.put(info, bounds);
        }
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1);
        Component text = Component.literal(new String(Character.toChars(codepoint))).withStyle(style -> style.withFont(id));
        font.drawInBatch(text, -(bounds.left() + bounds.right()) / 2, -(bounds.top() + bounds.bottom()) / 2,
                color, false, graphics.pose().last().pose(), graphics.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
        graphics.pose().popPose();
    }
}
