package net.ifmain.pinny.data

interface PlatformCapabilities {
    val supportsFts: Boolean
    fun nowMillis(): Long
}
