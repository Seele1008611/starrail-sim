const sets = [
  {id:'gunslinger',name:'野穗伴行的快枪手',ring:'外圈 · 四件套',image:'../../../崩铁/遗器/外圈/野穗伴行的快枪手/快枪手的野穗毡帽.png',two:'攻击力提高12%。',four:'暴击伤害提高12%，造成的伤害提高10%。'},
  {id:'passerby',name:'云无留迹的过客',ring:'外圈 · 四件套',image:'../../../崩铁/遗器/外圈/云无留迹的过客/过客的逢春木簪.png',two:'治疗效果提高15%。',four:'击杀敌对目标时，为自己及周围10格内的其他玩家恢复最大生命值5%。'},
  {id:'paladin',name:'净庭教宗的圣骑士',ring:'外圈 · 四件套',image:'../../../崩铁/遗器/外圈/净庭教宗的圣骑士/圣骑的宽恕盔面.png',two:'护甲值提高15%。',four:'提供的护盾量提高20%，在本mod中体现为伤害吸收生命值。'},
  {id:'station',name:'太空封印站',ring:'内圈 · 两件套',image:'../../../崩铁/遗器/内圈/太空封印站/「黑塔」的空间站点.png',two:'攻击力提高24%。',four:''},
  {id:'xianzhou',name:'不老者的仙舟',ring:'内圈 · 两件套',image:'../../../崩铁/遗器/内圈/不老者的仙舟/罗浮仙舟的天外楼船.png',two:'最大生命值提高20%。',four:''}
];
const levelRules = {
  1:{tier:'入门',roman:'Ⅰ',copy:'基础属性；逐波加入远程单位。',elite:'无精英强化',stars:[[2,50],[3,50]],waves:[['普通','普通','普通','普通'],['普通','普通','普通','远程'],['普通','普通','远程','远程']]},
  2:{tier:'入门',roman:'Ⅱ',copy:'基础属性；逐波加入远程单位。',elite:'无精英强化',stars:[[3,80],[4,20]],waves:[['普通','普通','普通','普通'],['普通','普通','普通','远程'],['普通','普通','远程','远程']]},
  3:{tier:'进阶',roman:'Ⅲ',copy:'后两波逐步加入强化精英。',elite:'精英生命 ×1.5 · 攻击 ×1.2',stars:[[3,40],[4,60]],waves:[['普通','普通','普通','普通'],['普通','普通','远程','精英'],['普通','远程','精英','精英']]},
  4:{tier:'进阶',roman:'Ⅳ',copy:'后两波逐步加入强化精英。',elite:'精英生命 ×1.5 · 攻击 ×1.2',stars:[[4,80],[5,20]],waves:[['普通','普通','普通','普通'],['普通','普通','远程','精英'],['普通','远程','精英','精英']]},
  5:{tier:'高阶',roman:'Ⅴ',copy:'逐波增加精英比例；五星遗器必得。',elite:'精英生命 ×2.0 · 攻击 ×1.3',stars:[[5,100]],waves:[['普通','普通','远程','精英'],['普通','远程','精英','精英'],['普通','精英','精英','精英']]},
  6:{tier:'高阶',roman:'Ⅵ',copy:'逐波增加精英比例；五星遗器必得。',elite:'精英生命 ×2.0 · 攻击 ×1.3',stars:[[5,100]],waves:[['普通','普通','远程','精英'],['普通','远程','精英','精英'],['普通','精英','精英','精英']]},
  7:{tier:'极境',roman:'Ⅶ',copy:'三波均有精英；末波四只精英。',elite:'精英生命 ×2.5 · 攻击 ×1.4',stars:[[5,100]],waves:[['普通','远程','精英','精英'],['普通','精英','精英','精英'],['精英','精英','精英','精英']]}
};
const state={level:1,setId:'gunslinger',started:false,finished:false,transitioning:false,wave:0,defeated:[],lockedStar:null};
const $=id=>document.getElementById(id);
const starLabel=n=>'★'.repeat(n);
const ARENA_SIZE=25;
const BUILD_PROGRESS_KEY='starrail_sim_arena_build_progress_v1';
const materialDefs=[
  {id:'polished_andesite',name:'磨制安山岩',color:'#89918e'},
  {id:'stone_bricks',name:'石砖',color:'#58686d'},
  {id:'deepslate_bricks',name:'深板岩砖',color:'#303c48'},
  {id:'dark_prismarine',name:'暗海晶石',color:'#286a6b'},
  {id:'sea_lantern',name:'海晶灯',color:'#c6e5c9'},
  {id:'end_rod',name:'末地烛',color:'#f0dc9d'},
  {id:'dark_oak_fence_gate',name:'深色橡木栅栏门',color:'#68462f'},
  {id:'lodestone',name:'磁石（中央装置底座）',color:'#b77d4b'}
];
const buildPlan=createBuildPlan();
let placedBlocks=new Set();
try {
  const saved=JSON.parse(localStorage.getItem(BUILD_PROGRESS_KEY)||'[]');
  if(Array.isArray(saved))placedBlocks=new Set(saved.filter(key=>buildPlan.has(key)));
} catch {}
function selectedSet(){return sets.find(s=>s.id===state.setId)}

function blockKey(x,y,z){return `${x},${y},${z}`}
function createBuildPlan(){
  const blocks=new Map();
  const put=(x,y,z,material,part)=>blocks.set(blockKey(x,y,z),{x,y,z,material,part});
  const center=12;
  // Ground floor: 21×21 interior. Concentric bands are rasterized into actual square blocks.
  for(let z=2;z<=22;z++)for(let x=2;x<=22;x++){
    const distance=Math.hypot(x-center,z-center);
    const ring=(distance>=7.8&&distance<=9.15)||(distance>=4.05&&distance<=5.15);
    put(x,0,z,ring?'dark_prismarine':'polished_andesite','室内地坪');
  }
  // Foundation ring and the two-block-deep entrance approach; every wall block has support.
  for(let z=0;z<ARENA_SIZE;z++)for(let x=0;x<ARENA_SIZE;x++){
    if(!(x<2||x>22||z<2||z>22))continue;
    const gateApproach=z<2&&x>=10&&x<=14;
    if(gateApproach)put(x,0,z,'polished_andesite','入口地面');
    else put(x,0,z,(x<=2||x>=22)&&(z<=2||z>=22)?'deepslate_bricks':'stone_bricks','围墙基础');
  }
  // Four lit cardinal pads mark the combat spawn points.
  [[12,4],[20,12],[12,20],[4,12]].forEach(([x,z])=>put(x,0,z,'sea_lantern','召唤点灯标'));
  // Raised 3×3 plinth and its vanilla lodestone core.
  for(let z=11;z<=13;z++)for(let x=11;x<=13;x++)put(x,1,z,'polished_andesite','中央台座');
  [[12,11],[13,12],[12,13],[11,12]].forEach(([x,z])=>put(x,1,z,'sea_lantern','中央台座灯'));
  put(12,2,12,'lodestone','中央装置');

  const isPerimeter=(x,z)=>x<2||x>22||z<2||z>22;
  const isTower=(x,z)=>(x<=2||x>=22)&&(z<=2||z>=22);
  const isNorthGate=(x,z,y)=>z<2&&x>=10&&x<=14&&y<=4;
  // Two-block-thick walls, with a five-wide and four-high opening in the north wall.
  for(let y=1;y<=7;y++)for(let z=0;z<ARENA_SIZE;z++)for(let x=0;x<ARENA_SIZE;x++){
    if(!isPerimeter(x,z)||isNorthGate(x,z,y))continue;
    put(x,y,z,isTower(x,z)?'deepslate_bricks':'stone_bricks',isTower(x,z)?'四角塔楼':'围墙');
  }
  // Corner towers rise two blocks above the wall.
  for(let y=8;y<=9;y++)for(let z=0;z<ARENA_SIZE;z++)for(let x=0;x<ARENA_SIZE;x++){
    if(isTower(x,z))put(x,y,z,'deepslate_bricks','四角塔楼');
  }
  // Five individually operable gates block the lower entrance while leaving a walk-through when opened.
  for(let x=10;x<=14;x++)put(x,1,0,'dark_oak_fence_gate','北侧入口栅栏门');
  // Wall sconces: an end rod below each sea lantern. These replace wall blocks.
  [[7,1],[17,1],[1,12],[23,12]].forEach(([x,z])=>{
    put(x,3,z,'end_rod','墙灯');
    put(x,4,z,'sea_lantern','墙灯');
  });
  return blocks;
}

function buildLayerName(y){
  if(y===0)return '基础：地坪、墙基与入口地面';
  if(y===1)return '围墙起砌、入口栅栏门与中央台座';
  if(y===2)return '墙体第2层与中央装置';
  if(y===3)return '墙体第3层与末地烛';
  if(y===4)return '墙体第4层与海晶灯';
  if(y<=7)return `围墙第${y}层`;
  return `角塔第${y}层`;
}
function persistBuildProgress(){
  try{localStorage.setItem(BUILD_PROGRESS_KEY,JSON.stringify([...placedBlocks]));}catch{}
}
function countBuildMaterials(){
  const counts=Object.fromEntries(materialDefs.map(m=>[m.id,{total:0,placed:0}]));
  for(const [key,block] of buildPlan){counts[block.material].total++;if(placedBlocks.has(key))counts[block.material].placed++;}
  return counts;
}
function renderBuildGrid(){
  const y=Number($('buildLayer').value||0);
  $('layerName').textContent=buildLayerName(y);
  const layerBlocks=new Map([...buildPlan].filter(([,block])=>block.y===y));
  const grid=$('buildingGrid');
  let cells='';
  for(let z=0;z<ARENA_SIZE;z++)for(let x=0;x<ARENA_SIZE;x++){
    const key=blockKey(x,y,z),block=layerBlocks.get(key);
    if(!block){cells+='<span class="build-cell empty" aria-hidden="true"></span>';continue;}
    const done=placedBlocks.has(key),material=materialDefs.find(m=>m.id===block.material);
    cells+=`<button class="build-cell has-block ${done?'placed':''}" type="button" data-block-key="${key}" data-material="${block.material}" aria-pressed="${done}" aria-label="X ${x}, Y ${y}, Z ${z}：${material.name}，${done?'已放置':'未放置'}" title="X${x} · Y${y} · Z${z}｜${material.name}｜${block.part}｜${done?'已放置':'点击标记已放置'}"></button>`;
  }
  grid.innerHTML=cells;
  renderBuildCounts(layerBlocks.size);
}
function renderBuildCounts(layerTotal){
  const y=Number($('buildLayer').value||0);
  const layerPlaced=[...buildPlan].reduce((n,[key,block])=>n+(block.y===y&&placedBlocks.has(key)?1:0),0);
  const totalPlaced=[...placedBlocks].filter(key=>buildPlan.has(key)).length;
  const total=buildPlan.size;
  $('layerProgress').textContent=`本层：${layerPlaced} / ${layerTotal} 格`;
  $('totalProgress').textContent=`总进度：${totalPlaced} / ${total} 方块`;
  $('buildPercent').textContent=`${total?Math.floor(totalPlaced/total*100):0}%`;
  const counts=countBuildMaterials();
  $('materialCounts').innerHTML=materialDefs.map(m=>{
    const c=counts[m.id],remaining=c.total-c.placed;
    if(!c.total)return '';
    return `<div class="material-row"><i class="material-swatch" style="background:${m.color}"></i><div><strong>${m.name}</strong><small>剩余 ${remaining}</small></div><span class="material-amount">${c.placed} / ${c.total}<em>已放 / 总数</em></span></div>`;
  }).join('');
  $('materialBlockTotal').textContent=String(total);
}
function initBuildPlanner(){
  const select=$('buildLayer');
  select.innerHTML=Array.from({length:10},(_,y)=>`<option value="${y}">Y=${y} · ${buildLayerName(y)}</option>`).join('');
  select.addEventListener('change',renderBuildGrid);
  $('layerPrev').addEventListener('click',()=>{select.value=String(Math.max(0,Number(select.value)-1));renderBuildGrid();});
  $('layerNext').addEventListener('click',()=>{select.value=String(Math.min(9,Number(select.value)+1));renderBuildGrid();});
  $('buildingGrid').addEventListener('click',event=>{
    const cell=event.target.closest('[data-block-key]');
    if(!cell)return;
    const key=cell.dataset.blockKey;
    if(placedBlocks.has(key))placedBlocks.delete(key);else placedBlocks.add(key);
    persistBuildProgress();renderBuildGrid();
  });
  $('markLayer').addEventListener('click',()=>{
    const y=Number(select.value);
    for(const [key,block] of buildPlan)if(block.y===y)placedBlocks.add(key);
    persistBuildProgress();renderBuildGrid();
  });
  $('resetBuild').addEventListener('click',()=>{
    placedBlocks.clear();
    try{localStorage.removeItem(BUILD_PROGRESS_KEY);}catch{}
    renderBuildGrid();
  });
  renderBuildGrid();
}
function makeSetGrid(){
  $('setGrid').innerHTML=sets.map(s=>`<button class="set-option ${s.id===state.setId?'selected':''}" data-set="${s.id}" aria-pressed="${s.id===state.setId}"><img src="${s.image}" alt=""><span>${s.name}<em>${s.ring}</em></span></button>`).join('');
  $('setGrid').querySelectorAll('.set-option').forEach(btn=>btn.addEventListener('click',()=>{if(state.started)return;state.setId=btn.dataset.set;refresh();}));
}
function renderSetDetails(){
  const s=selectedSet();
  $('selectedSet').innerHTML=`<strong>${s.name}</strong><span>${s.ring} · 二件套：${s.two}${s.four?` <b class="four-piece">四件套：${s.four}</b>`:''}</span>`;
  $('readySetName').textContent=s.name;
}
function rollStar(rule){
  let roll=Math.random()*100;
  for(const [star,chance] of rule.stars){roll-=chance;if(roll<0)return star;}
  return rule.stars.at(-1)[0];
}
function renderRewards(){
  const rule=levelRules[state.level];
  if(state.started&&state.lockedStar){$('rewardResult').innerHTML=`<div class="locked-reward"><span class="reward-glyph">✦</span><div><strong>${starLabel(state.lockedStar)}</strong><small>本场已锁定 · ${state.lockedStar}星遗器</small></div><b>已锁定</b></div>`;return;}
  $('rewardResult').innerHTML=rule.stars.map(([star,rate])=>`<div class="star-rate"><span>${star}星遗器</span><span class="stars-row">${starLabel(star)}</span><span class="bar"><i style="--rate:${rate}%"></i></span><span class="percent">${rate}%</span></div>`).join('');
}
function renderWaves(){
  const rule=levelRules[state.level];
  $('waveTrack').innerHTML=rule.waves.map((wave,index)=>{
    const counts=wave.reduce((map,type)=>(map[type]=(map[type]||0)+1,map),{});
    const chips=Object.entries(counts).map(([type,count])=>`<span class="mob-chip ${type==='精英'?'elite':type==='远程'?'ranged':''}"><i></i>${type} ×${count}</span>`).join('');
    const active=state.started&&!state.finished&&state.wave===index;
    const cleared=state.started&&(state.finished||state.wave>index);
    let foot=cleared?'已清除':active?`剩余 ${Math.max(0,4-state.defeated.length)} 只`:state.started?'待召出':'4只敌人';
    return `<article class="wave-card ${active?'active':''} ${cleared?'cleared':''}"><div class="wave-card-head"><strong>第 ${index+1} 波</strong><span>${cleared?'✓':active?`${state.defeated.length}/4`:'4 / 4'}</span></div><div class="mob-counts">${chips}</div><div class="wave-card-foot">${foot}</div></article>`;
  }).join('');
}
function renderEnemies(){
  if(!state.started||state.finished){$('enemies').innerHTML='';return;}
  const wave=levelRules[state.level].waves[state.wave];
  $('enemies').innerHTML=wave.map((type,index)=>`<button class="enemy ${type==='精英'?'elite':type==='远程'?'ranged':'melee'} ${state.defeated.includes(index)?'defeated':''}" data-index="${index}" aria-label="${type}敌人，点击模拟击败"><span class="mob-art"><i class="mob-head"></i><i class="mob-body"></i><i class="mob-legs"><b></b><b></b></i>${type==='远程'?'<i class="enemy-weapon">➶</i>':''}</span><span class="enemy-type">${type==='精英'?'精英':type==='远程'?'远程':'近战'}</span></button>`).join('');
  $('enemies').querySelectorAll('.enemy').forEach(enemy=>enemy.addEventListener('click',()=>defeatOne(Number(enemy.dataset.index))));
}
function refresh(){
  const rule=levelRules[state.level];
  $('pathLevel').value=String(state.level);
  $('levelSeal').textContent=rule.roman;
  $('difficultyPill').textContent=`${rule.tier}难度`;
  $('tierCopy').textContent=`${rule.tier} · ${rule.copy} ${rule.elite}`;
  $('readySummary').textContent=`${state.level}级 · ${rule.tier} · 三波试炼`;
  $('lockState').textContent=state.started?'本场已锁定':'未锁定';
  $('lockState').classList.toggle('locked',state.started);
  $('pathLevel').disabled=state.started;
  $('setGrid').querySelectorAll('button').forEach(btn=>{btn.disabled=state.started;btn.classList.toggle('selected',btn.dataset.set===state.setId);btn.setAttribute('aria-pressed',String(btn.dataset.set===state.setId));});
  renderSetDetails();renderRewards();renderWaves();renderEnemies();
  $('waveCounter').textContent=state.started?`${state.finished?3:state.wave+1} / 3`:'— / 3';
  $('enemyCounter').textContent=state.started&&!state.finished?`剩余 ${Math.max(0,4-state.defeated.length)} / 4`:'剩余 — / 4';
  $('arenaStatus').textContent=state.finished?'试炼完成 · 遗器已掉落':state.transitioning?`第 ${state.wave+1} 波清除 · 下一波召出中`:state.started?`第 ${state.wave+1} 波 · 点击敌人模拟战斗`:'待命 · 选择套装并开始';
  $('clearWave').disabled=!state.started||state.finished;
  $('clearWave').textContent=state.started&&state.wave===2?'完成最后一波':'清完本波';
  $('retry').hidden=!state.finished;
  const start=$('startTrial');start.classList.toggle('running',state.started&&!state.finished);start.classList.toggle('finished',state.finished);
  start.disabled=state.started&&!state.finished;
  start.innerHTML=state.finished?'<span>试炼完成</span><b>✓</b>':state.started?'<span>挑战进行中</span><b>···</b>':'<span>开始试炼</span><b>→</b>';
  $('controlHint').textContent=state.finished?'演示掉落已生成；重新挑战会开启新的独立场次。':state.started?'演示模式：点击场内敌人逐只击败，也可清完当前波。':'演示模式：开始后点击场内敌人模拟击败。';
  $('resultNote').textContent=state.finished?'星级与套装已在开场锁定；点击“重新挑战”开启新场次。':state.started?'套装、等级、难度和星级均已锁定。':'发起者等级锁定本场难度与星级；失败不发放遗器。';
  $('arenaView').classList.toggle('in-combat',state.started&&!state.finished);
}
function showToast(msg){const t=$('toast');t.textContent=msg;t.classList.add('show');clearTimeout(showToast.timer);showToast.timer=setTimeout(()=>t.classList.remove('show'),1700)}
function beginTrial(){
  if(state.started)return;
  state.lockedStar=rollStar(levelRules[state.level]);state.started=true;state.wave=0;state.defeated=[];state.transitioning=false;refresh();showToast(`挑战开始 · 本场${state.lockedStar}星奖励已锁定`);
}
function defeatOne(index){
  if(!state.started||state.finished||state.transitioning||state.defeated.includes(index))return;
  state.defeated.push(index);refresh();
  if(state.defeated.length===4){
    state.transitioning=true;refresh();
    if(state.wave<2){
      showToast(`第 ${state.wave+1} 波清除 · 准备下一波`);
      setTimeout(()=>{if(!state.started||state.finished||!state.transitioning)return;state.wave++;state.defeated=[];state.transitioning=false;refresh();showToast(`第 ${state.wave+1} 波已召出`);},850);
    }else{
      setTimeout(()=>{if(state.started&&!state.finished)completeTrial();},650);
    }
  }
}
function clearCurrentWave(){
  if(!state.started||state.finished)return;
  state.defeated=[0,1,2,3];state.transitioning=false;
  if(state.wave<2){state.wave++;state.defeated=[];refresh();showToast(`第 ${state.wave+1} 波已召出`);}
  else completeTrial();
}
function completeTrial(){
  state.finished=true;state.transitioning=false;state.defeated=[0,1,2,3];refresh();
  const s=selectedSet();const parts=s.ring.startsWith('外圈')?['头部','手部','躯干','脚部']:['位面球','连结绳'];
  const part=parts[Math.floor(Math.random()*parts.length)];
  $('arenaStatus').textContent='试炼完成 · 遗器已掉落';
  $('controlHint').textContent=`获得 ${starLabel(state.lockedStar)} ${state.lockedStar}星遗器 · ${s.name} · ${part}`;
  showToast(`通关奖励：${state.lockedStar}星 · ${part} · ${s.name}`);
}
function resetTrial(){state.started=false;state.finished=false;state.transitioning=false;state.wave=0;state.defeated=[];state.lockedStar=null;refresh();showToast('演示已重置');}
function retryTrial(){resetTrial();beginTrial();}
function setStructureView(view){
  const plan=view==='plan';
  const build=view==='build';
  $('arenaView').dataset.view=build?'build':plan?'plan':'perspective';
  $('perspectiveView').hidden=plan||build;
  $('planView').hidden=!plan;
  $('buildEditor').hidden=!build;
  document.querySelectorAll('[data-structure]').forEach(tab=>{
    const active=tab.dataset.structure===view;
    tab.classList.toggle('active',active);
    tab.setAttribute('aria-selected',String(active));
  });
  $('structureCaption').textContent=plan
    ?'25×25格方块平面图 · 墙厚2格 · 北侧留5格入口'
    :build?'逐层逐格放置 · 方块清单实时统计':'石砖围墙 · 四角深板岩塔 · 中央星轨装置';
}
function init(){
  makeSetGrid();
  initBuildPlanner();
  document.querySelectorAll('[data-structure]').forEach(tab=>tab.addEventListener('click',()=>setStructureView(tab.dataset.structure)));
  $('pathLevel').addEventListener('change',e=>{if(!state.started){state.level=Number(e.target.value);refresh();}});
  $('startTrial').addEventListener('click',()=>{if(state.finished)return;beginTrial();});
  $('clearWave').addEventListener('click',clearCurrentWave);
  $('retry').addEventListener('click',retryTrial);
  $('resetDemo').addEventListener('click',resetTrial);
  $('centerDevice').addEventListener('click',()=>state.started?showToast('本场进行中：装置已锁定套装'):showToast(`已选择：${selectedSet().name}`));
  refresh();
}
init();
