package it.movo.app.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.movo.app.data.model.ApplyPromoRequest
import it.movo.app.data.model.Promotion
import it.movo.app.data.model.PromotionsResponse

class PromotionApi(private val client: HttpClient) {
    suspend fun getPromotions(): PromotionsResponse =
        client.get("/promotions").body()

    suspend fun applyPromoCode(request: ApplyPromoRequest): Promotion =
        client.post("/promotions/apply") { setBody(request) }.body()
}
