<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50">
    <!-- Header -->
    <div class="bg-white shadow-sm border-b">
      <div class="max-w-6xl mx-auto px-4 py-4">
        <div class="flex items-center gap-3">
          <router-link to="/dashboard" class="btn btn-ghost btn-sm">
            ← Dashboard
          </router-link>
          <h1 class="text-2xl font-bold text-gray-800">🚗 I Miei Noleggi</h1>
        </div>
      </div>
    </div>

    <div class="max-w-6xl mx-auto px-4 py-8">
      <!-- Loading -->
      <div v-if="loading" class="flex justify-center py-12">
        <span class="loading loading-spinner loading-lg"></span>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="alert alert-error">
        <span>{{ error }}</span>
        <button @click="fetchRentals" class="btn btn-sm">Riprova</button>
      </div>

      <!-- Empty State -->
      <div v-else-if="rentals.length === 0" class="card bg-white shadow-xl">
        <div class="card-body text-center py-12">
          <div class="text-6xl mb-4">🚗</div>
          <h2 class="text-2xl font-bold mb-2">Nessun noleggio</h2>
          <p class="text-gray-600 mb-4">Non hai ancora completato alcun noleggio.</p>
          <router-link to="/" class="btn btn-primary">
            Trova un veicolo
          </router-link>
        </div>
      </div>

      <!-- Rental History Table -->
      <div v-else class="card bg-white shadow-xl">
        <div class="card-body">
          <h2 class="card-title mb-4">Storico Noleggi</h2>
          
          <div class="overflow-x-auto">
            <table class="table table-zebra">
              <thead>
                <tr>
                  <th>Veicolo</th>
                  <th>Data</th>
                  <th>Durata</th>
                  <th>Costo</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="rental in rentals" :key="rental.id">
                  <td>
                    <div class="font-semibold">{{ rental.vehicle?.model || 'N/D' }}</div>
                    <div class="text-sm text-gray-500">{{ rental.vehicle?.plate || '' }}</div>
                  </td>
                  <td>
                    <div>{{ formatDate(rental.started_at) }}</div>
                    <div class="text-sm text-gray-500">{{ formatTime(rental.started_at) }} - {{ formatTime(rental.ended_at) }}</div>
                  </td>
                  <td>{{ rental.duration_minutes }} min</td>
                  <td class="font-semibold text-primary">€{{ (rental.total_cost || 0).toFixed(2) }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Pagination -->
          <div v-if="pagination.pages > 1" class="flex justify-center gap-2 mt-6">
            <button 
              @click="currentPage--" 
              :disabled="currentPage === 1"
              class="btn btn-sm btn-outline"
            >
              ← Precedente
            </button>
            <span class="btn btn-sm btn-ghost">
              Pagina {{ currentPage }} di {{ pagination.pages }}
            </span>
            <button 
              @click="currentPage++" 
              :disabled="currentPage >= pagination.pages"
              class="btn btn-sm btn-outline"
            >
              Successiva →
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { authState } from '@/states/auth'
import { API_BASE_URL } from '@/config/api'

const rentals = ref([])
const loading = ref(true)
const error = ref('')
const currentPage = ref(1)
const pagination = ref({ page: 1, limit: 10, total: 0, pages: 1 })

async function fetchRentals() {
  loading.value = true
  error.value = ''
  
  try {
    const response = await fetch(
      `${API_BASE_URL}/rentals/history?page=${currentPage.value}&limit=10`,
      {
        headers: { 'Authorization': `Bearer ${authState.token}` }
      }
    )
    
    if (!response.ok) throw new Error('Failed to fetch rentals')
    
    const data = await response.json()
    rentals.value = data.rentals || []
    pagination.value = data.pagination || { page: 1, limit: 10, total: 0, pages: 1 }
  } catch (err) {
    console.error('Error fetching rentals:', err)
    error.value = 'Errore nel caricamento dello storico noleggi'
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('it-IT', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleTimeString('it-IT', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

watch(currentPage, () => {
  fetchRentals()
})

onMounted(() => {
  fetchRentals()
})
</script>
