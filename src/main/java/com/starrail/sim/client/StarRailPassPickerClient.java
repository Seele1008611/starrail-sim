package com.starrail.sim.client;

import com.starrail.sim.StarRailPath;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

/** Client-only entry point for opening the Special Pass picker. */
public final class StarRailPassPickerClient {
    private StarRailPassPickerClient() {
    }

    public static void open(StarRailPath path, InteractionHand hand) {
        Minecraft.getInstance().setScreen(new StarRailPassPickerScreen(path, hand));
    }
}
