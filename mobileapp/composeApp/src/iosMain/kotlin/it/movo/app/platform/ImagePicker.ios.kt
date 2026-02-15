package it.movo.app.platform

class IosImagePicker : ImagePicker {
    override suspend fun pickImage(): ByteArray? {
        // iOS image picker requires UIKit interop (PHPickerViewController).
        // Not implemented in this prototype; Android is the primary demo target.
        return null
    }
}
