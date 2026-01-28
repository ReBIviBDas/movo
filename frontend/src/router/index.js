import { createRouter, createWebHistory } from 'vue-router'
import { requireAuth, requireRole, redirectIfAuth } from './guards'

// Public views
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'

// User views
import UserDashboardView from '@/views/user/DashboardView.vue'

// Operator views
import OperatorDashboardView from '@/views/operator/DashboardView.vue'
import PendingUsersView from '@/views/operator/PendingUsersView.vue'
import FleetView from '@/views/operator/FleetView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ========================================
    // PUBLIC ROUTES
    // ========================================
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      beforeEnter: redirectIfAuth
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      beforeEnter: redirectIfAuth
    },

    // ========================================
    // USER ROUTES (Authenticated users)
    // ========================================
    {
      path: '/dashboard',
      name: 'user-dashboard',
      component: UserDashboardView,
      beforeEnter: requireAuth
    },

    // ========================================
    // OPERATOR ROUTES (Operators and Admins only)
    // ========================================
    {
      path: '/operator',
      name: 'operator-dashboard',
      component: OperatorDashboardView,
      beforeEnter: [requireAuth, requireRole('operator', 'admin')]
    },
    {
      path: '/operator/pending',
      name: 'operator-pending',
      component: PendingUsersView,
      beforeEnter: [requireAuth, requireRole('operator', 'admin')]
    },
    {
      path: '/operator/fleet',
      name: 'operator-fleet',
      component: FleetView,
      beforeEnter: [requireAuth, requireRole('operator', 'admin')]
    }
  ]
})

export default router
