package net.ifmain.pinny.work

class NoopMetadataSync: MetadataSync {
    override fun schedule(id: String, url: String) = Unit
}
