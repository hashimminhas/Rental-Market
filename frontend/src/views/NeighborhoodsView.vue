<template>
  <div class="page">
    <div class="page-hd">
      <div>
        <h1 class="page-title">Neighborhoods</h1>
        <p class="page-sub">Explore Tartu's rental districts and compare average prices.</p>
      </div>
      <div style="display:flex;align-items:center;gap:10px">
        <input v-model="search" class="form-input" style="width:200px" placeholder="Search neighborhoods…" />
        <select v-model="sortBy" class="form-select" style="width:170px">
          <option value="name">Name A–Z</option>
          <option value="price_asc">Price: low → high</option>
          <option value="price_desc">Price: high → low</option>
          <option value="count">Most listings</option>
        </select>
      </div>
    </div>

    <!-- Compare bar -->
    <div v-if="cmp.length>0" class="cmp-bar">
      <span class="cmp-lbl">Comparing:</span>
      <span v-for="n in cmp" :key="n" class="cmp-chip">{{ n }}<button @click="toggleCmp(n)" class="cmp-x">×</button></span>
      <button v-if="cmp.length>=2" class="btn btn-primary btn-sm" @click="showModal=true">Compare now</button>
      <button class="btn btn-ghost btn-sm" @click="cmp=[]">Clear</button>
    </div>

    <div v-if="loading" class="card" style="padding:60px">
      <div class="state-loading" style="padding:0"><div class="spinner"></div><span>Loading…</span></div>
    </div>
    <div v-else class="nb-grid">
      <div v-for="n in displayed" :key="n.name" class="nbcard">
        <div class="nbcard-hd">
          <div class="nbcard-icon">{{ n.name.charAt(0) }}</div>
          <div style="flex:1;min-width:0">
            <div class="nbcard-name">{{ n.name }}</div>
            <div class="nbcard-city">Tartu, Estonia</div>
          </div>
          <button :class="['btn btn-icon cmp-btn', cmp.includes(n.name)&&'cmp-btn--on']" @click="toggleCmp(n.name)" title="Add to compare">
            <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M18 20V10M12 20V4M6 20v-6"/></svg>
          </button>
        </div>
        <div class="nbcard-stats">
          <div class="nbstat"><span class="nbsv">€{{ fmt(n.averagePrice) }}</span><span class="nbsl">avg rent</span></div>
          <div class="nbstat"><span class="nbsv">€{{ fmtD(n.averagePricePerSqm) }}</span><span class="nbsl">per m²</span></div>
          <div class="nbstat"><span class="nbsv">{{ n.listingCount }}</span><span class="nbsl">listings</span></div>
        </div>
        <div class="nbbar-wrap"><div class="nbbar-fill" :style="{width:barW(n.averagePrice)+'%'}"></div></div>
        <div class="nbcard-ft">
          <div class="stars">
            <span v-for="i in 5" :key="i" :class="['star',i<=stars(n)&&'star--on']">★</span>
          </div>
          <span class="afford">{{ affordLabel(n) }}</span>
          <span v-if="n.priceChangePercent!=null" :class="['nbch',chCls(n.priceChangePercent)]">
            {{ n.priceChangePercent>=0?'+':'' }}{{ Number(n.priceChangePercent).toFixed(1) }}%
          </span>
        </div>
      </div>
    </div>

    <!-- Compare modal -->
    <div v-if="showModal&&cmp.length>=2" class="modal-ov" @click.self="showModal=false">
      <div class="modal">
        <div class="modal-hd">
          <span class="modal-title">Neighborhood Comparison</span>
          <button class="btn btn-icon" @click="showModal=false">
            <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M18 6L6 18M6 6l12 12"/></svg>
          </button>
        </div>
        <table class="data-table">
          <thead><tr><th>Metric</th><th v-for="n in cmp" :key="n">{{ n }}</th></tr></thead>
          <tbody>
            <tr><td>Avg rent</td><td v-for="n in cmp" :key="n">€{{ fmt(smap[n]?.averagePrice) }}</td></tr>
            <tr><td>Avg €/m²</td><td v-for="n in cmp" :key="n">€{{ fmtD(smap[n]?.averagePricePerSqm) }}</td></tr>
            <tr><td>Listings</td><td v-for="n in cmp" :key="n">{{ smap[n]?.listingCount||0 }}</td></tr>
            <tr><td>Change</td><td v-for="n in cmp" :key="n" :class="chCls(smap[n]?.priceChangePercent)">{{ fmtCh(smap[n]?.priceChangePercent) }}</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const loading   = ref(true)
const search    = ref('')
const sortBy    = ref('name')
const cmp       = ref([])
const showModal = ref(false)

const HOODS = ['Kesklinn','Ülejõe','Tammelinn','Annelinn','Karlova','Veeriku','Tähtvere','Supilinn','Ränilinn','Maarjamõisa']
const analytics = ref([])

const smap = computed(() => { const m={}; analytics.value.forEach(n=>{m[n.neighborhood]=n}); return m })

const all = computed(() => HOODS.map(name => {
  const s = smap.value[name] || {}
  return { name, averagePrice:s.averagePrice||0, averagePricePerSqm:s.averagePricePerSqm||0, listingCount:s.listingCount||0, priceChangePercent:s.priceChangePercent??null }
}))

const displayed = computed(() => {
  let a = all.value
  if(search.value){ const q=search.value.toLowerCase(); a=a.filter(n=>n.name.toLowerCase().includes(q)) }
  switch(sortBy.value){
    case 'price_asc':  return [...a].sort((x,y)=>x.averagePrice-y.averagePrice)
    case 'price_desc': return [...a].sort((x,y)=>y.averagePrice-x.averagePrice)
    case 'count':      return [...a].sort((x,y)=>y.listingCount-x.listingCount)
    default:           return [...a].sort((x,y)=>x.name.localeCompare(y.name))
  }
})

const maxP = computed(() => Math.max(...all.value.map(n=>n.averagePrice),1))
const barW  = p => Math.round((p/maxP.value)*100)
const stars = n => { const p=n.averagePrice; if(!p) return 3; if(p<450) return 5; if(p<600) return 4; if(p<800) return 3; if(p<1000) return 2; return 1 }
const affordLabel = n => ['','Premium','Above avg','Average','Good value','Best value'][stars(n)]

const fmt  = v => v ? Number(v).toLocaleString('et-EE',{maximumFractionDigits:0}) : '—'
const fmtD = v => v ? Number(v).toFixed(1) : '—'
const fmtCh = v => v==null ? '—' : (v>=0?'+':'')+Number(v).toFixed(1)+'%'
const chCls = v => v==null?'':v>0?'ch-up':v<0?'ch-down':''

function toggleCmp(name){
  const i=cmp.value.indexOf(name)
  if(i>=0) cmp.value.splice(i,1)
  else if(cmp.value.length<4) cmp.value.push(name)
}

onMounted(async()=>{
  try{ const r=await fetch('/api/analytics/neighborhoods'); if(r.ok) analytics.value=await r.json() }
  catch{} finally{ loading.value=false }
})
</script>

<style scoped>
.page       { display:flex; flex-direction:column; gap:18px; }
.page-hd    { display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:12px; }
.page-title { font-size:1.375rem; font-weight:700; color:var(--text); margin:0 0 4px; }
.page-sub   { font-size:.875rem; color:var(--muted); margin:0; }

.cmp-bar  { background:var(--primary-light); border:1px solid var(--primary); border-radius:var(--r); padding:10px 16px; display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.cmp-lbl  { font-size:.82rem; font-weight:600; color:var(--primary-dark); }
.cmp-chip { display:flex; align-items:center; gap:4px; background:#fff; border:1px solid var(--primary); border-radius:20px; padding:2px 8px 2px 10px; font-size:.8rem; color:var(--primary-dark); }
.cmp-x    { background:none; border:none; cursor:pointer; color:var(--primary); font-size:1rem; line-height:1; padding:0; }

.nb-grid  { display:grid; grid-template-columns:repeat(auto-fill,minmax(250px,1fr)); gap:16px; }

.nbcard {
  background:var(--card); border:1px solid var(--border); border-radius:var(--r);
  padding:18px; box-shadow:var(--shadow); display:flex; flex-direction:column; gap:12px;
  transition:box-shadow .2s,border-color .2s;
}
.nbcard:hover { box-shadow:0 4px 16px rgba(0,0,0,.1); border-color:var(--primary); }

.nbcard-hd   { display:flex; align-items:center; gap:10px; }
.nbcard-icon { width:40px; height:40px; border-radius:10px; background:var(--primary-light); color:var(--primary); font-size:1.1rem; font-weight:700; display:flex; align-items:center; justify-content:center; flex-shrink:0; }
.nbcard-name { font-size:.9rem; font-weight:600; color:var(--text); }
.nbcard-city { font-size:.75rem; color:var(--muted); }
.cmp-btn     { margin-left:auto; color:var(--muted); }
.cmp-btn--on { background:var(--primary-light); color:var(--primary); }

.nbcard-stats { display:grid; grid-template-columns:repeat(3,1fr); text-align:center; gap:4px; }
.nbstat { display:flex; flex-direction:column; align-items:center; gap:1px; }
.nbsv   { font-size:.9rem; font-weight:700; color:var(--text); }
.nbsl   { font-size:.7rem; color:var(--muted); }

.nbbar-wrap { height:4px; background:var(--border); border-radius:2px; overflow:hidden; }
.nbbar-fill { height:100%; background:var(--primary); border-radius:2px; transition:width .4s; }

.nbcard-ft { display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:4px; }
.stars  { display:flex; gap:1px; }
.star   { color:#e2e8f0; font-size:1rem; }
.star--on { color:#f59e0b; }
.afford { font-size:.75rem; color:var(--muted); font-weight:500; }
.nbch   { font-size:.78rem; font-weight:600; }
.ch-up  { color:#16a34a; }
.ch-down { color:#dc2626; }

.modal-ov { position:fixed; inset:0; background:rgba(0,0,0,.4); display:flex; align-items:center; justify-content:center; z-index:200; }
.modal    { background:#fff; border-radius:var(--r); padding:24px; min-width:400px; max-width:95vw; box-shadow:0 20px 60px rgba(0,0,0,.2); }
.modal-hd { display:flex; justify-content:space-between; align-items:center; margin-bottom:18px; }
.modal-title { font-size:1rem; font-weight:700; color:var(--text); }
</style>
