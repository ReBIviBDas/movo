package it.movo.app.platform

import android.content.Context
import android.content.Intent
import it.movo.app.ImagePickerActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidImagePicker(private val context: Context) : ImagePicker {
    override suspend fun pickImage(): ByteArray? = suspendCancellableCoroutine { continuation ->
        ImagePickerActivity.pendingCallback = { bytes ->
            if (continuation.isActive) {
                continuation.resume(bytes)
            }
        }
        val intent = Intent(context, ImagePickerActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)

        continuation.invokeOnCancellation {
            ImagePickerActivity.pendingCallback = null
        }
    }
}
