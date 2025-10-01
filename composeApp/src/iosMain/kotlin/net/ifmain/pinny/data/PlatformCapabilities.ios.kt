package net.ifmain.pinny.data

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

class AndroidPlatformCapabilities : PlatformCapabilities {
    override val supportsFts: Boolean = true
    override fun nowMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
}
