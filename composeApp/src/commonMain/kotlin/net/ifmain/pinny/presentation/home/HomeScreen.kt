package net.ifmain.pinny.presentation.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.*
import kotlinx.coroutines.flow.*
import net.ifmain.pinny.presentation.components.*
import net.ifmain.pinny.presentation.theme.*
import org.jetbrains.compose.ui.tooling.preview.*

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
                    query = it
                    viewModel.onIntent(HomeIntent.QueryChanged(it))
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
                    onRefresh = { onIntent(HomeIntent.Refresh) },
                    isRefreshing = state.isRefreshing,
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
    onRefresh: () -> Unit,
    isRefreshing: Boolean = false,
) {
    val pullState = rememberPullToRefreshState()

    PullToRefreshBox(
        onRefresh = onRefresh,
        isRefreshing = isRefreshing,
        modifier = Modifier.fillMaxSize(),
        state = pullState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullState,
                isRefreshing = isRefreshing,
                containerColor = MaterialTheme.colorScheme.surface,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
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
