package net.ifmain.pinny.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import net.ifmain.pinny.presentation.home.*
import net.ifmain.pinny.presentation.theme.*

/**
 * 드롭인 교체 가능한 AddEditSheet (디자인 업그레이드)
 * - 헤더 톤 다운 + 라운드 업
 * - URL 필드: 붙여넣기/Paste, 유효성(✓/!) 표시
 * - 태그: 삭제 가능한 칩 + (옵션) 추천칩 훅
 * - 버튼 영역 시각 정리 (Sticky는 아님; 외부 시트에 따라 높이 보정)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditSheet(
    initial: BookmarkListItem? = null,
    onSave: (url: String, note: String?, category: String?, tags: List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    var url by rememberSaveable { mutableStateOf(initial?.url.orEmpty()) }
    var note by rememberSaveable { mutableStateOf(initial?.note.orEmpty()) }
    var category by rememberSaveable { mutableStateOf(initial?.category.orEmpty()) }
    var tagsText by rememberSaveable { mutableStateOf(initial?.tags?.joinToString(", ").orEmpty()) }

    val spacing = MaterialTheme.spacing
    val isEditing = initial != null
    val actionLabel = if (isEditing) "업데이트" else "저장"
    val subtitle = if (isEditing) "내용을 다듬고 저장하면 목록에 바로 반영돼요." else "URL을 붙여넣고 필요한 메모와 태그를 추가해보세요."
    val currentTags = remember(tagsText) { parseTags(tagsText) }
    val clipboard = LocalClipboardManager.current

    val isUrlError = remember(url) { url.isNotBlank() && !(url.matches(Regex("https?://.*")) || url.matches(Regex("http?://.*"))) }

    val headerBrush = Brush.linearGradient(
        colorStops = PinnyEmptyStateGradientStops.mapIndexed { idx, (stop, color) ->
            val tinted = if (idx == 0) color.copy(alpha = 0.22f) else color.copy(alpha = 0.18f)
            stop to tinted
        }.toTypedArray()
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.lg, vertical = spacing.xl)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(MaterialTheme.corners.card + 4.dp),
            tonalElevation = MaterialTheme.elevations.level1,
        ) {
            Column(Modifier.padding(spacing.lg)) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(MaterialTheme.corners.card + 4.dp))
                        .background(headerBrush)
                        .padding(vertical = spacing.md, horizontal = spacing.lg)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.35f))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Bookmarks,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isEditing) "북마크 수정" else "새 북마크",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = subtitle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // 미리보기(있으면)
                initial?.let { item ->
                    Spacer(Modifier.height(spacing.md))
                    BookmarkSheetPreview(item)
                }

                Spacer(Modifier.height(spacing.lg))

                // URL 필드 (붙여넣기 + 유효성)
                UrlField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = "https://bof.ifmain.net",
                    isError = isUrlError,
                    onPaste = {
                        clipboard.getText()?.text?.let { pasted ->
                            url = pasted.trim()
                        }
                    }
                )

                Spacer(Modifier.height(spacing.sm))

                // 노트
                SheetField(
                    label = "노트",
                    value = note,
                    onValueChange = { note = it },
                    placeholder = "핵심 메모나 저장 이유를 적어보세요",
                    singleLine = false,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        showKeyboardOnFocus = true
                    )
                )

                Spacer(Modifier.height(spacing.sm))

                // 카테고리 + 태그
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    SheetField(
                        label = "카테고리",
                        value = category,
                        onValueChange = { category = it },
                        placeholder = "예: 쇼핑",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            showKeyboardOnFocus = true
                        )
                    )

                    SheetField(
                        label = "태그",
                        supportingText = "쉼표(,)로 구분하면 여러 개를 추가할 수 있어요!",
                        value = tagsText,
                        onValueChange = { tagsText = it },
                        placeholder = "예: 개발, 취미",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done,
                            showKeyboardOnFocus = true
                        )
                    )
                }

                // 태그 칩
                if (currentTags.isNotEmpty()) {
                    Spacer(Modifier.height(spacing.sm))
                    Text(
                        text = "현재 태그",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(spacing.xs))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(spacing.xs)
                    ) {
                        currentTags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(MaterialTheme.corners.card),
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        tag,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            val new = currentTags.filterNot { it == tag }
                                            tagsText = new.joinToString(", ")
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Close,
                                            contentDescription = "태그 제거",
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(spacing.lg))

                // 액션
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(MaterialTheme.corners.card)
                    ) { Text("취소") }

                    Button(
                        enabled = url.isNotBlank() && !isUrlError,
                        onClick = {
                            onSave(
                                url.trim(),
                                note.takeIf { it.isNotBlank() },
                                category.takeIf { it.isNotBlank() },
                                currentTags
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(MaterialTheme.corners.card)
                    ) { Text(actionLabel) }
                }
            }
        }

        // 바닥 여유 (시트 라운드와 스와이프 공간)
        Spacer(Modifier.height(spacing.md))
    }
}

@Composable
private fun UrlField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean,
    onPaste: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "URL",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "*",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(Modifier.height(spacing.xs))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next,
                showKeyboardOnFocus = true
            ),
            leadingIcon = {
                Icon(Icons.Outlined.Link, contentDescription = null)
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedVisibility(value.isNotBlank()) {
                        Icon(
                            imageVector = if (isError) Icons.Outlined.Error else Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    AssistChip(
                        onClick = onPaste,
                        label = { Text("붙여넣기") },
                        shape = RoundedCornerShape(12.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            leadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledContainerColor = Color.Transparent
                        ),
                        border = BorderStroke(0.dp, Color.Transparent)
                    )

                    Spacer(Modifier.width(6.dp))
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
            ),
            isError = isError,
            shape = RoundedCornerShape(MaterialTheme.corners.card)
        )
        AnimatedVisibility(isError) {
            Column {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "올바른 URL을 입력해 주세요",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun SheetField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    singleLine: Boolean = true,
    required: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val spacing = MaterialTheme.spacing
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (required) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "*",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        Spacer(Modifier.height(spacing.xs))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder?.let {
                {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            keyboardOptions = keyboardOptions,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
            ),
            shape = RoundedCornerShape(MaterialTheme.corners.card)
        )
        if (!supportingText.isNullOrBlank()) {
            Spacer(Modifier.height(spacing.xs))
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BookmarkSheetPreview(item: BookmarkListItem) {
    val spacing = MaterialTheme.spacing
    Surface(
        shape = RoundedCornerShape(MaterialTheme.corners.card),
        tonalElevation = MaterialTheme.elevations.level1,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(MaterialTheme.corners.card))
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.title.firstOrNull()?.uppercase() ?: "P",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.domain,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun parseTags(raw: String): List<String> = raw
    .split(',')
    .map { it.trim() }
    .filter { it.isNotBlank() }
