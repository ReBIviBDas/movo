package it.movo.app.data.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VehicleSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun vehicleMapItemDeserializesWithAllFields() {
        val raw = """
        {
            "id": "v1",
            "model": "Fiat 500e",
            "plate": "AB123CD",
            "location": { "type": "Point", "coordinates": [11.12, 46.07] },
            "battery_level": 85,
            "status": "available",
            "price_per_minute": 0.25
        }
        """.trimIndent()

        val item = json.decodeFromString<VehicleMapItem>(raw)

        assertEquals("v1", item.id)
        assertEquals("Fiat 500e", item.model)
        assertEquals("AB123CD", item.licensePlate)
        assertEquals(85, item.batteryLevel)
        assertEquals(VehicleStatus.AVAILABLE, item.status)
        assertEquals(0.25, item.basePricePerMinute)
        assertEquals(11.12, item.location.longitude, 0.001)
        assertEquals(46.07, item.location.latitude, 0.001)
    }

    @Test
    fun vehicleMapItemDeserializesWithMissingPlate() {
        val raw = """
        {
            "id": "v2",
            "model": "Smart EQ",
            "location": { "type": "Point", "coordinates": [11.0, 46.0] },
            "battery_level": 50,
            "status": "available"
        }
        """.trimIndent()

        val item = json.decodeFromString<VehicleMapItem>(raw)

        assertNull(item.licensePlate)
        assertEquals("v2", item.id)
        assertEquals(0.0, item.basePricePerMinute)
    }

    @Test
    fun vehicleDeserializesWithAllFields() {
        val raw = """
        {
            "id": "v3",
            "model": "Tesla Model 3",
            "plate": "EF456GH",
            "location": { "type": "Point", "coordinates": [11.12, 46.07] },
            "battery_level": 92,
            "status": "in_use",
            "price_per_minute": 0.30,
            "year": 2023,
            "color": "white",
            "features": ["gps", "heated_seats"],
            "range_km": 350,
            "last_maintenance_at": "2025-01-15T10:00:00Z"
        }
        """.trimIndent()

        val vehicle = json.decodeFromString<Vehicle>(raw)

        assertEquals("v3", vehicle.id)
        assertEquals("Tesla Model 3", vehicle.model)
        assertEquals("EF456GH", vehicle.licensePlate)
        assertEquals(92, vehicle.batteryLevel)
        assertEquals(VehicleStatus.IN_USE, vehicle.status)
        assertEquals(0.30, vehicle.basePricePerMinute)
        assertEquals(2023, vehicle.year)
        assertEquals("white", vehicle.color)
        assertEquals(listOf("gps", "heated_seats"), vehicle.features)
        assertEquals(350, vehicle.rangeKm)
        assertEquals("2025-01-15T10:00:00Z", vehicle.lastMaintenanceAt)
    }

    @Test
    fun vehicleDeserializesWithMinimalFields() {
        val raw = """
        {
            "id": "v4",
            "model": "Renault Zoe",
            "location": { "type": "Point", "coordinates": [11.0, 46.0] },
            "battery_level": 10
        }
        """.trimIndent()

        val vehicle = json.decodeFromString<Vehicle>(raw)

        assertEquals("v4", vehicle.id)
        assertNull(vehicle.licensePlate)
        assertEquals(VehicleStatus.AVAILABLE, vehicle.status)
        assertEquals(0.0, vehicle.basePricePerMinute)
        assertNull(vehicle.year)
        assertNull(vehicle.color)
        assertEquals(emptyList(), vehicle.features)
        assertNull(vehicle.rangeKm)
        assertNull(vehicle.lastMaintenanceAt)
    }

    @Test
    fun vehicleStatusDeserializesAllValues() {
        assertEquals(
            VehicleStatus.AVAILABLE,
            json.decodeFromString<VehicleStatus>("\"available\"")
        )
        assertEquals(
            VehicleStatus.RESERVED,
            json.decodeFromString<VehicleStatus>("\"reserved\"")
        )
        assertEquals(
            VehicleStatus.IN_USE,
            json.decodeFromString<VehicleStatus>("\"in_use\"")
        )
    }

    @Test
    fun vehicleSearchResultDeserializesWithDistanceMeters() {
        val raw = """
        {
            "id": "v5",
            "model": "BMW iX3",
            "plate": "IJ789KL",
            "location": { "type": "Point", "coordinates": [11.12, 46.07] },
            "battery_level": 77,
            "status": "available",
            "price_per_minute": 0.40,
            "distance_meters": 350
        }
        """.trimIndent()

        val result = json.decodeFromString<VehicleSearchResult>(raw)

        assertEquals("IJ789KL", result.licensePlate)
        assertEquals(350, result.distanceMeters)
        assertEquals(0.40, result.basePricePerMinute)
    }

    @Test
    fun geoPointCoordinatesMapCorrectly() {
        val raw = """{ "type": "Point", "coordinates": [11.1234, 46.0678] }"""

        val point = json.decodeFromString<GeoPoint>(raw)

        assertEquals(11.1234, point.longitude, 0.0001)
        assertEquals(46.0678, point.latitude, 0.0001)
    }

    @Test
    fun geoPointHandlesEmptyCoordinates() {
        val raw = """{ "type": "Point", "coordinates": [] }"""

        val point = json.decodeFromString<GeoPoint>(raw)

        assertEquals(0.0, point.longitude, 0.0001)
        assertEquals(0.0, point.latitude, 0.0001)
    }

    @Test
    fun vehiclesResponseDeserializes() {
        val raw = """
        {
            "vehicles": [
                {
                    "id": "v1",
                    "model": "Fiat 500e",
                    "location": { "type": "Point", "coordinates": [11.0, 46.0] },
                    "battery_level": 80
                }
            ],
            "updated_at": "2025-06-01T12:00:00Z"
        }
        """.trimIndent()

        val response = json.decodeFromString<VehiclesResponse>(raw)

        assertEquals(1, response.vehicles.size)
        assertEquals("v1", response.vehicles[0].id)
        assertEquals("2025-06-01T12:00:00Z", response.updatedAt)
    }
}
