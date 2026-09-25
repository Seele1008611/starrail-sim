package com.starrail.sim.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.starrail.sim.StarRailSimMod;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Client-only key mappings for the mod. */
@Mod.EventBusSubscriber(
        modid = StarRailSimMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class StarRailClient {
    public static final KeyMapping OPEN_CHARACTER = new KeyMapping(
            "key.starrail_sim.open_character",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.starrail_sim");

    private StarRailClient() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CHARACTER);
    }
}
