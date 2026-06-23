package com.apptolast.menufrontend

import androidx.compose.ui.window.ComposeUIViewController
import com.apptolast.menufrontend.core.screenshot.ScreenshotMode
import com.apptolast.menufrontend.di.initKoin
import org.apptolast.menuadmin.data.remote.firebase.FirebaseConfig
import platform.Foundation.NSProcessInfo
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@OptIn(ExperimentalNativeApi::class)
fun initKoinIos() {
    // Select the Firestore database by build type, mirroring Android: a debug binary (Xcode Debug) uses
    // the `debug` database, a release binary (Archive/TestFlight) the production `(default)`. Must run
    // before initKoin (before any Firestore call).
    FirebaseConfig.databaseId = if (Platform.isDebugBinary) "debug" else "(default)"
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
