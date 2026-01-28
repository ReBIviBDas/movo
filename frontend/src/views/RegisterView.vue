<script setup>
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { API_BASE_URL } from '@/config/api'

const router = useRouter()

// Form state (matching OpenAPI spec field names)
const formData = ref({
  first_name: '',
  last_name: '',
  email: '',
  password: '',
  phone: '',
  date_of_birth: '',
  fiscal_code: '',
  accept_terms: false,
  accept_privacy: false
})

const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// Handle registration form submission
async function handleRegister() {
  isLoading.value = true
  errorMessage.value = ''
  successMessage.value = ''
  
  // Validate terms acceptance
  if (!formData.value.accept_terms || !formData.value.accept_privacy) {
    errorMessage.value = 'Devi accettare i Termini di Servizio e la Privacy Policy.'
    isLoading.value = false
    return
  }
  
  try {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(formData.value)
    })
    
    const data = await response.json()
    
    if (!response.ok) {
      // Handle error (uses OpenAPI ProblemDetails format)
      errorMessage.value = data.detail || 'Registrazione fallita. Riprova.'
      return
    }
    
    // Success - show message and redirect to login
    successMessage.value = data.message || 'Registrazione completata!'
    
    // Redirect to login after 2 seconds
    setTimeout(() => {
      router.push('/login')
    }, 2000)
    
  } catch (err) {
    console.error('Registration error:', err)
    errorMessage.value = 'Errore di connessione. Verifica che il server sia attivo.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="min-h-[70vh] flex items-center justify-center py-8">
    <div class="card w-full max-w-lg bg-base-100 shadow-xl">
      <div class="card-body">
        <h2 class="card-title text-2xl font-bold text-center justify-center mb-4">
          Registrati a MOVO
        </h2>
        
        <!-- Error Alert -->
        <div v-if="errorMessage" class="alert alert-error mb-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span>{{ errorMessage }}</span>
        </div>
        
        <!-- Success Alert -->
        <div v-if="successMessage" class="alert alert-success mb-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span>{{ successMessage }} Reindirizzamento al login...</span>
        </div>
        
        <!-- Registration Form -->
        <form @submit.prevent="handleRegister" class="space-y-4">
          <!-- Name Row -->
          <div class="grid grid-cols-2 gap-4">
            <div class="form-control">
              <label class="label">
                <span class="label-text">Nome *</span>
              </label>
              <input 
                v-model="formData.first_name"
                type="text" 
                placeholder="Mario" 
                class="input input-bordered w-full" 
                required
              />
            </div>
            <div class="form-control">
              <label class="label">
                <span class="label-text">Cognome *</span>
              </label>
              <input 
                v-model="formData.last_name"
                type="text" 
                placeholder="Rossi" 
                class="input input-bordered w-full" 
                required
              />
            </div>
          </div>
          
          <!-- Email -->
          <div class="form-control">
            <label class="label">
              <span class="label-text">Email *</span>
            </label>
            <input 
              v-model="formData.email"
              type="email" 
              placeholder="mario.rossi@example.com" 
              class="input input-bordered w-full" 
              required
            />
          </div>
          
          <!-- Password -->
          <div class="form-control">
            <label class="label">
              <span class="label-text">Password *</span>
            </label>
            <input 
              v-model="formData.password"
              type="password" 
              placeholder="Minimo 8 caratteri" 
              class="input input-bordered w-full" 
              required
              minlength="8"
            />
          </div>
          
          <!-- Phone -->
          <div class="form-control">
            <label class="label">
              <span class="label-text">Telefono *</span>
            </label>
            <input 
              v-model="formData.phone"
              type="tel" 
              placeholder="+393331234567" 
              class="input input-bordered w-full" 
              required
            />
          </div>
          
          <!-- Date of Birth -->
          <div class="form-control">
            <label class="label">
              <span class="label-text">Data di Nascita *</span>
            </label>
            <input 
              v-model="formData.date_of_birth"
              type="date" 
              class="input input-bordered w-full" 
              required
            />
          </div>
          
          <!-- Fiscal Code -->
          <div class="form-control">
            <label class="label">
              <span class="label-text">Codice Fiscale *</span>
            </label>
            <input 
              v-model="formData.fiscal_code"
              type="text" 
              placeholder="RSSMRA95C15L378X" 
              class="input input-bordered w-full uppercase" 
              required
              minlength="16"
              maxlength="16"
              title="Inserisci un codice fiscale valido (16 caratteri)"
            />
          </div>

          <!-- Optional: Document Upload Section -->
          <div class="collapse collapse-arrow bg-base-200 mt-4">
            <input type="checkbox" /> 
            <div class="collapse-title font-medium">
              📄 Vuoi guidare? Carica i documenti (opzionale)
            </div>
            <div class="collapse-content">
              <p class="text-sm mb-4 opacity-70">
                Per poter noleggiare i veicoli MOVO, devi caricare patente e documento d'identità.
                Puoi farlo ora o in seguito dal tuo profilo.
              </p>
              <div class="alert alert-info mb-4">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span class="text-sm">I documenti verranno verificati da un operatore prima di abilitarti alla guida.</span>
              </div>
              <p class="text-sm text-center opacity-60">
                Potrai caricare i documenti dopo la registrazione dalla sezione Profilo.
              </p>
            </div>
          </div>
          
          <!-- Terms Checkboxes -->
          <div class="space-y-2 mt-4">
            <label class="flex items-center gap-3 cursor-pointer">
              <input 
                v-model="formData.accept_terms"
                type="checkbox" 
                class="checkbox checkbox-primary" 
              />
              <span class="label-text">
                Accetto i <a href="#" class="link link-primary">Termini di Servizio</a> *
              </span>
            </label>
            <label class="flex items-center gap-3 cursor-pointer">
              <input 
                v-model="formData.accept_privacy"
                type="checkbox" 
                class="checkbox checkbox-primary" 
              />
              <span class="label-text">
                Accetto la <a href="#" class="link link-primary">Privacy Policy</a> *
              </span>
            </label>
          </div>
          
          <!-- Submit Button -->
          <div class="form-control mt-6">
            <button 
              type="submit" 
              class="btn btn-primary w-full"
              :disabled="isLoading"
            >
              <span v-if="isLoading" class="loading loading-spinner"></span>
              <span v-if="!isLoading">Registrati</span>
              <span v-else>Registrazione in corso...</span>
            </button>
          </div>
        </form>
        
        <!-- Login Link -->
        <div class="divider">oppure</div>
        <p class="text-center">
          Hai già un account? 
          <RouterLink to="/login" class="link link-primary font-semibold">
            Accedi
          </RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>
