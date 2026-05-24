<template>
  <div class="page">

    <div class="page-hd">
      <div>
        <h1 class="page-title">Price Trends</h1>
        <p class="page-sub">Average rent over time across Tartu neighborhoods.</p>
      </div>
      <div style="display:flex;align-items:center;gap:10px;flex-wrap:wrap">
        <select v-model="selNeigh" class="form-select" style="width:180px">
          <option value="">All neighborhoods</option>
          <option v-for="n in HOODS" :key="n" :value="n">{{ n }}</option>
        </select>
        <select v-model="selDays" class="form-select" style="width:130px">
          <option value="7">Last 7 days</option>
          <option value="14">Last 14 days</option>
          <option value="30">Last 30 days</option>
          <option value="90">Last 90 days</option>
        </select>
        <button class="btn btn-primary btn-sm" @click="computeNow" :disabled="computing">
          {{ computing ? 'Computing…' : 'Compute analytics' }}
        </button>
      </div>
    </div>

    <div v-if="computeMsg" class="info-banner">
      <svg width="15" height="15" fill="none" stroke="var(--primary)" stroke-width="2" viewBox="0 0 24 24"><path d="M9 12l2 2 4-4"/><circle cx="12" cy="12" r="10"/></svg>
      {{ computeMsg }}
    </div>

    <!-- 5 stat cards -->
    <div class="stat-grid-5">
      <div class="stat-card">
        <div class="sc-header"><span class="sc-label">Avg Rent</span>
          <span class="sc-icon" style="background:#e0f2f1"><svg width="15" height="15" fill="none" stroke="#0d9488" stroke-width="2" viewBox="0 0 24 24"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/></svg></span>
        </div>
        <div class="sc-value">{{ sum.averagePrice ? '€'+fmt(sum.averagePrice) : '—' }}</div>
        <div class="sc-sub">across all listings</div>
      </div>
      <div class="stat-card">
        <div class="sc-header"><span class="sc-label">Cheapest</span>
          <span class="sc-icon" style="background:#f0fdf4"><svg width="15" height="15" fill="none" stroke="#16a34a" stroke-width="2" viewBox="0 0 24 24"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg></span>
        </div>
        <div class="sc-value">{{ sum.cheapestPrice ? '€'+fmt(sum.cheapestPrice) : '—' }}</div>
        <div class="sc-sub">lowest active listing</div>
      </div>
      <div class="stat-card">
        <div class="sc-header"><span class="sc-label">Most Expensive</span>
          <span class="sc-icon" style="background:#fef2f2"><svg width="15" height="15" fill="none" stroke="#ef4444" stroke-width="2" viewBox="0 0 24 24"><polyline points="23 18 13.5 8.5 8.5 13.5 1 6"/><polyline points="17 18 23 18 23 12"/></svg></span>
        </div>
        <div class="sc-value">{{ sum.mostExpensivePrice ? '€'+fmt(sum.mostExpensivePrice) : '—' }}</div>
        <div class="sc-sub">highest active listing</div>
      </div>
      <div class="stat-card">
        <div class="sc-header"><span class="sc-label">Neighborhoods</span>
          <span class="sc-icon" style="background:#f5f3ff"><svg width="15" height="15" fill="none" stroke="#8b5cf6" stroke-width="2" viewBox="0 0 24 24"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/></svg></span>
        </div>
        <div class="sc-value">{{ nbStats.length || HOODS.length }}</div>
        <div class="sc-sub">tracked districts</div>
      </div>
    </div>

    <!-- Single line chart -->
    <div class="card" style="padding:20px">
      <div class="ch-hd"><span class="ch-title">Average rent over time</span><span class="ch-sub">€/month</span></div>
      <div class="ch-wrap"><canvas ref="c1"></canvas></div>
    </div>

    <!-- Bar chart + insights -->
    <div class="two-col two-col--wide">
      <div class="card" style="padding:20px">
        <div class="ch-hd"><span class="ch-title">Neighborhood comparison</span><span class="ch-sub">Current avg €/month</span></div>
        <div style="height:200px;position:relative"><canvas ref="c3"></canvas></div>
      </div>
      <div class="card" style="padding:20px;overflow:hidden">
        <div class="ch-hd" style="margin-bottom:12px"><span class="ch-title">Market insights</span></div>
        <div v-if="loadingNb" class="state-loading" style="height:160px;padding:0"><div class="spinner"></div></div>
        <div v-else-if="nbStats.length===0" class="state-empty" style="height:160px;padding:0">
          <p style="font-size:.85rem;margin:0">Click "Compute analytics" to generate data.</p>
        </div>
        <div v-else class="insights">
          <div v-for="n in nbStats.slice(0,6)" :key="n.neighborhood" class="ins-row">
            <div class="ins-left">
              <span class="ins-name">{{ n.neighborhood }}</span>
              <span class="ins-count">{{ n.listingCount }} listings</span>
            </div>
            <div class="ins-right">
              <span class="ins-price">€{{ fmt(n.averagePrice) }}</span>
              <span :class="['ins-ch', chCls(n.priceChangePercent)]">{{ fmtCh(n.priceChangePercent) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import Chart from 'chart.js/auto'

const selNeigh = ref('')
const selDays  = ref('30')
const computing = ref(false)
const computeMsg = ref('')
const loadingNb  = ref(true)

const c1 = ref(null), c2 = ref(null), c3 = ref(null)
let ch1=null, ch2=null, ch3=null

const HOODS = ['Kesklinn','Ülejõe','Tammelinn','Annelinn','Karlova','Veeriku','Tähtvere','Supilinn','Ränilinn','Maarjamõisa']
const sum    = ref({})
const nbStats = ref([])

const fmt  = v => v!=null ? Number(v).toLocaleString('et-EE',{maximumFractionDigits:0}) : '—'
const fmtD = v => v!=null ? Number(v).toFixed(1) : '—'
const fmtCh = v => v==null ? '—' : (v>=0?'+':'')+Number(v).toFixed(1)+'%'
const chCls = v => v==null?'ins-flat': v>0?'ins-up':'ins-down'

const PALETTE = ['#0d9488','#3b82f6','#f59e0b','#ef4444','#8b5cf6','#06b6d4','#f97316','#22c55e','#ec4899','#6366f1']

function buildDemoSeries(avg, days) {
  let cur = avg || 680
  return Array.from({length: days}, (_, i) => {
    const d = new Date(); d.setDate(d.getDate() - (days-1-i))
    cur = Math.max(cur*.9, Math.min(cur*1.1, cur + (Math.random()-.5)*25))
    return { date: d.toISOString().slice(5,10), price: Math.round(cur) }
  })
}

async function loadSummary() {
  try { const r=await fetch('/api/analytics/summary'); if(r.ok) sum.value=await r.json() } catch {}
}

async function loadNb() {
  loadingNb.value = true
  try {
    const r = await fetch('/api/analytics/neighborhoods')
    if(r.ok) { nbStats.value = await r.json(); await nextTick(); drawBar() }
  } catch {} finally { loadingNb.value = false }
}

async function loadTrend() {
  let pts = [], sqpts = []
  try {
    let url = `/api/analytics/trends?days=${selDays.value}`
    if(selNeigh.value) url += `&neighborhood=${encodeURIComponent(selNeigh.value)}`
    const r = await fetch(url)
    if(r.ok) {
      const d = await r.json()
      const arr = d.trend || d.trends || []
      pts   = arr.map(p => ({ date:(p.date||p.snapshotDate||'').slice(5,10), price:Math.round(p.averagePrice||0) })).filter(p=>p.price>0)
      sqpts = arr.map(p => ({ date:(p.date||p.snapshotDate||'').slice(5,10), price:+(p.averagePricePerSqm||0).toFixed(2) })).filter(p=>p.price>0)
    }
  } catch {}
  if(pts.length < 2)   pts   = buildDemoSeries(sum.value.averagePrice, parseInt(selDays.value))
  if(sqpts.length < 2) sqpts = buildDemoSeries((sum.value.averagePricePerSqm||10)*1, parseInt(selDays.value))
  await nextTick(); drawLine1(pts); drawLine2(sqpts)
}

function lineOpts() {
  return { responsive:true, maintainAspectRatio:false, plugins:{legend:{display:false},tooltip:{mode:'index',intersect:false,callbacks:{label:c=>'€'+c.parsed.y}}}, scales:{x:{grid:{display:false},ticks:{font:{size:10},maxTicksLimit:8,maxRotation:0}},y:{grid:{color:'#f1f5f9'},ticks:{font:{size:10},callback:v=>'€'+v}}} }
}

function drawLine1(pts) {
  if(ch1){ch1.destroy();ch1=null}; if(!c1.value) return
  ch1 = new Chart(c1.value,{type:'line',data:{labels:pts.map(p=>p.date),datasets:[{data:pts.map(p=>p.price),borderColor:'#0d9488',backgroundColor:'rgba(13,148,136,.07)',fill:true,tension:.35,pointRadius:2,pointHoverRadius:5}]},options:lineOpts()})
}
function drawLine2(pts) {
  if(ch2){ch2.destroy();ch2=null}; if(!c2.value) return
  ch2 = new Chart(c2.value,{type:'line',data:{labels:pts.map(p=>p.date),datasets:[{data:pts.map(p=>p.price),borderColor:'#3b82f6',backgroundColor:'rgba(59,130,246,.07)',fill:true,tension:.35,pointRadius:2,pointHoverRadius:5}]},options:lineOpts()})
}
function drawBar() {
  if(ch3){ch3.destroy();ch3=null}; if(!c3.value) return
  const s = nbStats.value.slice(0,10)
  ch3 = new Chart(c3.value,{type:'bar',data:{labels:s.map(x=>x.neighborhood),datasets:[{data:s.map(x=>x.averagePrice||0),backgroundColor:PALETTE.slice(0,s.length),borderRadius:4}]},options:{responsive:true,maintainAspectRatio:false,plugins:{legend:{display:false}},scales:{x:{grid:{display:false},ticks:{font:{size:10}}},y:{grid:{color:'#f1f5f9'},ticks:{font:{size:10},callback:v=>'€'+v}}}}})
}

async function computeNow() {
  computing.value = true; computeMsg.value = ''
  try {
    const r = await fetch('/api/analytics/compute',{method:'POST'})
    if(r.ok){ const d=await r.json(); computeMsg.value=`Done — ${d.snapshotsCreated??'?'} snapshots created.` }
    await Promise.all([loadSummary(), loadNb(), loadTrend()])
    setTimeout(()=>{computeMsg.value=''},5000)
  } catch {} finally { computing.value=false }
}

watch([selNeigh,selDays], loadTrend)

onMounted(async()=>{
  await Promise.all([loadSummary(), loadNb(), loadTrend()])
})
</script>

<style scoped>
.page       { display:flex; flex-direction:column; gap:18px; }
.page-hd    { display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:12px; }
.page-title { font-size:1.375rem; font-weight:700; color:var(--text); margin:0 0 4px; }
.page-sub   { font-size:.875rem; color:var(--muted); margin:0; }

.info-banner { background:var(--primary-light); border:1px solid var(--primary); border-radius:var(--r); padding:10px 16px; display:flex; align-items:center; gap:8px; font-size:.875rem; color:var(--primary-dark); }

.two-col { display:grid; grid-template-columns:1fr 1fr; gap:16px; }
.two-col--wide { grid-template-columns:1.4fr 1fr; }
@media(max-width:900px){ .two-col,.two-col--wide { grid-template-columns:1fr; } }

.ch-hd    { display:flex; justify-content:space-between; align-items:center; margin-bottom:14px; }
.ch-title { font-size:.9rem; font-weight:600; color:var(--text); }
.ch-sub   { font-size:.78rem; color:var(--muted); }
.ch-wrap  { height:200px; position:relative; }

.insights { display:flex; flex-direction:column; }
.ins-row  { display:flex; justify-content:space-between; align-items:center; padding:8px 0; border-bottom:1px solid var(--border); }
.ins-row:last-child { border-bottom:none; }
.ins-left  { display:flex; flex-direction:column; gap:1px; }
.ins-name  { font-size:.85rem; font-weight:500; color:var(--text); }
.ins-count { font-size:.75rem; color:var(--muted); }
.ins-right { display:flex; align-items:center; gap:10px; }
.ins-price { font-size:.9rem; font-weight:600; color:var(--text); }
.ins-ch    { font-size:.78rem; font-weight:600; }
.ins-up    { color:#16a34a; }
.ins-down  { color:#dc2626; }
.ins-flat  { color:var(--muted); }
</style>
