package it.movo.app.data.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
            BookingStatus.CONVERTED_TO_RENTAL,
            json.decodeFromString<BookingStatus>("\"converted_to_rental\"")
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
        val request = CreateBookingRequest(vehicleId = "v1", durationMinutes = 15)
        val serialized = json.encodeToString(CreateBookingRequest.serializer(), request)

        assertEquals(true, serialized.contains("\"vehicle_id\":\"v1\""))
        assertEquals(true, serialized.contains("\"duration_minutes\":15"))
    }
}
