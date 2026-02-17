package it.movo.app.ui.viewmodel

import it.movo.app.ui.ride.ActiveRideUiState
import kotlin.test.Test
import kotlin.test.assertEquals

class ActiveRideUiStateTest {

    @Test
    fun durationTextFormatsZero() {
        val state = ActiveRideUiState(durationSeconds = 0)
        assertEquals("00:00:00", state.durationText)
    }

    @Test
    fun durationTextFormatsSecondsOnly() {
        val state = ActiveRideUiState(durationSeconds = 45)
        assertEquals("00:00:45", state.durationText)
    }

    @Test
    fun durationTextFormatsMinutesAndSeconds() {
        val state = ActiveRideUiState(durationSeconds = 125)
        assertEquals("00:02:05", state.durationText)
    }

    @Test
    fun durationTextFormatsHoursMinutesSeconds() {
        val state = ActiveRideUiState(durationSeconds = 3661)
        assertEquals("01:01:01", state.durationText)
    }

    @Test
    fun durationTextFormatsLargeValues() {
        val state = ActiveRideUiState(durationSeconds = 36000)
        assertEquals("10:00:00", state.durationText)
    }

    @Test
    fun costTextFormatsZeroCents() {
        val state = ActiveRideUiState(currentCostCents = 0)
        assertEquals("€0.00", state.costText)
    }

    @Test
    fun costTextFormatsSmallAmount() {
        val state = ActiveRideUiState(currentCostCents = 50)
        assertEquals("€0.50", state.costText)
    }

    @Test
    fun costTextFormatsWholeEuros() {
        val state = ActiveRideUiState(currentCostCents = 500)
        assertEquals("€5.00", state.costText)
    }

    @Test
    fun costTextFormatsEurosAndCents() {
        val state = ActiveRideUiState(currentCostCents = 1275)
        assertEquals("€12.75", state.costText)
    }

    @Test
    fun costTextFormatsSingleDigitCents() {
        val state = ActiveRideUiState(currentCostCents = 305)
        assertEquals("€3.05", state.costText)
    }

    @Test
    fun distanceTextFormatsZero() {
        val state = ActiveRideUiState(distanceKm = 0.0)
        assertEquals("0.0 km", state.distanceText)
    }

    @Test
    fun distanceTextFormatsWithDecimal() {
        val state = ActiveRideUiState(distanceKm = 5.7)
        assertEquals("5.7 km", state.distanceText)
    }

    @Test
    fun distanceTextTruncatesDecimal() {
        val state = ActiveRideUiState(distanceKm = 3.99)
        assertEquals("3.9 km", state.distanceText)
    }

    @Test
    fun rangeTextFormats() {
        val state = ActiveRideUiState(estimatedRangeKm = 120.5)
        assertEquals("120.5 km", state.rangeText)
    }

    @Test
    fun rangeTextFormatsZero() {
        val state = ActiveRideUiState(estimatedRangeKm = 0.0)
        assertEquals("0.0 km", state.rangeText)
    }

    @Test
    fun defaultStateValues() {
        val state = ActiveRideUiState()
        assertEquals(0, state.durationSeconds)
        assertEquals(0, state.currentCostCents)
        assertEquals(0.0, state.distanceKm, 0.001)
        assertEquals(0, state.batteryLevel)
        assertEquals("", state.vehicleName)
        assertEquals("", state.vehicleId)
    }
}
