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

    public static final KeyMapping ACTIVE_SKILL=new KeyMapping("key.starrail_sim.active_skill",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_R,"key.categories.starrail_sim");

    public static final KeyMapping USE_CURIO = new KeyMapping(
            "key.starrail_sim.use_curio", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G, "key.categories.starrail_sim");

    private StarRailClient() {
    }

    @SubscribeEvent
    public static void registerShaders(net.minecraftforge.client.event.RegisterShadersEvent event) {
        StarRailSmoothUi.register(event);
        StarRailCombatShaders.register(event);
        StarRailTrailShaders.register(event);
        StarRailCombatPost.register(event);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ACTIVE_SKILL);
        event.register(USE_CURIO);
        event.register(OPEN_CHARACTER);
        event.register(TOGGLE_STATUS);
    }

    @SubscribeEvent
    public static void registerFrostSignRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(com.starrail.sim.PathFrostSigns.SIGN_ENTITY.get(), net.minecraft.client.renderer.blockentity.SignRenderer::new);
        event.registerBlockEntityRenderer(com.starrail.sim.PathFrostSigns.HANGING_ENTITY.get(), net.minecraft.client.renderer.blockentity.HangingSignRenderer::new);
        event.registerBlockEntityRenderer(com.starrail.sim.PathSunnySigns.SIGN_ENTITY.get(), net.minecraft.client.renderer.blockentity.SignRenderer::new);
        event.registerBlockEntityRenderer(com.starrail.sim.PathSunnySigns.HANGING_ENTITY.get(), net.minecraft.client.renderer.blockentity.HangingSignRenderer::new);
        event.registerBlockEntityRenderer(com.starrail.sim.PathStarlightSigns.SIGN_ENTITY.get(), net.minecraft.client.renderer.blockentity.SignRenderer::new);
        event.registerBlockEntityRenderer(com.starrail.sim.PathStarlightSigns.HANGING_ENTITY.get(), net.minecraft.client.renderer.blockentity.HangingSignRenderer::new);
    }

    @SubscribeEvent
    public static void registerMenus(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(StarRailSimMod.RELIC_TRIAL_MENU.get(), RelicTrialScreen::new);
            MenuScreens.register(com.starrail.sim.PathKitchen.MENU.get(), KitchenScreen::new);
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
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.FROST_SPRUCE.leaves().get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.FROST_SPRUCE.sapling().get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.FROST_SPRUCE.door().get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.FROST_SPRUCE.trapdoor().get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.FROST_BERRY_BUSH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PathEcologyContent.FROST_BELL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(com.starrail.sim.PathOchreContent.OCHRE_LEAF_BUSH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(com.starrail.sim.PathOchreContent.CRYSTAL_SAND_CLUSTER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(com.starrail.sim.PathOchreContent.SUNPOD_WILD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(com.starrail.sim.PathOchreContent.SUNPOD_CROP.get(), RenderType.cutout());
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
