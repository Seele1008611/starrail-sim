(() => {
  const asset = name => "../../../崩铁/命途/" + name + "命途.png";
  const paths = [
    {
      key: "H", name: "巡猎", glyph: "巡", iconHue: 202, color: "#79baff", title: "循着猎意，锁定目标",
      kicker: "PATH TRACE / THE HUNT", shape: "贯穿主轴 · 中段下弧 · 下方双侧折枝",
      bonuses: [["暴击率", "+10%"], ["暴击伤害", "+20%"], ["攻击力", "+10%"]],
      stats: [{name:"暴击率",total:10},{name:"攻击力",total:50},{name:"暴击伤害",total:50}],
      mechanic: "击败敌对生物积累猎意。满层后可触发追猎一击。",
      attrNames: ["校准准星","迅捷箭矢","致命落点","弱点洞察","追猎锋芒","终局瞄准"],
      passives: [
        ["精准追击","猎意达到 3 层并触发追猎一击时，该次攻击获得 +5% 暴击率。",5,"✦"],
        ["追猎余势","追猎一击命中后，4 秒内对同一目标的下一次攻击获得 +8% 暴击伤害。",5,"✧"],
        ["逆时猎杀","逆时追猎冷却由 15 秒降低至 12 秒。",7,"✹"]
      ],
      canvas: [0,0,924,875],
      pos: [[480,332],[337,408],[480,488],[630,408],[480,610],[480,763],[480,225],[322,620],[633,620]],
      art: [
        ["M480 122 L480 763","p"],
        ["M310 149 Q480 95 650 149","a"],
        ["M220 286 L337 408 Q480 566 630 408 L742 286","a"],
        ["M112 418 L217 518 L322 620","a"],
        ["M852 418 L747 518 L633 620","a"],
        ["M322 620 Q480 595 633 620","a"]
      ]
    },
    {
      key: "PR", name: "存护", glyph: "盾", iconHue: 215, color: "#91baff", title: "以坚盾守护同行者",
      kicker: "PATH TRACE / PRESERVATION", shape: "贯穿主轴 · 中段斜枝 · 底部浅弧",
      bonuses: [["护甲值","+20%"],["护甲韧性","+15%"]],
      stats: [{name:"护甲值",total:60},{name:"护甲韧性",total:60}],
      mechanic: "成功格挡积累护卫；达到层数后形成护壁，并可反震来袭者。",
      attrNames: ["盾面淬炼","坚壁加固","壁垒核心","护缘叠层","稳固阵列","不破之盾"],
      passives: [
        ["守势余裕","护壁生效时，首次成功格挡额外获得一层短暂吸收护盾；每次护壁限触发一次。",5,"◈"],
        ["反震蓄势","护壁反震命中后，下一次格挡所需的护卫层数减少 1 层。",6,"✦"],
        ["壁垒共鸣","强化护壁期间，反震伤害提高 20%。",7,"⬡"]
      ],
      canvas: [0,0,1013,778],
      pos: [[532,262],[387,421],[532,403],[677,421],[532,564],[532,674],[532,161],[390,683],[678,683]],
      art: [
        ["M532 59 L532 674","p"],
        ["M359 86 Q532 32 704 86","a"],
        ["M262 291 L387 421 Q532 385 677 421 L800 291","a"],
        ["M135 411 L233 524 L390 683","a"],
        ["M928 411 L830 524 L678 683","a"],
        ["M390 683 Q532 653 678 683","a"]
      ]
    },
    {
      key: "AB", name: "丰饶", glyph: "丰", iconHue: 128, color: "#83e0ad", title: "让生机沿枝叶延展",
      kicker: "PATH TRACE / ABUNDANCE", shape: "贯穿主轴 · 双侧外枝 · 花冠下弧",
      bonuses: [["最大生命","+10%"],["治疗效果","+10%"]],
      stats: [{name:"最大生命",total:60},{name:"治疗效果",total:60}],
      mechanic: "治疗事件积累实践度；低生命治疗、溢出治疗与濒死保护逐级增强。",
      attrNames: ["生命萌芽","回春脉络","丰沛根系","甘露回响","生生不息","慈泽满溢"],
      passives: [
        ["回春余韵","生命值低于 50% 时受到治疗，额外获得持续 4 秒的缓慢生命恢复。",4,"❋"],
        ["溢疗化生","溢出治疗转化为吸收量的比例提高 15%。",5,"✿"],
        ["不息庇护","抵挡致死伤害后，额外恢复最大生命值的 10%。",7,"✧"]
      ],
      canvas: [0,0,937,767],
      pos: [[463,284],[324,393],[463,424],[602,393],[463,562],[541,697],[463,70],[245,610],[681,610]],
      art: [
        ["M463 70 L463 684","p"],
        ["M295 108 Q463 34 633 108","a"],
        ["M324 393 Q463 451 602 393","a"],
        ["M211 298 L103 397 L160 493 L245 610","a"],
        ["M714 298 L824 397 L770 493 L681 610","a"],
        ["M245 610 Q463 510 681 610","a"],
        ["M386 697 L463 684 L541 697","a"]
      ]
    },
    {
      key: "DE", name: "毁灭", glyph: "焰", iconHue: 5, color: "#ff8878", title: "于烈焰中锻出绝境锋芒",
      kicker: "PATH TRACE / DESTRUCTION", shape: "贯穿主轴 · 双侧下行折枝 · 两段浅弧",
      bonuses: [["暴击伤害","+30%"],["攻击力","+10%"]],
      stats: [{name:"暴击伤害",total:60},{name:"攻击力",total:60}],
      mechanic: "低生命值下搏斗积累实践度；怒火会强化下一击，绝境状态可触发强化。",
      attrNames: ["余烬攻势","烈火锻锋","焦灼核心","逆燃战意","焚身强袭","终焉余烬"],
      passives: [
        ["余烬不熄","怒火层数达到上限后，持续时间延长 2 秒。",5,"✦"],
        ["破釜一击","消耗满层怒火的强化攻击额外获得 +10% 暴击伤害。",6,"◆"],
        ["绝境回燃","绝境强化触发后，下一次攻击额外造成一次小范围灼烈冲击。",7,"✹"]
      ],
      canvas: [0,0,997,748],
      pos: [[514,257],[349,373],[514,396],[680,373],[514,549],[514,680],[514,153],[335,566],[692,566]],
      art: [
        ["M514 51 L514 680","p"],
        ["M342 79 Q514 23 686 79","a"],
        ["M221 257 L128 392 L227 491 L335 566","a"],
        ["M807 257 L904 392 L804 491 L692 566","a"],
        ["M349 373 Q514 425 680 373","a"],
        ["M335 566 Q514 530 692 566","a"]
      ]
    },
    {
      key: "ER", name: "智识", glyph: "知", iconHue: 266, color: "#c0a2ff", title: "在星图核心推演万象",
      kicker: "PATH TRACE / ERUDITION", shape: "纵横主轴 · 双侧短弧 · 底部浅弧",
      bonuses: [["暴击率","+5%"],["暴击伤害","+15%"],["攻击力","+15%"]],
      stats: [{name:"暴击率",total:10},{name:"暴击伤害",total:50},{name:"攻击力",total:50}],
      mechanic: "一次攻击命中多个敌人可积累实践度；知识回响会随等级扩大。",
      attrNames: ["星图洞察","并行演算","临界精度","回响推演","知识汇流","终局定理"],
      passives: [
        ["多重推演","知识回响额外波及 1 个目标。",4,"✧"],
        ["回响增幅","知识回响造成的伤害提高 15%。",5,"✦"],
        ["群星定论","命中三个不同敌人触发强化攻击后，短时间内下一次知识回响范围扩大。",7,"✹"]
      ],
      canvas: [0,0,945,738],
      pos: [[461,252],[320,418],[461,418],[600,418],[461,650],[324,636],[461,72],[205,418],[714,418]],
      art: [
        ["M461 72 L461 650","p"],
        ["M288 100 Q461 44 635 100","a"],
        ["M100 418 L820 418","a"],
        ["M129 297 Q71 418 129 539","a"],
        ["M791 297 Q849 418 791 539","a"],
        ["M324 636 Q461 670 600 636","a"]
      ]
    },
    {
      key: "NI", name: "虚无", glyph: "蚀", iconHue: 279, color: "#c49aff", title: "让侵蚀沿暗潮蔓延",
      kicker: "PATH TRACE / NIHILITY", shape: "贯穿主轴 · 中段浅拱 · 双侧折枝",
      bonuses: [["效果命中","+20%"],["攻击力","+10%"]],
      stats: [{name:"效果命中",total:50},{name:"攻击力",total:50},{name:"击破特攻",total:50}],
      mechanic: "对未被自己标记的敌人成功施加侵蚀可积累实践度；标记会逐级扩散。",
      attrNames: ["侵蚀刻印","暗潮命中","虚空增压","裂隙破击","沉降攻势","终末扩散"],
      passives: [
        ["侵蚀余波","侵蚀首次扩散时，额外传递给 1 个附近目标。",5,"◉"],
        ["持续崩解","对已被侵蚀标记的目标造成的持续伤害提高 15%。",6,"✦"],
        ["虚空回响","击杀被自己标记的敌人触发爆发后，周围目标额外承受一次较弱的侵蚀。",7,"✹"]
      ],
      canvas: [0,0,1046,767],
      pos: [[532,183],[375,320],[532,311],[688,325],[532,461],[532,570],[532,56],[247,276],[807,276]],
      art: [
        ["M532 56 L532 685","p"],
        ["M357 85 Q531 25 706 85","a"],
        ["M247 276 L132 399 L233 519 L337 640","a"],
        ["M807 276 L933 399 L829 519 L729 640","a"],
        ["M247 276 L375 320 Q532 295 688 325 L807 276","a"]
      ]
    },
    {
      key: "HA", name: "同谐", glyph: "和", iconHue: 43, color: "#f1cf7c", title: "让共鸣连接彼此的力量",
      kicker: "PATH TRACE / HARMONY", shape: "贯穿主轴 · 中段长拱 · 左右错位折枝",
      bonuses: [["护甲值","+5%"],["最大生命","+3%"],["护甲韧性","+2%"]],
      stats: [{name:"护甲值",total:50},{name:"最大生命",total:50},{name:"护甲韧性",total:50}],
      mechanic: "食用食物触发共鸣；共鸣期间击败敌人可延续协奏并推进命途实践。",
      attrNames: ["和弦护持","生命协奏","韧性节拍","共鸣增幅","交响壁垒","终曲守望"],
      passives: [
        ["延音共鸣","共鸣持续时间额外延长 4 秒。",4,"♫"],
        ["协奏强袭","共鸣期间可触发的增伤攻击次数增加 1 次。",5,"✦"],
        ["终曲回护","共鸣期间击败敌人时，额外恢复最大生命值的 5%。",6,"❋"]
      ],
      canvas: [0,0,1014,768],
      pos: [[362,355],[501,330],[639,356],[500,461],[500,593],[500,697],[500,179],[178,425],[830,427]],
      art: [
        ["M500 55 L500 697","p"],
        ["M338 85 Q501 25 665 85","a"],
        ["M226 257 L104 324 L178 425","a"],
        ["M178 425 Q501 235 830 427","a"],
        ["M830 427 L766 542 L649 502","a"],
        ["M347 669 Q501 725 656 669","a"]
      ]
    },
    {
      key: "RE", name: "记忆", glyph: "忆", iconHue: 188, color: "#8ddcf3", title: "将记录化作回响与回忆",
      kicker: "PATH TRACE / REMEMBRANCE", shape: "上部偏心弧 · 下部半环 · 外侧短弧",
      bonuses: [["攻击力","+15%"],["暴击伤害","+15%"]],
      stats: [{name:"攻击力",total:60},{name:"暴击伤害",total:60}],
      mechanic: "记录敌人并触发记忆回响以积累实践度；记录窗口与回响会随等级成长。",
      attrNames: ["初始记录","记忆刻痕","回响增幅","追忆之环","长存印记","终章回响"],
      passives: [
        ["延续记录","记忆记录窗口额外延长 3 秒。",4,"◌"],
        ["回忆追击","记忆回响后，对该目标的下一击额外获得 +10% 攻击力。",5,"✦"],
        ["深层回响","强化回响触发所需的回响累计次数减少 1 次。",7,"✧"]
      ],
      canvas: [0,0,950,796],
      pos: [[493,236],[276,436],[356,572],[713,436],[630,572],[493,617],[342,290],[849,436],[493,747]],
      art: [
        ["M493 236 L493 747","p"],
        ["M286 168 Q420 71 566 109","a"],
        ["M286 168 L342 290 C297 330 269 381 276 436 C283 495 315 543 356 572 C399 603 447 617 493 617 C545 617 589 602 630 572 C676 539 704 489 713 436 L849 436","a"],
        ["M159 298 Q85 436 159 574","a"],
        ["M122 436 L276 436","a"],
        ["M819 298 Q879 436 818 574","a"],
        ["M358 730 Q493 764 628 730","a"]
      ]
    },
    {
      key: "EL", name: "欢愉", glyph: "悦", iconHue: 326, color: "#ff91c8", title: "在连击与欢愉中引爆战局",
      kicker: "PATH TRACE / ELATION", shape: "双上支汇合 · 外侧半弧 · 下部短横枝",
      bonuses: [["暴击率","+6%"],["攻击力","+5%"],["暴击伤害","+5%"]],
      stats: [{name:"暴击率",total:10},{name:"攻击力",total:50},{name:"暴击伤害",total:50}],
      mechanic: "连续命中敌对生物积累连击；连续命中达到节点后触发随机增益与欢愉爆发。",
      attrNames: ["欢声起势","连击节拍","奇趣增幅","笑意回环","高潮迭起","盛宴终章"],
      passives: [
        ["余兴未散","连击中断前的等待窗口额外延长 2 秒。",4,"♪"],
        ["欢愉加码","欢愉爆发造成的伤害提高 15%。",5,"✦"],
        ["盛宴续曲","强化爆发命中后，后续攻击强化持续时间额外延长 2 秒。",7,"✹"]
      ],
      canvas: [0,0,1243,823],
      pos: [[594,94],[450,148],[738,148],[594,265],[594,410],[594,558],[594,725],[306,237],[878,237]],
      art: [
        ["M594 265 L594 725","p"],
        ["M450 148 C503 180 555 225 594 265 C633 225 685 180 738 148","a"],
        ["M450 148 C394 172 339 205 306 237 C268 280 249 328 250 375 C244 424 256 473 276 508 L372 430","a"],
        ["M738 148 C794 172 845 205 878 237 C916 280 939 328 936 375 C942 424 930 473 912 508 L816 430","a"],
        ["M456 526 Q594 590 732 526","a"],
        ["M476 725 L714 725","a"]
      ]
    }
  ];

  const pathMaterials = {
    "欢愉": "《绒绒号》典藏版合集",
    "记忆": "阿赖耶华",
    "虚无": "沉沦黑曜",
    "存护": "琥珀的坚守",
    "毁灭": "净世残刃",
    "同谐": "群星乐章",
    "丰饶": "永恒之花",
    "智识": "智识之钥",
    "巡猎": "逐星之矢"
  };
  const pathMaterialTextures = {
    "欢愉": "fluffy_collection",
    "记忆": "alaya_blossom",
    "虚无": "obsidian_of_obsession",
    "存护": "amber_safeguard",
    "毁灭": "worldbreaker_blade",
    "同谐": "stellar_symphony",
    "丰饶": "flower_of_eternity",
    "智识": "key_of_wisdom",
    "巡猎": "star_chasing_arrow"
  };
  const materialFor = path => pathMaterials[path.name];
  const materialIconFor = path => "../../../src/main/resources/assets/starrail_sim/textures/item/" + pathMaterialTextures[path.name] + ".png";
  const fullTreeMaterialCost = 33;
  const rankTitles = ["","踏上命途","初窥命途","命途共鸣","命途践行","深度践行","高阶行者","命途极境"];
  const states = Object.fromEntries(paths.map(path => [path.key, { selected: path.key + "-A1", filter: "all", unlocked: new Set() }]));
  const nodeEffects = new Map();
  let nodeEffectTimer = null;
  let resetInProgress = false;
  const ui = {
    selector: document.querySelector("#path-selector"),
    nodes: document.querySelector("#tree-nodes"),
    count: document.querySelector("#unlock-count"),
    stage: document.querySelector("#tree-stage"),
    detailPanel: document.querySelector("#detail-panel"),
    button: document.querySelector("#unlock-button"),
    resetNodeButton: document.querySelector("#reset-node-button"),
    status: document.querySelector("#node-status"),
    art: document.querySelector("#tree-art")
  };
  const resetDialog = document.querySelector("#reset-dialog");
  let pendingResetIds = [];
  let active = paths[0];
  let state = states[active.key];
  const nodeList = path => {
    const statIndices = path.stats.length === 2 ? [0,1,0,1,0,1] : [0,1,2,0,1,2];
    const counts = path.stats.map((_, i) => statIndices.filter(index => index === i).length);
    const attrs = path.attrNames.map((title, i) => {
      const branchStart = i < 3 ? 0 : 3;
      const previous = i === branchStart ? [] : [path.key + "-A" + i];
      const stat = path.stats[statIndices[i]];
      const amount = stat.total / counts[statIndices[i]];
      const point = path.pos[i];
      const rank = [1,3,5,2,4,6][i];
      return {
        id: path.key + "-A" + (i + 1), type: "attribute", title, branch: "属性加成 · 第" + (i + 1) + "节点",
        stat: stat.name, amount, effect: stat.name + " +" + amount + "%。", rank, cost: rank, requires: previous,
        x: point[0], y: point[1], glyph: ["◇","✧","✦","◈","✧","✦"][i], short: stat.name.slice(0,1)
      };
    });
    const passives = path.passives.map((entry, i) => {
      const point = path.pos[i + 6];
      return {
        id: path.key + "-P" + (i + 1), type: "passive", title: entry[0], branch: "被动能力 · " + ["第一节点","第二节点","核心节点"][i],
        effect: entry[1], rank: entry[2], cost: i + 3, requires: i ? [path.key + "-P" + i] : [],
        x: point[0], y: point[1], glyph: entry[3], short: i === 2 ? "核" : "被"
      };
    });
    return attrs.concat(passives);
  };
  let nodes = nodeList(active);

  function paintPathSelector() {
    ui.selector.innerHTML = paths.map(path =>
      "<button class=\"path-tab\" type=\"button\" data-path=\"" + path.key + "\" aria-label=\"切换到" + path.name + "命途\" title=\"" + path.name + "\">" +
      "<img src=\"" + asset(path.name) + "\" alt=\"\"><span>" + path.name + "</span></button>"
    ).join("");
    ui.selector.addEventListener("click", event => {
      const button = event.target.closest("[data-path]");
      if (!button || resetInProgress) return;
      clearNodeEffects();
      active = paths.find(path => path.key === button.dataset.path);
      state = states[active.key];
      nodes = nodeList(active);
      renderPath();
    });
  }

  const byId = id => nodes.find(node => node.id === id);
  const labelForType = type => type === "attribute" ? "属性增益" : "被动能力";
  const nodeState = node => {
    if (state.unlocked.has(node.id)) return "unlocked";
    if (5 < node.rank) return "locked";
    if (!node.requires.every(id => state.unlocked.has(id))) return "locked";
    return "available";
  };

  function renderArt() {
    const svg = ui.art.closest("svg");
    svg.setAttribute("viewBox", active.canvas.join(" "));
    svg.setAttribute("preserveAspectRatio", "xMidYMid meet");
    const pathsMarkup = active.art.map(item => {
      const className = item[1] === "p" ? "art-passive" : "art-attribute";
      return "<path class=\"" + className + "\" d=\"" + item[0] + "\"/>";
    }).join("");
    const halos = active.art.map(item => {
      const className = item[1] === "p" ? "art-halo passive" : "art-halo attribute";
      return "<path class=\"" + className + "\" d=\"" + item[0] + "\"/>";
    }).join("");
    const flow = active.art.map((item, index) => {
      const className = item[1] === "p" ? "art-flow passive" : "art-flow attribute";
      return "<path class=\"" + className + "\" pathLength=\"1\" style=\"--flow-offset:-" + (index * .72).toFixed(2) + "s\" d=\"" + item[0] + "\"/>";
    }).join("");
    ui.art.innerHTML = "<g class=\"art-halos\" fill=\"none\" stroke-linecap=\"round\" filter=\"url(#line-glow)\">" + halos +
      "</g><g class=\"art-lines\" fill=\"none\" stroke-linecap=\"round\">" + pathsMarkup +
      "</g><g class=\"art-flow-lines\" fill=\"none\" stroke-linecap=\"round\" filter=\"url(#line-glow)\">" + flow + "</g>";
  }

  function renderPathInfo() {
    const image = asset(active.name);
    document.querySelector("#brand-icon").src = image;
    document.querySelector("#brand-icon").alt = active.name + "命途图标";
    document.querySelector("#aside-icon").src = image;
    document.querySelector("#path-name").textContent = active.name;
    document.querySelector(".path-panel").style.setProperty("--path-glyph", JSON.stringify(active.glyph));
    document.querySelector(".path-panel").style.setProperty("--path-mobile-title", JSON.stringify(active.name + " · " + rankTitles[5]));
    document.querySelector("#page-title").textContent = active.name + "行迹";
    document.querySelector("#rank-name").textContent = rankTitles[5];
    document.querySelector("#path-kicker").textContent = active.kicker;
    document.querySelector("#tree-heading-title").textContent = active.title;
    document.querySelector("#shape-caption").textContent = active.shape;
    document.querySelector("#path-mechanic").textContent = active.mechanic;
    document.querySelector("#bottom-path-label").textContent = active.name + "行迹 · 1.6.0 设计草案";
    document.querySelector("#emblem-watermark").src = image;
    document.querySelector("#emblem-watermark").alt = active.name + "命途纹样";
    document.querySelector("#path-bonuses").innerHTML = active.bonuses.map(item =>
      "<div class=\"bonus-row\"><span>" + item[0] + "</span><b>" + item[1] + "</b></div>"
    ).join("");
    document.querySelector("#aggregate-strip").innerHTML = "<span>全树点亮 · 已确认总量</span>" +
      active.stats.map(stat => "<b>" + stat.name + " +" + stat.total + "%</b>").join("") +
      "<em>行迹额外加成</em><span class=\"aggregate-material\"><img src=\"" + materialIconFor(active) + "\" alt=\"\"><span>全树共 " + fullTreeMaterialCost + " 个 · " + materialFor(active) + "</span></span>";
    document.querySelectorAll(".path-tab").forEach(tab => tab.classList.toggle("active", tab.dataset.path === active.key));
    document.querySelector(".app-shell").style.setProperty("--path-accent", active.color);
  }

  // Match the SVG's uniform scale and letterboxing, including after a resize.
  function positionNodes() {
    const [originX, originY, canvasWidth, canvasHeight] = active.canvas;
    const width = ui.stage.clientWidth;
    const height = ui.stage.clientHeight;
    const scale = Math.min(width / canvasWidth, height / canvasHeight);
    const offsetX = (width - canvasWidth * scale) / 2;
    const offsetY = (height - canvasHeight * scale) / 2;
    ui.nodes.querySelectorAll("[data-node]").forEach(button => {
      const node = byId(button.dataset.node);
      button.style.left = offsetX + (node.x - originX) * scale + "px";
      button.style.top = offsetY + (node.y - originY) * scale + "px";
      button.style.width = Math.min(58, Math.max(30, (node.type === "passive" ? 80 : 68) * scale)) + "px";
    });
  }

  function renderNodes() {
    ui.nodes.innerHTML = nodes.map(node => {
      const current = nodeState(node);
      const dim = state.filter !== "all" && state.filter !== node.type ? "dimmed" : "";
      const selected = state.selected === node.id ? "selected" : "";
      const effect = nodeEffects.get(node.id) || { name: "", delay: 0 };
      const label = node.title + "，" + labelForType(node.type) + "，命途等级 " + node.rank + "，" +
        (current === "unlocked" ? "已点亮" : current === "available" ? "可点亮" : "未开放");
      return "<button class=\"tree-node " + node.type + " " + current + " " + dim + " " + selected + " " + effect.name +
        "\" data-node=\"" + node.id + "\" data-short=\"" + node.short + "\" type=\"button\" aria-label=\"" + label + "\" title=\"" + label +
        "\" style=\"--effect-delay:" + effect.delay + "ms\"><span class=\"node-glyph\">" +
        node.glyph + "</span><span class=\"node-rank\">Lv." + node.rank + "</span></button>";
    }).join("");
    positionNodes();
    ui.nodes.querySelectorAll(".tree-node").forEach(button => button.disabled = resetInProgress);
    ui.stage.classList.toggle("unlocking", Array.from(nodeEffects.values()).some(effect => effect.name === "just-unlocked"));
    ui.stage.classList.toggle("resetting", resetInProgress);
    ui.count.textContent = state.unlocked.size + " / " + nodes.length;
    document.querySelector("#reset-button").disabled = resetInProgress || state.unlocked.size === 0;
    const ring = document.querySelector("#progress-ring");
    ring.style.background = "conic-gradient(var(--path-accent) " + (state.unlocked.size / nodes.length * 360) + "deg, rgba(193,204,237,.16) 0)";
  }

  function nodesToReset(rootId) {
    const ids = new Set([rootId]);
    let changed = true;
    while (changed) {
      changed = false;
      for (const node of nodes) {
        if (!ids.has(node.id) && node.requires.some(id => ids.has(id))) {
          ids.add(node.id);
          changed = true;
        }
      }
    }
    return nodes.filter(node => ids.has(node.id) && state.unlocked.has(node.id));
  }

  function openResetDialog(targetNodes, wholePath) {
    if (!targetNodes.length) return;
    pendingResetIds = targetNodes.map(node => node.id);
    document.querySelector("#reset-title").textContent = wholePath ? "重置" + active.name + "整棵行迹树？" : "重置节点及其后续节点？";
    document.querySelector("#reset-description").textContent = wholePath
      ? "这会清除当前命途已点亮的全部行迹节点。"
      : "该节点有依赖它的后续节点，确认后会一并重置，避免分支前置关系断开。";
    document.querySelector("#reset-preview-list").innerHTML = targetNodes.map(node =>
      "<li><span>" + node.title + "</span><small>" + labelForType(node.type) + " · " + node.id + " · " + materialFor(active) + " × " + node.cost + "（返还）</small></li>"
    ).join("");
    const refundCount = targetNodes.reduce((total, node) => total + node.cost, 0);
    document.querySelector("#reset-refund-value").innerHTML = "<img class=\"refund-material-icon\" src=\"" + materialIconFor(active) + "\" alt=\"\"><span>" + materialFor(active) + " × " + refundCount + "（全额返还预览）</span>";
    resetDialog.showModal();
  }

  function renderDetail() {
    const node = byId(state.selected) || nodes[0];
    state.selected = node.id;
    const current = nodeState(node);
    document.querySelector("#node-id").textContent = node.id;
    document.querySelector("#detail-icon").textContent = node.glyph;
    document.querySelector("#detail-icon").className = "detail-icon " + node.type;
    const type = document.querySelector("#node-type");
    type.textContent = labelForType(node.type);
    type.className = "type-pill " + (node.type === "attribute" ? "attribute-pill" : "passive-pill");
    document.querySelector("#node-title").textContent = node.title;
    document.querySelector("#node-subtitle").textContent = node.branch;
    document.querySelector("#node-effect").textContent = node.effect;
    document.querySelector("#node-rank").textContent = "命途等级 " + node.rank;
    document.querySelector("#node-prerequisite").textContent = node.requires.length ? node.requires.map(id => byId(id).title).join("、") : "无";
    document.querySelector("#node-cost").innerHTML = "<img class=\"material-icon\" src=\"" + materialIconFor(active) + "\" alt=\"\"><span>" + materialFor(active) + "</span><strong>× " + node.cost + "</strong>";
    ui.status.className = "detail-status " + current;
    const statusText = current === "unlocked" ? "已点亮 · 演示状态" : current === "available" ? "等级和前置已满足，可点亮" : 5 < node.rank ? "命途等级达到 " + node.rank + " 后开放" : "先点亮前置节点后开放";
    ui.status.querySelector("span").textContent = statusText;
    ui.button.disabled = current !== "available";
    ui.button.textContent = current === "unlocked" ? "节点已点亮" : current === "available" ? "在样机中点亮节点" : "节点尚未开放";
    ui.resetNodeButton.disabled = current !== "unlocked";
  }

  function render() {
    renderNodes();
    renderDetail();
  }

  function renderPath() {
    renderPathInfo();
    renderArt();
    render();
  }

  function clearNodeEffects() {
    if (nodeEffectTimer) window.clearTimeout(nodeEffectTimer);
    nodeEffectTimer = null;
    nodeEffects.clear();
    ui.detailPanel.classList.remove("detail-updating");
  }

  function playNodeEffect(name, ids, duration) {
    clearNodeEffects();
    ids.forEach(id => nodeEffects.set(id, { name, delay: 0 }));
    render();
    ui.detailPanel.classList.remove("detail-updating");
    void ui.detailPanel.offsetWidth;
    ui.detailPanel.classList.add("detail-updating");
    nodeEffectTimer = window.setTimeout(() => {
      nodeEffects.clear();
      nodeEffectTimer = null;
      ui.detailPanel.classList.remove("detail-updating");
      renderNodes();
    }, duration);
  }

  ui.nodes.addEventListener("click", event => {
    if (resetInProgress) return;
    const target = event.target.closest("[data-node]");
    if (!target) return;
    state.selected = target.dataset.node;
    playNodeEffect("just-selected", [state.selected], 620);
  });

  document.querySelectorAll("[data-filter]").forEach(filterButton => {
    filterButton.addEventListener("click", () => {
      if (resetInProgress) return;
      clearNodeEffects();
      state.filter = filterButton.dataset.filter;
      document.querySelectorAll("[data-filter]").forEach(item => item.classList.toggle("active", item === filterButton));
      renderNodes();
    });
  });

  ui.button.addEventListener("click", () => {
    if (resetInProgress) return;
    const node = byId(state.selected);
    if (!node || nodeState(node) !== "available") return;
    state.unlocked.add(node.id);
    playNodeEffect("just-unlocked", [node.id], 900);
  });

  ui.resetNodeButton.addEventListener("click", () => {
    if (resetInProgress) return;
    const node = byId(state.selected);
    if (!node || !state.unlocked.has(node.id)) return;
    openResetDialog(nodesToReset(node.id), false);
  });

  document.querySelector("#reset-button").addEventListener("click", () => {
    if (resetInProgress) return;
    openResetDialog(nodes.filter(node => state.unlocked.has(node.id)), true);
  });

  document.querySelector("#reset-confirm").addEventListener("click", () => {
    if (resetInProgress || !pendingResetIds.length) return;
    clearNodeEffects();
    const resetIds = [...pendingResetIds];
    resetDialog.close();
    resetInProgress = true;
    ui.detailPanel.classList.add("detail-retreat");
    [...resetIds].reverse().forEach((id, index) => nodeEffects.set(id, { name: "reversing", delay: index * 78 }));
    renderNodes();
    ui.button.disabled = true;
    ui.resetNodeButton.disabled = true;
    const duration = 560 + Math.max(0, resetIds.length - 1) * 78;
    window.setTimeout(() => {
      resetIds.forEach(id => state.unlocked.delete(id));
      pendingResetIds = [];
      nodeEffects.clear();
      resetInProgress = false;
      ui.detailPanel.classList.remove("detail-retreat");
      render();
    }, duration);
  });

  document.querySelector("#reset-cancel").addEventListener("click", () => resetDialog.close());
  document.querySelector("#reset-cancel-x").addEventListener("click", () => resetDialog.close());

  document.querySelector(".close-button").addEventListener("click", () => {
    document.querySelector(".close-button").classList.toggle("pressed");
    document.querySelector(".close-button").title = "这是独立 HTML 样机，关闭页面即可返回";
  });

  function startStars() {
    const canvas = document.querySelector("#starfield");
    const ctx = canvas.getContext("2d");
    let width = 0;
    let height = 0;
    let stars = [];
    const resize = () => {
      const scale = Math.min(window.devicePixelRatio || 1, 2);
      width = window.innerWidth;
      height = window.innerHeight;
      canvas.width = width * scale;
      canvas.height = height * scale;
      ctx.setTransform(scale, 0, 0, scale, 0, 0);
      stars = Array.from({ length: Math.round(width * height / 7000) }, () => ({
        x: Math.random() * width,
        y: Math.random() * height,
        r: Math.random() * 1.25 + .25,
        phase: Math.random() * Math.PI * 2,
        speed: Math.random() * .7 + .25
      }));
    };
    const draw = time => {
      ctx.clearRect(0, 0, width, height);
      for (const star of stars) {
        const pulse = .16 + (Math.sin(time * .001 * star.speed + star.phase) + 1) * .24;
        ctx.beginPath();
        ctx.arc(star.x, star.y, star.r, 0, Math.PI * 2);
        ctx.fillStyle = "rgba(218,230,255," + pulse + ")";
        ctx.fill();
      }
      requestAnimationFrame(draw);
    };
    resize();
    window.addEventListener("resize", resize, { passive: true });
    requestAnimationFrame(draw);
  }

  paintPathSelector();
  renderPath();
  new ResizeObserver(positionNodes).observe(ui.stage);
  startStars();
})();
