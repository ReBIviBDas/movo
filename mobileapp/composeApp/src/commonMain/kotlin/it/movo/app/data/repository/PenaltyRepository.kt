package it.movo.app.data.repository

import it.movo.app.data.model.ContestPenaltyRequest
import it.movo.app.data.model.Penalty
import it.movo.app.data.remote.api.PenaltyApi

class PenaltyRepository(private val penaltyApi: PenaltyApi) {
    suspend fun getPenalties(
        status: String? = null,
        cursor: String? = null,
        limit: Int? = null
    ): Result<List<Penalty>> = runCatching {
        penaltyApi.getPenalties(status, cursor, limit).data
    }

    suspend fun getPenalty(id: String): Result<Penalty> = runCatching {
        penaltyApi.getPenalty(id)
    }

    suspend fun contestPenalty(id: String, reason: String): Result<Penalty> = runCatching {
        penaltyApi.contestPenalty(id, ContestPenaltyRequest(reason))
    }
}
