<template>
  <div>
    <h1 class="page-title">Dashboard</h1>
    <p class="page-subtitle">Tartu rental market at a glance</p>

    <div v-if="error" class="error">{{ error }}</div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-label">Total Listings</div>
        <div class="stat-value">{{ cityStats.totalListings ?? scraperStatus.totalActiveListings ?? '—' }}</div>
        <div class="stat-sub">scraped from kv.ee</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Avg Rent</div>
        <div class="stat-value">€{{ cityStats.averagePrice != null ? Number(cityStats.averagePrice).toFixed(0) : '—' }}</div>
        <div class="stat-sub">per month</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Price / m²</div>
        <div class="stat-value">€{{ cityStats.averagePricePerSqm != null ? Number(cityStats.averagePricePerSqm).toFixed(0) : '—' }}</div>
        <div class="stat-sub">city average</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Neighborhoods</div>
        <div class="stat-value">{{ neighborhoodCount }}</div>
        <div class="stat-sub">tracked areas</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Scraper</div>
        <div class="stat-value" style="font-size:1.1rem;">
          <span :class="scraperRunning ? 'badge badge-green' : 'badge badge-gray'">
            {{ scraperRunning ? 'Running' : 'Idle' }}
          </span>
        </div>
        <div class="stat-sub">{{ scraperStatus.lastScrapeTime ? 'Last run: ' + formatDate(scraperStatus.lastScrapeTime) : 'Never run' }}</div>
      </div>
    </div>

    <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:24px;">
      <div class="card">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
          <strong style="font-size:1rem;">Neighborhood Prices</strong>
          <button class="btn btn-outline btn-sm" @click="loadAnalytics">Refresh</button>
        </div>
        <div v-if="loadingAnalytics" class="loading" style="padding:20px;">Loading…</div>
        <div v-else-if="neighborhoodStats.length === 0" class="empty" style="padding:20px;">No data yet</div>
        <div v-else class="table-wrap">
          <table>
            <thead>
              <tr><th>Neighborhood</th><th>Avg Price</th><th>Listings</th></tr>
            </thead>
            <tbody>
              <tr v-for="n in neighborhoodStats" :key="n.neighborhood">
                <td>{{ n.neighborhood }}</td>
                <td>€{{ Number(n.averagePrice).toFixed(0) }}</td>
                <td>{{ n.listingCount }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card">
        <strong style="font-size:1rem;">Scraper Control</strong>
        <p style="color:#6c757d;font-size:0.85rem;margin:8px 0 16px;">
          Trigger a fresh scrape of kv.ee to update the listing database.
        </p>
        <button class="btn btn-primary" :disabled="triggering" @click="triggerScrape">
          {{ triggering ? 'Triggering…' : 'Trigger Scrape' }}
        </button>
        <div v-if="triggerMsg" style="margin-top:10px;font-size:0.85rem;color:#155724;">{{ triggerMsg }}</div>

        <hr style="margin:20px 0;border:none;border-top:1px solid #f0f2f5;">
        <strong style="font-size:1rem;">Compute Analytics</strong>
        <p style="color:#6c757d;font-size:0.85rem;margin:8px 0 16px;">
          Re-compute neighborhood averages from the current listing set.
        </p>
        <button class="btn btn-outline" :disabled="computing" @click="triggerCompute">
          {{ computing ? 'Computing…' : 'Compute Now' }}
        </button>
        <div v-if="computeMsg" style="margin-top:10px;font-size:0.85rem;color:#155724;">{{ computeMsg }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const cityStats = ref({})
const neighborhoodStats = ref([])
const neighborhoodCount = ref(0)
const scraperStatus = ref({})
const scraperRunning = ref(false)
const loadingAnalytics = ref(false)
const error = ref('')
const triggering = ref(false)
const triggerMsg = ref('')
const computing = ref(false)
const computeMsg = ref('')

function formatDate(iso) {
  if (!iso) return ''
  return new Date(iso).toLocaleString('et-EE', { dateStyle: 'short', timeStyle: 'short' })
}

async function loadAnalytics() {
  loadingAnalytics.value = true
  try {
    const [cityRes, nbRes, nbCountRes] = await Promise.all([
      fetch('/api/analytics/summary'),
      fetch('/api/analytics/neighborhoods'),
      fetch('/api/neighborhoods'),
    ])
    if (cityRes.ok) cityStats.value = await cityRes.json()
    if (nbRes.ok) {
      const data = await nbRes.json()
      neighborhoodStats.value = Array.isArray(data) ? data : (data.neighborhoods ?? [])
    }
    if (nbCountRes.ok) {
      const data = await nbCountRes.json()
      neighborhoodCount.value = Array.isArray(data) ? data.length : 0
    }
  } catch (e) {
    error.value = 'Could not load analytics: ' + e.message
  } finally {
    loadingAnalytics.value = false
  }
}

async function loadScraperStatus() {
  try {
    const res = await fetch('/api/scraper/status')
    if (res.ok) {
      scraperStatus.value = await res.json()
      scraperRunning.value = scraperStatus.value.currentJobStatus === 'RUNNING'
    }
  } catch (_) {}
}

async function triggerScrape() {
  triggering.value = true
  triggerMsg.value = ''
  try {
    const res = await fetch('/api/scraper/trigger', { method: 'POST' })
    triggerMsg.value = res.ok ? 'Scrape job triggered successfully.' : 'Failed to trigger scrape.'
  } catch (e) {
    triggerMsg.value = 'Error: ' + e.message
  } finally {
    triggering.value = false
  }
}

async function triggerCompute() {
  computing.value = true
  computeMsg.value = ''
  try {
    const res = await fetch('/api/analytics/compute', { method: 'POST' })
    computeMsg.value = res.ok ? 'Analytics computation triggered.' : 'Failed to compute analytics.'
  } catch (e) {
    computeMsg.value = 'Error: ' + e.message
  } finally {
    computing.value = false
  }
}

onMounted(() => {
  loadAnalytics()
  loadScraperStatus()
})
</script>
