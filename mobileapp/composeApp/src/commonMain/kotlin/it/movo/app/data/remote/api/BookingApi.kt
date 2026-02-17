package it.movo.app.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.movo.app.data.model.Booking
import it.movo.app.data.model.CreateBookingRequest

class BookingApi(private val client: HttpClient) {
    suspend fun createBooking(request: CreateBookingRequest): Booking =
        client.post("bookings") { setBody(request) }.body()

    suspend fun getActiveBooking(): Booking =
        client.get("bookings").body()

    suspend fun cancelBooking(id: String): Unit =
        client.delete("bookings/$id").body()
}
