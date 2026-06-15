rootProject.name = "MenuAdmin"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":adminApp")
include(":shared")
// The mobile app (Android + iOS, no web target) is optional for web-only builds — notably the Docker
// image, which copies just :shared and :adminApp. Include it only when its sources are present so the
// admin web can build without the consumerApp directory (and without the Android SDK).
if (java.io.File(settingsDir, "consumerApp").isDirectory) {
    include(":consumerApp")
}
