package it.movo.app.ui.viewmodel

import it.movo.app.ui.auth.LoginUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoginUiStateTest {

    @Test
    fun defaultStateHasEmptyFields() {
        val state = LoginUiState()

        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.passwordVisible)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertFalse(state.loginSuccess)
    }

    @Test
    fun copyPreservesOtherFields() {
        val state = LoginUiState(
            email = "test@example.com",
            password = "secret",
            passwordVisible = false
        )
        val updated = state.copy(passwordVisible = true)

        assertEquals("test@example.com", updated.email)
        assertEquals("secret", updated.password)
        assertTrue(updated.passwordVisible)
    }

    @Test
    fun errorMessageCanBeSetAndCleared() {
        val state = LoginUiState()
        val withError = state.copy(errorMessage = "Invalid credentials")
        assertEquals("Invalid credentials", withError.errorMessage)

        val cleared = withError.copy(errorMessage = null)
        assertNull(cleared.errorMessage)
    }

    @Test
    fun emailChangeResetsError() {
        val state = LoginUiState(
            email = "old@example.com",
            errorMessage = "Some error"
        )
        val updated = state.copy(email = "new@example.com", errorMessage = null)

        assertEquals("new@example.com", updated.email)
        assertNull(updated.errorMessage)
    }

    @Test
    fun loginSuccessFlag() {
        val state = LoginUiState()
        assertFalse(state.loginSuccess)

        val success = state.copy(loginSuccess = true)
        assertTrue(success.loginSuccess)
    }
}
