package net.ifmain.pinny.presentation.home

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import net.ifmain.pinny.presentation.components.AddEditSheet
import net.ifmain.pinny.presentation.components.BookmarkCard
import net.ifmain.pinny.presentation.components.CustomDialog
import net.ifmain.pinny.presentation.components.PinnyTopBar
import net.ifmain.pinny.presentation.theme.PinnyEmptyStateGradientStops
import net.ifmain.pinny.presentation.theme.PinnyTheme
import net.ifmain.pinny.presentation.theme.corners
import net.ifmain.pinny.presentation.theme.elevations
import net.ifmain.pinny.presentation.theme.spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HomeRoute(
    onOpenUrl: (String) -> Unit,
    viewModel: HomeViewModel = rememberHomeViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HomeEffect.OpenUrl -> onOpenUrl(effect.url)
                is HomeEffect.Snackbar -> snackbarHostState.showSnackbar(effect.message)
                is HomeEffect.ClipboardSuggest -> snackbarHostState.showSnackbar(effect.url)
            }
        }
    }

    state.undoRequest?.let { undo ->
        LaunchedEffect(undo) {
            val message = if (undo.targetArchived) "보관함으로 이동했어요" else "보관을 해제했어요"
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "되돌리기",
                duration = SnackbarDuration.Short,
                withDismissAction = true,
            )
            when (result) {
                SnackbarResult.ActionPerformed -> viewModel.onIntent(
                    HomeIntent.UndoArchive(
                        undo.id,
                        undo.previousArchived
                    )
                )

                SnackbarResult.Dismissed -> viewModel.onIntent(HomeIntent.DismissUndo)
            }
        }
    }

    HomeScreen(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: HomeViewModel = rememberHomeViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val behavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var deleteTarget by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            PinnyTopBar(
                isSearching = searching,
                searchText = query,
                onSearchTextChange = {
                    query = it; viewModel.onIntent(HomeIntent.QueryChanged(it))
                },
                onSearchToggle = { searching = it },
                onOverflowClick = { /* 메뉴 */ },
                scrollBehavior = behavior
            )
        },
        floatingActionButton = { AddFab { onIntent(HomeIntent.ShowAddSheet) } },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> ShimmerListPlaceholder()
                state.items.isEmpty() -> EmptyState(
                    isQueryBlank = state.query.isBlank(),
                    onAddClick = { onIntent(HomeIntent.ShowAddSheet) }
                )

                else -> BookmarkList(
                    items = state.items,
                    onOpen = { onIntent(HomeIntent.Open(it)) },
                    onArchive = { id, archived ->
                        onIntent(
                            HomeIntent.ToggleArchive(
                                id,
                                archived
                            )
                        )
                    },
                    onDelete = { id ->
                        deleteTarget = id
                    },
                )
            }
        }
    }

    if (state.isAddSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { onIntent(HomeIntent.HideAddSheet) },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(
                topStart = MaterialTheme.corners.sheet,
                topEnd = MaterialTheme.corners.sheet,
            ),
        ) {
            AddEditSheet(
                initial = null,
                onSave = { url, note, category, tags ->
                    onIntent(HomeIntent.Add(url, note, category, tags))
                },
                onDismiss = { onIntent(HomeIntent.HideAddSheet) }
            )
        }
    }

    deleteTarget?.let { id ->
        CustomDialog(
            onDismiss = { deleteTarget = null },
            title = "북마크 삭제",
            message = "정말로 삭제하시겠어요? 이 작업은 되돌릴 수 없어요.",
            confirmText = "삭제",
            onConfirm = {
                onIntent(HomeIntent.Delete(id))
                deleteTarget = null
            },
            dismissText = "취소",
            showDismiss = true
        )
    }
}

@Composable
fun AddFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Icon(Icons.Filled.Add, contentDescription = "북마크 추가")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkList(
    items: List<BookmarkListItem>,
    onOpen: (String) -> Unit,
    onArchive: (id: String, archived: Boolean) -> Unit,
    onDelete: (id: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = MaterialTheme.spacing.sm)
    ) {
        items(items, key = { it.id }) { item ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    when (value) {
                        SwipeToDismissBoxValue.StartToEnd -> {
                            onDelete(item.id)
                            false
                        }

                        SwipeToDismissBoxValue.EndToStart -> {
                            onArchive(item.id, !item.isArchived)
                            false
                        }

                        else -> false
                    }
                }
            )
            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = true,
                enableDismissFromEndToStart = true,
                backgroundContent = {
                    DismissBackground(
                        value = dismissState.targetValue,
                        startIcon = Icons.Outlined.Delete,
                        endIcon = Icons.Filled.Archive,
                    )
                },
            ) {
                BookmarkCard(
                    item = item,
                    onClick = { onOpen(item.id) },
                    onArchive = { onArchive(item.id, !item.isArchived) }
                )
            }
        }
    }
}

@Composable
fun DismissBackground(
    value: SwipeToDismissBoxValue,
    startIcon: ImageVector,
    endIcon: ImageVector,
) {
    val (color, icon) = when (value) {
        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.error to startIcon
        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.tertiary to endIcon
        else -> Color.Transparent to null
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color.copy(alpha = if (color == Color.Transparent) 0f else 0.15f)),
        contentAlignment = when (value) {
            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
            SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
            else -> Alignment.Center
        }
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color)
        }
    }
}

@Composable
fun EmptyState(
    isQueryBlank: Boolean,
    onAddClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val gradient =
            Brush.linearGradient(colorStops = PinnyEmptyStateGradientStops.toTypedArray())
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(brush = gradient, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Bookmarks,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
        Text(
            text = if (isQueryBlank) "먼저 북마크를 추가해보세요" else "검색 결과가 없어요",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Text(
            text = if (isQueryBlank) "공유 시트에서 바로 저장할 수 있어요" else "검색어를 바꿔보세요",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
        Button(onClick = onAddClick) {
            Text("북마크 추가하기")
        }
    }
}

@Composable
fun ShimmerListPlaceholder() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "placeholderAlpha"
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        repeat(5) {
            Box(
                modifier = Modifier
                    .padding(
                        horizontal = MaterialTheme.spacing.lg,
                        vertical = MaterialTheme.spacing.sm
                    )
                    .fillMaxWidth()
                    .height(88.dp)
                    .clip(RoundedCornerShape(MaterialTheme.corners.card))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BookmarkCardPreview() {
    val item = BookmarkListItem(
        id = "1",
        title = "Pinny Design System",
        url = "https://pinny.app",
        domain = "pinny.app",
        note = "서핑하다 저장한 아티클",
        tags = listOf("디자인", "리서치"),
        category = "읽을거리",
        thumbnailUrl = null,
        isArchived = false,
        updatedAt = 0L,
    )
    PinnyTheme {
        BookmarkCard(item = item, onClick = {}, onArchive = {})
    }
}
