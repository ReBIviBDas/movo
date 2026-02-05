<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { API_BASE_URL } from '@/config/api'
import { authState } from '@/states/auth'

const route = useRoute()
const router = useRouter()

const vehicle = ref(null)
const auditLogs = ref([])
const rentalHistory = ref([])
const isLoading = ref(true)
const error = ref('')

// Maintenance modal
const showMaintenanceModal = ref(false)
const maintenanceAction = ref('start')
const maintenanceNotes = ref('')
const isSubmitting = ref(false)

// Fetch vehicle details
const fetchVehicleDetails = async () => {
  try {
    isLoading.value = true
    error.value = ''
    
    const response = await fetch(`${API_BASE_URL}/fleet/${route.params.id}`, {
      headers: {
        'Authorization': `Bearer ${authState.token}`
      }
    })
    
    if (!response.ok) {
      throw new Error('Failed to fetch vehicle details')
    }
    
    const data = await response.json()
    vehicle.value = data.vehicle
    auditLogs.value = data.audit_logs
    rentalHistory.value = data.rental_history
  } catch (err) {
    error.value = err.message
  } finally {
    isLoading.value = false
  }
}

// Handle maintenance action
const handleMaintenance = async () => {
  try {
    isSubmitting.value = true
    
    const response = await fetch(`${API_BASE_URL}/fleet/${route.params.id}/maintenance`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authState.token}`
      },
      body: JSON.stringify({
        action: maintenanceAction.value,
        notes: maintenanceNotes.value || undefined
      })
    })
    
    if (!response.ok) {
      const err = await response.json()
      throw new Error(err.detail || 'Failed to update maintenance status')
    }
    
    showMaintenanceModal.value = false
    maintenanceNotes.value = ''
    await fetchVehicleDetails()
  } catch (err) {
    error.value = err.message
  } finally {
    isSubmitting.value = false
  }
}

// Format date
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('it-IT')
}

// Format relative time
const formatRelativeTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 60) return `${minutes} min fa`
  if (hours < 24) return `${hours} ore fa`
  return `${days} giorni fa`
}

// Get action badge class
const getActionBadge = (action) => {
  const badges = {
    created: 'badge-success',
    updated: 'badge-info',
    deleted: 'badge-error',
    status_changed: 'badge-warning',
    maintenance_started: 'badge-warning',
    maintenance_completed: 'badge-success'
  }
  return badges[action] || 'badge-ghost'
}

// Get action label
const getActionLabel = (action) => {
  const labels = {
    created: 'Creato',
    updated: 'Aggiornato',
    deleted: 'Eliminato',
    status_changed: 'Stato cambiato',
    maintenance_started: 'Manutenzione iniziata',
    maintenance_completed: 'Manutenzione completata'
  }
  return labels[action] || action
}

// Get status badge class
const getStatusBadge = (status) => {
  const badges = {
    available: 'badge-success',
    booked: 'badge-warning',
    rented: 'badge-info',
    maintenance: 'badge-error',
    charging: 'badge-primary'
  }
  return badges[status] || 'badge-ghost'
}

// Get battery color
const getBatteryColor = (level) => {
  if (level >= 60) return 'text-success'
  if (level >= 30) return 'text-warning'
  return 'text-error'
}

// Computed: Can start maintenance
const canStartMaintenance = computed(() => {
  return vehicle.value && vehicle.value.status !== 'rented' && vehicle.value.status !== 'maintenance'
})

// Computed: Can end maintenance
const canEndMaintenance = computed(() => {
  return vehicle.value && vehicle.value.status === 'maintenance'
})

onMounted(() => {
  fetchVehicleDetails()
})
</script>

<template>
  <div>
    <!-- Header -->
    <div class="flex items-center gap-4 mb-6">
      <RouterLink to="/operator/fleet" class="btn btn-ghost btn-sm">
        ← Torna alla Flotta
      </RouterLink>
      <h1 class="text-2xl font-bold" v-if="vehicle">
        {{ vehicle.plate }} - {{ vehicle.model }}
      </h1>
    </div>
    
    <!-- Loading -->
    <div v-if="isLoading" class="flex justify-center py-12">
      <span class="loading loading-spinner loading-lg"></span>
    </div>
    
    <!-- Error -->
    <div v-else-if="error" class="alert alert-error">
      <span>{{ error }}</span>
      <button @click="fetchVehicleDetails" class="btn btn-sm">Riprova</button>
    </div>
    
    <!-- Vehicle Details -->
    <div v-else-if="vehicle" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Vehicle Info Card -->
      <div class="lg:col-span-1">
        <div class="card bg-base-100 shadow-md">
          <div class="card-body">
            <h2 class="card-title">Informazioni Veicolo</h2>
            
            <div class="space-y-3 mt-4">
              <div class="flex justify-between">
                <span class="text-base-content/60">Targa</span>
                <span class="font-mono font-bold">{{ vehicle.plate }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-base-content/60">Modello</span>
                <span>{{ vehicle.model }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-base-content/60">Stato</span>
                <span class="badge" :class="getStatusBadge(vehicle.status)">
                  {{ vehicle.status }}
                </span>
              </div>
              <div class="flex justify-between">
                <span class="text-base-content/60">Batteria</span>
                <span :class="getBatteryColor(vehicle.battery_level)" class="font-bold">
                  🔋 {{ vehicle.battery_level }}%
                </span>
              </div>
              <div class="flex justify-between">
                <span class="text-base-content/60">Autonomia</span>
                <span>{{ vehicle.range_km }} km</span>
              </div>
              <div class="flex justify-between">
                <span class="text-base-content/60">Prezzo</span>
                <span>€{{ vehicle.price_per_minute.toFixed(2) }}/min</span>
              </div>
              <div class="flex justify-between">
                <span class="text-base-content/60">Posizione</span>
                <span class="text-sm">{{ vehicle.location.lat.toFixed(4) }}, {{ vehicle.location.lng.toFixed(4) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-base-content/60">Ultimo aggiornamento</span>
                <span class="text-sm">{{ formatRelativeTime(vehicle.last_updated) }}</span>
              </div>
            </div>
            
            <!-- Quick Actions -->
            <div class="divider">Azioni Rapide</div>
            <div class="flex flex-col gap-2">
              <button 
                v-if="canStartMaintenance"
                @click="maintenanceAction = 'start'; showMaintenanceModal = true" 
                class="btn btn-warning btn-sm"
              >
                🔧 Inizia Manutenzione
              </button>
              <button 
                v-if="canEndMaintenance"
                @click="maintenanceAction = 'end'; showMaintenanceModal = true" 
                class="btn btn-success btn-sm"
              >
                ✅ Termina Manutenzione
              </button>
              <RouterLink 
                :to="`/operator/fleet`" 
                class="btn btn-outline btn-sm"
              >
                ✏️ Modifica Veicolo
              </RouterLink>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Audit Log & Rental History -->
      <div class="lg:col-span-2 space-y-6">
        <!-- Audit Log Timeline -->
        <div class="card bg-base-100 shadow-md">
          <div class="card-body">
            <h2 class="card-title">📋 Cronologia Operazioni</h2>
            
            <div v-if="auditLogs.length === 0" class="text-center py-6 text-base-content/60">
              Nessuna operazione registrata
            </div>
            
            <div v-else class="overflow-x-auto">
              <table class="table table-sm">
                <thead>
                  <tr>
                    <th>Data</th>
                    <th>Azione</th>
                    <th>Operatore</th>
                    <th>Dettagli</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="log in auditLogs" :key="log.id">
                    <td class="text-sm">{{ formatDate(log.timestamp) }}</td>
                    <td>
                      <span class="badge badge-sm" :class="getActionBadge(log.action)">
                        {{ getActionLabel(log.action) }}
                      </span>
                    </td>
                    <td class="text-sm">{{ log.performed_by }}</td>
                    <td class="text-sm">
                      <template v-if="log.notes">{{ log.notes }}</template>
                      <template v-else-if="log.changes">
                        <span v-for="(change, field) in log.changes" :key="field" class="block">
                          {{ field }}: {{ change.old }} → {{ change.new }}
                        </span>
                      </template>
                      <span v-else class="text-base-content/40">-</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
        
        <!-- Rental History -->
        <div class="card bg-base-100 shadow-md">
          <div class="card-body">
            <h2 class="card-title">🚗 Storico Noleggi</h2>
            
            <div v-if="rentalHistory.length === 0" class="text-center py-6 text-base-content/60">
              Nessun noleggio registrato
            </div>
            
            <div v-else class="overflow-x-auto">
              <table class="table table-sm">
                <thead>
                  <tr>
                    <th>Data Inizio</th>
                    <th>Utente</th>
                    <th>Stato</th>
                    <th>Durata</th>
                    <th>Costo</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="rental in rentalHistory" :key="rental.id">
                    <td class="text-sm">{{ formatDate(rental.started_at) }}</td>
                    <td class="text-sm">{{ rental.user }}</td>
                    <td>
                      <span class="badge badge-sm" :class="rental.status === 'completed' ? 'badge-success' : 'badge-info'">
                        {{ rental.status }}
                      </span>
                    </td>
                    <td class="text-sm">
                      <template v-if="rental.ended_at">
                        {{ Math.round((new Date(rental.ended_at) - new Date(rental.started_at)) / 60000) }} min
                      </template>
                      <template v-else>In corso</template>
                    </td>
                    <td class="text-sm font-mono">
                      {{ rental.total_cost ? `€${rental.total_cost.toFixed(2)}` : '-' }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Maintenance Modal -->
    <div v-if="showMaintenanceModal" class="modal modal-open">
      <div class="modal-box">
        <h3 class="font-bold text-lg mb-4">
          {{ maintenanceAction === 'start' ? '🔧 Inizia Manutenzione' : '✅ Termina Manutenzione' }}
        </h3>
        
        <div class="form-control">
          <label class="label">
            <span class="label-text">Note (opzionale)</span>
          </label>
          <textarea 
            v-model="maintenanceNotes" 
            class="textarea textarea-bordered" 
            placeholder="Descrivi il motivo della manutenzione..."
            rows="3"
          ></textarea>
        </div>
        
        <div class="modal-action">
          <button 
            @click="showMaintenanceModal = false" 
            class="btn"
            :disabled="isSubmitting"
          >
            Annulla
          </button>
          <button 
            @click="handleMaintenance" 
            class="btn"
            :class="maintenanceAction === 'start' ? 'btn-warning' : 'btn-success'"
            :disabled="isSubmitting"
          >
            <span v-if="isSubmitting" class="loading loading-spinner loading-sm"></span>
            {{ maintenanceAction === 'start' ? 'Inizia Manutenzione' : 'Termina Manutenzione' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

