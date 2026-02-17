package it.movo.app.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import it.movo.app.data.model.CreateIssueRequest
import it.movo.app.data.model.Issue
import it.movo.app.data.model.IssuesResponse

class IssueApi(private val client: HttpClient) {
    suspend fun getIssues(
        status: String? = null,
        cursor: String? = null,
        limit: Int? = null
    ): IssuesResponse =
        client.get("issues") {
            status?.let { parameter("status", it) }
            cursor?.let { parameter("cursor", it) }
            limit?.let { parameter("limit", it) }
        }.body()

    suspend fun getIssue(id: String): Issue =
        client.get("issues/$id").body()

    suspend fun createIssue(request: CreateIssueRequest): Issue =
        client.submitFormWithBinaryData(
            url = "issues",
            formData = formData {
                append("vehicle_id", request.vehicleId)
                request.rentalId?.let { append("rental_id", it) }
                append("category", request.category.name.lowercase())
                append("description", request.description)
                request.photos.forEachIndexed { index, bytes ->
                    append("photos", bytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"photo_$index.jpg\"")
                    })
                }
            }
        ).body()
}
