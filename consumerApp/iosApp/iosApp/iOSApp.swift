import SwiftUI
import ConsumerApp
import FirebaseCore

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        MainViewControllerKt.doInitKoinIos()
        // Install the Kotlin↔Swift bridge for Sign in with Apple.
        SocialAuthCoordinator.shared.registerBridges()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}