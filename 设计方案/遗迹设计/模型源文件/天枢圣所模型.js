// 项目中文说明：普通母体遗迹的方块模型数据与三维预览逻辑。

/* Local, dependency-free architectural preview. One cell equals one Minecraft block.
   Colors approximate vanilla materials; this is a design model, not a game structure export. */
(() => {
  const canvas = document.getElementById('voxelModel');
  if (!canvas) return;
  const blocks = new Map(), key = (x,y,z) => `${x},${y},${z}`;
  const palette = {
    q:['#eeeade','平滑石英块'], qb:['#dbd9cf','石英砖'], pillar:['#ede8dc','石英柱'],
    calcite:['#d4d3cb','方解石'], prism:['#438b81','海晶石砖'], dark:['#285c58','暗海晶石'],
    black:['#373d49','磨制黑石砖'], slate:['#586270','深板岩瓦'],
    glow:['#dcfff0','海晶灯'], glass:['#82d7cf','青色染色玻璃'], amethyst:['#ac8dcf','紫水晶块'],
    copper:['#b89366','涂蜡的切制铜块'], chest:['#af783f','箱子'],
    seat:['#e3dfd3','平滑石英台阶'], carpet:['#397e7e','青色地毯'],
    shelf:['#916c42','书架'], wood:['#705135','云杉木板'], lectern:['#ad8756','讲台'],
    chain:['#778894','锁链'], rod:['#fff4d1','末地烛'], moss:['#638755','苔藓块'], leaves:['#668a5c','杜鹃树叶'],
    ladder:['#9b7955','梯子']
  };
  // Partial-height furniture uses the corresponding block-sized envelope.
  const shapes={seat:{h:.5},carpet:{h:.0625},chain:{w:.13},rod:{w:.13},ladder:{w:.13},chest:{w:.875,h:.875},lectern:{w:.8,h:.85}};
  function put(x,y,z,m='q') { blocks.set(key(x,y,z),{x,y,z,m}); }
  function box(x0,x1,y0,y1,z0,z1,m='q') {
    for(let x=x0;x<=x1;x++) for(let y=y0;y<=y1;y++) for(let z=z0;z<=z1;z++) put(x,y,z,m);
  }
  function rim(cx,cz,rx,rz,y,m) {
    for(let x=cx-rx;x<=cx+rx;x++) for(let z=cz-rz;z<=cz+rz;z++)
      if(x===cx-rx || x===cx+rx || z===cz-rz || z===cz+rz) put(x,y,z,m);
  }
  function disc(cx,cz,rx,rz,y,m,inner=0) {
    for(let x=-rx;x<=rx;x++) for(let z=-rz;z<=rz;z++) {
      const d=(x*x)/(rx*rx)+(z*z)/(rz*rz);
      if(d<=1.015 && d>=inner) put(cx+x,y,cz+z,m);
    }
  }
  function column(x,z,y,h,width=2) {
    box(x-1,x+width,y,y,z-1,z+width,'black');
    box(x,x+width-1,y+1,y+h,z,z+width-1,'pillar');
    box(x-1,x+width,y+h+1,y+h+1,z-1,z+width,'dark');
    box(x-1,x+width,y+h+2,y+h+2,z-1,z+width,'q');
  }
  // Continuous, stepped underside, with eight stone ribs running to the lower keel.
  for(let layer=0;layer<34;layer++) {
    const r=1-layer/38;
    disc(0,0,Math.round(36*r),Math.round(36*r),-layer-1,layer%6===0?'black':'slate');
  }
  for(let y=-41;y<=-35;y++) disc(0,0,Math.max(1,y+42),Math.max(1,y+42),y,y%3===0?'glow':'glass');
  for(let i=0;i<8;i++) for(let d=0;d<33;d++) {
    const a=i*Math.PI/4,r=35-d*.92,x=Math.round(Math.cos(a)*r),z=Math.round(Math.sin(a)*r);
    box(x-1,x+1,-d-1,-d-1,z-1,z+1,d%6===0?'prism':'black');
  }
  disc(0,0,37,37,0,'dark'); disc(0,0,36,36,1,'qb'); disc(0,0,35,35,2,'q');
  disc(0,0,33,33,2,'dark',.91); disc(0,0,25,25,2,'prism',.95);
  // Paving compass: broad approach, radial gold/copper inlay and recessed lanterns.
  for(let i=-31;i<=31;i++) {put(i,2,0,'calcite'); put(0,2,i,'prism');}
  for(let i=0;i<16;i++) {
    const a=i*Math.PI/8,x=Math.round(31*Math.cos(a)),z=Math.round(31*Math.sin(a));
    put(x,2,z,'glow');
  }
  // Front landing projects beyond the main island; all levels have visible support.
  box(-10,10,-1,1,32,46,'black'); box(-10,10,2,2,32,46,'q');
  for(const x of [-10,10]) box(x,x,3,3,33,46,'dark');
  for(const x of [-8,7]) column(x,40,3,13,2);
  box(-8,8,18,18,40,41,'q'); box(-8,8,19,19,40,41,'prism');
  // Raised main sanctuary and nine-block-wide ceremonial staircase.
  box(-14,14,3,6,-27,1,'black'); box(-14,14,7,7,-27,1,'q');
  rim(0,-13,14,14,6,'prism');
  for(let step=0;step<5;step++) box(-5,5,3,3+step,6-step,6-step,'qb');
  // Double-height nave. Open central portal, tall windows and external pilasters.
  for(let y=8;y<=23;y++) {
    for(let x=-13;x<=13;x++) for(const z of [-26,0]) {
      if(z===0 && Math.abs(x)<=4 && y<20-Math.max(0,Math.abs(x)-2)) continue;
      put(x,y,z,Math.abs(x)>5 && Math.abs(x)<9 && y>=11 && y<=19?'glass':'qb');
    }
    for(let z=-25;z<0;z++) for(const x of [-13,13])
      put(x,y,z,((z+26)%8>=2 && (z+26)%8<=4 && y>=11 && y<=20)?'glass':'qb');
  }
  for(const x of [-13,11]) for(const z of [-26,-18,-10,-2]) column(x,z,7,16,2);
  // Tall roof is terraced into a stepped vault, leaving the central tower aperture.
  for(let d=0;d<=5;d++) for(let z=-27;z<=1;z++) {
    for(const x of [-14+d,14-d]) {put(x,25+d,z,d===0?'dark':'q');put(x,26+d,z,'q');}
  }
  box(-9,9,31,31,-27,1,'q'); rim(0,-13,14,14,24,'prism');
  // Nave interior: altar, choir rows and luminous reliquary.
  box(-3,3,8,8,-23,-19,'dark');box(-2,2,9,9,-22,-20,'q');put(0,10,-21,'chest');
  for(const x of [-8,7]) for(let z=-17;z<=-5;z+=4) box(x,x+1,8,8,z,z+1,'qb');
  for(const x of [-9,9]) for(let z=-22;z<=-2;z+=5) put(x,8,z,'glow');
  // Main tower: broad lower drum, recessed clerestory, open crown and monumental halo.
  function tower(cx,cz,r,y0,h) {
    for(let y=y0;y<y0+h;y++) for(let x=-r;x<=r;x++) for(let z=-r;z<=r;z++) {
      if(Math.max(Math.abs(x),Math.abs(z))!==r) continue;
      const window=(Math.abs(x)<=1||Math.abs(z)<=1)&&y>y0+2&&y<y0+h-2;
      put(cx+x,y,cz+z,window?'glass':'qb');
    }
    for(const x of [-r,r-1]) for(const z of [-r,r-1]) column(cx+x,cz+z,y0,h-1,2);
    box(cx-r-1,cx+r+1,y0+h,y0+h,cz-r-1,cz+r+1,'dark');
    box(cx-r-1,cx+r+1,y0+h+1,y0+h+1,cz-r-1,cz+r+1,'q');
  }
  tower(0,-13,8,30,14); tower(0,-13,5,46,11);
  for(const x of [-3,2]) for(const z of [-16,-11]) column(x,z,59,7,2);
  rim(0,-13,4,4,68,'prism'); rim(0,-13,4,4,69,'q'); put(0,64,-13,'glow');
  // Upright voxel ring is an architectural stone crown, not a painted symbol.
  for(let x=-8;x<=8;x++) for(let y=65;y<=81;y++) {
    const r=Math.hypot(x,y-73); if(r>=6.5&&r<=8) {put(x,y,-14,'q');put(x,y,-13,r<7.2?'prism':'q');}
  }
  box(-1,1,71,74,-13,-13,'glow');
  // Two lower side sanctuaries, with separate spires and connected five-block bridges.
  for(const sign of [-1,1]) {
    const cx=sign*29;
    box(Math.min(sign*15,sign*26),Math.max(sign*15,sign*26),3,3,-14,-10,'qb');
    disc(cx,-12,10,12,2,'black'); disc(cx,-12,9,11,3,'q');
    for(let d=0;d<8;d++) disc(cx,-12,Math.max(2,9-d),Math.max(2,10-d),1-d,'slate');
    tower(cx,-14,5,4,18); tower(cx,-14,3,24,9);
    for(let d=0;d<5;d++) box(cx-3+d>cx?cx:cx-3+d,cx+3-d<cx?cx:cx+3-d,35+d,35+d,-14-Math.max(0,3-d),-14+Math.max(0,3-d),'q');
    put(cx,40,-14,'glow');
  }
  // Sweeping colonnades frame the forecourt; bridges and cornices span between pillars.
  for(const sign of [-1,1]) {
    for(let z=5;z<=26;z+=7) column(sign*22,z,3,12,2);
    box(sign*22-1,sign*22+2,18,18,4,28,'q');box(sign*22-1,sign*22+2,17,17,4,28,'dark');
    for(let z=5;z<26;z++) for(let x=sign*22-1;x<=sign*22+2;x++) put(x,3,z,'qb');
    // Outer small satellite chapel connected to main procession.
    const cx=sign*38,cz=16;
    box(Math.min(sign*26,cx),Math.max(sign*26,cx),2,2,14,18,'q');
    disc(cx,cz,7,8,1,'black');disc(cx,cz,7,8,2,'q');
    for(const dx of [-4,3]) for(const dz of [-4,3]) column(cx+dx,cz+dz,3,9,2);
    rim(cx,cz,6,6,15,'q');rim(cx,cz,6,6,14,'prism');
    box(cx-1,cx+1,3,3,cz-1,cz+1,'dark');put(cx,4,cz,'glow');
  }
  // Overscale gate and lantern obelisks frame the approach without closing its axis.
  for(const x of [-13,11]) column(x,22,3,19,2);
  box(-13,13,24,24,22,23,'q');box(-13,13,25,25,22,23,'dark');
  for(const x of [-17,17]) for(const z of [7,29]) {box(x,x,3,6,z,z,'black');put(x,7,z,'glow');}

  // Interior pass: furniture is part of the same world-coordinate model.
  // Recessed floor border and star medallion preserve the three-block central aisle.
  for(let x=-12;x<=12;x++) for(let z=-25;z<=-1;z++) {
    let m=(Math.abs(x)===11||z===-24||z===-2)?'dark':((x+z)%5===0?'calcite':'q');
    const dx=Math.abs(x),dz=Math.abs(z+12);
    if((dx+dz===5)||(dx===dz&&dx<=3))m='copper';
    if(dx+dz<=2)m='prism';
    put(x,7,z,m);
  }
  for(let x=-1;x<=1;x++)for(let z=-19;z<=-1;z++)if(z>-7||z<-17)put(x,8,z,'carpet');
  // Remove the earlier coarse seating before constructing detailed benches.
  for(const x of [-8,7])for(let z=-17;z<=-5;z+=4)for(let dx=0;dx<2;dx++)for(let dz=0;dz<2;dz++)blocks.delete(key(x+dx,8,z+dz));
  for(const sign of [-1,1]) for(const z of [-6,-11,-16]) {
    const x0=sign<0?-8:4,x1=sign<0?-4:8;
    box(x0,x1,8,8,z,z,'seat');box(x0,x1,8,8,z+1,z+1,'qb');
    put(x0,8,z,'dark');put(x1,8,z,'dark');
  }
  // Altar dais, treasure chest, reading desk and a monumental relief behind them.
  box(-4,4,8,8,-24,-20,'dark');box(-3,3,9,9,-23,-21,'q');
  put(0,10,-21,'chest');put(-3,10,-22,'amethyst');put(3,10,-22,'amethyst');
  put(-3,11,-22,'rod');put(3,11,-22,'rod');put(4,8,-19,'lectern');
  for(let x=-5;x<=5;x++)for(let y=12;y<=22;y++) {
    const d=Math.abs(x)+Math.abs(y-17);if(d===5)put(x,y,-25,'prism');if(d===4)put(x,y,-25,'q');
  }
  put(0,17,-25,'glow');put(0,16,-25,'copper');put(0,18,-25,'copper');
  // Recessed shrine cabinets and interior pilasters frame the stained glass.
  for(const sign of [-1,1])for(const z of [-21,-13,-5]) {
    const x=sign*11;
    box(x,x,8,8,z-1,z+1,'black');box(x,x,9,12,z-1,z-1,'pillar');box(x,x,9,12,z+1,z+1,'pillar');
    box(x,x,13,13,z-1,z+1,'dark');put(x,9,z,'q');put(x,10,z,'amethyst');put(x,12,z,'glow');
  }
  // Two hanging lantern clusters: chain joins roof, bright core stays overhead.
  for(const z of [-8,-17]) {
    for(const x of [-5,5]) {box(x,x,21,30,z,z,'chain');put(x,20,z,'dark');put(x,19,z,'glow');put(x,18,z,'rod');}
    box(-5,5,21,21,z,z,'dark');put(0,20,z,'glow');
  }
  for(const x of [-9,9])for(const z of [-22,-17,-12,-7,-2]) {put(x,8,z,'black');put(x,9,z,'rod');}
  // Library west, relic room east. Carve usable door openings towards the bridge.
  for(const sign of [-1,1]) {
    const cx=sign*29;
    for(let y=4;y<=8;y++)for(let z=-15;z<=-13;z++)blocks.delete(key(cx-sign*5,y,z));
    for(let y=8;y<=12;y++)for(let z=-14;z<=-12;z++)blocks.delete(key(sign*13,y,z));
    for(let i=0;i<5;i++) box(sign*(14+i),sign*(14+i),3,7-i,-14,-12,'qb');
    for(let x=cx-4;x<=cx+4;x++)for(let z=-18;z<=-10;z++)put(x,3,z,((x+z)%3===0)?'calcite':'q');
    for(const x of [cx-3,cx+3]){put(x,3,-11,'glow');put(x,3,-17,'glow');}
    if(sign<0) {
      box(cx-4,cx+4,4,7,-18,-18,'shelf');box(cx-4,cx-4,4,7,-17,-11,'shelf');
      box(cx-1,cx+1,4,4,-15,-14,'wood');put(cx,5,-14,'lectern');
      put(cx-1,5,-15,'rod');put(cx+1,5,-15,'amethyst');
      for(const z of [-12,-17])put(cx+2,4,z,'seat');
    } else {
      for(const [dx,dz,m] of [[-3,-3,'amethyst'],[3,-3,'glass'],[-3,3,'copper'],[3,3,'prism']]) {
        put(cx+dx,4,-14+dz,'black');put(cx+dx,5,-14+dz,'q');put(cx+dx,6,-14+dz,m);put(cx+dx,7,-14+dz,'rod');
      }
      put(cx,4,-14,'dark');put(cx,5,-14,'glow');put(cx,6,-14,'amethyst');
      box(cx-2,cx+2,7,7,-18,-18,'prism');
    }
    box(cx,cx,15,20,-14,-14,'chain');put(cx,14,-14,'glow');
  }
  // Lower tower observatory: circular chart, instrument and perimeter workstations.
  box(-7,7,31,31,-20,-6,'q');disc(0,-13,5,5,31,'dark',.7);
  for(let i=-4;i<=4;i++){put(i,31,-13,'copper');put(0,31,-13+i,'prism');}
  put(0,32,-13,'black');put(0,33,-13,'amethyst');put(0,34,-13,'rod');
  box(-5,5,32,32,-19,-19,'wood');
  for(const x of [-4,0,4])put(x,33,-19,'lectern');
  for(const x of [-6,6]) {box(x,x,32,34,-16,-15,'shelf');put(x,32,-9,'glow');}
  // Forecourt furnishings remain outside the main approach corridor.
  for(const sign of [-1,1])for(const z of [10,24]) {
    box(sign*18,sign*18,3,3,z-1,z+1,'seat');
    box(sign*19,sign*19,3,3,z-1,z+1,'qb');
    box(sign*18-1,sign*18+1,3,3,z+3,z+5,'q');
    put(sign*18,4,z+4,'moss');put(sign*18,5,z+4,'leaves');
  }

  const variantAPI=window.RuinVariants;
  const baseModel=variantAPI.addAccess([...blocks.values()]);
  Object.assign(palette,variantAPI.materials);
  const variantCache=new Map([['ordinary',baseModel]]);
  let variant='ordinary',original=baseModel;
  function modelFor(id){if(!variantCache.has(id))variantCache.set(id,variantAPI.build(baseModel,id));return variantCache.get(id);}

  const rooms={
    exterior:{title:'整体外观',description:'完整建筑。选择室内预设后，自动移开屋顶与近侧墙体；所有陈设来自同一方块模型。'},
    nave:{title:'主殿 · 礼仪与藏宝',bounds:[-14,14,7,23,-27,1],wall:10,description:'中央星纹地坪、两侧六组石英座席、后方祭坛与宝箱、壁龛、末地烛立灯和双组悬灯。中央保留三格宽通道。'},
    library:{title:'西塔 · 古代档案室',bounds:[-35,-23,3,20,-20,-8],wall:5,description:'沿后墙与侧墙排列书架，中间设置阅读桌、讲台和灯具；入口朝向连接主殿的廊桥。'},
    relic:{title:'东塔 · 遗物陈列室',bounds:[23,35,3,20,-20,-8],wall:5,description:'四座展台围绕中央紫水晶核心，利用黑石台座、石英底托和末地烛区分藏品层次。'},
    observatory:{title:'天枢塔 · 观星室',bounds:[-9,9,31,43,-22,-4],wall:33,description:'圆形星图地坪、中央观测装置、讲台工作台与资料架。东侧维护梯井连接主殿。'}
  };
  function roomInfo(id){
    const data=variantAPI.interiors[variant]?.[id];
    return data?{...rooms[id],title:data[0],description:data[1]}:rooms[id];
  }
  function updateRoomDetails(){
    document.getElementById('roomDescription').textContent=roomInfo(room).description;
    document.querySelectorAll('[data-room]').forEach(b=>{b.textContent=b.dataset.room==='exterior'?'完整建筑':roomInfo(b.dataset.room).title;});
    document.querySelectorAll('[data-room-card]').forEach(card=>{const info=roomInfo(card.dataset.roomCard);card.querySelector('b').textContent=info.title;card.querySelector('p').textContent=info.description;card.querySelector('small').textContent='可使用室内导览、四向旋转与剖切查看';});
  }
  let yaw=0,zoom=1,cut=94,scheduled=false,room='exterior';
  const ctx=canvas.getContext('2d'), plan=document.getElementById('blockPlan');
  function shade(hex,f) {const n=parseInt(hex.slice(1),16);return `rgb(${Math.round((n>>16)*f)},${Math.round(((n>>8)&255)*f)},${Math.round((n&255)*f)})`;}
  function rotate(b) {let x=b.x,z=b.z;for(let i=0;i<yaw;i++){const t=x;x=-z-1;z=t;}return {...b,x,z};}
  const poly=(points,color,line)=>{ctx.beginPath();points.forEach((p,i)=>i?ctx.lineTo(...p):ctx.moveTo(...p));ctx.closePath();ctx.fillStyle=color;ctx.fill();if(line){ctx.strokeStyle=line;ctx.lineWidth=.32;ctx.stroke();}};
  function drawScene(model,W,H,offset,title) {
    ctx.save();ctx.translate(offset,0);
    ctx.beginPath();ctx.rect(0,0,W,H);ctx.clip();
    const bg=ctx.createLinearGradient(0,0,0,H);bg.addColorStop(0,'#21333f');bg.addColorStop(1,'#0b1520');ctx.fillStyle=bg;ctx.fillRect(0,0,W,H);
    const spec=rooms[room],bounds=spec.bounds;
    const source=model.filter(b=>!bounds||(b.x>=bounds[0]&&b.x<=bounds[1]&&b.y>=bounds[2]&&b.y<=bounds[3]&&b.z>=bounds[4]&&b.z<=bounds[5])).map(rotate);
    const maxX=source.reduce((m,b)=>Math.max(m,b.x),-Infinity),maxZ=source.reduce((m,b)=>Math.max(m,b.z),-Infinity);
    const list=source.filter(b=>b.y<=cut&&(!bounds||b.y<=spec.wall||(b.x<maxX-2&&b.z<maxZ-2)));
    const occupied=new Map(list.map(b=>[key(b.x,b.y,b.z),b]));
    let u0=Infinity,u1=-Infinity,v0=Infinity,v1=-Infinity;
    // A shared envelope preserves true relative scale across the ten variants.
    const frame=bounds||[-46,46,-41,94,-39,47];
    for(const x of [frame[0],frame[1]])for(const y of [frame[2],frame[3]])for(const z of [frame[4],frame[5]]){
      const b=rotate({x,y,z}),u=b.x-b.z,v=(b.x+b.z)*.5-b.y;
      u0=Math.min(u0,u-1);u1=Math.max(u1,u+1);v0=Math.min(v0,v-1);v1=Math.max(v1,v+1);
    }
    const s=Math.min((W-64)/(u1-u0),(H-75)/(v1-v0))*zoom,ox=W/2-(u0+u1)*s/2,oy=H/2-(v0+v1)*s/2;
    const p=(x,y,z)=>[ox+(x-z)*s,oy+((x+z)*.5-y)*s];
    list.sort((a,b)=>(a.x+a.z+a.y)-(b.x+b.z+b.y));
    for(const b of list) {
      const {x,y,z,m}=b,c=palette[m][0],has=(dx,dy,dz)=>{const neighbor=occupied.get(key(x+dx,y+dy,z+dz));return neighbor&&!shapes[neighbor.m]&&!shapes[m];};
      const shape=shapes[m]||{},w=shape.w||1,h=shape.h||1,x0=x+(1-w)/2,x1=x0+w,z0=z+(1-w)/2,z1=z0+w;
      const edge=s>=4?'#15232b42':null;
      if(!has(1,0,0))poly([p(x1,y,z0),p(x1,y,z1),p(x1,y+h,z1),p(x1,y+h,z0)],shade(c,.72),edge);
      if(!has(0,0,1))poly([p(x0,y,z1),p(x1,y,z1),p(x1,y+h,z1),p(x0,y+h,z1)],shade(c,.87),edge);
      if(!has(0,1,0))poly([p(x0,y+h,z0),p(x1,y+h,z0),p(x1,y+h,z1),p(x0,y+h,z1)],c,edge);
      if(m==='shelf'&&s>4)for(let i=0;i<4;i++) {
        const a=.08+i*.22,e=a+.15,color=['#587f91','#9b5955','#a89c61','#667e60'][i];
        if(!has(0,0,1))poly([p(x+a,y+.15,z1+.001),p(x+e,y+.15,z1+.001),p(x+e,y+.79,z1+.001),p(x+a,y+.79,z1+.001)],color,null);
        if(!has(1,0,0))poly([p(x1+.001,y+.15,z+a),p(x1+.001,y+.15,z+e),p(x1+.001,y+.79,z+e),p(x1+.001,y+.79,z+a)],shade(color,.8),null);
      }
      if(m==='chest')poly([p(x+.44,y+.3,z1+.001),p(x+.57,y+.3,z1+.001),p(x+.57,y+.62,z1+.001),p(x+.44,y+.62,z1+.001)],'#edce86',null);
      if(m==='pillar'&&s>4&&!has(0,0,1)) {ctx.strokeStyle='#8b99953f';ctx.lineWidth=.5;const a=p(x+.4,y,z+1),e=p(x+.4,y+1,z+1);ctx.beginPath();ctx.moveTo(...a);ctx.lineTo(...e);ctx.stroke();}
    }
    // A 1.8-block player marker gives an honest reference for the monumental scale.
    const markers={exterior:{x:3,y:3,z:37},nave:{x:0,y:8,z:-4},library:{x:-27,y:4,z:-11},relic:{x:27,y:4,z:-11},observatory:{x:3,y:32,z:-9}};
    let person=rotate(markers[room]),foot=p(person.x,person.y,person.z),head=p(person.x,person.y+1.8,person.z);
    if(cut>=3) {ctx.strokeStyle='#efbc79';ctx.lineWidth=Math.max(2,s*.45);ctx.beginPath();ctx.moveTo(...foot);ctx.lineTo(...head);ctx.stroke();ctx.fillStyle='#efbc79';ctx.font='11px Microsoft YaHei';ctx.fillText('玩家 1.8 格',foot[0]+8,foot[1]+12);}
    ctx.fillStyle='#b2c5cf';ctx.font='11px Microsoft YaHei';ctx.fillText('每格 = 1 方块 · 材质为近似配色',18,H-18);
    ctx.fillStyle='#e1efea';ctx.font='14px Microsoft YaHei';ctx.fillText(title,18,25);
    ctx.restore();return list.length;
  }
  function draw(){
    scheduled=false;const rect=canvas.getBoundingClientRect();if(!rect.width||!rect.height)return;
    const W=rect.width,H=rect.height,dpr=Math.min(2,devicePixelRatio||1);
    canvas.width=W*dpr;canvas.height=H*dpr;ctx.setTransform(dpr,0,0,dpr,0,0);
    const def=variantAPI.definitions.find(v=>v.id===variant);
    const compare=document.getElementById('compareVariant').checked&&variant!=='ordinary';
    if(compare){drawScene(baseModel,W/2,H,0,'普通遗迹 · 同尺度');drawScene(original,W/2,H,W/2,def.name+' · '+def.subtitle);ctx.fillStyle='#3d5663';ctx.fillRect(W/2,0,1,H);}
    else drawScene(original,W,H,0,def.name+' · '+def.subtitle);
    document.getElementById('modelStats').textContent=def.name+' · '+original.length.toLocaleString()+' 个方块 · '+roomInfo(room).title;
    drawPlan();
  }
  function drawPlan() {
    if(!plan)return;const rect=plan.getBoundingClientRect();if(!rect.width)return;
    const c=plan.getContext('2d'),dpr=Math.min(2,devicePixelRatio||1),W=rect.width,H=rect.height;
    plan.width=W*dpr;plan.height=H*dpr;c.setTransform(dpr,0,0,dpr,0,0);c.fillStyle='#0d1924';c.fillRect(0,0,W,H);
    const cols=new Map();for(const b of original)if(b.y>=2&&b.y<=(Math.abs(b.x)<15?10:7)){const k=`${b.x},${b.z}`;if(!cols.has(k)||cols.get(k).y<b.y)cols.set(k,b);}
    const scale=Math.min((W-40)/96,(H-60)/96),ox=W/2,oy=H/2-6*scale;
    for(const b of cols.values()){c.fillStyle=palette[b.m][0];c.fillRect(ox+b.x*scale,oy+b.z*scale,Math.ceil(scale),Math.ceil(scale));}
    c.strokeStyle='#7ef2c7';c.lineWidth=2;c.setLineDash([5,4]);c.beginPath();c.moveTo(ox,oy+44*scale);c.lineTo(ox,oy+4*scale);c.lineTo(ox,oy-20*scale);c.stroke();c.setLineDash([]);
    c.font='12px Microsoft YaHei';c.textAlign='center';c.fillStyle='#eef6f1';for(const [t,x,z] of [['祭坛与宝箱',0,-23],[roomInfo('library').title,-29,-14],[roomInfo('relic').title,29,-14],['仪式前庭',0,15],['入口平台',0,43]])c.fillText(t,ox+x*scale,oy+z*scale);
  }
  function schedule(){if(!scheduled){scheduled=true;requestAnimationFrame(draw);}}
  document.getElementById('rotateModel').onclick=()=>{yaw=(yaw+1)%4;schedule();};
  document.getElementById('zoomModel').oninput=e=>{zoom=+e.target.value;schedule();};
  document.getElementById('sliceModel').oninput=e=>{cut=+e.target.value;document.getElementById('sliceLabel').textContent=cut>=94?'完整外观':`显示至第 ${cut} 层`;schedule();};
  const baseMaterialKeys=new Set(baseModel.map(block=>block.m));
  function updateMaterials(def){
    const counts=new Map();
    for(const block of original) counts.set(block.m,(counts.get(block.m)||0)+1);
    const board=document.getElementById('variantMaterialBoard');
    board.replaceChildren();
    for(const [material,count] of [...counts].sort((a,b)=>b[1]-a[1]||a[0].localeCompare(b[0]))){
      const [color,name]=palette[material];
      const card=document.createElement('div');card.className='material-card';
      const swatch=document.createElement('div');swatch.className='blockswatch';swatch.style.backgroundColor=color;
      const title=document.createElement('b');title.textContent=name;
      const amount=document.createElement('span');amount.textContent=count.toLocaleString('zh-CN')+' 件 · '+(count/original.length*100).toFixed(2)+'%';
      const origin=document.createElement('span');
      origin.textContent=variant==='ordinary'?'普通母体材料':baseMaterialKeys.has(material)?'与母体共用材料':'变体新增';
      if(!baseMaterialKeys.has(material)) {origin.style.color=def.accent;card.style.borderColor=def.accent;}
      card.append(swatch,title,amount,origin);board.append(card);
    }
    document.getElementById('materialTitle').textContent=def.name+' · 方块材料表 · Java 1.20.1';
    document.getElementById('materialSummary').textContent='完整建筑与内饰：'+counts.size+' 种材料 / '+original.length.toLocaleString('zh-CN')+' 个放置单元。'+(variant==='ordinary'?'普通母体基准。':'描边卡片为此变体相对普通母体新增的材料。');
  }
  function selectVariant(id){
    const def=variantAPI.definitions.find(v=>v.id===id)||variantAPI.definitions[0];
    variant=def.id;original=modelFor(variant);
    document.getElementById('variantName').textContent=def.name+' · '+def.subtitle;
    document.getElementById('variantShape').textContent=def.shape;
    document.getElementById('variantMaterials').textContent=def.materials;
    document.getElementById('variantInfo').style.borderColor=def.accent;
    document.getElementById('compareVariant').disabled=variant==='ordinary';
    document.querySelectorAll('[data-variant]').forEach(b=>b.classList.toggle('selected',b.dataset.variant===variant));
    document.getElementById('modelMaterialLegend').textContent='共同主体：石英砖、深板岩浮岛　｜　当前主题：'+def.materials;
    updateRoomDetails();
    updateMaterials(def);
    window.RuinBlueprint?.setVariant(variant,original,palette);
    schedule();
  }
  document.querySelectorAll('[data-variant]').forEach(b=>b.addEventListener('click',()=>{
    selectVariant(b.dataset.variant);
    if(b.classList.contains('variant-launch')){
      document.querySelectorAll('.tab,.view').forEach(x=>x.classList.remove('active'));
      document.querySelector('[data-view="overall"]').classList.add('active');document.getElementById('overall').classList.add('active');
      selectRoom('exterior');document.querySelector('.model-toolbar').scrollIntoView({behavior:'smooth',block:'start'});
    }
  }));
  document.getElementById('compareVariant').onchange=schedule;
  function selectRoom(value){
    room=rooms[value]?value:'exterior';zoom=1;yaw=0;cut=rooms[room].bounds?rooms[room].bounds[3]:94;
    document.getElementById('zoomModel').value=1;
    const slider=document.getElementById('sliceModel');slider.min=rooms[room].bounds?rooms[room].bounds[2]:2;slider.max=rooms[room].bounds?rooms[room].bounds[3]:94;slider.value=cut;
    document.getElementById('sliceLabel').textContent=room==='exterior'?'完整外观':`显示至第 ${cut} 层`;
    updateRoomDetails();
    document.querySelectorAll('[data-room]').forEach(b=>b.classList.toggle('selected',b.dataset.room===room));schedule();
  }
  document.querySelectorAll('[data-room]').forEach(b=>b.addEventListener('click',()=>selectRoom(b.dataset.room)));
  document.getElementById('resetModel').onclick=()=>selectRoom('exterior');
  document.querySelectorAll('.tab').forEach(b=>b.addEventListener('click',()=>{document.querySelectorAll('.tab,.view').forEach(x=>x.classList.remove('active'));b.classList.add('active');document.getElementById(b.dataset.view).classList.add('active');schedule();}));
  window.RuinDesign={getVariant:()=>variant,getModel:()=>original,getModelFor:modelFor,getPalette:()=>palette,selectVariant};
  window.addEventListener('resize',schedule);const params=new URLSearchParams(location.search);document.getElementById('compareVariant').checked=params.get('compare')==='1';selectVariant(params.get('variant')||'ordinary');selectRoom(params.get('room')||'exterior');
  const initialView=params.get('view');if(initialView&&document.getElementById(initialView)&&document.querySelector(`[data-view="${initialView}"]`))document.querySelector(`[data-view="${initialView}"]`).click();
})();
