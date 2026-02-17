package it.movo.app.data.repository

import it.movo.app.data.model.CancelSubscriptionResponse
import it.movo.app.data.model.SubscribeRequest
import it.movo.app.data.model.SubscriptionPlan
import it.movo.app.data.model.UserSubscription
import it.movo.app.data.remote.api.SubscriptionApi

class SubscriptionRepository(private val subscriptionApi: SubscriptionApi) {
    suspend fun getPlans(): Result<List<SubscriptionPlan>> = runCatching {
        subscriptionApi.getPlans().plans
    }

    suspend fun subscribe(planId: String, autoRenew: Boolean = true): Result<UserSubscription> =
        runCatching {
            subscriptionApi.subscribe(SubscribeRequest(planId, autoRenew))
        }

    suspend fun getActiveSubscription(): Result<UserSubscription> = runCatching {
        subscriptionApi.getActiveSubscription()
    }

    suspend fun toggleAutoRenew(id: String, autoRenew: Boolean): Result<UserSubscription> =
        runCatching {
            subscriptionApi.toggleAutoRenew(id, autoRenew)
        }

    suspend fun cancelSubscription(id: String): Result<CancelSubscriptionResponse> = runCatching {
        subscriptionApi.cancelSubscription(id)
    }
}
