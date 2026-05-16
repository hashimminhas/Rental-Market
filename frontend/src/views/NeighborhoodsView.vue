<template>
  <div>
    <h1 class="page-title">Neighborhoods</h1>
    <p class="page-subtitle">Explore Tartu's rental districts</p>

    <div v-if="error" class="error">{{ error }}</div>

    <div v-if="loading" class="loading">Loading neighborhoods…</div>

    <div v-else-if="neighborhoods.length === 0" class="empty">No neighborhoods found.</div>

    <div v-else>
      <div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(340px,1fr));gap:16px;margin-bottom:28px;">
        <div v-for="n in neighborhoods" :key="n.neighborhoodId || n.slug" class="card nb-card">
          <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:8px;">
            <div>
              <strong style="font-size:1.05rem;color:#1a3a5c;">{{ n.name }}</strong>
              <div style="font-size:0.78rem;color:#adb5bd;">{{ n.distanceToCenter ? n.distanceToCenter + ' km from centre' : '' }}</div>
            </div>
            <div style="text-align:right;">
              <div v-if="n.currentAveragePrice" class="badge badge-blue">€{{ Number(n.currentAveragePrice).toFixed(0) }}/mo</div>
              <div v-if="n.averageRating != null" style="font-size:0.82rem;margin-top:4px;">
                {{ starRating(n.averageRating) }} <span style="color:#6c757d;">({{ n.reviewCount ?? 0 }})</span>
              </div>
            </div>
          </div>

          <p v-if="n.description" style="font-size:0.85rem;color:#555;margin-bottom:10px;line-height:1.5;">
            {{ n.description }}
          </p>

          <div v-if="n.characteristics && n.characteristics.length" style="display:flex;flex-wrap:wrap;gap:6px;margin-bottom:10px;">
            <span v-for="c in n.characteristics" :key="c" class="badge badge-gray">{{ c }}</span>
          </div>

          <button class="btn btn-outline btn-sm" @click="openReviews(n)" style="width:100%;">
            Reviews ({{ n.reviewCount ?? 0 }})
          </button>
        </div>
      </div>

      <!-- Reviews panel -->
      <div v-if="selectedNeighborhood" class="card">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:14px;">
          <strong>Reviews — {{ selectedNeighborhood.name }}</strong>
          <button class="btn btn-outline btn-sm" @click="selectedNeighborhood = null">Close</button>
        </div>

        <!-- Submit review -->
        <div style="background:#f8f9fa;border-radius:8px;padding:14px;margin-bottom:16px;">
          <strong style="font-size:0.9rem;display:block;margin-bottom:10px;">Leave a Review</strong>
          <div style="display:flex;gap:10px;flex-wrap:wrap;align-items:flex-end;">
            <div class="form-group" style="margin:0;min-width:220px;">
              <label>User ID (UUID)</label>
              <input class="form-control" v-model="reviewForm.userId" placeholder="your user UUID" />
            </div>
            <div class="form-group" style="margin:0;min-width:120px;">
              <label>Rating (1–5)</label>
              <input class="form-control" type="number" v-model.number="reviewForm.rating" min="1" max="5" />
            </div>
            <div class="form-group" style="margin:0;flex:1;min-width:200px;">
              <label>Comment</label>
              <input class="form-control" v-model="reviewForm.comment" placeholder="Optional comment" />
            </div>
            <button class="btn btn-primary btn-sm" :disabled="submittingReview" @click="submitReview">
              {{ submittingReview ? 'Submitting…' : 'Submit' }}
            </button>
          </div>
          <div v-if="reviewError" style="color:#721c24;font-size:0.82rem;margin-top:6px;">{{ reviewError }}</div>
          <div v-if="reviewSuccess" style="color:#155724;font-size:0.82rem;margin-top:6px;">{{ reviewSuccess }}</div>
        </div>

        <div v-if="loadingReviews" class="loading" style="padding:20px;">Loading reviews…</div>
        <div v-else-if="reviews.length === 0" class="empty" style="padding:20px;">No reviews yet. Be the first!</div>
        <div v-else>
          <div v-for="r in reviews" :key="r.reviewId || r.id"
               style="padding:12px 0;border-bottom:1px solid #f0f2f5;">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:4px;">
              <span>{{ starRating(r.rating) }}</span>
              <span style="font-size:0.78rem;color:#adb5bd;">{{ formatDate(r.createdAt) }}</span>
            </div>
            <p v-if="r.comment" style="font-size:0.88rem;color:#495057;margin:0;">{{ r.comment }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const neighborhoods = ref([])
const loading = ref(false)
const error = ref('')
const selectedNeighborhood = ref(null)
const reviews = ref([])
const loadingReviews = ref(false)
const submittingReview = ref(false)
const reviewError = ref('')
const reviewSuccess = ref('')
const reviewForm = ref({ userId: '', rating: 5, comment: '' })

function starRating(rating) {
  const r = Math.round(rating ?? 0)
  return '★'.repeat(r) + '☆'.repeat(5 - r)
}

function formatDate(iso) {
  if (!iso) return ''
  return new Date(iso).toLocaleDateString('et-EE')
}

async function loadNeighborhoods() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch('/api/neighborhoods')
    if (!res.ok) throw new Error('HTTP ' + res.status)
    const data = await res.json()
    neighborhoods.value = Array.isArray(data) ? data : []
  } catch (e) {
    error.value = 'Could not load neighborhoods: ' + e.message
  } finally {
    loading.value = false
  }
}

async function openReviews(nb) {
  selectedNeighborhood.value = nb
  reviews.value = []
  reviewError.value = ''
  reviewSuccess.value = ''
  loadingReviews.value = true
  try {
    const res = await fetch(`/api/neighborhoods/${nb.neighborhoodId}/reviews`)
    if (res.ok) {
      const data = await res.json()
      reviews.value = Array.isArray(data) ? data : []
    }
  } catch (_) {} finally {
    loadingReviews.value = false
  }
}

async function submitReview() {
  reviewError.value = ''
  reviewSuccess.value = ''
  if (!reviewForm.value.userId) { reviewError.value = 'User ID is required.'; return }
  if (!reviewForm.value.rating || reviewForm.value.rating < 1 || reviewForm.value.rating > 5) {
    reviewError.value = 'Rating must be 1–5.'; return
  }
  submittingReview.value = true
  try {
    const body = {
      neighborhoodId: selectedNeighborhood.value.neighborhoodId,
      userId: reviewForm.value.userId,
      rating: reviewForm.value.rating,
      comment: reviewForm.value.comment || null,
    }
    const res = await fetch('/api/neighborhoods/reviews', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
    if (!res.ok) {
      const err = await res.json().catch(() => ({}))
      throw new Error(err.message || 'HTTP ' + res.status)
    }
    reviewSuccess.value = 'Review submitted!'
    reviewForm.value.comment = ''
    openReviews(selectedNeighborhood.value)
  } catch (e) {
    reviewError.value = 'Error: ' + e.message
  } finally {
    submittingReview.value = false
  }
}

onMounted(loadNeighborhoods)
</script>

<style scoped>
.nb-card { transition: transform 0.15s, box-shadow 0.15s; }
.nb-card:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(0,0,0,0.12); }
</style>
