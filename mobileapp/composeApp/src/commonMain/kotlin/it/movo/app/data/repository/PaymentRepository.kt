package it.movo.app.data.repository

import it.movo.app.data.model.AddPaymentMethodRequest
import it.movo.app.data.model.CreateSplitRequest
import it.movo.app.data.model.Payment
import it.movo.app.data.model.PaymentMethod
import it.movo.app.data.model.SplitParticipant
import it.movo.app.data.model.SplitRequest
import it.movo.app.data.remote.api.PaymentApi


class PaymentRepository(private val paymentApi: PaymentApi) {
    suspend fun getPaymentMethods(): Result<List<PaymentMethod>> = runCatching {
        paymentApi.getPaymentMethods().methods
    }

    suspend fun addPaymentMethod(
        stripeToken: String,
        setAsDefault: Boolean? = null
    ): Result<Unit> = runCatching {
        paymentApi.addPaymentMethod(AddPaymentMethodRequest(stripeToken, setAsDefault))
    }

    suspend fun deletePaymentMethod(id: String): Result<Unit> = runCatching {
        paymentApi.deletePaymentMethod(id)
    }

    suspend fun setDefaultPaymentMethod(id: String): Result<Unit> = runCatching {
        paymentApi.setDefaultPaymentMethod(id)
    }

    suspend fun createSplitRequest(
        rentalId: String,
        participants: List<SplitParticipant>
    ): Result<SplitRequest> = runCatching {
        paymentApi.createSplitRequest(CreateSplitRequest(rentalId, participants))
    }

    suspend fun respondToSplitRequest(id: String, accept: Boolean): Result<SplitRequest> =
        runCatching {
            paymentApi.respondToSplitRequest(id, accept)
        }

    suspend fun getTransactions(
        from: String? = null,
        to: String? = null,
        type: String? = null,
        cursor: String? = null,
        limit: Int? = null
    ): Result<Pair<List<Payment>, Boolean>> = runCatching {
        val response = paymentApi.getTransactions(from, to, type, cursor, limit)
        response.data to response.pagination.hasMore
    }
}
