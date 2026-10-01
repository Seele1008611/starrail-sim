// 项目中文说明：把交互式方块模型导出为游戏使用的压缩遗迹蓝图，并检查材料映射。

// Regenerate the packaged test structure from the interactive voxel model.
// Usage: node tools/export_ruin_blueprint.cjs [ordinary|destruction|...]
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const zlib = require('node:zlib');

const designDir = path.resolve(__dirname, '..', '设计方案');
const variantId = process.argv[2] || 'ordinary';
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
const model = modelFor(variantId);
const blockIds = {
  q: 'minecraft:smooth_quartz', qb: 'minecraft:quartz_bricks',
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
  variant: variantId,
  name: def.name,
  bounds,
  palette: materialKeys.map(key => ({key, block: blockIds[key], name: palette[key][1]})),
  blocks: model.map(block => [block.x, block.y, block.z, paletteIndex.get(block.m)])
};
// 蓝图最终写入模组资源目录，供运行时遗迹命令读取。
const output = path.resolve(__dirname, '..', 'src', 'main', 'resources',
  'data', 'starrail_sim', 'ruins', `${variantId}.json.gz`);
fs.mkdirSync(path.dirname(output), {recursive: true});
fs.writeFileSync(output, zlib.gzipSync(Buffer.from(JSON.stringify(data), 'utf8'), {level: 9}));
console.log(`${def.name}: ${model.length.toLocaleString()} blocks, ${materialKeys.length} materials`);
console.log(output);
