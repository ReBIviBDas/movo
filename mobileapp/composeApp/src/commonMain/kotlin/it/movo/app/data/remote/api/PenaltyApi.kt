package it.movo.app.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.movo.app.data.model.ContestPenaltyRequest
import it.movo.app.data.model.PenaltiesResponse
import it.movo.app.data.model.Penalty

class PenaltyApi(private val client: HttpClient) {
    suspend fun getPenalties(
        status: String? = null,
        cursor: String? = null,
        limit: Int? = null
    ): PenaltiesResponse =
        client.get("/penalties") {
            status?.let { parameter("status", it) }
            cursor?.let { parameter("cursor", it) }
            limit?.let { parameter("limit", it) }
        }.body()

    suspend fun getPenalty(id: String): Penalty =
        client.get("/penalties/$id").body()

    suspend fun contestPenalty(id: String, request: ContestPenaltyRequest): Penalty =
        client.post("/penalties/$id/contest") { setBody(request) }.body()
}
