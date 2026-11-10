// Refresh the existing EDU-account prototype in place; preserve node destinations.
const page=await figma.getNodeByIdAsync('0:1');await figma.setCurrentPageAsync(page);
await Promise.all(['Regular','Medium','Semi Bold','Bold'].map(style=>figma.loadFontAsync({family:'Inter',style})));
const changed=new Set(),created=[],removed=[];
const rgb=h=>({r:parseInt(h.slice(1,3),16)/255,g:parseInt(h.slice(3,5),16)/255,b:parseInt(h.slice(5,7),16)/255});
const paint=(h,opacity=1)=>[{type:'SOLID',color:rgb(h),opacity}];
const mark=n=>(changed.add(n.id),n);
function svg(parent,source,name){const n=figma.createNodeFromSvg(source);parent.appendChild(n);n.name=name;created.push(n.id,...n.findAll(()=>true).map(c=>c.id));return n;}
const icons={Discover:'<circle cx="12" cy="12" r="10"/><path d="m16 8-3 5-5 3 3-5Z"/>',Map:'<path d="m3 6 6-3 6 3 6-3v15l-6 3-6-3-6 3Zm6-3v15m6-12v15"/>',Saved:'<path d="M19 21l-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2Z"/>',Search:'<circle cx="10.5" cy="10.5" r="7.5"/><path d="m16 16 5 5"/>'};
function icon(parent,label,color){return svg(parent,`<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">${icons[label]}</svg>`,'Icon / '+label);}
function art(parent,w,index){const colors=[['#F0E4E0','#B76155','#743F37'],['#E8E7DE','#A29A64','#65654B'],['#DFE9EE','#7499AC','#3E6678'],['#E9E3ED','#A089AF','#6D587C'],['#E3EBE2','#83A28A','#4D7256']][index];const [bg,mid,dark]=colors;
const shapes=index===1?`<path d="M50 38h260v66H50Z" fill="#FAF9F1"/><path d="M66 52h65v45H66Zm82 0h65v45h-65Zm82 0h65v45h-65Z" fill="${mid}"/><path d="M88 108h185m-155 0v26m123-26v26" stroke="${dark}" stroke-width="7"/><path d="M143 98h40v9h-40Z" fill="${dark}"/>`:`<path d="M62 118V45L185 20l115 34v64Z" fill="${mid}"/><path d="M62 45l123-25v98H62Z" fill="#FAF8F0"/><path d="M62 45l123-25 115 34-122 23Z" fill="#FFFFFF"/><path d="M83 53v55m24-60v60m24-65v65m24-70v70" stroke="${dark}" stroke-width="13"/><path d="M200 83l80-14m-80 39 80-14" stroke="#E9EFE6" stroke-width="12"/>`;
return svg(parent,`<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="112" viewBox="0 0 360 140" preserveAspectRatio="none"><rect width="360" height="140" fill="${bg}"/><circle cx="305" cy="32" r="19" fill="#FFFFFF" opacity=".7"/><path d="M0 128 360 113v27H0Z" fill="${mid}" opacity=".3"/>${shapes}<path d="M34 105v35" stroke="${dark}" stroke-width="4"/><ellipse cx="34" cy="91" rx="17" ry="26" fill="${dark}" opacity=".7"/></svg>`,'Illustration / '+index);
}
const frames=page.children.filter(n=>n.type==='FRAME');
for(const f of frames){
 mark(f);const mobile=f.width<600;const key=f.name.split(' / ').slice(1).join(' / ');const body=f.children.find(n=>n.name==='Scrollable content');const nav=f.children.find(n=>n.name==='Navigation');const header=f.children.find(n=>n.name==='App header');
 if(body){mark(body);body.itemSpacing=mobile?16:20;}
 if(header){mark(header);header.paddingTop=header.paddingBottom=16;header.fills=paint('#FFFFFF',.94);for(const t of header.children.filter(n=>n.type==='TEXT')){mark(t);if(t.characters==='Seat-Scout'){t.fontSize=21;t.letterSpacing={unit:'PERCENT',value:-3};}else{t.characters='BU · DEMO';t.fontSize=11;t.letterSpacing={unit:'PERCENT',value:4};t.fills=paint('#8E253A');}}}
 if(body){const title=body.children.find(n=>n.type==='TEXT');if(title){mark(title);title.letterSpacing={unit:'PERCENT',value:-3};title.fontSize=mobile?32:44;title.lineHeight={unit:'PERCENT',value:110};}}
 if(nav){mark(nav);nav.paddingLeft=nav.paddingRight=mobile?20:40;nav.paddingTop=nav.paddingBottom=10;nav.fills=paint('#FFFFFF',.95);nav.effects=[{type:'BACKGROUND_BLUR',radius:20,visible:true}];nav.primaryAxisAlignItems='CENTER';nav.itemSpacing=8;
  const active=/Campus map|Directions/.test(key)?'Map':/^Saved/.test(key)?'Saved':'Discover';
  for(const b of nav.children.filter(n=>n.type==='FRAME')){mark(b);const t=b.findOne(n=>n.type==='TEXT');const label=t.characters;const selected=label===active;b.fills=paint(selected?'#F7E9EC':'#FFFFFF',selected?1:0);b.layoutSizingHorizontal='FILL';b.paddingTop=b.paddingBottom=12;b.paddingLeft=b.paddingRight=mobile?8:20;b.cornerRadius=24;b.itemSpacing=7;mark(t);t.fontSize=mobile?12:14;t.fills=paint(selected?'#9F1834':'#68686F');const n=icon(b,label,selected?'#9F1834':'#68686F');b.insertChild(0,n);}
 }
 for(const n of f.findAllWithCriteria({types:['FRAME']})){
 if(n.removed)continue;
  if(n.name==='Campus introduction'){
   mark(n);n.fills=paint('#701C32');n.cornerRadius=26;n.paddingTop=n.paddingBottom=20;n.paddingLeft=n.paddingRight=22;n.itemSpacing=10;
   const ts=n.children.filter(t=>t.type==='TEXT');mark(ts[0]);ts[0].characters='Space to think.';ts[0].fontSize=mobile?28:38;ts[0].letterSpacing={unit:'PERCENT',value:-3};mark(ts[1]);ts[1].characters='Your next good idea starts here.';ts[1].fontSize=mobile?14:17;ts[1].fills=paint('#F0D9E0');
   const c=n.children.find(c=>c.type==='FRAME');if(c){mark(c);c.paddingTop=c.paddingBottom=10;mark(c.children[0]);c.children[0].fontSize=14;c.children[0].characters='Find a space nearby';}
  }
  if(n.name.startsWith('Space / ')){
   mark(n);n.cornerRadius=24;n.strokes=paint('#FFFFFF');n.strokeWeight=1;n.effects=[{type:'DROP_SHADOW',color:{...rgb('#31313B'),a:.045},offset:{x:0,y:5},radius:18,spread:0,visible:true,blendMode:'NORMAL'}];
   const index=n.name.includes('PAL')?1:n.name.includes('Stone')?2:n.name.includes('Pardee')?3:n.name.includes('Engineering')?4:0;
   const old=n.children.find(c=>c.name==='Frame');if(old){removed.push(old.id,...old.findAll(()=>true).map(c=>c.id));const a=art(n,n.width,index);n.insertChild(0,a);old.remove();}
   const info=n.children.find(c=>c.name==='Space information');if(info){mark(info);info.paddingTop=info.paddingBottom=16;info.itemSpacing=8;const title=info.children.find(c=>c.type==='TEXT');mark(title);title.fontSize=18;title.letterSpacing={unit:'PERCENT',value:-2};}
  }
  if(['Unknown','Full','A few seats','Plenty of seats'].includes(n.name)){
   const theme=n.name==='Unknown'?['#F0F0F3','#6E6E78']:n.name==='Full'?['#FBE9EC','#A1233C']:n.name==='A few seats'?['#FFF2DC','#865A16']:['#E9F4EB','#246B43'];mark(n);n.fills=paint(theme[0]);for(const t of n.children.filter(c=>c.type==='TEXT')){mark(t);t.fills=paint(theme[1]);}
  }
  if(n.name==='Action / Search study spaces'){mark(n);n.primaryAxisAlignItems='MIN';n.paddingLeft=18;n.cornerRadius=16;n.fills=paint('#EAEAF0');const t=n.findOne(c=>c.type==='TEXT');mark(t);t.characters='Find a library or study space';t.fontSize=14;t.fontName={family:'Inter',style:'Regular'};t.fills=paint('#686873');const i=icon(n,'Search','#686873');n.insertChild(0,i);}
 }
 for(const n of f.findAll(()=>true)){
  if(!('reactions' in n)||!n.reactions.length)continue;
  const next=n.reactions.map(r=>({...r,actions:(r.actions||[]).map(a=>{
   if(a.type!=='NODE')return a;const dest=frames.find(d=>d.id===a.destinationId);if(!dest)return a;
   const target=dest.name.split(' / ').slice(1).join(' / ');const back=/Back|Cancel|Edit/.test(n.name);const report=/^(Report 0|Noise |Review )/.test(key)&&/^(Report 0|Noise |Review )/.test(target);
   const sheet=n.name==='Action / Report conditions'&&target==='Report 0';
   const transition=sheet?{type:'MOVE_IN',direction:'BOTTOM',matchLayers:false,duration:.3,easing:{type:'EASE_OUT'}}:report?{type:'PUSH',direction:back?'RIGHT':'LEFT',matchLayers:false,duration:.26,easing:{type:'EASE_OUT'}}:key==='Report 0'&&n.name==='Action / Cancel'?{type:'MOVE_OUT',direction:'BOTTOM',matchLayers:false,duration:.26,easing:{type:'EASE_OUT'}}:{type:'DISSOLVE',duration:.18,easing:{type:'EASE_OUT'}};
   return {...a,transition};
  })}));await n.setReactionsAsync(next);mark(n);
 }
}
return {createdNodeIds:created,mutatedNodeIds:[...changed],removedNodeIds:removed,frames:frames.length};
