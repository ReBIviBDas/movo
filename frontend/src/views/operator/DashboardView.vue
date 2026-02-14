<script setup>
import { RouterLink } from 'vue-router'
import { authState } from '@/states/auth'
import { ref, onMounted } from 'vue'
import { API_BASE_URL } from '@/config/api'

const pendingCount = ref(0)
const isLoading = ref(true)

// Fetch pending users count on mount
onMounted(async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/users/pending`, {
      headers: {
        'Authorization': `Bearer ${authState.token}`
      }
    })
    if (response.ok) {
      const data = await response.json()
      pendingCount.value = data.count
    }
  } catch (err) {
    console.error('Error fetching pending users:', err)
  } finally {
    isLoading.value = false
  }
})
</script>

<template>
  <div class="min-h-[70vh]">
    <!-- Welcome Section -->
    <div class="hero bg-primary text-primary-content rounded-box shadow-lg mb-6">
      <div class="hero-content text-center py-8">
        <div>
          <h1 class="text-3xl font-bold">
            Dashboard Operatore 🛠️
          </h1>
        </div>
      </div>
    </div>



    <!-- Quick Actions -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <!-- Pending Approvals -->
      <div class="card bg-base-100 shadow-md hover:shadow-xl transition-shadow border-l-4 border-warning">
        <div class="card-body">
          <div class="text-4xl mb-2">👤</div>
          <h2 class="card-title">Approvazioni Utenti</h2>
          <p>Verifica documenti e approva nuovi utenti</p>
          <div class="card-actions justify-end mt-4">
            <RouterLink to="/operator/pending" class="btn btn-warning">
              <span v-if="pendingCount > 0" class="badge badge-sm">{{ pendingCount }}</span>
              Gestisci
            </RouterLink>
          </div>
        </div>
      </div>

      <!-- Fleet Management -->
      <div class="card bg-base-100 shadow-md hover:shadow-xl transition-shadow border-l-4 border-primary">
        <div class="card-body">
          <div class="text-4xl mb-2">🚗</div>
          <h2 class="card-title">Gestione Flotta</h2>
          <p>Visualizza e gestisci i veicoli</p>
          <div class="card-actions justify-end mt-4">
            <RouterLink to="/operator/fleet" class="btn btn-primary">
              Apri Flotta
            </RouterLink>
          </div>
        </div>
      </div>

      <!-- User Management -->
      <div class="card bg-base-100 shadow-md hover:shadow-xl transition-shadow border-l-4 border-secondary">
        <div class="card-body">
          <div class="text-4xl mb-2">👥</div>
          <h2 class="card-title">Gestione Utenti</h2>
          <p>Cerca, visualizza e gestisci utenti</p>
          <div class="card-actions justify-end mt-4">
            <RouterLink to="/operator/users" class="btn btn-secondary">
              Vedi Utenti
            </RouterLink>
          </div>
        </div>
      </div>

      <!-- Reports Management -->
      <div class="card bg-base-100 shadow-md hover:shadow-xl transition-shadow border-l-4 border-error">
        <div class="card-body">
          <div class="text-4xl mb-2">🎫</div>
          <h2 class="card-title">Segnalazioni</h2>
          <p>Gestisci le segnalazioni degli utenti</p>
          <div class="card-actions justify-end mt-4">
            <RouterLink to="/operator/reports" class="btn btn-error">
              Gestisci
            </RouterLink>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
