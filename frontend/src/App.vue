<script setup>
import { RouterView, RouterLink } from 'vue-router'
import { authState, isAuthenticated, logout, loadFromStorage } from '@/states/auth'
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Load auth state from localStorage on app mount
onMounted(() => {
  loadFromStorage()
})

// Check if user is operator or admin
const isOperator = computed(() => {
  const role = authState.user?.role
  return role === 'operator' || role === 'admin'
})

// Handle logout
function handleLogout() {
  logout()
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-base-200" data-theme="corporate">
    <!-- Navbar -->
    <nav class="navbar bg-primary text-primary-content shadow-lg">
      <div class="flex-1">
        <RouterLink :to="isOperator ? '/operator' : '/'" class="btn btn-ghost text-xl font-bold">
          MOVO
        </RouterLink>
      </div>
      <div class="flex-none gap-2">
        <!-- Not authenticated -->
        <template v-if="!isAuthenticated">
          <RouterLink to="/login" class="btn btn-ghost">Login</RouterLink>
          <RouterLink to="/register" class="btn btn-secondary">Registrati</RouterLink>
        </template>
        
        <!-- Authenticated - show role-based menu -->
        <template v-else>
          <div class="dropdown dropdown-end">
            <div tabindex="0" role="button" class="btn btn-ghost">
              <span v-if="isOperator" class="badge badge-warning badge-sm mr-2">Operatore</span>
              <span class="mr-2">{{ authState.user?.email }}</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </div>
            
            <!-- Operator Menu -->
            <ul v-if="isOperator" tabindex="0" class="dropdown-content menu bg-base-100 rounded-box z-[1001] w-56 p-2 shadow text-base-content">
              <li><RouterLink to="/operator">📊 Dashboard Operatore</RouterLink></li>
              <li><RouterLink to="/operator/fleet">🚗 Gestione Flotta</RouterLink></li>
              <li><RouterLink to="/operator/pending">👥 Utenti in Attesa</RouterLink></li>
              <li><RouterLink to="/operator/reports">📋 Segnalazioni</RouterLink></li>
              <li class="border-t mt-2 pt-2"><a @click="handleLogout" class="text-error cursor-pointer">🚪 Logout</a></li>
            </ul>
            
            <!-- User Menu -->
            <ul v-else tabindex="0" class="dropdown-content menu bg-base-100 rounded-box z-[1001] w-52 p-2 shadow text-base-content">
              <li><RouterLink to="/dashboard">📊 Dashboard</RouterLink></li>
              <li><RouterLink to="/rentals">🚗 I miei noleggi</RouterLink></li>
              <li><RouterLink to="/my-reports">📝 Le mie segnalazioni</RouterLink></li>
              <li class="border-t mt-2 pt-2"><a @click="handleLogout" class="text-error cursor-pointer">🚪 Logout</a></li>
            </ul>
          </div>
        </template>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="container mx-auto p-4">
      <RouterView />
    </main>


  </div>
</template>

