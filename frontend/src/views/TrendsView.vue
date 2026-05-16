<template>
  <div>
    <h1 class="page-title">Price Trends</h1>
    <p class="page-subtitle">Average rent over time by neighborhood</p>

    <div class="card" style="margin-bottom:20px;">
      <div style="display:flex;gap:12px;align-items:flex-end;flex-wrap:wrap;">
        <div class="form-group" style="margin:0;min-width:200px;">
          <label>Neighborhood</label>
          <select class="form-control" v-model="selectedNeighborhood" @change="loadTrends">
            <option value="">— Select neighborhood —</option>
            <option v-for="n in neighborhoods" :key="n" :value="n">{{ n }}</option>
          </select>
        </div>
        <div class="form-group" style="margin:0;min-width:140px;">
          <label>Period</label>
          <select class="form-control" v-model="days" @change="loadTrends">
            <option :value="7">Last 7 days</option>
            <option :value="14">Last 14 days</option>
            <option :value="30">Last 30 days</option>
            <option :value="90">Last 90 days</option>
          </select>
        </div>
        <button class="btn btn-primary" @click="loadTrends" :disabled="!selectedNeighborhood">Load</button>
      </div>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <div class="card">
      <div v-if="!selectedNeighborhood" class="empty">Select a neighborhood to view price trends.</div>
      <div v-else-if="loading" class="loading">Loading chart data…</div>
      <div v-else-if="noData" class="empty">No trend data available for {{ selectedNeighborhood }} in this period.</div>
      <div v-else>
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
          <strong>{{ selectedNeighborhood }} — avg. price/month (€)</strong>
          <span class="badge badge-blue">{{ days }} days</span>
        </div>
        <canvas ref="chartCanvas" style="max-height:380px;"></canvas>
        <div style="margin-top:16px;display:flex;gap:24px;flex-wrap:wrap;">
          <div>
            <span style="font-size:0.8rem;color:#6c757d;">Min</span>
            <div style="font-weight:700;color:#1a3a5c;">€{{ trendSummary.min }}</div>
          </div>
          <div>
            <span style="font-size:0.8rem;color:#6c757d;">Max</span>
            <div style="font-weight:700;color:#1a3a5c;">€{{ trendSummary.max }}</div>
          </div>
          <div>
            <span style="font-size:0.8rem;color:#6c757d;">Average</span>
            <div style="font-weight:700;color:#1a3a5c;">€{{ trendSummary.avg }}</div>
          </div>
          <div>
            <span style="font-size:0.8rem;color:#6c757d;">Data Points</span>
            <div style="font-weight:700;color:#1a3a5c;">{{ trendSummary.count }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

const neighborhoods = ref([])
const selectedNeighborhood = ref('')
const days = ref(30)
const loading = ref(false)
const error = ref('')
const noData = ref(false)
const chartCanvas = ref(null)
const trendSummary = ref({ min: 0, max: 0, avg: 0, count: 0 })

let chartInstance = null

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

async function loadTrends() {
  if (!selectedNeighborhood.value) return
  loading.value = true
  error.value = ''
  noData.value = false

  try {
    const url = `/api/analytics/trends/${encodeURIComponent(selectedNeighborhood.value)}?days=${days.value}`
    const res = await fetch(url)
    if (!res.ok) throw new Error('HTTP ' + res.status)
    const data = await res.json()

    const points = Array.isArray(data) ? data : (data.trends ?? data.data ?? [])

    if (points.length === 0) {
      noData.value = true
      destroyChart()
      return
    }

    const labels = points.map(p => p.date || p.period || p.label || '')
    const values = points.map(p => p.averagePrice ?? p.avgPrice ?? p.price ?? 0)

    const nums = values.filter(v => v > 0)
    trendSummary.value = {
      min: nums.length ? Math.min(...nums).toFixed(0) : 0,
      max: nums.length ? Math.max(...nums).toFixed(0) : 0,
      avg: nums.length ? (nums.reduce((a, b) => a + b, 0) / nums.length).toFixed(0) : 0,
      count: points.length,
    }

    await nextTick()
    renderChart(labels, values)
  } catch (e) {
    error.value = 'Could not load trend data: ' + e.message
    noData.value = true
  } finally {
    loading.value = false
  }
}

function renderChart(labels, values) {
  destroyChart()
  if (!chartCanvas.value) return
  chartInstance = new Chart(chartCanvas.value, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: 'Avg Price (€)',
        data: values,
        borderColor: '#1a3a5c',
        backgroundColor: 'rgba(26,58,92,0.08)',
        borderWidth: 2,
        pointRadius: 4,
        pointHoverRadius: 6,
        fill: true,
        tension: 0.3,
      }],
    },
    options: {
      responsive: true,
      plugins: {
        legend: { display: false },
        tooltip: { callbacks: { label: ctx => '€' + ctx.parsed.y } },
      },
      scales: {
        y: {
          beginAtZero: false,
          ticks: { callback: v => '€' + v },
          grid: { color: '#f0f2f5' },
        },
        x: { grid: { display: false } },
      },
    },
  })
}

function destroyChart() {
  if (chartInstance) { chartInstance.destroy(); chartInstance = null }
}

onMounted(loadNeighborhoods)
onBeforeUnmount(destroyChart)
</script>
