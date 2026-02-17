package it.movo.app.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.movo.app.data.model.AutoRenewRequest
import it.movo.app.data.model.CancelSubscriptionResponse
import it.movo.app.data.model.SubscribeRequest
import it.movo.app.data.model.SubscriptionPlansResponse
import it.movo.app.data.model.UserSubscription

class SubscriptionApi(private val client: HttpClient) {
    suspend fun getPlans(): SubscriptionPlansResponse =
        client.get("subscriptions").body()

    suspend fun subscribe(request: SubscribeRequest): UserSubscription =
        client.post("subscriptions") { setBody(request) }.body()

    suspend fun getActiveSubscription(): UserSubscription =
        client.get("subscriptions/active").body()

    suspend fun toggleAutoRenew(id: String, autoRenew: Boolean): UserSubscription =
        client.patch("subscriptions/$id/auto-renew") {
            setBody(AutoRenewRequest(autoRenew))
        }.body()

    suspend fun cancelSubscription(id: String): CancelSubscriptionResponse =
        client.post("subscriptions/$id/cancel").body()
}
