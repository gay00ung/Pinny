package net.ifmain.pinny.domain.port

import kotlinx.coroutines.flow.Flow
import net.ifmain.pinny.domain.model.Bookmark

interface BookmarkRepository {
    suspend fun upsert(b: Bookmark)
    suspend fun archive(id: String, archived: Boolean)
    fun all(): Flow<List<Bookmark>>                       // 간단 리스트(플랫폼 공통)
    fun search(keyword: String): Flow<List<Bookmark>>     // FTS 결과(공통)
    suspend fun updateMeta(id: String, title: String?, thumbnailPath: String?)
    suspend fun delete(id: String)
}
