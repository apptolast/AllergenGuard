fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android version

```sh
[bundle exec] fastlane android version
```

Print the versionName/versionCode currently declared in build.gradle.kts

### android validate_play

```sh
[bundle exec] fastlane android validate_play
```

Sanity-check Play credentials by listing existing versionCodes on each track

### android build

```sh
[bundle exec] fastlane android build
```

Build a signed release AAB. Pass version_code:N version_name:X to override defaults.

### android internal

```sh
[bundle exec] fastlane android internal
```

Build & upload to Internal testing as DRAFT (manual local run).

### android release_from_tag

```sh
[bundle exec] fastlane android release_from_tag
```

CI-only: read the Git tag (GITHUB_REF_NAME), compute versionCode, build, upload as DRAFT.

### android upload_store_assets

```sh
[bundle exec] fastlane android upload_store_assets
```

Upload Play Store listing text + phone screenshots (reused from the iOS snapshot run). No AAB; requires the app's first manual upload to exist. Run `ios screenshots` first.

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
