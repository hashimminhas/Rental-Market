import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/',               name: 'dashboard',     component: () => import('../views/DashboardView.vue') },
  { path: '/listings',       name: 'listings',      component: () => import('../views/ListingsView.vue') },
  { path: '/trends',         name: 'trends',        component: () => import('../views/TrendsView.vue') },
  { path: '/alerts',         name: 'alerts',        component: () => import('../views/AlertsView.vue') },
  { path: '/neighborhoods',  name: 'neighborhoods', component: () => import('../views/NeighborhoodsView.vue') },
  { path: '/landlords',      name: 'landlords',     component: () => import('../views/LandlordsView.vue') },
  { path: '/notifications',  name: 'notifications', component: () => import('../views/NotificationsView.vue') },
  { path: '/system-status',  name: 'system-status', component: () => import('../views/SystemStatusView.vue') },
]

export default createRouter({ history: createWebHistory(import.meta.env.BASE_URL), routes })
