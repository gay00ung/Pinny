package net.ifmain.pinny.domain.usecase

import net.ifmain.pinny.domain.model.*
import net.ifmain.pinny.domain.port.*
import kotlin.uuid.*

class AddBookmark(private val repo: BookmarkRepository, private val clock: () -> Long) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        url: String,
        note: String?,
        category: String?,
        tags: List<String>
    ): Bookmark {
        val now = clock()
        val bookmark = Bookmark(
            id = Uuid.random().toString(),
            url = url,
            title = null,
            description = note,
            thumbnailUrl = null,
            category = category,
            tags = tags,
            createdAt = now,
            updatedAt = now,
            isArchived = false
        )
        repo.upsert(bookmark)
        return bookmark
    }
}
