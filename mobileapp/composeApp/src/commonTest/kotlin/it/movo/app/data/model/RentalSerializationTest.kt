package it.movo.app.data.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RentalSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun rentalDeserializesWithAllFields() {
        val raw = """
        {
            "id": "r1",
            "user_id": "u1",
            "vehicle_id": "v1",
            "vehicle": {
                "id": "v1",
                "model": "Fiat 500e",
                "plate": "AB123CD",
                "location": { "type": "Point", "coordinates": [11.0, 46.0] },
                "battery_level": 80
            },
            "status": "active",
            "started_at": "2025-06-01T10:00:00Z",
            "start_location": { "type": "Point", "coordinates": [11.0, 46.0] },
            "current_cost_cents": 450,
            "paused_at": "2025-06-01T10:15:00Z"
        }
        """.trimIndent()

        val rental = json.decodeFromString<Rental>(raw)

        assertEquals("r1", rental.id)
        assertEquals("u1", rental.userId)
        assertEquals("v1", rental.vehicleId)
        assertEquals("Fiat 500e", rental.vehicle?.model)
        assertEquals(RentalStatus.ACTIVE, rental.status)
        assertEquals("2025-06-01T10:00:00Z", rental.startedAt)
        assertEquals(450, rental.currentCostCents)
        assertEquals("2025-06-01T10:15:00Z", rental.pausedAt)
    }

    @Test
    fun rentalDeserializesWithMinimalFields() {
        val raw = """
        {
            "id": "r2",
            "user_id": "u1",
            "vehicle_id": "v2",
            "started_at": "2025-06-01T10:00:00Z"
        }
        """.trimIndent()

        val rental = json.decodeFromString<Rental>(raw)

        assertEquals("r2", rental.id)
        assertNull(rental.vehicle)
        assertEquals(RentalStatus.ACTIVE, rental.status)
        assertNull(rental.startLocation)
        assertEquals(0, rental.currentCostCents)
        assertNull(rental.pausedAt)
    }

    @Test
    fun rentalSummaryDeserializesWithAllFields() {
        val raw = """
        {
            "id": "r3",
            "user_id": "u1",
            "vehicle_id": "v1",
            "status": "completed",
            "started_at": "2025-06-01T10:00:00Z",
            "ended_at": "2025-06-01T10:30:00Z",
            "duration_minutes": 30,
            "distance_km": 5.2,
            "total_cost_cents": 900,
            "discount_applied_cents": 100,
            "final_cost_cents": 800
        }
        """.trimIndent()

        val summary = json.decodeFromString<RentalSummary>(raw)

        assertEquals(RentalStatus.COMPLETED, summary.status)
        assertEquals("2025-06-01T10:30:00Z", summary.endedAt)
        assertEquals(30, summary.durationMinutes)
        assertEquals(5.2, summary.distanceKm!!, 0.01)
        assertEquals(900, summary.totalCostCents)
        assertEquals(100, summary.discountAppliedCents)
        assertEquals(800, summary.finalCostCents)
    }

    @Test
    fun rentalStatusDeserializesAllValues() {
        assertEquals(
            RentalStatus.ACTIVE,
            json.decodeFromString<RentalStatus>("\"active\"")
        )
        assertEquals(
            RentalStatus.PAUSED,
            json.decodeFromString<RentalStatus>("\"paused\"")
        )
        assertEquals(
            RentalStatus.COMPLETED,
            json.decodeFromString<RentalStatus>("\"completed\"")
        )
    }

    @Test
    fun rentalHistoryResponseDeserializes() {
        val raw = """
        {
            "rentals": [
                {
                    "id": "r1",
                    "vehicle_id": "v1",
                    "status": "completed",
                    "started_at": "2025-06-01T10:00:00Z",
                    "total_cost": 5.00
                }
            ],
            "pagination": {
                "page": 1,
                "limit": 10,
                "total": 1,
                "pages": 1
            }
        }
        """.trimIndent()

        val response = json.decodeFromString<RentalHistoryResponse>(raw)

        assertEquals(1, response.rentals.size)
        assertEquals("r1", response.rentals[0].id)
        assertEquals(1, response.pagination?.page)
        assertEquals(1, response.pagination?.total)
    }
}
