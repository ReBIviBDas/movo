<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { API_BASE_URL } from '@/config/api'
import { authState } from '@/states/auth'

const router = useRouter()

const vehicles = ref([])
const isLoading = ref(true)
const error = ref('')
const filter = ref({ status: '', type: '' })
const showAddModal = ref(false)
const showEditModal = ref(false)
const editingVehicle = ref(null)
const recentActivity = ref([])

// Form data for add/edit
const form = ref({
  plate: '',
  model: '',
  type: 'car',
  battery_level: 100,
  lat: 46.0677,
  lng: 11.1219,
  range_km: 200,
  price_per_minute: 0.35
})

// Fetch fleet vehicles
async function fetchFleet() {
  isLoading.value = true
  error.value = ''
  
  try {
    const params = new URLSearchParams()
    if (filter.value.status) params.append('status', filter.value.status)
    if (filter.value.type) params.append('type', filter.value.type)
    
    const response = await fetch(`${API_BASE_URL}/operator/vehicles?${params}`, {
      headers: {
        'Authorization': `Bearer ${authState.token}`
      }
    })
    
    if (!response.ok) {
      throw new Error('Failed to fetch fleet')
    }
    
    const data = await response.json()
    vehicles.value = data.vehicles
    // Also fetch recent activity
    fetchRecentActivity()
  } catch (err) {
    console.error('Error:', err)
    error.value = 'Errore nel caricamento della flotta'
  } finally {
    isLoading.value = false
  }
}

// Add new vehicle
async function addVehicle() {
  try {
    const response = await fetch(`${API_BASE_URL}/operator/vehicles`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authState.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(form.value)
    })
    
    if (!response.ok) {
      const data = await response.json()
      alert(data.detail || 'Errore durante l\'aggiunta')
      return
    }
    
    alert('Veicolo aggiunto con successo!')
    showAddModal.value = false
    resetForm()
    fetchFleet()
  } catch (err) {
    console.error('Error:', err)
    alert('Errore di connessione')
  }
}

// Fetch recent activity
async function fetchRecentActivity() {
  try {
    const response = await fetch(`${API_BASE_URL}/operator/vehicles/audit?limit=5`, {
      headers: {
        'Authorization': `Bearer ${authState.token}`
      }
    })
    
    if (response.ok) {
      const data = await response.json()
      recentActivity.value = data.logs
    }
  } catch (err) {
    console.error('Error fetching activity:', err)
  }
}

// Update vehicle
async function updateVehicle() {
  try {
    const response = await fetch(`${API_BASE_URL}/operator/vehicles/${editingVehicle.value.id}`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${authState.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        status: form.value.status,
        battery_level: form.value.battery_level,
        lat: form.value.lat,
        lng: form.value.lng
      })
    })
    
    if (!response.ok) {
      const data = await response.json()
      alert(data.detail || 'Errore durante l\'aggiornamento')
      return
    }
    
    alert('Veicolo aggiornato!')
    showEditModal.value = false
    resetForm()
    fetchFleet()
  } catch (err) {
    console.error('Error:', err)
    alert('Errore di connessione')
  }
}

// Delete vehicle
async function deleteVehicle(id) {
  if (!confirm('Sei sicuro di voler eliminare questo veicolo?')) return
  
  try {
    const response = await fetch(`${API_BASE_URL}/operator/vehicles/${id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${authState.token}`
      }
    })
    
    if (!response.ok) {
      const data = await response.json()
      alert(data.detail || 'Errore durante l\'eliminazione')
      return
    }
    
    alert('Veicolo eliminato!')
    fetchFleet()
  } catch (err) {
    console.error('Error:', err)
    alert('Errore di connessione')
  }
}

// Open edit modal with vehicle data
function openEditModal(vehicle) {
  editingVehicle.value = vehicle
  form.value = {
    status: vehicle.status,
    battery_level: vehicle.battery_level,
    lat: vehicle.location.lat,
    lng: vehicle.location.lng
  }
  showEditModal.value = true
}

// Reset form
function resetForm() {
  form.value = {
    plate: '',
    model: '',
    type: 'car',
    battery_level: 100,
    lat: 46.0677,
    lng: 11.1219,
    range_km: 200,
    price_per_minute: 0.35
  }
  editingVehicle.value = null
}

// Get status badge class
function getStatusBadge(status) {
  const badges = {
    available: 'badge-success',
    rented: 'badge-warning',
    maintenance: 'badge-error',
    charging: 'badge-info'
  }
  return badges[status] || 'badge-ghost'
}

// Get battery color
function getBatteryColor(level) {
  if (level >= 70) return 'text-success'
  if (level >= 30) return 'text-warning'
  return 'text-error'
}

// Format date
function formatDate(dateStr) {
  if (!dateStr) return 'N/A'
  return new Date(dateStr).toLocaleString('it-IT')
}

// Format relative time
function formatRelativeTime(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  if (minutes < 60) return `${minutes} min fa`
  if (hours < 24) return `${hours} ore fa`
  return new Date(dateStr).toLocaleDateString('it-IT')
}

// Get action label
function getActionLabel(action) {
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

// Navigate to vehicle detail
function goToVehicle(id) {
  router.push(`/operator/fleet/${id}`)
}

onMounted(() => {
  fetchFleet()
})
</script>

<template>
  <div>
    <!-- Header -->
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 gap-4">
      <div>
        <h1 class="text-2xl font-bold">Gestione Flotta</h1>
        <p class="text-base-content/60">Gestisci tutti i veicoli della flotta MOVO</p>
      </div>
      <div class="flex gap-2">
        <RouterLink to="/operator" class="btn btn-ghost btn-sm">← Dashboard</RouterLink>
        <button @click="showAddModal = true" class="btn btn-primary btn-sm">
          + Aggiungi Veicolo
        </button>
      </div>
    </div>
    
    <!-- Filters -->
    <div class="flex flex-wrap gap-4 mb-6">
      <select v-model="filter.status" @change="fetchFleet" class="select select-bordered select-sm">
        <option value="">Tutti gli stati</option>
        <option value="available">Disponibile</option>
        <option value="rented">Noleggiato</option>
        <option value="maintenance">Manutenzione</option>
        <option value="charging">In carica</option>
      </select>
      <select v-model="filter.type" @change="fetchFleet" class="select select-bordered select-sm">
        <option value="">Tutti i tipi</option>
        <option value="car">Auto</option>
      </select>
      <button @click="fetchFleet" class="btn btn-outline btn-sm">🔄 Aggiorna</button>
    </div>
    
    <!-- Loading -->
    <div v-if="isLoading" class="flex justify-center py-12">
      <span class="loading loading-spinner loading-lg"></span>
    </div>
    
    <!-- Error -->
    <div v-else-if="error" class="alert alert-error">
      <span>{{ error }}</span>
      <button @click="fetchFleet" class="btn btn-sm">Riprova</button>
    </div>
    
    <!-- Empty -->
    <div v-else-if="vehicles.length === 0" class="text-center py-12">
      <div class="text-6xl mb-4">🚗</div>
      <h2 class="text-xl font-semibold mb-2">Nessun veicolo trovato</h2>
      <p class="text-base-content/60">Aggiungi il primo veicolo alla flotta</p>
    </div>
    
    <!-- Vehicles Table -->
    <div v-else class="overflow-x-auto">
      <table class="table table-zebra bg-base-100 shadow-md">
        <thead>
          <tr>
            <th>Targa</th>
            <th>Modello</th>
            <th>Tipo</th>
            <th>Stato</th>
            <th>Batteria</th>
            <th>Ultimo Agg.</th>
            <th>Azioni</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="vehicle in vehicles" :key="vehicle.id" class="hover cursor-pointer" @click="goToVehicle(vehicle.id)">
            <td class="font-mono font-bold text-primary">{{ vehicle.plate }}</td>
            <td>{{ vehicle.model }}</td>
            <td class="capitalize">🚗 Auto</td>
            <td>
              <span class="badge" :class="getStatusBadge(vehicle.status)">
                {{ vehicle.status }}
              </span>
            </td>
            <td>
              <span :class="getBatteryColor(vehicle.battery_level)" class="font-semibold">
                {{ vehicle.battery_level }}%
              </span>
            </td>
            <td class="text-sm">{{ formatDate(vehicle.last_updated) }}</td>
            <td>
              <div class="flex gap-1">
                <button @click="openEditModal(vehicle)" class="btn btn-xs btn-outline">
                  ✏️ Modifica
                </button>
                <button @click="deleteVehicle(vehicle.id)" class="btn btn-xs btn-error btn-outline">
                  🗑️
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- Recent Activity -->
    <div v-if="recentActivity.length > 0" class="mt-6">
      <div class="card bg-base-100 shadow-md">
        <div class="card-body">
          <h2 class="card-title text-lg">📋 Attività Recente</h2>
          <div class="overflow-x-auto">
            <table class="table table-sm">
              <thead>
                <tr>
                  <th>Quando</th>
                  <th>Veicolo</th>
                  <th>Azione</th>
                  <th>Operatore</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="log in recentActivity" :key="log.id">
                  <td class="text-sm">{{ formatRelativeTime(log.timestamp) }}</td>
                  <td class="font-mono">{{ log.vehicle_plate }}</td>
                  <td>{{ getActionLabel(log.action) }}</td>
                  <td class="text-sm">{{ log.performed_by }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Add Vehicle Modal -->
    <div v-if="showAddModal" class="modal modal-open">
      <div class="modal-box">
        <h3 class="font-bold text-lg mb-4">Aggiungi Veicolo</h3>
        <form @submit.prevent="addVehicle" class="space-y-4">
          <div class="form-control">
            <label class="label"><span class="label-text">Targa *</span></label>
            <input v-model="form.plate" type="text" class="input input-bordered" required placeholder="TN123AB" />
          </div>
          <div class="form-control">
            <label class="label"><span class="label-text">Modello *</span></label>
            <input v-model="form.model" type="text" class="input input-bordered" required placeholder="Fiat 500e" />
          </div>
          <div class="form-control">
            <label class="label"><span class="label-text">Tipo</span></label>
            <select v-model="form.type" class="select select-bordered" disabled>
              <option value="car">Auto</option>
            </select>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div class="form-control">
              <label class="label"><span class="label-text">Latitudine</span></label>
              <input v-model.number="form.lat" type="number" step="0.0001" class="input input-bordered" />
            </div>
            <div class="form-control">
              <label class="label"><span class="label-text">Longitudine</span></label>
              <input v-model.number="form.lng" type="number" step="0.0001" class="input input-bordered" />
            </div>
          </div>
          <div class="modal-action">
            <button type="button" @click="showAddModal = false; resetForm()" class="btn">Annulla</button>
            <button type="submit" class="btn btn-primary">Aggiungi</button>
          </div>
        </form>
      </div>
    </div>
    
    <!-- Edit Vehicle Modal -->
    <div v-if="showEditModal" class="modal modal-open">
      <div class="modal-box">
        <h3 class="font-bold text-lg mb-4">Modifica Veicolo: {{ editingVehicle?.plate }}</h3>
        <form @submit.prevent="updateVehicle" class="space-y-4">
          <div class="form-control">
            <label class="label"><span class="label-text">Stato</span></label>
            <select v-model="form.status" class="select select-bordered">
              <option value="available">Disponibile</option>
              <option value="rented">Noleggiato</option>
              <option value="maintenance">Manutenzione</option>
              <option value="charging">In carica</option>
            </select>
          </div>
          <div class="form-control">
            <label class="label"><span class="label-text">Batteria (%)</span></label>
            <input v-model.number="form.battery_level" type="number" min="0" max="100" class="input input-bordered" />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div class="form-control">
              <label class="label"><span class="label-text">Latitudine</span></label>
              <input v-model.number="form.lat" type="number" step="0.0001" class="input input-bordered" />
            </div>
            <div class="form-control">
              <label class="label"><span class="label-text">Longitudine</span></label>
              <input v-model.number="form.lng" type="number" step="0.0001" class="input input-bordered" />
            </div>
          </div>
          <div class="modal-action">
            <button type="button" @click="showEditModal = false; resetForm()" class="btn">Annulla</button>
            <button type="submit" class="btn btn-primary">Salva</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
