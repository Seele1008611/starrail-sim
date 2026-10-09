(() => {
  'use strict';
  const $ = id => document.getElementById(id);
  const {paths, common, assetRoot} = window.combatMeshDesign;
  const canvas = $('stage'), ctx = canvas.getContext('2d');
  const state = {path:paths[0], effect:paths[0].effects[1], time:0, playing:true,
    speed:1, zoom:1, detail:36, attack:'melee', view:'iso', follow:true, anchors:true, movement:false, crowd:false, combo:false};
  const player = {x:-2.6,y:0,z:0}, victim = {x:1.45,y:0,z:0};
  const extras = [{x:2.6,y:0,z:-1.8},{x:3.6,y:0,z:1.3}];
  const clamp = (v,a=0,b=1) => Math.max(a,Math.min(b,v));
  const P = (x,y,z=0) => ({x,y,z});
  const add = (a,b) => P(a.x+b.x,a.y+b.y,a.z+b.z);
  const mul = (a,f) => P(a.x*f,a.y*f,a.z*f);
  const lerp = (a,b,f) => add(mul(a,1-f),mul(b,f));
  const sub = (a,b) => add(a,mul(b,-1));
  const norm = a => mul(a,1/(Math.hypot(a.x,a.y,a.z)||1));
  const cross = (a,b) => P(a.y*b.z-a.z*b.y,a.z*b.x-a.x*b.z,a.x*b.y-a.y*b.x);
  const ease = t => {t=clamp(t);return t*t*(3-2*t);};
  const tint = (color,f) => '#'+color.slice(1).match(/../g).map(v=>Math.round(clamp(parseInt(v,16)*f,0,255)).toString(16).padStart(2,'0')).join('');
  let faces = [];
  const appearance = {
    hunt:'围猎落箭：四周起射标记错拍点亮，青蓝光箭带双侧尾迹、随行碎光斜向目标中央降下；空中断续轨环蓄势，最大主箭带刻纹与四道分段光轨垂直终结，再释放两层冲击纹和立体晶屑。普通四箭、强化八箭，远程保留横向先导贯入箭。',
    preservation:'琥珀棱面盾片与护甲嵌纹环身展开，双层断续边界错时旋亮；反震以三拍菱形脉冲从护盾来源推进至攻击者。',
    abundance:'环身枝芽、叶瓣与冠层逐段生长，甘露沿生命轨迹汇入身体；吸收盾亮起浅金晶片，慈航在多层莲瓣中释放生命光雨。',
    destruction:'裂火晶片沿地面向外撕开，火舌、碎烬与灼烧刻痕错拍释放；强化形成火冠和更高的裂火，怒火使用小型蓄势晶片。',
    erudition:'眼形计算框与多轴轨道旋转，环绕节点展开三行演算刻度并连向核心；推演终段释放分区扫描回响。',
    nihility:'暗片与残缺轨道向内卷入，细侵蚀轨迹收向空心暗涡；终段保留六向断裂回旋，体现侵蚀后的空洞余波。',
    harmony:'环绕音符沿玫粉音律带升起，柔金节点错拍点亮；三拍地面节奏刻度与外层音轨形成连贯共鸣余韵。',
    remembrance:'雪晶、立体沙漏与刻面晶片环绕目标，双拍晶痕错时复现；终段晶尘缓落，表现记录与回响留下的残像。',
    elation:'彩带、骰面和扇形牌阵错拍展开，菱形碎彩弹跳并拖出卷曲尾迹；终段五方小礼花再次跳起，保留粉色主调与柔金点缀。'
  };
  function targetNow() {
    const f = state.movement ? clamp((state.time-state.effect.impact)/.55) : 0;
    return add(victim,P(f*.8,Math.sin(f*Math.PI)*.08,0));
  }
  function anchor() { return state.effect.self ? player : state.follow ? targetNow() : victim; }
  function project(p) {
    const angle = state.view==='front' ? Math.PI/2 : state.view==='side' ? .12 : .72;
    const x=p.x+.45,z=p.z, side=x*Math.sin(angle)-z*Math.cos(angle), depth=x*Math.cos(angle)+z*Math.sin(angle);
    return {x:545+side*75*state.zoom,y:475+depth*25*state.zoom-p.y*72*state.zoom,depth};
  }
  function paint(points,color,opacity=1,stroke=false) {
    ctx.save();ctx.globalAlpha=opacity;ctx.beginPath();
    points.forEach((p,i)=>{const q=project(p);i?ctx.lineTo(q.x,q.y):ctx.moveTo(q.x,q.y);});
    ctx.closePath();ctx.fillStyle=color;ctx.fill();
    if(stroke){ctx.strokeStyle=stroke;ctx.lineWidth=.7;ctx.stroke();}ctx.restore();
  }
  function facet(points,color,opacity,birth=0,life=.8,edge=false) {
    const age=state.time-state.effect.impact-birth;
    if(age<0||age>=life)return;
    faces.push({points,color,edge,alpha:opacity*ease(age/.075)*ease((life-age)/.3),depth:points.reduce((s,p)=>s+project(p).depth,0)/points.length});
  }
  function ribbon(points,width,color,opacity=.68,birth=0,life=.75,normal=P(0,0,1)) {
    const edges=points.map((p,i)=>{
      const tangent=norm(sub(points[Math.min(points.length-1,i+1)],points[Math.max(0,i-1)]));
      let across=norm(cross(tangent,normal));if(Math.hypot(across.x,across.y,across.z)<.01)across=P(0,1,0);
      const w=mul(across,width*(.22+.78*Math.sin(i/(points.length-1)*Math.PI)));
      return {a:add(p,w),b:sub(p,w)};
    });
    for(let i=0;i<edges.length-1;i++)facet([edges[i].a,edges[i].b,edges[i+1].b,edges[i+1].a],color,opacity,birth+i/(edges.length-1)*.12,life);
  }
  function stroke(root,coords,width,color,alpha,birth=0,life=.75) {
    ribbon(coords.map(v=>add(root,P(v[0],v[1],v[2]||0))),width,color,alpha,birth,life);
  }
  function curve(root,fn,width,color,alpha,birth=0,life=.75,normal=P(0,0,1)) {
    ribbon(Array.from({length:state.detail+1},(_,i)=>add(root,fn(i/state.detail))),width,color,alpha,birth,life,normal);
  }
  function orbit(root,radius,color,birth=0,alpha=.5,life=.7,turn=0,span=Math.PI*2) {
    curve(root,f=>P(Math.cos(turn+f*span)*radius,0,Math.sin(turn+f*span)*radius),.032,color,alpha,birth,life,P(0,1,0));
  }
  function gem(root,size,color,birth=0,life=.8,rotation=0) {
    const age=state.time-state.effect.impact-birth;
    const c=add(root,P(0,Math.max(0,age)*.13,0));
    const ring=Array.from({length:4},(_,i)=>P(Math.cos(rotation+i*Math.PI/2)*size,0,Math.sin(rotation+i*Math.PI/2)*size));
    for(let i=0;i<4;i++) {
      facet([add(c,P(0,size*1.6,0)),add(c,ring[i]),add(c,ring[(i+1)%4])],tint(color,i%2?.8:1.08),i%2?.42:.7,birth,life,color+'70');
      facet([add(c,P(0,-size*1.6,0)),add(c,ring[(i+1)%4]),add(c,ring[i])],tint(color,.72),.38,birth,life,color+'45');
    }
  }
  function arrow(head,tail,color,birth=0,life=.6) {
    const dir=norm(sub(head,tail));let side=norm(cross(dir,P(0,1,0)));
    if(Math.hypot(side.x,side.y,side.z)<.01)side=P(1,0,0);
    ribbon([tail,lerp(tail,head,.72),head],.045,color,.7,birth,life,P(0,1,0));
    ribbon([tail,head],.025,color,.5,birth,life,P(0,0,1));
    for(const sign of [-1,1])ribbon([add(sub(head,mul(dir,.32)),mul(side,sign*.18)),head],.065,color,.7,birth+.07,life,cross(dir,side));
  }
  function connections(root,color,birth=.06) {
    extras.forEach((p,i)=>{const end=add(p,P(0,1.1,0));
      const a=add(root,P(0,1.1,0));
      const pts=Array.from({length:state.detail+1},(_,j)=>{const f=j/state.detail;return add(lerp(a,end,f),P(0,Math.sin(f*Math.PI)*.35,0));});
      ribbon(pts,.028,color,.62,birth+i*.12,.7);orbit(add(p,P(0,.08,0)),.65,color,birth+.18+i*.12,.45);
    });
  }
  function buildMeshes() {
    faces=[];
    const e=state.effect,p=state.path.palette,a=anchor(),c=add(a,P(0,1.08,0)),foot=P(a.x,a.y+.06,a.z),strong=e.strong;
    const age=state.time-e.impact;
    if(age<0||age>2.35)return;
    if(e.id==='hit'){
      stroke(c,[[-.24,-.14],[-.38,0],[-.24,.14]],.025,p.main,.65,0,.35);
      stroke(c,[[.24,-.14],[.38,0],[.24,.14]],.025,p.main,.65,0,.35);
      for(let i=0;i<4;i++){const theta=i*Math.PI/2,r=.24+clamp(age/.3)*.18;
        gem(add(c,P(Math.cos(theta)*r,Math.sin(theta)*r,0)),.014,p.main,.025+i*.01,.26,theta);}
      const direction=state.attack==='ranged'?P(1,0,0):P(.75,-.45,.25);
      for(let i=0;i<3;i++) {
        const progress=clamp(age/.24),root=add(c,add(mul(direction,.1+progress*.24),P(0,(i-1)*.065,0)));
        ribbon([root,add(root,mul(direction,.1))],.011,p.accent,.45,.015+i*.012,.22);
      }
    }
    else if(e.id==='break')breakMeshes(a,p.main);
    else {
      if(state.path.id!=='hunt' && !e.self && !e.multi && state.attack==='ranged')arrow(c,add(c,P(-1.1,.05,0)),p.main,0,.45);
      switch(state.path.id) {
        case 'hunt': {
          window.drawHuntCrown({c,foot,p,e,age,attack:state.attack,detail:state.detail,
            P,add,mul,cross,ease,clamp,facet,ribbon,curve,gem,tint});
          break;
        }
        case 'preservation': {
          const shield=e.id==='counter'?add(player,P(0,1.05,0)):c;
          for(const sign of [-1,1])stroke(shield,[[0,.75],[sign*.54,.45],[sign*.5,-.38],[0,-.78]],.08,p.main,.65,0,.95);
          for(let i=0;i<6;i++) {const f=i*Math.PI/3;const plate=add(shield,P(Math.cos(f)*.9,Math.sin(f)*.75,.12));
            stroke(plate,[[-.18,.24],[.18,.24],[.24,0],[.18,-.24],[-.18,-.24],[-.24,0],[-.18,.24]],.03,p.main,.48,.1+i*.045,.8);}
          orbit(e.id==='counter'?add(player,P(0,.07,0)):foot,1.05,p.main,.16,.42);
          if(e.id==='counter') {ribbon([shield,c],.065,p.accent,.65,.22,.45);stroke(c,[[0,.45],[.35,0],[0,-.45],[-.35,0],[0,.45]],.04,p.main,.65,.32);}
          break;
        }
        case 'destruction': {
          const arms=e.id==='rage'?3:strong?7:5;
          for(let i=0;i<arms;i++) {const angle=i*Math.PI*2/arms,r=strong?1.55:.95;
            ribbon([foot,add(foot,P(Math.cos(angle+.18)*r*.55,0,Math.sin(angle+.18)*r*.55)),add(foot,P(Math.cos(angle)*r,0,Math.sin(angle)*r))],.06,p.main,.65,i*.025,.75,P(0,1,0));
            const base=add(foot,P(Math.cos(angle)*r*.7,0,Math.sin(angle)*r*.7));
            stroke(base,[[0,0],[.12,.35],[-.09,.65],[.04,strong?1.25:.85]],.1,p.main,.52,.12+i*.035,.65);
          }
          if(strong)for(const sign of [-1,1])stroke(c,[[sign*.62,-.5],[sign*.86,.05],[sign*.54,.82],[sign*.42,.22]],.075,p.accent,.55,.2);
          break;
        }
        case 'erudition': {
          curve(c,f=>P(Math.cos(f*Math.PI*2)*.78,Math.sin(f*Math.PI*2)*.4,0),.04,p.main,.65);
          stroke(c,[[0,.22],[.22,0],[0,-.22],[-.22,0],[0,.22]],.035,p.accent,.6,.1);
          for(const x of [-1,1])for(const y of [-1,1])stroke(c,[[x*.55,y*.68],[x*.98,y*.68],[x*.98,y*.35]],.035,p.main,.6,.1);
          for(let i=0;i<(strong?5:3);i++)stroke(c,[[-.6,.46-i*.2],[.6,.46-i*.2]],.012,p.main,.35,.16+i*.055,.38);
          connections(a,p.main,.12);if(strong)orbit(foot,1.25,p.main,.32,.45);
          break;
        }
        case 'nihility': {
          for(let i=0;i<(strong?4:3);i++) {const radius=1.1-i*.14,turn=i*1.8-Math.max(0,age)*.8;
            curve(c,f=>P(Math.cos(turn+f*4.2)*radius,Math.sin(f*6.3)*.2,Math.sin(turn+f*4.2)*radius),.055,p.main,.5,i*.06,.8,P(0,1,0));}
          if(e.multi)connections(a,p.main,.2);
          if(strong)for(let i=0;i<3;i++)orbit(foot,.5+i*.35,p.main,.35+i*.06,.4,.55,i,.9*Math.PI);
          break;
        }
        case 'harmony': {
          for(const sign of [-1,1])for(let i=0;i<(strong?4:2);i++) {
            curve(c,f=>P(sign*(.22+f*1.25),Math.sin(f*Math.PI)*.45+i*.17-.25,Math.sin(f*Math.PI)*.08),.03,p.main,.6,.04+i*.09,.8);
            const note=add(c,P(sign*(1.12+i*.12),.35+i*.22,0));
            stroke(note,[[-.09,-.1],[.05,-.1],[.05,.25],[.25,.19]],.03,p.accent,.65,.18+i*.08,.7);}
          orbit(foot,1.0,p.main,.18,.38);if(strong)orbit(foot,1.3,p.accent,.3,.35);
          break;
        }
        case 'abundance': {
          for(const sign of [-1,1]) {
            curve(c,f=>P(sign*(.25+Math.sin(f*Math.PI*.7)*.65),f*1.6-.8,0),.045,p.main,.6);
            for(let i=0;i<3;i++) {const leaf=add(c,P(sign*(.48+i*.13),-.5+i*.45,0));
              stroke(leaf,[[0,0],[sign*.24,.32],[sign*.43,.38],[sign*.35,.1],[0,0]],.045,p.main,.6,.12+i*.08);}
          }
          if(e.id==='manna')curve(c,f=>P(Math.cos(f*6.3)*.68,Math.sin(f*6.3)*.85,0),.045,p.accent,.6,.2);
          if(strong)for(let i=0;i<8;i++) {const a=i*Math.PI/4;curve(foot,f=>P(Math.cos(a)*Math.sin(f*Math.PI)*1.2,Math.sin(f*Math.PI)*.35,Math.sin(a)*Math.sin(f*Math.PI)*1.2),.065,p.main,.5,.1+i*.03,.85,cross(P(Math.cos(a),0,Math.sin(a)),P(0,1,0)));}
          break;
        }
        case 'remembrance': {
          const layers=strong?3:e.id==='echo'?2:1;
          for(let i=0;i<layers;i++) {const r=.62+i*.1;
            stroke(c,[[-r,.6],[r,.6],[0,0],[-r,-.6],[r,-.6],[0,0],[-r,.6]],.035,p.main,.55,i*.14,.65);
            for(let arm=0;arm<6;arm++){const ang=arm*Math.PI/3;stroke(c,[[0,0],[Math.cos(ang)*r,Math.sin(ang)*r]],.028,p.main,.5,.1+i*.14,.6);}}
          for(let i=0;i<(strong?8:4);i++){const ang=i*Math.PI*2/(strong?8:4);gem(add(c,P(Math.cos(ang)*.95,.3+Math.sin(ang)*.45,Math.sin(ang)*.3)),.075,p.main,.18+i*.03,.75,age);}
          break;
        }
        case 'elation': {
          for(let i=0;i<(strong?3:2);i++)curve(c,f=>{const ang=i*2.1+f*Math.PI*1.5;return P(Math.cos(ang)*(.4+f),.45-f,Math.sin(ang)*(.4+f));},.055,i%2?p.accent:p.main,.55,i*.12,.75,P(0,1,0));
          for(const sign of [-1,1])stroke(add(c,P(sign*1.05,.3,0)),[[0,.23],[.06,.06],[.23,0],[.06,-.06],[0,-.23],[-.06,-.06],[-.23,0],[-.06,.06],[0,.23]],.035,p.main,.6,.18);
          const dice=add(c,P(.85,-.2,0));stroke(dice,[[-.2,-.2],[.2,-.2],[.2,.2],[-.2,.2],[-.2,-.2]],.035,p.accent,.6,.22);gem(dice,.035,p.main,.22,.6);
          if(strong)gem(add(c,P(-.9,.65,0)),.12,p.accent,.36,.7,age);
          break;
        }
      }
      layeredDesign(a,c,foot,p,e,age);
      window.combatGrandMeshes({path:state.path.id,a,c,foot,p,e,age,attack:state.attack,
        detail:state.detail,P,add,mul,lerp,cross,ease,clamp,facet,ribbon,stroke,curve,orbit,gem,arrow,leafMesh,tint,player,extras});
      // Separated geometric fragments outside the body. Deterministic positions allow timeline scrubbing.
      if(e.id!=='rage'&&state.path.id!=='hunt')for(let i=0;i<(strong?12:6);i++) {
        const angle=i*2.39996,birth=.28+i*.018,t=clamp(age-birth,0,1),r=.8+t*.65;
        const pos=add(c,P(Math.cos(angle)*r,Math.sin(angle)*.55+t*.2,Math.sin(angle)*r*.5));
        gem(pos,.024+(i%3)*.008,p.main,birth,.65,angle+t);
      }
    }
    if(state.combo&&!e.self&&e.id!=='hit'&&e.id!=='break')breakMeshes(a,p.main,.08);
  }
  function leafMesh(root,axis,width,color,birth,life=.9) {
    const growth=ease((state.time-state.effect.impact-birth)/.3);
    const direction=mul(axis,growth),tip=add(root,direction),mid=add(root,mul(direction,.55));
    let across=norm(cross(axis,P(0,0,1)));if(Math.hypot(across.x,across.y,across.z)<.01)across=P(1,0,0);
    const left=add(mid,mul(across,width*growth)),right=sub(mid,mul(across,width*growth));
    facet([root,left,tip],color,.3,birth,life,color+'75');
    facet([root,tip,right],tint(color,.76),.38,birth,life,color+'60');
    ribbon([root,mid,tip],.015,color,.62,birth+.06,life-.06);
  }
  function layeredDesign(a,c,foot,p,e,age) {
    const strong=e.strong,unfold=ease(age/.35),drift=clamp(age-.3)*.3;
    switch(state.path.id) {
      case 'hunt': break; // Crown and reverse-time choreography have one dedicated owner.
      case 'preservation': {
        const root=e.id==='counter'?add(player,P(0,1.08,0)):c;
        const base=e.id==='counter'?add(player,P(0,.065,0)):foot;
        for(let i=0;i<6;i++) {
          const angle=i*Math.PI/3,radial=P(Math.cos(angle),0,Math.sin(angle)),tangent=P(-Math.sin(angle),0,Math.cos(angle));
          const center=add(root,mul(radial,.65+unfold*.24));
          const v=(x,y)=>add(add(center,mul(tangent,x)),P(0,y,0));
          const poly=[v(-.18,.45),v(.18,.45),v(.25,0),v(0,-.48),v(-.25,0)];
          for(let j=0;j<poly.length;j++)facet([center,poly[j],poly[(j+1)%poly.length]],tint(p.main,j%2?.86:1),.12,.15+i*.04,.9,p.main+'80');
          ribbon([...poly,poly[0]],.022,p.main,.52,.15+i*.04,.9,radial);
        }
        orbit(base,1.14,p.main,.28,.4,.85,age*.25,Math.PI*1.65);
        break;
      }
      case 'destruction': {
        const count=e.id==='rage'?3:strong?7:5;
        for(let i=0;i<count;i++) {
          const angle=i*Math.PI*2/count,radial=P(Math.cos(angle),0,Math.sin(angle));
          const base=add(foot,mul(radial,.6+unfold*.23));
          leafMesh(base,P(radial.x*.25,(strong?1.15:.7)*unfold,radial.z*.25),.12,p.main,.1+i*.035,.75);
          if(e.id!=='rage')curve(base,f=>P(radial.x*Math.sin(f*Math.PI)*.22,f*(strong?1.25:.8),radial.z*Math.sin(f*Math.PI)*.22),.028,p.accent,.48,.23+i*.025,.65,radial);
        }
        if(strong)for(let i=0;i<4;i++) {const ang=i*Math.PI/2;gem(add(c,P(Math.cos(ang)*(1+drift),.5+drift,Math.sin(ang)*(1+drift))),.045,p.accent,.38+i*.035,.65,age*2);}
        break;
      }
      case 'erudition': {
        for(let layer=0;layer<(strong?2:1);layer++) {
          const radius=.85+layer*.18;
          curve(c,f=>{const t=f*Math.PI*1.6+age*.45+layer*Math.PI;return P(Math.cos(t)*radius,Math.sin(t)*radius*.5,Math.sin(t)*radius*.5);},.025,p.main,.4,.2+layer*.09,.8,P(0,1,1));
          for(let i=0;i<4;i++) {const ang=i*Math.PI/2+age*.45;gem(add(c,P(Math.cos(ang)*radius,Math.sin(ang)*radius*.5,Math.sin(ang)*radius*.5)),.045,p.accent,.23+i*.04,.7,ang);}
        }
        extras.forEach((target,i)=>{const center=add(target,P(0,1.08,0));
          stroke(center,[[0,.35],[.3,0],[0,-.35],[-.3,0],[0,.35]],.025,p.main,.5,.3+i*.1,.7);gem(add(center,P(0,.5,0)),.07,p.main,.35+i*.1,.6,age);});
        break;
      }
      case 'nihility': {
        for(let layer=0;layer<(strong?3:2);layer++) {
          const phase=layer*Math.PI*2/3-age*.8,radius=1.25-layer*.12-clamp(age)*.12;
          curve(c,f=>{const t=phase+f*Math.PI*1.4;return P(Math.cos(t)*radius,(f-.5)*.6,Math.sin(t)*radius);},.045,p.main,.38,.18+layer*.09,.85,P(0,1,0));
          const ang=phase+1.2;gem(add(c,P(Math.cos(ang)*radius,.22,Math.sin(ang)*radius)),.04,p.main,.28+layer*.09,.7,age);
        }
        if(e.multi)extras.forEach((target,i)=>orbit(add(target,P(0,.07,0)),.62,p.main,.32+i*.1,.35,.7,-age*.6,Math.PI*1.45));
        break;
      }
      case 'harmony': {
        for(const sign of [-1,1])for(let layer=0;layer<(strong?3:2);layer++) {
          curve(c,f=>P(sign*(.3+f*1.25),Math.sin(f*Math.PI)*.35+Math.sin(f*6.3-age*4)*.06-layer*.14,Math.sin(f*Math.PI)*(.2+layer*.06)),.025,p.main,.45,.2+layer*.1,.85);
          const t=clamp(age-.25-layer*.08),note=add(c,P(sign*(1.12+layer*.1),.35+layer*.15+t*.3,.12));
          stroke(note,[[-.09,-.06],[.04,-.06],[.04,.3],[.21,.23]],.025,p.accent,.58,.28+layer*.08,.8);
        }
        if(strong)for(let i=0;i<6;i++){const ang=i*Math.PI/3+age*.25;gem(add(foot,P(Math.cos(ang)*1.2,.12,Math.sin(ang)*1.2)),.04,p.accent,.35+i*.025,.75,age);}
        break;
      }
      case 'abundance': {
        for(const sign of [-1,1])for(let i=0;i<3;i++)leafMesh(add(c,P(sign*(.45+i*.12),-.5+i*.43,.12)),P(sign*.43,.32,.1),.13,p.main,.15+i*.08,.9);
        if(strong)for(let i=0;i<8;i++) {
          const angle=i*Math.PI/4,radial=P(Math.cos(angle),0,Math.sin(angle));
          leafMesh(add(foot,mul(radial,.08)),P(radial.x*1.15,.32,radial.z*1.15),.18,p.main,.18+i*.035,.95);
        }
        for(let i=0;i<(strong?8:4);i++){const ang=i*2.4;gem(add(c,P(Math.cos(ang)*1.05,-.4+clamp(age-.25)*.7,Math.sin(ang)*.6)),.03,p.accent,.26+i*.03,.8,ang);}
        break;
      }
      case 'remembrance': {
        for(let arm=0;arm<6;arm++) {
          const angle=arm*Math.PI/3,axis=P(Math.cos(angle),Math.sin(angle),0),across=P(-axis.y,axis.x,0);
          const node=add(c,mul(axis,.45)),tip=add(c,mul(axis,.85));
          ribbon([node,tip],.026,p.main,.52,.18,.75);
          for(const sign of [-1,1])ribbon([add(node,mul(axis,.15)),add(add(node,mul(axis,.06)),mul(across,sign*.15))],.018,p.main,.46,.24,.7);
        }
        if(strong)for(let i=0;i<6;i++){const ang=i*Math.PI/3;gem(add(c,P(Math.cos(ang)*(.9+drift),.55+Math.sin(ang)*.25,Math.sin(ang)*.55)),.09,p.main,.3+i*.05,.85,age*.6+ang);}
        break;
      }
      case 'elation': {
        for(let layer=0;layer<(strong?3:2);layer++)curve(c,f=>{const t=f*Math.PI*1.35+layer*2.4+age*.5;return P(Math.cos(t)*(1+f*.3),.65-f*1.1,Math.sin(t)*(1+f*.3));},.04,layer%2?p.accent:p.main,.46,.18+layer*.12,.8,P(0,1,0));
        const base=add(c,P(.95,.3+Math.sin(clamp(age)*Math.PI)*.22,.3));
        const points=[P(-.15,-.15,-.15),P(.15,-.15,-.15),P(.15,.15,-.15),P(-.15,.15,-.15),P(-.15,-.15,.15),P(.15,-.15,.15),P(.15,.15,.15),P(-.15,.15,.15)].map(v=>{const t=age*.8;return add(base,P(v.x*Math.cos(t)-v.z*Math.sin(t),v.y,v.x*Math.sin(t)+v.z*Math.cos(t)));});
        for(const indices of [[0,1,2,3],[4,5,6,7],[0,1,5,4],[2,3,7,6]])ribbon([...indices.map(i=>points[i]),points[indices[0]]],.018,p.accent,.55,.24,.85);
        for(let i=0;i<3;i++)gem(add(base,P((i-1)*.08,(i-1)*.08,.17)),.018,p.main,.28,.8,age);
        break;
      }
    }
  }
  function breakMeshes(root,color,birth=0) {
    const age=state.time-state.effect.impact-birth;
    // The cracked shell appears before its separated shards; rings stay on the foot plane.
    for(let side=0;side<6;side++) {
      const angle=side*Math.PI/3,radial=P(Math.cos(angle),0,Math.sin(angle));
      ribbon([add(root,add(mul(radial,.56),P(0,.3,0))),add(root,add(mul(radial,.65),P(0,.85,0))),add(root,add(mul(radial,.5),P(0,1.35,0)))],.026,color,.55,birth,.28,radial);
    }
    for(let i=0;i<8;i++) {
      const theta=i*Math.PI/4,dt=Math.PI/10;
      const v=(angle,y,r=.62)=>add(root,P(Math.cos(angle)*r,y,Math.sin(angle)*r));
      facet([v(theta-dt,.3),v(theta+dt,.3),v(theta,1.38,.68)],color,.13,birth,.26,color+'55');
      const t=ease((age-.2)/.65),radius=.72+t*.95;
      const center=v(theta,.75+(i%3)*.28+t*.25,radius);
      const radial=P(Math.cos(theta),0,Math.sin(theta)),side=P(-radial.z,0,radial.x);
      const shard=[add(center,mul(side,-.11)),add(center,mul(side,.11)),add(center,P(radial.x*.08,.38*(1-t*.6),radial.z*.08))];
      facet(shard,tint(color,i%2?.8:1),.48*(1-t*.55),birth+.18+i*.012,.9,color+'70');
    }
    for(let i=0;i<10;i++) {const angle=i*Math.PI/5,t=clamp((age-.18)/.65),radius=.65+t*.9;
      const pos=add(root,P(Math.cos(angle)*radius,.5+(i%3)*.4+t*.2,Math.sin(angle)*radius));
      gem(pos,.09*(1-t*.5),color,birth+.18+i*.014,.85,angle+age);
    }
    for(let i=0;i<4;i++)orbit(add(root,P(0,.065,0)),.4+clamp(age)*1.45+i*.07,color,birth+.22+i*.055,.34,.8,i*Math.PI/3,Math.PI*1.1);
    for(let i=0;i<8;i++){const theta=i*Math.PI/4,r=.65+ease((age-.25)/.65)*1.2;
      const base=add(root,P(Math.cos(theta)*r,.07,Math.sin(theta)*r));
      ribbon([add(base,P(-Math.sin(theta)*.13,0,Math.cos(theta)*.13)),
        add(base,P(Math.cos(theta)*.24,0,Math.sin(theta)*.24)),
        add(base,P(Math.sin(theta)*.13,0,-Math.cos(theta)*.13))],.015,color,.32,birth+.3+i*.012,.72,P(0,1,0));}
    // Secondary cracks and tumbling slivers have their own delayed release.
    for(let i=0;i<12;i++) {
      const theta=i*Math.PI/6,radial=P(Math.cos(theta),0,Math.sin(theta));
      const tangent=P(-radial.z,0,radial.x),t=ease((age-.32)/.75);
      const center=add(root,add(mul(radial,.65+t*1.25),P(0,.4+(i%4)*.22+Math.sin(t*Math.PI)*.35,0)));
      const tilt=mul(tangent,.045*(1-t*.4)),tip=add(center,add(mul(radial,.1),P(0,.16,0)));
      facet([add(center,tilt),add(center,mul(tilt,-1)),tip],i%2?color:tint(color,.65),.4,
        birth+.3+i*.008,.95);
      ribbon([center,add(center,mul(radial,-.14))],.012,color,.32,birth+.32+i*.008,.85,P(0,1,0));
    }
    for(let i=0;i<6;i++) {
      const theta=i*Math.PI/3,progress=ease((age-.2)/.65),radius=.7+progress*1.25;
      curve(add(root,P(0,.085,0)),f=>P(Math.cos(theta+f*.3)*radius,0,Math.sin(theta+f*.3)*radius),
        .025,color,.4,birth+.28+i*.012,.9,P(0,1,0));
    }
  }
  function box(root,w,h,d,colors,output=faces) {
    const a=add(root,P(-w/2,0,-d/2)),b=add(root,P(w/2,0,-d/2)),c=add(root,P(w/2,0,d/2)),e=add(root,P(-w/2,0,d/2)),up=p=>add(p,P(0,h,0));
    [[e,c,up(c),up(e)],[b,c,up(c),up(b)],[up(a),up(b),up(c),up(e)]].forEach((points,i)=>output.push({points,color:colors[i],alpha:1,depth:points.reduce((s,p)=>s+project(p).depth,0)/4}));
  }
  function actor(root,isPlayer) {
    const colors=isPlayer?['#477e94','#326679','#6c9eac']:['#756c73','#5d5766','#9a8d94'];
    for(const x of [-.15,.15])box(add(root,P(x,0,0)),.23,.65,.3,['#29364d','#202b3e','#3d4a60']);
    box(add(root,P(0,.65,0)),.62,.76,.38,colors);
    for(const x of [-.42,.42])box(add(root,P(x,.66,0)),.2,.68,.27,colors);
    box(add(root,P(0,1.41,0)),.56,.56,.5,isPlayer?['#ad8c74','#866e62','#c2a38a']:['#768780','#5b716b','#9ca89c']);
  }
  function guide(root,color,label) {
    const q=project(root);ctx.save();ctx.strokeStyle=color;ctx.fillStyle=color;ctx.lineWidth=1;ctx.setLineDash([3,4]);
    const foot=project(P(root.x,root.y-1.08,root.z));ctx.beginPath();ctx.moveTo(q.x,q.y);ctx.lineTo(foot.x,foot.y);ctx.stroke();ctx.setLineDash([]);
    ctx.beginPath();ctx.moveTo(q.x-8,q.y);ctx.lineTo(q.x+8,q.y);ctx.moveTo(q.x,q.y-8);ctx.lineTo(q.x,q.y+8);ctx.stroke();ctx.font='12px Microsoft YaHei';ctx.fillText(label,q.x+11,q.y-8);ctx.restore();
  }
  function draw() {
    ctx.clearRect(0,0,1120,720);ctx.fillStyle='#0b1425';ctx.fillRect(0,0,1120,720);
    for(let x=-6;x<7;x++)for(let z=-5;z<6;z++)paint([P(x-.49,-.04,z-.49),P(x+.49,-.04,z-.49),P(x+.49,-.04,z+.49),P(x-.49,-.04,z+.49)],(x+z)%2?'#152336':'#17283b',1,'#243c5166');
    buildMeshes();const meshCount=faces.length;actor(player,true);actor(targetNow(),false);
    if(state.crowd||state.effect.multi)extras.forEach(p=>actor(p,false));
    faces.sort((a,b)=>a.depth-b.depth);faces.forEach(f=>paint(f.points,f.color,f.alpha,f.edge));
    if(state.anchors){guide(add(player,P(0,1.08,0)),'#67a6be','玩家中心');guide(add(targetNow(),P(0,1.08,0)),'#d3aa8b','目标中心');guide(add(anchor(),P(0,1.08,0)),state.path.color,state.effect.self?'增益锚点':'特效锚点');
      const a=project(add(player,P(0,.03,0))),b=project(add(targetNow(),P(0,.03,0)));ctx.save();ctx.strokeStyle='#8da5ba66';ctx.setLineDash([5,5]);ctx.beginPath();ctx.moveTo(a.x,a.y);ctx.lineTo(b.x,b.y);ctx.stroke();ctx.restore();}
    const impact=state.time>=state.effect.impact,isBreak=impact&&(state.effect.id==='break'||state.combo),toughness=impact&&!state.effect.self?(isBreak?0:9):10;
    const phaseAge=state.time-state.effect.impact;
    $('mesh-phase').textContent=phaseAge<0?'等待有效触发':phaseAge<.2?'第一拍 · 主体展开':phaseAge<.65?'第二拍 · 命途华彩':phaseAge<1.6?'第三拍 · 回响与消散':'等待下一次触发';
    if(state.path.id==='hunt'&&!['hit','break'].includes(state.effect.id)) {
      $('mesh-phase').textContent=phaseAge<0?'等待有效命中':state.effect.strong
        ? phaseAge<.9?'第一拍 · 八方光箭围猎':phaseAge<1.05?'第二拍 · 光痕汇聚':phaseAge<1.37?'第三拍 · 中央主箭降下':phaseAge<2?'终结冲击与消散':'等待下一次触发'
        : phaseAge<.6?'四方光箭围猎':phaseAge<.65?'光痕汇聚':phaseAge<.97?'中央主箭降下':phaseAge<1.6?'终结冲击与消散':'等待下一次触发';
    }
    $('toughness-fill').style.width=toughness*10+'%';$('toughness-value').textContent=isBreak?'韧性击破':`韧性 ${toughness} / 10`;
    $('particle-count').textContent=meshCount+' 个网格面片';$('time').textContent=state.time.toFixed(2)+' / '+state.effect.duration.toFixed(2)+' 秒';$('timeline').value=Math.round(state.time*1000);
  }
  function playback(){ $('pause').textContent=state.playing?'暂停':'继续';$('play-state').textContent=state.playing?'播放中':'已暂停'; }
  function restart(){state.time=0;state.playing=true;playback();draw();}
  function selectEffect(e) {
    state.effect=e;$('scene-title').textContent=e.name;$('condition').textContent=e.condition;
    $('visual').textContent=e.id==='hit'?'贴合命中中心的短小折角与定向碎光；近战斜向切开、远程沿射击方向弹出，快速消散。':e.id==='break'?'轮廓裂纹与几何壳片先破开，主碎片及延迟细晶片分层飞散；两拍断续冲击纹锁定脚下。':appearance[state.path.id];
    $('timeline').max=e.duration*1000;$('attack-mode').disabled=e.self;
    $('combo').disabled=e.self||['hit','break'].includes(e.id);state.combo=!$('combo').disabled&&$('combo').checked;
    $('stage-caption').textContent=e.self?'玩家中心锚点 · 增益随主体移动':e.multi?'实际受影响目标 · 独立命中锚点':'目标中心锚点 · 地面层对齐脚下';
    document.querySelectorAll('.effect-button').forEach(b=>b.setAttribute('aria-pressed',String(b.dataset.effect===e.id)));
    document.querySelectorAll('[data-common]').forEach(b=>b.setAttribute('aria-pressed',String(b.dataset.common===e.id)));restart();
  }
  function selectPath(p) {
    state.path=p;document.documentElement.style.setProperty('--accent',p.color);
    document.documentElement.style.setProperty('--accent-rgb',p.color.match(/[a-f\d]{2}/gi).map(v=>parseInt(v,16)).join(','));
    $('path-name').textContent=p.name;$('path-icon').src=assetRoot+p.id+'.png';$('path-icon').alt=p.name+'命途图标';$('scene-kicker').textContent=p.name+' · '+p.subtitle;
    $('palette-name').textContent=p.palette.label;$('palette-swatches').replaceChildren();[p.palette.main,p.palette.light,p.palette.accent].forEach(c=>{const el=document.createElement('i');el.style.background=c;el.title=c;$('palette-swatches').append(el);});
    document.querySelectorAll('.path-button').forEach(b=>b.setAttribute('aria-pressed',String(b.dataset.path===p.id)));
    $('effects').replaceChildren();p.effects.forEach(e=>{const b=document.createElement('button');b.type='button';b.className='effect-button';b.dataset.effect=e.id;const n=document.createElement('span'),tier=document.createElement('span');n.textContent=e.name;tier.textContent=e.tier;b.append(n,tier);b.addEventListener('click',()=>selectEffect(e));$('effects').append(b);});
    selectEffect(p.effects[p.effects.length-1]);
  }
  paths.forEach((p,i)=>{const b=document.createElement('button');b.type='button';b.className='path-button';b.dataset.path=p.id;const icon=document.createElement('img'),name=document.createElement('span'),num=document.createElement('span');icon.src=assetRoot+p.id+'.png';icon.alt='';name.textContent=p.name;num.className='path-number';num.textContent=String(i+1).padStart(2,'0');b.append(icon,name,num);b.addEventListener('click',()=>selectPath(p));$('paths').append(b);});
  $('replay').addEventListener('click',restart);$('pause').addEventListener('click',()=>{state.playing=!state.playing;playback();});
  $('timeline').addEventListener('input',e=>{state.time=Number(e.target.value)/1000;state.playing=false;playback();draw();});
  $('attack-mode').addEventListener('change',e=>{state.attack=e.target.value;restart();});$('speed').addEventListener('change',e=>state.speed=Number(e.target.value));
  $('density').addEventListener('change',e=>{state.detail=e.target.value==='0.4'?18:36;draw();});
  $('camera').addEventListener('change',e=>{state.zoom=Number(e.target.value);$('camera-note').textContent=e.target.selectedOptions[0].textContent;draw();});
  $('view').addEventListener('change',e=>{state.view=e.target.value;draw();});$('anchor-mode').addEventListener('change',e=>{state.follow=e.target.value==='follow';draw();});
  $('anchors').addEventListener('change',e=>{state.anchors=e.target.checked;draw();});$('movement').addEventListener('change',e=>{state.movement=e.target.checked;draw();});
  $('crowd').addEventListener('change',e=>{state.crowd=e.target.checked;draw();});$('combo').addEventListener('change',()=>{state.combo=$('combo').checked&&!$('combo').disabled;restart();});
  document.querySelectorAll('[data-common]').forEach(b=>b.addEventListener('click',()=>selectEffect(common[b.dataset.common])));
  document.addEventListener('keydown',e=>{if(e.code==='Space'&&e.target===document.body){e.preventDefault();$('pause').click();}});
  const params=new URLSearchParams(location.search);selectPath(paths.find(p=>p.id===params.get('path'))||paths[0]);
  const effect=state.path.effects.find(e=>e.id===params.get('effect'))||common[params.get('effect')];if(effect)selectEffect(effect);
  if(params.get('attack')==='ranged'){state.attack='ranged';$('attack-mode').value='ranged';}
  if(params.has('t')){state.time=clamp(Number(params.get('t'))||0,0,state.effect.duration);state.playing=false;playback();draw();}
  else if(matchMedia('(prefers-reduced-motion: reduce)').matches){state.time=.9;state.playing=false;$('loop').checked=false;playback();draw();}
  let last=performance.now();function frame(now){const dt=Math.min(.05,(now-last)/1000);last=now;if(state.playing){state.time+=dt*state.speed;if(state.time>=state.effect.duration){if($('loop').checked)state.time=0;else{state.time=state.effect.duration;state.playing=false;playback();}}draw();}requestAnimationFrame(frame);}requestAnimationFrame(frame);
  document.documentElement.dataset.prototypeReady='true';
})();
