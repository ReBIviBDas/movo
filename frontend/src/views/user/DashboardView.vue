<script setup>
import { RouterLink } from 'vue-router'
import { authState } from '@/states/auth'
</script>

<template>
  <div class="min-h-[70vh]">
    <!-- Welcome Section -->
    <div class="hero bg-base-100 rounded-box shadow-lg mb-6">
      <div class="hero-content text-center py-8">
        <div>
          <h1 class="text-3xl font-bold">
            Benvenuto, {{ authState.user?.first_name || 'Utente' }}! 👋
          </h1>
          <p class="py-4 text-lg opacity-70">
            Cosa vuoi fare oggi?
          </p>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <!-- Find Vehicle -->
      <div class="card bg-base-100 shadow-md hover:shadow-xl transition-shadow">
        <div class="card-body">
          <div class="text-4xl mb-2">🚗</div>
          <h2 class="card-title">Trova un Veicolo</h2>
          <p>Cerca veicoli disponibili sulla mappa</p>
          <div class="card-actions justify-end mt-4">
            <RouterLink to="/map" class="btn btn-primary">
              Apri Mappa
            </RouterLink>
          </div>
        </div>
      </div>

      <!-- My Rentals -->
      <div class="card bg-base-100 shadow-md hover:shadow-xl transition-shadow">
        <div class="card-body">
          <div class="text-4xl mb-2">📋</div>
          <h2 class="card-title">I Miei Noleggi</h2>
          <p>Visualizza storico e noleggi attivi</p>
          <div class="card-actions justify-end mt-4">
            <RouterLink to="/rentals" class="btn btn-outline">
              Vedi Noleggi
            </RouterLink>
          </div>
        </div>
      </div>

      <!-- Wallet -->
      <div class="card bg-base-100 shadow-md hover:shadow-xl transition-shadow">
        <div class="card-body">
          <div class="text-4xl mb-2">💳</div>
          <h2 class="card-title">Portafoglio</h2>
          <p>Gestisci i metodi di pagamento</p>
          <div class="card-actions justify-end mt-4">
            <RouterLink to="/wallet" class="btn btn-outline">
              Gestisci
            </RouterLink>
          </div>
        </div>
      </div>
    </div>

    <!-- Account Status -->
    <div class="mt-8">
      <div class="card bg-base-100 shadow-md">
        <div class="card-body">
          <h2 class="card-title">Stato Account</h2>
          <div class="stats stats-vertical lg:stats-horizontal shadow mt-4">
            <div class="stat">
              <div class="stat-title">Stato</div>
              <div class="stat-value text-lg capitalize">
                {{ authState.user?.status || 'N/A' }}
              </div>
            </div>
            <div class="stat">
              <div class="stat-title">Ruolo</div>
              <div class="stat-value text-lg capitalize">
                {{ authState.user?.role || 'user' }}
              </div>
            </div>
            <div class="stat">
              <div class="stat-title">Email</div>
              <div class="stat-value text-lg text-sm">
                {{ authState.user?.email || 'N/A' }}
              </div>
            </div>
          </div>

          <!-- Pending Approval Warning -->
          <div v-if="authState.user?.status === 'pending_approval'" class="alert alert-warning mt-4">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <span>I tuoi documenti sono in fase di verifica. Ti notificheremo appena approvati.</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
