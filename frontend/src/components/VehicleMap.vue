<script setup>
import { ref, onMounted } from 'vue'
import { LMap, LTileLayer, LMarker, LPopup, LIcon } from '@vue-leaflet/vue-leaflet'
import 'leaflet/dist/leaflet.css'
import { API_BASE_URL } from '@/config/api'
import { authState } from '@/states/auth'

// Trento center coordinates
const TRENTO_CENTER = [46.0677, 11.1219]
const DEFAULT_ZOOM = 14

const zoom = ref(DEFAULT_ZOOM)
const center = ref(TRENTO_CENTER)
const vehicles = ref([])
const isLoading = ref(true)
const error = ref('')
const selectedVehicle = ref(null)

// Vehicle type icons (emoji-based for simplicity)
const vehicleIcons = {
  car: '🚗',
  scooter: '🛵',
  bike: '🚲'
}

// Fetch vehicles from API
async function fetchVehicles() {
  isLoading.value = true
  error.value = ''
  
  try {
    const response = await fetch(`${API_BASE_URL}/vehicles`, {
      headers: {
        'Authorization': `Bearer ${authState.token}`
      }
    })
    
    if (!response.ok) {
      throw new Error('Failed to fetch vehicles')
    }
    
    const data = await response.json()
    vehicles.value = data.vehicles
  } catch (err) {
    console.error('Error fetching vehicles:', err)
    error.value = 'Impossibile caricare i veicoli'
  } finally {
    isLoading.value = false
  }
}

// Select vehicle for popup
function selectVehicle(vehicle) {
  selectedVehicle.value = vehicle
}

// Get battery color based on level
function getBatteryColor(level) {
  if (level >= 70) return 'text-success'
  if (level >= 30) return 'text-warning'
  return 'text-error'
}

// Reserve vehicle (placeholder for now)
function reserveVehicle(vehicleId) {
  alert(`Prenotazione veicolo ${vehicleId} - Funzionalità in arrivo!`)
}

onMounted(() => {
  fetchVehicles()
})
</script>

<template>
  <div class="relative w-full h-[500px] rounded-xl overflow-hidden shadow-lg">
    <!-- Loading Overlay -->
    <div v-if="isLoading" class="absolute inset-0 z-[1000] bg-base-100/80 flex items-center justify-center">
      <span class="loading loading-spinner loading-lg text-primary"></span>
    </div>
    
    <!-- Error Message -->
    <div v-if="error" class="absolute top-4 left-4 right-4 z-[1000]">
      <div class="alert alert-error">
        <span>{{ error }}</span>
        <button @click="fetchVehicles" class="btn btn-sm">Riprova</button>
      </div>
    </div>
    
    <!-- Leaflet Map -->
    <LMap
      ref="map"
      v-model:zoom="zoom"
      v-model:center="center"
      :use-global-leaflet="false"
      class="w-full h-full"
    >
      <LTileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        layer-type="base"
        name="OpenStreetMap"
      />
      
      <!-- Vehicle Markers -->
      <LMarker
        v-for="vehicle in vehicles"
        :key="vehicle.id"
        :lat-lng="[vehicle.location.lat, vehicle.location.lng]"
        @click="selectVehicle(vehicle)"
      >
        <LPopup>
          <div class="min-w-[200px] p-2">
            <div class="flex items-center gap-2 mb-2">
              <span class="text-2xl">{{ vehicleIcons[vehicle.type] || '🚗' }}</span>
              <div>
                <h3 class="font-bold text-base">{{ vehicle.model }}</h3>
                <p class="text-xs text-gray-500">{{ vehicle.plate }}</p>
              </div>
            </div>
            
            <div class="space-y-1 text-sm">
              <div class="flex justify-between">
                <span>Batteria:</span>
                <span :class="getBatteryColor(vehicle.battery_level)" class="font-semibold">
                  {{ vehicle.battery_level }}%
                </span>
              </div>
              <div class="flex justify-between">
                <span>Autonomia:</span>
                <span>{{ vehicle.range_km }} km</span>
              </div>
              <div class="flex justify-between">
                <span>Prezzo:</span>
                <span>€{{ vehicle.price_per_minute.toFixed(2) }}/min</span>
              </div>
            </div>
            
            <button
              @click="reserveVehicle(vehicle.id)"
              class="btn btn-primary btn-sm w-full mt-3"
            >
              Prenota
            </button>
          </div>
        </LPopup>
      </LMarker>
    </LMap>
    
    <!-- Legend -->
    <div class="absolute bottom-4 left-4 z-[1000] bg-base-100 rounded-lg p-3 shadow-md">
      <div class="text-xs font-semibold mb-2">Legenda</div>
      <div class="flex gap-3 text-sm">
        <span>🚗 Auto</span>
        <span>🛵 Scooter</span>
        <span>🚲 Bici</span>
      </div>
    </div>
    
    <!-- Vehicle Count -->
    <div class="absolute top-4 right-4 z-[1000] bg-base-100 rounded-lg px-3 py-2 shadow-md">
      <span class="font-semibold">{{ vehicles.length }}</span> veicoli disponibili
    </div>
  </div>
</template>

<style scoped>
/* Fix Leaflet z-index issues with DaisyUI */
:deep(.leaflet-pane) {
  z-index: 1;
}
:deep(.leaflet-control) {
  z-index: 2;
}
</style>
