package net.ifmain.pinny.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import coil3.compose.*
import net.ifmain.pinny.presentation.home.*
import net.ifmain.pinny.presentation.theme.*

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
            BookmarkThumbnail(
                title = item.title,
                thumbnailUrl = item.thumbnailUrl
            )
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
fun BookmarkThumbnail(title: String, thumbnailUrl: String? = null) {
    Surface(
        modifier = Modifier
            .width(96.dp)
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(MaterialTheme.corners.card)),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        if (!thumbnailUrl.isNullOrBlank()) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "thumbnail",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(MaterialTheme.corners.card)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
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
