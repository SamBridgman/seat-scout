import React, { useEffect, useRef, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { Armchair, ArrowDownUp, ArrowLeft, ArrowRight, BookOpen, Check, ChevronDown, ChevronRight, Clock3, Coffee, Compass, ExternalLink, Grid2X2, Headphones, HeartHandshake, Info, Map, MapPin, Menu, MessageSquare, Navigation, Plus, Search, Send, SlidersHorizontal, Sparkles, Volume1, Volume2, VolumeX, Wifi, X, Zap } from 'lucide-react';
import { spaces, seedReports, SEATS, ATMOSPHERES, conditions, ageLabel, loadReports } from './data';
import './styles.css';

const Icon = ({ icon: Component, size = 18, ...props }) => <Component size={size} strokeWidth={1.7} aria-hidden="true" {...props}/>;
const atmosphereIcon = value => value === 'Quiet' ? VolumeX : value === 'Noisy' ? Volume2 : Volume1;

function LibraryArt({ type = 'library', hero = false }) {
  const palette = { library: ['#d7daca','#f3efdf','#93a58e','#667b67'], lounge: ['#e4d4be','#f7ecd9','#b28c65','#697e70'], stone: ['#ccd9dc','#eef2ed','#7998a0','#496e7c'], pardee: ['#d7cfdf','#ede6ef','#9a85a3','#6f677e'], science: ['#d6dfca','#edf0dd','#91a276','#5f795d'] }[type];
  return <svg className={`library-art ${hero ? 'hero-art' : ''}`} viewBox="0 0 600 260" fill="none" aria-hidden="true">
    <rect width="600" height="260" fill={hero ? 'transparent' : palette[0]}/>
    <circle cx="490" cy="55" r="35" fill={hero ? '#538070' : palette[1]} opacity=".75"/>
    <path d="M0 222L600 182V260H0Z" fill={hero ? '#315e50' : palette[2]} opacity=".3"/>
    <path d="M110 213V88L346 49L493 94V215L259 249Z" fill={hero ? '#598371' : palette[2]}/>
    <path d="M110 88L346 49V204L110 240Z" fill={hero ? '#a5bb9c' : palette[1]}/>
    <path d="M110 88L346 49L493 94L259 134Z" fill={hero ? '#c4d1b4' : '#faf8ee'}/>
    <path d="M259 134L493 94V215L259 249Z" fill={hero ? '#789a7d' : palette[2]}/>
    {[0,1,2,3,4,5,6].map(i => <g key={i}><path d={`M${126+i*30} ${103-i*5}v99l17-3v-99Z`} fill={hero ? '#315e50' : palette[3]}/><path d={`M${126+i*30} ${143-i*5}l17-3m-17 30l17-3`} stroke={hero ? '#adc2a7' : palette[1]} strokeWidth="3"/></g>)}
    {[0,1,2,3,4,5].map(i => <g key={i}><path d={`M${277+i*34} ${145-i*6}v25l17-3v-25Z`} fill={hero ? '#d7dfbd' : palette[3]}/><path d={`M${277+i*34} ${188-i*6}v25l17-3v-25Z`} fill={hero ? '#315e50' : palette[3]}/></g>)}
    <path d="M255 248L487 214L525 227L292 265Z" fill={hero ? '#c4d1b4' : palette[1]}/>
    <path d="M82 244V155M528 226V132M54 246V183" stroke={hero ? '#b3c5a7' : palette[3]} strokeWidth="5"/>
    <ellipse cx="82" cy="152" rx="31" ry="44" fill={hero ? '#749778' : palette[2]}/><ellipse cx="528" cy="128" rx="33" ry="48" fill={hero ? '#8bab85' : palette[3]}/><ellipse cx="54" cy="180" rx="22" ry="32" fill={hero ? '#9bb594' : palette[3]}/>
    <path d="M331 70L393 88L338 98L277 80Z" fill={hero ? '#92ad94' : palette[0]}/><path d="M338 98v14l55-10V88" fill={hero ? '#5a7f69' : palette[3]}/>
    {!hero && <><path d="M0 254L600 227" stroke="#ffffff" strokeOpacity=".5" strokeWidth="2"/><path d="M0 248l64-9" stroke="#ffffff" strokeOpacity=".6" strokeWidth="3"/></>}
  </svg>;
}

function CampusMap({ results, onSelect }) {
  return <div className="campus-map"><svg viewBox="0 0 1000 490" role="img" aria-label="Illustrative campus map, not for navigation">
    <defs><pattern id="mapGrid" width="30" height="30" patternUnits="userSpaceOnUse"><path d="M30 0H0V30" stroke="#e0e6dd" fill="none"/></pattern></defs>
    <rect width="1000" height="490" fill="#eef1e8"/><rect width="1000" height="490" fill="url(#mapGrid)"/>
    <path d="M-20 45Q400 210 1050 40" stroke="#c5dfe6" strokeWidth="92" fill="none"/><path d="M-20 45Q400 210 1050 40" stroke="#ebf8fc" strokeWidth="2" strokeDasharray="7 8" fill="none"/>
    <path d="M-20 420L1040 192" stroke="white" strokeWidth="47"/><path d="M-20 420L1040 192" stroke="#d6dcd3" strokeWidth="2"/><path d="M230 490L143 160M650 490L568 173M885 490L805 145" stroke="white" strokeWidth="22"/>
    {[[80,240,90,50],[245,229,100,58],[365,190,85,80],[495,195,76,55],[658,178,95,65],[790,166,80,46],[305,359,120,80],[465,320,110,90],[695,290,105,70],[835,300,90,110],[70,396,90,66],[145,315,69,50]].map(([x,y,w,h],i)=><rect key={i} x={x} y={y} width={w} height={h} rx="5" fill={i%3===0?'#ccdbc8':'#dce1d6'} stroke="#c1cdbc" strokeWidth="1.5"/>)}
    <text x="470" y="94" fill="#63858c" fontSize="17" fontStyle="italic" transform="rotate(-3 470 94)">Charles River</text><text x="335" y="337" fill="#959e90" fontSize="12" letterSpacing="2" transform="rotate(-12 335 337)">COMMONWEALTH AVENUE</text><text x="75" y="185" fill="#71856e" fontSize="12">BU Beach</text><text x="740" y="439" fill="#82907c" fontSize="14">Boston University</text>
  </svg><div className="map-pins">{results.map(s => <button key={s.id} className="map-pin" style={{left:`${s.x}%`, top:`${s.y}%`}} onClick={()=>onSelect(s)} aria-label={`View ${s.name}`}><Icon icon={BookOpen} size={16}/><span>{s.name.replace('Memorial Library','Library').replace('Science & Engineering Library','Science & Engineering')}</span></button>)}</div><span className="map-disclaimer"><Icon icon={Info} size={13}/> Illustrative campus map</span></div>;
}

function Modal({ title, children, onClose, wide = false }) {
  const ref = useRef(null);
  const closeRef = useRef(onClose);
  closeRef.current = onClose;
  useEffect(() => {
    const previous = document.activeElement;
    const originalOverflow = document.body.style.overflow;
    document.body.style.overflow='hidden';
    const timer = setTimeout(()=>ref.current?.querySelector('button')?.focus(), 0);
    function keydown(e) {
      if(e.key==='Escape') closeRef.current();
      if(e.key==='Tab') {
        const items = [...ref.current.querySelectorAll('button, a[href], select, input, [tabindex="0"]')].filter(el=>!el.disabled);
        const first=items[0], last=items[items.length-1];
        if(e.shiftKey && document.activeElement===first){e.preventDefault();last?.focus();}
        else if(!e.shiftKey && document.activeElement===last){e.preventDefault();first?.focus();}
      }
    }
    document.addEventListener('keydown',keydown);
    return ()=>{clearTimeout(timer);document.body.style.overflow=originalOverflow;document.removeEventListener('keydown',keydown);previous?.focus();};
  },[]);
  return <div className="modal-overlay" onMouseDown={e=>{if(e.target===e.currentTarget)onClose();}}><section ref={ref} className={`modal ${wide?'detail-modal':''}`} role="dialog" aria-modal="true" aria-label={title}><button className="close-button icon-button" onClick={onClose} aria-label="Close dialog"><Icon icon={X} size={21}/></button>{children}</section></div>;
}

function App() {
  const [page,setPage]=useState('explore');
  const [query,setQuery]=useState('');
  const [seatsFilter,setSeatsFilter]=useState('All seating');
  const [noiseFilter,setNoiseFilter]=useState('Any atmosphere');
  const [power,setPower]=useState(false);
  const [sort,setSort]=useState('Recommended');
  const [view,setView]=useState('grid');
  const [selected,setSelected]=useState(null);
  const [reportSpace,setReportSpace]=useState(null);
  const [newSeats,setNewSeats]=useState('');
  const [newNoise,setNewNoise]=useState('');
  const [personal,setPersonal]=useState(()=>{try{return loadReports(localStorage);}catch{return [];}});
  const [clock,setClock]=useState(Date.now());
  const [toast,setToast]=useState(null);
  const [menu,setMenu]=useState(false);
  const [storageError,setStorageError]=useState(false);
  const allReports=[...seedReports,...personal];
  const getConditions=id=>conditions(id,allReports,clock);
  useEffect(()=>{const t=setInterval(()=>setClock(Date.now()),15000);return ()=>clearInterval(t);},[]);
  useEffect(()=>{if(!toast)return;const t=setTimeout(()=>setToast(null),5000);return()=>clearTimeout(t);},[toast]);
  const filtered=spaces.filter(s=>{
    const c=getConditions(s.id);
    return `${s.name} ${s.area} ${s.category}`.toLowerCase().includes(query.toLowerCase())&&(seatsFilter==='All seating'||c.seats===seatsFilter)&&(noiseFilter==='Any atmosphere'||c.atmosphere===noiseFilter)&&(!power||s.power);
  }).sort((a,b)=>sort==='Nearest first'?a.walk-b.walk:sort==='Recently reported'?(getConditions(b.id).newest||0)-(getConditions(a.id).newest||0):sort==='Name A–Z'?a.name.localeCompare(b.name):0);
  const filterCount=(seatsFilter!=='All seating'?1:0)+(noiseFilter!=='Any atmosphere'?1:0)+(power?1:0);
  function clearFilters(){setQuery('');setSeatsFilter('All seating');setNoiseFilter('Any atmosphere');setPower(false);}
  function openReport(space){setNewSeats('');setNewNoise('');setSelected(null);setReportSpace(space);}
  function submitReport(e){
    e.preventDefault();
    if(!newSeats||!newNoise||!reportSpace)return;
    const now=Date.now();
    const report={id:crypto.randomUUID(),spaceId:reportSpace.id,seats:newSeats,atmosphere:newNoise,createdAt:now};
    const next=[...personal,report].slice(-100);
    try{localStorage.setItem('seatscout.reports.v1',JSON.stringify(next));setStorageError(false);}catch{setStorageError(true);}
    setPersonal(next);setClock(now);setReportSpace(null);setToast('Report added. Thanks for helping another Terrier!');
  }
  const navigate=next=>{setPage(next);setMenu(false);};
  return <div className="app-shell">
    {menu&&<div className="nav-scrim" onClick={()=>setMenu(false)}/>}
    <aside className={`sidebar ${menu?'is-open':''}`}>
      <a href="#" className="brand" onClick={e=>{e.preventDefault();navigate('explore');}}><span className="brand-mark"><Armchair size={25} strokeWidth={2}/></span><span>Seat-Scout<span className="brand-period">.</span></span></a>
      <div className="campus-label"><span className="bu-box">BU</span><span>Boston University<small>Made for your next study session</small></span></div>
      <div className="nav-label">Your campus, a little closer</div>
      <nav aria-label="Main navigation"><button className={page==='explore'?'active':''} onClick={()=>navigate('explore')}><Icon icon={Compass}/>Find a space<ChevronRight className="nav-chevron" size={15}/></button><button className={page==='reports'?'active':''} onClick={()=>navigate('reports')}><Icon icon={MessageSquare}/>My reports{personal.length>0&&<span className="nav-count">{personal.length}</span>}</button><button className={page==='about'?'active':''} onClick={()=>navigate('about')}><Icon icon={HeartHandshake}/>How it works</button></nav>
      <div className="sidebar-bottom"><div className="help-card"><span className="mini-spark"><Icon icon={Sparkles} size={19}/></span><strong>A little update.<br/>A big help.</strong><p>Found a spot? Let the next person know what it’s like.</p><button onClick={()=>{navigate('explore');setToast('Choose a study space, then select Report conditions.');}}>Share what you see <ArrowRight size={15}/></button></div><div className="sidebar-footer"><span className="status-dot"/>Built for your next study session.<small>CS 501 · Seat-Scout project</small></div></div>
    </aside>
    <div className="main-shell"><header className="topbar"><div className="breadcrumb"><button className="mobile-menu icon-button" aria-label="Open menu" onClick={()=>setMenu(true)}><Menu size={21}/></button><span>Campus</span><ChevronRight size={14}/><strong>{page==='explore'?'Find a space':page==='reports'?'My reports':'How it works'}</strong></div><div className="topbar-right"><span className="demo-pill"><span/> Demo mode</span><span className="topbar-divider"/><span className="avatar">MJ</span><span className="profile-name">Hey, Terrier <span>✌</span></span></div></header>
    <main>
      {page==='explore'&&<>
        <section className="page-heading"><div><div className="eyebrow"><span className="little-line"/>A good study session starts here</div><h1>Find your focus.</h1><p>Your next study spot, minus the guesswork.</p></div><div className="heading-note"><span className="online-dot"/>Built around student observations<span>Sample conditions for this demo</span></div></section>
        <section className="hero"><div className="hero-copy"><span className="hero-tag"><Sparkles size={13}/>Less wandering. More working.</span><h2>A seat that suits<br/>your state of mind.</h2><p>A quiet corner or a table for two.<br/>Find a space that works for you.</p><button onClick={()=>document.getElementById('spaces-search')?.focus()}>Explore study spaces <ArrowRight size={16}/></button></div><LibraryArt hero/><div className="hero-caption"><span className="caption-icon"><BookOpen size={17}/></span><div>Somewhere on campus,<small>your next good idea is waiting.</small></div></div></section>
        <div className="discovery-heading"><h2>Make yourself at home<span>5 spaces to explore</span></h2><div className="view-toggle" aria-label="View style"><button className={view==='grid'?'selected':''} onClick={()=>setView('grid')} aria-pressed={view==='grid'}><Grid2X2 size={15}/>Grid</button><button className={view==='map'?'selected':''} onClick={()=>setView('map')} aria-pressed={view==='map'}><Map size={15}/>Map</button></div></div>
        <section className="filters" aria-label="Search and filter study spaces"><label className="search-field"><Search size={18}/><input id="spaces-search" aria-label="Search study spaces" placeholder="Search a library, lounge, or study space" value={query} onChange={e=>setQuery(e.target.value)}/>{query&&<button aria-label="Clear search" onClick={()=>setQuery('')}><X size={15}/></button>}</label><div className="filter-row"><label className={`select-filter ${seatsFilter!=='All seating'?'filter-active':''}`}><Armchair size={16}/><select aria-label="Seating filter" value={seatsFilter} onChange={e=>setSeatsFilter(e.target.value)}><option>All seating</option>{SEATS.map(x=><option key={x}>{x}</option>)}<option>Unknown</option><option>Mixed</option></select><ChevronDown size={13}/></label><label className={`select-filter ${noiseFilter!=='Any atmosphere'?'filter-active':''}`}><Volume1 size={16}/><select aria-label="Atmosphere filter" value={noiseFilter} onChange={e=>setNoiseFilter(e.target.value)}><option>Any atmosphere</option>{ATMOSPHERES.map(x=><option key={x}>{x}</option>)}<option>Unknown</option><option>Mixed</option></select><ChevronDown size={13}/></label><button className={`power-filter ${power?'filter-active':''}`} aria-pressed={power} onClick={()=>setPower(!power)}><Zap size={15}/>Power outlets{power&&<Check size={14}/>}</button>{filterCount>0&&<button className="clear-filters" onClick={clearFilters}>Reset filters</button>}</div></section>
        <div className="results-bar"><span><strong>{filtered.length}</strong> study {filtered.length===1?'space':'spaces'}{filterCount>0||query?' match your search':' on your campus'}<span className="results-dot">·</span><span className="results-fresh">Observations expire after 30 minutes</span></span><label className="sort-control"><ArrowDownUp size={14}/><select aria-label="Sort spaces" value={sort} onChange={e=>setSort(e.target.value)}><option>Recommended</option><option>Nearest first</option><option>Recently reported</option><option>Name A–Z</option></select><ChevronDown size={12}/></label></div>
        {!filtered.length?<div className="empty-state"><Search size={32}/><h3>No spaces match just yet.</h3><p>Try another name or loosen your filters.</p><button className="primary-button" onClick={clearFilters}>Clear search & filters</button></div>:view==='map'?<CampusMap results={filtered} onSelect={setSelected}/>:<div className="space-grid">{filtered.map(space=><SpaceCard key={space.id} space={space} condition={getConditions(space.id)} onSelect={()=>setSelected(space)}/>)}</div>}
        <div className="community-strip"><span className="community-icon"><HeartHandshake size={23}/></span><div><strong>Good spaces are better when we share.</strong><p>Your quick report can save someone a walk across campus.</p></div><button onClick={()=>navigate('about')}>See how it works <ArrowRight size={16}/></button></div>
        <p className="data-note"><Info size={14}/>Presentation prototype. Locations, amenities, walking times, and conditions are illustrative. Verify access and hours before visiting.</p>
      </>}
      {page==='reports'&&<><section className="page-heading"><div className="eyebrow">A little campus kindness</div><h1>Your observations.</h1><p>Small updates that help someone find their spot.</p></section><div className="notice"><Info size={18}/><span>These demo reports are saved on this browser only. They aren’t shared with other students.{storageError?' Browser storage is unavailable; reports last for this session only.':''}</span></div>{personal.length?<div className="reports-list">{[...personal].reverse().map(r=>{const s=spaces.find(s=>s.id===r.spaceId);return <article key={r.id}><span className="report-icon"><Check size={20}/></span><div><h3>{s.name}</h3><p>{s.area}</p><div className="report-tags"><span>{r.seats}</span><span>{r.atmosphere}</span></div></div><div className="report-date"><span>{ageLabel(r.createdAt,clock)}</span><small>{clock-r.createdAt>=1800000?'Expired · no longer in conditions':'Included in current demo conditions'}</small><button onClick={()=>setSelected(s)}>View space <ArrowRight size={14}/></button></div></article>;})}</div>:<div className="empty-state"><MessageSquare size={36}/><h3>Your first report goes a long way.</h3><p>Visit a study space and share the seating and atmosphere you observe.</p><button className="primary-button" onClick={()=>navigate('explore')}>Find a study space <ArrowRight size={16}/></button></div>}</>}
      {page==='about'&&<><section className="page-heading"><div className="eyebrow">Made by students, for students</div><h1>Less guesswork.<br/>More good study sessions.</h1><p>Seat-Scout adds a human perspective to your campus map.</p></section><div className="about-steps">{[[Compass,'Find your kind of space','Search campus spaces and filter by seating, atmosphere, and power.'],[BookOpen,'Know before you go','Check recent observations. The official zone and reported noise are shown separately.'],[MessageSquare,'Pay it forward','Share what you see: many seats, a few, or full; quiet, moderate, or noisy.']].map(([icon,title,copy],i)=><article key={title}><span className="step-number">0{i+1}</span><Icon icon={icon} size={28}/><h3>{title}</h3><p>{copy}</p></article>)}</div><section className="about-trust"><h2>Conditions change. We show the uncertainty.</h2><p>Reports describe a moment, not a guarantee. Observations older than 30 minutes expire. When recent reports disagree, we show “Mixed.” With no fresh reports, we show “Unknown.”</p><div><span className="condition-pill many">Many seats</span><span className="condition-pill few">Few seats</span><span className="condition-pill full">Full</span><span className="condition-pill unknown">Unknown</span><span className="condition-pill unknown">Mixed</span></div></section><div className="notice"><Info size={20}/><p><strong>About this presentation demo</strong><br/>Built for the BU CS 501 Seat-Scout project by Manak, Sam, and Aaron. Uses illustrative study areas and sample reports; new observations are saved locally. This is a responsive web prototype of the proposal’s Android experience. No exact chair counts, reservations, or live campus data.</p></div><button className="primary-button" onClick={()=>navigate('explore')}>Find your next spot <ArrowRight size={16}/></button></>}
      <footer className="page-footer"><span>Seat-Scout<span className="brand-period">.</span><span className="footer-caption">A little less searching. A little more studying.</span></span><span>Made with care, at BU <span className="tiny-bu">BU</span></span></footer>
    </main></div>
    {selected&&<Modal title={selected.name} onClose={()=>setSelected(null)} wide><div className="detail-art"><LibraryArt type={selected.art}/><span className="art-tag">{selected.category}</span></div><div className="detail-content"><div className="detail-eyebrow"><MapPin size={14}/>Charles River Campus · Illustrative space</div><h2>{selected.name}</h2><p className="detail-area">{selected.area}</p><p className="detail-description">{selected.description}</p><div className="detail-meta"><span><VolumeX size={15}/>Official zone: {selected.zone}</span><span><Navigation size={15}/>{selected.walk} min walk · sample estimate</span></div><ConditionPanel condition={getConditions(selected.id)} clock={clock}/><h3 className="amenities-heading">A few things to know</h3><div className="amenity-grid">{selected.amenities.map(a=><span key={a}><Icon icon={a.includes('Power')?Zap:a==='Wi-Fi'?Wifi:a.includes('light')?Sparkles:Armchair} size={16}/>{a}</span>)}</div><p className="detail-caution"><Info size={14}/>Amenities, access, and hours need verification. Conditions can change quickly.</p><div className="detail-actions"><button className="primary-button" onClick={()=>openReport(selected)}><Plus size={17}/>Report conditions</button><a className="secondary-button" href={`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(selected.building)}`} target="_blank" rel="noreferrer"><Navigation size={16}/>Directions <ExternalLink size={13}/></a></div></div></Modal>}
    {reportSpace&&<Modal title={`Report conditions at ${reportSpace.name}`} onClose={()=>setReportSpace(null)}><div className="report-modal-content"><span className="report-heading-icon"><MessageSquare size={24}/></span><span className="modal-eyebrow">A quick update helps</span><h2>How’s the space?</h2><p className="report-location">{reportSpace.name}<br/><span>{reportSpace.area}</span></p><form onSubmit={submitReport}><fieldset><legend>How’s the seating?</legend><p>No need to count. Just your best observation.</p><div className="report-options">{SEATS.map((s,i)=><button type="button" key={s} aria-pressed={newSeats===s} className={newSeats===s?'chosen':''} onClick={()=>setNewSeats(s)}><Armchair size={21}/><strong>{s}</strong><small>{['Plenty of room','A few spots left','No seats spotted'][i]}</small>{newSeats===s&&<Check size={13} className="option-check"/>}</button>)}</div></fieldset><fieldset><legend>What’s the atmosphere like?</legend><p>Report what you hear, regardless of the official zone.</p><div className="report-options">{ATMOSPHERES.map((s,i)=><button type="button" key={s} aria-pressed={newNoise===s} className={newNoise===s?'chosen':''} onClick={()=>setNewNoise(s)}><Icon icon={atmosphereIcon(s)} size={21}/><strong>{s}</strong><small>{['Little disturbance','Background sound','Frequent sound'][i]}</small>{newNoise===s&&<Check size={13} className="option-check"/>}</button>)}</div></fieldset><div className="report-privacy"><Clock3 size={15}/><p>Reflects this moment. Expires from conditions in 30 minutes.<br/>Saved only on this browser for the demo.</p></div><button className="primary-button report-submit" type="submit" disabled={!newSeats||!newNoise}><Send size={16}/>Submit report</button></form></div></Modal>}
    {toast&&<div className="toast" role="status"><span><Check size={17}/></span>{toast}<button aria-label="Dismiss notification" onClick={()=>setToast(null)}><X size={16}/></button></div>}
    {storageError&&page!=='reports'&&<div className="storage-notice" role="status">Browser storage unavailable. Your reports will last for this session.</div>}
  </div>;
}

function SpaceCard({space,condition,onSelect}) {
  const seatClass=condition.seats==='Many seats'?'many':condition.seats==='Few seats'?'few':condition.seats==='Full'?'full':'unknown';
  return <article className="space-card"><button className="space-card-button" onClick={onSelect} aria-label={`View ${space.name}, ${space.area}`}><div className="card-art"><LibraryArt type={space.art}/><span className="art-tag">{space.category}</span><span className="walk-tag"><Navigation size={11}/>{space.walk} min</span></div><div className="card-body"><div className="card-location"><MapPin size={12}/>Charles River Campus</div><h3>{space.name}</h3><p className="space-area">{space.area}</p><div className="condition-row"><span className={`condition-pill ${seatClass}`}><span/>{condition.seats}</span><span className="noise-indicator"><Icon icon={atmosphereIcon(condition.atmosphere)} size={14}/>{condition.atmosphere}</span></div><div className="card-divider"/><div className="card-bottom"><span className="report-age"><Clock3 size={13}/>{ageLabel(condition.newest)}</span><span className="card-amenities">{space.power&&<Zap size={14}/>}<Wifi size={15}/><ChevronRight size={17}/></span></div></div></button></article>;
}

function ConditionPanel({condition,clock}) {
  return <section className="condition-panel"><div className="panel-heading"><h3>Current conditions</h3><span>Demo observations</span></div><div className="panel-values"><div><span><Armchair size={16}/>Seating</span><strong>{condition.seats}</strong></div><div><span><Icon icon={atmosphereIcon(condition.atmosphere)} size={16}/>Atmosphere</span><strong>{condition.atmosphere}</strong></div></div><div className="condition-source"><Clock3 size={13}/>{condition.count?`Based on ${condition.count} recent ${condition.count===1?'report':'reports'} · newest ${ageLabel(condition.newest,clock)}`:'No fresh observations. Be the first to share an update.'}</div>{(condition.seats==='Mixed'||condition.atmosphere==='Mixed')&&<p className="mixed-note">Recent observations disagree. Check the space before settling in.</p>}</section>;
}

createRoot(document.getElementById('root')).render(<App/>);

