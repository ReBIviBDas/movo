package it.movo.app.platform

class IosImagePicker : ImagePicker {
    override suspend fun pickImage(): ByteArray? {
        // TODO: Integrate with UIImagePickerController
        return null
    }
}
