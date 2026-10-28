package com.starrail.sim.client;

/**
 * 模组代码说明：客户端初始化入口，注册界面、渲染与客户端事件处理器。
 */

import com.mojang.blaze3d.platform.InputConstants;
import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.PathEcologyContent;
import com.starrail.sim.PathCaveContent;
import com.starrail.sim.PathMineralContent;
import com.starrail.sim.StarRailPath;
import com.starrail.sim.StarRailRuinContent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

/** Client-only key mappings for the mod. */
@Mod.EventBusSubscriber(
        modid = StarRailSimMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class StarRailClient {
    public static final KeyMapping TOGGLE_STATUS = new KeyMapping(
            "key.starrail_sim.toggle_status", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8, "key.categories.starrail_sim");
    public static final KeyMapping OPEN_CHARACTER = new KeyMapping(
            "key.starrail_sim.open_character",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.starrail_sim");

    private StarRailClient() {
    }

    @SubscribeEvent
    public static void registerShaders(net.minecraftforge.client.event.RegisterShadersEvent event) {
        StarRailSmoothUi.register(event);
        StarRailCombatShaders.register(event);
        StarRailCombatPost.register(event);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CHARACTER);
        event.register(TOGGLE_STATUS);
    }

    @SubscribeEvent
    public static void registerMenus(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(StarRailSimMod.RELIC_TRIAL_MENU.get(), RelicTrialScreen::new);
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.SUNMEADOW.leaves().get(),
                    RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.STARLIGHTWOOD.leaves().get(),
                    RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.SUNMEADOW.sapling().get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.STARLIGHTWOOD.sapling().get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.SUNMEADOW.trapdoor().get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.STARLIGHTWOOD.trapdoor().get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.MORNING_BERRY_BUSH.get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.FORESTBEAN_WILD.get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.FORESTBEAN_CROP.get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.STAR_TUBER_CROP.get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.STILLTIDE_RICE_CROP.get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.STARLIGHT_FUNGUS.get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.SUNMEADOW_BLOOM.get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.TIDE_GRASS.get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.CLEAR_SAND_BELLFLOWER.get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.STAR_TIDE_ALGAE.get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.MARSH_REED.get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.RIDGE_CUSHION.get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.RIDGE_WINDBUSH.get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.MARSH_ROOT_TUFT.get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathMineralContent.RESONANCE_CRYSTAL_WIRE.get(),
                    RenderType.cutout());
            for (StarRailPath path : StarRailPath.values()) {
                var anchor = StarRailRuinContent.anchor(path);
                if (anchor != null) {
                    ItemBlockRenderTypes.setRenderLayer(anchor.get(), RenderType.translucent());
                }
            }
            ItemBlockRenderTypes.setRenderLayer(PathCaveContent.SMALL_STARVEIN_BUD.get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathCaveContent.MEDIUM_STARVEIN_BUD.get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathCaveContent.LARGE_STARVEIN_BUD.get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathCaveContent.STARVEIN_CRYSTAL_CLUSTER.get(),
                    RenderType.cutout());
        });
    }
}
