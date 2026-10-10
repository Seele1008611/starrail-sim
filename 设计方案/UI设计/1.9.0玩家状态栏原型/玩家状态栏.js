(() => {
  'use strict';
  const $ = id => document.getElementById(id);
  const presets = {
    early:{max:28,hp:24,shield:3,armor:7.5,tough:2.2,food:18,sat:6,rank:'初窥 · II'},
    late:{max:286,hp:232,shield:64,armor:68.4,tough:26.8,food:18,sat:8.5,rank:'极境 · VII'},
    extreme:{max:1000000,hp:768432,shield:128560,armor:4280.5,tough:1632,food:20,sat:12.5,rank:'极境 · VII'}
  };
  let state, toastTimeout, loop = null, cycle = 0, jumpFrame = 0;
  const formatter = new Intl.NumberFormat('zh-CN',{maximumFractionDigits:1});
  const full = value => formatter.format(Math.max(0,value));
  const short = value => value >= 1000000 ? `${(value/1000000).toFixed(2)}M` : value >= 10000 ? `${(value/1000).toFixed(1)}K` : full(value);
  const number = value => $('exact').checked ? full(value) : short(value);
  const ratio = (value,max) => max > 0 ? Math.max(0,Math.min(1,value/max))*100 : 0;
  function bar(id,value,max){ $(id).style.width = `${ratio(value,max)}%`; $(id).classList.toggle('empty',value<=0); }
  function progress(id,value,max){$(id).setAttribute('aria-valuenow',String(value));$(id).setAttribute('aria-valuemin','0');$(id).setAttribute('aria-valuemax',String(max));}
  function toast(text){$('toast').textContent=text;$('toast').classList.add('visible');clearTimeout(toastTimeout);toastTimeout=setTimeout(()=>$('toast').classList.remove('visible'),1800);}
  function flash(kind){$('hud').classList.remove('hit','heal','shield','eat','blocked');void $('hud').offsetWidth;$('hud').classList.add(kind);}
  function reset(){stopCharge();state={...presets[$('preset').value],air:300,mount:36,mountMax:40};cycle=0;render();}
  function render(){
    $('hpText').textContent=`${number(state.hp)} / ${number(state.max)}`;
    $('shieldText').textContent=state.shield>0?`+ ${number(state.shield)}`:'0';
    $('armorText').textContent=number(state.armor);$('toughText').textContent=number(state.tough);
    $('foodText').textContent=`${full(state.food)} / 20`;
    $('satText').textContent=full(state.sat);
    $('nutritionTile').title=`饱食度 ${full(state.food)} / 20；饱和度 ${full(state.sat)} / ${full(state.food)}，浅绿细线以20为参考`;
    $('absorptionReadout').hidden=state.shield<=0;
    $('shieldMeter').hidden=state.shield<=0;
    bar('hpFill',state.hp,state.max);bar('hpEcho',state.hp,state.max);bar('shieldFill',state.shield,state.max);
    bar('foodFill',state.food,20);bar('satFill',state.sat,20);
    progress('healthMeter',state.hp,state.max);
    $('shieldMeter').setAttribute('aria-valuetext',`${full(state.shield)}，条长度相对最大生命值${full(state.max)}`);
    progress('shieldMeter',ratio(state.shield,state.max),100);
    $('shieldMeter').setAttribute('title',`伤害吸收 ${full(state.shield)}；条长度相对最大生命值，非护盾上限`);
    $('healthMeter').classList.toggle('low',state.hp<=state.max*.25);
    $('hud').classList.toggle('critical',state.hp>0&&state.hp<=state.max*.25);
    $('nutritionTile').classList.toggle('hungry',state.food<=6);
    $('hud').classList.toggle('context-busy',$('underwater').checked&&$('mounted').checked);
    $('absorptionReadout').title=`伤害吸收 ${full(state.shield)}`;
    $('airLine').title=`氧气 ${state.air} / 300`;
    $('airLine').classList.toggle('air-low',state.air<=60);
    $('mountLine').classList.toggle('mount-low',state.mount<=state.mountMax*.25);
    $('satText').classList.toggle('depleted',state.sat<=0);
    $('mountLine').title=`坐骑生命 ${state.mount} / ${state.mountMax}`;
    $('airLine').hidden=!$('underwater').checked;bar('airFill',state.air,300);$('airText').textContent=`${state.air}`;
    $('mountLine').hidden=!$('mounted').checked;bar('mountFill',state.mount,state.mountMax);$('mountText').textContent=`${state.mount} / ${state.mountMax}`;
    $('hudAnchor').className=`hud-anchor ${$('position').value}`;
    const survive=$('mode').value==='survival', visible=!$('hidden').checked;
    $('hudAnchor').hidden=!survive||!visible||$('native').checked;
    $('vanilla').hidden=!survive||!visible||!$('native').checked;
    $('jump').hidden=!survive||!$('mounted').checked;
    $('charge').hidden=!survive||!$('mounted').checked;
    if($('jump').hidden)stopCharge();
    document.querySelector('.hotbar-zone').hidden=!visible||$('mode').value==='spectator';
    document.querySelector('.xp').hidden=!survive;
    ['boss'].forEach(id=>$(id).hidden=!visible);
    document.querySelector('.crosshair').hidden=!visible||$('mode').value==='spectator';
    document.querySelector('.status-icons').hidden=!visible||$('mode').value==='spectator';
    document.querySelector('.scene-title').hidden=!visible;
    $('toast').hidden=!visible;
    $('rawHP').textContent=`${full(state.hp)} / ${full(state.max)}`;$('rawShield').textContent=full(state.shield);
    $('rawSat').textContent=full(state.sat);$('rawDefense').textContent=`${full(state.armor)} / ${full(state.tough)}`;
    $('sceneLabel').textContent=`${$('preset').selectedOptions[0].textContent} · ${$('mode').selectedOptions[0].textContent}`;
    fit();
  }
  function fit(){
    const stage=$('stage'), anchor=$('hudAnchor'), hotbar=document.querySelector('.hotbar-zone');
    const width=hotbar.offsetWidth;
    anchor.style.width=`${width}px`;
    const effective=Math.min(1,Number($('scale').value)/100,(stage.clientWidth-24)/width);
    anchor.style.setProperty('--hud-scale',effective);
    // Centered mode starts at the hotbar left edge, always to the right of the offhand slot.
    if($('position').value==='center'){
      anchor.style.left=`${(stage.clientWidth-width*effective)/2}px`;
    }else{
      anchor.style.left='12px';
    }
    // Even corner mode sits above the entire hotbar zone; their rectangles cannot overlap.
    anchor.style.bottom=`${hotbar.offsetHeight+18}px`;
    $('scaleLabel').textContent=`${Math.round(effective*100)}%`;
    anchor.style.left=`${Math.round(parseFloat(anchor.style.left))}px`;
    anchor.style.bottom=`${Math.round(hotbar.offsetHeight+18)}px`;
  }
  function action(kind){
    if($('mode').value!=='survival'){toast('切换到生存模式预览生存状态变化');return;}
    if(kind==='danger'){
      state.shield=0;state.hp=state.max*.18;state.food=5;state.sat=0;render();flash('hit');toast('低血与饥饿状态预览');
    }else if(kind==='hit'){
      const damage=state.max*.18, absorbed=Math.min(state.shield,damage), actual=Math.min(state.hp,damage-absorbed);
      state.shield-=absorbed;state.hp-=actual;
      state.food=Math.max(0,state.food-1);state.sat=Math.min(state.food,Math.max(0,state.sat-1));
      if($('underwater').checked)state.air=Math.max(0,state.air-60);
      render();flash(actual>0?'hit':'blocked');toast(`伤害吸收抵挡 ${full(absorbed)} · 生命损失 ${full(actual)}`);
    }else if(kind==='heal'){
      const restored=Math.min(state.max-state.hp,state.max*.25);state.hp+=restored;render();if(restored>0)flash('heal');toast(restored>0?`恢复 ${full(restored)} 生命`:'生命已满');
    }else if(kind==='shield'){
      state.shield+=state.max*.2;render();flash('shield');toast(`伤害吸收 +${full(state.max*.2)}`);
    }else if(kind==='eat'){
      state.food=Math.min(20,state.food+4);state.sat=Math.min(state.food,state.sat+4.8);render();flash('eat');toast('进食 · 饱食度与饱和度恢复');
    }
  }
  function stopCharge(){
    cancelAnimationFrame(jumpFrame);jumpFrame=0;
    $('jumpFill').style.width='0%';$('jump').setAttribute('aria-valuenow','0');
    $('jump').title='跳跃蓄力 0%';$('jump').classList.remove('charged');
    $('charge').disabled=false;
  }
  $('charge').addEventListener('click',()=>{
    if(!$('mounted').checked||$('mode').value!=='survival')return;
    stopCharge();$('charge').disabled=true;
    const start=performance.now();
    function frame(now){
      const elapsed=now-start;
      if(elapsed>=1150){stopCharge();toast('蓄力跳跃 · 预览完成');return;}
      const percent=Math.min(100,Math.round(elapsed/8));
      $('jumpFill').style.width=`${percent}%`;
      $('jump').setAttribute('aria-valuenow',String(percent));
      $('jump').title=`跳跃蓄力 ${percent}%`;
      $('jump').classList.toggle('charged',percent===100);
      jumpFrame=requestAnimationFrame(frame);
    }
    jumpFrame=requestAnimationFrame(frame);
  });
  // This geometric room is a local composition aid, not a Minecraft screenshot.
  function world(){
    const canvas=$('world'),width=canvas.clientWidth,height=canvas.clientHeight,dpr=Math.min(window.devicePixelRatio||1,2);
    canvas.width=Math.round(width*dpr);canvas.height=Math.round(height*dpr);
    const c=canvas.getContext('2d');c.scale(dpr,dpr);
    const horizon=height*.44, grad=c.createLinearGradient(0,0,0,height);grad.addColorStop(0,'#273a43');grad.addColorStop(.44,'#71847e');grad.addColorStop(1,'#364b50');c.fillStyle=grad;c.fillRect(0,0,width,height);
    const tile=width/15;
    for(let y=0;y<horizon;y+=tile*.48)for(let x=0;x<width;x+=tile){c.fillStyle=((Math.floor(x/tile)+Math.floor(y/(tile*.48)))%3===0)?'#69817a':'#62766f';c.fillRect(x+1,y+1,tile-2,tile*.48-2);c.fillStyle='#92a59a3a';c.fillRect(x+2,y+2,tile-4,2);}
    c.fillStyle='#223c3d';c.fillRect(width*.12,0,width*.065,horizon);c.fillRect(width*.82,0,width*.065,horizon);
    c.fillStyle='#427a72';c.fillRect(width*.13,0,width*.02,horizon);c.fillRect(width*.835,0,width*.02,horizon);
    c.fillStyle='#365657';c.fillRect(width*.4,horizon*.24,width*.2,horizon*.76);
    c.fillStyle='#182e37';c.fillRect(width*.425,horizon*.33,width*.15,horizon*.67);
    c.strokeStyle='#8ba49b';c.lineWidth=4;c.strokeRect(width*.4,horizon*.24,width*.2,horizon*.76);
    for(let i=0;i<12;i++){const p=i/11,y=horizon+(height-horizon)*p*p; c.strokeStyle='#1e353e';c.lineWidth=1;c.beginPath();c.moveTo(0,y);c.lineTo(width,y);c.stroke();}
    for(let i=-8;i<=8;i++){c.strokeStyle='#1d363e';c.beginPath();c.moveTo(width*.5+i*width*.012,horizon);c.lineTo(width*.5+i*width*.15,height);c.stroke();}
    c.fillStyle='#92d4ce';c.fillRect(width*.22,horizon*.38,Math.max(12,width*.025),Math.max(12,width*.025));c.fillRect(width*.75,horizon*.38,Math.max(12,width*.025),Math.max(12,width*.025));
    c.fillStyle='#9ecdc01a';c.fillRect(width*.16,horizon,width*.13,height-horizon);c.fillRect(width*.71,horizon,width*.13,height-horizon);
    // Block-shaped training target, placed behind the crosshair.
    const x=width*.5,y=height*.49,s=height*.10;
    c.fillStyle='#7b9f93';c.fillRect(x-s*.32,y-s*.95,s*.64,s*.64);c.fillStyle='#3e747a';c.fillRect(x-s*.44,y-s*.29,s*.88,s);c.fillStyle='#27444f';c.fillRect(x-s*.4,y+s*.71,s*.33,s*.8);c.fillRect(x+s*.07,y+s*.71,s*.33,s*.8);
    c.fillStyle='#58878b';c.fillRect(x-s*.68,y-s*.22,s*.24,s*.85);c.fillRect(x+s*.44,y-s*.22,s*.24,s*.85);c.fillStyle='#36483d';c.fillRect(x-s*.2,y-s*.69,s*.1,s*.08);c.fillRect(x+s*.1,y-s*.69,s*.1,s*.08);
    fit();
  }
  ['mode','position','exact','underwater','mounted','native','hidden'].forEach(id=>$(id).addEventListener('change',render));
  $('scale').addEventListener('input',fit);
  $('preset').addEventListener('change',reset);
  $('aspect').addEventListener('change',()=>{$('stage').className=`stage ${$('aspect').value}`;world();});
  ['hit','heal','shield','eat','danger'].forEach(id=>$(id).addEventListener('click',()=>action(id)));
  $('reset').addEventListener('click',()=>{stopLoop();reset();toast('状态已重置');});
  function stopLoop(){clearInterval(loop);loop=null;$('loop').textContent='战斗循环';$('loop').setAttribute('aria-pressed','false');}
  $('loop').addEventListener('click',()=>{
    if(loop){stopLoop();return;}
    if($('mode').value!=='survival'){toast('请先切换到生存模式');return;}
    $('loop').textContent='停止循环';$('loop').setAttribute('aria-pressed','true');
    loop=setInterval(()=>{if(document.hidden||$('hidden').checked)return;action(['hit','hit','hit','heal','shield','eat'][cycle++%6]);},1500);
  });
  $('mode').addEventListener('change',stopLoop);
  document.addEventListener('keydown',e=>{if(e.key==='F1'){e.preventDefault();$('hidden').checked=!$('hidden').checked;render();}});
  new ResizeObserver(world).observe($('stage'));
  window.addEventListener('pagehide',()=>{stopLoop();stopCharge();});
  reset();world();
})();
