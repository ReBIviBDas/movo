import { createRouter, createWebHistory } from 'vue-router'
import { requireAuth, requireRole, redirectIfAuth } from './guards'

// Public views
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'

// User views
import UserDashboardView from '@/views/user/DashboardView.vue'
import WalletView from '@/views/user/WalletView.vue'
import ActiveBookingView from '@/views/user/ActiveBookingView.vue'
import ActiveRentalView from '@/views/user/ActiveRentalView.vue'

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
    {
      path: '/wallet',
      name: 'wallet',
      component: WalletView,
      beforeEnter: requireAuth
    },
    {
      path: '/booking',
      name: 'booking',
      component: ActiveBookingView,
      beforeEnter: requireAuth
    },
    {
      path: '/rental',
      name: 'rental',
      component: ActiveRentalView,
      beforeEnter: requireAuth
    },
    {
      path: '/rentals',
      name: 'rental-history',
      component: () => import('@/views/user/RentalHistoryView.vue'),
      beforeEnter: requireAuth
    },
    {
      path: '/report',
      name: 'report-problem',
      component: () => import('@/views/user/ReportProblemView.vue'),
      beforeEnter: requireAuth
    },
    {
      path: '/my-reports',
      name: 'my-reports',
      component: () => import('@/views/user/MyReportsView.vue'),
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
    },
    {
      path: '/operator/fleet/:id',
      name: 'vehicle-detail',
      component: () => import('@/views/operator/VehicleDetailView.vue'),
      beforeEnter: [requireAuth, requireRole('operator', 'admin')]
    },
    {
      path: '/operator/reports',
      name: 'operator-reports',
      component: () => import('@/views/operator/ReportsView.vue'),
      beforeEnter: [requireAuth, requireRole('operator', 'admin')]
    },
    {
      path: '/operator/reports/:id',
      name: 'report-detail',
      component: () => import('@/views/operator/ReportDetailView.vue'),
      beforeEnter: [requireAuth, requireRole('operator', 'admin')]
    }
  ]
})

export default router
