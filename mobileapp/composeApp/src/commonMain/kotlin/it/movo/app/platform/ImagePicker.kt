package it.movo.app.platform

interface ImagePicker {
    suspend fun pickImage(): ByteArray?
}
