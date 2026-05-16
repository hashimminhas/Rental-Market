<template>
  <div>
    <h1 class="page-title">Landlords</h1>
    <p class="page-subtitle">Verified landlord profiles and tenant reviews</p>

    <div v-if="error" class="error">{{ error }}</div>

    <!-- Top-rated section -->
    <div v-if="topLandlords.length" style="margin-bottom:28px;">
      <h2 style="font-size:1.1rem;font-weight:700;color:#1a3a5c;margin-bottom:12px;">Top Rated</h2>
      <div style="display:flex;gap:14px;overflow-x:auto;padding-bottom:8px;">
        <div v-for="l in topLandlords" :key="l.landlordId" class="card top-card" style="min-width:200px;flex-shrink:0;">
          <div style="font-weight:700;font-size:1rem;color:#1a3a5c;margin-bottom:4px;">{{ l.displayName }}</div>
          <div style="font-size:0.88rem;margin-bottom:4px;">{{ starRating(l.averageRating) }}</div>
          <div style="font-size:0.8rem;color:#6c757d;">{{ l.reviewCount }} review{{ l.reviewCount === 1 ? '' : 's' }}</div>
          <div style="margin-top:8px;">
            <span v-if="l.verified || l.isVerified" class="badge badge-green">Verified</span>
            <span v-else class="badge badge-gray">Unverified</span>
          </div>
        </div>
      </div>
    </div>

    <!-- All landlords table -->
    <div class="card" style="margin-bottom:20px;">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:14px;">
        <strong style="font-size:1rem;">All Landlords</strong>
        <button class="btn btn-outline btn-sm" @click="loadLandlords">Refresh</button>
      </div>
      <div v-if="loading" class="loading" style="padding:20px;">Loading…</div>
      <div v-else-if="landlords.length === 0" class="empty" style="padding:20px;">No landlord profiles yet.</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr><th>Name</th><th>Rating</th><th>Reviews</th><th>Phone</th><th>Status</th><th></th></tr>
          </thead>
          <tbody>
            <tr v-for="l in landlords" :key="l.landlordId">
              <td style="font-weight:600;">{{ l.displayName }}</td>
              <td>{{ starRating(l.averageRating) }} <span style="color:#6c757d;font-size:0.8rem;">({{ Number(l.averageRating).toFixed(1) }})</span></td>
              <td>{{ l.reviewCount }}</td>
              <td>{{ l.phoneNumber || '—' }}</td>
              <td>
                <span :class="(l.verified || l.isVerified) ? 'badge badge-green' : 'badge badge-orange'">
                  {{ (l.verified || l.isVerified) ? 'Verified' : 'Unverified' }}
                </span>
              </td>
              <td>
                <button class="btn btn-outline btn-sm" @click="openProfile(l)">View</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Landlord detail + review form -->
    <div v-if="selectedLandlord" class="card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
        <div>
          <strong style="font-size:1.1rem;color:#1a3a5c;">{{ selectedLandlord.displayName }}</strong>
          <span v-if="selectedLandlord.verified || selectedLandlord.isVerified"
                class="badge badge-green" style="margin-left:8px;">Verified</span>
        </div>
        <button class="btn btn-outline btn-sm" @click="selectedLandlord = null">Close</button>
      </div>

      <p v-if="selectedLandlord.bio" style="color:#555;font-size:0.9rem;margin-bottom:14px;">{{ selectedLandlord.bio }}</p>
      <div style="display:flex;gap:24px;margin-bottom:16px;flex-wrap:wrap;">
        <div><span style="font-size:0.8rem;color:#6c757d;">Rating</span>
          <div style="font-weight:700;color:#1a3a5c;">{{ starRating(selectedLandlord.averageRating) }}</div></div>
        <div><span style="font-size:0.8rem;color:#6c757d;">Reviews</span>
          <div style="font-weight:700;color:#1a3a5c;">{{ selectedLandlord.reviewCount }}</div></div>
        <div v-if="selectedLandlord.phoneNumber">
          <span style="font-size:0.8rem;color:#6c757d;">Phone</span>
          <div style="font-weight:700;color:#1a3a5c;">{{ selectedLandlord.phoneNumber }}</div>
        </div>
      </div>

      <!-- Leave a review -->
      <div style="background:#f8f9fa;border-radius:8px;padding:14px;margin-bottom:16px;">
        <strong style="font-size:0.9rem;display:block;margin-bottom:10px;">Leave a Tenant Review</strong>
        <div style="display:flex;gap:10px;flex-wrap:wrap;align-items:flex-end;">
          <div class="form-group" style="margin:0;min-width:220px;">
            <label>Your User ID (UUID)</label>
            <input class="form-control" v-model="reviewForm.reviewerUserId" placeholder="your user UUID" />
          </div>
          <div class="form-group" style="margin:0;min-width:120px;">
            <label>Rating (1–5)</label>
            <input class="form-control" type="number" v-model.number="reviewForm.rating" min="1" max="5" />
          </div>
          <div class="form-group" style="margin:0;flex:1;min-width:200px;">
            <label>Comment</label>
            <input class="form-control" v-model="reviewForm.comment" placeholder="Optional" />
          </div>
          <button class="btn btn-primary btn-sm" :disabled="submittingReview" @click="submitReview">
            {{ submittingReview ? 'Submitting…' : 'Submit' }}
          </button>
        </div>
        <div v-if="reviewError" style="color:#721c24;font-size:0.82rem;margin-top:6px;">{{ reviewError }}</div>
        <div v-if="reviewSuccess" style="color:#155724;font-size:0.82rem;margin-top:6px;">{{ reviewSuccess }}</div>
      </div>

      <!-- Review list -->
      <div v-if="loadingReviews" class="loading" style="padding:20px;">Loading reviews…</div>
      <div v-else-if="reviews.length === 0" class="empty" style="padding:20px;">No tenant reviews yet.</div>
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
</template>

<script setup>
import { ref, onMounted } from 'vue'

const landlords = ref([])
const topLandlords = ref([])
const reviews = ref([])
const loading = ref(false)
const loadingReviews = ref(false)
const submittingReview = ref(false)
const error = ref('')
const reviewError = ref('')
const reviewSuccess = ref('')
const selectedLandlord = ref(null)
const reviewForm = ref({ reviewerUserId: '', rating: 5, comment: '' })

function starRating(rating) {
  const r = Math.round(rating ?? 0)
  return '★'.repeat(r) + '☆'.repeat(5 - r)
}

function formatDate(iso) {
  if (!iso) return ''
  return new Date(iso).toLocaleDateString('et-EE')
}

async function loadLandlords() {
  loading.value = true
  error.value = ''
  try {
    const [allRes, topRes] = await Promise.all([
      fetch('/api/landlords'),
      fetch('/api/landlords/top'),
    ])
    if (allRes.ok) {
      const data = await allRes.json()
      landlords.value = Array.isArray(data) ? data : []
    }
    if (topRes.ok) {
      const data = await topRes.json()
      topLandlords.value = Array.isArray(data) ? data : []
    }
  } catch (e) {
    error.value = 'Could not load landlords: ' + e.message
  } finally {
    loading.value = false
  }
}

async function openProfile(landlord) {
  selectedLandlord.value = landlord
  reviews.value = []
  reviewError.value = ''
  reviewSuccess.value = ''
  loadingReviews.value = true
  try {
    const res = await fetch(`/api/landlords/${landlord.landlordId}/reviews`)
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
  if (!reviewForm.value.reviewerUserId) { reviewError.value = 'Your User ID is required.'; return }
  if (!reviewForm.value.rating || reviewForm.value.rating < 1 || reviewForm.value.rating > 5) {
    reviewError.value = 'Rating must be 1–5.'; return
  }
  submittingReview.value = true
  try {
    const body = {
      landlordId: selectedLandlord.value.landlordId,
      reviewerUserId: reviewForm.value.reviewerUserId,
      rating: reviewForm.value.rating,
      comment: reviewForm.value.comment || null,
    }
    const res = await fetch('/api/landlords/reviews', {
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
    openProfile(selectedLandlord.value)
  } catch (e) {
    reviewError.value = 'Error: ' + e.message
  } finally {
    submittingReview.value = false
  }
}

onMounted(loadLandlords)
</script>

<style scoped>
.top-card { transition: transform 0.15s; cursor: default; }
.top-card:hover { transform: translateY(-2px); }
</style>
