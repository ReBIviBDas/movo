<script setup>
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { API_BASE_URL } from '@/config/api'
import { setLoggedUser } from '@/states/auth'

const router = useRouter()

// Form state
const email = ref('')
const password = ref('')
const isLoading = ref(false)
const errorMessage = ref('')

// Handle login form submission
async function handleLogin() {
  isLoading.value = true
  errorMessage.value = ''
  
  try {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        email: email.value,
        password: password.value
      })
    })
    
    const data = await response.json()
    
    if (!response.ok) {
      // Handle error (uses OpenAPI ProblemDetails format)
      errorMessage.value = data.detail || 'Login failed. Please try again.'
      return
    }
    
    // Success - store token and user info
    setLoggedUser(data)
    
    // Redirect to home
    router.push('/')
    
  } catch (err) {
    console.error('Login error:', err)
    errorMessage.value = 'Connection error. Please check if the server is running.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="min-h-[70vh] flex items-center justify-center">
    <div class="card w-full max-w-md bg-base-100 shadow-xl">
      <div class="card-body">
        <h2 class="card-title text-2xl font-bold text-center justify-center mb-4">
          Accedi a MOVO
        </h2>
        
        <!-- Error Alert -->
        <div v-if="errorMessage" class="alert alert-error mb-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span>{{ errorMessage }}</span>
        </div>
        
        <!-- Login Form -->
        <form @submit.prevent="handleLogin" class="space-y-4">
          <!-- Email -->
          <div class="form-control">
            <label class="label">
              <span class="label-text">Email</span>
            </label>
            <input 
              v-model="email"
              type="email" 
              placeholder="mario.rossi@example.com" 
              class="input input-bordered w-full" 
              required
            />
          </div>
          
          <!-- Password -->
          <div class="form-control">
            <label class="label">
              <span class="label-text">Password</span>
            </label>
            <input 
              v-model="password"
              type="password" 
              placeholder="••••••••" 
              class="input input-bordered w-full" 
              required
              minlength="8"
            />
          </div>
          
          <!-- Submit Button -->
          <div class="form-control mt-6">
            <button 
              type="submit" 
              class="btn btn-primary w-full"
              :class="{ 'loading': isLoading }"
              :disabled="isLoading"
            >
              <span v-if="!isLoading">Accedi</span>
              <span v-else>Accesso in corso...</span>
            </button>
          </div>
        </form>
        
        <!-- Register Link -->
        <div class="divider">oppure</div>
        <p class="text-center">
          Non hai un account? 
          <RouterLink to="/register" class="link link-primary font-semibold">
            Registrati
          </RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>
