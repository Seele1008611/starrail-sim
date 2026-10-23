package com.starrail.sim.client;

import com.mojang.logging.LogUtils;
import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.StarRailStatusConfig;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;

import java.io.IOException;

/** Optional local mesh material. Minecraft owns and closes registered shader instances. */
public final class StarRailCombatShaders {
    private static ShaderInstance shader;
    private static boolean failed;

    private StarRailCombatShaders() { }

    public static void register(RegisterShadersEvent event) {
        // Called again on resource reload; never retain a closed instance after a failed reload.
        shader = null;
        failed = false;
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    new ResourceLocation(StarRailSimMod.MOD_ID, "combat_mesh"),
                    com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR_TEX), loaded -> {
                shader = loaded;
                if (loaded.getUniform("CombatTime") == null||loaded.getUniform("EmissionPass")==null) {
                    disable(new IllegalStateException("Combat mesh shader has missing time/emission uniforms"));
                }
            });
        } catch (IOException | RuntimeException exception) {
            disable(exception);
        }
    }

    public static boolean available() { return shader != null && !failed; }
    public static boolean enhanced() { return available() && StarRailStatusConfig.COMBAT_SHADERS.get(); }

    public static ShaderInstance current() {
        // POSITION_COLOR's first two attributes match POSITION_COLOR_TEX; it ignores UV0.
        return enhanced() ? shader : GameRenderer.getPositionColorShader();
    }

    public static void prepare(float seconds) {
        if (!enhanced()) return;
        try {
            shader.safeGetUniform("CombatTime").set(seconds);
            shader.safeGetUniform("EmissionPass").set(0F);
        } catch (RuntimeException exception) {
            disable(exception);
        }
    }

    public static void emission(boolean enabled) {
        if(available()) {
            try {shader.safeGetUniform("EmissionPass").set(enabled?1F:0F);}
            catch(RuntimeException exception) {disable(exception);}
        }
    }

    public static void disable(Exception exception) {
        if (!failed) LogUtils.getLogger().warn("Combat mesh shader unavailable; using base vertex colours until resource reload", exception);
        failed = true;
    }

}
