<script setup>
import { authState, isAuthenticated } from '@/states/auth'
import { RouterLink } from 'vue-router'
import VehicleMap from '@/components/VehicleMap.vue'
</script>

<template>
  <div class="min-h-[70vh] flex flex-col items-center">
    <!-- Hero Section (for guests) -->
    <div v-if="!isAuthenticated" class="hero bg-base-100 rounded-box shadow-xl p-8 max-w-4xl my-8">
      <div class="hero-content text-center">
        <div class="max-w-md">
          <h1 class="text-5xl font-bold text-primary">⚡ MOVO</h1>
          <p class="py-6 text-lg">
            Car sharing elettrico al 100% a Trento. 
            Prenota, sblocca e guida in pochi minuti.
          </p>
          
          <div class="flex gap-4 justify-center">
            <RouterLink to="/register" class="btn btn-primary btn-lg">
              Inizia Ora
            </RouterLink>
            <RouterLink to="/login" class="btn btn-outline btn-lg">
              Accedi
            </RouterLink>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Authenticated User View with Map -->
    <div v-else class="w-full max-w-6xl px-4 py-6">
      <!-- Header -->
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 gap-4">
        <div>
          <h1 class="text-3xl font-bold">Trova un Veicolo</h1>
          <p class="text-base-content/60">Clicca su un marker per vedere i dettagli e prenotare</p>
        </div>
        <div class="flex gap-2">
          <RouterLink to="/dashboard" class="btn btn-outline btn-sm">
            📊 Dashboard
          </RouterLink>
        </div>
      </div>
      
      <!-- Vehicle Map -->
      <VehicleMap />
      
      <!-- Quick Actions -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mt-6">
        <div class="card bg-base-100 shadow-md">
          <div class="card-body">
            <h3 class="card-title text-lg">📍 Veicoli Vicini</h3>
            <p class="text-sm text-base-content/70">Trova auto elettriche nella tua zona</p>
          </div>
        </div>
        <div class="card bg-base-100 shadow-md">
          <div class="card-body">
            <h3 class="card-title text-lg">🔋 100% Elettrico</h3>
            <p class="text-sm text-base-content/70">Guida sostenibile con la nostra flotta green</p>
          </div>
        </div>
        <div class="card bg-base-100 shadow-md">
          <div class="card-body">
            <h3 class="card-title text-lg">💳 Paga per Minuto</h3>
            <p class="text-sm text-base-content/70">Solo €0.35/min per le nostre auto elettriche</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Features Section (for guests) -->
    <div v-if="!isAuthenticated" class="grid grid-cols-1 md:grid-cols-3 gap-6 mt-12 max-w-4xl px-4">
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
