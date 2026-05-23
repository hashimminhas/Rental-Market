<template>
  <div class="page">

    <!-- Header -->
    <div class="page-hd">
      <div>
        <h1 class="page-title">Rental Listings</h1>
        <p class="page-sub">{{ filteredListings.length }} of {{ listings.length }} listings match your filters.</p>
      </div>
      <div style="display:flex;align-items:center;gap:10px">
        <div class="view-toggle">
          <button :class="['vt-btn', view==='grid'&&'vt-on']" @click="view='grid'">
            <svg width="14" height="14" fill="currentColor" viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>
          </button>
          <button :class="['vt-btn', view==='list'&&'vt-on']" @click="view='list'">
            <svg width="14" height="14" fill="currentColor" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="2" rx="1"/><rect x="3" y="11" width="18" height="2" rx="1"/><rect x="3" y="18" width="18" height="2" rx="1"/></svg>
          </button>
        </div>
        <button class="btn btn-outline btn-sm" @click="triggerScrape" :disabled="scraping">
          <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>
          {{ scraping ? 'Refreshing…' : 'Refresh listings' }}
        </button>
      </div>
    </div>

    <!-- Filters -->
    <div class="card" style="padding:16px 20px">
      <div class="filter-row">
        <div class="fsearch">
          <svg width="14" height="14" fill="none" stroke="#94a3b8" stroke-width="2" viewBox="0 0 24 24" class="fsi"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
          <input v-model="q" class="finput" placeholder="Search listings…" />
        </div>
        <select v-model="fNeigh" class="form-select fsel">
          <option value="">All neighborhoods</option>
          <option v-for="n in neighborhoods" :key="n" :value="n">{{ n }}</option>
        </select>
        <select v-model="fSrc" class="form-select fsel">
          <option value="">All sources</option>
          <option value="KV_EE">KV.EE</option>
          <option value="CITY24">City24</option>
          <option value="KINNISVARA24">Kinnisvara24</option>
          <option value="RENDIN">Rendin</option>
          <option value="MAAMET">Maa-amet</option>
        </select>
        <div class="frange"><span class="frlbl">Max €</span><input v-model.number="fMaxP" type="number" class="finput frinp" placeholder="1200" /></div>
        <div class="frange"><span class="frlbl">Min m²</span><input v-model.number="fMinS" type="number" class="finput frinp" placeholder="30" /></div>
      </div>
      <div class="filter-row" style="margin-top:10px">
        <select v-model="fRooms" class="form-select fsel">
          <option value="">Any rooms</option>
          <option value="1">1 room</option>
          <option value="2">2 rooms</option>
          <option value="3">3 rooms</option>
          <option value="4">4+ rooms</option>
        </select>
        <select v-model="sortBy" class="form-select fsel">
          <option value="newest">Newest first</option>
          <option value="price_asc">Price: low → high</option>
          <option value="price_desc">Price: high → low</option>
          <option value="size_asc">Size: small → large</option>
          <option value="ppsqm">€/m²: low → high</option>
        </select>
        <button v-if="hasFilters" class="btn btn-ghost btn-sm" @click="clearF">
          <svg width="12" height="12" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M18 6L6 18M6 6l12 12"/></svg>
          Clear filters
        </button>
      </div>
    </div>

    <!-- States -->
    <div v-if="loading" class="card" style="padding:60px;text-align:center">
      <div class="state-loading" style="padding:0"><div class="spinner"></div><span>Loading listings…</span></div>
    </div>
    <div v-else-if="error" class="state-error">{{ error }}</div>
    <div v-else-if="paged.length===0" class="card" style="padding:60px">
      <div class="state-empty" style="padding:0">
        <svg width="36" height="36" fill="none" stroke="#94a3b8" stroke-width="1.5" viewBox="0 0 24 24"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/></svg>
        <p>No listings match your filters.</p>
        <button v-if="hasFilters" class="btn btn-outline btn-sm" @click="clearF">Clear filters</button>
      </div>
    </div>

    <!-- Grid view -->
    <div v-else-if="view==='grid'" class="listing-grid">
      <div v-for="l in paged" :key="l.listingId" class="lcard">
        <div class="lcard-top">
          <div style="display:flex;align-items:center;gap:5px">
            <span :class="['badge', srcBadge(l.source)]">{{ srcLabel(l.source) }}</span>
            <span v-if="l.synthetic" class="badge badge-demo" title="Demo data — not a real listing">Demo</span>
          </div>
          <button class="btn-save" @click="toggleSave(l)" :title="saved.has(l.listingId)?'Unsave':'Save'">
            <svg width="14" height="14" :fill="saved.has(l.listingId)?'var(--primary)':'none'" :stroke="saved.has(l.listingId)?'var(--primary)':'#94a3b8'" stroke-width="2" viewBox="0 0 24 24"><path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z"/></svg>
          </button>
        </div>
        <div class="lcard-title">{{ l.title || 'Apartment' }}</div>
        <div class="lcard-addr">
          <svg width="11" height="11" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
          {{ l.neighborhood||'Tartu' }}<span v-if="l.street"> · {{ l.street }}</span>
        </div>
        <div class="lcard-price">€{{ fmt(l.price) }}<span class="lcard-per">/month</span></div>
        <div class="lcard-chips">
          <span class="chip">{{ l.size ? l.size+' m²' : '—' }}</span>
          <span class="chip">{{ l.rooms ? l.rooms+(l.rooms===1?' room':' rooms') : '—' }}</span>
          <span class="chip" v-if="l.pricePerSqm">€{{ fmtD(l.pricePerSqm) }}/m²</span>
        </div>
        <div class="lcard-foot">
          <span class="lcard-age">{{ ago(l.scrapedAt) }}</span>
          <a :href="l.url||'#'" target="_blank" :class="['btn','btn-sm', l.synthetic ? 'btn-outline' : 'btn-primary']">
            {{ l.synthetic ? 'Search on site' : 'View listing' }}
          </a>
        </div>
      </div>
    </div>

    <!-- List view -->
    <div v-else class="card" style="padding:0;overflow:hidden">
      <table class="data-table">
        <thead><tr><th>Title</th><th>Source</th><th>Neighborhood</th><th>Rooms</th><th>Size</th><th>Price/mo</th><th>€/m²</th><th>Scraped</th><th></th></tr></thead>
        <tbody>
          <tr v-for="l in paged" :key="l.listingId">
            <td class="td-clip">{{ l.title||'Apartment' }}</td>
            <td><span :class="['badge', srcBadge(l.source)]">{{ srcLabel(l.source) }}</span></td>
            <td>{{ l.neighborhood||'—' }}</td>
            <td>{{ l.rooms||'—' }}</td>
            <td>{{ l.size ? l.size+' m²':'—' }}</td>
            <td style="font-weight:600;color:var(--primary)">€{{ fmt(l.price) }}</td>
            <td>{{ l.pricePerSqm ? '€'+fmtD(l.pricePerSqm):'—' }}</td>
            <td style="color:var(--muted);font-size:.8rem">{{ ago(l.scrapedAt) }}</td>
            <td>
              <div style="display:flex;align-items:center;gap:6px">
                <span v-if="l.synthetic" class="badge badge-demo">Demo</span>
                <a :href="l.url||'#'" target="_blank" :class="['btn','btn-sm', l.synthetic ? 'btn-outline' : 'btn-primary']">
                  {{ l.synthetic ? 'Search' : 'View' }}
                </a>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages>1" style="display:flex;align-items:center;justify-content:center;gap:14px">
      <button class="btn btn-outline btn-sm" :disabled="pg===1" @click="pg--">← Prev</button>
      <span style="font-size:.85rem;color:var(--muted)">Page {{ pg }} of {{ totalPages }}</span>
      <button class="btn btn-outline btn-sm" :disabled="pg===totalPages" @click="pg++">Next →</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'

const listings = ref([])
const loading  = ref(true)
const error    = ref(null)
const scraping = ref(false)
const view     = ref('grid')
const pg       = ref(1)
const SIZE     = 12
const saved    = ref(new Set())

const q       = ref('')
const fNeigh  = ref('')
const fSrc    = ref('')
const fMaxP   = ref(null)
const fMinS   = ref(null)
const fRooms  = ref('')
const sortBy  = ref('newest')

const neighborhoods = computed(() => [...new Set(listings.value.map(l=>l.neighborhood).filter(Boolean))].sort())
const hasFilters    = computed(() => !!(q.value||fNeigh.value||fSrc.value||fMaxP.value||fMinS.value||fRooms.value))

const filteredListings = computed(() => {
  let a = listings.value.filter(l => l.isActive !== false)
  if (q.value)     { const s = q.value.toLowerCase(); a = a.filter(l => (l.title||'').toLowerCase().includes(s)||(l.neighborhood||'').toLowerCase().includes(s)) }
  if (fNeigh.value) a = a.filter(l => l.neighborhood===fNeigh.value)
  if (fSrc.value)   a = a.filter(l => l.source===fSrc.value)
  if (fMaxP.value)  a = a.filter(l => l.price!=null && l.price<=fMaxP.value)
  if (fMinS.value)  a = a.filter(l => l.size!=null  && l.size>=fMinS.value)
  if (fRooms.value) { const r=parseInt(fRooms.value); a = a.filter(l => r===4?(l.rooms||0)>=4:l.rooms===r) }
  switch(sortBy.value){
    case 'price_asc':  return [...a].sort((x,y)=>(x.price||0)-(y.price||0))
    case 'price_desc': return [...a].sort((x,y)=>(y.price||0)-(x.price||0))
    case 'size_asc':   return [...a].sort((x,y)=>(x.size||0)-(y.size||0))
    case 'ppsqm':      return [...a].sort((x,y)=>(x.pricePerSqm||0)-(y.pricePerSqm||0))
    default:           return [...a].sort((x,y)=>new Date(y.scrapedAt||0)-new Date(x.scrapedAt||0))
  }
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredListings.value.length/SIZE)))
const paged      = computed(() => { const s=(pg.value-1)*SIZE; return filteredListings.value.slice(s,s+SIZE) })

watch([q,fNeigh,fSrc,fMaxP,fMinS,fRooms,sortBy], ()=>{ pg.value=1 })

function clearF(){ q.value=''; fNeigh.value=''; fSrc.value=''; fMaxP.value=null; fMinS.value=null; fRooms.value='' }
function srcLabel(s){ const m={KV_EE:'KV.EE',CITY24:'City24',KINNISVARA24:'K24',RENDIN:'Rendin',MAAMET:'Maa-amet'}; return m[s]||s||'?' }
function srcBadge(s){ const m={KV_EE:'badge-kv',CITY24:'badge-city',KINNISVARA24:'badge-k24',RENDIN:'badge-rendin',MAAMET:'badge-maamet'}; return m[s]||'badge-gray' }
function fmt(v){ return v!=null?Number(v).toLocaleString('et-EE',{maximumFractionDigits:0}):'—' }
function fmtD(v){ return v!=null?Number(v).toFixed(1):'—' }
function ago(ts){
  if(!ts) return '—'
  const d=Math.floor((Date.now()-new Date(ts))/60000)
  if(d<1)  return 'just now'
  if(d<60) return d+'m ago'
  const h=Math.floor(d/60)
  if(h<24) return h+'h ago'
  return Math.floor(h/24)+'d ago'
}
function toggleSave(l){ const s=new Set(saved.value); s.has(l.listingId)?s.delete(l.listingId):s.add(l.listingId); saved.value=s }

async function load(){
  loading.value=true; error.value=null
  try{
    const r=await fetch('/api/listings?size=500')
    if(!r.ok) throw new Error('HTTP '+r.status)
    const d=await r.json()
    listings.value=Array.isArray(d)?d:(d.content||d.listings||[])
  }catch(e){ error.value='Could not load listings — is the scraper service running?' }
  finally{ loading.value=false }
}

async function triggerScrape(){
  scraping.value=true
  try{ await fetch('/api/scraper/trigger',{method:'POST'}); await new Promise(r=>setTimeout(r,4000)); await load() }
  catch{} finally{ scraping.value=false }
}

onMounted(load)
</script>

<style scoped>
.page        { display:flex; flex-direction:column; gap:18px; }
.page-hd     { display:flex; justify-content:space-between; align-items:flex-start; }
.page-title  { font-size:1.375rem; font-weight:700; color:var(--text); margin:0 0 4px; }
.page-sub    { font-size:.875rem; color:var(--muted); margin:0; }

.filter-row  { display:flex; align-items:center; gap:10px; flex-wrap:wrap; }
.fsearch     { position:relative; flex:1; min-width:200px; }
.fsi         { position:absolute; left:10px; top:50%; transform:translateY(-50%); pointer-events:none; }
.finput      { width:100%; padding:7px 10px 7px 32px; border:1px solid var(--border); border-radius:var(--r); font-size:.85rem; color:var(--text); background:var(--bg); box-sizing:border-box; }
.finput:focus{ outline:none; border-color:var(--primary); box-shadow:0 0 0 3px rgba(13,148,136,.12); }
.fsel        { min-width:140px; font-size:.85rem; }
.frange      { display:flex; align-items:center; gap:6px; }
.frlbl       { font-size:.8rem; color:var(--muted); white-space:nowrap; }
.frinp       { padding-left:10px; max-width:85px; min-width:0; }

.view-toggle { display:flex; border:1px solid var(--border); border-radius:6px; overflow:hidden; }
.vt-btn      { padding:6px 9px; background:#fff; border:none; cursor:pointer; color:var(--muted); display:flex; align-items:center; }
.vt-btn+.vt-btn{ border-left:1px solid var(--border); }
.vt-on       { background:var(--primary-light); color:var(--primary); }

.listing-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(265px,1fr)); gap:16px; }

.lcard {
  background:var(--card); border:1px solid var(--border); border-radius:var(--r);
  padding:16px; display:flex; flex-direction:column; gap:9px;
  box-shadow:var(--shadow); transition:box-shadow .2s,border-color .2s;
}
.lcard:hover { box-shadow:0 4px 16px rgba(0,0,0,.1); border-color:var(--primary); }

.lcard-top   { display:flex; justify-content:space-between; align-items:center; }
.btn-save    { background:none; border:none; cursor:pointer; padding:2px; }
.lcard-title { font-size:.875rem; font-weight:600; color:var(--text); line-height:1.4; }
.lcard-addr  { display:flex; align-items:center; gap:4px; font-size:.78rem; color:var(--muted); }
.lcard-price { font-size:1.2rem; font-weight:700; color:var(--primary); }
.lcard-per   { font-size:.8rem; font-weight:400; color:var(--muted); margin-left:3px; }
.lcard-chips { display:flex; flex-wrap:wrap; gap:5px; }
.chip        { font-size:.75rem; background:var(--primary-light); color:var(--primary-dark); padding:2px 8px; border-radius:20px; font-weight:500; }
.lcard-foot  { display:flex; align-items:center; justify-content:space-between; margin-top:auto; padding-top:4px; }
.lcard-age   { font-size:.75rem; color:var(--light); }

.td-clip    { max-width:200px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; font-size:.85rem; }
.badge-demo { background:#fef3c7; color:#92400e; font-size:10px; border:1px solid #fde68a; }
</style>
