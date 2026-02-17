package it.movo.app.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.movo.app.data.model.EndRentalRequest
import it.movo.app.data.model.Rental
import it.movo.app.data.model.RentalHistoryResponse
import it.movo.app.data.model.RentalSummary
import it.movo.app.data.model.UnlockRequest

class RentalApi(private val client: HttpClient) {
    suspend fun unlockVehicle(request: UnlockRequest): Rental =
        client.post("rentals/unlock") { setBody(request) }.body()

    suspend fun getActiveRental(): Rental =
        client.get("rentals/active").body()

    suspend fun pauseRental(id: String): Rental =
        client.post("rentals/$id/pause").body()

    suspend fun resumeRental(id: String): Rental =
        client.post("rentals/$id/resume").body()

    suspend fun endRental(id: String, request: EndRentalRequest? = null): RentalSummary =
        client.post("rentals/$id/end") {
            request?.let { setBody(it) }
        }.body()

    suspend fun getRentalHistory(
        from: String? = null,
        to: String? = null,
        cursor: String? = null,
        limit: Int? = null
    ): RentalHistoryResponse =
        client.get("rentals/history") {
            from?.let { parameter("from", it) }
            to?.let { parameter("to", it) }
            cursor?.let { parameter("cursor", it) }
            limit?.let { parameter("limit", it) }
        }.body()

    suspend fun getRental(id: String): RentalSummary =
        client.get("rentals/$id").body()
}
