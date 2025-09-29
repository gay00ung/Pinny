package net.ifmain.pinny.data

class AndroidPlatformCapabilities : PlatformCapabilities {
    override val supportsFts: Boolean = true
    override fun nowMillis(): Long = System.currentTimeMillis()
}
