package net.ifmain.pinny.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.ifmain.pinny.presentation.home.BookmarkListItem
import net.ifmain.pinny.presentation.theme.corners
import net.ifmain.pinny.presentation.theme.elevations
import net.ifmain.pinny.presentation.theme.spacing

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
                ),
                border = BorderStroke(0.dp, Color.Transparent)
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
                ),
                border = BorderStroke(0.dp, Color.Transparent)
            )
        }
    }
}
