package net.ifmain.pinny.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

actual class HtmlMetadataParser actual constructor() {
    private val client = OkHttpClient()

    actual suspend fun fetch(url: String): HtmlMeta = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .build()

        client.newCall(request).execute().use { response ->
            val html = response.body.string().orEmpty()
            val doc = Jsoup.parse(html, url)

            val ogTitle = doc.select("meta[property=og:title]").attr("content").ifBlank { null }
            val title = ogTitle ?: doc.title().ifBlank { null }

            val ogImage = doc.select("meta[property=og:image]").attr("content")
            val twitterImage = if (ogImage.isBlank()) doc.select("meta[name=twitter:image]").attr("content") else ""

            val imageUrl = when {
                ogImage.isNotBlank() -> ogImage
                twitterImage.isNotBlank() -> twitterImage
                else -> faviconFrom(url)
            }

            HtmlMeta(title = title, imageUrl = imageUrl)
        }
    }

    private fun faviconFrom(url: String): String? = runCatching {
        val host = java.net.URI(url).host ?: return null
        "https://$host/favicon.ico"
    }.getOrNull()
}

