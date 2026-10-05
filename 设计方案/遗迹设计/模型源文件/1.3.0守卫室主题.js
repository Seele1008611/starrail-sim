/* 1.3.0 守卫室主题研究：本文件生成浏览器预览，不连接游戏蓝图导出器。 */
(() => {
  'use strict';
  const $ = id => document.getElementById(id);
  const paths = [
    {id:'destruction',name:'毁灭',en:'DESTRUCTION',title:'裂焰圣所',motif:'破裂的双刃与向心裂纹',pitch:'赤红砖痕像力量在石室中留下的回响，裂纹汇向门楣中央。',entrance:'门上方的双刃折线朝中央收束，像冲击波在石墙上定格。',walls:'红色下界砖沿墙缝构成断续裂纹，暗色底边稳住厚重感。',combat:'仅用纹样强调方向；战斗地坪仍是平整石质，墙饰不伸入交战空间。',palette:[['red_nether_bricks','红色下界砖','#6e3438'],['red_stained_glass','红色染色玻璃','#ad5555'],['nether_bricks','下界砖','#49323a'],['blackstone','黑石','#393c47']],symbol:'裂',glass:true},
    {id:'hunt',name:'巡猎',en:'THE HUNT',title:'逐星箭庭',motif:'收束视线的直线箭标',pitch:'修长的青铜绿线条穿过门楣，笔直指向后方的宝箱。',entrance:'高挑的中央箭标与细长窗格在入口形成明确的纵向视线。',walls:'氧化铜色边线和青色窗带保持纤细，点出瞄准与追踪的秩序。',combat:'地坪上的单线引导通向监守者，战斗结束后继续指向后方门。',palette:[['waxed_oxidized_cut_copper','涂蜡氧化切制铜块','#4c927f'],['cyan_stained_glass','青色染色玻璃','#559f9b'],['emerald_block','绿宝石块','#42a37b'],['prismarine_bricks','海晶石砖','#315a59']],symbol:'箭',glass:true},
    {id:'erudition',name:'智识',en:'ERUDITION',title:'天体演算院',motif:'交汇的星轨与中心算点',pitch:'青金石星轨交错在主墙，紫晶核心落在门楣上方。',entrance:'对称交叉星轨围绕单一中心，远看像一座嵌入墙面的观测仪。',walls:'青金石、紫晶和蓝玻璃采用分层细线，保留墙体大面积石质留白。',combat:'地面用四向坐标线标出战斗中心；图案不铺满行走区。',palette:[['lapis_block','青金石块','#354c88'],['blue_stained_glass','蓝色染色玻璃','#507bb4'],['amethyst_block','紫水晶块','#805a9e'],['polished_deepslate','磨制深板岩','#444957']],symbol:'星',glass:true},
    {id:'harmony',name:'同谐',en:'HARMONY',title:'共鸣圣堂',motif:'相连的三重音波',pitch:'三道粉色声纹由两侧相会，让守卫室显得宁静而庄严。',entrance:'三个相连的拱弧构成门上主纹，中央弧线与入口同轴。',walls:'粉色陶瓦描出音波，品红玻璃嵌在两侧窗带；墙脚沿用共通深色石材。',combat:'地坪左右成对呼应，中轴保持通畅，守卫与玩家仍有完整回旋空间。',palette:[['pink_terracotta','粉红色陶瓦','#b47a82'],['magenta_stained_glass','品红色染色玻璃','#a24d91'],['quartz_pillar','石英柱','#ded7ca'],['polished_blackstone_bricks','磨制黑石砖','#41444a']],symbol:'音',glass:true},
    {id:'nihility',name:'虚无',en:'NIHILITY',title:'寂静空庭',motif:'偏移的断环与大片留白',pitch:'稀疏紫光落在断开的暗色圆环之间，空处也成为图案的一部分。',entrance:'偏心断环不占满墙面；光只落在门侧与地坪的少数节点。',walls:'黑曜石、紫色玻璃与哭泣的黑曜石构成低饱和冷色层次。',combat:'中央地坪不铺暗色材质，人物轮廓和监守者动作仍清晰可见。',palette:[['obsidian','黑曜石','#282935'],['purple_stained_glass','紫色染色玻璃','#67527d'],['crying_obsidian','哭泣的黑曜石','#54416f'],['polished_blackstone_bricks','磨制黑石砖','#353741']],symbol:'环',glass:true},
    {id:'preservation',name:'存护',en:'PRESERVATION',title:'晶盾堡垒',motif:'层层收拢的盾形护壁',pitch:'厚实的护盾纹样包住门框，铁块窄线像盾面上的冷光。',entrance:'宽阔盾形轮廓让门洞成为被保护的核心，而不是被堵住的通路。',walls:'磨制安山岩作结构边框，铁块仅作窄线镶嵌。',combat:'重色集中在墙体与外围地坪，避免铁块占用中央战斗格。',palette:[['polished_andesite','磨制安山岩','#838d90'],['light_blue_stained_glass','淡蓝色染色玻璃','#8bb9d0'],['iron_block','铁块','#d0d5d2'],['deepslate_tiles','深板岩瓦','#40464a']],symbol:'盾',glass:true},
    {id:'abundance',name:'丰饶',en:'ABUNDANCE',title:'生息神苑',motif:'由根系托起的生命树',pitch:'苔藓色枝蔓由地面缓慢上升，在门楣中央舒展成树冠。',entrance:'向上展开的枝形纹样把目光带到门楣核心，叶色装饰收在高处。',walls:'苔藓砖和绿色玻璃嵌入石墙；不使用会蔓延或垂落的活植物。',combat:'地坪保持干燥、平坦；绿意集中于门楣和墙边，不占通行格。',palette:[['mossy_stone_bricks','苔石砖','#526d53'],['green_stained_glass','绿色染色玻璃','#56856b'],['moss_block','苔藓块','#607d45'],['polished_blackstone_bricks','磨制黑石砖','#3c4543']],symbol:'树',glass:true},
    {id:'remembrance',name:'记忆',en:'REMEMBRANCE',title:'镜映回廊',motif:'逐层重复的镜框与冰晶',pitch:'淡蓝色门框逐层后退，像同一段记忆在长廊中重复映现。',entrance:'三重矩形门框建立透视深度，中心留白指向通往宝箱的门。',walls:'方解石、浮冰与淡蓝玻璃分段镶嵌，冰材仅置于室内墙面。',combat:'冰晶纹样不落在地板行走区，避免滑行或意外融化。',palette:[['packed_ice','浮冰','#91b8cf'],['light_blue_stained_glass','淡蓝色染色玻璃','#a5d5df'],['calcite','方解石','#dddcd8'],['blue_ice','蓝冰','#638ead']],symbol:'忆',glass:true},
    {id:'elation',name:'欢愉',en:'ELATION',title:'奇想剧场',motif:'面具眉弧与微笑舞台线',pitch:'暖橙与柔粉色在严整石室里组成一张含蓄的欢愉面具。',entrance:'门楣上以两道眉弧和微笑线点题，保持庄重比例，不做杂乱拼色。',walls:'橙色、粉色陶瓦与品红窗格用作少量舞台色块，墙面仍以石英为主。',combat:'图案集中于高位和地坪外围，中央保持规整，战斗空间不会变成障碍赛。',palette:[['orange_terracotta','橙色陶瓦','#ad7055'],['pink_terracotta','粉红色陶瓦','#b58083'],['magenta_stained_glass','品红色染色玻璃','#a15391'],['polished_blackstone_bricks','磨制黑石砖','#41424a']],symbol:'笑',glass:true}
  ];
  // 第二稿以真实的三维构造区分命途，说明文本也随对应模型更新。
  const spatial={
    destruction:['倾斜巨肋 · 错位断梁','两组巨型斜肋向门楣汇聚，一侧保留断开与错位的梁段。','粗斜肋由两侧高位伸入殿顶，形成受冲击后的张力。','上部梁架呈不对称断裂，墙脚与外壳仍完整；地面用折线裂纹呼应。'],
    hunt:['三重尖拱箭廊','连续三道尖拱沿中轴前进，像一张张拉开的弓指向后室。','由近到远排列尖拱，中央高而两侧收束，远观有明确的箭廊轮廓。','直线边肋和连续地面箭头形成方向感，窗带细长，层次沿纵轴推进。'],
    erudition:['水平天球环 · 竖向观测仪','一座水平星轨悬在殿顶，与竖向观测环交错构成三维演算装置。','横竖两种环面相交，中央紫晶点位与两侧小仪台组成观测系统。','高位环体、径向连杆与地面刻度盘分别处于不同高度，形成可读的仪器层次。'],
    harmony:['双重圆拱 · 两侧音管阵列','圆拱与等距音管围成共鸣大厅，空间节奏来自成组重复。','两道圆弧拱架跨过主殿，两侧音管高低起伏，轮廓柔和而对称。','音管像壁面上的风琴，圆拱在前后呼应，地面双声纹沿侧缘延伸。'],
    nihility:['偏心断环 · 离散悬石','两个不同方向的断环悬在留白中，稀疏碎石让中心显得空寂。','偏心环在空中中断，第二道侧向环偏离轴线，强调不完整与悬浮。','构件不组成连续廊架；深色凹窗、断开的环段和几枚悬石拉开疏密。'],
    preservation:['厚重壁垛 · 层叠横梁','两侧壁垛托住厚横梁，门前再加一层盾形护架，像堡垒的内门。','粗壁垛和双层横梁构成宽厚矩形，护盾框把铁栅门包在核心。','侧壁按三组加厚，顶部带短齿；地坪以方形边框强化稳固感。'],
    abundance:['分叉树肋 · 高位冠盖','两侧石质树干在高处交织，冠盖沿墙舒展成一片静止的绿荫。','树肋从侧边向上分叉，顶部苔藓冠块打破规则梁架的外形。','枝条高低错落，根系嵌入地坪；绿意以固定方块表达，保留通行空间。'],
    remembrance:['四道递进镜框','四层方形镜框在纵深中重复，边框宽度和高度逐步变化。','重复矩形框连续后退，远看就是一条有层次的镜映回廊。','冰质窄框只位于高处与壁面，地坪用方解石复写矩形节奏，行走面不铺冰。'],
    elation:['剧场门楼 · 错层侧廊','舞台式门楼与左右错层小廊组成剧场，面具藏在上部陈列带中。','主门是宽阔的舞台拱台，两侧台沿一高一低，打破母体的完全对称。','暖色幕边垂在高位，侧廊有不同高度的台沿，地面用半圆舞台线收束。']
  };
  for(const p of paths){const s=spatial[p.id];p.motif=s[0];p.pitch=s[1];p.entrance=s[2];p.walls=s[3];p.combat='新增构件主要放在两侧 X=±11 与 Y≥14 的高位。中央 21×15 格的低位交战空间、梯井、铁门和宝箱位置继续保留。';}
  // 普通母体只用于并排比较，不作为第十种命途选项。
  const basePath={id:'ordinary',name:'普通母体',palette:[['prismarine_bricks','海晶石砖','#39716a'],['light_blue_stained_glass','淡蓝色染色玻璃','#91bbc2'],['sea_lantern','海晶灯','#9bd7c5'],['polished_blackstone_bricks','磨制黑石砖','#41454a']]};
  const blocks={
    quartz_bricks:['石英砖','#d9d4c9'],quartz_pillar:['石英柱','#d7d2c6'],smooth_quartz:['平滑石英','#e5e0d5'],
    polished_blackstone_bricks:['磨制黑石砖','#41434a'],blackstone:['黑石','#29242a'],deepslate_bricks:['深板岩砖','#373c43'],sea_lantern:['海晶灯','#a7ded0'],iron_bars:['铁栏杆','#aeb8ba'],chest:['箱子','#ad7440'],
    red_nether_bricks:['红色下界砖','#6e3438'],red_stained_glass:['红色染色玻璃','#ad5555'],nether_bricks:['下界砖','#49323a'],
    waxed_oxidized_cut_copper:['涂蜡氧化切制铜块','#4c927f'],cyan_stained_glass:['青色染色玻璃','#559f9b'],emerald_block:['绿宝石块','#42a37b'],prismarine_bricks:['海晶石砖','#315a59'],
    lapis_block:['青金石块','#354c88'],blue_stained_glass:['蓝色染色玻璃','#507bb4'],amethyst_block:['紫水晶块','#805a9e'],polished_deepslate:['磨制深板岩','#444957'],
    pink_terracotta:['粉红色陶瓦','#b47a82'],magenta_stained_glass:['品红色染色玻璃','#a24d91'],
    obsidian:['黑曜石','#282935'],purple_stained_glass:['紫色染色玻璃','#67527d'],crying_obsidian:['哭泣的黑曜石','#54416f'],
    polished_andesite:['磨制安山岩','#838d90'],light_blue_stained_glass:['淡蓝色染色玻璃','#8bb9d0'],iron_block:['铁块','#d0d5d2'],deepslate_tiles:['深板岩瓦','#40464a'],
    mossy_stone_bricks:['苔石砖','#526d53'],green_stained_glass:['绿色染色玻璃','#56856b'],moss_block:['苔藓块','#607d45'],
    packed_ice:['浮冰','#91b8cf'],calcite:['方解石','#dddcd8'],blue_ice:['蓝冰','#638ead'],orange_terracotta:['橙色陶瓦','#ad7055']
  };
  const common=['quartz_bricks','quartz_pillar','smooth_quartz','polished_blackstone_bricks','deepslate_bricks','sea_lantern','iron_bars','chest'];
  const key=(x,y,z)=>`${x},${y},${z}`;
  const rotate=(x,z,n)=>{while(n--){[x,z]=[-z,x];}return [x,z];};
  let current=paths[0], view='room', yaw=0, zoom=1, opened=false,clay=false;
  const cache=new Map();

  // 逐格生成公用房间，再叠加每条命途独有的高位梁架与侧壁构件。
  function build(path){
    if(cache.has(path.id)) return cache.get(path.id);
    const map=new Map();const put=(x,y,z,id)=>map.set(key(x,y,z),{x,y,z,id});
    const line=(a,b,id)=>{const n=Math.max(Math.abs(b[0]-a[0]),Math.abs(b[1]-a[1]));for(let i=0;i<=n;i++){const t=n?i/n:0;put(Math.round(a[0]+(b[0]-a[0])*t),Math.round(a[1]+(b[1]-a[1])*t),a[2],id);}};
    const box=(x0,x1,y0,y1,z0,z1,id)=>{for(let x=x0;x<=x1;x++)for(let y=y0;y<=y1;y++)for(let z=z0;z<=z1;z++)put(x,y,z,id);};
    const accent=path.palette[0][0], secondary=path.palette[2][0], glass=path.palette[1][0], trim=path.palette[3][0];
    // 地坪、外墙与顶封，房间外轮廓对应现有 1.2.0 守卫室。
    box(-12,12,7,7,-26,0,'quartz_bricks');
    for(const x of [-12,12])box(x,x,8,23,-26,0,'quartz_bricks');
    box(-12,12,8,23,-26,-26,'quartz_bricks');
    box(-12,12,8,23,0,0,'quartz_bricks');
    box(-12,12,8,23,-18,-18,'quartz_bricks');
    box(-12,12,24,24,-26,0,'smooth_quartz');
    // 东侧观星梯井墙体和梯子按既定坐标开口保留。
    for(let y=8;y<=23;y++){map.delete(key(7,y,-13));put(8,y,-13,'quartz_bricks');}
    for(let y=8;y<=10;y++)for(const z of [-13,-12])map.delete(key(6,y,z));
    // 地坪沿边压深色石，中央道路以主题色细线延伸至监守者位置。
    for(let x=-12;x<=12;x++)for(let z=-26;z<=0;z++)if(Math.abs(x)>=11||z===-26||z===0)put(x,7,z,'deepslate_bricks');
    const floorAccent=path.id==='remembrance'?'calcite':accent;
    for(let z=-17;z<=0;z++)put(0,7,z,floorAccent);
    for(const x of [-10,10])for(let z=-16;z<=-3;z+=3)put(x,7,z,path.id==='remembrance'?'calcite':trim);
    // 柱垛、窗格及海晶灯位置固定，仅玻璃和饰带随命途更换。
    for(const x of [-12,12])for(const z of [-23,-14,-5]){
      box(x,x,8,20,z,z,'quartz_pillar');for(let y=12;y<=17;y++)put(x,y,z+1,glass);
      for(let dz=-1;dz<=1;dz++)put(x,21,z+dz,accent);
      put(x===12?x-1:x+1,9,z,'sea_lantern');
    }
    for(const x of [-11,11])for(let z=-25;z<=-1;z++)if(z%4===0)put(x,8,z,'polished_blackstone_bricks');
    // 三格宽、两格净高的入口；隔墙内的五格门洞沿用固定坐标。
    for(let x=-1;x<=1;x++)for(let y=8;y<=9;y++)map.delete(key(x,y,0));
    for(let x=-2;x<=2;x++)for(let y=8;y<=12;y++)map.delete(key(x,y,-18));
    for(let x=-2;x<=2;x++)for(let y=8;y<=12;y++)put(x,y,-18,'iron_bars');
    for(const x of [-3,3])box(x,x,8,13,-18,-18,'quartz_pillar');
    box(-3,3,13,13,-18,-18,accent);put(-4,11,-18,'sea_lantern');put(4,11,-18,'sea_lantern');
    // 宝箱坐标 (0,10,-21) 与其上方箱盖空间固定。
    box(-3,3,8,8,-23,-20,'deepslate_bricks');box(-2,2,9,9,-23,-21,'quartz_bricks');
    box(-2,2,8,8,-19,-19,'quartz_bricks');put(0,10,-21,'chest');
    for(const x of [-7,7]){box(x,x,8,9,-23,-23,'quartz_pillar');put(x,10,-23,'sea_lantern');}
    // 两面墙上的主题像素徽纹稍后叠加：隔墙面向战斗区，后墙面向入口。
    // 窗玻璃作为浅色内嵌，不改动采光灯数量。
    for(const x of [-12,12])for(const z of [-22,-14,-6])for(let y=11;y<=18;y++)put(x,y,z,glass);
    // 固定海晶灯作辅助光源；纹样本身不承诺染色玻璃彩色投光。
    const room={blocks:[...map.values()],path};cache.set(path.id,room);return room;
  }

  // 绘制方块、标注与导览；所有预览均由同一套坐标数据派生。
  let pathNav,cards;
  if(!window.__RUIN_THEME_EXPORT__){
    pathNav=$('pathNav');cards=$('cards');
    for(const p of paths){
      const button=document.createElement('button');button.type='button';button.textContent=p.name;button.dataset.path=p.id;button.style.setProperty('--swatch',p.palette[0][2]);button.addEventListener('click',()=>select(p));pathNav.append(button);
      const card=document.createElement('button');card.type='button';card.dataset.path=p.id;card.style.setProperty('--swatch',p.palette[0][2]);card.innerHTML='<canvas aria-hidden="true"></canvas><div class="card-copy"><b>'+p.name+' · '+p.title+'</b><span>'+p.motif+'</span></div>';card.addEventListener('click',()=>{select(p);window.scrollTo({top:0,behavior:'smooth'});});cards.append(card);
    }
  }
  function select(p){current=p;document.documentElement.style.setProperty('--accent',p.palette[0][2]);for(const b of document.querySelectorAll('[data-path]'))b.classList.toggle('selected',b.dataset.path===p.id);$('pathLatin').textContent=p.en;$('pathName').textContent=p.name+' · '+p.title;$('pathPitch').textContent=p.pitch;$('pathMotif').textContent=p.motif;$('readEntrance').textContent=p.entrance;$('readWalls').textContent=p.walls;$('readCombat').textContent=p.combat;$('swatches').replaceChildren(...p.palette.map(([id,name,color])=>{const i=document.createElement('i');i.style.backgroundColor=color;i.title=name+' · minecraft:'+id;i.dataset.label=name;return i;}));render();materialTable();}

  function symbolCells(id){
    const out=[];const put=(x,y,m='main')=>out.push([x,y,m]);
    const line=(a,b,m='main')=>{let dx=Math.abs(b[0]-a[0]),sx=a[0]<b[0]?1:-1,dy=-Math.abs(b[1]-a[1]),sy=a[1]<b[1]?1:-1,err=dx+dy,x=a[0],y=a[1];for(;;){put(x,y,m);if(x===b[0]&&y===b[1])break;const e=2*err;if(e>=dy){err+=dy;x+=sx;}if(e<=dx){err+=dx;y+=sy;}}};
    const ring=(cx,cy,rx,ry,m='main',gap=false)=>{for(let y=-ry;y<=ry;y++)for(let x=-rx;x<=rx;x++){const d=x*x/(rx*rx)+y*y/(ry*ry);if(d>.99||d<.58)continue;if(gap&&x>0&&y<0)continue;put(cx+x,cy+y,m);}};
    switch(id){
      case'destruction':line([-4,-3],[0,0]);line([4,-3],[0,0]);line([0,0],[-4,4]);line([0,0],[4,4]);line([-1,-3],[1,-3],'detail');break;
      case'hunt':line([0,4],[0,-2]);line([0,-2],[-3,-5]);line([0,-2],[3,-5]);line([-2,3],[2,3],'detail');put(0,-5);break;
      case'erudition':ring(0,0,4,3,'main');line([-4,0],[4,0],'detail');line([0,-5],[0,5]);line([-3,3],[3,-3],'detail');put(0,0,'detail');break;
      case'harmony':ring(0,0,2,2,'detail');ring(0,0,4,4);line([-4,2],[0,4],'detail');line([0,4],[4,2],'detail');break;
      case'nihility':ring(-1,0,4,4,'main',true);ring(2,1,2,2,'detail',true);put(-4,4,'detail');break;
      case'preservation':line([-4,-4],[0,-5]);line([0,-5],[4,-4]);line([-4,-4],[-3,1]);line([4,-4],[3,1]);line([-3,1],[0,4]);line([3,1],[0,4]);line([-2,-1],[2,-1],'detail');break;
      case'abundance':line([0,4],[0,-2]);line([0,-1],[-3,-4]);line([-3,-4],[-4,-2]);line([0,-2],[3,-5]);line([3,-5],[4,-3]);line([0,1],[-3,3],'detail');line([0,1],[3,3],'detail');break;
      case'remembrance':for(let r=2;r<=4;r++)for(let x=-r;x<=r;x++){const y=Math.round(Math.sqrt(r*r-x*x));put(x,-2-y,r%2?'main':'detail');}line([-4,4],[4,4]);put(0,-5,'detail');break;
      case'elation':line([-4,-2],[-2,-4]);line([-2,-4],[0,-3]);line([0,-3],[2,-4]);line([2,-4],[4,-2]);line([-3,1],[-1,2]);line([-1,2],[1,2]);line([1,2],[3,1]);put(-2,-1,'detail');put(2,-1,'detail');break;
    }
    return out;
  }

  // 重写上方函数中的徽纹占位器：主题像素坐标落到两道既定墙面。
  function installSymbols(room,theme){
    const map=new Map(room.blocks.map(b=>[key(b.x,b.y,b.z),b]));
    const base=theme.palette[0][0],detail=theme.palette[2][0];
    for(const z of [-25,-17])for(const [x,y,tone] of symbolCells(theme.id)){
      const mat=tone==='detail'?detail:base;
      const wx=x*2,wy=18-Math.round(y*.7);
      for(const dx of [0,1])for(const dy of [0,1])map.set(key(wx+dx,wy+dy,z),{x:wx+dx,y:wy+dy,z,id:mat});
    }
    // 沿两侧墙安装窄窗格，并嵌入主题线条；绝不占据梯井净空。
    for(const x of [-12,12])for(const z of [-22,-14,-6])for(let y=11;y<=18;y++)map.set(key(x,y,z),{x,y,z,id:theme.palette[1][0]});
    return {...room,blocks:[...map.values()]};
  }

  // 每种命途增加不同的实体构件。低位中央、门洞、箱盖及梯井作为保留区域。
  function installArchitecture(room,path){
    if(path.id==='ordinary')return room;
    const map=new Map(room.blocks.map(b=>[key(b.x,b.y,b.z),b]));
    const a=path.palette[0][0],d=path.palette[2][0],stone='smooth_quartz';
    const protectedCell=(x,y,z)=>
      (y>=8&&y<=13&&x>=-10&&x<=10&&z>=-17&&z<=-3)||
      (x>=6&&x<=8&&z>=-13&&z<=-12&&y>=8)||
      (Math.abs(x)<=2&&z>=-23&&z<=-18&&y>=8&&y<=13)||
      (Math.abs(x)<=1&&z===0&&y>=8&&y<=9);
    const put=(x,y,z,id)=>{x=Math.round(x);y=Math.round(y);z=Math.round(z);if(Math.abs(x)>12||z<-26||z>0||y<7||y>23||protectedCell(x,y,z)||map.get(key(x,y,z))?.id==='sea_lantern')return;map.set(key(x,y,z),{x,y,z,id,architecture:true});};
    const box=(x0,x1,y0,y1,z0,z1,id)=>{for(let x=x0;x<=x1;x++)for(let y=y0;y<=y1;y++)for(let z=z0;z<=z1;z++)put(x,y,z,id);};
    const line=(p,q,id,w=0)=>{const n=Math.max(...p.map((v,i)=>Math.abs(v-q[i])));for(let t=0;t<=n;t++){const v=p.map((c,i)=>Math.round(c+(q[i]-c)*(n?t/n:0)));box(v[0]-w,v[0]+w,v[1],v[1],v[2],v[2],id);}};
    const ring=(cx,cy,cz,r,plane,id,gap=false)=>{for(let u=-r;u<=r;u++)for(let v=-r;v<=r;v++){const distance=Math.hypot(u,v);if(distance>r||distance<r-1.25||(gap&&u>0&&v<1))continue;put(cx+(plane==='yz'?0:u),cy+(plane==='xz'?0:v),cz+(plane==='xy'?0:plane==='yz'?u:v),id);}};
    const floorId=path.id==='remembrance'?'calcite':path.id==='abundance'?'mossy_stone_bricks':a;
    const floorLine=(p,q,id=floorId)=>line([p[0],7,p[1]],[q[0],7,q[1]],id);
    switch(path.id){
      case'destruction':
        for(const z of [-13,-5])for(const s of [-1,1]){
          line([s*11,10,z],[s*4,22,z],a,1);line([s*11,12,z+1],[s*4,23,z+1],stone);
        }
        box(-10,-6,20,21,-9,-8,'nether_bricks');box(5,10,18,19,-9,-8,a);
        for(const s of [-1,1]){floorLine([s*9,-4],[s*3,-9]);floorLine([s*3,-9],[s*7,-15]);}break;
      case'hunt':
        for(const z of [-15,-9,-3])for(let x=-11;x<=11;x++){
          const y=12+11-Math.abs(x);put(x,y,z,a);put(x,y+1,z,stone);
        }
        for(const z of [-4,-9,-14]){floorLine([-4,z+2],[0,z-2]);floorLine([4,z+2],[0,z-2]);}
        for(const s of [-1,1])line([s*11,18,-16],[s*11,18,-2],a);break;
      case'erudition':
        ring(0,21,-10,7,'xz',a);ring(0,18,-15,4,'xy',stone);
        for(const s of [-1,1]){line([s*11,21,-10],[s*7,21,-10],stone);box(s*11,s*11,10,12,-8,-7,a);}
        box(-1,1,20,22,-11,-9,d);ring(-2,7,-10,5,'xz',a);
        for(const s of [-1,1]){floorLine([-2+s*6,-10],[-2+s*8,-10],'polished_deepslate');floorLine([-2,-10+s*6],[-2,-10+s*7],'polished_deepslate');}break;
      case'harmony':
        for(const z of [-15,-6])for(let x=-11;x<=11;x++){const y=16+Math.round(6*Math.sqrt(Math.max(0,1-(x/11)**2)));put(x,y,z,stone);put(x,y+1,z,a);}
        for(const x of [-11,11])for(let z=-16;z<=-3;z+=2)box(x,x,10,14+Math.abs((z+16)%8-4),z,z,a);
        for(let z=-16;z<=-3;z++)for(const s of [-1,1])put(s*(5+Math.round(Math.sin(z*.7)*2)),7,z,a);break;
      case'nihility':
        ring(-2,19,-8,4,'xy',a,true);ring(6,18,-15,3,'yz',d,true);
        for(const [x,y,z] of [[-9,16,-5],[-7,20,-14],[8,18,-5],[10,21,-11]])box(x,x+1,y,y+1,z,z+1,a);
        for(const x of [-12,12])box(x,x,12,20,-16,-3,'purple_stained_glass');
        ring(2,7,-10,5,'xz','polished_blackstone_bricks',true);break;
      case'preservation':
        for(const x of [-11,11])for(const z of [-16,-8,-2])box(x,x,8,19,z-1,z+1,a);
        for(const z of [-16,-8]){box(-11,11,21,22,z,z+1,a);for(let x=-10;x<=10;x+=4)box(x,x+1,23,23,z,z+1,'iron_block');}
        for(const s of [-1,1]){line([s*7,22,-17],[s*5,16,-17],'iron_block');line([s*5,16,-17],[0,14,-17],a);}
        for(const x of [-9,9])floorLine([x,-16],[x,-4]);floorLine([-9,-16],[9,-16]);floorLine([-9,-4],[9,-4]);break;
      case'abundance':
        for(const s of [-1,1])for(const z of [-14,-5]){
          box(s*11,s*11,8,15,z,z,a);line([s*11,14,z],[s*5,21,z],stone);line([s*7,18,z],[s*1,23,z-2],a);
          box(s<0?-10:3,s<0?-3:10,22,23,z-2,z,d);
        }
        for(const s of [-1,1]){floorLine([0,-14],[s*8,-7]);floorLine([s*4,-10],[s*9,-13]);}break;
      case'remembrance':
        for(const [r,z,y] of [[6,-16,20],[8,-11,21],[10,-6,22],[11,-1,23]]){
          for(const x of [-r,r])box(x,x,14,y,z,z,a);
          box(-r,r,y,y,z,z,'calcite');box(-r,r,14,14,z,z,'blue_ice');
          floorLine([-r,z],[-r,z+2]);floorLine([r,z],[r,z+2]);floorLine([-r,z],[r,z]);
        }break;
      case'elation':
        for(const [x,y] of [[-11,13],[11,17]]){
          box(x,x,y,y,-16,-3,a);box(x,x,y+1,y+1,-16,-3,'pink_terracotta');
          for(let z=-15;z<=-4;z+=4)box(x,x,8,y-1,z,z,'quartz_pillar');
        }
        for(const s of [-1,1]){box(s*11,s*11,8,21,-17,-16,a);line([s*11,21,-17],[s*5,23,-17],'pink_terracotta');}
        box(-5,5,23,23,-17,-16,a);for(const x of [-9,-7,7,9])box(x,x,16,21,-17,-17,'magenta_stained_glass');
        for(let x=-8;x<=8;x++){const z=-9+Math.round(Math.sqrt(Math.max(0,64-x*x))*.7);put(x,7,z,a);}break;
    }
    // 低入口外壳、25 格铁栅门、原宝箱及原梯井都从公用房间保留。
    return {...room,blocks:[...map.values()]};
  }

  function entities(){return [{x:-2,y:8,z:-10,w:1.15,h:2.9,d:.8,id:'warden'}];}
  const models=new Map();
  function complete(path){if(!models.has(path.id)){let r=build(path);r=installSymbols(r,path);r=installArchitecture(r,path);models.set(path.id,r);}return models.get(path.id);}
  function coords(x,y,z,n){while(n--)[x,z]=[-z,x];return[x,y,z];}
  function shade(hex,f){const n=parseInt(hex.slice(1),16);return`rgb(${Math.round((n>>16)*f)},${Math.round((n>>8&255)*f)},${Math.round((n&255)*f)})`;}
  function color(id){return clay?'#b7b9b8':blocks[id]?.[1]||'#ff00ff';}

  function drawIso(canvas,path,small=false){
    const rect=canvas.getBoundingClientRect();if(!rect.width)return;const dpr=Math.min(window.devicePixelRatio||1,1.5),w=rect.width,h=rect.height;canvas.width=w*dpr;canvas.height=h*dpr;const c=canvas.getContext('2d');c.setTransform(dpr,0,0,dpr,0,0);c.clearRect(0,0,w,h);
    const room=complete(path),all=room.blocks;let shown=all.filter(b=>b.y<=23);
    // 为便于阅读，隐藏靠近观察者的入口墙、屋顶和一侧墙体，保留后墙徽纹与隔墙铁门。
    shown=shown.filter(b=>b.z!==0&&(b.architecture||b.y<=10||
      (Math.abs(b.x)<12&&b.z!==-26&&!(b.z===-18&&b.y>13))));
    const points=[];for(const b of shown){const [x,,z]=coords(b.x,0,b.z,yaw);points.push([x-z,(x+z)/2-b.y]);}
    const xs=points.map(p=>p[0]),ys=points.map(p=>p[1]);const minX=Math.min(...xs)-2,maxX=Math.max(...xs)+2,minY=Math.min(...ys)-2,maxY=Math.max(...ys)+2;
    const scale=Math.min((w-(small?22:62))/(maxX-minX),(h-(small?24:65))/(maxY-minY))*zoom*(small?.93:1);
    const proj=(x,y,z)=>{[x,z]=coords(x,0,z,yaw);return[w/2+(x-z-(minX+maxX)/2)*scale,h/2+((x+z)/2-y-(minY+maxY)/2)*scale+9];};
    const faces=[];
    function cube(x,y,z,ww,hh,dd,id,entity=false){const v=[[x,y,z],[x+ww,y,z],[x+ww,y,z+dd],[x,y,z+dd],[x,y+hh,z],[x+ww,y+hh,z],[x+ww,y+hh,z+dd],[x,y+hh,z+dd]];const fs=[[[1,2,6,5],[1,0,0],.7],[[0,4,7,3],[-1,0,0],.72],[[3,7,6,2],[0,0,1],.87],[[0,1,5,4],[0,0,-1],.85],[[4,5,6,7],[0,1,0],1]];
      for(const [idx,n,f] of fs){const [nx,,nz]=coords(n[0],0,n[2],yaw);if(!entity){if(n[1]===0&&nx+nz<=0)continue;if(n[1]===0&&nz>0&&z===0)continue;if(n[1]===0&&nx>0&&x===12)continue;}const ps=idx.map(i=>v[i]),center=ps.reduce((a,p)=>a.map((v,i)=>v+p[i]/4),[0,0,0]);const [cx,cy,cz]=coords(center[0],center[1],center[2],yaw);faces.push({p:ps.map(q=>proj(...q)),d:cx+cz+cy,col:entity?(id==='warden'?'#183b40':'#55c0b3'):shade(color(id),f)});
      }
    }
    for(const b of shown){if(b.id==='iron_bars'){if(opened)continue;cube(b.x+.44,b.y,b.z+.44,.12,1,.12,b.id);cube(b.x,b.y+.48,b.z+.44,1,.1,.12,b.id);}else if(b.id==='chest'){cube(b.x+.06,b.y,b.z+.06,.88,.88,.88,b.id);cube(b.x+.43,b.y+.26,b.z+.94,.14,.22,.025,'sea_lantern');}else cube(b.x,b.y,b.z,1,1,1,b.id);}
    if(!small){for(const e of entities())cube(e.x-e.w/2,e.y,e.z-e.d/2,e.w,e.h,e.d,'warden',true);cube(-.3,8,-3.3,.6,1.8,.6,'player',true);}
    faces.sort((a,b)=>a.d-b.d);for(const f of faces){c.beginPath();f.p.forEach((p,i)=>i?c.lineTo(...p):c.moveTo(...p));c.closePath();c.fillStyle=f.col;c.fill();if(scale>3.2){c.strokeStyle='#050b1050';c.lineWidth=.4;c.stroke();}}
    if(!small){c.fillStyle='#dce7e3';c.font='11px Microsoft YaHei';c.fillText(path.name+' · '+(opened?'门开启预览':'守卫在场'),14,20);}
  }

  function drawFront(canvas,path){
    const r=complete(path),rect=canvas.getBoundingClientRect();if(!rect.width)return;
    const dpr=Math.min(window.devicePixelRatio||1,1.5),w=rect.width,h=rect.height;
    canvas.width=w*dpr;canvas.height=h*dpr;const c=canvas.getContext('2d');
    c.setTransform(dpr,0,0,dpr,0,0);c.clearRect(0,0,w,h);
    // 从入口向后室观察：隐藏外墙与顶封，透视保留真实纵深的梁架。
    const layer=r.blocks.filter(b=>b.z<0&&b.y<24&&(b.architecture||
      (Math.abs(b.x)<12&&b.z!==-26&&!(b.z===-18&&b.y>13))));
    const sc=Math.min((w-70)/43,(h-65)/27)*zoom;
    const factor=z=>45/(26-z),px=(x,z)=>w/2+x*sc*factor(z),py=(y,z)=>h*.70-(y-11)*sc*factor(z);
    const items=layer.map(b=>({...b,entity:false}));items.push({x:-2.6,y:8,z:-10,id:'warden',entity:true});
    items.sort((a,b)=>a.z-b.z||a.y-b.y);
    for(const b of items){
      const k=sc*factor(b.z),x=px(b.x,b.z),y=py(b.y+1,b.z);
      if(b.entity){c.fillStyle='#16484a';c.fillRect(x,py(10.9,b.z),k*1.2,k*2.9);c.fillStyle='#71cdb8';c.fillRect(x,py(11.6,b.z),k*.2,k*.7);continue;}
      if(b.id==='iron_bars'){if(opened)continue;c.fillStyle=color(b.id);c.fillRect(x+k*.44,y,k*.12,k);c.fillRect(x,y+k*.48,k,k*.1);continue;}
      c.fillStyle=color(b.id);c.fillRect(x,y,k,k);
      c.strokeStyle='#08111955';c.lineWidth=.45;c.strokeRect(x,y,k,k);
      if(b.id==='chest'){c.fillStyle='#dfbd78';c.fillRect(x+k*.44,y+k*.35,k*.12,k*.25);}
    }
    c.fillStyle='#c4d4d1';c.font='10px Microsoft YaHei';c.fillText('入口透视 · 隐去外墙与屋顶，展示前后构造层次',14,19);
  }

  function drawFloor(canvas,path){
    const {blocks:b}=complete(path),rect=canvas.getBoundingClientRect();if(!rect.width)return;const dpr=Math.min(devicePixelRatio||1,1.5),w=rect.width,h=rect.height;canvas.width=w*dpr;canvas.height=h*dpr;const c=canvas.getContext('2d');c.setTransform(dpr,0,0,dpr,0,0);c.clearRect(0,0,w,h);
    const cell=Math.min((w-96)/25,(h-70)/28),ox=(w-25*cell)/2,oy=(h-27*cell)/2+4,pal=blocks;
    c.fillStyle='#0b1319';c.fillRect(ox,oy,25*cell,27*cell);
    for(const q of b.filter(v=>v.y===7)){const x=ox+(q.x+12)*cell,z=oy+(q.z+26)*cell;c.fillStyle=color(q.id);c.fillRect(x,z,cell+.2,cell+.2);}
    // 战斗范围和实体、门、箱子单独叠加，清楚标出实际行进方向。
    c.strokeStyle='#8ddaccaa';c.setLineDash([5,4]);c.lineWidth=2;c.strokeRect(ox+2*cell,oy+9*cell,21*cell,15*cell);c.setLineDash([]);
    const dot=(x,z,color,label)=>{c.fillStyle=color;c.beginPath();c.arc(ox+(x+12.5)*cell,oy+(z+26.5)*cell,Math.max(4,cell*.33),0,Math.PI*2);c.fill();c.fillStyle='#edf2e8';c.font='9px Microsoft YaHei';c.fillText(label,ox+(x+12.5)*cell+5,oy+(z+26.5)*cell+3);};
    dot(-2,-10,'#50c7b5','守');dot(0,-21,'#dfb56d','箱');c.fillStyle=opened?'#80d6af':'#b9c7c5';for(let x=-2;x<=2;x++)c.fillRect(ox+(x+12)*cell,oy+8*cell,cell*.85,cell*.3);
    c.strokeStyle='#dbc99b';c.lineWidth=2;c.beginPath();c.moveTo(w/2,oy+27*cell);c.lineTo(w/2,oy+cell*22);c.stroke();c.fillStyle='#dbc99b';c.font='10px Microsoft YaHei';c.fillText('入口 → 监守者 → 门 → 宝箱',12,h-10);
  }

  function layerName(y){
    if(y===7)return '基础地坪与外围边线';
    if(y<=13)return '低墙、门栅与固定交互位';
    if(y<=19)return '侧墙纹样与主题梁架';
    if(y<=23)return '高位拱架、环体与冠盖';
    return '完整屋顶封顶';
  }
  function drawLayer(){
    const canvas=$('layerCanvas'),rect=canvas.getBoundingClientRect();if(!rect.width)return;
    const dpr=Math.min(window.devicePixelRatio||1,1.5),w=rect.width,h=rect.height;
    canvas.width=w*dpr;canvas.height=h*dpr;const c=canvas.getContext('2d');
    c.setTransform(dpr,0,0,dpr,0,0);c.clearRect(0,0,w,h);
    const y=+$('layerRange').value,model=complete(current).blocks.filter(b=>b.y===y);
    const byCoord=new Map(model.map(b=>[key(b.x,b.y,b.z),b]));
    const cell=Math.max(5,Math.min((w-60)/25,(h-48)/27)),gw=25*cell,gh=27*cell;
    const ox=(w-gw)/2+9,oy=(h-gh)/2+4;
    c.fillStyle='#0a1117';c.fillRect(ox,oy,gw,gh);
    for(let rz=0;rz<27;rz++)for(let cx=0;cx<25;cx++){
      const x=cx-12,z=rz-26,b=byCoord.get(key(x,y,z)),px=ox+cx*cell,py=oy+rz*cell;
      if(b){c.fillStyle=color(b.id);c.fillRect(px+.5,py+.5,cell-1,cell-1);}
      c.strokeStyle=b?'#0b131888':'#31424a';c.lineWidth=.55;c.strokeRect(px+.25,py+.25,cell-.5,cell-.5);
    }
    c.fillStyle='#9aadb1';c.font=`${Math.max(8,Math.min(10,cell*.68))}px Consolas,monospace`;c.textAlign='center';
    for(let x=-12;x<=12;x+=4)c.fillText(String(x),ox+(x+12.5)*cell,oy-7);
    c.textAlign='right';for(let z=-26;z<=0;z+=3)c.fillText(String(z),ox-5,oy+(z+26.65)*cell);
    c.textAlign='left';
    $('layerValue').textContent='Y='+y;
    $('layerSummary').textContent=`${layerName(y)} · ${model.length} 格 · ${new Set(model.map(b=>b.id)).size} 种材料`;
    const counts=new Map();for(const b of model)counts.set(b.id,(counts.get(b.id)||0)+1);
    const rows=$('layerRows');rows.replaceChildren();$('layerEmpty').hidden=model.length!==0;
    for(const [id,n] of [...counts].sort((a,b)=>b[1]-a[1])){
      const tr=document.createElement('tr'),definition=blocks[id],name=definition?.[0]||id;
      tr.innerHTML=`<td><i class="mat-swatch" style="background:${color(id)}"></i>${name}<code>minecraft:${id}</code></td><td>${n}</td>`;rows.append(tr);
    }
    canvas.onmousemove=e=>{
      const box=canvas.getBoundingClientRect(),cx=Math.floor((e.clientX-box.left-ox)/cell),rz=Math.floor((e.clientY-box.top-oy)/cell);
      if(cx<0||cx>=25||rz<0||rz>=27){$('layerCoord').textContent='将鼠标移到格子上查看 X、Y、Z 与方块名称。';return;}
      const x=cx-12,z=rz-26,b=byCoord.get(key(x,y,z));
      $('layerCoord').textContent=b?`坐标 (${x}, ${y}, ${z}) · ${blocks[b.id]?.[0]||b.id} · minecraft:${b.id}`:`坐标 (${x}, ${y}, ${z}) · 留空`;
    };
    canvas.onmouseleave=()=>{$('layerCoord').textContent='将鼠标移到格子上查看 X、Y、Z 与方块名称。';};
  }

  function render(){
    $('roomCanvas').getContext('2d').clearRect(0,0,1,1);$('canvasTag').textContent=view==='room'?'构造剖切 · 隐去外壳，展示立体主题梁架':view==='entrance'?'入口透视 · 观察廊架与悬环的纵深':'地坪俯视 · 中央行走区保持开阔';
    if(view==='room')drawIso($('roomCanvas'),current);else if(view==='entrance')drawFront($('roomCanvas'),current);else drawFloor($('roomCanvas'),current);
    if($('compare').checked){if(view==='room')drawIso($('baseCanvas'),basePath,false);else if(view==='entrance')drawFront($('baseCanvas'),basePath);else drawFloor($('baseCanvas'),basePath);}
    $('gate').textContent='铁栅门：'+(opened?'开启':'关闭');$('gate').setAttribute('aria-pressed',opened);$('gateState').textContent=opened?'门开启预览 · 游戏奖励状态不变':'守卫在场 · 宝箱未解锁';$('stateNote').textContent=opened?'仅供观察开门后的空间关系；不会改变游戏内守卫、门或宝箱。':'按钮只切换建筑预览，不触发游戏内试炼。';document.querySelector('.status-strip').classList.toggle('open',opened);
    drawFloor($('planCanvas'),current);
    drawLayer();
    for(const card of cards.querySelectorAll('button'))drawIso(card.querySelector('canvas'),paths.find(p=>p.id===card.dataset.path),true);
  }
  function materialTable(){
    const model=complete(current),counts=new Map();for(const b of model.blocks)counts.set(b.id,(counts.get(b.id)||0)+1);
    $('materialSummary').textContent=current.name+' · '+counts.size+' 种方块 · '+model.blocks.length.toLocaleString('zh-CN')+' 格。计数包含门关闭状态与完整墙顶。';
    const tbody=$('materialRows');tbody.replaceChildren();for(const [id,n] of [...counts].sort((a,b)=>b[1]-a[1])){const tr=document.createElement('tr'),definition=blocks[id];if(!definition)throw new Error('缺少方块说明：minecraft:'+id);const place=id==='iron_bars'?'后隔墙门洞':id==='sea_lantern'?'固定照明点':id==='chest'?'原宝箱位':id==='deepslate_bricks'?'底边及地坪':id.includes('glass')?'侧窗与主题窗带':id==='smooth_quartz'?'顶封':id==='quartz_pillar'?'两侧立柱与门框':'地坪、墙体或命途纹样';tr.innerHTML=`<td><i class="mat-swatch" style="background:${definition[1]}"></i>${definition[0]}</td><td><code>minecraft:${id}</code></td><td>${n.toLocaleString('zh-CN')}</td><td>${place}</td><td>${common.includes(id)?'共用结构':'命途主题'}</td>`;tbody.append(tr);}
  }
  function exportCsv(){const r=complete(current),lines=['x,y,z,block_id'];for(const b of r.blocks)lines.push(`${b.x},${b.y},${b.z},minecraft:${b.id}`);const blob=new Blob(['\ufeff'+lines.join('\r\n')],{type:'text/csv;charset=utf-8'}),a=document.createElement('a');a.href=URL.createObjectURL(blob);a.download=`1.3.0_${current.name}_守卫室草案.csv`;a.click();setTimeout(()=>URL.revokeObjectURL(a.href),1000);}
  function exportLayerCsv(){const y=+$('layerRange').value,blocksAtY=complete(current).blocks.filter(b=>b.y===y),lines=['x,y,z,block_id',...blocksAtY.map(b=>`${b.x},${b.y},${b.z},minecraft:${b.id}`)],blob=new Blob(['\ufeff'+lines.join('\r\n')],{type:'text/csv;charset=utf-8'}),a=document.createElement('a');a.href=URL.createObjectURL(blob);a.download=`1.3.0_${current.name}_Y${y}_分层蓝图.csv`;a.click();setTimeout(()=>URL.revokeObjectURL(a.href),1000);}
  // 导出器复用这一份方块模型，避免 HTML 蓝图与游戏资源各自维护坐标。
  window.RuinThemeStudy={ids:paths.map(p=>p.id),materials:blocks,getModel:id=>complete(id==='ordinary'?basePath:paths.find(p=>p.id===id)).blocks.map(b=>({...b}))};
  if(window.__RUIN_THEME_EXPORT__)return;
  document.querySelectorAll('[data-view]').forEach(b=>b.addEventListener('click',()=>{view=b.dataset.view;document.querySelectorAll('[data-view]').forEach(q=>q.classList.toggle('active',q===b));render();}));
  $('clay').addEventListener('change',e=>{clay=e.target.checked;render();});
  $('layerRange').addEventListener('input',render);
  $('layerPrev').addEventListener('click',()=>{$('layerRange').value=Math.max(7,+$('layerRange').value-1);render();});
  $('layerNext').addEventListener('click',()=>{$('layerRange').value=Math.min(24,+$('layerRange').value+1);render();});
  $('layerCsv').addEventListener('click',exportLayerCsv);
  $('turn').addEventListener('click',()=>{yaw=(yaw+1)%4;render();});$('zoom').addEventListener('input',e=>{zoom=+e.target.value;render();});$('compare').addEventListener('change',e=>{$('baselineBox').hidden=!e.target.checked;$('viewport').classList.toggle('comparing',e.target.checked);render();});$('gate').addEventListener('click',()=>{opened=!opened;render();});$('csv').addEventListener('click',exportCsv);window.addEventListener('resize',render);
  select(paths[0]);
})();
