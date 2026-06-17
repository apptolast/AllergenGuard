fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## iOS

### ios bootstrap_match

```sh
[bundle exec] fastlane ios bootstrap_match
```

One-time: create/update App Store cert + provisioning profile in the match repo.

### ios match_certificates

```sh
[bundle exec] fastlane ios match_certificates
```

Sync App Store cert + provisioning profile from the match repo (readonly).

### ios build

```sh
[bundle exec] fastlane ios build
```

Build a Release .ipa locally (no upload).

### ios beta

```sh
[bundle exec] fastlane ios beta
```

Build & upload to TestFlight. Pass version_name:'X.Y.Z' to set the marketing version.

### ios release_from_tag

```sh
[bundle exec] fastlane ios release_from_tag
```

CI-only: read the Git tag, increment build number, build, upload to TestFlight.

### ios screenshots

```sh
[bundle exec] fastlane ios screenshots
```

Generate localized App Store screenshots on the simulator. The app's ScreenshotMode skips login and serves demo data;
the UI test launches per screen and shoots. Devices/languages live in the Snapfile. Upload afterwards with
upload_store_assets.

### ios upload_store_assets

```sh
[bundle exec] fastlane ios upload_store_assets
```

Upload App Store metadata + any generated screenshots (no binary, no review submission).

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
