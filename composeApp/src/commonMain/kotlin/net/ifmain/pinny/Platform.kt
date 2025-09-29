package net.ifmain.pinny

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform