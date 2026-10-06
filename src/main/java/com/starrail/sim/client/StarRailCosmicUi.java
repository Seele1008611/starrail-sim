package com.starrail.sim.client;

import net.minecraft.client.gui.GuiGraphics;
import java.util.Random;

/** Procedural decorations: no copied character art or additional downloads. */
public final class StarRailCosmicUi {
    private static final float[][] STARS = new float[520][4];
    static {
        Random random = new Random(71L);
        for (float[] star : STARS) {
            star[0] = random.nextFloat(); star[1] = random.nextFloat();
            star[2] = random.nextFloat() * 6.283185F; star[3] = 2F + random.nextFloat() * 3.5F;
        }
    }
    private StarRailCosmicUi() { }
    public static double seconds() { return System.nanoTime() / 1_000_000_000.0; }

    public static void backdrop(GuiGraphics graphics, int width, int height) {
        graphics.drawManaged(() -> backdropGeometry(graphics, width, height));
    }

    private static void backdropGeometry(GuiGraphics graphics, int width, int height) {
        graphics.fillGradient(0, 0, width, height, 0xFF080A1C, 0xFF13243A);
        double time = seconds();
        for (int i = 0; i < 9; i++) {
            int opacity = (int) (3 + 5 * (.5 + .5 * Math.sin(time / (14 + i % 3 * 5) + i * 1.8)));
            double driftX = Math.sin(time / (31 + i * 2) + i * 1.3) * width * .008;
            double driftY = Math.cos(time / (37 + i * 3) + i * 1.7) * height * .007;
            ellipse(graphics, width * ((i * .137 + .12) % 1) + driftX,
                    height * ((i * .193 + .24) % 1) + driftY,
                    width * .12, height * .055, opacity << 24 | 0x449AC0);
        }
        for (int i = 0; i < STARS.length; i++) {
            float[] star = STARS[i];
            int alpha = (int) (28 + 210 * (.5 + .5 * Math.sin(time * 6.283185 / star[3] + star[2])));
            int x = (int) (star[0] * width), y = (int) (star[1] * height);
            graphics.fill(x, y, x + 1, y + 1, alpha << 24 | 0xE7EFFF);
            if (i % 13 == 0) {
                graphics.fill(x - 1, y, x + 2, y + 1, (alpha / 4) << 24 | 0xBBDFFF);
                graphics.fill(x, y - 1, x + 1, y + 2, (alpha / 4) << 24 | 0xBBDFFF);
            }
        }
    }

    public static void ellipse(GuiGraphics graphics, double cx, double cy, double rx, double ry, int color) {
        for (int y = (int) -ry; y <= ry; y += 2) {
            double factor = Math.sqrt(Math.max(0, 1 - y * y / (ry * ry)));
            graphics.fill((int) (cx - rx * factor), (int) cy + y,
                    (int) (cx + rx * factor), (int) cy + y + 2, color);
        }
    }

    public static void line(GuiGraphics graphics, double x1, double y1, double x2, double y2, int color) {
        int steps = Math.max(1, (int) Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)));
        for (int i = 0; i <= steps; i++) {
            int x = (int) (x1 + (x2 - x1) * i / steps), y = (int) (y1 + (y2 - y1) * i / steps);
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    /** A dotted selection ring matching the navigation's slow orbital accent. */
    public static void orbit(GuiGraphics graphics, double x, double y, double radius, int color) {
        double phase = seconds() * Math.PI * 2 / 45;
        for (int i = 0; i < 32; i++) {
            double a = phase + i * Math.PI * 2 / 32;
            int px = (int) Math.round(x + Math.cos(a) * radius);
            int py = (int) Math.round(y + Math.sin(a) * radius);
            graphics.fill(px, py, px + 1, py + 1, color);
        }
    }

    public static void navigationIcon(GuiGraphics graphics, int index, int x, int y, int color) {
        switch (index) {
            case 0 -> { // person and orbital detail mark
                ellipse(graphics, x, y - 4, 2, 2, color);
                line(graphics, x, y, x - 2, y + 6, color);
                line(graphics, x - 5, y + 2, x + 5, y - 1, color);
                line(graphics, x - 5, y + 2, x - 3, y - 1, color);
                line(graphics, x + 5, y - 1, x + 3, y + 2, color);
            }
            case 1 -> { // light cone prism
                line(graphics, x - 5, y - 5, x + 5, y - 5, color);
                line(graphics, x - 5, y - 5, x, y + 6, color);
                line(graphics, x + 5, y - 5, x, y + 6, color);
                line(graphics, x - 3, y - 2, x + 3, y - 2, color);
            }
            case 2 -> { // traces, incomplete orbit
                for (int i = 0; i < 26; i++) {
                    double a = i * Math.PI / 16;
                    line(graphics, x + Math.cos(a) * 5, y + Math.sin(a) * 5,
                            x + Math.cos(a + .18) * 5, y + Math.sin(a + .18) * 5, color);
                }
                ellipse(graphics, x + 4, y - 4, 2, 2, color);
            }
            case 3 -> { // linked relic rings
                orbit(graphics, x - 2, y - 2, 3, color);
                orbit(graphics, x + 2, y + 2, 3, color);
            }
            case 4 -> { // path star
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4;
                    line(graphics, x + Math.cos(a) * 2, y + Math.sin(a) * 2,
                            x + Math.cos(a) * (i % 2 == 0 ? 6 : 4),
                            y + Math.sin(a) * (i % 2 == 0 ? 6 : 4), color);
                }
            }
            default -> { // guide folio
                line(graphics, x - 6, y - 4, x + 6, y - 4, color);
                line(graphics, x - 6, y + 5, x + 6, y + 5, color);
                line(graphics, x - 6, y - 4, x - 6, y + 5, color);
                line(graphics, x + 6, y - 4, x + 6, y + 5, color);
                line(graphics, x - 2, y - 4, x - 2, y + 5, color);
                line(graphics, x + 1, y - 1, x + 4, y - 1, color);
                line(graphics, x + 1, y + 2, x + 4, y + 2, color);
            }
        }
    }

    public static void platform(GuiGraphics graphics, int centerX, int baseY, int radius, int width) {
        graphics.drawManaged(() -> platformGeometry(graphics, centerX, baseY, radius, width));
    }

    private static void platformGeometry(GuiGraphics graphics, int centerX, int baseY, int radius, int width) {
        // Static translucent floor; the shared stars remain visible through it.
        ellipse(graphics, centerX, baseY + 95, width * .95, 135, 0x334E536F);
        ellipse(graphics, centerX, baseY + 18, radius * .42, radius * .08, 0x35242A43);
        double angle = seconds() * Math.PI * 2 / 60;
        for (int ring = 0; ring < 2; ring++) {
            double rx = radius * (ring == 0 ? .95 : .65), ry = rx * .25;
            for (int segment = 0; segment < 12; segment++) {
                double start = angle + segment * Math.PI * 2 / 12;
                for (int n = 0; n < 12; n++) {
                    double a = start + n * .018, b = a + .018;
                    double x1 = centerX + Math.cos(a) * rx, y1 = baseY + 14 + Math.sin(a) * ry;
                    double x2 = centerX + Math.cos(b) * rx, y2 = baseY + 14 + Math.sin(b) * ry;
                    line(graphics, x1, y1 + 1, x2, y2 + 1, 0x253D62B4);
                    line(graphics, x1, y1, x2, y2, 0xC6E4E9FF);
                }
            }
        }
        for (int x = 0; x < width; x++) {
            double dx = x - centerX;
            int rimY = (int) (baseY - 25 + dx * dx / (width * 25.0));
            double bright = Math.exp(-dx * dx / (width * width * .065));
            for (int layer = 7; layer >= 1; layer--) {
                int alpha = (int) ((3 + bright * 12) * (1 - layer / 9.0));
                graphics.fill(x, rimY - layer, x + 1, rimY + layer, alpha << 24 | 0xCCC6FF);
            }
            int alpha = 55 + (int) (180 * bright);
            graphics.fill(x, rimY, x + 1, rimY + 1, alpha << 24 | 0xF1EDFF);
            // Shallow upward light haze, faint and continuously expanding.
            double rise = (seconds() % 6) / 6;
            int height = 2 + (int) (rise * 10);
            graphics.fillGradient(x, rimY - height, x + 1, rimY,
                    0x00D5DCFF, (int) (14 * bright * Math.sin(rise * Math.PI)) << 24 | 0xD5DCFF);
        }
    }

    public static void prism(GuiGraphics graphics, int x, int y, int radius, double angle) {
        graphics.drawManaged(() -> prismGeometry(graphics, x, y, radius, angle));
    }

    private static void prismGeometry(GuiGraphics graphics, int x, int y, int radius, double angle) {
        int[] colors = {0xADA0FF, 0x86C5FF, 0x74E4DA, 0xA8E7AA, 0xEBDEAB, 0xEBC09D, 0xC19AEC};
        double turn = Math.toRadians(angle);
        // Broad soft rays, with continuous hues rather than alternating hard spokes.
        for (int ray = 0; ray < 21; ray++) {
            double a = (ray / 20.0 - .5) * 2.55 + turn;
            int color = colors[Math.min(colors.length - 1, ray * colors.length / 21)];
            for (int n = 2; n < radius; n += 2) {
                double fade = 1 - n / (double) radius;
                int px = x + (int) (Math.sin(a) * n);
                int py = y + (int) (Math.cos(a) * n * .38);
                int spread = 2 + n / 28;
                for (int band = 3; band > 0; band--) {
                    int alpha = (int) ((band == 1 ? 18 : 5) * fade * fade);
                    graphics.fill(px - spread * band, py - band, px + spread * band + 1,
                            py + band + 1, alpha << 24 | color);
                }
            }
        }
        for (int glow = 10; glow > 0; glow--) {
            ellipse(graphics, x, y + 2, radius * glow / 14.0, glow * 1.35,
                    (glow < 4 ? 14 : 5) << 24 | 0xF4F0FF);
        }
    }
}
