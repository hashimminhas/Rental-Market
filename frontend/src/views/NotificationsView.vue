<template>
  <div class="page">
    <div class="page-hd">
      <div>
        <h1 class="page-title">Notifications</h1>
        <p class="page-sub">{{ total }} notification{{ total!==1?'s':'' }} · {{ unread }} unread</p>
      </div>
      <div style="display:flex;align-items:center;gap:10px">
        <button v-if="unread>0" class="btn btn-outline btn-sm" @click="markAllRead">Mark all read</button>
        <button class="btn btn-ghost btn-sm" @click="load">
          <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>
          Refresh
        </button>
      </div>
    </div>

    <!-- Filter tabs -->
    <div class="tabs">
      <button v-for="t in tabs" :key="t.key" :class="['tab', active===t.key&&'tab--on']" @click="active=t.key;pg=1">
        {{ t.label }}
        <span v-if="t.count>0" class="tab-badge">{{ t.count }}</span>
      </button>
    </div>

    <div v-if="loading" class="card" style="padding:60px">
      <div class="state-loading" style="padding:0"><div class="spinner"></div><span>Loading…</span></div>
    </div>
    <div v-else-if="paged.length===0" class="card" style="padding:60px">
      <div class="state-empty" style="padding:0">
        <svg width="36" height="36" fill="none" stroke="#94a3b8" stroke-width="1.5" viewBox="0 0 24 24"><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/></svg>
        <p>No notifications here.</p>
      </div>
    </div>
    <div v-else class="card" style="padding:0;overflow:hidden">
      <div v-for="n in paged" :key="n.notificationId||n.id" :class="['nrow', !n.isRead&&'nrow--unread']" @click="markRead(n)">
        <div class="nrow-icon" :style="{background:iconBg(n)}">
          <svg width="15" height="15" fill="none" :stroke="iconColor(n)" stroke-width="2" viewBox="0 0 24 24" v-html="iconPath(n)"></svg>
        </div>
        <div class="nrow-body">
          <div class="nrow-title">{{ n.subject||n.title||'Notification' }}</div>
          <div class="nrow-msg">{{ n.message||n.body||'' }}</div>
          <div class="nrow-meta">
            <span class="nrow-time">{{ ago(n.sentAt||n.createdAt) }}</span>
            <span v-if="n.type"  :class="['badge', typeBadge(n.type)]">{{ n.type }}</span>
            <span v-if="n.channel" :class="['badge', n.channel==='EMAIL'?'badge-blue':'badge-gray']">{{ n.channel }}</span>
          </div>
        </div>
        <div class="nrow-status">
          <span v-if="!n.isRead" class="unread-dot"></span>
          <span :class="['badge', sBadge(n.status)]">{{ n.status||'sent' }}</span>
        </div>
      </div>
    </div>

    <div v-if="totalPages>1" style="display:flex;align-items:center;justify-content:center;gap:14px">
      <button class="btn btn-outline btn-sm" :disabled="pg===1" @click="pg--">← Prev</button>
      <span style="font-size:.85rem;color:var(--muted)">Page {{ pg }} of {{ totalPages }}</span>
      <button class="btn btn-outline btn-sm" :disabled="pg===totalPages" @click="pg++">Next →</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'

const notifs  = ref([])
const loading = ref(true)
const active  = ref('all')
const pg      = ref(1)
const SIZE    = 15

const tabs = computed(() => [
  { key:'all',     label:'All',     count: notifs.value.length },
  { key:'email',   label:'Email',   count: notifs.value.filter(n=>n.channel==='EMAIL').length },
  { key:'inapp',   label:'In-app',  count: notifs.value.filter(n=>n.channel==='IN_APP').length },
  { key:'sent',    label:'Sent',    count: notifs.value.filter(n=>n.status==='SENT'||n.status==='DELIVERED').length },
  { key:'pending', label:'Pending', count: notifs.value.filter(n=>n.status==='PENDING').length },
  { key:'failed',  label:'Failed',  count: notifs.value.filter(n=>n.status==='FAILED').length },
])

const filtered = computed(() => {
  switch(active.value){
    case 'email':   return notifs.value.filter(n=>n.channel==='EMAIL')
    case 'inapp':   return notifs.value.filter(n=>n.channel==='IN_APP')
    case 'sent':    return notifs.value.filter(n=>n.status==='SENT'||n.status==='DELIVERED')
    case 'pending': return notifs.value.filter(n=>n.status==='PENDING')
    case 'failed':  return notifs.value.filter(n=>n.status==='FAILED')
    default:        return notifs.value
  }
})

const total      = computed(() => filtered.value.length)
const unread     = computed(() => filtered.value.filter(n=>!n.isRead).length)
const totalPages = computed(() => Math.max(1,Math.ceil(filtered.value.length/SIZE)))
const paged      = computed(() => { const s=(pg.value-1)*SIZE; return filtered.value.slice(s,s+SIZE) })

watch(active, ()=>{ pg.value=1 })

function ago(ts){
  if(!ts) return '—'
  const d=Math.floor((Date.now()-new Date(ts))/60000)
  if(d<1) return 'just now'; if(d<60) return d+'m ago'
  const h=Math.floor(d/60); if(h<24) return h+'h ago'; return Math.floor(h/24)+'d ago'
}

function iconBg(n){ const t=(n.type||'').toUpperCase(); if(t.includes('ALERT')||t.includes('MATCH')) return '#e0f2f1'; if(t.includes('ERROR')||t.includes('FAIL')) return '#fef2f2'; return '#eff6ff' }
function iconColor(n){ const t=(n.type||'').toUpperCase(); if(t.includes('ALERT')||t.includes('MATCH')) return '#0d9488'; if(t.includes('ERROR')||t.includes('FAIL')) return '#ef4444'; return '#3b82f6' }
function iconPath(n){ const t=(n.type||'').toUpperCase(); if(t.includes('ALERT')) return '<path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/>'; if(t.includes('ERROR')) return '<circle cx="12" cy="12" r="10"/><path d="M12 8v4m0 4h.01"/>'; return '<path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/>' }
function typeBadge(t){ const u=(t||'').toUpperCase(); if(u.includes('ALERT')) return 'badge-teal'; if(u.includes('ERROR')) return 'badge-red'; return 'badge-blue' }
function sBadge(s){ const u=(s||'').toUpperCase(); if(u==='SENT'||u==='DELIVERED') return 'badge-green'; if(u==='FAILED') return 'badge-red'; if(u==='PENDING') return 'badge-yellow'; return 'badge-gray' }

async function markRead(n){
  if(n.isRead) return
  try{ await fetch(`/api/notifications/${n.notificationId||n.id}/read`,{method:'PATCH'}); n.isRead=true }catch{}
}
async function markAllRead(){
  try{ await fetch('/api/notifications/read-all',{method:'PATCH'}); notifs.value.forEach(n=>{n.isRead=true}) }catch{}
}

async function load(){
  loading.value=true
  try{
    const r=await fetch('/api/notifications?size=200')
    if(r.ok){ const d=await r.json(); notifs.value=(Array.isArray(d)?d:(d.content||d.notifications||[])).sort((a,b)=>new Date(b.sentAt||b.createdAt||0)-new Date(a.sentAt||a.createdAt||0)) }
  }catch{} finally{ loading.value=false }
}

onMounted(load)
</script>

<style scoped>
.page       { display:flex; flex-direction:column; gap:18px; }
.page-hd    { display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:12px; }
.page-title { font-size:1.375rem; font-weight:700; color:var(--text); margin:0 0 4px; }
.page-sub   { font-size:.875rem; color:var(--muted); margin:0; }

.tabs       { display:flex; gap:2px; border-bottom:1px solid var(--border); }
.tab        { padding:8px 14px; border:none; background:none; cursor:pointer; font-size:.85rem; color:var(--muted); border-bottom:2px solid transparent; margin-bottom:-1px; display:flex; align-items:center; gap:6px; transition:all .15s; }
.tab:hover  { color:var(--text); }
.tab--on    { color:var(--primary); border-bottom-color:var(--primary); font-weight:600; }
.tab-badge  { background:var(--border); color:var(--muted); border-radius:10px; padding:1px 6px; font-size:.72rem; font-weight:600; }
.tab--on .tab-badge { background:var(--primary-light); color:var(--primary); }

.nrow { display:flex; align-items:flex-start; gap:14px; padding:14px 18px; border-bottom:1px solid var(--border); cursor:pointer; transition:background .15s; }
.nrow:last-child { border-bottom:none; }
.nrow:hover { background:#f8fafc; }
.nrow--unread { background:#f0fdfa; }
.nrow--unread:hover { background:#e6faf7; }

.nrow-icon   { width:36px; height:36px; border-radius:8px; flex-shrink:0; display:flex; align-items:center; justify-content:center; margin-top:2px; }
.nrow-body   { flex:1; min-width:0; }
.nrow-title  { font-size:.875rem; font-weight:600; color:var(--text); margin-bottom:2px; }
.nrow-msg    { font-size:.82rem; color:var(--muted); margin-bottom:5px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.nrow-meta   { display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.nrow-time   { font-size:.75rem; color:var(--light); }
.nrow-status { display:flex; flex-direction:column; align-items:flex-end; gap:5px; flex-shrink:0; }
.unread-dot  { width:8px; height:8px; border-radius:50%; background:var(--primary); }
</style>
