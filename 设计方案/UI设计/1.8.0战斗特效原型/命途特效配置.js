(() => {
  'use strict';
  const assetRoot = '../../../src/main/resources/assets/starrail_sim/textures/gui/traces/';
  // Main hues follow the project's accepted ruin-anchor series. These are design
  // values for this prototype, not a claimed official set of hexadecimal colors.
  const palettes = {
    hunt:{main:'#67cef2',light:'#e0f8ff',shade:'#327caa',accent:'#a8eaff',label:'蓝青 · 银白'},
    preservation:{main:'#edb64e',light:'#fff1bd',shade:'#986322',accent:'#d78d35',label:'琥珀金 · 暖黄'},
    abundance:{main:'#77d99b',light:'#e3ffd8',shade:'#39876a',accent:'#d7eaa2',label:'生命绿 · 浅金'},
    destruction:{main:'#fa795d',light:'#ffe1ad',shade:'#a7413f',accent:'#ffb34e',label:'赤橙 · 火金'},
    erudition:{main:'#bd87f5',light:'#f3e6ff',shade:'#7151a0',accent:'#dabdff',label:'亮紫 · 银白'},
    nihility:{main:'#956ae1',light:'#d9c8ff',shade:'#44365d',accent:'#7150ae',label:'深紫 · 暗灰'},
    harmony:{main:'#ee97ca',light:'#ffe9f6',shade:'#995b8d',accent:'#f0d18c',label:'玫粉 · 柔金'},
    remembrance:{main:'#8addf0',light:'#e6fcff',shade:'#4b8fae',accent:'#b6d3ff',label:'冰蓝 · 晶白'},
    elation:{main:'#ff82b7',light:'#ffe5ed',shade:'#b84d85',accent:'#ffc77a',label:'亮粉 · 彩色点缀'}
  };
  const effect = (id, name, tier, condition, visual, options = {}) => ({id, name, tier, condition, visual, duration: 3, impact: .57, ...options});
  const paths = [
    {id:'hunt', name:'巡猎', subtitle:'单体追击', effects:[
      effect('pursuit','追猎一击','5 级','猎意达到 3 层后，下一次有效命中消耗猎意并强化伤害。','近战沿剑锋蓄光，再用交错锋弧贴身挥斩；远程沿实际命中方向显现带箭杆与尾羽的光矢，命中时张开尖锐双翼碎光。'),
      effect('reverse','逆时追猎','7 级','猎意满层且独立冷却结束后，强化攻击命中目标。','光轨先逆向回卷，再释放近战锋弧或远程主光矢；四道倾天光矢从上方合击，在同一命中时刻收束，双翼锐芒随后展开。',{strong:true})]},
    {id:'preservation', name:'存护', subtitle:'护壁与反震', effects:[
      effect('wall','护壁','5 级','成功格挡积满护卫层数，满足冷却条件后获得护壁。','双层中空盾面与六边形护盾阵列依次展开，脚下亮起守护边界，存护网格轮廓悬浮显影。',{self:true}),
      effect('counter','反震','6 级','护壁期间成功格挡，向攻击者返还护甲值对应的伤害。','护盾阵列与存护网格轮廓先亮起，盾缘收紧后向攻击者推出分段冲击，形成菱形回震。',{impact:.77})]},
    {id:'abundance', name:'丰饶', subtitle:'生命绽放', effects:[
      effect('heal','生生不息','4 级','治疗前生命值低于 80%，且冷却结束时触发生命恢复。','枝条与叶瓣由两侧舒展，生命光点沿枝叶上升，丰饶网格轮廓短暂显影。',{self:true,heal:true}),
      effect('manna','甘露','5 级','治疗出现溢出时，将对应比例的溢出治疗转为吸收盾。','舒展的枝叶环抱玩家，生命光点在身体外侧合成护持轮廓，上方显现丰饶图案。',{self:true,heal:true}),
      effect('mercy','慈航','7 级','受到致死伤害且冷却结束时触发濒死救援。','脚下莲瓣绽放，双侧枝叶舒展，生命光雨汇入玩家；丰饶轮廓显影后散成生命微粒。',{self:true,strong:true,heal:true,duration:3.4})]},
    {id:'destruction', name:'毁灭', subtitle:'怒火爆发', effects:[
      effect('rage','怒火','5 级','符合低生命条件时受到敌对伤害，积累怒火层数。','玩家周围燃起小型裂火与火冠，暗红火星上涌，毁灭轮廓短暂显影，表现怒火积累。',{self:true}),
      effect('burn','焚身','6 级','积满怒火后准备强化，随后命中时消耗强化。','玩家火星收束到攻击方向，目标脚下裂火张开，火冠与毁灭轮廓随命中爆发。'),
      effect('desperate','绝境','7 级','生命值不高于 25%，且满足绝境冷却条件时强化下一击。','多道地面裂火撕开，火冠向上爆发，毁灭网格轮廓显影后崩散，余烬迅速下落。',{strong:true})]},
    {id:'erudition', name:'智识', subtitle:'多目标推演', effects:[
      effect('knowledge','知识回响','4 级','攻击命中目标后，对范围内符合条件的其他敌人追加伤害。','亮紫计算环绕目标展开，智识轮廓聚合显影，眼形核心与计算光轨连接邻近目标，各目标出现推演脉冲。',{multi:true}),
      effect('endgame','终局推演','7 级','主目标附近至少另有两只敌人，且独立冷却结束时触发范围追加伤害。','亮紫计算环与智识轮廓先聚合，四方节点依次点亮；计算光轨连接周边目标，各目标展开几何脉冲。',{strong:true,multi:true,impact:.72})]},
    {id:'nihility', name:'虚无', subtitle:'侵蚀与扩散', effects:[
      effect('erosion','侵蚀施加','基础','命中敌人并通过效果命中判定后，施加侵蚀。','深紫侵蚀环向内收缩，虚无轮廓由离散网格聚合，消散时留下附着微粒。'),
      effect('spread','侵蚀扩散','5 级','首次标记目标时，向附近符合条件的未标记敌人尝试扩散。','虚无轮廓在收束的侵蚀环中显现，暗紫网格分出弧线并流向邻近敌人。',{multi:true}),
      effect('void-end','虚无终局','7 级','击杀自己标记的敌人后，对周围存活敌人施加侵蚀并造成伤害。','侵蚀环向暗涡中心收束，虚无网格轮廓随后崩散，释放紫色回响，邻近目标出现侵蚀光点。',{strong:true,multi:true})]},
    {id:'harmony', name:'同谐', subtitle:'共鸣节拍', effects:[
      effect('resonance','共鸣','基础','食用食物后获得共鸣，持续时间与当前命途等级有关。','玫粉双翼光带按节拍展开，柔金音符沿弧线升起，同谐轮廓聚合显影后散开。',{self:true}),
      effect('afterglow','余响','5 级','共鸣期间，符合次数条件的攻击获得额外伤害。','玫粉节拍光轨连接目标，命中时展开双翼音律光带、柔金音符与同谐网格轮廓。'),
      effect('concert','协奏回响','6 级','共鸣期间击杀敌人，且恢复冷却结束时恢复生命。','回响沿玫粉光带返回玩家，双翼音律和同谐轮廓显现，柔金节点按节拍点亮后汇入身体。',{self:true,heal:true}),
      effect('unison','万众同调','7 级','食用时获得短暂吸收盾；共鸣期间击杀可延长共鸣。','多层双翼音律光带包围玩家，同谐轮廓显影，柔金音符与节点向上合拍升起，表现强化增益。',{self:true,strong:true})]},
    {id:'remembrance', name:'记忆', subtitle:'记录与回响', effects:[
      effect('record','记录目标','基础','攻击命中敌人，记录该目标并开启记忆窗口。','冰晶环绕目标，六臂雪晶与沙漏标记依次出现，记忆网格轮廓显影后碎成细小晶点。'),
      effect('echo','回响','基础','在记忆窗口内再次命中同一目标，且满足回响间隔条件。','冰晶沿目标环绕，折返轨迹按两拍亮起，雪晶与记忆轮廓在回响命中时绽放。',{impact:.8}),
      effect('eternal','永恒回响','7 级','累计达到规定次数的回响后，下一次有效回响获得强化，并进入独立冷却。','双层雪晶与折返轨迹错开亮起，强化回响以更亮的晶白碎片释放。',{strong:true,impact:.99})]},
    {id:'elation', name:'欢愉', subtitle:'连击与狂欢', effects:[
      effect('burst','连击爆发','基础','连击窗口内每第三次有效命中，触发额外伤害及对应等级的随机增益。','粉色棱面网格从命中点跳跃弹开，短小弧线形成轻快的爆发节奏。'),
      effect('grand','终极欢愉','7 级','每第六次连击且冷却结束时，强化本次攻击并额外掷骰。','两次错开的跳跃爆发与彩色方块碎光；保持粉色为主，避免颜色杂乱。',{strong:true})]}
  ];
  paths.forEach(path => {path.palette=palettes[path.id];path.color=path.palette.main;});
  const common = {
    hit:effect('hit','韧性削减','通用','有效直接攻击成功削减目标韧性。','少量白色方形碎光在命中点弹开，保留对普通攻击的轻量反馈。'),
    break:effect('break','韧性击破','通用','目标韧性归零时触发一次击破。','目标外侧的轮廓先出现裂纹，再碎成几何碎片；脚边出现一次扩散环，与已有红色破韧数字配合。',{strong:true})
  };

  window.combatMeshDesign = {paths, common, assetRoot};
})();
