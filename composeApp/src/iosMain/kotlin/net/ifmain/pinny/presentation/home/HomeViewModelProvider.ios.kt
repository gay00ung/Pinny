package net.ifmain.pinny.presentation.home

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject

@Composable
actual fun rememberHomeViewModel(): HomeViewModel = koinInject()
