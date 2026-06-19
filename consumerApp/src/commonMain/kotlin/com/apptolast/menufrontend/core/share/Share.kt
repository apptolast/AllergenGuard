package com.apptolast.menufrontend.core.share

import androidx.compose.runtime.Composable

/**
 * Link shared from the app. Points to the public landing page for now; swap for the Play / App Store
 * URLs once the app is published (and later for per-restaurant deep links when those exist).
 */
const val APP_SHARE_URL = "https://albertohidalgo.apptolast.com"

/**
 * Returns a launcher that opens the platform share sheet with the given text.
 * - Android: `ACTION_SEND` chooser.
 * - iOS: `UIActivityViewController`.
 */
@Composable
expect fun rememberShareLauncher(): (String) -> Unit
