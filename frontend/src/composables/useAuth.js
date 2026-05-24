import { ref } from 'vue'

const ADMIN_PASSWORD = 'uuriturg2024'
const STORAGE_KEY    = 'uuriturg_admin'

const isAdmin = ref(localStorage.getItem(STORAGE_KEY) === 'true')

export function useAuth() {
  function login(password) {
    if (password === ADMIN_PASSWORD) {
      isAdmin.value = true
      localStorage.setItem(STORAGE_KEY, 'true')
      return true
    }
    return false
  }

  function logout() {
    isAdmin.value = false
    localStorage.removeItem(STORAGE_KEY)
  }

  return { isAdmin, login, logout }
}
