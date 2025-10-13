package net.ifmain.pinny.work

import android.content.Context

class AndroidMetadataSync(
    private val context: Context
): MetadataSync {
    override fun schedule(id: String, url: String) {
        enqueueFetchMetaWork(context, id, url)
    }
}
