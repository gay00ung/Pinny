package net.ifmain.pinny.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PinnyPink = Color(0xFFFF9EC4)
private val PinnyLavender = Color(0xFFA18CFF)
private val PinnyMint = Color(0xFF7CE5C3)
private val PinnyLightSurface = Color(0xFFFFFFFF)
private val PinnyDarkSurface = Color(0xFF1E1E1E)
private val PinnyLightBackground = Color(0xFFFFF7FA)
private val PinnyDarkBackground = Color(0xFF121212)
private val PinnyError = Color(0xFFFF6B6B)
private val PinnyNeutral = Color(0xFFBDBDBD)

private val LightColorScheme = lightColorScheme(
    primary = PinnyPink,
    onPrimary = Color(0xFF3A0017),
    primaryContainer = Color(0xFFFFC6DE),
    onPrimaryContainer = Color(0xFF280010),
    secondary = PinnyLavender,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE1D6FF),
    onSecondaryContainer = Color(0xFF1D0F44),
    tertiary = PinnyMint,
    onTertiary = Color(0xFF004C3B),
    tertiaryContainer = Color(0xFFE3FFF5),
    onTertiaryContainer = Color(0xFF002018),
    error = PinnyError,
    onError = Color(0xFFFFFFFF),
    background = PinnyLightBackground,
    onBackground = Color(0xFF1E1E1E),
    surface = PinnyLightSurface,
    onSurface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFFF5E9EF),
    onSurfaceVariant = Color(0xFF51454B),
    outline = PinnyNeutral,
)

private val DarkColorScheme = darkColorScheme(
    primary = PinnyPink,
    onPrimary = Color(0xFF2B0012),
    primaryContainer = Color(0xFF680C3A),
    onPrimaryContainer = Color(0xFFFFD9E4),
    secondary = PinnyLavender,
    onSecondary = Color(0xFF1F1146),
    secondaryContainer = Color(0xFF3D2E7D),
    onSecondaryContainer = Color(0xFFE4DFFF),
    tertiary = PinnyMint,
    onTertiary = Color(0xFF00291F),
    tertiaryContainer = Color(0xFF00513B),
    onTertiaryContainer = Color(0xFFC7FFEA),
    error = PinnyError,
    onError = Color(0xFF2D0000),
    background = PinnyDarkBackground,
    onBackground = Color(0xFFF6EDF1),
    surface = PinnyDarkSurface,
    onSurface = Color(0xFFF6EDF1),
    surfaceVariant = Color(0xFF3F3339),
    onSurfaceVariant = Color(0xFFC9BBC2),
    outline = PinnyNeutral,
)

@Composable
fun PinnyTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

/**
 * Gradient suggestion for empty-state icons:
 * Primary stop at 0f (Color(0xFFFF9EC4)) and Secondary stop at 1f (Color(0xFFA18CFF)).
 * Apply to bookmark/pin glyphs to keep the pastel neon tone across light and dark themes.
 */
val PinnyEmptyStateGradientStops = listOf(
    0.0f to PinnyPink,
    1.0f to PinnyLavender,
)
