package net.ifmain.pinny.presentation.home

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
actual fun rememberHomeViewModel(): HomeViewModel = koinViewModel()
