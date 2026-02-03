<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { API_BASE_URL } from '@/config/api'
import { authState } from '@/states/auth'

const users = ref([])
const isLoading = ref(true)
const error = ref('')

// Fetch pending users on mount
onMounted(async () => {
  await fetchPendingUsers()
})

async function fetchPendingUsers() {
  isLoading.value = true
  error.value = ''
  
  try {
    const response = await fetch(`${API_BASE_URL}/users/pending`, {
      headers: {
        'Authorization': `Bearer ${authState.token}`
      }
    })
    
    if (!response.ok) {
      throw new Error('Failed to fetch pending users')
    }
    
    const data = await response.json()
    users.value = data.users
  } catch (err) {
    console.error('Error:', err)
    error.value = 'Errore nel caricamento degli utenti'
  } finally {
    isLoading.value = false
  }
}

async function approveUser(userId) {
  if (!confirm('Sei sicuro di voler approvare questo utente?')) return
  
  try {
    const response = await fetch(`${API_BASE_URL}/users/${userId}/approve`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authState.token}`
      }
    })
    
    if (!response.ok) {
      throw new Error('Failed to approve user')
    }
    
    // Remove from list
    users.value = users.value.filter(u => u._id !== userId)
    alert('Utente approvato con successo!')
  } catch (err) {
    console.error('Error:', err)
    alert('Errore durante l\'approvazione')
  }
}

async function rejectUser(userId) {
  const reason = prompt('Motivo del rifiuto (opzionale):')
  if (reason === null) return // User cancelled
  
  try {
    const response = await fetch(`${API_BASE_URL}/users/${userId}/reject`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authState.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ reason })
    })
    
    if (!response.ok) {
      throw new Error('Failed to reject user')
    }
    
    // Remove from list
    users.value = users.value.filter(u => u._id !== userId)
    alert('Utente rifiutato. Notifica inviata.')
  } catch (err) {
    console.error('Error:', err)
    alert('Errore durante il rifiuto')
  }
}

function formatDate(dateStr) {
  if (!dateStr) return 'N/A'
  return new Date(dateStr).toLocaleDateString('it-IT')
}
</script>

<template>
  <div>
    <!-- Header -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold">Utenti in Attesa di Approvazione</h1>
        <p class="text-base-content/60">Verifica i documenti e approva le richieste</p>
      </div>
      <RouterLink to="/operator" class="btn btn-ghost">
        ← Dashboard
      </RouterLink>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="flex justify-center py-12">
      <span class="loading loading-spinner loading-lg"></span>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="alert alert-error">
      <span>{{ error }}</span>
      <button @click="fetchPendingUsers" class="btn btn-sm">Riprova</button>
    </div>

    <!-- Empty State -->
    <div v-else-if="users.length === 0" class="text-center py-12">
      <div class="text-6xl mb-4">✅</div>
      <h2 class="text-xl font-semibold mb-2">Nessun utente in attesa</h2>
      <p class="text-base-content/60">Tutti gli utenti sono stati verificati</p>
    </div>

    <!-- Users Table -->
    <div v-else class="overflow-x-auto">
      <table class="table table-zebra bg-base-100 shadow-md">
        <thead>
          <tr>
            <th>Utente</th>
            <th>Email</th>
            <th>Codice Fiscale</th>
            <th>Data Registrazione</th>
            <th>Documenti</th>
            <th>Azioni</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user._id">
            <td>
              <div class="font-bold">{{ user.first_name }} {{ user.last_name }}</div>
              <div class="text-sm opacity-50">{{ user.phone }}</div>
            </td>
            <td>{{ user.email }}</td>
            <td class="font-mono text-sm">{{ user.fiscal_code }}</td>
            <td>{{ formatDate(user.created_at) }}</td>
            <td>
              <div class="flex gap-2">
                <a v-if="user.driving_license" 
                   :href="`${API_BASE_URL.replace('/api/v1', '')}/${user.driving_license}`" 
                   target="_blank" 
                   class="btn btn-xs btn-outline">
                  📄 Patente
                </a>
                <span v-else class="badge badge-ghost">No patente</span>
                
                <a v-if="user.identity_document" 
                   :href="`${API_BASE_URL.replace('/api/v1', '')}/${user.identity_document}`" 
                   target="_blank" 
                   class="btn btn-xs btn-outline">
                  📄 Documento
                </a>
                <span v-else class="badge badge-ghost">No documento</span>
              </div>
            </td>
            <td>
              <div class="flex gap-2">
                <button @click="approveUser(user._id)" class="btn btn-success btn-sm">
                  ✓ Approva
                </button>
                <button @click="rejectUser(user._id)" class="btn btn-error btn-sm btn-outline">
                  ✗ Rifiuta
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
