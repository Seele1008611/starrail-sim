// 项目中文说明：九种命途变体的内饰模型数据和室内布局规则。

/* Interior layouts share circulation and chest coordinates with the ordinary ruin. */
window.RuinInteriors=(()=>{
  const descriptions={
    destruction:{nave:['裂焰祭殿','双列封闭熔光炉、锯齿地纹和向外张开的祭坛支架；中央保留三格通道。'],library:['熔核档案室','隔热书柜、红砖工作台和封装样本柜。'],relic:['裂焰核心室','三座封闭能源台与悬挂的熔光核心。'],observatory:['冲击观测台','交错的冲击环、分向刻度与侧置观测台。']},
    hunt:{nave:['逐星箭堂','连续箭纹引向宝箱，两侧是观测座席，祭坛背后立起弓形框架。'],library:['寻迹图室','并列讲台、路线档案和氧化铜测绘桌。'],relic:['弓弦陈列室','弓形展架、双侧箭簇陈列台与定向灯。'],observatory:['远星瞭望室','斜向望远装置、罗盘地坪和两侧读数台。']},
    erudition:{nave:['演算圣堂','交叉星仪、四座演算讲台、编码地坪和环形灯架。'],library:['星图档案库','密集书架围绕成组查阅桌，暖木家具与青金石仪器形成分区。'],relic:['演算核心室','交叉小型天球仪与两座数据展台。'],observatory:['天体演算室','正交天球环、紫水晶核心和四角记录台。']},
    harmony:{nave:['共鸣合奏厅','左右音管墙、成对合唱座席与悬挂共鸣环围绕中央祭坛。'],library:['乐谱档案室','乐谱架与并排讲台组成合奏工作台。'],relic:['谐振器陈列室','高低不同的垂挂音柱，双侧底座展示共鸣材料。'],observatory:['星声聆听室','四组音柱、中心共鸣圆盘与面向星空的记录台。']},
    nihility:{nave:['静默空庭','低矮独坐席、悬石与偏心断环，中央留出大面积安静空间。'],library:['遗忘档案室','分散的残存书柜和独立阅读台，让留白成为空间主体。'],relic:['无声陈列室','一枚悬浮晶体与两座空置展台。'],observatory:['无相观测室','偏心空环与低亮边灯，保留通往窗前的空地。']},
    preservation:{nave:['守护圣堂','成列壁垒、盾徽背墙、护栏式座席和厚重藏宝基座。'],library:['守望档案室','加厚柜体与铁饰查阅台，档案收纳沿墙展开。'],relic:['晶盾陈列室','三座盾形展架展示铁与蓝冰构成的守护核心。'],observatory:['防线观测室','方环地坪、分区观测桌和中央晶盾模型。']},
    abundance:{nave:['生息祈愿庭','树形祭坛、两侧绿植坛、枝形灯架和苔藓镶边；植物避开主通道。'],library:['种源档案室','资料柜、种源陈列与绿植工作台。'],relic:['生命核心室','树形生息核心、幼株展台与温和灯光。'],observatory:['空中育生室','小型生命树与环绕工作台，四角保留观景空间。']},
    remembrance:{nave:['记忆回廊','三重冰镜门框、冰晶藏品与镜面地纹，形成层层回望的纵深。'],library:['记忆档案室','蓝冰柜架与封存晶体，资料桌保留温暖石质底座。'],relic:['往昔陈列室','多组冰镜展屏沿墙陈列，中央留作观看空间。'],observatory:['回映观测室','重复门框与中心记忆晶体，窗前设置记录台。']},
    elation:{nave:['奇想剧场','面具舞台、错落彩色幕柱和两侧观演座席，宝箱位于舞台中央。'],library:['剧本与道具室','剧本柜、彩色道具格与工作讲台。'],relic:['面具陈列室','三组不同表情与配色的面具展架。'],observatory:['星空小剧场','小型阶梯看台、面具背景与彩色悬饰。']}
  };
  function build(map,id){
    if(!descriptions[id])return;
    const key=(x,y,z)=>`${x},${y},${z}`;
    const put=(x,y,z,m='q')=>{x=Math.round(x);y=Math.round(y);z=Math.round(z);map.set(key(x,y,z),{x,y,z,m});};
    function box(x0,x1,y0,y1,z0,z1,m){for(let x=x0;x<=x1;x++)for(let y=y0;y<=y1;y++)for(let z=z0;z<=z1;z++)put(x,y,z,m);}
    function clear(x0,x1,y0,y1,z0,z1){for(let x=x0;x<=x1;x++)for(let y=y0;y<=y1;y++)for(let z=z0;z<=z1;z++)map.delete(key(x,y,z));}
    function line(a,b,m='q'){const n=Math.max(...a.map((v,i)=>Math.abs(v-b[i])));for(let t=0;t<=n;t++){const p=a.map((v,i)=>v+(b[i]-v)*(n?t/n:0));put(...p,m);}}
    function ring(cx,y,cz,r,m,plane='xy',gap=false){for(let a=-r;a<=r;a++)for(let b=-r;b<=r;b++){const d=Math.hypot(a,b);if(d<r-1||d>r||gap&&a>0&&b>0)continue;if(plane==='xz')put(cx+a,y,cz+b,m);else if(plane==='yz')put(cx,y+a,cz+b,m);else put(cx+a,y+b,cz,m);}}
    function arch(cx,y,z,r,h,m){box(cx-r,cx-r,y,y+h,z,z,m);box(cx+r,cx+r,y,y+h,z,z,m);line([cx-r,y+h,z],[cx,y+h+2,z],m);line([cx,y+h+2,z],[cx+r,y+h,z],m);}
    function stand(x,y,z,m,h=2){put(x,y,z,'black');box(x,x,y+1,y+h,z,z,'q');put(x,y+h+1,z,m);}
    function benches(m='dark',rows=[-6,-11,-16]){for(const s of [-1,1])for(const z of rows){const x=s<0?-8:4;box(x,x+4,8,8,z,z,'seat');box(x,x+4,8,8,z+1,z+1,m);}}
    function shelves(cx,m='shelf'){box(cx-4,cx+4,4,7,-18,-18,m);box(cx-4,cx-4,4,6,-17,-11,m);}
    function desk(cx,z,m='wood',n=3,y=4){box(cx-n,cx+n,y,y,z,z,m);for(let x=-n+1;x<n;x+=2)put(cx+x,y+1,z,'lectern');}
    function tree(cx,y,z,h=6,r=3){box(cx,cx,y,y+h,z,z,'pillar');for(const s of [-1,1])line([cx,y+2,z],[cx+s*r,y+h,z],'q');for(let x=-r;x<=r;x++)for(let dz=-2;dz<=2;dz++)if(x*x+dz*dz<=r*r)put(cx+x,y+h+1,z+dz,'leaves');put(cx,y+h,z,'glow');}
    function mask(cx,y,z,m='pink',r=3){for(let x=-r;x<=r;x++)for(let dy=0;dy<=r*2;dy++){if(dy===r+1&&Math.abs(x)===1)continue;put(cx+x,y+dy,z,dy===1&&Math.abs(x)<2?'black':Math.abs(x)===r?m:'q');}}
    // Replace loose furnishings only. Exterior walls, doorways and floor levels survive.
    clear(-10,10,8,23,-24,-2);clear(-5,5,12,22,-25,-25);
    clear(-33,-25,4,20,-18,-10);clear(25,33,4,20,-18,-10);clear(-6,6,32,43,-19,-7);
    const theme={destruction:'redbrick',hunt:'oxidized',erudition:'lapis',harmony:'pink',nihility:'obsidian',preservation:'andesite',abundance:'moss',remembrance:'packedice',elation:'orange'}[id];
    for(let x=-10;x<=10;x++)for(let z=-24;z<=-2;z++)put(x,7,z,Math.abs(x)===10||z===-24?'black':(x+z)%5===0?'calcite':'q');
    for(const x of [-2,2])box(x,x,7,7,-19,-2,theme);
    // A stable altar and chest position across all ten ruins.
    box(-4,4,8,8,-24,-20,theme);box(-3,3,9,9,-23,-21,'q');put(0,10,-21,'chest');
    for(const x of [-3,3])put(x,10,-22,'rod');
    // Reuse roof attachment coordinates; every theme has suspended lights overhead.
    for(const z of [-8,-17])for(const x of [-5,5]){box(x,x,20,23,z,z,'chain');put(x,19,z,'glow');}
    // Side rooms retain an open three-wide central circulation band.
    for(const cx of [-29,29])for(let x=cx-4;x<=cx+4;x++)for(let z=-18;z<=-10;z++)put(x,3,z,Math.abs(x-cx)===4||z===-18?theme:'q');
    for(let x=-6;x<=6;x++)for(let z=-19;z<=-7;z++)put(x,31,z,(Math.abs(x)===5||z===-18||z===-8)?theme:'q');
    switch(id){
      case 'destruction':
        for(const s of [-1,1]){
          for(const z of [-7,-15]){box(s*6-1,s*6+1,8,8,z-1,z+1,'black');put(s*6,9,z,'magma');box(s*6-1,s*6+1,10,10,z-1,z+1,'redglass');}
          line([s*5,11,-24],[s*3,21,-24],'redbrick');line([s*3,21,-24],[s*6,22,-24],'magma');
          for(let z=-17;z<=-4;z++)put(s*(4+Math.abs(z%3)),7,z,'redbrick');
        }
        shelves(-29);desk(-29,-17,'redbrick',2);for(const x of [-32,-26])stand(x,4,-11,'redglass');
        for(const x of [26,29,32]){stand(x,4,-17,'magma');put(x,8,-17,'redglass');}
        ring(0,37,-13,4,'redbrick');ring(0,37,-13,3,'magma','yz');desk(0,-19,'black',4,32);break;
      case 'hunt':
        benches('oxidized',[-7,-14]);
        for(const z of [-5,-11,-17]){line([-2,7,z+2],[0,7,z],'oxidized');line([0,7,z],[2,7,z+2],'oxidized');}
        arch(0,12,-24,4,6,'oxidized');line([-3,16,-24],[3,16,-24],'q');line([1,14,-24],[4,16,-24],'oxidized');
        shelves(-29);desk(-29,-17,'oxidized',3);for(const x of [-32,-26])put(x,4,-11,'lectern');
        arch(29,4,-17,3,5,'oxidized');for(const x of [26,32])stand(x,4,-11,'emerald');
        ring(0,31,-13,4,'oxidized','xz');stand(0,32,-13,'q',1);line([-2,34,-12],[3,38,-16],'oxidized');put(3,38,-16,'glass');desk(0,-19,'q',4,32);break;
      case 'erudition':
        ring(0,17,-24,5,'lapis');ring(0,17,-24,3,'q','yz');put(0,17,-24,'amethyst');
        for(const x of [-6,6])for(const z of [-7,-14]){box(x-1,x+1,8,8,z,z,'lapis');put(x,9,z,'lectern');}
        for(let x=-8;x<=8;x++)for(let z=-18;z<=-3;z++)if((x*3+z)%7===0)put(x,7,z,'lapis');
        shelves(-29);box(-26,-26,4,8,-18,-16,'shelf');desk(-29,-16,'wood',2);
        ring(29,8,-17,3,'lapis');ring(29,8,-17,2,'q','yz');put(29,8,-17,'amethyst');for(const x of [26,32])stand(x,4,-11,'lapis');
        ring(0,37,-13,4,'lapis');ring(0,37,-13,3,'q','yz');put(0,37,-13,'amethyst');for(const x of [-5,5])for(const z of [-18,-8])put(x,32,z,'lectern');break;
      case 'harmony':
        benches('pink');for(const s of [-1,1])for(let i=0;i<4;i++){const x=s*(4+i);box(x,x,10,17+i,-24,-24,'pillar');put(x,18+i,-24,'pink');}
        ring(0,20,-12,5,'pink','xz');put(0,19,-12,'glow');
        shelves(-29);desk(-29,-17,'pink',3);for(const x of [-32,-26])put(x,4,-11,'seat');
        for(let x=-3;x<=3;x+=2){box(29+x,29+x,9+Math.abs(x),15,-17,-17,'rod');put(29+x,8+Math.abs(x),-17,'pink');}
        for(const x of [-4,4]){box(x,x,32,38,-16,-16,'pillar');put(x,39,-16,'pink');}ring(0,32,-13,3,'pink','xz');desk(0,-19,'q',3,32);break;
      case 'nihility':
        ring(-1,17,-24,5,'obsidian','xy',true);put(3,14,-24,'crying');
        for(const [x,z,h] of [[-7,-7,3],[6,-15,5],[-6,-17,2]]){box(x,x,8+h,9+h,z,z,'obsidian');put(x,8,z,'seat');}
        box(-32,-32,4,7,-18,-18,'shelf');box(-26,-26,4,6,-18,-18,'shelf');put(-29,4,-17,'lectern');
        stand(26,4,-17,'q');stand(32,4,-17,'q');put(29,9,-17,'purpleglass');box(29,29,10,14,-17,-17,'chain');
        ring(-1,37,-14,4,'obsidian','xy',true);put(3,33,-9,'crying');put(-4,32,-18,'lectern');break;
      case 'preservation':
        benches('andesite');for(const s of [-1,1])for(const z of [-6,-13,-20])box(s*9-1,s*9+1,8,13,z,z,'andesite');
        for(let y=12;y<=21;y++){const w=Math.min(4,Math.floor((y-12)/2));for(const x of [-w,w])put(x,y,-24,'iron');}box(-4,4,22,22,-24,-24,'iron');box(0,0,15,21,-24,-24,'blueice');
        shelves(-29);for(const x of [-33,-25])box(x,x,4,8,-18,-18,'andesite');desk(-29,-17,'iron',2);
        for(const x of [26,29,32]){box(x-1,x+1,6,8,-17,-17,'iron');put(x,5,-17,'blueice');put(x,7,-17,'blueice');}
        for(const r of [3,5]){box(-r,r,31,31,-13-r,-13-r,'andesite');box(-r,r,31,31,-13+r,-13+r,'andesite');}stand(0,32,-13,'blueice',3);desk(0,-19,'iron',4,32);break;
      case 'abundance':
        tree(0,10,-24,7,4);for(const s of [-1,1])for(const z of [-6,-13,-18]){box(s*6-1,s*6+1,8,8,z-1,z+1,'q');box(s*6-1,s*6+1,9,9,z-1,z+1,'moss');put(s*6,10,z,'leaves');put(s*8,8,z,'seat');}
        shelves(-29);desk(-29,-17,'q',2);for(const x of [-32,-26]){put(x,4,-11,'moss');put(x,5,-11,'leaves');}
        tree(29,4,-17,6,2);for(const x of [26,32]){put(x,4,-11,'q');put(x,5,-11,'leaves');}
        tree(0,32,-13,6,3);desk(0,-19,'q',3,32);for(const x of [-5,5])put(x,32,-8,'moss');break;
      case 'remembrance':
        for(const z of [-6,-12,-18])arch(0,8,z,4,6,'packedice');
        for(const s of [-1,1])for(const z of [-9,-16])stand(s*8,8,z,'lightblueglass',2);
        ring(0,17,-24,4,'blueice');
        for(const x of [-32,-26]){box(x,x,4,9,-17,-17,'blueice');put(x,7,-16,'amethyst');}desk(-29,-17,'q',2);box(-33,-33,4,7,-12,-11,'shelf');
        for(const x of [26,32])for(const z of [-17,-11]){box(x,x,4,9,z,z,'lightblueglass');put(x,4,z,'q');}put(29,6,-17,'amethyst');
        for(const z of [-16,-12,-8])arch(0,32,z,3,6,'packedice');put(0,33,-13,'amethyst');break;
      case 'elation':
        box(-8,8,8,8,-24,-19,'pink');box(-3,3,9,9,-23,-21,'q');put(0,10,-21,'chest');
        mask(0,13,-24,'orange',4);benches('pink',[-6,-11,-16]);
        for(const [x,h,m] of [[-8,18,'orange'],[-6,21,'yellow'],[6,19,'pink'],[8,22,'orange']])box(x,x,9,h,-24,-24,m);
        shelves(-29);desk(-29,-17,'pink',2);for(const [x,m] of [[-32,'orange'],[-29,'yellow'],[-26,'pink']])stand(x,4,-11,m,1);
        for(const [x,y,m] of [[26,6,'pink'],[29,8,'yellow'],[32,6,'orange']])mask(x,y,-17,m,1);
        mask(0,34,-18,'orange',2);for(const x of [-4,4])for(let d=0;d<3;d++)put(x,32+d,-9-d,'seat');for(const x of [-5,5])put(x,40,-13,'pink');break;
    }
    // Furniture never occupies the central walking corridor or the treasure lid space.
    for(let x=-1;x<=1;x++)for(let z=-19;z<=-1;z++)for(let y=8;y<=10;y++){const b=map.get(key(x,y,z));if(b&&b.m!=='carpet')map.delete(key(x,y,z));}
    for(const cx of [-29,29])clear(cx-4,cx+4,4,6,-15,-13);
    clear(0,0,11,12,-21,-21);put(0,10,-21,'chest');
  }
  return {build,descriptions};
})();
