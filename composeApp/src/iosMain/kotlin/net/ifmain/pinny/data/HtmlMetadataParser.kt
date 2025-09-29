package net.ifmain.pinny.data

actual class HtmlMetadataParser actual constructor() {
    actual suspend fun fetch(url: String): HtmlMeta = throw NotImplementedError("iOS metadata parser not implemented yet")
}
