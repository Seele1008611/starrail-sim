package com.starrail.sim.client;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;

/** Face-focused framing, curated for each existing cone texture; no duplicate image assets. */
final class StarRailLightConePortraits {
    record Focus(int centerX, int centerY, int sourceWidth) { }
    private static final Map<String, Focus> FACES = Map.ofEntries(
            Map.entry("a_star_illuminates_night_sky", new Focus(346, 302, 740)),
            Map.entry("a_thousand_fold_spring", new Focus(529, 248, 740)),
            Map.entry("an_age_etched_in_golden_blood", new Focus(562, 410, 740)),
            Map.entry("battle_isnt_over", new Focus(486, 410, 740)),
            Map.entry("before_dawn", new Focus(562, 292, 740)),
            Map.entry("blood_fire_burning_path", new Focus(529, 281, 740)),
            Map.entry("brighter_than_the_sun", new Focus(389, 313, 740)),
            Map.entry("dance_at_sunset", new Focus(637, 346, 740)),
            Map.entry("dawn_burns_just_so", new Focus(432, 367, 740)),
            Map.entry("do_not_forget_her_flame", new Focus(680, 302, 740)),
            Map.entry("echoes_of_the_coffin", new Focus(443, 346, 740)),
            Map.entry("embark_on_second_life", new Focus(443, 259, 740)),
            Map.entry("fate_never_fair", new Focus(432, 227, 740)),
            Map.entry("finale_of_a_lie", new Focus(605, 389, 740)),
            Map.entry("flower_world_mesmerizing_eyes", new Focus(497, 443, 740)),
            Map.entry("galaxy_railway", new Focus(486, 248, 740)),
            Map.entry("game_of_cosmic_worlds", new Focus(529, 302, 740)),
            Map.entry("i_will_hunt", new Focus(454, 270, 740)),
            Map.entry("ideal_burning_hell", new Focus(562, 302, 740)),
            Map.entry("if_time_were_a_flower", new Focus(605, 335, 740)),
            Map.entry("in_the_name_of_the_world", new Focus(605, 194, 740)),
            Map.entry("in_the_night", new Focus(421, 248, 740)),
            Map.entry("lies_in_the_wind", new Focus(540, 216, 740)),
            Map.entry("life_as_a_light", new Focus(562, 302, 740)),
            Map.entry("love_is_eternal", new Focus(540, 335, 740)),
            Map.entry("make_farewell_more_beautiful", new Focus(518, 432, 740)),
            Map.entry("may_rainbow_stay_in_the_sky", new Focus(691, 378, 740)),
            Map.entry("meet_in_the_next_flower_season", new Focus(540, 421, 740)),
            Map.entry("memory_of_me", new Focus(562, 486, 740)),
            Map.entry("moment_of_glory", new Focus(594, 259, 740)),
            Map.entry("moment_of_victory", new Focus(670, 259, 740)),
            Map.entry("new_flesh_of_inferno", new Focus(680, 184, 740)),
            Map.entry("night_flowing_colors", new Focus(518, 324, 740)),
            Map.entry("night_of_fright", new Focus(572, 313, 740)),
            Map.entry("ninja_scroll", new Focus(659, 324, 740)),
            Map.entry("no_reward_crowning", new Focus(367, 302, 740)),
            Map.entry("ocean_why_sings", new Focus(562, 270, 740)),
            Map.entry("on_the_shore_in_the_flow_of_time", new Focus(529, 778, 740)),
            Map.entry("only_the_scent_remains", new Focus(626, 313, 740)),
            Map.entry("only_wait", new Focus(616, 367, 740)),
            Map.entry("price_of_peace", new Focus(432, 281, 740)),
            Map.entry("pure_thought_baptism", new Focus(508, 324, 740)),
            Map.entry("rain_never_stops", new Focus(637, 324, 740)),
            Map.entry("reforged_remembrance", new Focus(540, 308, 740)),
            Map.entry("return_to_long_road", new Focus(637, 324, 740)),
            Map.entry("returning_to_earth", new Focus(562, 335, 740)),
            Map.entry("she_has_closed_her_eyes", new Focus(497, 324, 740)),
            Map.entry("sleep_like_the_dead", new Focus(540, 562, 740)),
            Map.entry("something_irreplaceable", new Focus(562, 238, 740)),
            Map.entry("sparkle_quietly_shines", new Focus(659, 302, 740)),
            Map.entry("starlight_for_long_nights", new Focus(454, 594, 740)),
            Map.entry("the_unreachable_side", new Focus(605, 216, 740)),
            Map.entry("this_body_as_sword", new Focus(518, 248, 740)),
            Map.entry("though_rivers_and_mountains", new Focus(454, 281, 740)),
            Map.entry("time_waits_for_no_one", new Focus(454, 346, 740)),
            Map.entry("towards_unanswerable", new Focus(475, 281, 740)),
            Map.entry("weave_the_time_into_gold", new Focus(540, 324, 740)),
            Map.entry("welcome_to_galactic_city", new Focus(637, 346, 740)),
            Map.entry("what_you_see_is_me", new Focus(324, 713, 740)),
            Map.entry("when_she_decides_to_see", new Focus(508, 421, 740)),
            Map.entry("where_dreams_belong", new Focus(572, 292, 740)),
            Map.entry("worrisome_blissful", new Focus(551, 238, 740))
    );

    private StarRailLightConePortraits() { }

    static Focus forSprite(ResourceLocation sprite) {
        String name = sprite.getPath();
        name = name.substring(name.lastIndexOf('/') + 1);
        return FACES.getOrDefault(name, new Focus(540, 360, 740));
    }
}
