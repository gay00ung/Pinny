package net.ifmain.pinny.work

interface MetadataSync {
    fun schedule(id: String, url: String)
}
