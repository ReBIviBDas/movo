<script setup>
import { ref, onMounted, computed } from 'vue'
import { RouterLink } from 'vue-router'
import { API_BASE_URL } from '@/config/api'
import { authState } from '@/states/auth'

const users = ref([])
const isLoading = ref(true)
const error = ref('')

// Filters
const statusFilter = ref('')
const searchQuery = ref('')

// Pagination
const pagination = ref({ page: 1, limit: 20, total: 0, pages: 0 })

// Expanded user detail
const expandedUserId = ref(null)
const expandedUser = ref(null)
const isLoadingDetail = ref(false)

// Action modal state
const actionModal = ref({
  show: false,
  type: '', // 'reject', 'suspend', 'block'
  userId: '',
  userName: '',
  reason: '',
  durationDays: '',
  isSubmitting: false,
  error: ''
})

const statusOptions = [
  { value: '', label: 'Tutti' },
  { value: 'active', label: 'Attivi' },
  { value: 'pending_approval', label: 'In Attesa' },
  { value: 'suspended', label: 'Sospesi' },
  { value: 'blocked', label: 'Bloccati' }
]

const statusBadge = (status) => {
  const map = {
    active: { class: 'badge-success', label: 'Attivo' },
    pending_approval: { class: 'badge-warning', label: 'In Attesa' },
    suspended: { class: 'badge-error', label: 'Sospeso' },
    blocked: { class: 'badge-neutral', label: 'Bloccato' }
  }
  return map[status] || { class: 'badge-ghost', label: status }
}

// Filter users client-side for search
const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value
  const q = searchQuery.value.toLowerCase()
  return users.value.filter(u =>
    (u.first_name + ' ' + u.last_name).toLowerCase().includes(q) ||
    u.email.toLowerCase().includes(q) ||
    (u.fiscal_code || '').toLowerCase().includes(q)
  )
})

onMounted(async () => {
  await fetchUsers()
})

async function fetchUsers(page = 1) {
  isLoading.value = true
  error.value = ''
  try {
    const params = new URLSearchParams()
    if (statusFilter.value) params.append('status', statusFilter.value)
    params.append('page', page)
    params.append('limit', pagination.value.limit)

    const response = await fetch(`${API_BASE_URL}/operator/users?${params}`, {
      headers: { 'Authorization': `Bearer ${authState.token}` }
    })
    if (!response.ok) throw new Error('Errore nel caricamento utenti')
    const data = await response.json()
    users.value = data.users
    pagination.value = { ...pagination.value, ...data.pagination }
  } catch (err) {
    console.error(err)
    error.value = 'Impossibile caricare gli utenti'
  } finally {
    isLoading.value = false
  }
}

async function toggleDetail(userId) {
  if (expandedUserId.value === userId) {
    expandedUserId.value = null
    expandedUser.value = null
    return
  }
  expandedUserId.value = userId
  isLoadingDetail.value = true
  try {
    const response = await fetch(`${API_BASE_URL}/operator/users/${userId}`, {
      headers: { 'Authorization': `Bearer ${authState.token}` }
    })
    if (!response.ok) throw new Error('Errore nel caricamento dettagli')
    expandedUser.value = await response.json()
  } catch (err) {
    console.error(err)
  } finally {
    isLoadingDetail.value = false
  }
}

// Actions
async function approveUser(userId) {
  if (!confirm('Sei sicuro di voler approvare questo utente?')) return
  try {
    const response = await fetch(`${API_BASE_URL}/operator/users/${userId}/approve`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${authState.token}` }
    })
    if (!response.ok) {
      const data = await response.json()
      alert(data.detail || 'Errore durante l\'approvazione')
      return
    }
    await fetchUsers(pagination.value.page)
    expandedUserId.value = null
  } catch (err) {
    console.error(err)
    alert('Errore durante l\'approvazione')
  }
}

function openActionModal(type, user) {
  actionModal.value = {
    show: true,
    type,
    userId: user._id || user.id,
    userName: `${user.first_name} ${user.last_name}`,
    reason: '',
    durationDays: '',
    isSubmitting: false,
    error: ''
  }
}

function closeActionModal() {
  actionModal.value.show = false
}

const actionModalTitle = computed(() => {
  const map = {
    reject: 'Rifiuta Utente',
    suspend: 'Sospendi Utente',
    block: 'Blocca Utente'
  }
  return map[actionModal.value.type] || ''
})

const actionModalMinLength = computed(() => {
  return actionModal.value.type === 'suspend' ? 1 : 10
})

async function submitAction() {
  const { type, userId, reason, durationDays } = actionModal.value

  if (!reason || reason.length < actionModalMinLength.value) {
    actionModal.value.error = `Il motivo deve contenere almeno ${actionModalMinLength.value} caratteri.`
    return
  }

  actionModal.value.isSubmitting = true
  actionModal.value.error = ''

  try {
    // Determine which endpoint to use
    let url = ''
    let body = {}

    if (type === 'reject') {
      url = `${API_BASE_URL}/operator/users/${userId}/reject`
      body = { reason }
    } else if (type === 'suspend') {
      url = `${API_BASE_URL}/operator/users/${userId}/suspend`
      body = { reason }
      if (durationDays) body.duration_days = parseInt(durationDays)
    } else if (type === 'block') {
      url = `${API_BASE_URL}/operator/users/${userId}/block`
      body = { reason }
    }

    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authState.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(body)
    })

    if (!response.ok) {
      const data = await response.json()
      throw new Error(data.detail || 'Errore nell\'operazione')
    }

    closeActionModal()
    await fetchUsers(pagination.value.page)
    expandedUserId.value = null
  } catch (err) {
    console.error(err)
    actionModal.value.error = err.message || 'Errore nell\'operazione'
  } finally {
    actionModal.value.isSubmitting = false
  }
}

async function reactivateUser(userId) {
  if (!confirm('Riattivare questo utente?')) return
  try {
    const response = await fetch(`${API_BASE_URL}/operator/users/${userId}/reactivate`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${authState.token}` }
    })
    if (!response.ok) {
      const data = await response.json()
      alert(data.detail || 'Errore durante la riattivazione')
      return
    }
    await fetchUsers(pagination.value.page)
    expandedUserId.value = null
  } catch (err) {
    console.error(err)
    alert('Errore durante la riattivazione')
  }
}

function onFilterChange() {
  fetchUsers(1)
}

function formatDate(dateStr) {
  if (!dateStr) return 'N/A'
  return new Date(dateStr).toLocaleDateString('it-IT')
}

function getDocUrl(path) {
  if (!path) return null
  const base = API_BASE_URL.replace('/api/v1', '')
  return `${base}/${path}`
}
</script>

<template>
  <div>
    <!-- Header -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold">Gestione Utenti</h1>
        <p class="text-base-content/60">Visualizza, cerca e gestisci gli utenti</p>
      </div>
      <RouterLink to="/operator" class="btn btn-ghost">← Dashboard</RouterLink>
    </div>

    <!-- Filters -->
    <div class="card bg-base-100 shadow-md mb-6">
      <div class="card-body py-4">
        <div class="flex flex-col md:flex-row gap-4">
          <div class="form-control flex-1">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Cerca per nome, email o codice fiscale..."
              class="input input-bordered w-full"
            />
          </div>
          <div class="form-control">
            <select v-model="statusFilter" @change="onFilterChange" class="select select-bordered">
              <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="isLoading" class="flex justify-center py-12">
      <span class="loading loading-spinner loading-lg"></span>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="alert alert-error">
      <span>{{ error }}</span>
      <button @click="fetchUsers()" class="btn btn-sm">Riprova</button>
    </div>

    <!-- Empty -->
    <div v-else-if="filteredUsers.length === 0" class="text-center py-12">
      <div class="text-6xl mb-4">👥</div>
      <h2 class="text-xl font-semibold mb-2">Nessun utente trovato</h2>
      <p class="text-base-content/60">Prova a cambiare i filtri di ricerca</p>
    </div>

    <!-- Users Table -->
    <div v-else class="overflow-x-auto">
      <table class="table table-zebra bg-base-100 shadow-md">
        <thead>
          <tr>
            <th></th>
            <th>Utente</th>
            <th>Email</th>
            <th>Codice Fiscale</th>
            <th>Stato</th>
            <th>Registrato</th>
            <th>Azioni</th>
          </tr>
        </thead>
        <tbody>
          <template v-for="u in filteredUsers" :key="u._id">
            <!-- User Row -->
            <tr class="hover cursor-pointer" @click="toggleDetail(u._id)">
              <td>
                <span class="text-sm">{{ expandedUserId === u._id ? '▼' : '▶' }}</span>
              </td>
              <td>
                <div class="font-bold">{{ u.first_name }} {{ u.last_name }}</div>
                <div class="text-sm opacity-50">{{ u.phone }}</div>
              </td>
              <td>{{ u.email }}</td>
              <td class="font-mono text-sm">{{ u.fiscal_code }}</td>
              <td>
                <span class="badge" :class="statusBadge(u.status).class">
                  {{ statusBadge(u.status).label }}
                </span>
              </td>
              <td>{{ formatDate(u.created_at) }}</td>
              <td>
                <div class="flex gap-1 flex-wrap" @click.stop>
                  <!-- Pending: Approve / Reject -->
                  <template v-if="u.status === 'pending_approval'">
                    <button @click="approveUser(u._id)" class="btn btn-success btn-xs">
                      ✓ Approva
                    </button>
                    <button @click="openActionModal('reject', u)" class="btn btn-error btn-xs btn-outline">
                      ✗ Rifiuta
                    </button>
                  </template>
                  <!-- Active: Suspend / Block -->
                  <template v-else-if="u.status === 'active'">
                    <button @click="openActionModal('suspend', u)" class="btn btn-warning btn-xs btn-outline">
                      ⏸ Sospendi
                    </button>
                    <button @click="openActionModal('block', u)" class="btn btn-error btn-xs btn-outline">
                      🚫 Blocca
                    </button>
                  </template>
                  <!-- Suspended: Reactivate / Block -->
                  <template v-else-if="u.status === 'suspended'">
                    <button @click="reactivateUser(u._id)" class="btn btn-success btn-xs">
                      ▶ Riattiva
                    </button>
                    <button @click="openActionModal('block', u)" class="btn btn-error btn-xs btn-outline">
                      🚫 Blocca
                    </button>
                  </template>
                  <!-- Blocked: no actions -->
                  <template v-else-if="u.status === 'blocked'">
                    <span class="text-xs opacity-50">Bloccato</span>
                  </template>
                </div>
              </td>
            </tr>

            <!-- Expanded Detail Row -->
            <tr v-if="expandedUserId === u._id">
              <td colspan="7" class="bg-base-200 p-4">
                <div v-if="isLoadingDetail" class="flex justify-center py-4">
                  <span class="loading loading-spinner"></span>
                </div>
                <div v-else-if="expandedUser" class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <!-- Left: User Info -->
                  <div class="space-y-2">
                    <h3 class="font-bold text-lg mb-2">Dettagli Utente</h3>
                    <div><span class="opacity-60">Telefono:</span> {{ expandedUser.phone }}</div>
                    <div><span class="opacity-60">Data di nascita:</span> {{ formatDate(expandedUser.date_of_birth) }}</div>
                    <div><span class="opacity-60">Indirizzo:</span> {{ expandedUser.address || 'N/A' }}</div>
                    <div><span class="opacity-60">Ruolo:</span> {{ expandedUser.role }}</div>
                    <div>
                      <span class="opacity-60">Guida abilitata:</span>
                      <span :class="expandedUser.driving_enabled ? 'text-success' : 'text-error'">
                        {{ expandedUser.driving_enabled ? '✓ Sì' : '✗ No' }}
                      </span>
                    </div>
                    <div>
                      <span class="opacity-60">Patente verificata:</span>
                      <span :class="expandedUser.driving_license_verified ? 'text-success' : 'text-error'">
                        {{ expandedUser.driving_license_verified ? '✓ Sì' : '✗ No' }}
                      </span>
                    </div>
                  </div>

                  <!-- Right: Documents -->
                  <div class="space-y-2">
                    <h3 class="font-bold text-lg mb-2">Documenti</h3>
                    <div class="flex flex-col gap-2">
                      <a
                        v-if="expandedUser.driving_license"
                        :href="getDocUrl(expandedUser.driving_license)"
                        target="_blank"
                        class="btn btn-outline btn-sm gap-2"
                      >
                        📄 Visualizza Patente
                      </a>
                      <span v-else class="badge badge-ghost">Patente non caricata</span>

                      <a
                        v-if="expandedUser.identity_document"
                        :href="getDocUrl(expandedUser.identity_document)"
                        target="_blank"
                        class="btn btn-outline btn-sm gap-2"
                      >
                        🪪 Visualizza Documento
                      </a>
                      <span v-else class="badge badge-ghost">Documento non caricato</span>
                    </div>
                  </div>
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>

      <!-- Pagination -->
      <div v-if="pagination.pages > 1" class="flex justify-center mt-4">
        <div class="join">
          <button
            v-for="p in pagination.pages"
            :key="p"
            class="join-item btn btn-sm"
            :class="{ 'btn-active': p === pagination.page }"
            @click="fetchUsers(p)"
          >
            {{ p }}
          </button>
        </div>
      </div>
    </div>

    <!-- Action Modal -->
    <dialog v-if="actionModal.show" class="modal modal-open">
      <div class="modal-box">
        <h3 class="font-bold text-lg">{{ actionModalTitle }}</h3>
        <p class="py-2">
          Utente: <strong>{{ actionModal.userName }}</strong>
        </p>

        <div v-if="actionModal.error" class="alert alert-error mb-4 text-sm">
          {{ actionModal.error }}
        </div>

        <div class="form-control">
          <label class="label">
            <span class="label-text">Motivo *</span>
          </label>
          <textarea
            v-model="actionModal.reason"
            class="textarea textarea-bordered"
            :placeholder="actionModal.type === 'reject' ? 'Documenti non leggibili o non validi...' : 'Motivo della sospensione/blocco...'"
            rows="3"
          ></textarea>
        </div>

        <div v-if="actionModal.type === 'suspend'" class="form-control mt-2">
          <label class="label">
            <span class="label-text">Durata (giorni, opzionale)</span>
          </label>
          <input
            v-model="actionModal.durationDays"
            type="number"
            min="1"
            class="input input-bordered"
            placeholder="Lascia vuoto per sospensione indefinita"
          />
        </div>

        <div class="modal-action">
          <button @click="closeActionModal" class="btn btn-ghost" :disabled="actionModal.isSubmitting">
            Annulla
          </button>
          <button
            @click="submitAction"
            class="btn"
            :class="{
              'btn-error': actionModal.type === 'reject' || actionModal.type === 'block',
              'btn-warning': actionModal.type === 'suspend'
            }"
            :disabled="actionModal.isSubmitting"
          >
            <span v-if="actionModal.isSubmitting" class="loading loading-spinner loading-sm"></span>
            Conferma
          </button>
        </div>
      </div>
      <form method="dialog" class="modal-backdrop">
        <button @click="closeActionModal">close</button>
      </form>
    </dialog>
  </div>
</template>
