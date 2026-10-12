'use strict';
const $ = id => document.getElementById(id);
const ROOT = '../../../崩铁/';
const sets = [
  {name:'野穗伴行的快枪手',type:'outer',names:['快枪手的野穗毡帽','快枪手的粗革手套','快枪手的猎风披肩','快枪手的铆钉马靴'],twoEffect:'攻击力提高12%。',fourEffect:'暴击伤害提高12%，造成的伤害提高10%。'},
  {name:'云无留迹的过客',type:'outer',names:['过客的逢春木簪','过客的游龙臂鞲','过客的残绣风衣','过客的冥途游履'],twoEffect:'治疗效果提高15%。',fourEffect:'装备者击杀敌对目标时，为自己和周围10格内的其他玩家恢复等同于装备者最大生命值5%的生命。'},
  {name:'净庭教宗的圣骑士',type:'outer',names:['圣骑的宽恕盔面','圣骑的沉默誓环','圣骑的肃穆胸甲','圣骑的秩序铁靴'],twoEffect:'护甲值提高15%。',fourEffect:'装备者提供的护盾量提高20%（在本mod中体现为伤害吸收生命值）。'},
  {name:'太空封印站',type:'inner',names:['「黑塔」的空间站点','「黑塔」的漫历轨迹'],twoEffect:'攻击力提高24%。'},
  {name:'不老者的仙舟',type:'inner',names:['罗浮仙舟的天外楼船','罗浮仙舟的建木枝蔓'],twoEffect:'最大生命值提高20%。'}
];
const slots={head:'头部',hands:'手部',body:'躯干',feet:'脚部',sphere:'位面球',rope:'连结绳'};
const colors={2:'var(--r2)',3:'var(--r3)',4:'var(--r4)',5:'var(--r5)'};
const caps={2:6,3:9,4:12,5:15};
const labels={hp:'生命值',hpPct:'生命值',atk:'攻击力',atkPct:'攻击力',armor:'护甲值',armorPct:'护甲值',crit:'暴击率',critDmg:'暴击伤害',hit:'效果命中',resist:'效果抵抗',break:'击破特攻',heal:'治疗效果',tough:'护甲韧性',toughPct:'护甲韧性'};
const pool=['hp','hpPct','atk','atkPct','armor','armorPct','crit','critDmg','hit','resist','break','heal','tough','toughPct'];
const mainPools={head:['hp'],hands:['atk'],body:['hpPct','atkPct','armorPct','crit','critDmg','hit','heal'],feet:['hpPct','atkPct','armorPct'],sphere:['hpPct','atkPct','armorPct'],rope:['hpPct','atkPct','armorPct','break']};
const percent=id=>!['hp','atk','armor','tough'].includes(id);
const fmt=(id,n)=>Number(Number(n).toFixed(2))+(percent(id)?'%':'');
const attr=id=>labels[id]+(percent(id)?' %':'');
const imagePath=(set,name)=>ROOT+'遗器/'+(set.type==='outer'?'外圈/':'内圈/')+set.name+'/'+name+'.png';
const materialFiles={crystal:'遗失晶块',remains:'遗器残骸',resin:'自塑尘脂',dice:'变量骰子'};
const materialIcons={crystal:'遗失晶块',remains:'遗器残骸',resin:'自塑尘脂',dice:'变量骰子'};
let sequence=100,view='equipment',selectedId=1,forgeMode='normal',forgeSet=0,forgeSlot='head',forgeMain='hp',salvageMode='crystal';
let inventory=[],equipped={},materials={},confirmAction=null,toastTimer;
let enhanceStep=3;
let motionSnapshot=null;
let inventoryMotionKey='';
const reduceMotion=()=>window.matchMedia('(prefers-reduced-motion: reduce)').matches;
function animateUI(element,frames,options={}){
  if(!element||reduceMotion()||!element.animate)return;
  return element.animate(frames,{duration:300,easing:'cubic-bezier(.2,.75,.25,1)',fill:'backwards',...options});
}
function pulseUI(element,color='var(--gold)'){
  if(element?.matches('.item-card')){
    animateUI(element,[{boxShadow:`inset 0 0 0 0 ${color}`},{boxShadow:`inset 0 0 16px 1px ${color}`,offset:.35},{boxShadow:'inset 0 0 0 0 transparent'}],{duration:620});
    return;
  }
  animateUI(element,[{boxShadow:`0 0 0 0 ${color}`,filter:'brightness(1)'},{boxShadow:`0 0 20px 3px ${color}`,filter:'brightness(1.16)',offset:.35},{boxShadow:'0 0 0 0 transparent',filter:'brightness(1)'}],{duration:620});
}
function burstUI(container,kind='gold'){
  if(!container||reduceMotion())return;
  const fx=document.createElement('div');fx.className='motion-burst '+kind;fx.setAttribute('aria-hidden','true');
  fx.innerHTML='<span class="burst-ring"></span><span class="burst-ring second"></span>'+Array.from({length:16},(_,n)=>`<i style="--angle:${n*22.5}deg;--distance:${48+n%4*17}px;--delay:${n%3*35}ms"></i>`).join('');
  container.append(fx);setTimeout(()=>fx.remove(),1000);
}
function materialNumber(element,from,to){
  if(reduceMotion()||from===to){element.textContent=to;return;}
  const start=performance.now();
  function step(now){if(!element.isConnected)return;const t=Math.min((now-start)/360,1);element.textContent=Math.round(from+(to-from)*(1-(1-t)**3));if(t<1)requestAnimationFrame(step);}
  requestAnimationFrame(step);
}
function playInventoryMotion(){
  const cards=[...$('inventory').querySelectorAll('[data-item]')],key=cards.map(c=>c.dataset.item).join(',');
  if(key!==inventoryMotionKey)cards.forEach((card,n)=>animateUI(card,[{opacity:0},{opacity:1}],{duration:230,delay:Math.min(n,9)*22}));
  inventoryMotionKey=key;
}
function playSalvageDeparture(){
  if(!motionSnapshot||reduceMotion())return;
  for(const id of motionSnapshot.items.keys())if(!inventory.some(i=>i.id===id)){
    const image=$('inventory').querySelector(`[data-item="${id}"] img`)||$('viewContent').querySelector('.bench-target img');
    if(!image)continue;
    const rect=image.getBoundingClientRect();if(rect.bottom<0||rect.top>innerHeight)continue;
    const ghost=image.cloneNode();ghost.alt='';ghost.setAttribute('aria-hidden','true');ghost.className='fading-relic';Object.assign(ghost.style,{left:rect.left+'px',top:rect.top+'px',width:rect.width+'px',height:rect.height+'px'});document.body.append(ghost);
    animateUI(ghost,[{opacity:1,transform:'scale(1)'},{opacity:.6,transform:'translateY(-14px) scale(.85)',offset:.5},{opacity:0,transform:'translateY(-32px) scale(.55)'}],{duration:480});setTimeout(()=>ghost.remove(),500);
  }
}
function playRenderMotion(previous){
  const current={view,selectedId,materials:{...materials},equipped:{...equipped},items:new Map(inventory.map(i=>[i.id,{level:i.level,pending:!!i.pending,locked:i.locked}])),cards:[...$('inventory').querySelectorAll('[data-item]')].map(b=>b.dataset.item).join(',')};
  if(!previous||previous.view!==view){
    animateUI($('viewContent'),[{opacity:0,transform:'translateY(12px)'},{opacity:1,transform:'translateY(0)'}],{duration:350});
    animateUI($('viewTitle'),[{opacity:.2,transform:'translateX(-8px)'},{opacity:1,transform:'translateX(0)'}]);
  }
  if(!previous||previous.selectedId!==selectedId){
    animateUI($('detail'),[{opacity:.35,transform:'translateX(12px)'},{opacity:1,transform:'translateX(0)'}],{duration:320});
    animateUI($('detail').querySelector('.detail-top img'),[{transform:'scale(.92)',opacity:.5},{transform:'scale(1)',opacity:1}],{duration:420});
    pulseUI($('inventory').querySelector('.selected'),'var(--rarity)');
    $('detail').scrollTop=0;
  }
  if(previous){
    for(const key of Object.keys(materials))if(previous.materials[key]!==materials[key]){
      materialNumber($(key).querySelector('strong'),previous.materials[key],materials[key]);
      pulseUI($(key),materials[key]>previous.materials[key]?'var(--cyan)':'var(--gold)');
      if(materials[key]>previous.materials[key])burstUI($(key),'cyan');
    }
    for(const part of Object.keys(slots))if(previous.equipped[part]!==equipped[part]){
      const slot=$('viewContent').querySelector(`[data-slot="${part}"]`);
      if(slot){pulseUI(slot);burstUI(slot,'cyan');animateUI(slot.querySelector('img'),[{transform:'scale(.65)',opacity:.2},{transform:'scale(1.08)',opacity:1,offset:.7},{transform:'scale(1)'}],{duration:480});}
      pulseUI($('detail').querySelector('.detail-top'),'var(--cyan)');
    }
    for(const item of inventory){
      const old=previous.items.get(item.id),card=$('inventory').querySelector(`[data-item="${item.id}"]`);
      if(!old){pulseUI(card);burstUI($('viewContent').querySelector('.forge-preview'));}
      else if(old.level!==item.level){pulseUI(card);pulseUI($('viewContent').querySelector('.bench-target'));burstUI($('viewContent').querySelector('.bench-target'));$('viewContent').querySelectorAll('.growth-node.done').forEach((node,n)=>animateUI(node,[{filter:'brightness(1)'},{filter:'brightness(1.8)',offset:.4},{filter:'brightness(1)'}],{duration:500,delay:n*70}));}
      else if(!old.pending&&item.pending){burstUI($('viewContent').querySelector('.bench-target'),'cyan');$('viewContent').querySelectorAll('.preview-table tr').forEach((row,n)=>animateUI(row,[{opacity:.2,transform:'translateX(10px)'},{opacity:1,transform:'translateX(0)'}],{delay:n*65}));}
      else if(old.locked!==item.locked){pulseUI($('toggleLock'),'var(--cyan)');pulseUI(card,'var(--cyan)');}
    }
  }
  motionSnapshot=current;
}
const statsOf=item=>{const result={};if(!item)return result;result[item.main]=mainValue(item);item.subs.forEach(s=>result[s.id]=(result[s.id]||0)+subValue(s));return result;};
const deltaHTML=(id,n)=>`<span class="delta ${n>0?'positive':n<0?'negative':'neutral'}">${n>0?'+':''}${fmt(id,n)}</span>`;
function compareHTML(previous,next,beforeLabel='已装备',afterLabel='选中'){
  const a=statsOf(previous),b=statsOf(next),ids=pool.filter(id=>id in a||id in b);
  return `<div class="comparison-names"><span>${previous?itemName(previous):'空装备槽'}</span><span>${itemName(next)}</span></div><table class="preview-table comparison-table"><thead><tr><th>属性</th><th>${beforeLabel}</th><th>${afterLabel}</th><th>变化</th></tr></thead><tbody>${ids.map(id=>`<tr><td>${attr(id)}</td><td>${fmt(id,a[id]||0)}</td><td>${fmt(id,b[id]||0)}</td><td>${deltaHTML(id,(b[id]||0)-(a[id]||0))}</td></tr>`).join('')}</tbody></table><p class="muted comparison-help">比较遗器的词条贡献；套装与玩家最终属性不计入演示差值。</p>`;
}
function showResult(title,body){confirm(title,body,()=>{});$('acceptDialog').textContent='完成';$('cancelDialog').hidden=true;burstUI($('dialogBody').querySelector('.result-banner'));animateUI($('dialogBody').querySelector('.result-banner'),[{opacity:0,transform:'scale(.92)'},{opacity:1,transform:'scale(1)'}],{duration:400});}
const quality={2:.4,3:.6,4:.8,5:1};
const mainGrowth={hp:[3,.8],atk:[.75,17/60],hpPct:[8,2],atkPct:[8,2],armorPct:[10,2.5],crit:[5,1],critDmg:[10,2],hit:[8,2],heal:[6,1.6],break:[12,3.2]};
const subMids={hp:1.2,hpPct:3,atk:.3,atkPct:3,armor:.5,armorPct:3,tough:.2,toughPct:2,crit:1.5,critDmg:3,hit:2,resist:2,break:3,heal:2};
const xpPerLevel={2:[100,100,100,200,200,200],3:[100,100,100,200,200,200,300,300,300],4:[100,100,200,200,300,300,400,400,500,500,600,600],5:[100,100,200,200,300,300,400,400,500,600,700,800,900,1000,1200]};
const mainValueAt=(main,star,level)=>{const [base,growth]=mainGrowth[main];return quality[star]*(base+growth*level);};
const subRoll=(id,star)=>subMids[id]*quality[star]*[.8,1,1.2][Math.floor(Math.random()*3)];
const initialCount=star=>star===2?0:star===3?(Math.random()<.8?1:2):star===4?(Math.random()<.8?2:3):(Math.random()<.8?3:4);
const experienceTo=(star,level)=>xpPerLevel[star].slice(0,level).reduce((sum,xp)=>sum+xp,0);
const crystalCost=(star,from,to)=>(experienceTo(star,to)-experienceTo(star,from))/100;
const crystalSalvage=item=>({2:1,3:1,4:3,5:5}[item.star]+Math.floor(experienceTo(item.star,item.level)*80/10000));
function makeItem(setIndex,part,star=5,level=0,mainOverride=null){
  const set=sets[setIndex];
  const main=mainOverride??mainPools[part][0],initial=initialCount(star);
  const item={id:++sequence,setIndex,part,star,level:0,main,initial,locked:false,subs:[],pending:null};
  const candidates=pool.filter(id=>id!==main);
  for(let n=candidates.length-1;n>0;n--){const pick=Math.floor(Math.random()*(n+1));[candidates[n],candidates[pick]]=[candidates[pick],candidates[n]];}
  for(let i=0;i<initial;i++){const id=candidates[i];item.subs.push({id,base:subRoll(id,star),extra:0,rolls:0});}
  advance(item,Math.min(level,caps[star]));
  return item;
}
function advance(item,target){
  for(let level=item.level+1;level<=target;level++)if(level%3===0){
    if(item.subs.length<4){const candidates=pool.filter(id=>id!==item.main&&!item.subs.some(s=>s.id===id)),id=candidates[Math.floor(Math.random()*candidates.length)];item.subs.push({id,base:subRoll(id,item.star),extra:0,rolls:0});}
    else {const sub=item.subs[Math.floor(Math.random()*4)];sub.rolls++;sub.extra=(sub.extra||0)+subRoll(sub.id,item.star);}
  }
  item.level=target;
}
const mainValue=item=>mainValueAt(item.main,item.star,item.level);
const subValue=s=>s.base+(s.extra||0);
const getSelected=()=>inventory.find(item=>item.id===selectedId);
const isEquipped=item=>equipped[item.part]===item.id;
function reset(){
  inventory=[];equipped={};sequence=100;motionSnapshot=null;inventoryMotionKey='';
  sets.forEach((set,si)=>(set.type==='outer'?['head','hands','body','feet']:['sphere','rope']).forEach((part,pi)=>{
    const star=si===1&&pi===0?2:si===1&&pi===1?3:si===1&&pi===2?4:5;
    const level=si===2&&(part==='head'||part==='hands')?15:si===0?9:si===1?0:6;
    const item=makeItem(si,part,star,level);inventory.push(item);if(si===0||si===4)equipped[part]=item.id;
  }));
  materials={crystal:120,remains:650,resin:3,dice:4};selectedId=inventory[0].id;
  view='equipment';salvageMode='crystal';forgeMode='normal';forgeSet=0;forgeSlot='head';forgeMain='hp';
  enhanceStep=3;clearFilters(false);render();
}
function toast(message){$('toast').textContent=message;$('toast').classList.add('show');clearTimeout(toastTimer);toastTimer=setTimeout(()=>$('toast').classList.remove('show'),2800);}
function confirm(title,body,action){$('dialogTitle').textContent=title;$('dialogBody').innerHTML=body;confirmAction=action;$('acceptDialog').textContent='确认';$('cancelDialog').hidden=false;$('confirmation').showModal();animateUI($('confirmation'),[{opacity:0,transform:'translateY(14px) scale(.97)'},{opacity:1,transform:'translateY(0) scale(1)'}],{duration:260});}
function select(id){selectedId=Number(id);render();}
function changeView(name){view=name;render();}
function render(){
  playSalvageDeparture();
  document.querySelectorAll('[data-view]').forEach(b=>b.classList.toggle('active',b.dataset.view===view));
  for(const key of Object.keys(materials))$(key).innerHTML=`<img src="${ROOT}物品/${materialIcons[key]}.png" alt=""><div><span>${materialFiles[key]}</span><strong>${materials[key]}</strong></div>`;
  const titles={equipment:['RELIC LOADOUT','六槽装备','点击部位或库存查看详情'],enhance:['ENHANCEMENT','遗器强化','属性与经验按已确认数值预览'],salvage:['SALVAGE','遗器分解','已装备、锁定项不可分解'],forge:['SYNTHESIS','遗器合成','选择套装、部位与主属性'],reroll:['VARIABLE DICE','变量重掷','仅限满级五星遗器']};
  [$('viewCaption').textContent,$('viewTitle').textContent,$('viewHint').textContent]=titles[view];
  if(view==='equipment')renderEquipment();else if(view==='forge')renderForge();else renderBench();
  renderInventory();renderDetail();
  decorateView();
  playRenderMotion(motionSnapshot);
}
function slotHTML(part,cls=''){
  const item=inventory.find(i=>i.id===equipped[part]);
  return `<button class="slot ${cls} ${item?'':'empty'} ${item?.id===selectedId?'selected':''}" style="--rarity:${item?colors[item.star]:'var(--line)'}" data-slot="${part}" data-equipped-id="${item?.id??''}" aria-pressed="${!!item&&item.id===selectedId}" aria-label="${slots[part]}${item?'，'+itemName(item):'，未装备'}"><span class="slot-orbit" aria-hidden="true"><svg viewBox="0 0 100 100"><circle class="slot-orbit-guide" cx="50" cy="50" r="46"/><g class="slot-orbit-traveler"><circle class="slot-orbit-arc" cx="50" cy="50" r="46" stroke-dasharray="40 105"/><circle class="slot-orbit-dot" cx="96" cy="50" r="1.7"/></g></svg></span><span class="slot-art">${slotArtHTML(item)}</span><span class="slot-label">${slots[part]}</span></button>`;
}
function slotArtHTML(item){return item?`<img src="${itemImage(item)}" alt=""><span class="slot-level">+${item.level}</span>`:'<span class="empty-symbol">＋</span>';}
function itemName(item){const set=sets[item.setIndex];return set.names[(set.type==='outer'?['head','hands','body','feet']:['sphere','rope']).indexOf(item.part)];}
function itemImage(item){return imagePath(sets[item.setIndex],itemName(item));}
function summary(type){
  const counts=new Map();Object.values(equipped).forEach(id=>{const item=inventory.find(i=>i.id===id);if(item&&sets[item.setIndex].type===type)counts.set(item.setIndex,(counts.get(item.setIndex)||0)+1);});
  return counts.size?[...counts].map(([si,n])=>`<div class="set-line"><strong>${sets[si].name}</strong><span>${n}/${type==='outer'?4:2} · ${n>=2?(type==='outer'&&n>=4?'2件与4件效果已激活':'2件效果已激活'):'未齐两件'}</span></div>`).join(''):'<p class="muted">尚未装备</p>';
}
function equippedSetCount(setIndex){return Object.values(equipped).filter(id=>inventory.find(i=>i.id===id)?.setIndex===setIndex).length;}
function orbitHTML(){
  const ticks=Array.from({length:32},(_,n)=>{const angle=n*Math.PI/16,r=n%4===0?145:150;return `<line x1="${160+Math.cos(angle)*r}" y1="${160+Math.sin(angle)*r}" x2="${160+Math.cos(angle)*155}" y2="${160+Math.sin(angle)*155}"/>`;}).join('');
  return `<div class="idle-orbit outer-orbit" aria-hidden="true"><svg viewBox="0 0 320 320"><g class="orbit-ticks">${ticks}</g><circle class="orbit-guide" cx="160" cy="160" r="143"/><g class="orbit-traveler"><circle class="orbit-arc" cx="160" cy="160" r="143" stroke-dasharray="90 359"/><circle class="orbit-dot" cx="303" cy="160" r="3"/><circle class="orbit-dot small" cx="17" cy="160" r="2"/></g></svg></div><div class="idle-orbit inner-orbit" aria-hidden="true"><svg viewBox="0 0 200 200"><circle class="orbit-guide" cx="100" cy="100" r="88"/><circle class="inner-dash" cx="100" cy="100" r="80"/><g class="orbit-traveler"><circle class="orbit-arc" cx="100" cy="100" r="88" stroke-dasharray="50 226"/><path class="orbit-star" d="M188 93L190 98L195 100L190 102L188 107L186 102L181 100L186 98Z"/><circle class="orbit-dot small" cx="12" cy="100" r="2"/></g></svg></div>`;
}
function renderEquipment(){
  if(!$('viewContent').querySelector('.loadout')){
    $('viewContent').innerHTML=`<div class="loadout" style="--idle-phase:${-performance.now()/1000}s">${orbitHTML()}${slotHTML('head','head-slot')}${slotHTML('hands','hands-slot')}${slotHTML('body','body-slot')}${slotHTML('feet','feet-slot')}<div class="inner-row" role="group" aria-label="内圈位面饰品"><span class="inner-tag">内圈 · 位面饰品</span>${slotHTML('sphere')}${slotHTML('rope')}</div></div><div class="set-summary"></div>`;
  }
  // 保留轨道、小圆框与图标节点，选择遗器时不重新启动CSS动画。
  $('viewContent').querySelectorAll('[data-slot]').forEach(button=>{
    const part=button.dataset.slot,item=inventory.find(i=>i.id===equipped[part]),nextId=String(item?.id??'');
    if(button.dataset.equippedId!==nextId){button.querySelector('.slot-art').innerHTML=slotArtHTML(item);button.dataset.equippedId=nextId;}
    else if(item)button.querySelector('.slot-level').textContent='+'+item.level;
    button.classList.toggle('empty',!item);button.classList.toggle('selected',!!item&&item.id===selectedId);
    button.style.setProperty('--rarity',item?colors[item.star]:'var(--line)');button.setAttribute('aria-pressed',String(!!item&&item.id===selectedId));button.setAttribute('aria-label',slots[part]+(item?'，'+itemName(item):'，未装备'));
    button.onclick=()=>{
      pulseUI(button);animateUI(button.querySelector('.slot-orbit'),[{transform:'scale(1)',filter:'brightness(1)'},{transform:'scale(1.08)',filter:'brightness(1.8)',offset:.4},{transform:'scale(1)',filter:'brightness(1)'}],{duration:440});
      const id=equipped[part];if(id)select(id);else{$('slotFilter').value=part;renderInventory();toast('在库存中选择'+slots[part]+'遗器');}
    };
  });
  $('viewContent').querySelector('.set-summary').innerHTML=`<div><h3>外圈套装 · 2件 / 4件</h3>${summary('outer')}</div><div><h3>内圈套装 · 2件</h3>${summary('inner')}</div>`;
}
function renderInventory(){
  const query=$('inventorySearch').value.trim().toLocaleLowerCase(),state=$('stateFilter').value;
  const list=inventory.filter(i=>($('slotFilter').value==='all'||i.part===$('slotFilter').value)&&($('setFilter').value==='all'||i.setIndex===Number($('setFilter').value))&&($('starFilter').value==='all'||i.star===Number($('starFilter').value))&&(!$('lockFilter').checked||i.locked)&&($('mainFilter').value==='all'||i.main===$('mainFilter').value)&&(!query||(itemName(i)+' '+sets[i.setIndex].name).toLocaleLowerCase().includes(query))&&(state==='all'||state==='equipped'&&isEquipped(i)||state==='available'&&!isEquipped(i)||state==='pending'&&i.pending));
  const order=$('sortOrder').value,partOrder=Object.keys(slots);
  list.sort((a,b)=>order==='recent'?b.id-a.id:order==='part'?partOrder.indexOf(a.part)-partOrder.indexOf(b.part)||b.star-a.star||b.level-a.level:order==='level'?b.level-a.level||b.star-a.star:b.star-a.star||b.level-a.level||a.id-b.id);
  $('inventoryCount').textContent=`${list.length} / ${inventory.length}`;
  $('filterSummary').textContent=`显示 ${list.length} 件 · 已装备 ${Object.keys(equipped).length}/6 · 待选择重掷 ${inventory.filter(i=>i.pending).length} 件`;
  const container=$('inventory');
  if(list.length){
    const previous=new Map([...container.querySelectorAll('[data-item]')].map(node=>[Number(node.dataset.item),node]));
    const next=list.map(i=>{
      let button=previous.get(i.id);
      if(!button){button=document.createElement('button');button.dataset.item=i.id;button.onclick=()=>select(button.dataset.item);}
      button.className=`item-card ${i.id===selectedId?'selected':''} ${i.pending?'pending':''}`;
      button.style.setProperty('--rarity',colors[i.star]);
      button.title=`${itemName(i)} · ${slots[i.part]} · ${i.star}星 +${i.level} · ${labels[i.main]} ${fmt(i.main,mainValue(i))}`;
      button.setAttribute('aria-label',button.title);button.setAttribute('aria-pressed',String(i.id===selectedId));
      const content=`<span class="item-state">${[i.pending?'待选':'',i.locked?'锁定':'',isEquipped(i)?'装备':''].filter(Boolean).join(' · ')}</span><span class="item-level">+${i.level}</span><img src="${itemImage(i)}" alt=""><span class="rarity-stars">${'★'.repeat(i.star)}</span><span class="item-name">${itemName(i)}</span><span class="card-main">${labels[i.main]} ${fmt(i.main,mainValue(i))}</span>`;
      if(button._relicContent!==content){button.innerHTML=content;button._relicContent=content;}
      return button;
    });
    // Keep unchanged cards/images alive when switching workbenches or selecting a relic.
    const retained=new Set(next);
    [...container.children].forEach(node=>{if(!retained.has(node))node.remove();});
    next.forEach((node,index)=>{if(container.children[index]!==node)container.insertBefore(node,container.children[index]||null);});
  }else if(!container.querySelector('.empty-inventory')){
    container.innerHTML='<div class="empty-inventory"><strong>没有符合条件的遗器</strong><p>可调整部位、主属性或装备状态，或清空筛选。</p><button id="emptyClear">清空筛选</button></div>';
  }
  if($('emptyClear'))$('emptyClear').onclick=()=>clearFilters();
  playInventoryMotion();
}
function renderDetail(){
  const i=getSelected();if(!i){$('detail').innerHTML='<div class="bench-empty">选择一件遗器查看详情</div>';return;}
  const set=sets[i.setIndex];
  const count=equippedSetCount(i.setIndex),twoActive=count>=2,fourActive=set.type==='outer'&&count>=4;
  $('detail').innerHTML=`<div class="detail-top" style="--rarity:${colors[i.star]}"><button class="lock-button" id="toggleLock" ${i.pending?'disabled':''}>${i.locked?'已锁定':'锁定'}</button><img src="${itemImage(i)}" alt="${itemName(i)}"><h2>${itemName(i)}</h2><span class="rarity-stars">${'★'.repeat(i.star)}</span><div class="detail-meta"><span>${slots[i.part]} · ${set.type==='outer'?'外圈':'内圈'}</span><span>+${i.level} / ${caps[i.star]}</span></div><span class="sample-badge">词条按确认表计算 · 此页仅预览</span></div><div class="detail-body"><h3>主属性</h3><div class="stat-row main-stat"><span>${labels[i.main]}</span><strong>${fmt(i.main,mainValue(i))}</strong></div><h3>副属性 ${i.subs.length}/4</h3>${i.subs.map(s=>`<div class="stat-row"><span>${labels[s.id]}</span><strong>${fmt(s.id,subValue(s))}</strong></div>`).join('')||'<p class="muted">强化至+3后解锁首条副属性</p>'}</div><div class="detail-body"><h3>${set.name} · 已装备${count}件</h3><div class="effect-box"><strong>两件套 · ${twoActive?'已激活':'未激活'}</strong>${set.twoEffect}</div>${set.type==='outer'?`<div class="effect-box"><strong>四件套 · ${fourActive?'已激活':'未激活'}</strong>${set.fourEffect} <span>集齐4件后，两件套效果同时生效。</span></div>`:''}</div><div class="detail-actions"><button class="primary" id="equip" ${i.pending?'disabled':''}>${isEquipped(i)?'卸下':'装备'}</button><button id="toEnhance" ${i.pending?'disabled':''}>强化</button></div>${i.pending?'<div class="notice pending-note">有一份重掷结果等待选择，请前往变量重掷页。</div>':''}`;
  $('toggleLock').onclick=()=>{i.locked=!i.locked;render();};
  $('equip').onclick=()=>{if(i.pending)return;if(isEquipped(i)){delete equipped[i.part];render();toast('已卸下'+itemName(i));}else{const previous=inventory.find(p=>p.id===equipped[i.part]);if(previous?.pending){toast('当前装备有待确认结果，先完成重掷选择');return;}const action=()=>{equipped[i.part]=i.id;render();toast('已装备'+itemName(i));};if(previous)confirm('替换'+slots[i.part],compareHTML(previous,i)+'<p class="muted">原装备保留在库存中。</p>',action);else action();}};
  $('toEnhance').onclick=()=>changeView('enhance');
  if(!isEquipped(i))$('detail').insertAdjacentHTML('beforeend',`<details class="detail-comparison" open><summary>与当前${slots[i.part]}比较</summary>${compareHTML(inventory.find(p=>p.id===equipped[i.part]),i)}</details>`);
  $('detail').insertAdjacentHTML('beforeend',`<div class="detail-shortcuts"><button id="detailSalvage" ${i.locked||isEquipped(i)||i.pending?'disabled':''}>分解</button><button id="detailReroll" ${i.star!==5||i.level!==15||i.locked||isEquipped(i)?'disabled':''}>${i.pending?'处理重掷':'重掷'}</button></div>`);
  $('detailSalvage').onclick=()=>changeView('salvage');$('detailReroll').onclick=()=>changeView('reroll');
}
function targetHTML(i){return `<div class="bench-target"><img src="${itemImage(i)}" alt=""><div><h3>${itemName(i)}</h3><p>${i.star}星 · ${slots[i.part]} · +${i.level} / ${caps[i.star]}${isEquipped(i)?' · 已装备':''}${i.locked?' · 已锁定':''}</p></div></div>`;}
function renderBench(){
  const i=getSelected();if(!i){$('viewContent').innerHTML='<div class="bench-empty">从右侧库存中选择一件遗器</div>';return;}
  let body=targetHTML(i);
  if(view==='enhance'){
    const target=Math.min(i.level+enhanceStep,caps[i.star]),next=structuredClone(i),cost=crystalCost(i.star,i.level,target);advance(next,target);
    body+=`<div class="notice">每个遗失晶块提供100遗器经验；主属性按等级成长，每+3级结算一次副属性。副属性每次从低、中、高三档等概率成长。</div><table class="preview-table"><tr><th>强化等级</th><th>当前</th><th>预览</th></tr><tr><td>等级</td><td>+${i.level}</td><td>+${target}</td></tr><tr><td>${labels[i.main]}</td><td>${fmt(i.main,mainValue(i))}</td><td>${fmt(i.main,mainValue(next))}</td></tr><tr><td>副属性条数</td><td>${i.subs.length}</td><td>${next.subs.length} / 4</td></tr></table><div class="meter"><i style="width:${i.level/caps[i.star]*100}%"></i></div><div class="operation-actions"><button class="primary" id="enhance" ${target===i.level||i.pending||materials.crystal<cost?'disabled':''}>强化至 +${target}</button><span class="cost">消耗 ${cost} 个遗失晶块（${cost*100}经验）</span></div>${i.pending?'<p class="pending-note">请先处理待选择的重掷结果。</p>':''}`;
  }else if(view==='salvage'){
    const refund=crystalSalvage(i),blocked=i.locked||isEquipped(i)||i.pending;
    body+=`<div class="notice">五星可以选择晶块或残骸，二者不同时领取。已装备、锁定及待确认重掷的遗器不能分解。</div><div class="mode-toggle"><button id="crystalMode" class="${salvageMode==='crystal'?'active':''}">遗失晶块</button><button id="remainsMode" class="${salvageMode==='remains'?'active':''}" ${i.star!==5?'disabled':''}>遗器残骸 · 仅五星</button></div><table class="preview-table"><tr><th>分解收益</th><th>数量</th></tr><tr><td>${salvageMode==='crystal'?'遗失晶块':'遗器残骸'}</td><td>${salvageMode==='crystal'?refund:10}</td></tr></table><p class="muted">${salvageMode==='crystal'?'收益=品质基础晶块+floor(实际投入经验×80%÷100)，逐件向下取整。':'残骸模式固定返还10个；放弃已投入的强化经验。'}</p><div class="operation-actions"><button class="danger" id="salvage" ${blocked?'disabled':''}>确认分解</button></div>${blocked?'<p class="pending-note">请先卸下、解锁，或处理待选重掷结果。</p>':''}`;
  }else{
    const eligible=i.star===5&&i.level===15&&!i.locked&&!isEquipped(i);
    body+='<div class="notice">保留主属性及四条副属性种类，重新分配副属性强化次数与增量。每次消耗1个变量骰子，保留旧结果也不返还。</div>';
    if(i.pending){body+=`<p class="pending-note">本次骰子已消耗。离开此页后，候选结果会保留至你做出选择。</p><table class="preview-table"><tr><th>副属性</th><th>原结果</th><th>新结果</th></tr>${i.subs.map((s,n)=>{const next=i.pending[n],diff=subValue(next)-subValue(s);return `<tr><td>${labels[s.id]}</td><td>${fmt(s.id,subValue(s))}<small> · ${s.rolls}次</small></td><td class="${diff>=0?'good':'bad'}">${fmt(s.id,subValue(next))}<small> · ${next.rolls}次</small></td></tr>`;}).join('')}</table><div class="operation-actions"><button id="keepOld">保留原结果</button><button class="primary" id="takeNew">采用新结果</button></div>`;}
    else body+=`<p class="muted">选择一件未装备、未锁定的+15五星遗器。库存中的「圣骑的沉默誓环」可直接演示。</p><div class="operation-actions"><button class="primary" id="reroll" ${!eligible||materials.dice<1?'disabled':''}>消耗1个骰子 · 重掷</button><button id="pickReroll">选择可重掷遗器</button></div><p class="muted">候选随机结果与增量仅用于展示交互。</p>`;
  }
  $('viewContent').innerHTML='<div class="workbench">'+body+'</div>';
  if(view==='enhance')$('enhance').onclick=()=>{const target=Math.min(i.level+enhanceStep,caps[i.star]),cost=crystalCost(i.star,i.level,target);if(!cost||i.pending||materials.crystal<cost)return;const before=structuredClone(i);materials.crystal-=cost;advance(i,target);render();showResult('强化完成',`<div class="result-banner">+${before.level} <span>→</span> +${i.level}</div><p>消耗 ${cost} 个遗失晶块（${cost*100}遗器经验）</p>${compareHTML(before,i,'强化前','强化后')}<p class="muted">已按确认规则结算跨越的副属性节点；随机增量按遗器星级和档位生成。</p>`);};
  if(view==='salvage'){
    $('crystalMode').onclick=()=>{salvageMode='crystal';render();};$('remainsMode').onclick=()=>{if(i.star===5){salvageMode='remains';render();}};
    if(i.star!==5&&salvageMode==='remains'){salvageMode='crystal';renderBench();return;}
    $('salvage').onclick=()=>{if(i.locked||isEquipped(i)||i.pending)return;const key=salvageMode==='remains'?'remains':'crystal',count=key==='remains'?10:crystalSalvage(i);confirm('分解遗器',`将分解 <strong>${itemName(i)}</strong>，获得 <strong>${count}个${materialFiles[key]}</strong>。<br>${key==='remains'?'将放弃已投入的强化经验。':'收益按确认的80%经验返还规则计算。'}`,()=>{materials[key]+=count;inventory=inventory.filter(p=>p.id!==i.id);selectedId=inventory[0]?.id??null;render();toast('已分解，材料已计入演示库存');});};
  }
  if(view==='reroll'){
    if(i.pending){$('keepOld').onclick=()=>{i.pending=null;render();toast('保留原结果 · 本次骰子已消耗');};$('takeNew').onclick=()=>{i.subs=i.pending;i.pending=null;render();toast('已采用新结果');};}
    else{$('pickReroll').onclick=()=>{const target=inventory.find(p=>p.star===5&&p.level===15&&!isEquipped(p)&&!p.locked);if(target)select(target.id);else toast('暂无符合条件的遗器，先强化一件五星至+15并卸下、解锁');};$('reroll').onclick=()=>{if(!eligible||materials.dice<1)return;confirm('使用变量骰子','消耗1个变量骰子，生成一份新结果。<br>你可选择新旧结果；选择保留原结果也会消耗本次骰子。',()=>{materials.dice--;const total=i.subs.reduce((n,s)=>n+s.rolls,0),candidate=i.subs.map(s=>({...s,rolls:0,extra:0}));for(let n=0;n<total;n++){const s=candidate[Math.floor(Math.random()*candidate.length)];s.rolls++;s.extra+=subRoll(s.id,i.star);}i.pending=candidate;render();toast('新结果已生成，请选择保留哪一份');});};}
  }
}
function renderForge(){
  const set=sets[forgeSet],parts=set.type==='outer'?['head','hands','body','feet']:['sphere','rope'];if(!parts.includes(forgeSlot))forgeSlot=parts[0];if(!mainPools[forgeSlot].includes(forgeMain))forgeMain=mainPools[forgeSlot][0];
  const fixed=mainPools[forgeSlot].length===1;if(fixed&&forgeMode==='target')forgeMode='normal';
  const disabled=materials.remains<100||(forgeMode==='target'&&materials.resin<1);
  $('viewContent').innerHTML=`<div class="workbench"><div class="notice">合成获得+0五星遗器，可选套装和部位。定向模式指定主属性，副属性仍随机。头部和手部主属性固定，无需消耗自塑尘脂。</div><div class="form-grid"><label>套装<select id="forgeSet">${sets.map((s,n)=>`<option value="${n}" ${n===forgeSet?'selected':''}>${s.type==='outer'?'外圈':'内圈'} · ${s.name}</option>`).join('')}</select></label><label>部位<select id="forgeSlot">${parts.map(p=>`<option value="${p}" ${p===forgeSlot?'selected':''}>${slots[p]}</option>`).join('')}</select></label></div><div class="mode-toggle"><button id="normalMode" class="${forgeMode==='normal'?'active':''}">普通合成</button><button id="targetMode" class="${forgeMode==='target'?'active':''}" ${fixed?'disabled':''}>定向主属性</button></div>${forgeMode==='target'?`<div class="form-grid"><label>指定主属性<select id="forgeMain">${mainPools[forgeSlot].map(id=>`<option value="${id}" ${forgeMain===id?'selected':''}>${attr(id)}</option>`).join('')}</select></label></div>`:`<p class="muted">${fixed?'固定主属性：'+attr(mainPools[forgeSlot][0]):'主属性从本部位合法属性池随机生成。'}</p>`}<div class="operation-actions"><button class="primary" id="forge" ${disabled?'disabled':''}>合成五星遗器</button><span class="cost">100遗器残骸${forgeMode==='target'?' + 1自塑尘脂':''}</span></div><div class="bench-controls"><button id="craftResin" ${materials.remains<200?'disabled':''}>制作自塑尘脂 · 200残骸</button><button id="craftDice" ${materials.remains<300?'disabled':''}>制作变量骰子 · 300残骸</button></div></div>`;
  $('forgeSet').onchange=e=>{forgeSet=Number(e.target.value);render();};$('forgeSlot').onchange=e=>{forgeSlot=e.target.value;render();};$('normalMode').onclick=()=>{forgeMode='normal';render();};$('targetMode').onclick=()=>{if(!fixed){forgeMode='target';render();}};if($('forgeMain'))$('forgeMain').onchange=e=>{forgeMain=e.target.value;render();};
  $('forge').onclick=()=>{if(disabled)return;confirm('合成五星遗器',`${set.name} · ${slots[forgeSlot]}<br>${forgeMode==='target'?'指定主属性：'+attr(forgeMain):'主属性随机（固定部位除外）'}<br>消耗100遗器残骸${forgeMode==='target'?'和1个自塑尘脂':''}。`,()=>{materials.remains-=100;if(forgeMode==='target')materials.resin--;const pool=mainPools[forgeSlot],main=forgeMode==='target'?forgeMain:pool[Math.floor(Math.random()*pool.length)],item=makeItem(forgeSet,forgeSlot,5,0,main);inventory.push(item);selectedId=item.id;['slotFilter','setFilter','starFilter'].forEach(id=>$(id).value='all');$('lockFilter').checked=false;render();toast('合成成功 · 新遗器已加入库存');});};
  $('craftResin').onclick=()=>craft('resin',200);$('craftDice').onclick=()=>craft('dice',300);
}
function craft(key,cost){if(materials.remains<cost)return;confirm('制作'+materialFiles[key],`消耗${cost}个遗器残骸，获得1个${materialFiles[key]}。`,()=>{materials.remains-=cost;materials[key]++;render();toast('已制作'+materialFiles[key]);});}
function clearFilters(redraw=true){
  ['slotFilter','setFilter','starFilter','mainFilter','stateFilter'].forEach(id=>$(id).value='all');
  $('inventorySearch').value='';$('sortOrder').value='star';$('lockFilter').checked=false;
  if(redraw)renderInventory();
}
function costTile(key,count){return `<div class="cost-tile ${materials[key]<count?'insufficient':''}"><img src="${ROOT}物品/${materialIcons[key]}.png" alt=""><div><span>${materialFiles[key]}</span><strong>${count} <small>/ 持有 ${materials[key]}</small></strong></div></div>`;}
function decorateView(){
  if(view==='equipment'){
    const oldSummary=$('viewContent').querySelector('.loadout-stats'),summaryOpen=!!oldSummary?.open;oldSummary?.remove();
    const totals={};Object.values(equipped).forEach(id=>{const item=inventory.find(i=>i.id===id);Object.entries(statsOf(item)).forEach(([key,value])=>totals[key]=(totals[key]||0)+value);});
    $('viewContent').insertAdjacentHTML('beforeend',`<details class="loadout-stats"><summary><span>遗器词条汇总</span><small>已装备 ${Object.keys(equipped).length}/6 · 不含套装</small></summary><div class="aggregate-grid">${pool.filter(id=>totals[id]).map(id=>`<div><span>${attr(id)}</span><strong>+${fmt(id,totals[id])}</strong></div>`).join('')||'<p class="muted">装备遗器后显示主副属性合计。</p>'}</div><p class="muted">百分比按同属性相加；此处为遗器贡献，不是玩家最终属性。</p></details>`);
    $('viewContent').querySelector('.loadout-stats').open=summaryOpen;
    return;
  }
  if(view==='forge'){
    if($('forgePreview'))return;
    const set=sets[forgeSet],parts=set.type==='outer'?['head','hands','body','feet']:['sphere','rope'],name=set.names[parts.indexOf(forgeSlot)];
    const body=$('viewContent').querySelector('.workbench');
    body.insertAdjacentHTML('afterbegin',`<div id="forgePreview" class="forge-preview"><div class="forge-image"><img src="${imagePath(set,name)}" alt="${name}"><span>★★★★★</span></div><div><span class="eyebrow">SYNTHESIS PREVIEW</span><h3>${name}</h3><p>${set.name} · ${slots[forgeSlot]} · +0</p><span class="pill">${forgeMode==='target'?'指定：'+attr(forgeMain):mainPools[forgeSlot].length===1?'固定：'+attr(mainPools[forgeSlot][0]):'合法主属性随机'}</span></div></div>`);
    $('forge').parentElement.insertAdjacentHTML('beforebegin',`<div class="cost-strip">${costTile('remains',100)}${forgeMode==='target'?costTile('resin',1):''}</div>`);
    if(materials.remains<100||forgeMode==='target'&&materials.resin<1)body.insertAdjacentHTML('beforeend','<p class="blocked-reason">材料不足：补足上方标红材料后即可合成。</p>');
    return;
  }
  const item=getSelected();if(!item)return;
  const body=$('viewContent').querySelector('.workbench');
  if(view==='enhance'){
    const target=Math.min(item.level+enhanceStep,caps[item.star]),cost=crystalCost(item.star,item.level,target);
    body.querySelector('.notice').insertAdjacentHTML('afterend',`<div class="enhance-options" role="group" aria-label="强化目标"><span>强化目标</span>${[[1,'提升1级'],[3,'提升3级'],[15,'升至上限']].map(([step,label])=>`<button data-enhance-step="${step}" class="${enhanceStep===step?'active':''}" ${item.level===caps[item.star]?'disabled':''}>${label}</button>`).join('')}</div><div class="growth-track">${Array.from({length:caps[item.star]/3},(_,n)=>(n+1)*3).map(level=>`<div class="growth-node ${level<=item.level?'done':level<=target?'upcoming':''}"><span>+${level}</span><small>${level<=item.level?'已完成':level<=target?'本次结算':'待强化'}</small></div>`).join('')}</div>`);
    body.querySelector('.operation-actions').insertAdjacentHTML('beforebegin',`<div class="cost-strip">${costTile('crystal',cost)}</div>`);
    const reason=item.pending?'先选择待确认的重掷结果。':cost===0?'已达到该星级的强化上限。':materials.crystal<cost?'遗失晶块不足，可降低强化目标或分解重复遗器。':'';
    if(reason)body.insertAdjacentHTML('beforeend',`<p class="blocked-reason">${reason}</p>`);
    document.querySelectorAll('[data-enhance-step]').forEach(b=>b.onclick=()=>{enhanceStep=Number(b.dataset.enhanceStep);render();});
  }
  if(view==='salvage'){
    const reasons=[isEquipped(item)?'当前已装备，需先卸下。':'',item.locked?'已锁定，需先解锁。':'',item.pending?'有待选择的重掷结果，需先处理。':''].filter(Boolean);
    if(reasons.length)body.insertAdjacentHTML('beforeend',`<div class="blocked-reason">${reasons.join('<br>')}</div>`);
    else body.insertAdjacentHTML('beforeend','<p class="operation-tip">点击确认分解后，弹窗再次列出物品和收益。</p>');
  }
  if(view==='reroll'&&!item.pending){
    const reasons=[item.star!==5?'仅五星遗器支持变量重掷。':'',item.level!==15?'需要强化至+15。':'',isEquipped(item)?'请先卸下。':'',item.locked?'请先解锁。':'',materials.dice<1?'变量骰子不足。':''].filter(Boolean);
    body.querySelector('.operation-actions').insertAdjacentHTML('beforebegin',`<div class="cost-strip">${costTile('dice',1)}</div>`);
    if(reasons.length)body.insertAdjacentHTML('beforeend',`<div class="blocked-reason">${reasons.join(' ')}</div>`);
  }
}
sets.forEach((set,n)=>{const option=document.createElement('option');option.value=n;option.textContent=set.name;$('setFilter').append(option);});
[...new Set(Object.values(mainPools).flat())].forEach(id=>{const option=document.createElement('option');option.value=id;option.textContent=attr(id);$('mainFilter').append(option);});
document.querySelectorAll('[data-view]').forEach(b=>b.onclick=()=>changeView(b.dataset.view));
['slotFilter','setFilter','starFilter','lockFilter','mainFilter','stateFilter','sortOrder'].forEach(id=>$(id).onchange=renderInventory);
$('inventorySearch').oninput=renderInventory;$('clearFilters').onclick=()=>clearFilters();
function feedbackControl(target,event){
  if(!(target instanceof Element))return;
  let control=target.closest('button,select,input,summary,label');
  if(control?.tagName==='LABEL')control=control.querySelector('input')||control;
  if(!control||control.disabled||reduceMotion())return;
  const surface=control.matches('input[type=checkbox]')?control.closest('label')||control:control;
  if(surface.matches('.item-card')){pulseUI(surface,'var(--rarity)');return;}
  animateUI(surface,[{filter:'brightness(1)'},{filter:'brightness(1.25)',offset:.3},{filter:'brightness(1)'}],{duration:240});
  if(control.matches('select,input,summary'))pulseUI(surface,'var(--cyan)');
  // 原生弹窗处于浏览器顶层，直接动画控件，避免把页面光圈画到弹窗背后。
  if(control.closest('dialog')){pulseUI(surface);return;}
  const rect=control.getBoundingClientRect(),ripple=document.createElement('span');ripple.className='click-spark';ripple.setAttribute('aria-hidden','true');
  ripple.style.left=(event?.clientX??rect.left+rect.width/2)+'px';ripple.style.top=(event?.clientY??rect.top+rect.height/2)+'px';document.body.append(ripple);
  animateUI(ripple,[{opacity:.6,transform:'translate(-50%,-50%) scale(.2)'},{opacity:0,transform:'translate(-50%,-50%) scale(1.8)'}],{duration:380});setTimeout(()=>ripple.remove(),400);
}
document.addEventListener('pointerdown',event=>feedbackControl(event.target,event));
document.addEventListener('keydown',event=>{if(event.repeat)return;if(event.key==='Enter'||event.key===' '&&event.target.matches('button,summary,input[type=checkbox]'))feedbackControl(event.target);});
document.addEventListener('change',event=>{if(event.target.matches('select,input[type=checkbox]'))feedbackControl(event.target);});
document.addEventListener('focusin',event=>{if(event.target.matches('button,select,input,summary'))animateUI(event.target,[{outlineOffset:'0px'},{outlineOffset:'3px'}],{duration:180});});
document.addEventListener('toggle',event=>{if(event.target.matches('details')){const summary=event.target.querySelector('summary');animateUI(summary,[{opacity:.6},{opacity:1}],{duration:200});}},true);
$('reset').onclick=()=>confirm('重置演示','将恢复示例装备、库存和材料；本页待选择重掷结果也会清除。',()=>reset());
function closeDialog(){
  const dialog=$('confirmation');
  if(dialog.open&&!reduceMotion()){
    const rect=dialog.getBoundingClientRect(),ghost=document.createElement('div');ghost.className='dialog-exit';ghost.setAttribute('aria-hidden','true');ghost.inert=true;
    [...dialog.children].forEach(child=>ghost.append(child.cloneNode(true)));ghost.querySelectorAll('[id]').forEach(node=>node.removeAttribute('id'));
    Object.assign(ghost.style,{left:rect.left+'px',top:rect.top+'px',width:rect.width+'px',height:rect.height+'px'});document.body.append(ghost);
    animateUI(ghost,[{opacity:1,transform:'translateY(0) scale(1)'},{opacity:0,transform:'translateY(8px) scale(.98)'}],{duration:160});setTimeout(()=>ghost.remove(),180);
  }
  dialog.close();confirmAction=null;
}
$('closeDialog').onclick=closeDialog;$('cancelDialog').onclick=closeDialog;$('confirmation').addEventListener('cancel',event=>{event.preventDefault();closeDialog();});
$('acceptDialog').onclick=()=>{const action=confirmAction;closeDialog();if(action)action();};
function initializeWidePreview(){
  const frame=$('gameFrame');
  // A single scale factor preserves every circle and matches a 16:9 game canvas.
  const fit=()=>{
    const viewport=frame.parentElement;
    const scale=Math.max(.05,Math.min((viewport.clientWidth-24)/1440,(viewport.clientHeight-24)/810));
    frame.style.setProperty('--frame-scale',scale);
  };
  fit();window.addEventListener('resize',fit);
  if(window.ResizeObserver)new ResizeObserver(fit).observe(frame.parentElement);
  const panel=document.querySelector('.inventory-panel');
  const header=panel.querySelector('.inventory-head');
  const filters=header.querySelector('.filters'),toolbar=panel.querySelector('.inventory-toolbar');
  const searchRow=document.createElement('div');searchRow.className='inventory-search-row';
  searchRow.append($('inventorySearch'));header.after(searchRow);
  const controls=document.createElement('details');controls.className='inventory-controls';
  const summary=document.createElement('summary');summary.textContent='筛选与排序';
  controls.append(summary,filters,toolbar);searchRow.after(controls);
  document.querySelector('.prototype').textContent='16:9 · 套装与成长数值已确认 · 系统为交互预览';
}
initializeWidePreview();
reset();
