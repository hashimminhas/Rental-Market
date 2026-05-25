<template>
  <div class="landing">

    <!-- ── Hero ── -->
    <section class="hero">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <div class="hero-badge">🏠 Tartu's #1 Rental Aggregator</div>
        <h1 class="hero-title">Find Your Perfect<br>Home in Tartu</h1>
        <p class="hero-sub">Real-time listings from KV.ee, City24 and more — all in one place.</p>
        <div class="hero-ctas">
          <router-link to="/listings" class="cta-primary">Browse Listings →</router-link>
          <router-link to="/alerts" class="cta-outline">🔔 Set Price Alert</router-link>
        </div>
      </div>
    </section>

    <!-- ── Live Stats ── -->
    <section class="stats-section">
      <div class="stats-card">
        <div class="stats-live">
          <span class="live-dot"></span> Live
        </div>
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-icon" style="background:#e0f2f1">
              <svg width="18" height="18" fill="none" stroke="#0d9488" stroke-width="2" viewBox="0 0 24 24"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/></svg>
            </div>
            <div class="stat-val">{{ stats.avg ? '€'+fmt(stats.avg) : '—' }}</div>
            <div class="stat-lbl">Average Rent /mo</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-icon" style="background:#f0fdf4">
              <svg width="18" height="18" fill="none" stroke="#16a34a" stroke-width="2" viewBox="0 0 24 24"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/></svg>
            </div>
            <div class="stat-val" style="color:#16a34a">{{ stats.min ? '€'+fmt(stats.min) : '—' }}</div>
            <div class="stat-lbl">Cheapest Available</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-icon" style="background:#fef2f2">
              <svg width="18" height="18" fill="none" stroke="#ef4444" stroke-width="2" viewBox="0 0 24 24"><polyline points="23 18 13.5 8.5 8.5 13.5 1 6"/></svg>
            </div>
            <div class="stat-val" style="color:#ef4444">{{ stats.max ? '€'+fmt(stats.max) : '—' }}</div>
            <div class="stat-lbl">Most Expensive</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-icon" style="background:#eff6ff">
              <svg width="18" height="18" fill="none" stroke="#3b82f6" stroke-width="2" viewBox="0 0 24 24"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/></svg>
            </div>
            <div class="stat-val">{{ stats.total ?? '—' }}</div>
            <div class="stat-lbl">Active Listings</div>
          </div>
        </div>
      </div>
    </section>

    <!-- ── How it works ── -->
    <section class="hiw-section">
      <h2 class="section-title">How it works</h2>
      <p class="section-sub">Three steps from looking to moving in.</p>
      <div class="hiw-grid">
        <div class="hiw-card">
          <div class="hiw-icon"><svg width="24" height="24" fill="none" stroke="#0d9488" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg></div>
          <div class="hiw-step">Step 1</div>
          <div class="hiw-title">Aggregated Daily</div>
          <div class="hiw-desc">We scrape KV.ee, City24, te.kv.ee every 6 hours automatically so you never miss a listing.</div>
        </div>
        <div class="hiw-card">
          <div class="hiw-icon"><svg width="24" height="24" fill="none" stroke="#0d9488" stroke-width="2" viewBox="0 0 24 24"><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/></svg></div>
          <div class="hiw-step">Step 2</div>
          <div class="hiw-title">Set Your Alert</div>
          <div class="hiw-desc">Enter your email and criteria. We email you instantly when a matching listing appears.</div>
        </div>
        <div class="hiw-card">
          <div class="hiw-icon"><svg width="24" height="24" fill="none" stroke="#0d9488" stroke-width="2" viewBox="0 0 24 24"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg></div>
          <div class="hiw-step">Step 3</div>
          <div class="hiw-title">Find Faster</div>
          <div class="hiw-desc">No more checking 5 different sites. Everything in one clean, up-to-date interface.</div>
        </div>
      </div>
    </section>

    <!-- ── Map ── -->
    <section class="map-section">
      <div class="map-header">
        <div>
          <h2 class="section-title" style="text-align:left;margin-bottom:4px">Explore Tartu's Rental Market</h2>
          <p class="section-sub" style="text-align:left">{{ stats.total ?? '...' }} listings across 10+ neighborhoods</p>
        </div>
        <select v-model="mapHood" class="form-select" style="width:180px">
          <option value="">All neighborhoods</option>
          <option v-for="n in HOODS" :key="n" :value="n">{{ n }}</option>
        </select>
      </div>
      <div class="map-wrap" ref="mapEl"></div>
    </section>

    <!-- ── Footer ── -->
    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <div class="footer-logo">
            <span style="width:26px;height:26px;background:#0d9488;border-radius:6px;display:flex;align-items:center;justify-content:center;flex-shrink:0">
              <svg width="14" height="14" viewBox="0 0 32 32" fill="none"><path d="M6 14l10-8 10 8v11a1.5 1.5 0 01-1.5 1.5h-5V19h-7v7.5H7A1.5 1.5 0 016 25V14z" fill="white"/></svg>
            </span>
            Üüriturg
          </div>
          <p class="footer-tagline">Tartu's real-time rental aggregator.<br>All listings in one place.</p>
        </div>
        <div class="footer-links">
          <div class="footer-col">
            <div class="footer-col-title">Product</div>
            <router-link to="/listings" class="footer-link">Listings</router-link>
            <router-link to="/alerts"   class="footer-link">Alerts</router-link>
            <router-link to="/insights" class="footer-link">Market Insights</router-link>
          </div>
        </div>
        <div class="footer-copy">
          Data updated every 6 hours from public listing sources.<br>
          © {{ new Date().getFullYear() }} Üüriturg
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

const HOODS = ['Kesklinn','Ülejõe','Tammelinn','Annelinn','Karlova','Veeriku','Tähtvere','Supilinn','Ränilinn','Maarjamõisa']

const stats  = ref({ avg: null, min: null, max: null, total: null })
const mapEl  = ref(null)
const mapHood = ref('')
let leafletMap = null
let clusterGroup = null

const fmt = v => Number(v).toLocaleString('et-EE', { maximumFractionDigits: 0 })

async function loadStats() {
  try {
    const [sumR, statusR] = await Promise.all([
      fetch('/api/analytics/summary'),
      fetch('/api/scraper/status')
    ])
    if (sumR.ok) {
      const d = await sumR.json()
      stats.value.avg = d.averagePrice
      stats.value.min = d.cheapestPrice
      stats.value.max = d.mostExpensivePrice
    }
    if (statusR.ok) {
      const d = await statusR.json()
      stats.value.total = d.totalActiveListings
    }
  } catch {}
}

async function initMap() {
  if (!mapEl.value) return
  const L = (await import('leaflet')).default
  await import('leaflet/dist/leaflet.css')
  await import('leaflet.markercluster')
  await import('leaflet.markercluster/dist/MarkerCluster.css')
  await import('leaflet.markercluster/dist/MarkerCluster.Default.css')

  leafletMap = L.map(mapEl.value).setView([58.3776, 26.7290], 13)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors', maxZoom: 19
  }).addTo(leafletMap)

  await loadMarkers(L)
}

async function loadMarkers(L) {
  if (!leafletMap) return
  if (clusterGroup) { leafletMap.removeLayer(clusterGroup) }

  clusterGroup = L.markerClusterGroup({
    maxClusterRadius: 50,
    iconCreateFunction(cluster) {
      const count = cluster.getChildCount()
      return L.divIcon({
        className: '',
        html: `<div style="background:#0d9488;color:#fff;width:36px;height:36px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:700;box-shadow:0 2px 8px rgba(13,148,136,.5);border:2px solid #fff">${count}</div>`,
        iconSize: [36, 36], iconAnchor: [18, 18]
      })
    }
  })

  try {
    let url = '/api/listings?size=500'
    if (mapHood.value) url += `&neighborhood=${encodeURIComponent(mapHood.value)}`
    const r = await fetch(url)
    if (!r.ok) return
    const data = await r.json()
    const list = Array.isArray(data) ? data : (data.content || [])
    list.filter(l => l.latitude && l.longitude).forEach(l => {
      const icon = L.divIcon({
        className: '',
        html: `<svg width="28" height="40" viewBox="0 0 28 40" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M14 0C6.268 0 0 6.268 0 14c0 10.5 14 26 14 26S28 24.5 28 14C28 6.268 21.732 0 14 0z" fill="#0d9488"/><circle cx="14" cy="14" r="6" fill="white"/></svg>`,
        iconSize: [28, 40],
        iconAnchor: [14, 40],
        popupAnchor: [0, -40]
      })
      L.marker([l.latitude, l.longitude], { icon })
        .bindPopup(`
          <div style="min-width:160px">
            <div style="font-weight:700;font-size:13px;margin-bottom:4px">${l.title || 'Apartment'}</div>
            <div style="color:#64748b;font-size:12px;margin-bottom:6px">${l.neighborhood || 'Tartu'}</div>
            <div style="font-size:16px;font-weight:800;color:#0d9488">€${Math.round(l.price)}<span style="font-size:11px;font-weight:500;color:#64748b">/month</span></div>
            ${l.size ? `<div style="font-size:11px;color:#94a3b8;margin-top:2px">${l.size} m² · ${l.rooms || '?'} rooms</div>` : ''}
            ${l.url ? `<a href="${l.url}" target="_blank" style="display:inline-block;margin-top:8px;padding:4px 10px;background:#0d9488;color:#fff;border-radius:6px;font-size:11px;font-weight:600;text-decoration:none">View listing →</a>` : ''}
          </div>
        `, { maxWidth: 220 })
        .addTo(clusterGroup)
    })
  } catch {}

  leafletMap.addLayer(clusterGroup)
}

watch(mapHood, async () => {
  const L = (await import('leaflet')).default
  await loadMarkers(L)
})

onMounted(async () => {
  await loadStats()
  setInterval(loadStats, 60000)
  await initMap()
})
</script>

<style scoped>
.landing { display: flex; flex-direction: column; }

/* Hero */
.hero {
  position: relative; min-height: 88vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #0f2027 0%, #203a43 50%, #2c5364 100%);
  overflow: hidden;
}
.hero-bg {
  position: absolute; inset: 0;
  background-image: url('https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=1600&q=80');
  background-size: cover; background-position: center;
  opacity: 0.25;
}
.hero-content { position: relative; text-align: center; padding: 60px 24px; max-width: 700px; }
.hero-badge { display: inline-block; background: rgba(255,255,255,.12); backdrop-filter: blur(8px); border: 1px solid rgba(255,255,255,.2); color: #fff; padding: 6px 16px; border-radius: 20px; font-size: 13px; font-weight: 500; margin-bottom: 24px; }
.hero-title { font-size: clamp(2.4rem, 6vw, 4rem); font-weight: 800; color: #fff; line-height: 1.1; margin-bottom: 18px; letter-spacing: -.02em; }
.hero-sub { font-size: 1.05rem; color: rgba(255,255,255,.8); margin-bottom: 36px; line-height: 1.6; }
.hero-ctas { display: flex; gap: 12px; justify-content: center; flex-wrap: wrap; }
.cta-primary { padding: 13px 28px; background: #0d9488; color: #fff; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 15px; transition: all .2s; }
.cta-primary:hover { background: #0f766e; transform: translateY(-1px); box-shadow: 0 8px 20px rgba(13,148,136,.4); }
.cta-outline { padding: 13px 28px; border: 2px solid rgba(255,255,255,.4); color: #fff; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 15px; transition: all .2s; backdrop-filter: blur(4px); }
.cta-outline:hover { background: rgba(255,255,255,.1); border-color: rgba(255,255,255,.7); }

/* Stats */
.stats-section { padding: 0 24px; margin-top: -52px; position: relative; z-index: 10; max-width: 1000px; margin-left: auto; margin-right: auto; width: 100%; }
.stats-card { background: #fff; border-radius: 16px; padding: 28px 32px; box-shadow: 0 8px 32px rgba(0,0,0,.12); border: 1px solid #e2e8f0; }
.stats-live { display: flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 600; color: #16a34a; margin-bottom: 20px; }
.live-dot { width: 8px; height: 8px; background: #16a34a; border-radius: 50%; box-shadow: 0 0 0 3px rgba(22,163,74,.2); animation: pulse 2s infinite; }
@keyframes pulse { 0%,100%{box-shadow:0 0 0 3px rgba(22,163,74,.2)} 50%{box-shadow:0 0 0 6px rgba(22,163,74,.05)} }
.stats-grid { display: grid; grid-template-columns: 1fr auto 1fr auto 1fr auto 1fr; align-items: center; gap: 8px; }
.stat-divider { width: 1px; height: 48px; background: #e2e8f0; }
.stat-item { display: flex; flex-direction: column; align-items: center; text-align: center; padding: 0 12px; }
.stat-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; margin-bottom: 8px; }
.stat-val { font-size: 1.6rem; font-weight: 800; color: #0f172a; line-height: 1; }
.stat-lbl { font-size: 12px; color: #64748b; margin-top: 4px; font-weight: 500; }

/* How it works */
.hiw-section { padding: 80px 24px; max-width: 1000px; margin: 0 auto; width: 100%; }
.section-title { font-size: 2rem; font-weight: 800; color: #0f172a; text-align: center; margin-bottom: 8px; }
.section-sub { font-size: .95rem; color: #64748b; text-align: center; margin-bottom: 48px; }
.hiw-grid { display: grid; grid-template-columns: repeat(3,1fr); gap: 24px; }
.hiw-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 28px 24px; transition: all .2s; }
.hiw-card:hover { box-shadow: 0 8px 24px rgba(0,0,0,.08); transform: translateY(-2px); }
.hiw-icon { width: 48px; height: 48px; background: #e0f2f1; border-radius: 12px; display: flex; align-items: center; justify-content: center; margin-bottom: 16px; }
.hiw-step { font-size: 11px; font-weight: 700; color: #0d9488; text-transform: uppercase; letter-spacing: .08em; margin-bottom: 6px; }
.hiw-title { font-size: 1.05rem; font-weight: 700; color: #0f172a; margin-bottom: 8px; }
.hiw-desc { font-size: .875rem; color: #64748b; line-height: 1.6; }

/* Map */
.map-section { padding: 0 24px 80px; max-width: 1200px; margin: 0 auto; width: 100%; }
.map-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 16px; flex-wrap: wrap; gap: 12px; }
.map-wrap { height: 460px; border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 4px 12px rgba(0,0,0,.06); }

/* Footer */
.footer { background: #0f172a; color: #94a3b8; padding: 48px 24px 32px; }
.footer-inner { max-width: 1200px; margin: 0 auto; display: grid; grid-template-columns: 1fr auto 1fr; gap: 40px; align-items: start; }
.footer-logo { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 700; color: #fff; margin-bottom: 10px; }
.footer-tagline { font-size: 13px; line-height: 1.6; }
.footer-col-title { font-size: 11px; font-weight: 700; color: #fff; text-transform: uppercase; letter-spacing: .08em; margin-bottom: 10px; }
.footer-link { display: block; font-size: 13px; color: #94a3b8; text-decoration: none; margin-bottom: 6px; transition: color .15s; }
.footer-link:hover { color: #fff; }
.footer-copy { font-size: 12px; text-align: right; line-height: 1.7; }

/* Responsive */
@media(max-width: 768px) {
  .stats-grid { grid-template-columns: 1fr 1fr; }
  .stat-divider { display: none; }
  .hiw-grid { grid-template-columns: 1fr; }
  .footer-inner { grid-template-columns: 1fr; }
  .footer-copy { text-align: left; }
  .stats-section { margin-top: -32px; }
  .stats-card { padding: 20px; }
}
</style>
