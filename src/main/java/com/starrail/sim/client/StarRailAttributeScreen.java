package com.starrail.sim.client;

import com.starrail.sim.StarRailAttributes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import java.util.Locale;

/** Read-only grouped attributes in the same light modal used by the HTML reference. */
public final class StarRailAttributeScreen extends StarRailModalScreen {
    private static final String[] LABELS = {
        "current_max_health", "attack_damage", "armor", "armor_toughness", "movement_speed", "attack_speed",
        "crit_rate", "crit_damage", "break_effect", "effect_hit_rate", "effect_resistance", "knockback_resistance", "healing_effect"
    };
    private final int[] rowPositions = new int[LABELS.length];
    private final double[] values = new double[LABELS.length];
    private final double[] bases = new double[LABELS.length];
    private final StarRailSmoothScroll scroll = new StarRailSmoothScroll();
    private int selected = -1;
    private int contentHeight;

    public StarRailAttributeScreen(StarRailStyledScreen parent) {
        super(Component.translatable("screen.starrail_sim.attributes"), parent);
    }

    private boolean percent(int index) { return index >= 6 && index != 11; }
    private String number(int index, double value) {
        return String.format(Locale.ROOT, percent(index) ? "%.1f%%" : index == 4 || index == 5 || index == 11
                ? "%.3f" : "%.1f", percent(index) ? value * 100 : value);
    }

    private void refresh(Player player) {
        Attribute[] attributes = {
            Attributes.MAX_HEALTH, Attributes.ATTACK_DAMAGE, Attributes.ARMOR, Attributes.ARMOR_TOUGHNESS,
            Attributes.MOVEMENT_SPEED, Attributes.ATTACK_SPEED,
            StarRailAttributes.CRIT_RATE.get(), StarRailAttributes.CRIT_DAMAGE.get(), StarRailAttributes.BREAK_EFFECT.get(),
            StarRailAttributes.EFFECT_HIT_RATE.get(), StarRailAttributes.EFFECT_RESISTANCE.get(),
            Attributes.KNOCKBACK_RESISTANCE, StarRailAttributes.HEALING_EFFECT.get()
        };
        for (int i = 0; i < attributes.length; i++) {
            var instance = player.getAttribute(attributes[i]);
            values[i] = instance == null ? 0 : instance.getValue();
            bases[i] = instance == null ? 0 : instance.getBaseValue();
        }
        values[1] = StarRailAttributes.getAttackDamage(player);
        values[6] = StarRailAttributes.getCritRate(player);
        values[7] = StarRailAttributes.getCritDamage(player);
    }

    @Override
    protected void renderModal(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Player player = minecraft.player;
        if (player != null) refresh(player);
        int left = modalLeft + 17, right = modalRight - 17;
        int bottom = modalBottom - 68;
        scroll.advance();
        clip(graphics, left, bodyTop, right, bottom);
        graphics.pose().pushPose();
        graphics.pose().translate(0, -scroll.position(), 0);
        int y = bodyTop + 7;
        for (int i = 0; i < LABELS.length; i++) {
            if (i == 0 || i == 6) {
                graphics.drawString(font, Component.translatable(i == 0
                        ? "ui.starrail_sim.attributes.basic" : "ui.starrail_sim.attributes.advanced"),
                        left + 2, y, SECONDARY_INK, false);
                y += 23;
            }
            rowPositions[i] = y;
            boolean hovered = mouseX >= left && mouseX < right && mouseY >= bodyTop && mouseY < bottom
                    && mouseY >= y - scroll.position() && mouseY < y - scroll.position() + 22;
            graphics.fill(left, y, right, y + 22, selected == i || hovered ? 0x22627799
                    : i % 2 == 0 ? 0x10707785 : 0x00707785);
            int icon = i < 5 ? i : i == 6 ? 5 : 6;
            StarRailUiStyle.statIcon(graphics, icon, left + 10, y + 11, SECONDARY_INK);
            graphics.drawString(font, Component.translatable("ui.starrail_sim." + LABELS[i]),
                    left + 24, y + 7, INK, false);
            // Every row separates the base value from the total bonus, including advanced attributes.
            String primary = number(i, bases[i]);
            graphics.drawString(font, primary, right - 114 - font.width(primary), y + 7, INK, false);
            double bonus = values[i] - bases[i];
            if (Math.abs(bonus) > .00001) {
                String extra = (bonus > 0 ? "+" : "") + number(i, bonus);
                graphics.drawString(font, extra, right - 27 - font.width(extra), y + 7,
                        bonus >= 0 ? 0xFF168EB9 : 0xFFA14A60, false);
            }
            StarRailCosmicUi.ellipse(graphics, right - 9, y + 11, 4, 4, 0xFF363945);
            graphics.drawString(font, "?", right - 11, y + 7, 0xFFF4F4F7, false);
            y += 25;
        }
        graphics.pose().popPose();
        graphics.disableScissor();
        contentHeight = y - bodyTop + 8;
        scroll.bounds(contentHeight - (bottom - bodyTop));
        renderScrollBar(graphics, scroll, modalRight - 10, bodyTop, bottom, contentHeight, mouseX, mouseY);
        graphics.fill(left, bottom + 9, right, bottom + 10, 0x30707785);
        Component help = Component.translatable("ui.starrail_sim.attributes.help");
        if (selected >= 0) {
            help = Component.translatable("ui.starrail_sim.attributes.breakdown",
                    Component.translatable("ui.starrail_sim." + LABELS[selected]),
                    number(selected, bases[selected]), number(selected, values[selected] - bases[selected]),
                    number(selected, values[selected]));
            if (selected == 0 && player != null) {
                help = help.copy().append("  ").append(Component.translatable("ui.starrail_sim.attributes.health",
                        String.format(Locale.ROOT, "%.1f", player.getHealth()), number(0, values[0])));
            }
        }
        int helpY = bottom + 18;
        for (FormattedCharSequence line : font.split(help, right - left)) {
            graphics.drawString(font, line, left, helpY, SECONDARY_INK, false);
            helpY += 12;
        }
    }

    @Override
    protected boolean logicalMouseClicked(double x, double y, int button) {
        if (button == 0 && x >= modalLeft + 17 && x < modalRight - 17
                && y >= bodyTop && y < modalBottom - 68) {
            for (int i = 0; i < rowPositions.length; i++) {
                if (y >= rowPositions[i] - scroll.position() && y < rowPositions[i] - scroll.position() + 22) {
                    selected = i;
                    return true;
                }
            }
        }
        return super.logicalMouseClicked(x, y, button);
    }

    @Override
    protected boolean logicalMouseScrolled(double x, double y, double amount) {
        if (x >= modalLeft && x <= modalRight && y >= bodyTop && y < modalBottom - 68) {
            scroll.wheel(amount, 25);
        }
        return true;
    }
}
