package it.movo.app.data.repository

import it.movo.app.data.model.DataExportResponse
import it.movo.app.data.model.DeleteAccountRequest
import it.movo.app.data.model.DeleteAccountResponse
import it.movo.app.data.model.DocumentUploadResponse
import it.movo.app.data.model.NotificationPreferences
import it.movo.app.data.model.PasswordUpdate
import it.movo.app.data.model.UserProfile
import it.movo.app.data.model.UserProfileUpdate
import it.movo.app.data.remote.api.UserApi

class UserRepository(private val userApi: UserApi) {
    suspend fun getProfile(): Result<UserProfile> = runCatching {
        userApi.getProfile()
    }

    suspend fun updateProfile(update: UserProfileUpdate): Result<UserProfile> = runCatching {
        userApi.updateProfile(update)
    }

    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> =
        runCatching {
            userApi.updatePassword(PasswordUpdate(currentPassword, newPassword))
        }

    suspend fun deleteAccount(password: String): Result<DeleteAccountResponse> = runCatching {
        userApi.deleteAccount(DeleteAccountRequest(password))
    }

    suspend fun getNotificationPreferences(): Result<NotificationPreferences> = runCatching {
        userApi.getNotificationPreferences()
    }

    suspend fun updateNotificationPreferences(prefs: NotificationPreferences): Result<NotificationPreferences> =
        runCatching {
            userApi.updateNotificationPreferences(prefs)
        }

    suspend fun exportData(): Result<DataExportResponse> = runCatching {
        userApi.exportData()
    }

    suspend fun uploadDocuments(
        drivingLicense: ByteArray? = null,
        identityDocument: ByteArray? = null
    ): Result<DocumentUploadResponse> = runCatching {
        userApi.uploadDocuments(drivingLicense, identityDocument)
    }
}
