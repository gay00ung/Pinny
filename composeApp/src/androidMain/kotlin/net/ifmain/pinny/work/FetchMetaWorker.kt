package net.ifmain.pinny.work

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.ifmain.pinny.data.HtmlMetadataParser
import net.ifmain.pinny.domain.port.BookmarkRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

private const val KEY_ID = "bookmark_id"
private const val KEY_URL = "bookmark_url"

class FetchMetaWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val parser: HtmlMetadataParser by inject()
    private val repository: BookmarkRepository by inject()

    override suspend fun doWork(): Result {
        val id = inputData.getString(KEY_ID) ?: return Result.failure()
        val url = inputData.getString(KEY_URL) ?: return Result.failure()

        return runCatching {
            val meta = parser.fetch(url)
            val filePath = meta.imageUrl?.let { saveThumbnail(it, id) }
            repository.updateMeta(id, meta.title ?: prettyHost(url), filePath)
            Result.success()
        }.getOrElse { throwable ->
            Result.retry().also {
                throwable.printStackTrace()
            }
        }
    }

    private suspend fun saveThumbnail(imageUrl: String, id: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val loader = ImageLoader(applicationContext)
            val request = ImageRequest.Builder(applicationContext)
                .data(imageUrl)
                .allowHardware(false)
                .build()

            val result = loader.execute(request).drawable as? BitmapDrawable ?: return@runCatching null
            val bitmap = result.bitmap
            val dir = File(applicationContext.filesDir, "thumbnails")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "$id.jpg")
            file.outputStream().use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            }
            file.absolutePath
        }.getOrNull()
    }

    private fun prettyHost(url: String): String = runCatching {
        val uri = java.net.URI(url)
        uri.host ?: url
    }.getOrDefault(url)
}

fun enqueueFetchMetaWork(context: Context, id: String, url: String) {
    val request = OneTimeWorkRequestBuilder<FetchMetaWorker>()
        .setInputData(workDataOf(KEY_ID to id, KEY_URL to url))
        .build()
    WorkManager.getInstance(context).enqueue(request)
}

