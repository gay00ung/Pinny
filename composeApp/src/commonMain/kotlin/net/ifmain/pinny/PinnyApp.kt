package net.ifmain.pinny

import androidx.compose.material3.*
import androidx.compose.runtime.*
import net.ifmain.pinny.presentation.home.*
import net.ifmain.pinny.presentation.theme.*
import org.jetbrains.compose.ui.tooling.preview.*

@Composable
fun PinnyApp(onOpenUrl: (String) -> Unit = {}) {
    PinnyTheme {
        HomeRoute(onOpenUrl = onOpenUrl)
    }
}

@Preview(showBackground = true)
@Composable
private fun PinnyAppPreview() {
    val sample = BookmarkListItem(
        id = "id",
        title = "Pinny Design System",
        url = "https://pinny.app",
        domain = "pinny.app",
        note = "다음에 읽을 UX 케이스",
        tags = listOf("UX", "리서치"),
        category = "읽을거리",
        thumbnailUrl = null,
        isArchived = false,
        updatedAt = 0L,
    )
    PinnyTheme {
        HomeScreen(
            state = HomeState(isLoading = false, items = listOf(sample)),
            onIntent = {},
            snackbarHostState = SnackbarHostState(),
        )
    }
}
