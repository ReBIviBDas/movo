package it.movo.app.ui.viewmodel

import it.movo.app.ui.auth.RegisterUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RegisterValidationTest {

    @Test
    fun isValidEmailAcceptsStandardEmail() {
        assertTrue(RegisterUiState.isValidEmail("user@example.com"))
    }

    @Test
    fun isValidEmailAcceptsEmailWithSubdomain() {
        assertTrue(RegisterUiState.isValidEmail("user@mail.example.com"))
    }

    @Test
    fun isValidEmailAcceptsEmailWithPlus() {
        assertTrue(RegisterUiState.isValidEmail("user+tag@example.com"))
    }

    @Test
    fun isValidEmailRejectsEmptyString() {
        assertFalse(RegisterUiState.isValidEmail(""))
    }

    @Test
    fun isValidEmailRejectsMissingAt() {
        assertFalse(RegisterUiState.isValidEmail("userexample.com"))
    }

    @Test
    fun isValidEmailRejectsMissingDomain() {
        assertFalse(RegisterUiState.isValidEmail("user@"))
    }

    @Test
    fun isValidEmailRejectsMissingTld() {
        assertFalse(RegisterUiState.isValidEmail("user@example"))
    }

    @Test
    fun isValidEmailRejectsSingleCharTld() {
        assertFalse(RegisterUiState.isValidEmail("user@example.c"))
    }

    @Test
    fun isValidPhoneAcceptsSevenDigits() {
        assertTrue(RegisterUiState.isValidPhone("1234567"))
    }

    @Test
    fun isValidPhoneAcceptsFifteenDigits() {
        assertTrue(RegisterUiState.isValidPhone("123456789012345"))
    }

    @Test
    fun isValidPhoneAcceptsFormattedNumber() {
        assertTrue(RegisterUiState.isValidPhone("+39 333 123 4567"))
    }

    @Test
    fun isValidPhoneRejectsSixDigits() {
        assertFalse(RegisterUiState.isValidPhone("123456"))
    }

    @Test
    fun isValidPhoneRejectsSixteenDigits() {
        assertFalse(RegisterUiState.isValidPhone("1234567890123456"))
    }

    @Test
    fun isValidPhoneRejectsEmptyString() {
        assertFalse(RegisterUiState.isValidPhone(""))
    }

    @Test
    fun isAtLeast18ReturnsTrueForAdult() {
        assertTrue(RegisterUiState.isAtLeast18("2000-01-01"))
    }

    @Test
    fun isAtLeast18ReturnsFalseForMinor() {
        assertTrue(!RegisterUiState.isAtLeast18("2020-01-01"))
    }

    @Test
    fun isAtLeast18ReturnsFalseForInvalidFormat() {
        assertFalse(RegisterUiState.isAtLeast18("01-01-2000"))
    }

    @Test
    fun isAtLeast18ReturnsFalseForEmptyString() {
        assertFalse(RegisterUiState.isAtLeast18(""))
    }

    @Test
    fun isAtLeast18ReturnsFalseForGarbageInput() {
        assertFalse(RegisterUiState.isAtLeast18("not-a-date"))
    }

    @Test
    fun progressCalculatesCorrectly() {
        val step1 = RegisterUiState(currentStep = 1)
        val step2 = RegisterUiState(currentStep = 2)
        val step3 = RegisterUiState(currentStep = 3)
        val step4 = RegisterUiState(currentStep = 4)

        assertEquals(0.25f, step1.progress)
        assertEquals(0.50f, step2.progress)
        assertEquals(0.75f, step3.progress)
        assertEquals(1.0f, step4.progress)
    }

    @Test
    fun canProceedStep1RequiresValidEmailAndPassword() {
        val invalid = RegisterUiState(
            currentStep = 1,
            email = "test@example.com",
            password = "short",
            confirmPassword = "short"
        )
        assertFalse(invalid.canProceed)

        val mismatch = RegisterUiState(
            currentStep = 1,
            email = "test@example.com",
            password = "password123",
            confirmPassword = "password456"
        )
        assertFalse(mismatch.canProceed)

        val valid = RegisterUiState(
            currentStep = 1,
            email = "test@example.com",
            password = "password123",
            confirmPassword = "password123"
        )
        assertTrue(valid.canProceed)
    }

    @Test
    fun canProceedStep1RejectsInvalidEmail() {
        val state = RegisterUiState(
            currentStep = 1,
            email = "not-an-email",
            password = "password123",
            confirmPassword = "password123"
        )
        assertFalse(state.canProceed)
    }

    @Test
    fun canProceedStep2RequiresPersonalInfo() {
        val incomplete = RegisterUiState(
            currentStep = 2,
            firstName = "Mario",
            lastName = "",
            dateOfBirth = "2000-01-01",
            fiscalCode = "RSSMRA00A01H501A",
            phone = "+39 333 1234567"
        )
        assertFalse(incomplete.canProceed)

        val valid = RegisterUiState(
            currentStep = 2,
            firstName = "Mario",
            lastName = "Rossi",
            dateOfBirth = "2000-01-01",
            fiscalCode = "RSSMRA00A01H501A",
            phone = "+39 333 1234567"
        )
        assertTrue(valid.canProceed)
    }

    @Test
    fun canProceedStep2RejectsMinor() {
        val state = RegisterUiState(
            currentStep = 2,
            firstName = "Mario",
            lastName = "Rossi",
            dateOfBirth = "2020-01-01",
            fiscalCode = "RSSMRA20A01H501A",
            phone = "+39 333 1234567"
        )
        assertFalse(state.canProceed)
    }

    @Test
    fun canProceedStep3AlwaysTrue() {
        val state = RegisterUiState(currentStep = 3)
        assertTrue(state.canProceed)
    }

    @Test
    fun canProceedStep4RequiresTermsAndPrivacy() {
        val noTerms = RegisterUiState(
            currentStep = 4,
            acceptTerms = false,
            acceptPrivacy = true
        )
        assertFalse(noTerms.canProceed)

        val noPrivacy = RegisterUiState(
            currentStep = 4,
            acceptTerms = true,
            acceptPrivacy = false
        )
        assertFalse(noPrivacy.canProceed)

        val valid = RegisterUiState(
            currentStep = 4,
            acceptTerms = true,
            acceptPrivacy = true,
            acceptCookies = false
        )
        assertTrue(valid.canProceed)
    }

    @Test
    fun isUnder18ComputesFromDateOfBirth() {
        val minor = RegisterUiState(dateOfBirth = "2020-06-15")
        assertTrue(minor.isUnder18)

        val adult = RegisterUiState(dateOfBirth = "2000-06-15")
        assertFalse(adult.isUnder18)

        val empty = RegisterUiState(dateOfBirth = "")
        assertFalse(empty.isUnder18)
    }

    @Test
    fun hasIdentityDocumentReflectsField() {
        val without = RegisterUiState(identityDocument = null)
        assertFalse(without.hasIdentityDocument)

        val with = RegisterUiState(identityDocument = byteArrayOf(1, 2, 3))
        assertTrue(with.hasIdentityDocument)
    }
}
