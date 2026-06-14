# ConsumerApp — iOS app

Xcode wrapper for the consumer mobile app. All UI and logic live in the Kotlin
`:consumerApp` module (Compose Multiplatform, sharing `:shared`); this target only
hosts the Compose `MainViewController()` inside a SwiftUI shell and links the
generated `ConsumerApp.framework`.

> Menus-Front never committed an `.xcodeproj`, so this project is defined
> declaratively in `project.yml` and generated with [XcodeGen]. Only the source
> of truth (`project.yml`, the Swift sources, `Config.xcconfig`, assets) is in git.

## Generate & run

```bash
brew install xcodegen          # once
cd consumerApp/iosApp
xcodegen generate              # produces ConsumerApp.xcodeproj
open ConsumerApp.xcodeproj     # then Run on a simulator/device in Xcode
```

The **Compile Kotlin Framework** build phase runs
`./gradlew :consumerApp:embedAndSignAppleFrameworkForXcode` automatically, so a
plain Xcode Run builds the Kotlin side too. Set your signing `TEAM_ID` in
`Configuration/Config.xcconfig` before running on a physical device.

[XcodeGen]: https://github.com/yonaskolb/XcodeGen
