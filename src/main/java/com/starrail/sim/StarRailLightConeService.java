package com.starrail.sim;

/**
 * 模组代码说明：管理玩家装备的光锥及其战斗效果。
 */

import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

/** Server-authoritative light-cone effects and the In the Night prototype. */
public final class StarRailLightConeService {
    public static final int NIGHT_BUTTERFLY_DURATION = 30 * 20;
    public static final int NIGHT_BUTTERFLY_MAX_STACKS = 6;
    public static final int FLOWING_LIGHT_DURATION = 30 * 20;
    public static final int FLOWING_LIGHT_MAX_STACKS = 3;
    public static final int TAMED_DURATION = 30 * 20;
    public static final int TAMED_MAX_STACKS = 3;
    public static final int BEAUTIFUL_DREAM_DURATION = 10 * 20;
    public static final int BEAUTIFUL_DREAM_COOLDOWN = 20 * 20;
    public static final int DEBATE_DURATION = 20 * 20;
    public static final int THOUGHT_TRAINING_MAX_STACKS = 3;
    public static final int RANGER_DURATION = 20 * 20;
    public static final int RANGER_MAX_STACKS = 4;
    public static final int PAINFUL_VOYAGE_THRESHOLD_PERCENT = 100;
    public static final int METAMORPHOSIS_THRESHOLD_PERCENT = 120;
    public static final int SHADOW_DEVOUR_DURATION = 30 * 20;
    public static final int SHADOW_DEVOUR_ATTACKS_REQUIRED = 4;
    public static final int FAMILY_DURATION = 10 * 20;
    public static final int FAMILY_MAX_STACKS = 2;
    public static final int DRAGON_ROAR_DURATION = 20 * 20;
    public static final int DRAGON_ROAR_MAX_STACKS = 2;
    public static final int FIRE_DANCE_DURATION = 20 * 20;
    public static final int FIRE_DANCE_MAX_STACKS = 3;
    public static final int NO_RETREAT_DURATION = 5 * 20;
    public static final int MOON_ECLIPSE_DURATION = 20 * 20;
    public static final int MOON_ECLIPSE_MAX_STACKS = 3;
    public static final int KNIGHT_KING_DURATION = 10 * 20;
    public static final int STRIFE_DURATION = 10 * 20;
    public static final int BLAZING_SUN_DURATION = 15 * 20;
    public static final int KINGLY_ENTERTAINMENT_DURATION = 20 * 20;
    public static final int METEOR_DURATION = 20 * 20;
    public static final int METEOR_MAX_STACKS = 5;
    public static final int KNIGHTLY_COURTESY_DURATION = 10 * 20;
    public static final int KNIGHTLY_COURTESY_MAX_STACKS = 2;
    public static final int DREAM_BODY_DURATION = 20 * 20;
    public static final int DREAM_BODY_MAX_STACKS = 2;
    public static final int PROMISE_DURATION = 20 * 20;
    public static final int PROMISE_MAX_STACKS = 3;
    public static final int DECONSTRUCTION_DURATION = 30 * 20;
    public static final int DECONSTRUCTION_MAX_STACKS = 2;
    public static final int THUNDER_ESCAPE_DURATION = 20 * 20;
    public static final int THUNDER_ESCAPE_MAX_STACKS = 2;
    public static final int ALCHEMY_DURATION = 15 * 20;
    public static final int DEPARTURE_DURATION = 20 * 20;
    public static final int DEPARTURE_MAX_STACKS = 5;
    public static final int WINNING_STREAK_DURATION = 20 * 20;
    public static final int WINNING_STREAK_MAX_STACKS = 3;
    public static final int DAYDREAM_DURATION = 20 * 20;
    public static final int DAYDREAM_MAX_STACKS = 5;
    public static final int BEST_FORTUNE_DURATION = 20 * 20;
    public static final int PUSH_STREAM_DURATION = 10 * 20;
    public static final int PUSH_STREAM_MAX_STACKS = 4;
    public static final int SHINING_CROWN_DURATION = 20 * 20;
    public static final int INHERITANCE_DURATION = 10 * 20;
    public static final int NIGHT_FLOWING_COLORS_MAX_CHANT_STACKS = 4;
    public static final int NIGHT_FLOWING_COLORS_SPLENDOR_DURATION = 20 * 20;
    public static final int GAME_OF_COSMIC_WORLDS_MASK_DURATION = 20 * 20;
    public static final int GAME_OF_COSMIC_WORLDS_FLAME_MAX_STACKS = 4;
    public static final int PSALM_DURATION = 15 * 20;
    public static final int PSALM_MAX_STACKS = 3;
    public static final int EDICT_DURATION = 20 * 20;
    public static final int LAW_DURATION = 20 * 20;
    public static final int PLUM_FRAGRANCE_DURATION = 30 * 20;
    public static final int PLUM_FRAGRANCE_MAX_STACKS = 2;
    public static final int STRIFE_COOLDOWN = 15 * 20;
    public static final int FOAM_ECHO_DURATION = 20 * 20;
    public static final int FOAM_ECHO_MAX_STACKS = 5;
    public static final int SPRING_STATUS_DURATION = 20 * 20;
    public static final int SILK_THREAD_DURATION = 10 * 20;
    public static final int WINTER_SHIELD_DURATION = 10 * 20;
    public static final int VISION_DURATION = 10 * 20;
    public static final int GARRISON_DURATION = 15 * 20;
    public static final int FATE_NEVER_FAIR_CRIT_DAMAGE_DURATION = 10 * 20;
    public static final int THORN_DURATION = 10 * 20;
    public static final int CHIPS_DURATION = 15 * 20;
    public static final int CLOSED_EYES_HEAL_INTERVAL = 60 * 20;
    public static final int AETHER_CODE_DURATION = 10 * 20;
    public static final int BEWILDERED_DURATION = 10 * 20;
    public static final int STOLEN_DURATION = 20 * 20;
    public static final int PROPHET_DURATION = 15 * 20;
    public static final int PROPHET_MAX_STACKS = 4;
    public static final int SOUL_TRANCE_DURATION = 20 * 20;
    public static final int SCORCHING_DURATION = 20 * 20;
    public static final int SCORCHING_MAX_STACKS = 2;

    private static final UUID IN_THE_NIGHT_CRIT_RATE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a101");
    private static final UUID IN_THE_NIGHT_STACK_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a102");
    private static final UUID IN_THE_NIGHT_STACK_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a103");
    private static final UUID I_WILL_HUNT_CRIT_RATE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a111");
    private static final UUID I_WILL_HUNT_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a112");
    private static final UUID WORRISOME_BLISSFUL_CRIT_RATE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a121");
    private static final UUID WORRISOME_BLISSFUL_ATTACK_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a122");
    private static final UUID SLEEP_LIKE_THE_DEAD_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a131");
    private static final UUID SLEEP_LIKE_THE_DEAD_ATTACK_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a132");
    private static final UUID BEAUTIFUL_DREAM_CRIT_RATE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a133");
    private static final UUID PURE_THOUGHT_BAPTISM_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a141");
    private static final UUID DEBATE_ATTACK_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a142");
    private static final UUID IDEAL_BURNING_HELL_CRIT_RATE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a151");
    private static final UUID IDEAL_BURNING_HELL_ATTACK_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a152");
    private static final UUID EMBARK_ON_SECOND_LIFE_BREAK_EFFECT_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a161");
    private static final UUID EMBARK_ON_SECOND_LIFE_VOYAGE_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a162");
    private static final UUID FINALE_OF_A_LIE_CRIT_RATE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a171");
    private static final UUID FINALE_OF_A_LIE_SHADOW_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a172");
    private static final UUID SOMETHING_IRREPLACEABLE_ATTACK_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a181");
    private static final UUID BRIGHTER_THAN_THE_SUN_CRIT_RATE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a191");
    private static final UUID BRIGHTER_THAN_THE_SUN_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a192");
    private static final UUID BRIGHTER_THAN_THE_SUN_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a193");
    private static final UUID DANCE_AT_SUNSET_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a201");
    private static final UUID DANCE_AT_SUNSET_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a202");
    private static final UUID THE_UNREACHABLE_SIDE_CRIT_RATE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a211");
    private static final UUID THE_UNREACHABLE_SIDE_MAX_HEALTH_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a212");
    private static final UUID THIS_BODY_AS_SWORD_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a221");
    private static final UUID THIS_BODY_AS_SWORD_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a222");
    private static final UUID NO_REWARD_CROWNING_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a231");
    private static final UUID KNIGHT_KING_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a232");
    private static final UUID BLOOD_FIRE_MAX_HEALTH_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a241");
    private static final UUID BLOOD_FIRE_HEALING_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a242");
    private static final UUID WHERE_DREAMS_BREAK_EFFECT_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a251");
    private static final UUID WHERE_DREAMS_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a252");
    private static final UUID DAWN_BURNS_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a261");
    private static final UUID WHAT_YOU_SEE_IS_ME_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a271");
    private static final UUID WHAT_YOU_SEE_IS_ME_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a272");
    private static final UUID GALAXY_RAILWAY_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a281");
    private static final UUID MOMENT_OF_GLORY_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a291");
    private static final UUID MOMENT_OF_GLORY_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a292");
    private static final UUID BEFORE_DAWN_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a2a1");
    private static final UUID BEFORE_DAWN_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a2a2");
    private static final UUID PRICE_OF_PEACE_CRIT_RATE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a2b1");
    private static final UUID PROMISE_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a2b2");
    private static final UUID TOWARDS_UNANSWERABLE_CRIT_RATE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a2c1");
    private static final UUID DECONSTRUCTION_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a2c2");
    private static final UUID NINJA_SCROLL_BREAK_EFFECT_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a2d1");
    private static final UUID NINJA_SCROLL_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a2d2");
    private static final UUID THUNDER_ESCAPE_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a2d3");
    private static final UUID LIFE_AS_A_LIGHT_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a2e1");
    private static final UUID ALCHEMY_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a2e2");
    private static final UUID A_STAR_ILLUMINATES_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a2f1");
    private static final UUID DEPARTURE_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a2f2");
    private static final UUID SPARKLE_QUIETLY_SHINES_CRIT_RATE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a2f3");
    private static final UUID SHINING_CROWN_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a2f4");
    private static final UUID INHERITANCE_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a301");
    private static final UUID PLUM_FRAGRANCE_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a311");
    private static final UUID MIRROR_BREAK_EFFECT_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a312");
    private static final UUID NIGHT_FLOWING_COLORS_CHANT_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a321");
    private static final UUID NIGHT_FLOWING_COLORS_SPLENDOR_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a322");
    private static final UUID GAME_OF_COSMIC_WORLDS_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a331");
    private static final UUID GAME_OF_COSMIC_WORLDS_MASK_CRIT_RATE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a332");
    private static final UUID GAME_OF_COSMIC_WORLDS_MASK_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a333");
    private static final UUID PSALM_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a341");
    private static final UUID EDICT_CRIT_RATE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a351");
    private static final UUID EDICT_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a352");
    private static final UUID GOLDEN_BLOOD_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a361");
    private static final UUID LAW_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a362");
    private static final UUID WILL_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a371");
    private static final UUID WILL_EFFECT_HIT_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a372");
    private static final UUID SHORE_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a381");
    private static final UUID FOAM_ECHO_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a382");
    private static final UUID THOUSAND_SPRINGS_EFFECT_HIT_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a391");
    private static final UUID ONLY_WAIT_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a401");
    private static final UUID RAIN_NEVER_STOPS_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a411");
    private static final UUID RAIN_NEVER_STOPS_EFFECT_HIT_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a412");
    private static final UUID LIES_IN_THE_WIND_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a421");
    private static final UUID RETURN_TO_LONG_ROAD_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a431");
    private static final UUID RETURN_TO_LONG_ROAD_BREAK_EFFECT_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a432");
    private static final UUID REFORGED_REMEMBRANCE_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a441");
    private static final UUID REFORGED_REMEMBRANCE_EFFECT_HIT_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a442");
    private static final UUID OCEAN_WHY_SINGS_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a451");
    private static final UUID OCEAN_WHY_SINGS_EFFECT_HIT_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a452");
    private static final UUID DO_NOT_FORGET_HER_FLAME_BREAK_EFFECT_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a461");
    private static final UUID DO_NOT_FORGET_HER_FLAME_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a462");
    private static final UUID DO_NOT_FORGET_HER_FLAME_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a463");
    private static final UUID NEW_FLESH_OF_INFERNO_MAX_HEALTH_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a471");
    private static final UUID MOMENT_OF_VICTORY_ARMOR_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a481");
    private static final UUID WINTER_SHIELD_ARMOR_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a482");
    private static final UUID WINTER_SHIELD_TOUGHNESS_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a483");
    private static final UUID CLOSED_EYES_ARMOR_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a484");
    private static final UUID CLOSED_EYES_MAX_HEALTH_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a485");
    private static final UUID THOUGH_RIVERS_ARMOR_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a486");
    private static final UUID FATE_NEVER_FAIR_ARMOR_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a491");
    private static final UUID FATE_NEVER_FAIR_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a492");
    private static final UUID ECHOES_OF_THE_COFFIN_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a501");
    private static final UUID ECHOES_OF_THE_COFFIN_HEALTH_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a502");
    private static final UUID TIME_WAITS_FOR_NO_ONE_MAX_HEALTH_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a511");
    private static final UUID TIME_WAITS_FOR_NO_ONE_HEALING_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a512");
    private static final UUID MAY_RAINBOW_MAX_HEALTH_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a513");
    private static final UUID MAY_RAINBOW_HEALING_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a514");
    private static final UUID NIGHT_OF_FRIGHT_MAX_HEALTH_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a521");
    private static final UUID DEEP_BREATH_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a523");
    private static final UUID ONLY_THE_SCENT_REMAINS_BREAK_EFFECT_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a531");
    private static final UUID ONLY_THE_SCENT_REMAINS_MAX_HEALTH_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a532");
    private static final UUID WEAVE_TIME_INTO_GOLD_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a541");
    private static final UUID BROCADE_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a542");
    private static final UUID FAREWELL_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a551");
    private static final UUID FAREWELL_MAX_HEALTH_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a552");
    private static final UUID LOVE_IS_ETERNAL_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a561");
    private static final UUID LOVE_IS_ETERNAL_VERSE_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a562");
    private static final UUID STARLIGHT_FOR_LONG_NIGHTS_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a571");
    private static final UUID WELCOME_TO_GALACTIC_CITY_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a591");
    private static final UUID WINNING_STREAK_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a592");
    private static final UUID MEET_IN_THE_NEXT_FLOWER_SEASON_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a5a1");
    private static final UUID MEET_IN_THE_NEXT_FLOWER_SEASON_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a5a2");
    private static final UUID WHEN_SHE_DECIDES_TO_SEE_ATTACK_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a5b1");
    private static final UUID BEST_FORTUNE_CRIT_RATE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a5b2");
    private static final UUID BEST_FORTUNE_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a5b3");
    private static final UUID FLOWER_WORLD_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a5c1");
    private static final UUID PUSH_STREAM_CRIT_DAMAGE_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a5c2");
    private static final UUID STARLIGHT_FOR_LONG_NIGHTS_MAX_HEALTH_MODIFIER =
            UUID.fromString("0c7cf6f8-1d9f-49ce-a6f4-8c7fb5f5a572");
    private static final String STARLIT_NIGHT_SUPERIMPOSITION_TAG =
            "starrail_sim_starlit_night_superimposition";
    private static final String WINNING_STREAK_SUPERIMPOSITION_TAG =
            "starrail_sim_winning_streak_superimposition";
    private static final String DAYDREAM_SUPERIMPOSITION_TAG =
            "starrail_sim_daydream_superimposition";
    private static final String BEST_FORTUNE_SUPERIMPOSITION_TAG =
            "starrail_sim_best_fortune_superimposition";
    private static final String BEST_FORTUNE_OWNER_TAG = "starrail_sim_best_fortune_owner";
    private static final String PUSH_STREAM_SUPERIMPOSITION_TAG =
            "starrail_sim_push_stream_superimposition";
    private static final String LOVE_IS_ETERNAL_BLANK_SUPERIMPOSITION_TAG =
            "starrail_sim_love_is_eternal_blank_superimposition";
    private static final String LOVE_IS_ETERNAL_VERSE_SUPERIMPOSITION_TAG =
            "starrail_sim_love_is_eternal_verse_superimposition";
    private static final String LOVE_IS_ETERNAL_VERSE_OWNER_TAG =
            "starrail_sim_love_is_eternal_verse_owner";
    private static final String VISION_SUPERIMPOSITION_TAG =
            "starrail_sim_vision_superimposition";
    private static final String CLOSED_EYES_NEXT_HEAL_TICK_TAG =
            "starrail_sim_closed_eyes_next_heal_tick";
    private static final String GARRISON_SUPERIMPOSITION_TAG =
            "starrail_sim_garrison_superimposition";
    private static final String FATE_NEVER_FAIR_CRIT_DAMAGE_UNTIL_TAG =
            "starrail_sim_fate_never_fair_crit_damage_until";
    private static final String THORN_SUPERIMPOSITION_TAG =
            "starrail_sim_thorn_superimposition";
    private static final String TIME_WAITS_FOR_NO_ONE_RECORDED_LOSS_TAG =
            "starrail_sim_time_waits_for_no_one_recorded_loss";
    private static final String MAY_RAINBOW_ATTACK_COUNT_TAG =
            "starrail_sim_may_rainbow_attack_count";
    private static final String MAY_RAINBOW_CONSUMED_HEALTH_TAG =
            "starrail_sim_may_rainbow_consumed_health";
    private static final String MAY_RAINBOW_PENDING_DAMAGE_TAG =
            "starrail_sim_may_rainbow_pending_damage";
    private static final String MAY_RAINBOW_PENDING_TARGET_TAG =
            "starrail_sim_may_rainbow_pending_target";
    private static final String DEEP_BREATH_SUPERIMPOSITION_TAG =
            "starrail_sim_deep_breath_superimposition";
    private static final String FORGET_WORRY_SUPERIMPOSITION_TAG =
            "starrail_sim_forget_worry_superimposition";
    private static final String FORGET_WORRY_EXTRA_VULNERABILITY_TAG =
            "starrail_sim_forget_worry_extra_vulnerability";
    private static final String FORGET_WORRY_APPLIED_TICK_TAG =
            "starrail_sim_forget_worry_applied_tick";
    private static final String CHIPS_SUPERIMPOSITION_TAG =
            "starrail_sim_chips_superimposition";
    private static final String CHIPS_APPLIED_TICK_TAG =
            "starrail_sim_chips_applied_tick";
    private static final double[] MOMENT_OF_VICTORY_ARMOR = {
            0.60D, 0.70D, 0.80D, 0.90D, 1.00D};
    private static final double[] WINTER_SHIELD_BONUS = {
            0.24D, 0.28D, 0.32D, 0.36D, 0.40D};
    private static final double[] CLOSED_EYES_ARMOR = {
            0.24D, 0.28D, 0.32D, 0.36D, 0.40D};
    private static final double[] CLOSED_EYES_DAMAGE = {
            0.09D, 0.105D, 0.12D, 0.135D, 0.15D};
    private static final double[] CLOSED_EYES_MISSING_HEALTH_HEAL = {
            0.30D, 0.35D, 0.40D, 0.45D, 0.50D};
    private static final double[] THOUGH_RIVERS_ARMOR = {
            0.64D, 0.80D, 0.96D, 1.12D, 1.28D};
    private static final double[] THOUGH_RIVERS_HEAL_FROM_ARMOR = {
            0.10D, 0.125D, 0.15D, 0.175D, 0.20D};
    private static final double[] THOUGH_RIVERS_GARRISON_DAMAGE = {
            0.24D, 0.30D, 0.36D, 0.42D, 0.48D};
    private static final double[] FATE_NEVER_FAIR_ARMOR = {
            0.40D, 0.46D, 0.52D, 0.58D, 0.64D};
    private static final double[] FATE_NEVER_FAIR_CRIT_DAMAGE = {
            0.40D, 0.46D, 0.52D, 0.58D, 0.64D};
    private static final double[] FATE_NEVER_FAIR_CHIPS_DAMAGE = {
            0.20D, 0.25D, 0.30D, 0.35D, 0.40D};
    private static final double[] ECHOES_OF_THE_COFFIN_ATTACK = {
            0.24D, 0.28D, 0.32D, 0.36D, 0.40D};
    private static final double[] ECHOES_OF_THE_COFFIN_HEALTH = {
            0.24D, 0.28D, 0.32D, 0.36D, 0.40D};
    private static final double[] ECHOES_OF_THE_COFFIN_BONUS_DAMAGE = {
            0.20D, 0.25D, 0.30D, 0.35D, 0.40D};
    private static final double[] TIME_WAITS_FOR_NO_ONE_MAX_HEALTH = {
            0.10D, 0.20D, 0.30D, 0.40D, 0.50D};
    private static final double[] TIME_WAITS_FOR_NO_ONE_HEALING = {
            0.24D, 0.28D, 0.32D, 0.36D, 0.40D};
    private static final double[] TIME_WAITS_FOR_NO_ONE_DAMAGE = {
            0.36D, 0.42D, 0.48D, 0.54D, 0.60D};
    private static final double[] MAY_RAINBOW_MAX_HEALTH = {
            0.40D, 0.45D, 0.50D, 0.55D, 0.60D};
    private static final double[] MAY_RAINBOW_HEALING = {
            0.20D, 0.25D, 0.30D, 0.35D, 0.40D};
    private static final double[] MAY_RAINBOW_HEALTH_COST = {
            0.01D, 0.0125D, 0.015D, 0.0175D, 0.02D};
    private static final double[] MAY_RAINBOW_DAMAGE_MULTIPLIER = {
            2.50D, 3.125D, 3.75D, 4.375D, 5.00D};
    public static final int MAY_RAINBOW_ATTACKS_REQUIRED = 5;
    private static final double[] NIGHT_OF_FRIGHT_MAX_HEALTH = {
            0.29D, 0.33D, 0.37D, 0.41D, 0.45D};
    private static final double[] NIGHT_OF_FRIGHT_HEALING = {
            0.14D, 0.16D, 0.18D, 0.20D, 0.22D};
    private static final double[] DEEP_BREATH_ATTACK_PER_STACK = {
            0.02D, 0.04D, 0.06D, 0.08D, 0.10D};
    private static final double[] ONLY_THE_SCENT_REMAINS_BREAK_EFFECT = {
            0.60D, 0.70D, 0.80D, 0.90D, 1.00D};
    private static final double[] ONLY_THE_SCENT_REMAINS_MAX_HEALTH = {
            0.20D, 0.25D, 0.30D, 0.35D, 0.40D};
    private static final double[] ONLY_THE_SCENT_REMAINS_VULNERABILITY = {
            0.20D, 0.25D, 0.30D, 0.35D, 0.40D};
    private static final double[] ONLY_THE_SCENT_REMAINS_EXTRA_VULNERABILITY = {
            0.16D, 0.20D, 0.24D, 0.26D, 0.30D};
    private static final double[] WEAVE_TIME_INTO_GOLD_ATTACK = {
            0.25D, 0.30D, 0.35D, 0.40D, 0.45D};
    private static final double[] BROCADE_CRIT_DAMAGE_PER_STACK = {
            0.20D, 0.25D, 0.30D, 0.35D, 0.40D};
    private static final double[] BROCADE_DAMAGE_PER_STACK = {
            0.09D, 0.105D, 0.12D, 0.135D, 0.15D};
    private static final double[] FAREWELL_MAX_HEALTH = {
            0.30D, 0.375D, 0.45D, 0.525D, 0.60D};
    private static final double[] FAREWELL_ATTACK = {
            0.20D, 0.25D, 0.30D, 0.35D, 0.40D};
    private static final double[] NETHER_BLOOM_DAMAGE_PER_STACK = {
            0.30D, 0.35D, 0.40D, 0.45D, 0.50D};
    private static final double[] LOVE_IS_ETERNAL_ATTACK = {
            0.28D, 0.31D, 0.34D, 0.37D, 0.40D};
    private static final double[] LOVE_IS_ETERNAL_BLANK_DAMAGE = {
            0.30D, 0.32D, 0.34D, 0.36D, 0.38D};
    private static final double[] LOVE_IS_ETERNAL_VERSE_CRIT_DAMAGE = {
            0.35D, 0.40D, 0.45D, 0.50D, 0.55D};
    private static final double[] LOVE_IS_ETERNAL_SYNERGY = {
            0.60D, 0.70D, 0.80D, 0.90D, 1.00D};
    private static final double[] STARLIGHT_FOR_LONG_NIGHTS_MAX_HEALTH = {
            0.30D, 0.375D, 0.45D, 0.525D, 0.60D};
    private static final double[] STARLIGHT_FOR_LONG_NIGHTS_ATTACK = {
            0.20D, 0.25D, 0.30D, 0.35D, 0.40D};
    private static final double[] WELCOME_TO_GALACTIC_CITY_ATTACK = {
            0.30D, 0.35D, 0.40D, 0.45D, 0.50D};
    private static final double[] WELCOME_TO_GALACTIC_CITY_DAMAGE = {
            0.20D, 0.24D, 0.28D, 0.32D, 0.36D};
    private static final double[] WINNING_STREAK_CRIT_DAMAGE_PER_STACK = {
            0.40D, 0.50D, 0.60D, 0.70D, 0.80D};
    private static final double[] MEET_IN_THE_NEXT_FLOWER_SEASON_CRIT_DAMAGE = {
            0.60D, 0.75D, 0.90D, 1.05D, 1.20D};
    private static final double[] MEET_IN_THE_NEXT_FLOWER_SEASON_ATTACK = {
            0.30D, 0.35D, 0.40D, 0.45D, 0.50D};
    private static final double[] DAYDREAM_DAMAGE_PER_STACK = {
            0.15D, 0.1875D, 0.225D, 0.2625D, 0.30D};
    private static final double[] WHEN_SHE_DECIDES_TO_SEE_ATTACK = {
            0.18D, 0.21D, 0.24D, 0.27D, 0.30D};
    private static final double[] BEST_FORTUNE_CRIT_RATE = {
            0.10D, 0.11D, 0.12D, 0.13D, 0.14D};
    private static final double[] BEST_FORTUNE_CRIT_DAMAGE = {
            0.30D, 0.375D, 0.45D, 0.525D, 0.60D};
    private static final double[] BEST_FORTUNE_DAMAGE = {
            0.32D, 0.34D, 0.36D, 0.38D, 0.40D};
    private static final double[] FLOWER_WORLD_CRIT_DAMAGE = {
            0.48D, 0.56D, 0.64D, 0.72D, 0.80D};
    private static final double[] PUSH_STREAM_DAMAGE_PER_STACK = {
            0.05D, 0.06D, 0.07D, 0.08D, 0.09D};
    private static final double[] PUSH_STREAM_CRIT_DAMAGE_PER_STACK = {
            0.20D, 0.24D, 0.28D, 0.32D, 0.36D};
    private static final double[] STARLIT_NIGHT_DAMAGE = {
            0.40D, 0.50D, 0.60D, 0.70D, 0.80D};
    public static final int STARLIT_NIGHT_DURATION = 20 * 20;
    public static final int DEEP_BREATH_MAX_STACKS = 5;
    public static final int DEEP_BREATH_DURATION = 20 * 20;
    public static final int BROCADE_MAX_STACKS = 6;
    public static final int BROCADE_DURATION = 30 * 20;
    public static final int NETHER_BLOOM_MAX_STACKS = 5;
    public static final int NETHER_BLOOM_DURATION = 20 * 20;
    private static final String AETHER_CODE_SUPERIMPOSITION_TAG =
            "starrail_sim_aether_code_superimposition";
    private static final String AETHER_CODE_APPLIED_TICK_TAG =
            "starrail_sim_aether_code_applied_tick";
    private static final String LIES_IN_THE_WIND_SUPERIMPOSITION_TAG =
            "starrail_sim_lies_in_the_wind_superimposition";
    private static final String LIES_IN_THE_WIND_APPLIED_TICK_TAG =
            "starrail_sim_lies_in_the_wind_applied_tick";
    private static final String SCORCHING_SUPERIMPOSITION_TAG =
            "starrail_sim_scorching_superimposition";
    private static final String SCORCHING_APPLIED_TICK_TAG =
            "starrail_sim_scorching_applied_tick";
    private static final String PROPHET_SUPERIMPOSITION_TAG =
            "starrail_sim_prophet_superimposition";
    private static final String OCEAN_WHY_SINGS_DOT_KEY = "ocean_why_sings_soul_trance";
    private static final String INFERNO_SUPERIMPOSITION_TAG =
            "starrail_sim_inferno_superimposition";
    private static final String INFERNO_OWNER_TAG = "starrail_sim_inferno_owner";
    private static final String BEAUTIFUL_DREAM_COOLDOWN_TAG =
            "starrail_sim_beautiful_dream_cooldown";
    private static final String THOUGHT_TRAINING_TAG =
            "starrail_sim_thought_training";
    private static final String SHADOW_DEVOUR_ATTACK_COUNT_TAG =
            "starrail_sim_shadow_devour_attack_count";
    private static final String KNIGHT_KING_ATTACK_TAG =
            "starrail_sim_knight_king_attack";
    private static final String STRIFE_COOLDOWN_TAG =
            "starrail_sim_strife_cooldown";
    private static final String KINGLY_ENTERTAINMENT_NEXT_ATTACK_TAG =
            "starrail_sim_kingly_entertainment_next_attack";
    private static final String INHERITANCE_SKIP_CURRENT_HIT_TAG =
            "starrail_sim_inheritance_skip_current_hit";
    private static final String PLUM_FRAGRANCE_SUPERIMPOSITION_TAG =
            "starrail_sim_plum_fragrance_superimposition";
    private static final String NIGHT_FLOWING_COLORS_CHANT_SUPERIMPOSITION_TAG =
            "starrail_sim_night_flowing_colors_chant_superimposition";
    private static final String NIGHT_FLOWING_COLORS_SPLENDOR_SUPERIMPOSITION_TAG =
            "starrail_sim_night_flowing_colors_splendor_superimposition";
    private static final String NIGHT_FLOWING_COLORS_SPLENDOR_OWNER_TAG =
            "starrail_sim_night_flowing_colors_splendor_owner";
    private static final String GAME_OF_COSMIC_WORLDS_MASK_SUPERIMPOSITION_TAG =
            "starrail_sim_game_of_cosmic_worlds_mask_superimposition";
    private static final String GAME_OF_COSMIC_WORLDS_MASK_OWNER_TAG =
            "starrail_sim_game_of_cosmic_worlds_mask_owner";
    private static final String PSALM_SUPERIMPOSITION_TAG =
            "starrail_sim_psalm_superimposition";
    private static final String EDICT_SUPERIMPOSITION_TAG =
            "starrail_sim_edict_superimposition";
    private static final String LAW_SUPERIMPOSITION_TAG =
            "starrail_sim_law_superimposition";
    private static final String IN_THE_NAME_SUPERIMPOSITION_TAG =
            "starrail_sim_in_the_name_superimposition";
    private static final String FOAM_ECHO_SUPERIMPOSITION_TAG =
            "starrail_sim_foam_echo_superimposition";
    private static final String SPRING_STATUS_OWNER_TAG = "starrail_sim_spring_status_owner";
    private static final String SPRING_STATUS_SUPERIMPOSITION_TAG =
            "starrail_sim_spring_status_superimposition";

    private static final double[] PSALM_ATTACK_PER_STACK = {
            0.10D, 0.125D, 0.15D, 0.175D, 0.20D};
    private static final double[] PSALM_DAMAGE_PER_STACK = {
            0.15D, 0.17D, 0.19D, 0.21D, 0.24D};

    private static final double[] GAME_OF_COSMIC_WORLDS_CRIT_DAMAGE = {
            0.32D, 0.39D, 0.46D, 0.53D, 0.60D};
    private static final double[] GAME_OF_COSMIC_WORLDS_MASK_CRIT_RATE = {
            0.10D, 0.11D, 0.12D, 0.13D, 0.14D};
    private static final double[] GAME_OF_COSMIC_WORLDS_MASK_CRIT_DAMAGE = {
            0.28D, 0.35D, 0.42D, 0.49D, 0.56D};
    private static final double[] IF_TIME_WERE_A_FLOWER_CRIT_DAMAGE = {
            0.36D, 0.42D, 0.48D, 0.54D, 0.60D};
    private static final double[] IF_TIME_WERE_A_FLOWER_CRIT_RATE_AURA = {
            0.075D, 0.10D, 0.125D, 0.15D, 0.175D};
    private static final double[] IF_TIME_WERE_A_FLOWER_CRIT_DAMAGE_AURA = {
            0.48D, 0.60D, 0.72D, 0.84D, 0.96D};
    private static final double[] GOLDEN_BLOOD_ATTACK = {
            0.64D, 0.80D, 0.96D, 1.12D, 1.28D};
    private static final double[] GOLDEN_BLOOD_LAW_ATTACK = {
            0.10D, 0.125D, 0.15D, 0.175D, 0.20D};
    private static final double[] GOLDEN_BLOOD_LAW_DAMAGE = {
            0.54D, 0.675D, 0.81D, 0.945D, 1.08D};
    private static final double[] IN_THE_NAME_DAMAGE_VS_DEBUFFED = {
            0.24D, 0.28D, 0.32D, 0.36D, 0.40D};
    private static final double[] IN_THE_NAME_WILL_EFFECT_HIT = {
            0.18D, 0.21D, 0.24D, 0.27D, 0.30D};
    private static final double[] IN_THE_NAME_WILL_ATTACK = {
            0.30D, 0.40D, 0.50D, 0.60D, 0.70D};
    private static final double[] SHORE_CRIT_DAMAGE = {
            0.40D, 0.50D, 0.60D, 0.70D, 0.80D};
    private static final double[] SHORE_ATTACK_PER_STACK = {
            0.24D, 0.28D, 0.32D, 0.36D, 0.40D};
    private static final double[] SHORE_DAMAGE_PER_STACK = {
            0.24D, 0.28D, 0.32D, 0.36D, 0.40D};
    private static final double[] THOUSAND_SPRINGS_EFFECT_HIT = {
            0.60D, 0.70D, 0.80D, 0.90D, 1.00D};
    private static final double[] THOUSAND_SPRINGS_STRIPPED_DAMAGE = {
            0.10D, 0.12D, 0.14D, 0.16D, 0.18D};
    private static final double[] THOUSAND_SPRINGS_CORNERED_EXTRA_DAMAGE = {
            0.14D, 0.16D, 0.18D, 0.20D, 0.22D};
    private static final double[] ONLY_WAIT_ATTACK = {
            0.10D, 0.15D, 0.20D, 0.25D, 0.30D};
    private static final double[] ONLY_WAIT_DAMAGE = {
            0.24D, 0.28D, 0.32D, 0.36D, 0.40D};
    private static final double[] ONLY_WAIT_DOT_ATTACK = {
            0.60D, 0.70D, 0.80D, 0.90D, 1.00D};
    private static final double[] RAIN_NEVER_STOPS_EFFECT_HIT = {
            0.24D, 0.28D, 0.32D, 0.36D, 0.40D};
    private static final double[] RAIN_NEVER_STOPS_ATTACK = {
            0.10D, 0.15D, 0.20D, 0.25D, 0.30D};
    private static final double[] RAIN_NEVER_STOPS_CRIT_RATE = {
            0.12D, 0.14D, 0.16D, 0.18D, 0.20D};
    private static final double[] RAIN_NEVER_STOPS_VULNERABILITY = {
            0.20D, 0.30D, 0.40D, 0.50D, 0.60D};
    private static final double[] LIES_IN_THE_WIND_ATTACK = {
            0.18D, 0.21D, 0.24D, 0.27D, 0.30D};
    private static final double[] LIES_IN_THE_WIND_BEWILDERED_VULNERABILITY = {
            0.16D, 0.18D, 0.20D, 0.22D, 0.24D};
    private static final double[] LIES_IN_THE_WIND_STOLEN_VULNERABILITY = {
            0.20D, 0.22D, 0.24D, 0.26D, 0.28D};
    private static final double[] RETURN_TO_LONG_ROAD_ATTACK = {
            0.10D, 0.15D, 0.20D, 0.25D, 0.30D};
    private static final double[] RETURN_TO_LONG_ROAD_BREAK_EFFECT = {
            0.60D, 0.70D, 0.80D, 0.90D, 1.00D};
    private static final double[] RETURN_TO_LONG_ROAD_SCORCHING_VULNERABILITY = {
            0.30D, 0.40D, 0.50D, 0.60D, 0.70D};
    private static final double[] REFORGED_REMEMBRANCE_EFFECT_HIT = {
            0.40D, 0.45D, 0.50D, 0.55D, 0.60D};
    private static final double[] REFORGED_REMEMBRANCE_ATTACK_PER_STACK = {
            0.10D, 0.125D, 0.15D, 0.175D, 0.20D};
    private static final double[] REFORGED_REMEMBRANCE_DAMAGE_PER_STACK = {
            0.08D, 0.10D, 0.12D, 0.14D, 0.16D};
    private static final double[] OCEAN_WHY_SINGS_EFFECT_HIT = {
            0.40D, 0.45D, 0.50D, 0.55D, 0.60D};
    private static final double[] OCEAN_WHY_SINGS_ATTACK = {
            0.25D, 0.30D, 0.35D, 0.40D, 0.55D};
    private static final double[] OCEAN_WHY_SINGS_DOT_ATTACK = {
            0.60D, 0.70D, 0.80D, 0.90D, 1.00D};
    private static final double[] DO_NOT_FORGET_HER_FLAME_BREAK_EFFECT = {
            0.60D, 0.75D, 0.90D, 1.05D, 1.20D};
    private static final double[] DO_NOT_FORGET_HER_FLAME_DAMAGE = {
            0.32D, 0.42D, 0.52D, 0.62D, 0.72D};
    private static final double[] DO_NOT_FORGET_HER_FLAME_ATTACK_WHILE_BURNING = {
            0.40D, 0.50D, 0.60D, 0.70D, 0.80D};
    private static final double[] DO_NOT_FORGET_HER_FLAME_CRIT_DAMAGE_WHILE_BURNING = {
            0.10D, 0.125D, 0.15D, 0.175D, 0.20D};
    private static final double[] NEW_FLESH_OF_INFERNO_MAX_HEALTH = {
            0.30D, 0.375D, 0.45D, 0.525D, 0.60D};
    private static final double[] NEW_FLESH_OF_INFERNO_CRIT_DAMAGE_TAKEN = {
            0.30D, 0.375D, 0.45D, 0.525D, 0.60D};

    private static final double[] CRIT_RATE = {0.18D, 0.21D, 0.24D, 0.27D, 0.30D};
    private static final double[] ATTACK_PER_STACK = {0.06D, 0.10D, 0.13D, 0.17D, 0.20D};
    private static final double[] CRIT_DAMAGE_PER_STACK = {
            0.12D, 0.17D, 0.21D, 0.26D, 0.30D};
    private static final double[] I_WILL_HUNT_CRIT_RATE = {
            0.15D, 0.175D, 0.20D, 0.225D, 0.25D};
    private static final double[] I_WILL_HUNT_CRIT_DAMAGE = {
            0.10D, 0.15D, 0.20D, 0.25D, 0.30D};
    private static final double[] I_WILL_HUNT_DAMAGE_PER_STACK = {
            0.30D, 0.34D, 0.38D, 0.42D, 0.46D};
    private static final double[] WORRISOME_BLISSFUL_CRIT_RATE = {
            0.18D, 0.21D, 0.24D, 0.27D, 0.30D};
    private static final double[] WORRISOME_BLISSFUL_ATTACK_DAMAGE = {
            0.30D, 0.35D, 0.40D, 0.45D, 0.50D};
    private static final double[] WORRISOME_BLISSFUL_CRIT_DAMAGE_PER_STACK = {
            0.12D, 0.14D, 0.16D, 0.18D, 0.20D};
    private static final double[] SLEEP_LIKE_THE_DEAD_CRIT_DAMAGE = {
            0.30D, 0.35D, 0.40D, 0.45D, 0.50D};
    private static final double[] SLEEP_LIKE_THE_DEAD_ATTACK_DAMAGE = {
            0.10D, 0.125D, 0.15D, 0.175D, 0.20D};
    private static final double[] BEAUTIFUL_DREAM_CRIT_RATE = {
            0.36D, 0.42D, 0.48D, 0.54D, 0.60D};
    private static final double[] PURE_THOUGHT_BAPTISM_CRIT_DAMAGE = {
            0.20D, 0.23D, 0.26D, 0.29D, 0.32D};
    private static final double[] THOUGHT_TRAINING_CRIT_DAMAGE_PER_STACK = {
            0.08D, 0.09D, 0.10D, 0.11D, 0.12D};
    private static final double[] DEBATE_DAMAGE = {
            0.36D, 0.39D, 0.42D, 0.45D, 0.48D};
    private static final double[] DEBATE_ATTACK_DAMAGE = {
            0.14D, 0.18D, 0.22D, 0.26D, 0.30D};
    private static final double[] RANGER_CRIT_RATE = {
            0.16D, 0.20D, 0.24D, 0.28D, 0.32D};
    private static final double[] RANGER_ATTACK_DAMAGE = {
            0.40D, 0.45D, 0.50D, 0.55D, 0.60D};
    private static final double[] RANGER_ATTACK_PER_STACK = {
            0.10D, 0.12D, 0.15D, 0.17D, 0.20D};
    private static final double[] PAINFUL_VOYAGE_BREAK_EFFECT = {
            0.60D, 0.70D, 0.80D, 0.90D, 1.00D};
    private static final double[] PAINFUL_VOYAGE_BREAK_DAMAGE = {
            0.30D, 0.40D, 0.50D, 0.60D, 0.70D};
    private static final double[] PAINFUL_VOYAGE_ATTACK_DAMAGE = {
            0.30D, 0.40D, 0.50D, 0.60D, 0.70D};
    private static final double[] FINALE_OF_A_LIE_CRIT_RATE = {
            0.18D, 0.21D, 0.24D, 0.27D, 0.30D};
    private static final double[] SHADOW_DEVOUR_ATTACK_DAMAGE = {
            0.40D, 0.50D, 0.60D, 0.70D, 0.80D};
    private static final double[] SHADOW_DEVOUR_DAMAGE = {
            0.20D, 0.225D, 0.25D, 0.275D, 0.30D};
    private static final double[] SOMETHING_IRREPLACEABLE_ATTACK_DAMAGE = {
            0.30D, 0.34D, 0.38D, 0.40D, 0.44D};
    private static final double[] FAMILY_HEALING = {
            0.08D, 0.09D, 0.10D, 0.11D, 0.12D};
    private static final double[] FAMILY_DAMAGE = {
            0.24D, 0.28D, 0.32D, 0.36D, 0.40D};
    private static final double[] BRIGHTER_THAN_THE_SUN_CRIT_RATE = {
            0.18D, 0.21D, 0.24D, 0.27D, 0.30D};
    private static final double[] DRAGON_ROAR_ATTACK_DAMAGE = {
            0.18D, 0.21D, 0.24D, 0.27D, 0.30D};
    private static final double[] DRAGON_ROAR_CRIT_DAMAGE = {
            0.10D, 0.15D, 0.20D, 0.25D, 0.30D};
    private static final double[] DANCE_AT_SUNSET_CRIT_DAMAGE = {
            0.36D, 0.42D, 0.48D, 0.54D, 0.60D};
    private static final double[] FIRE_DANCE_DAMAGE = {
            0.36D, 0.42D, 0.48D, 0.54D, 0.60D};
    private static final double[] NO_RETREAT_CRIT_RATE = {
            0.18D, 0.21D, 0.24D, 0.27D, 0.30D};
    private static final double[] NO_RETREAT_MAX_HEALTH = {
            0.18D, 0.21D, 0.24D, 0.27D, 0.30D};
    private static final double[] NO_RETREAT_DAMAGE = {
            0.24D, 0.28D, 0.32D, 0.36D, 0.40D};
    private static final double[] MOON_ECLIPSE_CRIT_DAMAGE = {
            0.35D, 0.40D, 0.45D, 0.50D, 0.55D};
    private static final double[] MOON_ECLIPSE_DAMAGE = {
            0.14D, 0.165D, 0.19D, 0.215D, 0.24D};
    private static final double[] MOON_ECLIPSE_FULL_ATTACK = {
            0.12D, 0.14D, 0.16D, 0.18D, 0.20D};
    private static final double[] NO_REWARD_CROWNING_CRIT_DAMAGE = {
            0.36D, 0.45D, 0.54D, 0.63D, 0.72D};
    private static final double[] KNIGHT_KING_ATTACK = {
            0.40D, 0.50D, 0.60D, 0.70D, 0.80D};
    private static final double[] BLOOD_FIRE_MAX_HEALTH = {
            0.18D, 0.21D, 0.24D, 0.27D, 0.30D};
    private static final double[] BLOOD_FIRE_HEALING = {
            0.20D, 0.25D, 0.30D, 0.35D, 0.40D};
    private static final double[] STRIFE_HEALTH_COST = {
            0.10D, 0.125D, 0.15D, 0.175D, 0.20D};
    private static final double[] STRIFE_DAMAGE = {
            0.30D, 0.35D, 0.40D, 0.45D, 0.50D};
    private static final double[] METAMORPHOSIS_BREAK_EFFECT = {
            0.80D, 0.90D, 1.00D, 1.10D, 1.20D};
    private static final double[] METAMORPHOSIS_DAMAGE = {
            0.60D, 0.70D, 0.80D, 0.90D, 1.00D};
    private static final double[] METAMORPHOSIS_ATTACK = {
            0.20D, 0.30D, 0.40D, 0.50D, 0.60D};
    private static final double[] DAWN_BURNS_ATTACK = {
            0.12D, 0.14D, 0.16D, 0.18D, 0.20D};
    private static final double[] DAWN_BURNS_DAMAGE = {
            0.18D, 0.225D, 0.27D, 0.315D, 0.36D};
    private static final double[] BLAZING_SUN_DAMAGE = {
            0.60D, 0.78D, 0.96D, 1.14D, 1.32D};
    private static final double[] WHAT_YOU_SEE_IS_ME_ATTACK = {
            0.18D, 0.21D, 0.24D, 0.27D, 0.30D};
    private static final double[] WHAT_YOU_SEE_IS_ME_NEXT_DAMAGE = {
            0.20D, 0.25D, 0.30D, 0.35D, 0.40D};
    private static final double[] KINGLY_ENTERTAINMENT_CRIT_DAMAGE = {
            0.24D, 0.30D, 0.36D, 0.42D, 0.48D};
    private static final double[] METEOR_ATTACK_PER_ENEMY = {
            0.09D, 0.105D, 0.12D, 0.135D, 0.15D};
    private static final double[] METEOR_DAMAGE = {
            0.40D, 0.45D, 0.55D, 0.60D, 0.65D};
    private static final double[] KNIGHTLY_COURTESY_ATTACK = {
            0.36D, 0.42D, 0.48D, 0.54D, 0.60D};
    private static final double[] MOMENT_OF_GLORY_CRIT_DAMAGE = {
            0.36D, 0.42D, 0.48D, 0.54D, 0.60D};
    private static final double[] BEFORE_DAWN_CRIT_DAMAGE = {
            0.36D, 0.42D, 0.48D, 0.54D, 0.60D};
    private static final double[] BEFORE_DAWN_ATTACK = {
            0.18D, 0.21D, 0.24D, 0.27D, 0.30D};
    private static final double[] DREAM_BODY_DAMAGE = {
            0.50D, 0.60D, 0.70D, 0.80D, 0.90D};
    private static final double[] PRICE_OF_PEACE_CRIT_RATE = {
            0.16D, 0.19D, 0.22D, 0.25D, 0.28D};
    private static final double[] PRICE_OF_PEACE_DAMAGE_PER_CRIT_STACK = {
            0.12D, 0.14D, 0.16D, 0.18D, 0.20D};
    private static final double[] PROMISE_ATTACK = {
            0.20D, 0.24D, 0.28D, 0.32D, 0.36D};
    private static final double[] TOWARDS_UNANSWERABLE_CRIT_RATE = {
            0.15D, 0.175D, 0.20D, 0.225D, 0.25D};
    private static final double[] DECONSTRUCTION_DAMAGE = {
            0.60D, 0.70D, 0.80D, 0.90D, 1.00D};
    private static final double[] DECONSTRUCTION_ATTACK = {
            0.20D, 0.25D, 0.30D, 0.35D, 0.40D};
    private static final double[] NINJA_SCROLL_BREAK_EFFECT = {
            0.60D, 0.70D, 0.80D, 0.90D, 1.00D};
    private static final double[] NINJA_SCROLL_CRIT_DAMAGE = {
            0.20D, 0.25D, 0.30D, 0.35D, 0.40D};
    private static final double[] THUNDER_ESCAPE_ATTACK = {
            0.50D, 0.60D, 0.70D, 0.80D, 0.90D};
    private static final double[] LIFE_AS_A_LIGHT_CRIT_DAMAGE = {
            0.40D, 0.425D, 0.45D, 0.475D, 0.50D};
    private static final double[] ALCHEMY_DAMAGE = {
            0.60D, 0.70D, 0.80D, 0.90D, 1.00D};
    private static final double[] ALCHEMY_ATTACK = {
            0.20D, 0.225D, 0.25D, 0.275D, 0.30D};
    private static final double[] A_STAR_ILLUMINATES_CRIT_DAMAGE = {
            0.30D, 0.40D, 0.50D, 0.60D, 0.70D};
    private static final double[] DEPARTURE_DAMAGE = {
            0.20D, 0.25D, 0.30D, 0.35D, 0.40D};
    private static final double[] DEPARTURE_ATTACK = {
            0.10D, 0.125D, 0.15D, 0.175D, 0.20D};
    private static final double[] SPARKLE_QUIETLY_SHINES_CRIT_RATE = {
            0.18D, 0.21D, 0.24D, 0.27D, 0.30D};
    private static final double[] SHINING_CROWN_ATTACK = {
            0.30D, 0.35D, 0.40D, 0.45D, 0.50D};
    private static final double[] SHINING_CROWN_DAMAGE = {
            0.72D, 0.84D, 0.96D, 1.08D, 1.20D};
    private static final double[] INHERITANCE_ATTACK = {
            0.20D, 0.25D, 0.30D, 0.35D, 0.40D};
    private static final double[] INHERITANCE_DAMAGE = {
            0.30D, 0.35D, 0.40D, 0.45D, 0.50D};
    private static final double[] NIGHT_FLOWING_COLORS_CHANT_ATTACK = {
            0.10D, 0.125D, 0.15D, 0.175D, 0.20D};
    private static final double[] NIGHT_FLOWING_COLORS_SPLENDOR_ATTACK = {
            0.50D, 0.60D, 0.70D, 0.80D, 0.90D};
    private static final double[] NIGHT_FLOWING_COLORS_SPLENDOR_DAMAGE = {
            0.24D, 0.28D, 0.32D, 0.36D, 0.40D};
    private static final double[] MIRROR_BREAK_EFFECT = {
            1.10D, 1.20D, 1.30D, 1.40D, 1.50D};
    private static final double[] PLUM_FRAGRANCE_ATTACK_PER_STACK = {
            0.20D, 0.30D, 0.40D, 0.50D, 0.60D};
    private static final double[] PLUM_FRAGRANCE_DAMAGE_PER_STACK = {
            0.20D, 0.30D, 0.40D, 0.50D, 0.60D};

    private StarRailLightConeService() {
    }

    public static double critRate(int superimposition) {
        return CRIT_RATE[boundedSuperimposition(superimposition) - 1];
    }

    public static double attackPerStack(int superimposition) {
        return ATTACK_PER_STACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double critDamagePerStack(int superimposition) {
        return CRIT_DAMAGE_PER_STACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double pursuitCritRate(int superimposition) {
        return I_WILL_HUNT_CRIT_RATE[boundedSuperimposition(superimposition) - 1];
    }

    public static double pursuitCritDamage(int superimposition) {
        return I_WILL_HUNT_CRIT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double pursuitDamagePerStack(int superimposition) {
        return I_WILL_HUNT_DAMAGE_PER_STACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double worrisomeBlissfulCritRate(int superimposition) {
        return WORRISOME_BLISSFUL_CRIT_RATE[boundedSuperimposition(superimposition) - 1];
    }

    public static double worrisomeBlissfulAttackDamage(int superimposition) {
        return WORRISOME_BLISSFUL_ATTACK_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double worrisomeBlissfulCritDamagePerStack(int superimposition) {
        return WORRISOME_BLISSFUL_CRIT_DAMAGE_PER_STACK[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double sleepLikeTheDeadCritDamage(int superimposition) {
        return SLEEP_LIKE_THE_DEAD_CRIT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double sleepLikeTheDeadAttackDamage(int superimposition) {
        return SLEEP_LIKE_THE_DEAD_ATTACK_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double beautifulDreamCritRate(int superimposition) {
        return BEAUTIFUL_DREAM_CRIT_RATE[boundedSuperimposition(superimposition) - 1];
    }

    public static double pureThoughtBaptismCritDamage(int superimposition) {
        return PURE_THOUGHT_BAPTISM_CRIT_DAMAGE[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double thoughtTrainingCritDamagePerStack(int superimposition) {
        return THOUGHT_TRAINING_CRIT_DAMAGE_PER_STACK[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double debateDamage(int superimposition) {
        return DEBATE_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double debateAttackDamage(int superimposition) {
        return DEBATE_ATTACK_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double rangerCritRate(int superimposition) {
        return RANGER_CRIT_RATE[boundedSuperimposition(superimposition) - 1];
    }

    public static double rangerAttackDamage(int superimposition) {
        return RANGER_ATTACK_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double rangerAttackPerStack(int superimposition) {
        return RANGER_ATTACK_PER_STACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double painfulVoyageBreakEffect(int superimposition) {
        return PAINFUL_VOYAGE_BREAK_EFFECT[boundedSuperimposition(superimposition) - 1];
    }

    public static double painfulVoyageBreakDamage(int superimposition) {
        return PAINFUL_VOYAGE_BREAK_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double painfulVoyageAttackDamage(int superimposition) {
        return PAINFUL_VOYAGE_ATTACK_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double finaleOfALieCritRate(int superimposition) {
        return FINALE_OF_A_LIE_CRIT_RATE[boundedSuperimposition(superimposition) - 1];
    }

    public static double shadowDevourAttackDamage(int superimposition) {
        return SHADOW_DEVOUR_ATTACK_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double shadowDevourDamage(int superimposition) {
        return SHADOW_DEVOUR_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double somethingIrreplaceableAttackDamage(int superimposition) {
        return SOMETHING_IRREPLACEABLE_ATTACK_DAMAGE[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double familyHealing(int superimposition) {
        return FAMILY_HEALING[boundedSuperimposition(superimposition) - 1];
    }

    public static double familyDamage(int superimposition) {
        return FAMILY_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double brighterThanTheSunCritRate(int superimposition) {
        return BRIGHTER_THAN_THE_SUN_CRIT_RATE[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double dragonRoarAttackDamage(int superimposition) {
        return DRAGON_ROAR_ATTACK_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double dragonRoarCritDamage(int superimposition) {
        return DRAGON_ROAR_CRIT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double danceAtSunsetCritDamage(int superimposition) {
        return DANCE_AT_SUNSET_CRIT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double fireDanceDamage(int superimposition) {
        return FIRE_DANCE_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double noRetreatCritRate(int superimposition) {
        return NO_RETREAT_CRIT_RATE[boundedSuperimposition(superimposition) - 1];
    }

    public static double noRetreatMaxHealth(int superimposition) {
        return NO_RETREAT_MAX_HEALTH[boundedSuperimposition(superimposition) - 1];
    }

    public static double noRetreatDamage(int superimposition) {
        return NO_RETREAT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double moonEclipseCritDamage(int superimposition) {
        return MOON_ECLIPSE_CRIT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double moonEclipseDamage(int superimposition) {
        return MOON_ECLIPSE_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double moonEclipseFullAttack(int superimposition) {
        return MOON_ECLIPSE_FULL_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double noRewardCrowningCritDamage(int superimposition) {
        return NO_REWARD_CROWNING_CRIT_DAMAGE[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double knightKingAttack(int superimposition) {
        return KNIGHT_KING_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double bloodFireMaxHealth(int superimposition) {
        return BLOOD_FIRE_MAX_HEALTH[boundedSuperimposition(superimposition) - 1];
    }

    public static double bloodFireHealing(int superimposition) {
        return BLOOD_FIRE_HEALING[boundedSuperimposition(superimposition) - 1];
    }

    public static double strifeHealthCost(int superimposition) {
        return STRIFE_HEALTH_COST[boundedSuperimposition(superimposition) - 1];
    }

    public static double strifeDamage(int superimposition) {
        return STRIFE_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double metamorphosisBreakEffect(int superimposition) {
        return METAMORPHOSIS_BREAK_EFFECT[boundedSuperimposition(superimposition) - 1];
    }

    public static double metamorphosisDamage(int superimposition) {
        return METAMORPHOSIS_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double metamorphosisAttack(int superimposition) {
        return METAMORPHOSIS_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double dawnBurnsAttack(int superimposition) {
        return DAWN_BURNS_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double dawnBurnsDamage(int superimposition) {
        return DAWN_BURNS_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double blazingSunDamage(int superimposition) {
        return BLAZING_SUN_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double whatYouSeeIsMeAttack(int superimposition) {
        return WHAT_YOU_SEE_IS_ME_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double whatYouSeeIsMeNextDamage(int superimposition) {
        return WHAT_YOU_SEE_IS_ME_NEXT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double kinglyEntertainmentCritDamage(int superimposition) {
        return KINGLY_ENTERTAINMENT_CRIT_DAMAGE[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double meteorAttackPerEnemy(int superimposition) {
        return METEOR_ATTACK_PER_ENEMY[boundedSuperimposition(superimposition) - 1];
    }

    public static double meteorDamage(int superimposition) {
        return METEOR_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double knightlyCourtesyAttack(int superimposition) {
        return KNIGHTLY_COURTESY_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double momentOfGloryCritDamage(int superimposition) {
        return MOMENT_OF_GLORY_CRIT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double beforeDawnCritDamage(int superimposition) {
        return BEFORE_DAWN_CRIT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double beforeDawnAttack(int superimposition) {
        return BEFORE_DAWN_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double dreamBodyDamage(int superimposition) {
        return DREAM_BODY_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double priceOfPeaceCritRate(int superimposition) {
        return PRICE_OF_PEACE_CRIT_RATE[boundedSuperimposition(superimposition) - 1];
    }

    public static double priceOfPeaceDamagePerCritStack(int superimposition) {
        return PRICE_OF_PEACE_DAMAGE_PER_CRIT_STACK[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double promiseAttack(int superimposition) {
        return PROMISE_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double towardsUnanswerableCritRate(int superimposition) {
        return TOWARDS_UNANSWERABLE_CRIT_RATE[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double deconstructionDamage(int superimposition) {
        return DECONSTRUCTION_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double deconstructionAttack(int superimposition) {
        return DECONSTRUCTION_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double ninjaScrollBreakEffect(int superimposition) {
        return NINJA_SCROLL_BREAK_EFFECT[boundedSuperimposition(superimposition) - 1];
    }

    public static double ninjaScrollCritDamage(int superimposition) {
        return NINJA_SCROLL_CRIT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double thunderEscapeAttack(int superimposition) {
        return THUNDER_ESCAPE_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double lifeAsALightCritDamage(int superimposition) {
        return LIFE_AS_A_LIGHT_CRIT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double alchemyDamage(int superimposition) {
        return ALCHEMY_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double alchemyAttack(int superimposition) {
        return ALCHEMY_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double aStarIlluminatesCritDamage(int superimposition) {
        return A_STAR_ILLUMINATES_CRIT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double departureDamage(int superimposition) {
        return DEPARTURE_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double departureAttack(int superimposition) {
        return DEPARTURE_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double sparkleQuietlyShinesCritRate(int superimposition) {
        return SPARKLE_QUIETLY_SHINES_CRIT_RATE[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double shiningCrownAttack(int superimposition) {
        return SHINING_CROWN_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double shiningCrownDamage(int superimposition) {
        return SHINING_CROWN_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double inheritanceAttack(int superimposition) {
        return INHERITANCE_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double inheritanceDamage(int superimposition) {
        return INHERITANCE_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double mirrorBreakEffect(int superimposition) {
        return MIRROR_BREAK_EFFECT[boundedSuperimposition(superimposition) - 1];
    }

    public static double plumFragranceAttack(int superimposition) {
        return PLUM_FRAGRANCE_ATTACK_PER_STACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double plumFragranceDamage(int superimposition) {
        return PLUM_FRAGRANCE_DAMAGE_PER_STACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double nightFlowingColorsChantAttack(int superimposition) {
        return NIGHT_FLOWING_COLORS_CHANT_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double nightFlowingColorsSplendorAttack(int superimposition) {
        return NIGHT_FLOWING_COLORS_SPLENDOR_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double nightFlowingColorsSplendorDamage(int superimposition) {
        return NIGHT_FLOWING_COLORS_SPLENDOR_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double gameOfCosmicWorldsCritDamage(int superimposition) {
        return GAME_OF_COSMIC_WORLDS_CRIT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double gameOfCosmicWorldsMaskCritRate(int superimposition) {
        return GAME_OF_COSMIC_WORLDS_MASK_CRIT_RATE[boundedSuperimposition(superimposition) - 1];
    }

    public static double gameOfCosmicWorldsMaskCritDamage(int superimposition) {
        return GAME_OF_COSMIC_WORLDS_MASK_CRIT_DAMAGE[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double ifTimeWereAFlowerCritDamage(int superimposition) {
        return IF_TIME_WERE_A_FLOWER_CRIT_DAMAGE[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double ifTimeWereAFlowerCritRate(int superimposition) {
        return IF_TIME_WERE_A_FLOWER_CRIT_RATE_AURA[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double ifTimeWereAFlowerCritDamageAura(int superimposition) {
        return IF_TIME_WERE_A_FLOWER_CRIT_DAMAGE_AURA[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double goldenBloodAttack(int superimposition) {
        return GOLDEN_BLOOD_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double goldenBloodLawAttack(int superimposition) {
        return GOLDEN_BLOOD_LAW_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double goldenBloodLawDamage(int superimposition) {
        return GOLDEN_BLOOD_LAW_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double inTheNameDamageVsDebuffed(int superimposition) {
        return IN_THE_NAME_DAMAGE_VS_DEBUFFED[boundedSuperimposition(superimposition) - 1];
    }

    public static double inTheNameWillEffectHit(int superimposition) {
        return IN_THE_NAME_WILL_EFFECT_HIT[boundedSuperimposition(superimposition) - 1];
    }

    public static double inTheNameWillAttack(int superimposition) {
        return IN_THE_NAME_WILL_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double psalmAttackPerStack(int superimposition) {
        return PSALM_ATTACK_PER_STACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double psalmDamagePerStack(int superimposition) {
        return PSALM_DAMAGE_PER_STACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double shoreCritDamage(int superimposition) {
        return SHORE_CRIT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double shoreAttackPerStack(int superimposition) {
        return SHORE_ATTACK_PER_STACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double shoreDamagePerStack(int superimposition) {
        return SHORE_DAMAGE_PER_STACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double thousandSpringsEffectHit(int superimposition) {
        return THOUSAND_SPRINGS_EFFECT_HIT[boundedSuperimposition(superimposition) - 1];
    }

    public static double thousandSpringsStrippedDamage(int superimposition) {
        return THOUSAND_SPRINGS_STRIPPED_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double thousandSpringsCorneredExtraDamage(int superimposition) {
        return THOUSAND_SPRINGS_CORNERED_EXTRA_DAMAGE[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double onlyWaitAttack(int superimposition) {
        return ONLY_WAIT_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double onlyWaitDamage(int superimposition) {
        return ONLY_WAIT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double rainNeverStopsEffectHit(int superimposition) {
        return RAIN_NEVER_STOPS_EFFECT_HIT[boundedSuperimposition(superimposition) - 1];
    }

    public static double rainNeverStopsAttack(int superimposition) {
        return RAIN_NEVER_STOPS_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double rainNeverStopsCritRate(int superimposition) {
        return RAIN_NEVER_STOPS_CRIT_RATE[boundedSuperimposition(superimposition) - 1];
    }

    public static double rainNeverStopsVulnerability(int superimposition) {
        return RAIN_NEVER_STOPS_VULNERABILITY[boundedSuperimposition(superimposition) - 1];
    }

    public static double liesInTheWindAttack(int superimposition) {
        return LIES_IN_THE_WIND_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double liesInTheWindBewilderedVulnerability(int superimposition) {
        return LIES_IN_THE_WIND_BEWILDERED_VULNERABILITY[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double liesInTheWindStolenVulnerability(int superimposition) {
        return LIES_IN_THE_WIND_STOLEN_VULNERABILITY[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double returnToLongRoadAttack(int superimposition) {
        return RETURN_TO_LONG_ROAD_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double returnToLongRoadBreakEffect(int superimposition) {
        return RETURN_TO_LONG_ROAD_BREAK_EFFECT[boundedSuperimposition(superimposition) - 1];
    }

    public static double returnToLongRoadScorchingVulnerability(int superimposition) {
        return RETURN_TO_LONG_ROAD_SCORCHING_VULNERABILITY[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double reforgedRemembranceEffectHit(int superimposition) {
        return REFORGED_REMEMBRANCE_EFFECT_HIT[boundedSuperimposition(superimposition) - 1];
    }

    public static double reforgedRemembranceAttackPerStack(int superimposition) {
        return REFORGED_REMEMBRANCE_ATTACK_PER_STACK[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double reforgedRemembranceDamagePerStack(int superimposition) {
        return REFORGED_REMEMBRANCE_DAMAGE_PER_STACK[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double oceanWhySingsEffectHit(int superimposition) {
        return OCEAN_WHY_SINGS_EFFECT_HIT[boundedSuperimposition(superimposition) - 1];
    }

    public static double oceanWhySingsAttack(int superimposition) {
        return OCEAN_WHY_SINGS_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double oceanWhySingsDotAttack(int superimposition) {
        return OCEAN_WHY_SINGS_DOT_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double doNotForgetHerFlameBreakEffect(int superimposition) {
        return DO_NOT_FORGET_HER_FLAME_BREAK_EFFECT[boundedSuperimposition(superimposition) - 1];
    }

    public static double doNotForgetHerFlameDamage(int superimposition) {
        return DO_NOT_FORGET_HER_FLAME_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double doNotForgetHerFlameAttackWhileBurning(int superimposition) {
        return DO_NOT_FORGET_HER_FLAME_ATTACK_WHILE_BURNING[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double doNotForgetHerFlameCritDamageWhileBurning(int superimposition) {
        return DO_NOT_FORGET_HER_FLAME_CRIT_DAMAGE_WHILE_BURNING[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double newFleshOfInfernoMaxHealth(int superimposition) {
        return NEW_FLESH_OF_INFERNO_MAX_HEALTH[boundedSuperimposition(superimposition) - 1];
    }

    public static double newFleshOfInfernoCritDamageTaken(int superimposition) {
        return NEW_FLESH_OF_INFERNO_CRIT_DAMAGE_TAKEN[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double onlyWaitDotAttack(int superimposition) {
        return ONLY_WAIT_DOT_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double momentOfVictoryArmor(int superimposition) {
        return MOMENT_OF_VICTORY_ARMOR[boundedSuperimposition(superimposition) - 1];
    }

    public static double winterShieldBonus(int superimposition) {
        return WINTER_SHIELD_BONUS[boundedSuperimposition(superimposition) - 1];
    }

    public static double closedEyesArmor(int superimposition) {
        return CLOSED_EYES_ARMOR[boundedSuperimposition(superimposition) - 1];
    }

    public static double closedEyesDamage(int superimposition) {
        return CLOSED_EYES_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double closedEyesMissingHealthHeal(int superimposition) {
        return CLOSED_EYES_MISSING_HEALTH_HEAL[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double thoughRiversArmor(int superimposition) {
        return THOUGH_RIVERS_ARMOR[boundedSuperimposition(superimposition) - 1];
    }

    public static double thoughRiversHealFromArmor(int superimposition) {
        return THOUGH_RIVERS_HEAL_FROM_ARMOR[boundedSuperimposition(superimposition) - 1];
    }

    public static double thoughRiversGarrisonDamage(int superimposition) {
        return THOUGH_RIVERS_GARRISON_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double fateNeverFairArmor(int superimposition) {
        return FATE_NEVER_FAIR_ARMOR[boundedSuperimposition(superimposition) - 1];
    }

    public static double fateNeverFairCritDamage(int superimposition) {
        return FATE_NEVER_FAIR_CRIT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double fateNeverFairChipsDamage(int superimposition) {
        return FATE_NEVER_FAIR_CHIPS_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double echoesOfTheCoffinAttack(int superimposition) {
        return ECHOES_OF_THE_COFFIN_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double echoesOfTheCoffinHealth(int superimposition) {
        return ECHOES_OF_THE_COFFIN_HEALTH[boundedSuperimposition(superimposition) - 1];
    }

    public static double echoesOfTheCoffinBonusDamage(int superimposition) {
        return ECHOES_OF_THE_COFFIN_BONUS_DAMAGE[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double timeWaitsForNoOneMaxHealth(int superimposition) {
        return TIME_WAITS_FOR_NO_ONE_MAX_HEALTH[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double timeWaitsForNoOneHealing(int superimposition) {
        return TIME_WAITS_FOR_NO_ONE_HEALING[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double timeWaitsForNoOneDamage(int superimposition) {
        return TIME_WAITS_FOR_NO_ONE_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double mayRainbowMaxHealth(int superimposition) {
        return MAY_RAINBOW_MAX_HEALTH[boundedSuperimposition(superimposition) - 1];
    }

    public static double mayRainbowHealing(int superimposition) {
        return MAY_RAINBOW_HEALING[boundedSuperimposition(superimposition) - 1];
    }

    public static double mayRainbowHealthCost(int superimposition) {
        return MAY_RAINBOW_HEALTH_COST[boundedSuperimposition(superimposition) - 1];
    }

    public static double mayRainbowDamageMultiplier(int superimposition) {
        return MAY_RAINBOW_DAMAGE_MULTIPLIER[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double nightOfFrightMaxHealth(int superimposition) {
        return NIGHT_OF_FRIGHT_MAX_HEALTH[boundedSuperimposition(superimposition) - 1];
    }

    public static double nightOfFrightHealing(int superimposition) {
        return NIGHT_OF_FRIGHT_HEALING[boundedSuperimposition(superimposition) - 1];
    }

    public static double deepBreathAttackPerStack(int superimposition) {
        return DEEP_BREATH_ATTACK_PER_STACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double weaveTimeIntoGoldAttack(int superimposition) {
        return WEAVE_TIME_INTO_GOLD_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double weaveTimeIntoGoldCritDamage(int superimposition) {
        return BROCADE_CRIT_DAMAGE_PER_STACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double weaveTimeIntoGoldDamagePerStack(int superimposition) {
        return BROCADE_DAMAGE_PER_STACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double farewellMaxHealth(int superimposition) {
        return FAREWELL_MAX_HEALTH[boundedSuperimposition(superimposition) - 1];
    }

    public static double farewellAttack(int superimposition) {
        return FAREWELL_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double netherBloomDamagePerStack(int superimposition) {
        return NETHER_BLOOM_DAMAGE_PER_STACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double loveIsEternalAttack(int superimposition) {
        return LOVE_IS_ETERNAL_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double loveIsEternalBlankDamage(int superimposition) {
        return LOVE_IS_ETERNAL_BLANK_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double loveIsEternalVerseCritDamage(int superimposition) {
        return LOVE_IS_ETERNAL_VERSE_CRIT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double loveIsEternalSynergy(int superimposition) {
        return LOVE_IS_ETERNAL_SYNERGY[boundedSuperimposition(superimposition) - 1];
    }

    public static double starlightForLongNightsMaxHealth(int superimposition) {
        return STARLIGHT_FOR_LONG_NIGHTS_MAX_HEALTH[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double starlightForLongNightsAttack(int superimposition) {
        return STARLIGHT_FOR_LONG_NIGHTS_ATTACK[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double welcomeToGalacticCityAttack(int superimposition) {
        return WELCOME_TO_GALACTIC_CITY_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double welcomeToGalacticCityDamage(int superimposition) {
        return WELCOME_TO_GALACTIC_CITY_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double winningStreakCritDamagePerStack(int superimposition) {
        return WINNING_STREAK_CRIT_DAMAGE_PER_STACK[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double meetInTheNextFlowerSeasonAttack(int superimposition) {
        return MEET_IN_THE_NEXT_FLOWER_SEASON_ATTACK[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double meetInTheNextFlowerSeasonCritDamage(int superimposition) {
        return MEET_IN_THE_NEXT_FLOWER_SEASON_CRIT_DAMAGE[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double daydreamDamagePerStack(int superimposition) {
        return DAYDREAM_DAMAGE_PER_STACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double whenSheDecidesToSeeAttack(int superimposition) {
        return WHEN_SHE_DECIDES_TO_SEE_ATTACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double bestFortuneCritRate(int superimposition) {
        return BEST_FORTUNE_CRIT_RATE[boundedSuperimposition(superimposition) - 1];
    }

    public static double bestFortuneCritDamage(int superimposition) {
        return BEST_FORTUNE_CRIT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double bestFortuneDamage(int superimposition) {
        return BEST_FORTUNE_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double flowerWorldCritDamage(int superimposition) {
        return FLOWER_WORLD_CRIT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double pushStreamDamagePerStack(int superimposition) {
        return PUSH_STREAM_DAMAGE_PER_STACK[boundedSuperimposition(superimposition) - 1];
    }

    public static double pushStreamCritDamagePerStack(int superimposition) {
        return PUSH_STREAM_CRIT_DAMAGE_PER_STACK[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double daydreamDamageMultiplier(ServerPlayer player) {
        ItemStack stack = getEquippedMeetInTheNextFlowerSeason(player);
        MobEffectInstance effect = player.getEffect(StarRailSimMod.DAYDREAM.get());
        if (stack == null || !isElationPath(player) || effect == null) {
            return 1.0D;
        }
        int stacks = Math.max(0, Math.min(DAYDREAM_MAX_STACKS,
                effect.getAmplifier() + 1));
        int superimposition = boundedSuperimposition(player.getPersistentData().getInt(
                DAYDREAM_SUPERIMPOSITION_TAG));
        return 1.0D + daydreamDamagePerStack(superimposition) * stacks;
    }

    public static double bestFortuneDamageMultiplier(ServerPlayer player) {
        ItemStack stack = getEquippedWhenSheDecidesToSee(player);
        MobEffectInstance effect = player.getEffect(StarRailSimMod.BEST_FORTUNE.get());
        if (stack == null || !isElationPath(player) || effect == null
                || !player.getStringUUID().equals(player.getPersistentData().getString(
                        BEST_FORTUNE_OWNER_TAG))) {
            return 1.0D;
        }
        return 1.0D + bestFortuneDamage(Math.max(1, Math.min(5,
                player.getPersistentData().getInt(BEST_FORTUNE_SUPERIMPOSITION_TAG))));
    }

    public static double pushStreamDamageMultiplier(ServerPlayer player) {
        ItemStack stack = getEquippedFlowerWorldMesmerizingEyes(player);
        MobEffectInstance effect = player.getEffect(StarRailSimMod.PUSH_STREAM.get());
        if (stack == null || !isElationPath(player) || effect == null) {
            return 1.0D;
        }
        int stacks = Math.max(0, Math.min(PUSH_STREAM_MAX_STACKS,
                effect.getAmplifier() + 1));
        int superimposition = boundedSuperimposition(player.getPersistentData().getInt(
                PUSH_STREAM_SUPERIMPOSITION_TAG));
        return 1.0D + pushStreamDamagePerStack(superimposition) * stacks;
    }

    public static double starlitNightDamage(int superimposition) {
        return STARLIT_NIGHT_DAMAGE[boundedSuperimposition(superimposition) - 1];
    }

    public static double onlyTheScentRemainsBreakEffect(int superimposition) {
        return ONLY_THE_SCENT_REMAINS_BREAK_EFFECT[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double onlyTheScentRemainsMaxHealth(int superimposition) {
        return ONLY_THE_SCENT_REMAINS_MAX_HEALTH[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double onlyTheScentRemainsVulnerability(int superimposition) {
        return ONLY_THE_SCENT_REMAINS_VULNERABILITY[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double onlyTheScentRemainsExtraVulnerability(int superimposition) {
        return ONLY_THE_SCENT_REMAINS_EXTRA_VULNERABILITY[
                boundedSuperimposition(superimposition) - 1];
    }

    public static double timeWaitsForNoOneRecordedLoss(ServerPlayer player) {
        ItemStack stack = getEquippedTimeWaitsForNoOne(player);
        if (stack == null || !isAbundancePath(player)) {
            return 0.0D;
        }
        return Math.max(0.0D, player.getPersistentData().getDouble(
                TIME_WAITS_FOR_NO_ONE_RECORDED_LOSS_TAG));
    }

    public static double timeWaitsForNoOneBonusDamage(ServerPlayer player) {
        ItemStack stack = getEquippedTimeWaitsForNoOne(player);
        if (stack == null || !isAbundancePath(player)) {
            return 0.0D;
        }
        int level = TimeWaitsForNoOneItem.getSuperimposition(stack);
        return timeWaitsForNoOneRecordedLoss(player) * timeWaitsForNoOneDamage(level);
    }

    /** Records actual health removed after armor and absorption have been handled. */
    public static void recordTimeWaitsForNoOneHealthLoss(ServerPlayer player, float amount) {
        ItemStack stack = getEquippedTimeWaitsForNoOne(player);
        if (stack == null || !isAbundancePath(player) || amount <= 0.0F
                || player.getHealth() <= 0.0F) {
            return;
        }
        double loss = Math.min(player.getHealth(), amount);
        player.getPersistentData().putDouble(TIME_WAITS_FOR_NO_ONE_RECORDED_LOSS_TAG,
                timeWaitsForNoOneRecordedLoss(player) + loss);
    }

    public static int mayRainbowAttackCount(ServerPlayer player) {
        ItemStack stack = getEquippedMayRainbowStayInTheSky(player);
        if (stack == null || !isRemembrancePath(player)) {
            return 0;
        }
        return Math.max(0, Math.min(MAY_RAINBOW_ATTACKS_REQUIRED - 1,
                player.getPersistentData().getInt(MAY_RAINBOW_ATTACK_COUNT_TAG)));
    }

    public static double mayRainbowConsumedHealth(ServerPlayer player) {
        ItemStack stack = getEquippedMayRainbowStayInTheSky(player);
        if (stack == null || !isRemembrancePath(player)) {
            return 0.0D;
        }
        return Math.max(0.0D, player.getPersistentData().getDouble(
                MAY_RAINBOW_CONSUMED_HEALTH_TAG));
    }

    /** Records a landed enemy hit, pays its current-health cost, and queues the fifth-hit burst. */
    public static void onMayRainbowStayInTheSkyAttack(ServerPlayer player,
                                                       LivingEntity target) {
        ItemStack stack = getEquippedMayRainbowStayInTheSky(player);
        if (stack == null || !isRemembrancePath(player) || !(target instanceof Monster)
                || !target.isAlive() || player.getHealth() <= 1.0F) {
            return;
        }
        int level = MayRainbowStayInTheSkyItem.getSuperimposition(stack);
        double intendedCost = player.getHealth() * mayRainbowHealthCost(level);
        double actualCost = Math.min(intendedCost, Math.max(0.0D, player.getHealth() - 1.0D));
        if (actualCost <= 0.0D) {
            return;
        }
        player.setHealth((float) (player.getHealth() - actualCost));

        double totalCost = mayRainbowConsumedHealth(player) + actualCost;
        int attacks = mayRainbowAttackCount(player) + 1;
        if (attacks >= MAY_RAINBOW_ATTACKS_REQUIRED) {
            player.getPersistentData().putDouble(MAY_RAINBOW_PENDING_DAMAGE_TAG,
                    totalCost * mayRainbowDamageMultiplier(level));
            player.getPersistentData().putString(MAY_RAINBOW_PENDING_TARGET_TAG,
                    target.getUUID().toString());
            player.getPersistentData().remove(MAY_RAINBOW_ATTACK_COUNT_TAG);
            player.getPersistentData().remove(MAY_RAINBOW_CONSUMED_HEALTH_TAG);
        } else {
            player.getPersistentData().putInt(MAY_RAINBOW_ATTACK_COUNT_TAG, attacks);
            player.getPersistentData().putDouble(MAY_RAINBOW_CONSUMED_HEALTH_TAG, totalCost);
        }
    }

    /** Grants the stackable Elation buff when an attack is made. */
    public static void onWelcomeToGalacticCityAttack(ServerPlayer player) {
        ItemStack stack = getEquippedWelcomeToGalacticCity(player);
        if (stack == null || !isElationPath(player)) {
            return;
        }
        MobEffectInstance current = player.getEffect(StarRailSimMod.WINNING_STREAK.get());
        int currentStacks = current == null ? 0 : current.getAmplifier() + 1;
        int nextStacks = Math.min(WINNING_STREAK_MAX_STACKS, currentStacks + 1);
        player.getPersistentData().putInt(WINNING_STREAK_SUPERIMPOSITION_TAG,
                WelcomeToGalacticCityItem.getSuperimposition(stack));
        player.addEffect(new MobEffectInstance(StarRailSimMod.WINNING_STREAK.get(),
                WINNING_STREAK_DURATION, nextStacks - 1, false, true, true));
        refresh(player);
    }

    /** Grants one stack of Daydream for each Elation-path attack. */
    public static void onMeetInTheNextFlowerSeasonAttack(ServerPlayer player) {
        ItemStack stack = getEquippedMeetInTheNextFlowerSeason(player);
        if (stack == null || !isElationPath(player)) {
            return;
        }
        MobEffectInstance current = player.getEffect(StarRailSimMod.DAYDREAM.get());
        int currentStacks = current == null ? 0 : current.getAmplifier() + 1;
        int nextStacks = Math.min(DAYDREAM_MAX_STACKS, currentStacks + 1);
        player.getPersistentData().putInt(DAYDREAM_SUPERIMPOSITION_TAG,
                MeetInTheNextFlowerSeasonItem.getSuperimposition(stack));
        player.addEffect(new MobEffectInstance(StarRailSimMod.DAYDREAM.get(),
                DAYDREAM_DURATION, nextStacks - 1, false, true, true));
        refresh(player);
    }

    /** Grants the wearer and nearby players the next-attack fortune aura. */
    public static void onWhenSheDecidesToSeeAttack(ServerPlayer player) {
        ItemStack stack = getEquippedWhenSheDecidesToSee(player);
        if (stack == null || !isElationPath(player)) {
            return;
        }
        int superimposition = WhenSheDecidesToSeeItem.getSuperimposition(stack);
        for (ServerPlayer recipient : player.serverLevel().getEntitiesOfClass(
                ServerPlayer.class, player.getBoundingBox().inflate(10.0D),
                target -> target.isAlive() && target.distanceToSqr(player) <= 100.0D)) {
            recipient.getPersistentData().putInt(BEST_FORTUNE_SUPERIMPOSITION_TAG,
                    superimposition);
            recipient.getPersistentData().putString(BEST_FORTUNE_OWNER_TAG,
                    player.getStringUUID());
            recipient.removeEffect(StarRailSimMod.BEST_FORTUNE.get());
            recipient.addEffect(new MobEffectInstance(StarRailSimMod.BEST_FORTUNE.get(),
                    BEST_FORTUNE_DURATION, 0, false, true, true));
            refresh(recipient);
        }
    }

    /** Adds one “Push Stream” stack after each attack from this cone. */
    public static void onFlowerWorldMesmerizingEyesAttack(ServerPlayer player) {
        ItemStack stack = getEquippedFlowerWorldMesmerizingEyes(player);
        if (stack == null || !isElationPath(player)) {
            return;
        }
        MobEffectInstance current = player.getEffect(StarRailSimMod.PUSH_STREAM.get());
        int currentStacks = current == null ? 0 : current.getAmplifier() + 1;
        int nextStacks = Math.min(PUSH_STREAM_MAX_STACKS, currentStacks + 1);
        player.getPersistentData().putInt(PUSH_STREAM_SUPERIMPOSITION_TAG,
                FlowerWorldMesmerizingEyesItem.getSuperimposition(stack));
        player.addEffect(new MobEffectInstance(StarRailSimMod.PUSH_STREAM.get(),
                PUSH_STREAM_DURATION, nextStacks - 1, false, true, true));
        refresh(player);
    }

    /** Returns and clears the queued fifth-hit damage for the exact target that was hit. */
    public static double consumeMayRainbowBonusDamage(ServerPlayer player,
                                                       LivingEntity target) {
        ItemStack stack = getEquippedMayRainbowStayInTheSky(player);
        if (stack == null || !isRemembrancePath(player)
                || !target.getUUID().toString().equals(player.getPersistentData().getString(
                        MAY_RAINBOW_PENDING_TARGET_TAG))) {
            return 0.0D;
        }
        double damage = Math.max(0.0D, player.getPersistentData().getDouble(
                MAY_RAINBOW_PENDING_DAMAGE_TAG));
        player.getPersistentData().remove(MAY_RAINBOW_PENDING_DAMAGE_TAG);
        player.getPersistentData().remove(MAY_RAINBOW_PENDING_TARGET_TAG);
        return damage;
    }

    public static double echoesOfTheCoffinBonusDamage(ServerPlayer player) {
        ItemStack stack = getEquippedEchoesOfTheCoffin(player);
        if (stack == null || !isAbundancePath(player)
                || !player.hasEffect(StarRailSimMod.THORN.get())) {
            return 0.0D;
        }
        int level = player.getPersistentData().getInt(THORN_SUPERIMPOSITION_TAG);
        return player.getMaxHealth() * echoesOfTheCoffinBonusDamage(level);
    }

    /** Reconciles dynamic attributes and removes special effects when invalid. */
    public static void refresh(ServerPlayer player) {
        ItemStack inTheNight = getEquippedInTheNight(player);
        ItemStack iWillHunt = getEquippedIWillHunt(player);
        ItemStack worrisomeBlissful = getEquippedWorrisomeBlissful(player);
        ItemStack sleepLikeTheDead = getEquippedSleepLikeTheDead(player);
        ItemStack pureThoughtBaptism = getEquippedPureThoughtBaptism(player);
        ItemStack idealBurningHell = getEquippedIdealBurningHell(player);
        ItemStack embarkOnSecondLife = getEquippedEmbarkOnSecondLife(player);
        ItemStack finaleOfALie = getEquippedFinaleOfALie(player);
        ItemStack somethingIrreplaceable = getEquippedSomethingIrreplaceable(player);
        ItemStack brighterThanTheSun = getEquippedBrighterThanTheSun(player);
        ItemStack danceAtSunset = getEquippedDanceAtSunset(player);
        ItemStack theUnreachableSide = getEquippedTheUnreachableSide(player);
        ItemStack thisBodyAsSword = getEquippedThisBodyAsSword(player);
        ItemStack noRewardCrowning = getEquippedNoRewardCrowning(player);
        ItemStack bloodFireBurningPath = getEquippedBloodFireBurningPath(player);
        ItemStack whereDreamsBelong = getEquippedWhereDreamsBelong(player);
        ItemStack dawnBurnsJustSo = getEquippedDawnBurnsJustSo(player);
        ItemStack whatYouSeeIsMe = getEquippedWhatYouSeeIsMe(player);
        ItemStack galaxyRailway = getEquippedGalaxyRailway(player);
        ItemStack momentOfGlory = getEquippedMomentOfGlory(player);
        ItemStack beforeDawn = getEquippedBeforeDawn(player);
        ItemStack priceOfPeace = getEquippedPriceOfPeace(player);
        ItemStack towardsUnanswerable = getEquippedTowardsUnanswerable(player);
        ItemStack ninjaScroll = getEquippedNinjaScroll(player);
        ItemStack lifeAsALight = getEquippedLifeAsALight(player);
        ItemStack aStarIlluminatesNightSky = getEquippedAStarIlluminatesNightSky(player);
        ItemStack sparkleQuietlyShines = getEquippedSparkleQuietlyShines(player);
        ItemStack mirrorOfThePast = getEquippedMirrorOfThePast(player);
        ItemStack gameOfCosmicWorlds = getEquippedGameOfCosmicWorlds(player);
        ItemStack returningToEarth = getEquippedReturningToEarth(player);
        ItemStack ifTimeWereAFlower = getEquippedIfTimeWereAFlower(player);
        ItemStack goldenBlood = getEquippedAnAgeEtchedInGoldenBlood(player);
        ItemStack inTheName = getEquippedInTheNameOfTheWorld(player);
        ItemStack onTheShore = getEquippedOnTheShoreInTheFlowOfTime(player);
        ItemStack thousandSprings = getEquippedAThousandFoldSpring(player);
        ItemStack onlyWait = getEquippedOnlyWait(player);
        ItemStack rainNeverStops = getEquippedRainNeverStops(player);
        ItemStack liesInTheWind = getEquippedLiesInTheWind(player);
        ItemStack returnToLongRoad = getEquippedReturnToLongRoad(player);
        ItemStack reforgedRemembrance = getEquippedReforgedRemembrance(player);
        ItemStack oceanWhySings = getEquippedOceanWhySings(player);
        ItemStack doNotForgetHerFlame = getEquippedDoNotForgetHerFlame(player);
        ItemStack newFleshOfInferno = getEquippedNewFleshOfInferno(player);
        ItemStack momentOfVictory = getEquippedMomentOfVictory(player);
        ItemStack sheHasClosedHerEyes = getEquippedSheHasClosedHerEyes(player);
        ItemStack thoughRiversAndMountains = getEquippedThoughRiversAndMountains(player);
        ItemStack fateNeverFair = getEquippedFateNeverFair(player);
        ItemStack echoesOfTheCoffin = getEquippedEchoesOfTheCoffin(player);
        ItemStack timeWaitsForNoOne = getEquippedTimeWaitsForNoOne(player);
        ItemStack nightOfFright = getEquippedNightOfFright(player);
        ItemStack onlyTheScentRemains = getEquippedOnlyTheScentRemains(player);
        ItemStack weaveTimeIntoGold = getEquippedWeaveTimeIntoGold(player);
        ItemStack makeFarewellMoreBeautiful = getEquippedMakeFarewellMoreBeautiful(player);
        ItemStack loveIsEternal = getEquippedLoveIsEternal(player);
        ItemStack starlightForLongNights = getEquippedStarlightForLongNights(player);
        ItemStack mayRainbowStayInTheSky = getEquippedMayRainbowStayInTheSky(player);
        ItemStack welcomeToGalacticCity = getEquippedWelcomeToGalacticCity(player);
        ItemStack meetInTheNextFlowerSeason = getEquippedMeetInTheNextFlowerSeason(player);
        ItemStack whenSheDecidesToSee = getEquippedWhenSheDecidesToSee(player);
        ItemStack flowerWorldMesmerizingEyes =
                getEquippedFlowerWorldMesmerizingEyes(player);
        boolean huntPath = isHuntPath(player);
        boolean destructionPath = isDestructionPath(player);
        boolean eruditionPath = isEruditionPath(player);
        boolean nightActive = inTheNight != null && huntPath;
        boolean huntActive = iWillHunt != null && huntPath;
        boolean worrisomeActive = worrisomeBlissful != null && huntPath;
        boolean sleepActive = sleepLikeTheDead != null && huntPath;
        boolean pureActive = pureThoughtBaptism != null && huntPath;
        boolean idealActive = idealBurningHell != null && huntPath;
        boolean embarkActive = embarkOnSecondLife != null && huntPath;
        boolean finaleActive = finaleOfALie != null && huntPath;
        boolean somethingActive = somethingIrreplaceable != null && destructionPath;
        boolean brighterActive = brighterThanTheSun != null && destructionPath;
        boolean danceActive = danceAtSunset != null && destructionPath;
        boolean unreachableActive = theUnreachableSide != null && destructionPath;
        boolean thisBodyActive = thisBodyAsSword != null && destructionPath;
        boolean noRewardCrowningActive = noRewardCrowning != null && destructionPath;
        boolean bloodFireActive = bloodFireBurningPath != null && destructionPath;
        boolean whereDreamsActive = whereDreamsBelong != null && destructionPath;
        boolean dawnBurnsActive = dawnBurnsJustSo != null && destructionPath;
        boolean whatYouSeeIsMeActive = whatYouSeeIsMe != null && destructionPath;
        boolean galaxyRailwayActive = galaxyRailway != null && eruditionPath;
        boolean momentOfGloryActive = momentOfGlory != null && eruditionPath;
        boolean beforeDawnActive = beforeDawn != null && eruditionPath;
        boolean priceOfPeaceActive = priceOfPeace != null && eruditionPath;
        boolean towardsUnanswerableActive = towardsUnanswerable != null && eruditionPath;
        boolean ninjaScrollActive = ninjaScroll != null && eruditionPath;
        boolean lifeAsALightActive = lifeAsALight != null && eruditionPath;
        boolean aStarIlluminatesNightSkyActive = aStarIlluminatesNightSky != null
                && eruditionPath;
        boolean sparkleQuietlyShinesActive = sparkleQuietlyShines != null && eruditionPath;
        boolean mirrorOfThePastActive = mirrorOfThePast != null && isHarmonyPath(player);
        boolean gameOfCosmicWorldsActive = gameOfCosmicWorlds != null && isHarmonyPath(player);
        boolean returningToEarthActive = returningToEarth != null && isHarmonyPath(player);
        boolean ifTimeWereAFlowerActive = ifTimeWereAFlower != null && isHarmonyPath(player);
        boolean goldenBloodActive = goldenBlood != null && isHarmonyPath(player);
        boolean inTheNameActive = inTheName != null && isNihilityPath(player);
        boolean onTheShoreActive = onTheShore != null && isNihilityPath(player);
        boolean thousandSpringsActive = thousandSprings != null && isNihilityPath(player);
        boolean onlyWaitActive = onlyWait != null && isNihilityPath(player);
        boolean rainNeverStopsActive = rainNeverStops != null && isNihilityPath(player);
        boolean liesInTheWindActive = liesInTheWind != null && isNihilityPath(player);
        boolean returnToLongRoadActive = returnToLongRoad != null && isNihilityPath(player);
        boolean reforgedRemembranceActive = reforgedRemembrance != null
                && isNihilityPath(player);
        boolean oceanWhySingsActive = oceanWhySings != null && isNihilityPath(player);
        boolean doNotForgetHerFlameActive = doNotForgetHerFlame != null
                && isNihilityPath(player);
        boolean newFleshOfInfernoActive = newFleshOfInferno != null
                && isNihilityPath(player);
        boolean momentOfVictoryActive = momentOfVictory != null && isPreservationPath(player);
        boolean sheHasClosedHerEyesActive = sheHasClosedHerEyes != null
                && isPreservationPath(player);
        boolean thoughRiversAndMountainsActive = thoughRiversAndMountains != null
                && isPreservationPath(player);
        boolean fateNeverFairActive = fateNeverFair != null && isPreservationPath(player);
        boolean echoesOfTheCoffinActive = echoesOfTheCoffin != null && isAbundancePath(player);
        boolean timeWaitsForNoOneActive = timeWaitsForNoOne != null && isAbundancePath(player);
        boolean nightOfFrightActive = nightOfFright != null && isAbundancePath(player);
        boolean onlyTheScentRemainsActive = onlyTheScentRemains != null
                && isAbundancePath(player);
        boolean weaveTimeIntoGoldActive = weaveTimeIntoGold != null
                && isRemembrancePath(player);
        boolean makeFarewellMoreBeautifulActive = makeFarewellMoreBeautiful != null
                && isRemembrancePath(player);
        boolean loveIsEternalActive = loveIsEternal != null && isRemembrancePath(player);
        boolean starlightForLongNightsActive = starlightForLongNights != null
                && isRemembrancePath(player);
        boolean mayRainbowStayInTheSkyActive = mayRainbowStayInTheSky != null
                && isRemembrancePath(player);
        boolean meetInTheNextFlowerSeasonActive = meetInTheNextFlowerSeason != null
                && isElationPath(player);
        boolean whenSheDecidesToSeeActive = whenSheDecidesToSee != null
                && isElationPath(player);
        boolean welcomeToGalacticCityActive = welcomeToGalacticCity != null
                && isElationPath(player);
        boolean flowerWorldMesmerizingEyesActive = flowerWorldMesmerizingEyes != null
                && isElationPath(player);
        MobEffectInstance deepBreath = player.getEffect(StarRailSimMod.DEEP_BREATH.get());
        if (deepBreath == null) {
            player.getPersistentData().remove(DEEP_BREATH_SUPERIMPOSITION_TAG);
        }
        int deepBreathSuperimposition = deepBreath == null ? 1 : boundedSuperimposition(
                player.getPersistentData().getInt(DEEP_BREATH_SUPERIMPOSITION_TAG));
        int deepBreathStacks = deepBreath == null ? 0
                : Math.min(DEEP_BREATH_MAX_STACKS, deepBreath.getAmplifier() + 1);
        MobEffectInstance brocade = player.getEffect(StarRailSimMod.BROCADE.get());
        if (!weaveTimeIntoGoldActive && brocade != null) {
            player.removeEffect(StarRailSimMod.BROCADE.get());
            brocade = null;
        }
        int brocadeStacks = brocade == null ? 0
                : Math.max(0, Math.min(BROCADE_MAX_STACKS, brocade.getAmplifier() + 1));
        int weaveTimeIntoGoldSuperimposition = weaveTimeIntoGoldActive
                ? WeaveTimeIntoGoldItem.getSuperimposition(weaveTimeIntoGold) : 1;
        if (!makeFarewellMoreBeautifulActive
                && player.hasEffect(StarRailSimMod.NETHER_BLOOM.get())) {
            player.removeEffect(StarRailSimMod.NETHER_BLOOM.get());
        }
        MobEffectInstance blank = player.getEffect(StarRailSimMod.BLANK.get());
        if (!loveIsEternalActive && blank != null) {
            player.removeEffect(StarRailSimMod.BLANK.get());
            player.getPersistentData().remove(LOVE_IS_ETERNAL_BLANK_SUPERIMPOSITION_TAG);
            blank = null;
        }
        MobEffectInstance verse = player.getEffect(StarRailSimMod.VERSE.get());
        if (verse == null) {
            player.getPersistentData().remove(LOVE_IS_ETERNAL_VERSE_SUPERIMPOSITION_TAG);
            player.getPersistentData().remove(LOVE_IS_ETERNAL_VERSE_OWNER_TAG);
        }
        MobEffectInstance starlitNight = player.getEffect(StarRailSimMod.STARLIT_NIGHT.get());
        if (!starlightForLongNightsActive && starlitNight != null) {
            player.removeEffect(StarRailSimMod.STARLIT_NIGHT.get());
            player.getPersistentData().remove(STARLIT_NIGHT_SUPERIMPOSITION_TAG);
            starlitNight = null;
        }
        if (starlitNight == null) {
            player.getPersistentData().remove(STARLIT_NIGHT_SUPERIMPOSITION_TAG);
        }
        MobEffectInstance winningStreak = player.getEffect(StarRailSimMod.WINNING_STREAK.get());
        if (!welcomeToGalacticCityActive && winningStreak != null) {
            player.removeEffect(StarRailSimMod.WINNING_STREAK.get());
            player.getPersistentData().remove(WINNING_STREAK_SUPERIMPOSITION_TAG);
            winningStreak = null;
        }
        if (winningStreak == null) {
            player.getPersistentData().remove(WINNING_STREAK_SUPERIMPOSITION_TAG);
        }
        int winningStreakStacks = winningStreak == null ? 0
                : Math.max(0, Math.min(WINNING_STREAK_MAX_STACKS,
                        winningStreak.getAmplifier() + 1));
        int winningStreakSuperimposition = winningStreak == null ? 1
                : boundedSuperimposition(player.getPersistentData().getInt(
                        WINNING_STREAK_SUPERIMPOSITION_TAG));
        MobEffectInstance daydream = player.getEffect(StarRailSimMod.DAYDREAM.get());
        if (!meetInTheNextFlowerSeasonActive && daydream != null) {
            player.removeEffect(StarRailSimMod.DAYDREAM.get());
            player.getPersistentData().remove(DAYDREAM_SUPERIMPOSITION_TAG);
            daydream = null;
        }
        if (daydream == null) {
            player.getPersistentData().remove(DAYDREAM_SUPERIMPOSITION_TAG);
        }
        int daydreamStacks = daydream == null ? 0
                : Math.max(0, Math.min(DAYDREAM_MAX_STACKS, daydream.getAmplifier() + 1));
        int daydreamSuperimposition = daydream == null ? 1
                : boundedSuperimposition(player.getPersistentData().getInt(
                        DAYDREAM_SUPERIMPOSITION_TAG));
        MobEffectInstance bestFortune = player.getEffect(StarRailSimMod.BEST_FORTUNE.get());
        if (bestFortune != null) {
            String ownerId = player.getPersistentData().getString(BEST_FORTUNE_OWNER_TAG);
            ServerPlayer owner = null;
            try {
                if (!ownerId.isEmpty()) {
                    owner = player.getServer().getPlayerList().getPlayer(
                            java.util.UUID.fromString(ownerId));
                }
            } catch (IllegalArgumentException ignored) {
                // Invalid stored owner data is treated as an expired aura.
            }
            if (owner == null || getEquippedWhenSheDecidesToSee(owner) == null
                    || !isElationPath(owner)) {
                player.removeEffect(StarRailSimMod.BEST_FORTUNE.get());
                player.getPersistentData().remove(BEST_FORTUNE_SUPERIMPOSITION_TAG);
                player.getPersistentData().remove(BEST_FORTUNE_OWNER_TAG);
                bestFortune = null;
            }
        } else {
            player.getPersistentData().remove(BEST_FORTUNE_SUPERIMPOSITION_TAG);
            player.getPersistentData().remove(BEST_FORTUNE_OWNER_TAG);
        }
        int bestFortuneSuperimposition = bestFortune == null ? 1
                : Math.max(1, Math.min(5, player.getPersistentData().getInt(
                        BEST_FORTUNE_SUPERIMPOSITION_TAG)));
        MobEffectInstance pushStream = player.getEffect(StarRailSimMod.PUSH_STREAM.get());
        if (!flowerWorldMesmerizingEyesActive && pushStream != null) {
            player.removeEffect(StarRailSimMod.PUSH_STREAM.get());
            player.getPersistentData().remove(PUSH_STREAM_SUPERIMPOSITION_TAG);
            pushStream = null;
        }
        if (pushStream == null) {
            player.getPersistentData().remove(PUSH_STREAM_SUPERIMPOSITION_TAG);
        }
        int pushStreamStacks = pushStream == null ? 0
                : Math.max(0, Math.min(PUSH_STREAM_MAX_STACKS,
                        pushStream.getAmplifier() + 1));
        int pushStreamSuperimposition = pushStream == null ? 1
                : boundedSuperimposition(player.getPersistentData().getInt(
                        PUSH_STREAM_SUPERIMPOSITION_TAG));
        int farewellSuperimposition = makeFarewellMoreBeautifulActive
                ? MakeFarewellMoreBeautifulItem.getSuperimposition(
                        makeFarewellMoreBeautiful) : 1;
        if (!timeWaitsForNoOneActive) {
            player.getPersistentData().remove(TIME_WAITS_FOR_NO_ONE_RECORDED_LOSS_TAG);
        }
        if (!mayRainbowStayInTheSkyActive) {
            player.getPersistentData().remove(MAY_RAINBOW_ATTACK_COUNT_TAG);
            player.getPersistentData().remove(MAY_RAINBOW_CONSUMED_HEALTH_TAG);
            player.getPersistentData().remove(MAY_RAINBOW_PENDING_DAMAGE_TAG);
            player.getPersistentData().remove(MAY_RAINBOW_PENDING_TARGET_TAG);
        }
        MobEffectInstance thorn = player.getEffect(StarRailSimMod.THORN.get());
        if (!echoesOfTheCoffinActive && thorn != null) {
            player.removeEffect(StarRailSimMod.THORN.get());
            player.getPersistentData().remove(THORN_SUPERIMPOSITION_TAG);
            thorn = null;
        }
        if (thorn == null) {
            player.getPersistentData().remove(THORN_SUPERIMPOSITION_TAG);
        }
        long fateCritDamageUntil = player.getPersistentData().getLong(
                FATE_NEVER_FAIR_CRIT_DAMAGE_UNTIL_TAG);
        if (!fateNeverFairActive) {
            player.getPersistentData().remove(FATE_NEVER_FAIR_CRIT_DAMAGE_UNTIL_TAG);
            fateCritDamageUntil = 0L;
        }
        int closedEyesSuperimposition = sheHasClosedHerEyes == null ? 1
                : SheHasClosedHerEyesItem.getSuperimposition(sheHasClosedHerEyes);
        MobEffectInstance will = player.getEffect(StarRailSimMod.WILL.get());
        if (!inTheNameActive && will != null) {
            player.removeEffect(StarRailSimMod.WILL.get());
            player.getPersistentData().remove(IN_THE_NAME_SUPERIMPOSITION_TAG);
            will = null;
        }
        if (will == null) {
            player.getPersistentData().remove(IN_THE_NAME_SUPERIMPOSITION_TAG);
        }
        int willSuperimposition = will == null ? 1
                : Math.max(1, Math.min(5, player.getPersistentData().getInt(
                        IN_THE_NAME_SUPERIMPOSITION_TAG)));
        MobEffectInstance foamEcho = player.getEffect(StarRailSimMod.FOAM_ECHO.get());
        if (!onTheShoreActive && foamEcho != null) {
            player.removeEffect(StarRailSimMod.FOAM_ECHO.get());
            player.getPersistentData().remove(FOAM_ECHO_SUPERIMPOSITION_TAG);
            foamEcho = null;
        }
        if (foamEcho == null) {
            player.getPersistentData().remove(FOAM_ECHO_SUPERIMPOSITION_TAG);
        }
        int foamEchoSuperimposition = foamEcho == null ? 1
                : Math.max(1, Math.min(5, player.getPersistentData().getInt(
                        FOAM_ECHO_SUPERIMPOSITION_TAG)));
        int foamEchoStacks = foamEcho == null ? 0
                : Math.max(1, Math.min(FOAM_ECHO_MAX_STACKS,
                        foamEcho.getAmplifier() + 1));
        MobEffectInstance inheritanceEffect = player.getEffect(StarRailSimMod.INHERITANCE.get());
        int inheritanceSuperimposition = inheritanceEffect == null
                ? 0 : Math.max(1, Math.min(5, inheritanceEffect.getAmplifier() + 1));
        MobEffectInstance chantEffect = player.getEffect(StarRailSimMod.CHANT.get());
        if (chantEffect == null) {
            player.getPersistentData().remove(NIGHT_FLOWING_COLORS_CHANT_SUPERIMPOSITION_TAG);
        }
        int chantSuperimposition = chantEffect == null ? 1
                : Math.max(1, Math.min(5, player.getPersistentData().getInt(
                        NIGHT_FLOWING_COLORS_CHANT_SUPERIMPOSITION_TAG)));
        int chantStacks = chantEffect == null ? 0
                : Math.max(1, Math.min(NIGHT_FLOWING_COLORS_MAX_CHANT_STACKS,
                        chantEffect.getAmplifier() + 1));
        MobEffectInstance splendorEffect = player.getEffect(StarRailSimMod.SPLENDOR.get());
        if (splendorEffect == null) {
            player.getPersistentData().remove(NIGHT_FLOWING_COLORS_SPLENDOR_SUPERIMPOSITION_TAG);
            player.getPersistentData().remove(NIGHT_FLOWING_COLORS_SPLENDOR_OWNER_TAG);
        }
        int splendorSuperimposition = splendorEffect == null ? 1
                : Math.max(1, Math.min(5, player.getPersistentData().getInt(
                        NIGHT_FLOWING_COLORS_SPLENDOR_SUPERIMPOSITION_TAG)));
        boolean splendorOwner = splendorEffect != null && player.getStringUUID().equals(
                player.getPersistentData().getString(NIGHT_FLOWING_COLORS_SPLENDOR_OWNER_TAG));
        MobEffectInstance gameMask = player.getEffect(StarRailSimMod.MASK.get());
        if (gameMask == null) {
            player.getPersistentData().remove(GAME_OF_COSMIC_WORLDS_MASK_SUPERIMPOSITION_TAG);
            player.getPersistentData().remove(GAME_OF_COSMIC_WORLDS_MASK_OWNER_TAG);
        }
        int gameMaskSuperimposition = gameMask == null ? 1
                : Math.max(1, Math.min(5, player.getPersistentData().getInt(
                        GAME_OF_COSMIC_WORLDS_MASK_SUPERIMPOSITION_TAG)));
        boolean gameMaskOwner = gameMask != null && player.getStringUUID().equals(
                player.getPersistentData().getString(GAME_OF_COSMIC_WORLDS_MASK_OWNER_TAG));
        boolean gameMaskRecipient = gameMask != null && !gameMaskOwner;
        MobEffectInstance edict = player.getEffect(StarRailSimMod.EDICT.get());
        if (edict == null) {
            player.getPersistentData().remove(EDICT_SUPERIMPOSITION_TAG);
        }
        int edictSuperimposition = edict == null ? 1
                : Math.max(1, Math.min(5, player.getPersistentData().getInt(
                        EDICT_SUPERIMPOSITION_TAG)));
        MobEffectInstance law = player.getEffect(StarRailSimMod.LAW.get());
        if (law == null) {
            player.getPersistentData().remove(LAW_SUPERIMPOSITION_TAG);
        }
        int lawSuperimposition = law == null ? 1
                : Math.max(1, Math.min(5, player.getPersistentData().getInt(
                        LAW_SUPERIMPOSITION_TAG)));
        if (gameOfCosmicWorldsActive && gameMaskOwner) {
            refreshGameOfCosmicWorldsAura(player, gameMaskSuperimposition);
        }
        MobEffectInstance plumFragranceEffect = player.getEffect(StarRailSimMod.PLUM_FRAGRANCE.get());
        if (plumFragranceEffect == null) {
            player.getPersistentData().remove(PLUM_FRAGRANCE_SUPERIMPOSITION_TAG);
        }
        int plumFragranceSuperimposition = plumFragranceEffect == null ? 1
                : Math.max(1, Math.min(5, player.getPersistentData()
                        .getInt(PLUM_FRAGRANCE_SUPERIMPOSITION_TAG)));
        int plumFragranceStackCount = plumFragranceEffect == null ? 0
                : Math.max(1, Math.min(PLUM_FRAGRANCE_MAX_STACKS,
                        plumFragranceEffect.getAmplifier() + 1));
        MobEffectInstance psalmEffect = player.getEffect(StarRailSimMod.PSALM.get());
        if (psalmEffect == null) {
            player.getPersistentData().remove(PSALM_SUPERIMPOSITION_TAG);
        }
        int psalmSuperimposition = psalmEffect == null ? 1
                : Math.max(1, Math.min(5, player.getPersistentData()
                        .getInt(PSALM_SUPERIMPOSITION_TAG)));
        int psalmStacks = psalmEffect == null ? 0
                : Math.max(1, Math.min(PSALM_MAX_STACKS, psalmEffect.getAmplifier() + 1));
        MobEffectInstance prophet = player.getEffect(StarRailSimMod.PROPHET.get());
        if (!reforgedRemembranceActive && prophet != null) {
            player.removeEffect(StarRailSimMod.PROPHET.get());
            player.getPersistentData().remove(PROPHET_SUPERIMPOSITION_TAG);
            prophet = null;
        }
        if (prophet == null) {
            player.getPersistentData().remove(PROPHET_SUPERIMPOSITION_TAG);
        }
        int prophetSuperimposition = prophet == null ? 1
                : Math.max(1, Math.min(5, player.getPersistentData()
                        .getInt(PROPHET_SUPERIMPOSITION_TAG)));
        int prophetStackCount = prophet == null ? 0
                : Math.max(1, Math.min(PROPHET_MAX_STACKS, prophet.getAmplifier() + 1));
        int mirrorSuperimposition = mirrorOfThePastActive
                ? MemoryOfMeItem.getSuperimposition(mirrorOfThePast) : 1;
        int nightSuperimposition = inTheNight == null
                ? 1 : InTheNightItem.getSuperimposition(inTheNight);
        int huntSuperimposition = iWillHunt == null
                ? 1 : IWillHuntItem.getSuperimposition(iWillHunt);
        int worrisomeSuperimposition = worrisomeBlissful == null
                ? 1 : WorrisomeBlissfulItem.getSuperimposition(worrisomeBlissful);
        int sleepSuperimposition = sleepLikeTheDead == null
                ? 1 : SleepLikeTheDeadItem.getSuperimposition(sleepLikeTheDead);
        int pureSuperimposition = pureThoughtBaptism == null
                ? 1 : PureThoughtBaptismItem.getSuperimposition(pureThoughtBaptism);
        int idealSuperimposition = idealBurningHell == null
                ? 1 : IdealBurningHellItem.getSuperimposition(idealBurningHell);
        int embarkSuperimposition = embarkOnSecondLife == null
                ? 1 : EmbarkOnSecondLifeItem.getSuperimposition(embarkOnSecondLife);
        int finaleSuperimposition = finaleOfALie == null
                ? 1 : FinaleOfALieItem.getSuperimposition(finaleOfALie);
        int somethingSuperimposition = somethingIrreplaceable == null
                ? 1 : SomethingIrreplaceableItem.getSuperimposition(somethingIrreplaceable);
        int brighterSuperimposition = brighterThanTheSun == null
                ? 1 : BrighterThanTheSunItem.getSuperimposition(brighterThanTheSun);
        int danceSuperimposition = danceAtSunset == null
                ? 1 : DanceAtSunsetItem.getSuperimposition(danceAtSunset);
        int unreachableSuperimposition = theUnreachableSide == null
                ? 1 : TheUnreachableSideItem.getSuperimposition(theUnreachableSide);
        int thisBodySuperimposition = thisBodyAsSword == null
                ? 1 : ThisBodyAsSwordItem.getSuperimposition(thisBodyAsSword);
        int noRewardCrowningSuperimposition = noRewardCrowning == null
                ? 1 : NoRewardCrowningItem.getSuperimposition(noRewardCrowning);
        int bloodFireSuperimposition = bloodFireBurningPath == null
                ? 1 : BloodFireBurningPathItem.getSuperimposition(bloodFireBurningPath);
        int whereDreamsSuperimposition = whereDreamsBelong == null
                ? 1 : WhereDreamsBelongItem.getSuperimposition(whereDreamsBelong);
        int dawnBurnsSuperimposition = dawnBurnsJustSo == null
                ? 1 : DawnBurnsJustSoItem.getSuperimposition(dawnBurnsJustSo);
        int whatYouSeeIsMeSuperimposition = whatYouSeeIsMe == null
                ? 1 : WhatYouSeeIsMeItem.getSuperimposition(whatYouSeeIsMe);
        int galaxyRailwaySuperimposition = galaxyRailway == null
                ? 1 : GalaxyRailwayItem.getSuperimposition(galaxyRailway);
        int momentOfGlorySuperimposition = momentOfGlory == null
                ? 1 : MomentOfGloryItem.getSuperimposition(momentOfGlory);
        int beforeDawnSuperimposition = beforeDawn == null
                ? 1 : BeforeDawnItem.getSuperimposition(beforeDawn);
        int priceOfPeaceSuperimposition = priceOfPeace == null
                ? 1 : PriceOfPeaceItem.getSuperimposition(priceOfPeace);
        int towardsUnanswerableSuperimposition = towardsUnanswerable == null
                ? 1 : TowardsUnanswerableItem.getSuperimposition(towardsUnanswerable);
        int ninjaScrollSuperimposition = ninjaScroll == null
                ? 1 : NinjaScrollItem.getSuperimposition(ninjaScroll);
        int lifeAsALightSuperimposition = lifeAsALight == null
                ? 1 : LifeAsALightItem.getSuperimposition(lifeAsALight);
        int aStarIlluminatesNightSkySuperimposition = aStarIlluminatesNightSky == null
                ? 1 : AStarIlluminatesNightSkyItem.getSuperimposition(
                        aStarIlluminatesNightSky);
        int sparkleQuietlyShinesSuperimposition = sparkleQuietlyShines == null
                ? 1 : SparkleQuietlyShinesItem.getSuperimposition(sparkleQuietlyShines);
        int nightStacks = nightActive ? nightButterflyStacks(player) : 0;
        int rangerStackCount = idealActive ? rangerStacks(player) : 0;
        int dragonRoarStackCount = brighterActive ? dragonRoarStacks(player) : 0;
        int fireDanceStackCount = danceActive ? fireDanceStacks(player) : 0;
        int moonEclipseStackCount = thisBodyActive ? moonEclipseStacks(player) : 0;
        boolean knightKingActive = noRewardCrowningActive
                && player.hasEffect(StarRailSimMod.KNIGHT_KING.get());

        if (!nightActive) {
            player.removeEffect(StarRailSimMod.NIGHT_BUTTERFLY.get());
        }
        if (!huntActive) {
            player.removeEffect(StarRailSimMod.FLOWING_LIGHT.get());
        }
        if (!sleepActive) {
            player.removeEffect(StarRailSimMod.BEAUTIFUL_DREAM.get());
        }
        if (!pureActive) {
            player.removeEffect(StarRailSimMod.DEBATE.get());
        }
        if (!idealActive) {
            player.removeEffect(StarRailSimMod.RANGER.get());
        }
        if (!somethingActive) {
            player.removeEffect(StarRailSimMod.FAMILY.get());
        }
        if (!brighterActive) {
            player.removeEffect(StarRailSimMod.DRAGON_ROAR.get());
        }
        if (!danceActive) {
            player.removeEffect(StarRailSimMod.FIRE_DANCE.get());
        }
        if (!unreachableActive) {
            player.removeEffect(StarRailSimMod.NO_RETREAT.get());
        }
        if (!thisBodyActive) {
            player.removeEffect(StarRailSimMod.MOON_ECLIPSE.get());
        }
        if (!noRewardCrowningActive) {
            player.removeEffect(StarRailSimMod.KNIGHT_KING.get());
            player.getPersistentData().remove(KNIGHT_KING_ATTACK_TAG);
        }
        if (!bloodFireActive) {
            player.removeEffect(StarRailSimMod.STRIFE.get());
            player.getPersistentData().remove(STRIFE_COOLDOWN_TAG);
        }
        if (!dawnBurnsActive) {
            player.removeEffect(StarRailSimMod.BLAZING_SUN.get());
        }
        if (!whatYouSeeIsMeActive) {
            player.removeEffect(StarRailSimMod.KINGLY_ENTERTAINMENT.get());
            player.getPersistentData().remove(KINGLY_ENTERTAINMENT_NEXT_ATTACK_TAG);
        }
        if (!galaxyRailwayActive) {
            player.removeEffect(StarRailSimMod.METEOR.get());
        }
        if (!momentOfGloryActive) {
            player.removeEffect(StarRailSimMod.KNIGHTLY_COURTESY.get());
        }
        if (!beforeDawnActive) {
            player.removeEffect(StarRailSimMod.DREAM_BODY.get());
        }
        if (!priceOfPeaceActive) {
            player.removeEffect(StarRailSimMod.PROMISE.get());
        }
        if (!towardsUnanswerableActive) {
            player.removeEffect(StarRailSimMod.DECONSTRUCTION.get());
        }
        if (!ninjaScrollActive) {
            player.removeEffect(StarRailSimMod.THUNDER_ESCAPE.get());
        }
        if (!lifeAsALightActive) {
            player.removeEffect(StarRailSimMod.ALCHEMY.get());
        }
        if (!aStarIlluminatesNightSkyActive) {
            player.removeEffect(StarRailSimMod.DEPARTURE.get());
        }
        if (!sparkleQuietlyShinesActive) {
            player.removeEffect(StarRailSimMod.SHINING_CROWN.get());
        }
        if (!doNotForgetHerFlameActive) {
            player.removeEffect(StarRailSimMod.BURNING_SELF.get());
        }
        MobEffectInstance winterShield = player.getEffect(StarRailSimMod.WINTER_SHIELD.get());
        if (!momentOfVictoryActive && winterShield != null) {
            player.removeEffect(StarRailSimMod.WINTER_SHIELD.get());
            winterShield = null;
        }

        AttributeInstance breakEffectAttribute = player.getAttribute(
                StarRailAttributes.BREAK_EFFECT.get());
        if (breakEffectAttribute != null) {
            applyModifier(breakEffectAttribute, DO_NOT_FORGET_HER_FLAME_BREAK_EFFECT_MODIFIER,
                    "starrail_sim.do_not_forget_her_flame.break_effect",
                    doNotForgetHerFlameActive ? doNotForgetHerFlameBreakEffect(
                            DoNotForgetHerFlameItem.getSuperimposition(doNotForgetHerFlame))
                            : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(breakEffectAttribute,
                    ONLY_THE_SCENT_REMAINS_BREAK_EFFECT_MODIFIER,
                    "starrail_sim.only_the_scent_remains.break_effect",
                    onlyTheScentRemainsActive ? onlyTheScentRemainsBreakEffect(
                            OnlyTheScentRemainsItem.getSuperimposition(
                                    onlyTheScentRemains)) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(breakEffectAttribute, EMBARK_ON_SECOND_LIFE_BREAK_EFFECT_MODIFIER,
                    "starrail_sim.embark_on_second_life.break_effect",
                    embarkActive ? painfulVoyageBreakEffect(embarkSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
        }
        boolean painfulVoyageActive = embarkActive
                && StarRailAttributes.getValue(player, StarRailAttributes.BREAK_EFFECT, 0.0D)
                >= PAINFUL_VOYAGE_THRESHOLD_PERCENT / 100.0D;
        if (!painfulVoyageActive) {
            player.removeEffect(StarRailSimMod.PAINFUL_VOYAGE.get());
        } else if (!player.hasEffect(StarRailSimMod.PAINFUL_VOYAGE.get())) {
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.PAINFUL_VOYAGE.get(), Integer.MAX_VALUE, 0,
                    false, true, true));
        }
        if (breakEffectAttribute != null) {
            applyModifier(breakEffectAttribute, MIRROR_BREAK_EFFECT_MODIFIER,
                    "starrail_sim.memory_of_me.break_effect",
                    mirrorOfThePastActive ? mirrorBreakEffect(mirrorSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(breakEffectAttribute, WHERE_DREAMS_BREAK_EFFECT_MODIFIER,
                    "starrail_sim.where_dreams_belong.break_effect",
                    whereDreamsActive
                            ? metamorphosisBreakEffect(whereDreamsSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(breakEffectAttribute, NINJA_SCROLL_BREAK_EFFECT_MODIFIER,
                    "starrail_sim.ninja_scroll.break_effect",
                    ninjaScrollActive
                            ? ninjaScrollBreakEffect(ninjaScrollSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(breakEffectAttribute, RETURN_TO_LONG_ROAD_BREAK_EFFECT_MODIFIER,
                    "starrail_sim.return_to_long_road.break_effect",
                    returnToLongRoadActive ? returnToLongRoadBreakEffect(
                            ReturnToLongRoadItem.getSuperimposition(returnToLongRoad)) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
        }
        boolean ninjaScrollBreakEffectActive = ninjaScrollActive
                && StarRailAttributes.getValue(player, StarRailAttributes.BREAK_EFFECT, 0.0D)
                >= PAINFUL_VOYAGE_THRESHOLD_PERCENT / 100.0D;
        boolean metamorphosisActive = whereDreamsActive
                && StarRailAttributes.getValue(player, StarRailAttributes.BREAK_EFFECT, 0.0D)
                >= METAMORPHOSIS_THRESHOLD_PERCENT / 100.0D;
        if (!metamorphosisActive) {
            player.removeEffect(StarRailSimMod.METAMORPHOSIS.get());
        } else if (!player.hasEffect(StarRailSimMod.METAMORPHOSIS.get())) {
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.METAMORPHOSIS.get(), Integer.MAX_VALUE, 0,
                    false, true, true));
        }
        double currentBreakEffect = StarRailAttributes.getValue(
                player, StarRailAttributes.BREAK_EFFECT, 0.0D);
        MobEffectInstance burningSelf = player.getEffect(StarRailSimMod.BURNING_SELF.get());
        if (!doNotForgetHerFlameActive) {
            player.removeEffect(StarRailSimMod.BURNING_SELF.get());
        } else if (currentBreakEffect >= 1.20D
                && (burningSelf == null || burningSelf.getDuration() <= 380)) {
            player.addEffect(new MobEffectInstance(StarRailSimMod.BURNING_SELF.get(),
                    400, 0, false, true, true));
        }
        if (!finaleActive) {
            player.removeEffect(StarRailSimMod.SHADOW_DEVOUR.get());
            player.getPersistentData().putInt(SHADOW_DEVOUR_ATTACK_COUNT_TAG, 0);
        }

        AttributeInstance critRate = player.getAttribute(StarRailAttributes.CRIT_RATE.get());
        if (critRate != null) {
            applyModifier(critRate, IN_THE_NIGHT_CRIT_RATE_MODIFIER,
                    "starrail_sim.in_the_night.crit_rate",
                    nightActive ? critRate(nightSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
        }

        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            applyModifier(attackDamage, DO_NOT_FORGET_HER_FLAME_ATTACK_MODIFIER,
                    "starrail_sim.do_not_forget_her_flame.burning_attack",
                    doNotForgetHerFlameActive
                            && player.hasEffect(StarRailSimMod.BURNING_SELF.get())
                            ? doNotForgetHerFlameAttackWhileBurning(
                                    DoNotForgetHerFlameItem.getSuperimposition(
                                            doNotForgetHerFlame)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, OCEAN_WHY_SINGS_ATTACK_MODIFIER,
                    "starrail_sim.ocean_why_sings.attack",
                    oceanWhySingsActive ? oceanWhySingsAttack(
                            OceanWhySingsItem.getSuperimposition(oceanWhySings)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, ONLY_WAIT_ATTACK_MODIFIER,
                    "starrail_sim.only_wait.spiders_web_attack",
                    onlyWaitActive ? onlyWaitAttack(
                            OnlyWaitItem.getSuperimposition(onlyWait)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, RAIN_NEVER_STOPS_ATTACK_MODIFIER,
                    "starrail_sim.rain_never_stops.attack",
                    rainNeverStopsActive ? rainNeverStopsAttack(
                            RainNeverStopsItem.getSuperimposition(rainNeverStops)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, LIES_IN_THE_WIND_ATTACK_MODIFIER,
                    "starrail_sim.lies_in_the_wind.attack",
                    liesInTheWindActive ? liesInTheWindAttack(
                            LiesInTheWindItem.getSuperimposition(liesInTheWind)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, RETURN_TO_LONG_ROAD_ATTACK_MODIFIER,
                    "starrail_sim.return_to_long_road.attack",
                    returnToLongRoadActive ? returnToLongRoadAttack(
                            ReturnToLongRoadItem.getSuperimposition(returnToLongRoad)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, REFORGED_REMEMBRANCE_ATTACK_MODIFIER,
                    "starrail_sim.reforged_remembrance.prophet_attack",
                    prophetStackCount > 0
                            ? reforgedRemembranceAttackPerStack(prophetSuperimposition)
                                    * prophetStackCount : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, IN_THE_NIGHT_STACK_ATTACK_MODIFIER,
                    "starrail_sim.in_the_night.night_butterfly_attack",
                    nightActive ? attackPerStack(nightSuperimposition) * nightStacks : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, WORRISOME_BLISSFUL_ATTACK_DAMAGE_MODIFIER,
                    "starrail_sim.worrisome_blissful.attack_damage",
                    worrisomeActive
                            ? worrisomeBlissfulAttackDamage(worrisomeSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, SLEEP_LIKE_THE_DEAD_ATTACK_DAMAGE_MODIFIER,
                    "starrail_sim.sleep_like_the_dead.attack_damage",
                    sleepActive
                            ? sleepLikeTheDeadAttackDamage(sleepSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, DEBATE_ATTACK_DAMAGE_MODIFIER,
                    "starrail_sim.pure_thought_baptism.debate_attack_damage",
                    pureActive && player.hasEffect(StarRailSimMod.DEBATE.get())
                            ? debateAttackDamage(pureSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, IDEAL_BURNING_HELL_ATTACK_DAMAGE_MODIFIER,
                    "starrail_sim.ideal_burning_hell.attack_damage",
                    idealActive
                            ? rangerAttackDamage(idealSuperimposition)
                            + rangerAttackPerStack(idealSuperimposition) * rangerStackCount
                            : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, EMBARK_ON_SECOND_LIFE_VOYAGE_ATTACK_MODIFIER,
                    "starrail_sim.embark_on_second_life.painful_voyage_attack",
                    painfulVoyageActive
                            ? painfulVoyageAttackDamage(embarkSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, FINALE_OF_A_LIE_SHADOW_ATTACK_MODIFIER,
                    "starrail_sim.finale_of_a_lie.shadow_devour_attack",
                    finaleActive && player.hasEffect(StarRailSimMod.SHADOW_DEVOUR.get())
                            ? shadowDevourAttackDamage(finaleSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, SOMETHING_IRREPLACEABLE_ATTACK_DAMAGE_MODIFIER,
                    "starrail_sim.something_irreplaceable.attack_damage",
                    somethingActive
                            ? somethingIrreplaceableAttackDamage(somethingSuperimposition)
                            : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, BRIGHTER_THAN_THE_SUN_ATTACK_MODIFIER,
                    "starrail_sim.brighter_than_the_sun.dragon_roar_attack",
                    brighterActive
                            ? dragonRoarAttackDamage(brighterSuperimposition)
                            * dragonRoarStackCount : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, DANCE_AT_SUNSET_ATTACK_MODIFIER,
                    "starrail_sim.dance_at_sunset.fire_dance_damage",
                    danceActive
                            ? fireDanceDamage(danceSuperimposition) * fireDanceStackCount
                            : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, THIS_BODY_AS_SWORD_ATTACK_MODIFIER,
                    "starrail_sim.this_body_as_sword.moon_eclipse_attack",
                    thisBodyActive && moonEclipseStackCount >= MOON_ECLIPSE_MAX_STACKS
                            ? moonEclipseFullAttack(thisBodySuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, KNIGHT_KING_ATTACK_MODIFIER,
                    "starrail_sim.no_reward_crowning.knight_king_attack",
                    knightKingActive
                            ? knightKingAttack(noRewardCrowningSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, WHERE_DREAMS_ATTACK_MODIFIER,
                    "starrail_sim.where_dreams_belong.metamorphosis_attack",
                    metamorphosisActive
                            ? metamorphosisAttack(whereDreamsSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, DAWN_BURNS_ATTACK_MODIFIER,
                    "starrail_sim.dawn_burns_just_so.attack",
                    dawnBurnsActive ? dawnBurnsAttack(dawnBurnsSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, WHAT_YOU_SEE_IS_ME_ATTACK_MODIFIER,
                    "starrail_sim.what_you_see_is_me.attack",
                    whatYouSeeIsMeActive
                            ? whatYouSeeIsMeAttack(whatYouSeeIsMeSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            int nearbyEnemies = galaxyRailwayActive
                    ? Math.min(METEOR_MAX_STACKS, player.level()
                    .getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(10.0D),
                            LivingEntity::isAlive).size()) : 0;
            applyModifier(attackDamage, GALAXY_RAILWAY_ATTACK_MODIFIER,
                    "starrail_sim.galaxy_railway.meteor_attack",
                    galaxyRailwayActive
                            ? meteorAttackPerEnemy(galaxyRailwaySuperimposition) * nearbyEnemies
                            : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, BEFORE_DAWN_ATTACK_MODIFIER,
                    "starrail_sim.before_dawn.attack",
                    beforeDawnActive
                            ? beforeDawnAttack(beforeDawnSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            int promiseStackCount = priceOfPeaceActive ? promiseStacks(player) : 0;
            applyModifier(attackDamage, PROMISE_ATTACK_MODIFIER,
                    "starrail_sim.price_of_peace.promise_attack",
                    priceOfPeaceActive
                            ? promiseAttack(priceOfPeaceSuperimposition) * promiseStackCount
                            : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            int deconstructionStackCount = towardsUnanswerableActive
                    ? deconstructionStacks(player) : 0;
            applyModifier(attackDamage, DECONSTRUCTION_ATTACK_MODIFIER,
                    "starrail_sim.towards_unanswerable.deconstruction_attack",
                    towardsUnanswerableActive
                            ? deconstructionAttack(towardsUnanswerableSuperimposition)
                            * deconstructionStackCount : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            int thunderEscapeStackCount = ninjaScrollActive
                    ? thunderEscapeStacks(player) : 0;
            applyModifier(attackDamage, THUNDER_ESCAPE_ATTACK_MODIFIER,
                    "starrail_sim.ninja_scroll.thunder_escape_attack",
                    ninjaScrollActive
                            ? thunderEscapeAttack(ninjaScrollSuperimposition)
                            * thunderEscapeStackCount : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, ALCHEMY_ATTACK_MODIFIER,
                    "starrail_sim.life_as_a_light.alchemy_attack",
                    lifeAsALightActive && player.hasEffect(StarRailSimMod.ALCHEMY.get())
                            ? alchemyAttack(lifeAsALightSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            int departureStackCount = aStarIlluminatesNightSkyActive
                    ? departureStacks(player) : 0;
            applyModifier(attackDamage, DEPARTURE_ATTACK_MODIFIER,
                    "starrail_sim.a_star_illuminates_night_sky.departure_attack",
                    aStarIlluminatesNightSkyActive
                            && departureStackCount >= DEPARTURE_MAX_STACKS
                            ? departureAttack(aStarIlluminatesNightSkySuperimposition)
                                    * departureStackCount : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, FOAM_ECHO_ATTACK_MODIFIER,
                    "starrail_sim.on_the_shore_in_the_flow_of_time.foam_echo_attack",
                    onTheShoreActive && foamEcho != null
                            ? shoreAttackPerStack(foamEchoSuperimposition) * foamEchoStacks
                            : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, SHINING_CROWN_ATTACK_MODIFIER,
                    "starrail_sim.sparkle_quietly_shines.shining_crown_attack",
                    sparkleQuietlyShinesActive
                            && player.hasEffect(StarRailSimMod.SHINING_CROWN.get())
                            ? shiningCrownAttack(sparkleQuietlyShinesSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, INHERITANCE_ATTACK_MODIFIER,
                    "starrail_sim.battle_isnt_over.inheritance_attack",
                    inheritanceSuperimposition > 0
                            ? inheritanceAttack(inheritanceSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, PLUM_FRAGRANCE_ATTACK_MODIFIER,
                    "starrail_sim.memory_of_me.plum_fragrance_attack",
                    plumFragranceStackCount > 0
                            ? plumFragranceAttack(plumFragranceSuperimposition)
                                    * plumFragranceStackCount : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, NIGHT_FLOWING_COLORS_CHANT_ATTACK_MODIFIER,
                    "starrail_sim.night_flowing_colors.chant_attack",
                    chantStacks > 0 ? nightFlowingColorsChantAttack(chantSuperimposition)
                            * chantStacks : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, NIGHT_FLOWING_COLORS_SPLENDOR_ATTACK_MODIFIER,
                    "starrail_sim.night_flowing_colors.splendor_attack",
                    splendorOwner
                            ? nightFlowingColorsSplendorAttack(splendorSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, PSALM_ATTACK_MODIFIER,
                    "starrail_sim.returning_to_earth.psalm_attack",
                    psalmStacks > 0
                            ? psalmAttackPerStack(psalmSuperimposition) * psalmStacks : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, GOLDEN_BLOOD_ATTACK_MODIFIER,
                    "starrail_sim.an_age_etched_in_golden_blood.attack",
                    goldenBloodActive ? goldenBloodAttack(
                            AnAgeEtchedInGoldenBloodItem.getSuperimposition(goldenBlood))
                            : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, LAW_ATTACK_MODIFIER,
                    "starrail_sim.an_age_etched_in_golden_blood.law_attack",
                    law != null ? goldenBloodLawAttack(lawSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, WILL_ATTACK_MODIFIER,
                    "starrail_sim.in_the_name_of_the_world.will_attack",
                    will != null ? inTheNameWillAttack(willSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            int knightlyCourtesyStacks = momentOfGloryActive
                    ? knightlyCourtesyStacks(player) : 0;
            applyModifier(attackDamage, MOMENT_OF_GLORY_ATTACK_MODIFIER,
                    "starrail_sim.moment_of_glory.knightly_courtesy_attack",
                    momentOfGloryActive
                            ? knightlyCourtesyAttack(momentOfGlorySuperimposition)
                            * knightlyCourtesyStacks : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, ECHOES_OF_THE_COFFIN_ATTACK_MODIFIER,
                    "starrail_sim.echoes_of_the_coffin.attack",
                    echoesOfTheCoffinActive ? echoesOfTheCoffinAttack(
                            EchoesOfTheCoffinItem.getSuperimposition(echoesOfTheCoffin)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, DEEP_BREATH_ATTACK_MODIFIER,
                    "starrail_sim.night_of_fright.deep_breath_attack",
                    deepBreathStacks > 0 ? deepBreathAttackPerStack(
                            deepBreathSuperimposition) * deepBreathStacks : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, WEAVE_TIME_INTO_GOLD_ATTACK_MODIFIER,
                    "starrail_sim.weave_time_into_gold.attack",
                    weaveTimeIntoGoldActive ? weaveTimeIntoGoldAttack(
                            weaveTimeIntoGoldSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, FAREWELL_ATTACK_MODIFIER,
                    "starrail_sim.make_farewell_more_beautiful.attack",
                    makeFarewellMoreBeautifulActive ? farewellAttack(
                            farewellSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, LOVE_IS_ETERNAL_ATTACK_MODIFIER,
                    "starrail_sim.love_is_eternal.attack",
                    loveIsEternalActive ? loveIsEternalAttack(
                            LoveIsEternalItem.getSuperimposition(loveIsEternal)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, STARLIGHT_FOR_LONG_NIGHTS_ATTACK_MODIFIER,
                    "starrail_sim.starlight_for_long_nights.attack",
                    starlightForLongNightsActive ? starlightForLongNightsAttack(
                            StarlightForLongNightsItem.getSuperimposition(
                                    starlightForLongNights)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, WELCOME_TO_GALACTIC_CITY_ATTACK_MODIFIER,
                    "starrail_sim.welcome_to_galactic_city.attack",
                    welcomeToGalacticCityActive ? welcomeToGalacticCityAttack(
                            WelcomeToGalacticCityItem.getSuperimposition(
                                    welcomeToGalacticCity)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, MEET_IN_THE_NEXT_FLOWER_SEASON_ATTACK_MODIFIER,
                    "starrail_sim.meet_in_the_next_flower_season.attack",
                    meetInTheNextFlowerSeasonActive ? meetInTheNextFlowerSeasonAttack(
                            MeetInTheNextFlowerSeasonItem.getSuperimposition(
                                    meetInTheNextFlowerSeason)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(attackDamage, WHEN_SHE_DECIDES_TO_SEE_ATTACK_MODIFIER,
                    "starrail_sim.when_she_decides_to_see.attack",
                    whenSheDecidesToSeeActive ? whenSheDecidesToSeeAttack(
                            WhenSheDecidesToSeeItem.getSuperimposition(
                                    whenSheDecidesToSee)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
        }

        AttributeInstance critDamage = player.getAttribute(StarRailAttributes.CRIT_DAMAGE.get());
        if (critDamage != null) {
            int verseLevel = verse == null ? 1 : boundedSuperimposition(
                    player.getPersistentData().getInt(
                            LOVE_IS_ETERNAL_VERSE_SUPERIMPOSITION_TAG));
            applyModifier(critDamage, LOVE_IS_ETERNAL_VERSE_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.love_is_eternal.verse_crit_damage",
                    verse == null ? 0.0D : loveIsEternalVerseCritDamage(verseLevel)
                            * (loveIsEternalVerseEnhanced(player)
                                    ? 1.0D + loveIsEternalSynergy(verseLevel) : 1.0D),
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, BROCADE_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.weave_time_into_gold.brocade_crit_damage",
                    weaveTimeIntoGoldActive
                            ? weaveTimeIntoGoldCritDamage(weaveTimeIntoGoldSuperimposition)
                                    * brocadeStacks : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, FATE_NEVER_FAIR_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.fate_never_fair.crit_damage",
                    fateNeverFairActive && fateCritDamageUntil > player.level().getGameTime()
                            ? fateNeverFairCritDamage(FateNeverFairItem
                                    .getSuperimposition(fateNeverFair)) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, DO_NOT_FORGET_HER_FLAME_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.do_not_forget_her_flame.burning_crit_damage",
                    doNotForgetHerFlameActive
                            && player.hasEffect(StarRailSimMod.BURNING_SELF.get())
                            ? doNotForgetHerFlameCritDamageWhileBurning(
                                    DoNotForgetHerFlameItem.getSuperimposition(
                                            doNotForgetHerFlame)) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, IN_THE_NIGHT_STACK_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.in_the_night.night_butterfly_crit_damage",
                    nightActive
                            ? critDamagePerStack(nightSuperimposition) * nightStacks : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, I_WILL_HUNT_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.i_will_hunt.crit_damage",
                    huntActive ? pursuitCritDamage(huntSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, SLEEP_LIKE_THE_DEAD_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.sleep_like_the_dead.crit_damage",
                    sleepActive
                            ? sleepLikeTheDeadCritDamage(sleepSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, PURE_THOUGHT_BAPTISM_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.pure_thought_baptism.crit_damage",
                    pureActive ? pureThoughtBaptismCritDamage(pureSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, BRIGHTER_THAN_THE_SUN_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.brighter_than_the_sun.dragon_roar_crit_damage",
                    brighterActive
                            ? dragonRoarCritDamage(brighterSuperimposition)
                            * dragonRoarStackCount : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, DANCE_AT_SUNSET_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.dance_at_sunset.crit_damage",
                    danceActive ? danceAtSunsetCritDamage(danceSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, THIS_BODY_AS_SWORD_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.this_body_as_sword.crit_damage",
                    thisBodyActive ? moonEclipseCritDamage(thisBodySuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, NO_REWARD_CROWNING_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.no_reward_crowning.crit_damage",
                    noRewardCrowningActive
                            ? noRewardCrowningCritDamage(noRewardCrowningSuperimposition)
                            : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, WHAT_YOU_SEE_IS_ME_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.what_you_see_is_me.kingly_entertainment_crit_damage",
                    whatYouSeeIsMeActive
                            && player.hasEffect(StarRailSimMod.KINGLY_ENTERTAINMENT.get())
                            ? kinglyEntertainmentCritDamage(whatYouSeeIsMeSuperimposition)
                            : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, MOMENT_OF_GLORY_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.moment_of_glory.crit_damage",
                    momentOfGloryActive
                            ? momentOfGloryCritDamage(momentOfGlorySuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, BEFORE_DAWN_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.before_dawn.crit_damage",
                    beforeDawnActive
                            ? beforeDawnCritDamage(beforeDawnSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, NINJA_SCROLL_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.ninja_scroll.crit_damage",
                    ninjaScrollBreakEffectActive
                            ? ninjaScrollCritDamage(ninjaScrollSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, LIFE_AS_A_LIGHT_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.life_as_a_light.crit_damage",
                    lifeAsALightActive
                            ? lifeAsALightCritDamage(lifeAsALightSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, A_STAR_ILLUMINATES_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.a_star_illuminates_night_sky.crit_damage",
                    aStarIlluminatesNightSkyActive
                            ? aStarIlluminatesCritDamage(
                                    aStarIlluminatesNightSkySuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, GAME_OF_COSMIC_WORLDS_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.game_of_cosmic_worlds.crit_damage",
                    gameOfCosmicWorldsActive ? gameOfCosmicWorldsCritDamage(
                            GameOfCosmicWorldsItem.getSuperimposition(gameOfCosmicWorlds))
                            : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, GAME_OF_COSMIC_WORLDS_MASK_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.game_of_cosmic_worlds.mask_crit_damage",
                    gameMaskRecipient ? gameOfCosmicWorldsMaskCritDamage(
                            gameMaskSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, EDICT_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.if_time_were_a_flower.edict_crit_damage",
                    edict != null ? ifTimeWereAFlowerCritDamageAura(edictSuperimposition)
                            : ifTimeWereAFlowerActive ? ifTimeWereAFlowerCritDamage(
                                    IfTimeWereAFlowerItem.getSuperimposition(ifTimeWereAFlower))
                            : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, SHORE_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.on_the_shore_in_the_flow_of_time.crit_damage",
                    onTheShoreActive
                            ? shoreCritDamage(OnTheShoreInTheFlowOfTimeItem
                                    .getSuperimposition(onTheShore)) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, WINNING_STREAK_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.welcome_to_galactic_city.winning_streak_crit_damage",
                    welcomeToGalacticCityActive && winningStreakStacks > 0
                            ? winningStreakCritDamagePerStack(
                                    winningStreakSuperimposition) * winningStreakStacks
                            : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, MEET_IN_THE_NEXT_FLOWER_SEASON_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.meet_in_the_next_flower_season.crit_damage",
                    meetInTheNextFlowerSeasonActive ? meetInTheNextFlowerSeasonCritDamage(
                            MeetInTheNextFlowerSeasonItem.getSuperimposition(
                                    meetInTheNextFlowerSeason)) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, BEST_FORTUNE_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.when_she_decides_to_see.best_fortune_crit_damage",
                    bestFortune != null
                            ? bestFortuneCritDamage(bestFortuneSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, FLOWER_WORLD_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.flower_world_mesmerizing_eyes.crit_damage",
                    flowerWorldMesmerizingEyesActive ? flowerWorldCritDamage(
                            FlowerWorldMesmerizingEyesItem.getSuperimposition(
                                    flowerWorldMesmerizingEyes)) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critDamage, PUSH_STREAM_CRIT_DAMAGE_MODIFIER,
                    "starrail_sim.flower_world_mesmerizing_eyes.push_stream_crit_damage",
                    flowerWorldMesmerizingEyesActive && pushStreamStacks > 0
                            ? pushStreamCritDamagePerStack(pushStreamSuperimposition)
                                    * pushStreamStacks : 0.0D,
                    AttributeModifier.Operation.ADDITION);
        }

        if (critRate != null) {
            applyModifier(critRate, I_WILL_HUNT_CRIT_RATE_MODIFIER,
                    "starrail_sim.i_will_hunt.crit_rate",
                    huntActive ? pursuitCritRate(huntSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critRate, WORRISOME_BLISSFUL_CRIT_RATE_MODIFIER,
                    "starrail_sim.worrisome_blissful.crit_rate",
                    worrisomeActive
                            ? worrisomeBlissfulCritRate(worrisomeSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critRate, BEAUTIFUL_DREAM_CRIT_RATE_MODIFIER,
                    "starrail_sim.sleep_like_the_dead.beautiful_dream_crit_rate",
                    sleepActive && player.hasEffect(StarRailSimMod.BEAUTIFUL_DREAM.get())
                            ? beautifulDreamCritRate(sleepSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critRate, IDEAL_BURNING_HELL_CRIT_RATE_MODIFIER,
                    "starrail_sim.ideal_burning_hell.crit_rate",
                    idealActive ? rangerCritRate(idealSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critRate, FINALE_OF_A_LIE_CRIT_RATE_MODIFIER,
                    "starrail_sim.finale_of_a_lie.crit_rate",
                    finaleActive ? finaleOfALieCritRate(finaleSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critRate, BRIGHTER_THAN_THE_SUN_CRIT_RATE_MODIFIER,
                    "starrail_sim.brighter_than_the_sun.crit_rate",
                    brighterActive
                            ? brighterThanTheSunCritRate(brighterSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critRate, THE_UNREACHABLE_SIDE_CRIT_RATE_MODIFIER,
                    "starrail_sim.the_unreachable_side.crit_rate",
                    unreachableActive
                            ? noRetreatCritRate(unreachableSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critRate, PRICE_OF_PEACE_CRIT_RATE_MODIFIER,
                    "starrail_sim.price_of_peace.crit_rate",
                    priceOfPeaceActive
                            ? priceOfPeaceCritRate(priceOfPeaceSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critRate, TOWARDS_UNANSWERABLE_CRIT_RATE_MODIFIER,
                    "starrail_sim.towards_unanswerable.crit_rate",
                    towardsUnanswerableActive
                            ? towardsUnanswerableCritRate(towardsUnanswerableSuperimposition)
                            : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critRate, SPARKLE_QUIETLY_SHINES_CRIT_RATE_MODIFIER,
                    "starrail_sim.sparkle_quietly_shines.crit_rate",
                    sparkleQuietlyShinesActive
                            ? sparkleQuietlyShinesCritRate(sparkleQuietlyShinesSuperimposition)
                            : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critRate, GAME_OF_COSMIC_WORLDS_MASK_CRIT_RATE_MODIFIER,
                    "starrail_sim.game_of_cosmic_worlds.mask_crit_rate",
                    gameMaskRecipient ? gameOfCosmicWorldsMaskCritRate(
                            gameMaskSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critRate, EDICT_CRIT_RATE_MODIFIER,
                    "starrail_sim.if_time_were_a_flower.edict_crit_rate",
                    edict != null ? ifTimeWereAFlowerCritRate(edictSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(critRate, BEST_FORTUNE_CRIT_RATE_MODIFIER,
                    "starrail_sim.when_she_decides_to_see.best_fortune_crit_rate",
                    bestFortune != null
                            ? bestFortuneCritRate(bestFortuneSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
        }

        int winterShieldLevel = winterShield == null ? 1
                : Math.max(1, Math.min(5, winterShield.getAmplifier() + 1));
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            applyModifier(armor, FATE_NEVER_FAIR_ARMOR_MODIFIER,
                    "starrail_sim.fate_never_fair.armor",
                    fateNeverFairActive ? fateNeverFairArmor(FateNeverFairItem
                            .getSuperimposition(fateNeverFair)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(armor, THOUGH_RIVERS_ARMOR_MODIFIER,
                    "starrail_sim.though_rivers_and_mountains.armor",
                    thoughRiversAndMountainsActive
                            ? thoughRiversArmor(ThoughRiversAndMountainsItem
                                    .getSuperimposition(thoughRiversAndMountains)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(armor, CLOSED_EYES_ARMOR_MODIFIER,
                    "starrail_sim.she_has_closed_her_eyes.armor",
                    sheHasClosedHerEyesActive
                            ? closedEyesArmor(closedEyesSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(armor, MOMENT_OF_VICTORY_ARMOR_MODIFIER,
                    "starrail_sim.moment_of_victory.armor",
                    momentOfVictoryActive ? momentOfVictoryArmor(
                            MomentOfVictoryItem.getSuperimposition(momentOfVictory)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(armor, WINTER_SHIELD_ARMOR_MODIFIER,
                    "starrail_sim.winter_shield.armor",
                    momentOfVictoryActive && winterShield != null
                            ? winterShieldBonus(winterShieldLevel) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        AttributeInstance armorToughness = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armorToughness != null) {
            applyModifier(armorToughness, WINTER_SHIELD_TOUGHNESS_MODIFIER,
                    "starrail_sim.winter_shield.armor_toughness",
                    momentOfVictoryActive && winterShield != null
                            ? winterShieldBonus(winterShieldLevel) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
        }

        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            applyModifier(maxHealth, MAY_RAINBOW_MAX_HEALTH_MODIFIER,
                    "starrail_sim.may_rainbow_stay_in_the_sky.max_health",
                    mayRainbowStayInTheSkyActive ? mayRainbowMaxHealth(
                            MayRainbowStayInTheSkyItem.getSuperimposition(
                                    mayRainbowStayInTheSky)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(maxHealth, STARLIGHT_FOR_LONG_NIGHTS_MAX_HEALTH_MODIFIER,
                    "starrail_sim.starlight_for_long_nights.max_health",
                    starlightForLongNightsActive ? starlightForLongNightsMaxHealth(
                            StarlightForLongNightsItem.getSuperimposition(
                                    starlightForLongNights)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(maxHealth, CLOSED_EYES_MAX_HEALTH_MODIFIER,
                    "starrail_sim.she_has_closed_her_eyes.max_health",
                    sheHasClosedHerEyesActive ? closedEyesArmor(closedEyesSuperimposition)
                            : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(maxHealth, NEW_FLESH_OF_INFERNO_MAX_HEALTH_MODIFIER,
                    "starrail_sim.new_flesh_of_inferno.max_health",
                    newFleshOfInfernoActive ? newFleshOfInfernoMaxHealth(
                            NewFleshOfInfernoItem.getSuperimposition(newFleshOfInferno)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(maxHealth, THE_UNREACHABLE_SIDE_MAX_HEALTH_MODIFIER,
                    "starrail_sim.the_unreachable_side.max_health",
                    unreachableActive
                            ? noRetreatMaxHealth(unreachableSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(maxHealth, BLOOD_FIRE_MAX_HEALTH_MODIFIER,
                    "starrail_sim.blood_fire_burning_path.max_health",
                    bloodFireActive
                            ? bloodFireMaxHealth(bloodFireSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(maxHealth, ECHOES_OF_THE_COFFIN_HEALTH_MODIFIER,
                    "starrail_sim.echoes_of_the_coffin.health",
                    echoesOfTheCoffinActive ? echoesOfTheCoffinHealth(
                            EchoesOfTheCoffinItem.getSuperimposition(echoesOfTheCoffin)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(maxHealth, TIME_WAITS_FOR_NO_ONE_MAX_HEALTH_MODIFIER,
                    "starrail_sim.time_waits_for_no_one.max_health",
                    timeWaitsForNoOneActive ? timeWaitsForNoOneMaxHealth(
                            TimeWaitsForNoOneItem.getSuperimposition(timeWaitsForNoOne))
                            : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(maxHealth, NIGHT_OF_FRIGHT_MAX_HEALTH_MODIFIER,
                    "starrail_sim.night_of_fright.max_health",
                    nightOfFrightActive ? nightOfFrightMaxHealth(
                            NightOfFrightItem.getSuperimposition(nightOfFright)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(maxHealth, ONLY_THE_SCENT_REMAINS_MAX_HEALTH_MODIFIER,
                    "starrail_sim.only_the_scent_remains.max_health",
                    onlyTheScentRemainsActive ? onlyTheScentRemainsMaxHealth(
                            OnlyTheScentRemainsItem.getSuperimposition(
                                    onlyTheScentRemains)) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(maxHealth, FAREWELL_MAX_HEALTH_MODIFIER,
                    "starrail_sim.make_farewell_more_beautiful.max_health",
                    makeFarewellMoreBeautifulActive ? farewellMaxHealth(
                            farewellSuperimposition) : 0.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        AttributeInstance healingEffect = player.getAttribute(
                StarRailAttributes.HEALING_EFFECT.get());
        if (healingEffect != null) {
            applyModifier(healingEffect, MAY_RAINBOW_HEALING_MODIFIER,
                    "starrail_sim.may_rainbow_stay_in_the_sky.healing",
                    mayRainbowStayInTheSkyActive ? mayRainbowHealing(
                            MayRainbowStayInTheSkyItem.getSuperimposition(
                                    mayRainbowStayInTheSky)) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(healingEffect, TIME_WAITS_FOR_NO_ONE_HEALING_MODIFIER,
                    "starrail_sim.time_waits_for_no_one.healing",
                    timeWaitsForNoOneActive ? timeWaitsForNoOneHealing(
                            TimeWaitsForNoOneItem.getSuperimposition(timeWaitsForNoOne))
                            : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(healingEffect, BLOOD_FIRE_HEALING_MODIFIER,
                    "starrail_sim.blood_fire_burning_path.healing",
                    bloodFireActive
                            ? bloodFireHealing(bloodFireSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
        }
        AttributeInstance effectHitRate = player.getAttribute(
                StarRailAttributes.EFFECT_HIT_RATE.get());
        if (effectHitRate != null) {
            applyModifier(effectHitRate, OCEAN_WHY_SINGS_EFFECT_HIT_MODIFIER,
                    "starrail_sim.ocean_why_sings.effect_hit",
                    oceanWhySingsActive ? oceanWhySingsEffectHit(
                            OceanWhySingsItem.getSuperimposition(oceanWhySings)) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(effectHitRate, RAIN_NEVER_STOPS_EFFECT_HIT_MODIFIER,
                    "starrail_sim.rain_never_stops.effect_hit",
                    rainNeverStopsActive ? rainNeverStopsEffectHit(
                            RainNeverStopsItem.getSuperimposition(rainNeverStops)) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(effectHitRate, WILL_EFFECT_HIT_MODIFIER,
                    "starrail_sim.in_the_name_of_the_world.will_effect_hit",
                    will != null ? inTheNameWillEffectHit(willSuperimposition) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(effectHitRate, THOUSAND_SPRINGS_EFFECT_HIT_MODIFIER,
                    "starrail_sim.a_thousand_fold_spring.effect_hit",
                    thousandSpringsActive ? thousandSpringsEffectHit(
                            AThousandFoldSpringItem.getSuperimposition(thousandSprings)) : 0.0D,
                    AttributeModifier.Operation.ADDITION);
            applyModifier(effectHitRate, REFORGED_REMEMBRANCE_EFFECT_HIT_MODIFIER,
                    "starrail_sim.reforged_remembrance.effect_hit",
                    reforgedRemembranceActive ? reforgedRemembranceEffectHit(
                            ReforgedRemembranceItem.getSuperimposition(reforgedRemembrance))
                            : 0.0D,
                    AttributeModifier.Operation.ADDITION);
        }
    }

    /** Applies kill-triggered light-cone effects and refreshes their timers. */
    public static void onMonsterKilled(ServerPlayer player) {
        triggerThoughRiversAndMountains(player);
        triggerNightOfFright(player);
        triggerLoveIsEternalPoem(player);
        ItemStack echoesOfTheCoffin = getEquippedEchoesOfTheCoffin(player);
        if (echoesOfTheCoffin != null && isAbundancePath(player)) {
            int superimposition = EchoesOfTheCoffinItem.getSuperimposition(
                    echoesOfTheCoffin);
            player.getPersistentData().putInt(THORN_SUPERIMPOSITION_TAG, superimposition);
            player.removeEffect(StarRailSimMod.THORN.get());
            player.addEffect(new MobEffectInstance(StarRailSimMod.THORN.get(),
                    THORN_DURATION, 0, false, true, true));
        }
        boolean huntPath = isHuntPath(player);
        ItemStack nightStack = getEquippedInTheNight(player);
        ItemStack pureStack = getEquippedPureThoughtBaptism(player);
        ItemStack nightFlowingColorsStack = getEquippedNightFlowingColors(player);
        ItemStack gameOfCosmicWorldsStack = getEquippedGameOfCosmicWorlds(player);
        if (nightFlowingColorsStack != null && isHarmonyPath(player)) {
            int superimposition = NightFlowingColorsItem.getSuperimposition(
                    nightFlowingColorsStack);
            player.removeEffect(StarRailSimMod.CHANT.get());
            player.getPersistentData().remove(NIGHT_FLOWING_COLORS_CHANT_SUPERIMPOSITION_TAG);
            for (ServerPlayer recipient : player.serverLevel().getEntitiesOfClass(
                    ServerPlayer.class, player.getBoundingBox().inflate(10.0D),
                    target -> target.isAlive() && target.distanceToSqr(player) <= 100.0D)) {
                recipient.removeEffect(StarRailSimMod.SPLENDOR.get());
                recipient.getPersistentData().putInt(
                        NIGHT_FLOWING_COLORS_SPLENDOR_SUPERIMPOSITION_TAG, superimposition);
                recipient.getPersistentData().putString(
                        NIGHT_FLOWING_COLORS_SPLENDOR_OWNER_TAG, player.getStringUUID());
                recipient.addEffect(new MobEffectInstance(StarRailSimMod.SPLENDOR.get(),
                        NIGHT_FLOWING_COLORS_SPLENDOR_DURATION, 0, false, true, true));
            }
        }
        if (gameOfCosmicWorldsStack != null && isHarmonyPath(player)) {
            triggerGameOfCosmicWorldsMask(player,
                    GameOfCosmicWorldsItem.getSuperimposition(gameOfCosmicWorldsStack));
        }
        if (huntPath && nightStack != null) {
            int currentStacks = nightButterflyStacks(player);
            int nextStacks = Math.min(NIGHT_BUTTERFLY_MAX_STACKS, currentStacks + 1);
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.NIGHT_BUTTERFLY.get(),
                    NIGHT_BUTTERFLY_DURATION,
                    nextStacks - 1,
                    false,
                    true,
                    true));
            StarRailPathMessages.send(player, StarRailPath.HUNT, Component.translatable(
                    "message.starrail_sim.night_butterfly", nextStacks,
                    NIGHT_BUTTERFLY_MAX_STACKS));
        }
        if (huntPath && pureStack != null) {
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.DEBATE.get(), DEBATE_DURATION, 0,
                    false, true, true));
        }
        ItemStack ninjaScrollStack = getEquippedNinjaScroll(player);
        if (isEruditionPath(player) && ninjaScrollStack != null) {
            int currentStacks = thunderEscapeStacks(player);
            int nextStacks = Math.min(THUNDER_ESCAPE_MAX_STACKS, currentStacks + 1);
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.THUNDER_ESCAPE.get(), THUNDER_ESCAPE_DURATION,
                    nextStacks - 1, false, true, true));
            StarRailPathMessages.send(player, StarRailPath.ERUDITION,
                    Component.translatable("message.starrail_sim.thunder_escape",
                            nextStacks, THUNDER_ESCAPE_MAX_STACKS));
        }
        ItemStack sparkleQuietlyShinesStack = getEquippedSparkleQuietlyShines(player);
        if (isEruditionPath(player) && sparkleQuietlyShinesStack != null) {
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.SHINING_CROWN.get(), SHINING_CROWN_DURATION,
                    0, false, true, true));
            StarRailPathMessages.send(player, StarRailPath.ERUDITION,
                    Component.translatable("message.starrail_sim.shining_crown"));
        }
        ItemStack familyStack = getEquippedSomethingIrreplaceable(player);
        if (isDestructionPath(player) && familyStack != null) {
            triggerFamily(player, familyStack);
        }
        ItemStack dawnStack = getEquippedDawnBurnsJustSo(player);
        if (isDestructionPath(player) && dawnStack != null) {
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.BLAZING_SUN.get(), BLAZING_SUN_DURATION,
                    0, false, true, true));
            StarRailPathMessages.send(player, StarRailPath.DESTRUCTION,
                    Component.translatable("message.starrail_sim.blazing_sun"));
        }
        ItemStack kinglyStack = getEquippedWhatYouSeeIsMe(player);
        if (isDestructionPath(player) && kinglyStack != null) {
            player.getPersistentData().putBoolean(KINGLY_ENTERTAINMENT_NEXT_ATTACK_TAG, true);
            StarRailPathMessages.send(player, StarRailPath.DESTRUCTION,
                    Component.translatable("message.starrail_sim.kingly_entertainment_next"));
        }
        refresh(player);
    }

    /** Triggers the Destruction light cone when its wearer is hit by a hostile mob. */
    public static void onPlayerAttacked(ServerPlayer player) {
        ItemStack familyStack = getEquippedSomethingIrreplaceable(player);
        if (familyStack != null && isDestructionPath(player)) {
            triggerFamily(player, familyStack);
            return;
        }
        refresh(player);
    }

    /** Triggers The Unreachable Side after the wearer loses health. */
    public static void onPlayerDamaged(ServerPlayer player) {
        onFateNeverFairDamaged(player);
        ItemStack stack = getEquippedTheUnreachableSide(player);
        if (stack != null && isDestructionPath(player)) {
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.NO_RETREAT.get(), NO_RETREAT_DURATION,
                    0, false, true, true));
            StarRailPathMessages.send(player, StarRailPath.DESTRUCTION,
                    Component.translatable("message.starrail_sim.no_retreat"));
        }
        refresh(player);
    }

    /** Grants Winter Shield after a direct attack hits the cone wearer. */
    public static void onMomentOfVictoryAttacked(ServerPlayer player) {
        ItemStack stack = getEquippedMomentOfVictory(player);
        if (stack == null || !isPreservationPath(player)) {
            return;
        }
        player.removeEffect(StarRailSimMod.WINTER_SHIELD.get());
        player.addEffect(new MobEffectInstance(StarRailSimMod.WINTER_SHIELD.get(),
                WINTER_SHIELD_DURATION,
                MomentOfVictoryItem.getSuperimposition(stack) - 1,
                false, true, true));
        refresh(player);
    }

    /** Grants the damage aura when the Preservation cone wearer actually loses health. */
    public static void onSheHasClosedHerEyesHealthLost(ServerPlayer wearer) {
        ItemStack stack = getEquippedSheHasClosedHerEyes(wearer);
        if (stack == null || !isPreservationPath(wearer)) {
            return;
        }
        int superimposition = SheHasClosedHerEyesItem.getSuperimposition(stack);
        for (ServerPlayer recipient : wearer.serverLevel().getEntitiesOfClass(
                ServerPlayer.class, wearer.getBoundingBox().inflate(10.0D),
                candidate -> candidate.isAlive()
                        && candidate.distanceToSqr(wearer) <= 100.0D)) {
            recipient.removeEffect(StarRailSimMod.VISION.get());
            recipient.getPersistentData().putInt(VISION_SUPERIMPOSITION_TAG,
                    superimposition);
            recipient.addEffect(new MobEffectInstance(StarRailSimMod.VISION.get(),
                    VISION_DURATION, 0, false, true, true));
        }
    }

    /** Runs the one-minute missing-health recovery while the cone is path-active. */
    public static void tickSheHasClosedHerEyes(ServerPlayer wearer) {
        ItemStack stack = getEquippedSheHasClosedHerEyes(wearer);
        if (stack == null || !isPreservationPath(wearer)) {
            wearer.getPersistentData().remove(CLOSED_EYES_NEXT_HEAL_TICK_TAG);
            return;
        }
        long now = wearer.level().getGameTime();
        long nextHeal = wearer.getPersistentData().getLong(CLOSED_EYES_NEXT_HEAL_TICK_TAG);
        if (nextHeal <= 0L) {
            wearer.getPersistentData().putLong(CLOSED_EYES_NEXT_HEAL_TICK_TAG,
                    now + CLOSED_EYES_HEAL_INTERVAL);
            return;
        }
        if (now < nextHeal) {
            return;
        }

        double healFraction = closedEyesMissingHealthHeal(
                SheHasClosedHerEyesItem.getSuperimposition(stack));
        for (ServerPlayer recipient : wearer.serverLevel().getEntitiesOfClass(
                ServerPlayer.class, wearer.getBoundingBox().inflate(10.0D),
                candidate -> candidate.isAlive()
                        && candidate.distanceToSqr(wearer) <= 100.0D)) {
            float missingHealth = recipient.getMaxHealth() - recipient.getHealth();
            if (missingHealth > 0.0F) {
                recipient.heal((float) (missingHealth * healFraction));
            }
        }
        wearer.getPersistentData().putLong(CLOSED_EYES_NEXT_HEAL_TICK_TAG,
                now + CLOSED_EYES_HEAL_INTERVAL);
    }

    private static void triggerFamily(ServerPlayer player, ItemStack stack) {
        int superimposition = SomethingIrreplaceableItem.getSuperimposition(stack);
        double healing = StarRailAttributes.getAttackDamage(player)
                * familyHealing(superimposition);
        if (healing > 0.0D) {
            player.heal((float) healing);
        }
        MobEffectInstance current = player.getEffect(StarRailSimMod.FAMILY.get());
        int currentStacks = current == null ? 0
                : Math.max(0, Math.min(FAMILY_MAX_STACKS, current.getAmplifier() + 1));
        int nextStacks = Math.min(FAMILY_MAX_STACKS, currentStacks + 1);
        player.addEffect(new MobEffectInstance(
                StarRailSimMod.FAMILY.get(), FAMILY_DURATION, nextStacks - 1,
                false, true, true));
        StarRailPathMessages.send(player, StarRailPath.DESTRUCTION, Component.translatable(
                "message.starrail_sim.family", nextStacks, FAMILY_MAX_STACKS));
    }

    private static void triggerGameOfCosmicWorldsMask(ServerPlayer player,
                                                       int superimposition) {
        player.getPersistentData().putInt(GAME_OF_COSMIC_WORLDS_MASK_SUPERIMPOSITION_TAG,
                boundedSuperimposition(superimposition));
        player.getPersistentData().putString(GAME_OF_COSMIC_WORLDS_MASK_OWNER_TAG,
                player.getStringUUID());
        player.addEffect(new MobEffectInstance(StarRailSimMod.MASK.get(),
                GAME_OF_COSMIC_WORLDS_MASK_DURATION, 0, false, true, true));
        refresh(player);
    }

    private static void refreshGameOfCosmicWorldsAura(ServerPlayer wearer,
                                                       int superimposition) {
        for (ServerPlayer recipient : wearer.serverLevel().getEntitiesOfClass(
                ServerPlayer.class, wearer.getBoundingBox().inflate(10.0D),
                target -> target != wearer && target.isAlive()
                        && target.distanceToSqr(wearer) <= 100.0D)) {
            MobEffectInstance current = recipient.getEffect(StarRailSimMod.MASK.get());
            String currentOwner = recipient.getPersistentData().getString(
                    GAME_OF_COSMIC_WORLDS_MASK_OWNER_TAG);
            if (current != null && !wearer.getStringUUID().equals(currentOwner)
                    && current.getDuration() > 4) {
                continue;
            }
            recipient.getPersistentData().putInt(
                    GAME_OF_COSMIC_WORLDS_MASK_SUPERIMPOSITION_TAG,
                    boundedSuperimposition(superimposition));
            recipient.getPersistentData().putString(GAME_OF_COSMIC_WORLDS_MASK_OWNER_TAG,
                    wearer.getStringUUID());
            recipient.addEffect(new MobEffectInstance(StarRailSimMod.MASK.get(),
                    4, 0, false, true, true));
        }
    }

    private static void triggerIfTimeWereAFlowerEdict(ServerPlayer wearer,
                                                       int superimposition) {
        int bounded = boundedSuperimposition(superimposition);
        for (ServerPlayer recipient : wearer.serverLevel().getEntitiesOfClass(
                ServerPlayer.class, wearer.getBoundingBox().inflate(10.0D),
                target -> target.isAlive() && target.distanceToSqr(wearer) <= 100.0D)) {
            recipient.getPersistentData().putInt(EDICT_SUPERIMPOSITION_TAG, bounded);
            recipient.removeEffect(StarRailSimMod.EDICT.get());
            recipient.addEffect(new MobEffectInstance(StarRailSimMod.EDICT.get(),
                    EDICT_DURATION, 0, false, true, true));
            refresh(recipient);
        }
    }

    private static void triggerLaw(ServerPlayer wearer, int superimposition) {
        int bounded = boundedSuperimposition(superimposition);
        for (ServerPlayer recipient : wearer.serverLevel().getEntitiesOfClass(
                ServerPlayer.class, wearer.getBoundingBox().inflate(10.0D),
                target -> target.isAlive() && target.distanceToSqr(wearer) <= 100.0D)) {
            recipient.getPersistentData().putInt(LAW_SUPERIMPOSITION_TAG, bounded);
            recipient.removeEffect(StarRailSimMod.LAW.get());
            recipient.addEffect(new MobEffectInstance(StarRailSimMod.LAW.get(),
                    LAW_DURATION, 0, false, true, true));
            refresh(recipient);
        }
    }

    /** Adds attack-triggered stacks for the equipped light cones. */
    public static void onAttack(ServerPlayer player) {
        triggerLoveIsEternalBlank(player);
        triggerStarlitNight(player);
        onWelcomeToGalacticCityAttack(player);
        onMeetInTheNextFlowerSeasonAttack(player);
        onWhenSheDecidesToSeeAttack(player);
        onFlowerWorldMesmerizingEyesAttack(player);
        boolean huntPath = isHuntPath(player);
        boolean destructionPath = isDestructionPath(player);
        boolean eruditionPath = isEruditionPath(player);
        ItemStack battleIsntOverStack = getEquippedBattleIsntOver(player);
        ItemStack nightFlowingColorsStack = getEquippedNightFlowingColors(player);
        ItemStack gameOfCosmicWorldsStack = getEquippedGameOfCosmicWorlds(player);
        ItemStack returningToEarthStack = getEquippedReturningToEarth(player);
        ItemStack ifTimeWereAFlowerStack = getEquippedIfTimeWereAFlower(player);
        ItemStack goldenBloodStack = getEquippedAnAgeEtchedInGoldenBlood(player);
        ItemStack inTheNameStack = getEquippedInTheNameOfTheWorld(player);
        ItemStack onTheShoreStack = getEquippedOnTheShoreInTheFlowOfTime(player);
        if (nightFlowingColorsStack != null && isHarmonyPath(player)) {
            int currentStacks = chantStacks(player);
            int nextStacks = Math.min(NIGHT_FLOWING_COLORS_MAX_CHANT_STACKS,
                    currentStacks + 1);
            player.getPersistentData().putInt(NIGHT_FLOWING_COLORS_CHANT_SUPERIMPOSITION_TAG,
                    NightFlowingColorsItem.getSuperimposition(nightFlowingColorsStack));
            player.addEffect(new MobEffectInstance(StarRailSimMod.CHANT.get(),
                    Integer.MAX_VALUE, nextStacks - 1, false, true, true));
        }
        if (gameOfCosmicWorldsStack != null && isHarmonyPath(player)) {
            MobEffectInstance current = player.getEffect(StarRailSimMod.COLORFUL_FLAME.get());
            int currentStacks = current == null ? 0 : current.getAmplifier() + 1;
            int nextStacks = currentStacks + 1;
            if (nextStacks >= GAME_OF_COSMIC_WORLDS_FLAME_MAX_STACKS) {
                player.removeEffect(StarRailSimMod.COLORFUL_FLAME.get());
                triggerGameOfCosmicWorldsMask(player,
                        GameOfCosmicWorldsItem.getSuperimposition(gameOfCosmicWorldsStack));
            } else {
                player.addEffect(new MobEffectInstance(StarRailSimMod.COLORFUL_FLAME.get(),
                        Integer.MAX_VALUE, nextStacks - 1, false, true, true));
            }
        }
        if (returningToEarthStack != null && isHarmonyPath(player)) {
            MobEffectInstance current = player.getEffect(StarRailSimMod.PSALM.get());
            int currentStacks = current == null ? 0 : current.getAmplifier() + 1;
            int nextStacks = Math.min(PSALM_MAX_STACKS, currentStacks + 1);
            int superimposition = ReturningToEarthItem.getSuperimposition(
                    returningToEarthStack);
            for (ServerPlayer recipient : player.serverLevel().getEntitiesOfClass(
                    ServerPlayer.class, player.getBoundingBox().inflate(10.0D),
                    target -> target.isAlive() && target.distanceToSqr(player) <= 100.0D)) {
                recipient.getPersistentData().putInt(PSALM_SUPERIMPOSITION_TAG,
                        superimposition);
                recipient.removeEffect(StarRailSimMod.PSALM.get());
                recipient.addEffect(new MobEffectInstance(StarRailSimMod.PSALM.get(),
                        PSALM_DURATION, nextStacks - 1, false, true, true));
            }
        }
        if (ifTimeWereAFlowerStack != null && isHarmonyPath(player)) {
            triggerIfTimeWereAFlowerEdict(player,
                    IfTimeWereAFlowerItem.getSuperimposition(ifTimeWereAFlowerStack));
        }
        if (goldenBloodStack != null && isHarmonyPath(player)) {
            triggerLaw(player,
                    AnAgeEtchedInGoldenBloodItem.getSuperimposition(goldenBloodStack));
        }
        if (inTheNameStack != null && isNihilityPath(player)) {
            int superimposition = InTheNameOfTheWorldItem.getSuperimposition(inTheNameStack);
            player.getPersistentData().putInt(IN_THE_NAME_SUPERIMPOSITION_TAG,
                    boundedSuperimposition(superimposition));
            player.removeEffect(StarRailSimMod.WILL.get());
            player.addEffect(new MobEffectInstance(StarRailSimMod.WILL.get(),
                    LAW_DURATION, 0, false, true, true));
        }
        if (onTheShoreStack != null && isNihilityPath(player)) {
            MobEffectInstance current = player.getEffect(StarRailSimMod.FOAM_ECHO.get());
            int currentStacks = current == null ? 0 : current.getAmplifier() + 1;
            int nextStacks = Math.min(FOAM_ECHO_MAX_STACKS, currentStacks + 1);
            player.getPersistentData().putInt(FOAM_ECHO_SUPERIMPOSITION_TAG,
                    boundedSuperimposition(OnTheShoreInTheFlowOfTimeItem
                            .getSuperimposition(onTheShoreStack)));
            player.addEffect(new MobEffectInstance(StarRailSimMod.FOAM_ECHO.get(),
                    FOAM_ECHO_DURATION, nextStacks - 1, false, true, true));
        }
        if (battleIsntOverStack != null && isHarmonyPath(player)) {
            int amplifier = BattleIsntOverItem.getSuperimposition(battleIsntOverStack) - 1;
            boolean firstGrant = !player.hasEffect(StarRailSimMod.INHERITANCE.get());
            for (ServerPlayer recipient : player.serverLevel().getEntitiesOfClass(
                    ServerPlayer.class, player.getBoundingBox().inflate(10.0D),
                    target -> target.isAlive() && target.distanceToSqr(player) <= 100.0D)) {
                recipient.removeEffect(StarRailSimMod.INHERITANCE.get());
                recipient.addEffect(new MobEffectInstance(
                        StarRailSimMod.INHERITANCE.get(), INHERITANCE_DURATION, amplifier,
                        false, true, true));
            }
            if (firstGrant) {
                player.getPersistentData().putBoolean(
                        INHERITANCE_SKIP_CURRENT_HIT_TAG, true);
            }
        }
        ItemStack flowingStack = getEquippedIWillHunt(player);
        ItemStack rangerStack = getEquippedIdealBurningHell(player);
        if (huntPath && flowingStack != null) {
            int currentStacks = flowingLightStacks(player);
            int nextStacks = Math.min(FLOWING_LIGHT_MAX_STACKS, currentStacks + 1);
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.FLOWING_LIGHT.get(), FLOWING_LIGHT_DURATION,
                    nextStacks - 1, false, true, true));
            StarRailPathMessages.send(player, StarRailPath.HUNT, Component.translatable(
                    "message.starrail_sim.flowing_light", nextStacks,
                    FLOWING_LIGHT_MAX_STACKS));
        }
        if (huntPath && rangerStack != null) {
            int currentStacks = rangerStacks(player);
            int nextStacks = Math.min(RANGER_MAX_STACKS, currentStacks + 1);
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.RANGER.get(), RANGER_DURATION, nextStacks - 1,
                    false, true, true));
        }
        ItemStack finaleStack = getEquippedFinaleOfALie(player);
        if (huntPath && finaleStack != null) {
            int attackCount = player.getPersistentData()
                    .getInt(SHADOW_DEVOUR_ATTACK_COUNT_TAG) + 1;
            if (attackCount >= SHADOW_DEVOUR_ATTACKS_REQUIRED) {
                player.addEffect(new MobEffectInstance(
                        StarRailSimMod.SHADOW_DEVOUR.get(), SHADOW_DEVOUR_DURATION,
                        0, false, true, true));
                attackCount = 0;
            }
            player.getPersistentData().putInt(SHADOW_DEVOUR_ATTACK_COUNT_TAG, attackCount);
        }
        ItemStack brighterStack = getEquippedBrighterThanTheSun(player);
        if (destructionPath && brighterStack != null) {
            int currentStacks = dragonRoarStacks(player);
            int nextStacks = Math.min(DRAGON_ROAR_MAX_STACKS, currentStacks + 1);
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.DRAGON_ROAR.get(), DRAGON_ROAR_DURATION,
                    nextStacks - 1, false, true, true));
            StarRailPathMessages.send(player, StarRailPath.DESTRUCTION,
                    Component.translatable("message.starrail_sim.dragon_roar",
                            nextStacks, DRAGON_ROAR_MAX_STACKS));
        }
        ItemStack danceStack = getEquippedDanceAtSunset(player);
        if (destructionPath && danceStack != null) {
            int currentStacks = fireDanceStacks(player);
            int nextStacks = Math.min(FIRE_DANCE_MAX_STACKS, currentStacks + 1);
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.FIRE_DANCE.get(), FIRE_DANCE_DURATION,
                    nextStacks - 1, false, true, true));
            StarRailPathMessages.send(player, StarRailPath.DESTRUCTION,
                    Component.translatable("message.starrail_sim.fire_dance",
                            nextStacks, FIRE_DANCE_MAX_STACKS));
        }
        ItemStack swordStack = getEquippedThisBodyAsSword(player);
        if (destructionPath && swordStack != null) {
            triggerMoonEclipse(player, swordStack);
        }
        ItemStack crowningStack = getEquippedNoRewardCrowning(player);
        if (destructionPath && crowningStack != null) {
            player.getPersistentData().putBoolean(KNIGHT_KING_ATTACK_TAG, true);
        }
        ItemStack bloodFireStack = getEquippedBloodFireBurningPath(player);
        if (destructionPath && bloodFireStack != null) {
            triggerStrife(player, bloodFireStack);
        }
        ItemStack kinglyStack = getEquippedWhatYouSeeIsMe(player);
        if (destructionPath && kinglyStack != null) {
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.KINGLY_ENTERTAINMENT.get(),
                    KINGLY_ENTERTAINMENT_DURATION, 0, false, true, true));
        }
        ItemStack knightlyStack = getEquippedMomentOfGlory(player);
        if (eruditionPath && knightlyStack != null) {
            int currentStacks = knightlyCourtesyStacks(player);
            int nextStacks = Math.min(KNIGHTLY_COURTESY_MAX_STACKS, currentStacks + 1);
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.KNIGHTLY_COURTESY.get(),
                    KNIGHTLY_COURTESY_DURATION, nextStacks - 1,
                    false, true, true));
            StarRailPathMessages.send(player, StarRailPath.ERUDITION,
                    Component.translatable("message.starrail_sim.knightly_courtesy",
                            nextStacks, KNIGHTLY_COURTESY_MAX_STACKS));
        }
        ItemStack beforeDawnStack = getEquippedBeforeDawn(player);
        if (eruditionPath && beforeDawnStack != null) {
            int currentStacks = dreamBodyStacks(player);
            int nextStacks = Math.min(DREAM_BODY_MAX_STACKS, currentStacks + 1);
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.DREAM_BODY.get(), DREAM_BODY_DURATION, nextStacks - 1,
                    false, true, true));
            StarRailPathMessages.send(player, StarRailPath.ERUDITION,
                    Component.translatable("message.starrail_sim.dream_body",
                            nextStacks, DREAM_BODY_MAX_STACKS));
        }
        ItemStack priceOfPeaceStack = getEquippedPriceOfPeace(player);
        if (eruditionPath && priceOfPeaceStack != null) {
            int currentStacks = promiseStacks(player);
            int nextStacks = Math.min(PROMISE_MAX_STACKS, currentStacks + 1);
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.PROMISE.get(), PROMISE_DURATION, nextStacks - 1,
                    false, true, true));
            StarRailPathMessages.send(player, StarRailPath.ERUDITION,
                    Component.translatable("message.starrail_sim.promise",
                            nextStacks, PROMISE_MAX_STACKS));
        }
        ItemStack towardsUnanswerableStack = getEquippedTowardsUnanswerable(player);
        if (eruditionPath && towardsUnanswerableStack != null) {
            int currentStacks = deconstructionStacks(player);
            int nextStacks = Math.min(DECONSTRUCTION_MAX_STACKS, currentStacks + 1);
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.DECONSTRUCTION.get(), DECONSTRUCTION_DURATION,
                    nextStacks - 1, false, true, true));
            StarRailPathMessages.send(player, StarRailPath.ERUDITION,
                    Component.translatable("message.starrail_sim.deconstruction",
                            nextStacks, DECONSTRUCTION_MAX_STACKS));
        }
        ItemStack aStarIlluminatesNightSkyStack = getEquippedAStarIlluminatesNightSky(player);
        if (eruditionPath && aStarIlluminatesNightSkyStack != null) {
            int currentStacks = departureStacks(player);
            int nextStacks = Math.min(DEPARTURE_MAX_STACKS, currentStacks + 1);
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.DEPARTURE.get(), DEPARTURE_DURATION,
                    nextStacks - 1, false, true, true));
            StarRailPathMessages.send(player, StarRailPath.ERUDITION,
                    Component.translatable("message.starrail_sim.departure",
                            nextStacks, DEPARTURE_MAX_STACKS));
        }
        refresh(player);
    }

    private static void triggerStrife(ServerPlayer player, ItemStack stack) {
        long now = player.level().getGameTime();
        long cooldownEnd = player.getPersistentData().getLong(STRIFE_COOLDOWN_TAG);
        if (now < cooldownEnd) {
            return;
        }
        int superimposition = BloodFireBurningPathItem.getSuperimposition(stack);
        float currentHealth = player.getHealth();
        float requestedLoss = (float) (player.getMaxHealth()
                * strifeHealthCost(superimposition));
        float actualLoss = Math.min(requestedLoss, Math.max(0.0F, currentHealth - 1.0F));
        if (actualLoss > 0.0F) {
            player.setHealth(currentHealth - actualLoss);
        }
        boolean exceedsTenHealth = actualLoss > 10.0F;
        player.addEffect(new MobEffectInstance(
                StarRailSimMod.STRIFE.get(), STRIFE_DURATION,
                exceedsTenHealth ? 1 : 0, false, true, true));
        player.getPersistentData().putLong(STRIFE_COOLDOWN_TAG,
                now + STRIFE_COOLDOWN);
        StarRailPathMessages.send(player, StarRailPath.DESTRUCTION,
                Component.translatable("message.starrail_sim.strife",
                        String.format(java.util.Locale.ROOT, "%.1f", actualLoss),
                        String.format(java.util.Locale.ROOT, "+%.0f%%",
                                strifeDamage(superimposition)
                                        * (exceedsTenHealth ? 200.0D : 100.0D))));
    }

    /** Grants the crown effect when the current attack breaks toughness. */
    public static void onToughnessBreak(ServerPlayer player, LivingEntity hitTarget,
                                        boolean toughnessBroken) {
        ItemStack liesInTheWind = getEquippedLiesInTheWind(player);
        if (toughnessBroken && liesInTheWind != null && isNihilityPath(player)
                && hitTarget instanceof Monster monster
                && StarRailEffectService.rollEffectHit(player, monster, 0.60D)) {
            applyLiesInTheWindStatus(player, monster, StarRailSimMod.STOLEN.get(),
                    LiesInTheWindItem.getSuperimposition(liesInTheWind), STOLEN_DURATION);
        }
        ItemStack returnToLongRoad = getEquippedReturnToLongRoad(player);
        if (toughnessBroken && returnToLongRoad != null && isNihilityPath(player)
                && hitTarget instanceof Monster monster) {
            applyScorching(player, monster,
                    ReturnToLongRoadItem.getSuperimposition(returnToLongRoad));
        }
        ItemStack mirrorStack = getEquippedMirrorOfThePast(player);
        if (toughnessBroken && mirrorStack != null && isHarmonyPath(player)) {
            int currentStacks = plumFragranceStacks(player);
            int nextStacks = Math.min(PLUM_FRAGRANCE_MAX_STACKS, currentStacks + 1);
            int superimposition = MemoryOfMeItem.getSuperimposition(mirrorStack);
            for (ServerPlayer recipient : player.serverLevel().getEntitiesOfClass(
                    ServerPlayer.class, player.getBoundingBox().inflate(10.0D),
                    target -> target.isAlive() && target.distanceToSqr(player) <= 100.0D)) {
                recipient.removeEffect(StarRailSimMod.PLUM_FRAGRANCE.get());
                recipient.getPersistentData().putInt(
                        PLUM_FRAGRANCE_SUPERIMPOSITION_TAG, superimposition);
                recipient.addEffect(new MobEffectInstance(
                        StarRailSimMod.PLUM_FRAGRANCE.get(), PLUM_FRAGRANCE_DURATION,
                        nextStacks - 1, false, true, true));
            }
        }
        ItemStack stack = getEquippedNoRewardCrowning(player);
        if (toughnessBroken && stack != null && isDestructionPath(player)) {
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.KNIGHT_KING.get(), KNIGHT_KING_DURATION, 0,
                    false, true, true));
            StarRailPathMessages.send(player, StarRailPath.DESTRUCTION,
                    Component.translatable("message.starrail_sim.knight_king"));
        }
        ItemStack galaxyStack = getEquippedGalaxyRailway(player);
        if (toughnessBroken && galaxyStack != null && isEruditionPath(player)) {
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.METEOR.get(), METEOR_DURATION, 0,
                    false, true, true));
            StarRailPathMessages.send(player, StarRailPath.ERUDITION,
                    Component.translatable("message.starrail_sim.meteor"));
        }
        ItemStack lifeAsALightStack = getEquippedLifeAsALight(player);
        if (toughnessBroken && lifeAsALightStack != null && isEruditionPath(player)) {
            player.addEffect(new MobEffectInstance(
                    StarRailSimMod.ALCHEMY.get(), ALCHEMY_DURATION, 0,
                    false, true, true));
            StarRailPathMessages.send(player, StarRailPath.ERUDITION,
                    Component.translatable("message.starrail_sim.alchemy"));
        }
        refresh(player);
    }

    /** Applies the Moon Eclipse trigger when the wearer consumes health. */
    public static void onHealthConsumed(ServerPlayer player) {
        ItemStack stack = getEquippedThisBodyAsSword(player);
        if (stack != null && isDestructionPath(player)) {
            triggerMoonEclipse(player, stack);
            refresh(player);
        }
    }

    private static void triggerMoonEclipse(ServerPlayer player, ItemStack stack) {
        int currentStacks = moonEclipseStacks(player);
        int nextStacks = Math.min(MOON_ECLIPSE_MAX_STACKS, currentStacks + 1);
        player.addEffect(new MobEffectInstance(
                StarRailSimMod.MOON_ECLIPSE.get(), MOON_ECLIPSE_DURATION,
                nextStacks - 1, false, true, true));
        StarRailPathMessages.send(player, StarRailPath.DESTRUCTION,
                Component.translatable("message.starrail_sim.moon_eclipse",
                        nextStacks, MOON_ECLIPSE_MAX_STACKS));
    }

    /** Grants Beautiful Dream after a non-critical attack, respecting its cooldown. */
    public static void onNonCriticalAttack(ServerPlayer player) {
        ItemStack stack = getEquippedSleepLikeTheDead(player);
        if (stack == null || !isHuntPath(player)) {
            refresh(player);
            return;
        }

        long now = player.level().getGameTime();
        long cooldownEnd = player.getPersistentData()
                .getLong(BEAUTIFUL_DREAM_COOLDOWN_TAG);
        if (now < cooldownEnd) {
            return;
        }

        player.addEffect(new MobEffectInstance(
                StarRailSimMod.BEAUTIFUL_DREAM.get(),
                BEAUTIFUL_DREAM_DURATION,
                0,
                false,
                true,
                true));
        player.getPersistentData().putLong(BEAUTIFUL_DREAM_COOLDOWN_TAG,
                now + BEAUTIFUL_DREAM_COOLDOWN);
        refresh(player);
    }

    /** Applies target-hit stacks for the equipped Hunt light cone. */
    public static void onTargetHit(ServerPlayer player, LivingEntity target) {
        onNewFleshOfInfernoHit(player, target);
        onFateNeverFairTargetHit(player, target);
        onWeaveTimeIntoGoldHit(player, target);
        applyOnlyTheScentRemainsForgetWorry(player, target);
        ItemStack oceanWhySings = getEquippedOceanWhySings(player);
        if (target != player && target instanceof Monster monster && oceanWhySings != null
                && isNihilityPath(player) && !monster.hasEffect(StarRailSimMod.SOUL_TRANCE.get())
                && monster.getActiveEffects().stream().anyMatch(effect ->
                        effect.getEffect().getCategory() == MobEffectCategory.HARMFUL)
                && StarRailEffectService.rollEffectHit(player, monster, 0.60D)) {
            int level = OceanWhySingsItem.getSuperimposition(oceanWhySings);
            float damagePerSecond = (float) (StarRailAttributes.getAttackDamage(player)
                    * oceanWhySingsDotAttack(level));
            StarRailDebuffService.applyDamageOverTime(monster, OCEAN_WHY_SINGS_DOT_KEY,
                    player, damagePerSecond, SOUL_TRANCE_DURATION, 20);
            monster.addEffect(new MobEffectInstance(StarRailSimMod.SOUL_TRANCE.get(),
                    SOUL_TRANCE_DURATION, 0, false, true, true));
        }
        ItemStack reforgedRemembrance = getEquippedReforgedRemembrance(player);
        if (target != player && target instanceof Monster monster
                && reforgedRemembrance != null && isNihilityPath(player)
                && monster.getActiveEffects().stream().anyMatch(effect ->
                        effect.getEffect().getCategory() == MobEffectCategory.HARMFUL)) {
            int currentStacks = prophetStacks(player);
            int nextStacks = Math.min(PROPHET_MAX_STACKS, currentStacks + 1);
            int superimposition = ReforgedRemembranceItem.getSuperimposition(
                    reforgedRemembrance);
            player.addEffect(new MobEffectInstance(StarRailSimMod.PROPHET.get(),
                    PROPHET_DURATION, nextStacks - 1, false, true, true));
            player.getPersistentData().putInt(PROPHET_SUPERIMPOSITION_TAG, superimposition);
            StarRailPathMessages.send(player, StarRailPath.NIHILITY,
                    Component.translatable("message.starrail_sim.prophet",
                            nextStacks, PROPHET_MAX_STACKS));
        }
        if (target != player && target instanceof Monster monster && isNihilityPath(player)) {
            applyLiesInTheWindBewildered(player, monster);
        }
        if (target != player && target instanceof Monster && isNihilityPath(player)) {
            applyRainNeverStopsAetherCode(player, target);
        }
        if (target != player && target instanceof Monster monster && isNihilityPath(player)) {
            applyOnlyWaitSilkThread(player, monster);
        }
        if (target != player && target instanceof Monster monster && isNihilityPath(player)) {
            applyThousandSpringsStatuses(player, monster);
        }
        if (!isHuntPath(player) || target == player) {
            refresh(player);
            return;
        }

        ItemStack worrisome = getEquippedWorrisomeBlissful(player);
        if (worrisome != null) {
            MobEffectInstance effect = target.getEffect(StarRailSimMod.TAMED.get());
            int currentStacks = effect == null ? 0
                    : Math.max(0, Math.min(TAMED_MAX_STACKS, effect.getAmplifier() + 1));
            int nextStacks = Math.min(TAMED_MAX_STACKS, currentStacks + 1);
            target.addEffect(new MobEffectInstance(
                    StarRailSimMod.TAMED.get(), TAMED_DURATION, nextStacks - 1,
                    false, true, true));
        }

        ItemStack pure = getEquippedPureThoughtBaptism(player);
        if (pure != null) {
            CompoundTag stacks = target.getPersistentData().getCompound(THOUGHT_TRAINING_TAG);
            String playerKey = player.getUUID().toString();
            int nextStacks = Math.min(THOUGHT_TRAINING_MAX_STACKS,
                    Math.max(0, stacks.getInt(playerKey)) + 1);
            stacks.putInt(playerKey, nextStacks);
            target.getPersistentData().put(THOUGHT_TRAINING_TAG, stacks);
        }
        refresh(player);
    }

    /** Applies Inferno to a living target hit by the wearer. */
    public static void onNewFleshOfInfernoHit(ServerPlayer player, LivingEntity target) {
        ItemStack cone = getEquippedNewFleshOfInferno(player);
        if (target == player || !target.isAlive() || cone == null || !isNihilityPath(player)) {
            return;
        }
        target.addEffect(new MobEffectInstance(StarRailSimMod.INFERNO.get(),
                200, 0, false, true, true));
        target.getPersistentData().putInt(INFERNO_SUPERIMPOSITION_TAG,
                NewFleshOfInfernoItem.getSuperimposition(cone));
        target.getPersistentData().putString(INFERNO_OWNER_TAG, player.getStringUUID());
    }

    private static void applyLiesInTheWindBewildered(ServerPlayer player, Monster target) {
        ItemStack cone = getEquippedLiesInTheWind(player);
        CompoundTag data = target.getPersistentData();
        if (target.hasEffect(StarRailSimMod.STOLEN.get())
                && data.getLong(LIES_IN_THE_WIND_APPLIED_TICK_TAG)
                == target.level().getGameTime()) {
            return;
        }
        if (cone != null && StarRailEffectService.rollEffectHit(player, target, 0.60D)) {
            applyLiesInTheWindStatus(player, target, StarRailSimMod.BEWILDERED.get(),
                    LiesInTheWindItem.getSuperimposition(cone), BEWILDERED_DURATION);
        }
    }

    private static void applyLiesInTheWindStatus(ServerPlayer player, Monster target,
                                                 MobEffect status, int superimposition,
                                                 int duration) {
        if (status == StarRailSimMod.BEWILDERED.get()) {
            target.removeEffect(StarRailSimMod.STOLEN.get());
        } else {
            target.removeEffect(StarRailSimMod.BEWILDERED.get());
        }
        target.addEffect(new MobEffectInstance(status, duration, 0, false, true, true));
        target.getPersistentData().putInt(LIES_IN_THE_WIND_SUPERIMPOSITION_TAG,
                superimposition);
        target.getPersistentData().putLong(LIES_IN_THE_WIND_APPLIED_TICK_TAG,
                target.level().getGameTime());
    }

    private static void applyRainNeverStopsAetherCode(ServerPlayer player, LivingEntity target) {
        ItemStack cone = getEquippedRainNeverStops(player);
        if (cone == null || !StarRailEffectService.rollEffectHit(player, target, 0.60D)) {
            return;
        }
        target.addEffect(new MobEffectInstance(StarRailSimMod.AETHER_CODE.get(),
                AETHER_CODE_DURATION, 0, false, true, true));
        target.getPersistentData().putInt(AETHER_CODE_SUPERIMPOSITION_TAG,
                RainNeverStopsItem.getSuperimposition(cone));
        target.getPersistentData().putLong(AETHER_CODE_APPLIED_TICK_TAG,
                target.level().getGameTime());
    }

    private static void applyOnlyWaitSilkThread(ServerPlayer player, Monster target) {
        ItemStack cone = getEquippedOnlyWait(player);
        if (cone == null || StarRailDebuffService.hasSilkThread(target)
                || !StarRailEffectService.rollEffectHit(player, target, 0.60D)) {
            return;
        }
        int level = OnlyWaitItem.getSuperimposition(cone);
        float damagePerSecond = (float) (StarRailAttributes.getAttackDamage(player)
                * onlyWaitDotAttack(level) * (1.0D + onlyWaitDamage(level)));
        StarRailDebuffService.applyDamageOverTime(target, "only_wait_silk_thread", player,
                damagePerSecond, SILK_THREAD_DURATION, 20);
        target.addEffect(new MobEffectInstance(StarRailSimMod.SILK_THREAD.get(),
                SILK_THREAD_DURATION, 0, false, true, true));
    }

    private static void applyThousandSpringsStatuses(ServerPlayer player, Monster target) {
        ItemStack cone = getEquippedAThousandFoldSpring(player);
        if (cone == null) {
            return;
        }
        int level = AThousandFoldSpringItem.getSuperimposition(cone);
        CompoundTag data = target.getPersistentData();
        String owner = player.getStringUUID();
        MobEffectInstance cornered = target.getEffect(StarRailSimMod.CORNERED_PREY.get());
        if (cornered != null && owner.equals(data.getString(SPRING_STATUS_OWNER_TAG))) {
            return;
        }

        MobEffectInstance stripped = target.getEffect(StarRailSimMod.STRIPPED_ARMOR.get());
        boolean ownStripped = stripped != null
                && owner.equals(data.getString(SPRING_STATUS_OWNER_TAG));
        boolean ownDebuff = ownStripped
                || StarRailDebuffService.isNihilityMarkOwnedBy(target, player.getUUID());
        if (ownStripped && ownDebuff
                && StarRailEffectService.rollEffectHit(player, target, 0.40D)) {
            target.removeEffect(StarRailSimMod.STRIPPED_ARMOR.get());
            target.addEffect(new MobEffectInstance(StarRailSimMod.CORNERED_PREY.get(),
                    SPRING_STATUS_DURATION, 0, false, true, true));
            data.putString(SPRING_STATUS_OWNER_TAG, owner);
            data.putInt(SPRING_STATUS_SUPERIMPOSITION_TAG, level);
            return;
        }

        if (cornered == null && StarRailEffectService.rollEffectHit(player, target, 0.60D)) {
            target.addEffect(new MobEffectInstance(StarRailSimMod.STRIPPED_ARMOR.get(),
                    SPRING_STATUS_DURATION, 0, false, true, true));
            data.putString(SPRING_STATUS_OWNER_TAG, owner);
            data.putInt(SPRING_STATUS_SUPERIMPOSITION_TAG, level);
        }
    }

    /** Damage-taken multiplier for the two wearer-owned Nihility debuffs. */
    public static double incomingLightConeVulnerabilityMultiplier(LivingEntity target) {
        if (target == null) {
            return 1.0D;
        }
        CompoundTag data = target.getPersistentData();
        int level = Math.max(1, Math.min(5,
                data.getInt(SPRING_STATUS_SUPERIMPOSITION_TAG)));
        double multiplier = 1.0D;
        MobEffectInstance forgetWorry = target.getEffect(StarRailSimMod.FORGET_WORRY.get());
        if (forgetWorry != null && target.level().getGameTime()
                > data.getLong(FORGET_WORRY_APPLIED_TICK_TAG)) {
            int forgetWorryLevel = boundedSuperimposition(
                    data.getInt(FORGET_WORRY_SUPERIMPOSITION_TAG));
            double vulnerability = onlyTheScentRemainsVulnerability(forgetWorryLevel);
            if (data.getBoolean(FORGET_WORRY_EXTRA_VULNERABILITY_TAG)) {
                vulnerability += onlyTheScentRemainsExtraVulnerability(forgetWorryLevel);
            }
            multiplier *= 1.0D + vulnerability;
        } else if (forgetWorry == null) {
            data.remove(FORGET_WORRY_SUPERIMPOSITION_TAG);
            data.remove(FORGET_WORRY_EXTRA_VULNERABILITY_TAG);
            data.remove(FORGET_WORRY_APPLIED_TICK_TAG);
        }
        if (target.hasEffect(StarRailSimMod.CHIPS.get())
                && target.level().getGameTime() > data.getLong(CHIPS_APPLIED_TICK_TAG)) {
            int chipsLevel = Math.max(1, Math.min(5,
                    data.getInt(CHIPS_SUPERIMPOSITION_TAG)));
            multiplier *= 1.0D + fateNeverFairChipsDamage(chipsLevel);
        } else if (!target.hasEffect(StarRailSimMod.CHIPS.get())) {
            data.remove(CHIPS_SUPERIMPOSITION_TAG);
            data.remove(CHIPS_APPLIED_TICK_TAG);
        }
        if (target.hasEffect(StarRailSimMod.CORNERED_PREY.get())) {
            multiplier *= 1.0D + thousandSpringsStrippedDamage(level)
                    + thousandSpringsCorneredExtraDamage(level);
        } else if (target.hasEffect(StarRailSimMod.STRIPPED_ARMOR.get())) {
            multiplier *= 1.0D + thousandSpringsStrippedDamage(level);
        } else {
            data.remove(SPRING_STATUS_OWNER_TAG);
            data.remove(SPRING_STATUS_SUPERIMPOSITION_TAG);
        }
        if (target.hasEffect(StarRailSimMod.AETHER_CODE.get())
                && target.level().getGameTime()
                > data.getLong(AETHER_CODE_APPLIED_TICK_TAG)) {
            int aetherLevel = Math.max(1, Math.min(5,
                    data.getInt(AETHER_CODE_SUPERIMPOSITION_TAG)));
            multiplier *= 1.0D + rainNeverStopsVulnerability(aetherLevel);
        } else if (!target.hasEffect(StarRailSimMod.AETHER_CODE.get())) {
            data.remove(AETHER_CODE_SUPERIMPOSITION_TAG);
            data.remove(AETHER_CODE_APPLIED_TICK_TAG);
        }
        boolean bewildered = target.hasEffect(StarRailSimMod.BEWILDERED.get());
        boolean stolen = target.hasEffect(StarRailSimMod.STOLEN.get());
        if ((bewildered || stolen) && target.level().getGameTime()
                > data.getLong(LIES_IN_THE_WIND_APPLIED_TICK_TAG)) {
            int liesLevel = Math.max(1, Math.min(5,
                    data.getInt(LIES_IN_THE_WIND_SUPERIMPOSITION_TAG)));
            multiplier *= 1.0D + (bewildered
                    ? liesInTheWindBewilderedVulnerability(liesLevel)
                    : liesInTheWindStolenVulnerability(liesLevel));
        } else if (!bewildered && !stolen) {
            data.remove(LIES_IN_THE_WIND_SUPERIMPOSITION_TAG);
            data.remove(LIES_IN_THE_WIND_APPLIED_TICK_TAG);
        }
        MobEffectInstance scorching = target.getEffect(StarRailSimMod.SCORCHING.get());
        if (scorching != null && target.level().getGameTime()
                > data.getLong(SCORCHING_APPLIED_TICK_TAG)) {
            int scorchingLevel = Math.max(1, Math.min(5,
                    data.getInt(SCORCHING_SUPERIMPOSITION_TAG)));
            multiplier *= 1.0D + returnToLongRoadScorchingVulnerability(scorchingLevel)
                    * Math.min(SCORCHING_MAX_STACKS, scorching.getAmplifier() + 1);
        } else if (scorching == null) {
            data.remove(SCORCHING_SUPERIMPOSITION_TAG);
            data.remove(SCORCHING_APPLIED_TICK_TAG);
        }
        return multiplier;
    }

    /** Starts the damage-triggered critical-damage window while the cone is active. */
    public static void onFateNeverFairDamaged(ServerPlayer player) {
        ItemStack stack = getEquippedFateNeverFair(player);
        if (stack == null || !isPreservationPath(player)) {
            return;
        }
        player.getPersistentData().putLong(FATE_NEVER_FAIR_CRIT_DAMAGE_UNTIL_TAG,
                player.level().getGameTime() + FATE_NEVER_FAIR_CRIT_DAMAGE_DURATION);
        refresh(player);
    }

    /** Applies the cone's damage-taken debuff to a target struck by the wearer. */
    public static void onFateNeverFairTargetHit(ServerPlayer player, LivingEntity target) {
        ItemStack stack = getEquippedFateNeverFair(player);
        if (stack == null || !isPreservationPath(player) || target == player
                || !(target instanceof Monster)) {
            return;
        }
        int level = FateNeverFairItem.getSuperimposition(stack);
        target.addEffect(new MobEffectInstance(StarRailSimMod.CHIPS.get(),
                CHIPS_DURATION, 0, false, true, true));
        target.getPersistentData().putInt(CHIPS_SUPERIMPOSITION_TAG, level);
        target.getPersistentData().putLong(CHIPS_APPLIED_TICK_TAG,
                target.level().getGameTime());
    }

    /** Conditional critical-rate bonus when attacking a debuffed target. */
    public static double rainNeverStopsCritRateBonus(ServerPlayer player, LivingEntity target) {
        ItemStack cone = getEquippedRainNeverStops(player);
        if (cone == null || !isNihilityPath(player) || target == null
                || target.getActiveEffects().stream().noneMatch(effect ->
                        effect.getEffect().getCategory() == MobEffectCategory.HARMFUL)) {
            return 0.0D;
        }
        return rainNeverStopsCritRate(RainNeverStopsItem.getSuperimposition(cone));
    }

    /** Returns the target-specific critical damage bonus from Tamed. */
    public static double tamedCritDamageBonus(ServerPlayer player, LivingEntity target) {
        ItemStack stack = getEquippedWorrisomeBlissful(player);
        if (stack == null || !isHuntPath(player) || target == null) {
            return 0.0D;
        }
        MobEffectInstance effect = target.getEffect(StarRailSimMod.TAMED.get());
        if (effect == null) {
            return 0.0D;
        }
        int stacks = Math.max(0, Math.min(TAMED_MAX_STACKS, effect.getAmplifier() + 1));
        return worrisomeBlissfulCritDamagePerStack(
                WorrisomeBlissfulItem.getSuperimposition(stack)) * stacks;
    }

    /** Returns the per-target critical damage bonus from Thought Training. */
    public static double thoughtTrainingCritDamageBonus(ServerPlayer player,
                                                        LivingEntity target) {
        ItemStack stack = getEquippedPureThoughtBaptism(player);
        if (stack == null || !isHuntPath(player) || target == null) {
            return 0.0D;
        }
        CompoundTag stacks = target.getPersistentData().getCompound(THOUGHT_TRAINING_TAG);
        int currentStacks = Math.max(0, Math.min(THOUGHT_TRAINING_MAX_STACKS,
                stacks.getInt(player.getUUID().toString())));
        return thoughtTrainingCritDamagePerStack(
                PureThoughtBaptismItem.getSuperimposition(stack)) * currentStacks;
    }

    /** Crit-DMG vulnerability from Inferno, including its wearer's extra share. */
    public static double infernoCritDamageBonus(ServerPlayer attacker, LivingEntity target) {
        if (target == null || !target.hasEffect(StarRailSimMod.INFERNO.get())) {
            return 0.0D;
        }
        CompoundTag data = target.getPersistentData();
        int level = Math.max(1, Math.min(5, data.getInt(INFERNO_SUPERIMPOSITION_TAG)));
        double bonus = newFleshOfInfernoCritDamageTaken(level);
        if (attacker != null && attacker.getStringUUID().equals(
                data.getString(INFERNO_OWNER_TAG))) {
            bonus += newFleshOfInfernoCritDamageTaken(level);
        }
        return bonus;
    }

    /** Returns the damage multiplier supplied by the active Flowing Light stacks. */
    public static double pursuitDamageMultiplier(ServerPlayer player) {
        ItemStack stack = getEquippedIWillHunt(player);
        if (stack == null || !isHuntPath(player)) {
            return 1.0D;
        }
        return 1.0D + pursuitDamagePerStack(IWillHuntItem.getSuperimposition(stack))
                * flowingLightStacks(player);
    }

    /** Returns all unconditional/stack-based Hunt light-cone damage multipliers. */
    public static double lightConeDamageMultiplier(ServerPlayer player, LivingEntity target) {
        double multiplier = pursuitDamageMultiplier(player);
        multiplier *= loveIsEternalDamageMultiplier(player);
        multiplier *= starlitNightDamageMultiplier(player);
        multiplier *= weaveTimeIntoGoldDamageMultiplier(player);
        multiplier *= netherBloomDamageMultiplier(player);
        multiplier *= bestFortuneDamageMultiplier(player);
        multiplier *= pushStreamDamageMultiplier(player);
        MobEffectInstance garrison = player.getEffect(StarRailSimMod.GARRISON.get());
        if (garrison != null) {
            int superimposition = Math.max(1, Math.min(5, player.getPersistentData()
                    .getInt(GARRISON_SUPERIMPOSITION_TAG)));
            multiplier *= 1.0D + thoughRiversGarrisonDamage(superimposition);
        }
        MobEffectInstance vision = player.getEffect(StarRailSimMod.VISION.get());
        if (vision != null) {
            int superimposition = Math.max(1, Math.min(5, player.getPersistentData()
                    .getInt(VISION_SUPERIMPOSITION_TAG)));
            multiplier *= 1.0D + closedEyesDamage(superimposition);
        }
        ItemStack reforgedRemembrance = getEquippedReforgedRemembrance(player);
        MobEffectInstance prophet = player.getEffect(StarRailSimMod.PROPHET.get());
        if (reforgedRemembrance != null && isNihilityPath(player) && prophet != null) {
            int superimposition = Math.max(1, Math.min(5, player.getPersistentData()
                    .getInt(PROPHET_SUPERIMPOSITION_TAG)));
            int stacks = Math.max(1, Math.min(PROPHET_MAX_STACKS,
                    prophet.getAmplifier() + 1));
            multiplier *= 1.0D + reforgedRemembranceDamagePerStack(superimposition) * stacks;
        }
        ItemStack onlyWait = getEquippedOnlyWait(player);
        if (onlyWait != null && isNihilityPath(player)) {
            multiplier *= 1.0D + onlyWaitDamage(OnlyWaitItem.getSuperimposition(onlyWait));
        }
        ItemStack inTheName = getEquippedInTheNameOfTheWorld(player);
        if (inTheName != null && isNihilityPath(player) && target != null
                && target.getActiveEffects().stream().anyMatch(effect ->
                        effect.getEffect().getCategory() == MobEffectCategory.HARMFUL)) {
            multiplier *= 1.0D + inTheNameDamageVsDebuffed(
                    InTheNameOfTheWorldItem.getSuperimposition(inTheName));
        }
        ItemStack onTheShore = getEquippedOnTheShoreInTheFlowOfTime(player);
        MobEffectInstance foamEcho = player.getEffect(StarRailSimMod.FOAM_ECHO.get());
        if (onTheShore != null && isNihilityPath(player) && foamEcho != null) {
            int superimposition = Math.max(1, Math.min(5, player.getPersistentData()
                    .getInt(FOAM_ECHO_SUPERIMPOSITION_TAG)));
            int stacks = Math.max(1, Math.min(FOAM_ECHO_MAX_STACKS,
                    foamEcho.getAmplifier() + 1));
            multiplier *= 1.0D + shoreDamagePerStack(superimposition) * stacks;
        }
        ItemStack doNotForgetHerFlame = getEquippedDoNotForgetHerFlame(player);
        if (doNotForgetHerFlame != null && isNihilityPath(player)) {
            multiplier *= 1.0D + doNotForgetHerFlameDamage(
                    DoNotForgetHerFlameItem.getSuperimposition(doNotForgetHerFlame));
        }
        MobEffectInstance inheritance = player.getEffect(StarRailSimMod.INHERITANCE.get());
        boolean skipInheritanceOnThisHit = player.getPersistentData()
                .getBoolean(INHERITANCE_SKIP_CURRENT_HIT_TAG);
        player.getPersistentData().remove(INHERITANCE_SKIP_CURRENT_HIT_TAG);
        if (inheritance != null && !skipInheritanceOnThisHit) {
            multiplier *= 1.0D + inheritanceDamage(inheritance.getAmplifier() + 1);
        }
        MobEffectInstance splendor = player.getEffect(StarRailSimMod.SPLENDOR.get());
        if (splendor != null) {
            int superimposition = Math.max(1, Math.min(5, player.getPersistentData()
                    .getInt(NIGHT_FLOWING_COLORS_SPLENDOR_SUPERIMPOSITION_TAG)));
            multiplier *= 1.0D + nightFlowingColorsSplendorDamage(superimposition);
        }
        MobEffectInstance plumFragrance = player.getEffect(StarRailSimMod.PLUM_FRAGRANCE.get());
        if (plumFragrance != null) {
            int superimposition = Math.max(1, Math.min(5, player.getPersistentData()
                    .getInt(PLUM_FRAGRANCE_SUPERIMPOSITION_TAG)));
            int stacks = Math.max(1, Math.min(PLUM_FRAGRANCE_MAX_STACKS,
                    plumFragrance.getAmplifier() + 1));
            multiplier *= 1.0D + plumFragranceDamage(superimposition) * stacks;
        }
        MobEffectInstance psalm = player.getEffect(StarRailSimMod.PSALM.get());
        if (psalm != null) {
            int superimposition = Math.max(1, Math.min(5, player.getPersistentData()
                    .getInt(PSALM_SUPERIMPOSITION_TAG)));
            int stacks = Math.max(1, Math.min(PSALM_MAX_STACKS, psalm.getAmplifier() + 1));
            multiplier *= 1.0D + psalmDamagePerStack(superimposition) * stacks;
        }
        MobEffectInstance law = player.getEffect(StarRailSimMod.LAW.get());
        if (law != null) {
            int superimposition = Math.max(1, Math.min(5, player.getPersistentData()
                    .getInt(LAW_SUPERIMPOSITION_TAG)));
            multiplier *= 1.0D + goldenBloodLawDamage(superimposition);
        }
        ItemStack stack = getEquippedWorrisomeBlissful(player);
        if (stack != null && isHuntPath(player)) {
            multiplier *= 1.0D + worrisomeBlissfulAttackDamage(
                    WorrisomeBlissfulItem.getSuperimposition(stack));
        }
        ItemStack pure = getEquippedPureThoughtBaptism(player);
        if (pure != null && isHuntPath(player)
                && player.hasEffect(StarRailSimMod.DEBATE.get())) {
            multiplier *= 1.0D + debateDamage(
                    PureThoughtBaptismItem.getSuperimposition(pure));
        }
        ItemStack finale = getEquippedFinaleOfALie(player);
        if (finale != null && isHuntPath(player)
                && player.hasEffect(StarRailSimMod.SHADOW_DEVOUR.get())) {
            multiplier *= 1.0D + shadowDevourDamage(
                    FinaleOfALieItem.getSuperimposition(finale));
        }
        ItemStack familyStack = getEquippedSomethingIrreplaceable(player);
        if (familyStack != null && isDestructionPath(player)
                && player.hasEffect(StarRailSimMod.FAMILY.get())) {
            int stacks = familyStacks(player);
            multiplier *= 1.0D + familyDamage(
                    SomethingIrreplaceableItem.getSuperimposition(familyStack)) * stacks;
        }
        ItemStack unreachableStack = getEquippedTheUnreachableSide(player);
        if (unreachableStack != null && isDestructionPath(player)
                && player.hasEffect(StarRailSimMod.NO_RETREAT.get())) {
            multiplier *= 1.0D + noRetreatDamage(
                    TheUnreachableSideItem.getSuperimposition(unreachableStack));
        }
        ItemStack crowningStack = getEquippedNoRewardCrowning(player);
        if (crowningStack != null && isDestructionPath(player)
                && player.getPersistentData().getBoolean(KNIGHT_KING_ATTACK_TAG)) {
            multiplier *= 1.0D + knightKingAttack(
                    NoRewardCrowningItem.getSuperimposition(crowningStack));
            player.getPersistentData().remove(KNIGHT_KING_ATTACK_TAG);
        }
        ItemStack bloodFireStack = getEquippedBloodFireBurningPath(player);
        if (bloodFireStack != null && isDestructionPath(player)
                && player.hasEffect(StarRailSimMod.STRIFE.get())) {
            MobEffectInstance effect = player.getEffect(StarRailSimMod.STRIFE.get());
            double damageBonus = strifeDamage(
                    BloodFireBurningPathItem.getSuperimposition(bloodFireStack));
            if (effect != null && effect.getAmplifier() > 0) {
                damageBonus *= 2.0D;
            }
            multiplier *= 1.0D + damageBonus;
        }
        ItemStack whereDreamsStack = getEquippedWhereDreamsBelong(player);
        if (whereDreamsStack != null && isDestructionPath(player)
                && player.hasEffect(StarRailSimMod.METAMORPHOSIS.get())) {
            multiplier *= 1.0D + metamorphosisDamage(
                    WhereDreamsBelongItem.getSuperimposition(whereDreamsStack));
        }
        ItemStack dawnStack = getEquippedDawnBurnsJustSo(player);
        if (dawnStack != null && isDestructionPath(player)) {
            int superimposition = DawnBurnsJustSoItem.getSuperimposition(dawnStack);
            multiplier *= 1.0D + dawnBurnsDamage(superimposition);
            if (player.hasEffect(StarRailSimMod.BLAZING_SUN.get())) {
                multiplier *= 1.0D + blazingSunDamage(superimposition);
            }
        }
        ItemStack kinglyStack = getEquippedWhatYouSeeIsMe(player);
        if (kinglyStack != null && isDestructionPath(player)) {
            if (player.getPersistentData().getBoolean(KINGLY_ENTERTAINMENT_NEXT_ATTACK_TAG)) {
                multiplier *= 1.0D + whatYouSeeIsMeNextDamage(
                        WhatYouSeeIsMeItem.getSuperimposition(kinglyStack));
                player.getPersistentData().remove(KINGLY_ENTERTAINMENT_NEXT_ATTACK_TAG);
            }
        }
        ItemStack galaxyStack = getEquippedGalaxyRailway(player);
        if (galaxyStack != null && isEruditionPath(player)
                && player.hasEffect(StarRailSimMod.METEOR.get())) {
            multiplier *= 1.0D + meteorDamage(
                    GalaxyRailwayItem.getSuperimposition(galaxyStack));
        }
        ItemStack beforeDawnStack = getEquippedBeforeDawn(player);
        if (beforeDawnStack != null && isEruditionPath(player)
                && player.hasEffect(StarRailSimMod.DREAM_BODY.get())) {
            int stacks = dreamBodyStacks(player);
            multiplier *= 1.0D + dreamBodyDamage(
                    BeforeDawnItem.getSuperimposition(beforeDawnStack)) * stacks;
        }
        ItemStack priceOfPeaceStack = getEquippedPriceOfPeace(player);
        if (priceOfPeaceStack != null && isEruditionPath(player)) {
            double critDamage = StarRailAttributes.getValue(player,
                    StarRailAttributes.CRIT_DAMAGE, StarRailAttributes.DEFAULT_CRIT_DAMAGE);
            int overThresholdStacks = Math.max(0, Math.min(3,
                    (int) Math.floor((critDamage - 1.20D) / 0.05D + 1.0E-9D)));
            multiplier *= 1.0D + priceOfPeaceDamagePerCritStack(
                    PriceOfPeaceItem.getSuperimposition(priceOfPeaceStack))
                    * overThresholdStacks;
        }
        ItemStack towardsUnanswerableStack = getEquippedTowardsUnanswerable(player);
        if (towardsUnanswerableStack != null && isEruditionPath(player)
                && player.hasEffect(StarRailSimMod.DECONSTRUCTION.get())) {
            multiplier *= 1.0D + deconstructionDamage(
                    TowardsUnanswerableItem.getSuperimposition(towardsUnanswerableStack))
                    * deconstructionStacks(player);
        }
        ItemStack lifeAsALightStack = getEquippedLifeAsALight(player);
        if (lifeAsALightStack != null && isEruditionPath(player)
                && player.hasEffect(StarRailSimMod.ALCHEMY.get())) {
            multiplier *= 1.0D + alchemyDamage(
                    LifeAsALightItem.getSuperimposition(lifeAsALightStack));
        }
        ItemStack aStarIlluminatesNightSkyStack = getEquippedAStarIlluminatesNightSky(player);
        if (aStarIlluminatesNightSkyStack != null && isEruditionPath(player)
                && player.hasEffect(StarRailSimMod.DEPARTURE.get())) {
            multiplier *= 1.0D + departureDamage(
                    AStarIlluminatesNightSkyItem.getSuperimposition(
                            aStarIlluminatesNightSkyStack))
                    * departureStacks(player);
        }
        ItemStack sparkleQuietlyShinesStack = getEquippedSparkleQuietlyShines(player);
        if (sparkleQuietlyShinesStack != null && isEruditionPath(player)
                && player.hasEffect(StarRailSimMod.SHINING_CROWN.get())) {
            multiplier *= 1.0D + shiningCrownDamage(
                    SparkleQuietlyShinesItem.getSuperimposition(sparkleQuietlyShinesStack));
        }
        ItemStack welcomeToGalacticCity = getEquippedWelcomeToGalacticCity(player);
        if (welcomeToGalacticCity != null && isElationPath(player)) {
            multiplier *= 1.0D + welcomeToGalacticCityDamage(
                    WelcomeToGalacticCityItem.getSuperimposition(welcomeToGalacticCity));
        }
        ItemStack meetInTheNextFlowerSeason = getEquippedMeetInTheNextFlowerSeason(player);
        MobEffectInstance daydream = player.getEffect(StarRailSimMod.DAYDREAM.get());
        if (meetInTheNextFlowerSeason != null && isElationPath(player) && daydream != null) {
            int stacks = Math.max(0, Math.min(DAYDREAM_MAX_STACKS,
                    daydream.getAmplifier() + 1));
            int superimposition = boundedSuperimposition(player.getPersistentData().getInt(
                    DAYDREAM_SUPERIMPOSITION_TAG));
            multiplier *= 1.0D + daydreamDamagePerStack(superimposition) * stacks;
        }
        return multiplier;
    }

    /** Refreshes the wearer's attack-triggered Blank status. */
    public static void onLoveIsEternalAttack(ServerPlayer player) {
        triggerLoveIsEternalBlank(player);
    }

    /** Refreshes the attack-triggered Starlit Night status for indirect hits. */
    public static void onStarlightForLongNightsAttack(ServerPlayer player) {
        triggerStarlitNight(player);
    }

    /** Damage multiplier from Blank, including the paired-status enhancement. */
    public static double loveIsEternalDamageMultiplier(ServerPlayer player) {
        ItemStack stack = getEquippedLoveIsEternal(player);
        MobEffectInstance blank = player.getEffect(StarRailSimMod.BLANK.get());
        if (stack == null || !isRemembrancePath(player) || blank == null) {
            return 1.0D;
        }
        int level = boundedSuperimposition(player.getPersistentData().getInt(
                LOVE_IS_ETERNAL_BLANK_SUPERIMPOSITION_TAG));
        double bonus = loveIsEternalBlankDamage(level);
        if (player.hasEffect(StarRailSimMod.VERSE.get())) {
            bonus *= 1.0D + loveIsEternalSynergy(level);
        }
        return 1.0D + bonus;
    }

    /** Damage multiplier while the wearer has the Starlit Night status. */
    public static double starlitNightDamageMultiplier(ServerPlayer player) {
        ItemStack stack = getEquippedStarlightForLongNights(player);
        MobEffectInstance effect = player.getEffect(StarRailSimMod.STARLIT_NIGHT.get());
        if (stack == null || !isRemembrancePath(player) || effect == null) {
            return 1.0D;
        }
        int level = boundedSuperimposition(player.getPersistentData().getInt(
                STARLIT_NIGHT_SUPERIMPOSITION_TAG));
        return 1.0D + starlitNightDamage(level);
    }

    private static void triggerStarlitNight(ServerPlayer wearer) {
        ItemStack stack = getEquippedStarlightForLongNights(wearer);
        if (stack == null || !isRemembrancePath(wearer)) {
            return;
        }
        int level = StarlightForLongNightsItem.getSuperimposition(stack);
        wearer.getPersistentData().putInt(STARLIT_NIGHT_SUPERIMPOSITION_TAG, level);
        wearer.removeEffect(StarRailSimMod.STARLIT_NIGHT.get());
        wearer.addEffect(new MobEffectInstance(StarRailSimMod.STARLIT_NIGHT.get(),
                STARLIT_NIGHT_DURATION, 0, false, true, true));
        refresh(wearer);
    }

    private static boolean loveIsEternalVerseEnhanced(ServerPlayer recipient) {
        if (!recipient.hasEffect(StarRailSimMod.VERSE.get())) {
            return false;
        }
        String ownerValue = recipient.getPersistentData().getString(
                LOVE_IS_ETERNAL_VERSE_OWNER_TAG);
        if (ownerValue.isEmpty()) {
            return false;
        }
        try {
            ServerPlayer owner = recipient.getServer().getPlayerList()
                    .getPlayer(UUID.fromString(ownerValue));
            return owner != null && getEquippedLoveIsEternal(owner) != null
                    && isRemembrancePath(owner)
                    && owner.hasEffect(StarRailSimMod.BLANK.get())
                    && owner.hasEffect(StarRailSimMod.VERSE.get());
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private static void triggerLoveIsEternalBlank(ServerPlayer wearer) {
        ItemStack stack = getEquippedLoveIsEternal(wearer);
        if (stack == null || !isRemembrancePath(wearer)) {
            return;
        }
        int level = LoveIsEternalItem.getSuperimposition(stack);
        wearer.getPersistentData().putInt(LOVE_IS_ETERNAL_BLANK_SUPERIMPOSITION_TAG, level);
        wearer.removeEffect(StarRailSimMod.BLANK.get());
        wearer.addEffect(new MobEffectInstance(StarRailSimMod.BLANK.get(), 10 * 20,
                0, false, true, true));
        refresh(wearer);
    }

    private static void triggerLoveIsEternalPoem(ServerPlayer wearer) {
        ItemStack stack = getEquippedLoveIsEternal(wearer);
        if (stack == null || !isRemembrancePath(wearer)) {
            return;
        }
        int level = LoveIsEternalItem.getSuperimposition(stack);
        for (ServerPlayer recipient : wearer.serverLevel().getEntitiesOfClass(
                ServerPlayer.class, wearer.getBoundingBox().inflate(10.0D),
                target -> target.isAlive() && target.distanceToSqr(wearer) <= 100.0D)) {
            recipient.getPersistentData().putInt(
                    LOVE_IS_ETERNAL_VERSE_SUPERIMPOSITION_TAG, level);
            recipient.getPersistentData().putString(
                    LOVE_IS_ETERNAL_VERSE_OWNER_TAG, wearer.getStringUUID());
            recipient.removeEffect(StarRailSimMod.VERSE.get());
            recipient.addEffect(new MobEffectInstance(StarRailSimMod.VERSE.get(),
                    20 * 20, 0, false, true, true));
            refresh(recipient);
        }
    }

    /** Damage bonus available only after all six Brocade stacks have formed. */
    public static double weaveTimeIntoGoldDamageMultiplier(ServerPlayer player) {
        ItemStack stack = getEquippedWeaveTimeIntoGold(player);
        MobEffectInstance brocade = player.getEffect(StarRailSimMod.BROCADE.get());
        if (stack == null || !isRemembrancePath(player) || brocade == null) {
            return 1.0D;
        }
        int stacks = Math.max(0, Math.min(BROCADE_MAX_STACKS,
                brocade.getAmplifier() + 1));
        if (stacks < BROCADE_MAX_STACKS) {
            return 1.0D;
        }
        return 1.0D + weaveTimeIntoGoldDamagePerStack(
                WeaveTimeIntoGoldItem.getSuperimposition(stack)) * stacks;
    }

    /** Returns the damage bonus from the active Nether Bloom stacks. */
    public static double netherBloomDamageMultiplier(ServerPlayer player) {
        ItemStack stack = getEquippedMakeFarewellMoreBeautiful(player);
        MobEffectInstance effect = player.getEffect(StarRailSimMod.NETHER_BLOOM.get());
        if (stack == null || !isRemembrancePath(player) || effect == null) {
            return 1.0D;
        }
        int stacks = Math.max(0, Math.min(NETHER_BLOOM_MAX_STACKS,
                effect.getAmplifier() + 1));
        return 1.0D + netherBloomDamagePerStack(
                MakeFarewellMoreBeautifulItem.getSuperimposition(stack)) * stacks;
    }

    /** Returns the damage multiplier for toughness-break damage. */
    public static double breakDamageMultiplier(ServerPlayer player) {
        ItemStack stack = getEquippedEmbarkOnSecondLife(player);
        if (stack == null || !isHuntPath(player)
                || !player.hasEffect(StarRailSimMod.PAINFUL_VOYAGE.get())) {
            return 1.0D;
        }
        return 1.0D + painfulVoyageBreakDamage(
                EmbarkOnSecondLifeItem.getSuperimposition(stack));
    }

    private static boolean isHuntPath(ServerPlayer player) {
        return player.getCapability(StarRailPathCapability.PATH_DATA)
                .map(data -> data.getCurrentPath() == StarRailPath.HUNT)
                .orElse(false);
    }

    private static boolean isDestructionPath(ServerPlayer player) {
        return player.getCapability(StarRailPathCapability.PATH_DATA)
                .map(data -> data.getCurrentPath() == StarRailPath.DESTRUCTION)
                .orElse(false);
    }

    private static boolean isEruditionPath(ServerPlayer player) {
        return player.getCapability(StarRailPathCapability.PATH_DATA)
                .map(data -> data.getCurrentPath() == StarRailPath.ERUDITION)
                .orElse(false);
    }

    private static boolean isHarmonyPath(ServerPlayer player) {
        return player.getCapability(StarRailPathCapability.PATH_DATA)
                .map(data -> data.getCurrentPath() == StarRailPath.HARMONY)
                .orElse(false);
    }

    private static boolean isRemembrancePath(ServerPlayer player) {
        return player.getCapability(StarRailPathCapability.PATH_DATA)
                .map(data -> data.getCurrentPath() == StarRailPath.REMEMBRANCE)
                .orElse(false);
    }

    private static boolean isNihilityPath(ServerPlayer player) {
        return player.getCapability(StarRailPathCapability.PATH_DATA)
                .map(data -> data.getCurrentPath() == StarRailPath.NIHILITY)
                .orElse(false);
    }

    private static boolean isPreservationPath(ServerPlayer player) {
        return player.getCapability(StarRailPathCapability.PATH_DATA)
                .map(data -> data.getCurrentPath() == StarRailPath.PRESERVATION)
                .orElse(false);
    }

    private static boolean isAbundancePath(ServerPlayer player) {
        return player.getCapability(StarRailPathCapability.PATH_DATA)
                .map(data -> data.getCurrentPath() == StarRailPath.ABUNDANCE)
                .orElse(false);
    }

    private static boolean isElationPath(ServerPlayer player) {
        return player.getCapability(StarRailPathCapability.PATH_DATA)
                .map(data -> data.getCurrentPath() == StarRailPath.ELATION)
                .orElse(false);
    }

    private static int nightButterflyStacks(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(StarRailSimMod.NIGHT_BUTTERFLY.get());
        if (effect == null) {
            return 0;
        }
        return Math.max(0, Math.min(NIGHT_BUTTERFLY_MAX_STACKS,
                effect.getAmplifier() + 1));
    }

    private static int prophetStacks(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(StarRailSimMod.PROPHET.get());
        return effect == null ? 0 : Math.max(0, Math.min(PROPHET_MAX_STACKS,
                effect.getAmplifier() + 1));
    }

    private static int plumFragranceStacks(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(StarRailSimMod.PLUM_FRAGRANCE.get());
        return effect == null ? 0 : Math.max(0, Math.min(PLUM_FRAGRANCE_MAX_STACKS,
                effect.getAmplifier() + 1));
    }

    private static int chantStacks(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(StarRailSimMod.CHANT.get());
        return effect == null ? 0 : Math.max(0, Math.min(
                NIGHT_FLOWING_COLORS_MAX_CHANT_STACKS, effect.getAmplifier() + 1));
    }

    private static int flowingLightStacks(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(StarRailSimMod.FLOWING_LIGHT.get());
        if (effect == null) {
            return 0;
        }
        return Math.max(0, Math.min(FLOWING_LIGHT_MAX_STACKS,
                effect.getAmplifier() + 1));
    }

    private static int rangerStacks(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(StarRailSimMod.RANGER.get());
        if (effect == null) {
            return 0;
        }
        return Math.max(0, Math.min(RANGER_MAX_STACKS,
                effect.getAmplifier() + 1));
    }

    private static int familyStacks(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(StarRailSimMod.FAMILY.get());
        if (effect == null) {
            return 0;
        }
        return Math.max(0, Math.min(FAMILY_MAX_STACKS, effect.getAmplifier() + 1));
    }

    private static int dragonRoarStacks(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(StarRailSimMod.DRAGON_ROAR.get());
        if (effect == null) {
            return 0;
        }
        return Math.max(0, Math.min(DRAGON_ROAR_MAX_STACKS, effect.getAmplifier() + 1));
    }

    private static int fireDanceStacks(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(StarRailSimMod.FIRE_DANCE.get());
        if (effect == null) {
            return 0;
        }
        return Math.max(0, Math.min(FIRE_DANCE_MAX_STACKS, effect.getAmplifier() + 1));
    }

    private static int moonEclipseStacks(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(StarRailSimMod.MOON_ECLIPSE.get());
        if (effect == null) {
            return 0;
        }
        return Math.max(0, Math.min(MOON_ECLIPSE_MAX_STACKS,
                effect.getAmplifier() + 1));
    }

    private static int knightlyCourtesyStacks(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(StarRailSimMod.KNIGHTLY_COURTESY.get());
        if (effect == null) {
            return 0;
        }
        return Math.max(0, Math.min(KNIGHTLY_COURTESY_MAX_STACKS,
                effect.getAmplifier() + 1));
    }

    private static int dreamBodyStacks(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(StarRailSimMod.DREAM_BODY.get());
        if (effect == null) {
            return 0;
        }
        return Math.max(0, Math.min(DREAM_BODY_MAX_STACKS, effect.getAmplifier() + 1));
    }

    private static int promiseStacks(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(StarRailSimMod.PROMISE.get());
        if (effect == null) {
            return 0;
        }
        return Math.max(0, Math.min(PROMISE_MAX_STACKS, effect.getAmplifier() + 1));
    }

    private static int deconstructionStacks(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(StarRailSimMod.DECONSTRUCTION.get());
        if (effect == null) {
            return 0;
        }
        return Math.max(0, Math.min(DECONSTRUCTION_MAX_STACKS,
                effect.getAmplifier() + 1));
    }

    private static int thunderEscapeStacks(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(StarRailSimMod.THUNDER_ESCAPE.get());
        if (effect == null) {
            return 0;
        }
        return Math.max(0, Math.min(THUNDER_ESCAPE_MAX_STACKS,
                effect.getAmplifier() + 1));
    }

    private static int departureStacks(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(StarRailSimMod.DEPARTURE.get());
        if (effect == null) {
            return 0;
        }
        return Math.max(0, Math.min(DEPARTURE_MAX_STACKS,
                effect.getAmplifier() + 1));
    }

    private static int boundedSuperimposition(int superimposition) {
        return Math.max(1, Math.min(5, superimposition));
    }

    private static ItemStack getEquippedInTheNight(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.IN_THE_NIGHT.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedIWillHunt(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.I_WILL_HUNT.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedWorrisomeBlissful(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.WORRISOME_BLISSFUL.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedSleepLikeTheDead(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.SLEEP_LIKE_THE_DEAD.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedPureThoughtBaptism(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.PURE_THOUGHT_BAPTISM.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedIdealBurningHell(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.IDEAL_BURNING_HELL.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedEmbarkOnSecondLife(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.EMBARK_ON_SECOND_LIFE.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedFinaleOfALie(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.FINALE_OF_A_LIE.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedSomethingIrreplaceable(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.SOMETHING_IRREPLACEABLE.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedBrighterThanTheSun(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.BRIGHTER_THAN_THE_SUN.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedDanceAtSunset(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.DANCE_AT_SUNSET.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedTheUnreachableSide(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.THE_UNREACHABLE_SIDE.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedThisBodyAsSword(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.THIS_BODY_AS_SWORD.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedNoRewardCrowning(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.NO_REWARD_CROWNING.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedBloodFireBurningPath(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.BLOOD_FIRE_BURNING_PATH.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedWhereDreamsBelong(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.WHERE_DREAMS_BELONG.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedDawnBurnsJustSo(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.DAWN_BURNS_JUST_SO.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedWhatYouSeeIsMe(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.WHAT_YOU_SEE_IS_ME.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedGalaxyRailway(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.GALAXY_RAILWAY.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedMomentOfGlory(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.MOMENT_OF_GLORY.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedBeforeDawn(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.BEFORE_DAWN.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedPriceOfPeace(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.PRICE_OF_PEACE.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedTowardsUnanswerable(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.TOWARDS_UNANSWERABLE.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedNinjaScroll(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.NINJA_SCROLL.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedLifeAsALight(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.LIFE_AS_A_LIGHT.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedAStarIlluminatesNightSky(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.A_STAR_ILLUMINATES_NIGHT_SKY.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedSparkleQuietlyShines(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.SPARKLE_QUIETLY_SHINES.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedBattleIsntOver(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.BATTLE_ISNT_OVER.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedMirrorOfThePast(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.MEMORY_OF_ME.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedWeaveTimeIntoGold(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.WEAVE_TIME_INTO_GOLD.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedMakeFarewellMoreBeautiful(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.MAKE_FAREWELL_MORE_BEAUTIFUL.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedLoveIsEternal(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.LOVE_IS_ETERNAL.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedStarlightForLongNights(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.STARLIGHT_FOR_LONG_NIGHTS.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedMayRainbowStayInTheSky(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.MAY_RAINBOW_STAY_IN_THE_SKY.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedWelcomeToGalacticCity(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.WELCOME_TO_GALACTIC_CITY.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedMeetInTheNextFlowerSeason(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.MEET_IN_THE_NEXT_FLOWER_SEASON.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedWhenSheDecidesToSee(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.WHEN_SHE_DECIDES_TO_SEE.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedFlowerWorldMesmerizingEyes(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.FLOWER_WORLD_MESMERIZING_EYES.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedNightFlowingColors(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.NIGHT_FLOWING_COLORS.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedGameOfCosmicWorlds(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.GAME_OF_COSMIC_WORLDS.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedReturningToEarth(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.RETURNING_TO_EARTH.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedIfTimeWereAFlower(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.IF_TIME_WERE_A_FLOWER.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedAnAgeEtchedInGoldenBlood(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.AN_AGE_ETCHED_IN_GOLDEN_BLOOD.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedInTheNameOfTheWorld(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.IN_THE_NAME_OF_THE_WORLD.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedOnTheShoreInTheFlowOfTime(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.ON_THE_SHORE_IN_THE_FLOW_OF_TIME.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedAThousandFoldSpring(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.A_THOUSAND_FOLD_SPRING.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedOnlyWait(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.ONLY_WAIT.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedRainNeverStops(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.RAIN_NEVER_STOPS.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedLiesInTheWind(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.LIES_IN_THE_WIND.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedReturnToLongRoad(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.RETURN_TO_LONG_ROAD.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedReforgedRemembrance(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.REFORGED_REMEMBRANCE.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedOceanWhySings(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.OCEAN_WHY_SINGS.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedDoNotForgetHerFlame(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.DO_NOT_FORGET_HER_FLAME.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedNewFleshOfInferno(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.NEW_FLESH_OF_INFERNO.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedMomentOfVictory(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.MOMENT_OF_VICTORY.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedSheHasClosedHerEyes(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.SHE_HAS_CLOSED_HER_EYES.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedThoughRiversAndMountains(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.THOUGH_RIVERS_AND_MOUNTAINS.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedFateNeverFair(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.FATE_NEVER_FAIR.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedEchoesOfTheCoffin(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.ECHOES_OF_THE_COFFIN.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedTimeWaitsForNoOne(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.TIME_WAITS_FOR_NO_ONE.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedNightOfFright(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.NIGHT_OF_FRIGHT.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    private static ItemStack getEquippedOnlyTheScentRemains(ServerPlayer player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, StarRailSimMod.ONLY_THE_SCENT_REMAINS.get())
                .map(result -> result.stack())
                .orElse(null);
    }

    /** Adds one Brocade stack after a successful attack, refreshing its 30-second duration. */
    public static void onWeaveTimeIntoGoldHit(ServerPlayer wearer, LivingEntity target) {
        ItemStack stack = getEquippedWeaveTimeIntoGold(wearer);
        if (stack == null || !isRemembrancePath(wearer) || target == null
                || target == wearer || !target.isAlive()) {
            return;
        }
        MobEffectInstance current = wearer.getEffect(StarRailSimMod.BROCADE.get());
        int stacks = current == null ? 0 : Math.max(0, Math.min(BROCADE_MAX_STACKS,
                current.getAmplifier() + 1));
        int nextStacks = Math.min(BROCADE_MAX_STACKS, stacks + 1);
        wearer.addEffect(new MobEffectInstance(StarRailSimMod.BROCADE.get(),
                BROCADE_DURATION, nextStacks - 1, false, true, true));
        refresh(wearer);
    }

    /** Adds one Nether Bloom stack when the wearer actually loses health. */
    public static void onMakeFarewellMoreBeautifulHealthLost(
            ServerPlayer wearer, float healthLoss) {
        ItemStack stack = getEquippedMakeFarewellMoreBeautiful(wearer);
        if (healthLoss <= 0.0F || !wearer.isAlive() || stack == null
                || !isRemembrancePath(wearer)) {
            return;
        }
        MobEffectInstance current = wearer.getEffect(StarRailSimMod.NETHER_BLOOM.get());
        int stacks = current == null ? 0 : Math.max(0, Math.min(
                NETHER_BLOOM_MAX_STACKS, current.getAmplifier() + 1));
        int nextStacks = Math.min(NETHER_BLOOM_MAX_STACKS, stacks + 1);
        wearer.addEffect(new MobEffectInstance(StarRailSimMod.NETHER_BLOOM.get(),
                NETHER_BLOOM_DURATION, nextStacks - 1, false, true, true));
        refresh(wearer);
    }

    /** Applies the scent's time-limited damage-taken debuff to the struck enemy. */
    public static void applyOnlyTheScentRemainsForgetWorry(
            ServerPlayer wearer, LivingEntity target) {
        ItemStack stack = getEquippedOnlyTheScentRemains(wearer);
        if (stack == null || !isAbundancePath(wearer) || target == wearer
                || !(target instanceof Monster)) {
            return;
        }
        int superimposition = OnlyTheScentRemainsItem.getSuperimposition(stack);
        boolean extraVulnerability = StarRailAttributes.getValue(wearer,
                StarRailAttributes.BREAK_EFFECT, 0.0D) >= 1.0D;
        target.addEffect(new MobEffectInstance(StarRailSimMod.FORGET_WORRY.get(),
                20 * 20, 0, false, true, true));
        target.getPersistentData().putInt(FORGET_WORRY_SUPERIMPOSITION_TAG,
                superimposition);
        target.getPersistentData().putBoolean(FORGET_WORRY_EXTRA_VULNERABILITY_TAG,
                extraVulnerability);
        target.getPersistentData().putLong(FORGET_WORRY_APPLIED_TICK_TAG,
                target.level().getGameTime());
    }

    private static void triggerNightOfFright(ServerPlayer wearer) {
        ItemStack stack = getEquippedNightOfFright(wearer);
        if (stack == null || !isAbundancePath(wearer)) {
            return;
        }
        int superimposition = NightOfFrightItem.getSuperimposition(stack);
        ServerPlayer recipient = wearer.serverLevel().getEntitiesOfClass(
                        ServerPlayer.class, wearer.getBoundingBox().inflate(10.0D),
                        target -> target.isAlive() && target.distanceToSqr(wearer) <= 100.0D)
                .stream()
                .min(java.util.Comparator.comparingDouble(target ->
                        target.getHealth() / Math.max(1.0F, target.getMaxHealth())))
                .orElse(null);
        if (recipient == null) {
            return;
        }

        float healAmount = (float) (wearer.getMaxHealth()
                * nightOfFrightHealing(superimposition));
        recipient.heal(healAmount);

        MobEffectInstance current = recipient.getEffect(StarRailSimMod.DEEP_BREATH.get());
        int stacks = Math.min(DEEP_BREATH_MAX_STACKS,
                current == null ? 1 : current.getAmplifier() + 2);
        recipient.getPersistentData().putInt(DEEP_BREATH_SUPERIMPOSITION_TAG,
                superimposition);
        recipient.addEffect(new MobEffectInstance(StarRailSimMod.DEEP_BREATH.get(),
                DEEP_BREATH_DURATION, stacks - 1, false, true, true));
        refresh(recipient);
    }

    private static void triggerThoughRiversAndMountains(ServerPlayer wearer) {
        ItemStack stack = getEquippedThoughRiversAndMountains(wearer);
        if (stack == null || !isPreservationPath(wearer)) {
            return;
        }
        int superimposition = ThoughRiversAndMountainsItem.getSuperimposition(stack);
        float healAmount = (float) (wearer.getAttributeValue(Attributes.ARMOR)
                * thoughRiversHealFromArmor(superimposition));
        for (ServerPlayer recipient : wearer.getServer().getPlayerList().getPlayers()) {
            if (!recipient.isAlive() || recipient.level() != wearer.level()) {
                continue;
            }
            if (recipient.distanceToSqr(wearer) <= 100.0D) {
                recipient.heal(healAmount);
            }
            recipient.getPersistentData().putInt(GARRISON_SUPERIMPOSITION_TAG,
                    superimposition);
            recipient.addEffect(new MobEffectInstance(StarRailSimMod.GARRISON.get(),
                    GARRISON_DURATION, 0, false, true, true));
        }
    }

    private static void applyScorching(ServerPlayer player, Monster target, int superimposition) {
        MobEffectInstance existing = target.getEffect(StarRailSimMod.SCORCHING.get());
        int stacks = Math.min(SCORCHING_MAX_STACKS,
                existing == null ? 1 : existing.getAmplifier() + 2);
        target.removeEffect(StarRailSimMod.SCORCHING.get());
        target.addEffect(new MobEffectInstance(StarRailSimMod.SCORCHING.get(),
                SCORCHING_DURATION, stacks - 1, false, true, true));
        target.getPersistentData().putInt(SCORCHING_SUPERIMPOSITION_TAG,
                boundedSuperimposition(superimposition));
        target.getPersistentData().putLong(SCORCHING_APPLIED_TICK_TAG,
                target.level().getGameTime());
        StarRailPathMessages.send(player, StarRailPath.NIHILITY,
                Component.translatable("message.starrail_sim.scorching",
                        stacks, SCORCHING_MAX_STACKS));
    }

    private static void applyModifier(AttributeInstance instance, UUID id, String name,
                                      double amount, AttributeModifier.Operation operation) {
        AttributeModifier existing = instance.getModifier(id);
        if (amount == 0.0D) {
            if (existing != null) {
                instance.removeModifier(existing);
            }
            return;
        }
        if (existing != null
                && Double.compare(existing.getAmount(), amount) == 0
                && existing.getOperation() == operation) {
            return;
        }
        if (existing != null) {
            instance.removeModifier(existing);
        }
        instance.addPermanentModifier(new AttributeModifier(id, name, amount, operation));
    }
}
