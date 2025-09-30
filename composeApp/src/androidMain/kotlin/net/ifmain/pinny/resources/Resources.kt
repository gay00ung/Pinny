package net.ifmain.pinny.resources

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.ifmain.pinny.di.PinnyApp

actual suspend fun readResource(path: String): ByteArray = withContext(Dispatchers.IO) {
    val context = PinnyApp.instance
    context?.assets?.open(path)?.use { return@withContext it.readBytes() }

    val loader = Thread.currentThread().contextClassLoader ?: this::class.java.classLoader
    val stream = loader?.getResourceAsStream(path)
        ?: error("Resource not found: $path")
    stream.use { it.readBytes() }
}

actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap {
    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: error("Decode failed")
    return bmp.asImageBitmap()
}
