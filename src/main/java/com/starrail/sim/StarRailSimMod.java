package com.starrail.sim;

/**
 * 模组代码说明：模组入口和注册中心，集中注册光锥、状态效果、配方与创造模式物品栏，并连接 Forge 生命周期。
 */

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Main entry point for the Star Rail Sim mod.
 *
 * <p>Light Cones use Curios for equipment slots. Star Rail-inspired combat
 * attributes are registered by {@link StarRailAttributes} and attached to
 * players during entity attribute setup.</p>
 */
@Mod(StarRailSimMod.MOD_ID)
public final class StarRailSimMod {
    public static final String MOD_ID = "starrail_sim";

    // 延迟注册器：先声明注册表，再在模组构造阶段挂接 Forge 生命周期。
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);

    // 以下按命途注册自定义光锥；注册 ID 必须与模型、语言文件和战利品表一致。
    public static final RegistryObject<InTheNightItem> IN_THE_NIGHT = ITEMS.register(
            "in_the_night", InTheNightItem::new);
    public static final RegistryObject<IWillHuntItem> I_WILL_HUNT = ITEMS.register(
            "i_will_hunt", IWillHuntItem::new);
    public static final RegistryObject<WorrisomeBlissfulItem> WORRISOME_BLISSFUL = ITEMS.register(
            "worrisome_blissful", WorrisomeBlissfulItem::new);
    public static final RegistryObject<SleepLikeTheDeadItem> SLEEP_LIKE_THE_DEAD = ITEMS.register(
            "sleep_like_the_dead", SleepLikeTheDeadItem::new);
    public static final RegistryObject<PureThoughtBaptismItem> PURE_THOUGHT_BAPTISM = ITEMS.register(
            "pure_thought_baptism", PureThoughtBaptismItem::new);
    public static final RegistryObject<IdealBurningHellItem> IDEAL_BURNING_HELL = ITEMS.register(
            "ideal_burning_hell", IdealBurningHellItem::new);
    public static final RegistryObject<EmbarkOnSecondLifeItem> EMBARK_ON_SECOND_LIFE = ITEMS.register(
            "embark_on_second_life", EmbarkOnSecondLifeItem::new);
    public static final RegistryObject<FinaleOfALieItem> FINALE_OF_A_LIE = ITEMS.register(
            "finale_of_a_lie", FinaleOfALieItem::new);
    public static final RegistryObject<SomethingIrreplaceableItem> SOMETHING_IRREPLACEABLE =
            ITEMS.register("something_irreplaceable", SomethingIrreplaceableItem::new);
    public static final RegistryObject<BrighterThanTheSunItem> BRIGHTER_THAN_THE_SUN =
            ITEMS.register("brighter_than_the_sun", BrighterThanTheSunItem::new);
    public static final RegistryObject<DanceAtSunsetItem> DANCE_AT_SUNSET =
            ITEMS.register("dance_at_sunset", DanceAtSunsetItem::new);
    public static final RegistryObject<TheUnreachableSideItem> THE_UNREACHABLE_SIDE =
            ITEMS.register("the_unreachable_side", TheUnreachableSideItem::new);
    public static final RegistryObject<ThisBodyAsSwordItem> THIS_BODY_AS_SWORD =
            ITEMS.register("this_body_as_sword", ThisBodyAsSwordItem::new);
    public static final RegistryObject<NoRewardCrowningItem> NO_REWARD_CROWNING =
            ITEMS.register("no_reward_crowning", NoRewardCrowningItem::new);
    public static final RegistryObject<BloodFireBurningPathItem> BLOOD_FIRE_BURNING_PATH =
            ITEMS.register("blood_fire_burning_path", BloodFireBurningPathItem::new);
    public static final RegistryObject<WhereDreamsBelongItem> WHERE_DREAMS_BELONG =
            ITEMS.register("where_dreams_belong", WhereDreamsBelongItem::new);
    public static final RegistryObject<DawnBurnsJustSoItem> DAWN_BURNS_JUST_SO =
            ITEMS.register("dawn_burns_just_so", DawnBurnsJustSoItem::new);
    public static final RegistryObject<WhatYouSeeIsMeItem> WHAT_YOU_SEE_IS_ME =
            ITEMS.register("what_you_see_is_me", WhatYouSeeIsMeItem::new);
    public static final RegistryObject<GalaxyRailwayItem> GALAXY_RAILWAY =
            ITEMS.register("galaxy_railway", GalaxyRailwayItem::new);
    public static final RegistryObject<MomentOfGloryItem> MOMENT_OF_GLORY =
            ITEMS.register("moment_of_glory", MomentOfGloryItem::new);
    public static final RegistryObject<BeforeDawnItem> BEFORE_DAWN =
            ITEMS.register("before_dawn", BeforeDawnItem::new);
    public static final RegistryObject<PriceOfPeaceItem> PRICE_OF_PEACE = ITEMS.register(
            "price_of_peace", PriceOfPeaceItem::new);
    public static final RegistryObject<TowardsUnanswerableItem> TOWARDS_UNANSWERABLE =
            ITEMS.register("towards_unanswerable", TowardsUnanswerableItem::new);
    public static final RegistryObject<NinjaScrollItem> NINJA_SCROLL = ITEMS.register(
            "ninja_scroll", NinjaScrollItem::new);
    public static final RegistryObject<LifeAsALightItem> LIFE_AS_A_LIGHT = ITEMS.register(
            "life_as_a_light", LifeAsALightItem::new);
    public static final RegistryObject<AStarIlluminatesNightSkyItem> A_STAR_ILLUMINATES_NIGHT_SKY =
            ITEMS.register("a_star_illuminates_night_sky", AStarIlluminatesNightSkyItem::new);
    public static final RegistryObject<SparkleQuietlyShinesItem> SPARKLE_QUIETLY_SHINES =
            ITEMS.register("sparkle_quietly_shines", SparkleQuietlyShinesItem::new);
    public static final RegistryObject<BattleIsntOverItem> BATTLE_ISNT_OVER = ITEMS.register(
            "battle_isnt_over", BattleIsntOverItem::new);
    public static final RegistryObject<MemoryOfMeItem> MEMORY_OF_ME = ITEMS.register(
            "memory_of_me", MemoryOfMeItem::new);
    public static final RegistryObject<NightFlowingColorsItem> NIGHT_FLOWING_COLORS = ITEMS.register(
            "night_flowing_colors", NightFlowingColorsItem::new);
    public static final RegistryObject<GameOfCosmicWorldsItem> GAME_OF_COSMIC_WORLDS = ITEMS.register(
            "game_of_cosmic_worlds", GameOfCosmicWorldsItem::new);
    public static final RegistryObject<ReturningToEarthItem> RETURNING_TO_EARTH = ITEMS.register(
            "returning_to_earth", ReturningToEarthItem::new);
    public static final RegistryObject<IfTimeWereAFlowerItem> IF_TIME_WERE_A_FLOWER = ITEMS.register(
            "if_time_were_a_flower", IfTimeWereAFlowerItem::new);
    public static final RegistryObject<AnAgeEtchedInGoldenBloodItem>
            AN_AGE_ETCHED_IN_GOLDEN_BLOOD = ITEMS.register(
                    "an_age_etched_in_golden_blood", AnAgeEtchedInGoldenBloodItem::new);
    public static final RegistryObject<WeaveTimeIntoGoldItem> WEAVE_TIME_INTO_GOLD =
            ITEMS.register("weave_time_into_gold", WeaveTimeIntoGoldItem::new);
    public static final RegistryObject<MakeFarewellMoreBeautifulItem>
            MAKE_FAREWELL_MORE_BEAUTIFUL = ITEMS.register(
                    "make_farewell_more_beautiful", MakeFarewellMoreBeautifulItem::new);
    public static final RegistryObject<LoveIsEternalItem> LOVE_IS_ETERNAL = ITEMS.register(
            "love_is_eternal", LoveIsEternalItem::new);
    public static final RegistryObject<StarlightForLongNightsItem> STARLIGHT_FOR_LONG_NIGHTS =
            ITEMS.register("starlight_for_long_nights", StarlightForLongNightsItem::new);
    public static final RegistryObject<WelcomeToGalacticCityItem> WELCOME_TO_GALACTIC_CITY =
            ITEMS.register("welcome_to_galactic_city", WelcomeToGalacticCityItem::new);
    public static final RegistryObject<MeetInTheNextFlowerSeasonItem>
            MEET_IN_THE_NEXT_FLOWER_SEASON = ITEMS.register(
                    "meet_in_the_next_flower_season", MeetInTheNextFlowerSeasonItem::new);
    public static final RegistryObject<WhenSheDecidesToSeeItem> WHEN_SHE_DECIDES_TO_SEE =
            ITEMS.register("when_she_decides_to_see", WhenSheDecidesToSeeItem::new);
    public static final RegistryObject<FlowerWorldMesmerizingEyesItem>
            FLOWER_WORLD_MESMERIZING_EYES = ITEMS.register(
                    "flower_world_mesmerizing_eyes", FlowerWorldMesmerizingEyesItem::new);
    public static final RegistryObject<MayRainbowStayInTheSkyItem> MAY_RAINBOW_STAY_IN_THE_SKY =
            ITEMS.register("may_rainbow_stay_in_the_sky", MayRainbowStayInTheSkyItem::new);
    public static final RegistryObject<InTheNameOfTheWorldItem> IN_THE_NAME_OF_THE_WORLD =
            ITEMS.register("in_the_name_of_the_world", InTheNameOfTheWorldItem::new);
    public static final RegistryObject<OnTheShoreInTheFlowOfTimeItem>
            ON_THE_SHORE_IN_THE_FLOW_OF_TIME = ITEMS.register(
                    "on_the_shore_in_the_flow_of_time",
                    OnTheShoreInTheFlowOfTimeItem::new);
    public static final RegistryObject<AThousandFoldSpringItem> A_THOUSAND_FOLD_SPRING =
            ITEMS.register("a_thousand_fold_spring", AThousandFoldSpringItem::new);
    public static final RegistryObject<OnlyWaitItem> ONLY_WAIT =
            ITEMS.register("only_wait", OnlyWaitItem::new);
    public static final RegistryObject<RainNeverStopsItem> RAIN_NEVER_STOPS =
            ITEMS.register("rain_never_stops", RainNeverStopsItem::new);
    public static final RegistryObject<LiesInTheWindItem> LIES_IN_THE_WIND =
            ITEMS.register("lies_in_the_wind", LiesInTheWindItem::new);
    public static final RegistryObject<ReforgedRemembranceItem> REFORGED_REMEMBRANCE =
            ITEMS.register("reforged_remembrance", ReforgedRemembranceItem::new);
    public static final RegistryObject<OceanWhySingsItem> OCEAN_WHY_SINGS =
            ITEMS.register("ocean_why_sings", OceanWhySingsItem::new);
    public static final RegistryObject<DoNotForgetHerFlameItem> DO_NOT_FORGET_HER_FLAME =
            ITEMS.register("do_not_forget_her_flame", DoNotForgetHerFlameItem::new);
    public static final RegistryObject<NewFleshOfInfernoItem> NEW_FLESH_OF_INFERNO =
            ITEMS.register("new_flesh_of_inferno", NewFleshOfInfernoItem::new);
    public static final RegistryObject<ReturnToLongRoadItem> RETURN_TO_LONG_ROAD =
            ITEMS.register("return_to_long_road", ReturnToLongRoadItem::new);
    public static final RegistryObject<MomentOfVictoryItem> MOMENT_OF_VICTORY =
            ITEMS.register("moment_of_victory", MomentOfVictoryItem::new);
    public static final RegistryObject<SheHasClosedHerEyesItem> SHE_HAS_CLOSED_HER_EYES =
            ITEMS.register("she_has_closed_her_eyes", SheHasClosedHerEyesItem::new);
    public static final RegistryObject<ThoughRiversAndMountainsItem>
            THOUGH_RIVERS_AND_MOUNTAINS = ITEMS.register(
                    "though_rivers_and_mountains", ThoughRiversAndMountainsItem::new);
    public static final RegistryObject<FateNeverFairItem> FATE_NEVER_FAIR = ITEMS.register(
            "fate_never_fair", FateNeverFairItem::new);
    public static final RegistryObject<PathTrialTokenItem> PATH_TRIAL_TOKEN = ITEMS.register(
            "path_trial_token", PathTrialTokenItem::new);
    public static final RegistryObject<EchoesOfTheCoffinItem> ECHOES_OF_THE_COFFIN =
            ITEMS.register("echoes_of_the_coffin", EchoesOfTheCoffinItem::new);
    public static final RegistryObject<TimeWaitsForNoOneItem> TIME_WAITS_FOR_NO_ONE =
            ITEMS.register("time_waits_for_no_one", TimeWaitsForNoOneItem::new);
    public static final RegistryObject<NightOfFrightItem> NIGHT_OF_FRIGHT =
            ITEMS.register("night_of_fright", NightOfFrightItem::new);
    public static final RegistryObject<OnlyTheScentRemainsItem> ONLY_THE_SCENT_REMAINS =
            ITEMS.register("only_the_scent_remains", OnlyTheScentRemainsItem::new);
    // 状态效果集中注册区，光锥和命途效果通过这些注册对象引用。
    public static final RegistryObject<MobEffect> AETHER_CODE = EFFECTS.register(
            "aether_code", AetherCodeEffect::new);
    public static final RegistryObject<MobEffect> BEWILDERED = EFFECTS.register(
            "bewildered", BewilderedEffect::new);
    public static final RegistryObject<MobEffect> STOLEN = EFFECTS.register(
            "stolen", StolenEffect::new);
    public static final RegistryObject<MobEffect> SCORCHING = EFFECTS.register(
            "scorching", ScorchingEffect::new);
    public static final RegistryObject<MobEffect> PROPHET = EFFECTS.register(
            "prophet", ProphetEffect::new);
    public static final RegistryObject<MobEffect> SOUL_TRANCE = EFFECTS.register(
            "soul_trance", SoulTranceEffect::new);
    public static final RegistryObject<MobEffect> BURNING_SELF = EFFECTS.register(
            "burning_self", BurningSelfEffect::new);
    public static final RegistryObject<MobEffect> INFERNO = EFFECTS.register(
            "inferno", InfernoEffect::new);
    public static final RegistryObject<MobEffect> NIGHT_BUTTERFLY = EFFECTS.register(
            "night_butterfly", NightButterflyEffect::new);
    public static final RegistryObject<MobEffect> FLOWING_LIGHT = EFFECTS.register(
            "flowing_light", FlowingLightEffect::new);
    public static final RegistryObject<MobEffect> TAMED = EFFECTS.register(
            "tamed", TamedEffect::new);
    public static final RegistryObject<MobEffect> BEAUTIFUL_DREAM = EFFECTS.register(
            "beautiful_dream", BeautifulDreamEffect::new);
    public static final RegistryObject<MobEffect> DEBATE = EFFECTS.register(
            "debate", DebateEffect::new);
    public static final RegistryObject<MobEffect> RANGER = EFFECTS.register(
            "ranger", RangerEffect::new);
    public static final RegistryObject<MobEffect> PAINFUL_VOYAGE = EFFECTS.register(
            "painful_voyage", PainfulVoyageEffect::new);
    public static final RegistryObject<MobEffect> SHADOW_DEVOUR = EFFECTS.register(
            "shadow_devour", ShadowDevourEffect::new);
    public static final RegistryObject<MobEffect> FAMILY = EFFECTS.register(
            "family", FamilyEffect::new);
    public static final RegistryObject<MobEffect> DRAGON_ROAR = EFFECTS.register(
            "dragon_roar", DragonRoarEffect::new);
    public static final RegistryObject<MobEffect> FIRE_DANCE = EFFECTS.register(
            "fire_dance", FireDanceEffect::new);
    public static final RegistryObject<MobEffect> NO_RETREAT = EFFECTS.register(
            "no_retreat", NoRetreatEffect::new);
    public static final RegistryObject<MobEffect> MOON_ECLIPSE = EFFECTS.register(
            "moon_eclipse", MoonEclipseEffect::new);
    public static final RegistryObject<MobEffect> KNIGHT_KING = EFFECTS.register(
            "knight_king", KnightKingEffect::new);
    public static final RegistryObject<MobEffect> STRIFE = EFFECTS.register(
            "strife", StrifeEffect::new);
    public static final RegistryObject<MobEffect> METAMORPHOSIS = EFFECTS.register(
            "metamorphosis", MetamorphosisEffect::new);
    public static final RegistryObject<MobEffect> BLAZING_SUN = EFFECTS.register(
            "blazing_sun", BlazingSunEffect::new);
    public static final RegistryObject<MobEffect> KINGLY_ENTERTAINMENT = EFFECTS.register(
            "kingly_entertainment", KinglyEntertainmentEffect::new);
    public static final RegistryObject<MobEffect> METEOR = EFFECTS.register(
            "meteor", MeteorEffect::new);
    public static final RegistryObject<MobEffect> KNIGHTLY_COURTESY = EFFECTS.register(
            "knightly_courtesy", KnightlyCourtesyEffect::new);
    public static final RegistryObject<MobEffect> DREAM_BODY = EFFECTS.register(
            "dream_body", DreamBodyEffect::new);
    public static final RegistryObject<MobEffect> PROMISE = EFFECTS.register(
            "promise", PromiseEffect::new);
    public static final RegistryObject<MobEffect> DECONSTRUCTION = EFFECTS.register(
            "deconstruction", DeconstructionEffect::new);
    public static final RegistryObject<MobEffect> THUNDER_ESCAPE = EFFECTS.register(
            "thunder_escape", ThunderEscapeEffect::new);
    public static final RegistryObject<MobEffect> ALCHEMY = EFFECTS.register(
            "alchemy", AlchemyEffect::new);
    public static final RegistryObject<MobEffect> DEPARTURE = EFFECTS.register(
            "departure", DepartureEffect::new);
    public static final RegistryObject<MobEffect> SHINING_CROWN = EFFECTS.register(
            "shining_crown", ShiningCrownEffect::new);
    public static final RegistryObject<MobEffect> INHERITANCE = EFFECTS.register(
            "inheritance", InheritanceEffect::new);
    public static final RegistryObject<MobEffect> PLUM_FRAGRANCE = EFFECTS.register(
            "plum_fragrance", PlumFragranceEffect::new);
    public static final RegistryObject<MobEffect> CHANT = EFFECTS.register(
            "chant", ChantEffect::new);
    public static final RegistryObject<MobEffect> SPLENDOR = EFFECTS.register(
            "splendor", SplendorEffect::new);
    public static final RegistryObject<MobEffect> COLORFUL_FLAME = EFFECTS.register(
            "colorful_flame", ColorfulFlameEffect::new);
    public static final RegistryObject<MobEffect> MASK = EFFECTS.register(
            "mask", MaskEffect::new);
    public static final RegistryObject<MobEffect> PSALM = EFFECTS.register(
            "psalm", PsalmEffect::new);
    public static final RegistryObject<MobEffect> EDICT = EFFECTS.register(
            "edict", EdictEffect::new);
    public static final RegistryObject<MobEffect> LAW = EFFECTS.register(
            "law", LawEffect::new);
    public static final RegistryObject<MobEffect> WILL = EFFECTS.register(
            "will", WillEffect::new);
    public static final RegistryObject<MobEffect> FOAM_ECHO = EFFECTS.register(
            "foam_echo", FoamEchoEffect::new);
    public static final RegistryObject<MobEffect> STRIPPED_ARMOR = EFFECTS.register(
            "stripped_armor", StrippedArmorEffect::new);
    public static final RegistryObject<MobEffect> CORNERED_PREY = EFFECTS.register(
            "cornered_prey", CorneredPreyEffect::new);
    public static final RegistryObject<MobEffect> SILK_THREAD = EFFECTS.register(
            "silk_thread", SilkThreadEffect::new);
    public static final RegistryObject<MobEffect> WINTER_SHIELD = EFFECTS.register(
            "winter_shield", WinterShieldEffect::new);
    public static final RegistryObject<MobEffect> VISION = EFFECTS.register(
            "vision", VisionEffect::new);
    public static final RegistryObject<MobEffect> GARRISON = EFFECTS.register(
            "garrison", GarrisonEffect::new);
    public static final RegistryObject<MobEffect> CHIPS = EFFECTS.register(
            "chips", ChipsEffect::new);
    public static final RegistryObject<MobEffect> THORN = EFFECTS.register(
            "thorn", ThornEffect::new);
    public static final RegistryObject<MobEffect> DEEP_BREATH = EFFECTS.register(
            "deep_breath", DeepBreathEffect::new);
    public static final RegistryObject<MobEffect> FORGET_WORRY = EFFECTS.register(
            "forget_worry", ForgetWorryEffect::new);
    public static final RegistryObject<MobEffect> BROCADE = EFFECTS.register(
            "brocade", BrocadeEffect::new);
    public static final RegistryObject<MobEffect> NETHER_BLOOM = EFFECTS.register(
            "nether_bloom", NetherBloomEffect::new);
    public static final RegistryObject<MobEffect> BLANK = EFFECTS.register(
            "blank", BlankEffect::new);
    public static final RegistryObject<MobEffect> VERSE = EFFECTS.register(
            "verse", VerseEffect::new);
    public static final RegistryObject<MobEffect> STARLIT_NIGHT = EFFECTS.register(
            "starlit_night", StarlightNightEffect::new);
    public static final RegistryObject<MobEffect> WINNING_STREAK = EFFECTS.register(
            "winning_streak", WinningStreakEffect::new);
    public static final RegistryObject<MobEffect> DAYDREAM = EFFECTS.register(
            "daydream", DaydreamEffect::new);
    public static final RegistryObject<MobEffect> BEST_FORTUNE = EFFECTS.register(
            "best_fortune", BestFortuneEffect::new);
    public static final RegistryObject<MobEffect> PUSH_STREAM = EFFECTS.register(
            "push_stream", PushStreamEffect::new);
    public static final RegistryObject<Item> STAR_RAIL_LOGO = ITEMS.register(
            "star_rail_logo", () -> new Item(new Item.Properties()));
    public static final RegistryObject<RecipeSerializer<InTheNightSuperimpositionRecipe>>
            IN_THE_NIGHT_SUPERIMPOSITION_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "in_the_night_superimposition",
                    () -> new SimpleCraftingRecipeSerializer<>(
                            (id, category) -> new InTheNightSuperimpositionRecipe(
                                    id, category)));
    public static final RegistryObject<CreativeModeTab> MAIN_TAB = CREATIVE_TABS.register(
            "main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.starrail_sim.main"))
                    .icon(() -> new ItemStack(STAR_RAIL_LOGO.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(IN_THE_NIGHT.get());
                        output.accept(I_WILL_HUNT.get());
                        output.accept(WORRISOME_BLISSFUL.get());
                        output.accept(SLEEP_LIKE_THE_DEAD.get());
                        output.accept(PURE_THOUGHT_BAPTISM.get());
                        output.accept(IDEAL_BURNING_HELL.get());
                        output.accept(EMBARK_ON_SECOND_LIFE.get());
                        output.accept(FINALE_OF_A_LIE.get());
                        output.accept(SOMETHING_IRREPLACEABLE.get());
                        output.accept(BRIGHTER_THAN_THE_SUN.get());
                        output.accept(DANCE_AT_SUNSET.get());
                        output.accept(THE_UNREACHABLE_SIDE.get());
                        output.accept(THIS_BODY_AS_SWORD.get());
                        output.accept(NO_REWARD_CROWNING.get());
                        output.accept(BLOOD_FIRE_BURNING_PATH.get());
                        output.accept(WHERE_DREAMS_BELONG.get());
                        output.accept(DAWN_BURNS_JUST_SO.get());
                        output.accept(WHAT_YOU_SEE_IS_ME.get());
                        output.accept(GALAXY_RAILWAY.get());
                        output.accept(MOMENT_OF_GLORY.get());
                        output.accept(BEFORE_DAWN.get());
                        output.accept(PRICE_OF_PEACE.get());
                        output.accept(TOWARDS_UNANSWERABLE.get());
                        output.accept(NINJA_SCROLL.get());
                        output.accept(LIFE_AS_A_LIGHT.get());
                        output.accept(A_STAR_ILLUMINATES_NIGHT_SKY.get());
                        output.accept(SPARKLE_QUIETLY_SHINES.get());
                        output.accept(BATTLE_ISNT_OVER.get());
                        output.accept(MEMORY_OF_ME.get());
                        output.accept(NIGHT_FLOWING_COLORS.get());
                        output.accept(GAME_OF_COSMIC_WORLDS.get());
                        output.accept(RETURNING_TO_EARTH.get());
                        output.accept(IF_TIME_WERE_A_FLOWER.get());
                        output.accept(AN_AGE_ETCHED_IN_GOLDEN_BLOOD.get());
                        output.accept(IN_THE_NAME_OF_THE_WORLD.get());
                        output.accept(ON_THE_SHORE_IN_THE_FLOW_OF_TIME.get());
                        output.accept(A_THOUSAND_FOLD_SPRING.get());
                        output.accept(ONLY_WAIT.get());
                        output.accept(RAIN_NEVER_STOPS.get());
                        output.accept(LIES_IN_THE_WIND.get());
                        output.accept(RETURN_TO_LONG_ROAD.get());
                        output.accept(REFORGED_REMEMBRANCE.get());
                        output.accept(OCEAN_WHY_SINGS.get());
                        output.accept(DO_NOT_FORGET_HER_FLAME.get());
                        output.accept(NEW_FLESH_OF_INFERNO.get());
                        output.accept(MOMENT_OF_VICTORY.get());
                        output.accept(SHE_HAS_CLOSED_HER_EYES.get());
                        output.accept(THOUGH_RIVERS_AND_MOUNTAINS.get());
                        output.accept(FATE_NEVER_FAIR.get());
                        output.accept(ECHOES_OF_THE_COFFIN.get());
                        output.accept(TIME_WAITS_FOR_NO_ONE.get());
                        output.accept(NIGHT_OF_FRIGHT.get());
                        output.accept(ONLY_THE_SCENT_REMAINS.get());
                        output.accept(WEAVE_TIME_INTO_GOLD.get());
                        output.accept(MAKE_FAREWELL_MORE_BEAUTIFUL.get());
                        output.accept(LOVE_IS_ETERNAL.get());
                        output.accept(STARLIGHT_FOR_LONG_NIGHTS.get());
                        output.accept(WELCOME_TO_GALACTIC_CITY.get());
                        output.accept(MEET_IN_THE_NEXT_FLOWER_SEASON.get());
                        output.accept(WHEN_SHE_DECIDES_TO_SEE.get());
                        output.accept(FLOWER_WORLD_MESMERIZING_EYES.get());
                        output.accept(MAY_RAINBOW_STAY_IN_THE_SKY.get());
                        // 创造模式中提供九种命途凭证，供测试各自的试炼流程。
                        output.accept(pathTrialTokenForCreative(StarRailPath.PRESERVATION));
                        output.accept(pathTrialTokenForCreative(StarRailPath.DESTRUCTION));
                        output.accept(pathTrialTokenForCreative(StarRailPath.HUNT));
                        output.accept(pathTrialTokenForCreative(StarRailPath.ERUDITION));
                        output.accept(pathTrialTokenForCreative(StarRailPath.HARMONY));
                        output.accept(pathTrialTokenForCreative(StarRailPath.NIHILITY));
                        output.accept(pathTrialTokenForCreative(StarRailPath.ABUNDANCE));
                        output.accept(pathTrialTokenForCreative(StarRailPath.REMEMBRANCE));
                        output.accept(pathTrialTokenForCreative(StarRailPath.ELATION));
                    })
                    .build());

    // 创建带命途标记和可辨识名称的创造模式占位凭证。
    private static ItemStack pathTrialTokenForCreative(StarRailPath path) {
        ItemStack stack = new ItemStack(PATH_TRIAL_TOKEN.get());
        stack.getOrCreateTag().putString(PathTrialTokenItem.PATH_TAG, path.getId());
        return stack;
    }

    // 把各延迟注册器接入模组事件总线，并设置创造模式物品栏内容。
    public StarRailSimMod(FMLJavaModLoadingContext context) {
        var modEventBus = context.getModEventBus();
        StarRailRuinWorldgen.register(modEventBus);
        StarRailNetwork.register();
        StarRailAttributes.ATTRIBUTES.register(modEventBus);
        EFFECTS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        modEventBus.addListener(StarRailAttributes::addPlayerAttributes);
    }
}
