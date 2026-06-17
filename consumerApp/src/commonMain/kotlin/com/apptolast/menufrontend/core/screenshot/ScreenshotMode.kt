package com.apptolast.menufrontend.core.screenshot

import com.apptolast.menufrontend.core.screenshot.ScreenshotMode.enabled
import com.apptolast.menufrontend.core.screenshot.ScreenshotMode.startScreen


/**
 * App Store screenshot mode, activated via the `-screenshotMode` launch argument by the iOS fastlane
 * snapshot UI tests. When enabled, the app skips the login gate and serves in-memory demo data (no
 * network / no Firebase), starting directly on [startScreen] so screenshots can be captured
 * unattended on simulators.
 *
 * Never enabled in normal runs: without the launch argument [enabled] stays false, and the argument
 * is only ever passed by the iOS UI test — so Android and every production build are unaffected.
 */
object ScreenshotMode {
    var enabled: Boolean = false
        private set

    /** One of: login, home, menu, dish, profile. Maps to a start route in Navigation. */
    var startScreen: String = "home"
        private set

    fun activate(startScreen: String?) {
        enabled = true
        if (!startScreen.isNullOrBlank()) this.startScreen = startScreen
    }
}
