package net.ifmain.pinny.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import org.jetbrains.compose.ui.tooling.preview.*

@Composable
fun CustomDialog(
    onDismiss: () -> Unit,
    title: String,
    message: String? = null,
    confirmText: String = "확인",
    onConfirm: () -> Unit,
    dismissText: String = "취소",
    showDismiss: Boolean = true
) {
    val gradient = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.90f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.90f),
        )
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 2.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(bottom = 12.dp)) {
                // Header
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(gradient)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .padding(top = 18.dp, bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }

                // Body
                if (!message.isNullOrBlank()) {
                    Text(
                        message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                    )
                }

                // Actions
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (showDismiss) {
                        TextButton(onClick = onDismiss) { Text(dismissText) }
                        Spacer(Modifier.width(4.dp))
                    }
                    Button(
                        onClick = {
                            onConfirm()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PinnyDialogPreview() {
    var open by remember { mutableStateOf(true) }
    if (open) {
        CustomDialog(
            onDismiss = { open = false },
            title = "정말로 삭제할까요?",
            message = "삭제한 항목은 복구할 수 없어요.",
            onConfirm = { /* Do something */ }
        )
    }
}
