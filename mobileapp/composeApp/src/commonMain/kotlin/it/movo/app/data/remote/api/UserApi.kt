package it.movo.app.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import it.movo.app.data.model.DataExportResponse
import it.movo.app.data.model.DeleteAccountRequest
import it.movo.app.data.model.DocumentUploadResponse
import it.movo.app.data.model.DeleteAccountResponse
import it.movo.app.data.model.NotificationPreferences
import it.movo.app.data.model.PasswordUpdate
import it.movo.app.data.model.UserProfile
import it.movo.app.data.model.UserProfileUpdate

class UserApi(private val client: HttpClient) {
    suspend fun getProfile(): UserProfile =
        client.get("/users/me").body()

    suspend fun updateProfile(update: UserProfileUpdate): UserProfile =
        client.patch("/users/me") { setBody(update) }.body()

    suspend fun updatePassword(request: PasswordUpdate): Unit =
        client.put("/users/me/password") { setBody(request) }.body()

    suspend fun deleteAccount(request: DeleteAccountRequest): DeleteAccountResponse =
        client.post("/users/me/delete") { setBody(request) }.body()

    suspend fun getNotificationPreferences(): NotificationPreferences =
        client.get("/notifications/preferences").body()

    suspend fun updateNotificationPreferences(prefs: NotificationPreferences): NotificationPreferences =
        client.put("/notifications/preferences") { setBody(prefs) }.body()

    suspend fun uploadDocuments(
        drivingLicense: ByteArray? = null,
        identityDocument: ByteArray? = null
    ): DocumentUploadResponse =
        client.submitFormWithBinaryData(
            url = "/users/me/documents",
            formData = formData {
                drivingLicense?.let {
                    append("driving_license", it, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"driving_license.jpg\"")
                    })
                }
                identityDocument?.let {
                    append("identity_document", it, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"identity_document.jpg\"")
                    })
                }
            }
        ).body()

    suspend fun exportData(): DataExportResponse =
        client.get("/users/me/data-export").body()
}
