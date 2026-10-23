package com.starrail.sim.client;

import com.starrail.sim.StarRailStatusConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/** Client display preferences. Choices are directly selectable and saved immediately. */
public final class StarRailDisplaySettingsScreen extends StarRailStyledScreen {
    private record Choice(Button button, BooleanSupplier selected) { }
    private final Screen parent;
    private final List<Choice> choices = new ArrayList<>();
    private int left, top, right, bottom, rowStart, rowStep, choiceHeight;
    private static final String[] ROWS = {"quality", "motion", "hud", "position", "hud_scale", "numbers", "combat_effects", "combat_strength", "combat_shake"};

    public StarRailDisplaySettingsScreen(Screen parent) {
        super(Component.translatable("screen.starrail_sim.display_settings"));
        this.parent = parent;
    }

    @Override protected void init() {
        super.init(); choices.clear();
        left = StarRailUiStyle.panelLeft(width); top = StarRailUiStyle.panelTop(height);
        right = left + StarRailUiStyle.panelWidth(width); bottom = top + StarRailUiStyle.panelHeight(height);
        rowStart=top+(bottom-top<320?44:74);
        rowStep=Math.min(41,Math.max(14,(bottom-48-rowStart)/ROWS.length));
        choiceHeight=Math.min(26,rowStep-2);
        booleanRow(0, "smooth", "ordinary", StarRailStatusConfig.SMOOTH_UI);
        booleanRow(1, "full_motion", "reduced_motion", StarRailStatusConfig.REDUCED_MOTION, true);
        booleanRow(2, "mod_hud", "vanilla_hud", StarRailStatusConfig.HUD);
        booleanRow(3, "above_hotbar", "left_corner", StarRailStatusConfig.LEFT_CORNER, true);
        double[] scales = {.75, .85, 1};
        for (int index = 0; index < scales.length; index++) {
            double scale = scales[index];
            option(4, index, 3, Component.literal(Math.round(scale * 100) + "%"),
                    () -> Math.abs(StarRailStatusConfig.SCALE.get() - scale) < .001,
                    () -> StarRailStatusConfig.SCALE.set(scale));
        }
        booleanRow(5, "abbreviated", "exact", StarRailStatusConfig.EXACT, true);
        booleanRow(6, "enhanced_effects", "base_effects", StarRailStatusConfig.COMBAT_SHADERS);
        double[] strengths={.6,1,1.5};String[] strengthNames={"effects_soft","effects_standard","effects_strong"};
        for(int i=0;i<strengths.length;i++) {
            double strength=strengths[i];
            option(7,i,3,strengthNames[i],()->Math.abs(StarRailStatusConfig.COMBAT_STRENGTH.get()-strength)<.001,
                ()->StarRailStatusConfig.COMBAT_STRENGTH.set(strength));
        }
        booleanRow(8,"effects_on","effects_off",StarRailStatusConfig.COMBAT_CAMERA_SHAKE);
        addRenderableWidget(StarRailUiStyle.button(Component.translatable("ui.starrail_sim.settings.preview"),
                ignored -> minecraft.setScreen(new StarRailDisplayPreviewScreen(this)), right - 168, top + 19, 138, 24));
        addRenderableWidget(StarRailUiStyle.button(Component.translatable("ui.starrail_sim.settings.defaults"),
                ignored -> defaults(), left + 24, bottom - 38, Math.min(170,(right-left-64)/2), 26));
        addRenderableWidget(StarRailUiStyle.button(Component.translatable("gui.back"),
                ignored -> onClose(), right - 134, bottom - 38, 110, 26));
        refresh();
    }

    private void booleanRow(int row, String first, String second, net.minecraftforge.common.ForgeConfigSpec.BooleanValue value) {
        booleanRow(row, first, second, value, false);
    }
    private void booleanRow(int row, String first, String second, net.minecraftforge.common.ForgeConfigSpec.BooleanValue value, boolean reversed) {
        option(row, 0, 2, first, () -> value.get() != reversed, () -> value.set(!reversed));
        option(row, 1, 2, second, () -> value.get() == reversed, () -> value.set(reversed));
    }
    private void option(int row, int index, int count, String key, BooleanSupplier selected, Runnable change) {
        option(row, index, count, Component.translatable("ui.starrail_sim.settings." + key), selected, change);
    }
    private void option(int row, int index, int count, Component label, BooleanSupplier selected, Runnable change) {
        int start = left + Math.min(194,(right-left)/2), gap = 6, w = (right - 24 - start - gap * (count - 1)) / count;
        Button button = addRenderableWidget(StarRailUiStyle.button(label,
                ignored -> { change.run(); save(); }, start + index * (w + gap), rowStart + row * rowStep, w, choiceHeight));
        choices.add(new Choice(button, selected));
    }
    private void refresh() {
        choices.forEach(choice -> StarRailUiStyle.setSelected(choice.button(), choice.selected().getAsBoolean()));
    }
    private void save() {
        StarRailStatusConfig.CLIENT.save();
        font = StarRailUiFonts.forMod(); refresh();
    }
    private void defaults() {
        StarRailStatusConfig.SMOOTH_UI.set(true); StarRailStatusConfig.REDUCED_MOTION.set(false);
        StarRailStatusConfig.COMBAT_SHADERS.set(true);
        StarRailStatusConfig.COMBAT_STRENGTH.set(1.0);StarRailStatusConfig.COMBAT_CAMERA_SHAKE.set(true);
        StarRailStatusConfig.HUD.set(true); StarRailStatusConfig.LEFT_CORNER.set(false);
        StarRailStatusConfig.SCALE.set(1.0); StarRailStatusConfig.EXACT.set(false); save();
    }
    @Override protected void renderPage(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        StarRailUiStyle.renderBackdrop(graphics, width, height);
        graphics.drawString(font, title, left + 24, top + 22, StarRailUiStyle.GOLD_ACCENT, false);
        if(bottom-top>=320)graphics.drawString(font, Component.translatable("ui.starrail_sim.settings.hint"),
                left + 24, top + 47, StarRailUiStyle.MUTED_COLOR, false);
        for (int row = 0; row < ROWS.length; row++) graphics.drawString(font,
                Component.translatable("ui.starrail_sim.settings." + ROWS[row]),
                left + 24, rowStart + row * rowStep + (choiceHeight-font.lineHeight)/2, StarRailUiStyle.VALUE_COLOR, false);
        super.renderPage(graphics, mouseX, mouseY, partialTick);
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
}
