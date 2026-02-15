<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { API_BASE_URL } from '@/config/api'
import { authState } from '@/states/auth'

const router = useRouter()

const booking = ref(null)
const isLoading = ref(true)
const error = ref('')
const isUnlocking = ref(false)
const remainingSeconds = ref(0)
let countdownInterval = null

// Fetch active booking
async function fetchBooking() {
  isLoading.value = true
  error.value = ''
  
  try {
    const response = await fetch(`${API_BASE_URL}/bookings`, {
      headers: { 'Authorization': `Bearer ${authState.token}` }
    })
    
    if (!response.ok) throw new Error('Failed to fetch')
    
    const data = await response.json()
    
    if (!data.active_booking) {
      // No active booking, redirect to home
      router.push('/')
      return
    }
    
    booking.value = data.active_booking
    remainingSeconds.value = data.active_booking.remaining_seconds
    startCountdown()
  } catch (err) {
    error.value = 'Errore nel caricamento della prenotazione'
  } finally {
    isLoading.value = false
  }
}

// Countdown timer
function startCountdown() {
  if (countdownInterval) clearInterval(countdownInterval)
  
  countdownInterval = setInterval(() => {
    if (remainingSeconds.value > 0) {
      remainingSeconds.value--
    } else {
      clearInterval(countdownInterval)
      // Booking expired
      booking.value = null
      alert('La prenotazione è scaduta')
      router.push('/')
    }
  }, 1000)
}

// Format time as MM:SS
const formattedTime = computed(() => {
  const minutes = Math.floor(remainingSeconds.value / 60)
  const seconds = remainingSeconds.value % 60
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
})

// Timer progress (0-100)
const timerProgress = computed(() => {
  if (!booking.value) return 0
  const total = booking.value.duration_minutes || 15
  return ((total * 60 - remainingSeconds.value) / (total * 60)) * 100
})

// Unlock vehicle
async function unlockVehicle() {
  isUnlocking.value = true
  
  try {
    // Get user's location (mock for now)
    const userLocation = {
      lat: booking.value.vehicle.location.lat,
      lng: booking.value.vehicle.location.lng
    }
    
    const response = await fetch(`${API_BASE_URL}/rentals/unlock`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authState.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        booking_id: booking.value.id,
        user_location: userLocation
      })
    })
    
    const data = await response.json()
    
    if (!response.ok) {
      if (response.status === 400 && data.type === 'too_far') {
        alert(`Avvicinati al veicolo (distanza: ${data.distance}m)`)
      } else {
        alert(data.detail || 'Errore durante lo sblocco')
      }
      return
    }
    
    // Success - go to active rental
    router.push('/rental')
  } catch (err) {
    alert('Errore di connessione')
  } finally {
    isUnlocking.value = false
  }
}

// Cancel booking
async function cancelBooking() {
  if (!confirm('Sei sicuro di voler cancellare la prenotazione?')) return
  
  try {
    const response = await fetch(`${API_BASE_URL}/bookings/${booking.value.id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${authState.token}` }
    })
    
    if (response.ok) {
      router.push('/')
    } else {
      alert('Errore durante la cancellazione')
    }
  } catch (err) {
    alert('Errore di connessione')
  }
}

onMounted(() => {
  fetchBooking()
})

onUnmounted(() => {
  if (countdownInterval) clearInterval(countdownInterval)
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
    
    <!-- Active Booking -->
    <div v-else-if="booking" class="space-y-6">
      <!-- Header -->
      <div class="text-center">
        <h1 class="text-2xl font-bold">🚗 Prenotazione Attiva</h1>
        <p class="text-base-content/60">Raggiungi il veicolo e sbloccalo</p>
      </div>
      
      <!-- Timer -->
      <div class="card bg-base-100 shadow-xl">
        <div class="card-body items-center">
          <div class="radial-progress text-primary text-4xl" 
               :style="`--value:${100 - timerProgress}; --size:8rem; --thickness:8px;`"
               role="progressbar">
            {{ formattedTime }}
          </div>
          <p class="text-sm text-base-content/60 mt-2">
            Tempo rimanente per raggiungere il veicolo
          </p>
        </div>
      </div>
      
      <!-- Vehicle Info -->
      <div class="card bg-base-100 shadow-md">
        <div class="card-body">
          <h2 class="card-title">{{ booking.vehicle.model }}</h2>
          <div class="grid grid-cols-2 gap-2 text-sm">
            <div>
              <span class="text-base-content/60">Targa:</span>
              <span class="font-mono font-bold ml-2">{{ booking.vehicle.plate }}</span>
            </div>
            <div>
              <span class="text-base-content/60">Batteria:</span>
              <span class="font-semibold ml-2">{{ booking.vehicle.battery_level }}%</span>
            </div>
            <div class="col-span-2">
              <span class="text-base-content/60">📍 Posizione:</span>
              <span class="ml-2">{{ booking.vehicle.location.lat.toFixed(4) }}, {{ booking.vehicle.location.lng.toFixed(4) }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Actions -->
      <div class="flex flex-col gap-3">
        <button 
          @click="unlockVehicle" 
          class="btn btn-primary btn-lg w-full"
          :disabled="isUnlocking"
        >
          <span v-if="isUnlocking" class="loading loading-spinner"></span>
          🔓 Sblocca Veicolo
        </button>
        <button @click="cancelBooking" class="btn btn-outline btn-error w-full">
          ❌ Annulla Prenotazione
        </button>
      </div>
    </div>
    
    <!-- No Booking -->
    <div v-else class="text-center py-12">
      <div class="text-6xl mb-4">🚗</div>
      <h2 class="text-xl font-semibold mb-2">Nessuna prenotazione attiva</h2>
      <router-link to="/" class="btn btn-primary">Trova un Veicolo</router-link>
    </div>
  </div>
</template>
