package net.ifmain.pinny.presentation.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.*
import kotlinx.coroutines.flow.*
import net.ifmain.pinny.presentation.theme.*
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
                SnackbarResult.ActionPerformed -> viewModel.onIntent(HomeIntent.UndoArchive(undo.id, undo.previousArchived))
                SnackbarResult.Dismissed -> viewModel.onIntent(HomeIntent.DismissUndo)
            }
        }
    }

    HomeScreen(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = { PinnyTopBar() },
        floatingActionButton = { AddFab { onIntent(HomeIntent.ShowAddSheet) } },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                value = state.query,
                onValueChange = { onIntent(HomeIntent.QueryChanged(it)) },
                onSearch = { onIntent(HomeIntent.SubmitSearch) },
                onClear = { onIntent(HomeIntent.ClearSearch) },
            )
            when {
                state.isLoading -> ShimmerListPlaceholder()
                state.items.isEmpty() -> EmptyState(
                    isQueryBlank = state.query.isBlank(),
                    onAddClick = { onIntent(HomeIntent.ShowAddSheet) }
                )
                else -> BookmarkList(
                    items = state.items,
                    onOpen = { onIntent(HomeIntent.Open(it)) },
                    onArchive = { id, archived -> onIntent(HomeIntent.ToggleArchive(id, archived)) },
                    onDelete = { id -> onIntent(HomeIntent.Delete(id)) },
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinnyTopBar() {
    TopAppBar(
        title = { Text("Pinny") },
        actions = {
            IconButton(onClick = { /* TODO: overflow actions */ }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "옵션")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        )
    )
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

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("링크, 노트, 태그 검색") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (value.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Filled.Close, contentDescription = "검색어 지우기")
                }
            }
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.sm),
        shape = RoundedCornerShape(MaterialTheme.corners.card),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        )
    )
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
fun BookmarkCard(
    item: BookmarkListItem,
    onClick: () -> Unit,
    onArchive: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.sm)
            .fillMaxWidth(),
        shape = RoundedCornerShape(MaterialTheme.corners.card),
        tonalElevation = MaterialTheme.elevations.level1,
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(MaterialTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookmarkThumbnail(title = item.title)
            Spacer(Modifier.width(MaterialTheme.spacing.lg))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.domain,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                item.note?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(MaterialTheme.spacing.xs))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (item.tags.isNotEmpty()) {
                    Spacer(Modifier.height(MaterialTheme.spacing.sm))
                    TagRow(tags = item.tags)
                }
            }
            IconButton(onClick = onArchive) {
                Icon(Icons.Filled.Archive, contentDescription = "보관")
            }
        }
    }
}

@Composable
fun BookmarkThumbnail(title: String) {
    Box(
        modifier = Modifier
            .width(96.dp)
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(MaterialTheme.corners.card))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title.take(1).uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun TagRow(tags: List<String>) {
    val visible = tags.take(2)
    val overflow = tags.size - visible.size
    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
        visible.forEach { tag ->
            AssistChip(
                onClick = {},
                label = { Text(tag) },
                shape = RoundedCornerShape(999.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            )
        }
        if (overflow > 0) {
            AssistChip(
                onClick = {},
                label = { Text("+${overflow}") },
                shape = RoundedCornerShape(999.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )
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
        val gradient = Brush.linearGradient(colorStops = PinnyEmptyStateGradientStops.toTypedArray())
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(brush = gradient, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Bookmarks, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
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
                    .padding(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.sm)
                    .fillMaxWidth()
                    .height(88.dp)
                    .clip(RoundedCornerShape(MaterialTheme.corners.card))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
            )
        }
    }
}

@Composable
fun AddEditSheet(
    initial: BookmarkListItem? = null,
    onSave: (url: String, note: String?, category: String?, tags: List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    var url by rememberSaveable { mutableStateOf(initial?.url.orEmpty()) }
    var note by rememberSaveable { mutableStateOf(initial?.note.orEmpty()) }
    var category by rememberSaveable { mutableStateOf(initial?.category.orEmpty()) }
    var tags by rememberSaveable { mutableStateOf(initial?.tags?.joinToString(", ").orEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.xl, vertical = MaterialTheme.spacing.lg)
    ) {
        Text(
            text = if (initial == null) "새 북마크" else "북마크 수정",
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
        TextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            )
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        TextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("메모") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            )
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
            TextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("카테고리") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                )
            )
            TextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("태그(쉼표)") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                )
            )
        }
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onDismiss) { Text("취소") }
            Spacer(Modifier.width(MaterialTheme.spacing.sm))
            Button(
                enabled = url.isNotBlank(),
                onClick = {
                    onSave(
                        url.trim(),
                        note.takeIf { it.isNotBlank() },
                        category.takeIf { it.isNotBlank() },
                        parseTags(tags),
                    )
                }
            ) {
                Text("저장")
            }
        }
    }
}

private fun parseTags(raw: String): List<String> = raw
    .split(',')
    .map { it.trim() }
    .filter { it.isNotBlank() }

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
