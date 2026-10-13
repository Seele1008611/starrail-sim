package com.starrail.sim.mixin.client;

import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BakedGlyph.class)
public interface BakedGlyphAccessor {
    @Accessor("left") float starrail$left();
    @Accessor("right") float starrail$right();
    @Accessor("up") float starrail$up();
    @Accessor("down") float starrail$down();
}
