package com.apptolast.menufrontend.core.theme

import androidx.compose.ui.graphics.Color

// Primary
val Blue500 = Color(0xFF3B82F6)
val Blue600 = Color(0xFF2563EB)
val Blue50 = Color(0xFFEFF6FF)

// Background & Surface
val White = Color(0xFFFFFFFF)
val Gray50 = Color(0xFFF8FAFC)
val Gray100 = Color(0xFFF1F5F9)

// Text
val TextPrimary = Color(0xFF1A1A2E)
val TextSecondary = Color(0xFF64748B)
val TextTertiary = Color(0xFF94A3B8)

// Borders
val BorderLight = Color(0xFFE2E8F0)
val BorderMedium = Color(0xFFCBD5E1)

// Canvas vs surface: cards (surface) sit on a slightly different canvas (background) so they pop.
val CanvasLight = Color(0xFFF1F5F9) // soft gray page background (light)
val SurfaceVariantLight = Color(0xFFEDF1F6) // subtle fills/chips on white cards (light)
val CanvasDark = Color(0xFF12121E) // deep page background (dark)
val SurfaceDark = Color(0xFF20203A) // cards, lighter than the canvas (dark)
val SurfaceVariantDark = Color(0xFF2C2C46) // subtle fills/chips on dark cards (dark)

// Semantic - Allergen Safety
val SafeGreen = Color(0xFF22C55E)
val SafeGreenLight = Color(0xFFF0FDF4)
val SafeGreenBorder = Color(0xFF86EFAC)
val SafeGreenContainer = Color(0xFFDCFCE7)

val WarningOrange = Color(0xFFF59E0B)
val WarningOrangeLight = Color(0xFFFFFBEB)

val DangerRed = Color(0xFFEF4444)
val DangerRedDark = Color(0xFFDC2626)
val DangerRedLight = Color(0xFFFEF2F2)
val DangerRedContainer = Color(0xFFFEE2E2)

// Allergen chip states
val AllergenActiveBg = Color(0xFFFEE2E2)
val AllergenActiveText = Color(0xFFDC2626)
val AllergenInactiveBg = Color(0xFFF8FAFC)
val AllergenInactiveText = Color(0xFF64748B)
