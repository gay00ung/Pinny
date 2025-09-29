package net.ifmain.pinny.domain.usecase

import net.ifmain.pinny.domain.model.Bookmark
import net.ifmain.pinny.domain.port.BookmarkRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AddBookmark(private val repo: BookmarkRepository, private val clock: () -> Long) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(url: String, note: String?, category: String?, tags: List<String>) {
        val now = clock()
        repo.upsert(
            Bookmark(
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
        )
    }
}
