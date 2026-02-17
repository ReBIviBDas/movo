package it.movo.app.ui.viewmodel

import it.movo.app.ui.booking.BookingUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BookingUiStateTest {

    @Test
    fun defaultRemainingSecondsIs899() {
        val state = BookingUiState()
        assertEquals(899, state.remainingSeconds)
    }

    @Test
    fun timerMinutesCalculatesCorrectly() {
        assertEquals(14, BookingUiState(remainingSeconds = 899).timerMinutes)
        assertEquals(10, BookingUiState(remainingSeconds = 600).timerMinutes)
        assertEquals(0, BookingUiState(remainingSeconds = 59).timerMinutes)
        assertEquals(0, BookingUiState(remainingSeconds = 0).timerMinutes)
    }

    @Test
    fun timerSecondsCalculatesCorrectly() {
        assertEquals(59, BookingUiState(remainingSeconds = 899).timerSeconds)
        assertEquals(0, BookingUiState(remainingSeconds = 600).timerSeconds)
        assertEquals(59, BookingUiState(remainingSeconds = 59).timerSeconds)
        assertEquals(30, BookingUiState(remainingSeconds = 90).timerSeconds)
    }

    @Test
    fun timerTextFormatsWithLeadingZeros() {
        assertEquals("14:59", BookingUiState(remainingSeconds = 899).timerText)
        assertEquals("10:00", BookingUiState(remainingSeconds = 600).timerText)
        assertEquals("00:59", BookingUiState(remainingSeconds = 59).timerText)
        assertEquals("00:00", BookingUiState(remainingSeconds = 0).timerText)
        assertEquals("01:05", BookingUiState(remainingSeconds = 65).timerText)
    }

    @Test
    fun isExpiredWhenZero() {
        assertTrue(BookingUiState(remainingSeconds = 0).isExpired)
        assertFalse(BookingUiState(remainingSeconds = 1).isExpired)
        assertFalse(BookingUiState(remainingSeconds = 899).isExpired)
    }
}
