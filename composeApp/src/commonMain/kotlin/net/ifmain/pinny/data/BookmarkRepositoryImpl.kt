package net.ifmain.pinny.data

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import bookmark.Bookmarks
import bookmark.BookmarksQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.ifmain.pinny.domain.model.Bookmark
import net.ifmain.pinny.domain.port.BookmarkRepository

class BookmarkRepositoryImpl(
    private val queries: BookmarksQueries,
    private val json: Json,
    private val platform: PlatformCapabilities
) : BookmarkRepository {

    override suspend fun upsert(b: Bookmark) {
        queries.insertBookmark(
            id = b.id,
            url = b.url,
            title = b.title,
            description = b.description,
            thumbnailUrl = b.thumbnailUrl,
            category = b.category,
            tagsJson = json.encodeToString(b.tags),
            createdAt = b.createdAt,
            updatedAt = b.updatedAt,
            isArchived = if (b.isArchived) 1 else 0
        )
    }

    override suspend fun archive(id: String, archived: Boolean) {
        queries.updateArchived(
            isArchived = if (archived) 1 else 0,
            updatedAt = platform.nowMillis(),
            id = id
        )
    }

    override fun all(): Flow<List<Bookmark>> =
        queries.selectAll().asListFlow().map { rows -> rows.map { it.toDomain(json) } }

    override fun search(keyword: String): Flow<List<Bookmark>> {
        val like = "%$keyword%"
        return queries.searchAllLike(like, like, like, like)
            .asListFlow()
            .map { rows -> rows.map { it.toDomain(json) } }
    }

    override suspend fun updateMeta(id: String, title: String?, thumbnailPath: String?) {
        queries.updateMeta(
            title = title,
            thumbnailUrl = thumbnailPath,
            updatedAt = platform.nowMillis(),
            id = id
        )
    }

    override suspend fun delete(id: String) {
        queries.deleteBookmarkById(id)
    }
}

private fun Bookmarks.toDomain(json: Json): Bookmark = Bookmark(
    id = id,
    url = url,
    title = title,
    description = description,
    thumbnailUrl = thumbnailUrl,
    category = category,
    tags = runCatching { json.decodeFromString<List<String>>(tagsJson) }.getOrDefault(emptyList()),
    createdAt = createdAt,
    updatedAt = updatedAt,
    isArchived = isArchived != 0L
)

private fun <T : Any> Query<T>.asListFlow(): Flow<List<T>> =
    asFlow().mapToList(Dispatchers.Default)
