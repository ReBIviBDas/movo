import { reactive, computed } from 'vue'

// Reactive auth state (Simple Store pattern from lecture notes)
export const authState = reactive({
    token: null,
    user: null
})

// Computed property for authentication status
export const isAuthenticated = computed(() => !!authState.token)

// Set logged user data and persist to localStorage
export function setLoggedUser(data) {
    authState.token = data.access_token
    authState.user = data.user
    
    // Persist to localStorage
    localStorage.setItem('movo_token', data.access_token)
    localStorage.setItem('movo_user', JSON.stringify(data.user))
}

// Clear auth state and localStorage
export function logout() {
    authState.token = null
    authState.user = null
    localStorage.removeItem('movo_token')
    localStorage.removeItem('movo_user')
}

// Load auth state from localStorage (called on app init)
export function loadFromStorage() {
    const token = localStorage.getItem('movo_token')
    const userStr = localStorage.getItem('movo_user')
    
    if (token && userStr) {
        try {
            authState.token = token
            authState.user = JSON.parse(userStr)
        } catch (e) {
            // Invalid data in storage, clear it
            logout()
        }
    }
}
