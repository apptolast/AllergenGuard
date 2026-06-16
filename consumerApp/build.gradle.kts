import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

// Consumer app (ex apptolast/Menus-Front): MOBILE (Android + iOS). Reuses :shared (data + domain).
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinx.serialization)
    // Firebase Analytics + Crashlytics on Android (auto-initialised from google-services.json).
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
}

// local.properties → GOOGLE_WEB_CLIENT_ID for Google Sign-In (web OAuth client, client_type 3).
val localProperties: Properties by lazy {
    Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }
}

// keystore.properties (gitignored) → release signing. Absent on dev machines / CI without secrets,
// in which case release builds run unsigned so `assembleRelease` still works without credentials.
val keystoreProperties: Properties by lazy {
    Properties().apply {
        val file = rootProject.file("keystore.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ConsumerApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
            // Google Sign-In via Credential Manager.
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.googleid)
        }
        commonMain.dependencies {
            implementation(projects.shared)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "com.apptolast.menufrontend.resources"
    generateResClass = always
}

android {
    namespace = "com.apptolast.menufrontend"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.apptolast.menufrontend"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        // Tag-driven release: fastlane injects -PappVersionCode / -PappVersionName; defaults for dev.
        versionCode = (project.findProperty("appVersionCode") as String?)?.toInt() ?: 1
        versionName = (project.findProperty("appVersionName") as String?) ?: "0.1.0"

        // Web OAuth client id (client_type 3) used as Credential Manager serverClientId. Empty when
        // not set → AndroidSocialAuthClient reports Google unavailable and hides the button.
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${localProperties.getProperty("GOOGLE_WEB_CLIENT_ID", "")}\"",
        )
    }
    signingConfigs {
        create("release") {
            val storeFilePath = keystoreProperties.getProperty("storeFile")
            if (!storeFilePath.isNullOrBlank() && file(storeFilePath).exists()) {
                storeFile = file(storeFilePath)
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            // Only sign when a keystore is configured; otherwise leave the build unsigned.
            if (signingConfigs.getByName("release").storeFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}
