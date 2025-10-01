package net.ifmain.pinny.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import net.ifmain.pinny.presentation.home.BookmarkListItem
import net.ifmain.pinny.presentation.theme.PinnyEmptyStateGradientStops
import net.ifmain.pinny.presentation.theme.corners
import net.ifmain.pinny.presentation.theme.elevations
import net.ifmain.pinny.presentation.theme.spacing

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
    var tags by rememberSaveable { mutableStateOf(initial?.tags?.joinToString(", ").orEmpty()) }

    val spacing = MaterialTheme.spacing
    val isEditing = initial != null
    val actionLabel = if (isEditing) "업데이트" else "저장"
    val subtitle = if (isEditing) "내용을 다듬고 저장하면 목록에 바로 반영돼요." else "URL을 붙여넣고 필요한 메모와 태그를 추가해보세요."
    val headerBrush = Brush.linearGradient(
        colorStops = PinnyEmptyStateGradientStops.mapIndexed { index, pair ->
            val (stop, color) = pair
            val tinted = if (index == 0) color else color.copy(alpha = 0.85f)
            stop to tinted
        }.toTypedArray()
    )
    val currentTags = remember(tags) { parseTags(tags) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.lg, vertical = spacing.xl)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(MaterialTheme.corners.card),
            tonalElevation = MaterialTheme.elevations.level1,
        ) {
            Column(Modifier.padding(spacing.lg)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(MaterialTheme.corners.card))
                        .background(headerBrush)
                        .padding(vertical = spacing.md, horizontal = spacing.lg)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        Text(
                            text = if (isEditing) "북마크 수정" else "새 북마크",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = subtitle,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                initial?.let { item ->
                    Spacer(Modifier.height(spacing.md))
                    BookmarkSheetPreview(item)
                }

                Spacer(Modifier.height(spacing.lg))

                SheetField(
                    label = "URL",
                    required = true,
                    value = url,
                    onValueChange = { url = it },
                    placeholder = "https://bof.ifmain.net",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next,
                        showKeyboardOnFocus = true
                    )
                )

                Spacer(Modifier.height(spacing.sm))

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

                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    SheetField(
                        label = "카테고리",
                        value = category,
                        onValueChange = { category = it },
                        placeholder = "예: 읽을거리",
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
                        value = tags,
                        onValueChange = { tags = it },
                        placeholder = "예: 개발, 취미",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done,
                            showKeyboardOnFocus = true
                        )
                    )
                }

                if (currentTags.isNotEmpty()) {
                    Spacer(Modifier.height(spacing.sm))
                    Text(
                        text = "현재 태그",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(spacing.xs))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(spacing.xs)
                    ) {
                        currentTags.forEach { tag ->
                            AssistChip(
                                onClick = {},
                                label = { Text(tag) },
                                leadingIcon = null,
                                shape = RoundedCornerShape(MaterialTheme.corners.card),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(spacing.lg))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("취소")
                    }

                    Button(
                        enabled = url.isNotBlank(),
                        onClick = {
                            onSave(
                                url.trim(),
                                note.takeIf { it.isNotBlank() },
                                category.takeIf { it.isNotBlank() },
                                parseTags(tags),
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(actionLabel)
                    }
                }
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
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
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
            Spacer(Modifier.height(MaterialTheme.spacing.xs))
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
            horizontalArrangement = Arrangement.spacedBy(spacing.md)
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
