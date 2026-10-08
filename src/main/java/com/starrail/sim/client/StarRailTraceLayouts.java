package com.starrail.sim.client;

import com.starrail.sim.StarRailPath;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Per-path viewBox, emblem sizing, palette, and SVG branch strokes from the HTML prototype. */
public final class StarRailTraceLayouts {
    public record Segment(int kind, double x1, double y1, double c1x, double c1y,
                          double c2x, double c2y, double x2, double y2) { }
    public record Stroke(boolean core, List<Segment> segments) { }
    public record Layout(int canvasWidth, int canvasHeight, int textureWidth, int textureHeight,
                         int accent, String heading, List<Stroke> strokes) { }

    private static final Pattern TOKEN = Pattern.compile("[MLQC]|-?\\d+(?:\\.\\d+)?");
    private static final Map<StarRailPath, Layout> LAYOUTS = new EnumMap<>(StarRailPath.class);

    static {
        put(StarRailPath.HUNT, 924, 875, 512, 448, 0x79BAFF, "循着猎意，锁定目标",
                line("M480 122 L480 763", true),
                line("M310 149 Q480 95 650 149", false),
                line("M220 286 L337 408 Q480 566 630 408 L742 286", false),
                line("M112 418 L217 518 L322 620", false),
                line("M852 418 L747 518 L633 620", false),
                line("M322 620 Q480 595 633 620", false));
        put(StarRailPath.PRESERVATION, 1013, 778, 432, 512, 0x91BAFF, "以坚盾守护同行者",
                line("M532 59 L532 674", true),
                line("M359 86 Q532 32 704 86", false),
                line("M262 291 L387 421 Q532 385 677 421 L800 291", false),
                line("M135 411 L233 524 L390 683", false),
                line("M928 411 L830 524 L678 683", false),
                line("M390 683 Q532 653 678 683", false));
        put(StarRailPath.ABUNDANCE, 937, 767, 452, 512, 0x83E0AD, "让生机沿枝叶延展",
                line("M463 70 L463 684", true),
                line("M295 108 Q463 34 633 108", false),
                line("M324 393 Q463 451 602 393", false),
                line("M211 298 L103 397 L160 493 L245 610", false),
                line("M714 298 L824 397 L770 493 L681 610", false),
                line("M245 610 Q463 510 681 610", false),
                line("M386 697 L463 684 L541 697", false));
        put(StarRailPath.DESTRUCTION, 997, 748, 486, 512, 0xFF8878, "于烈焰中锻出绝境锋芒",
                line("M514 51 L514 680", true),
                line("M342 79 Q514 23 686 79", false),
                line("M221 257 L128 392 L227 491 L335 566", false),
                line("M807 257 L904 392 L804 491 L692 566", false),
                line("M349 373 Q514 425 680 373", false),
                line("M335 566 Q514 530 692 566", false));
        put(StarRailPath.ERUDITION, 945, 738, 512, 453, 0xC0A2FF, "在星图核心推演万象",
                line("M461 72 L461 650", true),
                line("M288 100 Q461 44 635 100", false),
                line("M100 418 L820 418", false),
                line("M129 297 Q71 418 129 539", false),
                line("M791 297 Q849 418 791 539", false),
                line("M324 636 Q461 670 600 636", false));
        put(StarRailPath.NIHILITY, 1046, 767, 498, 512, 0xC49AFF, "让侵蚀沿暗潮蔓延",
                line("M532 56 L532 685", true),
                line("M357 85 Q531 25 706 85", false),
                line("M247 276 L132 399 L233 519 L337 640", false),
                line("M807 276 L933 399 L829 519 L729 640", false),
                line("M247 276 L375 320 Q532 295 688 325 L807 276", false));
        put(StarRailPath.HARMONY, 1014, 768, 512, 507, 0xF1CF7C, "让共鸣连接彼此的力量",
                line("M500 55 L500 697", true),
                line("M338 85 Q501 25 665 85", false),
                line("M226 257 L104 324 L178 425", false),
                line("M178 425 Q501 235 830 427", false),
                line("M830 427 L766 542 L649 502", false),
                line("M347 669 Q501 725 656 669", false));
        put(StarRailPath.REMEMBRANCE, 950, 796, 467, 512, 0x8DDCF3, "将记录化作回响与回忆",
                line("M493 236 L493 747", true),
                line("M286 168 Q420 71 566 109", false),
                line("M286 168 L342 290 C297 330 269 381 276 436 C283 495 315 543 356 572 C399 603 447 617 493 617 C545 617 589 602 630 572 C676 539 704 489 713 436 L849 436", false),
                line("M159 298 Q85 436 159 574", false),
                line("M122 436 L276 436", false),
                line("M819 298 Q879 436 818 574", false),
                line("M358 730 Q493 764 628 730", false));
        put(StarRailPath.ELATION, 1243, 823, 502, 512, 0xFF91C8, "在连击与欢愉中引爆战局",
                line("M594 265 L594 725", true),
                line("M450 148 C503 180 555 225 594 265 C633 225 685 180 738 148", false),
                line("M450 148 C394 172 339 205 306 237 C268 280 249 328 250 375 C244 424 256 473 276 508 L372 430", false),
                line("M738 148 C794 172 845 205 878 237 C916 280 939 328 936 375 C942 424 930 473 912 508 L816 430", false),
                line("M456 526 Q594 590 732 526", false),
                line("M476 725 L714 725", false));
    }

    private StarRailTraceLayouts() { }
    private static void put(StarRailPath path, int width, int height, int textureWidth, int textureHeight,
                            int accent, String heading, Stroke... strokes) {
        LAYOUTS.put(path, new Layout(width, height, textureWidth, textureHeight, accent,
                heading, List.of(strokes)));
    }
    private static Stroke line(String data, boolean core) { return new Stroke(core, parse(data)); }

    private static List<Segment> parse(String data) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(data);
        while (matcher.find()) tokens.add(matcher.group());
        List<Segment> segments = new ArrayList<>();
        char command = 'M';
        double x = 0, y = 0;
        for (int i = 0; i < tokens.size();) {
            String token = tokens.get(i++);
            if (Character.isLetter(token.charAt(0))) command = token.charAt(0);
            else i--;
            if (command == 'M') {
                x = number(tokens.get(i++)); y = number(tokens.get(i++)); command = 'L';
            } else if (command == 'L') {
                double nx = number(tokens.get(i++)), ny = number(tokens.get(i++));
                segments.add(new Segment(0, x, y, 0, 0, 0, 0, nx, ny)); x = nx; y = ny;
            } else if (command == 'Q') {
                double cx = number(tokens.get(i++)), cy = number(tokens.get(i++));
                double nx = number(tokens.get(i++)), ny = number(tokens.get(i++));
                segments.add(new Segment(1, x, y, cx, cy, 0, 0, nx, ny)); x = nx; y = ny;
            } else if (command == 'C') {
                double c1x = number(tokens.get(i++)), c1y = number(tokens.get(i++));
                double c2x = number(tokens.get(i++)), c2y = number(tokens.get(i++));
                double nx = number(tokens.get(i++)), ny = number(tokens.get(i++));
                segments.add(new Segment(2, x, y, c1x, c1y, c2x, c2y, nx, ny)); x = nx; y = ny;
            }
        }
        return List.copyOf(segments);
    }
    private static double number(String value) { return Double.parseDouble(value); }
    public static Layout get(StarRailPath path) { return LAYOUTS.getOrDefault(path, LAYOUTS.get(StarRailPath.HUNT)); }
}
