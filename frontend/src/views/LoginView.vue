<template>
  <div class="login-wrap">
    <div class="login-card">
      <div class="login-logo">
        <div class="brand-sq">Ü</div>
        <div>
          <div class="login-title">Admin access</div>
          <div class="login-sub">Üüriturg control panel</div>
        </div>
      </div>

      <form @submit.prevent="attempt">
        <div class="form-group">
          <label class="form-label">Password</label>
          <input
            v-model="pw"
            type="password"
            class="form-input"
            placeholder="Enter admin password"
            autofocus
          />
          <div v-if="error" class="login-error">Incorrect password</div>
        </div>
        <button type="submit" class="btn btn-primary" style="width:100%;justify-content:center">
          Sign in
        </button>
      </form>

      <div class="login-back">
        <router-link to="/listings" class="link">← Back to listings</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth.js'

const { login } = useAuth()
const router = useRouter()
const pw    = ref('')
const error = ref(false)

function attempt() {
  error.value = false
  if (login(pw.value)) {
    router.push('/dashboard')
  } else {
    error.value = true
    pw.value = ''
  }
}
</script>

<style scoped>
.login-wrap  { min-height:100vh; display:flex; align-items:center; justify-content:center; background:var(--bg); }
.login-card  { width:360px; background:var(--card); border:1px solid var(--border); border-radius:var(--r); padding:32px; box-shadow:0 4px 24px rgba(0,0,0,.07); }
.login-logo  { display:flex; align-items:center; gap:12px; margin-bottom:28px; }
.brand-sq    { width:40px; height:40px; border-radius:8px; background:var(--primary); color:#fff; display:flex; align-items:center; justify-content:center; font-weight:800; font-size:18px; flex-shrink:0; }
.login-title { font-size:1.1rem; font-weight:700; color:var(--text); }
.login-sub   { font-size:.8rem; color:var(--muted); margin-top:1px; }
.login-error { color:var(--red); font-size:.8rem; margin-top:5px; }
.login-back  { text-align:center; margin-top:18px; }
</style>
