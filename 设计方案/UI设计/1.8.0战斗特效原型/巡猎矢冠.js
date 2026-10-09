(() => {
  'use strict';
  // Historical module name retained so the existing page loads the replacement directly.
  window.drawHuntCrown = kit => {
    const {c,foot,p,e,age,attack,P,add,mul,cross,ease,clamp,facet,ribbon,curve,gem}=kit;
    const strong=!!e.strong;
    const up=P(0,1,0);
    const norm=v=>mul(v,1/(Math.hypot(v.x,v.y,v.z)||1));
    const trace=(points,width,color,alpha,birth,life,normal=P(0,0,1))=>
      ribbon(points,width,color,alpha,birth,life,normal);

    function arrow(tip,dir,size,birth,life,central=false) {
      const reference=Math.abs(dir.y)>.8?P(1,0,0):up;
      const depth=norm(cross(dir,reference));
      const side=norm(cross(depth,dir));
      const at=(length,x=0,z=0)=>add(tip,add(mul(dir,length*size),
        add(mul(side,x*size),mul(depth,z*size))));
      const apex=at(.08),shoulders=[at(-.57,.23),at(-.57,0,.23),at(-.57,-.23),at(-.57,0,-.23)];
      const ridge=at(-.4,0,0);
      for(let i=0;i<4;i++) {
        facet([apex,shoulders[i],shoulders[(i+1)%4]],i%2?p.shade:p.main,
          central?.82:.66,birth,life);
        trace([shoulders[i],apex],central?.014:.01,p.accent,central?.8:.42,birth,life,
          i%2?side:depth);
        facet([shoulders[i],ridge,at(-.82,(i===0?.12:i===2?-.12:0),(i===1?.12:i===3?-.12:0))],
          p.main,.4,birth,life);
      }
      trace([at(-2.35),at(-.56)],.045*size,p.main,central?.82:.55,birth,life,side);
      trace([at(-2.35),at(-.56)],.032*size,p.main,central?.66:.42,birth,life,depth);
      if(central)trace([at(-2.15),at(-.58)],.012*size,p.accent,.86,birth,life,side);
      // Four small vanes make a true arrow from every viewing direction.
      for(let i=0;i<4;i++) {
        const angle=i*Math.PI/2,dx=Math.cos(angle),dz=Math.sin(angle);
        facet([at(-1.78,dx*.045,dz*.045),at(-2.22,dx*.3,dz*.3),at(-2.05,dx*.045,dz*.045)],
          i%2?p.shade:p.main,central?.62:.4,birth,life);
        trace([at(-2.22,dx*.3,dz*.3),at(-1.78,dx*.045,dz*.045)],.012*size,p.accent,
          central?.65:.35,birth,life,i%2?side:depth);
      }
      if(central) {
        // Segmented silver-blue inlays emphasise the finishing arrow's length.
        for(let k=0;k<3;k++) {
          const shaft=-.85-k*.4;
          for(let i=0;i<4;i++) {
            const a=i*Math.PI/2,dx=Math.cos(a),dz=Math.sin(a);
            trace([at(shaft-.13,dx*.08,dz*.08),at(shaft,dx*.13,dz*.13),
              at(shaft+.13,dx*.08,dz*.08)],.013*size,p.accent,.55,birth+.02*k,life-.02*k,
              i%2?side:depth);
          }
        }
      }
    }

    function impact(root,birth,central=false) {
      const elapsed=age-birth;
      if(elapsed<0||elapsed>(central?.85:.4))return;
      const expansion=ease(elapsed/.48),radius=(central?1.35:.34)*expansion;
      const sectors=central?8:4;
      for(let i=0;i<sectors;i++) {
        const angle=i*Math.PI*2/sectors;
        curve(root,f=>P(Math.cos(angle+f*.32)*radius,.012,
          Math.sin(angle+f*.32)*radius),central?.035:.018,p.main,central?.64:.4,birth,.6,up);
        const direction=P(Math.cos(angle),0,Math.sin(angle));
        trace([add(root,mul(direction,radius*.7)),add(root,add(mul(direction,radius),P(0,.08,0)))],
          .019,p.accent,central?.54:.3,birth,.42,up);
        gem(add(root,add(mul(direction,radius*.8),P(0,Math.sin(expansion*Math.PI)*.3,0))),
          central?.033:.018,p.main,birth+i*.012,.5,angle);
      }
      if(central) {
        for(let i=0;i<4;i++) {
          const a=i*Math.PI/2;
          const side=P(Math.cos(a),0,Math.sin(a));
          trace([add(root,mul(side,.09)),add(root,add(mul(side,.3+expansion*.25),P(0,.65*(1-expansion),0))),
            add(root,add(mul(side,.48+expansion*.35),P(0,.14,0)))],
            .027,p.main,.55,birth,.5,cross(side,up));
        }
        // Two offset broken shock fronts and radial crystal splinters open after impact.
        for(let layer=0;layer<2;layer++)for(let i=0;i<8;i++) {
          const a=i*Math.PI/4+layer*.2,r=(.3+ease((elapsed-layer*.075)/.6)*1.65);
          curve(root,f=>P(Math.cos(a+f*.22)*r,.018+layer*.025,Math.sin(a+f*.22)*r),
            layer?.016:.028,layer?p.shade:p.main,layer?.36:.52,birth+layer*.075,.8,up);
        }
        for(let i=0;i<(strong?24:14);i++) {
          const a=i*2.39996,t=ease(elapsed/.65),reach=(.55+(i%5)*.19)*t;
          const base=add(root,P(Math.cos(a)*reach,Math.sin(t*Math.PI)*(.28+(i%4)*.13),Math.sin(a)*reach));
          const spine=P(Math.cos(a)*.06,.12,Math.sin(a)*.06),edge=P(-Math.sin(a)*.035,0,Math.cos(a)*.035);
          facet([base,add(base,spine),add(base,edge)],i%3?p.main:p.shade,.52,birth+i*.006,.78);
          trace([base,add(base,mul(spine,-.65))],.012,p.main,.35,birth+.05,.7);
        }
        // The final constellation opens above the blast as four detached arrow-point echoes.
        for(let i=0;i<4;i++) {
          const angle=i*Math.PI/2+.3,t=ease(elapsed/.7),r=.35+t*.55;
          const center=add(root,P(Math.cos(angle)*r,.35+Math.sin(t*Math.PI)*.65,Math.sin(angle)*r));
          const side=P(-Math.sin(angle),0,Math.cos(angle));
          trace([add(center,mul(side,-.08)),add(center,P(0,.18,0)),add(center,mul(side,.08))],
            .019,p.accent,.42,birth+.11,.68,P(Math.cos(angle),0,Math.sin(angle)));
        }
      }
    }

    function fall(root,birth,size,central=false,offset=P(0,2.9,0)) {
      const duration=central?.32:.27,hit=birth+duration;
      const local=age-birth;
      // Impact has its own lifetime, independent of the disappearing arrow.
      impact(root,hit,central);
      if(local<0||local>duration+(central?.48:.22))return;
      const t=clamp(local/duration),tip=add(root,mul(offset,1-t*t));
      const dir=norm(mul(offset,-1));
      const depth=norm(cross(dir,Math.abs(dir.y)>.8?P(1,0,0):up));
      const side=norm(cross(depth,dir));
      arrow(tip,dir,size,birth,duration+(central?.43:.19),central);
      // Slim coloured flight trails stay separate from the faceted arrow body.
      for(const sign of [-1,1]) {
        const shift=mul(depth,sign*.055*size);
        trace([add(tip,add(shift,mul(dir,-.38))),
          add(tip,add(shift,mul(dir,central?-2.8:-1.65)))],
          central?.022:.012,p.main,central?.43:.27,birth,duration+.18,depth);
      }
      for(const sign of [-1,1]) {
        curve(tip,f=>add(mul(dir,-(.3+f*(central?2.45:1.35))),
          add(mul(depth,sign*Math.sin(f*Math.PI)*(central?.25:.12)),
            mul(side,Math.sin(f*Math.PI*2)*.045))),
          central?.018:.011,p.main,central?.4:.3,birth+.025,duration+.16,depth);
      }
      const sparks=central?12:5;
      for(let i=0;i<sparks;i++) {
        const a=i*2.39996,behind=.32+i*(central?.15:.13);
        const spread=central?.14:.075;
        const pos=add(tip,add(mul(dir,-behind),add(mul(side,Math.cos(a)*spread),mul(depth,Math.sin(a)*spread))));
        gem(pos,i%3===0?.021:.012,i%3?p.main:p.accent,birth+.015*i,duration+.17,a);
      }
      const burst=clamp((age-hit)/.24);
      for(let i=0;i<(central?8:3);i++) {
        const a=i*Math.PI*2/(central?8:3);
        const back=add(mul(dir,-(.15+burst*.6)),add(mul(side,Math.cos(a)*burst*.22),mul(depth,Math.sin(a)*burst*.22)));
        trace([add(root,mul(back,.55)),add(root,back)],.014,p.main,.45,hit,.28,depth);
      }
    }

    const count=strong?8:4,centralBirth=strong?1.05:.65;
    for(let i=0;i<count;i++) {
      // Alternating opposite sectors creates a surround instead of a single flat curtain.
      const index=i%2===0?i/2:count/2+(i-1)/2;
      const angle=index*Math.PI*2/count+.32;
      const radius=strong?2.05:1.75;
      const offset=P(Math.cos(angle)*radius,2.2,Math.sin(angle)*radius);
      const root=c;
      const birth=.04+i*(strong?.085:.1);
      // Tiny split gates announce each direction at the arrow's launch point.
      const start=add(c,offset),tangent=P(-Math.sin(angle),0,Math.cos(angle));
      for(const sign of [-1,1])trace([add(start,mul(tangent,sign*.22)),
        add(start,add(mul(tangent,sign*.13),P(0,.1,0))),add(start,mul(tangent,sign*.06))],
        .018,p.main,.48,Math.max(0,birth-.04),.22,norm(offset));
      trace([add(root,P(-.09,.01,0)),add(root,P(0,.01,-.09)),add(root,P(.09,.01,0)),
        add(root,P(0,.01,.09)),add(root,P(-.09,.01,0))],.016,p.main,.45,
        Math.max(0,birth-.08),.4,up);
      fall(root,birth,(strong?.43:.38)*1.5,false,offset);
      // The afterimage also contracts down the same oblique path into the target.
      const converge=birth+.27;
      const t=ease((age-converge)/.22);
      const head=add(c,mul(offset,.24*(1-t)));
      trace([head,add(head,mul(offset,.1*(1-t)))],
        .018,p.main,.32,converge,.24,norm(cross(norm(offset),up)));
    }
    if(attack==='ranged'&&age<.48) {
      const travel=ease(age/.2);
      arrow(add(c,P(-1.7*(1-travel),0,0)),P(1,0,0),.5,0,.45);
    }
    // A short suspended spear point announces the final, much larger vertical shot.
    gem(add(foot,P(0,2.55,0)),.07,p.accent,centralBirth-.17,.25,0);
    for(let i=0;i<4;i++) {
      const a=i*Math.PI/2;
      trace([add(foot,P(Math.cos(a)*.24,2.55,Math.sin(a)*.24)),
        add(foot,P(Math.cos(a)*.1,2.55,Math.sin(a)*.1))],.018,p.main,.55,
        centralBirth-.17,.25,up);
    }
    const charge=centralBirth-.22;
    for(let layer=0;layer<2;layer++)for(let i=0;i<6;i++) {
      const phase=clamp((age-charge)/.3),a=i*Math.PI/3+layer*.25+phase*.22;
      const r=(.5+layer*.18)*(1-phase*.35),height=2.65+layer*.35;
      curve(foot,f=>P(Math.cos(a+f*.38)*r,height,Math.sin(a+f*.38)*r),
        .019,layer?p.shade:p.main,.48,charge+layer*.03,.42,up);
      trace([add(foot,P(Math.cos(a)*r,height,Math.sin(a)*r)),
        add(foot,P(Math.cos(a)*r*.65,height-.16,Math.sin(a)*r*.65))],
        .012,p.accent,.4,charge+.04,.35,up);
    }
    // Four slender, segmented rails descend beside the main arrow, framing its impact.
    for(let i=0;i<4;i++)for(let segment=0;segment<3;segment++) {
      const a=i*Math.PI/2+.3,r=.25+segment*.045;
      const start=centralBirth+.04+segment*.055;
      const drop=ease((age-start)/.3),y=2.65*(1-drop)+segment*.12;
      trace([add(foot,P(Math.cos(a)*r,y,Math.sin(a)*r)),
        add(foot,P(Math.cos(a)*r,y+.32,Math.sin(a)*r))],.019,
        segment===1?p.accent:p.main,segment===1?.48:.35,start,.48,P(-Math.sin(a),0,Math.cos(a)));
    }
    fall(foot,centralBirth,strong?1.05:.76,true);
  };
})();
