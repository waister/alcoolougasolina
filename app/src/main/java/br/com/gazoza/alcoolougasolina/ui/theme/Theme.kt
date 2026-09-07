package br.com.gazoza.alcoolougasolina.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
    darkColorScheme(
        primary = GreenPrimary,
        onPrimary = TextPrimary,
        primaryContainer = GreenLight,
        onPrimaryContainer = TextPrimary,
        secondary = GreenLight,
        onSecondary = TextPrimary,
        background = DarkBackground,
        onBackground = TextPrimary,
        surface = DarkSurface,
        onSurface = TextPrimary,
        surfaceVariant = DarkCard,
        onSurfaceVariant = TextSecondary,
        outline = DividerColor,
        error = StatusRed,
        onError = TextPrimary,
    )

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content,
    )
}
