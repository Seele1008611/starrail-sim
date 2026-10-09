(() => {
  'use strict';
  // Ornamental geometry only: colours, anchor and timing come from the existing preview.
  window.combatGrandMeshes = kit => {
    const {path,a,c,foot,p,e,age,attack,detail,P,add,mul,cross,ease,clamp,
      facet,ribbon,stroke,curve,orbit,gem,arrow,leafMesh,tint,player,extras}=kit;
    const strong=!!e.strong,grow=ease((age-.14)/.45),late=clamp(age-.55);
    const ring3=(root,r,xAxis,yAxis,birth,turn=0,span=Math.PI*2,color=p.main,alpha=.36,life=1)=>
      curve(root,f=>add(mul(xAxis,Math.cos(turn+f*span)*r),mul(yAxis,Math.sin(turn+f*span)*r)),
        .021,color,alpha,birth,life,cross(xAxis,yAxis));
    const radial=(theta,r,y=0)=>P(Math.cos(theta)*r,y,Math.sin(theta)*r);
    const ticks=(root,r,xAxis,yAxis,count,birth,turn=0)=>{
      for(let i=0;i<count;i++) {
        const theta=turn+i*Math.PI*2/count,dir=add(mul(xAxis,Math.cos(theta)),mul(yAxis,Math.sin(theta)));
        ribbon([add(root,mul(dir,r)),add(root,mul(dir,r+(i%3===0?.13:.06)))],.014,
          i%3?p.main:p.accent,.48,birth+i*.006,.85,cross(xAxis,yAxis));
      }
    };
    const star=(root,r,color,birth,alpha=.55)=>{
      const coords=Array.from({length:9},(_,i)=>{const theta=i*Math.PI/4;
        const size=i%2?r*.2:r;return [Math.sin(theta)*size,Math.cos(theta)*size];});
      stroke(root,coords,.026,color,alpha,birth,.85);
    };
    switch(path) {
      case 'hunt': break; // Hunt's oblique arrow rain has its own choreography.
      case 'preservation': {
        const owner=e.id==='counter'?player:a,base=add(owner,P(0,.07,0));
        // A low fortress with open panels leaves the protected character clearly visible.
        for(let i=0;i<6;i++) {
          const angle=i*Math.PI/3,dir=radial(angle,1),side=radial(angle+Math.PI/2,1);
          const center=add(base,mul(dir,1.27));
          const pt=(x,y)=>add(add(center,mul(side,x)),P(0,y,0));
          ribbon([pt(-.36,0),pt(-.36,.6*grow),pt(-.2,.6*grow),pt(-.2,.82*grow),
            pt(.2,.82*grow),pt(.2,.6*grow),pt(.36,.6*grow),pt(.36,0)],.024,p.main,.46,.26+i*.04,1.03,dir);
          facet([pt(-.32,.12),pt(.32,.12),pt(.32,.5*grow),pt(-.32,.5*grow)],p.main,.07,.3+i*.04,.95);
          gem(add(center,P(0,.88*grow,0)),.045,p.accent,.4+i*.035,.9,angle+age*.15);
        }
        for(let layer=0;layer<2;layer++) {
          const hex=Array.from({length:7},(_,i)=>add(base,radial(i*Math.PI/3,1.4+layer*.14)));
          ribbon(hex,.021,p.main,.32,.24+layer*.11,1.05,P(0,1,0));
        }
        break;
      }
      case 'destruction': {
        if(e.id==='rage') {
          for(let i=0;i<3;i++)gem(add(c,radial(i*Math.PI*2/3,.6,.45+late*.3)),.03,p.accent,.25+i*.05,.45,age);
          break;
        }
        const tongues=strong?8:5;
        for(let i=0;i<tongues;i++) {
          const angle=i*Math.PI*2/tongues,dir=radial(angle,1),base=add(foot,mul(dir,1.05));
          leafMesh(base,P(dir.x*.38,(strong?1.65:1.1),dir.z*.38),.12,p.main,.28+i*.025,.87);
          curve(base,f=>P(dir.x*f*.4+Math.sin(f*7-age*3)*.06,f*(strong?1.7:1.15),dir.z*f*.4),
            .025,p.accent,.47,.36+i*.025,.75,radial(angle+Math.PI/2,1));
          gem(add(base,P(dir.x*late*.15,.4+late*.65,dir.z*late*.15)),.035,p.accent,.46+i*.025,.8,age*2+i);
        }
        if(strong)for(let layer=0;layer<2;layer++) {
          const jag=Array.from({length:17},(_,i)=>add(foot,radial(i*Math.PI/8,(i%2?1.25:1.6)+layer*.15)));
          ribbon(jag,.022,p.main,.38,.38+layer*.12,.8,P(0,1,0));
        }
        break;
      }
      case 'erudition': {
        const axes=[[P(1,0,0),P(0,1,0)],[P(1,.25,0),P(0,.4,1)],[P(.25,0,1),P(0,1,0)]];
        for(let layer=0;layer<(strong?3:2);layer++) {
          const [x,y]=axes[layer],r=1.1+layer*.15;
          ring3(c,r,x,y,.24+layer*.085,age*(layer%2?-.28:.3),Math.PI*1.78);
          ticks(c,r,x,y,detail<24?12:24,.3+layer*.07,age*(layer%2?-.28:.3));
        }
        for(let i=0;i<4;i++) {
          const angle=i*Math.PI/2,node=add(c,P(Math.cos(angle)*1.48,Math.sin(angle)*1.1,.05));
          stroke(node,[[-.12,0],[0,.15],[.12,0],[0,-.15],[-.12,0]],.018,p.accent,.5,.4+i*.045,.82);
        }
        if(strong)extras.forEach((target,i)=>{const mark=add(target,P(0,1.05,0));
          ring3(mark,.62,P(1,0,0),P(0,1,0),.42+i*.12,age*.4,Math.PI*1.6,p.main,.38,.8);});
        break;
      }
      case 'nihility': {
        const fold=clamp(age/1.3),r=1.38-fold*.28;
        for(let band=0;band<(strong?4:3);band++) {
          const spin=band*Math.PI/2-age*.7;
          curve(c,f=>{const t=spin+f*Math.PI*1.4;return P(Math.cos(t)*(r+band*.05),Math.sin(t)*(.3+band*.12),Math.sin(t)*r*.75);},
            band%2?.025:.06,band%2?p.shade:p.main,.38,.23+band*.07,.98,P(0,1,0));
        }
        // Dark annular fragments describe the eclipse without painting a disc across the actor.
        for(let i=0;i<12;i++) {
          const t=i*Math.PI/6-age*.35,dt=.17;
          const v=(angle,radius)=>add(c,P(Math.cos(angle)*radius,Math.sin(angle)*radius*.58,0));
          facet([v(t,r),v(t+dt,r),v(t+dt,r+.12),v(t,r+.12)],p.shade,.36,.3+i*.012,.95);
        }
        if(strong)for(let i=0;i<3;i++)orbit(foot,.9+grow*.6+i*.13,p.main,.48+i*.08,.28,.78,age*.4+i*2,Math.PI);
        break;
      }
      case 'harmony': {
        for(const sign of [-1,1])for(let row=0;row<(strong?5:3);row++) {
          curve(c,f=>P(sign*(.4+f*1.55),Math.sin(f*Math.PI)*(.4+row*.07)+(row-2)*.14,
            Math.sin(f*Math.PI)*.22+Math.sin(f*7-age*4)*.05),.018,p.main,.4,.24+row*.065,.95);
          const note=add(c,P(sign*(1.38+row*.09),.1+row*.17+late*.18,.15));
          stroke(note,[[0,0],[.08,.28],[.26,.22]],.024,p.accent,.5,.38+row*.055,.8);
          curve(note,f=>P(Math.cos(f*Math.PI*2)*.06,Math.sin(f*Math.PI*2)*.04,0),.018,p.accent,.52,.38+row*.055,.8);
        }
        for(let beat=0;beat<3;beat++)for(let sector=0;sector<4;sector++)
          orbit(foot,.95+beat*.18,p.accent,.32+beat*.13,.26,.72,sector*Math.PI/2+age*.2,Math.PI*.33);
        break;
      }
      case 'abundance': {
        // A branching life canopy and opening petals, anchored to the healed player.
        for(const sign of [-1,1])for(let branch=0;branch<(strong?4:3);branch++) {
          const start=add(c,P(sign*.32,-.65+branch*.33,.1)),tip=add(c,P(sign*(.98+branch*.14),.05+branch*.43,.15));
          ribbon([start,add(start,P(sign*.24,.36,0)),tip],.025,p.main,.46,.25+branch*.06,.98);
          leafMesh(tip,P(sign*.34,.32,.12),.12,p.main,.4+branch*.06,.9);
          gem(add(tip,P(0,.12,0)),.025,p.accent,.53+branch*.055,.8,age);
        }
        if(strong)for(let i=0;i<12;i++) {
          const theta=i*Math.PI/6,dir=radial(theta,1),base=add(foot,mul(dir,.12));
          leafMesh(base,P(dir.x*1.65,.25,dir.z*1.65),.15,p.main,.32+i*.025,1.02);
          gem(add(foot,radial(theta,1.5,.16)),.035,p.accent,.58+i*.018,.75,theta);
        }
        break;
      }
      case 'remembrance': {
        const planes=strong?3:2;
        for(let i=0;i<planes;i++) {
          const theta=i*Math.PI/planes+age*.22,x=P(Math.cos(theta),0,Math.sin(theta));
          const points=[[-.65,.83],[.65,.83],[0,0],[-.65,-.83],[.65,-.83],[0,0],[-.65,.83]]
            .map(([u,v])=>add(c,add(mul(x,u),P(0,v,0))));
          ribbon(points,.023,p.main,.36,.28+i*.11,.92,cross(x,P(0,1,0)));
        }
        const count=strong?10:6;
        for(let i=0;i<count;i++) {
          const theta=i*Math.PI*2/count+age*.25;
          gem(add(c,radial(theta,1.26+late*.12,.4+Math.sin(theta)*.4)),i%2?.065:.1,p.main,.36+i*.027,.95,theta+age*.5);
        }
        if(strong)ring3(c,1.47,P(1,0,0),P(0,1,0),.48,age*.2,Math.PI*1.65,p.main,.28,.9);
        break;
      }
      case 'elation': {
        const cards=strong?5:3;
        for(let i=0;i<cards;i++) {
          const theta=(i-(cards-1)/2)*.35+age*.12;
          const card=add(c,P(Math.sin(theta)*1.85,.9+Math.cos(theta)*.2,.22));
          const x=P(Math.cos(theta),-.15*Math.sin(theta),Math.sin(theta)*.25),y=P(-Math.sin(theta)*.15,1,0);
          const corner=(u,v)=>add(card,add(mul(x,u),mul(y,v)));
          const outline=[corner(-.16,-.27),corner(.16,-.27),corner(.16,.27),corner(-.16,.27),corner(-.16,-.27)];
          facet(outline.slice(0,4),p.main,.1,.35+i*.045,.95,p.main+'80');
          ribbon(outline,.02,i%2?p.accent:p.main,.5,.35+i*.045,.95,cross(x,y));
          star(card,.08,p.accent,.43+i*.045,.52);
        }
        // A small comedy-mask smile in the upper halo, leaving the target's face unpainted.
        const mask=add(c,P(0,1.12,.2));
        curve(mask,f=>P((f-.5)*.68,Math.sin(f*Math.PI)*-.2,0),.026,p.main,.55,.5,.75);
        for(const sign of [-1,1])stroke(mask,[[sign*.1,.15],[sign*.18,.2],[sign*.26,.15]],.02,p.accent,.52,.52,.72);
        for(let i=0;i<(strong?14:8);i++) {
          const theta=i*2.4,t=clamp(age-.46),r=1.15+t*.65;
          const center=add(c,radial(theta,r,.4-t*.35+Math.sin(theta)*.35));
          leafMesh(center,P(Math.cos(theta+age)*.1,.05,Math.sin(theta+age)*.1),.027,i%4?p.main:p.accent,.46+i*.016,.85);
        }
        break;
      }
    }
    flourish(kit);
  };

  function flourish(kit) {
    const {path,a,c,foot,p,e,age,attack,P,add,mul,cross,ease,clamp,facet,ribbon,curve,gem,leafMesh,player}=kit;
    if(path==='hunt')return;
    const strong=!!e.strong,up=P(0,1,0),late=ease((age-.65)/.8);
    const polar=(angle,r,y=0)=>P(Math.cos(angle)*r,y,Math.sin(angle)*r);
    const line=(points,width,color,birth,life=.85,alpha=.45,normal=P(0,0,1))=>
      ribbon(points,width,color,alpha,birth,life,normal);
    const diamond=(root,xAxis,yAxis,size,birth,color=p.main)=>{
      const point=(u,v)=>add(root,add(mul(xAxis,u*size),mul(yAxis,v*size)));
      const top=point(0,1),bottom=point(0,-1),left=point(-.45,0),right=point(.45,0);
      facet([top,left,bottom],color,.32,birth,.85);
      facet([top,bottom,right],p.shade,.42,birth,.85);
      line([top,left,bottom,right,top],.012,p.accent,birth,.85,.45,cross(xAxis,yAxis));
    };
    // The entry carries the path's colour, but uses different close/ranged motion.
    if(!e.self&&e.id!=='rage')for(let layer=0;layer<(strong?3:2);layer++) {
      if(attack==='ranged')curve(c,f=>P(-1.7+f*1.7,Math.sin(f*Math.PI*2+layer)*.1,
        Math.sin(f*Math.PI)*(.16+layer*.1)),.018,p.main,.4,.02+layer*.035,.48);
      else curve(c,f=>{
        const angle=-1.1+f*2.2+layer*.16;
        return P(Math.sin(angle)*(.7+layer*.09),Math.cos(angle)*.35-.12,Math.cos(angle)*.35);
      },.022,p.main,.4,.025+layer*.045,.48,up);
    }
    switch(path) {
      case 'preservation': {
        const root=e.id==='counter'?add(player,P(0,1.05,0)):c;
        for(let i=0;i<8;i++) {
          const angle=i*Math.PI/4,radial=polar(angle,1),side=polar(angle+Math.PI/2,1);
          const center=add(root,mul(radial,.92+late*.08));
          diamond(center,side,up,.18,.08+i*.028);
          line([add(center,P(0,-.24,0)),add(center,P(0,-.38,0))],.019,p.main,.2+i*.025,.9,.5,radial);
          for(let k=0;k<2;k++)curve(root,f=>polar(angle+f*.3,1.13+k*.12,-.65+k*1.2),
            .025,p.main,.42,.7+k*.1+i*.012,.95,up);
        }
        if(e.id==='counter')for(let wave=0;wave<3;wave++) {
          const t=ease((age-.38-wave*.09)/.36),center=add(mul(root,1-t),mul(c,t));
          diamond(center,P(1,0,0),up,.28+wave*.045,.38+wave*.09,p.accent);
        }
        break;
      }
      case 'destruction': {
        const rage=e.id==='rage',count=rage?4:strong?10:7;
        for(let i=0;i<count;i++) {
          const angle=i*Math.PI*2/count,dir=polar(angle,1),side=polar(angle+Math.PI/2,1);
          const r=rage?.45:.75+late*.7,root=add(foot,mul(dir,r));
          const height=rage?.48:strong?1.35:.85;
          diamond(add(root,P(0,height*.45,0)),side,up,rage?.09:.14,.22+i*.025,p.accent);
          curve(root,f=>add(mul(dir,f*.3),P(Math.sin(f*9+i)*.06,f*height,0)),
            .02,p.main,.42,.55+i*.018,.9,side);
          if(!rage)line([add(root,mul(side,-.12)),add(root,P(0,.18,0)),add(root,mul(side,.12))],
            .025,p.shade,.78+i*.018,.8,.5,dir);
        }
        if(!rage)for(let i=0;i<12;i++) {
          const angle=i*2.39996,t=clamp((age-.6)/1.1);
          const root=add(foot,polar(angle,.6+t*1.1,.25+Math.sin(t*Math.PI)*(i%3*.2+.3)));
          facet([root,add(root,P(.04,.12,0)),add(root,P(-.035,.025,.035))],p.accent,.48,.6+i*.014,1.05);
        }
        break;
      }
      case 'erudition': {
        for(let i=0;i<(strong?8:5);i++) {
          const angle=i*Math.PI*2/(strong?8:5)+age*.17,root=add(c,polar(angle,1.25,.25+Math.sin(angle)*.35));
          const side=polar(angle+Math.PI/2,1);
          diamond(root,side,up,.1,.2+i*.025);
          for(let row=0;row<3;row++)line([add(root,add(mul(side,-.13),P(0,.2+row*.05,0))),
            add(root,add(mul(side,.08+(row%2)*.08),P(0,.2+row*.05,0)))],.01,p.main,.38+i*.025,.8,.4,polar(angle,1));
          const target=add(c,P(0,(i%3-1)*.17,0));
          line([root,add(root,P(0,.16,0)),target],.012,p.main,.72+i*.025,.6,.35,up);
        }
        for(let sector=0;sector<6;sector++)curve(c,f=>{
          const theta=sector*Math.PI/3+f*.45,r=.6+late*.8;
          return P(Math.cos(theta)*r,Math.sin(theta)*r,Math.sin(theta*2)*.28);
        },.018,p.accent,.45,.95+sector*.02,.8);
        break;
      }
      case 'nihility': {
        for(let i=0;i<(strong?16:10);i++) {
          const theta=i*2.39996-age*.35,r=1.45-late*.65;
          const root=add(c,polar(theta,r,Math.sin(theta*1.7)*.6));
          const side=polar(theta+Math.PI/2,1);
          diamond(root,side,up,.1,.3+i*.016,p.shade);
          curve(c,f=>polar(theta-f*.75,r*(1-f*.65),Math.sin(theta*1.7)*.6*(1-f)),
            .017,p.main,.35,.68+i*.012,.8,up);
        }
        for(let i=0;i<6;i++) {
          const theta=i*Math.PI/3;
          curve(c,f=>polar(theta+f*1.3,.28+f*.75,(f-.5)*.75),
            .024,p.shade,.55,1+i*.03,.85,up);
        }
        break;
      }
      case 'harmony': {
        for(let i=0;i<(strong?8:5);i++) {
          const theta=i*Math.PI*2/(strong?8:5),side=polar(theta+Math.PI/2,1);
          const node=add(c,polar(theta,1.05,.25+late*.65));
          line([node,add(node,P(0,.25,0)),add(node,add(mul(side,.17),P(0,.2,0)))],
            .019,p.accent,.3+i*.045,1.0,.55,polar(theta,1));
          gem(node,.034,p.accent,.3+i*.045,1,theta);
          curve(c,f=>polar(theta+f*.9,1.02+Math.sin(f*Math.PI)*.22,(f-.3)*.7+late*.2),
            .016,p.main,.4,.55+i*.03,.95,up);
        }
        for(let beat=0;beat<3;beat++)for(let i=0;i<6;i++) {
          const angle=i*Math.PI/3,root=add(foot,polar(angle,1.1+beat*.2,.06));
          line([root,add(root,P(0,.09+beat*.04,0))],.021,p.accent,.8+beat*.16,.62,.55,polar(angle,1));
        }
        break;
      }
      case 'abundance': {
        const count=strong?10:6;
        for(let i=0;i<count;i++) {
          const angle=i*Math.PI*2/count,dir=polar(angle,1),base=add(foot,mul(dir,.72));
          curve(base,f=>add(mul(dir,Math.sin(f*Math.PI)*.27),P(0,f*1.55,0)),
            .018,p.main,.4,.32+i*.03,1.15,polar(angle+Math.PI/2,1));
          for(let bud=0;bud<2;bud++) {
            const root=add(base,P(0,.55+bud*.55,0));
            leafMesh(root,add(mul(dir,.25),P(0,.18,0)),.07,p.main,.52+i*.025+bud*.08,1.05);
          }
          const t=ease((age-.62-i*.02)/.8),r=.65*(1-t);
          gem(add(c,polar(angle,r,1.25-t*1.1)),.026,p.accent,.62+i*.02,1,angle);
        }
        if(e.id==='manna')for(let i=0;i<6;i++)diamond(add(c,polar(i*Math.PI/3,.85,.2)),
          polar(i*Math.PI/3+Math.PI/2,1),up,.14,.65+i*.03,p.accent);
        break;
      }
      case 'remembrance': {
        for(let i=0;i<(strong?10:6);i++) {
          const theta=i*Math.PI*2/(strong?10:6),side=polar(theta+Math.PI/2,1);
          const root=add(c,polar(theta,.95+late*.18,Math.sin(theta)*.45));
          diamond(root,side,up,.13,.26+i*.028);
          for(let echo=0;echo<2;echo++) {
            const r=.8+echo*.16;
            curve(c,f=>polar(theta+f*.42,r,.2+Math.sin(f*Math.PI)*.25),
              .018,echo?p.shade:p.main,.4,.66+echo*.2+i*.015,.75,up);
          }
          const t=ease((age-1.12)/.65);
          gem(add(root,P(0,-t*.55,0)),.018,p.accent,1.12+i*.015,.85,theta);
        }
        break;
      }
      case 'elation': {
        const count=strong?12:7;
        for(let i=0;i<count;i++) {
          const theta=i*2.39996,t=clamp((age-.45-i*.015)/1.05),r=.55+t*1.05;
          const root=add(c,polar(theta,r,.2+Math.sin(t*Math.PI)*(.4+i%3*.12)-t*.35));
          const side=polar(theta+Math.PI/2,1),color=i%4?p.main:p.accent;
          diamond(root,side,up,.07,.45+i*.015,color);
          curve(root,f=>add(mul(side,f*.17),P(Math.sin(f*7+age*3)*.05,-f*.2,0)),
            .018,color,.45,.66+i*.02,.8,polar(theta,1));
        }
        for(let i=0;i<5;i++) {
          const theta=i*Math.PI*2/5,root=add(c,polar(theta,1.05,.9));
          line([add(root,P(-.06,.12,0)),root,add(root,P(.06,.12,0))],.023,
            i%2?p.main:p.accent,1.02+i*.035,.75,.55);
        }
        break;
      }
    }
  }
})();
