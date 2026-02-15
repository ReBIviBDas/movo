<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50">
    <!-- Header -->
    <div class="bg-white shadow-sm border-b">
      <div class="max-w-5xl mx-auto px-4 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <router-link to="/dashboard" class="btn btn-ghost btn-sm">
              ← Dashboard
            </router-link>
            <h1 class="text-2xl font-bold text-gray-800">📋 Le Mie Segnalazioni</h1>
          </div>
          <router-link to="/report" class="btn btn-primary">
            ➕ Nuova Segnalazione
          </router-link>
        </div>
      </div>
    </div>

    <div class="max-w-5xl mx-auto px-4 py-8">
      <!-- Filters -->
      <div class="flex gap-3 mb-6">
        <select v-model="statusFilter" @change="fetchReports" class="select select-bordered select-sm">
          <option value="">Tutti gli stati</option>
          <option value="open">Aperte</option>
          <option value="in_progress">In lavorazione</option>
          <option value="resolved">Risolte</option>
          <option value="closed">Chiuse</option>
        </select>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="flex justify-center py-12">
        <span class="loading loading-spinner loading-lg text-primary"></span>
      </div>

      <!-- Empty State -->
      <div v-else-if="reports.length === 0" class="card bg-white shadow-lg">
        <div class="card-body text-center py-16">
          <div class="text-6xl mb-4">📭</div>
          <h2 class="text-2xl font-bold text-gray-600 mb-2">Nessuna segnalazione</h2>
          <p class="text-gray-500 mb-6">Non hai ancora inviato segnalazioni</p>
          <router-link to="/report" class="btn btn-primary">
            📝 Segnala un problema
          </router-link>
        </div>
      </div>

      <!-- Reports List -->
      <div v-else class="space-y-4">
        <div 
          v-for="report in reports" 
          :key="report.id"
          class="card bg-white shadow-md hover:shadow-lg transition-shadow cursor-pointer"
          @click="viewReport(report.id)"
        >
          <div class="card-body py-4">
            <div class="flex items-start justify-between gap-4">
              <div class="flex-1">
                <div class="flex items-center gap-3 mb-2">
                  <span class="text-2xl">{{ getCategoryIcon(report.category) }}</span>
                  <div>
                    <h3 class="font-bold text-lg">{{ report.category_label }}</h3>
                    <p class="text-sm text-gray-500">{{ report.reference_id }}</p>
                  </div>
                </div>
                <p class="text-gray-600 line-clamp-2">{{ report.description }}</p>
                <div class="flex items-center gap-4 mt-3 text-sm text-gray-500">
                  <span v-if="report.vehicle">🚗 {{ report.vehicle }}</span>
                  <span>📅 {{ formatDate(report.created_at) }}</span>
                </div>
              </div>
              <div class="flex flex-col items-end gap-2">
                <span :class="['badge badge-lg', getStatusBadgeClass(report.status)]">
                  {{ getStatusLabel(report.status) }}
                </span>
                <span :class="['badge', getPriorityBadgeClass(report.priority)]">
                  {{ getPriorityLabel(report.priority) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="pagination.pages > 1" class="flex justify-center mt-8">
        <div class="btn-group">
          <button 
            class="btn"
            :disabled="pagination.page <= 1"
            @click="changePage(pagination.page - 1)"
          >«</button>
          <button class="btn">Pagina {{ pagination.page }} di {{ pagination.pages }}</button>
          <button 
            class="btn"
            :disabled="pagination.page >= pagination.pages"
            @click="changePage(pagination.page + 1)"
          >»</button>
        </div>
      </div>
    </div>

    <!-- Report Detail Modal -->
    <dialog ref="detailModal" class="modal">
      <div class="modal-box max-w-2xl">
        <form method="dialog">
          <button class="btn btn-sm btn-circle btn-ghost absolute right-2 top-2">✕</button>
        </form>
        
        <div v-if="selectedReport">
          <h3 class="font-bold text-xl mb-4">
            {{ getCategoryIcon(selectedReport.category) }} {{ selectedReport.category_label }}
          </h3>
          
          <div class="space-y-4">
            <div class="flex justify-between items-center">
              <span class="text-gray-600">Riferimento</span>
              <span class="font-mono font-bold">{{ selectedReport.reference_id }}</span>
            </div>
            
            <div class="flex justify-between items-center">
              <span class="text-gray-600">Stato</span>
              <span :class="['badge badge-lg', getStatusBadgeClass(selectedReport.status)]">
                {{ getStatusLabel(selectedReport.status) }}
              </span>
            </div>
            
            <div v-if="selectedReport.vehicle" class="flex justify-between items-center">
              <span class="text-gray-600">Veicolo</span>
              <span>{{ selectedReport.vehicle.plate }} - {{ selectedReport.vehicle.model }}</span>
            </div>
            
            <div>
              <span class="text-gray-600 block mb-2">Descrizione</span>
              <p class="bg-gray-50 p-4 rounded-lg">{{ selectedReport.description }}</p>
            </div>
            
            <div v-if="selectedReport.photos?.length > 0">
              <span class="text-gray-600 block mb-2">Foto allegate</span>
              <div class="flex gap-3 flex-wrap">
                <img 
                  v-for="(photo, i) in selectedReport.photos" 
                  :key="i"
                  :src="getPhotoUrl(photo)"
                  class="w-24 h-24 object-cover rounded-lg cursor-pointer hover:opacity-80"
                  @click="openFullPhoto(photo)"
                />
              </div>
            </div>
            
            <div v-if="selectedReport.resolution" class="bg-green-50 p-4 rounded-lg">
              <span class="text-green-700 font-semibold block mb-1">✅ Risoluzione</span>
              <p class="text-green-800">{{ selectedReport.resolution }}</p>
            </div>
            
            <div class="flex justify-between text-sm text-gray-500 pt-4 border-t">
              <span>Creato: {{ formatDateTime(selectedReport.created_at) }}</span>
              <span v-if="selectedReport.resolved_at">
                Risolto: {{ formatDateTime(selectedReport.resolved_at) }}
              </span>
            </div>
          </div>
        </div>
      </div>
      <form method="dialog" class="modal-backdrop">
        <button>close</button>
      </form>
    </dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { authState } from '@/states/auth'
import { API_BASE_URL } from '@/config/api'

const reports = ref([])
const loading = ref(true)
const statusFilter = ref('')
const pagination = reactive({ page: 1, pages: 1, total: 0 })
const selectedReport = ref(null)
const detailModal = ref(null)

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
  fetchReports()
})

async function fetchReports() {
  loading.value = true
  try {
    const params = new URLSearchParams()
    params.append('page', pagination.page)
    params.append('limit', 10)
    if (statusFilter.value) params.append('status', statusFilter.value)
    
    const response = await fetch(`${API_BASE_URL}/issues?${params}`, {
      headers: { 'Authorization': `Bearer ${authState.token}` }
    })
    
    if (response.ok) {
      const data = await response.json()
      reports.value = data.reports
      pagination.page = data.pagination.page
      pagination.pages = data.pagination.pages
      pagination.total = data.pagination.total
    }
  } catch (err) {
    console.error('Error fetching reports:', err)
  } finally {
    loading.value = false
  }
}

async function viewReport(id) {
  try {
    const response = await fetch(`${API_BASE_URL}/issues/${id}`, {
      headers: { 'Authorization': `Bearer ${authState.token}` }
    })
    
    if (response.ok) {
      const data = await response.json()
      selectedReport.value = data.report
      detailModal.value?.showModal()
    }
  } catch (err) {
    console.error('Error fetching report:', err)
  }
}

function changePage(page) {
  pagination.page = page
  fetchReports()
}

function getCategoryIcon(category) {
  return categoryIcons[category] || '❓'
}

function getStatusLabel(status) {
  const labels = {
    open: 'Aperta',
    in_progress: 'In lavorazione',
    resolved: 'Risolta',
    closed: 'Chiusa'
  }
  return labels[status] || status
}

function getStatusBadgeClass(status) {
  const classes = {
    open: 'badge-warning',
    in_progress: 'badge-info',
    resolved: 'badge-success',
    closed: 'badge-neutral'
  }
  return classes[status] || ''
}

function getPriorityLabel(priority) {
  const labels = {
    low: 'Bassa',
    medium: 'Media',
    high: 'Alta',
    urgent: 'Urgente'
  }
  return labels[priority] || priority
}

function getPriorityBadgeClass(priority) {
  const classes = {
    low: 'badge-ghost',
    medium: 'badge-info badge-outline',
    high: 'badge-warning badge-outline',
    urgent: 'badge-error'
  }
  return classes[priority] || ''
}

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString('it-IT', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

function formatDateTime(dateStr) {
  return new Date(dateStr).toLocaleString('it-IT', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function getPhotoUrl(path) {
  // Photos are stored with full path, serve from backend
  return `${API_BASE_URL.replace('/api/v1', '')}/${path.replace(/\\/g, '/')}`
}

function openFullPhoto(path) {
  window.open(getPhotoUrl(path), '_blank')
}
</script>
