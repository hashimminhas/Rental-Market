<template>
  <div id="app">
    <nav class="navbar">
      <div class="nav-inner">
        <span class="nav-brand">🏠 Üüriturg</span>
        <div class="nav-links">
          <router-link to="/">Dashboard</router-link>
          <router-link to="/listings">Listings</router-link>
          <router-link to="/trends">Trends</router-link>
          <router-link to="/neighborhoods">Neighborhoods</router-link>
          <router-link to="/landlords">Landlords</router-link>
          <router-link to="/alerts">Alerts</router-link>
        </div>
      </div>
    </nav>
    <main class="main-content">
      <router-view />
    </main>
    <footer class="footer">
      <span>Üüriturg — Tartu Rental Market · University of Tartu ESI Project 2026</span>
    </footer>
  </div>
</template>

<style>
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: system-ui, -apple-system, sans-serif; background: #f0f2f5; color: #212529; }
#app { min-height: 100vh; display: flex; flex-direction: column; }

.navbar {
  background: #1a3a5c;
  position: sticky; top: 0; z-index: 100;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}
.nav-inner {
  max-width: 1200px; margin: 0 auto;
  padding: 0 24px; height: 54px;
  display: flex; align-items: center; gap: 32px;
}
.nav-brand { color: #fff; font-weight: 700; font-size: 1.15rem; white-space: nowrap; }
.nav-links { display: flex; gap: 4px; }
.nav-links a {
  color: #aac4e0; text-decoration: none; font-size: 0.9rem;
  padding: 6px 12px; border-radius: 6px; transition: all 0.15s;
}
.nav-links a:hover { color: #fff; background: rgba(255,255,255,0.1); }
.nav-links a.router-link-active { color: #fff; background: rgba(255,255,255,0.15); font-weight: 600; }

.main-content { flex: 1; max-width: 1200px; margin: 0 auto; width: 100%; padding: 28px 24px; }

.footer {
  background: #1a3a5c; color: #7a9cbf;
  text-align: center; padding: 14px; font-size: 0.8rem;
}

/* Shared utility classes used across all views */
.page-title { font-size: 1.6rem; font-weight: 700; color: #1a3a5c; margin-bottom: 4px; }
.page-subtitle { color: #6c757d; margin-bottom: 24px; font-size: 0.95rem; }

.card {
  background: #fff; border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07);
  padding: 20px;
}

.stat-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 28px; }
.stat-card {
  background: #fff; border-radius: 10px; padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07);
  border-left: 4px solid #1a3a5c;
}
.stat-label { font-size: 0.8rem; color: #6c757d; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 6px; }
.stat-value { font-size: 1.8rem; font-weight: 700; color: #1a3a5c; }
.stat-sub { font-size: 0.8rem; color: #6c757d; margin-top: 4px; }

.badge {
  display: inline-block; padding: 3px 10px; border-radius: 20px;
  font-size: 0.78rem; font-weight: 600;
}
.badge-green { background: #d4edda; color: #155724; }
.badge-red   { background: #f8d7da; color: #721c24; }
.badge-blue  { background: #cce5ff; color: #004085; }
.badge-gray  { background: #e9ecef; color: #495057; }
.badge-orange{ background: #fff3cd; color: #856404; }

.btn {
  padding: 8px 18px; border-radius: 6px; border: none;
  cursor: pointer; font-size: 0.9rem; font-weight: 500;
  transition: all 0.15s;
}
.btn-primary { background: #1a3a5c; color: #fff; }
.btn-primary:hover { background: #0f2840; }
.btn-danger  { background: #dc3545; color: #fff; }
.btn-danger:hover { background: #b02a37; }
.btn-secondary { background: #6c757d; color: #fff; }
.btn-secondary:hover { background: #5a6268; }
.btn-outline {
  background: transparent; border: 1px solid #1a3a5c;
  color: #1a3a5c;
}
.btn-outline:hover { background: #1a3a5c; color: #fff; }
.btn-sm { padding: 4px 10px; font-size: 0.8rem; }

.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 0.85rem; font-weight: 600; color: #495057; margin-bottom: 5px; }
.form-control {
  width: 100%; padding: 8px 12px; border: 1px solid #ced4da;
  border-radius: 6px; font-size: 0.9rem; color: #212529;
  transition: border-color 0.15s;
}
.form-control:focus { outline: none; border-color: #1a3a5c; box-shadow: 0 0 0 3px rgba(26,58,92,0.1); }

.table-wrap { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
th { background: #f8f9fa; color: #495057; font-weight: 600; padding: 10px 14px; text-align: left; border-bottom: 2px solid #dee2e6; }
td { padding: 10px 14px; border-bottom: 1px solid #f0f2f5; vertical-align: middle; }
tr:hover td { background: #f8f9fa; }

.loading { text-align: center; padding: 40px; color: #6c757d; font-size: 1rem; }
.empty   { text-align: center; padding: 40px; color: #adb5bd; font-size: 0.95rem; }
.error   { background: #f8d7da; color: #721c24; padding: 12px 16px; border-radius: 8px; margin-bottom: 16px; font-size: 0.9rem; }
</style>
