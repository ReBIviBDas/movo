<script setup>
import { authState, isAuthenticated } from '@/states/auth'
import { RouterLink } from 'vue-router'
</script>

<template>
  <div class="min-h-[70vh] flex flex-col items-center justify-center">
    <!-- Hero Section -->
    <div class="hero bg-base-100 rounded-box shadow-xl p-8 max-w-4xl">
      <div class="hero-content text-center">
        <div class="max-w-md">
          <h1 class="text-5xl font-bold text-primary">⚡ MOVO</h1>
          <p class="py-6 text-lg">
            Car sharing elettrico al 100% a Trento. 
            Prenota, sblocca e guida in pochi minuti.
          </p>
          
          <!-- Not authenticated -->
          <div v-if="!isAuthenticated" class="flex gap-4 justify-center">
            <RouterLink to="/register" class="btn btn-primary btn-lg">
              Inizia Ora
            </RouterLink>
            <RouterLink to="/login" class="btn btn-outline btn-lg">
              Accedi
            </RouterLink>
          </div>
          
          <!-- Authenticated -->
          <div v-else class="space-y-4">
            <div class="alert alert-success">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>Benvenuto, <strong>{{ authState.user?.email }}</strong>!</span>
            </div>
            <div class="stats shadow">
              <div class="stat">
                <div class="stat-title">Ruolo</div>
                <div class="stat-value text-primary capitalize">{{ authState.user?.role }}</div>
              </div>
              <div class="stat">
                <div class="stat-title">Stato</div>
                <div class="stat-value text-secondary capitalize">{{ authState.user?.status }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Features Section -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mt-12 max-w-4xl">
      <div class="card bg-base-100 shadow-md">
        <div class="card-body items-center text-center">
          <span class="text-4xl">🔋</span>
          <h2 class="card-title">100% Elettrico</h2>
          <p>Flotta completamente elettrica per una mobilità sostenibile.</p>
        </div>
      </div>
      <div class="card bg-base-100 shadow-md">
        <div class="card-body items-center text-center">
          <span class="text-4xl">📱</span>
          <h2 class="card-title">Facile da Usare</h2>
          <p>Prenota e sblocca il veicolo direttamente dall'app.</p>
        </div>
      </div>
      <div class="card bg-base-100 shadow-md">
        <div class="card-body items-center text-center">
          <span class="text-4xl">💰</span>
          <h2 class="card-title">Conveniente</h2>
          <p>Paga solo per il tempo che usi, senza costi nascosti.</p>
        </div>
      </div>
    </div>
  </div>
</template>
