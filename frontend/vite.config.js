import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api/scraper': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/api\/scraper/, '')
      },
      '/api/analytics': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/api\/analytics/, '')
      },
      '/api/users': {
        target: 'http://localhost:8083',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/api\/users/, '')
      },
      '/api/alerts': {
        target: 'http://localhost:8084',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/api\/alerts/, '')
      },
      '/api/neighborhoods': {
        target: 'http://localhost:8085',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/api\/neighborhoods/, '')
      },
      '/api/landlords': {
        target: 'http://localhost:8086',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/api\/landlords/, '')
      },
      '/api/managed-listings': {
        target: 'http://localhost:8087',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/api\/managed-listings/, '')
      },
      '/api/notifications': {
        target: 'http://localhost:8088',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/api\/notifications/, '')
      }
    }
  }
})
