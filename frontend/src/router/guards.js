import { authState } from '@/states/auth'
import { useRouter } from 'vue-router'

/**
 * Route guard: Require authentication
 * Redirect to login if not authenticated
 */
export function requireAuth(to, from, next) {
    if (!authState.token) {
        next({ name: 'login', query: { redirect: to.fullPath } })
    } else {
        next()
    }
}

/**
 * Route guard: Require specific role
 * Must be used after requireAuth
 */
export function requireRole(...allowedRoles) {
    return (to, from, next) => {
        if (!authState.user) {
            next({ name: 'login' })
            return
        }

        const userRole = authState.user.role
        if (allowedRoles.includes(userRole)) {
            next()
        } else {
            // Redirect to appropriate dashboard based on role
            if (userRole === 'operator' || userRole === 'admin') {
                next({ name: 'operator-dashboard' })
            } else {
                next({ name: 'user-dashboard' })
            }
        }
    }
}

/**
 * Route guard: Redirect if already authenticated
 * For login/register pages
 */
export function redirectIfAuth(to, from, next) {
    if (authState.token) {
        // Redirect based on role
        const userRole = authState.user?.role
        if (userRole === 'operator' || userRole === 'admin') {
            next({ name: 'operator-dashboard' })
        } else {
            next({ name: 'user-dashboard' })
        }
    } else {
        next()
    }
}
