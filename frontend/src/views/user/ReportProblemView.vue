<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50">
    <!-- Header -->
    <div class="bg-white shadow-sm border-b">
      <div class="max-w-4xl mx-auto px-4 py-4">
        <div class="flex items-center gap-3">
          <router-link to="/dashboard" class="btn btn-ghost btn-sm">
            ← Torna alla Dashboard
          </router-link>
          <h1 class="text-2xl font-bold text-gray-800">📝 Segnala un Problema</h1>
        </div>
      </div>
    </div>

    <div class="max-w-4xl mx-auto px-4 py-8">
      <!-- Success Message -->
      <div v-if="submitted" class="card bg-gradient-to-r from-green-500 to-emerald-600 text-white shadow-2xl">
        <div class="card-body text-center py-12">
          <div class="text-6xl mb-4">✅</div>
          <h2 class="text-3xl font-bold mb-2">Segnalazione Inviata!</h2>
          <p class="text-lg opacity-90 mb-4">La tua segnalazione è stata ricevuta e sarà gestita al più presto.</p>
          <div class="bg-white/20 rounded-xl p-4 inline-block">
            <p class="text-sm opacity-80">Numero di riferimento</p>
            <p class="text-2xl font-mono font-bold">{{ referenceId }}</p>
          </div>
          <div class="mt-6 flex gap-3 justify-center">
            <router-link to="/my-reports" class="btn btn-white">
              📋 Le Mie Segnalazioni
            </router-link>
            <router-link to="/dashboard" class="btn btn-outline btn-white">
              🏠 Dashboard
            </router-link>
          </div>
        </div>
      </div>

      <!-- Report Form -->
      <div v-else class="card bg-white shadow-xl">
        <div class="card-body">
          <form @submit.prevent="submitReport">
            <!-- Category -->
            <div class="form-control mb-6">
              <label class="label">
                <span class="label-text font-semibold text-lg">Categoria del problema *</span>
              </label>
              <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                <button 
                  v-for="cat in categories" 
                  :key="cat.value"
                  type="button"
                  @click="form.category = cat.value"
                  :class="[
                    'btn btn-outline h-auto py-4 flex flex-col gap-1',
                    form.category === cat.value ? 'btn-primary' : ''
                  ]"
                >
                  <span class="text-2xl">{{ cat.icon }}</span>
                  <span class="text-xs">{{ cat.label }}</span>
                </button>
              </div>
              <p v-if="errors.category" class="text-error text-sm mt-1">{{ errors.category }}</p>
            </div>

            <!-- Vehicle Selection (optional) -->
            <div class="form-control mb-6">
              <label class="label">
                <span class="label-text font-semibold">Veicolo coinvolto (opzionale)</span>
              </label>
              <select v-model="form.vehicle_id" class="select select-bordered w-full">
                <option value="">-- Nessun veicolo specifico --</option>
                <option v-for="rental in recentRentals" :key="rental.id" :value="rental.vehicle_id">
                  {{ rental.vehicle?.plate || 'N/D' }} - {{ rental.vehicle?.model || 'Veicolo' }} ({{ formatDate(rental.ended_at) }})
                </option>
              </select>
            </div>

            <!-- Description -->
            <div class="form-control mb-6">
              <label class="label">
                <span class="label-text font-semibold text-lg">Descrivi il problema *</span>
              </label>
              <textarea 
                v-model="form.description"
                class="textarea textarea-bordered h-40 text-base"
                placeholder="Descrivi dettagliatamente il problema riscontrato..."
                maxlength="2000"
              ></textarea>
              <label class="label">
                <span class="label-text-alt text-gray-500">{{ form.description.length }}/2000 caratteri</span>
              </label>
              <p v-if="errors.description" class="text-error text-sm">{{ errors.description }}</p>
            </div>

            <!-- Photo Upload -->
            <div class="form-control mb-6">
              <label class="label">
                <span class="label-text font-semibold">Aggiungi foto (max 5)</span>
              </label>
              <div 
                class="border-2 border-dashed border-gray-300 rounded-xl p-8 text-center cursor-pointer hover:border-primary transition-colors"
                @click="$refs.fileInput.click()"
                @dragover.prevent="dragOver = true"
                @dragleave="dragOver = false"
                @drop.prevent="handleDrop"
                :class="{ 'border-primary bg-primary/5': dragOver }"
              >
                <input 
                  type="file" 
                  ref="fileInput" 
                  class="hidden" 
                  accept="image/*" 
                  multiple 
                  @change="handleFileSelect"
                />
                <div class="text-4xl mb-2">📸</div>
                <p class="text-gray-600">Trascina le immagini qui o clicca per selezionare</p>
                <p class="text-sm text-gray-400 mt-1">JPEG, PNG o WebP (max 5MB ciascuna)</p>
              </div>
              
              <!-- Preview -->
              <div v-if="photoPreviewUrls.length > 0" class="flex flex-wrap gap-3 mt-4">
                <div v-for="(url, index) in photoPreviewUrls" :key="index" class="relative">
                  <img :src="url" class="w-24 h-24 object-cover rounded-lg" />
                  <button 
                    type="button"
                    @click="removePhoto(index)"
                    class="absolute -top-2 -right-2 btn btn-circle btn-xs btn-error"
                  >×</button>
                </div>
              </div>
            </div>

            <!-- Location -->
            <div class="form-control mb-6">
              <label class="label">
                <span class="label-text font-semibold">Posizione (opzionale)</span>
              </label>
              <div class="flex gap-3">
                <button 
                  type="button" 
                  @click="captureLocation"
                  :class="['btn', form.lat ? 'btn-success' : 'btn-outline']"
                  :disabled="capturingLocation"
                >
                  <span v-if="capturingLocation" class="loading loading-spinner loading-sm"></span>
                  <span v-else>📍</span>
                  {{ form.lat ? 'Posizione acquisita' : 'Usa posizione attuale' }}
                </button>
                <span v-if="form.lat" class="text-sm text-gray-500 self-center">
                  {{ form.lat.toFixed(4) }}, {{ form.lng.toFixed(4) }}
                </span>
              </div>
            </div>

            <!-- Submit -->
            <div class="flex gap-3 mt-8">
              <button 
                type="submit" 
                class="btn btn-primary btn-lg flex-1"
                :disabled="submitting"
              >
                <span v-if="submitting" class="loading loading-spinner"></span>
                <span v-else>📤</span>
                Invia Segnalazione
              </button>
              <router-link to="/dashboard" class="btn btn-ghost btn-lg">
                Annulla
              </router-link>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { authState } from '@/states/auth'
import { API_BASE_URL } from '@/config/api'

const categories = [
  { value: 'vehicle_damage', label: 'Danni veicolo', icon: '🚗💥' },
  { value: 'vehicle_malfunction', label: 'Guasto', icon: '⚠️' },
  { value: 'accident', label: 'Incidente', icon: '🚨' },
  { value: 'parking_issue', label: 'Parcheggio', icon: '🅿️' },
  { value: 'app_issue', label: 'Problema app', icon: '📱' },
  { value: 'payment_issue', label: 'Pagamento', icon: '💳' },
  { value: 'other', label: 'Altro', icon: '❓' }
]

const form = reactive({
  category: '',
  description: '',
  vehicle_id: '',
  lat: null,
  lng: null
})

const photos = ref([])
const photoPreviewUrls = ref([])
const recentRentals = ref([])
const errors = reactive({})
const submitting = ref(false)
const submitted = ref(false)
const referenceId = ref('')
const dragOver = ref(false)
const capturingLocation = ref(false)

onMounted(async () => {
  await fetchRecentRentals()
})

async function fetchRecentRentals() {
  try {
    const response = await fetch(`${API_BASE_URL}/rentals/history?limit=5`, {
      headers: { 'Authorization': `Bearer ${authState.token}` }
    })
    if (response.ok) {
      const data = await response.json()
      recentRentals.value = data.rentals || []
    }
  } catch (err) {
    console.error('Error fetching rentals:', err)
  }
}

function handleFileSelect(event) {
  addPhotos(Array.from(event.target.files))
}

function handleDrop(event) {
  dragOver.value = false
  addPhotos(Array.from(event.dataTransfer.files))
}

function addPhotos(files) {
  const imageFiles = files.filter(f => f.type.startsWith('image/'))
  const remaining = 5 - photos.value.length
  const toAdd = imageFiles.slice(0, remaining)
  
  toAdd.forEach(file => {
    photos.value.push(file)
    photoPreviewUrls.value.push(URL.createObjectURL(file))
  })
}

function removePhoto(index) {
  URL.revokeObjectURL(photoPreviewUrls.value[index])
  photos.value.splice(index, 1)
  photoPreviewUrls.value.splice(index, 1)
}

async function captureLocation() {
  if (!navigator.geolocation) {
    alert('Geolocalizzazione non supportata')
    return
  }
  
  capturingLocation.value = true
  try {
    const position = await new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject, {
        enableHighAccuracy: true,
        timeout: 10000
      })
    })
    form.lat = position.coords.latitude
    form.lng = position.coords.longitude
  } catch (err) {
    alert('Impossibile acquisire la posizione: ' + err.message)
  } finally {
    capturingLocation.value = false
  }
}

function validate() {
  errors.category = ''
  errors.description = ''
  
  if (!form.category) {
    errors.category = 'Seleziona una categoria'
  }
  if (!form.description.trim()) {
    errors.description = 'Inserisci una descrizione del problema'
  } else if (form.description.trim().length < 10) {
    errors.description = 'La descrizione deve essere di almeno 10 caratteri'
  }
  
  return !errors.category && !errors.description
}

async function submitReport() {
  if (!validate()) return
  
  submitting.value = true
  
  try {
    const formData = new FormData()
    formData.append('category', form.category)
    formData.append('description', form.description.trim())
    
    if (form.vehicle_id) {
      formData.append('vehicle_id', form.vehicle_id)
    }
    if (form.lat && form.lng) {
      formData.append('lat', form.lat)
      formData.append('lng', form.lng)
    }
    
    photos.value.forEach(photo => {
      formData.append('photos', photo)
    })
    
    const response = await fetch(`${API_BASE_URL}/issues`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authState.token}`
      },
      body: formData
    })
    
    if (!response.ok) {
      const error = await response.json()
      throw new Error(error.detail || 'Errore durante l\'invio')
    }
    
    const data = await response.json()
    referenceId.value = data.report.reference_id
    submitted.value = true
    
  } catch (err) {
    alert('Errore: ' + err.message)
  } finally {
    submitting.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('it-IT', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}
</script>
