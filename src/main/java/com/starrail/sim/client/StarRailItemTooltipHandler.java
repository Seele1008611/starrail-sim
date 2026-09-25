package com.starrail.sim.client;

import com.starrail.sim.InTheNightItem;
import com.starrail.sim.IWillHuntItem;
import com.starrail.sim.StarRailLightConeService;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Locale;

/** Switches the light cone's inventory tooltip between lore and mechanics pages. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarRailItemTooltipHandler {
    private static final ChatFormatting IN_THE_NIGHT_THEME = ChatFormatting.BLUE;
    private static final ChatFormatting I_WILL_HUNT_THEME = ChatFormatting.GOLD;
    private static final ChatFormatting WORRISOME_BLISSFUL_THEME = ChatFormatting.RED;
    private static final ChatFormatting SLEEP_LIKE_THE_DEAD_THEME = ChatFormatting.YELLOW;
    private static final ChatFormatting PURE_THOUGHT_BAPTISM_THEME = ChatFormatting.LIGHT_PURPLE;
    private static final ChatFormatting SOMETHING_IRREPLACEABLE_THEME = ChatFormatting.RED;
    private static final ChatFormatting BRIGHTER_THAN_THE_SUN_THEME = ChatFormatting.GOLD;
    private static final ChatFormatting DANCE_AT_SUNSET_THEME = ChatFormatting.GOLD;
    private static final ChatFormatting THE_UNREACHABLE_SIDE_THEME = ChatFormatting.DARK_AQUA;
    private static final ChatFormatting THIS_BODY_AS_SWORD_THEME = ChatFormatting.BLUE;
    private static final ChatFormatting NO_REWARD_CROWNING_THEME = ChatFormatting.GOLD;
    private static final ChatFormatting BLOOD_FIRE_BURNING_PATH_THEME = ChatFormatting.DARK_RED;
    private static final ChatFormatting WHERE_DREAMS_BELONG_THEME = ChatFormatting.AQUA;
    private static final ChatFormatting DAWN_BURNS_JUST_SO_THEME = ChatFormatting.GOLD;
    private static final ChatFormatting WHAT_YOU_SEE_IS_ME_THEME = ChatFormatting.RED;
    private static final ChatFormatting GALAXY_RAILWAY_THEME = ChatFormatting.DARK_AQUA;
    private static final ChatFormatting MOMENT_OF_GLORY_THEME = ChatFormatting.GOLD;
    private static final ChatFormatting BEFORE_DAWN_THEME = ChatFormatting.DARK_AQUA;
    private static final ChatFormatting PRICE_OF_PEACE_THEME = ChatFormatting.LIGHT_PURPLE;
    private static final ChatFormatting TOWARDS_UNANSWERABLE_THEME = ChatFormatting.DARK_AQUA;
    private static final ChatFormatting NINJA_SCROLL_THEME = ChatFormatting.DARK_PURPLE;
    private static final ChatFormatting LIFE_AS_A_LIGHT_THEME = ChatFormatting.DARK_RED;
    private static final ChatFormatting A_STAR_ILLUMINATES_NIGHT_SKY_THEME =
            ChatFormatting.AQUA;
    private static final ChatFormatting SPARKLE_QUIETLY_SHINES_THEME = ChatFormatting.GOLD;
    private static final ChatFormatting BATTLE_ISNT_OVER_THEME = ChatFormatting.AQUA;
    private static final ChatFormatting MEMORY_OF_ME_THEME = ChatFormatting.GOLD;
    private static final ChatFormatting WEAVE_TIME_INTO_GOLD_THEME = ChatFormatting.GOLD;
    private static final ChatFormatting MAKE_FAREWELL_MORE_BEAUTIFUL_THEME =
            ChatFormatting.DARK_RED;
    private static final ChatFormatting LOVE_IS_ETERNAL_THEME = ChatFormatting.AQUA;
    private static final ChatFormatting STARLIGHT_FOR_LONG_NIGHTS_THEME =
            ChatFormatting.LIGHT_PURPLE;
    private static final ChatFormatting WELCOME_TO_GALACTIC_CITY_THEME =
            ChatFormatting.LIGHT_PURPLE;
    private static final ChatFormatting MEET_IN_THE_NEXT_FLOWER_SEASON_THEME =
            ChatFormatting.LIGHT_PURPLE;
    private static final ChatFormatting WHEN_SHE_DECIDES_TO_SEE_THEME =
            ChatFormatting.AQUA;
    private static final ChatFormatting FLOWER_WORLD_MESMERIZING_EYES_THEME =
            ChatFormatting.LIGHT_PURPLE;
    private static final ChatFormatting MAY_RAINBOW_STAY_IN_THE_SKY_THEME =
            ChatFormatting.LIGHT_PURPLE;
    private static final ChatFormatting NIGHT_FLOWING_COLORS_THEME =
            ChatFormatting.LIGHT_PURPLE;
    private static final ChatFormatting GAME_OF_COSMIC_WORLDS_THEME = ChatFormatting.GOLD;
    private static final ChatFormatting RETURNING_TO_EARTH_THEME = ChatFormatting.AQUA;
    private static final ChatFormatting IF_TIME_WERE_A_FLOWER_THEME = ChatFormatting.GOLD;
    private static final ChatFormatting AN_AGE_ETCHED_IN_GOLDEN_BLOOD_THEME =
            ChatFormatting.GOLD;
    private static final ChatFormatting IN_THE_NAME_OF_THE_WORLD_THEME =
            ChatFormatting.LIGHT_PURPLE;
    private static final ChatFormatting ON_THE_SHORE_THEME = ChatFormatting.DARK_AQUA;
    private static final ChatFormatting A_THOUSAND_FOLD_SPRING_THEME = ChatFormatting.GREEN;
    private static final ChatFormatting ONLY_WAIT_THEME = ChatFormatting.LIGHT_PURPLE;
    private static final ChatFormatting RAIN_NEVER_STOPS_THEME = ChatFormatting.BLUE;
    private static final ChatFormatting LIES_IN_THE_WIND_THEME = ChatFormatting.BLUE;
    private static final ChatFormatting REFORGED_REMEMBRANCE_THEME =
            ChatFormatting.LIGHT_PURPLE;
    private static final ChatFormatting OCEAN_WHY_SINGS_THEME = ChatFormatting.BLUE;
    private static final ChatFormatting MOMENT_OF_VICTORY_THEME = ChatFormatting.AQUA;
    private static final ChatFormatting THOUGH_RIVERS_AND_MOUNTAINS_THEME = ChatFormatting.GOLD;
    private static final ChatFormatting FATE_NEVER_FAIR_THEME = ChatFormatting.GOLD;
    private static final ChatFormatting ECHOES_OF_THE_COFFIN_THEME = ChatFormatting.AQUA;
    private static final ChatFormatting TIME_WAITS_FOR_NO_ONE_THEME =
            ChatFormatting.LIGHT_PURPLE;

    private StarRailItemTooltipHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof InTheNightItem)
                && !(stack.getItem() instanceof IWillHuntItem)
                && !(stack.getItem() instanceof WorrisomeBlissfulItem)
                && !(stack.getItem() instanceof SleepLikeTheDeadItem)
                && !(stack.getItem() instanceof PureThoughtBaptismItem)
                && !(stack.getItem() instanceof IdealBurningHellItem)
                && !(stack.getItem() instanceof EmbarkOnSecondLifeItem)
                && !(stack.getItem() instanceof FinaleOfALieItem)
                && !(stack.getItem() instanceof SomethingIrreplaceableItem)
                && !(stack.getItem() instanceof BrighterThanTheSunItem)
                && !(stack.getItem() instanceof DanceAtSunsetItem)
                && !(stack.getItem() instanceof TheUnreachableSideItem)
                && !(stack.getItem() instanceof ThisBodyAsSwordItem)
                && !(stack.getItem() instanceof NoRewardCrowningItem)
                && !(stack.getItem() instanceof BloodFireBurningPathItem)
                && !(stack.getItem() instanceof WhereDreamsBelongItem)
                && !(stack.getItem() instanceof DawnBurnsJustSoItem)
                && !(stack.getItem() instanceof WhatYouSeeIsMeItem)
                && !(stack.getItem() instanceof GalaxyRailwayItem)
                && !(stack.getItem() instanceof MomentOfGloryItem)
                && !(stack.getItem() instanceof BeforeDawnItem)
                && !(stack.getItem() instanceof PriceOfPeaceItem)
                && !(stack.getItem() instanceof TowardsUnanswerableItem)
                && !(stack.getItem() instanceof NinjaScrollItem)
                && !(stack.getItem() instanceof LifeAsALightItem)
                && !(stack.getItem() instanceof AStarIlluminatesNightSkyItem)
                && !(stack.getItem() instanceof SparkleQuietlyShinesItem)
                && !(stack.getItem() instanceof BattleIsntOverItem)
                && !(stack.getItem() instanceof MemoryOfMeItem)
                && !(stack.getItem() instanceof NightFlowingColorsItem)
                && !(stack.getItem() instanceof GameOfCosmicWorldsItem)
                && !(stack.getItem() instanceof ReturningToEarthItem)
                && !(stack.getItem() instanceof IfTimeWereAFlowerItem)
                && !(stack.getItem() instanceof AnAgeEtchedInGoldenBloodItem)
                && !(stack.getItem() instanceof InTheNameOfTheWorldItem)
                && !(stack.getItem() instanceof OnTheShoreInTheFlowOfTimeItem)
                && !(stack.getItem() instanceof AThousandFoldSpringItem)
                && !(stack.getItem() instanceof OnlyWaitItem)
                && !(stack.getItem() instanceof RainNeverStopsItem)
                && !(stack.getItem() instanceof LiesInTheWindItem)
                && !(stack.getItem() instanceof ReturnToLongRoadItem)
                && !(stack.getItem() instanceof ReforgedRemembranceItem)
                && !(stack.getItem() instanceof OceanWhySingsItem)
                && !(stack.getItem() instanceof DoNotForgetHerFlameItem)
                && !(stack.getItem() instanceof NewFleshOfInfernoItem)
                && !(stack.getItem() instanceof MomentOfVictoryItem)
                && !(stack.getItem() instanceof SheHasClosedHerEyesItem)
                && !(stack.getItem() instanceof ThoughRiversAndMountainsItem)
                && !(stack.getItem() instanceof FateNeverFairItem)
                && !(stack.getItem() instanceof EchoesOfTheCoffinItem)
                && !(stack.getItem() instanceof TimeWaitsForNoOneItem)
                && !(stack.getItem() instanceof NightOfFrightItem)
                && !(stack.getItem() instanceof OnlyTheScentRemainsItem)
                && !(stack.getItem() instanceof WeaveTimeIntoGoldItem)
                && !(stack.getItem() instanceof MakeFarewellMoreBeautifulItem)
                && !(stack.getItem() instanceof LoveIsEternalItem)
                && !(stack.getItem() instanceof StarlightForLongNightsItem)
                && !(stack.getItem() instanceof WelcomeToGalacticCityItem)
                && !(stack.getItem() instanceof MeetInTheNextFlowerSeasonItem)
                && !(stack.getItem() instanceof WhenSheDecidesToSeeItem)
                && !(stack.getItem() instanceof FlowerWorldMesmerizingEyesItem)
                && !(stack.getItem() instanceof MayRainbowStayInTheSkyItem)) {
            return;
        }

        List<Component> tooltip = event.getToolTip();
        Component itemName = tooltip.isEmpty() ? stack.getHoverName() : tooltip.get(0);
        tooltip.clear();
        tooltip.add(itemName);
        ChatFormatting theme = stack.getItem() instanceof IWillHuntItem
                ? I_WILL_HUNT_THEME
                : stack.getItem() instanceof WorrisomeBlissfulItem
                        ? WORRISOME_BLISSFUL_THEME
                        : stack.getItem() instanceof SleepLikeTheDeadItem
                        ? SLEEP_LIKE_THE_DEAD_THEME : IN_THE_NIGHT_THEME;
        if (stack.getItem() instanceof PureThoughtBaptismItem) {
            theme = PURE_THOUGHT_BAPTISM_THEME;
        } else if (stack.getItem() instanceof IdealBurningHellItem) {
            theme = ChatFormatting.GOLD;
        } else if (stack.getItem() instanceof EmbarkOnSecondLifeItem) {
            theme = ChatFormatting.BLUE;
        } else if (stack.getItem() instanceof FinaleOfALieItem) {
            theme = ChatFormatting.RED;
        } else if (stack.getItem() instanceof SomethingIrreplaceableItem) {
            theme = SOMETHING_IRREPLACEABLE_THEME;
        } else if (stack.getItem() instanceof BrighterThanTheSunItem) {
            theme = BRIGHTER_THAN_THE_SUN_THEME;
        } else if (stack.getItem() instanceof DanceAtSunsetItem) {
            theme = DANCE_AT_SUNSET_THEME;
        } else if (stack.getItem() instanceof TheUnreachableSideItem) {
            theme = THE_UNREACHABLE_SIDE_THEME;
        } else if (stack.getItem() instanceof ThisBodyAsSwordItem) {
            theme = THIS_BODY_AS_SWORD_THEME;
        } else if (stack.getItem() instanceof NoRewardCrowningItem) {
            theme = NO_REWARD_CROWNING_THEME;
        } else if (stack.getItem() instanceof BloodFireBurningPathItem) {
            theme = BLOOD_FIRE_BURNING_PATH_THEME;
        } else if (stack.getItem() instanceof WhereDreamsBelongItem) {
            theme = WHERE_DREAMS_BELONG_THEME;
        } else if (stack.getItem() instanceof DawnBurnsJustSoItem) {
            theme = DAWN_BURNS_JUST_SO_THEME;
        } else if (stack.getItem() instanceof WhatYouSeeIsMeItem) {
            theme = WHAT_YOU_SEE_IS_ME_THEME;
        } else if (stack.getItem() instanceof GalaxyRailwayItem) {
            theme = GALAXY_RAILWAY_THEME;
        } else if (stack.getItem() instanceof MomentOfGloryItem) {
            theme = MOMENT_OF_GLORY_THEME;
        } else if (stack.getItem() instanceof BeforeDawnItem) {
            theme = BEFORE_DAWN_THEME;
        } else if (stack.getItem() instanceof PriceOfPeaceItem) {
            theme = PRICE_OF_PEACE_THEME;
        } else if (stack.getItem() instanceof TowardsUnanswerableItem) {
            theme = TOWARDS_UNANSWERABLE_THEME;
        } else if (stack.getItem() instanceof NinjaScrollItem) {
            theme = NINJA_SCROLL_THEME;
        } else if (stack.getItem() instanceof LifeAsALightItem) {
            theme = LIFE_AS_A_LIGHT_THEME;
        } else if (stack.getItem() instanceof AStarIlluminatesNightSkyItem) {
            theme = A_STAR_ILLUMINATES_NIGHT_SKY_THEME;
        } else if (stack.getItem() instanceof SparkleQuietlyShinesItem) {
            theme = SPARKLE_QUIETLY_SHINES_THEME;
        } else if (stack.getItem() instanceof BattleIsntOverItem) {
            theme = BATTLE_ISNT_OVER_THEME;
        } else if (stack.getItem() instanceof MemoryOfMeItem) {
            theme = MEMORY_OF_ME_THEME;
        } else if (stack.getItem() instanceof WeaveTimeIntoGoldItem) {
            theme = WEAVE_TIME_INTO_GOLD_THEME;
        } else if (stack.getItem() instanceof MakeFarewellMoreBeautifulItem) {
            theme = MAKE_FAREWELL_MORE_BEAUTIFUL_THEME;
        } else if (stack.getItem() instanceof LoveIsEternalItem) {
            theme = LOVE_IS_ETERNAL_THEME;
        } else if (stack.getItem() instanceof StarlightForLongNightsItem) {
            theme = STARLIGHT_FOR_LONG_NIGHTS_THEME;
        } else if (stack.getItem() instanceof WelcomeToGalacticCityItem) {
            theme = WELCOME_TO_GALACTIC_CITY_THEME;
        } else if (stack.getItem() instanceof MeetInTheNextFlowerSeasonItem) {
            theme = MEET_IN_THE_NEXT_FLOWER_SEASON_THEME;
        } else if (stack.getItem() instanceof WhenSheDecidesToSeeItem) {
            theme = WHEN_SHE_DECIDES_TO_SEE_THEME;
        } else if (stack.getItem() instanceof FlowerWorldMesmerizingEyesItem) {
            theme = FLOWER_WORLD_MESMERIZING_EYES_THEME;
        } else if (stack.getItem() instanceof MayRainbowStayInTheSkyItem) {
            theme = MAY_RAINBOW_STAY_IN_THE_SKY_THEME;
        } else if (stack.getItem() instanceof NightFlowingColorsItem) {
            theme = NIGHT_FLOWING_COLORS_THEME;
        } else if (stack.getItem() instanceof GameOfCosmicWorldsItem) {
            theme = GAME_OF_COSMIC_WORLDS_THEME;
        } else if (stack.getItem() instanceof ReturningToEarthItem) {
            theme = RETURNING_TO_EARTH_THEME;
        } else if (stack.getItem() instanceof IfTimeWereAFlowerItem) {
            theme = IF_TIME_WERE_A_FLOWER_THEME;
        } else if (stack.getItem() instanceof AnAgeEtchedInGoldenBloodItem) {
            theme = AN_AGE_ETCHED_IN_GOLDEN_BLOOD_THEME;
        } else if (stack.getItem() instanceof InTheNameOfTheWorldItem) {
            theme = IN_THE_NAME_OF_THE_WORLD_THEME;
        } else if (stack.getItem() instanceof OnTheShoreInTheFlowOfTimeItem) {
            theme = ON_THE_SHORE_THEME;
        } else if (stack.getItem() instanceof AThousandFoldSpringItem) {
            theme = A_THOUSAND_FOLD_SPRING_THEME;
        } else if (stack.getItem() instanceof OnlyWaitItem) {
            theme = ONLY_WAIT_THEME;
        } else if (stack.getItem() instanceof RainNeverStopsItem) {
            theme = RAIN_NEVER_STOPS_THEME;
        } else if (stack.getItem() instanceof LiesInTheWindItem) {
            theme = LIES_IN_THE_WIND_THEME;
        } else if (stack.getItem() instanceof ReturnToLongRoadItem) {
            theme = ChatFormatting.GOLD;
        } else if (stack.getItem() instanceof ReforgedRemembranceItem) {
            theme = REFORGED_REMEMBRANCE_THEME;
        } else if (stack.getItem() instanceof OceanWhySingsItem) {
            theme = OCEAN_WHY_SINGS_THEME;
        } else if (stack.getItem() instanceof DoNotForgetHerFlameItem) {
            theme = ChatFormatting.LIGHT_PURPLE;
        } else if (stack.getItem() instanceof NewFleshOfInfernoItem) {
            theme = ChatFormatting.GOLD;
        } else if (stack.getItem() instanceof MomentOfVictoryItem) {
            theme = MOMENT_OF_VICTORY_THEME;
        } else if (stack.getItem() instanceof SheHasClosedHerEyesItem) {
            theme = ChatFormatting.LIGHT_PURPLE;
        } else if (stack.getItem() instanceof ThoughRiversAndMountainsItem) {
            theme = THOUGH_RIVERS_AND_MOUNTAINS_THEME;
        } else if (stack.getItem() instanceof FateNeverFairItem) {
            theme = FATE_NEVER_FAIR_THEME;
        } else if (stack.getItem() instanceof EchoesOfTheCoffinItem) {
            theme = ECHOES_OF_THE_COFFIN_THEME;
        } else if (stack.getItem() instanceof TimeWaitsForNoOneItem) {
            theme = TIME_WAITS_FOR_NO_ONE_THEME;
        } else if (stack.getItem() instanceof NightOfFrightItem) {
            theme = ChatFormatting.LIGHT_PURPLE;
        } else if (stack.getItem() instanceof OnlyTheScentRemainsItem) {
            theme = ChatFormatting.GOLD;
        }
        if (Screen.hasShiftDown()) {
            addMechanicsTooltip(tooltip, stack, theme);
        } else {
            addStoryTooltip(tooltip, stack, theme);
        }
        addFixedBaseAttributes(tooltip, theme);
    }

    private static void addStoryTooltip(List<Component> tooltip, ItemStack stack,
                                        ChatFormatting theme) {
        String prefix;
        int lineCount;
        int firstLineIndex = 1;
        if (stack.getItem() instanceof IWillHuntItem) {
            prefix = "tooltip.starrail_sim.i_will_hunt.story_";
            lineCount = 10;
        } else if (stack.getItem() instanceof WorrisomeBlissfulItem) {
            prefix = "tooltip.starrail_sim.worrisome_blissful.story_";
            lineCount = 7;
        } else if (stack.getItem() instanceof SleepLikeTheDeadItem) {
            prefix = "tooltip.starrail_sim.sleep_like_the_dead.story_";
            lineCount = 6;
        } else if (stack.getItem() instanceof PureThoughtBaptismItem) {
            prefix = "tooltip.starrail_sim.pure_thought_baptism.story_";
            lineCount = 5;
        } else if (stack.getItem() instanceof IdealBurningHellItem) {
            prefix = "tooltip.starrail_sim.ideal_burning_hell.story_";
            lineCount = 7;
        } else if (stack.getItem() instanceof EmbarkOnSecondLifeItem) {
            prefix = "tooltip.starrail_sim.embark_on_second_life.story_";
            lineCount = 8;
        } else if (stack.getItem() instanceof FinaleOfALieItem) {
            prefix = "tooltip.starrail_sim.finale_of_a_lie.story_";
            lineCount = 8;
        } else if (stack.getItem() instanceof SomethingIrreplaceableItem) {
            prefix = "tooltip.starrail_sim.something_irreplaceable.story_";
            lineCount = 7;
        } else if (stack.getItem() instanceof BrighterThanTheSunItem) {
            prefix = "tooltip.starrail_sim.brighter_than_the_sun.story_";
            lineCount = 6;
        } else if (stack.getItem() instanceof DanceAtSunsetItem) {
            prefix = "tooltip.starrail_sim.dance_at_sunset.story_";
            lineCount = 7;
        } else if (stack.getItem() instanceof TheUnreachableSideItem) {
            prefix = "tooltip.starrail_sim.the_unreachable_side.story_";
            lineCount = 4;
        } else if (stack.getItem() instanceof ThisBodyAsSwordItem) {
            prefix = "tooltip.starrail_sim.this_body_as_sword.story_";
            lineCount = 7;
        } else if (stack.getItem() instanceof NoRewardCrowningItem) {
            prefix = "tooltip.starrail_sim.no_reward_crowning.story_";
            lineCount = 9;
        } else if (stack.getItem() instanceof BloodFireBurningPathItem) {
            prefix = "tooltip.starrail_sim.blood_fire_burning_path.story_";
            lineCount = 13;
        } else if (stack.getItem() instanceof WhereDreamsBelongItem) {
            prefix = "tooltip.starrail_sim.where_dreams_belong.story_";
            lineCount = 13;
        } else if (stack.getItem() instanceof DawnBurnsJustSoItem) {
            prefix = "tooltip.starrail_sim.dawn_burns_just_so.story_";
            lineCount = 11;
        } else if (stack.getItem() instanceof WhatYouSeeIsMeItem) {
            prefix = "tooltip.starrail_sim.what_you_see_is_me.story_";
            lineCount = 7;
        } else if (stack.getItem() instanceof GalaxyRailwayItem) {
            prefix = "tooltip.starrail_sim.galaxy_railway.story_";
            lineCount = 4;
        } else if (stack.getItem() instanceof MomentOfGloryItem) {
            prefix = "tooltip.starrail_sim.moment_of_glory.story_";
            lineCount = 7;
        } else if (stack.getItem() instanceof BeforeDawnItem) {
            prefix = "tooltip.starrail_sim.before_dawn.story_";
            lineCount = 7;
        } else if (stack.getItem() instanceof PriceOfPeaceItem) {
            prefix = "tooltip.starrail_sim.price_of_peace.story_";
            lineCount = 14;
        } else if (stack.getItem() instanceof TowardsUnanswerableItem) {
            prefix = "tooltip.starrail_sim.towards_unanswerable.story_";
            lineCount = 10;
        } else if (stack.getItem() instanceof NinjaScrollItem) {
            prefix = "tooltip.starrail_sim.ninja_scroll.story_";
            lineCount = 13;
        } else if (stack.getItem() instanceof LifeAsALightItem) {
            prefix = "tooltip.starrail_sim.life_as_a_light.story_";
            lineCount = 9;
        } else if (stack.getItem() instanceof AStarIlluminatesNightSkyItem) {
            prefix = "tooltip.starrail_sim.a_star_illuminates_night_sky.story_";
            lineCount = 14;
        } else if (stack.getItem() instanceof SparkleQuietlyShinesItem) {
            prefix = "tooltip.starrail_sim.sparkle_quietly_shines.story_";
            lineCount = 9;
        } else if (stack.getItem() instanceof BattleIsntOverItem) {
            prefix = "tooltip.starrail_sim.battle_isnt_over.story_";
            lineCount = 6;
        } else if (stack.getItem() instanceof MemoryOfMeItem) {
            prefix = "tooltip.starrail_sim.memory_of_me.story_";
            lineCount = 7;
        } else if (stack.getItem() instanceof WeaveTimeIntoGoldItem) {
            prefix = "tooltip.starrail_sim.weave_time_into_gold.story_";
            lineCount = 9;
        } else if (stack.getItem() instanceof MakeFarewellMoreBeautifulItem) {
            prefix = "tooltip.starrail_sim.make_farewell_more_beautiful.story_";
            lineCount = 14;
        } else if (stack.getItem() instanceof LoveIsEternalItem) {
            prefix = "tooltip.starrail_sim.love_is_eternal.story_";
            lineCount = 11;
        } else if (stack.getItem() instanceof StarlightForLongNightsItem) {
            prefix = "tooltip.starrail_sim.starlight_for_long_nights.story_";
            lineCount = 11;
        } else if (stack.getItem() instanceof WelcomeToGalacticCityItem) {
            prefix = "tooltip.starrail_sim.welcome_to_galactic_city.story_";
            lineCount = 12;
            firstLineIndex = 0;
        } else if (stack.getItem() instanceof MeetInTheNextFlowerSeasonItem) {
            prefix = "tooltip.starrail_sim.meet_in_the_next_flower_season.story_";
            lineCount = 9;
            firstLineIndex = 0;
        } else if (stack.getItem() instanceof WhenSheDecidesToSeeItem) {
            prefix = "tooltip.starrail_sim.when_she_decides_to_see.story_";
            lineCount = 12;
            firstLineIndex = 0;
        } else if (stack.getItem() instanceof FlowerWorldMesmerizingEyesItem) {
            prefix = "tooltip.starrail_sim.flower_world_mesmerizing_eyes.story_";
            lineCount = 12;
            firstLineIndex = 0;
        } else if (stack.getItem() instanceof MayRainbowStayInTheSkyItem) {
            prefix = "tooltip.starrail_sim.may_rainbow_stay_in_the_sky.story_";
            lineCount = 11;
        } else if (stack.getItem() instanceof NightFlowingColorsItem) {
            prefix = "tooltip.starrail_sim.night_flowing_colors.story_";
            lineCount = 11;
        } else if (stack.getItem() instanceof GameOfCosmicWorldsItem) {
            prefix = "tooltip.starrail_sim.game_of_cosmic_worlds.story_";
            lineCount = 6;
        } else if (stack.getItem() instanceof ReturningToEarthItem) {
            prefix = "tooltip.starrail_sim.returning_to_earth.story_";
            lineCount = 9;
        } else if (stack.getItem() instanceof IfTimeWereAFlowerItem) {
            prefix = "tooltip.starrail_sim.if_time_were_a_flower.story_";
            lineCount = 16;
        } else if (stack.getItem() instanceof AnAgeEtchedInGoldenBloodItem) {
            prefix = "tooltip.starrail_sim.an_age_etched_in_golden_blood.story_";
            lineCount = 11;
        } else if (stack.getItem() instanceof InTheNameOfTheWorldItem) {
            prefix = "tooltip.starrail_sim.in_the_name_of_the_world.story_";
            lineCount = 6;
        } else if (stack.getItem() instanceof OnTheShoreInTheFlowOfTimeItem) {
            prefix = "tooltip.starrail_sim.on_the_shore_in_the_flow_of_time.story_";
            lineCount = 6;
        } else if (stack.getItem() instanceof AThousandFoldSpringItem) {
            prefix = "tooltip.starrail_sim.a_thousand_fold_spring.story_";
            lineCount = 6;
        } else if (stack.getItem() instanceof OnlyWaitItem) {
            prefix = "tooltip.starrail_sim.only_wait.story_";
            lineCount = 7;
        } else if (stack.getItem() instanceof RainNeverStopsItem) {
            prefix = "tooltip.starrail_sim.rain_never_stops.story_";
            lineCount = 5;
        } else if (stack.getItem() instanceof LiesInTheWindItem) {
            prefix = "tooltip.starrail_sim.lies_in_the_wind.story_";
            lineCount = 11;
        } else if (stack.getItem() instanceof ReturnToLongRoadItem) {
            prefix = "tooltip.starrail_sim.return_to_long_road.story_";
            lineCount = 9;
        } else if (stack.getItem() instanceof ReforgedRemembranceItem) {
            prefix = "tooltip.starrail_sim.reforged_remembrance.story_";
            lineCount = 5;
        } else if (stack.getItem() instanceof OceanWhySingsItem) {
            prefix = "tooltip.starrail_sim.ocean_why_sings.story_";
            lineCount = 12;
        } else if (stack.getItem() instanceof DoNotForgetHerFlameItem) {
            prefix = "tooltip.starrail_sim.do_not_forget_her_flame.story_";
            lineCount = 8;
        } else if (stack.getItem() instanceof NewFleshOfInfernoItem) {
            prefix = "tooltip.starrail_sim.new_flesh_of_inferno.story_";
            lineCount = 8;
        } else if (stack.getItem() instanceof MomentOfVictoryItem) {
            prefix = "tooltip.starrail_sim.moment_of_victory.story_";
            lineCount = 5;
        } else if (stack.getItem() instanceof SheHasClosedHerEyesItem) {
            prefix = "tooltip.starrail_sim.she_has_closed_her_eyes.story_";
            lineCount = 5;
        } else if (stack.getItem() instanceof ThoughRiversAndMountainsItem) {
            prefix = "tooltip.starrail_sim.though_rivers_and_mountains.story_";
            lineCount = 11;
        } else if (stack.getItem() instanceof FateNeverFairItem) {
            prefix = "tooltip.starrail_sim.fate_never_fair.story_";
            lineCount = 7;
        } else if (stack.getItem() instanceof EchoesOfTheCoffinItem) {
            prefix = "tooltip.starrail_sim.echoes_of_the_coffin.story_";
            lineCount = 5;
        } else if (stack.getItem() instanceof TimeWaitsForNoOneItem) {
            prefix = "tooltip.starrail_sim.time_waits_for_no_one.story_";
            lineCount = 6;
        } else if (stack.getItem() instanceof NightOfFrightItem) {
            prefix = "tooltip.starrail_sim.night_of_fright.story_";
            lineCount = 8;
        } else if (stack.getItem() instanceof OnlyTheScentRemainsItem) {
            prefix = "tooltip.starrail_sim.only_the_scent_remains.story_";
            lineCount = 6;
        } else {
            prefix = "tooltip.starrail_sim.in_the_night.story_";
            lineCount = 4;
        }
        for (int index = firstLineIndex; index < firstLineIndex + lineCount; index++) {
            tooltip.add(Component.translatable(prefix + index).withStyle(theme));
        }
        tooltip.add(Component.empty());
        String hintKey;
        if (stack.getItem() instanceof IWillHuntItem) {
            hintKey = "tooltip.starrail_sim.i_will_hunt.shift_hint";
        } else if (stack.getItem() instanceof WorrisomeBlissfulItem) {
            hintKey = "tooltip.starrail_sim.worrisome_blissful.shift_hint";
        } else if (stack.getItem() instanceof SleepLikeTheDeadItem) {
            hintKey = "tooltip.starrail_sim.sleep_like_the_dead.shift_hint";
        } else if (stack.getItem() instanceof PureThoughtBaptismItem) {
            hintKey = "tooltip.starrail_sim.pure_thought_baptism.shift_hint";
        } else if (stack.getItem() instanceof IdealBurningHellItem) {
            hintKey = "tooltip.starrail_sim.ideal_burning_hell.shift_hint";
        } else if (stack.getItem() instanceof EmbarkOnSecondLifeItem) {
            hintKey = "tooltip.starrail_sim.embark_on_second_life.shift_hint";
        } else if (stack.getItem() instanceof FinaleOfALieItem) {
            hintKey = "tooltip.starrail_sim.finale_of_a_lie.shift_hint";
        } else if (stack.getItem() instanceof SomethingIrreplaceableItem) {
            hintKey = "tooltip.starrail_sim.something_irreplaceable.shift_hint";
        } else if (stack.getItem() instanceof BrighterThanTheSunItem) {
            hintKey = "tooltip.starrail_sim.brighter_than_the_sun.shift_hint";
        } else if (stack.getItem() instanceof DanceAtSunsetItem) {
            hintKey = "tooltip.starrail_sim.dance_at_sunset.shift_hint";
        } else if (stack.getItem() instanceof TheUnreachableSideItem) {
            hintKey = "tooltip.starrail_sim.the_unreachable_side.shift_hint";
        } else if (stack.getItem() instanceof ThisBodyAsSwordItem) {
            hintKey = "tooltip.starrail_sim.this_body_as_sword.shift_hint";
        } else if (stack.getItem() instanceof NoRewardCrowningItem) {
            hintKey = "tooltip.starrail_sim.no_reward_crowning.shift_hint";
        } else if (stack.getItem() instanceof BloodFireBurningPathItem) {
            hintKey = "tooltip.starrail_sim.blood_fire_burning_path.shift_hint";
        } else if (stack.getItem() instanceof WhereDreamsBelongItem) {
            hintKey = "tooltip.starrail_sim.where_dreams_belong.shift_hint";
        } else if (stack.getItem() instanceof DawnBurnsJustSoItem) {
            hintKey = "tooltip.starrail_sim.dawn_burns_just_so.shift_hint";
        } else if (stack.getItem() instanceof WhatYouSeeIsMeItem) {
            hintKey = "tooltip.starrail_sim.what_you_see_is_me.shift_hint";
        } else if (stack.getItem() instanceof GalaxyRailwayItem) {
            hintKey = "tooltip.starrail_sim.galaxy_railway.shift_hint";
        } else if (stack.getItem() instanceof MomentOfGloryItem) {
            hintKey = "tooltip.starrail_sim.moment_of_glory.shift_hint";
        } else if (stack.getItem() instanceof BeforeDawnItem) {
            hintKey = "tooltip.starrail_sim.before_dawn.shift_hint";
        } else if (stack.getItem() instanceof PriceOfPeaceItem) {
            hintKey = "tooltip.starrail_sim.price_of_peace.shift_hint";
        } else if (stack.getItem() instanceof TowardsUnanswerableItem) {
            hintKey = "tooltip.starrail_sim.towards_unanswerable.shift_hint";
        } else if (stack.getItem() instanceof NinjaScrollItem) {
            hintKey = "tooltip.starrail_sim.ninja_scroll.shift_hint";
        } else if (stack.getItem() instanceof LifeAsALightItem) {
            hintKey = "tooltip.starrail_sim.life_as_a_light.shift_hint";
        } else if (stack.getItem() instanceof AStarIlluminatesNightSkyItem) {
            hintKey = "tooltip.starrail_sim.a_star_illuminates_night_sky.shift_hint";
        } else if (stack.getItem() instanceof SparkleQuietlyShinesItem) {
            hintKey = "tooltip.starrail_sim.sparkle_quietly_shines.shift_hint";
        } else if (stack.getItem() instanceof BattleIsntOverItem) {
            hintKey = "tooltip.starrail_sim.battle_isnt_over.shift_hint";
        } else if (stack.getItem() instanceof MemoryOfMeItem) {
            hintKey = "tooltip.starrail_sim.memory_of_me.shift_hint";
        } else if (stack.getItem() instanceof WeaveTimeIntoGoldItem) {
            hintKey = "tooltip.starrail_sim.weave_time_into_gold.shift_hint";
        } else if (stack.getItem() instanceof MakeFarewellMoreBeautifulItem) {
            hintKey = "tooltip.starrail_sim.make_farewell_more_beautiful.shift_hint";
        } else if (stack.getItem() instanceof LoveIsEternalItem) {
            hintKey = "tooltip.starrail_sim.love_is_eternal.shift_hint";
        } else if (stack.getItem() instanceof StarlightForLongNightsItem) {
            hintKey = "tooltip.starrail_sim.starlight_for_long_nights.shift_hint";
        } else if (stack.getItem() instanceof WelcomeToGalacticCityItem) {
            hintKey = "tooltip.starrail_sim.welcome_to_galactic_city.shift_hint";
        } else if (stack.getItem() instanceof MeetInTheNextFlowerSeasonItem) {
            hintKey = "tooltip.starrail_sim.meet_in_the_next_flower_season.shift_hint";
        } else if (stack.getItem() instanceof WhenSheDecidesToSeeItem) {
            hintKey = "tooltip.starrail_sim.when_she_decides_to_see.shift_hint";
        } else if (stack.getItem() instanceof FlowerWorldMesmerizingEyesItem) {
            hintKey = "tooltip.starrail_sim.flower_world_mesmerizing_eyes.shift_hint";
        } else if (stack.getItem() instanceof MayRainbowStayInTheSkyItem) {
            hintKey = "tooltip.starrail_sim.may_rainbow_stay_in_the_sky.shift_hint";
        } else if (stack.getItem() instanceof NightFlowingColorsItem) {
            hintKey = "tooltip.starrail_sim.night_flowing_colors.shift_hint";
        } else if (stack.getItem() instanceof GameOfCosmicWorldsItem) {
            hintKey = "tooltip.starrail_sim.game_of_cosmic_worlds.shift_hint";
        } else if (stack.getItem() instanceof ReturningToEarthItem) {
            hintKey = "tooltip.starrail_sim.returning_to_earth.shift_hint";
        } else if (stack.getItem() instanceof IfTimeWereAFlowerItem) {
            hintKey = "tooltip.starrail_sim.if_time_were_a_flower.shift_hint";
        } else if (stack.getItem() instanceof AnAgeEtchedInGoldenBloodItem) {
            hintKey = "tooltip.starrail_sim.an_age_etched_in_golden_blood.shift_hint";
        } else if (stack.getItem() instanceof InTheNameOfTheWorldItem) {
            hintKey = "tooltip.starrail_sim.in_the_name_of_the_world.shift_hint";
        } else if (stack.getItem() instanceof OnTheShoreInTheFlowOfTimeItem) {
            hintKey = "tooltip.starrail_sim.on_the_shore_in_the_flow_of_time.shift_hint";
        } else if (stack.getItem() instanceof AThousandFoldSpringItem) {
            hintKey = "tooltip.starrail_sim.a_thousand_fold_spring.shift_hint";
        } else if (stack.getItem() instanceof OnlyWaitItem) {
            hintKey = "tooltip.starrail_sim.only_wait.shift_hint";
        } else if (stack.getItem() instanceof RainNeverStopsItem) {
            hintKey = "tooltip.starrail_sim.rain_never_stops.shift_hint";
        } else if (stack.getItem() instanceof LiesInTheWindItem) {
            hintKey = "tooltip.starrail_sim.lies_in_the_wind.shift_hint";
        } else if (stack.getItem() instanceof ReturnToLongRoadItem) {
            hintKey = "tooltip.starrail_sim.return_to_long_road.shift_hint";
        } else if (stack.getItem() instanceof ReforgedRemembranceItem) {
            hintKey = "tooltip.starrail_sim.reforged_remembrance.shift_hint";
        } else if (stack.getItem() instanceof OceanWhySingsItem) {
            hintKey = "tooltip.starrail_sim.ocean_why_sings.shift_hint";
        } else if (stack.getItem() instanceof DoNotForgetHerFlameItem) {
            hintKey = "tooltip.starrail_sim.do_not_forget_her_flame.shift_hint";
        } else if (stack.getItem() instanceof NewFleshOfInfernoItem) {
            hintKey = "tooltip.starrail_sim.new_flesh_of_inferno.shift_hint";
        } else if (stack.getItem() instanceof MomentOfVictoryItem) {
            hintKey = "tooltip.starrail_sim.moment_of_victory.shift_hint";
        } else if (stack.getItem() instanceof SheHasClosedHerEyesItem) {
            hintKey = "tooltip.starrail_sim.she_has_closed_her_eyes.shift_hint";
        } else if (stack.getItem() instanceof ThoughRiversAndMountainsItem) {
            hintKey = "tooltip.starrail_sim.though_rivers_and_mountains.shift_hint";
        } else if (stack.getItem() instanceof FateNeverFairItem) {
            hintKey = "tooltip.starrail_sim.fate_never_fair.shift_hint";
        } else if (stack.getItem() instanceof EchoesOfTheCoffinItem) {
            hintKey = "tooltip.starrail_sim.echoes_of_the_coffin.shift_hint";
        } else if (stack.getItem() instanceof TimeWaitsForNoOneItem) {
            hintKey = "tooltip.starrail_sim.time_waits_for_no_one.shift_hint";
        } else if (stack.getItem() instanceof NightOfFrightItem) {
            hintKey = "tooltip.starrail_sim.night_of_fright.shift_hint";
        } else if (stack.getItem() instanceof OnlyTheScentRemainsItem) {
            hintKey = "tooltip.starrail_sim.only_the_scent_remains.shift_hint";
        } else {
            hintKey = "tooltip.starrail_sim.in_the_night.shift_hint";
        }
        tooltip.add(Component.translatable(hintKey)
                .withStyle(ChatFormatting.GRAY));
    }

    private static void addFixedBaseAttributes(List<Component> tooltip,
                                               ChatFormatting theme) {
        tooltip.removeIf(StarRailItemTooltipHandler::isExistingFixedBaseAttribute);
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.light_cone.base_header").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.light_cone.base_values").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.light_cone.base_defense").withStyle(theme));
    }

    private static boolean isExistingFixedBaseAttribute(Component component) {
        if (component.getContents() instanceof TranslatableContents translatable) {
            String key = translatable.getKey();
            if (key.endsWith(".base_header") || key.endsWith(".base_values")
                    || key.endsWith(".base_defense") || key.endsWith(".base_attack")
                    || key.endsWith(".base_health_armor")
                    || key.startsWith("tooltip.starrail_sim.light_cone.base_")) {
                return true;
            }
        }
        String text = component.getString().toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replace("\u3000", "");
        boolean isBaseHeader = text.equals("光锥属性") || text.equals("基础属性")
                || text.equals("lightconeattributes") || text.equals("baseattributes");
        boolean isBaseOffenseAndHealth = text.contains("+10%")
                && (text.contains("最大生命值+5%") || text.contains("maxhealth+5%")
                || text.contains("maxhp+5%"));
        boolean isBaseArmor = text.equals("护甲值+5%") || text.equals("armor+5%");
        return isBaseHeader || isBaseOffenseAndHealth || isBaseArmor;
    }

    private static void addMechanicsTooltip(List<Component> tooltip, ItemStack stack,
                                            ChatFormatting theme) {
        if (stack.getItem() instanceof IWillHuntItem) {
            addIWillHuntMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof WorrisomeBlissfulItem) {
            addWorrisomeBlissfulMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof SleepLikeTheDeadItem) {
            addSleepLikeTheDeadMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof PureThoughtBaptismItem) {
            addPureThoughtBaptismMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof IdealBurningHellItem) {
            addIdealBurningHellMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof EmbarkOnSecondLifeItem) {
            addEmbarkOnSecondLifeMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof FinaleOfALieItem) {
            addFinaleOfALieMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof SomethingIrreplaceableItem) {
            addSomethingIrreplaceableMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof BrighterThanTheSunItem) {
            addBrighterThanTheSunMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof DanceAtSunsetItem) {
            addDanceAtSunsetMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof TheUnreachableSideItem) {
            addTheUnreachableSideMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof ThisBodyAsSwordItem) {
            addThisBodyAsSwordMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof NoRewardCrowningItem) {
            addNoRewardCrowningMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof BloodFireBurningPathItem) {
            addBloodFireBurningPathMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof WhereDreamsBelongItem) {
            addWhereDreamsBelongMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof DawnBurnsJustSoItem) {
            addDawnBurnsJustSoMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof WhatYouSeeIsMeItem) {
            addWhatYouSeeIsMeMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof GalaxyRailwayItem) {
            addGalaxyRailwayMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof MomentOfGloryItem) {
            addMomentOfGloryMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof BeforeDawnItem) {
            addBeforeDawnMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof PriceOfPeaceItem) {
            addPriceOfPeaceMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof TowardsUnanswerableItem) {
            addTowardsUnanswerableMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof NinjaScrollItem) {
            addNinjaScrollMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof LifeAsALightItem) {
            addLifeAsALightMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof AStarIlluminatesNightSkyItem) {
            addAStarIlluminatesNightSkyMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof SparkleQuietlyShinesItem) {
            addSparkleQuietlyShinesMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof BattleIsntOverItem) {
            addBattleIsntOverMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof MemoryOfMeItem) {
            addMemoryOfMeMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof WeaveTimeIntoGoldItem) {
            addWeaveTimeIntoGoldMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof MakeFarewellMoreBeautifulItem) {
            addMakeFarewellMoreBeautifulMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof LoveIsEternalItem) {
            addLoveIsEternalMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof StarlightForLongNightsItem) {
            addStarlightForLongNightsMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof WelcomeToGalacticCityItem) {
            addWelcomeToGalacticCityMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof MeetInTheNextFlowerSeasonItem) {
            addMeetInTheNextFlowerSeasonMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof WhenSheDecidesToSeeItem) {
            addWhenSheDecidesToSeeMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof FlowerWorldMesmerizingEyesItem) {
            addFlowerWorldMesmerizingEyesMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof MayRainbowStayInTheSkyItem) {
            addMayRainbowStayInTheSkyMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof NightFlowingColorsItem) {
            addNightFlowingColorsMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof GameOfCosmicWorldsItem) {
            addGameOfCosmicWorldsMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof ReturningToEarthItem) {
            addReturningToEarthMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof IfTimeWereAFlowerItem) {
            addIfTimeWereAFlowerMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof AnAgeEtchedInGoldenBloodItem) {
            addAnAgeEtchedInGoldenBloodMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof InTheNameOfTheWorldItem) {
            addInTheNameOfTheWorldMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof OnTheShoreInTheFlowOfTimeItem) {
            addOnTheShoreInTheFlowOfTimeMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof AThousandFoldSpringItem) {
            addAThousandFoldSpringMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof OnlyWaitItem) {
            addOnlyWaitMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof RainNeverStopsItem) {
            addRainNeverStopsMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof LiesInTheWindItem) {
            addLiesInTheWindMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof ReturnToLongRoadItem) {
            addReturnToLongRoadMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof ReforgedRemembranceItem) {
            addReforgedRemembranceMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof OceanWhySingsItem) {
            addOceanWhySingsMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof DoNotForgetHerFlameItem) {
            addDoNotForgetHerFlameMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof NewFleshOfInfernoItem) {
            addNewFleshOfInfernoMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof MomentOfVictoryItem) {
            addMomentOfVictoryMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof SheHasClosedHerEyesItem) {
            addSheHasClosedHerEyesMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof ThoughRiversAndMountainsItem) {
            addThoughRiversAndMountainsMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof FateNeverFairItem) {
            addFateNeverFairMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof EchoesOfTheCoffinItem) {
            addEchoesOfTheCoffinMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof TimeWaitsForNoOneItem) {
            addTimeWaitsForNoOneMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof NightOfFrightItem) {
            addNightOfFrightMechanics(tooltip, stack, theme);
        } else if (stack.getItem() instanceof OnlyTheScentRemainsItem) {
            addOnlyTheScentRemainsMechanics(tooltip, stack, theme);
        } else {
            addInTheNightMechanics(tooltip, stack, theme);
        }
    }

    private static void addInTheNightMechanics(List<Component> tooltip, ItemStack stack,
                                               ChatFormatting theme) {
        int superimposition = InTheNightItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.in_the_night.superimposition", theme,
                superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.in_the_night.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.in_the_night.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.in_the_night.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.in_the_night.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.in_the_night.special_crit_rate", theme,
                formatPercent(StarRailLightConeService.critRate(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.in_the_night.special_butterfly", theme,
                formatPercent(StarRailLightConeService.attackPerStack(superimposition)),
                formatPercent(StarRailLightConeService.critDamagePerStack(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.in_the_night.special_limit", theme);
    }

    private static void addIWillHuntMechanics(List<Component> tooltip, ItemStack stack,
                                              ChatFormatting theme) {
        int superimposition = IWillHuntItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.i_will_hunt.superimposition", theme,
                superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.i_will_hunt.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.i_will_hunt.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.i_will_hunt.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.i_will_hunt.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.i_will_hunt.special_crit_rate", theme,
                formatPercent(StarRailLightConeService.pursuitCritRate(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.i_will_hunt.special_crit_damage", theme,
                formatPercent(StarRailLightConeService.pursuitCritDamage(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.i_will_hunt.special_flowing_light", theme,
                formatPercent(StarRailLightConeService.pursuitDamagePerStack(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.i_will_hunt.special_limit", theme);
    }

    private static void addWorrisomeBlissfulMechanics(List<Component> tooltip,
                                                      ItemStack stack,
                                                      ChatFormatting theme) {
        int superimposition = WorrisomeBlissfulItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.worrisome_blissful.superimposition", theme,
                superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.worrisome_blissful.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.worrisome_blissful.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.worrisome_blissful.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.worrisome_blissful.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.worrisome_blissful.special_crit_rate", theme,
                formatPercent(StarRailLightConeService.worrisomeBlissfulCritRate(
                        superimposition)));
        addStyled(tooltip,
                "tooltip.starrail_sim.worrisome_blissful.special_attack_damage", theme,
                formatPercent(StarRailLightConeService.worrisomeBlissfulAttackDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.worrisome_blissful.special_tamed", theme,
                formatPercent(StarRailLightConeService.worrisomeBlissfulCritDamagePerStack(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.worrisome_blissful.special_limit", theme);
    }

    private static void addSleepLikeTheDeadMechanics(List<Component> tooltip,
                                                      ItemStack stack,
                                                      ChatFormatting theme) {
        int superimposition = SleepLikeTheDeadItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.sleep_like_the_dead.superimposition", theme,
                superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.sleep_like_the_dead.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.sleep_like_the_dead.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.sleep_like_the_dead.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.sleep_like_the_dead.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.sleep_like_the_dead.special_crit_damage",
                theme, formatPercent(StarRailLightConeService.sleepLikeTheDeadCritDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.sleep_like_the_dead.special_attack_damage",
                theme, formatPercent(StarRailLightConeService.sleepLikeTheDeadAttackDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.sleep_like_the_dead.special_dream", theme,
                formatPercent(StarRailLightConeService.beautifulDreamCritRate(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.sleep_like_the_dead.special_limit", theme);
    }

    private static void addPureThoughtBaptismMechanics(List<Component> tooltip,
                                                       ItemStack stack,
                                                       ChatFormatting theme) {
        int superimposition = PureThoughtBaptismItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.pure_thought_baptism.superimposition", theme,
                superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.pure_thought_baptism.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.pure_thought_baptism.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.pure_thought_baptism.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.pure_thought_baptism.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.pure_thought_baptism.special_crit_damage",
                theme, formatPercent(StarRailLightConeService.pureThoughtBaptismCritDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.pure_thought_baptism.special_training", theme,
                formatPercent(StarRailLightConeService.thoughtTrainingCritDamagePerStack(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.pure_thought_baptism.special_debate", theme,
                formatPercent(StarRailLightConeService.debateDamage(superimposition)),
                formatPercent(StarRailLightConeService.debateAttackDamage(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.pure_thought_baptism.special_limit", theme);
    }

    private static void addIdealBurningHellMechanics(List<Component> tooltip,
                                                     ItemStack stack,
                                                     ChatFormatting theme) {
        int superimposition = IdealBurningHellItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.ideal_burning_hell.superimposition", theme,
                superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.ideal_burning_hell.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.ideal_burning_hell.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.ideal_burning_hell.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.ideal_burning_hell.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.ideal_burning_hell.special_crit_rate", theme,
                formatPercent(StarRailLightConeService.rangerCritRate(superimposition)));
        addStyled(tooltip,
                "tooltip.starrail_sim.ideal_burning_hell.special_attack_damage", theme,
                formatPercent(StarRailLightConeService.rangerAttackDamage(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.ideal_burning_hell.special_ranger", theme,
                formatPercent(StarRailLightConeService.rangerAttackPerStack(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.ideal_burning_hell.special_limit", theme);
    }

    private static void addEmbarkOnSecondLifeMechanics(List<Component> tooltip,
                                                       ItemStack stack,
                                                       ChatFormatting theme) {
        int superimposition = EmbarkOnSecondLifeItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.embark_on_second_life.superimposition", theme,
                superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.embark_on_second_life.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.embark_on_second_life.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.embark_on_second_life.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.embark_on_second_life.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.embark_on_second_life.special_break_effect",
                theme, formatPercent(StarRailLightConeService.painfulVoyageBreakEffect(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.embark_on_second_life.special_break_damage",
                theme, formatPercent(StarRailLightConeService.painfulVoyageBreakDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.embark_on_second_life.special_voyage_attack",
                theme, formatPercent(StarRailLightConeService.painfulVoyageAttackDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.embark_on_second_life.special_limit", theme);
    }

    private static void addFinaleOfALieMechanics(List<Component> tooltip,
                                                 ItemStack stack,
                                                 ChatFormatting theme) {
        int superimposition = FinaleOfALieItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.finale_of_a_lie.superimposition", theme,
                superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.finale_of_a_lie.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.finale_of_a_lie.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.finale_of_a_lie.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.finale_of_a_lie.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.finale_of_a_lie.special_crit_rate", theme,
                formatPercent(StarRailLightConeService.finaleOfALieCritRate(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.finale_of_a_lie.special_shadow_attack",
                theme, formatPercent(StarRailLightConeService.shadowDevourAttackDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.finale_of_a_lie.special_shadow_damage",
                theme, formatPercent(StarRailLightConeService.shadowDevourDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.finale_of_a_lie.special_limit", theme);
    }

    private static void addSomethingIrreplaceableMechanics(List<Component> tooltip,
                                                            ItemStack stack,
                                                            ChatFormatting theme) {
        int superimposition = SomethingIrreplaceableItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.something_irreplaceable.superimposition",
                theme, superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.something_irreplaceable.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.something_irreplaceable.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.something_irreplaceable.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.something_irreplaceable.special_header",
                theme);
        addStyled(tooltip, "tooltip.starrail_sim.something_irreplaceable.special_attack",
                theme, formatPercent(StarRailLightConeService
                        .somethingIrreplaceableAttackDamage(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.something_irreplaceable.special_heal", theme,
                formatPercent(StarRailLightConeService.familyHealing(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.something_irreplaceable.special_family",
                theme, formatPercent(StarRailLightConeService.familyDamage(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.something_irreplaceable.special_limit", theme);
    }

    private static void addBrighterThanTheSunMechanics(List<Component> tooltip,
                                                       ItemStack stack,
                                                       ChatFormatting theme) {
        int superimposition = BrighterThanTheSunItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.brighter_than_the_sun.superimposition",
                theme, superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.brighter_than_the_sun.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.brighter_than_the_sun.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.brighter_than_the_sun.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.brighter_than_the_sun.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.brighter_than_the_sun.special_crit_rate",
                theme, formatPercent(StarRailLightConeService
                        .brighterThanTheSunCritRate(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.brighter_than_the_sun.special_roar_attack",
                theme, formatPercent(StarRailLightConeService
                        .dragonRoarAttackDamage(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.brighter_than_the_sun.special_roar_crit_damage",
                theme, formatPercent(StarRailLightConeService
                        .dragonRoarCritDamage(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.brighter_than_the_sun.special_limit", theme);
    }

    private static void addDanceAtSunsetMechanics(List<Component> tooltip, ItemStack stack,
                                                  ChatFormatting theme) {
        int superimposition = DanceAtSunsetItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.dance_at_sunset.superimposition",
                theme, superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.dance_at_sunset.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.dance_at_sunset.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.dance_at_sunset.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.dance_at_sunset.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.dance_at_sunset.special_crit_damage",
                theme, formatPercent(StarRailLightConeService
                        .danceAtSunsetCritDamage(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.dance_at_sunset.special_fire_dance",
                theme, formatPercent(StarRailLightConeService
                        .fireDanceDamage(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.dance_at_sunset.special_limit", theme);
    }

    private static void addTheUnreachableSideMechanics(List<Component> tooltip,
                                                       ItemStack stack,
                                                       ChatFormatting theme) {
        int superimposition = TheUnreachableSideItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.the_unreachable_side.superimposition",
                theme, superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.the_unreachable_side.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.the_unreachable_side.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.the_unreachable_side.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.the_unreachable_side.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.the_unreachable_side.special_crit_rate",
                theme, formatPercent(StarRailLightConeService
                        .noRetreatCritRate(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.the_unreachable_side.special_max_health",
                theme, formatPercent(StarRailLightConeService
                        .noRetreatMaxHealth(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.the_unreachable_side.special_damage",
                theme, formatPercent(StarRailLightConeService
                        .noRetreatDamage(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.the_unreachable_side.special_limit", theme);
    }

    private static void addThisBodyAsSwordMechanics(List<Component> tooltip,
                                                    ItemStack stack,
                                                    ChatFormatting theme) {
        int superimposition = ThisBodyAsSwordItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.this_body_as_sword.superimposition",
                theme, superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.this_body_as_sword.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.this_body_as_sword.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.this_body_as_sword.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.this_body_as_sword.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.this_body_as_sword.special_crit_damage",
                theme, formatPercent(StarRailLightConeService
                        .moonEclipseCritDamage(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.this_body_as_sword.special_moon_eclipse",
                theme, formatPercent(StarRailLightConeService
                        .moonEclipseDamage(superimposition)), formatPercent(
                        StarRailLightConeService.moonEclipseFullAttack(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.this_body_as_sword.special_limit", theme);
    }

    private static void addNoRewardCrowningMechanics(List<Component> tooltip,
                                                     ItemStack stack,
                                                     ChatFormatting theme) {
        int superimposition = NoRewardCrowningItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.no_reward_crowning.superimposition",
                theme, superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.no_reward_crowning.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.no_reward_crowning.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.no_reward_crowning.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.no_reward_crowning.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.no_reward_crowning.special_crit_damage",
                theme, formatPercent(StarRailLightConeService
                        .noRewardCrowningCritDamage(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.no_reward_crowning.special_attack",
                theme, formatPercent(StarRailLightConeService
                        .knightKingAttack(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.no_reward_crowning.special_break_attack",
                theme, formatPercent(StarRailLightConeService
                        .knightKingAttack(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.no_reward_crowning.special_limit", theme);
    }

    private static void addBloodFireBurningPathMechanics(List<Component> tooltip,
                                                          ItemStack stack,
                                                          ChatFormatting theme) {
        int superimposition = BloodFireBurningPathItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.blood_fire_burning_path.superimposition",
                theme, superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.blood_fire_burning_path.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.blood_fire_burning_path.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.blood_fire_burning_path.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.blood_fire_burning_path.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.blood_fire_burning_path.special_max_health",
                theme, formatPercent(StarRailLightConeService
                        .bloodFireMaxHealth(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.blood_fire_burning_path.special_healing",
                theme, formatPercent(StarRailLightConeService
                        .bloodFireHealing(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.blood_fire_burning_path.special_strife",
                theme, formatPercent(StarRailLightConeService.strifeDamage(superimposition)),
                formatPercent(StarRailLightConeService.strifeDamage(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.blood_fire_burning_path.special_limit",
                theme, formatPercent(StarRailLightConeService
                        .strifeHealthCost(superimposition)));
    }

    private static void addWhereDreamsBelongMechanics(List<Component> tooltip,
                                                      ItemStack stack,
                                                      ChatFormatting theme) {
        int superimposition = WhereDreamsBelongItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.where_dreams_belong.superimposition",
                theme, superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.where_dreams_belong.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.where_dreams_belong.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.where_dreams_belong.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.where_dreams_belong.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.where_dreams_belong.special_break_effect",
                theme, formatPercent(StarRailLightConeService
                        .metamorphosisBreakEffect(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.where_dreams_belong.special_effect",
                theme, formatPercent(StarRailLightConeService
                        .metamorphosisDamage(superimposition)), formatPercent(
                        StarRailLightConeService.metamorphosisAttack(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.where_dreams_belong.special_limit", theme);
    }

    private static void addDawnBurnsJustSoMechanics(List<Component> tooltip,
                                                     ItemStack stack,
                                                     ChatFormatting theme) {
        int superimposition = DawnBurnsJustSoItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.dawn_burns_just_so.superimposition", theme,
                superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.dawn_burns_just_so.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.dawn_burns_just_so.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.dawn_burns_just_so.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.dawn_burns_just_so.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.dawn_burns_just_so.special_attack", theme,
                formatPercent(StarRailLightConeService.dawnBurnsAttack(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.dawn_burns_just_so.special_damage", theme,
                formatPercent(StarRailLightConeService.dawnBurnsDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.dawn_burns_just_so.special_sun", theme,
                formatPercent(StarRailLightConeService.blazingSunDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.dawn_burns_just_so.special_limit", theme);
    }

    private static void addWhatYouSeeIsMeMechanics(List<Component> tooltip,
                                                    ItemStack stack,
                                                    ChatFormatting theme) {
        int superimposition = WhatYouSeeIsMeItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.what_you_see_is_me.superimposition", theme,
                superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.what_you_see_is_me.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.what_you_see_is_me.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.what_you_see_is_me.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.what_you_see_is_me.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.what_you_see_is_me.special_attack", theme,
                formatPercent(StarRailLightConeService.whatYouSeeIsMeAttack(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.what_you_see_is_me.special_next_damage",
                theme, formatPercent(StarRailLightConeService.whatYouSeeIsMeNextDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.what_you_see_is_me.special_crit_damage",
                theme, formatPercent(StarRailLightConeService.kinglyEntertainmentCritDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.what_you_see_is_me.special_limit", theme);
    }

    private static void addGalaxyRailwayMechanics(List<Component> tooltip,
                                                   ItemStack stack,
                                                   ChatFormatting theme) {
        int superimposition = GalaxyRailwayItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.galaxy_railway.superimposition", theme,
                superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.galaxy_railway.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.galaxy_railway.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.galaxy_railway.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.galaxy_railway.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.galaxy_railway.special_attack_per_enemy",
                theme, formatPercent(StarRailLightConeService.meteorAttackPerEnemy(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.galaxy_railway.special_meteor_damage",
                theme, formatPercent(StarRailLightConeService.meteorDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.galaxy_railway.special_limit", theme);
    }

    private static void addMomentOfGloryMechanics(List<Component> tooltip,
                                                  ItemStack stack,
                                                  ChatFormatting theme) {
        int superimposition = MomentOfGloryItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.moment_of_glory.superimposition", theme,
                superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.moment_of_glory.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.moment_of_glory.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.moment_of_glory.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.moment_of_glory.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.moment_of_glory.special_crit_damage",
                theme, formatPercent(StarRailLightConeService.momentOfGloryCritDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.moment_of_glory.special_courtesy_attack",
                theme, formatPercent(StarRailLightConeService.knightlyCourtesyAttack(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.moment_of_glory.special_limit", theme);
    }

    private static void addBeforeDawnMechanics(List<Component> tooltip,
                                               ItemStack stack,
                                               ChatFormatting theme) {
        int superimposition = BeforeDawnItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.before_dawn.superimposition", theme,
                superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.before_dawn.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.before_dawn.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.before_dawn.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.before_dawn.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.before_dawn.special_crit_damage", theme,
                formatPercent(StarRailLightConeService.beforeDawnCritDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.before_dawn.special_attack", theme,
                formatPercent(StarRailLightConeService.beforeDawnAttack(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.before_dawn.special_dream_body", theme,
                formatPercent(StarRailLightConeService.dreamBodyDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.before_dawn.special_limit", theme);
    }

    private static void addPriceOfPeaceMechanics(List<Component> tooltip,
                                                 ItemStack stack,
                                                 ChatFormatting theme) {
        int superimposition = PriceOfPeaceItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.price_of_peace.superimposition", theme,
                superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.price_of_peace.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.price_of_peace.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.price_of_peace.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.price_of_peace.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.price_of_peace.special_crit_rate", theme,
                formatPercent(StarRailLightConeService.priceOfPeaceCritRate(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.price_of_peace.special_over_crit_damage",
                theme, formatPercent(StarRailLightConeService.priceOfPeaceDamagePerCritStack(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.price_of_peace.special_promise_attack",
                theme, formatPercent(StarRailLightConeService.promiseAttack(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.price_of_peace.special_limit", theme);
    }

    private static void addTowardsUnanswerableMechanics(List<Component> tooltip,
                                                        ItemStack stack,
                                                        ChatFormatting theme) {
        int superimposition = TowardsUnanswerableItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.towards_unanswerable.superimposition", theme,
                superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.towards_unanswerable.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.towards_unanswerable.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.towards_unanswerable.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.towards_unanswerable.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.towards_unanswerable.special_crit_rate",
                theme, formatPercent(StarRailLightConeService.towardsUnanswerableCritRate(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.towards_unanswerable.special_deconstruction_damage",
                theme, formatPercent(StarRailLightConeService.deconstructionDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.towards_unanswerable.special_deconstruction_attack",
                theme, formatPercent(StarRailLightConeService.deconstructionAttack(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.towards_unanswerable.special_limit", theme);
    }

    private static void addNinjaScrollMechanics(List<Component> tooltip,
                                                ItemStack stack,
                                                ChatFormatting theme) {
        int superimposition = NinjaScrollItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.ninja_scroll.superimposition", theme,
                superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.ninja_scroll.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.ninja_scroll.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.ninja_scroll.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.ninja_scroll.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.ninja_scroll.special_break_effect", theme,
                formatPercent(StarRailLightConeService.ninjaScrollBreakEffect(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.ninja_scroll.special_crit_damage", theme,
                formatPercent(StarRailLightConeService.ninjaScrollCritDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.ninja_scroll.special_thunder_escape_attack",
                theme, formatPercent(StarRailLightConeService.thunderEscapeAttack(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.ninja_scroll.special_limit", theme);
    }

    private static void addLifeAsALightMechanics(List<Component> tooltip,
                                                 ItemStack stack,
                                                 ChatFormatting theme) {
        int superimposition = LifeAsALightItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.life_as_a_light.superimposition", theme,
                superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.life_as_a_light.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.life_as_a_light.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.life_as_a_light.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.life_as_a_light.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.life_as_a_light.special_crit_damage", theme,
                formatPercent(StarRailLightConeService.lifeAsALightCritDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.life_as_a_light.special_alchemy_damage",
                theme, formatPercent(StarRailLightConeService.alchemyDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.life_as_a_light.special_alchemy_attack",
                theme, formatPercent(StarRailLightConeService.alchemyAttack(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.life_as_a_light.special_limit", theme);
    }

    private static void addAStarIlluminatesNightSkyMechanics(List<Component> tooltip,
                                                              ItemStack stack,
                                                              ChatFormatting theme) {
        int superimposition = AStarIlluminatesNightSkyItem.getSuperimposition(stack);
        addStyled(tooltip,
                "tooltip.starrail_sim.a_star_illuminates_night_sky.superimposition",
                theme, superimposition);
        addStyled(tooltip,
                "tooltip.starrail_sim.a_star_illuminates_night_sky.base_header", theme);
        addStyled(tooltip,
                "tooltip.starrail_sim.a_star_illuminates_night_sky.base_values", theme);
        addStyled(tooltip,
                "tooltip.starrail_sim.a_star_illuminates_night_sky.base_defense", theme);
        addStyled(tooltip,
                "tooltip.starrail_sim.a_star_illuminates_night_sky.special_header", theme);
        addStyled(tooltip,
                "tooltip.starrail_sim.a_star_illuminates_night_sky.special_crit_damage",
                theme, formatPercent(StarRailLightConeService.aStarIlluminatesCritDamage(
                        superimposition)));
        addStyled(tooltip,
                "tooltip.starrail_sim.a_star_illuminates_night_sky.special_departure_damage",
                theme, formatPercent(StarRailLightConeService.departureDamage(
                        superimposition)));
        addStyled(tooltip,
                "tooltip.starrail_sim.a_star_illuminates_night_sky.special_departure_attack",
                theme, formatPercent(StarRailLightConeService.departureAttack(
                        superimposition)));
        addStyled(tooltip,
                "tooltip.starrail_sim.a_star_illuminates_night_sky.special_limit", theme);
    }

    private static void addSparkleQuietlyShinesMechanics(List<Component> tooltip,
                                                         ItemStack stack,
                                                         ChatFormatting theme) {
        int superimposition = SparkleQuietlyShinesItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.sparkle_quietly_shines.superimposition",
                theme, superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.sparkle_quietly_shines.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.sparkle_quietly_shines.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.sparkle_quietly_shines.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.sparkle_quietly_shines.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.sparkle_quietly_shines.special_crit_rate",
                theme, formatPercent(StarRailLightConeService.sparkleQuietlyShinesCritRate(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.sparkle_quietly_shines.special_crown_attack",
                theme, formatPercent(StarRailLightConeService.shiningCrownAttack(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.sparkle_quietly_shines.special_crown_damage",
                theme, formatPercent(StarRailLightConeService.shiningCrownDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.sparkle_quietly_shines.special_limit", theme);
    }

    private static void addBattleIsntOverMechanics(List<Component> tooltip,
                                                   ItemStack stack,
                                                   ChatFormatting theme) {
        int superimposition = BattleIsntOverItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.battle_isnt_over.superimposition",
                theme, superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.battle_isnt_over.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.battle_isnt_over.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.battle_isnt_over.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.battle_isnt_over.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.battle_isnt_over.special_attack",
                theme, formatPercent(StarRailLightConeService.inheritanceAttack(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.battle_isnt_over.special_damage",
                theme, formatPercent(StarRailLightConeService.inheritanceDamage(
                        superimposition)), formatPercent(StarRailLightConeService
                        .inheritanceAttack(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.battle_isnt_over.special_limit", theme);
    }

    private static void addMemoryOfMeMechanics(List<Component> tooltip,
                                                ItemStack stack,
                                                ChatFormatting theme) {
        int superimposition = MemoryOfMeItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.memory_of_me.superimposition",
                theme, superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.memory_of_me.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.memory_of_me.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.memory_of_me.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.memory_of_me.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.memory_of_me.special_break_effect",
                theme, formatPercent(StarRailLightConeService.mirrorBreakEffect(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.memory_of_me.special_attack",
                theme, formatPercent(StarRailLightConeService.plumFragranceAttack(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.memory_of_me.special_damage",
                theme, formatPercent(StarRailLightConeService.plumFragranceDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.memory_of_me.special_limit", theme);
    }

    private static void addWeaveTimeIntoGoldMechanics(List<Component> tooltip,
                                                       ItemStack stack,
                                                       ChatFormatting theme) {
        int superimposition = WeaveTimeIntoGoldItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.weave_time_into_gold.superimposition",
                theme, superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.weave_time_into_gold.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.weave_time_into_gold.special_attack",
                theme, formatPercent(StarRailLightConeService.weaveTimeIntoGoldAttack(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.weave_time_into_gold.special_crit_damage",
                theme, formatPercent(StarRailLightConeService.weaveTimeIntoGoldCritDamage(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.weave_time_into_gold.special_full_stack",
                theme, formatPercent(StarRailLightConeService.weaveTimeIntoGoldDamagePerStack(
                        superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.weave_time_into_gold.special_duration", theme);
    }

    private static void addMakeFarewellMoreBeautifulMechanics(List<Component> tooltip,
                                                               ItemStack stack,
                                                               ChatFormatting theme) {
        int superimposition = MakeFarewellMoreBeautifulItem.getSuperimposition(stack);
        addStyled(tooltip,
                "tooltip.starrail_sim.make_farewell_more_beautiful.superimposition",
                theme, superimposition);
        addStyled(tooltip,
                "tooltip.starrail_sim.make_farewell_more_beautiful.special_header", theme);
        addStyled(tooltip,
                "tooltip.starrail_sim.make_farewell_more_beautiful.special_values", theme,
                formatPercent(StarRailLightConeService.farewellMaxHealth(superimposition)),
                formatPercent(StarRailLightConeService.farewellAttack(superimposition)),
                formatPercent(StarRailLightConeService.netherBloomDamagePerStack(
                        superimposition)));
    }

    private static void addLoveIsEternalMechanics(List<Component> tooltip,
                                                  ItemStack stack,
                                                  ChatFormatting theme) {
        int level = LoveIsEternalItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.love_is_eternal.superimposition",
                theme, level);
        addStyled(tooltip, "tooltip.starrail_sim.love_is_eternal.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.love_is_eternal.special_attack", theme,
                formatPercent(StarRailLightConeService.loveIsEternalAttack(level)));
        addStyled(tooltip, "tooltip.starrail_sim.love_is_eternal.special_blank", theme,
                formatPercent(StarRailLightConeService.loveIsEternalBlankDamage(level)));
        addStyled(tooltip, "tooltip.starrail_sim.love_is_eternal.special_verse", theme,
                formatPercent(
                        StarRailLightConeService.loveIsEternalVerseCritDamage(level)));
        addStyled(tooltip, "tooltip.starrail_sim.love_is_eternal.special_synergy", theme,
                formatPercent(StarRailLightConeService.loveIsEternalSynergy(level)));
    }

    private static void addLoveIsEternalBaseAttributes(List<Component> tooltip,
                                                       ChatFormatting theme) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.love_is_eternal.base_header").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.love_is_eternal.base_values").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.love_is_eternal.base_defense").withStyle(theme));
    }

    private static void addStarlightForLongNightsMechanics(List<Component> tooltip,
                                                            ItemStack stack,
                                                            ChatFormatting theme) {
        int level = StarlightForLongNightsItem.getSuperimposition(stack);
        addStyled(tooltip,
                "tooltip.starrail_sim.starlight_for_long_nights.superimposition",
                theme, level);
        addStyled(tooltip,
                "tooltip.starrail_sim.starlight_for_long_nights.special_header", theme);
        addStyled(tooltip,
                "tooltip.starrail_sim.starlight_for_long_nights.special_health", theme,
                formatPercent(StarRailLightConeService
                        .starlightForLongNightsMaxHealth(level)));
        addStyled(tooltip,
                "tooltip.starrail_sim.starlight_for_long_nights.special_attack", theme,
                formatPercent(StarRailLightConeService
                        .starlightForLongNightsAttack(level)));
        addStyled(tooltip,
                "tooltip.starrail_sim.starlight_for_long_nights.special_night", theme,
                formatPercent(StarRailLightConeService.starlitNightDamage(level)));
    }

    private static void addStarlightForLongNightsBaseAttributes(
            List<Component> tooltip, ChatFormatting theme) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.starlight_for_long_nights.base_header")
                .withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.starlight_for_long_nights.base_values")
                .withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.starlight_for_long_nights.base_defense")
                .withStyle(theme));
    }

    private static void addWelcomeToGalacticCityMechanics(List<Component> tooltip,
                                                           ItemStack stack,
                                                           ChatFormatting theme) {
        int level = WelcomeToGalacticCityItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.welcome_to_galactic_city.superimposition",
                theme, level);
        addStyled(tooltip, "tooltip.starrail_sim.welcome_to_galactic_city.special_header",
                theme);
        addStyled(tooltip, "tooltip.starrail_sim.welcome_to_galactic_city.special_attack",
                theme, formatPercent(StarRailLightConeService
                        .welcomeToGalacticCityAttack(level)));
        addStyled(tooltip, "tooltip.starrail_sim.welcome_to_galactic_city.special_damage",
                theme, formatPercent(StarRailLightConeService
                        .welcomeToGalacticCityDamage(level)));
        addStyled(tooltip, "tooltip.starrail_sim.welcome_to_galactic_city.special_streak",
                theme, formatPercent(StarRailLightConeService
                        .winningStreakCritDamagePerStack(level)));
    }

    private static void addWelcomeToGalacticCityBaseAttributes(
            List<Component> tooltip, ChatFormatting theme) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.welcome_to_galactic_city.base_header")
                .withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.welcome_to_galactic_city.base_attack")
                .withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.welcome_to_galactic_city.base_health_armor")
                .withStyle(theme));
    }

    private static void addMeetInTheNextFlowerSeasonMechanics(
            List<Component> tooltip, ItemStack stack, ChatFormatting theme) {
        int level = MeetInTheNextFlowerSeasonItem.getSuperimposition(stack);
        addStyled(tooltip,
                "tooltip.starrail_sim.meet_in_the_next_flower_season.superimposition",
                theme, level);
        addStyled(tooltip,
                "tooltip.starrail_sim.meet_in_the_next_flower_season.special_header", theme);
        addStyled(tooltip,
                "tooltip.starrail_sim.meet_in_the_next_flower_season.special_crit_damage",
                theme, formatPercent(StarRailLightConeService
                        .meetInTheNextFlowerSeasonCritDamage(level)));
        addStyled(tooltip,
                "tooltip.starrail_sim.meet_in_the_next_flower_season.special_attack",
                theme, formatPercent(StarRailLightConeService
                        .meetInTheNextFlowerSeasonAttack(level)));
        addStyled(tooltip,
                "tooltip.starrail_sim.meet_in_the_next_flower_season.special_daydream",
                theme, formatPercent(StarRailLightConeService.daydreamDamagePerStack(level)));
    }

    private static void addMeetInTheNextFlowerSeasonBaseAttributes(
            List<Component> tooltip, ChatFormatting theme) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.meet_in_the_next_flower_season.base_header")
                .withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.meet_in_the_next_flower_season.base_attack")
                .withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.meet_in_the_next_flower_season.base_health_armor")
                .withStyle(theme));
    }

    private static void addWhenSheDecidesToSeeMechanics(
            List<Component> tooltip, ItemStack stack, ChatFormatting theme) {
        int level = WhenSheDecidesToSeeItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.when_she_decides_to_see.superimposition",
                theme, level);
        addStyled(tooltip, "tooltip.starrail_sim.when_she_decides_to_see.special_header",
                theme);
        addStyled(tooltip, "tooltip.starrail_sim.when_she_decides_to_see.special_attack",
                theme, formatPercent(StarRailLightConeService
                        .whenSheDecidesToSeeAttack(level)));
        addStyled(tooltip, "tooltip.starrail_sim.when_she_decides_to_see.special_fortune",
                theme, formatPercent(StarRailLightConeService.bestFortuneCritRate(level)),
                formatPercent(StarRailLightConeService.bestFortuneCritDamage(level)),
                formatPercent(StarRailLightConeService.bestFortuneDamage(level)));
    }

    private static void addWhenSheDecidesToSeeBaseAttributes(
            List<Component> tooltip, ChatFormatting theme) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.when_she_decides_to_see.base_header").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.when_she_decides_to_see.base_attack").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.when_she_decides_to_see.base_health_armor")
                .withStyle(theme));
    }

    private static void addFlowerWorldMesmerizingEyesMechanics(
            List<Component> tooltip, ItemStack stack, ChatFormatting theme) {
        int level = FlowerWorldMesmerizingEyesItem.getSuperimposition(stack);
        addStyled(tooltip,
                "tooltip.starrail_sim.flower_world_mesmerizing_eyes.superimposition",
                theme, level);
        addStyled(tooltip,
                "tooltip.starrail_sim.flower_world_mesmerizing_eyes.special_header", theme);
        addStyled(tooltip,
                "tooltip.starrail_sim.flower_world_mesmerizing_eyes.special_crit_damage",
                theme, formatPercent(StarRailLightConeService.flowerWorldCritDamage(level)));
        addStyled(tooltip,
                "tooltip.starrail_sim.flower_world_mesmerizing_eyes.special_push_stream",
                theme, formatPercent(StarRailLightConeService.pushStreamDamagePerStack(level)),
                formatPercent(StarRailLightConeService
                        .pushStreamCritDamagePerStack(level)));
    }

    private static void addFlowerWorldMesmerizingEyesBaseAttributes(
            List<Component> tooltip, ChatFormatting theme) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.flower_world_mesmerizing_eyes.base_header")
                .withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.flower_world_mesmerizing_eyes.base_attack")
                .withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.flower_world_mesmerizing_eyes.base_health_armor")
                .withStyle(theme));
    }

    private static void addMayRainbowStayInTheSkyMechanics(List<Component> tooltip,
                                                            ItemStack stack,
                                                            ChatFormatting theme) {
        int level = MayRainbowStayInTheSkyItem.getSuperimposition(stack);
        addStyled(tooltip,
                "tooltip.starrail_sim.may_rainbow_stay_in_the_sky.superimposition",
                theme, level);
        addStyled(tooltip,
                "tooltip.starrail_sim.may_rainbow_stay_in_the_sky.special_header", theme);
        addStyled(tooltip,
                "tooltip.starrail_sim.may_rainbow_stay_in_the_sky.special_health", theme,
                formatPercent(StarRailLightConeService.mayRainbowMaxHealth(level)));
        addStyled(tooltip,
                "tooltip.starrail_sim.may_rainbow_stay_in_the_sky.special_healing", theme,
                formatPercent(StarRailLightConeService.mayRainbowHealing(level)));
        addStyled(tooltip,
                "tooltip.starrail_sim.may_rainbow_stay_in_the_sky.special_cost", theme,
                formatPercent(StarRailLightConeService.mayRainbowHealthCost(level)));
        addStyled(tooltip,
                "tooltip.starrail_sim.may_rainbow_stay_in_the_sky.special_burst", theme,
                StarRailLightConeService.MAY_RAINBOW_ATTACKS_REQUIRED,
                formatPercent(StarRailLightConeService.mayRainbowDamageMultiplier(level)));
        addStyled(tooltip,
                "tooltip.starrail_sim.may_rainbow_stay_in_the_sky.special_reset", theme);
    }

    private static void addMayRainbowStayInTheSkyBaseAttributes(
            List<Component> tooltip, ChatFormatting theme) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.may_rainbow_stay_in_the_sky.base_header")
                .withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.may_rainbow_stay_in_the_sky.base_values")
                .withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.may_rainbow_stay_in_the_sky.base_defense")
                .withStyle(theme));
    }

    private static void addMakeFarewellMoreBeautifulBaseAttributes(
            List<Component> tooltip, ChatFormatting theme) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.make_farewell_more_beautiful.base_header")
                .withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.make_farewell_more_beautiful.base_values")
                .withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.make_farewell_more_beautiful.base_defense")
                .withStyle(theme));
    }

    private static void addNightFlowingColorsMechanics(List<Component> tooltip,
                                                       ItemStack stack,
                                                       ChatFormatting theme) {
        int superimposition = NightFlowingColorsItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.night_flowing_colors.superimposition",
                theme, superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.night_flowing_colors.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.night_flowing_colors.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.night_flowing_colors.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.night_flowing_colors.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.night_flowing_colors.special_chant",
                theme, formatPercent(StarRailLightConeService
                        .nightFlowingColorsChantAttack(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.night_flowing_colors.special_splendor",
                theme, formatPercent(StarRailLightConeService
                        .nightFlowingColorsSplendorAttack(superimposition)),
                formatPercent(StarRailLightConeService
                        .nightFlowingColorsSplendorDamage(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.night_flowing_colors.special_limit", theme);
    }

    private static void addGameOfCosmicWorldsMechanics(List<Component> tooltip,
                                                       ItemStack stack,
                                                       ChatFormatting theme) {
        int superimposition = GameOfCosmicWorldsItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.game_of_cosmic_worlds.superimposition",
                theme, superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.game_of_cosmic_worlds.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.game_of_cosmic_worlds.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.game_of_cosmic_worlds.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.game_of_cosmic_worlds.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.game_of_cosmic_worlds.special_crit_damage",
                theme, formatPercent(StarRailLightConeService
                        .gameOfCosmicWorldsCritDamage(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.game_of_cosmic_worlds.special_mask",
                theme, formatPercent(StarRailLightConeService
                        .gameOfCosmicWorldsMaskCritRate(superimposition)),
                formatPercent(StarRailLightConeService
                        .gameOfCosmicWorldsMaskCritDamage(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.game_of_cosmic_worlds.special_flame", theme);
        addStyled(tooltip, "tooltip.starrail_sim.game_of_cosmic_worlds.special_limit", theme);
    }

    private static void addReturningToEarthMechanics(List<Component> tooltip,
                                                      ItemStack stack,
                                                      ChatFormatting theme) {
        int superimposition = ReturningToEarthItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.returning_to_earth.superimposition",
                theme, superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.returning_to_earth.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.returning_to_earth.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.returning_to_earth.base_defense", theme);
        addStyled(tooltip, "tooltip.starrail_sim.returning_to_earth.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.returning_to_earth.special_trigger", theme);
        addStyled(tooltip, "tooltip.starrail_sim.returning_to_earth.special_bonus", theme,
                formatPercent(StarRailLightConeService.psalmAttackPerStack(superimposition)),
                formatPercent(StarRailLightConeService.psalmDamagePerStack(superimposition)));
    }

    private static void addIfTimeWereAFlowerMechanics(List<Component> tooltip,
                                                      ItemStack stack,
                                                      ChatFormatting theme) {
        int superimposition = IfTimeWereAFlowerItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.if_time_were_a_flower.superimposition",
                theme, superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.if_time_were_a_flower.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.if_time_were_a_flower.special_crit_damage",
                theme, formatPercent(StarRailLightConeService
                        .ifTimeWereAFlowerCritDamage(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.if_time_were_a_flower.special_trigger", theme);
        addStyled(tooltip, "tooltip.starrail_sim.if_time_were_a_flower.special_aura",
                theme, formatPercent(StarRailLightConeService
                        .ifTimeWereAFlowerCritRate(superimposition)),
                formatPercent(StarRailLightConeService
                        .ifTimeWereAFlowerCritDamageAura(superimposition)));
    }

    private static void addAnAgeEtchedInGoldenBloodMechanics(List<Component> tooltip,
                                                              ItemStack stack,
                                                              ChatFormatting theme) {
        int superimposition = AnAgeEtchedInGoldenBloodItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.an_age_etched_in_golden_blood.superimposition",
                theme, superimposition);
        addStyled(tooltip,
                "tooltip.starrail_sim.an_age_etched_in_golden_blood.special_header", theme);
        addStyled(tooltip,
                "tooltip.starrail_sim.an_age_etched_in_golden_blood.special_attack", theme,
                formatPercent(StarRailLightConeService.goldenBloodAttack(superimposition)));
        addStyled(tooltip,
                "tooltip.starrail_sim.an_age_etched_in_golden_blood.special_trigger", theme,
                formatPercent(StarRailLightConeService.goldenBloodLawAttack(superimposition)),
                formatPercent(StarRailLightConeService.goldenBloodLawDamage(superimposition)));
    }

    private static void addInTheNameOfTheWorldMechanics(List<Component> tooltip,
                                                         ItemStack stack,
                                                         ChatFormatting theme) {
        int superimposition = InTheNameOfTheWorldItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.in_the_name_of_the_world.superimposition",
                theme, superimposition);
        addStyled(tooltip, "tooltip.starrail_sim.in_the_name_of_the_world.special_header",
                theme);
        addStyled(tooltip, "tooltip.starrail_sim.in_the_name_of_the_world.special_damage",
                theme, formatPercent(StarRailLightConeService
                        .inTheNameDamageVsDebuffed(superimposition)));
        addStyled(tooltip, "tooltip.starrail_sim.in_the_name_of_the_world.special_will",
                theme, formatPercent(StarRailLightConeService
                        .inTheNameWillEffectHit(superimposition)),
                formatPercent(StarRailLightConeService.inTheNameWillAttack(superimposition)));
    }

    private static void addOnTheShoreInTheFlowOfTimeMechanics(List<Component> tooltip,
                                                               ItemStack stack,
                                                               ChatFormatting theme) {
        int superimposition = OnTheShoreInTheFlowOfTimeItem.getSuperimposition(stack);
        addStyled(tooltip,
                "tooltip.starrail_sim.on_the_shore_in_the_flow_of_time.superimposition",
                theme, superimposition);
        addStyled(tooltip,
                "tooltip.starrail_sim.on_the_shore_in_the_flow_of_time.special_header", theme);
        addStyled(tooltip,
                "tooltip.starrail_sim.on_the_shore_in_the_flow_of_time.special_crit_damage",
                theme, formatPercent(StarRailLightConeService.shoreCritDamage(superimposition)));
        addStyled(tooltip,
                "tooltip.starrail_sim.on_the_shore_in_the_flow_of_time.special_trigger", theme);
        addStyled(tooltip,
                "tooltip.starrail_sim.on_the_shore_in_the_flow_of_time.special_foam_echo",
                theme, formatPercent(StarRailLightConeService.shoreAttackPerStack(
                        superimposition)),
                formatPercent(StarRailLightConeService.shoreDamagePerStack(superimposition)));
    }

    private static void addAThousandFoldSpringMechanics(List<Component> tooltip,
                                                        ItemStack stack,
                                                        ChatFormatting theme) {
        int level = AThousandFoldSpringItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.a_thousand_fold_spring.superimposition",
                theme, level);
        addStyled(tooltip, "tooltip.starrail_sim.a_thousand_fold_spring.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.a_thousand_fold_spring.effect_hit", theme,
                formatPercent(StarRailLightConeService.thousandSpringsEffectHit(level)));
        addStyled(tooltip, "tooltip.starrail_sim.a_thousand_fold_spring.apply_stripped", theme,
                formatPercent(StarRailLightConeService.thousandSpringsStrippedDamage(level)));
        addStyled(tooltip, "tooltip.starrail_sim.a_thousand_fold_spring.upgrade_cornered", theme,
                formatPercent(StarRailLightConeService
                        .thousandSpringsCorneredExtraDamage(level)));
    }

    private static void addOnlyWaitMechanics(List<Component> tooltip, ItemStack stack,
                                             ChatFormatting theme) {
        int level = OnlyWaitItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.only_wait.superimposition", theme, level);
        addStyled(tooltip, "tooltip.starrail_sim.only_wait.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.only_wait.special_bonuses", theme,
                formatPercent(StarRailLightConeService.onlyWaitAttack(level)),
                formatPercent(StarRailLightConeService.onlyWaitDamage(level)));
        addStyled(tooltip, "tooltip.starrail_sim.only_wait.special_trigger", theme);
        addStyled(tooltip, "tooltip.starrail_sim.only_wait.special_dot", theme,
                formatPercent(StarRailLightConeService.onlyWaitDotAttack(level)));
    }

    private static void addRainNeverStopsMechanics(List<Component> tooltip, ItemStack stack,
                                                    ChatFormatting theme) {
        int level = RainNeverStopsItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.rain_never_stops.superimposition",
                theme, level);
        addStyled(tooltip, "tooltip.starrail_sim.rain_never_stops.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.rain_never_stops.special_bonuses", theme,
                formatPercent(StarRailLightConeService.rainNeverStopsEffectHit(level)),
                formatPercent(StarRailLightConeService.rainNeverStopsAttack(level)));
        addStyled(tooltip, "tooltip.starrail_sim.rain_never_stops.special_crit", theme,
                formatPercent(StarRailLightConeService.rainNeverStopsCritRate(level)));
        addStyled(tooltip, "tooltip.starrail_sim.rain_never_stops.special_trigger", theme);
        addStyled(tooltip, "tooltip.starrail_sim.rain_never_stops.special_aether_code", theme,
                formatPercent(StarRailLightConeService.rainNeverStopsVulnerability(level)));
    }

    private static void addLiesInTheWindMechanics(List<Component> tooltip, ItemStack stack,
                                                  ChatFormatting theme) {
        int level = LiesInTheWindItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.lies_in_the_wind.superimposition",
                theme, level);
        addStyled(tooltip, "tooltip.starrail_sim.lies_in_the_wind.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.lies_in_the_wind.special_attack", theme,
                formatPercent(StarRailLightConeService.liesInTheWindAttack(level)));
        addStyled(tooltip, "tooltip.starrail_sim.lies_in_the_wind.special_bewildered", theme,
                formatPercent(StarRailLightConeService
                        .liesInTheWindBewilderedVulnerability(level)));
        addStyled(tooltip, "tooltip.starrail_sim.lies_in_the_wind.special_stolen", theme,
                formatPercent(StarRailLightConeService
                        .liesInTheWindStolenVulnerability(level)));
        addStyled(tooltip, "tooltip.starrail_sim.lies_in_the_wind.special_latest", theme);
    }

    private static void addReturnToLongRoadMechanics(List<Component> tooltip,
                                                     ItemStack stack, ChatFormatting theme) {
        int level = ReturnToLongRoadItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.return_to_long_road.superimposition",
                theme, level);
        addStyled(tooltip, "tooltip.starrail_sim.return_to_long_road.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.return_to_long_road.special_bonuses", theme,
                formatPercent(StarRailLightConeService.returnToLongRoadAttack(level)),
                formatPercent(StarRailLightConeService.returnToLongRoadBreakEffect(level)));
        addStyled(tooltip, "tooltip.starrail_sim.return_to_long_road.special_trigger", theme);
        addStyled(tooltip, "tooltip.starrail_sim.return_to_long_road.special_scorching", theme,
                formatPercent(StarRailLightConeService
                        .returnToLongRoadScorchingVulnerability(level)));
    }

    private static void addReforgedRemembranceMechanics(List<Component> tooltip,
                                                        ItemStack stack,
                                                        ChatFormatting theme) {
        int level = ReforgedRemembranceItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.reforged_remembrance.superimposition",
                theme, level);
        addStyled(tooltip, "tooltip.starrail_sim.reforged_remembrance.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.reforged_remembrance.special_effect_hit",
                theme, formatPercent(StarRailLightConeService
                        .reforgedRemembranceEffectHit(level)));
        addStyled(tooltip, "tooltip.starrail_sim.reforged_remembrance.special_trigger",
                theme);
        addStyled(tooltip, "tooltip.starrail_sim.reforged_remembrance.special_per_stack",
                theme, formatPercent(StarRailLightConeService
                        .reforgedRemembranceAttackPerStack(level)),
                formatPercent(StarRailLightConeService
                        .reforgedRemembranceDamagePerStack(level)));
    }

    private static void addReforgedRemembranceBaseAttributes(List<Component> tooltip,
                                                              ChatFormatting theme) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.reforged_remembrance.base_header").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.reforged_remembrance.base_values").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.reforged_remembrance.base_defense").withStyle(theme));
    }

    private static void addOceanWhySingsMechanics(List<Component> tooltip, ItemStack stack,
                                                   ChatFormatting theme) {
        int level = OceanWhySingsItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.ocean_why_sings.superimposition", theme,
                level);
        addStyled(tooltip, "tooltip.starrail_sim.ocean_why_sings.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.ocean_why_sings.special_bonuses", theme,
                formatPercent(StarRailLightConeService.oceanWhySingsEffectHit(level)),
                formatPercent(StarRailLightConeService.oceanWhySingsAttack(level)));
        addStyled(tooltip, "tooltip.starrail_sim.ocean_why_sings.special_trigger", theme);
        addStyled(tooltip, "tooltip.starrail_sim.ocean_why_sings.special_dot", theme,
                formatPercent(StarRailLightConeService.oceanWhySingsDotAttack(level)));
    }

    private static void addOceanWhySingsBaseAttributes(List<Component> tooltip,
                                                        ChatFormatting theme) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.ocean_why_sings.base_header").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.ocean_why_sings.base_values").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.ocean_why_sings.base_defense").withStyle(theme));
    }

    private static void addDoNotForgetHerFlameMechanics(List<Component> tooltip,
                                                         ItemStack stack,
                                                         ChatFormatting theme) {
        int level = DoNotForgetHerFlameItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.do_not_forget_her_flame.superimposition",
                theme, level);
        addStyled(tooltip, "tooltip.starrail_sim.do_not_forget_her_flame.special_header",
                theme);
        addStyled(tooltip, "tooltip.starrail_sim.do_not_forget_her_flame.special_values",
                theme,
                formatPercent(StarRailLightConeService.doNotForgetHerFlameBreakEffect(level)),
                formatPercent(StarRailLightConeService.doNotForgetHerFlameDamage(level)),
                formatPercent(StarRailLightConeService
                        .doNotForgetHerFlameAttackWhileBurning(level)),
                formatPercent(StarRailLightConeService
                        .doNotForgetHerFlameCritDamageWhileBurning(level)));
    }

    private static void addDoNotForgetHerFlameBaseAttributes(List<Component> tooltip,
                                                              ChatFormatting theme) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.do_not_forget_her_flame.base_header").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.do_not_forget_her_flame.base_values").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.do_not_forget_her_flame.base_defense").withStyle(theme));
    }

    private static void addNewFleshOfInfernoMechanics(List<Component> tooltip,
                                                       ItemStack stack,
                                                       ChatFormatting theme) {
        int level = NewFleshOfInfernoItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.new_flesh_of_inferno.superimposition",
                theme, level);
        addStyled(tooltip, "tooltip.starrail_sim.new_flesh_of_inferno.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.new_flesh_of_inferno.special_values", theme,
                formatPercent(StarRailLightConeService.newFleshOfInfernoMaxHealth(level)),
                formatPercent(StarRailLightConeService
                        .newFleshOfInfernoCritDamageTaken(level)),
                formatPercent(StarRailLightConeService
                        .newFleshOfInfernoCritDamageTaken(level)));
    }

    private static void addNewFleshOfInfernoBaseAttributes(List<Component> tooltip,
                                                            ChatFormatting theme) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.new_flesh_of_inferno.base_header").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.new_flesh_of_inferno.base_values").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.new_flesh_of_inferno.base_defense").withStyle(theme));
    }

    private static void addMomentOfVictoryMechanics(List<Component> tooltip, ItemStack stack,
                                                    ChatFormatting theme) {
        int level = MomentOfVictoryItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.moment_of_victory.superimposition",
                theme, level);
        addStyled(tooltip, "tooltip.starrail_sim.moment_of_victory.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.moment_of_victory.special_values", theme,
                formatPercent(StarRailLightConeService.momentOfVictoryArmor(level)),
                formatPercent(StarRailLightConeService.winterShieldBonus(level)));
    }

    private static void addMomentOfVictoryBaseAttributes(List<Component> tooltip,
                                                          ChatFormatting theme) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.moment_of_victory.base_header").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.moment_of_victory.base_values").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.moment_of_victory.base_defense").withStyle(theme));
    }

    private static void addSheHasClosedHerEyesMechanics(List<Component> tooltip,
                                                        ItemStack stack,
                                                        ChatFormatting theme) {
        int level = SheHasClosedHerEyesItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.she_has_closed_her_eyes.superimposition",
                theme, level);
        addStyled(tooltip, "tooltip.starrail_sim.she_has_closed_her_eyes.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.she_has_closed_her_eyes.special_values", theme,
                formatPercent(StarRailLightConeService.closedEyesArmor(level)),
                formatPercent(StarRailLightConeService.closedEyesArmor(level)),
                formatPercent(StarRailLightConeService.closedEyesDamage(level)),
                formatPercent(StarRailLightConeService.closedEyesMissingHealthHeal(level)));
    }

    private static void addSheHasClosedHerEyesBaseAttributes(List<Component> tooltip,
                                                              ChatFormatting theme) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.she_has_closed_her_eyes.base_header").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.she_has_closed_her_eyes.base_values").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.she_has_closed_her_eyes.base_defense").withStyle(theme));
    }

    private static void addThoughRiversAndMountainsMechanics(List<Component> tooltip,
                                                              ItemStack stack,
                                                              ChatFormatting theme) {
        int level = ThoughRiversAndMountainsItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.though_rivers_and_mountains.superimposition",
                theme, level);
        addStyled(tooltip,
                "tooltip.starrail_sim.though_rivers_and_mountains.special_header", theme);
        addStyled(tooltip,
                "tooltip.starrail_sim.though_rivers_and_mountains.special_values", theme,
                formatPercent(StarRailLightConeService.thoughRiversArmor(level)),
                formatPercent(StarRailLightConeService.thoughRiversHealFromArmor(level)),
                formatPercent(StarRailLightConeService.thoughRiversGarrisonDamage(level)));
    }

    private static void addThoughRiversAndMountainsBaseAttributes(List<Component> tooltip,
                                                                   ChatFormatting theme) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.though_rivers_and_mountains.base_header").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.though_rivers_and_mountains.base_values").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.though_rivers_and_mountains.base_defense").withStyle(theme));
    }

    private static void addFateNeverFairMechanics(List<Component> tooltip, ItemStack stack,
                                                   ChatFormatting theme) {
        int level = FateNeverFairItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.fate_never_fair.superimposition", theme,
                level);
        addStyled(tooltip, "tooltip.starrail_sim.fate_never_fair.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.fate_never_fair.special_values", theme,
                formatPercent(StarRailLightConeService.fateNeverFairArmor(level)),
                formatPercent(StarRailLightConeService.fateNeverFairCritDamage(level)),
                formatPercent(StarRailLightConeService.fateNeverFairChipsDamage(level)));
    }

    private static void addFateNeverFairBaseAttributes(List<Component> tooltip,
                                                        ChatFormatting theme) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.fate_never_fair.base_header").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.fate_never_fair.base_values").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.fate_never_fair.base_defense").withStyle(theme));
    }

    private static void addEchoesOfTheCoffinMechanics(List<Component> tooltip,
                                                       ItemStack stack,
                                                       ChatFormatting theme) {
        int level = EchoesOfTheCoffinItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.echoes_of_the_coffin.superimposition",
                theme, level);
        addStyled(tooltip, "tooltip.starrail_sim.echoes_of_the_coffin.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.echoes_of_the_coffin.special_values", theme,
                formatPercent(StarRailLightConeService.echoesOfTheCoffinAttack(level)),
                formatPercent(StarRailLightConeService.echoesOfTheCoffinHealth(level)),
                formatPercent(StarRailLightConeService.echoesOfTheCoffinBonusDamage(level)));
    }

    private static void addEchoesOfTheCoffinBaseAttributes(List<Component> tooltip,
                                                            ChatFormatting theme) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.echoes_of_the_coffin.base_header").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.echoes_of_the_coffin.base_values").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.echoes_of_the_coffin.base_defense").withStyle(theme));
    }

    private static void addTimeWaitsForNoOneMechanics(List<Component> tooltip,
                                                       ItemStack stack,
                                                       ChatFormatting theme) {
        int level = TimeWaitsForNoOneItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.time_waits_for_no_one.superimposition",
                theme, level);
        addStyled(tooltip, "tooltip.starrail_sim.time_waits_for_no_one.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.time_waits_for_no_one.special_stats", theme,
                formatPercent(StarRailLightConeService.timeWaitsForNoOneMaxHealth(level)),
                formatPercent(StarRailLightConeService.timeWaitsForNoOneHealing(level)));
        addStyled(tooltip, "tooltip.starrail_sim.time_waits_for_no_one.special_damage", theme,
                formatPercent(StarRailLightConeService.timeWaitsForNoOneDamage(level)));
    }

    private static void addTimeWaitsForNoOneBaseAttributes(List<Component> tooltip,
                                                            ChatFormatting theme) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.time_waits_for_no_one.base_header").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.time_waits_for_no_one.base_values").withStyle(theme));
        tooltip.add(Component.translatable(
                "tooltip.starrail_sim.time_waits_for_no_one.base_defense").withStyle(theme));
    }

    private static void addNightOfFrightMechanics(List<Component> tooltip, ItemStack stack,
                                                   ChatFormatting theme) {
        int level = NightOfFrightItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.night_of_fright.superimposition",
                theme, level);
        addStyled(tooltip, "tooltip.starrail_sim.night_of_fright.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.night_of_fright.special_health", theme,
                formatPercent(StarRailLightConeService.nightOfFrightMaxHealth(level)));
        addStyled(tooltip, "tooltip.starrail_sim.night_of_fright.special_heal", theme,
                formatPercent(StarRailLightConeService.nightOfFrightHealing(level)));
        addStyled(tooltip, "tooltip.starrail_sim.night_of_fright.special_buff", theme,
                formatPercent(StarRailLightConeService.deepBreathAttackPerStack(level)));
        addStyled(tooltip, "tooltip.starrail_sim.night_of_fright.special_duration", theme);
        addStyled(tooltip, "tooltip.starrail_sim.night_of_fright.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.night_of_fright.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.night_of_fright.base_defense", theme);
    }

    private static void addOnlyTheScentRemainsMechanics(List<Component> tooltip,
                                                        ItemStack stack,
                                                        ChatFormatting theme) {
        int level = OnlyTheScentRemainsItem.getSuperimposition(stack);
        addStyled(tooltip, "tooltip.starrail_sim.only_the_scent_remains.superimposition",
                theme, level);
        addStyled(tooltip, "tooltip.starrail_sim.only_the_scent_remains.special_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.only_the_scent_remains.special_stats", theme,
                formatPercent(StarRailLightConeService.onlyTheScentRemainsBreakEffect(level)),
                formatPercent(StarRailLightConeService.onlyTheScentRemainsMaxHealth(level)));
        addStyled(tooltip, "tooltip.starrail_sim.only_the_scent_remains.special_vulnerability",
                theme,
                formatPercent(StarRailLightConeService.onlyTheScentRemainsVulnerability(level)));
        addStyled(tooltip, "tooltip.starrail_sim.only_the_scent_remains.special_extra", theme,
                formatPercent(StarRailLightConeService.onlyTheScentRemainsExtraVulnerability(
                        level)));
        addStyled(tooltip, "tooltip.starrail_sim.only_the_scent_remains.base_header", theme);
        addStyled(tooltip, "tooltip.starrail_sim.only_the_scent_remains.base_values", theme);
        addStyled(tooltip, "tooltip.starrail_sim.only_the_scent_remains.base_defense", theme);
    }

    private static void addStyled(List<Component> tooltip, String key,
                                  ChatFormatting theme, Object... args) {
        // Fixed base attributes are shown outside the special-effect text.
        if (key.endsWith(".base_header") || key.endsWith(".base_values")
                || key.endsWith(".base_defense")) {
            return;
        }
        if (net.minecraft.client.resources.language.I18n.get(key).isBlank()) {
            return;
        }
        Object[] displayArgs = args.clone();
        for (int index = 0; index < displayArgs.length; index++) {
            if (displayArgs[index] instanceof String value && value.startsWith("+")) {
                displayArgs[index] = value.substring(1);
            }
        }
        tooltip.add(Component.translatable(key, displayArgs).withStyle(theme));
    }

    private static String formatPercent(double value) {
        double percent = value * 100.0D;
        if (Math.abs(percent - Math.rint(percent)) < 0.001D) {
            return String.format(Locale.ROOT, "+%.0f%%", percent);
        }
        return String.format(Locale.ROOT, "+%.1f%%", percent);
    }
}
