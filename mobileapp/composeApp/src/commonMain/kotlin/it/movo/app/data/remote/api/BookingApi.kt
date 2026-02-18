package it.movo.app.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.movo.app.data.model.ActiveBookingResponse
import it.movo.app.data.model.Booking
import it.movo.app.data.model.CreateBookingRequest
import it.movo.app.data.model.CreateBookingResponse

class BookingApi(private val client: HttpClient) {
    suspend fun createBooking(request: CreateBookingRequest): Booking {
        val response: CreateBookingResponse = client.post("bookings") { setBody(request) }.body()
        return response.booking
    }

    suspend fun getActiveBooking(): Booking? {
        val response: ActiveBookingResponse = client.get("bookings").body()
        return response.activeBooking
    }

    suspend fun cancelBooking(id: String): Unit =
        client.delete("bookings/$id").body()
}
