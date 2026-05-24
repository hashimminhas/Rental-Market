<template>
  <div class="page">
    <div class="page-hd">
      <div>
        <h1 class="page-title">Map View</h1>
        <p class="page-sub">{{ mapped.length }} listings with coordinates</p>
      </div>
      <div style="display:flex;gap:10px;align-items:center">
        <select v-model="fNeigh" class="form-select fsel">
          <option value="">All neighborhoods</option>
          <option v-for="n in neighborhoods" :key="n" :value="n">{{ n }}</option>
        </select>
        <div class="frange"><span class="frlbl">Max €</span><input v-model.number="fMaxP" type="number" class="finput frinp" placeholder="1200" /></div>
      </div>
    </div>

    <div v-if="loading" class="card" style="padding:60px;text-align:center">
      <div class="state-loading" style="padding:0"><div class="spinner"></div><span>Loading listings…</span></div>
    </div>
    <div v-else-if="error" class="state-error">{{ error }}</div>
    <div v-else class="map-layout">
      <div id="map-container" ref="mapEl" class="map-box"></div>
      <div class="map-sidebar">
        <div v-if="!selected" class="map-hint">
          <svg width="28" height="28" fill="none" stroke="#94a3b8" stroke-width="1.5" viewBox="0 0 24 24"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
          <p style="color:var(--muted);font-size:.85rem;text-align:center;margin-top:8px">Click a marker to see listing details</p>
        </div>
        <div v-else class="map-detail card">
          <div v-if="selected.imageUrl" class="map-detail-img">
            <img :src="selected.imageUrl" :alt="selected.title" loading="lazy" @error="e=>e.target.style.display='none'">
          </div>
          <div style="padding:14px">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:8px;margin-bottom:8px">
              <span :class="['badge', srcBadge(selected.source)]">{{ srcLabel(selected.source) }}</span>
              <button class="btn-ghost-sm" @click="selected=null">✕</button>
            </div>
            <div style="font-size:.875rem;font-weight:600;color:var(--text);line-height:1.4;margin-bottom:6px">{{ selected.title || 'Apartment' }}</div>
            <div style="display:flex;align-items:center;gap:4px;font-size:.78rem;color:var(--muted);margin-bottom:10px">
              <svg width="11" height="11" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
              {{ selected.neighborhood || 'Tartu' }}<span v-if="selected.street"> · {{ selected.street }}</span>
            </div>
            <div style="font-size:1.2rem;font-weight:700;color:var(--primary);margin-bottom:8px">€{{ fmt(selected.price) }}<span style="font-size:.8rem;font-weight:400;color:var(--muted);margin-left:3px">/month</span></div>
            <div style="display:flex;flex-wrap:wrap;gap:5px;margin-bottom:12px">
              <span class="chip">{{ selected.size ? selected.size+' m²' : '—' }}</span>
              <span class="chip">{{ selected.rooms ? selected.rooms+(selected.rooms===1?' room':' rooms') : '—' }}</span>
              <span class="chip" v-if="selected.pricePerSqm">€{{ fmtD(selected.pricePerSqm) }}/m²</span>
            </div>
            <a :href="selected.url||'#'" target="_blank" class="btn btn-primary btn-sm" style="width:100%;justify-content:center">View listing →</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: new URL('leaflet/dist/images/marker-icon-2x.png', import.meta.url).href,
  iconUrl:       new URL('leaflet/dist/images/marker-icon.png',    import.meta.url).href,
  shadowUrl:     new URL('leaflet/dist/images/marker-shadow.png',  import.meta.url).href,
})

const listings = ref([])
const loading  = ref(true)
const error    = ref(null)
const selected = ref(null)
const mapEl    = ref(null)
const fNeigh   = ref('')
const fMaxP    = ref(null)

let map = null
const markers = []

const neighborhoods = computed(() => [...new Set(listings.value.map(l=>l.neighborhood).filter(Boolean))].sort())

const mapped = computed(() => {
  let a = listings.value.filter(l => l.latitude && l.longitude && l.isActive !== false)
  if (fNeigh.value) a = a.filter(l => l.neighborhood === fNeigh.value)
  if (fMaxP.value)  a = a.filter(l => l.price != null && l.price <= fMaxP.value)
  return a
})

function srcLabel(s){ const m={KV_EE:'KV.EE',CITY24:'City24',RENDIN:'Rendin',KINNISVARA24:'K24',MAAMET:'Maa-amet'}; return m[s]||s||'?' }
function srcBadge(s){ const m={KV_EE:'badge-kv',CITY24:'badge-city',RENDIN:'badge-rendin',KINNISVARA24:'badge-k24',MAAMET:'badge-maamet'}; return m[s]||'badge-gray' }
function fmt(v){ return v!=null?Number(v).toLocaleString('et-EE',{maximumFractionDigits:0}):'—' }
function fmtD(v){ return v!=null?Number(v).toFixed(1):'—' }

function initMap() {
  if (!mapEl.value || map) return
  map = L.map(mapEl.value).setView([58.3776, 26.7290], 13)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© <a href="https://openstreetmap.org/copyright">OpenStreetMap</a>',
    maxZoom: 19
  }).addTo(map)
}

function rebuildMarkers() {
  markers.forEach(m => m.remove())
  markers.length = 0
  if (!map) return

  mapped.value.forEach(l => {
    const marker = L.marker([l.latitude, l.longitude])
      .bindPopup(`<strong>€${fmt(l.price)}/mo</strong><br>${l.title || 'Apartment'}`, { maxWidth: 200 })
      .on('click', () => { selected.value = l })
      .addTo(map)
    markers.push(marker)
  })

  if (markers.length > 0) {
    const group = L.featureGroup(markers)
    map.fitBounds(group.getBounds().pad(0.15))
  }
}

watch(mapped, () => rebuildMarkers())

async function load() {
  loading.value = true; error.value = null
  try {
    const r = await fetch('/api/listings?size=500')
    if (!r.ok) throw new Error('HTTP ' + r.status)
    const d = await r.json()
    listings.value = Array.isArray(d) ? d : (d.content || d.listings || [])
  } catch (e) {
    error.value = 'Could not load listings — is the scraper service running?'
  } finally {
    loading.value = false
    await nextTick()
    initMap()
    rebuildMarkers()
  }
}

onMounted(load)
onUnmounted(() => { if (map) { map.remove(); map = null } })
</script>

<style scoped>
.page       { display:flex; flex-direction:column; gap:18px; }
.page-hd    { display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:10px; }
.page-title { font-size:1.375rem; font-weight:700; color:var(--text); margin:0 0 4px; }
.page-sub   { font-size:.875rem; color:var(--muted); margin:0; }

.fsel  { min-width:140px; font-size:.85rem; }
.frange{ display:flex; align-items:center; gap:6px; }
.frlbl { font-size:.8rem; color:var(--muted); white-space:nowrap; }
.finput{ width:100%; padding:7px 10px; border:1px solid var(--border); border-radius:var(--r); font-size:.85rem; color:var(--text); background:var(--bg); }
.finput:focus{ outline:none; border-color:var(--primary); box-shadow:0 0 0 3px rgba(13,148,136,.12); }
.frinp { max-width:85px; min-width:0; }

.map-layout  { display:grid; grid-template-columns:1fr 320px; gap:16px; height:calc(100vh - 220px); min-height:480px; }
.map-box     { border-radius:var(--r); border:1px solid var(--border); overflow:hidden; z-index:0; }
.map-sidebar { overflow-y:auto; display:flex; flex-direction:column; gap:12px; }
.map-hint    { display:flex; flex-direction:column; align-items:center; justify-content:center; background:var(--card); border:1px solid var(--border); border-radius:var(--r); padding:32px 16px; flex:1; }

.map-detail        { padding:0; overflow:hidden; }
.map-detail-img    { height:160px; overflow:hidden; }
.map-detail-img img{ width:100%; height:100%; object-fit:cover; display:block; }

.btn-ghost-sm { background:none; border:none; cursor:pointer; color:var(--muted); font-size:.85rem; padding:2px 4px; border-radius:4px; }
.btn-ghost-sm:hover { background:#f1f5f9; color:var(--text); }

.chip { font-size:.75rem; background:var(--primary-light); color:var(--primary-dark); padding:2px 8px; border-radius:20px; font-weight:500; }

@media(max-width:768px) {
  .map-layout { grid-template-columns:1fr; height:auto; }
  .map-box    { height:55vmax; min-height:320px; }
}
</style>
