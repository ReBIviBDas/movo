package it.movo.app.data.repository

import it.movo.app.data.model.Booking
import it.movo.app.data.model.CreateBookingRequest
import it.movo.app.data.remote.api.BookingApi

class BookingRepository(private val bookingApi: BookingApi) {
    suspend fun createBooking(vehicleId: String, durationMinutes: Int = 15): Result<Booking> = runCatching {
        bookingApi.createBooking(CreateBookingRequest(vehicleId, durationMinutes))
    }

    suspend fun getActiveBooking(): Result<Booking> = runCatching {
        bookingApi.getActiveBooking()
    }

    suspend fun cancelBooking(id: String): Result<Unit> = runCatching {
        bookingApi.cancelBooking(id)
    }
}
