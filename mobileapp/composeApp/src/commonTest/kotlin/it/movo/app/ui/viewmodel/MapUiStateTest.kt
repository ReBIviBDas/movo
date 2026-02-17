package it.movo.app.ui.viewmodel

import it.movo.app.ui.map.MapUiState
import it.movo.app.data.model.VehicleMapItem
import it.movo.app.data.model.VehicleStatus
import it.movo.app.data.model.GeoPoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MapUiStateTest {

    private fun createVehicleMapItem(
        id: String = "v1",
        model: String = "Fiat 500e",
        batteryLevel: Int = 80
    ) = VehicleMapItem(
        id = id,
        model = model,
        licensePlate = null,
        location = GeoPoint(coordinates = listOf(11.12, 46.07)),
        batteryLevel = batteryLevel,
        status = VehicleStatus.AVAILABLE
    )

    @Test
    fun defaultStateHasEmptyLists() {
        val state = MapUiState()
        assertEquals(emptyList(), state.vehicles)
        assertEquals(emptyList(), state.parkingAreas)
    }

    @Test
    fun defaultStateHasNoSelection() {
        val state = MapUiState()
        assertNull(state.selectedVehicle)
        assertNull(state.selectedPreview)
        assertFalse(state.showVehicleDetails)
    }

    @Test
    fun defaultFiltersAreUnrestricted() {
        val state = MapUiState()
        assertEquals(0, state.minBattery)
        assertEquals(Double.MAX_VALUE, state.maxPrice, 0.001)
        assertEquals(Double.MAX_VALUE, state.maxDistance, 0.001)
    }

    @Test
    fun filterSheetToggle() {
        val state = MapUiState(showFilterSheet = false)
        val toggled = state.copy(showFilterSheet = !state.showFilterSheet)
        assertTrue(toggled.showFilterSheet)

        val toggledBack = toggled.copy(showFilterSheet = !toggled.showFilterSheet)
        assertFalse(toggledBack.showFilterSheet)
    }

    @Test
    fun filterValuesUpdate() {
        val state = MapUiState()
        val updated = state.copy(minBattery = 20, maxPrice = 0.50, maxDistance = 500.0)

        assertEquals(20, updated.minBattery)
        assertEquals(0.50, updated.maxPrice, 0.001)
        assertEquals(500.0, updated.maxDistance, 0.001)
    }

    @Test
    fun filterReset() {
        val filtered = MapUiState(minBattery = 50, maxPrice = 0.30, maxDistance = 200.0)
        val reset = filtered.copy(
            minBattery = 0,
            maxPrice = Double.MAX_VALUE,
            maxDistance = Double.MAX_VALUE,
            showFilterSheet = false
        )

        assertEquals(0, reset.minBattery)
        assertEquals(Double.MAX_VALUE, reset.maxPrice, 0.001)
        assertEquals(Double.MAX_VALUE, reset.maxDistance, 0.001)
        assertFalse(reset.showFilterSheet)
    }

    @Test
    fun vehicleSelectionAndDismiss() {
        val vehicle = createVehicleMapItem()
        val state = MapUiState()
        val selected = state.copy(selectedPreview = vehicle)

        assertEquals(vehicle, selected.selectedPreview)
        assertEquals("v1", selected.selectedPreview?.id)

        val dismissed = selected.copy(selectedPreview = null)
        assertNull(dismissed.selectedPreview)
    }

    @Test
    fun searchQueryUpdates() {
        val state = MapUiState()
        val withQuery = state.copy(searchQuery = "Fiat")

        assertEquals("Fiat", withQuery.searchQuery)

        val cleared = withQuery.copy(searchQuery = "")
        assertEquals("", cleared.searchQuery)
    }

    @Test
    fun vehicleListUpdates() {
        val vehicles = listOf(
            createVehicleMapItem(id = "v1", model = "Fiat 500e"),
            createVehicleMapItem(id = "v2", model = "Smart EQ", batteryLevel = 60)
        )
        val state = MapUiState(vehicles = vehicles)

        assertEquals(2, state.vehicles.size)
        assertEquals("Fiat 500e", state.vehicles[0].model)
        assertEquals("Smart EQ", state.vehicles[1].model)
    }
}
