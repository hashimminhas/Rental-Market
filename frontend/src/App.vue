<template>
  <div>
    <nav style="background:#1a3a5c;padding:0 24px;display:flex;align-items:center;gap:24px;height:52px;position:sticky;top:0;z-index:100;">
      <span style="color:#fff;font-weight:700;font-size:1.1rem;margin-right:16px;">Üüriturg</span>
      <router-link to="/" style="color:#cce;text-decoration:none;font-size:0.95rem;" active-class="nav-active">Dashboard</router-link>
      <router-link to="/listings" style="color:#cce;text-decoration:none;font-size:0.95rem;" active-class="nav-active">Listings</router-link>
      <router-link to="/trends" style="color:#cce;text-decoration:none;font-size:0.95rem;" active-class="nav-active">Trends</router-link>
      <router-link to="/alerts" style="color:#cce;text-decoration:none;font-size:0.95rem;" active-class="nav-active">Alerts</router-link>
    </nav>
    <main style="padding:24px;max-width:1200px;margin:0 auto;">
      <router-view />
    </main>
  </div>
</template>

<style>
.nav-active { color: #fff !important; font-weight: 600; border-bottom: 2px solid #fff; padding-bottom: 2px; }
</style>
