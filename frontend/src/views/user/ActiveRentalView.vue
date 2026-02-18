<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { API_BASE_URL } from '@/config/api'
import { authState } from '@/states/auth'

const router = useRouter()

const rental = ref(null)
const isLoading = ref(true)
const error = ref('')
const isEnding = ref(false)
const showReceipt = ref(false)
const receipt = ref(null)

let costInterval = null
const elapsedMinutes = ref(0)
const currentCost = ref(0)

// Fetch active rental
async function fetchRental() {
  isLoading.value = true
  error.value = ''
  
  try {
    const response = await fetch(`${API_BASE_URL}/rentals/active`, {
      headers: { 'Authorization': `Bearer ${authState.token}` }
    })
    
    if (!response.ok) throw new Error('Failed to fetch')
    
    const data = await response.json()
    
    if (!data.active_rental) {
      // No active rental, check for active booking
      const bookingRes = await fetch(`${API_BASE_URL}/bookings`, {
        headers: { 'Authorization': `Bearer ${authState.token}` }
      })
      const bookingData = await bookingRes.json()
      
      if (bookingData.active_booking) {
        router.push('/booking')
      } else {
        router.push('/')
      }
      return
    }
    
    rental.value = data.active_rental
    elapsedMinutes.value = data.active_rental.duration_minutes
    currentCost.value = data.active_rental.current_cost
    startCostCounter()
  } catch (err) {
    error.value = 'Errore nel caricamento del noleggio'
  } finally {
    isLoading.value = false
  }
}

// Update cost every minute
function startCostCounter() {
  if (costInterval) clearInterval(costInterval)
  
  costInterval = setInterval(() => {
    elapsedMinutes.value++
    currentCost.value = elapsedMinutes.value * rental.value.price_per_minute
  }, 60000) // Every minute
}

// Format elapsed time
const formattedDuration = computed(() => {
  const hours = Math.floor(elapsedMinutes.value / 60)
  const mins = elapsedMinutes.value % 60
  if (hours > 0) {
    return `${hours}h ${mins}m`
  }
  return `${mins} min`
})

// End rental
async function endRental() {
  
  if (!rental.value || !rental.value.id) {
    console.error('No rental or rental ID found')
    alert('Errore: impossibile trovare il noleggio attivo')
    return
  }
  
  if (!confirm(`Confermi la fine del noleggio?\nCosto stimato: €${currentCost.value.toFixed(2)}`)) {
    return
  }
  
  isEnding.value = true
  
  try {
    const response = await fetch(`${API_BASE_URL}/rentals/${rental.value.id}/end`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authState.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        end_location: null // Use vehicle's current location
      })
    })
    
    const data = await response.json()
    
    if (!response.ok) {
      if (data.type === 'invalid_parking') {
        alert('Parcheggia il veicolo in una zona autorizzata')
      } else {
        alert(data.detail || 'Errore durante la terminazione')
      }
      return
    }
    
    // Show receipt
    receipt.value = data.receipt
    showReceipt.value = true
    rental.value = null
    if (costInterval) clearInterval(costInterval)
  } catch (err) {
    console.error('End rental error:', err)
    alert('Errore di connessione')
  } finally {
    isEnding.value = false
  }
}

// Close receipt and go home
function closeReceipt() {
  showReceipt.value = false
  router.push('/')
}

// Format date
function formatDate(dateStr) {
  return new Date(dateStr).toLocaleString('it-IT')
}

onMounted(() => {
  fetchRental()
})

onUnmounted(() => {
  if (costInterval) clearInterval(costInterval)
})
</script>

<template>
  <div class="max-w-lg mx-auto">
    <!-- Loading -->
    <div v-if="isLoading" class="flex justify-center py-12">
      <span class="loading loading-spinner loading-lg"></span>
    </div>
    
    <!-- Error -->
    <div v-else-if="error" class="alert alert-error">
      <span>{{ error }}</span>
    </div>
    
    <!-- Active Rental -->
    <div v-else-if="rental" class="space-y-6">
      <!-- Header -->
      <div class="text-center">
        <h1 class="text-2xl font-bold">🚗 Noleggio Attivo</h1>
        <p class="text-success">Buon viaggio!</p>
      </div>
      
      <!-- Live Stats -->
      <div class="stats stats-vertical lg:stats-horizontal shadow w-full">
        <div class="stat">
          <div class="stat-title">Durata</div>
          <div class="stat-value text-primary">{{ formattedDuration }}</div>
        </div>
        <div class="stat">
          <div class="stat-title">Costo Attuale</div>
          <div class="stat-value text-secondary">€{{ currentCost.toFixed(2) }}</div>
          <div class="stat-desc">€{{ rental.price_per_minute.toFixed(2) }}/min</div>
        </div>
      </div>
      
      <!-- Vehicle Info -->
      <div class="card bg-base-100 shadow-md">
        <div class="card-body">
          <h2 class="card-title">{{ rental.vehicle.model }}</h2>
          <div class="grid grid-cols-2 gap-2 text-sm">
            <div>
              <span class="text-base-content/60">Targa:</span>
              <span class="font-mono font-bold ml-2">{{ rental.vehicle.plate }}</span>
            </div>
            <div>
              <span class="text-base-content/60">Batteria:</span>
              <span class="font-semibold ml-2">{{ rental.vehicle.battery_level }}%</span>
            </div>
            <div class="col-span-2">
              <span class="text-base-content/60">Inizio:</span>
              <span class="ml-2">{{ formatDate(rental.started_at) }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- End Rental Button -->
      <button 
        @click="endRental" 
        class="btn btn-error btn-lg w-full"
        :disabled="isEnding"
      >
        <span v-if="isEnding" class="loading loading-spinner"></span>
        🏁 Termina Noleggio
      </button>
      
      <p class="text-center text-sm text-base-content/60">
        Parcheggia in zona autorizzata prima di terminare
      </p>
    </div>
    
    <!-- Receipt Modal -->
    <div v-if="showReceipt" class="modal modal-open">
      <div class="modal-box">
        <h3 class="font-bold text-lg text-center mb-4">🧾 Ricevuta</h3>
        
        <div v-if="receipt" class="space-y-4">
          <div class="bg-base-200 rounded-lg p-4">
            <div class="text-center mb-4">
              <div class="text-2xl font-bold text-success">€{{ receipt.total_cost.toFixed(2) }}</div>
              <div class="text-sm text-base-content/60">Pagato con {{ receipt.payment_method }}</div>
            </div>
            
            <div class="divider my-2"></div>
            
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span>Veicolo:</span>
                <span class="font-semibold">{{ receipt.vehicle.model }} ({{ receipt.vehicle.plate }})</span>
              </div>
              <div class="flex justify-between">
                <span>Durata:</span>
                <span>{{ receipt.duration_minutes }} minuti</span>
              </div>
              <div class="flex justify-between">
                <span>Tariffa:</span>
                <span>€{{ receipt.price_per_minute.toFixed(2) }}/min</span>
              </div>
              <div class="flex justify-between">
                <span>Inizio:</span>
                <span>{{ formatDate(receipt.started_at) }}</span>
              </div>
              <div class="flex justify-between">
                <span>Fine:</span>
                <span>{{ formatDate(receipt.ended_at) }}</span>
              </div>
            </div>
          </div>
          
          <div class="text-center text-xs text-base-content/60">
            ID Transazione: {{ receipt.charge_id }}<br>
            Parcheggiato in: {{ receipt.parking_zone }}
          </div>
        </div>
        
        <div class="modal-action">
          <button @click="closeReceipt" class="btn btn-primary w-full">
            ✓ Chiudi
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
