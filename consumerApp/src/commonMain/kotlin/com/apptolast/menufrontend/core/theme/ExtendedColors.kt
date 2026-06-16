package com.apptolast.menufrontend.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colors beyond Material's [androidx.compose.material3.ColorScheme]: the safe/danger food
 * states and the allergen chip surfaces. Provided per theme through [LocalExtendedColors] so they
 * adapt to dark mode instead of being hardcoded to a single light palette.
 */
@Immutable
data class ExtendedColors(
    val safe: Color,
    val safeContainer: Color,
    val onSafeContainer: Color,
    val safeBorder: Color,
    val danger: Color,
    val dangerContainer: Color,
    val onDangerContainer: Color,
    val warning: Color,
    val chipUnselectedContainer: Color,
    val onChipUnselected: Color,
)

val LightExtendedColors = ExtendedColors(
    safe = Color(0xFF16A34A), // green-600
    safeContainer = SafeGreenLight, // 0xFFF0FDF4
    onSafeContainer = Color(0xFF14532D), // green-900
    safeBorder = SafeGreenBorder, // 0xFF86EFAC
    danger = DangerRedDark, // 0xFFDC2626
    dangerContainer = AllergenActiveBg, // 0xFFFEE2E2
    onDangerContainer = AllergenActiveText, // 0xFFDC2626
    warning = WarningOrange, // 0xFFF59E0B
    chipUnselectedContainer = AllergenInactiveBg, // 0xFFF8FAFC
    onChipUnselected = AllergenInactiveText, // 0xFF64748B
)

val DarkExtendedColors = ExtendedColors(
    safe = Color(0xFF4ADE80), // green-400, pops on dark surfaces
    safeContainer = Color(0xFF12281C), // deep green-tinted surface
    onSafeContainer = Color(0xFFDCFCE7), // green-100, readable on dark green
    safeBorder = Color(0xFF2F6F4A), // muted green border
    danger = Color(0xFFF87171), // red-400
    dangerContainer = Color(0xFF44211F), // deep red-tinted surface
    onDangerContainer = Color(0xFFFECACA), // red-200
    warning = WarningOrange, // amber reads on both themes
    chipUnselectedContainer = Color(0xFF262636), // slightly elevated navy
    onChipUnselected = TextTertiary, // 0xFF94A3B8
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

/** Convenience accessor: `MaterialTheme.extendedColors.safeContainer`. */
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
