<script setup>
import { authState, isAuthenticated } from '@/states/auth'
import { RouterLink } from 'vue-router'
import VehicleMap from '@/components/VehicleMap.vue'
</script>

<template>
  <div class="min-h-[70vh] flex flex-col items-center">
    <!-- Header Section -->
    <div class="w-full max-w-6xl px-4 py-6">
      <!-- Guest Header -->
      <div v-if="!isAuthenticated" class="text-center mb-6">
        <h1 class="text-4xl font-bold text-primary mb-2">MOVO</h1>
        <p class="text-lg text-base-content/70 mb-4">
          Car sharing elettrico al 100% a Trento
        </p>
        <div class="flex gap-4 justify-center mb-6">
          <RouterLink to="/register" class="btn btn-primary">
            Inizia Ora
          </RouterLink>
          <RouterLink to="/login" class="btn btn-outline">
            Accedi
          </RouterLink>
        </div>
      </div>
      
      <!-- Authenticated Header -->
      <div v-else class="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 gap-4">
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
      
      <!-- Vehicle Map (visible to everyone) -->
      <VehicleMap :is-public="!isAuthenticated" />
      
      <!-- Info Cards -->
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
      
      <!-- Features for guests -->
      <div v-if="!isAuthenticated" class="grid grid-cols-1 md:grid-cols-3 gap-6 mt-8">
        <div class="card bg-base-100 shadow-md">
          <div class="card-body items-center text-center">
            <span class="text-4xl">📱</span>
            <h2 class="card-title">Facile da Usare</h2>
            <p>Prenota e sblocca il veicolo direttamente dall'app.</p>
          </div>
        </div>
        <div class="card bg-base-100 shadow-md">
          <div class="card-body items-center text-center">
            <span class="text-4xl">🔓</span>
            <h2 class="card-title">Sblocco Istantaneo</h2>
            <p>Avvicinati al veicolo e sbloccalo con un tap.</p>
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
  </div>
</template>
