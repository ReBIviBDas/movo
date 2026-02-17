package it.movo.app.platform

class JvmPlatform : Platform {
    override val name: String = "JVM"
}

actual fun getPlatform(): Platform = JvmPlatform()
