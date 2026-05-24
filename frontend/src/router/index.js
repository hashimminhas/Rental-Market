import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/',          name: 'home',          component: () => import('../views/LandingView.vue') },
  { path: '/listings',  name: 'listings',      component: () => import('../views/ListingsView.vue') },
  { path: '/insights',  name: 'insights',      component: () => import('../views/TrendsView.vue') },
  { path: '/alerts',    name: 'alerts',        component: () => import('../views/AlertsView.vue') },
  { path: '/login',     name: 'login',         component: () => import('../views/LoginView.vue') },

  // legacy redirects
  { path: '/trends',        redirect: '/insights' },
  { path: '/neighborhoods', redirect: '/insights' },
  { path: '/map',           redirect: '/' },

  // admin
  { path: '/dashboard',     name: 'dashboard',     component: () => import('../views/DashboardView.vue'),    meta: { admin: true } },
  { path: '/system-status', name: 'system-status', component: () => import('../views/SystemStatusView.vue'), meta: { admin: true } },
  { path: '/landlords',     name: 'landlords',     component: () => import('../views/LandlordsView.vue'),    meta: { admin: true } },
  { path: '/notifications', name: 'notifications', component: () => import('../views/NotificationsView.vue'),meta: { admin: true } },
]

const router = createRouter({ history: createWebHistory(import.meta.env.BASE_URL), routes })

router.beforeEach((to) => {
  if (to.meta.admin && localStorage.getItem('uuriturg_admin') !== 'true') {
    return { name: 'login' }
  }
})

export default router
