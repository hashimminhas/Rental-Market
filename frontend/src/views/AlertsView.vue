<template>
  <div>
    <h1 class="page-title">Alerts</h1>
    <p class="page-subtitle">Get notified when a new listing matches your criteria</p>

    <div v-if="globalError" class="error">{{ globalError }}</div>

    <div style="display:grid;grid-template-columns:360px 1fr;gap:20px;align-items:start;">

      <!-- Create alert form -->
      <div class="card">
        <strong style="font-size:1rem;display:block;margin-bottom:14px;">Create Alert</strong>
        <div class="form-group">
          <label>User ID (UUID)</label>
          <input class="form-control" v-model="form.userId" placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" />
        </div>
        <div class="form-group">
          <label>Neighborhood</label>
          <select class="form-control" v-model="form.neighborhood">
            <option value="">Any neighborhood</option>
            <option v-for="n in neighborhoods" :key="n" :value="n">{{ n }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>Max Price (€)</label>
          <input class="form-control" type="number" v-model.number="form.maxPrice" placeholder="e.g. 700" min="0" />
        </div>
        <div class="form-group">
          <label>Min Size (m²)</label>
          <input class="form-control" type="number" v-model.number="form.minSize" placeholder="e.g. 25" min="0" />
        </div>
        <div class="form-group">
          <label>Min Rooms</label>
          <input class="form-control" type="number" v-model.number="form.minRooms" placeholder="e.g. 1" min="1" />
        </div>
        <div v-if="formError" class="error" style="margin-bottom:10px;">{{ formError }}</div>
        <div v-if="formSuccess" style="color:#155724;font-size:0.85rem;margin-bottom:10px;">{{ formSuccess }}</div>
        <button class="btn btn-primary" :disabled="saving" @click="createAlert" style="width:100%;">
          {{ saving ? 'Creating…' : 'Create Alert' }}
        </button>
      </div>

      <!-- Alerts table -->
      <div class="card">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:14px;">
          <strong style="font-size:1rem;">All Alerts</strong>
          <button class="btn btn-outline btn-sm" @click="loadAlerts">Refresh</button>
        </div>
        <div v-if="loadingAlerts" class="loading" style="padding:20px;">Loading…</div>
        <div v-else-if="alerts.length === 0" class="empty" style="padding:20px;">No alerts registered yet.</div>
        <div v-else class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Neighborhood</th>
                <th>Max Price</th>
                <th>Min Size</th>
                <th>Min Rooms</th>
                <th>Status</th>
                <th>Matches</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="a in alerts" :key="a.alertRuleId || a.id">
                <td>{{ a.neighborhood || 'Any' }}</td>
                <td>{{ a.maxPrice ? '€' + a.maxPrice : '—' }}</td>
                <td>{{ a.minSize ? a.minSize + ' m²' : '—' }}</td>
                <td>{{ a.minRooms ?? '—' }}</td>
                <td>
                  <span :class="a.active !== false ? 'badge badge-green' : 'badge badge-gray'">
                    {{ a.active !== false ? 'Active' : 'Inactive' }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-outline btn-sm" @click="loadMatches(a)">
                    View matches
                  </button>
                </td>
                <td>
                  <button v-if="a.active !== false"
                    class="btn btn-danger btn-sm" @click="deactivate(a)">
                    Deactivate
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Matches panel -->
    <div v-if="selectedAlert" class="card" style="margin-top:20px;">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:14px;">
        <strong>Matches for alert — {{ selectedAlert.neighborhood || 'Any neighborhood' }}, max €{{ selectedAlert.maxPrice ?? '∞' }}</strong>
        <button class="btn btn-outline btn-sm" @click="selectedAlert = null">Close</button>
      </div>
      <div v-if="loadingMatches" class="loading" style="padding:20px;">Loading matches…</div>
      <div v-else-if="matches.length === 0" class="empty" style="padding:20px;">No matches for this alert yet.</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr><th>Listing ID</th><th>Price</th><th>Size</th><th>Rooms</th><th>Neighborhood</th><th>Matched At</th></tr>
          </thead>
          <tbody>
            <tr v-for="m in matches" :key="m.matchId || m.id">
              <td style="font-size:0.78rem;color:#6c757d;">{{ (m.listingId || m.scrapedListingId || '—').toString().slice(0, 8) }}…</td>
              <td>€{{ m.price ?? '—' }}</td>
              <td>{{ m.size ?? '—' }} m²</td>
              <td>{{ m.rooms ?? '—' }}</td>
              <td>{{ m.neighborhood ?? '—' }}</td>
              <td>{{ formatDate(m.matchedAt || m.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const alerts = ref([])
const matches = ref([])
const neighborhoods = ref([])
const loadingAlerts = ref(false)
const loadingMatches = ref(false)
const saving = ref(false)
const globalError = ref('')
const formError = ref('')
const formSuccess = ref('')
const selectedAlert = ref(null)

const form = ref({ userId: '', neighborhood: '', maxPrice: null, minSize: null, minRooms: null })

function formatDate(iso) {
  if (!iso) return ''
  return new Date(iso).toLocaleString('et-EE', { dateStyle: 'short', timeStyle: 'short' })
}

async function loadAlerts() {
  loadingAlerts.value = true
  try {
    const res = await fetch('/api/alerts')
    if (res.ok) {
      const data = await res.json()
      alerts.value = Array.isArray(data) ? data : []
    }
  } catch (e) {
    globalError.value = 'Could not load alerts: ' + e.message
  } finally {
    loadingAlerts.value = false
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

async function createAlert() {
  formError.value = ''
  formSuccess.value = ''
  if (!form.value.userId) { formError.value = 'User ID is required.'; return }
  saving.value = true
  try {
    const body = { userId: form.value.userId }
    if (form.value.neighborhood) body.neighborhood = form.value.neighborhood
    if (form.value.maxPrice)     body.maxPrice = form.value.maxPrice
    if (form.value.minSize)      body.minSize = form.value.minSize
    if (form.value.minRooms)     body.minRooms = form.value.minRooms

    const res = await fetch('/api/alerts', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
    if (!res.ok) {
      const err = await res.json().catch(() => ({}))
      throw new Error(err.message || 'HTTP ' + res.status)
    }
    formSuccess.value = 'Alert created successfully!'
    form.value = { userId: form.value.userId, neighborhood: '', maxPrice: null, minSize: null, minRooms: null }
    loadAlerts()
  } catch (e) {
    formError.value = 'Error: ' + e.message
  } finally {
    saving.value = false
  }
}

async function deactivate(alert) {
  const id = alert.alertRuleId || alert.id
  try {
    const res = await fetch(`/api/alerts/${id}`, { method: 'DELETE' })
    if (res.ok || res.status === 204) {
      alert.active = false
    }
  } catch (e) {
    globalError.value = 'Could not deactivate alert: ' + e.message
  }
}

async function loadMatches(alert) {
  selectedAlert.value = alert
  matches.value = []
  loadingMatches.value = true
  const id = alert.alertRuleId || alert.id
  try {
    const res = await fetch(`/api/alerts/${id}/matches`)
    if (res.ok) {
      const data = await res.json()
      matches.value = Array.isArray(data) ? data : []
    }
  } catch (e) {
    globalError.value = 'Could not load matches: ' + e.message
  } finally {
    loadingMatches.value = false
  }
}

onMounted(() => {
  loadAlerts()
  loadNeighborhoods()
})
</script>
