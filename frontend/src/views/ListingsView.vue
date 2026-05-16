<template>
  <div>
    <h1 class="page-title">Listings</h1>
    <p class="page-subtitle">Browse scraped rental listings from kv.ee</p>

    <div class="card" style="margin-bottom:20px;">
      <div style="display:flex;gap:12px;flex-wrap:wrap;align-items:flex-end;">
        <div class="form-group" style="margin:0;min-width:180px;">
          <label>Neighborhood</label>
          <select class="form-control" v-model="filters.neighborhood">
            <option value="">All neighborhoods</option>
            <option v-for="n in neighborhoods" :key="n" :value="n">{{ n }}</option>
          </select>
        </div>
        <div class="form-group" style="margin:0;min-width:130px;">
          <label>Max Price (€)</label>
          <input class="form-control" type="number" v-model.number="filters.maxPrice" placeholder="e.g. 800" min="0" />
        </div>
        <div class="form-group" style="margin:0;min-width:130px;">
          <label>Min Size (m²)</label>
          <input class="form-control" type="number" v-model.number="filters.minSize" placeholder="e.g. 30" min="0" />
        </div>
        <div class="form-group" style="margin:0;min-width:100px;">
          <label>Min Rooms</label>
          <input class="form-control" type="number" v-model.number="filters.minRooms" placeholder="e.g. 2" min="1" />
        </div>
        <button class="btn btn-primary" @click="loadListings">Search</button>
        <button class="btn btn-outline" @click="resetFilters">Reset</button>
        <button class="btn btn-secondary" style="margin-left:auto;" :disabled="triggering" @click="triggerScrape">
          {{ triggering ? 'Triggering…' : '↻ Scrape Now' }}
        </button>
      </div>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <div v-if="loading" class="loading">Loading listings…</div>

    <div v-else-if="listings.length === 0" class="empty">No listings match your filters.</div>

    <div v-else>
      <p style="color:#6c757d;font-size:0.85rem;margin-bottom:12px;">{{ listings.length }} listing(s) found</p>
      <div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(320px,1fr));gap:16px;">
        <div v-for="l in listings" :key="l.listingId || l.id" class="card listing-card">
          <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:8px;">
            <strong style="font-size:1rem;color:#1a3a5c;line-height:1.3;">{{ l.title || 'Untitled' }}</strong>
            <span class="badge badge-blue" style="white-space:nowrap;margin-left:8px;">€{{ l.price }}</span>
          </div>
          <div style="color:#6c757d;font-size:0.85rem;margin-bottom:8px;">
            {{ l.neighborhood }} · {{ l.size }} m² · {{ l.rooms }} room{{ l.rooms === 1 ? '' : 's' }}
          </div>
          <div v-if="l.address" style="font-size:0.82rem;color:#888;margin-bottom:8px;">📍 {{ l.address }}</div>
          <div style="display:flex;justify-content:space-between;align-items:center;">
            <span style="font-size:0.78rem;color:#adb5bd;">{{ formatDate(l.scrapedAt || l.createdAt) }}</span>
            <a v-if="l.originalUrl || l.url" :href="l.originalUrl || l.url" target="_blank"
               class="btn btn-outline btn-sm">View on kv.ee</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const listings = ref([])
const neighborhoods = ref([])
const loading = ref(false)
const error = ref('')
const triggering = ref(false)

const filters = ref({ neighborhood: '', maxPrice: null, minSize: null, minRooms: null })

function formatDate(iso) {
  if (!iso) return ''
  return new Date(iso).toLocaleDateString('et-EE')
}

function resetFilters() {
  filters.value = { neighborhood: '', maxPrice: null, minSize: null, minRooms: null }
  loadListings()
}

async function loadListings() {
  loading.value = true
  error.value = ''
  try {
    const params = new URLSearchParams()
    if (filters.value.neighborhood) params.append('neighborhood', filters.value.neighborhood)
    if (filters.value.maxPrice)     params.append('maxPrice', filters.value.maxPrice)
    if (filters.value.minSize)      params.append('minSize', filters.value.minSize)
    if (filters.value.minRooms)     params.append('minRooms', filters.value.minRooms)

    const url = '/api/listings' + (params.toString() ? '?' + params.toString() : '')
    const res = await fetch(url)
    if (!res.ok) throw new Error('HTTP ' + res.status)
    const data = await res.json()
    listings.value = Array.isArray(data) ? data : (data.content ?? data.listings ?? [])
  } catch (e) {
    error.value = 'Could not load listings: ' + e.message
    listings.value = []
  } finally {
    loading.value = false
  }
}

async function loadNeighborhoods() {
  try {
    const res = await fetch('/api/neighborhoods')
    if (res.ok) {
      const data = await res.json()
      const arr = Array.isArray(data) ? data : []
      neighborhoods.value = arr.map(n => n.name || n.neighborhood).filter(Boolean)
    }
  } catch (_) {}
}

async function triggerScrape() {
  triggering.value = true
  try {
    await fetch('/api/scraper/trigger', { method: 'POST' })
    setTimeout(loadListings, 2000)
  } catch (_) {} finally {
    triggering.value = false
  }
}

onMounted(() => {
  loadListings()
  loadNeighborhoods()
})
</script>

<style scoped>
.listing-card { transition: transform 0.15s, box-shadow 0.15s; }
.listing-card:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(0,0,0,0.12); }
</style>
