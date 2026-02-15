<script setup>
import { ref, onMounted, computed } from 'vue'
import { API_BASE_URL } from '@/config/api'
import { authState } from '@/states/auth'

const user = ref(null)
const isLoading = ref(true)
const error = ref('')

// Upload state
const drivingLicenseFile = ref(null)
const identityDocumentFile = ref(null)
const isUploading = ref(false)
const uploadMessage = ref('')
const uploadError = ref('')

// Computed helpers
const statusLabel = computed(() => {
  const map = {
    active: 'Attivo',
    pending_approval: 'In attesa di approvazione',
    suspended: 'Sospeso',
    blocked: 'Bloccato'
  }
  return map[user.value?.status] || user.value?.status || 'N/A'
})

const statusBadgeClass = computed(() => {
  const map = {
    active: 'badge-success',
    pending_approval: 'badge-warning',
    suspended: 'badge-error',
    blocked: 'badge-error'
  }
  return map[user.value?.status] || 'badge-ghost'
})

const canUploadDocuments = computed(() => {
  if (!user.value) return false
  // Can upload if: no documents yet, or documents were rejected (driving_license is null)
  // Cannot upload if already pending_approval (waiting for review)
  return user.value.status !== 'pending_approval' && !user.value.driving_license_verified
})

const hasDocuments = computed(() => {
  return user.value?.driving_license || user.value?.identity_document
})

// Fetch profile
onMounted(async () => {
  await fetchProfile()
})

async function fetchProfile() {
  isLoading.value = true
  error.value = ''
  try {
    const response = await fetch(`${API_BASE_URL}/users/me`, {
      headers: { 'Authorization': `Bearer ${authState.token}` }
    })
    if (!response.ok) throw new Error('Errore nel caricamento del profilo')
    user.value = await response.json()
  } catch (err) {
    console.error(err)
    error.value = 'Impossibile caricare i dati del profilo'
  } finally {
    isLoading.value = false
  }
}

function onFileChange(event, type) {
  const file = event.target.files[0]
  if (type === 'driving_license') {
    drivingLicenseFile.value = file
  } else {
    identityDocumentFile.value = file
  }
}

async function uploadDocuments() {
  if (!drivingLicenseFile.value || !identityDocumentFile.value) {
    uploadError.value = 'Devi caricare sia la patente che il documento d\'identità.'
    return
  }

  isUploading.value = true
  uploadError.value = ''
  uploadMessage.value = ''

  try {
    const formData = new FormData()
    formData.append('driving_license', drivingLicenseFile.value)
    formData.append('identity_document', identityDocumentFile.value)

    const response = await fetch(`${API_BASE_URL}/users/me/documents`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authState.token}`
      },
      body: formData
    })

    const data = await response.json()

    if (!response.ok) {
      throw new Error(data.detail || 'Errore durante il caricamento')
    }

    uploadMessage.value = data.message || 'Documenti caricati con successo!'
    
    // Refresh profile to show updated status
    await fetchProfile()

    // Clear file inputs
    drivingLicenseFile.value = null
    identityDocumentFile.value = null
  } catch (err) {
    console.error(err)
    uploadError.value = err.message || 'Errore durante il caricamento dei documenti'
  } finally {
    isUploading.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return 'N/A'
  return new Date(dateStr).toLocaleDateString('it-IT')
}
</script>

<template>
  <div class="max-w-3xl mx-auto py-4">
    <h1 class="text-2xl font-bold mb-6">Il Mio Profilo</h1>

    <!-- Loading -->
    <div v-if="isLoading" class="flex justify-center py-12">
      <span class="loading loading-spinner loading-lg"></span>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="alert alert-error">
      <span>{{ error }}</span>
      <button @click="fetchProfile" class="btn btn-sm">Riprova</button>
    </div>

    <template v-else-if="user">
      <!-- Profile Info Card -->
      <div class="card bg-base-100 shadow-md mb-6">
        <div class="card-body">
          <h2 class="card-title">Informazioni Personali</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
            <div>
              <div class="text-sm opacity-60">Nome</div>
              <div class="font-medium">{{ user.first_name }} {{ user.last_name }}</div>
            </div>
            <div>
              <div class="text-sm opacity-60">Email</div>
              <div class="font-medium">{{ user.email }}</div>
            </div>
            <div>
              <div class="text-sm opacity-60">Telefono</div>
              <div class="font-medium">{{ user.phone }}</div>
            </div>
            <div>
              <div class="text-sm opacity-60">Data di Nascita</div>
              <div class="font-medium">{{ formatDate(user.date_of_birth) }}</div>
            </div>
            <div>
              <div class="text-sm opacity-60">Codice Fiscale</div>
              <div class="font-medium font-mono">{{ user.fiscal_code }}</div>
            </div>
            <div>
              <div class="text-sm opacity-60">Stato Account</div>
              <div>
                <span class="badge" :class="statusBadgeClass">{{ statusLabel }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Driving Status Card -->
      <div class="card bg-base-100 shadow-md mb-6">
        <div class="card-body">
          <h2 class="card-title">Stato Guida</h2>

          <!-- Already verified -->
          <div v-if="user.driving_license_verified" class="mt-4">
            <div class="alert alert-success">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>I tuoi documenti sono stati verificati. Puoi noleggiare veicoli MOVO! 🚗</span>
            </div>
          </div>

          <!-- Pending approval -->
          <div v-else-if="user.status === 'pending_approval'" class="mt-4">
            <div class="alert alert-warning">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>I tuoi documenti sono in fase di verifica. Ti notificheremo appena saranno approvati.</span>
            </div>
          </div>

          <!-- Upload section -->
          <div v-else class="mt-4">
            <p class="text-sm opacity-70 mb-4">
              Per poter noleggiare i veicoli, devi caricare la patente di guida e un documento d'identità.
              Un operatore verificherà i tuoi documenti.
            </p>

            <!-- Upload success message -->
            <div v-if="uploadMessage" class="alert alert-success mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>{{ uploadMessage }}</span>
            </div>

            <!-- Upload error -->
            <div v-if="uploadError" class="alert alert-error mb-4">
              <span>{{ uploadError }}</span>
            </div>

            <!-- File Inputs -->
            <div v-if="canUploadDocuments" class="space-y-4">
              <div class="form-control">
                <label class="label">
                  <span class="label-text font-medium">📄 Patente di Guida *</span>
                </label>
                <input
                  type="file"
                  accept="image/jpeg,image/png,image/webp"
                  class="file-input file-input-bordered w-full"
                  @change="(e) => onFileChange(e, 'driving_license')"
                />
                <label class="label">
                  <span class="label-text-alt">Foto fronte della patente (JPEG, PNG, WebP, max 5MB)</span>
                </label>
              </div>

              <div class="form-control">
                <label class="label">
                  <span class="label-text font-medium">🪪 Documento d'Identità *</span>
                </label>
                <input
                  type="file"
                  accept="image/jpeg,image/png,image/webp"
                  class="file-input file-input-bordered w-full"
                  @change="(e) => onFileChange(e, 'identity_document')"
                />
                <label class="label">
                  <span class="label-text-alt">Foto fronte del documento (JPEG, PNG, WebP, max 5MB)</span>
                </label>
              </div>

              <button
                @click="uploadDocuments"
                class="btn btn-primary w-full"
                :disabled="isUploading || !drivingLicenseFile || !identityDocumentFile"
              >
                <span v-if="isUploading" class="loading loading-spinner loading-sm"></span>
                <span v-if="!isUploading">Carica Documenti</span>
                <span v-else>Caricamento in corso...</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
