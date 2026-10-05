// 项目中文说明：把交互式方块模型导出为游戏使用的压缩遗迹蓝图，并检查材料映射。

// 从交互式方块模型生成压缩遗迹蓝图。
// Usage: node tools/export_ruin_blueprint.cjs [ordinary|destruction|...] [--guard] [--theme-v2]
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const zlib = require('node:zlib');

const designDir = path.resolve(__dirname, '..', '设计方案', '遗迹设计', '模型源文件');
const variantId = process.argv[2] || 'ordinary';
const guarded = process.argv.includes('--guard');
const themeV2 = process.argv.includes('--theme-v2');
if (themeV2 && !guarded) throw new Error('--theme-v2 must be combined with --guard.');
if (themeV2 && variantId === 'ordinary') throw new Error('The v2 design changes the nine Path variants; the ordinary baseline is preserved.');
const variantSource = fs.readFileSync(path.join(designDir, '命途变体.js'), 'utf8');
const interiorSource = fs.readFileSync(path.join(designDir, '命途内饰.js'), 'utf8');
let modelSource = fs.readFileSync(path.join(designDir, '天枢圣所模型.js'), 'utf8');
// 只保留模型初始化与方块数据定义，避免运行浏览器渲染部分。
const endOfModel = modelSource.indexOf('  const rooms={');
if (endOfModel < 0) throw new Error('Could not find voxel model initialization boundary.');
modelSource = modelSource.slice(0, endOfModel)
  + 'window.__exportModel={baseModel,modelFor,palette};\n})();';

const window = {};
const context = {window, document: {getElementById: () => ({})}};
vm.createContext(context);
vm.runInContext(interiorSource, context);
vm.runInContext(variantSource, context);
vm.runInContext(modelSource, context);

// 先确认请求的变体存在，再用相同模型生成逻辑构建数据。
const def = window.RuinVariants.definitions.find(entry => entry.id === variantId);
if (!def) throw new Error(`Unknown ruin variant: ${variantId}`);
const {modelFor, palette} = window.__exportModel;
let model = modelFor(variantId);
let themeBlocks = [];
let themeMaterials = {};
if (guarded && themeV2) {
  // 从 HTML 设计源读取逐格房间数据，避免游戏蓝图与审阅过的分层图分叉。
  const themeWindow = {__RUIN_THEME_EXPORT__: true};
  const themeContext = {window: themeWindow, document: {getElementById: () => null}};
  vm.createContext(themeContext);
  const themeSource = fs.readFileSync(path.join(designDir, '1.3.0守卫室主题.js'), 'utf8');
  vm.runInContext(themeSource, themeContext);
  const study = themeWindow.RuinThemeStudy;
  if (!study || !study.ids.includes(variantId)) throw new Error(`Theme design not found: ${variantId}`);
  themeBlocks = study.getModel(variantId);
  themeMaterials = study.materials;
  // 旧母体模型继续提供浮岛、塔楼、庭院和外部连廊；仅替换守卫室包围盒。
  const inGuardRoom = b => b.x >= -12 && b.x <= 12 && b.y >= 7 && b.y <= 24
    && b.z >= -26 && b.z <= 0;
  model = model.filter(b => !inGuardRoom(b)).concat(themeBlocks.map(b => ({
    x: b.x, y: b.y, z: b.z, m: `theme:${b.id}`
  })));
} else if (guarded) {
  // 守卫室复用同一布局；build 会按命途调色板替换墙面玻璃与强调色。
  window.RuinDesign = {getModelFor:modelFor, getPalette:()=>palette};
  let guardSource = fs.readFileSync(path.join(designDir, '监守者守卫室.js'), 'utf8');
  guardSource = guardSource.slice(0, guardSource.indexOf("  const canvas=$('guardCanvas')"))
    + `window.__guardModel=build(${JSON.stringify(variantId)},false);\n})();`;
  vm.runInContext(guardSource, context);
  model = window.__guardModel;
  palette.guardBars = ['#a4bac0', '铁栏杆'];
}
const blockIds = {
  guardBars: 'minecraft:iron_bars', q: 'minecraft:smooth_quartz', qb: 'minecraft:quartz_bricks',
  pillar: 'minecraft:quartz_pillar', calcite: 'minecraft:calcite',
  prism: 'minecraft:prismarine_bricks', dark: 'minecraft:dark_prismarine',
  black: 'minecraft:polished_blackstone_bricks', slate: 'minecraft:deepslate_tiles',
  glow: 'minecraft:sea_lantern', glass: 'minecraft:cyan_stained_glass',
  amethyst: 'minecraft:amethyst_block', copper: 'minecraft:waxed_cut_copper',
  chest: 'minecraft:chest', seat: 'minecraft:smooth_quartz_slab',
  carpet: 'minecraft:cyan_carpet', shelf: 'minecraft:bookshelf',
  wood: 'minecraft:spruce_planks', lectern: 'minecraft:lectern',
  chain: 'minecraft:chain', rod: 'minecraft:end_rod', moss: 'minecraft:moss_block',
  leaves: 'minecraft:azalea_leaves', ladder: 'minecraft:ladder',
  redbrick: 'minecraft:red_nether_bricks', redglass: 'minecraft:red_stained_glass',
  magma: 'minecraft:magma_block', oxidized: 'minecraft:oxidized_cut_copper',
  emerald: 'minecraft:emerald_block', lapis: 'minecraft:lapis_block',
  blueglass: 'minecraft:blue_stained_glass', pink: 'minecraft:pink_terracotta',
  magentaglass: 'minecraft:magenta_stained_glass', crying: 'minecraft:crying_obsidian',
  obsidian: 'minecraft:obsidian', purpleglass: 'minecraft:purple_stained_glass',
  andesite: 'minecraft:polished_andesite', blueice: 'minecraft:blue_ice',
  iron: 'minecraft:iron_block', greenglass: 'minecraft:green_stained_glass',
  packedice: 'minecraft:packed_ice', lightblueglass: 'minecraft:light_blue_stained_glass',
  orange: 'minecraft:orange_terracotta', yellow: 'minecraft:yellow_terracotta'
};

if (themeV2) {
  // 为 v2 房间内饰补齐原版方块注册名及中文材料名，供压缩蓝图统一编码。
  const blockIdToKey = new Map(Object.entries(blockIds).map(([key, id]) => [id, key]));
  const themeIds = [...new Set(themeBlocks.map(block => block.id))];
  const themeKeyById = new Map();
  for (const id of themeIds) {
    const blockId = `minecraft:${id}`;
    let key = blockIdToKey.get(blockId);
    if (!key) {
      key = `theme_${id}`;
      blockIds[key] = blockId;
      blockIdToKey.set(blockId, key);
    }
    const material = themeMaterials[id];
    if (!material) throw new Error(`Missing v2 material metadata: ${id}`);
    if (!palette[key]) palette[key] = material;
    themeKeyById.set(id, key);
  }
  for (const block of model) {
    if (block.m.startsWith('theme:')) block.m = themeKeyById.get(block.m.slice(6));
  }
}

// 核对模型中出现的每种材料都有 Minecraft 方块映射和调色板信息。
const materialKeys = [...new Set(model.map(block => block.m))].sort();
const missing = materialKeys.filter(key => !blockIds[key] || !palette[key]);
if (missing.length) throw new Error(`Missing block mapping: ${missing.join(', ')}`);
const paletteIndex = new Map(materialKeys.map((key, index) => [key, index]));
const bounds = model.reduce((b, block) => ({
  minX: Math.min(b.minX, block.x), maxX: Math.max(b.maxX, block.x),
  minY: Math.min(b.minY, block.y), maxY: Math.max(b.maxY, block.y),
  minZ: Math.min(b.minZ, block.z), maxZ: Math.max(b.maxZ, block.z)
}), {minX: Infinity, maxX: -Infinity, minY: Infinity, maxY: -Infinity,
  minZ: Infinity, maxZ: -Infinity});
// 组装蓝图版本、边界、调色板和每个方块的局部坐标。
const data = {
  version: 1,
  guarded,
  variant: variantId,
  name: def.name,
  bounds,
  palette: materialKeys.map(key => ({key, block: blockIds[key], name: palette[key][1]})),
  blocks: model.map(block => [block.x, block.y, block.z, paletteIndex.get(block.m)])
};
// 蓝图最终写入模组资源目录，供运行时遗迹命令读取。
const output = path.resolve(__dirname, '..', 'src', 'main', 'resources',
  'data', 'starrail_sim', 'ruins', `${variantId}${guarded?'_guard':''}.json.gz`);
fs.mkdirSync(path.dirname(output), {recursive: true});
fs.writeFileSync(output, zlib.gzipSync(Buffer.from(JSON.stringify(data), 'utf8'), {level: 9}));
console.log(`${def.name}: ${model.length.toLocaleString()} blocks, ${materialKeys.length} materials`);
console.log(output);
