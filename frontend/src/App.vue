<script setup>
import { RouterView, RouterLink } from 'vue-router'
import { authState, isAuthenticated, logout, loadFromStorage } from '@/states/auth'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Load auth state from localStorage on app mount
onMounted(() => {
  loadFromStorage()
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
        <RouterLink to="/" class="btn btn-ghost text-xl font-bold">
          ⚡ MOVO
        </RouterLink>
      </div>
      <div class="flex-none gap-2">
        <!-- Not authenticated -->
        <template v-if="!isAuthenticated">
          <RouterLink to="/login" class="btn btn-ghost">Login</RouterLink>
          <RouterLink to="/register" class="btn btn-secondary">Registrati</RouterLink>
        </template>
        
        <!-- Authenticated -->
        <template v-else>
          <div class="dropdown dropdown-end">
            <div tabindex="0" role="button" class="btn btn-ghost">
              <span class="mr-2">{{ authState.user?.email }}</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </div>
            <ul tabindex="0" class="dropdown-content menu bg-base-100 rounded-box z-[1] w-52 p-2 shadow text-base-content">
              <li><a>Profilo</a></li>
              <li><a>I miei noleggi</a></li>
              <li class="border-t mt-2 pt-2"><a @click="handleLogout" class="text-error">Logout</a></li>
            </ul>
          </div>
        </template>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="container mx-auto p-4">
      <RouterView />
    </main>

    <!-- Footer -->
    <footer class="footer footer-center p-4 bg-base-300 text-base-content mt-8">
      <aside>
        <p>MOVO © 2026 - Car Sharing Elettrico Trento</p>
      </aside>
    </footer>
  </div>
</template>
