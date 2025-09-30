package net.ifmain.pinny.resources

import androidx.compose.ui.graphics.ImageBitmap

expect suspend fun readResource(path: String): ByteArray
expect fun decodeImageBitmap(bytes: ByteArray): ImageBitmap
