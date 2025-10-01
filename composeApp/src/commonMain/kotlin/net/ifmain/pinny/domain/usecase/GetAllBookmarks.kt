package net.ifmain.pinny.domain.usecase

import kotlinx.coroutines.flow.Flow
import net.ifmain.pinny.domain.model.Bookmark
import net.ifmain.pinny.domain.port.BookmarkRepository

class GetAllBookmarks(private val repo: BookmarkRepository) {
    operator fun invoke(): Flow<List<Bookmark>> = repo.all()
}
