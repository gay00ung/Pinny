package net.ifmain.pinny.resources

import androidx.compose.ui.graphics.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import org.jetbrains.skia.*
import platform.Foundation.*

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual suspend fun readResource(path: String): ByteArray = withContext(Dispatchers.Default) {
    val fileName = path.substringAfterLast('/')
    val name = fileName.substringBeforeLast('.')
    val ext  = fileName.substringAfterLast('.', "")

    val url = NSBundle.mainBundle.URLForResource(name, ext)
        ?: error("Resource not found: $path")

    val data = NSData.dataWithContentsOfURL(url)
        ?: error("Failed to load: $path")

    val len = data.length.toInt()
    val out = ByteArray(len)

    out.usePinned { pinned ->
        data.getBytes(pinned.addressOf(0), data.length)
    }
    out
}

actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap {
    val img = Image.makeFromEncoded(bytes)
    return img.toComposeImageBitmap()
}
