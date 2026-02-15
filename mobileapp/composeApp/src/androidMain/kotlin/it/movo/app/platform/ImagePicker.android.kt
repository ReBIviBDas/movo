package it.movo.app.platform

import android.content.Context

class AndroidImagePicker(private val context: Context) : ImagePicker {
    override suspend fun pickImage(): ByteArray? {
        // TODO: Integrate with ActivityResultLauncher for real image picking
        // This requires Activity context and launcher registration
        return null
    }
}
