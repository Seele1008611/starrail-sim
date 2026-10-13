package com.starrail.sim.mixin.client;

import com.starrail.sim.client.StarRailSharpText;
import com.starrail.sim.client.StarRailUiFonts;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiGraphics.class)
public abstract class GuiTextMixin {
    @Unique
    private boolean sharp(Font font, Component text, int x, int y, int color, boolean center) {
        return StarRailUiFonts.replaces(font) && StarRailSharpText.draw((GuiGraphics) (Object) this,
                text, x, y, color, center, false);
    }
    @Inject(method="drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I", at=@At("HEAD"), cancellable=true)
    private void string(Font font, String text, int x, int y, int color, boolean shadow, CallbackInfoReturnable<Integer> ci) {
        if (text != null && sharp(font, StarRailUiFonts.legacy(text), x, y, color, false)) ci.setReturnValue(x + font.width(text));
    }
    @Inject(method="drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I", at=@At("HEAD"), cancellable=true)
    private void component(Font font, Component text, int x, int y, int color, boolean shadow, CallbackInfoReturnable<Integer> ci) {
        if (sharp(font, text, x, y, color, false)) ci.setReturnValue(x + font.width(text));
    }
    @Inject(method="drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)I", at=@At("HEAD"), cancellable=true)
    private void sequence(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow, CallbackInfoReturnable<Integer> ci) {
        if (StarRailUiFonts.replaces(font) && sharp(font, component(text), x, y, color, false)) ci.setReturnValue(x + font.width(text));
    }
    @Inject(method="drawCenteredString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V", at=@At("HEAD"), cancellable=true)
    private void centeredString(Font font, String text, int x, int y, int color, CallbackInfo ci) {
        if (text != null && sharp(font, StarRailUiFonts.legacy(text), x, y, color, true)) ci.cancel();
    }
    @Inject(method="drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V", at=@At("HEAD"), cancellable=true)
    private void centeredComponent(Font font, Component text, int x, int y, int color, CallbackInfo ci) {
        if (sharp(font, text, x, y, color, true)) ci.cancel();
    }
    @Inject(method="drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)V", at=@At("HEAD"), cancellable=true)
    private void centeredSequence(Font font, FormattedCharSequence text, int x, int y, int color, CallbackInfo ci) {
        if (StarRailUiFonts.replaces(font) && sharp(font, component(text), x, y, color, true)) ci.cancel();
    }
    @Unique
    private static Component component(FormattedCharSequence sequence) {
        MutableComponent result = Component.empty();
        StringBuilder run = new StringBuilder();
        Style[] previous = {null};
        sequence.accept((index, style, cp) -> {
            if (previous[0] != null && !previous[0].equals(style)) {
                result.append(Component.literal(run.toString()).setStyle(previous[0])); run.setLength(0);
            }
            previous[0] = style; run.appendCodePoint(cp); return true;
        });
        if (!run.isEmpty()) result.append(Component.literal(run.toString()).setStyle(previous[0]));
        return result;
    }
}
