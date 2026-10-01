// 项目中文说明：九种命途遗迹变体的主题差异、调色板和模型生成逻辑。

/* Shape studies on the shared ordinary ruin. All materials are vanilla 1.20.1 blocks. */
window.RuinVariants = (() => {
  const definitions = [
    {id:'ordinary',name:'普通遗迹',subtitle:'天枢圣所',accent:'#79cdbc',shape:'石质冠环 · 双塔与长柱廊',materials:'石英砖 / 暗海晶石 / 海晶灯',map:{}},
    {id:'destruction',name:'毁灭',subtitle:'裂焰圣所',accent:'#df806a',shape:'向外张开的双角塔冠 · 斜向巨型扶壁 · 裂焰门楣',materials:'红色下界砖 / 红色染色玻璃 / 岩浆块',map:{prism:'redbrick',dark:'redbrick',glass:'redglass',amethyst:'magma'}},
    {id:'hunt',name:'巡猎',subtitle:'逐星箭庭',accent:'#87c9ab',shape:'弓弦塔冠与长箭 · 针状侧塔 · 箭纹入口',materials:'氧化的切制铜块 / 青色染色玻璃 / 绿宝石块',map:{prism:'oxidized',dark:'dark',glass:'glass',amethyst:'emerald'}},
    {id:'erudition',name:'智识',subtitle:'天体演算院',accent:'#999fe7',shape:'交叉天球环 · 双侧观测仪 · 演算核心',materials:'青金石块 / 紫水晶块 / 蓝色染色玻璃',map:{prism:'lapis',dark:'lapis',glass:'blueglass',amethyst:'amethyst'}},
    {id:'harmony',name:'同谐',subtitle:'共鸣圣堂',accent:'#ddacd2',shape:'三重音管塔冠 · 对应钟架 · 节律门楣',materials:'粉红色陶瓦 / 品红色染色玻璃 / 海晶灯',map:{prism:'pink',dark:'pink',glass:'magentaglass',amethyst:'amethyst'}},
    {id:'nihility',name:'虚无',subtitle:'寂静空庭',accent:'#ab8cda',shape:'偏心断环 · 暗色悬石 · 留空的塔冠中心',materials:'哭泣的黑曜石 / 黑曜石 / 紫色染色玻璃',map:{prism:'crying',dark:'obsidian',glass:'purpleglass',amethyst:'obsidian'}},
    {id:'preservation',name:'存护',subtitle:'晶盾堡垒',accent:'#98bed9',shape:'巨型盾徽 · 层叠护壁 · 厚重门楼',materials:'磨制安山岩 / 蓝冰 / 铁块',map:{prism:'andesite',dark:'slate',glass:'blueice',amethyst:'iron'}},
    {id:'abundance',name:'丰饶',subtitle:'生息神苑',accent:'#9dcc85',shape:'生命树式塔冠 · 枝形支架 · 侧翼花园',materials:'苔藓块 / 杜鹃树叶 / 绿色染色玻璃',map:{prism:'moss',dark:'dark',glass:'greenglass',amethyst:'emerald'}},
    {id:'remembrance',name:'记忆',subtitle:'镜映圣所',accent:'#b0e5ed',shape:'三层记忆门框 · 冰晶侧冠 · 冰镜入口',materials:'浮冰 / 蓝冰 / 淡蓝色染色玻璃',map:{prism:'packedice',dark:'blueice',glass:'lightblueglass',amethyst:'blueice'}},
    {id:'elation',name:'欢愉',subtitle:'奇想剧场',accent:'#efb19c',shape:'面具塔冠 · 高低错位彩饰 · 半环剧场',materials:'橙色陶瓦 / 粉红色陶瓦 / 品红色染色玻璃',map:{prism:'orange',dark:'pink',glass:'magentaglass',amethyst:'yellow'}}
  ];
  const materials={
    redbrick:['#743735','红色下界砖'],redglass:['#b85852','红色染色玻璃'],magma:['#de833e','岩浆块'],
    oxidized:['#5aab89','氧化的切制铜块'],emerald:['#69b887','绿宝石块'],lapis:['#455f9a','青金石块'],
    blueglass:['#778eca','蓝色染色玻璃'],pink:['#b88688','粉红色陶瓦'],magentaglass:['#cb96c8','品红色染色玻璃'],
    crying:['#635080','哭泣的黑曜石'],obsidian:['#363140','黑曜石'],purpleglass:['#8c76b1','紫色染色玻璃'],
    andesite:['#929c9e','磨制安山岩'],blueice:['#79afdc','蓝冰'],iron:['#e4e7e7','铁块'],
    greenglass:['#8fb77a','绿色染色玻璃'],packedice:['#9cbcd8','浮冰'],lightblueglass:['#b2deeb','淡蓝色染色玻璃'],
    orange:['#bd876b','橙色陶瓦'],yellow:['#d6b65f','黄色陶瓦']
  };
  // A shared vertical access shaft connects the nave floor to the observatory.
  function addAccess(data){
    const map=new Map(data.map(b=>[`${b.x},${b.y},${b.z}`,b]));
    for(let y=8;y<=10;y++)for(const z of [-13,-12])map.delete(`6,${y},${z}`);
    for(let y=8;y<=31;y++){
      map.delete(`7,${y},-13`);
      map.set(`8,${y},-13`,{x:8,y,z:-13,m:'q'});
      map.set(`7,${y},-13`,{x:7,y,z:-13,m:'ladder'});
    }
    return [...map.values()];
  }
  function build(base,id) {
    const def=definitions.find(d=>d.id===id)||definitions[0];
    if(def.id==='ordinary')return base;
    const key=(x,y,z)=>`${x},${y},${z}`;
    const map=new Map(base.filter(b=>b.y<69).map(b=>[key(b.x,b.y,b.z),def.map[b.m]?{...b,m:def.map[b.m]}:b]));
    function put(x,y,z,m='q'){x=Math.round(x);y=Math.round(y);z=Math.round(z);map.set(key(x,y,z),{x,y,z,m});}
    function box(x0,x1,y0,y1,z0,z1,m='q'){for(let x=x0;x<=x1;x++)for(let y=y0;y<=y1;y++)for(let z=z0;z<=z1;z++)put(x,y,z,m);}
    function line(a,b,m='q',w=0){const n=Math.max(...a.map((v,i)=>Math.abs(v-b[i])));for(let t=0;t<=n;t++){const p=a.map((v,i)=>Math.round(v+(b[i]-v)*(n?t/n:0)));box(p[0]-w,p[0]+w,p[1],p[1],p[2],p[2],m);}}
    function ring(cx,cy,cz,r,plane,m,thickness=1.4,gap=false){for(let a=-r-1;a<=r+1;a++)for(let b=-r-1;b<=r+1;b++){const d=Math.hypot(a,b);if(d>r||d<r-thickness||(gap&&a>3&&b>4))continue;if(plane==='yz')put(cx,cy+a,cz+b,m);else put(cx+a,cy+(plane==='xz'?0:b),cz+(plane==='xz'?b:0),m);}}
    function crystal(cx,y,cz,h,m){for(let d=0;d<h;d++){const w=d<h-3?1:0;box(cx-w,cx+w,y+d,y+d,cz-w,cz+w,m);}put(cx,y+h,cz,'glow');}
    function leafCluster(cx,y,cz,r){for(let x=-r;x<=r;x++)for(let z=-r;z<=r;z++)if(x*x+z*z<=r*r)put(cx+x,y,cz+z,'leaves');}
    switch(id){
      case 'destruction':
        for(const sign of [-1,1]){
          for(let d=0;d<20;d++){const x=sign*(3+Math.floor(d*.5));box(x-1,x+1,69+d,69+d,-14,-12,d%4===0?'magma':'redbrick');put(x,69+d,-11,'q');}
          for(const z of [-22,-4])line([sign*20,4,z],[sign*14,27,z],'redbrick',1);
          line([sign*12,26,22],[sign*3,31,22],'redbrick',1);
        }
        crystal(0,70,-13,9,'redglass');break;
      case 'hunt':
        for(let y=68;y<=92;y++){const t=(y-80)/12,x=-Math.round(12*Math.sqrt(Math.max(0,1-t*t)));box(x-1,x,y,y,-14,-13,'oxidized');put(0,y,-13,'q');}
        line([-5,80,-13],[18,80,-13],'q');line([13,76,-13],[19,80,-13],'oxidized');line([13,84,-13],[19,80,-13],'oxidized');
        for(const s of [-1,1]){crystal(s*29,40,-14,10,'oxidized');line([s*8,25,22],[0,30,22],'oxidized');}break;
      case 'erudition':
        ring(0,79,-13,12,'xy','lapis',1.8);ring(0,79,-13,10,'yz','q',1.5);crystal(0,74,-13,8,'amethyst');
        for(const s of [-1,1]){ring(s*38,20,16,6,'xy','lapis');ring(s*38,20,16,5,'xz','q');put(s*38,20,16,'glow');}break;
      case 'harmony':
        for(const [cx,h] of [[-8,84],[0,92],[8,84]]){
          box(cx-2,cx-2,69,h-1,-13,-13,'q');box(cx+2,cx+2,69,h-1,-13,-13,'q');box(cx-2,cx+2,h,h,-13,-13,'pink');box(cx,cx,h-7,h-1,-13,-13,'rod');put(cx,h-8,-13,'glow');
        }
        for(const s of [-1,1])for(let d=0;d<4;d++)box(s*(6+d),s*(6+d),15,20+d,1,1,'pink');
        for(const s of [-1,1]){line([s*38-5,15,16],[s*38,21,16],'q');line([s*38,21,16],[s*38+5,15,16],'q');}break;
      case 'nihility':
        ring(-2,79,-13,13,'xy','obsidian',2,true);ring(8,78,-11,7,'xy','crying',1.5,true);
        for(const [x,z,y] of [[-40,-5,10],[-37,30,12],[40,29,9],[29,36,6]]){box(x,x+1,y,y+2,z,z+1,'obsidian');put(x,y+3,z,'crying');}
        box(-6,6,25,25,22,23,'crying');break;
      case 'preservation':
        for(let y=69;y<=89;y++){const w=y<79?Math.floor((y-69)*.9):9;for(const x of [-w,w])box(x-1,x, y,y,-14,-13,'iron');}
        box(-9,9,89,90,-14,-13,'andesite');box(-1,1,75,86,-14,-13,'blueice');box(-6,6,82,83,-14,-13,'blueice');
        for(const s of [-1,1]){for(const z of [-22,-6]){box(s*16-1,s*16+1,3,21,z-1,z+1,'andesite');line([s*16,21,z],[s*12,28,z],'q',1);}box(s*15-2,s*15+2,3,15,21,25,'andesite');box(s*15-2,s*15+2,16,16,21,25,'iron');}break;
      case 'abundance':
        box(-1,1,69,83,-14,-12,'pillar');
        for(const [x,z,y] of [[-10,-14,84],[10,-12,86],[-6,-21,82],[6,-5,85],[0,-13,91]]){
          line([0,74,-13],[x,y,z],'q',1);leafCluster(x,y+1,z,4);leafCluster(x,y+2,z,3);put(x,y,z,'glow');
        }
        for(const s of [-1,1]){for(const x of [s*38-4,s*38+4])for(let y=8;y<=14;y++)put(x,y,12,'leaves');for(let x=s*38-2;x<=s*38+2;x++)box(x,x,3,3,19,21,'moss');}break;
      case 'remembrance':
        for(let i=0;i<3;i++){const r=6+i*3,z=-20+i*7;box(-r,-r,69,80+i*2,z,z,'blueice');box(r,r,69,80+i*2,z,z,'blueice');line([-r,80+i*2,z],[0,86+i*2,z],'packedice');line([0,86+i*2,z],[r,80+i*2,z],'packedice');put(0,84+i*2,z,'glow');}
        for(const s of [-1,1]){crystal(s*38,15,16,9,'blueice');crystal(s*29,40,-14,7,'packedice');}
        line([-9,26,22],[0,33,22],'packedice');line([0,33,22],[9,26,22],'packedice');break;
      case 'elation':
        for(let y=70;y<=87;y++){const w=y<75?5:8;for(let x=-w;x<=w;x++){
          if(y>=80&&y<=82&&Math.abs(x)>=3&&Math.abs(x)<=5)continue;
          const mouth=y===75&&Math.abs(x)<=3||y===76&&Math.abs(x)===4;
          put(x,y,-13,mouth?'pink':(Math.abs(x)===w?'orange':'q'));
        }}
        crystal(-10,83,-13,8,'yellow');crystal(10,78,-13,6,'pink');
        for(let i=0;i<3;i++)for(let x=-5-i;x<=5+i;x++)put(x-14,3+i,17+i,'seat');
        for(const [x,y,m] of [[-16,28,'pink'],[-10,30,'yellow'],[9,28,'orange']])box(x,x+2,y,y+2,22,23,m);break;
    }
    window.RuinInteriors.build(map,id);
    return addAccess([...map.values()]);
  }
  return {definitions,materials,build,addAccess,interiors:window.RuinInteriors.descriptions};
})();
