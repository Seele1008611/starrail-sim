package com.starrail.sim.client;

/**
 * 模组代码说明：客户端界面类，构建对应页面并处理玩家的界面交互。
 */

import com.starrail.sim.InTheNightItem;
import com.starrail.sim.IWillHuntItem;
import com.starrail.sim.StarRailLightConeService;
import com.starrail.sim.StarRailPath;
import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.SleepLikeTheDeadItem;
import com.starrail.sim.PureThoughtBaptismItem;
import com.starrail.sim.IdealBurningHellItem;
import com.starrail.sim.EmbarkOnSecondLifeItem;
import com.starrail.sim.FinaleOfALieItem;
import com.starrail.sim.WorrisomeBlissfulItem;
import com.starrail.sim.SomethingIrreplaceableItem;
import com.starrail.sim.BrighterThanTheSunItem;
import com.starrail.sim.DanceAtSunsetItem;
import com.starrail.sim.TheUnreachableSideItem;
import com.starrail.sim.ThisBodyAsSwordItem;
import com.starrail.sim.NoRewardCrowningItem;
import com.starrail.sim.BloodFireBurningPathItem;
import com.starrail.sim.WhereDreamsBelongItem;
import com.starrail.sim.DawnBurnsJustSoItem;
import com.starrail.sim.WhatYouSeeIsMeItem;
import com.starrail.sim.GalaxyRailwayItem;
import com.starrail.sim.MomentOfGloryItem;
import com.starrail.sim.BeforeDawnItem;
import com.starrail.sim.PriceOfPeaceItem;
import com.starrail.sim.TowardsUnanswerableItem;
import com.starrail.sim.NinjaScrollItem;
import com.starrail.sim.LifeAsALightItem;
import com.starrail.sim.AStarIlluminatesNightSkyItem;
import com.starrail.sim.SparkleQuietlyShinesItem;
import com.starrail.sim.BattleIsntOverItem;
import com.starrail.sim.MemoryOfMeItem;
import com.starrail.sim.WeaveTimeIntoGoldItem;
import com.starrail.sim.MakeFarewellMoreBeautifulItem;
import com.starrail.sim.LoveIsEternalItem;
import com.starrail.sim.StarlightForLongNightsItem;
import com.starrail.sim.MayRainbowStayInTheSkyItem;
import com.starrail.sim.WelcomeToGalacticCityItem;
import com.starrail.sim.MeetInTheNextFlowerSeasonItem;
import com.starrail.sim.WhenSheDecidesToSeeItem;
import com.starrail.sim.FlowerWorldMesmerizingEyesItem;
import com.starrail.sim.NightFlowingColorsItem;
import com.starrail.sim.GameOfCosmicWorldsItem;
import com.starrail.sim.ReturningToEarthItem;
import com.starrail.sim.IfTimeWereAFlowerItem;
import com.starrail.sim.AnAgeEtchedInGoldenBloodItem;
import com.starrail.sim.InTheNameOfTheWorldItem;
import com.starrail.sim.OnTheShoreInTheFlowOfTimeItem;
import com.starrail.sim.AThousandFoldSpringItem;
import com.starrail.sim.OnlyWaitItem;
import com.starrail.sim.RainNeverStopsItem;
import com.starrail.sim.LiesInTheWindItem;
import com.starrail.sim.ReturnToLongRoadItem;
import com.starrail.sim.ReforgedRemembranceItem;
import com.starrail.sim.OceanWhySingsItem;
import com.starrail.sim.DoNotForgetHerFlameItem;
import com.starrail.sim.NewFleshOfInfernoItem;
import com.starrail.sim.MomentOfVictoryItem;
import com.starrail.sim.SheHasClosedHerEyesItem;
import com.starrail.sim.ThoughRiversAndMountainsItem;
import com.starrail.sim.FateNeverFairItem;
import com.starrail.sim.EchoesOfTheCoffinItem;
import com.starrail.sim.TimeWaitsForNoOneItem;
import com.starrail.sim.NightOfFrightItem;
import com.starrail.sim.OnlyTheScentRemainsItem;
import com.starrail.sim.StarRailAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import com.mojang.math.Axis;

/** Read-only light cone page connected to the character navigation. */
public final class StarRailLightConeScreen extends StarRailStyledScreen {
    private final Button[] navigationButtons = new Button[
            StarRailUiStyle.CHARACTER_NAVIGATION_KEYS.length];
    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;
    private int contentLeft;
    private int contentRight;
    private int contentTop;
    private int centerRight;
    private int rightLeft;
    private boolean compactLayout;
    private float coneAngle;
    private float coneTargetAngle;
    private long coneFrame;
    private boolean draggingCone;
    private final StarRailSmoothScroll detailsScroll = new StarRailSmoothScroll();
    private int detailsBottom;

    public StarRailLightConeScreen() {
        super(Component.translatable("screen.starrail_sim.character_light_cone"));
    }

    @Override
    protected void init() {
        super.init();
        int panelWidth = StarRailUiStyle.panelWidth(width);
        int panelHeight = StarRailUiStyle.panelHeight(height);
        panelLeft = StarRailUiStyle.panelLeft(width);
        panelTop = StarRailUiStyle.panelTop(height);
        panelRight = panelLeft + panelWidth;
        panelBottom = panelTop + panelHeight;
        compactLayout = StarRailUiStyle.isCompact(panelWidth, panelHeight);
        draggingCone = false;
        contentLeft = panelLeft + (compactLayout ? 12 + 116 + 16 : 20 + 132 + 24);
        contentRight = panelRight - (compactLayout ? 12 : 20);
        contentTop = panelTop + (compactLayout ? 92 : 74);

        int contentWidth = contentRight - contentLeft;
        rightLeft = contentRight - Math.min(245, Math.max(200, contentWidth * 33 / 100));
        centerRight = rightLeft - (compactLayout ? 12 : 18);

        Button[] createdNavigation = StarRailUiStyle.createCharacterNavigation(
                panelLeft, panelTop, panelWidth, panelHeight, 1, index -> {
                    if (index == 0) {
                        minecraft.setScreen(new StarRailCharacterScreen());
                    } else if (index == 4) {
                        minecraft.setScreen(new StarRailPathScreen());
                    } else if (index == 5) {
                        minecraft.setScreen(new StarRailGuideScreen());
                    }
                });
        for (int index = 0; index < navigationButtons.length; index++) {
            navigationButtons[index] = addRenderableWidget(createdNavigation[index]);
        }

        int pageY = panelBottom - (compactLayout ? 25 : 30);
        addRenderableWidget(StarRailUiStyle.outlinedButton(
                Component.translatable("ui.starrail_sim.cone.preview"),
                ignored -> minecraft.setScreen(new FrontView(this,
                        StarRailLightConeClientData.findEquipped(minecraft.player).copy())),
                centerRight - 88, pageY, 88, 22));
    }

    private static final class FrontView extends StarRailModalScreen {
        private final ItemStack stack;

        private FrontView(StarRailLightConeScreen parent, ItemStack stack) {
            super(stack.isEmpty() ? Component.translatable("ui.starrail_sim.cone.preview") : stack.getHoverName(), parent);
            this.stack = stack;
        }

        @Override protected int preferredWidth() { return 400; }
        @Override protected int preferredHeight() { return 390; }

        @Override
        protected void renderModal(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            float h = Math.min(modalBottom - bodyTop - 26, (modalRight - modalLeft - 60) / .75F);
            StarRailLightConeDisplay.draw(graphics, stack, (modalLeft + modalRight) / 2F,
                    (bodyTop + modalBottom) / 2F, h, 0, true);
        }
    }






    @Override
    protected void renderPage(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        StarRailUiStyle.renderBackdrop(graphics, width, height);
        StarRailUiStyle.renderPanel(graphics, panelLeft, panelTop, panelRight, panelBottom);
        StarRailUiStyle.renderHeader(graphics, title, panelLeft, panelTop);

        Player player = Minecraft.getInstance().player;
        ItemStack stack = StarRailLightConeClientData.findEquipped(player);
        detailsScroll.advance();
        long now = System.nanoTime();
        double dt = coneFrame == 0 ? 0 : Math.min(.1, (now - coneFrame) / 1_000_000_000.0);
        coneFrame = now;
        coneAngle += (coneTargetAngle - coneAngle) * (float) (1 - Math.exp(-dt / .035));
        if (Math.abs(coneTargetAngle - coneAngle) < .01F) coneAngle = coneTargetAngle;
        renderCenter(graphics, stack);
        clip(graphics, rightLeft, contentTop, contentRight, panelBottom - 42);
        graphics.pose().pushPose();
        graphics.pose().translate(0, -detailsScroll.position(), 0);
        detailsBottom = contentTop;
        renderDetails(graphics, player, stack);
        graphics.pose().popPose();
        graphics.disableScissor();
        int visibleHeight = panelBottom - 42 - contentTop;
        detailsScroll.bounds(detailsBottom - contentTop - visibleHeight + 14);
        renderScrollBar(graphics, detailsScroll, contentRight - 2, contentTop, panelBottom - 42,
                detailsBottom - contentTop + 14, mouseX, mouseY);


        super.renderPage(graphics, mouseX, mouseY, partialTick);
    }

    private void renderCenter(GuiGraphics graphics, ItemStack stack) {
        int bottom = panelBottom - 42;
        int centerX = (contentLeft + centerRight) / 2 - 24;
        if (stack.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("screen.starrail_sim.light_cone_empty"),
                    centerX, (contentTop + bottom) / 2, StarRailUiStyle.VALUE_COLOR);
            return;
        }
        float cardHeight = Math.min(bottom - contentTop - 42, (centerRight - contentLeft - 48) / .85F);
        float centerY = (contentTop + bottom) / 2F - 8;
        StarRailCosmicUi.prism(graphics, centerX + (int) (cardHeight * .5 * Math.sin(Math.toRadians(8))),
                (int) (centerY + cardHeight * .5 * Math.cos(Math.toRadians(8))),
                Math.max(70, (centerRight - contentLeft) / 2), coneAngle);
        StarRailLightConeDisplay.draw(graphics, stack, centerX, centerY, cardHeight, coneAngle, false);
    }

    @Override
    protected boolean logicalMouseClicked(double x, double y, int button) {
        if (button == 0 && x >= contentLeft && x < centerRight && y >= contentTop
                && y < panelBottom - 40) {
            draggingCone = true;
            return true;
        }
        return super.logicalMouseClicked(x, y, button);
    }

    @Override
    protected boolean logicalMouseDragged(double x, double y, int button, double dx, double dy) {
        if (draggingCone && button == 0) {
            coneTargetAngle = (float) Math.max(-30, Math.min(30, coneTargetAngle + dx * .3));
            return true;
        }
        return super.logicalMouseDragged(x, y, button, dx, dy);
    }

    @Override
    protected boolean logicalMouseReleased(double x, double y, int button) {
        if (button == 0 && draggingCone) { draggingCone = false; return true; }
        return super.logicalMouseReleased(x, y, button);
    }

    @Override
    protected boolean logicalMouseScrolled(double x, double y, double amount) {
        if (x >= rightLeft && x <= contentRight && y >= contentTop && y < panelBottom - 40) {
            detailsScroll.wheel(amount, 24);
            return true;
        }
        return super.logicalMouseScrolled(x, y, amount);
    }

    private void renderDetails(GuiGraphics graphics, Player player, ItemStack stack) {
        int x = rightLeft;
        int width = contentRight - rightLeft;
        int y = contentTop;
        graphics.drawString(font,
                Component.translatable("screen.starrail_sim.light_cone_details"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 24;
        if (stack.isEmpty()) {
            drawWrapped(graphics,
                    Component.translatable("screen.starrail_sim.light_cone_empty_hint"),
                    x, y, width, StarRailUiStyle.MUTED_COLOR);
            return;
        }

        y = drawWrapped(graphics, stack.getHoverName(), x, y, width - 8, StarRailUiStyle.VALUE_COLOR) + 10;
        boolean destructionLightCone = isDestructionLightCone(stack);
        boolean eruditionLightCone = isEruditionLightCone(stack);
        boolean nihilityLightCone = isNihilityLightCone(stack);
        boolean preservationLightCone = isPreservationLightCone(stack);
        boolean abundanceLightCone = isAbundanceLightCone(stack);
        boolean remembranceLightCone = stack.getItem() instanceof WeaveTimeIntoGoldItem
                || stack.getItem() instanceof MakeFarewellMoreBeautifulItem
                || stack.getItem() instanceof LoveIsEternalItem
                || stack.getItem() instanceof StarlightForLongNightsItem
                || stack.getItem() instanceof MayRainbowStayInTheSkyItem;
        boolean elationLightCone = stack.getItem() instanceof WelcomeToGalacticCityItem
                || stack.getItem() instanceof MeetInTheNextFlowerSeasonItem
                || stack.getItem() instanceof WhenSheDecidesToSeeItem
                || stack.getItem() instanceof FlowerWorldMesmerizingEyesItem;
        boolean harmonyLightCone = stack.getItem() instanceof BattleIsntOverItem
                || stack.getItem() instanceof MemoryOfMeItem
                || stack.getItem() instanceof NightFlowingColorsItem
                || stack.getItem() instanceof GameOfCosmicWorldsItem
                || stack.getItem() instanceof ReturningToEarthItem
                || stack.getItem() instanceof IfTimeWereAFlowerItem
                || stack.getItem() instanceof AnAgeEtchedInGoldenBloodItem;
        abundanceLightCone |= stack.getItem() instanceof TimeWaitsForNoOneItem;
        abundanceLightCone |= stack.getItem() instanceof NightOfFrightItem;
        abundanceLightCone |= stack.getItem() instanceof OnlyTheScentRemainsItem;
        drawDetailLine(graphics, "screen.starrail_sim.light_cone_path",
                Component.translatable(remembranceLightCone
                        ? "screen.starrail_sim.light_cone_remembrance"
                        : harmonyLightCone
                        ? "screen.starrail_sim.light_cone_harmony"
                        : eruditionLightCone
                        ? "screen.starrail_sim.light_cone_erudition"
                        : nihilityLightCone
                        ? "screen.starrail_sim.light_cone_nihility"
                        : preservationLightCone
                        ? "screen.starrail_sim.light_cone_preservation"
                        : abundanceLightCone
                        ? "screen.starrail_sim.light_cone_abundance"
                        : elationLightCone
                        ? "screen.starrail_sim.light_cone_elation"
                        : destructionLightCone
                        ? "screen.starrail_sim.light_cone_destruction"
                        : "screen.starrail_sim.light_cone_hunt"), x, y, width);
        y += 18;
        drawDetailLine(graphics, "screen.starrail_sim.light_cone_superimposition_label",
                Component.literal("S" + getSuperimposition(stack)), x, y,
                width);
        y += 28;

            graphics.drawString(font,
                    Component.translatable("screen.starrail_sim.light_cone_basic"), x, y,
                    StarRailUiStyle.CYAN_ACCENT);
            y += 22;
            y = drawBullet(graphics, "screen.starrail_sim.light_cone_base_attack", x, y,
                    width);
            y = drawBullet(graphics, "screen.starrail_sim.light_cone_base_health", x, y,
                    width);
            y = drawBullet(graphics, "screen.starrail_sim.light_cone_base_armor", x, y,
                    width);
            y += 12;
            StarRailPath currentPath = StarRailPathClientState.getCurrentPath();
            StarRailPath lightConePath = harmonyLightCone ? StarRailPath.HARMONY
                    : eruditionLightCone ? StarRailPath.ERUDITION
                    : nihilityLightCone ? StarRailPath.NIHILITY
                    : destructionLightCone ? StarRailPath.DESTRUCTION
                    : preservationLightCone ? StarRailPath.PRESERVATION
                    : abundanceLightCone ? StarRailPath.ABUNDANCE
                    : elationLightCone ? StarRailPath.ELATION
                    : remembranceLightCone ? StarRailPath.REMEMBRANCE : StarRailPath.HUNT;
            boolean active = currentPath == lightConePath;
            graphics.drawString(font,
                    Component.translatable(active
                            ? (harmonyLightCone
                            ? "screen.starrail_sim.light_cone_harmony_active"
                            : eruditionLightCone
                            ? "screen.starrail_sim.light_cone_erudition_active"
                            : nihilityLightCone
                            ? "screen.starrail_sim.light_cone_nihility_active"
                            : destructionLightCone
                            ? "screen.starrail_sim.light_cone_destruction_active"
                            : preservationLightCone
                            ? "screen.starrail_sim.light_cone_preservation_active"
                            : abundanceLightCone
                            ? "screen.starrail_sim.light_cone_abundance_active"
                            : elationLightCone
                            ? "screen.starrail_sim.light_cone_elation_active"
                            : remembranceLightCone
                            ? "screen.starrail_sim.light_cone_remembrance_active"
                            : "screen.starrail_sim.light_cone_active")
                            : (harmonyLightCone
                            ? "screen.starrail_sim.light_cone_harmony_inactive"
                            : eruditionLightCone
                            ? "screen.starrail_sim.light_cone_erudition_inactive"
                            : nihilityLightCone
                            ? "screen.starrail_sim.light_cone_nihility_inactive"
                            : destructionLightCone
                            ? "screen.starrail_sim.light_cone_destruction_inactive"
                            : preservationLightCone
                            ? "screen.starrail_sim.light_cone_preservation_inactive"
                            : abundanceLightCone
                            ? "screen.starrail_sim.light_cone_abundance_inactive"
                            : elationLightCone
                            ? "screen.starrail_sim.light_cone_elation_inactive"
                            : remembranceLightCone
                            ? "screen.starrail_sim.light_cone_remembrance_inactive"
                            : "screen.starrail_sim.light_cone_inactive")), x, y,
                    active ? StarRailUiStyle.GOLD_ACCENT : StarRailUiStyle.MUTED_COLOR);
        detailsBottom = Math.max(detailsBottom, y + 12);
        y += 28;
        renderSpecialEffect(graphics, player, stack, x, y, width);
    }

    private void renderSpecialEffect(GuiGraphics graphics, Player player, ItemStack stack,
                                     int x, int y, int width) {
        if (stack.getItem() instanceof IWillHuntItem) {
            renderIWillHuntSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof WorrisomeBlissfulItem) {
            renderWorrisomeBlissfulSpecialEffect(graphics, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof SleepLikeTheDeadItem) {
            renderSleepLikeTheDeadSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof PureThoughtBaptismItem) {
            renderPureThoughtBaptismSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof IdealBurningHellItem) {
            renderIdealBurningHellSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof EmbarkOnSecondLifeItem) {
            renderEmbarkOnSecondLifeSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof FinaleOfALieItem) {
            renderFinaleOfALieSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof SomethingIrreplaceableItem) {
            renderSomethingIrreplaceableSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof BrighterThanTheSunItem) {
            renderBrighterThanTheSunSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof DanceAtSunsetItem) {
            renderDanceAtSunsetSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof TheUnreachableSideItem) {
            renderTheUnreachableSideSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof ThisBodyAsSwordItem) {
            renderThisBodyAsSwordSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof NoRewardCrowningItem) {
            renderNoRewardCrowningSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof BloodFireBurningPathItem) {
            renderBloodFireBurningPathSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof WhereDreamsBelongItem) {
            renderWhereDreamsBelongSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof DawnBurnsJustSoItem) {
            renderDawnBurnsJustSoSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof WhatYouSeeIsMeItem) {
            renderWhatYouSeeIsMeSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof GalaxyRailwayItem) {
            renderGalaxyRailwaySpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof MomentOfGloryItem) {
            renderMomentOfGlorySpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof BeforeDawnItem) {
            renderBeforeDawnSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof PriceOfPeaceItem) {
            renderPriceOfPeaceSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof TowardsUnanswerableItem) {
            renderTowardsUnanswerableSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof NinjaScrollItem) {
            renderNinjaScrollSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof LifeAsALightItem) {
            renderLifeAsALightSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof AStarIlluminatesNightSkyItem) {
            renderAStarIlluminatesNightSkySpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof SparkleQuietlyShinesItem) {
            renderSparkleQuietlyShinesSpecialEffect(graphics, player, stack, x, y, width);
            return;
        }
        if (stack.getItem() instanceof BattleIsntOverItem) {
            renderBattleIsntOverSpecialEffect(graphics, player, stack, x, y, width);
        } else if (stack.getItem() instanceof MemoryOfMeItem) {
            renderMemoryOfMeSpecialEffect(graphics, player, stack, x, y, width);
        } else if (stack.getItem() instanceof WeaveTimeIntoGoldItem) {
            renderWeaveTimeIntoGoldSpecialEffect(graphics, player, stack, x, y, width);
        } else if (stack.getItem() instanceof MakeFarewellMoreBeautifulItem) {
            renderMakeFarewellMoreBeautifulSpecialEffect(
                    graphics, player, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof LoveIsEternalItem) {
            renderLoveIsEternalSpecialEffect(graphics, player, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof StarlightForLongNightsItem) {
            renderStarlightForLongNightsSpecialEffect(
                    graphics, player, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof WelcomeToGalacticCityItem) {
            renderWelcomeToGalacticCitySpecialEffect(graphics, player, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof MeetInTheNextFlowerSeasonItem) {
            renderMeetInTheNextFlowerSeasonSpecialEffect(
                    graphics, player, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof WhenSheDecidesToSeeItem) {
            renderWhenSheDecidesToSeeSpecialEffect(graphics, player, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof FlowerWorldMesmerizingEyesItem) {
            renderFlowerWorldMesmerizingEyesSpecialEffect(
                    graphics, player, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof MayRainbowStayInTheSkyItem) {
            renderMayRainbowStayInTheSkySpecialEffect(graphics, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof NightFlowingColorsItem) {
            renderNightFlowingColorsSpecialEffect(graphics, player, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof GameOfCosmicWorldsItem) {
            renderGameOfCosmicWorldsSpecialEffect(graphics, player, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof ReturningToEarthItem) {
            renderReturningToEarthSpecialEffect(graphics, player, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof IfTimeWereAFlowerItem) {
            renderIfTimeWereAFlowerSpecialEffect(graphics, player, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof AnAgeEtchedInGoldenBloodItem) {
            renderAnAgeEtchedInGoldenBloodSpecialEffect(graphics, player, stack, x, y,
                    width);
            return;
        } else if (stack.getItem() instanceof InTheNameOfTheWorldItem) {
            renderInTheNameOfTheWorldSpecialEffect(graphics, player, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof OnTheShoreInTheFlowOfTimeItem) {
            renderOnTheShoreInTheFlowOfTimeSpecialEffect(graphics, player, stack,
                    x, y, width);
            return;
        } else if (stack.getItem() instanceof AThousandFoldSpringItem) {
            renderAThousandFoldSpringSpecialEffect(graphics, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof OnlyWaitItem) {
            renderOnlyWaitSpecialEffect(graphics, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof RainNeverStopsItem) {
            renderRainNeverStopsSpecialEffect(graphics, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof LiesInTheWindItem) {
            renderLiesInTheWindSpecialEffect(graphics, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof ReturnToLongRoadItem) {
            renderReturnToLongRoadSpecialEffect(graphics, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof ReforgedRemembranceItem) {
            renderReforgedRemembranceSpecialEffect(graphics, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof OceanWhySingsItem) {
            renderOceanWhySingsSpecialEffect(graphics, stack, x, y, width);
        } else if (stack.getItem() instanceof DoNotForgetHerFlameItem) {
            renderDoNotForgetHerFlameSpecialEffect(graphics, stack, x, y, width);
        } else if (stack.getItem() instanceof NewFleshOfInfernoItem) {
            renderNewFleshOfInfernoSpecialEffect(graphics, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof MomentOfVictoryItem) {
            renderMomentOfVictorySpecialEffect(graphics, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof SheHasClosedHerEyesItem) {
            renderSheHasClosedHerEyesSpecialEffect(graphics, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof ThoughRiversAndMountainsItem) {
            renderThoughRiversAndMountainsSpecialEffect(graphics, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof FateNeverFairItem) {
            renderFateNeverFairSpecialEffect(graphics, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof EchoesOfTheCoffinItem) {
            renderEchoesOfTheCoffinSpecialEffect(graphics, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof TimeWaitsForNoOneItem) {
            renderTimeWaitsForNoOneSpecialEffect(graphics, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof NightOfFrightItem) {
            renderNightOfFrightSpecialEffect(graphics, stack, x, y, width);
            return;
        } else if (stack.getItem() instanceof OnlyTheScentRemainsItem) {
            renderOnlyTheScentRemainsSpecialEffect(graphics, stack, x, y, width);
            return;
        }
        int superimposition = InTheNightItem.getSuperimposition(stack);
        graphics.drawString(font,
                Component.translatable("screen.starrail_sim.light_cone_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_crit_rate",
                formatPercent(StarRailLightConeService.critRate(superimposition))), x, y,
                width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_night_butterfly",
                formatPercent(StarRailLightConeService.attackPerStack(superimposition)),
                formatPercent(StarRailLightConeService.critDamagePerStack(superimposition))),
                x, y, width);
        y += 10;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.NIGHT_BUTTERFLY.get());
        if (effect == null) {
            drawWrapped(graphics,
                    Component.translatable("screen.starrail_sim.light_cone_no_night_butterfly"),
                    x, y, width, StarRailUiStyle.MUTED_COLOR);
        } else {
            int stacks = Math.min(StarRailLightConeService.NIGHT_BUTTERFLY_MAX_STACKS,
                    effect.getAmplifier() + 1);
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_night_butterfly", stacks,
                    seconds), x, y, width, StarRailUiStyle.GOLD_ACCENT);
        }
    }

    private void renderIWillHuntSpecialEffect(GuiGraphics graphics, Player player,
                                              ItemStack stack, int x, int y, int width) {
        int superimposition = IWillHuntItem.getSuperimposition(stack);
        graphics.drawString(font,
                Component.translatable("screen.starrail_sim.light_cone_i_will_hunt_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_pursuit_crit_rate",
                formatPercent(StarRailLightConeService.pursuitCritRate(superimposition))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_pursuit_crit_damage",
                formatPercent(StarRailLightConeService.pursuitCritDamage(superimposition))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_flowing_light",
                formatPercent(StarRailLightConeService.pursuitDamagePerStack(superimposition))),
                x, y, width);
        y += 10;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.FLOWING_LIGHT.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_flowing_light"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int stacks = Math.min(StarRailLightConeService.FLOWING_LIGHT_MAX_STACKS,
                    effect.getAmplifier() + 1);
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_flowing_light", stacks,
                    seconds), x, y, width, StarRailUiStyle.GOLD_ACCENT);
        }
    }

    private void renderWorrisomeBlissfulSpecialEffect(GuiGraphics graphics, ItemStack stack,
                                                      int x, int y, int width) {
        int superimposition = WorrisomeBlissfulItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_worrisome_blissful_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_worrisome_blissful_crit_rate",
                formatPercent(StarRailLightConeService.worrisomeBlissfulCritRate(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_worrisome_blissful_attack_damage",
                formatPercent(StarRailLightConeService.worrisomeBlissfulAttackDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_tamed",
                formatPercent(StarRailLightConeService.worrisomeBlissfulCritDamagePerStack(
                        superimposition))), x, y, width);
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_tamed_limit"), x, y + 4, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderSleepLikeTheDeadSpecialEffect(GuiGraphics graphics, Player player,
                                                     ItemStack stack, int x, int y,
                                                     int width) {
        int superimposition = SleepLikeTheDeadItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_sleep_like_the_dead_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_sleep_like_the_dead_crit_damage",
                formatPercent(StarRailLightConeService.sleepLikeTheDeadCritDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_sleep_like_the_dead_attack_damage",
                formatPercent(StarRailLightConeService.sleepLikeTheDeadAttackDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_beautiful_dream",
                formatPercent(StarRailLightConeService.beautifulDreamCritRate(
                        superimposition))), x, y, width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.BEAUTIFUL_DREAM.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_beautiful_dream"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_beautiful_dream", seconds),
                    x, y, width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_beautiful_dream_cooldown"), x,
                y + 22, width, StarRailUiStyle.MUTED_COLOR);
    }

    private void renderPureThoughtBaptismSpecialEffect(GuiGraphics graphics, Player player,
                                                       ItemStack stack, int x, int y,
                                                       int width) {
        int superimposition = PureThoughtBaptismItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_pure_thought_baptism_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_pure_thought_baptism_crit_damage",
                formatPercent(StarRailLightConeService.pureThoughtBaptismCritDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_thought_training",
                formatPercent(StarRailLightConeService.thoughtTrainingCritDamagePerStack(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_debate",
                formatPercent(StarRailLightConeService.debateDamage(superimposition)),
                formatPercent(StarRailLightConeService.debateAttackDamage(superimposition))),
                x, y, width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.DEBATE.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_debate"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_debate", seconds), x, y,
                    width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_debate_limit"), x, y + 22, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderIdealBurningHellSpecialEffect(GuiGraphics graphics, Player player,
                                                     ItemStack stack, int x, int y,
                                                     int width) {
        int superimposition = IdealBurningHellItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_ideal_burning_hell_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_ideal_burning_hell_crit_rate",
                formatPercent(StarRailLightConeService.rangerCritRate(superimposition))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_ideal_burning_hell_attack_damage",
                formatPercent(StarRailLightConeService.rangerAttackDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_ranger",
                formatPercent(StarRailLightConeService.rangerAttackPerStack(
                        superimposition))), x, y, width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.RANGER.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_ranger"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int stacks = Math.min(StarRailLightConeService.RANGER_MAX_STACKS,
                    effect.getAmplifier() + 1);
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_ranger", stacks,
                    seconds), x, y, width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_ranger_limit"), x, y + 22, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderEmbarkOnSecondLifeSpecialEffect(GuiGraphics graphics, Player player,
                                                       ItemStack stack, int x, int y,
                                                       int width) {
        int superimposition = EmbarkOnSecondLifeItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_embark_on_second_life_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_embark_on_second_life_break_effect",
                formatPercent(StarRailLightConeService.painfulVoyageBreakEffect(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_embark_on_second_life_break_damage",
                formatPercent(StarRailLightConeService.painfulVoyageBreakDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_painful_voyage_attack",
                formatPercent(StarRailLightConeService.painfulVoyageAttackDamage(
                        superimposition))), x, y, width);
        y += 4;
        drawWrapped(graphics, Component.translatable(
                player != null && player.hasEffect(StarRailSimMod.PAINFUL_VOYAGE.get())
                        ? "screen.starrail_sim.light_cone_current_painful_voyage"
                        : "screen.starrail_sim.light_cone_no_painful_voyage"), x, y, width,
                player != null && player.hasEffect(StarRailSimMod.PAINFUL_VOYAGE.get())
                ? StarRailUiStyle.GOLD_ACCENT : StarRailUiStyle.MUTED_COLOR);
    }

    private void renderFinaleOfALieSpecialEffect(GuiGraphics graphics, Player player,
                                                 ItemStack stack, int x, int y, int width) {
        int superimposition = FinaleOfALieItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_finale_of_a_lie_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_finale_of_a_lie_crit_rate",
                formatPercent(StarRailLightConeService.finaleOfALieCritRate(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_shadow_devour_attack",
                formatPercent(StarRailLightConeService.shadowDevourAttackDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_shadow_devour_damage",
                formatPercent(StarRailLightConeService.shadowDevourDamage(
                        superimposition))), x, y, width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.SHADOW_DEVOUR.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_shadow_devour"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_shadow_devour", seconds), x, y,
                    width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_shadow_devour_limit"), x, y + 22,
                width, StarRailUiStyle.MUTED_COLOR);
    }

    private void renderSomethingIrreplaceableSpecialEffect(GuiGraphics graphics,
                                                            Player player, ItemStack stack,
                                                            int x, int y, int width) {
        int superimposition = SomethingIrreplaceableItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_something_irreplaceable_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_something_irreplaceable_attack",
                formatPercent(StarRailLightConeService
                        .somethingIrreplaceableAttackDamage(superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_something_irreplaceable_heal",
                formatPercent(StarRailLightConeService.familyHealing(superimposition))), x, y,
                width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_family_damage",
                formatPercent(StarRailLightConeService.familyDamage(superimposition))), x, y,
                width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.FAMILY.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_family"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int stacks = Math.min(StarRailLightConeService.FAMILY_MAX_STACKS,
                    effect.getAmplifier() + 1);
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_family", stacks, seconds), x, y,
                    width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_family_limit"), x, y + 22, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderBrighterThanTheSunSpecialEffect(GuiGraphics graphics, Player player,
                                                       ItemStack stack, int x, int y,
                                                       int width) {
        int superimposition = BrighterThanTheSunItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_brighter_than_the_sun_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_brighter_than_the_sun_crit_rate",
                formatPercent(StarRailLightConeService
                        .brighterThanTheSunCritRate(superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_dragon_roar_attack",
                formatPercent(StarRailLightConeService
                        .dragonRoarAttackDamage(superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_dragon_roar_crit_damage",
                formatPercent(StarRailLightConeService
                        .dragonRoarCritDamage(superimposition))), x, y, width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.DRAGON_ROAR.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_dragon_roar"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int stacks = Math.min(StarRailLightConeService.DRAGON_ROAR_MAX_STACKS,
                    effect.getAmplifier() + 1);
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_dragon_roar", stacks, seconds),
                    x, y, width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_dragon_roar_limit"), x, y + 22, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderDanceAtSunsetSpecialEffect(GuiGraphics graphics, Player player,
                                                  ItemStack stack, int x, int y, int width) {
        int superimposition = DanceAtSunsetItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_dance_at_sunset_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_dance_at_sunset_crit_damage",
                formatPercent(StarRailLightConeService.danceAtSunsetCritDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_fire_dance_damage",
                formatPercent(StarRailLightConeService.fireDanceDamage(
                        superimposition))), x, y, width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.FIRE_DANCE.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_fire_dance"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int stacks = Math.min(StarRailLightConeService.FIRE_DANCE_MAX_STACKS,
                    effect.getAmplifier() + 1);
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_fire_dance", stacks, seconds),
                    x, y, width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_fire_dance_limit"), x, y + 22, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderTheUnreachableSideSpecialEffect(GuiGraphics graphics, Player player,
                                                       ItemStack stack, int x, int y,
                                                       int width) {
        int superimposition = TheUnreachableSideItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_the_unreachable_side_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_the_unreachable_side_crit_rate",
                formatPercent(StarRailLightConeService.noRetreatCritRate(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_the_unreachable_side_max_health",
                formatPercent(StarRailLightConeService.noRetreatMaxHealth(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_no_retreat_damage",
                formatPercent(StarRailLightConeService.noRetreatDamage(
                        superimposition))), x, y, width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.NO_RETREAT.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_no_retreat"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_no_retreat", seconds), x, y,
                    width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_no_retreat_limit"), x, y + 22, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderThisBodyAsSwordSpecialEffect(GuiGraphics graphics, Player player,
                                                    ItemStack stack, int x, int y,
                                                    int width) {
        int superimposition = ThisBodyAsSwordItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_this_body_as_sword_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_this_body_as_sword_crit_damage",
                formatPercent(StarRailLightConeService.moonEclipseCritDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_moon_eclipse_damage",
                formatPercent(StarRailLightConeService.moonEclipseDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_moon_eclipse_attack",
                formatPercent(StarRailLightConeService.moonEclipseFullAttack(
                        superimposition))), x, y, width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.MOON_ECLIPSE.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_moon_eclipse"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int stacks = Math.min(StarRailLightConeService.MOON_ECLIPSE_MAX_STACKS,
                    effect.getAmplifier() + 1);
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_moon_eclipse", stacks,
                    seconds), x, y, width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_moon_eclipse_limit"), x, y + 22, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderNoRewardCrowningSpecialEffect(GuiGraphics graphics, Player player,
                                                     ItemStack stack, int x, int y,
                                                     int width) {
        int superimposition = NoRewardCrowningItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_no_reward_crowning_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_no_reward_crowning_crit_damage",
                formatPercent(StarRailLightConeService.noRewardCrowningCritDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_knight_king_attack",
                formatPercent(StarRailLightConeService.knightKingAttack(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_knight_king_break_attack",
                formatPercent(StarRailLightConeService.knightKingAttack(
                        superimposition))), x, y, width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.KNIGHT_KING.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_knight_king"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_knight_king", seconds), x, y,
                    width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_knight_king_limit"), x, y + 22, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderBloodFireBurningPathSpecialEffect(GuiGraphics graphics, Player player,
                                                         ItemStack stack, int x, int y,
                                                         int width) {
        int superimposition = BloodFireBurningPathItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_blood_fire_burning_path_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_blood_fire_burning_path_max_health",
                formatPercent(StarRailLightConeService.bloodFireMaxHealth(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_blood_fire_burning_path_healing",
                formatPercent(StarRailLightConeService.bloodFireHealing(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_strife_damage",
                formatPercent(StarRailLightConeService.strifeDamage(superimposition))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_strife_extra",
                formatPercent(StarRailLightConeService.strifeDamage(superimposition))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_strife_cost",
                formatPercent(StarRailLightConeService.strifeHealthCost(superimposition))),
                x, y, width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.STRIFE.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_strife"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            double currentDamage = StarRailLightConeService.strifeDamage(superimposition)
                    * (effect.getAmplifier() > 0 ? 2.0D : 1.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_strife", formatPercent(currentDamage),
                    seconds), x, y, width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_strife_limit"), x, y + 22, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderWhereDreamsBelongSpecialEffect(GuiGraphics graphics, Player player,
                                                       ItemStack stack, int x, int y,
                                                       int width) {
        int superimposition = WhereDreamsBelongItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_where_dreams_belong_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_where_dreams_belong_break_effect",
                formatPercent(StarRailLightConeService.metamorphosisBreakEffect(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_metamorphosis_damage",
                formatPercent(StarRailLightConeService.metamorphosisDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_metamorphosis_attack",
                formatPercent(StarRailLightConeService.metamorphosisAttack(
                        superimposition))), x, y, width);
        y += 4;
        boolean active = player != null
                && player.hasEffect(StarRailSimMod.METAMORPHOSIS.get());
        drawWrapped(graphics, Component.translatable(active
                        ? "screen.starrail_sim.light_cone_current_metamorphosis"
                        : "screen.starrail_sim.light_cone_no_metamorphosis"),
                x, y, width, active ? StarRailUiStyle.GOLD_ACCENT
                        : StarRailUiStyle.MUTED_COLOR);
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_metamorphosis_limit"), x, y + 22, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderDawnBurnsJustSoSpecialEffect(GuiGraphics graphics, Player player,
                                                    ItemStack stack, int x, int y,
                                                    int width) {
        int superimposition = DawnBurnsJustSoItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_dawn_burns_just_so_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_dawn_burns_just_so_attack",
                formatPercent(StarRailLightConeService.dawnBurnsAttack(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_dawn_burns_just_so_damage",
                formatPercent(StarRailLightConeService.dawnBurnsDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_blazing_sun_damage",
                formatPercent(StarRailLightConeService.blazingSunDamage(
                        superimposition))), x, y, width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.BLAZING_SUN.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_blazing_sun"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_blazing_sun", seconds), x, y,
                    width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_blazing_sun_limit"), x, y + 22, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderWhatYouSeeIsMeSpecialEffect(GuiGraphics graphics, Player player,
                                                   ItemStack stack, int x, int y,
                                                   int width) {
        int superimposition = WhatYouSeeIsMeItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_what_you_see_is_me_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_what_you_see_is_me_attack",
                formatPercent(StarRailLightConeService.whatYouSeeIsMeAttack(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_what_you_see_is_me_next_damage",
                formatPercent(StarRailLightConeService.whatYouSeeIsMeNextDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_kingly_entertainment_crit_damage",
                formatPercent(StarRailLightConeService.kinglyEntertainmentCritDamage(
                        superimposition))), x, y, width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.KINGLY_ENTERTAINMENT.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_kingly_entertainment"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_kingly_entertainment", seconds),
                    x, y, width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_kingly_entertainment_limit"), x, y + 22,
                width, StarRailUiStyle.MUTED_COLOR);
    }

    private void renderGalaxyRailwaySpecialEffect(GuiGraphics graphics, Player player,
                                                  ItemStack stack, int x, int y,
                                                  int width) {
        int superimposition = GalaxyRailwayItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_galaxy_railway_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        int nearbyEnemies = player == null ? 0 : Math.min(
                StarRailLightConeService.METEOR_MAX_STACKS,
                player.level().getEntitiesOfClass(Monster.class,
                        player.getBoundingBox().inflate(10.0D),
                        entity -> entity.isAlive()).size());
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_galaxy_railway_attack_per_enemy",
                formatPercent(StarRailLightConeService.meteorAttackPerEnemy(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_meteor_damage",
                formatPercent(StarRailLightConeService.meteorDamage(superimposition))),
                x, y, width);
        y += 4;
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_current_meteor_count", nearbyEnemies),
                x, y, width, StarRailUiStyle.MUTED_COLOR);
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.METEOR.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_meteor"), x, y + 22, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_meteor", seconds), x, y + 22,
                    width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_meteor_limit"), x, y + 44, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderMomentOfGlorySpecialEffect(GuiGraphics graphics, Player player,
                                                  ItemStack stack, int x, int y,
                                                  int width) {
        int superimposition = MomentOfGloryItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_moment_of_glory_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_moment_of_glory_crit_damage",
                formatPercent(StarRailLightConeService.momentOfGloryCritDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_knightly_courtesy_attack",
                formatPercent(StarRailLightConeService.knightlyCourtesyAttack(
                        superimposition))), x, y, width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.KNIGHTLY_COURTESY.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_knightly_courtesy"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int stacks = Math.min(StarRailLightConeService.KNIGHTLY_COURTESY_MAX_STACKS,
                    effect.getAmplifier() + 1);
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_knightly_courtesy", stacks,
                    StarRailLightConeService.KNIGHTLY_COURTESY_MAX_STACKS, seconds),
                    x, y, width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_knightly_courtesy_limit"), x, y + 22,
                width, StarRailUiStyle.MUTED_COLOR);
    }

    private void renderBeforeDawnSpecialEffect(GuiGraphics graphics, Player player,
                                               ItemStack stack, int x, int y,
                                               int width) {
        int superimposition = BeforeDawnItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_before_dawn_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_before_dawn_crit_damage",
                formatPercent(StarRailLightConeService.beforeDawnCritDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_before_dawn_attack",
                formatPercent(StarRailLightConeService.beforeDawnAttack(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_dream_body_damage",
                formatPercent(StarRailLightConeService.dreamBodyDamage(
                        superimposition))), x, y, width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.DREAM_BODY.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_dream_body"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int stacks = Math.min(StarRailLightConeService.DREAM_BODY_MAX_STACKS,
                    effect.getAmplifier() + 1);
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_dream_body", stacks,
                    StarRailLightConeService.DREAM_BODY_MAX_STACKS, seconds),
                    x, y, width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_dream_body_limit"), x, y + 22, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderPriceOfPeaceSpecialEffect(GuiGraphics graphics, Player player,
                                                 ItemStack stack, int x, int y,
                                                 int width) {
        int superimposition = PriceOfPeaceItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_price_of_peace_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_price_of_peace_crit_rate",
                formatPercent(StarRailLightConeService.priceOfPeaceCritRate(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_price_of_peace_over_crit_damage",
                formatPercent(StarRailLightConeService.priceOfPeaceDamagePerCritStack(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_price_of_peace_promise_attack",
                formatPercent(StarRailLightConeService.promiseAttack(
                        superimposition))), x, y, width);
        y += 4;
        double critDamage = player == null ? StarRailAttributes.DEFAULT_CRIT_DAMAGE
                : StarRailAttributes.getValue(player, StarRailAttributes.CRIT_DAMAGE,
                StarRailAttributes.DEFAULT_CRIT_DAMAGE);
        int thresholdStacks = Math.max(0, Math.min(3,
                (int) Math.floor((critDamage - 1.20D) / 0.05D + 1.0E-9D)));
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_price_of_peace_current_crit_stacks",
                formatPercent(critDamage), thresholdStacks), x, y, width,
                StarRailUiStyle.GOLD_ACCENT);
        y += 22;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.PROMISE.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_promise"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int stacks = Math.min(StarRailLightConeService.PROMISE_MAX_STACKS,
                    effect.getAmplifier() + 1);
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_promise", stacks,
                    StarRailLightConeService.PROMISE_MAX_STACKS, seconds),
                    x, y, width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_price_of_peace_promise_limit"), x, y + 22,
                width, StarRailUiStyle.MUTED_COLOR);
    }

    private void renderTowardsUnanswerableSpecialEffect(GuiGraphics graphics, Player player,
                                                        ItemStack stack, int x, int y,
                                                        int width) {
        int superimposition = TowardsUnanswerableItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_towards_unanswerable_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_towards_unanswerable_crit_rate",
                formatPercent(StarRailLightConeService.towardsUnanswerableCritRate(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_deconstruction_damage",
                formatPercent(StarRailLightConeService.deconstructionDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_deconstruction_attack",
                formatPercent(StarRailLightConeService.deconstructionAttack(
                        superimposition))), x, y, width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.DECONSTRUCTION.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_deconstruction"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int stacks = Math.min(StarRailLightConeService.DECONSTRUCTION_MAX_STACKS,
                    effect.getAmplifier() + 1);
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_deconstruction", stacks,
                    StarRailLightConeService.DECONSTRUCTION_MAX_STACKS, seconds),
                    x, y, width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_deconstruction_limit"), x, y + 22, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderNinjaScrollSpecialEffect(GuiGraphics graphics, Player player,
                                                ItemStack stack, int x, int y,
                                                int width) {
        int superimposition = NinjaScrollItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_ninja_scroll_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_ninja_scroll_break_effect",
                formatPercent(StarRailLightConeService.ninjaScrollBreakEffect(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_ninja_scroll_crit_damage",
                formatPercent(StarRailLightConeService.ninjaScrollCritDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_thunder_escape_attack",
                formatPercent(StarRailLightConeService.thunderEscapeAttack(
                        superimposition))), x, y, width);
        y += 4;
        double breakEffect = player == null ? 0.0D
                : StarRailAttributes.getValue(player, StarRailAttributes.BREAK_EFFECT, 0.0D);
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_ninja_scroll_current_break_effect",
                formatPercent(breakEffect), breakEffect >= 1.0D
                        ? Component.translatable("screen.starrail_sim.active")
                        : Component.translatable("screen.starrail_sim.inactive")), x, y, width,
                breakEffect >= 1.0D ? StarRailUiStyle.GOLD_ACCENT
                        : StarRailUiStyle.MUTED_COLOR);
        y += 22;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.THUNDER_ESCAPE.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_thunder_escape"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int stacks = Math.min(StarRailLightConeService.THUNDER_ESCAPE_MAX_STACKS,
                    effect.getAmplifier() + 1);
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_thunder_escape", stacks,
                    StarRailLightConeService.THUNDER_ESCAPE_MAX_STACKS, seconds),
                    x, y, width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_thunder_escape_limit"), x, y + 22, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderLifeAsALightSpecialEffect(GuiGraphics graphics, Player player,
                                                 ItemStack stack, int x, int y,
                                                 int width) {
        int superimposition = LifeAsALightItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_life_as_a_light_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_life_as_a_light_crit_damage",
                formatPercent(StarRailLightConeService.lifeAsALightCritDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_alchemy_damage",
                formatPercent(StarRailLightConeService.alchemyDamage(superimposition))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_alchemy_attack",
                formatPercent(StarRailLightConeService.alchemyAttack(superimposition))),
                x, y, width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.ALCHEMY.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_alchemy"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_alchemy", seconds), x, y,
                    width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_alchemy_limit"), x, y + 22, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderAStarIlluminatesNightSkySpecialEffect(GuiGraphics graphics,
                                                             Player player, ItemStack stack,
                                                             int x, int y, int width) {
        int superimposition = AStarIlluminatesNightSkyItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_a_star_illuminates_night_sky_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_a_star_illuminates_night_sky_crit_damage",
                formatPercent(StarRailLightConeService.aStarIlluminatesCritDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_departure_damage",
                formatPercent(StarRailLightConeService.departureDamage(superimposition))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_departure_attack",
                formatPercent(StarRailLightConeService.departureAttack(superimposition))),
                x, y, width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.DEPARTURE.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_departure"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int stacks = Math.min(StarRailLightConeService.DEPARTURE_MAX_STACKS,
                    effect.getAmplifier() + 1);
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_departure", stacks,
                    StarRailLightConeService.DEPARTURE_MAX_STACKS, seconds),
                    x, y, width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_departure_limit"), x, y + 22, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderSparkleQuietlyShinesSpecialEffect(GuiGraphics graphics, Player player,
                                                         ItemStack stack, int x, int y,
                                                         int width) {
        int superimposition = SparkleQuietlyShinesItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_sparkle_quietly_shines_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_sparkle_quietly_shines_crit_rate",
                formatPercent(StarRailLightConeService.sparkleQuietlyShinesCritRate(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_shining_crown_attack",
                formatPercent(StarRailLightConeService.shiningCrownAttack(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_shining_crown_damage",
                formatPercent(StarRailLightConeService.shiningCrownDamage(
                        superimposition))), x, y, width);
        y += 4;
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.SHINING_CROWN.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_shining_crown"), x, y, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_shining_crown", seconds), x, y,
                    width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_shining_crown_limit"), x, y + 22, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderBattleIsntOverSpecialEffect(GuiGraphics graphics, Player player,
                                                   ItemStack stack, int x, int y,
                                                   int width) {
        int superimposition = BattleIsntOverItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_battle_isnt_over_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_inheritance_attack",
                formatPercent(StarRailLightConeService.inheritanceAttack(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_inheritance_damage",
                formatPercent(StarRailLightConeService.inheritanceDamage(
                        superimposition))), x, y, width);
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.INHERITANCE.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_inheritance"), x, y + 4, width,
                    StarRailUiStyle.MUTED_COLOR);
        } else {
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_inheritance", seconds), x,
                    y + 4, width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_inheritance_limit"), x, y + 26, width,
                StarRailUiStyle.MUTED_COLOR);
    }

    private void renderMemoryOfMeSpecialEffect(GuiGraphics graphics, Player player,
                                                ItemStack stack, int x, int y, int width) {
        int superimposition = MemoryOfMeItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_memory_of_me_special"), x, y,
                StarRailUiStyle.GOLD_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_plum_break_effect",
                formatPercent(StarRailLightConeService.mirrorBreakEffect(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_plum_fragrance_attack",
                formatPercent(StarRailLightConeService.plumFragranceAttack(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_plum_fragrance_damage",
                formatPercent(StarRailLightConeService.plumFragranceDamage(
                        superimposition))), x, y, width);
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.PLUM_FRAGRANCE.get());
        if (effect == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_no_plum_fragrance"), x, y + 4,
                    width, StarRailUiStyle.MUTED_COLOR);
        } else {
            int stacks = Math.min(StarRailLightConeService.PLUM_FRAGRANCE_MAX_STACKS,
                    effect.getAmplifier() + 1);
            int seconds = (int) Math.ceil(effect.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_current_plum_fragrance",
                    stacks, seconds), x, y + 4, width, StarRailUiStyle.GOLD_ACCENT);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_plum_fragrance_limit"), x, y + 26,
                width, StarRailUiStyle.MUTED_COLOR);
    }

    private void renderWeaveTimeIntoGoldSpecialEffect(GuiGraphics graphics, Player player,
                                                       ItemStack stack,
                                                       int x, int y, int width) {
        int superimposition = WeaveTimeIntoGoldItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_weave_time_into_gold_special"), x, y,
                StarRailUiStyle.GOLD_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_weave_time_into_gold_attack",
                formatPercent(StarRailLightConeService.weaveTimeIntoGoldAttack(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_weave_time_into_gold_crit_damage",
                formatPercent(StarRailLightConeService.weaveTimeIntoGoldCritDamage(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_weave_time_into_gold_full_stack",
                formatPercent(StarRailLightConeService.weaveTimeIntoGoldDamagePerStack(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_weave_time_into_gold_duration"), x, y, width);
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.BROCADE.get());
        int stacks = effect == null ? 0 : Math.min(
                StarRailLightConeService.BROCADE_MAX_STACKS, effect.getAmplifier() + 1);
        int remainingSeconds = effect == null ? 0 : (effect.getDuration() + 19) / 20;
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_weave_time_into_gold_current_stacks",
                stacks, StarRailLightConeService.BROCADE_MAX_STACKS, remainingSeconds), x, y + 4, width,
                stacks > 0 ? StarRailUiStyle.GOLD_ACCENT : StarRailUiStyle.MUTED_COLOR);
    }

    private void renderMakeFarewellMoreBeautifulSpecialEffect(
            GuiGraphics graphics, Player player, ItemStack stack, int x, int y, int width) {
        int superimposition = MakeFarewellMoreBeautifulItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_make_farewell_more_beautiful_special",
                superimposition), x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_make_farewell_more_beautiful_max_health",
                formatPercent(StarRailLightConeService.farewellMaxHealth(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_make_farewell_more_beautiful_attack",
                formatPercent(StarRailLightConeService.farewellAttack(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_make_farewell_more_beautiful_stacks",
                formatPercent(StarRailLightConeService.netherBloomDamagePerStack(
                        superimposition))), x, y, width);
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.NETHER_BLOOM.get());
        int stacks = effect == null ? 0 : Math.min(
                StarRailLightConeService.NETHER_BLOOM_MAX_STACKS,
                effect.getAmplifier() + 1);
        int remainingSeconds = effect == null ? 0 : (effect.getDuration() + 19) / 20;
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_make_farewell_more_beautiful_current_stacks",
                stacks, StarRailLightConeService.NETHER_BLOOM_MAX_STACKS,
                remainingSeconds), x, y + 4, width,
                stacks > 0 ? StarRailUiStyle.GOLD_ACCENT : StarRailUiStyle.MUTED_COLOR);
    }

    private void renderLoveIsEternalSpecialEffect(
            GuiGraphics graphics, Player player, ItemStack stack, int x, int y, int width) {
        int level = LoveIsEternalItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_love_is_eternal_special", level),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_love_is_eternal_attack",
                formatPercent(StarRailLightConeService.loveIsEternalAttack(level))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_love_is_eternal_blank",
                formatPercent(StarRailLightConeService.loveIsEternalBlankDamage(level))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_love_is_eternal_verse",
                formatPercent(StarRailLightConeService.loveIsEternalVerseCritDamage(level))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_love_is_eternal_synergy",
                formatPercent(StarRailLightConeService.loveIsEternalSynergy(level))),
                x, y, width);
        MobEffectInstance blank = player == null ? null
                : player.getEffect(StarRailSimMod.BLANK.get());
        MobEffectInstance verse = player == null ? null
                : player.getEffect(StarRailSimMod.VERSE.get());
        int blankSeconds = blank == null ? 0 : (blank.getDuration() + 19) / 20;
        int verseSeconds = verse == null ? 0 : (verse.getDuration() + 19) / 20;
        y += 4;
        y = drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_love_is_eternal_blank_timer", blankSeconds),
                x, y, width, blankSeconds > 0
                        ? StarRailUiStyle.CYAN_ACCENT : StarRailUiStyle.MUTED_COLOR);
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_love_is_eternal_verse_timer", verseSeconds),
                x, y, width, verseSeconds > 0
                        ? StarRailUiStyle.CYAN_ACCENT : StarRailUiStyle.MUTED_COLOR);
    }

    private void renderStarlightForLongNightsSpecialEffect(
            GuiGraphics graphics, Player player, ItemStack stack, int x, int y, int width) {
        int level = StarlightForLongNightsItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_starlight_for_long_nights_special", level),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_starlight_for_long_nights_health",
                formatPercent(StarRailLightConeService
                        .starlightForLongNightsMaxHealth(level))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_starlight_for_long_nights_attack",
                formatPercent(StarRailLightConeService
                        .starlightForLongNightsAttack(level))), x, y, width);
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_starlight_for_long_nights_damage",
                formatPercent(StarRailLightConeService.starlitNightDamage(level))),
                x, y, width);
        MobEffectInstance night = player == null ? null
                : player.getEffect(StarRailSimMod.STARLIT_NIGHT.get());
        int seconds = night == null ? 0 : (night.getDuration() + 19) / 20;
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_starlight_for_long_nights_timer", seconds),
                x, y + 4, width, seconds > 0
                        ? StarRailUiStyle.CYAN_ACCENT : StarRailUiStyle.MUTED_COLOR);
    }

    private void renderWelcomeToGalacticCitySpecialEffect(
            GuiGraphics graphics, Player player, ItemStack stack, int x, int y, int width) {
        int level = WelcomeToGalacticCityItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_welcome_to_galactic_city_special", level),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_welcome_to_galactic_city_attack",
                formatPercent(StarRailLightConeService
                        .welcomeToGalacticCityAttack(level))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_welcome_to_galactic_city_damage",
                formatPercent(StarRailLightConeService
                        .welcomeToGalacticCityDamage(level))), x, y, width);
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_welcome_to_galactic_city_streak",
                StarRailLightConeService.WINNING_STREAK_MAX_STACKS,
                StarRailLightConeService.WINNING_STREAK_DURATION / 20,
                formatPercent(StarRailLightConeService
                        .winningStreakCritDamagePerStack(level))), x, y, width);
        MobEffectInstance streak = player == null ? null
                : player.getEffect(StarRailSimMod.WINNING_STREAK.get());
        if (streak != null) {
            int seconds = (streak.getDuration() + 19) / 20;
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_welcome_to_galactic_city_current",
                    Math.min(StarRailLightConeService.WINNING_STREAK_MAX_STACKS,
                            streak.getAmplifier() + 1), seconds), x, y + 4, width,
                    StarRailUiStyle.GOLD_ACCENT);
        }
    }

    private void renderMeetInTheNextFlowerSeasonSpecialEffect(
            GuiGraphics graphics, Player player, ItemStack stack, int x, int y, int width) {
        int level = MeetInTheNextFlowerSeasonItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_meet_in_the_next_flower_season_special", level),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_meet_in_the_next_flower_season_crit_damage",
                formatPercent(StarRailLightConeService
                        .meetInTheNextFlowerSeasonCritDamage(level))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_meet_in_the_next_flower_season_attack",
                formatPercent(StarRailLightConeService
                        .meetInTheNextFlowerSeasonAttack(level))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_meet_in_the_next_flower_season_daydream",
                StarRailLightConeService.DAYDREAM_MAX_STACKS,
                StarRailLightConeService.DAYDREAM_DURATION / 20,
                formatPercent(StarRailLightConeService.daydreamDamagePerStack(level))),
                x, y, width);
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.DAYDREAM.get());
        if (effect != null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_meet_in_the_next_flower_season_current",
                    Math.min(StarRailLightConeService.DAYDREAM_MAX_STACKS,
                            effect.getAmplifier() + 1), (effect.getDuration() + 19) / 20),
                    x, y + 4, width, StarRailUiStyle.GOLD_ACCENT);
        }
    }

    private void renderWhenSheDecidesToSeeSpecialEffect(
            GuiGraphics graphics, Player player, ItemStack stack, int x, int y, int width) {
        int level = WhenSheDecidesToSeeItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_when_she_decides_to_see_special", level),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_when_she_decides_to_see_attack",
                formatPercent(StarRailLightConeService.whenSheDecidesToSeeAttack(level))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_when_she_decides_to_see_fortune",
                formatPercent(StarRailLightConeService.bestFortuneCritRate(level)),
                formatPercent(StarRailLightConeService.bestFortuneCritDamage(level)),
                formatPercent(StarRailLightConeService.bestFortuneDamage(level))), x, y, width);
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.BEST_FORTUNE.get());
        if (effect != null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_when_she_decides_to_see_current",
                    effect.getDuration() / 20), x, y + 4, width, StarRailUiStyle.GOLD_ACCENT);
        }
    }

    private void renderFlowerWorldMesmerizingEyesSpecialEffect(
            GuiGraphics graphics, Player player, ItemStack stack, int x, int y, int width) {
        int level = FlowerWorldMesmerizingEyesItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_flower_world_mesmerizing_eyes_special",
                level), x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_flower_world_mesmerizing_eyes_crit_damage",
                formatPercent(StarRailLightConeService.flowerWorldCritDamage(level))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_flower_world_mesmerizing_eyes_push_stream",
                StarRailLightConeService.PUSH_STREAM_MAX_STACKS,
                StarRailLightConeService.PUSH_STREAM_DURATION / 20,
                formatPercent(StarRailLightConeService.pushStreamDamagePerStack(level)),
                formatPercent(StarRailLightConeService.pushStreamCritDamagePerStack(level))),
                x, y, width);
        MobEffectInstance effect = player == null ? null
                : player.getEffect(StarRailSimMod.PUSH_STREAM.get());
        if (effect != null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_flower_world_mesmerizing_eyes_current",
                    Math.min(StarRailLightConeService.PUSH_STREAM_MAX_STACKS,
                            effect.getAmplifier() + 1), (effect.getDuration() + 19) / 20),
                    x, y + 4, width, StarRailUiStyle.GOLD_ACCENT);
        }
    }

    private void renderMayRainbowStayInTheSkySpecialEffect(
            GuiGraphics graphics, ItemStack stack, int x, int y, int width) {
        int level = MayRainbowStayInTheSkyItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_may_rainbow_stay_in_the_sky_special", level),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_may_rainbow_stay_in_the_sky_health",
                formatPercent(StarRailLightConeService.mayRainbowMaxHealth(level))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_may_rainbow_stay_in_the_sky_healing",
                formatPercent(StarRailLightConeService.mayRainbowHealing(level))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_may_rainbow_stay_in_the_sky_cost",
                formatPercent(StarRailLightConeService.mayRainbowHealthCost(level))),
                x, y, width);
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_may_rainbow_stay_in_the_sky_burst",
                StarRailLightConeService.MAY_RAINBOW_ATTACKS_REQUIRED,
                formatPercent(StarRailLightConeService.mayRainbowDamageMultiplier(level))),
                x, y, width);
    }

    private void renderNightFlowingColorsSpecialEffect(GuiGraphics graphics, Player player,
                                                        ItemStack stack, int x, int y,
                                                        int width) {
        int superimposition = NightFlowingColorsItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_night_flowing_colors_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_night_flowing_colors_chant",
                formatPercent(StarRailLightConeService.nightFlowingColorsChantAttack(
                        superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_night_flowing_colors_splendor",
                formatPercent(StarRailLightConeService.nightFlowingColorsSplendorAttack(
                        superimposition)),
                formatPercent(StarRailLightConeService.nightFlowingColorsSplendorDamage(
                        superimposition))), x, y, width);
        MobEffectInstance chant = player == null ? null
                : player.getEffect(StarRailSimMod.CHANT.get());
        MobEffectInstance splendor = player == null ? null
                : player.getEffect(StarRailSimMod.SPLENDOR.get());
        if (splendor != null) {
            int seconds = (int) Math.ceil(splendor.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_night_flowing_colors_active_splendor",
                    seconds), x, y + 4, width, StarRailUiStyle.GOLD_ACCENT);
        } else if (chant != null) {
            int stacks = Math.min(StarRailLightConeService
                    .NIGHT_FLOWING_COLORS_MAX_CHANT_STACKS, chant.getAmplifier() + 1);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_night_flowing_colors_active_chant",
                    stacks, StarRailLightConeService.NIGHT_FLOWING_COLORS_MAX_CHANT_STACKS),
                    x, y + 4, width, StarRailUiStyle.GOLD_ACCENT);
        } else {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_night_flowing_colors_inactive"), x,
                    y + 4, width, StarRailUiStyle.MUTED_COLOR);
        }
        drawWrapped(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_night_flowing_colors_limit"), x, y + 26,
                width, StarRailUiStyle.MUTED_COLOR);
    }

    private void renderGameOfCosmicWorldsSpecialEffect(GuiGraphics graphics, Player player,
                                                       ItemStack stack, int x, int y,
                                                       int width) {
        int superimposition = GameOfCosmicWorldsItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_game_of_cosmic_worlds_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_game_of_cosmic_worlds_crit_damage",
                formatPercent(StarRailLightConeService
                        .gameOfCosmicWorldsCritDamage(superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_game_of_cosmic_worlds_mask",
                formatPercent(StarRailLightConeService
                        .gameOfCosmicWorldsMaskCritRate(superimposition)),
                formatPercent(StarRailLightConeService
                        .gameOfCosmicWorldsMaskCritDamage(superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_game_of_cosmic_worlds_flame"), x, y, width);
        MobEffectInstance mask = player == null ? null
                : player.getEffect(StarRailSimMod.MASK.get());
        MobEffectInstance flame = player == null ? null
                : player.getEffect(StarRailSimMod.COLORFUL_FLAME.get());
        if (mask != null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_game_of_cosmic_worlds_mask_active",
                    (int) Math.ceil(mask.getDuration() / 20.0D)), x, y, width,
                    StarRailUiStyle.GOLD_ACCENT);
        } else if (flame != null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_game_of_cosmic_worlds_flame_active",
                    flame.getAmplifier() + 1,
                    StarRailLightConeService.GAME_OF_COSMIC_WORLDS_FLAME_MAX_STACKS),
                    x, y, width, StarRailUiStyle.GOLD_ACCENT);
        } else {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_game_of_cosmic_worlds_inactive"),
                    x, y, width, StarRailUiStyle.MUTED_COLOR);
        }
    }

    private void renderReturningToEarthSpecialEffect(GuiGraphics graphics, Player player,
                                                      ItemStack stack, int x, int y,
                                                      int width) {
        int superimposition = ReturningToEarthItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_returning_to_earth_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_returning_to_earth_bonus",
                formatPercent(StarRailLightConeService.psalmAttackPerStack(superimposition)),
                formatPercent(StarRailLightConeService.psalmDamagePerStack(superimposition))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_returning_to_earth_trigger"), x, y, width);
        MobEffectInstance psalm = player == null ? null
                : player.getEffect(StarRailSimMod.PSALM.get());
        if (psalm == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_returning_to_earth_inactive"), x,
                    y + 4, width, StarRailUiStyle.MUTED_COLOR);
        } else {
            int stacks = Math.min(StarRailLightConeService.PSALM_MAX_STACKS,
                    psalm.getAmplifier() + 1);
            int seconds = (int) Math.ceil(psalm.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_returning_to_earth_active",
                    stacks, seconds), x, y + 4, width, StarRailUiStyle.GOLD_ACCENT);
        }
    }

    private void renderIfTimeWereAFlowerSpecialEffect(GuiGraphics graphics, Player player,
                                                       ItemStack stack, int x, int y,
                                                       int width) {
        int superimposition = IfTimeWereAFlowerItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_if_time_were_a_flower_special"), x, y,
                StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_if_time_were_a_flower_crit_damage",
                formatPercent(StarRailLightConeService
                        .ifTimeWereAFlowerCritDamage(superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_if_time_were_a_flower_trigger"), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_if_time_were_a_flower_aura",
                formatPercent(StarRailLightConeService
                        .ifTimeWereAFlowerCritRate(superimposition)),
                formatPercent(StarRailLightConeService
                        .ifTimeWereAFlowerCritDamageAura(superimposition))), x, y, width);
        MobEffectInstance edict = player == null ? null
                : player.getEffect(StarRailSimMod.EDICT.get());
        if (edict == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_if_time_were_a_flower_inactive"), x,
                    y + 4, width, StarRailUiStyle.MUTED_COLOR);
        } else {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_if_time_were_a_flower_active",
                    (int) Math.ceil(edict.getDuration() / 20.0D)), x, y + 4, width,
                    StarRailUiStyle.GOLD_ACCENT);
        }
    }

    private void renderAnAgeEtchedInGoldenBloodSpecialEffect(GuiGraphics graphics,
                                                              Player player,
                                                              ItemStack stack,
                                                              int x, int y, int width) {
        int superimposition = AnAgeEtchedInGoldenBloodItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_an_age_etched_in_golden_blood_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_an_age_etched_in_golden_blood_attack",
                formatPercent(StarRailLightConeService.goldenBloodAttack(superimposition))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_an_age_etched_in_golden_blood_law",
                formatPercent(StarRailLightConeService.goldenBloodLawAttack(superimposition)),
                formatPercent(StarRailLightConeService.goldenBloodLawDamage(superimposition))),
                x, y, width);
        MobEffectInstance law = player == null ? null
                : player.getEffect(StarRailSimMod.LAW.get());
        if (law == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_an_age_etched_in_golden_blood_inactive"),
                    x, y + 4, width, StarRailUiStyle.MUTED_COLOR);
        } else {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_an_age_etched_in_golden_blood_active",
                    (int) Math.ceil(law.getDuration() / 20.0D)), x, y + 4, width,
                    StarRailUiStyle.GOLD_ACCENT);
        }
    }

    private void renderInTheNameOfTheWorldSpecialEffect(GuiGraphics graphics, Player player,
                                                         ItemStack stack,
                                                         int x, int y, int width) {
        int superimposition = InTheNameOfTheWorldItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_in_the_name_of_the_world_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_in_the_name_of_the_world_damage",
                formatPercent(StarRailLightConeService
                        .inTheNameDamageVsDebuffed(superimposition))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_in_the_name_of_the_world_will",
                formatPercent(StarRailLightConeService.inTheNameWillEffectHit(
                        superimposition)),
                formatPercent(StarRailLightConeService.inTheNameWillAttack(
                        superimposition))), x, y, width);
        MobEffectInstance will = player == null ? null
                : player.getEffect(StarRailSimMod.WILL.get());
        if (will == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_in_the_name_of_the_world_inactive"),
                    x, y + 4, width, StarRailUiStyle.MUTED_COLOR);
        } else {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_in_the_name_of_the_world_active",
                    (int) Math.ceil(will.getDuration() / 20.0D)), x, y + 4, width,
                    StarRailUiStyle.GOLD_ACCENT);
        }
    }

    private void renderOnTheShoreInTheFlowOfTimeSpecialEffect(
            GuiGraphics graphics, Player player, ItemStack stack,
            int x, int y, int width) {
        int superimposition = OnTheShoreInTheFlowOfTimeItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_on_the_shore_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_on_the_shore_crit_damage",
                formatPercent(StarRailLightConeService.shoreCritDamage(superimposition))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_on_the_shore_trigger"), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_on_the_shore_bonuses",
                formatPercent(StarRailLightConeService.shoreAttackPerStack(superimposition)),
                formatPercent(StarRailLightConeService.shoreDamagePerStack(superimposition))),
                x, y, width);
        MobEffectInstance foamEcho = player == null ? null
                : player.getEffect(StarRailSimMod.FOAM_ECHO.get());
        if (foamEcho == null) {
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_on_the_shore_inactive"),
                    x, y + 4, width, StarRailUiStyle.MUTED_COLOR);
        } else {
            int stacks = Math.min(StarRailLightConeService.FOAM_ECHO_MAX_STACKS,
                    foamEcho.getAmplifier() + 1);
            int seconds = (int) Math.ceil(foamEcho.getDuration() / 20.0D);
            drawWrapped(graphics, Component.translatable(
                    "screen.starrail_sim.light_cone_on_the_shore_active", stacks, seconds),
                    x, y + 4, width, StarRailUiStyle.GOLD_ACCENT);
        }
    }

    private void renderAThousandFoldSpringSpecialEffect(
            GuiGraphics graphics, ItemStack stack, int x, int y, int width) {
        int level = AThousandFoldSpringItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_a_thousand_fold_spring_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_a_thousand_fold_spring_effect_hit",
                formatPercent(StarRailLightConeService.thousandSpringsEffectHit(level))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_a_thousand_fold_spring_apply",
                formatPercent(StarRailLightConeService.thousandSpringsStrippedDamage(level))),
                x, y, width);
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_a_thousand_fold_spring_upgrade",
                formatPercent(StarRailLightConeService
                        .thousandSpringsCorneredExtraDamage(level))), x, y, width);
    }

    private void renderOnlyWaitSpecialEffect(
            GuiGraphics graphics, ItemStack stack, int x, int y, int width) {
        int level = OnlyWaitItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_only_wait_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_only_wait_bonuses",
                formatPercent(StarRailLightConeService.onlyWaitAttack(level)),
                formatPercent(StarRailLightConeService.onlyWaitDamage(level))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_only_wait_trigger"), x, y, width);
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_only_wait_dot",
                formatPercent(StarRailLightConeService.onlyWaitDotAttack(level))),
                x, y, width);
    }

    private void renderRainNeverStopsSpecialEffect(
            GuiGraphics graphics, ItemStack stack, int x, int y, int width) {
        int level = RainNeverStopsItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_rain_never_stops_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_rain_never_stops_bonuses",
                formatPercent(StarRailLightConeService.rainNeverStopsEffectHit(level)),
                formatPercent(StarRailLightConeService.rainNeverStopsAttack(level))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_rain_never_stops_crit",
                formatPercent(StarRailLightConeService.rainNeverStopsCritRate(level))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_rain_never_stops_trigger"), x, y, width);
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_rain_never_stops_aether_code",
                formatPercent(StarRailLightConeService.rainNeverStopsVulnerability(level))),
                x, y, width);
    }

    private void renderLiesInTheWindSpecialEffect(
            GuiGraphics graphics, ItemStack stack, int x, int y, int width) {
        int level = LiesInTheWindItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_lies_in_the_wind_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_lies_in_the_wind_attack",
                formatPercent(StarRailLightConeService.liesInTheWindAttack(level))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_lies_in_the_wind_bewildered",
                formatPercent(StarRailLightConeService
                        .liesInTheWindBewilderedVulnerability(level))), x, y, width);
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_lies_in_the_wind_stolen",
                formatPercent(StarRailLightConeService
                .liesInTheWindStolenVulnerability(level))), x, y, width);
    }

    private void renderReturnToLongRoadSpecialEffect(
            GuiGraphics graphics, ItemStack stack, int x, int y, int width) {
        int level = ReturnToLongRoadItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_return_to_long_road_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_return_to_long_road_bonuses",
                formatPercent(StarRailLightConeService.returnToLongRoadAttack(level)),
                formatPercent(StarRailLightConeService.returnToLongRoadBreakEffect(level))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_return_to_long_road_trigger"), x, y, width);
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_return_to_long_road_scorching",
                formatPercent(StarRailLightConeService
                        .returnToLongRoadScorchingVulnerability(level))), x, y, width);
    }

    private void renderReforgedRemembranceSpecialEffect(
            GuiGraphics graphics, ItemStack stack, int x, int y, int width) {
        int level = ReforgedRemembranceItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_reforged_remembrance_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_reforged_remembrance_effect_hit",
                formatPercent(StarRailLightConeService.reforgedRemembranceEffectHit(level))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_reforged_remembrance_trigger"), x, y, width);
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_reforged_remembrance_per_stack",
                formatPercent(StarRailLightConeService
                        .reforgedRemembranceAttackPerStack(level)),
                formatPercent(StarRailLightConeService
                        .reforgedRemembranceDamagePerStack(level))), x, y, width);
    }

    private void renderOceanWhySingsSpecialEffect(
            GuiGraphics graphics, ItemStack stack, int x, int y, int width) {
        int level = OceanWhySingsItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_ocean_why_sings_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_ocean_why_sings_bonuses",
                formatPercent(StarRailLightConeService.oceanWhySingsEffectHit(level)),
                formatPercent(StarRailLightConeService.oceanWhySingsAttack(level))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_ocean_why_sings_trigger"), x, y, width);
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_ocean_why_sings_dot",
                formatPercent(StarRailLightConeService.oceanWhySingsDotAttack(level))),
                x, y, width);
    }

    private void renderDoNotForgetHerFlameSpecialEffect(
            GuiGraphics graphics, ItemStack stack, int x, int y, int width) {
        int level = DoNotForgetHerFlameItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_do_not_forget_her_flame_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_do_not_forget_her_flame_values",
                formatPercent(StarRailLightConeService.doNotForgetHerFlameBreakEffect(level)),
                formatPercent(StarRailLightConeService.doNotForgetHerFlameDamage(level)),
                formatPercent(StarRailLightConeService
                        .doNotForgetHerFlameAttackWhileBurning(level)),
                formatPercent(StarRailLightConeService
                        .doNotForgetHerFlameCritDamageWhileBurning(level))), x, y, width);
    }

    private void renderNewFleshOfInfernoSpecialEffect(
            GuiGraphics graphics, ItemStack stack, int x, int y, int width) {
        int level = NewFleshOfInfernoItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_new_flesh_of_inferno_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_new_flesh_of_inferno_values",
                formatPercent(StarRailLightConeService.newFleshOfInfernoMaxHealth(level)),
                formatPercent(StarRailLightConeService
                        .newFleshOfInfernoCritDamageTaken(level)),
                formatPercent(StarRailLightConeService
                        .newFleshOfInfernoCritDamageTaken(level))), x, y, width);
    }

    private void renderMomentOfVictorySpecialEffect(
            GuiGraphics graphics, ItemStack stack, int x, int y, int width) {
        int level = MomentOfVictoryItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_moment_of_victory_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_moment_of_victory_values",
                formatPercent(StarRailLightConeService.momentOfVictoryArmor(level)),
                formatPercent(StarRailLightConeService.winterShieldBonus(level))),
                x, y, width);
    }

    private void renderSheHasClosedHerEyesSpecialEffect(
            GuiGraphics graphics, ItemStack stack, int x, int y, int width) {
        int level = SheHasClosedHerEyesItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_she_has_closed_her_eyes_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_she_has_closed_her_eyes_values",
                formatPercent(StarRailLightConeService.closedEyesArmor(level)),
                formatPercent(StarRailLightConeService.closedEyesArmor(level)),
                formatPercent(StarRailLightConeService.closedEyesDamage(level))), x, y, width);
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_she_has_closed_her_eyes_heal",
                formatPercent(StarRailLightConeService.closedEyesMissingHealthHeal(level))),
                x, y, width);
    }

    private void renderThoughRiversAndMountainsSpecialEffect(
            GuiGraphics graphics, ItemStack stack, int x, int y, int width) {
        int level = ThoughRiversAndMountainsItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_though_rivers_and_mountains_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_though_rivers_and_mountains_values",
                formatPercent(StarRailLightConeService.thoughRiversArmor(level)),
                formatPercent(StarRailLightConeService.thoughRiversHealFromArmor(level))),
                x, y, width);
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_though_rivers_and_mountains_garrison",
                formatPercent(StarRailLightConeService.thoughRiversGarrisonDamage(level))),
                x, y, width);
    }

    private void renderFateNeverFairSpecialEffect(
            GuiGraphics graphics, ItemStack stack, int x, int y, int width) {
        int level = FateNeverFairItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_fate_never_fair_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_fate_never_fair_armor",
                formatPercent(StarRailLightConeService.fateNeverFairArmor(level))), x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_fate_never_fair_crit_damage",
                formatPercent(StarRailLightConeService.fateNeverFairCritDamage(level))),
                x, y, width);
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_fate_never_fair_chips",
                formatPercent(StarRailLightConeService.fateNeverFairChipsDamage(level))),
                x, y, width);
    }

    private void renderEchoesOfTheCoffinSpecialEffect(
            GuiGraphics graphics, ItemStack stack, int x, int y, int width) {
        int level = EchoesOfTheCoffinItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_echoes_of_the_coffin_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_echoes_of_the_coffin_attack",
                formatPercent(StarRailLightConeService.echoesOfTheCoffinAttack(level))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_echoes_of_the_coffin_health",
                formatPercent(StarRailLightConeService.echoesOfTheCoffinHealth(level))),
                x, y, width);
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_echoes_of_the_coffin_thorn",
                formatPercent(StarRailLightConeService.echoesOfTheCoffinBonusDamage(level))),
                x, y, width);
    }

    private void renderTimeWaitsForNoOneSpecialEffect(
            GuiGraphics graphics, ItemStack stack, int x, int y, int width) {
        int level = TimeWaitsForNoOneItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_time_waits_for_no_one_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_time_waits_for_no_one_health",
                formatPercent(StarRailLightConeService.timeWaitsForNoOneMaxHealth(level))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_time_waits_for_no_one_healing",
                formatPercent(StarRailLightConeService.timeWaitsForNoOneHealing(level))),
                x, y, width);
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_time_waits_for_no_one_bonus",
                formatPercent(StarRailLightConeService.timeWaitsForNoOneDamage(level))),
                x, y, width);
    }

    private void renderNightOfFrightSpecialEffect(
            GuiGraphics graphics, ItemStack stack, int x, int y, int width) {
        int level = NightOfFrightItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_night_of_fright_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_night_of_fright_health",
                formatPercent(StarRailLightConeService.nightOfFrightMaxHealth(level))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_night_of_fright_heal",
                formatPercent(StarRailLightConeService.nightOfFrightHealing(level))),
                x, y, width);
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_night_of_fright_buff",
                formatPercent(StarRailLightConeService.deepBreathAttackPerStack(level))),
                x, y, width);
    }

    private void renderOnlyTheScentRemainsSpecialEffect(
            GuiGraphics graphics, ItemStack stack, int x, int y, int width) {
        int level = OnlyTheScentRemainsItem.getSuperimposition(stack);
        graphics.drawString(font, Component.translatable(
                "screen.starrail_sim.light_cone_only_the_scent_remains_special"),
                x, y, StarRailUiStyle.CYAN_ACCENT);
        y += 22;
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_only_the_scent_remains_break_effect",
                formatPercent(StarRailLightConeService.onlyTheScentRemainsBreakEffect(level))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_only_the_scent_remains_health",
                formatPercent(StarRailLightConeService.onlyTheScentRemainsMaxHealth(level))),
                x, y, width);
        y = drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_only_the_scent_remains_vulnerability",
                formatPercent(StarRailLightConeService.onlyTheScentRemainsVulnerability(level))),
                x, y, width);
        drawBullet(graphics, Component.translatable(
                "screen.starrail_sim.light_cone_only_the_scent_remains_extra",
                formatPercent(StarRailLightConeService.onlyTheScentRemainsExtraVulnerability(
                        level))), x, y, width);
    }

    private static boolean isNihilityLightCone(ItemStack stack) {
        return stack.getItem() instanceof InTheNameOfTheWorldItem
                || stack.getItem() instanceof OnTheShoreInTheFlowOfTimeItem
                || stack.getItem() instanceof AThousandFoldSpringItem
                || stack.getItem() instanceof OnlyWaitItem
                || stack.getItem() instanceof RainNeverStopsItem
                || stack.getItem() instanceof LiesInTheWindItem
                || stack.getItem() instanceof ReturnToLongRoadItem
                || stack.getItem() instanceof ReforgedRemembranceItem
                || stack.getItem() instanceof OceanWhySingsItem
                || stack.getItem() instanceof DoNotForgetHerFlameItem
                || stack.getItem() instanceof NewFleshOfInfernoItem;
    }

    private static boolean isPreservationLightCone(ItemStack stack) {
        return stack.getItem() instanceof MomentOfVictoryItem
                || stack.getItem() instanceof SheHasClosedHerEyesItem
                || stack.getItem() instanceof ThoughRiversAndMountainsItem
                || stack.getItem() instanceof FateNeverFairItem;
    }

    private static boolean isAbundanceLightCone(ItemStack stack) {
        return stack.getItem() instanceof EchoesOfTheCoffinItem
                || stack.getItem() instanceof TimeWaitsForNoOneItem
                || stack.getItem() instanceof NightOfFrightItem
                || stack.getItem() instanceof OnlyTheScentRemainsItem;
    }

    private static boolean isDestructionLightCone(ItemStack stack) {
        return stack.getItem() instanceof SomethingIrreplaceableItem
                || stack.getItem() instanceof BrighterThanTheSunItem
                || stack.getItem() instanceof DanceAtSunsetItem
                || stack.getItem() instanceof TheUnreachableSideItem
                || stack.getItem() instanceof ThisBodyAsSwordItem
                || stack.getItem() instanceof NoRewardCrowningItem
                || stack.getItem() instanceof BloodFireBurningPathItem
                || stack.getItem() instanceof WhereDreamsBelongItem
                || stack.getItem() instanceof DawnBurnsJustSoItem
                || stack.getItem() instanceof WhatYouSeeIsMeItem;
    }

    private static boolean isEruditionLightCone(ItemStack stack) {
        return stack.getItem() instanceof GalaxyRailwayItem
                || stack.getItem() instanceof MomentOfGloryItem
                || stack.getItem() instanceof BeforeDawnItem
                || stack.getItem() instanceof PriceOfPeaceItem
                || stack.getItem() instanceof TowardsUnanswerableItem
                || stack.getItem() instanceof NinjaScrollItem
                || stack.getItem() instanceof LifeAsALightItem
                || stack.getItem() instanceof AStarIlluminatesNightSkyItem
                || stack.getItem() instanceof SparkleQuietlyShinesItem;
    }

    private static int getSuperimposition(ItemStack stack) {
        if (stack.getItem() instanceof IWillHuntItem) {
            return IWillHuntItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof WorrisomeBlissfulItem) {
            return WorrisomeBlissfulItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof SleepLikeTheDeadItem) {
            return SleepLikeTheDeadItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof PureThoughtBaptismItem) {
            return PureThoughtBaptismItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof IdealBurningHellItem) {
            return IdealBurningHellItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof EmbarkOnSecondLifeItem) {
            return EmbarkOnSecondLifeItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof FinaleOfALieItem) {
            return FinaleOfALieItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof SomethingIrreplaceableItem) {
            return SomethingIrreplaceableItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof BrighterThanTheSunItem) {
            return BrighterThanTheSunItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof DanceAtSunsetItem) {
            return DanceAtSunsetItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof TheUnreachableSideItem) {
            return TheUnreachableSideItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof ThisBodyAsSwordItem) {
            return ThisBodyAsSwordItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof NoRewardCrowningItem) {
            return NoRewardCrowningItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof BloodFireBurningPathItem) {
            return BloodFireBurningPathItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof WhereDreamsBelongItem) {
            return WhereDreamsBelongItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof DawnBurnsJustSoItem) {
            return DawnBurnsJustSoItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof WhatYouSeeIsMeItem) {
            return WhatYouSeeIsMeItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof GalaxyRailwayItem) {
            return GalaxyRailwayItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof MomentOfGloryItem) {
            return MomentOfGloryItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof MomentOfVictoryItem) {
            return MomentOfVictoryItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof SheHasClosedHerEyesItem) {
            return SheHasClosedHerEyesItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof ThoughRiversAndMountainsItem) {
            return ThoughRiversAndMountainsItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof FateNeverFairItem) {
            return FateNeverFairItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof EchoesOfTheCoffinItem) {
            return EchoesOfTheCoffinItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof TimeWaitsForNoOneItem) {
            return TimeWaitsForNoOneItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof NightOfFrightItem) {
            return NightOfFrightItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof OnlyTheScentRemainsItem) {
            return OnlyTheScentRemainsItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof BeforeDawnItem) {
            return BeforeDawnItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof PriceOfPeaceItem) {
            return PriceOfPeaceItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof TowardsUnanswerableItem) {
            return TowardsUnanswerableItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof NinjaScrollItem) {
            return NinjaScrollItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof LifeAsALightItem) {
            return LifeAsALightItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof AStarIlluminatesNightSkyItem) {
            return AStarIlluminatesNightSkyItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof SparkleQuietlyShinesItem) {
            return SparkleQuietlyShinesItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof BattleIsntOverItem) {
            return BattleIsntOverItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof MemoryOfMeItem) {
            return MemoryOfMeItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof WeaveTimeIntoGoldItem) {
            return WeaveTimeIntoGoldItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof MakeFarewellMoreBeautifulItem) {
            return MakeFarewellMoreBeautifulItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof LoveIsEternalItem) {
            return LoveIsEternalItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof StarlightForLongNightsItem) {
            return StarlightForLongNightsItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof WelcomeToGalacticCityItem) {
            return WelcomeToGalacticCityItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof MeetInTheNextFlowerSeasonItem) {
            return MeetInTheNextFlowerSeasonItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof FlowerWorldMesmerizingEyesItem) {
            return FlowerWorldMesmerizingEyesItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof WhenSheDecidesToSeeItem) {
            return WhenSheDecidesToSeeItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof MayRainbowStayInTheSkyItem) {
            return MayRainbowStayInTheSkyItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof NightFlowingColorsItem) {
            return NightFlowingColorsItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof GameOfCosmicWorldsItem) {
            return GameOfCosmicWorldsItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof ReturningToEarthItem) {
            return ReturningToEarthItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof IfTimeWereAFlowerItem) {
            return IfTimeWereAFlowerItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof AnAgeEtchedInGoldenBloodItem) {
            return AnAgeEtchedInGoldenBloodItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof InTheNameOfTheWorldItem) {
            return InTheNameOfTheWorldItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof OnTheShoreInTheFlowOfTimeItem) {
            return OnTheShoreInTheFlowOfTimeItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof AThousandFoldSpringItem) {
            return AThousandFoldSpringItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof OnlyWaitItem) {
            return OnlyWaitItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof RainNeverStopsItem) {
            return RainNeverStopsItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof LiesInTheWindItem) {
            return LiesInTheWindItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof ReturnToLongRoadItem) {
            return ReturnToLongRoadItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof ReforgedRemembranceItem) {
            return ReforgedRemembranceItem.getSuperimposition(stack);
        }
        return InTheNightItem.getSuperimposition(stack);
    }

    private int drawBullet(GuiGraphics graphics, String key, int x, int y, int width) {
        return drawBullet(graphics, Component.translatable(key), x, y, width);
    }

    private int drawBullet(GuiGraphics graphics, Component text, int x, int y, int width) {
        graphics.drawString(font, Component.literal("•"), x, y, StarRailUiStyle.GOLD_ACCENT);
        return drawWrapped(graphics, text, x + 10, y, width - 10,
                StarRailUiStyle.VALUE_COLOR) + 6;
    }

    private int drawDetailLine(GuiGraphics graphics, String key, Component value, int x,
                               int y, int width) {
        graphics.fill(x, y - 3, x + width - 4, y + 13, 0x1A35415E);
        Component label = Component.translatable(key);
        graphics.drawString(font, label, x + 4, y, StarRailUiStyle.MUTED_COLOR);
        graphics.drawString(font, value, x + width - 8 - font.width(value), y,
                StarRailUiStyle.VALUE_COLOR);
        return y;
    }

    private int drawWrapped(GuiGraphics graphics, Component text, int x, int y, int width,
                            int color) {
        int lineY = y;
        for (FormattedCharSequence line : font.split(Component.literal(StarRailUiStyle.readableText(text)), width)) {
            graphics.drawString(font, line, x, lineY, color);
            lineY += font.lineHeight + 3;
        }
        detailsBottom = Math.max(detailsBottom, lineY);
        return lineY;
    }

    private static String formatPercent(double value) {
        double percent = value * 100.0D;
        if (Math.abs(percent - Math.rint(percent)) < 0.001D) {
            return String.format(Locale.ROOT, "%.0f%%", percent);
        }
        return String.format(Locale.ROOT, "%.1f%%", percent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
