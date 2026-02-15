package it.movo.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class ImagePickerActivity : ComponentActivity() {

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val bytes = uri?.let { selectedUri ->
            contentResolver.openInputStream(selectedUri)?.use { it.readBytes() }
        }
        pendingCallback?.invoke(bytes)
        pendingCallback = null
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickImage.launch("image/*")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (pendingCallback != null) {
            pendingCallback?.invoke(null)
            pendingCallback = null
        }
    }

    companion object {
        var pendingCallback: ((ByteArray?) -> Unit)? = null
    }
}
