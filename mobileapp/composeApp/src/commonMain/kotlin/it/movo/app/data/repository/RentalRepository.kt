package it.movo.app.data.repository

import it.movo.app.data.model.EndRentalRequest
import it.movo.app.data.model.GeoPoint
import it.movo.app.data.model.Rental
import it.movo.app.data.model.RentalSummary
import it.movo.app.data.model.UnlockRequest
import it.movo.app.data.remote.api.RentalApi

class RentalRepository(private val rentalApi: RentalApi) {
    suspend fun unlockVehicle(bookingId: String, userLocation: GeoPoint? = null): Result<Rental> = runCatching {
        rentalApi.unlockVehicle(UnlockRequest(bookingId, userLocation))
    }

    suspend fun getActiveRental(): Result<Rental> = runCatching {
        rentalApi.getActiveRental()
    }

    suspend fun pauseRental(id: String): Result<Rental> = runCatching {
        rentalApi.pauseRental(id)
    }

    suspend fun resumeRental(id: String): Result<Rental> = runCatching {
        rentalApi.resumeRental(id)
    }

    suspend fun endRental(id: String, endLocation: GeoPoint? = null, photos: List<String>? = null): Result<RentalSummary> = runCatching {
        rentalApi.endRental(id, EndRentalRequest(endLocation, photos ?: emptyList()))
    }

    suspend fun getRentalHistory(from: String? = null, to: String? = null, cursor: String? = null, limit: Int? = null): Result<List<RentalSummary>> = runCatching {
        rentalApi.getRentalHistory(from, to, cursor, limit).data
    }

    suspend fun getRental(id: String): Result<RentalSummary> = runCatching {
        rentalApi.getRental(id)
    }
}
