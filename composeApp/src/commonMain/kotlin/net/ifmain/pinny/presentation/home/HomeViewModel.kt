package net.ifmain.pinny.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.ifmain.pinny.domain.usecase.AddBookmark
import net.ifmain.pinny.domain.usecase.ArchiveBookmark
import net.ifmain.pinny.domain.usecase.DeleteBookmark
import net.ifmain.pinny.domain.usecase.GetAllBookmarks
import net.ifmain.pinny.domain.usecase.SearchBookmarks
import net.ifmain.pinny.work.MetadataSync

class HomeViewModel(
    private val getAllBookmarks: GetAllBookmarks,
    private val searchBookmarks: SearchBookmarks,
    private val addBookmark: AddBookmark,
    private val archiveBookmark: ArchiveBookmark,
    private val deleteBookmark: DeleteBookmark,
    private val metadataSync: MetadataSync,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val refresh = MutableSharedFlow<Unit>(replay = 1)

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>(extraBufferCapacity = 1)
    val effect = _effect.asSharedFlow()

    init {
        refresh.tryEmit(Unit)
        observeBookmarks()
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.QueryChanged -> onQueryChanged(intent.value)
            HomeIntent.SubmitSearch -> onSubmitSearch()
            HomeIntent.ClearSearch -> clearSearch()
            is HomeIntent.Open -> openBookmark(intent.bookmarkId)
            is HomeIntent.Add -> addBookmark(intent)
            is HomeIntent.ToggleArchive -> toggleArchive(intent.id, intent.archived)
            is HomeIntent.UndoArchive -> undoArchive(intent)
            is HomeIntent.Delete -> handleDelete(intent.id)
            HomeIntent.ShowAddSheet -> _state.update { it.copy(isAddSheetVisible = true) }
            HomeIntent.HideAddSheet -> _state.update { it.copy(isAddSheetVisible = false) }
            HomeIntent.Refresh -> refresh.tryEmit(Unit)
            HomeIntent.DismissUndo -> _state.update { it.copy(undoRequest = null) }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeBookmarks() {
        viewModelScope.launch {
            combine(
                query.debounce(200),
                refresh
            ) { q, _ -> q.trim() }
                .flatMapLatest { keyword ->
                    _state.update { it.copy(isLoading = true, query = keyword) }
                    if (keyword.isBlank()) {
                        getAllBookmarks()
                    } else {
                        searchBookmarks(keyword)
                    }
                }
                .map { bookmarks ->
                    bookmarks
                        .map { it.toListItem() }
                        .sortedByDescending { it.updatedAt }
                }
                .catch { throwable ->
                    _state.update { it.copy(isLoading = false) }
                    _effect.emit(HomeEffect.Snackbar(throwable.message ?: "알 수 없는 오류가 발생했어요"))
                }
                .collect { list ->
                    val currentQuery = query.value
                    val items = if (currentQuery.isBlank()) {
                        list.filterNot { it.isArchived }
                    } else {
                        list
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            items = items,
                        )
                    }
                }
        }
    }

    private fun onQueryChanged(value: String) {
        query.value = value
    }

    private fun onSubmitSearch() {
        refresh.tryEmit(Unit)
    }

    private fun clearSearch() {
        query.value = ""
        refresh.tryEmit(Unit)
    }

    private fun openBookmark(id: String) {
        val url = state.value.items.firstOrNull { it.id == id }?.url ?: return
        viewModelScope.launch { _effect.emit(HomeEffect.OpenUrl(url)) }
    }

    private fun addBookmark(intent: HomeIntent.Add) {
        viewModelScope.launch {
            runCatching {
                addBookmark(intent.url, intent.note, intent.category, intent.tags)
            }.onSuccess { bookmark ->
                metadataSync.schedule(bookmark.id, bookmark.url)
                _state.update { it.copy(isAddSheetVisible = false) }
                _effect.emit(HomeEffect.Snackbar("저장했어요! 메타데이터는 곧 업데이트돼요."))
            }.onFailure { throwable ->
                _effect.emit(HomeEffect.Snackbar(throwable.message ?: "저장에 실패했어요"))
            }
        }
    }

    private fun toggleArchive(id: String, archived: Boolean) {
        val previous = state.value.items.firstOrNull { it.id == id }?.isArchived ?: false
        _state.update { it.copy(undoRequest = UndoArchiveRequest(id, previous, archived)) }
        viewModelScope.launch {
            runCatching { archiveBookmark(id, archived) }
                .onFailure { throwable ->
                    _effect.emit(HomeEffect.Snackbar(throwable.message ?: "작업을 완료하지 못했어요"))
                }
        }
    }

    private fun undoArchive(intent: HomeIntent.UndoArchive) {
        viewModelScope.launch {
            runCatching { archiveBookmark(intent.id, intent.previousArchived) }
                .onFailure { throwable ->
                    _effect.emit(HomeEffect.Snackbar(throwable.message ?: "되돌리기에 실패했어요"))
                }
        }
        _state.update { it.copy(undoRequest = null) }
    }

    private fun handleDelete(id: String) {
        viewModelScope.launch {
            runCatching {
                // TODO: 삭제 전에 다이얼로그 한번 더 띄우기
                deleteBookmark(id)
            }.onSuccess {
                _effect.emit(HomeEffect.Snackbar("삭제했어요"))
            }.onFailure { throwable ->
                _effect.emit(HomeEffect.Snackbar(throwable.message ?: "삭제에 실패했어요"))
            }
        }
    }
}
