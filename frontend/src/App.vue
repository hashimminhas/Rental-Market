<template>
  <div id="app">
    <aside class="sidebar">
      <div class="sidebar-brand">
        <div class="brand-square">Ü</div>
        <div class="brand-text">
          <div class="brand-title">Üüriturg</div>
          <div class="brand-sub">Tartu rental intelligence</div>
        </div>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/" class="nav-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>
          Dashboard
        </router-link>
        <router-link to="/listings" class="nav-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><circle cx="3" cy="6" r="1" fill="currentColor"/><circle cx="3" cy="12" r="1" fill="currentColor"/><circle cx="3" cy="18" r="1" fill="currentColor"/></svg>
          Listings
        </router-link>
        <router-link to="/trends" class="nav-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>
          Price trends
        </router-link>
        <router-link to="/alerts" class="nav-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
          Alerts
        </router-link>
        <router-link to="/neighborhoods" class="nav-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
          Neighborhoods
        </router-link>
        <router-link to="/landlords" class="nav-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
          Landlords
        </router-link>
        <router-link to="/notifications" class="nav-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
          Notifications
        </router-link>
        <router-link to="/system-status" class="nav-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
          System status
        </router-link>
      </nav>
      <div class="demo-notice">
        <div class="demo-title">Demo data</div>
        <div class="demo-sub">All listings, alerts and notifications are mocked locally.</div>
      </div>
    </aside>
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style>
:root {
  --primary: #0d9488;
  --primary-dark: #0f766e;
  --primary-light: #e0f2f1;
  --primary-hover: rgba(13,148,136,0.08);
  --bg: #f8fafc;
  --card: #ffffff;
  --border: #e2e8f0;
  --text: #1e293b;
  --muted: #64748b;
  --light: #94a3b8;
  --green: #16a34a; --green-bg: #dcfce7;
  --yellow: #d97706; --yellow-bg: #fef3c7;
  --red: #dc2626; --red-bg: #fee2e2;
  --blue: #2563eb; --blue-bg: #dbeafe;
  --sidebar-w: 210px;
  --r: 8px;
  --shadow: 0 1px 3px rgba(0,0,0,0.07), 0 1px 2px rgba(0,0,0,0.04);
}
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
html, body { width: 100%; height: 100%; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Inter', 'Segoe UI', sans-serif; background: var(--bg); color: var(--text); font-size: 14px; line-height: 1.5; }
#app { display: flex; width: 100%; min-height: 100vh; }

/* ── Sidebar ── */
.sidebar { width: var(--sidebar-w); min-width: var(--sidebar-w); flex-shrink: 0; background: var(--card); border-right: 1px solid var(--border); display: flex; flex-direction: column; position: sticky; top: 0; height: 100vh; overflow-y: auto; z-index: 100; }
.sidebar-brand { display: flex; align-items: center; gap: 10px; padding: 16px 14px; border-bottom: 1px solid var(--border); }
.brand-square { width: 32px; height: 32px; border-radius: 6px; background: var(--primary); color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 800; font-size: 15px; flex-shrink: 0; }
.brand-title { font-weight: 700; font-size: 13px; color: var(--text); line-height: 1.2; }
.brand-sub { font-size: 11px; color: var(--muted); }
.sidebar-nav { flex: 1; padding: 10px 8px; }
.nav-item { display: flex; align-items: center; gap: 9px; padding: 7px 10px; border-radius: var(--r); color: var(--muted); text-decoration: none; font-size: 13px; font-weight: 500; transition: all 0.15s; margin-bottom: 2px; }
.nav-item svg { width: 15px; height: 15px; flex-shrink: 0; }
.nav-item:hover { background: var(--primary-hover); color: var(--primary); }
.nav-item.router-link-exact-active { background: var(--primary-light); color: var(--primary); font-weight: 600; }
.demo-notice { margin: 10px 8px 12px; padding: 10px 12px; background: #f8fafc; border-radius: var(--r); border: 1px solid var(--border); }
.demo-title { font-size: 11px; font-weight: 700; color: var(--text); margin-bottom: 3px; }
.demo-sub { font-size: 11px; color: var(--muted); line-height: 1.4; }

/* ── Main ── */
.main-content { flex: 1; min-width: 0; padding: 28px 32px; background: var(--bg); min-height: 100vh; overflow-x: hidden; }

/* ── Page header ── */
.ph { margin-bottom: 22px; display: flex; justify-content: space-between; align-items: flex-start; }
.ph-left .ph-title { font-size: 22px; font-weight: 700; color: var(--text); }
.ph-left .ph-sub { font-size: 13px; color: var(--muted); margin-top: 2px; }

/* ── Cards ── */
.card { background: var(--card); border-radius: var(--r); border: 1px solid var(--border); padding: 16px; box-shadow: var(--shadow); }
.card-title { font-size: 14px; font-weight: 600; color: var(--text); }
.card-sub { font-size: 12px; color: var(--muted); margin-top: 2px; }

/* ── Stat cards ── */
.stat-grid-4 { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.stat-grid-3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.stat-grid-5 { display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px; }
@media(max-width:1200px) { .stat-grid-5 { grid-template-columns: repeat(3, 1fr); } }
@media(max-width:900px)  { .stat-grid-4, .stat-grid-5 { grid-template-columns: repeat(2, 1fr); } }
.stat-card { background: var(--card); border-radius: var(--r); border: 1px solid var(--border); padding: 14px 16px; }
.sc-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.sc-label { font-size: 10px; font-weight: 700; color: var(--muted); text-transform: uppercase; letter-spacing: 0.06em; }
.sc-icon { width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.sc-icon svg { width: 15px; height: 15px; }
.sc-value { font-size: 24px; font-weight: 700; color: var(--text); line-height: 1.1; }
.sc-sub { font-size: 11px; color: var(--muted); margin-top: 3px; display: flex; align-items: center; gap: 4px; }
.change-up { color: var(--green); font-weight: 600; }
.change-down { color: var(--red); font-weight: 600; }

/* ── Badges ── */
.badge { display: inline-flex; align-items: center; padding: 2px 8px; border-radius: 20px; font-size: 11px; font-weight: 600; }
.badge-green { background: var(--green-bg); color: var(--green); }
.badge-yellow { background: var(--yellow-bg); color: var(--yellow); }
.badge-red { background: var(--red-bg); color: var(--red); }
.badge-blue { background: var(--blue-bg); color: var(--blue); }
.badge-gray { background: #f1f5f9; color: #475569; }
.badge-teal { background: var(--primary-light); color: var(--primary); }
.badge-kv     { background: #fdf4ff; color: #9333ea; font-size: 10px; }
.badge-city   { background: #eff6ff; color: #3b82f6; font-size: 10px; }
.badge-k24    { background: #fff7ed; color: #ea580c; font-size: 10px; }
.badge-rendin { background: #f0fdf4; color: #16a34a; font-size: 10px; }
.badge-maamet { background: #f8fafc; color: #475569; font-size: 10px; }

/* ── Buttons ── */
.btn { display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: var(--r); font-size: 13px; font-weight: 500; cursor: pointer; border: none; transition: all 0.15s; line-height: 1; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }
.btn svg { width: 14px; height: 14px; }
.btn-primary { background: var(--primary); color: #fff; }
.btn-primary:hover:not(:disabled) { background: var(--primary-dark); }
.btn-outline { background: var(--card); border: 1px solid var(--border); color: var(--text); }
.btn-outline:hover:not(:disabled) { background: #f8fafc; }
.btn-ghost { background: transparent; color: var(--muted); }
.btn-ghost:hover { background: #f1f5f9; color: var(--text); }
.btn-danger { background: var(--red); color: #fff; }
.btn-sm { padding: 5px 10px; font-size: 12px; }
.btn-sm svg { width: 12px; height: 12px; }
.btn-icon { padding: 6px; background: transparent; border: 1px solid var(--border); color: var(--muted); border-radius: var(--r); }
.btn-icon:hover { background: #f1f5f9; }

/* ── Forms ── */
.form-group { margin-bottom: 13px; }
.form-label { display: block; font-size: 12px; font-weight: 600; color: var(--text); margin-bottom: 5px; }
.form-input, .form-select {
  width: 100%; padding: 8px 11px; border: 1px solid var(--border);
  border-radius: var(--r); font-size: 13px; color: var(--text);
  background: var(--card); transition: border-color 0.15s; font-family: inherit;
}
.form-input:focus, .form-select:focus { outline: none; border-color: var(--primary); box-shadow: 0 0 0 3px rgba(13,148,136,0.1); }
.form-input::placeholder { color: var(--light); }
.form-select { appearance: none; background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%2394a3b8' stroke-width='2.5'%3E%3Cpolyline points='6 9 12 15 18 9'/%3E%3C/svg%3E"); background-repeat: no-repeat; background-position: right 10px center; padding-right: 30px; }

/* ── Tables ── */
.tbl-wrap { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-size: 13px; }
thead th { padding: 8px 12px; text-align: left; font-size: 10px; font-weight: 700; color: var(--muted); text-transform: uppercase; letter-spacing: 0.06em; border-bottom: 1px solid var(--border); }
tbody td { padding: 10px 12px; border-bottom: 1px solid #f1f5f9; }
tbody tr:last-child td { border-bottom: none; }
tbody tr:hover td { background: #f8fafc; }

/* ── Spinner ── */
@keyframes spin { to { transform: rotate(360deg); } }
.spinner { width: 22px; height: 22px; border: 2px solid var(--border); border-top-color: var(--primary); border-radius: 50%; animation: spin .7s linear infinite; display: inline-block; }

/* ── States ── */
.state-loading { display: flex; align-items: center; justify-content: center; gap: 10px; padding: 40px; color: var(--muted); font-size: 13px; }
.state-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px; padding: 40px; color: var(--light); font-size: 13px; }
.state-empty p { margin: 0; }
.state-error { display: flex; align-items: center; gap: 8px; background: var(--red-bg); color: var(--red); padding: 10px 14px; border-radius: var(--r); font-size: 13px; margin-bottom: 14px; }

/* ── Data table (alias) ── */
.data-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.data-table thead th { padding: 8px 12px; text-align: left; font-size: 10px; font-weight: 700; color: var(--muted); text-transform: uppercase; letter-spacing: 0.06em; border-bottom: 1px solid var(--border); background: var(--bg); }
.data-table tbody td { padding: 10px 12px; border-bottom: 1px solid #f1f5f9; }
.data-table tbody tr:last-child td { border-bottom: none; }
.data-table tbody tr:hover td { background: #f8fafc; }

/* ── Divider ── */
.divider { border: none; border-top: 1px solid var(--border); margin: 16px 0; }

/* ── Link ── */
.link { color: var(--primary); text-decoration: none; font-size: 12px; font-weight: 500; }
.link:hover { text-decoration: underline; }

/* ── Toggle switch ── */
.toggle { position: relative; width: 36px; height: 20px; flex-shrink: 0; }
.toggle input { opacity: 0; width: 0; height: 0; }
.toggle-track { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background: #cbd5e1; border-radius: 20px; transition: 0.2s; }
.toggle-track::before { content: ''; position: absolute; width: 14px; height: 14px; left: 3px; bottom: 3px; background: white; border-radius: 50%; transition: 0.2s; }
.toggle input:checked + .toggle-track { background: var(--primary); }
.toggle input:checked + .toggle-track::before { transform: translateX(16px); }
</style>
