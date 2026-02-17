package it.movo.app.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import it.movo.app.data.model.AddPaymentMethodRequest
import it.movo.app.data.model.CreateSplitRequest
import it.movo.app.data.model.PaymentMethod
import it.movo.app.data.model.PaymentMethodsResponse
import it.movo.app.data.model.SplitRequest
import it.movo.app.data.model.TransactionsResponse

class PaymentApi(private val client: HttpClient) {
    suspend fun getPaymentMethods(): PaymentMethodsResponse =
        client.get("payments/methods").body()

    suspend fun addPaymentMethod(request: AddPaymentMethodRequest): PaymentMethod =
        client.post("payments/methods") { setBody(request) }.body()

    suspend fun deletePaymentMethod(id: String): Unit =
        client.delete("payments/methods/$id").body()

    suspend fun setDefaultPaymentMethod(id: String): PaymentMethod =
        client.put("payments/methods/$id/default").body()

    suspend fun createSplitRequest(request: CreateSplitRequest): SplitRequest =
        client.post("payments/split-request") { setBody(request) }.body()

    suspend fun respondToSplitRequest(id: String, accept: Boolean): SplitRequest =
        client.post("payments/split-request/$id/respond") {
            setBody(mapOf("accept" to accept))
        }.body()

    suspend fun getTransactions(
        from: String? = null,
        to: String? = null,
        type: String? = null,
        cursor: String? = null,
        limit: Int? = null
    ): TransactionsResponse =
        client.get("payments/transactions") {
            from?.let { parameter("from", it) }
            to?.let { parameter("to", it) }
            type?.let { parameter("type", it) }
            cursor?.let { parameter("cursor", it) }
            limit?.let { parameter("limit", it) }
        }.body()
}
