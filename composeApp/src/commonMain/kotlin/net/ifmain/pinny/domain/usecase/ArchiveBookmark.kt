package net.ifmain.pinny.domain.usecase

import net.ifmain.pinny.domain.port.BookmarkRepository

class ArchiveBookmark(private val repo: BookmarkRepository) {
    suspend operator fun invoke(id: String, archived: Boolean) = repo.archive(id, archived)
}
