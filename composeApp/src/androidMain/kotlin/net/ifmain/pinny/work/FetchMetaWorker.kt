package net.ifmain.pinny.work

import android.content.*
import android.graphics.Bitmap
import android.util.*
import androidx.work.*
import coil3.*
import coil3.request.*
import kotlinx.coroutines.*
import net.ifmain.pinny.data.*
import net.ifmain.pinny.domain.port.*
import org.koin.core.component.*
import java.io.*

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

        Log.d(TAG, "Starting metadata sync for bookmark=$id url=$url")

        return runCatching {
            val meta = parser.fetch(url)
            val filePath = meta.imageUrl?.let { saveThumbnail(it, id) }
            repository.updateMeta(id, meta.title ?: prettyHost(url), filePath)
            Log.i(TAG, "Metadata updated for bookmark=$id title='${meta.title}' thumbnailPath=$filePath")
            Result.success()
        }.getOrElse { throwable ->
            Log.e(TAG, "Metadata sync failed for bookmark=$id url=$url", throwable)
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

            val result = loader.execute(request)
            val bitmap = (result as? SuccessResult)?.image?.toBitmap()
                ?: return@runCatching null
            val dir = File(applicationContext.filesDir, "thumbnails")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "$id.jpg")
            file.outputStream().use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            }
            Log.d(TAG, "Thumbnail saved for bookmark=$id path=${file.absolutePath}")
            file.absolutePath
        }.onFailure { throwable ->
            Log.w(TAG, "Failed to save thumbnail for bookmark=$id imageUrl=$imageUrl", throwable)
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
    Log.d(TAG, "Work enqueued for metadata sync bookmark=$id")
}

private const val TAG = "PinnyMetadata"
