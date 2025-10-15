package net.ifmain.pinny.presentation.home

import net.ifmain.pinny.domain.model.Bookmark

/** Pinny 홈 화면이 다루는 고정 상태 모델. */
data class HomeState(
    val query: String = "",
    val isLoading: Boolean = true,
    val items: List<BookmarkListItem> = emptyList(),
    val isAddSheetVisible: Boolean = false,
    val isOffline: Boolean = false,
    val undoRequest: UndoArchiveRequest? = null,
    val isRefreshing: Boolean = false,
)

sealed interface HomeIntent {
    data class QueryChanged(val value: String) : HomeIntent
    data object SubmitSearch : HomeIntent
    data object ClearSearch : HomeIntent
    data class Open(val bookmarkId: String) : HomeIntent
    data class Add(
        val url: String,
        val note: String?,
        val category: String?,
        val tags: List<String>,
    ) : HomeIntent
    data class ToggleArchive(val id: String, val archived: Boolean) : HomeIntent
    data class UndoArchive(val id: String, val previousArchived: Boolean) : HomeIntent
    data class Delete(val id: String) : HomeIntent
    data object ShowAddSheet : HomeIntent
    data object HideAddSheet : HomeIntent
    data object Refresh : HomeIntent
    data object DismissUndo : HomeIntent
}

sealed interface HomeEffect {
    data class OpenUrl(val url: String) : HomeEffect
    data class Snackbar(val message: String) : HomeEffect
    data class ClipboardSuggest(val url: String) : HomeEffect
}

data class UndoArchiveRequest(
    val id: String,
    val previousArchived: Boolean,
    val targetArchived: Boolean,
)

data class BookmarkListItem(
    val id: String,
    val title: String,
    val url: String,
    val domain: String,
    val note: String?,
    val tags: List<String>,
    val category: String?,
    val thumbnailUrl: String?,
    val isArchived: Boolean,
    val updatedAt: Long,
)

fun Bookmark.toListItem(): BookmarkListItem {
    val fallbackTitle = title?.takeIf { it.isNotBlank() } ?: prettifyHost(url)
    return BookmarkListItem(
        id = id,
        title = fallbackTitle,
        url = url,
        domain = prettifyHost(url),
        note = description,
        tags = tags,
        category = category,
        thumbnailUrl = thumbnailUrl,
        isArchived = isArchived,
        updatedAt = updatedAt,
    )
}

private val hostRegex = Regex("^(?:[a-zA-Z][a-zA-Z0-9+.-]*://)?([^/?#]+)")

private fun prettifyHost(url: String): String {
    val match = hostRegex.find(url)?.groupValues?.getOrNull(1) ?: url
    return match.removePrefix("www.")
}
