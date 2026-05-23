<template>
  <div class="page">
    <div class="page-hd">
      <div>
        <h1 class="page-title">System Status</h1>
        <p class="page-sub">Health and performance of all microservices. Last checked: {{ lastCheck }}</p>
      </div>
      <div style="display:flex;align-items:center;gap:10px">
        <button class="btn btn-outline btn-sm" @click="checkAll" :disabled="checking">
          <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>
          {{ checking ? 'Checking…' : 'Refresh all' }}
        </button>
        <button class="btn btn-primary btn-sm" @click="triggerScrape" :disabled="scraping">
          {{ scraping ? 'Running…' : 'Trigger scrape' }}
        </button>
      </div>
    </div>

    <!-- Overall health -->
    <div class="card" style="padding:18px 22px">
      <div style="display:flex;align-items:center;gap:12px;margin-bottom:10px">
        <div :class="['overall-dot', 'dot-'+overallCls]"></div>
        <span style="font-size:.95rem;font-weight:600;color:var(--text);flex:1">{{ overallLabel }}</span>
        <span style="font-size:.8rem;color:#16a34a;font-weight:500">{{ upCount }} up</span>
        <span style="font-size:.8rem;color:#d97706;font-weight:500">{{ warnCount }} degraded</span>
        <span style="font-size:.8rem;color:#dc2626;font-weight:500">{{ downCount }} down</span>
      </div>
      <div class="hbar-track"><div class="hbar-fill" :style="{width:healthPct+'%'}"></div></div>
    </div>

    <!-- Service grid -->
    <div class="svc-grid">
      <div v-for="s in services" :key="s.id" :class="['scard','scard--'+s.status]">
        <div class="scard-hd">
          <div class="scard-icon" :style="{background:sBg(s.status)}">
            <svg width="15" height="15" fill="none" :stroke="sColor(s.status)" stroke-width="2" viewBox="0 0 24 24" v-html="s.icon"></svg>
          </div>
          <div style="flex:1;min-width:0">
            <div class="scard-name">{{ s.name }}</div>
            <div class="scard-port">:{{ s.port }}</div>
          </div>
          <div :class="['sdot','sdot--'+s.status]"></div>
        </div>
        <div class="scard-meta">
          <div class="smeta"><span class="sml">Status</span><span :class="['badge',sBadge(s.status)]">{{ s.label }}</span></div>
          <div class="smeta" v-if="s.ms!=null"><span class="sml">Response</span><span class="smv">{{ s.ms }}ms</span></div>
        </div>
      </div>
    </div>

    <!-- Scraper panel + recent jobs -->
    <div class="two-col">
      <div class="card" style="padding:20px">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
          <span style="font-size:.9rem;font-weight:600;color:var(--text)">Scraper Status</span>
        </div>
        <div v-if="!scraperSt" class="state-loading" style="height:60px;padding:0"><div class="spinner"></div></div>
        <div v-else class="scraper-stats">
          <div class="sst">
            <span class="sst-lbl">Job status</span>
            <span :class="['badge', scraperSt.currentJobStatus==='RUNNING'?'badge-yellow':'badge-green']">{{ scraperSt.currentJobStatus||'IDLE' }}</span>
          </div>
          <div class="sst">
            <span class="sst-lbl">Active listings</span>
            <span class="sst-val">{{ scraperSt.totalActiveListings||0 }}</span>
          </div>
          <div class="sst">
            <span class="sst-lbl">Last scrape</span>
            <span class="sst-val">{{ scraperSt.lastScrapeTime ? ago(scraperSt.lastScrapeTime) : 'Never' }}</span>
          </div>
          <div class="sst" v-if="scraperSt.lastScrapeTime">
            <span class="sst-lbl">Timestamp</span>
            <span class="sst-val">{{ fmtDt(scraperSt.lastScrapeTime) }}</span>
          </div>
        </div>
      </div>
      <div class="card" style="padding:0;overflow:hidden">
        <div style="padding:16px 20px;border-bottom:1px solid var(--border)">
          <span style="font-size:.9rem;font-weight:600;color:var(--text)">Recent Scrape Jobs</span>
        </div>
        <table class="data-table">
          <thead><tr><th>Job ID</th><th>Started</th><th>Status</th><th>Found</th><th>New</th></tr></thead>
          <tbody>
            <tr v-if="jobs.length===0"><td colspan="5" style="text-align:center;color:var(--muted);padding:20px;font-size:.85rem">No jobs recorded yet</td></tr>
            <tr v-for="j in jobs.slice(0,8)" :key="j.jobId">
              <td style="font-family:monospace;font-size:.78rem;color:var(--muted)">{{ (j.jobId||'').slice(0,8) }}…</td>
              <td style="font-size:.82rem">{{ fmtDt(j.startedAt) }}</td>
              <td><span :class="['badge',jBadge(j.status)]">{{ j.status||'?' }}</span></td>
              <td>{{ j.listingsFound??'—' }}</td>
              <td>{{ j.newListings??'—' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const checking  = ref(false)
const scraping  = ref(false)
const lastCheck = ref('—')
const scraperSt = ref(null)
const jobs      = ref([])

const DEFS = [
  { id:'gateway',      name:'API Gateway',         port:8080, icon:'<path d="M5 12h14M12 5l7 7-7 7"/>' },
  { id:'scraper',      name:'Scraper Service',      port:8081, icon:'<circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>' },
  { id:'analytics',    name:'Analytics Service',    port:8082, icon:'<polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>' },
  { id:'user',         name:'User Service',          port:8083, icon:'<path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/>' },
  { id:'alert',        name:'Alert Service',         port:8084, icon:'<path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/>' },
  { id:'neighborhood', name:'Neighborhood Service', port:8085, icon:'<path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/>' },
  { id:'landlord',     name:'Landlord Service',     port:8086, icon:'<rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 00-2-2h-4a2 2 0 00-2 2v16"/>' },
  { id:'listing',      name:'Listing Service',      port:8087, icon:'<path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/>' },
  { id:'notification', name:'Notification Service', port:8088, icon:'<path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/>' },
]

const PATHS = {
  gateway:      '/actuator/health',
  scraper:      '/api/health/scraper',
  analytics:    '/api/health/analytics',
  user:         '/api/health/user',
  alert:        '/api/health/alert',
  neighborhood: '/api/health/neighborhood',
  landlord:     '/api/health/landlord',
  listing:      '/api/health/listing',
  notification: '/api/health/notification',
}

const services = ref(DEFS.map(d => ({...d, status:'unknown', label:'Checking…', ms:null})))

const upCount   = computed(() => services.value.filter(s=>s.status==='up').length)
const warnCount = computed(() => services.value.filter(s=>s.status==='warn').length)
const downCount = computed(() => services.value.filter(s=>s.status==='down').length)
const healthPct = computed(() => Math.round((upCount.value/services.value.length)*100))

const overallCls = computed(() => { if(!downCount.value&&!warnCount.value) return 'up'; if(downCount.value>services.value.length/2) return 'down'; return 'warn' })
const overallLabel = computed(() => { if(!downCount.value&&!warnCount.value) return 'All systems operational'; if(downCount.value>0) return `${downCount.value} service${downCount.value>1?'s':''} unreachable`; return 'Partial degradation' })

const sBg    = s => s==='up'?'#f0fdf4':s==='warn'?'#fffbeb':s==='down'?'#fef2f2':'#f8fafc'
const sColor = s => s==='up'?'#16a34a':s==='warn'?'#f59e0b':s==='down'?'#dc2626':'#94a3b8'
const sBadge = s => s==='up'?'badge-green':s==='warn'?'badge-yellow':s==='down'?'badge-red':'badge-gray'
const jBadge = s => { const u=(s||'').toUpperCase(); if(u==='COMPLETED') return 'badge-green'; if(u==='FAILED') return 'badge-red'; if(u==='RUNNING') return 'badge-yellow'; return 'badge-gray' }

function ago(ts){ if(!ts) return '—'; const m=Math.floor((Date.now()-new Date(ts))/60000); if(m<1) return 'just now'; if(m<60) return m+'m ago'; const h=Math.floor(m/60); if(h<24) return h+'h ago'; return Math.floor(h/24)+'d ago' }
function fmtDt(d){ if(!d) return '—'; return new Date(d).toLocaleString('et-EE',{month:'short',day:'numeric',hour:'2-digit',minute:'2-digit'}) }

async function checkHealth(svc){
  const t=Date.now()
  try{
    const r=await fetch(PATHS[svc.id],{signal:AbortSignal.timeout(5000)})
    svc.ms=Date.now()-t
    if(r.ok){
      const d=await r.json().catch(()=>({}))
      const st=(d.status||'').toUpperCase()
      if(st==='UP'||st===''){svc.status='up';svc.label='Operational'}
      else if(st==='DOWN'){svc.status='down';svc.label='Down'}
      else{svc.status='warn';svc.label=st||'Degraded'}
    } else { svc.status='down'; svc.label='HTTP '+r.status }
  }catch{ svc.status='down'; svc.label='Unreachable'; svc.ms=null }
}

async function checkAll(){
  checking.value=true
  await Promise.allSettled(services.value.map(s=>checkHealth(s)))
  lastCheck.value=new Date().toLocaleTimeString('et-EE')
  checking.value=false
}

async function loadScraperSt(){ try{ const r=await fetch('/api/scraper/status'); if(r.ok) scraperSt.value=await r.json() }catch{} }
async function loadJobs(){ try{ const r=await fetch('/api/scraper/jobs?size=10'); if(r.ok){ const d=await r.json(); jobs.value=Array.isArray(d)?d:(d.content||[]) } }catch{} }

async function triggerScrape(){
  scraping.value=true
  try{ await fetch('/api/scraper/trigger',{method:'POST'}); await new Promise(r=>setTimeout(r,3000)); await Promise.all([loadScraperSt(),loadJobs()]) }
  catch{} finally{ scraping.value=false }
}

onMounted(()=>{ Promise.all([checkAll(), loadScraperSt(), loadJobs()]) })
</script>

<style scoped>
.page       { display:flex; flex-direction:column; gap:18px; }
.page-hd    { display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:12px; }
.page-title { font-size:1.375rem; font-weight:700; color:var(--text); margin:0 0 4px; }
.page-sub   { font-size:.875rem; color:var(--muted); margin:0; }

.overall-dot { width:13px; height:13px; border-radius:50%; flex-shrink:0; }
.dot-up   { background:#22c55e; box-shadow:0 0 0 3px rgba(34,197,94,.2); }
.dot-warn { background:#f59e0b; box-shadow:0 0 0 3px rgba(245,158,11,.2); }
.dot-down { background:#ef4444; box-shadow:0 0 0 3px rgba(239,68,68,.2); }

.hbar-track { height:6px; background:var(--border); border-radius:3px; overflow:hidden; }
.hbar-fill  { height:100%; background:var(--primary); border-radius:3px; transition:width .5s; }

.svc-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(220px,1fr)); gap:12px; }

.scard { background:var(--card); border:1px solid var(--border); border-radius:var(--r); padding:15px; box-shadow:var(--shadow); transition:border-color .2s; }
.scard--up   { border-left:3px solid #22c55e; }
.scard--warn { border-left:3px solid #f59e0b; }
.scard--down { border-left:3px solid #ef4444; }
.scard--unknown { border-left:3px solid var(--border); }

.scard-hd   { display:flex; align-items:center; gap:9px; margin-bottom:10px; }
.scard-icon { width:34px; height:34px; border-radius:8px; display:flex; align-items:center; justify-content:center; flex-shrink:0; }
.scard-name { font-size:.85rem; font-weight:600; color:var(--text); }
.scard-port { font-size:.75rem; color:var(--muted); font-family:monospace; }

.sdot       { width:8px; height:8px; border-radius:50%; flex-shrink:0; }
.sdot--up   { background:#22c55e; }
.sdot--warn { background:#f59e0b; }
.sdot--down { background:#ef4444; animation:pulseR 1.5s infinite; }
.sdot--unknown { background:#94a3b8; }
@keyframes pulseR{ 0%,100%{box-shadow:0 0 0 0 rgba(239,68,68,.4)}50%{box-shadow:0 0 0 5px rgba(239,68,68,0)} }

.scard-meta { display:flex; flex-direction:column; gap:5px; }
.smeta      { display:flex; justify-content:space-between; align-items:center; font-size:.8rem; }
.sml        { color:var(--muted); }
.smv        { font-weight:500; color:var(--text); }

.two-col { display:grid; grid-template-columns:1fr 1.5fr; gap:16px; }
@media(max-width:900px){ .two-col { grid-template-columns:1fr; } }

.scraper-stats { display:grid; grid-template-columns:1fr 1fr; gap:12px; }
.sst     { display:flex; flex-direction:column; gap:4px; }
.sst-lbl { font-size:.75rem; color:var(--muted); }
.sst-val { font-size:.9rem; font-weight:600; color:var(--text); }
</style>
