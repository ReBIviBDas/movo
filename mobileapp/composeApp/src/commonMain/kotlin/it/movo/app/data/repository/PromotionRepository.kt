package it.movo.app.data.repository

import it.movo.app.data.model.ApplyPromoRequest
import it.movo.app.data.model.Promotion
import it.movo.app.data.remote.api.PromotionApi

class PromotionRepository(private val promotionApi: PromotionApi) {
    suspend fun getPromotions(): Result<List<Promotion>> = runCatching {
        promotionApi.getPromotions().promotions
    }

    suspend fun applyPromoCode(promoCode: String): Result<Promotion> = runCatching {
        promotionApi.applyPromoCode(ApplyPromoRequest(promoCode))
    }
}
