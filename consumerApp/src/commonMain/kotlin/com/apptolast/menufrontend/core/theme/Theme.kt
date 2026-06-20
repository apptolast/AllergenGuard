package com.apptolast.menufrontend.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColorScheme = lightColorScheme(
    primary = Blue500,
    onPrimary = White,
    primaryContainer = Blue50,
    onPrimaryContainer = TextPrimary,
    secondary = TextSecondary,
    onSecondary = White,
    background = CanvasLight,
    onBackground = TextPrimary,
    surface = White,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    outline = BorderMedium,
    outlineVariant = BorderLight,
    error = DangerRed,
    onError = White,
    errorContainer = DangerRedContainer,
    onErrorContainer = DangerRedDark,
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue500,
    onPrimary = White,
    primaryContainer = Blue600,
    onPrimaryContainer = White,
    background = CanvasDark,
    onBackground = White,
    surface = SurfaceDark,
    onSurface = White,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextTertiary,
    outline = BorderMedium,
    outlineVariant = BorderLight,
    error = DangerRed,
    onError = White,
    errorContainer = DangerRedContainer,
    onErrorContainer = DangerRedDark,
)

@Composable
fun AllergenGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DmSansTypography(),
            content = content,
        )
    }
}
