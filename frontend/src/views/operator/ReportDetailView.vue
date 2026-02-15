<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-100 via-white to-blue-50">
    <!-- Header -->
    <div class="bg-white shadow-sm border-b">
      <div class="max-w-5xl mx-auto px-4 py-4">
        <div class="flex items-center gap-3">
          <router-link to="/operator/reports" class="btn btn-ghost btn-sm">
            ← Tutte le Segnalazioni
          </router-link>
          <h1 class="text-2xl font-bold text-gray-800" v-if="report">
            {{ getCategoryIcon(report.category) }} {{ report.reference_id }}
          </h1>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex justify-center py-16">
      <span class="loading loading-spinner loading-lg text-primary"></span>
    </div>

    <div v-else-if="report" class="max-w-5xl mx-auto px-4 py-6">
      <div class="grid grid-cols-3 gap-6">
        <!-- Main Content -->
        <div class="col-span-2 space-y-6">
          <!-- Report Info Card -->
          <div class="card bg-white shadow-lg">
            <div class="card-body">
              <div class="flex justify-between items-start mb-4">
                <div>
                  <h2 class="text-xl font-bold">{{ report.category_label }}</h2>
                  <p class="text-gray-500">Segnalazione di {{ report.user?.name }}</p>
                </div>
                <div class="flex gap-2">
                  <span :class="['badge badge-lg', getStatusBadgeClass(report.status)]">
                    {{ getStatusLabel(report.status) }}
                  </span>
                  <span :class="['badge badge-lg', getPriorityBadgeClass(report.priority)]">
                    {{ getPriorityLabel(report.priority) }}
                  </span>
                </div>
              </div>
              
              <!-- Description -->
              <div class="bg-gray-50 p-4 rounded-xl mb-4">
                <p class="whitespace-pre-wrap">{{ report.description }}</p>
              </div>
              
              <!-- Photos -->
              <div v-if="report.photos?.length > 0" class="mb-4">
                <h3 class="font-semibold mb-3">📸 Foto allegate</h3>
                <div class="flex gap-3 flex-wrap">
                  <img 
                    v-for="(photo, i) in report.photos" 
                    :key="i"
                    :src="getPhotoUrl(photo)"
                    class="w-32 h-32 object-cover rounded-xl cursor-pointer hover:opacity-80 transition-opacity shadow-md"
                    @click="openFullPhoto(photo)"
                  />
                </div>
              </div>
              
              <!-- Location Map Placeholder -->
              <div v-if="report.location" class="mb-4">
                <h3 class="font-semibold mb-3">📍 Posizione</h3>
                <div class="bg-gray-200 h-40 rounded-xl flex items-center justify-center">
                  <span class="text-gray-500">
                    {{ report.location.lat.toFixed(5) }}, {{ report.location.lng.toFixed(5) }}
                  </span>
                </div>
              </div>
              
              <!-- Resolution (if resolved) -->
              <div v-if="report.resolution" class="bg-green-50 p-4 rounded-xl border border-green-200">
                <h3 class="font-semibold text-green-700 mb-2">✅ Risoluzione</h3>
                <p class="text-green-800">{{ report.resolution }}</p>
                <p class="text-sm text-green-600 mt-2">
                  Risolto il {{ formatDateTime(report.resolved_at) }}
                </p>
              </div>
            </div>
          </div>

          <!-- Operator Notes -->
          <div class="card bg-white shadow-lg">
            <div class="card-body">
              <h3 class="font-bold text-lg mb-3">📝 Note Operatore</h3>
              <textarea 
                v-model="operatorNotes"
                class="textarea textarea-bordered w-full h-32"
                placeholder="Note interne per gli operatori..."
              ></textarea>
              <div class="flex justify-end mt-3">
                <button @click="saveNotes" class="btn btn-sm btn-primary" :disabled="saving">
                  <span v-if="saving" class="loading loading-spinner loading-xs"></span>
                  Salva Note
                </button>
              </div>
            </div>
          </div>

          <!-- Resolution Form -->
          <div v-if="report.status !== 'resolved' && report.status !== 'closed'" class="card bg-white shadow-lg">
            <div class="card-body">
              <h3 class="font-bold text-lg mb-3">🏁 Risolvi Segnalazione</h3>
              <textarea 
                v-model="resolutionText"
                class="textarea textarea-bordered w-full h-32"
                placeholder="Descrivi come è stato risolto il problema..."
              ></textarea>
              <div class="flex justify-end mt-3">
                <button 
                  @click="resolveReport" 
                  class="btn btn-success"
                  :disabled="!resolutionText.trim() || resolving"
                >
                  <span v-if="resolving" class="loading loading-spinner loading-xs"></span>
                  ✅ Segna come Risolto
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Sidebar -->
        <div class="space-y-6">
          <!-- Details Card -->
          <div class="card bg-white shadow-lg">
            <div class="card-body">
              <h3 class="font-bold text-lg mb-4">Dettagli</h3>
              
              <div class="space-y-4 text-sm">
                <div>
                  <span class="text-gray-500 block">Utente</span>
                  <span class="font-medium">{{ report.user?.name }}</span>
                  <span class="text-gray-500 block text-xs">{{ report.user?.email }}</span>
                </div>
                
                <div v-if="report.vehicle">
                  <span class="text-gray-500 block">Veicolo</span>
                  <span class="font-medium">{{ report.vehicle.plate }}</span>
                  <span class="text-gray-500 block text-xs">{{ report.vehicle.model }}</span>
                </div>
                
                <div v-if="report.rental">
                  <span class="text-gray-500 block">Noleggio</span>
                  <span class="font-medium">{{ formatDateTime(report.rental.started_at) }}</span>
                  <span class="text-gray-500 block text-xs">€{{ report.rental.total_cost?.toFixed(2) }}</span>
                </div>
                
                <div>
                  <span class="text-gray-500 block">Creato</span>
                  <span class="font-medium">{{ formatDateTime(report.created_at) }}</span>
                </div>
                
                <div v-if="report.assigned_to">
                  <span class="text-gray-500 block">Assegnato a</span>
                  <span class="font-medium">{{ report.assigned_to.name }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Actions Card -->
          <div class="card bg-white shadow-lg">
            <div class="card-body">
              <h3 class="font-bold text-lg mb-4">Azioni</h3>
              
              <div class="space-y-3">
                <div>
                  <label class="label py-1"><span class="label-text">Stato</span></label>
                  <select 
                    v-model="newStatus" 
                    @change="updateStatus"
                    class="select select-bordered select-sm w-full"
                    :disabled="updating"
                  >
                    <option value="open">Aperta</option>
                    <option value="in_progress">In lavorazione</option>
                    <option value="resolved">Risolta</option>
                    <option value="closed">Chiusa</option>
                  </select>
                </div>
                
                <div>
                  <label class="label py-1"><span class="label-text">Priorità</span></label>
                  <select 
                    v-model="newPriority" 
                    @change="updatePriority"
                    class="select select-bordered select-sm w-full"
                    :disabled="updating"
                  >
                    <option value="low">⚪ Bassa</option>
                    <option value="medium">🟡 Media</option>
                    <option value="high">🟠 Alta</option>
                    <option value="urgent">🔴 Urgente</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authState } from '@/states/auth'
import { API_BASE_URL } from '@/config/api'

const route = useRoute()
const router = useRouter()

const report = ref(null)
const loading = ref(true)
const saving = ref(false)
const resolving = ref(false)
const updating = ref(false)

const operatorNotes = ref('')
const resolutionText = ref('')
const newStatus = ref('')
const newPriority = ref('')

const categoryIcons = {
  vehicle_damage: '🚗💥',
  vehicle_malfunction: '⚠️',
  accident: '🚨',
  parking_issue: '🅿️',
  app_issue: '📱',
  payment_issue: '💳',
  other: '❓'
}

onMounted(() => {
  fetchReport()
})

async function fetchReport() {
  loading.value = true
  try {
    const response = await fetch(`${API_BASE_URL}/operator/issues/${route.params.id}`, {
      headers: { 'Authorization': `Bearer ${authState.token}` }
    })
    
    if (response.ok) {
      const data = await response.json()
      report.value = data.report
      operatorNotes.value = data.report.operator_notes || ''
      newStatus.value = data.report.status
      newPriority.value = data.report.priority
    } else {
      router.push('/operator/reports')
    }
  } catch (err) {
    console.error('Error fetching report:', err)
  } finally {
    loading.value = false
  }
}

async function saveNotes() {
  saving.value = true
  try {
    await fetch(`${API_BASE_URL}/operator/issues/${route.params.id}`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${authState.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ operator_notes: operatorNotes.value })
    })
  } catch (err) {
    console.error('Error saving notes:', err)
  } finally {
    saving.value = false
  }
}

async function updateStatus() {
  updating.value = true
  try {
    await fetch(`${API_BASE_URL}/operator/issues/${route.params.id}`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${authState.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ status: newStatus.value })
    })
    report.value.status = newStatus.value
  } catch (err) {
    console.error('Error updating status:', err)
  } finally {
    updating.value = false
  }
}

async function updatePriority() {
  updating.value = true
  try {
    await fetch(`${API_BASE_URL}/operator/issues/${route.params.id}`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${authState.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ priority: newPriority.value })
    })
    report.value.priority = newPriority.value
  } catch (err) {
    console.error('Error updating priority:', err)
  } finally {
    updating.value = false
  }
}

async function resolveReport() {
  if (!resolutionText.value.trim()) return
  
  resolving.value = true
  try {
    const response = await fetch(`${API_BASE_URL}/operator/issues/${route.params.id}/resolve`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${authState.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ resolution: resolutionText.value.trim() })
    })
    
    if (response.ok) {
      await fetchReport()
    }
  } catch (err) {
    console.error('Error resolving report:', err)
  } finally {
    resolving.value = false
  }
}

function getCategoryIcon(category) {
  return categoryIcons[category] || '❓'
}

function getStatusLabel(status) {
  const labels = { open: 'Aperta', in_progress: 'In lavorazione', resolved: 'Risolta', closed: 'Chiusa' }
  return labels[status] || status
}

function getStatusBadgeClass(status) {
  const classes = { open: 'badge-warning', in_progress: 'badge-info', resolved: 'badge-success', closed: 'badge-neutral' }
  return classes[status] || ''
}

function getPriorityLabel(priority) {
  const labels = { low: 'Bassa', medium: 'Media', high: 'Alta', urgent: 'Urgente' }
  return labels[priority] || priority
}

function getPriorityBadgeClass(priority) {
  const classes = { low: 'badge-ghost', medium: 'badge-info', high: 'badge-warning', urgent: 'badge-error' }
  return classes[priority] || ''
}

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('it-IT', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

function getPhotoUrl(path) {
  return `${API_BASE_URL.replace('/api/v1', '')}/${path.replace(/\\/g, '/')}`
}

function openFullPhoto(path) {
  window.open(getPhotoUrl(path), '_blank')
}
</script>
