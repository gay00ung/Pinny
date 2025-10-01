package net.ifmain.pinny.data

data class HtmlMeta(val title: String?, val imageUrl: String?)

expect class HtmlMetadataParser() {
    suspend fun fetch(url: String): HtmlMeta
}

