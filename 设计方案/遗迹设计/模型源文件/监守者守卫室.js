// 中文说明：1.2.0 守卫室设计草案。复用十种现有模型，只在本页预览中替换主殿内饰。
// 实体与方块分开保存，监守者示意不计入方块用量，也不会进入现有游戏蓝图导出。
(() => {
  const design = window.RuinDesign, variants = window.RuinVariants.definitions;
  const $ = id => document.getElementById(id);
  if (!design || !$('guardCanvas')) return;
  const palette = {...design.getPalette(), guardBars:['#a4bac0','铁栏杆'],
    guardBody:['#12333a','监守者示意'],guardHorn:['#93d1cb','监守者触角'],guardCore:['#59c6c5','监守者胸腔示意']};
  const layout = Object.freeze({guardian:{x:-2,y:8,z:-10}, chest:{x:0,y:10,z:-21},
    gate:{z:-18,x0:-2,x1:2,y0:8,y1:12}, arena:{x0:-10,x1:10,z0:-17,z1:-3},
    entrance:{x:0,y:8,z:0}, ladder:{x:7,y:8,z:-13}});
  let variant = 'ordinary', defeated = false, view = 'room', turn = 0, zoom = 1, layer = 'route';
  let current = [], scheduled = false;
  const cache = new Map(), key = (x,y,z) => `${x},${y},${z}`;

  // 一套整数方块坐标用于所有命途。原版监守者保持实体身份，装饰才按命途替换。
  function build(id, open) {
    const cacheKey = id + ':' + open;
    if (cache.has(cacheKey)) return cache.get(cacheKey);
    const def = variants.find(d => d.id === id), source = design.getModelFor(id);
    const map = new Map(source.map(b => [key(b.x,b.y,b.z), {...b}]));
    const put = (x,y,z,m) => map.set(key(x,y,z), {x,y,z,m});
    const box = (x0,x1,y0,y1,z0,z1,m) => {
      for(let x=x0;x<=x1;x++) for(let y=y0;y<=y1;y++) for(let z=z0;z<=z1;z++) put(x,y,z,m);
    };
    // 清出战斗区与后方藏宝室；现有主殿、浮岛和高塔外形继续取自原模型。
    for(const b of source) if(b.x>=-12 && b.x<=12 && b.y>=8 && b.y<=23 && b.z>=-25 && b.z<=-1)
      map.delete(key(b.x,b.y,b.z));
    const accent = def.map.prism || 'prism';
    box(-12,12,7,7,-26,0,'q');
    for(const x of [-12,12]) box(x,x,8,23,-26,0,'qb');
    for(const z of [-26,0]) box(-12,12,8,23,z,z,'qb');
    box(-12,12,24,24,-26,0,'q');
    // 入口净宽三格、净高两格：玩家可进退，约三格高的监守者不能离开主殿。
    for(let x=-1;x<=1;x++) for(let y=8;y<=9;y++) map.delete(key(x,y,0));
    box(-3,3,10,10,0,0,accent);
    // 彩色材料仅作墙面点缀；战斗地面统一石质，避免岩浆或冰面引入额外危险。
    for(const x of [-12,12]) for(const z of [-22,-14,-6]) {
      box(x,x,11,18,z-1,z+1,def.map.glass||'glass');
      put(x,9,z,'glow');
    }
    // 贴墙立柱与高位色带延续原主殿的尺度感，不占用中央战斗路线。
    for(const x of [-11,11]) for(const z of [-23,-15,-7]) {
      box(x,x,8,21,z,z,'pillar');put(x,22,z,accent);put(x,10,z+1,'glow');
    }
    for(const x of [-12,12]) box(x,x,21,21,-26,0,accent);
    box(-12,12,21,21,-26,-26,accent);
    for(const x of [-11,11]) for(let z=-25;z<=-1;z++) put(x,7,z,'slate');
    for(let x=-4;x<=0;x++) for(let z=-12;z<=-8;z++)
      if(x===-4 || x===0 || z===-12 || z===-8) put(x,7,z,'slate');
    // 隔墙封到主殿顶，门洞五格宽五格高；开门预览仅移除铁栏杆。
    box(-12,12,8,23,-18,-18,'qb');
    for(let x=-2;x<=2;x++) for(let y=8;y<=12;y++) {
      map.delete(key(x,y,-18)); if(!open) put(x,y,-18,'guardBars');
    }
    for(const x of [-3,3]) box(x,x,8,13,-18,-18,'pillar');
    box(-3,3,13,13,-18,-18,accent);
    for(const x of [-4,4]) put(x,11,-18,'glow');
    // 保留已通过游戏检查的藏宝坐标与向前的阶梯，箱盖上方保持空位。
    box(-3,3,8,8,-23,-20,'black'); box(-2,2,9,9,-23,-21,'q');
    box(-2,2,8,8,-19,-19,'q'); put(0,10,-21,'chest');
    for(const x of [-7,7]) {box(x,x,8,9,-23,-23,'pillar');put(x,10,-23,'glow');}
    // 保留东侧原有梯井与支撑。它仍连接观星室，不在主殿中央另加楼梯。
    for(const b of source) if(b.z===-13 && (b.x===7 || b.x===8) && b.y>=8 && b.y<=31)
      put(b.x,b.y,b.z,b.m);
    const blocks = [...map.values()]; cache.set(cacheKey,blocks); return blocks;
  }

  // 监守者为约 2.9 格高的简化实体轮廓，青色触角和胸腔只表示外观。
  function guardianParts() {
    if(defeated) return [];
    const {x,y,z} = layout.guardian;
    const part=(dx,dy,dz,w,h,d,m)=>({x:x+dx,y:y+dy,z:z+dz,w,h,d,m,entity:true});
    return [part(-.38,0,-.28,.29,.76,.5,'guardBody'),part(.09,0,-.28,.29,.76,.5,'guardBody'),
      part(-.55,.69,-.34,1.1,1.31,.68,'guardBody'),part(-.74,.92,-.24,.23,1.13,.46,'guardBody'),
      part(.51,.92,-.24,.23,1.13,.46,'guardBody'),part(-.4,1.92,-.3,.8,.58,.6,'guardBody'),
      part(-.52,2.42,-.1,.16,.38,.2,'guardHorn'),part(.36,2.42,-.1,.16,.38,.2,'guardHorn'),
      part(-.63,2.72,-.1,.27,.16,.2,'guardHorn'),part(.36,2.72,-.1,.27,.16,.2,'guardHorn'),
      part(-.28,1.11,.345,.56,.58,.06,'guardCore')];
  }

  const canvas=$('guardCanvas'), ctx=canvas.getContext('2d');
  const rotatePoint=(x,y,z)=>{for(let i=0;i<turn;i++){const t=x;x=-z;z=t;}return [x,y,z];};
  const shade=(hex,f)=>{const n=parseInt(hex.slice(1),16);return `rgb(${Math.round((n>>16)*f)},${Math.round((n>>8&255)*f)},${Math.round((n&255)*f)})`;};
  function draw() {
    scheduled=false;
    const rect=canvas.getBoundingClientRect(); if(!rect.width) return;
    const W=rect.width,H=rect.height,dpr=Math.min(devicePixelRatio||1,2);
    canvas.width=W*dpr;canvas.height=H*dpr;ctx.setTransform(dpr,0,0,dpr,0,0);
    ctx.clearRect(0,0,W,H);
    const frame=view==='whole'?[-46,46,-41,94,-39,47]:view==='location'?[-20,20,2,33,-31,12]:view==='treasure'?[-14,14,7,19,-28,-17]:[-15,15,7,24,-28,2];
    let u0=Infinity,u1=-Infinity,v0=Infinity,v1=-Infinity;
    const uv=(x,y,z)=>{const r=rotatePoint(x,y,z);return [r[0]-r[2],(r[0]+r[2])*.5-r[1]];};
    for(const x of [frame[0],frame[1]])for(const y of [frame[2],frame[3]])for(const z of [frame[4],frame[5]]){
      const [u,v]=uv(x,y,z);u0=Math.min(u0,u);u1=Math.max(u1,u);v0=Math.min(v0,v);v1=Math.max(v1,v);
    }
    const scale=Math.min((W-75)/(u1-u0),(H-100)/(v1-v0))*zoom;
    const p=(x,y,z)=>{const [u,v]=uv(x,y,z);return [W/2+(u-(u0+u1)/2)*scale,H/2+(v-(v0+v1)/2)*scale];};
    // 剖切只影响显示，不会从实际房间数据删除屋顶或围墙。
    const list=current.filter(b=>view==='whole'||(b.x>=frame[0]&&b.x<=frame[1]&&b.z>=frame[4]&&b.z<=frame[5]&&b.y>=frame[2]&&b.y<=(view==='room'?23:frame[3])));
    const nearWall=b=>turn===0?b.z>=0||b.x>=12:turn===1?b.z<=-26||b.x>=12:
      turn===2?b.z<=-26||b.x<=-12:b.z>=0||b.x<=-12;
    const filtered=list.filter(b=>view==='whole'||!(b.y>8&&(nearWall(b)||
      (b.z===-18&&(view==='treasure'||(turn<2?b.x>=3:b.x<=-3))))));
    const occupied=new Set(filtered.filter(b=>b.m!=='guardBars'&&b.m!=='chest'&&b.m!=='ladder').map(b=>key(b.x,b.y,b.z)));
    const faces=[];
    function cube(x,y,z,w,h,d,m,solid=false) {
      const vertices=[[x,y,z],[x+w,y,z],[x+w,y,z+d],[x,y,z+d],[x,y+h,z],[x+w,y+h,z],[x+w,y+h,z+d],[x,y+h,z+d]];
      for(const [indices,normal,factor] of [[[1,2,6,5],[1,0,0],.73],[[0,4,7,3],[-1,0,0],.73],[[3,7,6,2],[0,0,1],.87],[[0,1,5,4],[0,0,-1],.87],[[4,5,6,7],[0,1,0],1]]) {
        const rn=rotatePoint(...normal);
        if(normal[1]===0&&rn[0]+rn[2]<=0) continue;
        if(solid&&occupied.has(key(x+normal[0],y+normal[1],z+normal[2]))) continue;
        const points=indices.map(i=>vertices[i]),center=points.reduce((a,b)=>a.map((v,i)=>v+b[i]/4),[0,0,0]);
        const r=rotatePoint(...center);faces.push({points:points.map(v=>p(...v)),depth:r[0]+r[2]+r[1],color:shade(palette[m][0],factor)});
      }
    }
    for(const b of filtered) {
      if(b.m==='guardBars') {cube(b.x+.44,b.y,b.z+.44,.12,1,.12,b.m);cube(b.x,b.y+.48,b.z+.44,1,.1,.12,b.m);}
      else if(b.m==='ladder') cube(b.x+.35,b.y,b.z+.87,.3,1,.12,b.m);
      else if(b.m==='chest') {cube(b.x+.06,b.y,b.z+.06,.88,.88,.88,b.m);cube(b.x+.44,b.y+.3,b.z+.95,.13,.23,.03,'copper');}
      else cube(b.x,b.y,b.z,1,b.m==='seat'?.5:b.m==='carpet'?.063:1,1,b.m,!['seat','carpet','rod','chain','lectern'].includes(b.m));
    }
    if(view!=='treasure')for(const e of guardianParts()) cube(e.x,e.y,e.z,e.w,e.h,e.d,e.m);
    // 玩家是比例尺，不是守卫挑战中的另一只实体。
    if(view!=='treasure'){cube(-.3,8,-3.3,.6,1.3,.6,'copper');cube(-.23,9.3,-3.23,.46,.5,.46,'q');}
    faces.sort((a,b)=>a.depth-b.depth);
    for(const f of faces) {ctx.beginPath();f.points.forEach((v,i)=>i?ctx.lineTo(...v):ctx.moveTo(...v));ctx.closePath();ctx.fillStyle=f.color;ctx.fill();if(scale>4){ctx.strokeStyle='#03121b38';ctx.lineWidth=.45;ctx.stroke();}}
    if(view!=='whole') {
      const annotations=view==='treasure'?[['原宝箱 (0,10,-21)',0,11,-21,55,-40,'#f3c078'],['原有祭坛阶梯',0,8,-19,40,30,'#85eee0']]:[['监守者位置',-2,11,-10,-90,-22,'#85eee0'],['五格铁栅门',0,13,-18,55,-10,defeated?'#8ee9bb':'#f3c078'],['原宝箱',0,11,-21,60,-43,'#f3c078'],['玩家 · 1.8 格',0,9.8,-3,-90,20,'#e7caa2']];
      for(const [text,x,y,z,dx,dy,color] of annotations) {const q=p(x,y,z),a=[Math.max(15,Math.min(W-135,q[0]+dx)),Math.max(45,Math.min(H-35,q[1]+dy))];ctx.strokeStyle=color;ctx.lineWidth=1;ctx.beginPath();ctx.moveTo(...q);ctx.lineTo(a[0]+4,a[1]+5);ctx.stroke();ctx.fillStyle='#0c1820e8';ctx.fillRect(a[0],a[1]-12,124,22);ctx.fillStyle=color;ctx.font='12px Microsoft YaHei';ctx.fillText(text,a[0]+5,a[1]+3);}
    }
    ctx.fillStyle='#c5e2df';ctx.font='13px Microsoft YaHei';ctx.fillText(variants.find(v=>v.id===variant).name+' · '+(defeated?'守卫已击败 / 开门预览':'守卫存活 / 宝箱室封闭'),18,27);
    drawPlan();
  }
  function schedule(){if(!scheduled){scheduled=true;requestAnimationFrame(draw);}}

  // 俯视图与三维预览读取同一方块列表，可切换精确 Y 层查看门、地坪和顶封。
  function drawPlan() {
    const cell=16,ox=330,oz=505,point=(x,z)=>[ox+(x+.5)*cell,oz+(z+.5)*cell];
    const columns=new Map();
    for(const b of current) if(b.x>=-14&&b.x<=14&&b.z>=-27&&b.z<=1&&
      (layer==='route'?b.y>=7&&b.y<=10:b.y===Number(layer))) {
      const k=`${b.x},${b.z}`;if(!columns.has(k)||columns.get(k).y<b.y) columns.set(k,b);
    }
    let svg='<rect width="660" height="570" fill="#0c1721"/>';
    for(let x=-15;x<=15;x++)svg+=`<path d="M${ox+x*cell} 41V537" stroke="#263f4d" stroke-width=".4"/>`;
    for(let z=-29;z<=2;z++)svg+=`<path d="M90 ${oz+z*cell}H570" stroke="#263f4d" stroke-width=".4"/>`;
    for(const b of columns.values())svg+=`<rect x="${ox+b.x*cell}" y="${oz+b.z*cell}" width="16" height="16" fill="${palette[b.m][0]}" stroke="#11243088" stroke-width=".5" data-guard-cell="${b.x},${b.y},${b.z}" data-material="${b.m}"/>`;
    let overlay='';
    if(layer==='route') {
      overlay+=`<rect x="${ox-10*cell}" y="${oz-17*cell}" width="336" height="240" fill="#359b9228" stroke="#65cbbf" stroke-dasharray="5 4"/>`;
      const g=point(-2,-10),c=point(0,-21);
      overlay+=`<circle cx="${g[0]}" cy="${g[1]}" r="14" fill="${defeated?'#2a4b49':'#173d45'}" stroke="#8ce4dc" stroke-width="2"/><text x="${g[0]}" y="${g[1]+4}" fill="#9cf0e3" text-anchor="middle" font-size="12">${defeated?'✓':'守'}</text>`;
      overlay+=`<rect x="${c[0]-7}" y="${c[1]-7}" width="14" height="14" fill="#cb914b" stroke="#ffe8b4"/>`;
      overlay+=`<path d="M${point(0,1)[0]} ${point(0,1)[1]}L${point(0,-5)[0]} ${point(0,-5)[1]}L${point(-5,-10)[0]} ${point(-5,-10)[1]}" fill="none" stroke="#83d5c6" stroke-width="2" stroke-dasharray="5 4"/>`;
      if(defeated)overlay+=`<path d="M${point(0,-15)[0]} ${point(0,-15)[1]}L${c[0]} ${c[1]+20}" stroke="#a7e5b8" stroke-width="2" stroke-dasharray="5 4"/>`;
      const label=(t,x,z,tx,ty,color)=>{const a=point(x,z);return `<path d="M${a[0]} ${a[1]}L${tx} ${ty}" stroke="${color}" fill="none"/><rect x="${tx-3}" y="${ty-14}" width="124" height="24" fill="#0b1722ed"/><text x="${tx+3}" y="${ty+2}" fill="${color}" font-size="12">${t}</text>`;};
      overlay+=label('宝箱 (0,10,-21)',0,-21,470,97,'#f2cc8f');
      overlay+=label(defeated?'已开放的门':'封闭铁栅门',0,-18,470,157,defeated?'#8ee9bb':'#f2cc8f');
      overlay+=label('监守者 (-2,8,-10)',-2,-10,18,317,'#8ce4dc');
      overlay+=label('原有观星梯井',7,-13,470,257,'#9fc5d4');
      overlay+=label('三格宽入口',0,0,470,525,'#9fc5d4');
      overlay+=`<rect x="245" y="271" width="170" height="23" fill="#132d36e8"/><text x="330" y="287" text-anchor="middle" fill="#dbefeb" font-size="13">战斗区 · 21 × 15 格</text>`;
    }
    svg+=`<g pointer-events="none">${overlay}<text x="24" y="24" fill="#9dbcc9" font-size="12">X →　Z ↓　每格 = 1 方块 · ${layer==='route'?'功能俯视图':`精确层 Y=${layer}`}</text>`;
    for(let x=-10;x<=10;x+=5)svg+=`<text x="${point(x,0)[0]}" y="48" text-anchor="middle" fill="#8baab9" font-size="10">${x}</text>`;
    svg+='</g>';$('guardPlan').innerHTML=svg;
  }

  function update() {
    current=build(variant,defeated);
    const def=variants.find(v=>v.id===variant);
    $('guard').style.setProperty('--guard-accent',def.accent);
    $('guardVariantTitle').textContent=def.name+' · 同一守卫布局';
    $('guardStateTitle').textContent=defeated?'已击败 · 通路永久开放':'存活 · 宝箱室封闭';
    $('guardStateButton').textContent=defeated?'恢复守卫存活预览':'预览击败后开门';
    $('guardStateButton').classList.toggle('open',defeated);
    $('guardStatusText').textContent=defeated?'铁栅门移除，可通过阶梯走到原宝箱。再次切换命途仍查看同一完成状态。':'一只原版监守者位于 (-2,8,-10)。击败它后，后方宝箱室开放。';
    $('guardCaption').textContent=view==='whole'?'完整遗迹定位：守卫区置于既有主殿内，浮岛与高塔取自当前命途原模型。':'剖切显示：屋顶、近侧墙及部分隔墙已隐藏以便观察；实际模型保留完整封顶与隔墙。东侧原梯井保留。';
    document.querySelectorAll('[data-guard-variant]').forEach(b=>{const on=b.dataset.guardVariant===variant;b.classList.toggle('selected',on);b.setAttribute('aria-pressed',on);});
    document.querySelectorAll('[data-guard-view]').forEach(b=>b.classList.toggle('selected',b.dataset.guardView===view));
    // 材料表仅统计守卫室范围，忽略剖切状态和门开闭预览，避免切换造成施工量跳变。
    const counts=new Map();for(const b of build(variant,false))if(b.x>=-12&&b.x<=12&&b.y>=7&&b.y<=24&&b.z>=-26&&b.z<=0)counts.set(b.m,(counts.get(b.m)||0)+1);
    $('guardMaterials').innerHTML=[...counts].sort((a,b)=>b[1]-a[1]).map(([m,n])=>`<div class="guard-mat"><i style="background:${palette[m][0]}"></i><div><b>${palette[m][1]}</b><small>${n.toLocaleString()} 件 · 房间范围内</small></div></div>`).join('');
    schedule();
  }
  for(const def of variants) {
    const b=document.createElement('button');b.type='button';b.dataset.guardVariant=def.id;b.textContent=def.name;b.onclick=()=>{variant=def.id;update();};$('guardVariantPicker').append(b);
  }
  $('guardStateButton').onclick=()=>{defeated=!defeated;update();};
  $('guardTurn').onclick=()=>{turn=(turn+1)%4;schedule();};
  $('guardReset').onclick=()=>{turn=0;zoom=1;view='room';$('guardZoom').value=1;update();};
  $('guardZoom').oninput=e=>{zoom=Number(e.target.value);schedule();};
  document.querySelectorAll('[data-guard-view]').forEach(b=>b.onclick=()=>{view=b.dataset.guardView;zoom=1;$('guardZoom').value=1;update();});
  document.querySelectorAll('[data-guard-layer]').forEach(b=>b.onclick=()=>{layer=b.dataset.guardLayer;document.querySelectorAll('[data-guard-layer]').forEach(x=>x.classList.toggle('selected',x===b));drawPlan();});
  $('guardPlan').onpointermove=e=>{const cell=e.target.closest('[data-guard-cell]');$('guardHover').textContent=cell?`局部坐标 (${cell.dataset.guardCell}) · ${palette[cell.dataset.material][1]} · 世界坐标 = 锚点 + 局部坐标`:'将鼠标移到色格上查看方块坐标和材料。';};
  document.querySelector('[data-view="guard"]').addEventListener('click',schedule);
  window.addEventListener('resize',schedule);
  const params=new URLSearchParams(location.search);if(variants.some(v=>v.id===params.get('variant')))variant=params.get('variant');
  if(['room','treasure','location','whole'].includes(params.get('guardView')))view=params.get('guardView');
  defeated=params.get('guardState')==='defeated';update();
  // 暴露只读草案数据，便于检验十种变体坐标一致，避免实体混入方块施工清单。
  window.RuinGuardDesign={layout,build,getState:()=>({variant,defeated,view,layer})};
})();
