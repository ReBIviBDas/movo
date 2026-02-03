<script setup>
import { ref, onMounted, computed } from 'vue'
import { RouterLink } from 'vue-router'
import { API_BASE_URL } from '@/config/api'
import { authState } from '@/states/auth'

const paymentMethods = ref([])
const isLoading = ref(true)
const error = ref('')
const showAddModal = ref(false)

// Form data
const form = ref({
  card_number: '',
  expiry_month: '',
  expiry_year: '',
  cvv: '',
  nickname: ''
})
const formError = ref('')
const isSubmitting = ref(false)

// Card type detection
const detectedCardType = computed(() => {
  const num = form.value.card_number.replace(/\s/g, '')
  if (/^4/.test(num)) return 'visa'
  if (/^5[1-5]/.test(num)) return 'mastercard'
  if (/^3[47]/.test(num)) return 'amex'
  return null
})

// Format card number with spaces
function formatCardNumber(e) {
  let value = e.target.value.replace(/\D/g, '')
  value = value.match(/.{1,4}/g)?.join(' ') || value
  form.value.card_number = value.substring(0, 19)
}

// Fetch payment methods
async function fetchPaymentMethods() {
  isLoading.value = true
  error.value = ''
  
  try {
    const response = await fetch(`${API_BASE_URL}/payment-methods`, {
      headers: { 'Authorization': `Bearer ${authState.token}` }
    })
    
    if (!response.ok) throw new Error('Failed to fetch')
    
    const data = await response.json()
    paymentMethods.value = data.payment_methods
  } catch (err) {
    error.value = 'Errore nel caricamento dei metodi di pagamento'
  } finally {
    isLoading.value = false
  }
}

// Add payment method
async function addPaymentMethod() {
  formError.value = ''
  isSubmitting.value = true
  
  try {
    const response = await fetch(`${API_BASE_URL}/payment-methods`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authState.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        card_number: form.value.card_number.replace(/\s/g, ''),
        expiry_month: parseInt(form.value.expiry_month),
        expiry_year: parseInt(form.value.expiry_year),
        cvv: form.value.cvv,
        nickname: form.value.nickname || undefined
      })
    })
    
    const data = await response.json()
    
    if (!response.ok) {
      formError.value = data.detail || 'Errore durante l\'aggiunta'
      return
    }
    
    showAddModal.value = false
    resetForm()
    fetchPaymentMethods()
  } catch (err) {
    formError.value = 'Errore di connessione'
  } finally {
    isSubmitting.value = false
  }
}

// Set as default
async function setDefault(methodId) {
  try {
    const response = await fetch(`${API_BASE_URL}/payment-methods/${methodId}/default`, {
      method: 'PATCH',
      headers: { 'Authorization': `Bearer ${authState.token}` }
    })
    
    if (response.ok) {
      fetchPaymentMethods()
    }
  } catch (err) {
    alert('Errore durante l\'aggiornamento')
  }
}

// Delete method
async function deleteMethod(methodId) {
  if (!confirm('Sei sicuro di voler eliminare questo metodo di pagamento?')) return
  
  try {
    const response = await fetch(`${API_BASE_URL}/payment-methods/${methodId}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${authState.token}` }
    })
    
    const data = await response.json()
    
    if (!response.ok) {
      alert(data.detail || 'Errore durante l\'eliminazione')
      return
    }
    
    fetchPaymentMethods()
  } catch (err) {
    alert('Errore di connessione')
  }
}

// Reset form
function resetForm() {
  form.value = {
    card_number: '',
    expiry_month: '',
    expiry_year: '',
    cvv: '',
    nickname: ''
  }
  formError.value = ''
}

// Get card icon
function getCardIcon(type) {
  const icons = {
    visa: '💳',
    mastercard: '💳',
    amex: '💳'
  }
  return icons[type] || '💳'
}

// Generate year options
const yearOptions = computed(() => {
  const currentYear = new Date().getFullYear()
  return Array.from({ length: 10 }, (_, i) => currentYear + i)
})

onMounted(() => {
  fetchPaymentMethods()
})
</script>

<template>
  <div>
    <!-- Header -->
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 gap-4">
      <div>
        <h1 class="text-2xl font-bold">💳 Portafoglio</h1>
        <p class="text-base-content/60">Gestisci i tuoi metodi di pagamento</p>
      </div>
      <div class="flex gap-2">
        <RouterLink to="/dashboard" class="btn btn-ghost btn-sm">← Dashboard</RouterLink>
        <button @click="showAddModal = true" class="btn btn-primary btn-sm">
          + Aggiungi Carta
        </button>
      </div>
    </div>
    
    <!-- Loading -->
    <div v-if="isLoading" class="flex justify-center py-12">
      <span class="loading loading-spinner loading-lg"></span>
    </div>
    
    <!-- Error -->
    <div v-else-if="error" class="alert alert-error">
      <span>{{ error }}</span>
      <button @click="fetchPaymentMethods" class="btn btn-sm">Riprova</button>
    </div>
    
    <!-- Empty State -->
    <div v-else-if="paymentMethods.length === 0" class="text-center py-12">
      <div class="text-6xl mb-4">💳</div>
      <h2 class="text-xl font-semibold mb-2">Nessun metodo di pagamento</h2>
      <p class="text-base-content/60 mb-4">Aggiungi una carta per prenotare veicoli</p>
      <button @click="showAddModal = true" class="btn btn-primary">
        + Aggiungi Carta
      </button>
    </div>
    
    <!-- Payment Methods List -->
    <div v-else class="grid gap-4 max-w-2xl">
      <div 
        v-for="method in paymentMethods" 
        :key="method.id"
        class="card bg-base-100 shadow-md"
        :class="{ 'ring-2 ring-primary': method.is_default }"
      >
        <div class="card-body flex-row items-center justify-between">
          <div class="flex items-center gap-4">
            <span class="text-3xl">{{ getCardIcon(method.card_type) }}</span>
            <div>
              <div class="font-bold">
                {{ method.card_type.toUpperCase() }} •••• {{ method.last_four }}
                <span v-if="method.is_default" class="badge badge-primary badge-sm ml-2">Default</span>
              </div>
              <div class="text-sm text-base-content/60">
                Scade: {{ method.expiry_month }}/{{ method.expiry_year }}
                <span v-if="method.nickname" class="ml-2">({{ method.nickname }})</span>
              </div>
            </div>
          </div>
          <div class="flex gap-2">
            <button 
              v-if="!method.is_default"
              @click="setDefault(method.id)" 
              class="btn btn-xs btn-outline"
            >
              Imposta Default
            </button>
            <button @click="deleteMethod(method.id)" class="btn btn-xs btn-error btn-outline">
              🗑️
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Add Card Modal -->
    <div v-if="showAddModal" class="modal modal-open">
      <div class="modal-box">
        <h3 class="font-bold text-lg mb-4">Aggiungi Carta</h3>
        
        <div v-if="formError" class="alert alert-error mb-4">
          <span>{{ formError }}</span>
        </div>
        
        <form @submit.prevent="addPaymentMethod" class="space-y-4">
          <div class="form-control">
            <label class="label">
              <span class="label-text">Numero Carta</span>
              <span v-if="detectedCardType" class="label-text-alt uppercase">{{ detectedCardType }}</span>
            </label>
            <input 
              v-model="form.card_number"
              @input="formatCardNumber"
              type="text"
              inputmode="numeric"
              placeholder="1234 5678 9012 3456"
              class="input input-bordered"
              required
            />
          </div>
          
          <div class="grid grid-cols-3 gap-4">
            <div class="form-control">
              <label class="label"><span class="label-text">Mese</span></label>
              <select v-model="form.expiry_month" class="select select-bordered" required>
                <option value="">MM</option>
                <option v-for="m in 12" :key="m" :value="m">{{ m.toString().padStart(2, '0') }}</option>
              </select>
            </div>
            <div class="form-control">
              <label class="label"><span class="label-text">Anno</span></label>
              <select v-model="form.expiry_year" class="select select-bordered" required>
                <option value="">YYYY</option>
                <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}</option>
              </select>
            </div>
            <div class="form-control">
              <label class="label"><span class="label-text">CVV</span></label>
              <input 
                v-model="form.cvv"
                type="password"
                inputmode="numeric"
                maxlength="4"
                placeholder="123"
                class="input input-bordered"
                required
              />
            </div>
          </div>
          
          <div class="form-control">
            <label class="label"><span class="label-text">Nome Carta (opzionale)</span></label>
            <input 
              v-model="form.nickname"
              type="text"
              placeholder="es. Carta Personale"
              class="input input-bordered"
            />
          </div>
          
          <div class="modal-action">
            <button type="button" @click="showAddModal = false; resetForm()" class="btn">
              Annulla
            </button>
            <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="loading loading-spinner loading-sm"></span>
              Aggiungi
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
