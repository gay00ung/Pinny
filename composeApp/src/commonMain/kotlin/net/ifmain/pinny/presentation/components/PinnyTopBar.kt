package net.ifmain.pinny.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import net.ifmain.pinny.resources.decodeImageBitmap
import net.ifmain.pinny.resources.readResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinnyTopBar(
    modifier: Modifier = Modifier,
    title: String = "Pinny",
    isSearching: Boolean,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearchToggle: (Boolean) -> Unit,
    onAddClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onBackFromSearch: () -> Unit = { onSearchToggle(false) },
    scrollBehavior: TopAppBarScrollBehavior? = TopAppBarDefaults.pinnedScrollBehavior()

) {
    val infinite = rememberInfiniteTransition(label = "grad")
    val offset by infinite.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offset"
    )
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.90f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.90f)
        ),
        start = Offset(0f, offset),
        end = Offset(offset, 0f)
    )

    // 컨테이너: 하단 라운드 + 음영
    Box(
        modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp), clip = false)
            .background(gradient)
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // 검색/기본 모드 전환
        AnimatedContent(
            targetState = isSearching,
            transitionSpec = {
                fadeIn(tween(150)) + slideInVertically { it / 2 } togetherWith
                    fadeOut(tween(120)) + slideOutVertically { -it / 3 }
            },
            label = "topbarContent"
        ) { searching ->
            if (searching) {
                SearchRow(
                    text = searchText,
                    onTextChange = onSearchTextChange,
                    onBack = onBackFromSearch,
                    onClear = { onSearchTextChange("") }
                )
            } else {
                DefaultRow(
                    title = title,
                    onSearch = { onSearchToggle(true) },
                    onAdd = onAddClick,
                    onOverflow = onOverflowClick
                )
            }
        }
    }

    // 스크롤 거동 전달(Scaffold에 nestedScroll 연결해서 사용)
    scrollBehavior?.let {
        Spacer(
            Modifier
                .fillMaxWidth()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        )
    }

}

@Composable
private fun DefaultRow(
    title: String,
    onSearch: () -> Unit,
    onAdd: () -> Unit,
    onOverflow: () -> Unit
) {
    var bmp by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(Unit) {
        runCatching { readResource("pinny_app_icon.png") }
            .onSuccess { bytes -> runCatching { decodeImageBitmap(bytes) }.onSuccess { bmp = it } }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 좌측 로고 자리(원형 아이콘 배지)
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            bmp?.let {
                Image(
                    bitmap = it,
                    contentDescription = "Pinny",
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Search
        SmallIconButton(icon = Icons.Filled.Search, contentDesc = "검색", onClick = onSearch)

        Spacer(Modifier.width(15.dp))

        // Add
        SmallIconButton(icon = Icons.Filled.Add, contentDesc = "추가", onClick = onAdd)
        
        Spacer(Modifier.width(15.dp))

        // Overflow
        SmallIconButton(icon = Icons.Filled.MoreVert, contentDesc = "옵션", onClick = onOverflow)
    }
}

@Preview
@Composable
private fun DefaultRowPreview() {
    DefaultRow(title = "Pinny", onSearch = {}, onAdd = {}, onOverflow = {})
}

/* ===== 검색 행(필 형태) ===== */
@Composable
private fun SearchRow(
    text: String,
    onTextChange: (String) -> Unit,
    onBack: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        SmallIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDesc = "뒤로",
            onClick = onBack
        )

        Spacer(Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.92f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(44.dp)
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 6.dp)
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFFAA99FF))
                Spacer(Modifier.width(6.dp))
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    placeholder = { Text("Search links, notes, tags", color = Color(0xFF888888)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.secondary,
                    ),
                    modifier = Modifier.weight(1f)
                )
                AnimatedVisibility(text.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "지우기",
                            tint = Color(0xFFB0AEC6)
                        )
                    }
                }
            }
        }
    }
}

/* ===== 공통: 작은 원형 아이콘 버튼 ===== */
@Composable
private fun SmallIconButton(
    icon: ImageVector,
    contentDesc: String?,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.18f))
    ) {
        Icon(icon, contentDescription = contentDesc, tint = Color.White)
    }
}
