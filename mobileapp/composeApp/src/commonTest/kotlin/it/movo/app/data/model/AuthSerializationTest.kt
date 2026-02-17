package it.movo.app.data.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun authResponseDeserializesWithAllFields() {
        val raw = """
        {
            "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test",
            "token_type": "Bearer",
            "expires_in": 3600,
            "refresh_token": "refresh_abc123",
            "user": {
                "id": "u1",
                "email": "test@example.com",
                "role": "user",
                "status": "active"
            }
        }
        """.trimIndent()

        val response = json.decodeFromString<AuthResponse>(raw)

        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test", response.accessToken)
        assertEquals("Bearer", response.tokenType)
        assertEquals(3600, response.expiresIn)
        assertEquals("refresh_abc123", response.refreshToken)
        assertEquals("u1", response.user.id)
        assertEquals("test@example.com", response.user.email)
        assertEquals(UserRole.USER, response.user.role)
        assertEquals(UserStatus.ACTIVE, response.user.status)
    }

    @Test
    fun authResponseDeserializesWithMissingRefreshToken() {
        val raw = """
        {
            "access_token": "token123",
            "expires_in": 7200,
            "user": {
                "id": "u2",
                "email": "admin@example.com",
                "role": "operator",
                "status": "active"
            }
        }
        """.trimIndent()

        val response = json.decodeFromString<AuthResponse>(raw)

        assertNull(response.refreshToken)
        assertEquals("Bearer", response.tokenType)
        assertEquals(UserRole.OPERATOR, response.user.role)
    }

    @Test
    fun userRoleDeserializesAllValues() {
        assertEquals(UserRole.USER, json.decodeFromString<UserRole>("\"user\""))
        assertEquals(UserRole.OPERATOR, json.decodeFromString<UserRole>("\"operator\""))
        assertEquals(UserRole.ADMIN, json.decodeFromString<UserRole>("\"admin\""))
    }

    @Test
    fun userStatusDeserializesAllValues() {
        assertEquals(UserStatus.ACTIVE, json.decodeFromString<UserStatus>("\"active\""))
        assertEquals(
            UserStatus.PENDING_APPROVAL,
            json.decodeFromString<UserStatus>("\"pending_approval\"")
        )
        assertEquals(UserStatus.SUSPENDED, json.decodeFromString<UserStatus>("\"suspended\""))
        assertEquals(UserStatus.BLOCKED, json.decodeFromString<UserStatus>("\"blocked\""))
    }

    @Test
    fun loginRequestSerializes() {
        val request = LoginRequest(email = "test@example.com", password = "secret123")
        val serialized = json.encodeToString(LoginRequest.serializer(), request)

        assertEquals(true, serialized.contains("\"email\":\"test@example.com\""))
        assertEquals(true, serialized.contains("\"password\":\"secret123\""))
    }

    @Test
    fun registrationStatusDeserializes() {
        assertEquals(
            RegistrationStatus.ACTIVE,
            json.decodeFromString<RegistrationStatus>("\"active\"")
        )
        assertEquals(
            RegistrationStatus.PENDING_APPROVAL,
            json.decodeFromString<RegistrationStatus>("\"pending_approval\"")
        )
    }

    @Test
    fun registerResponseDeserializes() {
        val raw = """
        {
            "user_id": "u3",
            "email": "new@example.com",
            "status": "pending_approval",
            "message": "Registration successful. Awaiting document verification."
        }
        """.trimIndent()

        val response = json.decodeFromString<RegisterResponse>(raw)

        assertEquals("u3", response.userId)
        assertEquals("new@example.com", response.email)
        assertEquals(RegistrationStatus.PENDING_APPROVAL, response.status)
    }
}
