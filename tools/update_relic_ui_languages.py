"""Maintain the native relic UI's bilingual labels without changing other keys."""
import json
import subprocess
import sys
sys.dont_write_bytecode = True
from pathlib import Path
from export_relic_ui_assets import ROOT, SOURCES

LABELS = {
    'filters': ('筛选与排序', 'Filters and Sorting'), 'inner_label': ('内圈 · 位面饰品', 'Inner · Planar Ornaments'),
    'part.head': ('头部', 'Head'), 'part.hands': ('手部', 'Hands'),
    'part.body': ('躯干', 'Body'), 'part.feet': ('脚部', 'Feet'),
    'part.sphere': ('位面球', 'Planar Sphere'), 'part.rope': ('连结绳', 'Link Rope'),
    'stat.hp': ('生命值', 'HP'), 'stat.hp_pct': ('生命值加成', 'HP Boost'),
    'stat.attack': ('攻击力', 'ATK'), 'stat.attack_pct': ('攻击力加成', 'ATK Boost'),
    'stat.armor': ('护甲值', 'Armor'), 'stat.armor_pct': ('护甲值加成', 'Armor Boost'),
    'stat.crit': ('暴击率', 'CRIT Rate'), 'stat.crit_damage': ('暴击伤害', 'CRIT DMG'),
    'stat.hit': ('效果命中', 'Effect Hit Rate'), 'stat.resist': ('效果抵抗', 'Effect RES'),
    'stat.break': ('击破特攻', 'Break Effect'), 'stat.heal': ('治疗效果', 'Healing Effect'),
    'stat.toughness': ('护甲韧性', 'Armor Toughness'), 'stat.toughness_pct': ('护甲韧性加成', 'Toughness Boost'),
    'material.lost_crystal': ('遗失晶块', 'Lost Crystal'), 'material.relic_remains': ('遗器残骸', 'Relic Remains'),
    'material.self_modeling_resin': ('自塑尘脂', 'Self-Modeling Resin'), 'material.variable_dice': ('变量骰子', 'Variable Dice'),
    'set.musketeer': ('野穗伴行的快枪手', 'Musketeer of Wild Wheat'),
    'set.passerby': ('云无留迹的过客', 'Passerby of Wandering Cloud'),
    'set.knight': ('净庭教宗的圣骑士', 'Knight of Purity Palace'),
    'set.space_station': ('太空封印站', 'Space Sealing Station'),
    'set.fleet': ('不老者的仙舟', 'Fleet of the Ageless'),
    'view.equipment': ('装备', 'Equip'), 'view.enhance': ('强化', 'Enhance'),
    'view.salvage': ('分解', 'Salvage'), 'view.forge': ('合成', 'Synthesize'), 'view.reroll': ('重掷', 'Reroll'),
    'leave_preview': ('退出界面预览', 'Leave UI Preview'), 'enter_preview': ('开启界面预览', 'Enter UI Preview'),
    'preview_badge': ('界面预览 · 不影响存档', 'UI Preview · No Save Changes'),
    'contributions': ('属性汇总', 'Stat Summary'), 'contribution_note': ('仅汇总预览装备的词条；套装效果仅展示，尚未应用到战斗。', 'Preview equipment stats only; set effects are displayed but not applied to combat.'),
    'unequip': ('卸下', 'Unequip'), 'equip': ('装备', 'Equip'), 'replace': ('替换当前遗器', 'Replace Equipped Relic'),
    'unlock': ('解锁', 'Unlock'), 'lock': ('锁定', 'Lock'), 'all_parts': ('全部部位', 'All Slots'),
    'all_sets': ('全部套装', 'All Sets'), 'all_stars': ('全部星级', 'All Rarities'), 'all_mains': ('全部主属性', 'All Main Stats'),
    'stars': ('%s星', '%s Stars'), 'search': ('搜索遗器或套装', 'Search Relics or Sets'),
    'filter_state.0': ('全部状态', 'All States'), 'filter_state.1': ('已装备', 'Equipped'),
    'filter_state.2': ('未装备', 'Unequipped'), 'filter_state.3': ('待选择', 'Pending'),
    'sort.0': ('星级优先', 'By Rarity'), 'sort.1': ('等级优先', 'By Level'),
    'sort.2': ('最新优先', 'Newest'), 'sort.3': ('部位排序', 'By Slot'),
    'locked_filter': ('仅锁定', 'Locked'), 'clear': ('清空筛选', 'Clear'), 'to_cap': ('升至上限', 'To Max'),
    'plus_levels': ('提升%s级', '+%s Levels'), 'enhance': ('强化', 'Enhance'), 'salvage': ('分解', 'Salvage'),
    'salvage_gain': ('获得 %s × %s', 'Gain %s × %s'), 'forfeit_xp': ('选择残骸返还时不返还强化经验。', 'Remains salvage does not return enhancement EXP.'),
    'remove_item': ('分解后将移除这件预览遗器。', 'This preview relic will be removed.'),
    'keep_old': ('保留原结果', 'Keep Original'), 'take_new': ('采用新结果', 'Accept New'),
    'reroll': ('重掷副属性', 'Reroll Substats'), 'pick_eligible': ('选择可重掷遗器', 'Select Eligible Relic'),
    'target_forge': ('定向主属性', 'Target Main Stat'), 'normal_forge': ('随机主属性', 'Random Main Stat'),
    'random_main': ('主属性随机', 'Random Main Stat'), 'forge': ('合成遗器', 'Synthesize'),
    'forge_cost': ('消耗：%s 遗器残骸', 'Cost: %s Relic Remains'), 'craft_resin': ('合成自塑尘脂', 'Craft Resin'),
    'craft_dice': ('合成变量骰子', 'Craft Dice'), 'craft_cost': ('消耗：%s 遗器残骸', 'Cost: %s Relic Remains'),
    'level_change': ('等级 +%s → +%s', 'Level +%s → +%s'),
    'inventory_count': ('遗器库存 %s / %s', 'Relics %s / %s'), 'empty_equipment': ('六槽尚未装备遗器', 'No Relics Equipped'),
    'target_cost': ('100 残骸 + 1 自塑尘脂 · 预览消耗', '100 Remains + 1 Resin · Preview Cost'),
    'normal_cost': ('100 遗器残骸 · 预览消耗', '100 Remains · Preview Cost'),
    'select_item': ('请先选择一件遗器', 'Select a Relic First'), 'item_meta': ('%s星 · +%s / +%s', '%s Stars · +%s / +%s'),
    'main_change': ('%s：%s → %s', '%s: %s → %s'), 'demo_cost': ('预览消耗：%s 遗失晶块', 'Preview Cost: %s Lost Crystals'),
    'pending': ('请先选择待处理的重掷结果', 'Resolve the Pending Reroll First'),
    'protected': ('已装备或锁定的遗器受保护', 'Equipped or Locked Relics Are Protected'),
    'reroll_requirement': ('仅限未装备、未锁定的五星 +15 遗器', 'Requires an Unequipped, Unlocked 5-Star +15 Relic'),
    'dice_warning': ('消耗 1 变量骰子；保留原结果也不返还。', 'Costs 1 Dice; keeping the original does not refund it.'),
    'worn': ('已装备', 'Equipped'), 'locked': ('已锁定', 'Locked'), 'unworn': ('未装备', 'Unequipped'),
    'main': ('主属性', 'Main Stat'), 'subs': ('副属性 · %s / 4', 'Substats · %s / 4'),
    'set_two_count': ('两件套（%s/2）', '2-Piece Set (%s/2)'), 'set_four_count': ('四件套（%s/4）', '4-Piece Set (%s/4)'),
    'set_effect.passerby.two': ('治疗效果提高15%。', 'Healing effect +15%.'),
    'set_effect.passerby.four': ('击杀敌对目标时，为自己和10格内其他玩家恢复装备者最大生命值5%的生命。', 'When you kill a hostile target, restore health equal to 5% of your maximum health to yourself and nearby players within 10 blocks.'),
    'set_effect.musketeer.two': ('攻击力提高12%。', 'ATK +12%.'),
    'set_effect.musketeer.four': ('暴击伤害提高12%，造成的伤害提高10%。', 'CRIT DMG +12%; damage dealt +10%.'),
    'set_effect.knight.two': ('护甲值提高15%。', 'Armor +15%.'),
    'set_effect.knight.four': ('提供的护盾量提高20%（以伤害吸收生命值体现）。', 'Shield amount provided +20% (represented by absorption health).'),
    'set_effect.space_station.two': ('攻击力提高24%。', 'ATK +24%.'),
    'set_effect.fleet.two': ('最大生命值提高20%。', 'Max HP +20%.'),
    'compare': ('与当前装备对比', 'Compare with Equipped'), 'empty_inventory': ('库存为空 · 遗器系统待接入', 'Empty Inventory · Relic System Pending'),
    'empty_filter': ('没有符合筛选条件的遗器', 'No Matching Relics'),
    'done': ('完成', 'Done'), 'confirm': ('确认', 'Confirm'), 'cancel': ('取消', 'Cancel'),
    'not_ready': ('遗器功能正在接入中', 'Relic System Is Under Development'),
    'insufficient': ('预览材料不足', 'Not Enough Preview Materials'), 'invalid': ('当前操作不可用', 'Action Unavailable'),
    'crafted': ('材料合成完成', 'Material Crafted'), 'forged': ('遗器合成完成', 'Relic Synthesized'),
    'unequipped': ('已卸下预览遗器', 'Preview Relic Unequipped'), 'equipped': ('已装备预览遗器', 'Preview Relic Equipped'),
    'unlocked': ('已解除锁定', 'Relic Unlocked'), 'max_level': ('已达到强化上限', 'Maximum Level Reached'),
    'enhanced': ('强化完成', 'Enhancement Complete'), 'five_only': ('只有五星遗器能分解为遗器残骸', 'Only 5-Star Relics Yield Remains'),
    'salvaged': ('分解完成', 'Salvage Complete'), 'candidate_ready': ('重掷完成，请选择保留结果', 'Reroll Complete; Choose a Result'),
    'chosen': ('已保存选择的预览结果', 'Preview Result Selected'),
}

EN_ITEMS = {
    'musketeer': ['Musketeer\'s Wild Wheat Felt Hat', 'Musketeer\'s Coarse Leather Gloves', 'Musketeer\'s Wind-Hunting Shawl', 'Musketeer\'s Rivets Riding Boots'],
    'passerby': ['Passerby\'s Rejuvenated Wooden Hairstick', 'Passerby\'s Roaming Dragon Bracer', 'Passerby\'s Ragged Embroidered Coat', 'Passerby\'s Stygian Hiking Boots'],
    'knight': ['Knight\'s Forgiving Casque', 'Knight\'s Silent Oath Ring', 'Knight\'s Solemn Breastplate', 'Knight\'s Iron Boots of Order'],
    'space_station': ['Herta\'s Space Station', 'Herta\'s Wandering Trek'],
    'fleet': ['The Xianzhou Luofu\'s Celestial Ark', 'The Xianzhou Luofu\'s Ambrosial Arbor Vines'],
}

if __name__ == '__main__':
    for set_id, (folder, names) in SOURCES.items():
        parts = ['sphere', 'rope'] if folder.startswith('内圈') else ['head', 'hands', 'body', 'feet']
        for part, zh, en in zip(parts, names, EN_ITEMS[set_id]):
            LABELS[f'item.{set_id}.{part}'] = (zh, en)
    for column, locale in enumerate(['zh_cn', 'en_us']):
        path = ROOT / f'src/main/resources/assets/starrail_sim/lang/{locale}.json'
        values = json.loads(path.read_text(encoding='utf-8-sig'))
        additions = {'ui.starrail_sim.relic.' + key: pair[column] for key, pair in LABELS.items()}
        original = subprocess.run(['git', 'show', 'HEAD:' + path.relative_to(ROOT).as_posix()], cwd=ROOT,
                                  capture_output=True, check=True).stdout.decode('utf-8-sig')
        existing = {key: value for key, value in values.items() if not key.startswith('ui.starrail_sim.relic.')}
        if existing == json.loads(original):
            tail = json.dumps(additions, ensure_ascii=False, indent=2)[1:-1].strip('\n')
            output = original.rstrip().removesuffix('}').rstrip() + ',\n' + tail + '\n}\n'
        else:
            values.update(additions)
            output = json.dumps(values, ensure_ascii=False, indent=2) + '\n'
        path.write_text(output, encoding='utf-8')
    print('Updated native relic UI labels in zh_cn and en_us.')
