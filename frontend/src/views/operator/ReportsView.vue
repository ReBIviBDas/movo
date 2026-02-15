<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-100 via-white to-blue-50">
    <!-- Header -->
    <div class="bg-white shadow-sm border-b">
      <div class="max-w-7xl mx-auto px-4 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <router-link to="/operator" class="btn btn-ghost btn-sm">
              ← Dashboard
            </router-link>
            <h1 class="text-2xl font-bold text-gray-800">🎫 Gestione Segnalazioni</h1>
          </div>
          <div class="text-sm text-gray-500">
            {{ counts.open }} aperte · {{ counts.in_progress }} in lavorazione
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 py-6">
      <!-- Stats Cards -->
      <div class="grid grid-cols-4 gap-4 mb-6">
        <div 
          v-for="stat in stats" 
          :key="stat.status"
          @click="statusFilter = stat.status === statusFilter ? '' : stat.status; fetchReports()"
          :class="[
            'card cursor-pointer transition-all hover:shadow-lg',
            statusFilter === stat.status ? 'ring-2 ring-primary bg-primary/5' : 'bg-white'
          ]"
        >
          <div class="card-body py-4">
            <div class="flex justify-between items-center">
              <span class="text-3xl">{{ stat.icon }}</span>
              <span class="text-3xl font-bold">{{ stat.count }}</span>
            </div>
            <p class="text-gray-600 font-medium">{{ stat.label }}</p>
          </div>
        </div>
      </div>

      <!-- Filters -->
      <div class="flex gap-3 mb-4">
        <select v-model="categoryFilter" @change="fetchReports" class="select select-bordered select-sm">
          <option value="">Tutte le categorie</option>
          <option value="vehicle_damage">Danni veicolo</option>
          <option value="vehicle_malfunction">Guasto</option>
          <option value="accident">Incidente</option>
          <option value="parking_issue">Parcheggio</option>
          <option value="app_issue">Problema app</option>
          <option value="payment_issue">Pagamento</option>
          <option value="other">Altro</option>
        </select>
        <select v-model="priorityFilter" @change="fetchReports" class="select select-bordered select-sm">
          <option value="">Tutte le priorità</option>
          <option value="urgent">🔴 Urgente</option>
          <option value="high">🟠 Alta</option>
          <option value="medium">🟡 Media</option>
          <option value="low">⚪ Bassa</option>
        </select>
      </div>

      <!-- Reports Table -->
      <div class="card bg-white shadow-lg">
        <div class="overflow-x-auto">
          <table class="table table-zebra">
            <thead>
              <tr class="bg-gray-100">
                <th>Riferimento</th>
                <th>Utente</th>
                <th>Categoria</th>
                <th>Veicolo</th>
                <th>Priorità</th>
                <th>Stato</th>
                <th>Data</th>
                <th>Azioni</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="8" class="text-center py-8">
                  <span class="loading loading-spinner loading-lg"></span>
                </td>
              </tr>
              <tr v-else-if="reports.length === 0">
                <td colspan="8" class="text-center py-8 text-gray-500">
                  Nessuna segnalazione trovata
                </td>
              </tr>
              <tr 
                v-else 
                v-for="report in reports" 
                :key="report.id"
                class="hover:bg-blue-50 cursor-pointer"
                @click="openReport(report.id)"
              >
                <td class="font-mono text-sm">{{ report.reference_id }}</td>
                <td>
                  <div>{{ report.user }}</div>
                  <div class="text-xs text-gray-500">{{ report.user_email }}</div>
                </td>
                <td>
                  <span class="flex items-center gap-2">
                    {{ getCategoryIcon(report.category) }}
                    {{ report.category_label }}
                  </span>
                </td>
                <td>{{ report.vehicle || '-' }}</td>
                <td>
                  <span :class="['badge', getPriorityBadgeClass(report.priority)]">
                    {{ getPriorityLabel(report.priority) }}
                  </span>
                </td>
                <td>
                  <span :class="['badge', getStatusBadgeClass(report.status)]">
                    {{ getStatusLabel(report.status) }}
                  </span>
                </td>
                <td class="text-sm">{{ formatDate(report.created_at) }}</td>
                <td>
                  <button 
                    v-if="report.status === 'open'"
                    class="btn btn-xs btn-primary"
                    @click.stop="takeReport(report.id)"
                  >
                    Prendi in carico
                  </button>
                  <button 
                    v-else-if="report.status === 'in_progress'"
                    class="btn btn-xs btn-success"
                    @click.stop="openReport(report.id)"
                  >
                    Gestisci
                  </button>
                  <button 
                    v-else
                    class="btn btn-xs btn-ghost"
                    @click.stop="openReport(report.id)"
                  >
                    Visualizza
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="pagination.pages > 1" class="flex justify-center mt-6">
        <div class="btn-group">
          <button 
            class="btn btn-sm"
            :disabled="pagination.page <= 1"
            @click="changePage(pagination.page - 1)"
          >«</button>
          <button class="btn btn-sm">{{ pagination.page }} / {{ pagination.pages }}</button>
          <button 
            class="btn btn-sm"
            :disabled="pagination.page >= pagination.pages"
            @click="changePage(pagination.page + 1)"
          >»</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authState } from '@/states/auth'
import { API_BASE_URL } from '@/config/api'

const router = useRouter()
const reports = ref([])
const loading = ref(true)
const statusFilter = ref('')
const categoryFilter = ref('')
const priorityFilter = ref('')
const pagination = reactive({ page: 1, pages: 1, total: 0 })
const counts = reactive({ open: 0, in_progress: 0, resolved: 0, closed: 0 })

const stats = computed(() => [
  { status: 'open', label: 'Aperte', icon: '📬', count: counts.open },
  { status: 'in_progress', label: 'In lavorazione', icon: '🔧', count: counts.in_progress },
  { status: 'resolved', label: 'Risolte', icon: '✅', count: counts.resolved },
  { status: 'closed', label: 'Chiuse', icon: '📁', count: counts.closed }
])

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
    params.append('limit', 20)
    if (statusFilter.value) params.append('status', statusFilter.value)
    if (categoryFilter.value) params.append('category', categoryFilter.value)
    if (priorityFilter.value) params.append('priority', priorityFilter.value)
    
    const response = await fetch(`${API_BASE_URL}/operator/issues?${params}`, {
      headers: { 'Authorization': `Bearer ${authState.token}` }
    })
    
    if (response.ok) {
      const data = await response.json()
      reports.value = data.reports
      counts.open = data.counts.open
      counts.in_progress = data.counts.in_progress
      counts.resolved = data.counts.resolved
      counts.closed = data.counts.closed
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

async function takeReport(id) {
  try {
    const response = await fetch(`${API_BASE_URL}/operator/issues/${id}`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${authState.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ status: 'in_progress' })
    })
    
    if (response.ok) {
      fetchReports()
      router.push(`/operator/reports/${id}`)
    }
  } catch (err) {
    console.error('Error taking report:', err)
  }
}

function openReport(id) {
  router.push(`/operator/reports/${id}`)
}

function changePage(page) {
  pagination.page = page
  fetchReports()
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

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })
}
</script>
