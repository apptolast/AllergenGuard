package com.apptolast.menufrontend

import androidx.compose.ui.window.ComposeUIViewController
import com.apptolast.menufrontend.core.screenshot.ScreenshotMode
import com.apptolast.menufrontend.di.initKoin
import platform.Foundation.NSProcessInfo

fun initKoinIos() {
    // Fastlane snapshot launches the app with `-screenshotMode -screenshotScreen <name>` to capture
    // App Store screenshots; this bypasses login and serves demo data. No-op in normal runs.
    val args = NSProcessInfo.processInfo.arguments.map { it.toString() }
    if (args.contains("-screenshotMode")) {
        val idx = args.indexOf("-screenshotScreen")
        ScreenshotMode.activate(if (idx >= 0) args.getOrNull(idx + 1) else null)
    }
    initKoin()
}

fun MainViewController() = ComposeUIViewController { App() }
