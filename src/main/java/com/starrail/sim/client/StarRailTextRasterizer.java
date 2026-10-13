package com.starrail.sim.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.util.List;

/** Rasterize at the final device size. No Minecraft or OpenGL state is involved here. */
public final class StarRailTextRasterizer {
    public record Run(String text, int argb, int style, boolean underline, boolean strike) { }
    public record Raster(BufferedImage image, Rectangle ink, float advance) { }
    private static final int PADDING = 2;
    private static final FontRenderContext CONTEXT = new FontRenderContext(null,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

    private StarRailTextRasterizer() { }

    public static Raster rasterize(Font base, List<Run> runs, float deviceSize) {
        StringBuilder text = new StringBuilder();
        for (Run run : runs) text.append(run.text());
        if (text.isEmpty() || text.toString().isBlank()) return null;
        AttributedString attributed = new AttributedString(text.toString());
        int offset = 0;
        for (Run run : runs) {
            int end = offset + run.text().length();
            if (end == offset) continue;
            attributed.addAttribute(TextAttribute.FONT, base.deriveFont(run.style(), deviceSize), offset, end);
            attributed.addAttribute(TextAttribute.FOREGROUND, new Color(run.argb(), true), offset, end);
            if (run.underline()) attributed.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, offset, end);
            if (run.strike()) attributed.addAttribute(TextAttribute.STRIKETHROUGH, true, offset, end);
            offset = end;
        }
        TextLayout layout = new TextLayout(attributed.getIterator(), CONTEXT);
        // Some JDKs estimate getPixelBounds by painting against white; white foreground
        // then reports an empty box. Outline bounds are independent of text color.
        Rectangle bounds = layout.getBounds().getBounds();
        int width = bounds.width + PADDING * 2, height = bounds.height + PADDING * 2;
        if (width > 4096 || height > 256) throw new IllegalArgumentException("Text raster exceeds UI limits");
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            layout.draw(graphics, PADDING - bounds.x, PADDING - bounds.y);
        } finally { graphics.dispose(); }
        // Layout bounds may contain transparent padding, which must not affect visual centering.
        int minX = width, minY = height, maxX = -1, maxY = -1;
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
            if ((image.getRGB(x, y) >>> 24) == 0) continue;
            minX = Math.min(minX, x); minY = Math.min(minY, y);
            maxX = Math.max(maxX, x); maxY = Math.max(maxY, y);
        }
        if (maxX < minX) return null;
        return new Raster(image, new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1),
                layout.getAdvance());
    }
}
