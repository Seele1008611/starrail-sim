package com.starrail.sim.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftFontAccessor {
    @Mutable @Accessor("font") void starrail$setFont(Font font);
    @Mutable @Accessor("fontFilterFishy") void starrail$setFilteredFont(Font font);
}
