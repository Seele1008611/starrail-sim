package com.starrail.sim.mixin.client;

import com.mojang.blaze3d.platform.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.font.providers.BitmapProvider$Glyph")
public interface BitmapGlyphAccessor {
    @Accessor("image") NativeImage starrail$image();
    @Accessor("offsetX") int starrail$offsetX();
    @Accessor("offsetY") int starrail$offsetY();
    @Accessor("width") int starrail$width();
    @Accessor("height") int starrail$height();
}
