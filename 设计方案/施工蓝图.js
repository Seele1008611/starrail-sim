// 项目中文说明：分层蓝图交互脚本：按高度、区域和材质筛选遗迹方块，并绘制对应剖面。

/* Exact horizontal slices of the current voxel design. Coordinates are local to the ruin. */
(() => {
  const design=window.RuinDesign;
  const variants=window.RuinVariants.definitions;
  const $=id=>document.getElementById(id);
  const regions={
    all:{name:'完整建筑',x0:-45,x1:45,z0:-37,z1:46,cell:10},
    nave:{name:'主殿与祭坛',x0:-15,x1:15,z0:-28,z1:2,cell:22},
    west:{name:'西侧功能室',x0:-36,x1:-22,z0:-21,z1:-7,cell:28},
    east:{name:'东侧陈列室',x0:22,x1:36,z0:-21,z1:-7,cell:28},
    tower:{name:'高层观星室',x0:-10,x1:10,z0:-23,z1:-3,cell:25},
    approach:{name:'入口与前庭',x0:-19,x1:19,z0:4,z1:46,cell:15}
  };
  const ids={
    q:'smooth_quartz',qb:'quartz_bricks',pillar:'quartz_pillar',calcite:'calcite',
    prism:'prismarine_bricks',dark:'dark_prismarine',black:'polished_blackstone_bricks',
    slate:'deepslate_tiles',glow:'sea_lantern',glass:'cyan_stained_glass',
    amethyst:'amethyst_block',copper:'waxed_cut_copper',chest:'chest',
    seat:'smooth_quartz_slab',carpet:'cyan_carpet',shelf:'bookshelf',wood:'spruce_planks',
    lectern:'lectern',chain:'chain',rod:'end_rod',moss:'moss_block',leaves:'azalea_leaves',
    ladder:'ladder',redbrick:'red_nether_bricks',redglass:'red_stained_glass',magma:'magma_block',
    oxidized:'oxidized_cut_copper',emerald:'emerald_block',lapis:'lapis_block',
    blueglass:'blue_stained_glass',pink:'pink_terracotta',magentaglass:'magenta_stained_glass',
    crying:'crying_obsidian',obsidian:'obsidian',purpleglass:'purple_stained_glass',
    andesite:'polished_andesite',blueice:'blue_ice',iron:'iron_block',
    greenglass:'green_stained_glass',packedice:'packed_ice',lightblueglass:'light_blue_stained_glass',
    orange:'orange_terracotta',yellow:'yellow_terracotta'
  };
  const canvas=$('blueprintCanvas'),ctx=canvas.getContext('2d');
  const select=$('blueprintVariant');
  for(const def of variants){
    const option=document.createElement('option');option.value=def.id;
    option.textContent=def.name+' · '+def.subtitle;select.append(option);
  }
  let variant='ordinary',model=[],palette={},index=new Map(),levels=[],y=7,focusMat=null;
  const region=()=>regions[$('blueprintRegion').value]||regions.all;
  const inside=(b,r)=>b.x>=r.x0&&b.x<=r.x1&&b.z>=r.z0&&b.z<=r.z1;
  const layer=()=>index.get(y)||new Map();
  const key=(x,z)=>`${x},${z}`;
  const nearest=value=>levels.reduce((a,b)=>Math.abs(b-value)<Math.abs(a-value)?b:a,levels[0]);
  const origin=()=>['originX','originY','originZ'].map(id=>Number($(id).value)||0);
  function stage(y){
    if(y<=-35)return '能量底核 / 浮岛尖端';
    if(y<0)return '浮岛下部与放射支撑';
    if(y<=2)return '主平台、入口平台与岛面';
    if(y<=6)return '前庭、侧殿基座与主殿阶梯';
    if(y<=10)return '主殿地坪、祭坛、宝箱与侧室陈设';
    if(y<=23)return '主殿墙体、柱廊、窗与内饰';
    if(y<=30)return '主殿屋顶与中央塔过渡';
    if(y<=43)return '高层观星室与侧塔';
    if(y<=68)return '中央塔上层与塔冠支架';
    return '命途塔冠 / 最高装饰';
  }
  function setY(next){
    y=nearest(Number(next));
    $('blueprintY').value=y;
    $('blueprintLayer').value=levels.indexOf(y);
    render();
  }
  function setVariant(id,data,colors){
    variant=id;model=data;palette=colors;
    index=new Map();
    for(const b of model){
      if(!index.has(b.y))index.set(b.y,new Map());
      index.get(b.y).set(key(b.x,b.z),b);
    }
    levels=[...index.keys()].sort((a,b)=>a-b);
    select.value=id;focusMat=null;
    $('blueprintLayer').min=0;$('blueprintLayer').max=levels.length-1;
    $('blueprintY').min=levels[0];$('blueprintY').max=levels.at(-1);
    const shortcuts=$('blueprintLevels');shortcuts.replaceChildren();
    for(const target of [-41,-34,-1,0,2,3,7,8,10,24,31,32,44,59,69,81,93]){
      const actual=nearest(target);
      if(shortcuts.querySelector(`[data-y="${actual}"]`))continue;
      const button=document.createElement('button');button.type='button';
      button.dataset.y=actual;button.textContent=`Y=${actual}`;
      button.onclick=()=>setY(actual);shortcuts.append(button);
    }
    setY(y<levels[0]||y>levels.at(-1)?7:y);
  }
  function render(){
    const r=region(),size=r.cell,left=53,top=35;
    const width=left+(r.x1-r.x0+1)*size+17,height=top+(r.z1-r.z0+1)*size+23;
    canvas.width=width;canvas.height=height;
    ctx.fillStyle='#0b1722';ctx.fillRect(0,0,width,height);
    const cells=layer(),counts=new Map();let shown=0;
    for(let z=r.z0;z<=r.z1;z++)for(let x=r.x0;x<=r.x1;x++){
      const b=cells.get(key(x,z));if(!b)continue;
      const color=palette[b.m]?.[0]||'#ff00ff';
      const px=left+(x-r.x0)*size,pz=top+(z-r.z0)*size;
      ctx.fillStyle=color;ctx.fillRect(px+.5,pz+.5,size-1,size-1);
      if(focusMat&&b.m!==focusMat){ctx.fillStyle='#09141dc4';ctx.fillRect(px,pz,size,size);}
      if(b.m==='chest'||b.m==='ladder'){
        ctx.strokeStyle=b.m==='chest'?'#ffe08b':'#e6ba7d';ctx.lineWidth=1.4;
        ctx.strokeRect(px+2,pz+2,size-4,size-4);
      }
      counts.set(b.m,(counts.get(b.m)||0)+1);shown++;
    }
    ctx.beginPath();
    for(let x=r.x0;x<=r.x1+1;x++){
      const px=left+(x-r.x0)*size+.5;
      ctx.moveTo(px,top);ctx.lineTo(px,height-23);
    }
    for(let z=r.z0;z<=r.z1+1;z++){
      const pz=top+(z-r.z0)*size+.5;
      ctx.moveTo(left,pz);ctx.lineTo(width-17,pz);
    }
    ctx.strokeStyle='#ffffff15';ctx.lineWidth=.5;ctx.stroke();
    ctx.strokeStyle='#a1d5ce8a';ctx.lineWidth=1;
    ctx.beginPath();
    for(let x=r.x0;x<=r.x1+1;x++)if(x%10===0){const px=left+(x-r.x0)*size+.5;ctx.moveTo(px,top);ctx.lineTo(px,height-23);}
    for(let z=r.z0;z<=r.z1+1;z++)if(z%10===0){const pz=top+(z-r.z0)*size+.5;ctx.moveTo(left,pz);ctx.lineTo(width-17,pz);}
    ctx.stroke();
    ctx.fillStyle='#b8d5d3';ctx.font='11px Microsoft YaHei, sans-serif';
    ctx.textAlign='center';
    for(let x=r.x0;x<=r.x1;x++)if(x%5===0)ctx.fillText(x,left+(x-r.x0+.5)*size,top-10);
    ctx.textAlign='right';
    for(let z=r.z0;z<=r.z1;z++)if(z%5===0)ctx.fillText(z,left-7,top+(z-r.z0+.7)*size);
    ctx.fillStyle='#76d5c3';ctx.fillText('Z ↓',left-7,top-19);
    ctx.textAlign='center';ctx.fillText('X →',left+(r.x1-r.x0+1)*size/2,height-5);
    const def=variants.find(v=>v.id===variant);
    $('blueprintHeading').textContent=`${def.name} · Y=${y} · ${r.name}`;
    $('blueprintStage').textContent=`${stage(y)}。当前区域 ${shown.toLocaleString('zh-CN')} 格；完整本层 ${cells.size.toLocaleString('zh-CN')} 格。`;
    $('blueprintLevelInfo').textContent=`Y=${y} / ${levels[0]}…${levels.at(-1)} · 共 ${levels.length} 个有方块的层`;
    $('blueprintLevels').querySelectorAll('button').forEach(b=>b.classList.toggle('selected',Number(b.dataset.y)===y));
    const legend=$('blueprintLegend');legend.replaceChildren();
    for(const [m,count] of [...counts].sort((a,b)=>b[1]-a[1])){
      const button=document.createElement('button');button.type='button';button.classList.toggle('selected',focusMat===m);
      const swatch=document.createElement('i');swatch.style.backgroundColor=palette[m][0];
      const label=document.createElement('span');label.textContent=palette[m][1];
      const amount=document.createElement('em');amount.textContent=count.toLocaleString('zh-CN');
      button.append(swatch,label,amount);
      button.onclick=()=>{focusMat=focusMat===m?null:m;render();};legend.append(button);
    }
    if(!shown)legend.textContent='该区域本层没有方块。';
  }
  function inspect(event){
    const r=region(),rect=canvas.getBoundingClientRect();
    const px=(event.clientX-rect.left)*canvas.width/rect.width;
    const pz=(event.clientY-rect.top)*canvas.height/rect.height;
    const x=r.x0+Math.floor((px-53)/r.cell),z=r.z0+Math.floor((pz-35)/r.cell);
    if(x<r.x0||x>r.x1||z<r.z0||z>r.z1)return;
    const block=layer().get(key(x,z)),[ox,oy,oz]=origin();
    $('blueprintHover').textContent=`局部 (${x}, ${y}, ${z}) → 世界 (${ox+x}, ${oy+y}, ${oz+z})：${block?palette[block.m][1]+' · minecraft:'+ids[block.m]:'空位'}`;
  }
  function csvCell(value){return '"'+String(value).replaceAll('"','""')+'"';}
  function exportCsv(all){
    const r=region(),cells=all?model:model.filter(b=>b.y===y&&inside(b,r));
    const [ox,oy,oz]=origin();
    const sorted=[...cells].sort((a,b)=>a.y-b.y||a.z-b.z||a.x-b.x);
    const rows=['world_x,world_y,world_z,local_x,local_y,local_z,block_id,block_state,material_cn'];
    for(const b of sorted){
      const state=b.m==='ladder'?'facing=west':b.m==='seat'?'type=bottom':'';
      rows.push([ox+b.x,oy+b.y,oz+b.z,b.x,b.y,b.z,'minecraft:'+ids[b.m],state,palette[b.m][1]].map(csvCell).join(','));
    }
    const blob=new Blob(['\uFEFF',rows.join('\r\n')],{type:'text/csv;charset=utf-8'});
    const url=URL.createObjectURL(blob),link=document.createElement('a');
    link.href=url;link.download=`floating_ruin_${variant}_${all?'all':$('blueprintRegion').value+'_y'+y}.csv`;
    document.body.append(link);link.click();link.remove();setTimeout(()=>URL.revokeObjectURL(url),1000);
  }
  $('blueprintRegion').onchange=render;
  $('blueprintVariant').onchange=e=>design.selectVariant(e.target.value);
  $('blueprintLayer').oninput=e=>setY(levels[Number(e.target.value)]);
  $('blueprintY').onchange=e=>setY(e.target.value);
  $('blueprintPrev').onclick=()=>setY(levels[Math.max(0,levels.indexOf(y)-1)]);
  $('blueprintNext').onclick=()=>setY(levels[Math.min(levels.length-1,levels.indexOf(y)+1)]);
  $('blueprintExportLayer').onclick=()=>exportCsv(false);
  $('blueprintExportAll').onclick=()=>exportCsv(true);
  for(const id of ['originX','originY','originZ'])$(id).onchange=()=>{$('blueprintHover').textContent='世界锚点已更新；将鼠标移到色格上可读取新坐标。';};
  canvas.onmousemove=inspect;canvas.onclick=inspect;
  window.RuinBlueprint={setVariant};
  setVariant(design.getVariant(),design.getModel(),design.getPalette());
})();
