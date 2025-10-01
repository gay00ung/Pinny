package net.ifmain.pinny.domain.usecase

import net.ifmain.pinny.domain.port.BookmarkRepository

class DeleteBookmark(private val repo: BookmarkRepository) {
    suspend operator fun invoke(id: String) {
        repo.delete(id)
    }
}
