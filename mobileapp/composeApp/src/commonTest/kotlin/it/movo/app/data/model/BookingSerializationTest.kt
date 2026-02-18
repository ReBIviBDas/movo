package it.movo.app.data.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertTrue

class BookingSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun bookingDeserializesWithAllFields() {
        val raw = """
        {
            "id": "b1",
            "user_id": "u1",
            "vehicle_id": "v1",
            "vehicle": {
                "id": "v1",
                "model": "Fiat 500e",
                "plate": "AB123CD",
                "location": { "type": "Point", "coordinates": [11.0, 46.0] },
                "battery_level": 80
            },
            "duration_minutes": 15,
            "status": "active",
            "created_at": "2025-06-01T10:00:00Z",
            "expires_at": "2025-06-01T10:15:00Z"
        }
        """.trimIndent()

        val booking = json.decodeFromString<Booking>(raw)

        assertEquals("b1", booking.id)
        assertEquals("u1", booking.userId)
        assertEquals("v1", booking.vehicleId)
        assertEquals("Fiat 500e", booking.vehicle?.model)
        assertEquals("AB123CD", booking.vehicle?.licensePlate)
        assertEquals(15, booking.durationMinutes)
        assertEquals(BookingStatus.ACTIVE, booking.status)
        assertEquals("2025-06-01T10:00:00Z", booking.createdAt)
        assertEquals("2025-06-01T10:15:00Z", booking.expiresAt)
    }

    @Test
    fun bookingDeserializesWithNullVehicle() {
        val raw = """
        {
            "id": "b2",
            "user_id": "u1",
            "vehicle_id": "v2",
            "duration_minutes": 10,
            "status": "expired",
            "created_at": "2025-06-01T09:00:00Z",
            "expires_at": "2025-06-01T09:10:00Z"
        }
        """.trimIndent()

        val booking = json.decodeFromString<Booking>(raw)

        assertEquals("b2", booking.id)
        assertNull(booking.vehicle)
        assertEquals(BookingStatus.EXPIRED, booking.status)
    }

    @Test
    fun bookingStatusDeserializesAllValues() {
        assertEquals(
            BookingStatus.ACTIVE,
            json.decodeFromString<BookingStatus>("\"active\"")
        )
        assertEquals(
            BookingStatus.EXPIRED,
            json.decodeFromString<BookingStatus>("\"expired\"")
        )
        assertEquals(
            BookingStatus.COMPLETED,
            json.decodeFromString<BookingStatus>("\"completed\"")
        )
        assertEquals(
            BookingStatus.CANCELLED,
            json.decodeFromString<BookingStatus>("\"cancelled\"")
        )
    }

    @Test
    fun bookingVehicleWithMissingPlateDeserializes() {
        val raw = """
        {
            "id": "b3",
            "user_id": "u1",
            "vehicle_id": "v3",
            "vehicle": {
                "id": "v3",
                "model": "Smart EQ",
                "location": { "type": "Point", "coordinates": [11.0, 46.0] },
                "battery_level": 60
            },
            "duration_minutes": 15,
            "status": "active",
            "created_at": "2025-06-01T10:00:00Z",
            "expires_at": "2025-06-01T10:15:00Z"
        }
        """.trimIndent()

        val booking = json.decodeFromString<Booking>(raw)

        assertNull(booking.vehicle?.licensePlate)
        assertEquals("Smart EQ", booking.vehicle?.model)
    }

    @Test
    fun createBookingRequestSerializes() {
        val request = CreateBookingRequest(vehicleId = "v1", durationMinutes = 30)
        val serialized = json.encodeToString(CreateBookingRequest.serializer(), request)

        assertTrue(serialized.contains("vehicle_id"))
        assertTrue(serialized.contains("v1"))
        assertTrue(serialized.contains("duration_minutes"))
        assertTrue(serialized.contains("30"))
    }

    @Test
    fun bookingDeserializesFromPostResponse() {
        val raw = """
        {
            "id": "b4",
            "expires_at": "2025-06-01T10:15:00Z",
            "remaining_seconds": 900,
            "vehicle": {
                "id": "v1",
                "model": "Fiat 500e",
                "plate": "AB123CD",
                "location": { "lat": 46.0, "lng": 11.0 }
            }
        }
        """.trimIndent()

        val booking = json.decodeFromString<Booking>(raw)

        assertEquals("b4", booking.id)
        assertEquals("2025-06-01T10:15:00Z", booking.expiresAt)
        assertEquals(900, booking.remainingSeconds)
        assertEquals("", booking.userId)
        assertEquals("", booking.vehicleId)
        assertEquals(15, booking.durationMinutes)
        assertEquals("", booking.createdAt)
        assertEquals("Fiat 500e", booking.vehicle?.model)
    }

    @Test
    fun createBookingResponseDeserializes() {
        val raw = """
        {
            "message": "Booking created successfully",
            "booking": {
                "id": "b5",
                "expires_at": "2025-06-01T10:15:00Z",
                "remaining_seconds": 900,
                "vehicle": {
                    "id": "v1",
                    "model": "Fiat 500e",
                    "plate": "AB123CD",
                    "location": { "lat": 46.0, "lng": 11.0 }
                }
            }
        }
        """.trimIndent()

        val response = json.decodeFromString<CreateBookingResponse>(raw)

        assertEquals("Booking created successfully", response.message)
        assertEquals("b5", response.booking.id)
        assertEquals(900, response.booking.remainingSeconds)
    }

    @Test
    fun activeBookingResponseDeserializes() {
        val raw = """
        {
            "active_booking": {
                "id": "b6",
                "status": "active",
                "expires_at": "2025-06-01T10:15:00Z",
                "remaining_seconds": 600,
                "created_at": "2025-06-01T10:00:00Z",
                "vehicle": {
                    "id": "v1",
                    "model": "Fiat 500e",
                    "plate": "AB123CD",
                    "type": "car",
                    "battery_level": 80,
                    "location": { "lat": 46.0, "lng": 11.0 }
                }
            }
        }
        """.trimIndent()

        val response = json.decodeFromString<ActiveBookingResponse>(raw)

        assertEquals("b6", response.activeBooking?.id)
        assertEquals(600, response.activeBooking?.remainingSeconds)
        assertEquals(80, response.activeBooking?.vehicle?.batteryLevel)
    }

    @Test
    fun activeBookingResponseDeserializesNull() {
        val raw = """
        {
            "active_booking": null,
            "message": "No active booking found"
        }
        """.trimIndent()

        val response = json.decodeFromString<ActiveBookingResponse>(raw)

        assertNull(response.activeBooking)
    }
}
