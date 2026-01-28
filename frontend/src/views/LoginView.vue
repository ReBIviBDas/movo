<script setup>
import { ref, computed } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { API_BASE_URL } from '@/config/api'
import { setLoggedUser, authState } from '@/states/auth'

const router = useRouter()

// Form state
const email = ref('')
const password = ref('')
const isLoading = ref(false)
const isGoogleLoading = ref(false)
const errorMessage = ref('')

// Determine redirect based on role
function redirectByRole(role) {
  if (role === 'operator' || role === 'admin') {
    router.push('/operator')
  } else {
    router.push('/dashboard')
  }
}

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
    
    // Redirect based on role
    redirectByRole(data.user.role)
    
  } catch (err) {
    console.error('Login error:', err)
    errorMessage.value = 'Connection error. Please check if the server is running.'
  } finally {
    isLoading.value = false
  }
}

// Google OAuth Configuration
const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID || ''

// Handle Google Sign-In callback
async function handleGoogleCallback(response) {
  isGoogleLoading.value = true
  errorMessage.value = ''
  
  try {
    const res = await fetch(`${API_BASE_URL}/auth/login/google`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        credential: response.credential
      })
    })
    
    const data = await res.json()
    
    if (!res.ok) {
      errorMessage.value = data.detail || 'Google login failed.'
      return
    }
    
    // Success
    setLoggedUser(data)
    redirectByRole(data.user.role)
    
  } catch (err) {
    console.error('Google login error:', err)
    errorMessage.value = 'Error during Google authentication.'
  } finally {
    isGoogleLoading.value = false
  }
}

// Initialize Google Sign-In when component mounts
function initGoogleSignIn() {
  if (!GOOGLE_CLIENT_ID) {
    console.warn('Google Client ID not configured')
    return
  }
  
  // Load Google Identity Services script
  if (!window.google) {
    const script = document.createElement('script')
    script.src = 'https://accounts.google.com/gsi/client'
    script.async = true
    script.defer = true
    script.onload = renderGoogleButton
    document.head.appendChild(script)
  } else {
    renderGoogleButton()
  }
}

function renderGoogleButton() {
  if (!window.google || !GOOGLE_CLIENT_ID) return
  
  window.google.accounts.id.initialize({
    client_id: GOOGLE_CLIENT_ID,
    callback: handleGoogleCallback
  })
  
  window.google.accounts.id.renderButton(
    document.getElementById('google-signin-button'),
    { 
      theme: 'outline', 
      size: 'large',
      width: '100%',
      text: 'signin_with'
    }
  )
}

// Initialize on mount
import { onMounted } from 'vue'
onMounted(() => {
  initGoogleSignIn()
})

const showGoogleButton = computed(() => !!GOOGLE_CLIENT_ID)
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

        <!-- Google Sign In Button -->
        <div v-if="showGoogleButton" class="mb-4">
          <div id="google-signin-button" class="flex justify-center"></div>
          <div v-if="isGoogleLoading" class="text-center mt-2">
            <span class="loading loading-spinner loading-sm"></span>
            <span class="ml-2">Accesso con Google...</span>
          </div>
        </div>

        <div v-if="showGoogleButton" class="divider">oppure</div>
        
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
              :disabled="isLoading"
            >
              <span v-if="isLoading" class="loading loading-spinner loading-sm"></span>
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
