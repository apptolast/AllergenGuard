import XCTest

/// Captures App Store screenshots via fastlane snapshot.
///
/// The app does the work: launched with `-screenshotMode -screenshotScreen <name>` it skips login,
/// serves in-memory demo data (no network/Firebase) and starts directly on the requested screen
/// (see ScreenshotMode.kt / ScreenshotDemoData.kt in :consumerApp). This test just relaunches the
/// app per screen and shoots — no in-app navigation, so no flakiness.
@MainActor
final class ConsumerAppScreenshots: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func testCaptureScreenshots() {
        let screens = ["login", "home", "menu", "dish", "profile"]
        for (index, screen) in screens.enumerated() {
            let app = XCUIApplication()
            setupSnapshot(app)
            app.launchArguments += ["-screenshotMode", "-screenshotScreen", screen]
            app.launch()
            // Give Compose + the demo data (and remote food thumbnails) time to settle before shooting.
            sleep(8)
            snapshot(String(format: "%02d-%@", index + 1, screen))
            app.terminate()
        }
    }
}
