import SwiftUI
import ConsumerApp

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}