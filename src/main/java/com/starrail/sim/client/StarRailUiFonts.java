package com.starrail.sim.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

/** Uses Minecraft's active default font so resource packs and other mods retain control. */
public final class StarRailUiFonts {
    private StarRailUiFonts() { }

    public static Font forMod() {
        return Minecraft.getInstance().font;
    }
}
