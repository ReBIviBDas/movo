package it.movo.app.data.repository

import it.movo.app.data.model.CreateIssueRequest
import it.movo.app.data.model.Issue
import it.movo.app.data.model.IssueCategory
import it.movo.app.data.remote.api.IssueApi

class IssueRepository(private val issueApi: IssueApi) {
    suspend fun getIssues(
        status: String? = null,
        cursor: String? = null,
        limit: Int? = null
    ): Result<List<Issue>> = runCatching {
        issueApi.getIssues(status, cursor, limit).data
    }

    suspend fun getIssue(id: String): Result<Issue> = runCatching {
        issueApi.getIssue(id)
    }

    suspend fun createIssue(
        vehicleId: String,
        rentalId: String? = null,
        category: IssueCategory,
        description: String,
        photos: List<ByteArray>? = null,
        gpsLocation: String? = null
    ): Result<Issue> = runCatching {
        issueApi.createIssue(
            CreateIssueRequest(
                vehicleId = vehicleId,
                rentalId = rentalId,
                category = category,
                description = description,
                photos = photos ?: emptyList(),
                gpsLocation = gpsLocation
            )
        )
    }
}
