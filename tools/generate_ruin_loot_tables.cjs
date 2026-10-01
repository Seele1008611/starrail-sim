// 项目中文说明：根据各命途光锥清单生成普通及命途遗迹宝箱战利品表，并补全对应语言文本。

const fs = require("node:fs");
const path = require("node:path");

const root = path.resolve(__dirname, "..");
const lootDir = path.join(root, "src/main/resources/data/starrail_sim/loot_tables/chests");

// Keep this list in sync with the light-cone item registrations in StarRailSimMod.
// 该清单按命途列出可进入宝箱的光锥 ID。
const pathCones = {
  destruction: [
    "something_irreplaceable", "brighter_than_the_sun", "dance_at_sunset",
    "the_unreachable_side", "this_body_as_sword", "no_reward_crowning",
    "blood_fire_burning_path", "where_dreams_belong", "dawn_burns_just_so",
    "what_you_see_is_me",
  ],
  hunt: [
    "in_the_night", "i_will_hunt", "worrisome_blissful", "sleep_like_the_dead",
    "pure_thought_baptism", "ideal_burning_hell", "embark_on_second_life",
    "finale_of_a_lie",
  ],
  erudition: [
    "galaxy_railway", "moment_of_glory", "before_dawn", "price_of_peace",
    "towards_unanswerable", "ninja_scroll", "life_as_a_light",
    "a_star_illuminates_night_sky", "sparkle_quietly_shines",
  ],
  harmony: [
    "battle_isnt_over", "memory_of_me", "night_flowing_colors",
    "game_of_cosmic_worlds", "returning_to_earth", "if_time_were_a_flower",
    "an_age_etched_in_golden_blood",
  ],
  nihility: [
    "in_the_name_of_the_world", "on_the_shore_in_the_flow_of_time",
    "a_thousand_fold_spring", "only_wait", "rain_never_stops", "lies_in_the_wind",
    "reforged_remembrance", "ocean_why_sings", "do_not_forget_her_flame",
    "new_flesh_of_inferno", "return_to_long_road",
  ],
  preservation: [
    "moment_of_victory", "she_has_closed_her_eyes", "though_rivers_and_mountains",
    "fate_never_fair",
  ],
  abundance: [
    "echoes_of_the_coffin", "time_waits_for_no_one", "night_of_fright",
    "only_the_scent_remains",
  ],
  remembrance: [
    "weave_time_into_gold", "make_farewell_more_beautiful", "love_is_eternal",
    "starlight_for_long_nights", "may_rainbow_stay_in_the_sky",
  ],
  elation: [
    "welcome_to_galactic_city", "meet_in_the_next_flower_season",
    "when_she_decides_to_see", "flower_world_mesmerizing_eyes",
  ],
};

// 普通遗迹从所有命途的光锥中随机抽取。
const allCones = [...new Set(Object.values(pathCones).flat())];
const uniform = (min, max) => ({ type: "minecraft:uniform", min, max });
const itemEntries = (ids) => ids.map((id) => ({
  type: "minecraft:item",
  name: `starrail_sim:${id}`,
}));
const suppliesPool = {
  rolls: 1,
  entries: [{
    type: "minecraft:loot_table",
    name: "minecraft:chests/desert_pyramid",
  }],
};

// 光锥池的抽取数量固定为 1 到 3 张。
function conePool(ids) {
  return {
    rolls: uniform(1, 3),
    entries: itemEntries(ids),
  };
}

// 把战利品池序列化为 Minecraft 使用的 JSON 文件。
function writeLootTable(name, pools) {
  fs.writeFileSync(path.join(lootDir, `${name}.json`), `${JSON.stringify({
    type: "minecraft:chest",
    pools,
  }, null, 2)}\n`, "utf8");
}

fs.mkdirSync(lootDir, { recursive: true });
writeLootTable("ruin_ordinary", [conePool(allCones), suppliesPool]);

// 命途遗迹额外保证一件带命途 NBT 的试炼凭证，并只抽取该命途光锥。
for (const [pathId, coneIds] of Object.entries(pathCones)) {
  writeLootTable(`ruin_${pathId}`, [
    {
      rolls: 1,
      entries: [{
        type: "minecraft:item",
        name: "starrail_sim:path_trial_token",
        functions: [{
          function: "minecraft:set_nbt",
          tag: `{Path:"${pathId}"}`,
        }],
      }],
    },
    conePool(coneIds),
    suppliesPool,
  ]);
}

function appendLanguageEntries(relativePath, entries) {
  const filePath = path.join(root, relativePath);
  const source = fs.readFileSync(filePath, "utf8");
  const missingEntries = Object.entries(entries).filter(([key]) => !source.includes(`"${key}"`));
  if (missingEntries.length === 0) {
    return;
  }
  const closeIndex = source.lastIndexOf("}");
  const additions = missingEntries
    .map(([key, value]) => `  ,${JSON.stringify(key)}: ${JSON.stringify(value)}`)
    .join("\n");
  const updated = `${source.slice(0, closeIndex).trimEnd()}\n${additions}\n${source.slice(closeIndex)}`;
  JSON.parse(updated);
  fs.writeFileSync(filePath, updated, "utf8");
}

appendLanguageEntries("src/main/resources/assets/starrail_sim/lang/zh_cn.json", {
  "item.starrail_sim.path_trial_token": "命途试炼凭证",
  "tooltip.starrail_sim.path_trial_token.path": "命途：%s",
  "tooltip.starrail_sim.path_trial_token.use": "右键使用以开启对应命途试炼。",
  "message.starrail_sim.invalid_path_trial_token": "这件凭证没有记录有效的命途。",
});
appendLanguageEntries("src/main/resources/assets/starrail_sim/lang/en_us.json", {
  "item.starrail_sim.path_trial_token": "Path Trial Token",
  "tooltip.starrail_sim.path_trial_token.path": "Path: %s",
  "tooltip.starrail_sim.path_trial_token.use": "Right-click to begin the recorded Path trial.",
  "message.starrail_sim.invalid_path_trial_token": "This token has no valid Path recorded.",
});

console.log(`Generated 10 ruin loot tables with ${allCones.length} distinct Light Cones.`);
