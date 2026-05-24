<template>
  <div id="app">
    <!-- Top Navbar -->
    <header class="navbar" :class="{ 'navbar--scrolled': scrolled }">
      <div class="navbar-inner">
        <router-link to="/" class="navbar-logo">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
          Üüriturg
        </router-link>

        <!-- Desktop nav -->
        <nav class="navbar-links">
          <router-link to="/listings" class="nav-link">Listings</router-link>
          <router-link to="/insights" class="nav-link">Market Insights</router-link>
          <router-link to="/alerts" class="nav-link">Alerts</router-link>
        </nav>

        <div class="navbar-right">
          <router-link v-if="!isAdmin" to="/login" class="nav-link nav-link--muted">Admin</router-link>
          <template v-else>
            <router-link to="/dashboard" class="nav-link nav-link--muted">Admin</router-link>
            <button class="btn-logout" @click="doLogout">Sign out</button>
          </template>
          <!-- Mobile hamburger -->
          <button class="hamburger" @click="menuOpen=!menuOpen" aria-label="Menu">
            <span :class="{ open: menuOpen }"></span>
            <span :class="{ open: menuOpen }"></span>
            <span :class="{ open: menuOpen }"></span>
          </button>
        </div>
      </div>

      <!-- Mobile menu -->
      <div class="mobile-menu" :class="{ 'mobile-menu--open': menuOpen }">
        <router-link to="/listings"  class="mob-link" @click="menuOpen=false">Listings</router-link>
        <router-link to="/insights"  class="mob-link" @click="menuOpen=false">Market Insights</router-link>
        <router-link to="/alerts"    class="mob-link" @click="menuOpen=false">Alerts</router-link>
        <router-link v-if="!isAdmin" to="/login" class="mob-link mob-link--muted" @click="menuOpen=false">Admin login</router-link>
        <template v-else>
          <router-link to="/dashboard" class="mob-link mob-link--muted" @click="menuOpen=false">Admin panel</router-link>
          <button class="mob-link mob-link--danger" @click="doLogout">Sign out</button>
        </template>
      </div>
    </header>

    <!-- Page content -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from './composables/useAuth.js'

const { isAdmin, logout } = useAuth()
const router   = useRouter()
const menuOpen = ref(false)
const scrolled = ref(false)

function doLogout() {
  logout()
  menuOpen.value = false
  router.push('/')
}

function onScroll() { scrolled.value = window.scrollY > 10 }
onMounted(() => window.addEventListener('scroll', onScroll))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>

<style>
:root {
  --primary: #0d9488;
  --primary-dark: #0f766e;
  --primary-light: #e0f2f1;
  --primary-hover: rgba(13,148,136,0.08);
  --bg: #f8fafc;
  --card: #ffffff;
  --border: #e2e8f0;
  --text: #0f172a;
  --muted: #64748b;
  --light: #94a3b8;
  --green: #16a34a; --green-bg: #dcfce7;
  --yellow: #d97706; --yellow-bg: #fef3c7;
  --red: #dc2626;   --red-bg: #fee2e2;
  --blue: #2563eb;  --blue-bg: #dbeafe;
  --navbar-h: 60px;
  --r: 8px;
  --shadow: 0 1px 3px rgba(0,0,0,0.07), 0 1px 2px rgba(0,0,0,0.04);
  --shadow-md: 0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -1px rgba(0,0,0,0.06);
}
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
html, body { width: 100%; height: 100%; }
body { font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: var(--bg); color: var(--text); font-size: 14px; line-height: 1.5; }
#app { display: flex; flex-direction: column; min-height: 100vh; }

/* ── Navbar ── */
.navbar {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  height: var(--navbar-h); background: rgba(255,255,255,0.95);
  backdrop-filter: blur(8px); border-bottom: 1px solid transparent;
  transition: border-color .2s, box-shadow .2s;
}
.navbar--scrolled { border-bottom-color: var(--border); box-shadow: 0 1px 8px rgba(0,0,0,0.06); }
.navbar-inner { max-width: 1280px; margin: 0 auto; padding: 0 24px; height: 100%; display: flex; align-items: center; gap: 32px; }
.navbar-logo { display: flex; align-items: center; gap: 8px; text-decoration: none; font-size: 17px; font-weight: 800; color: var(--primary); flex-shrink: 0; }
.navbar-logo svg { color: var(--primary); }
.navbar-links { display: flex; align-items: center; gap: 4px; flex: 1; }
.nav-link { padding: 6px 12px; border-radius: 6px; text-decoration: none; font-size: 14px; font-weight: 500; color: var(--muted); transition: all .15s; }
.nav-link:hover { color: var(--text); background: #f1f5f9; }
.nav-link.router-link-active { color: var(--primary); font-weight: 600; }
.nav-link--muted { color: var(--light); }
.navbar-right { margin-left: auto; display: flex; align-items: center; gap: 8px; }
.btn-logout { padding: 5px 12px; border-radius: 6px; border: 1px solid var(--border); background: transparent; color: var(--muted); font-size: 13px; cursor: pointer; transition: all .15s; }
.btn-logout:hover { background: var(--red-bg); color: var(--red); border-color: var(--red); }

/* ── Hamburger ── */
.hamburger { display: none; flex-direction: column; gap: 5px; background: none; border: none; cursor: pointer; padding: 6px; border-radius: 6px; }
.hamburger span { display: block; width: 20px; height: 2px; background: var(--text); border-radius: 2px; transition: all .2s; }
.hamburger:hover { background: #f1f5f9; }

/* ── Mobile menu ── */
.mobile-menu { display: none; flex-direction: column; background: white; border-top: 1px solid var(--border); padding: 8px 16px 16px; }
.mobile-menu--open { display: flex; }
.mob-link { display: block; padding: 11px 0; font-size: 15px; font-weight: 500; color: var(--text); text-decoration: none; border-bottom: 1px solid #f1f5f9; background: none; border-left: none; border-right: none; text-align: left; cursor: pointer; font-family: inherit; }
.mob-link:last-child { border-bottom: none; }
.mob-link--muted { color: var(--muted); }
.mob-link--danger { color: var(--red); }

/* ── Main content ── */
.main-content { flex: 1; padding-top: var(--navbar-h); }

/* ── Page wrapper (for non-landing pages) ── */
.page-wrap { max-width: 1280px; margin: 0 auto; padding: 32px 24px; }

/* ── Cards ── */
.card { background: var(--card); border-radius: var(--r); border: 1px solid var(--border); padding: 16px; box-shadow: var(--shadow); }

/* ── Stat cards ── */
.stat-grid-4 { display: grid; grid-template-columns: repeat(4,1fr); gap: 12px; }
.stat-grid-3 { display: grid; grid-template-columns: repeat(3,1fr); gap: 12px; }
.stat-grid-5 { display: grid; grid-template-columns: repeat(5,1fr); gap: 12px; }
@media(max-width:1100px){ .stat-grid-5,.stat-grid-4 { grid-template-columns: repeat(2,1fr); } }
.stat-card { background: var(--card); border-radius: var(--r); border: 1px solid var(--border); padding: 16px 18px; box-shadow: var(--shadow); }
.sc-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.sc-label { font-size: 10px; font-weight: 700; color: var(--muted); text-transform: uppercase; letter-spacing: .06em; }
.sc-icon { width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.sc-icon svg { width: 15px; height: 15px; }
.sc-value { font-size: 24px; font-weight: 700; color: var(--text); line-height: 1.1; }
.sc-sub { font-size: 11px; color: var(--muted); margin-top: 3px; }

/* ── Badges ── */
.badge { display: inline-flex; align-items: center; padding: 2px 8px; border-radius: 20px; font-size: 11px; font-weight: 600; }
.badge-green { background: var(--green-bg); color: var(--green); }
.badge-red   { background: var(--red-bg);   color: var(--red); }
.badge-blue  { background: var(--blue-bg);  color: var(--blue); }
.badge-gray  { background: #f1f5f9; color: #475569; }
.badge-teal  { background: var(--primary-light); color: var(--primary); }
.badge-kv     { background: #fdf4ff; color: #9333ea; font-size: 10px; }
.badge-city   { background: #eff6ff; color: #3b82f6; font-size: 10px; }
.badge-k24    { background: #fff7ed; color: #ea580c; font-size: 10px; }
.badge-rendin { background: #f0fdf4; color: #16a34a; font-size: 10px; }
.badge-maamet { background: #f8fafc; color: #475569; font-size: 10px; }

/* ── Buttons ── */
.btn { display: inline-flex; align-items: center; gap: 6px; padding: 8px 16px; border-radius: var(--r); font-size: 14px; font-weight: 500; cursor: pointer; border: none; transition: all .15s; line-height: 1; text-decoration: none; }
.btn:disabled { opacity: .6; cursor: not-allowed; }
.btn-primary { background: var(--primary); color: #fff; }
.btn-primary:hover:not(:disabled) { background: var(--primary-dark); }
.btn-outline { background: var(--card); border: 1px solid var(--border); color: var(--text); }
.btn-outline:hover:not(:disabled) { background: #f8fafc; }
.btn-ghost { background: transparent; color: var(--muted); }
.btn-ghost:hover { background: #f1f5f9; }
.btn-sm { padding: 5px 10px; font-size: 12px; }
.btn-icon { padding: 6px; background: transparent; border: 1px solid var(--border); color: var(--muted); border-radius: var(--r); }
.btn-icon:hover { background: #f1f5f9; }

/* ── Forms ── */
.form-group { margin-bottom: 14px; }
.form-label { display: block; font-size: 12px; font-weight: 600; color: var(--text); margin-bottom: 5px; }
.form-input, .form-select {
  width: 100%; padding: 9px 12px; border: 1px solid var(--border);
  border-radius: var(--r); font-size: 13px; color: var(--text);
  background: var(--card); transition: border-color .15s; font-family: inherit;
}
.form-input:focus, .form-select:focus { outline: none; border-color: var(--primary); box-shadow: 0 0 0 3px rgba(13,148,136,.1); }
.form-input::placeholder { color: var(--light); }
.form-select { appearance: none; background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%2394a3b8' stroke-width='2.5'%3E%3Cpolyline points='6 9 12 15 18 9'/%3E%3C/svg%3E"); background-repeat: no-repeat; background-position: right 10px center; padding-right: 30px; }
.form-hint { font-size: 11px; color: var(--muted); margin-top: 4px; }

/* ── Tables ── */
.tbl-wrap { overflow-x: auto; }
table, .data-table { width: 100%; border-collapse: collapse; font-size: 13px; }
thead th { padding: 8px 12px; text-align: left; font-size: 10px; font-weight: 700; color: var(--muted); text-transform: uppercase; letter-spacing: .06em; border-bottom: 1px solid var(--border); }
tbody td { padding: 10px 12px; border-bottom: 1px solid #f1f5f9; }
tbody tr:last-child td { border-bottom: none; }
tbody tr:hover td { background: #f8fafc; }

/* ── Spinner / states ── */
@keyframes spin { to { transform: rotate(360deg); } }
.spinner { width: 22px; height: 22px; border: 2px solid var(--border); border-top-color: var(--primary); border-radius: 50%; animation: spin .7s linear infinite; display: inline-block; }
.state-loading { display: flex; align-items: center; justify-content: center; gap: 10px; padding: 40px; color: var(--muted); font-size: 13px; }
.state-empty   { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px; padding: 40px; color: var(--light); font-size: 13px; }
.state-error   { display: flex; align-items: center; gap: 8px; background: var(--red-bg); color: var(--red); padding: 10px 14px; border-radius: var(--r); font-size: 13px; }

/* ── Toggle ── */
.toggle { position: relative; width: 36px; height: 20px; flex-shrink: 0; }
.toggle input { opacity: 0; width: 0; height: 0; }
.toggle-track { position: absolute; cursor: pointer; inset: 0; background: #cbd5e1; border-radius: 20px; transition: .2s; }
.toggle-track::before { content:''; position:absolute; width:14px; height:14px; left:3px; bottom:3px; background:#fff; border-radius:50%; transition:.2s; }
.toggle input:checked + .toggle-track { background: var(--primary); }
.toggle input:checked + .toggle-track::before { transform: translateX(16px); }

/* ── Chip ── */
.chip { font-size: .75rem; background: var(--primary-light); color: var(--primary-dark,#0f766e); padding: 2px 8px; border-radius: 20px; font-weight: 500; }

/* ── Divider ── */
.divider { border: none; border-top: 1px solid var(--border); margin: 16px 0; }

/* ── Page (used by inner views) ── */
.page { max-width: 1280px; margin: 0 auto; padding: 32px 24px; display: flex; flex-direction: column; gap: 18px; }
.page-hd { display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 12px; }
.page-title { font-size: 1.4rem; font-weight: 700; color: var(--text); margin: 0 0 4px; }
.page-sub { font-size: .875rem; color: var(--muted); margin: 0; }
.ch-hd { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.ch-title { font-size: .9rem; font-weight: 600; color: var(--text); }
.ch-sub { font-size: .78rem; color: var(--muted); }
.ch-wrap { height: 200px; position: relative; }
.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.two-col--wide { grid-template-columns: 1.4fr 1fr; }
@media(max-width:900px){ .two-col,.two-col--wide { grid-template-columns: 1fr; } }

/* ── Mobile ── */
@media(max-width: 768px) {
  .navbar-links { display: none; }
  .hamburger { display: flex; }
  .navbar-right .nav-link, .navbar-right .btn-logout { display: none; }
  .page-wrap { padding: 24px 16px; }
}
</style>
